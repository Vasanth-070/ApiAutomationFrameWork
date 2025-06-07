package com.automation.framework.base;

import com.automation.framework.interfaces.ApiTestInterface;
import com.automation.framework.config.ApiConfig;
import com.automation.framework.utils.ResponseValidator;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseApiTest implements ApiTestInterface {

    protected ApiConfig apiConfig;
    protected ResponseValidator responseValidator;

    @BeforeClass
    public void baseSetup() {
        apiConfig = new ApiConfig();
        responseValidator = new ResponseValidator();
        RestAssured.baseURI = apiConfig.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        setupTestData();
    }

    @AfterClass
    public void baseTeardown() {
        cleanupTestData();
    }

    @Override
    public void validateStatusCode(Response response, int expectedStatusCode) {
        responseValidator.validateStatusCode(response, expectedStatusCode);
    }

    @Override
    public void validateResponse(Response response, String expectedKey, Object expectedValue) {
        responseValidator.validateResponseField(response, expectedKey, expectedValue);
    }

    @Override
    public void validateResponseFields(Response response, Map<String, Object> expectedFields) {
        responseValidator.validateMultipleFields(response, expectedFields);
    }

    @Override
    public Map<String, String> getApiHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    @Override
    public Map<String, String> getAuthHeaders(String token) {
        Map<String, String> headers = getApiHeaders();
        headers.put("Authorization", "Bearer " + token);
        return headers;
    }

    @Override
    public void validateResponseSchema(Response response, String schemaPath) {
        responseValidator.validateSchema(response, schemaPath);
    }

    @Override
    public String extractValueFromResponse(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    @Override
    public void setupTestData() {
        // Default implementation - can be overridden by test classes
    }

    @Override
    public void cleanupTestData() {
        // Default implementation - can be overridden by test classes
    }
}