package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Payment Data model representing the main data section of payment response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentData {

    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("paymentTransactionId")
    private String paymentTransactionId;

    @JsonProperty("upi")
    private UpiData upi;
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }
    
    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    // Getters and Setters
    public UpiData getUpi() {
        return upi;
    }

    public void setUpi(UpiData upi) {
        this.upi = upi;
    }
}