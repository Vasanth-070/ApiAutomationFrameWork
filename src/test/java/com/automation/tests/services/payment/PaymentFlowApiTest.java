package com.automation.tests.services.payment;

import com.automation.framework.core.base.BaseApiTest;
import com.automation.framework.services.payment.endpoints.PaymentEndpoints;
import com.automation.framework.services.payment.models.PaymentFormRequest;
import com.automation.framework.services.payment.models.PaymentFormResponse;
import com.automation.framework.shared.utils.HttpMethod;
import com.fasterxml.jackson.databind.JsonNode;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.UUID;

/**
 * Payment Flow API Tests
 * Tests the complete payment flow: Create payment transaction → Retrieve payment details
 */
public class PaymentFlowApiTest extends BaseApiTest {

    private static final String TEST_DATA_KEY = "testPaymentFlow";
    private String paymentTransactionId;

    @Override
    public String getTestSuiteName() {
        return "Payment Flow API Tests";
    }

    @Override
    public void defineTestCases() {
        // Test cases are defined as TestNG methods below
        // 1. testCompletePaymentFlow
    }

    @Test(priority = 1, description = "Create payment transaction and retrieve payment details")
    public void testCompletePaymentFlow() {
        executeTest(
            "Complete Payment Flow Test",
            "POST /payments/v1/internal/form → GET /payments/v4/transaction/{id}",
            () -> {
                // Step 1: Create payment transaction
                createPaymentTransaction();
                
                // Step 2: Retrieve payment transaction details
                retrievePaymentTransactionDetails();
            }
        );
    }

    private void createPaymentTransaction() throws Exception {
        // Load test data
        Object testDataObj = testDataProvider.getTestData(TEST_DATA_KEY);
        JsonNode testData = objectMapper.valueToTree(testDataObj);
        JsonNode paymentFormData = testData.get("paymentFormRequest");
        
        // Create request payload with dynamic product transaction ID
        PaymentFormRequest request = createPaymentFormRequest(paymentFormData);
        String requestBody = objectMapper.writeValueAsString(request);
        
        // Make API call
        Response response = makeApiCall(
            HttpMethod.POST, 
            PaymentEndpoints.PAYMENT_FORM, 
            requestBody
        );
        
        // Validate response status code
        validateWithLogging("Payment Form Response Status", () -> {
            responseValidator.validateStatusCode(response, 200);
        });
        
        // Validate response structure and extract transaction ID
        validateWithLogging("Payment Form Response Structure", () -> {
            // Parse response and extract payment transaction ID
            PaymentFormResponse paymentResponse = objectMapper.readValue(
                response.asString(), 
                PaymentFormResponse.class
            );
            
            // Validate response structure
            validatePaymentFormResponse(paymentResponse);
            
            // Store payment transaction ID for next step
            paymentTransactionId = paymentResponse.getData().getPaymentTransactionId();
            testLogger.logInfo("Payment Transaction ID: " + paymentTransactionId);
        });
    }

    private void retrievePaymentTransactionDetails() throws Exception {
        // Validate we have transaction ID from previous step
        if (paymentTransactionId == null || paymentTransactionId.isEmpty()) {
            throwAssertionError("Payment transaction ID is required for this test");
        }
        
        // Build endpoint with transaction ID
        String endpoint = PaymentEndpoints.PAYMENT_TRANSACTION_V4
            .replace("{paymentTransactionId}", paymentTransactionId);
        
        // Make API call
        Response response = makeApiCall(HttpMethod.GET, endpoint);
        
        // Validate response structure
        validateWithLogging("Transaction Response Structure", () -> {
            validateTransactionResponse(response);
        });
    }

    private PaymentFormRequest createPaymentFormRequest(JsonNode testData) {
        PaymentFormRequest request = new PaymentFormRequest();
        
        // Generate unique product transaction ID
        String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String productTxnId = testData.get("productTransactionId").asText()
            .replace("#{randomId}", uniqueId);
        
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

    private void validatePaymentFormResponse(PaymentFormResponse response) {
        if (response.getData() == null) {
            throwAssertionError("Response data is null");
        }
        
        PaymentFormResponse.PaymentFormData data = response.getData();
        
        if (data.getPaymentTransactionId() == null || data.getPaymentTransactionId().isEmpty()) {
            throwAssertionError("Payment transaction ID is missing in response");
        }
        
        if (data.getGatewayData() == null || data.getGatewayData().getForm() == null) {
            throwAssertionError("Gateway data or form is missing in response");
        }
        
        if (data.getGatewayData().getForm().getTxnid() == null) {
            throwAssertionError("Transaction ID (txnid) is missing in gateway form");
        }
        
        if (data.getGatewayData().getForm().getAmount() == null || 
            data.getGatewayData().getForm().getAmount() <= 0) {
            throwAssertionError("Amount is missing or invalid in gateway form");
        }
        
        testLogger.logInfo("✓ Payment form response validation passed");
    }

    private void validateTransactionResponse(Response response) throws Exception {
        JsonNode responseJson = objectMapper.readTree(response.asString());
        
        // Check if response has the expected structure
        if (!responseJson.has("data")) {
            throwAssertionError("Response should contain 'data' field");
        }
        
        // Additional validations can be added based on actual API response structure
        testLogger.logInfo("✓ Transaction response validation passed");
        testLogger.logInfo("Transaction details retrieved successfully for ID: " + paymentTransactionId);
    }

    @Override
    public void performTestSetup() {
        testLogger.logInfo("Setting up Payment Flow API Tests");
        // Load test data to verify it exists
        testDataProvider.loadTestData("src/test/resources/testData/paymentFlowTestData.json");
    }

    @Override
    public void performTestCleanup() {
        testLogger.logInfo("Cleaning up Payment Flow API Tests");
        // Any cleanup logic can be added here
    }

}