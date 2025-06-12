package com.automation.tests.services.payment;

import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.FlightEndpoints;
import com.automation.framework.shared.utils.HttpMethod;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentPageApiTests extends BaseApiTest {

    // Test Constants - to avoid string literals
    private static final String TEST_NAME = "Flight Booking API Tests";
    private static final String TEST_SUITE_NAME = "Flight Booking API Test Suite";
    private static final String TEST_METHOD_NAME = "Ixigo Flight Trip Details";
    private static final String TEST_DESCRIPTION = "Test Ixigo Flight Trip Details API call with authentication";
    private static final String TEST_DATA_SETUP_MESSAGE = "Setting up test data for Flight Booking API tests";
    private static final String TEST_DATA_CLEANUP_MESSAGE = "Cleaning up test data for Flight Booking API tests";
    private static final String TRIP_ID = "01JXG6YG1JPTJKEWS3PK9VQWAWYJ11XG6G0";
    private static final String UPI_DATA_STRUCTURE_VALIDATION = "UPI data structure";
    private static final String UPI_OPTIONS_VALIDATION = "UPI options";
    private static final String UPI_HEADING_EMPTY_ERROR = "UPI heading should not be empty";
    private static final String UPI_OPTIONS_EMPTY_ERROR = "UPI options should not be empty";
    private static final String UPI_HEADING_PATH = "data.upi.heading";
    private static final String UPI_OPTIONS_PATH = "data.upi.options";
    private static final String[] REQUIRED_FIELDS = {"name", "paymentMethod", "paymentReference", "txnType"};

    // ==================== REQUIRED ABSTRACT METHOD IMPLEMENTATIONS ====================

    @Override
    public String getTestSuiteName() {
        return TEST_SUITE_NAME;
    }


    @Override
    public void defineTestCases() {
        // Actual test cases are defined as TestNG methods below:
        // - testGetFlightTripDetails(): Tests flight booking API with UPI validation
    }

    @Override
    public void performTestSetup() {
        testLogger.logInfo(TEST_DATA_SETUP_MESSAGE);
        // Additional test-specific setup can be added here
    }

    @Override
    public void performTestCleanup() {
        testLogger.logInfo(TEST_DATA_CLEANUP_MESSAGE);
        // Additional test-specific cleanup can be added here
    }

    /**
     * Get monitoring headers (optional for this API)
     * BaseApiTest automatically handles all authentication headers
     */
    private Map<String, String> getMonitoringHeaders() {
        Map<String, String> headers = new HashMap<>();

        // Optional monitoring headers
        String baggage = apiConfig.getProperty("api.baggage");
        if (baggage != null) {
            headers.put("baggage", baggage);
        }

        String sentryTrace = apiConfig.getProperty("api.sentry.trace");
        if (sentryTrace != null) {
            headers.put("sentry-trace", sentryTrace);
        }

        return headers;
    }

    @Test(description = TEST_DESCRIPTION)
    public void testGetFlightTripDetails() {
        executeTest(TEST_METHOD_NAME, TEST_DESCRIPTION, () -> {
            String tripId = TRIP_ID;
            String endpoint = FlightEndpoints.Payment_init.replace("{transactionId}", tripId);

            // BaseApiTest automatically adds auth headers, we only need monitoring headers
            Response response = makeApiCall(HttpMethod.GET, endpoint, getMonitoringHeaders(), null);

            validateWithLogging(UPI_DATA_STRUCTURE_VALIDATION, () -> validateUpiDataStructure(response));
            validateWithLogging(UPI_OPTIONS_VALIDATION, () -> validateUpiOptions(response));
        });
    }

    /**
     * Validate UPI data - heading should not be empty
     *
     * @param response - API response containing UPI data
     */
    private void validateUpiDataStructure(Response response) {
        String upiHeading = response.jsonPath().getString(UPI_HEADING_PATH);

        if (upiHeading == null || upiHeading.trim().isEmpty()) {
            throwAssertionError(UPI_HEADING_EMPTY_ERROR);
        }
    }

    /**
     * Validate UPI options array and required fields
     *
     * @param response - API response containing UPI data
     */
    private void validateUpiOptions(Response response) {
        List<Object> upiOptions = response.jsonPath().getList(UPI_OPTIONS_PATH);

        if (upiOptions == null || upiOptions.isEmpty()) {
            throwAssertionError(UPI_OPTIONS_EMPTY_ERROR);
        }

        for (int i = 0; i < upiOptions.size(); i++) {
            validateUpiOptionRequiredFields(response, i);
        }
    }

    /**
     * Validate required fields for each UPI option
     *
     * @param response - API response
     * @param index    - index of the option to validate
     */
    private void validateUpiOptionRequiredFields(Response response, int index) {
        String basePath = "data.upi.options[" + index + "]";

        for (String field : REQUIRED_FIELDS) {
            String value = response.jsonPath().getString(basePath + "." + field);
            if (value == null || value.trim().isEmpty()) {
                throwAssertionError("UPI option " + index + " should have non-empty " + field);
            }
        }
    }
}