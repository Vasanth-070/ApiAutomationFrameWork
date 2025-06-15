package com.automation.framework.core.auth;

import com.automation.framework.core.config.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * HeaderManager manages HTTP headers for API requests
 * Handles both common headers and authentication-specific headers
 */
public class HeaderManager {
    private static final Logger logger = LoggerFactory.getLogger(HeaderManager.class);
    private static final String API_KEY_SUFFIX = "!2$";

    /**
     * Get common headers from properties file
     */
    public Map<String, String> getCommonHeaders(String clientId) {
        Map<String, String> headers = new HashMap<>();

        try {
            // Load headers from properties file
            String propertiesPath = "src/main/resources/config/headers/" + clientId + "_headers.properties";
            Properties props = new Properties();

            try (FileReader reader = new FileReader(propertiesPath)) {
                props.load(reader);

                // Convert properties to map
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    headers.put(entry.getKey().toString(), entry.getValue().toString());
                }

                logger.debug("Loaded {} common headers for client: {}", headers.size(), clientId);
            } catch (IOException e) {
                logger.warn("Could not load headers from file: {}. Using default headers.", propertiesPath);
                // Fall back to default headers
                headers.putAll(getDefaultHeaders(clientId));
            }

        } catch (Exception e) {
            logger.error("Error loading common headers for client: {}", clientId, e);
            headers.putAll(getDefaultHeaders(clientId));
        }

        return headers;
    }

    /**
     * Get default headers when properties file is not available
     */
    private Map<String, String> getDefaultHeaders(String clientId) {
        Map<String, String> headers = new HashMap<>();
        headers.put("apiKey", clientId + API_KEY_SUFFIX);
        headers.put("accept", "*/*");
        headers.put("ixiSrc", clientId);
        headers.put("clientId", clientId);
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        return headers;
    }

    /**
     * Get headers for OTP request
     */
    public Map<String, String> getOtpHeaders(String clientId, String deviceId, Long deviceTime) {
        Map<String, String> headers = getCommonHeaders(clientId);

        // Add OTP-specific headers
        headers.put("deviceId", deviceId);
        headers.put("deviceTime", deviceTime.toString());
        headers.put("clientId", clientId);
        headers.put("uuid", deviceId);
        headers.put("X-Requested-With", "XMLHttpRequest");

        logger.debug("Built OTP headers for client: {} with device: {}", clientId, deviceId);
        return headers;
    }

    /**
     * Get headers for login request
     */
    public Map<String, String> getLoginHeaders(String clientId, String deviceId) {
        Map<String, String> headers = getCommonHeaders(clientId);

        // Add login-specific headers
        headers.put("deviceId", deviceId);
        headers.put("requesttimestamp", String.valueOf(System.currentTimeMillis()));
        headers.put("X-Requested-With", "XMLHttpRequest");

        // Add mobile app specific headers for certain clients
        if (clientId.equalsIgnoreCase("iximatr")) {
            UUID uuid = UUID.randomUUID();
            headers.put("appVersion", "431");
            headers.put("deviceOs", "Android");
            headers.put("deviceOsVersion", "22");
            headers.put("uuid", uuid.toString());
            headers.put("Accept-Language", "en");
        }

        logger.debug("Built login headers for client: {} with device: {}", clientId, deviceId);
        return headers;
    }

    /**
     * Build complete API headers for BaseApiTest with authentication and
     * configuration support
     * Centralizes header management from BaseApiTest.buildApiHeaders()
     * 
     * @param apiConfig           - ApiConfig instance for property access
     * @param testSpecificHeaders - optional test-specific headers to merge
     * @return complete header map for API requests
     */
    public Map<String, String> buildApiHeaders(ApiConfig apiConfig, Map<String, String> testSpecificHeaders) {
        Map<String, String> headers = new HashMap<>();

        // Standard HTTP headers
        headers.put("Content-Type", "application/json");
        headers.put("Accept", apiConfig.getProperty("api.accept", "application/json"));
        headers.put("Accept-Language", apiConfig.getProperty("api.accept.language", "en-US,en;q=0.9"));
        headers.put("User-Agent", apiConfig.getProperty("api.user.agent", "ApiAutomationFramework/1.0"));

        // Application-specific headers
        String timezone = apiConfig.getProperty("api.timezone");
        if (timezone != null) {
            headers.put("Timezone", timezone);
        }

        // API Key
        String apiKey = apiConfig.getProperty("api.key");
        if (apiKey != null) {
            headers.put("apikey", apiKey);
        }

        // Authentication token handling
        String authToken = null;
        try {
            authToken = SessionAuthenticationManager.getInstance().getSessionAuthToken();
            logger.debug("Using session-based authentication token");
        } catch (Exception e) {
            logger.warn("Failed to get session auth token: " + e.getMessage());
            // Fallback to configured token
            String configuredToken = apiConfig.getProperty("api.auth.token");
            if (configuredToken != null && !configuredToken.trim().isEmpty()) {
                authToken = "Bearer " + configuredToken;
                logger.debug("Using configured authentication token");
            }
        }

        if (authToken != null) {
            // Ensure Bearer prefix
            if (!authToken.startsWith("Bearer ")) {
                authToken = "Bearer " + authToken;
            }
            headers.put("Authorization", authToken);
        }

        // Client identification headers
        String clientId = apiConfig.getProperty("auth.user.clientid");
        if (clientId != null) {
            headers.put("clientid", clientId);
        }

        String deviceId = SessionAuthenticationManager.getInstance().getDeviceId();
        if (deviceId != null) {
            headers.put("deviceid", deviceId);
        }

        // App version headers
        String appVersion = apiConfig.getProperty("api.app.version");
        if (appVersion != null) {
            headers.put("x-request-webappversion", appVersion);
        }

        String sdkVersion = apiConfig.getProperty("api.sdk.version");
        if (sdkVersion != null) {
            headers.put("psdkuiversion", sdkVersion);
        }

        String ixiSrc = apiConfig.getProperty("api.ixisrc");
        if (ixiSrc != null) {
            headers.put("ixisrc", ixiSrc);
        }

        // Merge test-specific headers (they take precedence)
        if (testSpecificHeaders != null) {
            headers.putAll(testSpecificHeaders);
        }

        logger.debug("Built complete API headers with {} total headers", headers.size());
        return headers;
    }

}