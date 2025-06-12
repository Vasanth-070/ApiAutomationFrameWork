package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * UPI Option model representing individual UPI payment option
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpiOption {
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("paymentMethod")
    private String paymentMethod;
    
    @JsonProperty("paymentReference")
    private String paymentReference;
    
    @JsonProperty("txnType")
    private String txnType;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getTxnType() {
        return txnType;
    }


    // Business validation methods
    public boolean hasRequiredFields() {
        return name != null && !name.trim().isEmpty() &&
               paymentMethod != null && !paymentMethod.trim().isEmpty() &&
               paymentReference != null && !paymentReference.trim().isEmpty() &&
               txnType != null && !txnType.trim().isEmpty();
    }

    public boolean isValid() {
        return hasRequiredFields();
    }

    @Override
    public String toString() {
        return "UpiOption{" +
                "name='" + name + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", paymentReference='" + paymentReference + '\'' +
                ", txnType='" + txnType + '\'' +
                '}';
    }
}