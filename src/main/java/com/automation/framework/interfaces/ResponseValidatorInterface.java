package com.automation.framework.interfaces;

import io.restassured.response.Response;
import java.util.Map;

/**
 * Interface for response validation operations
 * Defines common validation operations that can be implemented by different validators
 */
public interface ResponseValidatorInterface {
    
    /**
     * Validate HTTP status code
     */
    void validateStatusCode(Response response, int expectedStatusCode);
    
    /**
     * Validate HTTP status code is in array of acceptable status codes
     */
    void validateStatusCode(Response response, int[] expectedStatusCodes);
    
    /**
     * Validate a single response field
     */
    void validateResponseField(Response response, String key, Object expectedValue);
    
    /**
     * Validate multiple response fields
     */
    default void validateMultipleFields(Response response, Map<String, Object> expectedFields) {
        for (Map.Entry<String, Object> entry : expectedFields.entrySet()) {
            validateResponseField(response, entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Validate response against JSON schema
     */
    void validateSchema(Response response, String schemaPath);
    
    /**
     * Validate response time
     */
    void validateResponseTime(Response response, long maxResponseTime);
    
    /**
     * Validate response contains specific text
     */
    void validateResponseContains(Response response, String expectedText);
    
    /**
     * Validate response not null
     */
    void validateResponseNotNull(Response response);
    
    /**
     * Validate response is empty
     */
    void validateResponseEmpty(Response response);
    
    /**
     * Validate custom assertion with message
     */
    void validateCustomAssertion(boolean condition, String message);
    
    /**
     * Common helper method to extract value from response
     */
    default Object extractValueFromResponse(Response response, String jsonPath) {
        return response.jsonPath().get(jsonPath);
    }
    
    /**
     * Common helper method to get response body as string
     */
    default String getResponseBodyAsString(Response response) {
        return response.getBody().asString();
    }
}