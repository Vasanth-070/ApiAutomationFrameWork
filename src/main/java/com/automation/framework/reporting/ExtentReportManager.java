package com.automation.framework.reporting;

import com.automation.framework.interfaces.ReportingInterface;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.restassured.response.Response;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ExtentReportManager implements ReportingInterface {

    private ExtentReports extentReports;
    private ExtentTest extentTest;
    private String reportPath;

    @Override
    public void initializeReport(String suiteName, String environment) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        reportPath = "reports/extent-reports/ExtentReport_" + timestamp + ".html";

        ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
        sparkReporter.config().setTheme(Theme.DARK);
        sparkReporter.config().setDocumentTitle("API Automation Test Report");
        sparkReporter.config().setReportName("API Test Execution Report");

        extentReports = new ExtentReports();
        extentReports.attachReporter(sparkReporter);
        extentReports.setSystemInfo("Environment", environment);
        extentReports.setSystemInfo("Test Suite", suiteName);
        extentReports.setSystemInfo("Executed By", System.getProperty("user.name"));
        extentReports.setSystemInfo("Execution Time", LocalDateTime.now().toString());
    }

    @Override
    public void startTest(String testName, String description) {
        extentTest = extentReports.createTest(testName, description);
    }

    @Override
    public void logStep(String stepDescription, String status) {
        Status extentStatus = Status.valueOf(status.toUpperCase());
        extentTest.log(extentStatus, stepDescription);
    }

    @Override
    public void logApiRequest(String method, String endpoint, String requestBody, String headers) {
        StringBuilder requestDetails = new StringBuilder();
        requestDetails.append("<b>Method:</b> ").append(method).append("<br>");
        requestDetails.append("<b>Endpoint:</b> ").append(endpoint).append("<br>");
        requestDetails.append("<b>Headers:</b> <pre>").append(headers).append("</pre><br>");
        requestDetails.append("<b>Request Body:</b> <pre>").append(requestBody).append("</pre>");

        extentTest.info("API Request Details: " + requestDetails.toString());
    }

    @Override
    public void logApiResponse(Response response, String responseBody) {
        StringBuilder responseDetails = new StringBuilder();
        responseDetails.append("<b>Status Code:</b> ").append(response.getStatusCode()).append("<br>");
        responseDetails.append("<b>Response Time:</b> ").append(response.getTime()).append(" ms<br>");
        responseDetails.append("<b>Response Headers:</b> <pre>").append(response.getHeaders().toString()).append("</pre><br>");
        responseDetails.append("<b>Response Body:</b> <pre>").append(responseBody).append("</pre>");

        extentTest.info("API Response Details: " + responseDetails.toString());
    }

    @Override
    public void markTestPassed(String testName, String details) {
        extentTest.pass(details);
    }

    @Override
    public void markTestFailed(String testName, String errorMessage, Throwable exception) {
        extentTest.fail(errorMessage);
        if (exception != null) {
            extentTest.fail(exception);
        }
    }

    @Override
    public void markTestSkipped(String testName, String reason) {
        extentTest.skip(reason);
    }

    @Override
    public void addScreenshot(String screenshotPath, String description) {
        extentTest.addScreenCaptureFromPath(screenshotPath, description);
    }

    @Override
    public void finalizeReport() {
        extentReports.flush();
    }

    @Override
    public String getReportPath() {
        return reportPath;
    }
}