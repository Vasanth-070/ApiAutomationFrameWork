package com.automation.framework.logging;

public class TestLogger extends ApiLogger {

    private static TestLogger instance;

    private TestLogger() {
        super("TestExecution");
    }

    public static TestLogger getInstance() {
        if (instance == null) {
            synchronized (TestLogger.class) {
                if (instance == null) {
                    instance = new TestLogger();
                }
            }
        }
        return instance;
    }

    public void logTestSuiteStart(String suiteName) {
        logInfo("🚀 Starting Test Suite: " + suiteName);
        logInfo("=" + "=".repeat(60));
    }

    public void logTestSuiteEnd(String suiteName, int totalTests, int passed, int failed, int skipped) {
        logInfo("=" + "=".repeat(60));
        logInfo("🏁 Test Suite Completed: " + suiteName);
        logInfo("📊 Total Tests: " + totalTests);
        logInfo("✅ Passed: " + passed);
        logInfo("❌ Failed: " + failed);
        logInfo("⏭️ Skipped: " + skipped);
        logInfo("=" + "=".repeat(60));
    }

    public void logAssertionPassed(String assertion) {
        logInfo("✓ Assertion Passed: " + assertion);
    }

    public void logAssertionFailed(String assertion, String expected, String actual) {
        logError("✗ Assertion Failed: " + assertion, null);
        logError("Expected: " + expected, null);
        logError("Actual: " + actual, null);
    }

    public void logValidationStep(String step, boolean result) {
        if (result) {
            logInfo("✓ Validation: " + step);
        } else {
            logError("✗ Validation Failed: " + step, null);
        }
    }
}