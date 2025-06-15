package com.automation.framework.core.auth;

/**
 * AuthResponse represents the result of an authentication attempt
 */
public class AuthResponse {
    private final boolean success;
    private final String message;
    private final String accessToken;
    private final String cookie;
    
    public AuthResponse(boolean success, String message, String accessToken, String cookie) {
        this.success = success;
        this.message = message;
        this.accessToken = accessToken;
        this.cookie = cookie;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getCookie() {
        return cookie;
    }
    
    public String getBearerToken() {
        return accessToken != null ? "Bearer " + accessToken : null;
    }
    
    @Override
    public String toString() {
        return "AuthResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", hasToken=" + (accessToken != null) +
                ", hasCookie=" + (cookie != null) +
                '}';
    }
}