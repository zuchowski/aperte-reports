package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import eu.livotov.tpt.i18n.TM;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.dashboard.html.ReportStreamReceiver;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Displays a component with a list of available report templates and lets manually generate a report with
 * temporal parameters.
 */
public class VriesInvokerComponent extends CustomComponent {

    private VerticalLayout mainLayout;

    /**
     * A window with report parameters.
     */
    private ReportParamWindow reportParamWindow;
    /**
     * Generates a report.
     */
    private Button generateReportButton;
    /**
     * Refreshes the report list.
     */
    private Button refreshButton;

    private ReportTemplate report;

    private Select reportSelect;

    private ReportStreamReceiver receiver = null;

    private HorizontalLayout buttons;

    public VriesInvokerComponent(boolean showReportList) {
        this(null, showReportList);
    }

    public VriesInvokerComponent(boolean showReportList, ReportStreamReceiver receiver) {
        this(null, showReportList);
        this.receiver = receiver;
    }

    /**
     * Creates a main layout.
     *
     * @param report Pre-selected report.
     * @param showReportList <code>TRUE</code> to show the report list
     */
    public VriesInvokerComponent(ReportTemplate report, boolean showReportList) {
        this.report = report;
        buildMainLayout();

        Panel panel = new Panel();
        panel.setScrollable(true);
        panel.setStyleName("borderless light");
        panel.setSizeUndefined();
        panel.setContent(mainLayout);

        setCompositionRoot(panel);
        initShowReportParamsButton();
        if (showReportList) {
            fillReportList();
        }
    }

    /**
     * Sets currently selected report
     *
     * @param report A report
     */
    public void setReport(ReportTemplate report) {
        this.report = report;
    }

    /**
     * Build the main layout.
     */
    private void buildMainLayout() {
        mainLayout = new VerticalLayout();

        generateReportButton = new Button(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("invoker.intro.generate"));
        generateReportButton.setImmediate(true);

        buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        buttons.addComponent(generateReportButton);

        mainLayout.addComponent(buttons);
    }

    /**
     * Initializes the report select.
     */
    private void fillReportList() {
        reportSelect = new Select(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("invoker.intro.select_report"));
        reportSelect.setNullSelectionAllowed(false);
        reportSelect.setWidth(250, UNITS_PIXELS);

        fillReportSelect();

        mainLayout.addComponent(reportSelect, 0);

        refreshButton = new Button(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("dashboard.view.refresh"));
        refreshButton.setImmediate(true);
        refreshButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                reportSelect.removeAllItems();
                fillReportSelect();
            }
        });
        buttons.addComponent(refreshButton);

        reportSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                Integer reportId = (Integer) reportSelect.getValue();
                report = ReportTemplateDAO.fetchReport(reportId);
            }
        });
    }

    /**
     * Initializes the generate button. Once clicked it opens a new window with report parameters.
     */
    private void initShowReportParamsButton() {
        generateReportButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                if (report != null) {
                    reportParamWindow = new ReportParamWindow(report, pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("invoker.window.title"), receiver);
                    getWindow().addWindow(reportParamWindow);
                }
            }
        });
    }

    /**
     * Downloads the report template list from database. The reports are sorted by report name.
     */
    private void fillReportSelect() {
        List<ReportTemplate> reports = new ArrayList<ReportTemplate>(ReportTemplateDAO.fetchAllReports(true));
        Collections.sort(reports, new Comparator<ReportTemplate>() {
            @Override
            public int compare(ReportTemplate o1, ReportTemplate o2) {
                return o1.getReportname().compareTo(o2.getReportname());
            }
        });
        for (ReportTemplate report : reports) {
            if (report == null || StringUtils.isEmpty(report.getDescription())) {
                continue;
            }
            reportSelect.addItem(report.getId());
            reportSelect.setItemCaption(report.getId(), report.getReportname() + " (" + report.getDescription() + ")");
        }
    }
}
