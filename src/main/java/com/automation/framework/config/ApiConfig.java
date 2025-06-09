package com.automation.framework.config;

import com.automation.framework.interfaces.ConfigInterface;
import java.io.IOException;
import java.util.Properties;

public class ApiConfig implements ConfigInterface {

    private Properties properties;
    private String environment;

    public ApiConfig() {
        this.environment = System.getProperty("env", "dev");
        loadConfig(environment);
    }

    @Override
    public void loadConfig(String environment) {
        properties = new Properties();
        try {
            String configFile = "/config/" + environment + ".properties";
            properties.load(this.getClass().getResourceAsStream(configFile));
        } catch (IOException | NullPointerException e) {
            throw new RuntimeException("Failed to load configuration for environment: " + environment, e);
        }
    }

    @Override
    public String getBaseUrl() {
        return getProperty("base.url");
    }

    @Override
    public String getEnvironment() {
        return environment;
    }

    @Override
    public int getTimeout() {
        return Integer.parseInt(getProperty("api.timeout", "30"));
    }

    @Override
    public int getRetryCount() {
        return Integer.parseInt(getProperty("api.retry.count", "3"));
    }

    @Override
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}