package com.automation.framework.data;

public class ApiEndPoints {

    // User Management Endpoints
    public static final String USERS = "/api/v1/users";
    public static final String USER_BY_ID = "/api/v1/users/{id}";
    public static final String USER_LOGIN = "/api/v1/auth/login";
    public static final String USER_LOGOUT = "/api/v1/auth/logout";

    // Product Management Endpoints
    public static final String PRODUCTS = "/api/v1/products";
    public static final String PRODUCT_BY_ID = "/api/v1/products/{id}";
    public static final String PRODUCT_CATEGORIES = "/api/v1/products/categories";

    // Order Management Endpoints
    public static final String ORDERS = "/api/v1/orders";
    public static final String ORDER_BY_ID = "/api/v1/orders/{id}";
    public static final String ORDER_STATUS = "/api/v1/orders/{id}/status";

    // Health Check
    public static final String HEALTH_CHECK = "/health";
}