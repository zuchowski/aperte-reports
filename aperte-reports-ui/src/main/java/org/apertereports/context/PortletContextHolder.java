package org.apertereports.context;

import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSession;
import java.io.Serializable;

/**
 * A bean that contains temporal Liferay context parameters. Populated and passed to the {@link ContextReloadHandler}.
 */
public class PortletContextHolder implements Serializable {
    private String windowId;
    private PortletPreferences preferences;
    private PortletSession session;
    private PortletMode mode;

    public PortletContextHolder() {
    }

    public PortletContextHolder(String windowId, PortletMode mode, PortletPreferences preferences, PortletSession session) {
        this.windowId = windowId;
        this.preferences = preferences;
        this.session = session;
        this.mode = mode;
    }

    public String getWindowId() {
        return windowId;
    }

    public void setWindowId(String windowId) {
        this.windowId = windowId;
    }

    public PortletPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(PortletPreferences preferences) {
        this.preferences = preferences;
    }

    public PortletSession getSession() {
        return session;
    }

    public void setSession(PortletSession session) {
        this.session = session;
    }

    public PortletMode getMode() {
        return mode;
    }

    public void setMode(PortletMode mode) {
        this.mode = mode;
    }
}
