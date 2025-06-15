package com.automation.framework.core.auth;

import com.automation.framework.core.interfaces.LoggingInterface;
import com.automation.framework.core.logging.ApiLogger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * TokenStorage manages authentication tokens and cookies for different users
 * Thread-safe storage for parallel test execution
 */
public class TokenStorage {
    private static final LoggingInterface logger = new ApiLogger(TokenStorage.class);
    
    // Using ConcurrentHashMap for thread-safe operations
    private final Map<String, String> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, String> cookies = new ConcurrentHashMap<>();
    private final Map<String, Long> tokenTimestamps = new ConcurrentHashMap<>();
    
    // Token expiry time in milliseconds (default: 24 hours)
    private final long TOKEN_EXPIRY_TIME = 24 * 60 * 60 * 1000;
    
    /**
     * Store authentication token and cookie for a user
     */
    public void storeToken(String userId, String accessToken, String cookie) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.logWarning("Cannot store token: userId is null or empty");
            return;
        }
        
        String tokenKey = "auth_" + userId;
        String cookieKey = "cookie_" + userId;
        
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            accessTokens.put(tokenKey, accessToken.trim());
            tokenTimestamps.put(tokenKey, System.currentTimeMillis());
            logger.logDebug("Stored access token for user: " + userId);
        }
        
        if (cookie != null && !cookie.trim().isEmpty()) {
            cookies.put(cookieKey, cookie.trim());
            logger.logDebug("Stored cookie for user: " + userId);
        }
    }
    
    /**
     * Get authentication header (Bearer token) for a user
     */
    public String getAuthHeader(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.logWarning("Cannot get auth header: userId is null or empty");
            return null;
        }
        
        String tokenKey = "auth_" + userId;
        String accessToken = accessTokens.get(tokenKey);
        
        if (accessToken == null) {
            logger.logDebug("No access token found for user: " + userId);
            return null;
        }
        
        // Check if token is expired
        if (isTokenExpired(tokenKey)) {
            logger.logWarning("Access token expired for user: " + userId);
            removeToken(userId);
            return null;
        }
        
        return "Bearer " + accessToken;
    }
    
    /**
     * Get raw access token for a user
     */
    public String getAccessToken(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.logWarning("Cannot get access token: userId is null or empty");
            return null;
        }
        
        String tokenKey = "auth_" + userId;
        String accessToken = accessTokens.get(tokenKey);
        
        if (accessToken == null) {
            logger.logDebug("No access token found for user: " + userId);
            return null;
        }
        
        // Check if token is expired
        if (isTokenExpired(tokenKey)) {
            logger.logWarning("Access token expired for user: " + userId);
            removeToken(userId);
            return null;
        }
        
        return accessToken;
    }
    
    /**
     * Get cookie for a user
     */
    public String getCookie(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.logWarning("Cannot get cookie: userId is null or empty");
            return null;
        }
        
        String cookieKey = "cookie_" + userId;
        String cookie = cookies.get(cookieKey);
        
        if (cookie == null) {
            logger.logDebug("No cookie found for user: " + userId);
        }
        
        return cookie;
    }
    
    /**
     * Remove tokens for a user (logout)
     */
    public void removeToken(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            logger.logWarning("Cannot remove token: userId is null or empty");
            return;
        }
        
        String tokenKey = "auth_" + userId;
        String cookieKey = "cookie_" + userId;
        
        boolean tokenRemoved = accessTokens.remove(tokenKey) != null;
        boolean timestampRemoved = tokenTimestamps.remove(tokenKey) != null;
        boolean cookieRemoved = cookies.remove(cookieKey) != null;
        
        if (tokenRemoved || timestampRemoved || cookieRemoved) {
            logger.logDebug("Removed authentication data for user: " + userId);
        } else {
            logger.logDebug("No authentication data found to remove for user: " + userId);
        }
    }
    
    /**
     * Check if user has valid token
     */
    public boolean hasValidToken(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return false;
        }
        
        String tokenKey = "auth_" + userId;
        return accessTokens.containsKey(tokenKey) && !isTokenExpired(tokenKey);
    }
    
    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(String tokenKey) {
        Long timestamp = tokenTimestamps.get(tokenKey);
        if (timestamp == null) {
            return true;
        }
        
        long currentTime = System.currentTimeMillis();
        return (currentTime - timestamp) > TOKEN_EXPIRY_TIME;
    }
    
    /**
     * Clear all stored tokens (cleanup)
     */
    public void clearAllTokens() {
        int tokenCount = accessTokens.size();
        int cookieCount = cookies.size();
        
        accessTokens.clear();
        cookies.clear();
        tokenTimestamps.clear();
        
        logger.logInfo("Cleared " + tokenCount + " access tokens and " + cookieCount + " cookies from storage");
    }
    
    /**
     * Get count of stored users
     */
    public int getStoredUserCount() {
        return accessTokens.size();
    }
    
    /**
     * Get all stored user IDs (for debugging)
     */
    public java.util.Set<String> getStoredUserIds() {
        return accessTokens.keySet().stream()
                .map(key -> key.replace("auth_", ""))
                .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Update token expiry time
     */
    public void setTokenExpiryTime(long expiryTimeMs) {
        // This could be made configurable per instance if needed
        logger.logInfo("Token expiry time updated to: " + expiryTimeMs + " ms");
    }
}