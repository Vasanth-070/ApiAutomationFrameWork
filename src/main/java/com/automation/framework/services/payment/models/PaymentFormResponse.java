package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentFormResponse {
    
    @JsonProperty("data")
    private PaymentFormData data;

    public PaymentFormData getData() {
        return data;
    }

    public void setData(PaymentFormData data) {
        this.data = data;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PaymentFormData {
        
        @JsonProperty("gateway")
        private String gateway;
        
        @JsonProperty("gatewayData")
        private GatewayData gatewayData;
        
        @JsonProperty("fallbackUrl")
        private String fallbackUrl;
        
        @JsonProperty("paymentTransactionId")
        private String paymentTransactionId;

        // Getters and Setters
        public String getGateway() {
            return gateway;
        }

        public void setGateway(String gateway) {
            this.gateway = gateway;
        }

        public GatewayData getGatewayData() {
            return gatewayData;
        }

        public void setGatewayData(GatewayData gatewayData) {
            this.gatewayData = gatewayData;
        }

        public String getFallbackUrl() {
            return fallbackUrl;
        }

        public void setFallbackUrl(String fallbackUrl) {
            this.fallbackUrl = fallbackUrl;
        }

        public String getPaymentTransactionId() {
            return paymentTransactionId;
        }

        public void setPaymentTransactionId(String paymentTransactionId) {
            this.paymentTransactionId = paymentTransactionId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GatewayData {
        
        @JsonProperty("form")
        private FormData form;
        
        @JsonProperty("actionUrl")
        private String actionUrl;

        public FormData getForm() {
            return form;
        }

        public void setForm(FormData form) {
            this.form = form;
        }

        public String getActionUrl() {
            return actionUrl;
        }

        public void setActionUrl(String actionUrl) {
            this.actionUrl = actionUrl;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FormData {
        
        @JsonProperty("amount")
        private Double amount;
        
        @JsonProperty("txnid")
        private String txnid;

        public Double getAmount() {
            return amount;
        }

        public void setAmount(Double amount) {
            this.amount = amount;
        }

        public String getTxnid() {
            return txnid;
        }

        public void setTxnid(String txnid) {
            this.txnid = txnid;
        }
    }
}