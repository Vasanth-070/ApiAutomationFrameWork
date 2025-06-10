package com.automation.framework.base;

import com.automation.framework.interfaces.ApiTestInterface;
import com.automation.framework.interfaces.DataProviderInterface;
import com.automation.framework.interfaces.LoggingInterface;
import com.automation.framework.interfaces.ReportingInterface;
import com.automation.framework.interfaces.ResponseValidatorInterface;
import com.automation.framework.config.ApiConfig;
import com.automation.framework.factory.DataProviderFactory;
import com.automation.framework.factory.LoggerFactory;
import com.automation.framework.factory.ReportManagerFactory;
import com.automation.framework.factory.ResponseValidatorFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseApiTest implements ApiTestInterface {

    protected ApiConfig apiConfig;
    protected ResponseValidatorInterface responseValidator;
    protected DataProviderInterface testDataProvider;
    protected LoggingInterface testLogger;
    protected ReportingInterface reportManager;
    protected ObjectMapper objectMapper;

    @BeforeClass
    public void baseSetup() {
        // Initialize core dependencies
        apiConfig = new ApiConfig();
        responseValidator = ResponseValidatorFactory.createValidator();
        testDataProvider = DataProviderFactory.createDataProvider();
        testLogger = LoggerFactory.createLogger();
        reportManager = ReportManagerFactory.createReportManager();
        objectMapper = new ObjectMapper();
        
        // Configure RestAssured
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
        headers.put("x-api-key", "reqres-free-v1");
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
    
    /**
     * Helper method to start a test with common logging and reporting
     */
    protected void startApiTest(String testName, String testDescription, String testMethodName, String testClassName) {
        reportManager.startTest(testName, testDescription);
        testLogger.logTestStart(testMethodName, testClassName);
    }
    
    /**
     * Helper method to log API request
     */
    protected void logApiRequest(String method, String endpoint, String payload, Map<String, String> headers) {
        testLogger.logApiRequest(method, endpoint, payload);
        reportManager.logApiRequest(method, endpoint, payload, headers.toString());
    }
    
    /**
     * Helper method to log API response
     */
    protected void logApiResponse(Response response) {
        testLogger.logApiResponse(response.getStatusCode(), response.getBody().asString(), response.getTime());
        reportManager.logApiResponse(response, response.getBody().asString());
    }
    
    /**
     * Helper method to end test with success
     */
    protected void endTestPassed(String testMethodName, String successMessage, long startTime) {
        long endTime = System.currentTimeMillis();
        testLogger.logTestEnd(testMethodName, "PASSED", endTime - startTime);
        reportManager.markTestPassed(testMethodName, successMessage);
    }
    
    /**
     * Helper method to end test with failure
     */
    protected void endTestFailed(String testMethodName, String errorMessage, Exception exception, long startTime) {
        long endTime = System.currentTimeMillis();
        testLogger.logTestEnd(testMethodName, "FAILED", endTime - startTime);
        testLogger.logError("Test failed with exception", exception);
        reportManager.markTestFailed(testMethodName, "Test failed: " + errorMessage, exception);
    }
    
    /**
     * Helper method to execute an API test with common pattern
     */
    protected void executeApiTest(String testName, String testDescription, String testMethodName, String testClassName, ApiTestExecutor executor) {
        startApiTest(testName, testDescription, testMethodName, testClassName);
        long startTime = System.currentTimeMillis();
        
        try {
            executor.execute();
            endTestPassed(testMethodName, "Test completed successfully", startTime);
        } catch (Exception e) {
            endTestFailed(testMethodName, e.getMessage(), e, startTime);
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Standardized exception handling for API tests
     */
    protected void handleTestException(String testMethodName, Exception exception, long startTime) {
        long endTime = System.currentTimeMillis();
        testLogger.logTestEnd(testMethodName, "FAILED", endTime - startTime);
        testLogger.logError("Test failed with exception", exception);
        reportManager.markTestFailed(testMethodName, "Test failed: " + exception.getMessage(), exception);
        throw new RuntimeException(exception);
    }
    
    /**
     * Execute API test with manual exception handling (for tests that need custom logic in catch block)
     */
    protected void executeApiTestWithManualExceptionHandling(String testName, String testDescription, 
                                                           String testMethodName, String testClassName, 
                                                           ApiTestExecutor executor) {
        startApiTest(testName, testDescription, testMethodName, testClassName);
        try {
            executor.execute();
        } catch (Exception e) {
            // Let the executor handle its own exceptions
            // This catch is just to satisfy compiler requirements
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Functional interface for test execution
     */
    @FunctionalInterface
    protected interface ApiTestExecutor {
        void execute() throws Exception;
    }
}