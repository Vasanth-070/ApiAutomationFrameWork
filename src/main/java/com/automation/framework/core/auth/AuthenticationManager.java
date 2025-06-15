package com.automation.framework.core.auth;

import com.automation.framework.core.config.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AuthenticationManager handles OAuth authentication flow with OTP verification
 * Based on the patterns from the original Cucumber API framework
 */
public class AuthenticationManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);
    
    private final String baseUrl;
    private final HeaderManager headerManager;
    private final TokenStorage tokenStorage;
    private RedisManager redisManager; // Lazy initialization
    private final ApiConfig apiConfig;
    
    public AuthenticationManager(String baseUrl) {
        this.baseUrl = baseUrl;
        this.headerManager = new HeaderManager();
        this.tokenStorage = new TokenStorage();
        this.apiConfig = new ApiConfig();
        // RedisManager will be initialized only when needed
    }
    
    /**
     * Get RedisManager instance only when needed (lazy initialization)
     */
    private RedisManager getRedisManager() {
        if (redisManager == null) {
            redisManager = RedisManager.getInstance();
        }
        return redisManager;
    }
    
    /**
     * Complete authentication flow - generates OTP and performs login
     */
    public AuthResponse authenticate(String loginId, String clientId, String deviceId) {
        try {
            // Step 1: Generate and get OTP (hybrid approach: API trigger + Redis retrieval)
            String otp = generateAndGetOtp(loginId, clientId, deviceId);
            if (otp == null || otp.trim().isEmpty()) {
                logger.error("Failed to retrieve OTP for user: {}", loginId);
                return new AuthResponse(false, "Failed to retrieve OTP", null, null);
            }
            
            // Step 2: Login with OTP
            Response loginResponse = login(loginId, otp, clientId, deviceId);
            if (loginResponse.getStatusCode() == 200) {
                String accessToken = loginResponse.jsonPath().getString("data.access_token");
                String cookie = loginResponse.getCookie("sapphire");
                
                // Store token for future use
                tokenStorage.storeToken(loginId, accessToken, cookie);
                
                logger.info("Authentication successful for user: {}", loginId);
                return new AuthResponse(true, "Authentication successful", accessToken, cookie);
            } else {
                logger.error("Login failed. Status: {}, Response: {}", 
                           loginResponse.getStatusCode(), loginResponse.asString());
                return new AuthResponse(false, "Login failed", null, null);
            }
            
        } catch (Exception e) {
            logger.error("Authentication error for user: {}", loginId, e);
            return new AuthResponse(false, "Authentication error: " + e.getMessage(), null, null);
        }
    }
    
    /**
     * Send OTP to email/phone
     * This generates otp in user selected login channel, i.e, email or phone
     * And stores the value in redis server. we need to extract it from there.
     */
    public Response sendOtp(String loginId, String clientId, String deviceId) {
        Long deviceTime = System.currentTimeMillis();
        
        // Build headers
        Map<String, String> headers = headerManager.getOtpHeaders(clientId, deviceId, deviceTime);
        
        // Generate SHA512 token
        String token = generateSHA512Token(loginId, clientId, deviceId, deviceTime);
        
        // Determine API endpoint
        String apiPath = loginId.contains("@") 
            ? baseUrl + "/api/v4/oauth/login/email/send-otp"
            : baseUrl + "/api/v4/oauth/dual/mobile/send-otp";
            
        // Build form parameters
        Map<String, String> formParams = new HashMap<>();
        formParams.put("token", token);
        formParams.put("sixDigitOTP", "true");
        
        if (loginId.contains("@")) {
            formParams.put("email", loginId);
        } else {
            formParams.put("prefix", "+91");
            formParams.put("phone", loginId);
            formParams.put("resendOnCall", "false");
        }
        
        // Build request
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeaders(headers);
        builder.addFormParams(formParams);
        RequestSpecification requestSpec = builder.build();
        
        logger.debug("Sending OTP to: {} using endpoint: {}", loginId, apiPath);
        
        // Execute request
        Response response = RestAssured.given()
                .config(RestAssured.config().encoderConfig(
                        EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                .contentType("application/x-www-form-urlencoded")
                .spec(requestSpec)
                .post(apiPath);
                
        logger.debug("OTP response: {}", response.asString());
        return response;
    }
    
    /**
     * Login with OTP
     */
    public Response login(String loginId, String otp, String clientId, String deviceId) {
        // Build headers
        Map<String, String> headers = headerManager.getLoginHeaders(clientId, deviceId);
        
        // Build form parameters
        Map<String, String> formParams = new HashMap<>();
        String token = "";
        
        if (loginId.contains("@")) {
            formParams.put("grant_type", "emotp");
            token = Base64.getEncoder().encodeToString((loginId + "~" + otp.trim()).getBytes());
        } else {
            formParams.put("grant_type", "photp");
            token = Base64.getEncoder().encodeToString((loginId + "~" + "+91" + "~" + otp.trim()).getBytes());
        }
        
        formParams.put("token", token);
        formParams.put("sixDigitOTP", "true");
        
        // API endpoint
        String apiPath = baseUrl + "/api/v4/oauth/dual/mobile/verify-otp";
        
        // Build request
        RequestSpecBuilder builder = new RequestSpecBuilder();
        builder.addHeaders(headers);
        builder.addFormParams(formParams);
        RequestSpecification requestSpec = builder.build();
        
        logger.debug("Logging in user: {} with endpoint: {}", loginId, apiPath);
        
        // Execute request
        Response response = RestAssured.given()
                .spec(requestSpec)
                .post(apiPath);
                
        logger.debug("Login response: {}", response.asString());
        return response;
    }
    
    /**
     * Get stored authentication token
     */
    public String getAuthToken(String loginId) {
        return tokenStorage.getAuthHeader(loginId);
    }
    
    /**
     * Get stored cookie
     */
    public String getCookie(String loginId) {
        return tokenStorage.getCookie(loginId);
    }
    
    /**
     * Generate SHA512 hash for authentication token
     */
    private String generateSHA512Token(String loginId, String clientId, String deviceId, Long deviceTime) {
        String message = "";
        
        if (loginId.contains("@")) {
            message = loginId + "~" + clientId + "~" + deviceId + "~" + deviceTime;
        } else {
            message = loginId + "~" + "+91" + "~" + clientId + "~" + deviceId + "~" + deviceTime;
        }
        
        logger.debug("SHA512 message: {}", message);
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(message.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            String token = hexString.toString();
            logger.debug("Generated SHA512 token: {}", token);
            return token;
            
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-512 algorithm not available", e);
            throw new RuntimeException("SHA-512 algorithm not available", e);
        }
    }
    
    /**
     * Generate random device ID
     */
    public static String generateDeviceId() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Get OTP using Redis or fallback to mock value
     * Follows the same hybrid approach as the original framework
     */
    public String getOtp(String loginId) {
        // Check if mock OTP is enabled
        boolean useMockOtp = Boolean.parseBoolean(apiConfig.getProperty("auth.otp.mock", "false"));
        
        if (useMockOtp) {
            String mockOtp = apiConfig.getProperty("auth.otp.mock.value", "123456");
            logger.debug("Using mock OTP for testing: {} for loginId: {}", mockOtp, loginId);
            return mockOtp;
        }
        
        // Try to get OTP from Redis
        try {
            String otp = getRedisManager().getOtp(loginId);
            if (otp != null && !otp.trim().isEmpty()) {
                logger.info("Retrieved OTP from Redis for loginId: {}", loginId);
                return otp;
            } else {
                logger.warn("OTP not found in Redis for loginId: {}, falling back to mock value", loginId);
            }
        } catch (Exception e) {
            logger.error("Error retrieving OTP from Redis for loginId: {}, falling back to mock value", loginId, e);
        }
        
        // Fallback to mock OTP if Redis fails
        String fallbackOtp = apiConfig.getProperty("auth.otp.mock.value", "123456");
        logger.warn("Using fallback mock OTP: {} for loginId: {}", fallbackOtp, loginId);
        return fallbackOtp;
    }
    
    /**
     * Generate and retrieve OTP using the hybrid approach
     * Calls OTP API to trigger generation, then retrieves from Redis
     */
    public String generateAndGetOtp(String loginId, String clientId, String deviceId) {
        try {
            // Step 1: Trigger OTP generation via API
            Response otpResponse = sendOtp(loginId, clientId, deviceId);
            if (otpResponse.getStatusCode() != 200) {
                logger.error("Failed to trigger OTP generation. Status: {}, Response: {}", 
                           otpResponse.getStatusCode(), otpResponse.asString());
                return getOtp(loginId); // Fallback to Redis/mock
            }
            // Step 2: Wait a moment for Redis to be updated (backend processing)
            Thread.sleep(1000);
            
            // Step 3: Retrieve OTP from Redis
            String otp = getOtp(loginId);
            logger.info("Generated and retrieved OTP for loginId: {}", loginId);
            return otp;
            
        } catch (Exception e) {
            logger.error("Error in generateAndGetOtp for loginId: {}", loginId, e);
            return getOtp(loginId); // Fallback
        }
    }
    
    /**
     * Clean up OTP rate limit keys to avoid throttling during testing
     */
    public void cleanupOtpLimits(String loginId) {
        try {
            getRedisManager().deleteOtpLimit(loginId);
            logger.debug("Cleaned up OTP limit keys for loginId: {}", loginId);
        } catch (Exception e) {
            logger.warn("Error cleaning up OTP limits for loginId: {}", loginId, e);
        }
    }
    
    /**
     * Check Redis connection health
     */
    public boolean isRedisHealthy() {
        try {
            return redisManager.isHealthy();
        } catch (Exception e) {
            logger.warn("Redis health check failed", e);
            return false;
        }
    }
    
    /**
     * Get Redis connection information for debugging
     */
    public String getRedisConnectionInfo() {
        try {
            return redisManager.getConnectionInfo();
        } catch (Exception e) {
            logger.warn("Failed to get Redis connection info", e);
            return "Redis connection info unavailable";
        }
    }
    
}