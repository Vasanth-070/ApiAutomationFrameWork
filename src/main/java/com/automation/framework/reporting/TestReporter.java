package com.automation.framework.reporting;

import com.automation.framework.interfaces.ReportingInterface;
import io.restassured.response.Response;

public class TestReporter implements ReportingInterface {

    private ExtentReportManager extentReportManager;
    private AllureReportManager allureReportManager;
    private CustomReportGenerator customReportGenerator;
    private boolean useExtentReport;
    private boolean useAllureReport;
    private boolean useCustomReport;

    public TestReporter() {
        this.useExtentReport = Boolean.parseBoolean(System.getProperty("extent.report", "true"));
        this.useAllureReport = Boolean.parseBoolean(System.getProperty("allure.report", "false"));
        this.useCustomReport = Boolean.parseBoolean(System.getProperty("custom.report", "false"));

        if (useExtentReport) {
            extentReportManager = new ExtentReportManager();
        }
        if (useAllureReport) {
            allureReportManager = new AllureReportManager();
        }
        if (useCustomReport) {
            customReportGenerator = new CustomReportGenerator();
        }
    }

    @Override
    public void initializeReport(String suiteName, String environment) {
        if (useExtentReport) {
            extentReportManager.initializeReport(suiteName, environment);
        }
        if (useAllureReport) {
            allureReportManager.initializeReport(suiteName, environment);
        }
        if (useCustomReport) {
            customReportGenerator.initializeReport(suiteName, environment);
        }
    }

    @Override
    public void startTest(String testName, String description) {
        if (useExtentReport) {
            extentReportManager.startTest(testName, description);
        }
        if (useAllureReport) {
            allureReportManager.startTest(testName, description);
        }
        if (useCustomReport) {
            customReportGenerator.startTest(testName, description);
        }
    }

    @Override
    public void logStep(String stepDescription, String status) {
        if (useExtentReport) {
            extentReportManager.logStep(stepDescription, status);
        }
        if (useAllureReport) {
            allureReportManager.logStep(stepDescription, status);
        }
        if (useCustomReport) {
            customReportGenerator.logStep(stepDescription, status);
        }
    }

    @Override
    public void logApiRequest(String method, String endpoint, String requestBody, String headers) {
        if (useExtentReport) {
            extentReportManager.logApiRequest(method, endpoint, requestBody, headers);
        }
        if (useAllureReport) {
            allureReportManager.logApiRequest(method, endpoint, requestBody, headers);
        }
        if (useCustomReport) {
            customReportGenerator.logApiRequest(method, endpoint, requestBody, headers);
        }
    }

    @Override
    public void logApiResponse(Response response, String responseBody) {
        if (useExtentReport) {
            extentReportManager.logApiResponse(response, responseBody);
        }
        if (useAllureReport) {
            allureReportManager.logApiResponse(response, responseBody);
        }
        if (useCustomReport) {
            customReportGenerator.logApiResponse(response, responseBody);
        }
    }

    @Override
    public void markTestPassed(String testName, String details) {
        if (useExtentReport) {
            extentReportManager.markTestPassed(testName, details);
        }
        if (useAllureReport) {
            allureReportManager.markTestPassed(testName, details);
        }
        if (useCustomReport) {
            customReportGenerator.markTestPassed(testName, details);
        }
    }

    @Override
    public void markTestFailed(String testName, String errorMessage, Throwable exception) {
        if (useExtentReport) {
            extentReportManager.markTestFailed(testName, errorMessage, exception);
        }
        if (useAllureReport) {
            allureReportManager.markTestFailed(testName, errorMessage, exception);
        }
        if (useCustomReport) {
            customReportGenerator.markTestFailed(testName, errorMessage, exception);
        }
    }

    @Override
    public void markTestSkipped(String testName, String reason) {
        if (useExtentReport) {
            extentReportManager.markTestSkipped(testName, reason);
        }
        if (useAllureReport) {
            allureReportManager.markTestSkipped(testName, reason);
        }
        if (useCustomReport) {
            customReportGenerator.markTestSkipped(testName, reason);
        }
    }

    @Override
    public void addScreenshot(String screenshotPath, String description) {
        if (useExtentReport) {
            extentReportManager.addScreenshot(screenshotPath, description);
        }
        if (useAllureReport) {
            allureReportManager.addScreenshot(screenshotPath, description);
        }
        if (useCustomReport) {
            customReportGenerator.addScreenshot(screenshotPath, description);
        }
    }

    @Override
    public void finalizeReport() {
        if (useExtentReport) {
            extentReportManager.finalizeReport();
        }
        if (useAllureReport) {
            allureReportManager.finalizeReport();
        }
        if (useCustomReport) {
            customReportGenerator.finalizeReport();
        }
    }

    @Override
    public String getReportPath() {
        if (useExtentReport) {
            return extentReportManager.getReportPath();
        }
        if (useAllureReport) {
            return allureReportManager.getReportPath();
        }
        if (useCustomReport) {
            return customReportGenerator.getReportPath();
        }
        return "";
    }
}