package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Main Payment Init API Response model
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentInitResponse {
    
    @JsonProperty("data")
    private PaymentData data;
    
    // Getters and Setters
    public PaymentData getData() {
        return data;
    }

    public void setData(PaymentData data) {
        this.data = data;
    }
}