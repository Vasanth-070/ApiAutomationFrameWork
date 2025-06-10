package com.automation.framework.core.interfaces;

import org.slf4j.Logger;

public interface LoggingInterface {

    /**
     * Get logger instance
     */
    Logger getLogger();

    /**
     * Log info message
     */
    void logInfo(String message);

    /**
     * Log debug message
     */
    void logDebug(String message);

    /**
     * Log warning message
     */
    void logWarning(String message);

    /**
     * Log error message
     */
    void logError(String message, Throwable exception);

    /**
     * Log API request
     */
    void logApiRequest(String method, String url, String payload);

    /**
     * Log API response
     */
    void logApiResponse(int statusCode, String responseBody, long responseTime);

    /**
     * Log test execution start
     */
    void logTestStart(String testName, String testClass);

    /**
     * Log test execution end
     */
    void logTestEnd(String testName, String status, long executionTime);

    /**
     * Configure logging level
     */
    void setLoggingLevel(String level);
    
    /**
     * Log test suite start
     */
    void logTestSuiteStart(String suiteName);
    
    /**
     * Log test suite end
     */
    void logTestSuiteEnd(String suiteName, int totalTests, int passedTests, int failedTests, int skippedTests);
    
    /**
     * Log assertion passed
     */
    void logAssertionPassed(String assertionMessage);
}