package org.apertereports.engine;

import org.apertereports.common.ReportConstants.Keys;

/**
 * Represents a single parameter property.
 */
public class ReportProperty {
    /**
     * Property key.
     */
    private Keys key;
    /**
     * Property value;
     */
    private String value;

    public ReportProperty(Keys key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public ReportProperty(String propertyName, String propertyValue) {
        this(Keys.valueOf(propertyName), propertyValue);
    }

    public Keys getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(Keys key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
