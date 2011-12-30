package org.apertereports;

import org.apertereports.components.AperteInvokerComponent;
import org.apertereports.components.ReportManagerComponent;
import org.apertereports.components.ReportOrderBrowserComponent;
import org.apertereports.dashboard.EditDashboardComponent;
import org.apertereports.dashboard.ViewDashboardComponent;
import org.apertereports.dashboard.cyclic.CyclicReportsPanel;
import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;

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
        AperteInvokerComponent invoker = new AperteInvokerComponent(true);
        ReportManagerComponent manager = new ReportManagerComponent();
        ReportOrderBrowserComponent reportOrderBrowser = new ReportOrderBrowserComponent();
        tabs.addTab(manager);
        tabs.getTab(manager).setCaption(VaadinUtil.getValue("combo.tab.manager"));
        tabs.addTab(invoker);
        tabs.getTab(invoker).setCaption(VaadinUtil.getValue("combo.tab.invoker"));
        tabs.addTab(reportOrderBrowser);
        tabs.getTab(reportOrderBrowser).setCaption(VaadinUtil.getValue("combo.tab.order-browser"));

        EditDashboardComponent dashboard = new EditDashboardComponent();
        tabs.addTab(dashboard);
        tabs.getTab(dashboard).setCaption(VaadinUtil.getValue("combo.tab.edit-dashboard"));
        dashboard.initData();

        ViewDashboardComponent viewDashboard = new ViewDashboardComponent();
        tabs.addTab(viewDashboard);
        tabs.getTab(viewDashboard).setCaption(VaadinUtil.getValue("combo.tab.view-dashboard"));
        viewDashboard.initData();

        CyclicReportsPanel cyclicReportsPanel = new CyclicReportsPanel();
        tabs.addTab(cyclicReportsPanel);
        tabs.getTab(cyclicReportsPanel).setCaption(VaadinUtil.getValue("combo.tab.cyclic-reports"));

        Window mainWindow = new Window(VaadinUtil.getValue("invoker.window.title"), tabs);

        setMainWindow(mainWindow);
        setTheme("chameleon");
    }

}
