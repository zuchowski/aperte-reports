package org.apertereports.components;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.apertereports.backbone.jms.ARJmsFacade;
import org.apertereports.backbone.scheduler.CyclicReportScheduler;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ARConstants.ReportType;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.CyclicReportConfigDAO;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.CyclicReportConfig;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.CronExpressionValidator;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.VaadinUtil;
import org.quartz.SchedulerException;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.EmailValidator;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import java.util.LinkedList;
import java.util.List;
import org.apertereports.common.users.User;
import org.apertereports.common.xml.config.ReportConfigParameter;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class CyclicReportsComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String COMPONENT_STYLE_NAME = "borderless light";
    private PaginatedPanelList<CyclicReportConfig, CyclicReportPanel> list;
    private static final String DESCRIPTION_STYLE = "small";
    private static final String FORMAT_STYLE = "h4";
    private static final String ORDER_DESCRIPTION = "description";
    private static final String ORDER_CRON_SPEC = "cronSpec";
    private static final String ORDER_RECIPIENT_EMAIL = "recipientEmail";
    private static final String ORDER_OUTPUT_FORMAT = "outputFormat";
    private static final String ORDER_REPORT_REPORTNAME = "reportname";
    private static final String ORDER_REPORT = "report";
    private static final String VALIDATION_EMAIL = "validation.email";
    private static final String VALIDATION_CRON_EXPRESSION = "validation.cronExpression";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR = "cycylic.edit.required-error.";
    private static final String CYCLIC_EDIT_INPUT_PROMPT = "cyclic.edit.input-prompt.";
    private static final String CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME = "cyclic.edit.input-prompt.reportname";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME = "cycylic.edit.required-error.reportname";
    private static final String CYCLIC_EDIT_INPUT_PROMPT_FORMAT = "cyclic.edit.input-prompt.format";
    private static final String CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT = "cycylic.edit.required-error.format";
    private boolean addingNew = false;
    private Component addOrEditComponent;
    private User user = null;
    private Button addButton;
    private static Logger logger = LoggerFactory.getLogger(CyclicReportsComponent.class);

    public CyclicReportsComponent() {
        init();
    }

    public void initData(User user) {
        this.user = user;
        addButton.setVisible(user != null);
        list.filter(null);
    }

    private void init() {

        HorizontalLayout header = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);
        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, header, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        });
        filterField.setWidth("150px");

        UiFactory.createSpacer(header, "10px", null);

        UiFactory.createSpacer(header, FAction.SET_EXPAND_RATIO_1_0);
        addButton = UiFactory.createButton(UiIds.LABEL_ADD, header, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                addNew();
            }
        }, FAction.SET_INVISIBLE);

        list = new PaginatedPanelList<CyclicReportConfig, CyclicReportsComponent.CyclicReportPanel>(PAGE_SIZE) {

            @Override
            protected CyclicReportPanel transform(CyclicReportConfig object) {
                return new CyclicReportPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return CyclicReportConfigDAO.count(user, filter);
            }

            @Override
            protected Collection<CyclicReportConfig> fetch(String filter, int firstResult, int maxResults) {
                return CyclicReportConfigDAO.fetch(user, filter, firstResult, maxResults);
            }
        };

        addComponent(list);
        setStyleName(COMPONENT_STYLE_NAME);
        list.filter(null);

        if (!ARJmsFacade.isJmsAvailable()) {
            VerticalLayout footer = UiFactory.createVLayout(this, FAction.SET_FULL_WIDTH);
            UiFactory.createSpacer(footer, null, "10px");
            UiFactory.createLabel("JMS unavailable, cyclic reports execution is disabled!", footer);
        }
    }

    private void addNew() {
        if (addingNew) {
            return;
        }
        addingNew = true;
        addButton.setVisible(false);

        CyclicReportConfig order = new CyclicReportConfig();
        VerticalLayout vl = UiFactory.createVLayout(null);
        UiFactory.createSpacer(vl, null, "5px");
        EditCyclicReportPanel ecrp = new EditCyclicReportPanel(order, true);
        vl.addComponent(ecrp);

        addOrEditComponent = vl;
        list.addComponent(addOrEditComponent, 0);
    }

    private class CyclicReportPanel extends Panel {

        private static final String REPORT_PANEL_STYLE = COMPONENT_STYLE_NAME;
        private CyclicReportConfig config;
        private ReportParamPanel paramsPanel;
        private Button toggleParamsButton;
        private HorizontalLayout nameContainer;
        private Button enabledButton;

        public CyclicReportPanel(CyclicReportConfig config) {
            this.config = config;
            setStyleName(REPORT_PANEL_STYLE);
            BeanItem<CyclicReportConfig> item = new BeanItem<CyclicReportConfig>(config);
            ((AbstractLayout) getContent()).setMargin(true, true, false, true);

            HorizontalLayout row1 = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH, FAction.SET_SPACING);
            HorizontalLayout row2 = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH, FAction.SET_SPACING);

            //name
            nameContainer = UiFactory.createHLayout(row1, FAction.ALIGN_LEFT, FAction.SET_SPACING);
            nameContainer.setEnabled(Boolean.TRUE.equals(CyclicReportPanel.this.config.getEnabled()));
            UiFactory.createLabel(new BeanItem<ReportTemplate>(config.getReport()),
                    ORDER_REPORT_REPORTNAME, nameContainer, FORMAT_STYLE, FAction.ALIGN_LEFT);
            //format
            UiFactory.createLabel(item, ORDER_OUTPUT_FORMAT, nameContainer, FAction.ALIGN_LEFT);
            UiFactory.createSpacer(row1, FAction.SET_EXPAND_RATIO_1_0);
            //enable/disable button
            enabledButton = UiFactory.createButton(getStateLabelCaption(), row1, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleEnable();
                }

                private void toggleEnable() {
                    boolean enabled = !Boolean.TRUE.equals(CyclicReportPanel.this.config.getEnabled());
                    CyclicReportPanel.this.config.setEnabled(enabled);
                    nameContainer.setEnabled(enabled);
                    enabledButton.setCaption(VaadinUtil.getValue(getStateLabelCaption()));
                    CyclicReportConfigDAO.saveOrUpdate(CyclicReportPanel.this.config);

                    scheduleOrUnschedule(CyclicReportPanel.this.config);
                }
            }, FAction.ALIGN_RIGTH);
            HorizontalLayout hl = UiFactory.createHLayout(row2);
            VerticalLayout vl = UiFactory.createVLayout(hl);
            UiFactory.createLabel(item, ORDER_RECIPIENT_EMAIL, vl);
            UiFactory.createSpacer(row2, FAction.SET_EXPAND_RATIO_1_0);
            UiFactory.createLabel(item, ORDER_CRON_SPEC, row2);
            UiFactory.createLabel(item, ORDER_DESCRIPTION, vl, DESCRIPTION_STYLE, FAction.SET_FULL_WIDTH);
            HorizontalLayout row3 = UiFactory.createHLayout(vl, FAction.SET_SPACING);
            toggleParamsButton = UiFactory.createButton(UiIds.LABEL_PARAMETERS, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParamsPanel();
                }
            });
            UiFactory.createButton(UiIds.LABEL_EDIT, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    edit();
                }
            });
            UiFactory.createButton(UiIds.LABEL_DELETE, row3, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    remove();
                }
            });

        }

        private String getStateLabelCaption() {
            return Boolean.TRUE.equals(config.getEnabled()) ? UiIds.LABEL_DISABLE : UiIds.LABEL_ENABLE;
        }

        protected void remove() {
            try {
                CyclicReportScheduler.unschedule(config);
            } catch (SchedulerException ex) {
                throw new ARRuntimeException(ex);
            }
            CyclicReportConfigDAO.remove(config);
            list.removeComponent(this);
        }

        protected void edit() {
            addOrEditComponent = new EditCyclicReportPanel(this.config, false);
            list.replaceComponent(this, addOrEditComponent);
        }

        protected void toggleParamsPanel() {
            if (paramsPanel == null) {
                paramsPanel = createParamsPanel();
                addComponent(paramsPanel);
                toggleParamsButton.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PARAMETERS));
            } else {
                removeComponent(paramsPanel);
                paramsPanel = null;
                toggleParamsButton.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            }
        }

        private ReportParamPanel createParamsPanel() {
            List<ReportConfigParameter> params = new LinkedList<ReportConfigParameter>();
            String paramsXml = config.getParametersXml();
            if (paramsXml != null) {
                params = XmlReportConfigLoader.getInstance().xmlAsParameters(paramsXml);
            }

            final ReportParamPanel panel = new ReportParamPanel(config.getReport(), false, params);
            panel.setStyleName("borderless");
            HorizontalLayout hl = UiFactory.createHLayout(panel, FAction.SET_SPACING, FAction.SET_FULL_WIDTH);

            UiFactory.createButton(UiIds.LABEL_GENERATE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        if (!panel.validateForm()) {
                            return;
                        }
                        ReportMaster rm = new ReportMaster(config.getReport().getContent(), config.getReport().getId().toString(),
                                new ReportTemplateProvider(),user);
                        byte[] reportData = rm.generateAndExportReport(config.getOutputFormat(),
                                new HashMap<String, Object>(panel.collectParametersValues()),
                                ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), config.getReport().getReportname(), reportData,
                                config.getOutputFormat());
                    } catch (ARException e) {
                        throw new ARRuntimeException(e);
                    }
                }
            });

            UiFactory.createSpacer(hl, FAction.SET_EXPAND_RATIO_1_0);
            UiFactory.createButton(UiIds.LABEL_SAVE, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!paramsPanel.validateForm()) {
                        return;
                    }
                    config.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(
                            paramsPanel.collectParametersValues()));
                    CyclicReportConfigDAO.saveOrUpdate(config);
                    toggleParamsPanel();
                }
            }, FAction.ALIGN_RIGTH);

            UiFactory.createButton(UiIds.LABEL_CANCEL, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParamsPanel();
                }
            }, FAction.ALIGN_RIGTH);

            return panel;
        }
    }

    private void scheduleOrUnschedule(CyclicReportConfig config) {
        try {
            if (Boolean.TRUE.equals(config.getEnabled())) {
                CyclicReportScheduler.schedule(config);
            } else {
                CyclicReportScheduler.unschedule(config);
            }
        } catch (SchedulerException e) {
            throw new ARRuntimeException(e);
        }
    }

    private class EditCyclicReportPanel extends Panel {

        private CyclicReportConfig config;
        private EditCyclicReportForm form;
        private boolean newItem;

        public EditCyclicReportPanel(CyclicReportConfig config, boolean newItem) {
            this.newItem = newItem;
            this.config = config;

            setCaption(VaadinUtil.getValue(newItem ? UiIds.LABEL_ADDING : UiIds.LABEL_EDITION));

            setWidth("100%");
            form = new EditCyclicReportForm(config);
            addComponent(form);
            UiFactory.createSpacer(this, null, "5px");
            HorizontalLayout buttons = UiFactory.createHLayout(this, FAction.SET_SPACING, FAction.SET_FULL_WIDTH);
            UiFactory.createSpacer(buttons, FAction.SET_EXPAND_RATIO_1_0);
            UiFactory.createButton(UiIds.LABEL_SAVE, buttons, new ClickListener() {
                @Override
                public void buttonClick(ClickEvent event) {
                    save();
                }
            }, FAction.ALIGN_RIGTH);
            UiFactory.createButton(UiIds.LABEL_CANCEL, buttons, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    cancel();
                }
            }, FAction.ALIGN_RIGTH);
        }

        protected void cancel() {
            form.discard();
            if (!newItem) {
                list.replaceComponent(this, new CyclicReportPanel(config));
            } else {
                list.removeComponent(this);
            }
            finish();
        }

        protected void save() {
            try {
                form.commit();
                CyclicReportConfigDAO.saveOrUpdate(config);
                scheduleOrUnschedule(config);
                list.replaceComponent(this, new CyclicReportPanel(config));
                finish();
            } catch (InvalidValueException e) {
                logger.warn("Edit cyclic report: invalid user input", e);
            }
        }

        private void finish() {
            addingNew = false;
            addButton.setVisible(true);
            list.removeComponent(addOrEditComponent);
        }
    }

    private class EditCyclicReportForm extends Form {

        private GridLayout layout;

        public EditCyclicReportForm(CyclicReportConfig config) {
            layout = new GridLayout(1, 5);
            layout.setWidth("100%");
            layout.setSpacing(true);
            setLayout(layout);
            setFormFieldFactory(new EditCyclicFormFactory());
            setItemDataSource(new BeanItem<CyclicReportConfig>(config));
            setVisibleItemProperties(Arrays.asList(new String[]{ORDER_REPORT, ORDER_CRON_SPEC, ORDER_RECIPIENT_EMAIL,
                        ORDER_OUTPUT_FORMAT, ORDER_DESCRIPTION}));
            setWidth("100%");
            setWriteThrough(false);
        }

        @Override
        protected void attachField(Object propertyId, Field field) {
            if (propertyId.equals(ORDER_REPORT)) {
                layout.addComponent(field, 0, 0);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_OUTPUT_FORMAT)) {
                layout.addComponent(field, 0, 1);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                layout.addComponent(field, 0, 2);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_CRON_SPEC)) {
                layout.addComponent(field, 0, 3);
                layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
            } else if (propertyId.equals(ORDER_DESCRIPTION)) {
                layout.addComponent(field, 0, 4);
            }
        }
    }

    private class EditCyclicFormFactory extends DefaultFieldFactory {

        private static final String CYCLIC_EDIT_CAPTION = "cyclic.edit.caption.";

        @Override
        public Field createField(Item item, Object propertyId, Component uiContext) {
            if (propertyId.equals(ORDER_OUTPUT_FORMAT)) {
                String value = (String) item.getItemProperty(ORDER_OUTPUT_FORMAT).getValue();
                if (value == null) {
                    value = ReportType.PDF.name();
                }
                ReportType selectedValue = ReportType.valueOf(value);
                ComboBox format = ComponentFactory.createFormatCombo(selectedValue, UiIds.LABEL_FORMAT);
                format.setRequired(true);
                format.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT));
                format.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_FORMAT));
                return format;
            } else if (propertyId.equals(ORDER_REPORT)) {
                ComboBox reportname = ComponentFactory.createReportTemplateCombo(
                        user, (ReportTemplate) item.getItemProperty(ORDER_REPORT).getValue(), CYCLIC_EDIT_CAPTION + propertyId);
                reportname.setRequired(true);
                reportname.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME));
                reportname.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME));
                return reportname;
            } else if (propertyId.equals(ORDER_DESCRIPTION)) {
                TextField field = (TextField) super.createField(item, propertyId, uiContext);
                field.setWidth("100%");
                field.setCaption(VaadinUtil.getValue(UiIds.LABEL_DESCRIPTION));
                field.setNullRepresentation("");
                field.setInputPrompt(VaadinUtil.getValue(UiIds.LABEL_DESCRIPTION));
                return field;
            } else {
                Field field = super.createField(item, propertyId, uiContext);
                field.setCaption(VaadinUtil.getValue(CYCLIC_EDIT_CAPTION + propertyId));
                if (propertyId.equals(ORDER_CRON_SPEC) || propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                    ((TextField) field).setNullRepresentation("");
                    ((TextField) field).setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT + propertyId));
                }
                if (propertyId.equals(ORDER_CRON_SPEC)) {
                    field.addValidator(new CronExpressionValidator(VaadinUtil.getValue(VALIDATION_CRON_EXPRESSION)));
                    field.setRequired(true);
                    field.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR + propertyId));
                }
                if (propertyId.equals(ORDER_RECIPIENT_EMAIL)) {
                    field.addValidator(new EmailValidator(VaadinUtil.getValue(VALIDATION_EMAIL)));
                }
                return field;
            }
        }
    }
}
