package org.apertereports.components;

import com.vaadin.ui.*;
import eu.livotov.tpt.gui.windows.TPTWindow;
import org.apertereports.common.ReportConstants;

import java.util.HashMap;
import java.util.Map.Entry;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;

import org.apertereports.util.VaadinUtil;

/**
 * Displays a help window with tabbed contents based on passed {@link Module} and {@link Tab}.
 */
public class HelpWindow extends TPTWindow {

    private TabSheet tabs;

    /**
     * Initializes the possible tab contents.
     *
     * @param module Module the help concerns
     * @param tab Tab that should be selected
     */
    public HelpWindow(Module module, Tab tab) {
        super(VaadinUtil.getValue("help_window." + module.toString().toLowerCase() + ".title"));
        setWidth("40%");
        setHeight("80%");
        setModal(true);

        tabs = new TabSheet();
        HashMap<Tab, Component> tabContent = new HashMap<HelpWindow.Tab, Component>();

        if (Module.MANAGER.equals(module)) {
            tabContent.put(Tab.PARAMS, getReportParametersTab());
            tabContent.put(Tab.TEMPLATE_OPTIONS, getTemplatesOptionsTab());
        }
        else if (Module.DASHBOARD.equals(module)) {
            tabContent.put(Tab.REPORT_DETAILS, getReportDetailsTab());
            tabContent.put(Tab.EDIT_REPORT, getEditReportTab());
            tabContent.put(Tab.CYCLIC_REPORTS, getCyclicReportsTab());
        }
        for (Entry<Tab, Component> comp : tabContent.entrySet()) {
            tabs.addTab(comp.getValue());
            tabs.getTab(comp.getValue()).setCaption(
                    VaadinUtil.getValue("help_window.tabs." + comp.getKey().toString().toLowerCase() + ".title"));
        }
        tabs.setSelectedTab(tabContent.get(tab));
        addComponent(tabs);
    }

    /**
     * Closes the window on pressing "esc" key.
     */
    @Override
    public void escapeKeyPressed() {
        close();
    }

    /**
     * Shows information about report parameters.
     *
     * @return A component to render
     */
    private Component getReportParametersTab() {
        VerticalLayout vl = UiFactory.createVLayout(null);
        vl.addComponent(new Label(VaadinUtil.getValue("help_window.tabs." + Tab.PARAMS.toString().toLowerCase() + ".intro"), Label.CONTENT_XHTML));
        Accordion accordion = new Accordion();
        for (ReportConstants.Keys param : ReportConstants.Keys.values()) {
            String title = VaadinUtil.getValue("help_window.tabs." + Tab.PARAMS.toString().toLowerCase() + "."
                    + param.toString().toLowerCase() + ".title", param.toString().toLowerCase()) + " - " + param.toString().toLowerCase();

            String property_name = VaadinUtil.getValue("help_window.tabs." + Tab.PARAMS.toString().toLowerCase() + ".param_name", param.toString().toLowerCase());
            String property_desc = VaadinUtil.getValue("help_window.tabs." + Tab.PARAMS.toString().toLowerCase() + "."
                    + param.toString().toLowerCase() + ".content", param.toString().toLowerCase());
            Label content = new Label(property_name + "<br/>" + property_desc, Label.CONTENT_XHTML);
            accordion.addTab(content, title, null);
        }
        vl.addComponent(accordion);
        accordion.setSelectedTab(new Label());
        return vl;
    }

    /**
     * Displays help for report template options.
     *
     * @return A component to render
     */
    private Component getTemplatesOptionsTab() {
        return getSimpleHelpTab(Tab.TEMPLATE_OPTIONS.toString().toLowerCase());
    }

    /**
     * Displays help for dashboard report details.
     *
     * @return A component to render
     */
    private Component getReportDetailsTab() {
        return getSimpleHelpTab(Tab.REPORT_DETAILS.toString().toLowerCase());
    }

    /**
     * Displays help for dashboard edit mode
     *
     * @return A component to render
     */
    private Component getEditReportTab() {
        return getSimpleHelpTab(Tab.EDIT_REPORT.toString().toLowerCase());
    }

    /**
     * Displays help for cyclic reports.
     *
     * @return A component to render
     */
    private Component getCyclicReportsTab() {
        return getSimpleHelpTab(Tab.CYCLIC_REPORTS.toString().toLowerCase());
    }

    /**
     * Just shows HTML formatted labels with help contents.
     * The message key should be of form <code>help_window.tabs.modulename.intro</code> - the help title and
     * <code>help_window.tabs.modulename.content</code> - the help contents.
     *
     * @param infix the infix module name of the help contents
     * @return A component to render
     */
    private Component getSimpleHelpTab(String infix) {
        VerticalLayout vl = UiFactory.createVLayout(null, FAction.SET_SPACING);
        vl.addComponent(new Label(VaadinUtil.getValue("help_window.tabs." + infix + ".intro"), Label.CONTENT_XHTML));
        vl.addComponent(new Label(VaadinUtil.getValue("help_window.tabs." + infix + ".content"), Label.CONTENT_XHTML));
        return vl;
    }

    /**
     * Help modules.
     */
    public enum Module {
        MANAGER, INVOKER, DASHBOARD
    }

    /**
     * Tab names of the help content.
     */
    public enum Tab {
        PARAMS, TEMPLATE_OPTIONS, EDIT_REPORT, REPORT_DETAILS, CYCLIC_REPORTS
    }
}
