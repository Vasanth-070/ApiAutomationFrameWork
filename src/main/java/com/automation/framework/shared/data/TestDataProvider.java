package com.automation.framework.shared.data;

import com.automation.framework.core.interfaces.DataProviderInterface;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestDataProvider implements DataProviderInterface {

    private Map<String, Object> testData;
    private ObjectMapper objectMapper;

    public TestDataProvider() {
        this.testData = new HashMap<>();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object getTestData(String key) {
        return testData.get(key);
    }

    @Override
    public Map<String, Object> getAllTestData() {
        return new HashMap<>(testData);
    }

    @Override
    public Map<String, Object> getTestDataForCase(String testCaseName) {
        Object caseData = testData.get(testCaseName);
        if (caseData instanceof Map) {
            return (Map<String, Object>) caseData;
        }
        return new HashMap<>();
    }

    @Override
    public void loadTestData(String filePath) {
        try {
            File file = new File(filePath);
            Map<String, Object> data = objectMapper.readValue(file, Map.class);
            testData.putAll(data);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load test data from: " + filePath, e);
        }
    }
    
    /**
     * Loads test data from multiple JSON files in a single call
     * All data is merged into the same testData map
     * 
     * @param filePaths Variable number of file paths to load
     * @throws RuntimeException if any file fails to load
     */
    @Override
    public void loadTestData(String... filePaths) {
        for (String filePath : filePaths) {
            loadTestData(filePath);
        }
    }
}