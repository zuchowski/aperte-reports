package pl.net.bluesoft.rnd.apertereports.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Represents a cyclic report config stored in portlet preferences or database.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"parameters"})
@XmlRootElement(name = "cyclicReportConfig")
public class CyclicReportConfig {
    @XmlElement(name = "reportParameters")
    private List<ReportConfigParameter> parameters;

    public CyclicReportConfig() {
    }

    public CyclicReportConfig(List<ReportConfigParameter> parameters) {
        this.parameters = parameters;
    }

    public List<ReportConfigParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportConfigParameter> parameters) {
        this.parameters = parameters;
    }
}
