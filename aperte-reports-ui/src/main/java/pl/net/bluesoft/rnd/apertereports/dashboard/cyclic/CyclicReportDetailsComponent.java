package pl.net.bluesoft.rnd.apertereports.dashboard.cyclic;

import com.vaadin.data.Property;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import eu.livotov.tpt.gui.widgets.TPTLazyLoadingLayout;
import eu.livotov.tpt.i18n.TM;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.components.ReportParametersComponent;
import pl.net.bluesoft.rnd.apertereports.components.SimpleHorizontalLayout;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.data.CyclicReportOrder;
import pl.net.bluesoft.rnd.apertereports.data.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.data.ReportOrder.Status;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.apertereports.util.*;
import pl.net.bluesoft.rnd.apertereports.util.validators.CronValidator;
import pl.net.bluesoft.rnd.apertereports.xml.XmlHelper;

import javax.xml.bind.JAXBException;
import java.util.*;

import static pl.net.bluesoft.rnd.apertereports.data.ReportOrder.Status.FAILED;
import static pl.net.bluesoft.rnd.apertereports.data.ReportOrder.Status.PROCESSING;
import static pl.net.bluesoft.rnd.apertereports.data.ReportOrder.Status.SUCCEEDED;
import static pl.net.bluesoft.rnd.apertereports.util.Constants.ReportType;

/**
 * Displays cyclic report order details (i.e. description, format, cron expression).
 * Each cyclic report can be configured to use a different set of report parameters.
 * These are configurable from this panel.
 */
public abstract class CyclicReportDetailsComponent extends CustomComponent {
    /**
     * Current cyclic report.
     */
    private CyclicReportOrder cyclicReport;

    private VerticalLayout mainPanel = new VerticalLayout();

    private VerticalLayout reportDetailsPanel = new VerticalLayout();
    private ReportParametersComponent parametersComponent;
    private Panel reportParametersPanel = new Panel(TM.get("dashboard.edit.report.parameters"));

    /**
     * Report template select.
     */
    private Select reportSelect;
    /**
     * Currently selected report template.
     */
    private ReportTemplate report;

    /**
     * The cyclic report detail fields.
     */
    private TextField descriptionField = new TextField(TM.get("cyclic.report.table.desc"));
    private TextField statusField = new TextField(TM.get("cyclic.report.table.status"));
    private TextField cronExpressionField = new TextField(TM.get("cyclic.report.table.when"));
    private CheckBox enabledCheckBox = new CheckBox(TM.get("cyclic.report.enabled"));
    private TextField emailField = new TextField(TM.get("cyclic.report.email"));
    private Select outputFormatSelect = new Select(TM.get("cyclic.report.email.format"));

    /**
     * Buttons.
     */
    private Button saveButton = new Button(TM.get("cyclic.report.update"));
    private Button cancelButton = new Button(TM.get("dashboard.edit.cancel"));
    private Button downloadButton = new Button(TM.get("cyclic.report.download"));
    private Select downloadFormatSelect = new Select();

    public CyclicReportDetailsComponent(CyclicReportOrder cyclicReport) {
        this.cyclicReport = cyclicReport != null ? new CyclicReportOrder(cyclicReport) : new CyclicReportOrder();
        this.report = this.cyclicReport.getReport();
        initView();
        initData();
        setCompositionRoot(mainPanel);
    }

    /**
     * Initializes the data. The report templates are displayed sorted by name.
     */
    private void initData() {
        for (ReportType rt : ReportType.values()) {
            downloadFormatSelect.addItem(rt);
            downloadFormatSelect.setItemCaption(rt, rt.name());
            outputFormatSelect.addItem(rt);
            outputFormatSelect.setItemCaption(rt, rt.name());
        }
        reportSelect.removeAllItems();
        List<ReportTemplate> reports = new ArrayList<ReportTemplate>(ReportTemplateDAO.fetchAllReports(true));
        Collections.sort(reports, new Comparator<ReportTemplate>() {
            @Override
            public int compare(ReportTemplate o1, ReportTemplate o2) {
                return o1.getReportname().compareTo(o2.getReportname());
            }
        });
        for (ReportTemplate rep : reports) {
            reportSelect.addItem(rep);
            reportSelect.setItemCaption(rep, rep.getReportname() + " (" + rep.getDescription() + ")");
            if (report != null && rep.getId().equals(report.getId())) {
                reportSelect.setValue(rep);
            }
        }
        initReportParameters();
        initOtherReportData();
    }

    /**
     * Gets a message key according to a given report status.
     *
     * @param status Report status
     * @return A message key
     */
    private String getStatusKey(Status status) {
        if (FAILED.equals(status)) {
            return "report_order.table.status.failed";
        }
        else if (SUCCEEDED.equals(status)) {
            return "report_order.table.status.succeeded";
        }
        else if (PROCESSING.equals(status)) {
            return "report_order.table.status.processing";
        }
        return "report_order.table.status.new";
    }

    /**
     * Initializes the values of the report detail fields.
     */
    private void initOtherReportData() {
        if (cyclicReport != null) {
            descriptionField.setValue(cyclicReport.getDescription() != null ? cyclicReport.getDescription() : "");
            ReportOrder rep = cyclicReport.getProcessedOrder() != null ? cyclicReport.getProcessedOrder() : cyclicReport.getReportOrder();
            statusField.setValue(TM.get(getStatusKey(rep != null ? rep.getReportStatus() : null)));
            try {
                cronExpressionField.setValue(cyclicReport.getCronSpec() != null ? cyclicReport.getCronSpec() : "");
            }
            catch (Validator.InvalidValueException e) {
                cronExpressionField.setValue("");
            }
            enabledCheckBox.setValue(cyclicReport.getEnabled() != null ? cyclicReport.getEnabled() : Boolean.FALSE);
            emailField.setValue(cyclicReport.getRecipientEmail() != null ? cyclicReport.getRecipientEmail() : "");
            outputFormatSelect.setValue(cyclicReport.getOutputFormat() != null ? ReportType.valueOf(cyclicReport.getOutputFormat()) : "");
        }
    }

    /**
     * Initializes the fields of the report details. An instance of {@link CronValidator} is added to a text field
     * responsible for displaying the cron expression.
     */
    private void initView() {
        mainPanel.addComponent(new Label(TM.get("cyclic.report.name") + ": " + (cyclicReport.getComponentId() != null ?
                cyclicReport.getComponentId() : TM.get("cyclic.report.new"))));
        mainPanel.addComponent(reportDetailsPanel);
        mainPanel.addComponent(getButtonsPanel());
        mainPanel.setSpacing(true);
        mainPanel.setSizeFull();

        reportSelect = new Select(TM.get("dashboard.edit.table.report"));
        reportSelect.setWidth("-1px");
        reportSelect.setImmediate(true);
        reportSelect.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        reportSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                report = (ReportTemplate) reportSelect.getValue();
                initReportParameters();
                initOtherReportData();
            }
        });

        reportDetailsPanel.addComponent(reportSelect);
        reportDetailsPanel.addComponent(reportParametersPanel);
        reportDetailsPanel.addComponent(descriptionField);
        reportDetailsPanel.addComponent(cronExpressionField);
        reportDetailsPanel.addComponent(statusField);
        reportDetailsPanel.addComponent(enabledCheckBox);
        reportDetailsPanel.addComponent(emailField);
        reportDetailsPanel.addComponent(outputFormatSelect);
        statusField.setEnabled(false);
        descriptionField.setRequired(true);
        emailField.setRequired(true);

        outputFormatSelect.setRequired(true);
        outputFormatSelect.setMultiSelect(false);
        outputFormatSelect.setImmediate(true);
        outputFormatSelect.setNullSelectionAllowed(false);
        outputFormatSelect.setWidth(6, UNITS_EM);
        outputFormatSelect.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);

        cronExpressionField.setRequired(true);
        cronExpressionField.addValidator(new CronValidator(TM.get("cyclic.report.when.validationError")));
        cronExpressionField.setInvalidAllowed(false);
        cronExpressionField.setImmediate(true);
        cronExpressionField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (cronExpressionField.isValid()) {
                    cronExpressionField.setComponentError(null);
                    cronExpressionField.setValidationVisible(false);
                }
            }
        });

        reportParametersPanel.setHeight(400, UNITS_PIXELS);
        reportParametersPanel.setWidth(600, UNITS_PIXELS);
        VerticalLayout vl = new VerticalLayout();
        vl.setHeight(-1, UNITS_PIXELS);
        vl.setWidth(100, UNITS_PERCENTAGE);
        vl.setSpacing(true);
        reportParametersPanel.setContent(vl);
        reportParametersPanel.setScrollable(true);
    }

    /**
     * Initializes a selected report parameters. The parameters are show in a {@link ReportParametersComponent}.
     * The buttons are available only when a report template has been selected.
     */
    private void initReportParameters() {
        reportParametersPanel.removeAllComponents();
        if (report != null) {
            try {
                reportParametersPanel.addComponent(new TPTLazyLoadingLayout(parametersComponent =
                        new ReportParametersComponent(report.getContent() != null ? String.valueOf(report.getContent()) : "",
                                report.getId(), XmlHelper.xmlAsParameters(cyclicReport.getParametersXml() != null ?
                                String.valueOf(cyclicReport.getParametersXml()) : ""), false, true, false), true));
            }
            catch (Exception e) {
                ExceptionUtil.logSevereException(e);
                NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"));
            }
        }
        reportParametersPanel.setVisible(report != null);
        saveButton.setEnabled(report != null);
        downloadButton.setEnabled(report != null);
    }

    /**
     * Adds listeners to buttons and wraps them in a {@link SimpleHorizontalLayout}.
     *
     * @return A layout with buttons
     */
    private Component getButtonsPanel() {
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (validate()) {
                    updateReportData();
                    onConfirm();
                }
                else {
                    NotificationUtil.validationErrors(getWindow());
                }
            }
        });
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onCancel();
            }
        });
        downloadButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (report != null && validateParameters()) {
                    ReportType reportType = (ReportType) downloadFormatSelect.getValue();
                    try {
                        ReportMaster reportMaster = new ReportMaster(String.valueOf(report.getContent()),
                                report.getId());
                        Map<String, String> parameters = parametersComponent.collectParametersValues();
                        byte[] data = reportMaster.generateAndExportReport(parameters, reportType.name(), ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), report.getReportname(), data, reportType.name());
                    }
                    catch (JRException e) {
                        ExceptionUtil.logSevereException(e);
                        NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"));
                    }
                }
            }
        });
        downloadFormatSelect.setImmediate(true);
        downloadFormatSelect.setNullSelectionAllowed(false);
        downloadFormatSelect.setWidth(6, UNITS_EM);
        downloadFormatSelect.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);

        return new SimpleHorizontalLayout(saveButton, cancelButton, downloadFormatSelect, downloadButton);
    }

    /**
     * Validates report parameters.
     *
     * @return <code>TRUE</code> if the parameters contain no errors
     */
    private boolean validateParameters() {
        return parametersComponent.validateForm();
    }

    /**
     * Validates the whole cyclic report details form.
     *
     * @return <code>TRUE</code> if the form is valid
     */
    private boolean validate() {
        return report != null && cronExpressionField.getValue() != null && cronExpressionField.isValid()
                && StringUtils.isNotEmpty((String) descriptionField.getValue()) && outputFormatSelect.getValue() != null
                && StringUtils.isNotEmpty((String) emailField.getValue()) && validateParameters();
    }

    /**
     * Updates the report data. This should be called after a positive validation.
     */
    private void updateReportData() {
        try {
            cyclicReport.setCronSpec(cronExpressionField.getValue().toString());
            cyclicReport.setDescription(descriptionField.getValue().toString());
            cyclicReport.setEnabled((Boolean) enabledCheckBox.getValue());
            cyclicReport.setOutputFormat(outputFormatSelect.getValue().toString());
            cyclicReport.setParametersXml(XmlHelper.mapAsXml(parametersComponent.collectParametersValues()).toCharArray());
            cyclicReport.setRecipientEmail(emailField.getValue().toString());
            cyclicReport.setReport(report);
        }
        catch (JAXBException e) {
            ExceptionUtil.logSevereException(e);
            NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"));
        }
    }

    /**
     * Gets currently displayed cyclic report.
     *
     * @return A cyclic report
     */
    public CyclicReportOrder getCyclicReportOrder() {
        return cyclicReport;
    }

    /**
     * Invoked after the form was validated successfully and the cyclic report config was updated.
     */
    public abstract void onConfirm();

    /**
     * Invoked after the cancel button was clicked.
     */
    public abstract void onCancel();

}
