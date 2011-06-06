package pl.net.bluesoft.rnd.vries.dashboard;

import com.vaadin.data.Property;
import com.vaadin.data.validator.IntegerValidator;
import com.vaadin.ui.*;
import eu.livotov.tpt.gui.widgets.TPTLazyLoadingLayout;
import eu.livotov.tpt.i18n.TM;
import net.sf.jasperreports.engine.JRException;
import pl.net.bluesoft.rnd.vries.components.HelpLayout;
import pl.net.bluesoft.rnd.vries.components.HelpWindow.Module;
import pl.net.bluesoft.rnd.vries.components.HelpWindow.Tab;
import pl.net.bluesoft.rnd.vries.components.ReportParametersComponent;
import pl.net.bluesoft.rnd.vries.components.SimpleHorizontalLayout;
import pl.net.bluesoft.rnd.vries.dao.CyclicReportOrderDAO;
import pl.net.bluesoft.rnd.vries.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.vries.dashboard.cyclic.CyclicReportsPanel;
import pl.net.bluesoft.rnd.vries.data.CyclicReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportTemplate;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.ExceptionUtil;
import pl.net.bluesoft.rnd.vries.util.NotificationUtil;
import pl.net.bluesoft.rnd.vries.xml.ReportConfig;
import pl.net.bluesoft.rnd.vries.xml.ReportConfigParameter;
import pl.net.bluesoft.rnd.vries.xml.XmlHelper;

import javax.xml.bind.JAXBException;
import java.util.*;

/**
 * Displays a configuration panel for a dashboard report. This component is used
 * in the portlet edit mode to setup things like cache timeout, report parameters
 * or available formats.
 */
public abstract class ReportDetailsComponent extends CustomComponent {
    /**
     * Current report template.
     */
    private ReportTemplate reportTemplate;
    /**
     * Current report config.
     */
    private ReportConfig reportConfig;
    /**
     * Table selected item.
     */
    private Object selectedItem = null;

    /**
     * Common buttons.
     */
    private Button saveButton = new Button(TM.get("dashboard.edit.save"));
    private Button cancelButton = new Button(TM.get("dashboard.edit.cancel"));
    private Button openCyclicReportsButton = new Button(TM.get("dashboard.edit.cyclicReports"));

    private VerticalLayout mainPanel = new VerticalLayout();
    private VerticalLayout reportDetailsPanel = new VerticalLayout();

    private Select reportSelect;
    private ReportParametersComponent reportParametersComponent;
    private Panel reportParametersPanel = new Panel(TM.get("dashboard.edit.report.parameters"));

    private TextField cacheTimeoutField = new TextField();
    private CheckBox allowRefreshField = new CheckBox();
    private Map<String, CheckBox> formatsMap = new HashMap<String, CheckBox>();

    private boolean readonly;

    private CyclicReportsPanel cyclicReportsPanel;

    public ReportDetailsComponent(ReportTemplate reportTemplate, ReportConfig reportConfig, boolean readonly) {
        this.reportTemplate = reportTemplate;
        this.reportConfig = reportConfig;
        this.readonly = readonly;
        initView();
        initData();
        setCompositionRoot(mainPanel);
    }

    /**
     * Setups the button panel.
     *
     * @return A horizontal layout of buttons
     */
    private Component getButtonsPanel() {
        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (validate()) {
                    selectedItem = reportSelect.getValue();
                    reportTemplate = selectedItem instanceof CyclicReportOrder ? ((CyclicReportOrder) selectedItem).getReport() :
                            (ReportTemplate) selectedItem;
                    updateReportConfig();
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
        openCyclicReportsButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                openEditCyclicReports();
            }
        });
        return new SimpleHorizontalLayout(saveButton, cancelButton, openCyclicReportsButton);
    }

    /**
     * Opens the cyclic reports layout. The content of the current window is replaced with {@link CyclicReportsPanel}.
     */
    private void openEditCyclicReports() {
        cyclicReportsPanel = new CyclicReportsPanel() {
            @Override
            public void onConfirm() {
                super.onConfirm();
                removePanel();
                initData();
            }

            @Override
            public void onCancel() {
                super.onCancel();
                removePanel();
            }

            private void removePanel() {
                cyclicReportsPanel = null;
                ReportDetailsComponent.this.setCompositionRoot(mainPanel);
            }
        };
        setCompositionRoot(cyclicReportsPanel);
    }

    /**
     * Initializes the GUI components of this view.
     */
    private void initView() {
        HorizontalLayout helpLayout = new HelpLayout(Module.DASHBOARD, Tab.REPORT_DETAILS);

        mainPanel.addComponent(helpLayout);
        mainPanel.addComponent(reportDetailsPanel);
        mainPanel.addComponent(getButtonsPanel());
        mainPanel.setSizeFull();

        reportSelect = new Select(TM.get("dashboard.edit.table.report"));
        reportSelect.setImmediate(true);
        reportSelect.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
        reportSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                selectedItem = reportSelect.getValue();
                reportTemplate = selectedItem instanceof CyclicReportOrder ? ((CyclicReportOrder) selectedItem).getReport() :
                        (ReportTemplate) selectedItem;
                initReportParameters();
            }
        });

        reportDetailsPanel.addComponent(reportSelect);

        reportParametersPanel.setHeight(400, UNITS_PIXELS);
        reportParametersPanel.setWidth(600, UNITS_PIXELS);
        VerticalLayout vl = new VerticalLayout();
        vl.setHeight(-1, UNITS_PIXELS);
        vl.setWidth(100, UNITS_PERCENTAGE);
        vl.setSpacing(true);
        reportParametersPanel.setContent(vl);
        reportParametersPanel.setScrollable(true);

        reportDetailsPanel.addComponent(reportParametersPanel);

        cacheTimeoutField.setRequired(true);
        cacheTimeoutField.setInvalidAllowed(false);
        cacheTimeoutField.setImmediate(true);
        cacheTimeoutField.setMaxLength(4);
        cacheTimeoutField.setColumns(3);
        cacheTimeoutField.addValidator(new IntegerValidator(TM.get("dashboard.edit.report.cacheTimeout.error")));
        cacheTimeoutField.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (cacheTimeoutField.isValid()) {
                    cacheTimeoutField.setComponentError(null);
                    cacheTimeoutField.setValidationVisible(false);
                }
            }
        });

        reportDetailsPanel.addComponent(new SimpleHorizontalLayout(new Label(TM.get("dashboard.edit.report.cacheTimeout")), cacheTimeoutField,
                new Label(" s [" + TM.get("dashboard.edit.report.cacheTimeout.instructions") + "]")));

        HorizontalLayout hl = new HorizontalLayout();
        hl.setSpacing(true);
        hl.setSizeFull();
        hl.addComponent(new Label(TM.get("dashboard.edit.report.allowRefresh")));
        hl.addComponent(allowRefreshField);
        reportDetailsPanel.addComponent(hl);

        for (Constants.ReportType rt : Constants.ReportType.values()) {
            formatsMap.put(rt.name(), new CheckBox(rt.name()));
        }
        GridLayout grid = new GridLayout(formatsMap.values().size(), 1);
        grid.setSizeFull();
        grid.setSpacing(true);
        for (CheckBox cb : formatsMap.values()) {
            grid.addComponent(cb);
        }
        for (int i = 0; i < grid.getColumns(); ++i) {
            grid.setColumnExpandRatio(i, 2f);
        }
        reportDetailsPanel.addComponent(new SimpleHorizontalLayout(new Label(TM.get("dashboard.edit.report.formats")), grid));
    }

    /**
     * Validates the whole form.
     *
     * @return <code>TRUE</code> if the form is valid
     */
    private boolean validate() {
        boolean formatSelected = false;
        for (CheckBox cb : formatsMap.values()) {
            if (cb.booleanValue()) {
                formatSelected = true;
                break;
            }
        }
        return reportSelect.getValue() != null && reportParametersComponent != null && reportParametersComponent.validateForm() &&
                cacheTimeoutField.isValid() && formatSelected;
    }

    /**
     * Fills in the data of all the GUI components. The data is fetched from database.
     */
    private void initData() {
        String reportType = TM.get("dashboard.edit.table.type.online");
        Collection<ReportTemplate> reports = ReportTemplateDAO.fetchAllReports(true);
        for (ReportTemplate rep : reports) {
            reportSelect.addItem(rep);
            reportSelect.setItemCaption(rep, reportType + ": " + rep.getReportname() + " (" + rep.getDescription() + ")");
            if (reportConfig != null && rep.getId().equals(reportConfig.getReportId())) {
                reportSelect.setValue(rep);
            }
        }
        reportType = TM.get("dashboard.edit.table.type.cyclic");
        Collection<CyclicReportOrder> cyclicReportOrders = CyclicReportOrderDAO.fetchAllEnabledCyclicReports();
        for (CyclicReportOrder rep : cyclicReportOrders) {
            ReportTemplate r = rep.getReport();
            reportSelect.addItem(rep);
            reportSelect.setItemCaption(rep, reportType + ": " + r.getReportname() + " (" + r.getDescription() + ")");
            if (reportConfig != null && rep.getId().equals(reportConfig.getCyclicReportId())) {
                reportSelect.setValue(rep);
            }
        }
        initReportParameters();
        initOtherParams();
    }

    /**
     * Setups the values of the dashboard configuration (cache timeout, refreshing, formats etc).
     */
    private void initOtherParams() {
        if (reportConfig != null) {
            if (reportConfig.getCacheTimeout() != null) {
                cacheTimeoutField.setValue(reportConfig.getCacheTimeout().toString());
            }
            allowRefreshField.setValue(reportConfig.getAllowRefresh());
            for (String format : reportConfig.getAllowedFormatsAsList()) {
                CheckBox cb = formatsMap.get(format);
                if (cb != null) {
                    cb.setValue(true);
                }
            }
        }
    }

    /**
     * Sets the values of the report parameters used by the current report config.
     * The parameters are displayed in a {@link ReportParametersComponent} instance.
     */
    private void initReportParameters() {
        reportParametersPanel.removeAllComponents();
        if (reportTemplate != null) {
            try {
                List<ReportConfigParameter> params = reportConfig != null ? reportConfig.getParameters() : null;
                if (selectedItem instanceof CyclicReportOrder) {
                    CyclicReportOrder cro = (CyclicReportOrder) selectedItem;
                    params = XmlHelper.xmlAsParameters(cro.getParametersXml() != null ? String.valueOf(cro.getParametersXml()) : "");
                }
                readonly = selectedItem instanceof CyclicReportOrder;
                reportParametersPanel.addComponent(new TPTLazyLoadingLayout(reportParametersComponent =
                        new ReportParametersComponent(String.valueOf(reportTemplate.getContent()), reportTemplate.getId(), params,
                                false, true, readonly), true));
            }
            catch (JRException e) {
                ExceptionUtil.logSevereException(e);
                NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"));
            }
            catch (JAXBException e) {
                ExceptionUtil.logSevereException(e);
                NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"));
            }
        }
        reportParametersPanel.setVisible(reportTemplate != null);
        saveButton.setEnabled(reportTemplate != null);
    }

    /**
     * Gets current report config (updated or not).
     *
     * @return A report config
     */
    public ReportConfig getReportConfig() {
        return reportConfig;
    }

    /**
     * Updates current report config with selected values.
     */
    private void updateReportConfig() {
        if (reportConfig == null) {
            reportConfig = new ReportConfig();
        }
        List<String> allowedFormats = new ArrayList<String>();
        for (CheckBox cb : formatsMap.values()) {
            if (cb.booleanValue()) {
                allowedFormats.add(cb.getCaption());
            }
        }
        reportConfig.setAllowedFormatsFromList(allowedFormats);
        reportConfig.setAllowRefresh(allowRefreshField.booleanValue());
        reportConfig.setCyclicReportId(selectedItem instanceof CyclicReportOrder ? ((CyclicReportOrder) selectedItem).getId() : null);
        reportConfig.setReportId(selectedItem instanceof CyclicReportOrder ? null : reportTemplate.getId());
        reportConfig.setCacheTimeout(Integer.parseInt("" + cacheTimeoutField.getValue()));
        reportConfig.setParameters(XmlHelper.mapToParameterList(reportParametersComponent.collectParametersValues()));
    }

    /**
     * Invoked after the selected values are discarded by the user (by clicking the cancel button).
     */
    public abstract void onCancel();

    /**
     * Invoked after the current report config has been updated. The update is performed after a positive validation of the form.
     */
    public abstract void onConfirm();
}
