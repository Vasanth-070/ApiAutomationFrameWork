package com.automation.framework.core.interfaces;

import java.util.Map;

public interface DataProviderInterface {

    /**
     * Get test data by key
     */
    Object getTestData(String key);

    /**
     * Get all test data as map
     */
    Map<String, Object> getAllTestData();

    /**
     * Get test data for specific test case
     */
    Map<String, Object> getTestDataForCase(String testCaseName);

    /**
     * Load test data from file
     */
    void loadTestData(String filePath);
}