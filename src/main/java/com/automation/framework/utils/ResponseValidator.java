package com.automation.framework.utils;

import io.restassured.response.Response;
import io.restassured.module.jsv.JsonSchemaValidator;
import org.testng.Assert;
import java.util.Map;

public class ResponseValidator {

    public void validateStatusCode(Response response, int expectedStatusCode) {
        Assert.assertEquals(response.getStatusCode(), expectedStatusCode,
                "Status code validation failed");
    }

    public void validateResponseField(Response response, String key, Object expectedValue) {
        Object actualValue = response.jsonPath().get(key);
        Assert.assertEquals(actualValue, expectedValue,
                "Response field validation failed for key: " + key);
    }

    public void validateMultipleFields(Response response, Map<String, Object> expectedFields) {
        for (Map.Entry<String, Object> entry : expectedFields.entrySet()) {
            validateResponseField(response, entry.getKey(), entry.getValue());
        }
    }

    public void validateSchema(Response response, String schemaPath) {
        response.then().body(JsonSchemaValidator.matchesJsonSchemaInClasspath(schemaPath));
    }

    public void validateResponseTime(Response response, long maxResponseTime) {
        Assert.assertTrue(response.getTime() <= maxResponseTime,
                "Response time exceeded expected threshold");
    }

    public void validateResponseContains(Response response, String expectedText) {
        Assert.assertTrue(response.getBody().asString().contains(expectedText),
                "Response does not contain expected text: " + expectedText);
    }
}