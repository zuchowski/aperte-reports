package org.apertereports;

import org.apertereports.components.ReportOrderBrowserComponent;
import org.apertereports.components.ReportOrderBrowserComponentNew;
import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.Window;

/**
 * This portlet displays a table containing all report orders.
 * <p/>A user can browse executed and processing report orders from here. Each row
 * of the table contains the report name, its status, the XML with parameters used to generate and date of issue.
 * Additionally, for finished report orders, a couple of options for generating the report again are available.
 */
public class ReportOrderApplication extends AbstractReportingApplication {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        Window mainWindow = new Window(VaadinUtil.getValue("report_order.window.title"), new ReportOrderBrowserComponentNew());
        setMainWindow(mainWindow);
    }
}
