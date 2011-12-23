package org.apertereports.engine;

import org.apertereports.common.ReportConstants.Keys;

import java.util.Map;

/**
 * Used to specify a single report parameter. Each instance contains a number of properties that define,
 * for instance, a datasource, a widget type, and so on.
 */
public class ReportParameter {
    /**
     * Name of the parameter.
     */
    private String name;
    /**
     * Value type.
     */
    private String type;
    /**
     * The value.
     */
    private Object value;

    /**
     * Map containing the parameter properties.
     */
    private Map<Keys, ReportProperty> properties;

    public String getName() {
        return name;
    }

    public Map<Keys, ReportProperty> getProperties() {
        return properties;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProperties(Map<Keys, ReportProperty> outputProperties) {
        properties = outputProperties;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
