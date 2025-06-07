package com.automation.framework.logging;

import com.automation.framework.interfaces.LoggingInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Level;

public class ApiLogger implements LoggingInterface {

    private final Logger logger;
    private final String loggerName;

    public ApiLogger(Class<?> clazz) {
        this.loggerName = clazz.getName();
        this.logger = LoggerFactory.getLogger(clazz);
    }

    public ApiLogger(String name) {
        this.loggerName = name;
        this.logger = LoggerFactory.getLogger(name);
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public void logInfo(String message) {
        logger.info(message);
    }

    @Override
    public void logDebug(String message) {
        logger.debug(message);
    }

    @Override
    public void logWarning(String message) {
        logger.warn(message);
    }

    @Override
    public void logError(String message, Throwable exception) {
        if (exception != null) {
            logger.error(message, exception);
        } else {
            logger.error(message);
        }
    }

    @Override
    public void logApiRequest(String method, String url, String payload) {
        StringBuilder requestLog = new StringBuilder();
        requestLog.append("\n").append("=".repeat(50));
        requestLog.append("\nAPI REQUEST");
        requestLog.append("\n").append("=".repeat(50));
        requestLog.append("\nMethod: ").append(method);
        requestLog.append("\nURL: ").append(url);
        requestLog.append("\nPayload: ").append(payload != null ? payload : "No payload");
        requestLog.append("\n").append("=".repeat(50));

        logger.info(requestLog.toString());
    }

    @Override
    public void logApiResponse(int statusCode, String responseBody, long responseTime) {
        StringBuilder responseLog = new StringBuilder();
        responseLog.append("\n").append("=".repeat(50));
        responseLog.append("\nAPI RESPONSE");
        responseLog.append("\n").append("=".repeat(50));
        responseLog.append("\nStatus Code: ").append(statusCode);
        responseLog.append("\nResponse Time: ").append(responseTime).append(" ms");
        responseLog.append("\nResponse Body: ").append(responseBody != null ? responseBody : "No response body");
        responseLog.append("\n").append("=".repeat(50));

        logger.info(responseLog.toString());
    }

    @Override
    public void logTestStart(String testName, String testClass) {
        String message = String.format("Starting test: %s in class: %s", testName, testClass);
        logger.info("▶️ " + message);
    }

    @Override
    public void logTestEnd(String testName, String status, long executionTime) {
        String emoji = getStatusEmoji(status);
        String message = String.format("Test completed: %s | Status: %s | Duration: %d ms",
                testName, status, executionTime);
        logger.info(emoji + " " + message);
    }

    @Override
    public void setLoggingLevel(String level) {
        ch.qos.logback.classic.Logger logbackLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName);
        logbackLogger.setLevel(Level.valueOf(level.toUpperCase()));
    }

    private String getStatusEmoji(String status) {
        switch (status.toUpperCase()) {
            case "PASSED":
                return "✅";
            case "FAILED":
                return "❌";
            case "SKIPPED":
                return "⏭️";
            default:
                return "ℹ️";
        }
    }
}