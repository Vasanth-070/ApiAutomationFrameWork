package com.automation.framework.services.payment;

import com.automation.framework.services.payment.models.PaymentFormRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request generator class for Payment Form API
 * Provides reusable methods for creating payment form requests
 */
public class PaymentFormApiRequestGenerator {
    
    private final ObjectMapper objectMapper;
    
    public PaymentFormApiRequestGenerator() {
        this.objectMapper = new ObjectMapper();
    }
    
    
    /**
     * Creates PaymentFormRequest object from JSON test data
     * 
     * @param testData JsonNode containing payment form request data
     * @return PaymentFormRequest object ready for API call
     */
    public PaymentFormRequest createPaymentFormRequest(JsonNode testData) {
        PaymentFormRequest request = new PaymentFormRequest();

        // Generate unique product transaction ID
        String uniqueId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String productTxnId = testData.get("productTransactionId").asText()
            .replace("#{transactionId}", uniqueId);

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
    
    /**
     * Creates PaymentFormRequest object with custom transaction ID
     * 
     * @param testData JsonNode containing payment form request data
     * @param customTransactionId Custom transaction ID to use instead of generated one
     * @return PaymentFormRequest object ready for API call
     */
    public PaymentFormRequest createPaymentFormRequest(JsonNode testData, String customTransactionId) {
        PaymentFormRequest request = new PaymentFormRequest();

        // Use provided transaction ID
        String productTxnId = testData.get("productTransactionId").asText()
            .replace("#{transactionId}", customTransactionId);

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
}