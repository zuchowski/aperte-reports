package pl.net.bluesoft.rnd.vries.xml;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * Root element of all dashboard report preferences. Contains a list of report configs a dashboard displays.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"reportConfigs"})
@XmlRootElement(name = "reportConfigs")
public class ReportConfigRoot {
    @XmlElement(name = "reportConfig")
    protected List<ReportConfig> reportConfigs;

    public ReportConfigRoot() {
    }

    public ReportConfigRoot(List<ReportConfig> reportConfigs) {
        this.reportConfigs = reportConfigs;
    }

    public List<ReportConfig> getReportConfigs() {
        return reportConfigs;
    }

    public void setReportConfigs(List<ReportConfig> reportConfigs) {
        this.reportConfigs = reportConfigs;
    }
}
