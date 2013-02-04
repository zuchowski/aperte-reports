package org.apertereports;

import org.apertereports.components.ReportOrderBrowserComponent;
import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.Window;
import org.apertereports.common.users.User;

/**
 * This portlet displays a table containing all report orders.
 * <p/>
 * A user can browse executed and processing report orders from here. Each row
 * of the table contains the report name, its status, the XML with parameters
 * used to generate and date of issue. Additionally, for finished report orders,
 * a couple of options for generating the report again are available.
 */
public class ReportOrderApplication extends AbstractReportingApplication<ReportOrderBrowserComponent> {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        mainPanel = new ReportOrderBrowserComponent();
        mainWindow = new Window(VaadinUtil.getValue("report_order.window.title"), mainPanel);
    }

    @Override
    protected void reinitUserData(User user) {
        mainPanel.initData(user);
    }
}
