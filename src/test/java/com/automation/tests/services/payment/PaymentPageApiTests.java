package com.automation.tests.services.payment;
import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.PaymentEndpoints;
import com.automation.framework.services.payment.models.PaymentFormRequest;
import com.automation.framework.services.payment.models.PaymentFormResponse;
import com.automation.framework.services.payment.validators.PaymentFormValidator;
import com.automation.framework.services.payment.PaymentFormApiRequestGenerator;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
public class PaymentPageApiTests extends BaseApiTest {
    // Test Suite Configuration
    private static final String TEST_SUITE_NAME = "Flight Booking API Test Suite";
    private static final String TEST_DATA_PATH = "src/test/resources/testData/paymentFlowTestData.json";
    private static final String TEST_DATA_KEY = "testPaymentFlow";
    
    // Payment Form Test Data Configuration
    private static final String PAYMENT_FORM_TEST_DATA_PATH = "src/test/resources/testData/paymentFormTestData.json";
    private static final String PAYMENT_FORM_TEST_DATA_KEY = "paymentFormApi";
    
    // Test Method Configuration
    private static final String TEST_METHOD_NAME = "Payment Init API Comprehensive Test";
    private static final String TEST_DESCRIPTION = "Comprehensive validation of Payment Init API with schema, business rules, and performance checks";
    
    // Test Messages
    private static final String TEST_DATA_SETUP_MESSAGE = "Setting up test data for Payment API tests";
    private static final String TEST_DATA_CLEANUP_MESSAGE = "Cleaning up test data for Payment API tests";
    private static final String TEST_DATA_NULL_MESSAGE = "Test data is null! Check if the test data file %s exists and key %s is loaded correctly.";
    
    // Validation Messages
    private static final String VALIDATION_PAYMENT_FORM = "Payment Form Response Status";
    private static final String VALIDATION_FORM_STRUCTURE = "Payment Form Response Structure";
    private static final String VALIDATION_TRANSACTION = "Transaction Response Structure";
    
    // API Headers
    private static final String HEADER_BAGGAGE = "baggage";
    private static final String HEADER_SENTRY_TRACE = "sentry-trace";
    private static final String PROP_BAGGAGE = "api.baggage";
    private static final String PROP_SENTRY_TRACE = "api.sentry.trace";
    
    // Service instances
    private PaymentFormApiRequestGenerator paymentFormRequestGenerator;
    private PaymentFormValidator paymentFormValidator;
    
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
        testDataProvider.loadTestData(TEST_DATA_PATH, PAYMENT_FORM_TEST_DATA_PATH);
        validateTestData();
        
        // Initialize service instances
        paymentFormRequestGenerator = new PaymentFormApiRequestGenerator();
        paymentFormValidator = new PaymentFormValidator();
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
        addHeaderIfExists(headers, HEADER_BAGGAGE, PROP_BAGGAGE);
        addHeaderIfExists(headers, HEADER_SENTRY_TRACE, PROP_SENTRY_TRACE);
        return headers;
    }

    private void addHeaderIfExists(Map<String, String> headers, String headerName, String propertyKey) {
        String value = apiConfig.getProperty(propertyKey);
        if (value != null && !value.trim().isEmpty()) {
            headers.put(headerName, value);
        }
    }

    protected String createNewPaymentTransactionId() {
        try {
            String uniqueId = generateUniqueId();
            PaymentFormRequest request = paymentFormRequestGenerator.createPaymentFormRequest(getPaymentFormData());
            String transactionId = createPaymentTransaction(request, uniqueId);
            testLogger.logInfo("Created payment transaction with ID: " + transactionId);
            return transactionId;
        } catch (Exception e) {
            testLogger.logError("Failed to create payment transaction", e);
            throw new RuntimeException("Failed to create payment transaction", e);
        }
    }

    private String generateUniqueId() {
        String uniqueId = UUID.randomUUID().toString();
        testLogger.logInfo("Generated unique ID: " + uniqueId);
        return uniqueId;
    }

    private JsonNode getPaymentFormData() {
        Object testDataObj = testDataProvider.getTestData(PAYMENT_FORM_TEST_DATA_KEY);
        if (testDataObj == null) {
            throw new IllegalStateException("Payment Form test data not loaded! Ensure performTestSetup() is called first.");
        }
        return objectMapper.valueToTree(testDataObj).get("paymentFormRequest");
    }

    private String createPaymentTransaction(PaymentFormRequest request, String uniqueId) throws Exception {
        String productTransactionId = request.getProductTransactionId().replace("#{transactionId}", uniqueId);
        request.setProductTransactionId(productTransactionId);

        Response response = makeApiCall(
            HttpMethod.POST,
            PaymentEndpoints.PAYMENT_FORM,
            getMonitoringHeaders(),
            objectMapper.writeValueAsString(request)
        );

        return extractTransactionId(response);
    }

    private String extractTransactionId(Response response) throws IOException {
        JsonNode responseJson = objectMapper.readTree(response.asString());
        return responseJson.path("data").path("paymentTransactionId").asText();
    }

    @Test(description = TEST_DESCRIPTION)
    public void testCompletePaymentFlowAndInitComprehensive() {
        executeTest(TEST_METHOD_NAME, TEST_DESCRIPTION, () -> {
            // Create and validate payment form
            PaymentFormRequest request = paymentFormRequestGenerator.createPaymentFormRequest(
                objectMapper.valueToTree(testDataProvider.getTestData(PAYMENT_FORM_TEST_DATA_KEY))
                    .get("paymentFormRequest")
            );
            
            Response postResponse = makeApiCall(
                HttpMethod.POST,
                PaymentEndpoints.PAYMENT_FORM,
                objectMapper.writeValueAsString(request)
            );

            validateWithLogging(VALIDATION_PAYMENT_FORM, () -> 
                responseValidator.validateStatusCode(postResponse, 200)
            );

            // Extract transaction ID and validate structure
            final String[] paymentTransactionId = new String[1];
            validateWithLogging(VALIDATION_FORM_STRUCTURE, () -> {
                PaymentFormResponse paymentResponse = objectMapper.readValue(
                    postResponse.asString(),
                    PaymentFormResponse.class
                );
                String responseJson = paymentFormValidator.validateAndSerializeResponse(paymentResponse);
                testLogger.logInfo("Full PaymentFormResponse: " + responseJson);
                testLogger.logInfo("✓ Payment form response validation passed");
                paymentTransactionId[0] = paymentResponse.getData().getPaymentTransactionId();
            });

            // Get and validate transaction details
            String endpoint = PaymentEndpoints.PAYMENT_TRANSACTION_V4
                .replace("{paymentTransactionId}", paymentTransactionId[0]);
            Response getResponse = makeApiCall(HttpMethod.GET, endpoint);

            validateWithLogging(VALIDATION_TRANSACTION, () -> {
                paymentFormValidator.validateTransactionResponse(getResponse);
                testLogger.logInfo("✓ Transaction response validation passed");
            });

            // validateWithLogging(VALIDATION_UPI_RULES, () -> {
            //     PaymentInitResponse paymentResponse = PaymentResponseValidator
            //         .parsePaymentResponse(getResponse);
            //     if (paymentResponse.getData() != null && 
            //         paymentResponse.getData().getUpi() != null) {
            //         PaymentResponseValidator.validateUpiData(
            //             paymentResponse.getData().getUpi()
            //         );
            //     }
            // });
        });
    }




    protected void throwAssertionError(String message) {
        throw new AssertionError(message);
    }

    private void validateTestData() {
        if (testDataProvider.getTestData(TEST_DATA_KEY) == null) {
            throw new IllegalStateException(
                String.format(TEST_DATA_NULL_MESSAGE, TEST_DATA_PATH, TEST_DATA_KEY)
            );
        }
    }
}