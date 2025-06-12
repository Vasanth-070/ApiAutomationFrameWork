package com.automation.framework.shared.utils;

/**
 * Enum representing HTTP methods for API calls
 * Provides type-safe method specification for makeApiCall functions
 * 
 * @author API Automation Framework
 * @version 1.0
 */
public enum HttpMethod {
    
    /**
     * GET method - for retrieving data
     */
    GET("GET"),
    
    /**
     * POST method - for creating new resources
     */
    POST("POST"),
    
    /**
     * PUT method - for updating existing resources
     */
    PUT("PUT"),
    
    /**
     * DELETE method - for deleting resources
     */
    DELETE("DELETE"),
    
    /**
     * PATCH method - for partial updates
     */
    PATCH("PATCH"),
    
    /**
     * HEAD method - for getting headers only
     */
    HEAD("HEAD"),
    
    /**
     * OPTIONS method - for checking supported methods
     */
    OPTIONS("OPTIONS");
    
    private final String value;
    
    /**
     * Constructor for HttpMethod enum
     * @param value the string representation of the HTTP method
     */
    HttpMethod(String value) {
        this.value = value;
    }
    
    /**
     * Get the string value of the HTTP method
     * @return the HTTP method as string
     */
    public String getValue() {
        return value;
    }
    
    /**
     * Get the string representation (same as getValue())
     * @return the HTTP method as string
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * Create HttpMethod from string value
     * @param method the string representation of HTTP method
     * @return HttpMethod enum value
     * @throws IllegalArgumentException if method is not supported
     */
    public static HttpMethod fromString(String method) {
        if (method == null) {
            throw new IllegalArgumentException("HTTP method cannot be null");
        }
        
        for (HttpMethod httpMethod : HttpMethod.values()) {
            if (httpMethod.value.equalsIgnoreCase(method.trim())) {
                return httpMethod;
            }
        }
        
        throw new IllegalArgumentException("Unsupported HTTP method: " + method);
    }
}