package com.automation.framework.services.payment.validators;

import com.automation.framework.services.payment.models.PaymentFormResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

/**
 * Validator class for Payment Form API responses
 * Provides comprehensive validation methods for payment form related operations
 */
public class PaymentFormValidator {
    
    private final ObjectMapper objectMapper;
    
    public PaymentFormValidator() {
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Validates complete PaymentFormResponse including serialization and data validation
     * 
     * @param response PaymentFormResponse object to validate
     * @throws AssertionError if validation fails
     */
    public void validatePaymentFormResponse(PaymentFormResponse response) {
        try {
            validateResponseData(response.getData());
        } catch (Exception e) {
            throw new AssertionError("Payment form response validation failed", e);
        }
    }
    
    /**
     * Validates PaymentFormResponse and returns JSON string for logging
     * 
     * @param response PaymentFormResponse object to validate
     * @return JSON string representation of the response
     * @throws AssertionError if validation fails
     */
    public String validateAndSerializeResponse(PaymentFormResponse response) {
        try {
            validateResponseData(response.getData());
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Failed to serialize PaymentFormResponse", e);
        } catch (Exception e) {
            throw new AssertionError("Payment form response validation failed", e);
        }
    }
    
    /**
     * Validates PaymentFormData object
     * 
     * @param data PaymentFormData object to validate
     * @throws AssertionError if validation fails
     */
    public void validateResponseData(PaymentFormResponse.PaymentFormData data) {
        if (data == null) {
            throwValidationError("Response data is null");
        }
        
        validateField(data.getPaymentTransactionId(), "Payment transaction ID");
        validateGatewayData(data.getGatewayData());
    }
    
    /**
     * Validates gateway data and form information
     * 
     * @param gatewayData GatewayData object to validate
     * @throws AssertionError if validation fails
     */
    public void validateGatewayData(PaymentFormResponse.GatewayData gatewayData) {
        if (gatewayData == null || gatewayData.getForm() == null) {
            throwValidationError("Gateway data or form is missing");
        }
        
        validateField(gatewayData.getForm().getTxnid(), "Transaction ID (txnid)");
        validateAmount(gatewayData.getForm().getAmount());
    }
    
    /**
     * Validates amount field
     * 
     * @param amount Amount to validate
     * @throws AssertionError if validation fails
     */
    public void validateAmount(Double amount) {
        if (amount == null || amount <= 0) {
            throwValidationError("Amount is missing or invalid");
        }
    }
    
    /**
     * Validates string field for null or empty values
     * 
     * @param value String value to validate
     * @param fieldName Name of the field for error messaging
     * @throws AssertionError if validation fails
     */
    public void validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throwValidationError(fieldName + " is missing");
        }
    }
    
    /**
     * Validates transaction response from API call
     * 
     * @param response Response object from transaction API
     * @throws AssertionError if validation fails
     */
    public void validateTransactionResponse(Response response) {
        try {
            JsonNode responseJson = objectMapper.readTree(response.asString());
            if (!responseJson.has("data")) {
                throwValidationError("Response should contain 'data' field");
            }
        } catch (Exception e) {
            throwValidationError("Failed to parse transaction response: " + e.getMessage());
        }
    }
    
    /**
     * Validates payment form response against expected criteria
     * 
     * @param response PaymentFormResponse to validate
     * @param expectedCriteria JsonNode containing validation criteria
     * @throws AssertionError if validation fails
     */
    public void validateAgainstCriteria(PaymentFormResponse response, JsonNode expectedCriteria) {
        // Validate basic response structure first
        validatePaymentFormResponse(response);
        
        // Validate against expected criteria
        if (expectedCriteria.has("requiredFields")) {
            validateRequiredFields(response, expectedCriteria.get("requiredFields"));
        }
        
        if (expectedCriteria.has("validations")) {
            validateBusinessRules(response, expectedCriteria.get("validations"));
        }
    }
    
    /**
     * Validates that all required fields are present in the response
     * 
     * @param response PaymentFormResponse to check
     * @param requiredFields JsonNode array of required field paths
     */
    private void validateRequiredFields(PaymentFormResponse response, JsonNode requiredFields) {
        try {
            JsonNode responseJson = objectMapper.valueToTree(response);
            
            for (JsonNode fieldPath : requiredFields) {
                String field = fieldPath.asText();
                if (!hasField(responseJson, field)) {
                    throwValidationError("Required field missing: " + field);
                }
            }
        } catch (Exception e) {
            throwValidationError("Failed to validate required fields: " + e.getMessage());
        }
    }
    
    /**
     * Validates business rules against the response
     * 
     * @param response PaymentFormResponse to validate
     * @param validations JsonNode containing validation rules
     */
    private void validateBusinessRules(PaymentFormResponse response, JsonNode validations) {
        if (validations.has("paymentTransactionIdExists") && 
            validations.get("paymentTransactionIdExists").asBoolean()) {
            validateField(response.getData().getPaymentTransactionId(), "Payment Transaction ID");
        }
        
        if (validations.has("txnidExists") && 
            validations.get("txnidExists").asBoolean()) {
            validateField(response.getData().getGatewayData().getForm().getTxnid(), "Transaction ID (txnid)");
        }
        
        if (validations.has("amountGreaterThan")) {
            double expectedMinAmount = validations.get("amountGreaterThan").asDouble();
            Double actualAmount = response.getData().getGatewayData().getForm().getAmount();
            if (actualAmount == null || actualAmount <= expectedMinAmount) {
                throwValidationError("Amount should be greater than " + expectedMinAmount + ", but was " + actualAmount);
            }
        }
        
        if (validations.has("gatewayDataExists") && 
            validations.get("gatewayDataExists").asBoolean()) {
            if (response.getData().getGatewayData() == null) {
                throwValidationError("Gateway data should exist");
            }
        }
    }
    
    /**
     * Checks if a field exists in the JSON response using dot notation
     * 
     * @param json JsonNode to search in
     * @param fieldPath Field path in dot notation (e.g., "data.gatewayData.form.txnid")
     * @return true if field exists, false otherwise
     */
    private boolean hasField(JsonNode json, String fieldPath) {
        String[] parts = fieldPath.split("\\.");
        JsonNode current = json;
        
        for (String part : parts) {
            if (current == null || !current.has(part)) {
                return false;
            }
            current = current.get(part);
        }
        
        return current != null && !current.isNull();
    }
    
    /**
     * Utility method to throw validation errors with consistent format
     * 
     * @param message Error message
     * @throws AssertionError Always throws this exception
     */
    private void throwValidationError(String message) {
        throw new AssertionError("Validation Error: " + message);
    }
}