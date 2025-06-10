package com.automation.framework.factory;

import com.automation.framework.interfaces.ResponseValidatorInterface;
import com.automation.framework.utils.ResponseValidator;
import com.automation.framework.utils.JUnitResponseValidator;

/**
 * Factory class for creating response validator instances
 * Provides centralized creation logic for different validator implementations
 */
public class ResponseValidatorFactory {
    
    /**
     * Enum for different validator types
     */
    public enum ValidatorType {
        TESTNG,     // Default TestNG-based validator
        JUNIT,      // JUnit-style validator
        DEFAULT     // Alias for TESTNG
    }
    
    /**
     * Create default response validator (TestNG-based)
     */
    public static ResponseValidatorInterface createValidator() {
        return createValidator(ValidatorType.DEFAULT);
    }
    
    /**
     * Create response validator of specified type
     */
    public static ResponseValidatorInterface createValidator(ValidatorType type) {
        switch (type) {
            case TESTNG:
            case DEFAULT:
                return new ResponseValidator();
            case JUNIT:
                return new JUnitResponseValidator();
            default:
                throw new IllegalArgumentException("Unknown validator type: " + type);
        }
    }
    
    /**
     * Create response validator by string type name
     */
    public static ResponseValidatorInterface createValidator(String typeName) {
        try {
            ValidatorType type = ValidatorType.valueOf(typeName.toUpperCase());
            return createValidator(type);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown validator type: " + typeName + 
                                             ". Available types: TESTNG, JUNIT, DEFAULT");
        }
    }
    
    /**
     * Create validator based on system property
     * Usage: -Dvalidator.type=junit
     */
    public static ResponseValidatorInterface createValidatorFromSystemProperty() {
        String validatorType = System.getProperty("validator.type", "default");
        return createValidator(validatorType);
    }
}