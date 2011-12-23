package org.apertereports.common.xml.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

/**
 * Represents a cyclic report config stored in portlet preferences or database.
 */
@XStreamAlias("cyclicReportConfig")
public class CyclicReportConfig {
    @XStreamImplicit
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
