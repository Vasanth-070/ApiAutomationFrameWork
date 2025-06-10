package com.automation.framework.services.user.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Support {

    @JsonProperty("url")
    private String url;

    @JsonProperty("text")
    private String text;

    public Support() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "Support{" +
                "url='" + url + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}