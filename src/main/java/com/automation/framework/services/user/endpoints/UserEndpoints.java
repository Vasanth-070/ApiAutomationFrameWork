package com.automation.framework.services.user.endpoints;

public class UserEndpoints {
    
    // User Management Endpoints
    public static final String USERS = "/api/v1/users";
    public static final String USER_BY_ID = "/api/v1/users/{id}";
    public static final String USER_LOGIN = "/api/v1/auth/login";
    public static final String USER_LOGOUT = "/api/v1/auth/logout";
    
    // JSONPlaceholder API Endpoints (for testing)
    public static final String JSONPLACEHOLDER_USERS = "/users";
    public static final String JSONPLACEHOLDER_USER_BY_ID = "/users/{id}";
}