package com.automation.framework.core.factory;

import com.automation.framework.core.interfaces.DataProviderInterface;
import com.automation.framework.shared.data.TestDataProvider;

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