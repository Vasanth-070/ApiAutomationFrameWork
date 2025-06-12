package com.automation.framework.core.interfaces;

/**
 * Core interface that enforces API test architecture patterns.
 * All API test classes must implement these methods to ensure consistency.
 */
public interface ApiTestInterface {

    /**
     * REQUIRED: Provide the test suite name for reporting and logging.
     * This name will be used in reports and log files.
     * 
     * @return The name of the test suite (e.g., "User Management API Tests")
     */
    String getTestSuiteName();

    /**
     * REQUIRED: Define all test cases using the framework's test execution pattern.
     * Must use executeTest() wrapper for all test methods.
     * This method is called by the framework to register all test cases.
     */
    void defineTestCases();

    /**
     * Test-specific setup logic executed before test suite starts.
     * Override to add custom test data setup.
     */
    default void performTestSetup() {
        // Default empty implementation - override as needed
    }

    /**
     * Test-specific cleanup logic executed after test suite completes.
     * Override to add custom test data cleanup.
     */
    default void performTestCleanup() {
        // Default empty implementation - override as needed
    }
}