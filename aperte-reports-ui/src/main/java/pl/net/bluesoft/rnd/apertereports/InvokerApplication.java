package pl.net.bluesoft.rnd.apertereports;

import com.vaadin.ui.Window;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.VriesInvokerComponent;

/**
 * This portlet displays a list of available reports.
 * <p/>A user can then invoke the generation of the report manually with temporal parameters.
 * It is also possible to generate the report in the background and send the result by email
 * to the currently logged Liferay user.
 */
public class InvokerApplication extends AbstractReportingApplication {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        VriesInvokerComponent invoker = new VriesInvokerComponent(true);

        Window mainWindow = new Window(TM.get("invoker.window.title"), invoker);

        setMainWindow(mainWindow);
    }
}
