package org.apertereports.common.xml.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.io.Serializable;

/**
 * Represents a single report config parameter stored in portlet preferences or database.
 */
@XStreamAlias("reportParameter")
public class ReportConfigParameter implements Serializable {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String value;
    @XStreamAsAttribute
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
