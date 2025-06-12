package com.automation.framework.services.payment.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * UPI Data model representing UPI payment section
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpiData {

    @JsonProperty("heading")
    private String heading;
    
    @JsonProperty("options")
    private List<UpiOption> options;

    // Getters and Setters
    public String getHeading() {
        return heading;
    }
    public List<UpiOption> getOptions() {
        return options;
    }

    // Business validation methods
    public boolean hasValidHeading() {
        return heading != null && !heading.trim().isEmpty();
    }

    public boolean hasValidOptions() {
        return options != null && !options.isEmpty();
    }

    public boolean hasValidRequiredOptions() {
        if (!hasValidOptions()) {
            return false;
        }

        return options.stream().allMatch(UpiOption::hasRequiredFields);
    }

    public boolean isValid() {
        return hasValidHeading() && hasValidRequiredOptions();
    }

    public int getValidOptionsCount() {
        if (options == null) {
            return 0;
        }
        return (int) options.stream().filter(UpiOption::isValid).count();
    }

    @Override
    public String toString() {
        return "UpiData{" +
                "heading='" + heading + '\'' +
                ", optionsCount=" + (options != null ? options.size() : 0) +
                '}';
    }
}