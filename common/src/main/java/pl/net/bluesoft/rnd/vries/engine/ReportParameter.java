package pl.net.bluesoft.rnd.vries.engine;

import pl.net.bluesoft.rnd.vries.util.Constants.Keys;

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
}
