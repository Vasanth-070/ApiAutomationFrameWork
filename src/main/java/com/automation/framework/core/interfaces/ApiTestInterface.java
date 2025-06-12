package com.automation.framework.core.interfaces;

public interface ApiTestInterface {

    /**
     * Setup test data before test execution
     */
    void setupTestData();

    /**
     * Cleanup test data after test execution
     */
    void cleanupTestData();
}