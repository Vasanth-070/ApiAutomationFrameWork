package com.automation.framework.services.payment.endpoints;

public class PaymentEndpoints {
    
    // Payment Form API - Creates payment transaction and returns transaction ID
    public static final String PAYMENT_FORM = "/payments/v1/internal/form";
    
    // Payment Init API V4 - Initialize payment transaction
    public static final String PAYMENT_INIT_V4 = "/payments/v4/internal/init";
    
    // Payment Transaction API - Retrieves payment details using transaction ID
    public static final String PAYMENT_TRANSACTION_V4 = "/payments/v4/transaction/{paymentTransactionId}";
    
    // Payment Transaction API V1 - Alternative endpoint
    public static final String PAYMENT_TRANSACTION_V1 = "/payments/v1/transaction/{paymentId}";
    
    // Payment Mode Setting
    public static final String PAYMENT_MODE_V4 = "/payments/v4/payment-mode";
    
    // Payment Callback
    public static final String PAYMENT_CALLBACK = "/payments/v4/express/callback/{paymentTransactionId}";
}