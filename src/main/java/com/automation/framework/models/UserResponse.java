package com.automation.framework.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {

    @JsonProperty("data")
    private User data;

    @JsonProperty("support")
    private Support support;

    public UserResponse() {}

    public User getData() {
        return data;
    }

    public void setData(User data) {
        this.data = data;
    }

    public Support getSupport() {
        return support;
    }

    public void setSupport(Support support) {
        this.support = support;
    }

    @Override
    public String toString() {
        return "UserResponse{" +
                "data=" + data +
                ", support=" + support +
                '}';
    }
}