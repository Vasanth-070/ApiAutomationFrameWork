package com.automation.framework.services.payment.endpoints;

/**
 * Contains all UPI-related API endpoints
 */
public class UpiEndpoints {
    
    // Step 1: UPI Validation API - Validates UPI ID when user enters it
    public static final String UPI_VALIDATION = "/payments/v2/upi/{upi}";
    
    // Step 2: Payment Mode API - Called when user clicks Pay button
    public static final String PAYMENT_MODE = "/payments/v4/payment-mode";
    
    // Step 3: User Bank Details API - Saves UPI details
    public static final String USER_BANK_DETAILS = "/payments/v1/user-bank-details";
    
    // Step 4: Juspay Transaction API - External payment gateway
    public static final String JUSPAY_TRANSACTION = "https://api.juspay.in/txns";
} 