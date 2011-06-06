package pl.net.bluesoft.rnd.vries;

import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.vries.components.HelpComponent;
import pl.net.bluesoft.rnd.vries.context.AbstractContextReloadHandler;
import pl.net.bluesoft.rnd.vries.context.ContextReloadListener;
import pl.net.bluesoft.rnd.vries.context.PortletContextHolder;
import pl.net.bluesoft.rnd.vries.dashboard.AbstractDashboardComponent;
import pl.net.bluesoft.rnd.vries.dashboard.EditDashboardComponent;
import pl.net.bluesoft.rnd.vries.dashboard.ViewDashboardComponent;

/**
 * This portlet displays a dashboard based on an existing report from the database.
 * <p/>Each dashboard configuration is stored in portlet preferences. The means of configuration
 * are available in the portlet <code>EDIT</code> mode.
 * <p/>It is possible to configure more than one dashboard in a single portlet, however it is not
 * recommended for complex reports due to performance loss.
 * <p/>The dashboard display can be also configured to let users generate the currently shown report
 * directly to a file with thumbnails. These are buttons shown above the generated report.
 * <p/>The generated report contents are cached inside the application and maintained by a single
 * thread that keeps the track of the reports. Once the thread discovers a report is outdated it releases
 * the resource. The next refresh of the page would launch the report generation process again.
 */
public class VriesDashboardApplication extends AbstractVriesApplication {
    /**
     * Dashboard display view.
     */
    private AbstractDashboardComponent viewMode = new ViewDashboardComponent();
    /**
     * Dashboard edit view.
     */
    private AbstractDashboardComponent editMode = new EditDashboardComponent();
    /**
     * Displays help view.
     */
    private HelpComponent helpMode = new HelpComponent("dashboard.help.content");

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        final Window mainWindow = new Window(TM.get("dashboard.window.title"), viewMode);
        setMainWindow(mainWindow);

        if (getContext() instanceof PortletApplicationContext2) {
            PortletApplicationContext2 ctx = (PortletApplicationContext2) getContext();
            ctx.addPortletListener(this, new ContextReloadListener(new AbstractContextReloadHandler() {
                @Override
                public void handleHelp(PortletContextHolder holder) {
                    if (mainWindow.getContent() != helpMode) {
                        mainWindow.setContent(helpMode);
                    }
                }

                @Override
                public void handleEdit(PortletContextHolder holder) {
                    update(mainWindow, editMode, holder, true);
                }

                @Override
                public void handleView(PortletContextHolder holder) {
                    update(mainWindow, viewMode, holder, true);
                }

                @Override
                public void handleResource(PortletContextHolder holder) {
                    if (mainWindow.getContent() instanceof AbstractDashboardComponent) {
                        update(mainWindow, (AbstractDashboardComponent) mainWindow.getContent(), holder, false);
                    }
                }
            }));
        }
    }

    /**
     * Updates the view in the Vaadin window. Omits the data initialization on a resource request.
     *
     * @param window    Current Vaadin window
     * @param component Component to show in window
     * @param holder    Context holder
     * @param init      <code>TRUE</code> to init the component data
     */
    private void update(Window window, AbstractDashboardComponent component, PortletContextHolder holder, boolean init) {
        component.setPortletPreferences(holder.getPreferences());
        component.setPortletSession(holder.getSession());
        component.setPortletId(holder.getWindowId());
        if (window.getContent() != component) {
            window.setContent(component);
        }
        if (init) {
            component.initData();
        }
    }

}
