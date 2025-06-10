package com.automation.framework.utils;

import com.automation.framework.interfaces.ResponseValidatorInterface;
import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import java.util.Map;

/**
 * Alternative implementation of ResponseValidatorInterface using JUnit-style assertions
 * This demonstrates the flexibility of the interface
 */
public class JUnitResponseValidator implements ResponseValidatorInterface {

    @Override
    public void validateStatusCode(Response response, int expectedStatusCode) {
        int actualStatusCode = response.getStatusCode();
        if (actualStatusCode != expectedStatusCode) {
            throw new AssertionError("Status code validation failed. Expected: " + expectedStatusCode + 
                                   ", Actual: " + actualStatusCode);
        }
    }

    @Override
    public void validateResponseField(Response response, String key, Object expectedValue) {
        Object actualValue = extractValueFromResponse(response, key);
        if (!java.util.Objects.equals(actualValue, expectedValue)) {
            throw new AssertionError("Response field validation failed for key: " + key + 
                                   ". Expected: " + expectedValue + ", Actual: " + actualValue);
        }
    }

    // validateMultipleFields uses default implementation from interface

    @Override
    public void validateSchema(Response response, String schemaPath) {
        try {
            response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
        } catch (Exception e) {
            throw new AssertionError("JSON schema validation failed for schema: " + schemaPath + 
                                   ". Error: " + e.getMessage());
        }
    }

    @Override
    public void validateResponseTime(Response response, long maxResponseTime) {
        long actualTime = response.getTime();
        if (actualTime > maxResponseTime) {
            throw new AssertionError("Response time exceeded expected threshold. Expected: <= " + 
                                   maxResponseTime + "ms, Actual: " + actualTime + "ms");
        }
    }

    @Override
    public void validateResponseContains(Response response, String expectedText) {
        String responseBody = getResponseBodyAsString(response);
        if (!responseBody.contains(expectedText)) {
            throw new AssertionError("Response does not contain expected text: '" + expectedText + 
                                   "'. Response body: " + responseBody);
        }
    }

    @Override
    public void validateResponseNotNull(Response response) {
        if (response == null) {
            throw new AssertionError("Response is null");
        }
        if (getResponseBodyAsString(response) == null) {
            throw new AssertionError("Response body is null");
        }
    }

    @Override
    public void validateResponseEmpty(Response response) {
        String responseBody = getResponseBodyAsString(response);
        if (responseBody != null && !responseBody.trim().isEmpty()) {
            throw new AssertionError("Response is not empty. Response body: " + responseBody);
        }
    }

    @Override
    public void validateCustomAssertion(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}