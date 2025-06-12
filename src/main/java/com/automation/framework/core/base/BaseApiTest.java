package com.automation.framework.core.base;

import com.automation.framework.core.interfaces.ApiTestInterface;
import com.automation.framework.core.interfaces.DataProviderInterface;
import com.automation.framework.core.interfaces.LoggingInterface;
import com.automation.framework.core.interfaces.ReportingInterface;
import com.automation.framework.core.interfaces.ResponseValidatorInterface;
import com.automation.framework.core.config.ApiConfig;
import com.automation.framework.core.factory.DataProviderFactory;
import com.automation.framework.core.factory.LoggerFactory;
import com.automation.framework.core.factory.ReportManagerFactory;
import com.automation.framework.core.factory.ResponseValidatorFactory;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class that ENFORCES API test architecture patterns through Template Method pattern.
 * 
 * ARCHITECTURAL ENFORCEMENT:
 * - FORCES proper test lifecycle management through final methods
 * - ENSURES consistent test structure and metadata via abstract methods
 * - PREVENTS bypass of framework functionality with final core methods
 * - PROVIDES template methods for guided test creation
 * 
 * REQUIRED IMPLEMENTATIONS (ABSTRACT METHODS):
 * - getTestSuiteName(): Provide test suite name for reporting
 * - defineTestCases(): Define all test cases using executeTest() wrapper
 * 
 * USAGE PATTERN (ENFORCED):
 * - Use executeTest() wrapper for ALL test methods (cannot be bypassed)
 * - Use makeApiCall() for HTTP requests (framework handles auth/logging)
 * - Use validateWithLogging() for assertions (framework handles reporting)
 * - Use performTestSetup()/performTestCleanup() for test-specific logic
 * 
 * FRAMEWORK GUARANTEE:
 * - All tests follow consistent patterns
 * - All API calls are logged and authenticated
 * - All validations are reported
 * - Test lifecycle is properly managed
 */
public abstract class BaseApiTest implements ApiTestInterface {

    // ==================== CONSTANTS - HTTP HEADERS ====================
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_ACCEPT = "Accept";
    public static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_TIMEZONE = "Timezone";
    public static final String HEADER_API_KEY = "apikey";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CLIENT_ID = "clientid";
    public static final String HEADER_DEVICE_ID = "deviceid";
    public static final String HEADER_WEBAPP_VERSION = "x-request-webappversion";
    public static final String HEADER_SDK_VERSION = "psdkuiversion";
    public static final String HEADER_IXI_SRC = "ixisrc";
    
    // ==================== CONSTANTS - DEFAULT VALUES ====================
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String DEFAULT_ACCEPT_LANGUAGE = "en-US,en;q=0.9";
    public static final String DEFAULT_USER_AGENT = "ApiAutomationFramework/1.0";
    public static final String EMPTY_BODY = "";
    
    // ==================== CONSTANTS - CONFIGURATION KEYS ====================
    public static final String PROP_API_ACCEPT = "api.accept";
    public static final String PROP_API_ACCEPT_LANGUAGE = "api.accept.language";
    public static final String PROP_API_USER_AGENT = "api.user.agent";
    public static final String PROP_API_TIMEZONE = "api.timezone";
    public static final String PROP_API_KEY = "api.key";
    public static final String PROP_API_CLIENT_ID = "api.client.id";
    public static final String PROP_API_DEVICE_ID = "api.device.id";
    public static final String PROP_API_APP_VERSION = "api.app.version";
    public static final String PROP_API_SDK_VERSION = "api.sdk.version";
    public static final String PROP_API_IXI_SRC = "api.ixisrc";
    public static final String PROP_API_AUTH_TOKEN = "api.auth.token";
    
    // ==================== CONSTANTS - TEST STATUS ====================
    public static final String STATUS_PASSED = "PASSED";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    
    // ==================== CONSTANTS - LOG MESSAGES ====================
    public static final String MSG_TEST_FAILED_EXCEPTION = "Test failed with exception";
    public static final String MSG_TEST_FAILED_PREFIX = "Test failed: ";
    public static final String MSG_TEST_COMPLETED_SUCCESS = "Test completed successfully";
    public static final String MSG_FULL_REQUEST_URL = "Full Request URL: ";
    public static final String MSG_REQUEST_HEADERS = "Request Headers: ";
    public static final String MSG_RESPONSE_STATUS_CODE = "Response Status Code: ";
    public static final String MSG_RESPONSE_TIME = "Response Time: ";
    public static final String MSG_MILLISECONDS = " ms";
    public static final String MSG_RESPONSE_HEADERS = "Response Headers: ";
    public static final String MSG_VALIDATING_PREFIX = "Validating ";
    public static final String MSG_VALIDATING_SUFFIX = "...";
    public static final String MSG_ASSERTION_PASSED = "✓ ASSERTION PASSED: ";
    public static final String MSG_ASSERTION_FAILED = "✗ ASSERTION FAILED: ";
    public static final String MSG_VALIDATION_ERROR = "✗ VALIDATION ERROR: ";
    public static final String MSG_VALIDATION_SUFFIX = " validation";
    public static final String MSG_TEST_EXECUTION_COMPLETED = "Test execution completed. Report generated at: ";
    
    // ==================== CONSTANTS - ERROR MESSAGES ====================
    public static final String ERROR_UNSUPPORTED_HTTP_METHOD = "Unsupported HTTP method: ";
    public static final String ERROR_TEST_FAILED_UNEXPECTED = "Test failed due to unexpected exception: ";
    public static final String ERROR_VALIDATION_FAILED_UNEXPECTED = "Validation failed due to unexpected exception: ";

    protected ApiConfig apiConfig;
    protected ResponseValidatorInterface responseValidator;
    protected DataProviderInterface testDataProvider;
    protected LoggingInterface testLogger;
    protected ReportingInterface reportManager;
    protected ObjectMapper objectMapper;
    
    // Test counters for dynamic reporting
    private int totalTests = 0;
    private int passedTests = 0;
    private int failedTests = 0;
    private int skippedTests = 0;
    
    // ==================== ABSTRACT METHODS (ENFORCED IMPLEMENTATION) ====================
    
    /**
     * REQUIRED: Must return test suite name for reporting and logging.
     * Used in report generation and log file naming.
     */
    public abstract String getTestSuiteName();
    
    
    /**
     * REQUIRED: Must define all test cases using executeTest() wrapper pattern.
     * This method is called during test initialization to register test cases.
     * 
     * Example implementation:
     * <pre>
     * &#64;Override
     * public void defineTestCases() {
     *     // Test cases will be defined as TestNG methods
     *     // This method serves as documentation of the test structure
     * }
     * </pre>
     */
    public abstract void defineTestCases();

    /**
     * FINAL - Framework-controlled setup that cannot be overridden.
     * Ensures consistent initialization across all test classes.
     * Uses Template Method pattern to call abstract methods.
     */
    @BeforeClass
    public final void baseSetup() {
        // Initialize core dependencies
        apiConfig = new ApiConfig();
        
        responseValidator = ResponseValidatorFactory.createValidator();
        testDataProvider = DataProviderFactory.createDataProvider();
        testLogger = LoggerFactory.createLogger();
        reportManager = ReportManagerFactory.createReportManager();
        objectMapper = new ObjectMapper();
        
        // Configure RestAssured with base URL from config
        RestAssured.baseURI = apiConfig.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // Initialize report with enforced test suite name
        reportManager.initializeReport(getTestSuiteName(), apiConfig.getEnvironment());
        testLogger.logTestSuiteStart(getTestSuiteName());
        
        // Call test-specific setup hook
        performTestSetup();
        
        // Register test cases (enforced implementation)
        defineTestCases();
    }

    /**
     * FINAL - Framework-controlled teardown that cannot be overridden.
     * Ensures consistent cleanup across all test classes.
     */
    @AfterClass
    public final void baseTeardown() {
        // Call test-specific cleanup hook
        performTestCleanup();
        
        // Framework-controlled teardown with enforced test suite name
        baseTearDown(getTestSuiteName());
    }
    
    /**
     * Internal method to build headers with authentication
     */
    private Map<String, String> buildApiHeaders(String token) {
        Map<String, String> headers = new HashMap<>();
        
        // Standard HTTP headers
        headers.put(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
        headers.put(HEADER_ACCEPT, apiConfig.getProperty(PROP_API_ACCEPT, CONTENT_TYPE_JSON));
        headers.put(HEADER_ACCEPT_LANGUAGE, apiConfig.getProperty(PROP_API_ACCEPT_LANGUAGE, DEFAULT_ACCEPT_LANGUAGE));
        headers.put(HEADER_USER_AGENT, apiConfig.getProperty(PROP_API_USER_AGENT, DEFAULT_USER_AGENT));
        
        // Application-specific headers
        String timezone = apiConfig.getProperty(PROP_API_TIMEZONE);
        if (timezone != null) {
            headers.put(HEADER_TIMEZONE, timezone);
        }
        
        // Authentication headers
        String apiKey = apiConfig.getProperty(PROP_API_KEY);
        if (apiKey != null) {
            headers.put(HEADER_API_KEY, apiKey);
        }
        
        if (token != null) {
            headers.put(HEADER_AUTHORIZATION, BEARER_PREFIX + token);
        }
        
        String clientId = apiConfig.getProperty(PROP_API_CLIENT_ID);
        if (clientId != null) {
            headers.put(HEADER_CLIENT_ID, clientId);
        }
        
        String deviceId = apiConfig.getProperty(PROP_API_DEVICE_ID);
        if (deviceId != null) {
            headers.put(HEADER_DEVICE_ID, deviceId);
        }
        
        // App version headers
        String appVersion = apiConfig.getProperty(PROP_API_APP_VERSION);
        if (appVersion != null) {
            headers.put(HEADER_WEBAPP_VERSION, appVersion);
        }
        
        String sdkVersion = apiConfig.getProperty(PROP_API_SDK_VERSION);
        if (sdkVersion != null) {
            headers.put(HEADER_SDK_VERSION, sdkVersion);
        }
        
        String ixiSrc = apiConfig.getProperty(PROP_API_IXI_SRC);
        if (ixiSrc != null) {
            headers.put(HEADER_IXI_SRC, ixiSrc);
        }
        
        return headers;
    }

    // ==================== FRAMEWORK HOOK METHODS (OVERRIDE FOR CUSTOM LOGIC) ====================
    
    /**
     * Override to add test-specific setup logic.
     * Called by framework during baseSetup() lifecycle.
     */
    @Override
    public void performTestSetup() {
        // Default empty implementation - override as needed
    }
    
    /**
     * Override to add test-specific cleanup logic.
     * Called by framework during baseTeardown() lifecycle.
     */
    @Override
    public void performTestCleanup() {
        // Default empty implementation - override as needed
    }
    
    
    /**
     * Functional interface for test execution
     */
    @FunctionalInterface
    protected interface ApiTestExecutor {
        void execute() throws Exception;
    }
    
    /**
     * Functional interface for validation execution
     */
    @FunctionalInterface
    protected interface ValidationExecutor {
        void validate() throws Exception;
    }
    
    /**
     * FINAL - Main test execution wrapper that handles entire test lifecycle.
     * CANNOT BE OVERRIDDEN - Ensures all tests follow framework patterns.
     * @param testName - name of the test for reporting
     * @param description - test description
     * @param executor - test execution logic
     */
    protected final void executeTest(String testName, String description, ApiTestExecutor executor) {
        totalTests++;
        reportManager.startTest(testName, description);
        
        String className = this.getClass().getSimpleName();
        testLogger.logTestStart(testName, className);
        
        long startTime = System.currentTimeMillis();
        
        try {
            executor.execute();
            passedTests++;
            long endTime = System.currentTimeMillis();
            testLogger.logTestEnd(testName, STATUS_COMPLETED, endTime - startTime);
            reportManager.markTestPassed(testName, MSG_TEST_COMPLETED_SUCCESS);
        } catch (AssertionError e) {
            // Test assertions failed - let TestNG handle these naturally
            logTestFailure(testName, e, startTime);
            throw e;
        } catch (RuntimeException e) {
            // Runtime exceptions - pass through without wrapping
            logTestFailure(testName, e, startTime);
            throw e;
        } catch (Exception e) {
            // Checked exceptions - log and convert to AssertionError for test framework
            logTestFailure(testName, e, startTime);
            throw new AssertionError(ERROR_TEST_FAILED_UNEXPECTED + e.getMessage(), e);
        }
    }
    
    /**
     * Private helper method to handle test failure logging and reporting
     * @param testName - name of the test
     * @param exception - the exception that caused the failure
     * @param startTime - test start time for duration calculation
     */
    private void logTestFailure(String testName, Throwable exception, long startTime) {
        long endTime = System.currentTimeMillis();
        testLogger.logTestEnd(testName, STATUS_FAILED, endTime - startTime);
        testLogger.logError(MSG_TEST_FAILED_EXCEPTION, exception);
        reportManager.markTestFailed(testName, MSG_TEST_FAILED_PREFIX + exception.getMessage(), exception);
    }
    
    /**
     * Private helper method to handle validation failure logging and reporting
     * @param validationName - name of the validation for logging
     * @param exception - the exception that caused the validation failure
     * @param isAssertionError - whether this is an assertion error (affects log message)
     */
    private void logValidationFailure(String validationName, Throwable exception, boolean isAssertionError) {
        String logMessage = isAssertionError ? MSG_ASSERTION_FAILED : MSG_VALIDATION_ERROR;
        testLogger.logError(logMessage + validationName, exception);
        reportManager.logStep(validationName + MSG_VALIDATION_SUFFIX, "FAILED");
    }
    
    /**
     * FINAL - API call wrapper that handles logging and RestAssured calls.
     * CANNOT BE OVERRIDDEN - Ensures all API calls are authenticated and logged.
     * Automatically adds auth headers to provided test-specific headers.
     * @param method - HTTP method enum (HttpMethod.GET, HttpMethod.POST, etc.)
     * @param endpoint - API endpoint
     * @param testSpecificHeaders - test-specific headers (can be null or empty)
     * @param body - request body (can be null)
     * @return Response object
     */
    protected final Response makeApiCall(HttpMethod method, String endpoint, Map<String, String> testSpecificHeaders, String body) {
        return makeApiCall(method, endpoint, testSpecificHeaders, body, true, 200);
    }
    
    /**
     * Internal API call implementation with status code validation control
     * Automatically merges auth headers with test-specific headers
     * @param method - HTTP method enum (HttpMethod.GET, HttpMethod.POST, etc.)
     * @param endpoint - API endpoint
     * @param testSpecificHeaders - test-specific headers (can be null or empty)
     * @param body - request body (can be null)
     * @param validateStatus - whether to validate status code
     * @param expectedStatusCodes - expected status codes
     * @return Response object
     */
    private Response makeApiCall(HttpMethod method, String endpoint, Map<String, String> testSpecificHeaders, String body, boolean validateStatus, int... expectedStatusCodes) {
        // Start with authenticated headers
        String token = apiConfig.getProperty(PROP_API_AUTH_TOKEN);
        
        Map<String, String> finalHeaders = buildApiHeaders(token);
        if (testSpecificHeaders != null) {
            finalHeaders.putAll(testSpecificHeaders);
        }
        
        testLogger.logApiRequest(method.getValue(), endpoint, body);
        reportManager.logApiRequest(method.getValue(), endpoint, body, finalHeaders.toString());
        
        String fullUrl = apiConfig.getBaseUrl() + endpoint;
        testLogger.logInfo(MSG_FULL_REQUEST_URL + fullUrl);
        testLogger.logInfo(MSG_REQUEST_HEADERS + finalHeaders.toString());
        
        Response response;
        switch(method) {
            case GET:
                response = RestAssured.given()
                        .headers(finalHeaders)
                        .when()
                        .get(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case POST:
                response = RestAssured.given()
                        .headers(finalHeaders)
                        .body(body != null ? body : EMPTY_BODY)
                        .when()
                        .post(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case PUT:
                response = RestAssured.given()
                        .headers(finalHeaders)
                        .body(body != null ? body : EMPTY_BODY)
                        .when()
                        .put(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case DELETE:
                response = RestAssured.given()
                        .headers(finalHeaders)
                        .when()
                        .delete(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            default:
                throw new IllegalArgumentException(ERROR_UNSUPPORTED_HTTP_METHOD + method.getValue());
        }
        
        testLogger.logInfo(MSG_RESPONSE_STATUS_CODE + response.getStatusCode());
        testLogger.logInfo(MSG_RESPONSE_TIME + response.getTime() + MSG_MILLISECONDS);
        testLogger.logInfo(MSG_RESPONSE_HEADERS + response.getHeaders().toString());
        
        // Validate status code if requested
        if (validateStatus) {
            if (expectedStatusCodes.length == 1) {
                responseValidator.validateStatusCode(response, expectedStatusCodes[0]);
            } else {
                responseValidator.validateStatusCode(response, expectedStatusCodes);
            }
        }
        
        return response;
    }
    
    /**
     * FINAL - Validation wrapper that handles logging and reporting.
     * CANNOT BE OVERRIDDEN - Ensures all validations are logged and reported.
     * @param validationName - name of validation for logging
     * @param validator - validation logic
     */
    protected final void validateWithLogging(String validationName, ValidationExecutor validator) {
        testLogger.logInfo(MSG_VALIDATING_PREFIX + validationName + MSG_VALIDATING_SUFFIX);
        try {
            validator.validate();
            testLogger.logInfo(MSG_ASSERTION_PASSED + validationName);
            reportManager.logStep(validationName + MSG_VALIDATION_SUFFIX, "PASSED");
        } catch (AssertionError e) {
            // Assertion failures - preserve as AssertionError for test frameworks
            logValidationFailure(validationName, e, true);
            throw e;
        } catch (RuntimeException e) {
            // Runtime exceptions - pass through
            logValidationFailure(validationName, e, false);
            throw e;
        } catch (Exception e) {
            // Checked exceptions - convert to AssertionError
            logValidationFailure(validationName, e, false);
            throw new AssertionError(ERROR_VALIDATION_FAILED_UNEXPECTED + e.getMessage(), e);
        }
    }
    
    /**
     * Throws assertion error with failure count increment
     * @param message - error message
     */
    protected void throwAssertionError(String message) {
        failedTests++;
        throw new AssertionError(message);
    }
    
    /**
     * Common teardown for test suites
     */
    private void baseTearDown(String suiteName, int totalTests, int passedTests, int failedTests, int skippedTests) {
        testLogger.logTestSuiteEnd(suiteName, totalTests, passedTests, failedTests, skippedTests);
        reportManager.finalizeReport();
        testLogger.logInfo(MSG_TEST_EXECUTION_COMPLETED + reportManager.getReportPath());
    }
    
    /**
     * Common teardown for test suites using instance variables
     */
    protected void baseTearDown(String suiteName) {
        baseTearDown(suiteName, this.totalTests, this.passedTests, this.failedTests, this.skippedTests);
    }
    // ==================== CONVENIENCE METHODS ====================
    
    /**
     * FINAL - API call with no test-specific headers (most common case).
     * Auth headers are automatically added by BaseApiTest.
     */
    protected final Response makeApiCall(HttpMethod method, String endpoint) {
        return makeApiCall(method, endpoint, null, null);
    }
    
    /**
     * FINAL - API call with body but no test-specific headers.
     * Auth headers are automatically added by BaseApiTest.
     */
    protected final Response makeApiCall(HttpMethod method, String endpoint, String body) {
        return makeApiCall(method, endpoint, null, body);
    }
    
}