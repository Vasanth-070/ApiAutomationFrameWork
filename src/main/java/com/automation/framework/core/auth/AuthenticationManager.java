package com.automation.framework.core.auth;

import com.automation.framework.core.config.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.EncoderConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.automation.framework.core.interfaces.LoggingInterface;
import com.automation.framework.core.logging.ApiLogger;

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
    private static final LoggingInterface logger = new ApiLogger(AuthenticationManager.class);
    
    private final String baseUrl;
    private final HeaderManager headerManager;
    private final TokenStorage tokenStorage;
    
    // Session-aware request specification for automatic cookie handling
    private RequestSpecification sessionSpec;
    private RedisManager redisManager; // Lazy initialization
    private final ApiConfig apiConfig;
    
    public AuthenticationManager(String baseUrl) {
        this.baseUrl = baseUrl;
        this.headerManager = new HeaderManager();
        this.tokenStorage = new TokenStorage();
        this.apiConfig = new ApiConfig();
        // RedisManager will be initialized only when needed
    }
    
    public AuthenticationManager(String baseUrl, RequestSpecification sessionSpec) {
        this.baseUrl = baseUrl;
        this.headerManager = new HeaderManager();
        this.tokenStorage = new TokenStorage();
        this.apiConfig = new ApiConfig();
        this.sessionSpec = sessionSpec;
        logger.logDebug("AuthenticationManager initialized with sessionSpec");
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
                logger.logError("Failed to retrieve OTP for user: " + loginId, null);
                return new AuthResponse(false, "Failed to retrieve OTP", null, null);
            }
            
            // Step 2: Login with OTP
            Response loginResponse = login(loginId, otp, clientId, deviceId);
            if (loginResponse.getStatusCode() == 200) {
                String accessToken = loginResponse.jsonPath().getString("data.login.access_token");
                
                // Cookie is automatically handled by REST Assured sessionSpec
                // No need to manually extract or store cookies for HTTP requests
                logger.logInfo("Authentication successful for user: " + loginId + ". Cookies automatically managed by REST Assured.");
                
                // Store only the access token (cookies are handled automatically)
                tokenStorage.storeToken(loginId, accessToken, null);
                
                return new AuthResponse(true, "Authentication successful", accessToken, null);
            } else {
                logger.logError("Login failed. Status: " + loginResponse.getStatusCode() + ", Response: " + loginResponse.asString(), null);
                return new AuthResponse(false, "Login failed", null, null);
            }
            
        } catch (Exception e) {
            logger.logError("Authentication error for user: " + loginId, e);
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
        
        logger.logDebug("Sending OTP to: " + loginId + " using endpoint: " + apiPath);
        
        // Execute request using sessionSpec for automatic cookie handling
        Response response;
        if (sessionSpec != null) {
            response = sessionSpec
                    .config(RestAssured.config().encoderConfig(
                            EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                    .contentType("application/x-www-form-urlencoded")
                    .spec(requestSpec)
                    .post(apiPath);
            logger.logDebug("Send OTP request executed with sessionSpec for automatic cookie handling");
        } else {
            // Fallback to regular RestAssured if sessionSpec not available
            response = RestAssured.given()
                    .config(RestAssured.config().encoderConfig(
                            EncoderConfig.encoderConfig().encodeContentTypeAs("x-www-form-urlencoded", ContentType.URLENC)))
                    .contentType("application/x-www-form-urlencoded")
                    .spec(requestSpec)
                    .post(apiPath);
            logger.logWarning("SessionSpec not available for sendOtp, using direct RestAssured call");
        }
                
        logger.logDebug("OTP response: " + response.asString());
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
        
        if (loginId.contains("@") && loginId.contains(".com")) {
            formParams.put("grant_type", "emotp");
            token = Base64.getEncoder().encodeToString((loginId + "~" + otp.trim()).getBytes());
        } else {
            formParams.put("grant_type", "photp");
            token = Base64.getEncoder().encodeToString((loginId + "~" + "+91" + "~" + otp.trim()).getBytes());
        }
        
        // Set grant_type again (following the working implementation pattern)
        if (loginId.contains("@") || loginId.contains(".com")) {
            formParams.put("grant_type", "emotp");
        } else {
            formParams.put("grant_type", "photp");
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
        
        logger.logDebug("Logging in user: " + loginId + " with endpoint: " + apiPath);
        
        // Execute request using sessionSpec for automatic cookie handling
        Response response;
        if (sessionSpec != null) {
            response = sessionSpec
                    .contentType("application/x-www-form-urlencoded")
                    .spec(requestSpec)
                    .post(apiPath);
            logger.logDebug("Login request executed with sessionSpec for automatic cookie handling");
        } else {
            // Fallback to regular RestAssured if sessionSpec not available
            response = RestAssured.given()
                    .contentType("application/x-www-form-urlencoded")
                    .spec(requestSpec)
                    .post(apiPath);
            logger.logWarning("SessionSpec not available, using direct RestAssured call");
        }
                
        logger.logDebug("Login response: " + response.asString());
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
        
        logger.logDebug("SHA512 message: " + message);
        
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
            logger.logDebug("Generated SHA512 token: " + token);
            return token;
            
        } catch (NoSuchAlgorithmException e) {
            logger.logError("SHA-512 algorithm not available", e);
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
            logger.logDebug("Using mock OTP for testing: " + mockOtp + " for loginId: " + loginId);
            return mockOtp;
        }
        
        // Try to get OTP from Redis
        try {
            String otp = getRedisManager().getOtp(loginId);
            if (otp != null && !otp.trim().isEmpty()) {
                logger.logInfo("Retrieved OTP from Redis for loginId: " + loginId);
                return otp;
            } else {
                logger.logWarning("OTP not found in Redis for loginId: " + loginId + ", falling back to mock value");
            }
        } catch (Exception e) {
            logger.logError("Error retrieving OTP from Redis for loginId: " + loginId + ", falling back to mock value", e);
        }
        
        // Fallback to mock OTP if Redis fails
        String fallbackOtp = apiConfig.getProperty("auth.otp.mock.value", "123456");
        logger.logWarning("Using fallback mock OTP: " + fallbackOtp + " for loginId: " + loginId);
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
                logger.logError("Failed to trigger OTP generation. Status: " + otpResponse.getStatusCode() + ", Response: " + otpResponse.asString(), null);
                return getOtp(loginId); // Fallback to Redis/mock
            }
            // Step 2: Wait a moment for Redis to be updated (backend processing)
            Thread.sleep(1000);
            
            // Step 3: Retrieve OTP from Redis
            String otp = getOtp(loginId);
            logger.logInfo("Generated and retrieved OTP for loginId: " + loginId);
            return otp;
            
        } catch (Exception e) {
            logger.logError("Error in generateAndGetOtp for loginId: " + loginId, e);
            return getOtp(loginId); // Fallback
        }
    }
    
    /**
     * Clean up OTP rate limit keys to avoid throttling during testing
     */
    public void cleanupOtpLimits(String loginId) {
        try {
            getRedisManager().deleteOtpLimit(loginId);
            logger.logDebug("Cleaned up OTP limit keys for loginId: " + loginId);
        } catch (Exception e) {
            logger.logWarning("Error cleaning up OTP limits for loginId: " + loginId);
        }
    }
    
    /**
     * Check Redis connection health
     */
    public boolean isRedisHealthy() {
        try {
            return redisManager.isHealthy();
        } catch (Exception e) {
            logger.logWarning("Redis health check failed");
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
            logger.logWarning("Failed to get Redis connection info");
            return "Redis connection info unavailable";
        }
    }
    
}