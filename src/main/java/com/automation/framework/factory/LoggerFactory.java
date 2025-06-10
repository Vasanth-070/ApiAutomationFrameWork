package com.automation.framework.factory;

import com.automation.framework.interfaces.LoggingInterface;
import com.automation.framework.logging.TestLogger;

public class LoggerFactory {
    
    public static LoggingInterface createLogger() {
        return TestLogger.getInstance();
    }
    
    public static LoggingInterface createLogger(String loggerType) {
        switch (loggerType.toLowerCase()) {
            case "test":
            case "default":
                return TestLogger.getInstance();
            default:
                throw new IllegalArgumentException("Unknown logger type: " + loggerType);
        }
    }
}