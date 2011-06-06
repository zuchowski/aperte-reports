package pl.net.bluesoft.rnd.vries.xml;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

/**
 * Represents a single report config parameter stored in portlet preferences or database.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "reportParameter")
public class ReportConfigParameter implements Serializable {
    @XmlAttribute
    private String name;
    @XmlAttribute
    private String value;
    @XmlAttribute
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
