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
import com.automation.framework.core.auth.SessionAuthenticationManager;
import com.automation.framework.core.auth.HeaderManager;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
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
    
    // ==================== CONSTANTS - DEFAULT VALUES ====================
    public static final String EMPTY_BODY = "";

    
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
    // will be used by child classes
    protected DataProviderInterface testDataProvider;
    protected LoggingInterface testLogger;
    protected ReportingInterface reportManager;
    protected ObjectMapper objectMapper;
    protected SessionAuthenticationManager sessionAuthManager;
    protected HeaderManager headerManager;
    
    
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
        
        // Initialize session-based authentication manager
        sessionAuthManager = SessionAuthenticationManager.getInstance();
        
        // Initialize header manager
        headerManager = new HeaderManager();
        
        // Configure RestAssured with base URL from config
        RestAssured.baseURI = apiConfig.getBaseUrl();
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        
        // SessionSpec is now managed by SessionAuthenticationManager (singleton)
        // No need to initialize here as it's already initialized once
        
        testLogger.logDebug("REST Assured configured with automatic cookie/session management");
        
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
        failedTests++;
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
    protected Response makeApiCall(HttpMethod method, String endpoint, Map<String, String> testSpecificHeaders, String body, boolean validateStatus, int... expectedStatusCodes) {
        // Build headers with session-based authentication
        Map<String, String> finalHeaders = headerManager.buildApiHeaders(apiConfig, testSpecificHeaders);
        testLogger.logApiRequest(method.getValue(), endpoint, body);
        reportManager.logApiRequest(method.getValue(), endpoint, body, finalHeaders.toString());
        
        String fullUrl = apiConfig.getBaseUrl() + endpoint;
        testLogger.logInfo(MSG_FULL_REQUEST_URL + fullUrl);
        testLogger.logInfo(MSG_REQUEST_HEADERS + finalHeaders.toString());
        
        Response response;
        // Get sessionSpec from SessionAuthenticationManager
        RequestSpecification sessionSpec = sessionAuthManager.getSessionSpec();
        
        switch(method) {
            case GET:
                response = sessionSpec
                        .headers(finalHeaders)
                        .when()
                        .get(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case POST:
                response = sessionSpec
                        .headers(finalHeaders)
                        .body(body != null ? body : EMPTY_BODY)
                        .when()
                        .post(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case PUT:
                response = sessionSpec
                        .headers(finalHeaders)
                        .body(body != null ? body : EMPTY_BODY)
                        .when()
                        .put(endpoint)
                        .then()
                        .extract()
                        .response();
                break;
            case DELETE:
                response = sessionSpec
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
            try {
                if (expectedStatusCodes.length == 1) {
                    responseValidator.validateStatusCode(response, expectedStatusCodes[0]);
                } else {
                    responseValidator.validateStatusCode(response, expectedStatusCodes);
                }
            } catch (AssertionError e) {
                // Log full API response only when status code validation fails
                testLogger.logApiResponse(response.getStatusCode(), response.asString(), response.getTime());
                reportManager.logApiResponse(response, response.asString());
                throw e; // Re-throw the assertion error
            } catch (Exception e) {
                // Log full API response for other validation exceptions
                testLogger.logApiResponse(response.getStatusCode(), response.asString(), response.getTime());
                reportManager.logApiResponse(response, response.asString());
                throw e; // Re-throw the validation exception
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
     * Throws assertion error (failure count is handled at test level)
     * @param message - error message
     */
    protected void throwAssertionError(String message) {
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
    
    // ==================== AUTHENTICATION MANAGEMENT METHODS ====================
    
    /**
     * Force re-authentication for current session
     * Useful when token expires or authentication issues occur
     */
    protected final void forceReauthentication() {
        try {
            sessionAuthManager.forceReauthentication("default_session");
            testLogger.logInfo("Successfully re-authenticated. New token available.");
        } catch (Exception e) {
            testLogger.logError("Failed to re-authenticate", e);
            throw new RuntimeException("Re-authentication failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if current session has valid authentication
     */
    protected final boolean hasValidAuthentication() {
        return sessionAuthManager.hasValidSession("default_session");
    }
    
    /**
     * Get current session authentication data
     */
    protected final SessionAuthenticationManager.SessionAuthData getSessionAuthData() {
        return sessionAuthManager.getSessionData("default_session");
    }
    
    
    
    
    
    /**
     * Clear session authentication cache
     * Useful for testing different authentication scenarios
     */
    protected final void clearAuthenticationCache() {
        sessionAuthManager.clearSessionCache();
        testLogger.logInfo("Authentication cache cleared");
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