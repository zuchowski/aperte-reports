package pl.net.bluesoft.rnd.apertereports;

import com.vaadin.ui.Window;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.VriesReportOrderBrowserComponent;

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
        Window mainWindow = new Window(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("report_order.window.title"), new VriesReportOrderBrowserComponent());
        setMainWindow(mainWindow);
    }
}
