package org.apertereports.common.xml.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a dashboard report config. The config is later stored in portlet preferences as marshaled string.
 * <p>The parameters contain:
 * <ul>
 * <li>reportId - database identifier of a report template</li>
 * <li>cacheTimeout - dashboard display cache timeout</li>
 * <li>cyclicReportId - database identifier of a cyclic report (if specified)</li>
 * <li>allowRefresh - should manual refresh be allowed</li>
 * <li>allowedFormats - coma separated list of allowed formats (i.e. PDF, XLS etc)</li>
 * <li>parameters - a list of report configuration parameters set in dashboard preferences</li>
 * </ul>
 */
@XStreamAlias("reportConfig")
public class ReportConfig implements Serializable {
    @XStreamAsAttribute
    private Integer id = -1;
    @XStreamAsAttribute
    private Integer reportId = -1;
    @XStreamAsAttribute
    private Integer cacheTimeout = 0;
    @XStreamAsAttribute
    private Long cyclicReportId = -1L;
    @XStreamAsAttribute
    private Boolean allowRefresh = false;
    @XStreamAsAttribute
    private String allowedFormats = null;
    @XStreamImplicit
    private List<ReportConfigParameter> parameters = null;

    public ReportConfig() {
    }

    public ReportConfig(Integer id, Integer reportId, Integer cacheTimeout, Long cyclicReportId, Boolean allowRefresh, String allowedFormats,
                        List<ReportConfigParameter> parameters) {
        this.id = id;
        this.reportId = reportId;
        this.cacheTimeout = cacheTimeout;
        this.cyclicReportId = cyclicReportId;
        this.allowRefresh = allowRefresh;
        this.allowedFormats = allowedFormats;
        this.parameters = parameters;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReportId() {
        return reportId;
    }

    public void setReportId(Integer reportId) {
        this.reportId = reportId;
    }

    public Integer getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(Integer cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public Long getCyclicReportId() {
        return cyclicReportId;
    }

    public void setCyclicReportId(Long cyclicReportId) {
        this.cyclicReportId = cyclicReportId;
    }

    public Boolean getAllowRefresh() {
        return allowRefresh;
    }

    public void setAllowRefresh(Boolean allowRefresh) {
        this.allowRefresh = allowRefresh;
    }

    public List<String> getAllowedFormatsAsList() {
        return allowedFormats != null ? Arrays.asList(allowedFormats.split(",")) : new ArrayList<String>();
    }

    public String getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(String allowedFormats) {
        this.allowedFormats = allowedFormats;
    }

    /**
     * Sets the coma separated list of allowed formats.
     *
     * @param allowedFormats A list of values
     */
    public void setAllowedFormatsFromList(List<String> allowedFormats) {
        if (allowedFormats != null && !allowedFormats.isEmpty()) {
            StringBuffer sb = new StringBuffer().append(allowedFormats.get(0));
            for (int i = 1; i < allowedFormats.size(); ++i) {
                sb.append(",").append(allowedFormats.get(i));
            }
            this.allowedFormats = sb.toString();
        }
        else {
            this.allowedFormats = null;
        }
    }

    public List<ReportConfigParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<ReportConfigParameter> parameters) {
        this.parameters = parameters;
    }
}
