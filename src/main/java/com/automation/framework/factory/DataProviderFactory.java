package com.automation.framework.factory;

import com.automation.framework.interfaces.DataProviderInterface;
import com.automation.framework.data.TestDataProvider;

public class DataProviderFactory {
    
    public static DataProviderInterface createDataProvider() {
        return new TestDataProvider();
    }
    
    public static DataProviderInterface createDataProvider(String providerType) {
        switch (providerType.toLowerCase()) {
            case "json":
            case "default":
                return new TestDataProvider();
            default:
                throw new IllegalArgumentException("Unknown data provider type: " + providerType);
        }
    }
}