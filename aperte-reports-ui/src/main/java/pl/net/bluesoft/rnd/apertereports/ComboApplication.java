package pl.net.bluesoft.rnd.apertereports;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.VriesInvokerComponent;
import pl.net.bluesoft.rnd.apertereports.components.VriesManagerComponent;
import pl.net.bluesoft.rnd.apertereports.components.VriesReportOrderBrowserComponent;
import pl.net.bluesoft.rnd.apertereports.dashboard.EditDashboardComponent;
import pl.net.bluesoft.rnd.apertereports.dashboard.ViewDashboardComponent;
import pl.net.bluesoft.rnd.apertereports.dashboard.cyclic.CyclicReportsPanel;

/**
 * This portlet displays the main components used in every other portlet.
 * Its main purpose is quick testing. Should not be used in production environment.
 */
public class ComboApplication extends AbstractReportingApplication {

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.setHeight("400px");
        VriesInvokerComponent invoker = new VriesInvokerComponent(true);
        VriesManagerComponent manager = new VriesManagerComponent();
        VriesReportOrderBrowserComponent reportOrderBrowser = new VriesReportOrderBrowserComponent();
        tabs.addTab(manager);
        tabs.getTab(manager).setCaption("manager");
        tabs.addTab(invoker);
        tabs.getTab(invoker).setCaption("invoker");
        tabs.addTab(reportOrderBrowser);
        tabs.getTab(reportOrderBrowser).setCaption("reportOrderBrowser");

        EditDashboardComponent dashboard = new EditDashboardComponent();
        tabs.addTab(dashboard);
        tabs.getTab(dashboard).setCaption("edit dashboard");
        dashboard.initData();

        ViewDashboardComponent viewDashboard = new ViewDashboardComponent();
        tabs.addTab(viewDashboard);
        tabs.getTab(viewDashboard).setCaption("view dashboard");
        viewDashboard.initData();

        CyclicReportsPanel cyclicReportsPanel = new CyclicReportsPanel();
        tabs.addTab(cyclicReportsPanel);
        tabs.getTab(cyclicReportsPanel).setCaption("cyclic reports");

        Window mainWindow = new Window(TM.get("invoker.window.title"), tabs);

        setMainWindow(mainWindow);
    }

}
