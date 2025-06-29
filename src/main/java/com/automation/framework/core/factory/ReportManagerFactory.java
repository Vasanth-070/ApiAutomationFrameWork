package com.automation.framework.core.factory;

import com.automation.framework.core.interfaces.ReportingInterface;
import com.automation.framework.core.reporting.ExtentReportManager;
import com.automation.framework.core.reporting.AllureReportingManager;

public class ReportManagerFactory {
    
    public enum ReportType {
        EXTENT, ALLURE
    }
    
    public static ReportingInterface createReportManager() {
        return createReportManager(ReportType.EXTENT);
    }
    
    public static ReportingInterface createReportManager(ReportType reportType) {
        switch (reportType) {
            case EXTENT:
                return new ExtentReportManager();
            case ALLURE:
                return new AllureReportingManager();
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }
    
    public static ReportingInterface createReportManager(String reportType) {
        switch (reportType.toLowerCase()) {
            case "extent":
                return new ExtentReportManager();
            case "allure":
                return new AllureReportingManager();
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }
}