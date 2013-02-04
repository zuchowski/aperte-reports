package org.apertereports.dashboard;

import com.vaadin.ui.CustomComponent;
import org.apache.commons.codec.binary.Base64;
import org.apertereports.util.cache.MapCache;

import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.common.xml.config.XmlReportConfigLoader;

import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.apertereports.util.DashboardPreferences.PREFERENCE_REPORT_CONFIGS_KEY;
import static org.apertereports.util.DashboardPreferences.PREFERENCE_TEMPLATE_KEY;

/**
 * An abstract class that manages the portlet session and preferences.
 * <p/>The preferences are transformed from an Base64 encoded XML to a list of {@link ReportConfig} objects.
 * Extending classes use these configuration objects to either display the dashboard
 * or show an edit form.
 */
public abstract class AbstractDashboardComponent extends CustomComponent {
    protected List<ReportConfig> reportConfigs;
    protected String template;
    protected PortletPreferences preferences;
    protected PortletSession session;
    protected String portletId;

    /**
     * An instance of a {@link MapCache} for general caching purposes.
     *
     * @see MapCache
     */
    protected MapCache cache = new MapCache();

    /**
     * Loads the dashboard HTML template from preferences with a key specified by
     * {@link org.apertereports.util.DashboardPreferences#PREFERENCE_TEMPLATE_KEY}.
     * <p/>Also loads an XML containing a list of {@link ReportConfig}s which is transformed so that
     * classes extending this class can use it.
     */
    protected void prepareData() {
        if (preferences != null) {
            template = preferences.getValue(PREFERENCE_TEMPLATE_KEY, null);
            template = template != null ? new String(Base64.decodeBase64(template.getBytes())) : null;
            String rc = preferences.getValue(PREFERENCE_REPORT_CONFIGS_KEY, null);
            if (rc != null) {
                rc = new String(Base64.decodeBase64(rc.getBytes()));
                reportConfigs = XmlReportConfigLoader.getInstance().stringAsReportConfigs(rc);
                Collections.sort(reportConfigs, new Comparator<ReportConfig>() {
                    @Override
                    public int compare(ReportConfig o1, ReportConfig o2) {
                        return o1.getId().compareTo(o2.getId());
                    }
                });
            }
            else {
                reportConfigs = new ArrayList<ReportConfig>();
            }
        }
    }

    /**
     * Stores the dashboard HTML template to preferences. The template is stored under
     * {@link org.apertereports.util.DashboardPreferences#PREFERENCE_TEMPLATE_KEY} key
     * as a Base64 encoded string.
     * <p/>Also stores the list of {@link ReportConfig} which is marshaled to a single string using JAXB.
     */
    protected void saveData() {
        if (preferences != null) {
            try {
                template = template != null ? new String(Base64.encodeBase64(template.getBytes())) : null;
                preferences.setValue(PREFERENCE_TEMPLATE_KEY, template);
                if (reportConfigs != null) {
                    for (ReportConfig rc : reportConfigs) {
                        if (rc.getCyclicReportId() != null) {
                            rc.setParameters(null);
                        }
                    }
                }
                String rc = XmlReportConfigLoader.getInstance().reportConfigsAsString(reportConfigs);
                rc = new String(Base64.encodeBase64(rc.getBytes()));
                preferences.setValue(PREFERENCE_REPORT_CONFIGS_KEY, rc);
                preferences.store();
            }
            catch (Exception e) {
                throw new ARRuntimeException(e);
            }
        }
    }

    /**
     * Loads preferences and initializes the view.
     */
    public void initData() {
        prepareData();
        initComponentData();
    }

    /**
     * Init extending class view.
     */
    protected abstract void initComponentData();

    public void setPortletPreferences(PortletPreferences preferences) {
        this.preferences = preferences;
    }

    public void setPortletSession(PortletSession session) {
        this.session = session;
    }

    public void setPortletId(String portletId) {
        this.portletId = portletId;
    }
}
