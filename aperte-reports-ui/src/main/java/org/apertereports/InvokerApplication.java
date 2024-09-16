package org.apertereports;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apertereports.components.AperteInvokerComponent;
import org.apertereports.util.VaadinUtil;

import com.vaadin.terminal.gwt.server.HttpServletRequestListener;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Window;

import org.apertereports.common.users.User;

/**
 * This portlet displays a list of available reports.
 * <p/>
 * A user can then invoke the generation of the report manually with temporal
 * parameters. It is also possible to generate the report in the background and
 * send the result by email to the currently logged Liferay user.
 */
public class InvokerApplication extends AbstractReportingApplication<AperteInvokerComponent> {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
    	setTheme("mytheme");
        mainPanel = new AperteInvokerComponent();
        mainWindow = new Window(VaadinUtil.getValue("invoker.window.title"), mainPanel);
    }

    @Override
    protected void reinitUserData(User user) {
        mainPanel.initData(user);
    }
    
    
}
