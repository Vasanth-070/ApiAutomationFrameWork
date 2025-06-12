package com.automation.tests.services.payment;

import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.FlightEndpoints;
import com.automation.framework.services.payment.models.PaymentInitResponse;
import com.automation.framework.services.payment.validators.PaymentResponseValidator;
import com.automation.framework.shared.utils.HttpMethod;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaymentPageApiTests extends BaseApiTest {

    // Test Constants - to avoid string literals
    private static final String TEST_SUITE_NAME = "Flight Booking API Test Suite";
    private static final String TEST_METHOD_NAME = "Payment Init API Comprehensive Test";
    private static final String TEST_DESCRIPTION = "Comprehensive validation of Payment Init API with schema, business rules, and performance checks";
    private static final String TEST_DATA_SETUP_MESSAGE = "Setting up test data for Payment API tests";
    private static final String TEST_DATA_CLEANUP_MESSAGE = "Cleaning up test data for Payment API tests";
    private static final String TRIP_ID = "01JXG6YG1JPTJKEWS3PK9VQWAWYJ11XG6G0";
    
    // Validation test names
    private static final String UPI_BUSINESS_RULES_VALIDATION = "UPI business rules validation";

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
    public void testPaymentInitComprehensive() {
        executeTest(TEST_METHOD_NAME, TEST_DESCRIPTION, () -> {
            String tripId = TRIP_ID;
            String endpoint = FlightEndpoints.Payment_init.replace("{transactionId}", tripId);

            // BaseApiTest automatically adds auth headers, we only need monitoring headers
            Response response = makeApiCall(HttpMethod.GET, endpoint, getMonitoringHeaders(), null);

            validateWithLogging(UPI_BUSINESS_RULES_VALIDATION, () -> {
                try {
                    PaymentInitResponse paymentResponse = PaymentResponseValidator.parsePaymentResponse(response);
                    if (paymentResponse.getData() != null && paymentResponse.getData().getUpi() != null) {
                        PaymentResponseValidator.validateUpiData(paymentResponse.getData().getUpi());
                    }
                } catch (IOException e) {
                    throw new AssertionError("UPI business rules validation failed: " + e.getMessage(), e);
                }
            });
        });
    }

}