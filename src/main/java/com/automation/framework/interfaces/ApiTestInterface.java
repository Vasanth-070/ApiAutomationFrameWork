package com.automation.framework.interfaces;

import io.restassured.response.Response;
import java.util.Map;

public interface ApiTestInterface {

    /**
     * Validate HTTP status code
     */
    void validateStatusCode(Response response, int expectedStatusCode);
    
    /**
     * Validate HTTP status code is in array of acceptable status codes
     */
    void validateStatusCode(Response response, int[] expectedStatusCodes);

    /**
     * Validate response body content
     */
    void validateResponse(Response response, String expectedKey, Object expectedValue);

    /**
     * Validate multiple response fields
     */
    void validateResponseFields(Response response, Map<String, Object> expectedFields);

    /**
     * Set up API headers
     */
    Map<String, String> getApiHeaders();

    /**
     * Set up authentication headers
     */
    Map<String, String> getAuthHeaders(String token);

    /**
     * Validate response schema
     */
    void validateResponseSchema(Response response, String schemaPath);

    /**
     * Extract value from response
     */
    String extractValueFromResponse(Response response, String jsonPath);

    /**
     * Setup test data before test execution
     */
    void setupTestData();

    /**
     * Cleanup test data after test execution
     */
    void cleanupTestData();
}