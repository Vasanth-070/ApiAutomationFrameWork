package com.automation.tests.services.payment.data;

import com.automation.framework.core.base.BaseApiTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.DataProvider;

public class UpiTestDataProvider extends BaseApiTest {
    
    private static final String UPI_TEST_DATA_PATH = "src/test/resources/testData/payment/upi/upiValidationData.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @DataProvider(name = "validUpiScenarios")
    public Object[][] getValidUpiScenarios() {
        JsonNode testData = loadTestData(UPI_TEST_DATA_PATH);
        JsonNode scenarios = testData.get("validUpiScenarios");
        
        Object[][] data = new Object[scenarios.size()][3];
        for (int i = 0; i < scenarios.size(); i++) {
            JsonNode scenario = scenarios.get(i);
            data[i][0] = scenario.get("upiId").asText();
            data[i][1] = scenario.get("expectedName").asText();
            data[i][2] = scenario.get("description").asText();
        }
        return data;
    }
    
    @DataProvider(name = "invalidUpiScenarios")
    public Object[][] getInvalidUpiScenarios() {
        JsonNode testData = loadTestData(UPI_TEST_DATA_PATH);
        JsonNode scenarios = testData.get("invalidUpiScenarios");
        
        Object[][] data = new Object[scenarios.size()][3];
        for (int i = 0; i < scenarios.size(); i++) {
            JsonNode scenario = scenarios.get(i);
            data[i][0] = scenario.get("upiId").asText();
            data[i][1] = scenario.get("expectedError").asText();
            data[i][2] = scenario.get("description").asText();
        }
        return data;
    }
    
    private JsonNode loadTestData(String path) {
        try {
            return objectMapper.readTree(new java.io.File(path));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data from: " + path, e);
        }
    }

    @Override
    public void defineTestCases() {
        // No test cases needed for data provider
    }

    @Override
    public String getTestSuiteName() {
        return "UPI Test Data Provider";
    }
} 