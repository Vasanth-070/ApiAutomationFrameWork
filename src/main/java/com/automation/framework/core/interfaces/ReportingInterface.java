package com.automation.framework.core.interfaces;

import io.restassured.response.Response;

public interface ReportingInterface {

    /**
     * Initialize reporting for test suite
     */
    void initializeReport(String suiteName, String environment);

    /**
     * Start test case reporting
     */
    void startTest(String testName, String description);

    /**
     * Log test step
     */
    void logStep(String stepDescription, String status);

    /**
     * Log API request details
     */
    void logApiRequest(String method, String endpoint, String requestBody, String headers);

    /**
     * Log API response details
     */
    void logApiResponse(Response response, String responseBody);

    /**
     * Mark test as passed
     */
    void markTestPassed(String testName, String details);

    /**
     * Mark test as failed
     */
    void markTestFailed(String testName, String errorMessage, Throwable exception);

    /**
     * Mark test as skipped
     */
    void markTestSkipped(String testName, String reason);

    /**
     * Add screenshot to report
     */
    void addScreenshot(String screenshotPath, String description);

    /**
     * Finalize and generate report
     */
    void finalizeReport();

    /**
     * Get report file path
     */
    String getReportPath();
}