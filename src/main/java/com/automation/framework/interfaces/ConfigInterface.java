package com.automation.framework.interfaces;

public interface ConfigInterface {

    /**
     * Get base URL for API
     */
    String getBaseUrl();

    /**
     * Get environment configuration
     */
    String getEnvironment();

    /**
     * Get timeout configuration
     */
    int getTimeout();

    /**
     * Get retry count
     */
    int getRetryCount();

    /**
     * Get property value by key
     */
    String getProperty(String key);

    /**
     * Load configuration for specific environment
     */
    void loadConfig(String environment);
}