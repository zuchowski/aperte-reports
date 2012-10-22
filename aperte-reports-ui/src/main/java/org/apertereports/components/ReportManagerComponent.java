package org.apertereports.components;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.backbone.util.ReportOrderPusher;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.UserUtil;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.dao.CyclicReportOrderDAO;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.CyclicReportOrder;

/**
 * Component to manage reports.
 *
 * @author Zbigniew Malinowski
 *
 */
@SuppressWarnings("serial")
public class ReportManagerComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String DESC_STYLE = "small";
    private static final String CHANGED_DATE_STYLE = "h3";
    private static final String REPORT_NAME_STYLE = "h4";
    private static final String FILE_NAME_STYLE = "h3";
    private static final String EDIT_PANEL_STYLE = "bubble";
    private static final String PANEL_STYLE = "borderless";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String FILENAME_PROPERTY = "filename";
    private static final String CREATED_PROPERTY = "created";
    private static final String REPORTNAME_PROPERTY = "reportname";
    private static final String ALLOW_BACKGROUND_PROCESSING_PROPERTY = "allowBackgroundProcessing";
    private static final String ALLOW_ONLINE_DISPLAY_PROPERTY = "allowOnlineDisplay";
    private static final String ACTIVE_PROPERTY = "active";
    private static final String REPORT_MANAGER_NEW_REPORT_BUTTON = "report.manager.newReportButton";
    private static final String REPORT_MANAGER_ITEM_DOWNLOAD = "report.manager.item.download";
    private static final String REPORT_MANAGER_ITEM_EDIT = "report.manager.item.edit";
    private static final String REPORT_MANAGER_ITEM_UPLOAD_CHANGE = "report.manager.item.upload.change";
    private static final String REPORT_MANAGER_ITEM_REMOVE = "report.manager.item.remove";
    private static final String REPORT_MANAGER_ITEM_EDIT_DESC_PROMPT = "report.manager.item.edit.desc.prompt";
    private static final String BUTTON_CANCEL = "button.cancel";
    private static final String BUTTON_OK = "button.ok";
    private static final String REPORT_MANAGER_ITEM_EDIT_BACKGROUND = "report.manager.item.edit.background";
    private static final String REPORT_MANAGER_ITEM_EDIT_ONLINE = "report.manager.item.edit.online";
    private static final String REPORT_MANAGER_ITEM_EDIT_ACTIVE = "report.manager.item.edit.active";
    private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE = "report-params.toggle-visibility.true";
    private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE = "report-params.toggle-visibility.false";
    private static final String DUPLICATE_REPORT_NAME_TITLE = "exception.duplicate_report_name.title";
    private static final String DUPLICATE_REPORT_NAME_DESC = "exception.duplicate_report_name.desc";
    private static final String PARAMS_FORM_SEND_EMAIL = "params-form.send-email";
    private static final String MSG_REMOVING_REPORT = "report.manager.removing.report";
    private static final String MSG_REPORT_IS_USED = "report.manager.report.is.used";
    private static final String MSG_DO_YOU_WANT_TO_CONTINUE = "q.do.you.want.to.continue";
    private PaginatedPanelList<ReportTemplate, ReportItemPanel> list;
    private ReportReceiver newReportReceiver;

    public ReportManagerComponent() {

        init();
    }

    private void init() {
        VerticalLayout mainLayout = new VerticalLayout();
        newReportReceiver = new ReportReceiver(new ReportTemplate());
        newReportReceiver.addListener(new ReportReceivedListener() {

            @Override
            public void reportReceived(ReportTemplate reportTemplate) {
                addNewReport(reportTemplate);
            }
        });
        Upload newReportUpload = new Upload(null, newReportReceiver);
        newReportUpload.addListener((SucceededListener) newReportReceiver);
        newReportUpload.addListener((FailedListener) newReportReceiver);
        newReportUpload.setButtonCaption(VaadinUtil.getValue(REPORT_MANAGER_NEW_REPORT_BUTTON));
        newReportUpload.setImmediate(true);
        HorizontalLayout hl = ComponentFactory.createHLayoutFull(mainLayout);
        list = new PaginatedPanelList<ReportTemplate, ReportManagerComponent.ReportItemPanel>(PAGE_SIZE) {

            @Override
            protected ReportItemPanel transform(ReportTemplate object) {
                return new ReportItemPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportTemplateDAO.countMatching(filter);
            }

            @Override
            protected Collection<ReportTemplate> fetch(String filter, int firstResult, int maxResults) {
                return ReportTemplateDAO.fetch(filter, firstResult, maxResults);
            }
        };

        TextField search = ComponentFactory.createSearchBox(new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        }, hl);
        hl.setExpandRatio(search, 1.0f);
        hl.addComponent(newReportUpload);
        hl.setComponentAlignment(search, Alignment.MIDDLE_RIGHT);
        hl.setComponentAlignment(newReportUpload, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(list);
        addComponent(mainLayout);
        list.filter(null);
        setWidth("100%");
    }

    private void addNewReport(ReportTemplate reportTemplate) {
        ReportItemPanel reportItem = new ReportItemPanel(reportTemplate);
        list.addComponent(reportItem, 0);
        editReportData(reportItem);
        newReportReceiver.reportTemplate = new ReportTemplate();
    }

    /**
     * List item in edit state.
     *
     * @author Zbigniew Malinowski
     *
     */
    private class EditReportItemPanel extends Panel {

        private ReportItemPanel item;
        private ReportTemplate temporaryData;
        private BeanItem<ReportTemplate> beanItem;
        private TextField nameField;

        public EditReportItemPanel(ReportItemPanel item) {
            this.item = item;
            setStyleName(EDIT_PANEL_STYLE);
            deepCopy(item.reportTemplate, temporaryData = new ReportTemplate());
            beanItem = new BeanItem<ReportTemplate>(temporaryData);

            HorizontalLayout headerRow = ComponentFactory.createHLayoutFull(this);

            nameField = new TextField(beanItem.getItemProperty(REPORTNAME_PROPERTY));
            headerRow.addComponent(nameField);
            headerRow.setComponentAlignment(nameField, Alignment.MIDDLE_LEFT);

            headerRow.addComponent(new Label());

            HorizontalLayout uploadCell = ComponentFactory.createHLayout(headerRow);

            ComponentFactory.createLabel(beanItem, FILENAME_PROPERTY, FILE_NAME_STYLE, uploadCell);

            ReportReceiver uploadReceiver = new ReportReceiver(temporaryData);
            uploadReceiver.addListener(new ReportReceivedListener() {

                @Override
                public void reportReceived(ReportTemplate reportTemplate) {
                    requestRepaintAll();
                }
            });

            Upload changeReportupload = new Upload(null, uploadReceiver);
            changeReportupload.setWidth(null);
            changeReportupload.addListener((Upload.SucceededListener) uploadReceiver);
            changeReportupload.addListener((Upload.FailedListener) uploadReceiver);
            changeReportupload.setImmediate(true);
            changeReportupload.setButtonCaption(VaadinUtil.getValue(REPORT_MANAGER_ITEM_UPLOAD_CHANGE));
            uploadCell.addComponent(changeReportupload);
            headerRow.addComponent(uploadCell);
            headerRow.setComponentAlignment(uploadCell, Alignment.MIDDLE_RIGHT);

            ComponentFactory.createTextArea(beanItem, DESCRIPTION_PROPERTY, REPORT_MANAGER_ITEM_EDIT_DESC_PROMPT, this);

            HorizontalLayout footerRow = ComponentFactory.createHLayoutFull(this);
            HorizontalLayout checkboxCell = ComponentFactory.createHLayout(footerRow);
            ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_ACTIVE, beanItem, ACTIVE_PROPERTY, checkboxCell);
            ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_ONLINE, beanItem, ALLOW_ONLINE_DISPLAY_PROPERTY,
                    checkboxCell);
            ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_BACKGROUND, beanItem,
                    ALLOW_BACKGROUND_PROCESSING_PROPERTY, checkboxCell);

            footerRow.addComponent(new Label());

            HorizontalLayout buttonsCell = ComponentFactory.createHLayout(footerRow);

            Button save = ComponentFactory.createButton(BUTTON_OK, null, buttonsCell);
            save.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    saveChanges();

                }
            });
            Button cancel = ComponentFactory.createButton(BUTTON_CANCEL, null, buttonsCell);
            cancel.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    discardChanges();

                }
            });

            footerRow.setComponentAlignment(buttonsCell, Alignment.MIDDLE_RIGHT);

        }

        protected void discardChanges() {
            if (item.reportTemplate.getId() == null) {
                list.removeComponent(this);
            } else {
                list.replaceComponent(this, item);
            }
        }

        protected void saveChanges() {
            if (!checkUnique(temporaryData)) {
                return;
            }
            item.requestRepaintAll();
            deepCopy(temporaryData, item.reportTemplate);
            ReportTemplateDAO.saveOrUpdate(item.reportTemplate);
            list.replaceComponent(this, this.item);

        }

        private boolean checkUnique(ReportTemplate reportTemplate) {
            if (reportTemplate.getId() != null) {
                //todots check behaviour when editing name of the existing report
                return true;
            }
            List<ReportTemplate> exists = ReportTemplateDAO.fetchByName(reportTemplate.getReportname());
            if (exists.size() > 0) {
                nameField.focus();
                getWindow().showNotification(VaadinUtil.getValue(DUPLICATE_REPORT_NAME_TITLE),
                        "<br/>" + VaadinUtil.getValue(DUPLICATE_REPORT_NAME_DESC), Window.Notification.TYPE_ERROR_MESSAGE);
                return false;
            }
            return true;
        }
    }

    /**
     * List item in normal state.
     *
     * @author Zbigniew Malinowski
     *
     */
    private class ReportItemPanel extends Panel {

        private ReportTemplate reportTemplate;
        private BeanItem<ReportTemplate> beanItem;
        private ReportParamPanel paramsPanel = null;
        private Button toggleParams;

        public ReportItemPanel(ReportTemplate reportTemplate) {
            this.reportTemplate = reportTemplate;
            beanItem = new BeanItem<ReportTemplate>(this.reportTemplate);
            setStyleName(PANEL_STYLE);
            HorizontalLayout headerRow = new HorizontalLayout();
            headerRow.setWidth("100%");
            addComponent(headerRow);

            Label reportNameLabel = ComponentFactory.createLabel(beanItem, REPORTNAME_PROPERTY, REPORT_NAME_STYLE,
                    headerRow);

            Label spacer = new Label();
            headerRow.addComponent(spacer);

            Label changedDateLabel = ComponentFactory.createDateLabel(beanItem, CREATED_PROPERTY, CHANGED_DATE_STYLE,
                    headerRow);

            headerRow.setComponentAlignment(reportNameLabel, Alignment.MIDDLE_LEFT);
            headerRow.setComponentAlignment(changedDateLabel, Alignment.MIDDLE_RIGHT);

            HorizontalLayout uploadRow = new HorizontalLayout();
            addComponent(uploadRow);

            Label desc = ComponentFactory.createLabel(beanItem, DESCRIPTION_PROPERTY, DESC_STYLE, this);
            desc.setWidth("100%");

            HorizontalLayout footerRow = new HorizontalLayout();
            addComponent(footerRow);
            footerRow.setSpacing(true);
            toggleParams = ComponentFactory.createButton(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE, BaseTheme.BUTTON_LINK,
                    footerRow);
            toggleParams.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();
                }
            });
            Button reportSettingsButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_EDIT,
                    BaseTheme.BUTTON_LINK, footerRow);
            reportSettingsButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    editReportData(ReportItemPanel.this);
                }
            });

            Button downloadButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_DOWNLOAD, BaseTheme.BUTTON_LINK,
                    footerRow);
            downloadButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    download();
                }
            });

            Button removeButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_REMOVE, BaseTheme.BUTTON_LINK,
                    footerRow);
            removeButton.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    removeMe();
                }
            });
        }

        protected void removeMe() {
            remove(this);

        }

        private void toggleParams() {
            if (paramsPanel == null) {
                addComponent(paramsPanel = createParamsPanel());
                toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE));
            } else {
                removeComponent(paramsPanel);
                paramsPanel = null;
                toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE));
            }
        }

        // TODO: could be better
        private ReportParamPanel createParamsPanel() {
            final ReportParamPanel panel = new ReportParamPanel(reportTemplate, true);
            HorizontalLayout hl = ComponentFactory.createHLayout(panel);
            ComponentFactory.createButton("params-form.generate", BaseTheme.BUTTON_LINK, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!panel.validateForm()) {
                        return;
                    }
                    try {
                        ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(), new ReportTemplateProvider());
                        byte[] reportData = rm.generateAndExportReport(panel.getOuptutFormat(),
                                new HashMap<String, Object>(panel.collectParametersValues()),
                                org.apertereports.dao.utils.ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), reportTemplate.getReportname(), reportData,
                                panel.getOuptutFormat());
                    } catch (AperteReportsException e) {
                        throw new AperteReportsRuntimeException(e);
                    }

                }
            });

            Button backgroundGenerate = ComponentFactory.createButton("params-form.background-generate",
                    BaseTheme.BUTTON_LINK, hl);
            final CheckBox sendEmailCheckbox = new CheckBox(VaadinUtil.getValue(PARAMS_FORM_SEND_EMAIL));
            hl.addComponent(sendEmailCheckbox);
            backgroundGenerate.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    Map<String, String> parameters = panel.collectParametersValues();
                    String email = UserUtil.getUserEmail();
                    if ((Boolean) sendEmailCheckbox.getValue() != Boolean.TRUE) {
                        email = null;
                    }
                    ReportOrder reportOrder = ReportOrderPusher.buildNewOrder(reportTemplate, parameters,
                            panel.getOuptutFormat(), email, UserUtil.getUsername(), null);
                    Long id = reportOrder.getId();
                    if (id != null) {
                        ReportOrderPusher.addToJMS(id);
                    }
                }
            });
            if (!backgorundGenerationAvail()) {
                backgroundGenerate.setEnabled(false);
                sendEmailCheckbox.setEnabled(false);
            }

            panel.addComponent(hl);
            return panel;
        }

        private boolean backgorundGenerationAvail() {
            return AperteReportsJmsFacade.isJmsAvailable() && reportTemplate.getAllowBackgroundOrder() == Boolean.TRUE
                    && reportTemplate.getActive();
        }

        private void download() {
            byte[] reportContent = Base64.decodeBase64(reportTemplate.getContent().getBytes());
            FileStreamer.openFileInCurrentWindow(getApplication(), reportTemplate.getFilename(), reportContent,
                    "application/octet-stream");
        }
    }

    protected void editReportData(ReportItemPanel reportItemPanel) {
        EditReportItemPanel edit = new EditReportItemPanel(reportItemPanel);
        list.replaceComponent(reportItemPanel, edit);

    }

    private void removeReportAndDependants(ReportItemPanel panel, ReportTemplate reportTemplate, Collection<CyclicReportOrder> cyclic,
            Collection<ReportOrder> orders) {

        if (cyclic != null && !cyclic.isEmpty()) {
            CyclicReportOrderDAO.remove(cyclic);
        }
        if (orders != null && !orders.isEmpty()) {
            ReportOrderDAO.remove(orders);
        }
        ReportTemplateDAO.remove(reportTemplate);
        list.removeComponent(panel);
    }

    private void remove(final ReportItemPanel reportItemPanel) {
        final ReportTemplate rt = reportItemPanel.reportTemplate;
        final Collection<CyclicReportOrder> cyclic = CyclicReportOrderDAO.fetchByTemplateId(rt.getId());
        final Collection<ReportOrder> orders = ReportOrderDAO.fetchByTemplateId(rt.getId());
        if (!cyclic.isEmpty()) {
            NotificationUtil.showConfirmWindow(getWindow(), VaadinUtil.getValue(MSG_REMOVING_REPORT, rt.getReportname()),
                    VaadinUtil.getValue(MSG_REPORT_IS_USED)
                    + "</br>" + VaadinUtil.getValue(MSG_DO_YOU_WANT_TO_CONTINUE), new NotificationUtil.ConfirmListener() {

                @Override
                public void onConfirm() {
                    removeReportAndDependants(reportItemPanel, rt, cyclic, orders);
                }

                @Override
                public void onCancel() {
                }
            });
        } else {
            removeReportAndDependants(reportItemPanel, rt, cyclic, orders);
        }
    }

    /**
     * Class handling file upload.
     *
     * @author Zbigniew Malinowski
     *
     */
    private class ReportReceiver implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

        private ByteArrayOutputStream baos;
        private ReportTemplate reportTemplate;
        private List<ReportReceivedListener> listeners = new LinkedList<ReportManagerComponent.ReportReceivedListener>();

        public ReportReceiver(ReportTemplate reportTemplate) {
            this.reportTemplate = reportTemplate;

        }

        @Override
        public void uploadFailed(FailedEvent event) {
            NotificationUtil.showExceptionNotification(getWindow(),
                    new AperteReportsRuntimeException(event.getReason()));
        }

        @Override
        public void uploadSucceeded(SucceededEvent event) {
            String content = new String(Base64.encodeBase64(baos.toByteArray()));
            try {
                ReportMaster rm = new ReportMaster(content, new ReportTemplateProvider());
                reportTemplate.setReportname(rm.getReportName());
            } catch (AperteReportsException e) {
                NotificationUtil.showExceptionNotification(getWindow(), new AperteReportsRuntimeException(e));
                return;
            }

            reportTemplate.setContent(content);
            reportTemplate.setFilename(event.getFilename());
            if (StringUtils.isEmpty(reportTemplate.getDescription())) {
                reportTemplate.setDescription("");
            }

            for (ReportReceivedListener l : listeners) {
                l.reportReceived(reportTemplate);
            }

        }

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            return baos = new ByteArrayOutputStream();
        }

        public void addListener(ReportReceivedListener listener) {
            listeners.add(listener);
        }
    }

    private interface ReportReceivedListener {

        public void reportReceived(ReportTemplate reportTemplate);
    }

    private static void deepCopy(ReportTemplate source, ReportTemplate target) {
        target.setActive(source.getActive());
        target.setAllowBackgroundOrder(source.getAllowBackgroundOrder());
        target.setAllowOnlineDisplay(source.getAllowOnlineDisplay());
        target.setContent(source.getContent());
        target.setCreated(source.getCreated());
        target.setDescription(source.getDescription());
        target.setFilename(source.getFilename());
        target.setId(source.getId());
        target.setReportname(source.getReportname());
    }
}
