package org.apertereports.common.xml.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamInclude;

import java.util.List;

/**
 * Root element of all dashboard report preferences. Contains a list of report configs a dashboard displays.
 */
@XStreamAlias("reportConfigs")
@XStreamInclude({CyclicReportConfig.class, ReportConfig.class, ReportConfigParameter.class})
public class ReportConfigRoot {
    @XStreamImplicit
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
