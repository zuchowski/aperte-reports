package org.apertereports.dashboard;

import java.util.LinkedList;

import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.components.ReportParamPanel;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apertereports.AbstractReportingApplication;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.common.users.User;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.ui.UiIds;

/**
 * Class defines panel for dashboard report configuration
 */
@SuppressWarnings("serial")
public class EditDashboardComponentNew extends AbstractDashboardComponent {

    private static final String CACHE_TIMEOUT = "cacheTimeout";
    private static final String REPORT = "report";
    private static final String EXPORT_BUTTONS = "exportButtons";
    private static final String REFRESH_BUTTON = "refreshButton";
    private static final String DASHBOARD_EDIT_CAPTION_CACHE_TIMEOUT = "dashboard.edit.caption.cacheTimeout";
    private static final String DASHBOARD_EDIT_REQUIRED_ERROR_CACHE_TIMEOUT = "dashboard.edit.required-error.cacheTimeout";
    private static final String DASHBOARD_EDIT_INPUT_PROMPT_REPORT_ID = "dashboard.edit.input-prompt.reportId";
    private static final String DASHBOARD_EDIT_REQUIRED_ERROR_REPORT_ID = "dashboard.edit.required-error.reportId";
    private static final String DASHBOARD_EDIT_CAPTION_REPORT_ID = "dashboard.edit.caption.reportId";
    private static final String DASHBOARD_EDIT_CAPTION_SHOW_EXPORT_BUTTONS = "dashboard.edit.caption.showExportButtons";
    private static final String DASHBOARD_EDIT_CAPTION_SHOW_REFRESH_BUTTON = "dashboard.edit.caption.showRefreshButton";
    private Panel mainPanel;
    private ReportParamPanel paramsPanel = new ReportParamPanel();
    private Form form;
    private Item datasource;
    private ReportConfig reportConfig;
    private DisposeListener disposeListener = null;
    private AbstractReportingApplication app;

    @Override
    protected void initComponentData() {
        mainPanel = new Panel(VaadinUtil.getValue(UiIds.LABEL_CONFIGURATION));
        setCompositionRoot(mainPanel);

        app = (AbstractReportingApplication) getApplication();

        VerticalLayout vl = ComponentFactory.createVLayout(mainPanel, true, true);

        HorizontalLayout reportRow = ComponentFactory.createHLayoutFull(vl);
        reportRow.addComponent(form = new EditDashboardForm());

        vl.addComponent(paramsPanel);
        paramsPanel.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));

        HorizontalLayout hl = ComponentFactory.createHLayout(vl);
        ComponentFactory.createButton(UiIds.LABEL_OK, "", hl, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (!paramsPanel.validateForm()) {
                    return;
                }
                saveConfiguration();

                if (disposeListener != null) {
                    disposeListener.dispose();
                }
            }
        });
        ComponentFactory.createButton(UiIds.LABEL_CANCEL, "", hl, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                if (disposeListener != null) {
                    disposeListener.dispose();
                }
            }
        });
    }

    /**
     * Sets dispose listener
     *
     * @param disposeListener Dispose listener
     */
    public void setDisposeListener(DisposeListener disposeListener) {
        this.disposeListener = disposeListener;
    }

    private class EditDashboardForm extends Form {

        private GridLayout layout;

        public EditDashboardForm() {

            reportConfig = getCurrentConfig();

            ReportTemplate selectedReport = null;
            if (reportConfig.getReportId() != null) {
                selectedReport = ReportTemplateDAO.fetchById(app.getArUser(), reportConfig.getReportId());
            }

            layout = new GridLayout(3, 3);
            layout.setSpacing(true);
            layout.setWidth("100%");
            setLayout(layout);

            setFormFieldFactory(new EditDashboardFieldFactory());

            datasource = new PropertysetItem();
            datasource.addItemProperty(REPORT, new ObjectProperty<ReportTemplate>(selectedReport, ReportTemplate.class));
            datasource.addItemProperty(CACHE_TIMEOUT, new ObjectProperty<Integer>(reportConfig.getCacheTimeout()));
            datasource.addItemProperty(EXPORT_BUTTONS, new ObjectProperty<Boolean>(reportConfig.getAllowedFormats() != null));
            datasource.addItemProperty(REFRESH_BUTTON, new ObjectProperty<Boolean>(reportConfig.getAllowRefresh()));
            setItemDataSource(datasource);
        }

        @Override
        protected void attachField(Object propertyId, Field field) {

            if (propertyId.equals(REPORT)) {
                layout.addComponent(field, 0, 0, 1, 0);
            } else if (propertyId.equals(CACHE_TIMEOUT)) {
                layout.addComponent(field, 2, 0);
                layout.setComponentAlignment(field, Alignment.MIDDLE_RIGHT);
            } else if (propertyId.equals(EXPORT_BUTTONS)) {
                layout.addComponent(field, 0, 1, 2, 1);
            } else if (propertyId.equals(REFRESH_BUTTON)) {
                layout.addComponent(field, 0, 2, 2, 2);
            }
        }

        private class EditDashboardFieldFactory extends DefaultFieldFactory {

            @Override
            public Field createField(Item item, Object propertyId, Component uiContext) {
                if (propertyId.equals(REPORT)) {

                    paramsPanel = new ReportParamPanel();

                    //xxx it could be better to manage only list of names and above set only selected report name
                    //or maybe it is possible to manage ids
                    //some functionality could be developed in AbstractDashboardComponent
                    ComboBox field = ComponentFactory.createReportTemplateCombo(app.getArUser(), null, DASHBOARD_EDIT_CAPTION_REPORT_ID);
                    field.setRequired(true);
                    field.setRequiredError(VaadinUtil.getValue(DASHBOARD_EDIT_REQUIRED_ERROR_REPORT_ID));
                    field.setInputPrompt(VaadinUtil.getValue(DASHBOARD_EDIT_INPUT_PROMPT_REPORT_ID));
                    field.setImmediate(true);
                    field.addListener(new ValueChangeListener() {

                        @Override
                        public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
                            reloadParams((ReportTemplate) event.getProperty().getValue());
                        }
                    });
                    return field;
                } else if (propertyId.equals(CACHE_TIMEOUT)) {
                    Field field = super.createField(item, propertyId, uiContext);
                    field.setRequired(true);
                    field.setRequiredError(VaadinUtil.getValue(DASHBOARD_EDIT_REQUIRED_ERROR_CACHE_TIMEOUT));
                    field.setCaption(VaadinUtil.getValue(DASHBOARD_EDIT_CAPTION_CACHE_TIMEOUT));
                    field.setWidth("80px");
                    return field;
                } else if (propertyId.equals(EXPORT_BUTTONS)) {
                    return ComponentFactory.createCheckBox(DASHBOARD_EDIT_CAPTION_SHOW_EXPORT_BUTTONS, item, portletId, null);
                } else if (propertyId.equals(REFRESH_BUTTON)) {
                    return ComponentFactory.createCheckBox(DASHBOARD_EDIT_CAPTION_SHOW_REFRESH_BUTTON, item, portletId, null);
                }
                return null;
            }
        }
    }

    /**
     * Reloads params panel for selected report
     *
     * @param template Report for which params should be shown
     */
    protected void reloadParams(ReportTemplate template) {
        ReportParamPanel newParamsPanel = new ReportParamPanel(template, false, reportConfig.getParameters());
        mainPanel.replaceComponent(paramsPanel, newParamsPanel);
        paramsPanel = newParamsPanel;
    }

    private void saveConfiguration() {
        try {
            form.commit();
        } catch (InvalidValueException e) {
            return;
        }
        template = "<report idx=\"0\"></report>";

        ReportTemplate report = (ReportTemplate) datasource.getItemProperty(REPORT).getValue();

        ReportConfig config = new ReportConfig();
        config.setId(0);
        config.setReportId(report.getId());
        config.setCacheTimeout((Integer) datasource.getItemProperty(CACHE_TIMEOUT).getValue());
        config.setAllowRefresh((Boolean) datasource.getItemProperty(REFRESH_BUTTON).getValue());
        config.setParameters(XmlReportConfigLoader.getInstance().mapToParameterList(paramsPanel.collectParametersValues()));

        boolean addExportButtons = (Boolean) datasource.getItemProperty(EXPORT_BUTTONS).getValue();
        String types = null;
        if (addExportButtons) {
            types = ReportType.XLS + "," + ReportType.PDF + "," + ReportType.HTML + "," + ReportType.CSV;
        }
        config.setAllowedFormats(types);

        reportConfigs = new LinkedList<ReportConfig>();
        reportConfigs.add(config);

        saveData();
    }

    /**
     * Returns config for saved report
     *
     * @return Report config or
     * <code>null</code>
     */
    //todots consider moving config management to base class
    private ReportConfig getCurrentConfig() {
        if (reportConfigs == null || reportConfigs.isEmpty()) {
            return new ReportConfig();
        }
        return reportConfigs.get(0);
    }

    /**
     * Defines listener for this edit component for portlet application to allow
     * switch to view mode
     */
    public interface DisposeListener {

        /**
         * Invoked when this component should be disposed
         */
        public void dispose();
    }
}
