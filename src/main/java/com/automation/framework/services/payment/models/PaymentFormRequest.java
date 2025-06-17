package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PaymentFormRequest {
    
    @JsonProperty("productTransactionId")
    private String productTransactionId;
    
    @JsonProperty("productType")
    private String productType;
    
    @JsonProperty("userDetail")
    private UserDetail userDetail;
    
    @JsonProperty("fareDetail")
    private FareDetail fareDetail;
    
    @JsonProperty("transactionDetail")
    private TransactionDetail transactionDetail;
    
    @JsonProperty("platform")
    private String platform;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("productInfo")
    private String productInfo;
    
    @JsonProperty("version")
    private Integer version;

    // Getters and Setters
    public String getProductTransactionId() {
        return productTransactionId;
    }

    public void setProductTransactionId(String productTransactionId) {
        this.productTransactionId = productTransactionId;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public UserDetail getUserDetail() {
        return userDetail;
    }

    public void setUserDetail(UserDetail userDetail) {
        this.userDetail = userDetail;
    }

    public FareDetail getFareDetail() {
        return fareDetail;
    }

    public void setFareDetail(FareDetail fareDetail) {
        this.fareDetail = fareDetail;
    }

    public TransactionDetail getTransactionDetail() {
        return transactionDetail;
    }

    public void setTransactionDetail(TransactionDetail transactionDetail) {
        this.transactionDetail = transactionDetail;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(String productInfo) {
        this.productInfo = productInfo;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    // Inner classes
    public static class UserDetail {
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("firstName")
        private String firstName;
        
        @JsonProperty("lastName")
        private String lastName;
        
        @JsonProperty("mobile")
        private String mobile;
        
        @JsonProperty("userId")
        private String userId;

        // Getters and Setters
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }

    public static class FareDetail {
        @JsonProperty("paymentAmount")
        private String paymentAmount;
        
        @JsonProperty("ixiMoneyBurnAmount")
        private String ixiMoneyBurnAmount;
        
        @JsonProperty("ixiMoneyPremiumBurnAmount")
        private String ixiMoneyPremiumBurnAmount;
        
        @JsonProperty("ixigoServiceCharge")
        private String ixigoServiceCharge;
        
        @JsonProperty("pgCharge")
        private String pgCharge;
        
        @JsonProperty("bookingClass")
        private String bookingClass;
        
        @JsonProperty("allInclusiveFare")
        private String allInclusiveFare;

        // Getters and Setters
        public String getPaymentAmount() { return paymentAmount; }
        public void setPaymentAmount(String paymentAmount) { this.paymentAmount = paymentAmount; }
        public String getIxiMoneyBurnAmount() { return ixiMoneyBurnAmount; }
        public void setIxiMoneyBurnAmount(String ixiMoneyBurnAmount) { this.ixiMoneyBurnAmount = ixiMoneyBurnAmount; }
        public String getIxiMoneyPremiumBurnAmount() { return ixiMoneyPremiumBurnAmount; }
        public void setIxiMoneyPremiumBurnAmount(String ixiMoneyPremiumBurnAmount) { this.ixiMoneyPremiumBurnAmount = ixiMoneyPremiumBurnAmount; }
        public String getIxigoServiceCharge() { return ixigoServiceCharge; }
        public void setIxigoServiceCharge(String ixigoServiceCharge) { this.ixigoServiceCharge = ixigoServiceCharge; }
        public String getPgCharge() { return pgCharge; }
        public void setPgCharge(String pgCharge) { this.pgCharge = pgCharge; }
        public String getBookingClass() { return bookingClass; }
        public void setBookingClass(String bookingClass) { this.bookingClass = bookingClass; }
        public String getAllInclusiveFare() { return allInclusiveFare; }
        public void setAllInclusiveFare(String allInclusiveFare) { this.allInclusiveFare = allInclusiveFare; }
    }

    public static class TransactionDetail {
        @JsonProperty("startTime")
        private String startTime;
        
        @JsonProperty("expiryTime")
        private String expiryTime;
        
        @JsonProperty("expiryActionUrl")
        private String expiryActionUrl;
        
        @JsonProperty("expiryTitle")
        private String expiryTitle;
        
        @JsonProperty("expiryMessage")
        private String expiryMessage;

        // Getters and Setters
        public String getStartTime() { return startTime; }
        public void setStartTime(String startTime) { this.startTime = startTime; }
        public String getExpiryTime() { return expiryTime; }
        public void setExpiryTime(String expiryTime) { this.expiryTime = expiryTime; }
        public String getExpiryActionUrl() { return expiryActionUrl; }
        public void setExpiryActionUrl(String expiryActionUrl) { this.expiryActionUrl = expiryActionUrl; }
        public String getExpiryTitle() { return expiryTitle; }
        public void setExpiryTitle(String expiryTitle) { this.expiryTitle = expiryTitle; }
        public String getExpiryMessage() { return expiryMessage; }
        public void setExpiryMessage(String expiryMessage) { this.expiryMessage = expiryMessage; }
    }
}