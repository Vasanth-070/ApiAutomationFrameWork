package com.automation.framework.core.logging;

import com.automation.framework.core.interfaces.LoggingInterface;
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
        logger.info("ℹ️ [FRAMEWORK] " + message);
    }

    @Override
    public void logDebug(String message) {
        logger.debug("🔍 [DEBUG] " + message);
    }

    @Override
    public void logWarning(String message) {
        logger.warn("⚠️ [WARNING] " + message);
    }

    @Override
    public void logError(String message, Throwable exception) {
        String formattedMessage = "❌ [ERROR] " + message;
        if (exception != null) {
            logger.error(formattedMessage, exception);
        } else {
            logger.error(formattedMessage);
        }
    }

    @Override
    public void logApiRequest(String method, String url, String payload) {
        String requestLog = "\n" + "═".repeat(25) + " API REQUEST " + "═".repeat(25) +
                "\n▶ [METHOD] " + method +
                "\n🌐 [URL] " + url +
                "\n📄 [PAYLOAD] " + (payload != null ? payload : "No payload") +
                "\n" + "─".repeat(65) + "\n";

        logger.info(requestLog);
    }

    @Override
    public void logApiResponse(int statusCode, String responseBody, long responseTime) {
        String statusEmoji = getStatusCodeEmoji(statusCode);
        String responseLog = "\n" + "═".repeat(25) + " API RESPONSE " + "═".repeat(24) +
                "\n" + statusEmoji + " [STATUS] " + statusCode +
                "\n⏱ [TIME] " + responseTime + " ms" +
                "\n📄 [RESPONSE] " + (responseBody != null ? responseBody : "No response body") +
                "\n" + "─".repeat(65) + "\n";

        logger.info(responseLog);
    }

    @Override
    public void logTestStart(String testName, String testClass) {
        String message = String.format("\n▶ [TEST-START] %s in class: %s", testName, testClass);
        logger.info(message);
    }

    @Override
    public void logTestEnd(String testName, String status, long executionTime) {
        String emoji = getStatusEmoji(status);
        String message = String.format("\n◀ [TEST-END] %s | Status: %s | Duration: %d ms %s\n",
                testName, status, executionTime, emoji);
        logger.info(message);
    }

    @Override
    public void setLoggingLevel(String level) {
        ch.qos.logback.classic.Logger logbackLogger =
                (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(loggerName);
        logbackLogger.setLevel(Level.valueOf(level.toUpperCase()));
    }
    
    @Override
    public void logTestSuiteStart(String suiteName) {
        String message = "\n" + "═".repeat(30) + "\n▶ [SUITE-START] Test Suite: " + suiteName + "\n" + "═".repeat(30);
        logger.info(message);
    }
    
    @Override
    public void logTestSuiteEnd(String suiteName, int totalTests, int passedTests, int failedTests, int skippedTests) {
        String message = "\n" + "═".repeat(30) + 
                "\n◀ [SUITE-END] Test Suite Completed: " + suiteName +
                "\n▣ [SUMMARY] Total Tests: " + totalTests +
                "\n✓ [PASSED] " + passedTests +
                "\n✗ [FAILED] " + failedTests +
                "\n⊘ [SKIPPED] " + skippedTests +
                "\n" + "═".repeat(30);
        logger.info(message);
    }
    
    @Override
    public void logAssertionPassed(String assertionMessage) {
        logger.info("✓ [ASSERTION] Passed: " + assertionMessage);
    }

    private String getStatusEmoji(String status) {
        switch (status.toUpperCase()) {
            case "PASSED":
                return "✓";
            case "FAILED":
                return "✗";
            case "SKIPPED":
                return "⊘";
            default:
                return "ℹ";
        }
    }
    
    private String getStatusCodeEmoji(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) {
            return "✓"; // Success
        } else if (statusCode >= 300 && statusCode < 400) {
            return "↻"; // Redirect
        } else if (statusCode >= 400 && statusCode < 500) {
            return "⚠"; // Client Error
        } else if (statusCode >= 500) {
            return "✗"; // Server Error
        } else {
            return "ℹ"; // Unknown
        }
    }
}