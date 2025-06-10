package com.automation.framework.shared.utils;

import com.automation.framework.core.interfaces.ResponseValidatorInterface;
import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.Assert;
import java.util.Map;

/**
 * Default implementation of ResponseValidatorInterface using TestNG assertions
 */
public class ResponseValidator implements ResponseValidatorInterface {

    @Override
    public void validateStatusCode(Response response, int expectedStatusCode) {
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode,
                "Status code validation failed. Expected: " + expectedStatusCode + 
                ", Actual: " + response.getStatusCode());
    }
    
    @Override
    public void validateStatusCode(Response response, int[] expectedStatusCodes) {
        int actualStatusCode = response.getStatusCode();
        boolean isValidStatusCode = false;
        
        for (int expectedStatusCode : expectedStatusCodes) {
            if (actualStatusCode == expectedStatusCode) {
                isValidStatusCode = true;
                break;
            }
        }
        
        Assert.assertTrue(isValidStatusCode,
                "Status code validation failed. Expected one of: " + java.util.Arrays.toString(expectedStatusCodes) + 
                ", Actual: " + actualStatusCode);
    }

    @Override
    public void validateResponseField(Response response, String key, Object expectedValue) {
        Object actualValue = extractValueFromResponse(response, key);
        Assert.assertEquals(actualValue, expectedValue,
                "Response field validation failed for key: " + key + 
                ". Expected: " + expectedValue + ", Actual: " + actualValue);
    }

    // validateMultipleFields uses default implementation from interface

    @Override
    public void validateSchema(Response response, String schemaPath) {
        try {
            response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
        } catch (Exception e) {
            Assert.fail("JSON schema validation failed for schema: " + schemaPath + 
                       ". Error: " + e.getMessage());
        }
    }

    @Override
    public void validateResponseTime(Response response, long maxResponseTime) {
        long actualTime = response.getTime();
        Assert.assertTrue(actualTime <= maxResponseTime,
                "Response time exceeded expected threshold. Expected: <= " + maxResponseTime + 
                "ms, Actual: " + actualTime + "ms");
    }

    @Override
    public void validateResponseContains(Response response, String expectedText) {
        String responseBody = getResponseBodyAsString(response);
        Assert.assertTrue(responseBody.contains(expectedText),
                "Response does not contain expected text: '" + expectedText + 
                "'. Response body: " + responseBody);
    }

    @Override
    public void validateResponseNotNull(Response response) {
        Assert.assertNotNull(response, "Response is null");
        Assert.assertNotNull(getResponseBodyAsString(response), "Response body is null");
    }

    @Override
    public void validateResponseEmpty(Response response) {
        String responseBody = getResponseBodyAsString(response);
        Assert.assertTrue(responseBody == null || responseBody.trim().isEmpty(),
                "Response is not empty. Response body: " + responseBody);
    }

    @Override
    public void validateCustomAssertion(boolean condition, String message) {
        Assert.assertTrue(condition, message);
    }
}