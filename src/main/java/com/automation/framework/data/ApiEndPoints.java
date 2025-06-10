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

    // JSONPlaceholder API Endpoints
    public static final String JSONPLACEHOLDER_POSTS = "/posts";
    public static final String JSONPLACEHOLDER_POST_BY_ID = "/posts/{id}";
    public static final String JSONPLACEHOLDER_USERS = "/users";
    public static final String JSONPLACEHOLDER_USER_BY_ID = "/users/{id}";
    public static final String JSONPLACEHOLDER_COMMENTS = "/comments";
    public static final String JSONPLACEHOLDER_ALBUMS = "/albums";
    public static final String JSONPLACEHOLDER_PHOTOS = "/photos";

    // Ixigo Flight Booking API Endpoints
    public static final String IXIGO_FLIGHT_TRIP_DETAILS = "/flight-booking-read/flight-trip/details/{tripId}";
}