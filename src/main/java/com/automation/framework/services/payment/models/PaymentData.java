package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Payment Data model representing the main data section of payment response
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentData {
    
    @JsonProperty("upi")
    private UpiData upi;
    
    // Getters and Setters
    public UpiData getUpi() {
        return upi;
    }
}