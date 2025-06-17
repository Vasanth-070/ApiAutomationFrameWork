package com.automation.framework.services.payment.validators;

import com.automation.framework.services.payment.models.PaymentInitResponse;
import com.automation.framework.services.payment.models.UpiData;
import com.automation.framework.services.payment.models.UpiOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;

import java.io.IOException;
import java.util.List;

/**
 * Payment-specific response validator with business logic validation
 */
public class PaymentResponseValidator {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parse response to PaymentInitResponse model
     */
    public static PaymentInitResponse parsePaymentResponse(Response response) throws IOException {
        return objectMapper.readValue(response.getBody().asString(), PaymentInitResponse.class);
    }
    
    /**
     * Validate UPI data structure and business rules
     */
    public static void validateUpiData(UpiData upiData) {
        // Basic structure validation
        if (!upiData.isValid()) {
            throw new AssertionError("UPI data structure validation failed");
        }
        
        // Business rule validations
        validateUpiHeading(upiData.getHeading());
        validateUpiOptions(upiData.getOptions());
    }
    
    /**
     * Validate UPI heading
     */
    private static void validateUpiHeading(String heading) {
        if (heading == null || heading.trim().isEmpty()) {
            throw new AssertionError("UPI heading cannot be null or empty");
        }
        
        if (heading.length() > 100) {
            throw new AssertionError("UPI heading exceeds maximum length of 100 characters");
        }
    }
    
    /**
     * Validate UPI options list
     */
    private static void validateUpiOptions(List<UpiOption> options) {
        if (options == null || options.isEmpty()) {
            throw new AssertionError("UPI options cannot be null or empty");
        }
        
        if (options.size() > 20) {
            throw new AssertionError("Too many UPI options (max 20 allowed)");
        }
        
        // Validate each option
        for (int i = 0; i < options.size(); i++) {
            validateUpiOption(options.get(i), i);
        }
        
        // Check for duplicate payment methods
        long uniquePaymentMethods = options.stream()
            .map(UpiOption::getPaymentMethod)
            .distinct()
            .count();
            
        if (uniquePaymentMethods != options.size()) {
            throw new AssertionError("Duplicate payment methods found in UPI options");
        }
    }
    
    /**
     * Validate individual UPI option
     */
    private static void validateUpiOption(UpiOption option, int index) {
        if (!option.isValid()) {
            throw new AssertionError("UPI option at index " + index + " is invalid");
        }
        
        // Validate transaction type
        if (!isValidTxnType(option.getTxnType())) {
            throw new AssertionError("Invalid transaction type at index " + index);
        }
    }
    
    /**
     * Validate transaction type
     */
    private static boolean isValidTxnType(String txnType) {
        return txnType != null && 
               List.of("UPI_COLLECT", "UPI_PAY").contains(txnType.toUpperCase());
    }
}