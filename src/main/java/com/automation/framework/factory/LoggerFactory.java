package com.automation.framework.factory;

import com.automation.framework.interfaces.LoggingInterface;
import com.automation.framework.logging.ApiLogger;

public class LoggerFactory {
    
    public static LoggingInterface createLogger() {
        return new ApiLogger("TestExecution");
    }
    
    public static LoggingInterface createLogger(String loggerName) {
        return new ApiLogger(loggerName);
    }
    
    public static LoggingInterface createLogger(Class<?> clazz) {
        return new ApiLogger(clazz);
    }
}