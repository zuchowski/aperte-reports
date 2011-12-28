package org.apertereports;

import org.apertereports.components.ReportManagerComponent;
import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.Window;

/**
 * This is the main report administration portlet.
 * <p/>A user can add new, modify or delete existing report templates from the application.
 * The reports are displayed in a table, providing information about the name of the report,
 * description and permissions whether to let users generate in the background or directly.
 * It is also possible to disable a report so that is no longer available in the dashboards,
 * but remains in the database.
 * <p/>To add a new report one needs to upload a JRXML Jasper template using the panel on the right
 * of the table. Once the report is uploaded one can configure the permissions and generate a
 * sample report for checking.
 */
public class ManagerApplication extends AbstractReportingApplication {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        ReportManagerComponent manager = new ReportManagerComponent();

        Window mainWindow = new Window(VaadinUtil.getValue("manager.window.title"), manager);

        setMainWindow(mainWindow);
        setTheme("chameleon");
    }

}
