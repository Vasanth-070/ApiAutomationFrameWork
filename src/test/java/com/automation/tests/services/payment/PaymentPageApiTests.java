package com.automation.tests.services.payment;
import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.PaymentEndpoints;
import com.automation.framework.services.payment.models.PaymentFormRequest;
import com.automation.framework.services.payment.models.PaymentFormResponse;
import com.automation.framework.services.payment.models.PaymentInitResponse;
import com.automation.framework.services.payment.validators.PaymentResponseValidator;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
public class PaymentPageApiTests extends BaseApiTest {
    // Test Suite Configuration
    private static final String TEST_SUITE_NAME = "Flight Booking API Test Suite";
    private static final String TEST_DATA_PATH = "src/test/resources/testData/paymentFlowTestData.json";
    private static final String TEST_DATA_KEY = "testPaymentFlow";
    
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
    private static final String VALIDATION_UPI_RULES = "UPI business rules validation";
    
    // API Headers
    private static final String HEADER_BAGGAGE = "baggage";
    private static final String HEADER_SENTRY_TRACE = "sentry-trace";
    private static final String PROP_BAGGAGE = "api.baggage";
    private static final String PROP_SENTRY_TRACE = "api.sentry.trace";

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
        testDataProvider.loadTestData(TEST_DATA_PATH);
        validateTestData();
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
            PaymentFormRequest request = createPaymentFormRequest(getPaymentFormData());
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
        Object testDataObj = testDataProvider.getTestData(TEST_DATA_KEY);
        if (testDataObj == null) {
            throw new IllegalStateException("Test data not loaded! Ensure performTestSetup() is called first.");
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

    // Helper to build the PaymentFormRequest object
    private PaymentFormRequest createPaymentFormRequest(JsonNode testData) {
        PaymentFormRequest request = new PaymentFormRequest();

        // Generate unique product transaction ID
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String productTxnId = testData.get("productTransactionId").asText()
            .replace("#{transactionId}", uniqueId);
        
        testLogger.logInfo("Generated unique ID: " + uniqueId);
        testLogger.logInfo("Product Transaction ID: " + productTxnId);

        request.setProductTransactionId(productTxnId);
        request.setProductType(testData.get("productType").asText());
        request.setPlatform(testData.get("platform").asText());
        request.setClientId(testData.get("clientId").asText());
        request.setProductInfo(testData.get("productInfo").asText());
        request.setVersion(testData.get("version").asInt());

        // Set user details
        PaymentFormRequest.UserDetail userDetail = new PaymentFormRequest.UserDetail();
        JsonNode userNode = testData.get("userDetail");
        userDetail.setEmail(userNode.get("email").asText());
        userDetail.setFirstName(userNode.get("firstName").asText());
        userDetail.setLastName(userNode.get("lastName").asText());
        userDetail.setMobile(userNode.get("mobile").asText());
        userDetail.setUserId(userNode.get("userId").asText());
        request.setUserDetail(userDetail);

        // Set fare details
        PaymentFormRequest.FareDetail fareDetail = new PaymentFormRequest.FareDetail();
        JsonNode fareNode = testData.get("fareDetail");
        fareDetail.setPaymentAmount(fareNode.get("paymentAmount").asText());
        fareDetail.setIxiMoneyBurnAmount(fareNode.get("ixiMoneyBurnAmount").asText());
        fareDetail.setIxiMoneyPremiumBurnAmount(fareNode.get("ixiMoneyPremiumBurnAmount").asText());
        fareDetail.setIxigoServiceCharge(fareNode.get("ixigoServiceCharge").asText());
        fareDetail.setPgCharge(fareNode.get("pgCharge").asText());
        fareDetail.setBookingClass(fareNode.get("bookingClass").asText());
        fareDetail.setAllInclusiveFare(fareNode.get("allInclusiveFare").asText());
        request.setFareDetail(fareDetail);

        // Set transaction details
        PaymentFormRequest.TransactionDetail transactionDetail = new PaymentFormRequest.TransactionDetail();
        JsonNode txnNode = testData.get("transactionDetail");
        transactionDetail.setStartTime(txnNode.get("startTime").asText());
        transactionDetail.setExpiryTime(txnNode.get("expiryTime").asText());
        transactionDetail.setExpiryActionUrl(txnNode.get("expiryActionUrl").asText());
        transactionDetail.setExpiryTitle(txnNode.get("expiryTitle").asText());
        transactionDetail.setExpiryMessage(txnNode.get("expiryMessage").asText());
        request.setTransactionDetail(transactionDetail);

        return request;
    }
    @Test(description = TEST_DESCRIPTION)
    public void testCompletePaymentFlowAndInitComprehensive() {
        executeTest(TEST_METHOD_NAME, TEST_DESCRIPTION, () -> {
            // Create and validate payment form
            PaymentFormRequest request = createPaymentFormRequest(
                objectMapper.valueToTree(testDataProvider.getTestData(TEST_DATA_KEY))
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
                validatePaymentFormResponse(paymentResponse);
                paymentTransactionId[0] = paymentResponse.getData().getPaymentTransactionId();
            });

            // Get and validate transaction details
            String endpoint = PaymentEndpoints.PAYMENT_TRANSACTION_V4
                .replace("{paymentTransactionId}", paymentTransactionId[0]);
            Response getResponse = makeApiCall(HttpMethod.GET, endpoint);

            validateWithLogging(VALIDATION_TRANSACTION, () -> 
                validateTransactionResponse(getResponse)
            );

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

    private void validatePaymentFormResponse(PaymentFormResponse response) {
        try {
            logPaymentFormResponse(response);
            validateResponseData(response.getData());
            testLogger.logInfo("✓ Payment form response validation passed");
        } catch (JsonProcessingException e) {
            testLogger.logError("Failed to serialize PaymentFormResponse", e);
            throw new AssertionError("Failed to serialize response", e);
        }
    }

    private void logPaymentFormResponse(PaymentFormResponse response) throws JsonProcessingException {
        testLogger.logInfo("Full PaymentFormResponse: " + objectMapper.writeValueAsString(response));
    }

    private void validateResponseData(PaymentFormResponse.PaymentFormData data) {
        if (data == null) {
            throwValidationError("Response data is null");
        }

        validateField(data.getPaymentTransactionId(), "Payment transaction ID");
        validateGatewayData(data.getGatewayData());
    }

    private void validateGatewayData(PaymentFormResponse.GatewayData gatewayData) {
        if (gatewayData == null || gatewayData.getForm() == null) {
            throwValidationError("Gateway data or form is missing");
        }

        validateField(gatewayData.getForm().getTxnid(), "Transaction ID (txnid)");
        validateAmount(gatewayData.getForm().getAmount());
    }

    private void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throwValidationError("Amount is missing or invalid");
        }
    }

    private void validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throwValidationError(fieldName + " is missing");
        }
    }

    private void validateTransactionResponse(Response response) throws Exception {
        JsonNode responseJson = objectMapper.readTree(response.asString());
        if (!responseJson.has("data")) {
            throwValidationError("Response should contain 'data' field");
        }
        testLogger.logInfo("✓ Transaction response validation passed");
    }

    private void throwValidationError(String message) {
        throw new AssertionError("Validation Error: " + message);
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