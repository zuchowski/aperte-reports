package org.apertereports.components;

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
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

import javax.naming.NamingException;

import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.service.UserLocalServiceUtil;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.jms.ARJmsFacade;
import org.apertereports.backbone.util.ReportOrderBuilder;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.users.User;
import org.apertereports.dao.CyclicReportConfigDAO;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.CyclicReportConfig;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.ui.*;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component to manage reports.
 *
 * @author Zbigniew Malinowski
 *
 */
@SuppressWarnings("serial")
public class ReportManagerComponent extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(ReportManagerComponent.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String DESC_STYLE = "small";
    private static final String CHANGED_DATE_STYLE = "h3";
    private static final String REPORT_NAME_STYLE = "h4";
    private static final String FILE_NAME_STYLE = "h3";
    private static final String EDIT_PANEL_STYLE = "bubble";
    private static final String PANEL_STYLE = "borderless light";
    private static final String DESCRIPTION_PROPERTY = "description";
    private static final String FILENAME_PROPERTY = "filename";
    private static final String CREATED_PROPERTY = "created";
    private static final String REPORTNAME_PROPERTY = "reportname";
    private static final String ALLOW_BACKGROUND_PROCESSING_PROPERTY = "allowBackgroundProcessing";
    private static final String ALLOW_ONLINE_DISPLAY_PROPERTY = "allowOnlineDisplay";
    private static final String ACTIVE_PROPERTY = "active";
    private static final String REPORT_MANAGER_ITEM_UPLOAD_CHANGE = "report.manager.item.upload.change";
    private static final String REPORT_MANAGER_ITEM_EDIT_NAME_PROMPT = "report.manager.item.edit.name.prompt";
    private static final String REPORT_MANAGER_ITEM_EDIT_BACKGROUND = "report.manager.item.edit.background";
    private static final String REPORT_MANAGER_ITEM_EDIT_ONLINE = "report.manager.item.edit.online";
    private static final String MSG_REMOVING_REPORT = "report.manager.removing.report";
    private static final String MSG_REPORT_IS_USED = "report.manager.report.is.used";
    private static final String MSG_DO_YOU_WANT_TO_CONTINUE = "q.do.you.want.to.continue";
    private CssPaginatedPanelList<ReportTemplate, ReportItemPanel> list;
    private ReportReceiver newReportReceiver;
    private transient User user;
    

    public ReportManagerComponent() {
    	super(new CssLayout());
        init();
    }

    private void init() {
        CssLayout mainLayout = new CssLayout();
        mainLayout.addStyleName("a-config");
        this.addComponent(mainLayout);
        newReportReceiver = new ReportReceiver(new ReportTemplate());
        newReportReceiver.addListener(new ReportReceivedListener() {

            @Override
            public void reportReceived(ReportTemplate reportTemplate) {
                addNewReport(reportTemplate);
            }
        });
        Upload newReportUpload = new Upload(null, newReportReceiver);
        newReportUpload.addStyleName("btn");
        newReportUpload.addListener((SucceededListener) newReportReceiver);
        newReportUpload.addListener((FailedListener) newReportReceiver);
        newReportUpload.setButtonCaption(VaadinUtil.getValue(UiIds.LABEL_ADD));
        newReportUpload.setImmediate(true);

        HorizontalLayout hl = UiFactory.createHLayout(mainLayout, FAction.SET_FULL_WIDTH);
        list = new CssPaginatedPanelList<ReportTemplate, ReportManagerComponent.ReportItemPanel>(PAGE_SIZE) {

            @Override
            protected ReportItemPanel transform(ReportTemplate object) {
                return new ReportItemPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportTemplateDAO.count(user, filter);
            }

            @Override
            protected Collection<ReportTemplate> fetch(String filter, int firstResult, int maxResults) {
                return ReportTemplateDAO.fetch(user, filter, firstResult, maxResults);
            }
        };

        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, hl, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        });
        filterField.setWidth("150px");

        hl.addComponent(newReportUpload);
        hl.setComponentAlignment(newReportUpload, Alignment.MIDDLE_RIGHT);
        mainLayout.addComponent(list);
        setWidth("95%");
        setStyleName(PANEL_STYLE);
    }

    private void addNewReport(ReportTemplate reportTemplate) {
        ReportItemPanel reportItem = new ReportItemPanel(reportTemplate);
        list.addComponent(reportItem, 0);
        addOrEditReport(reportItem, true);
        newReportReceiver.reportTemplate = new ReportTemplate();
    }

    public void initData(User user) {
        this.user = user;
        list.filter(null);
    }

    /**
     * List item in adding or edition state.
     *
     * @author Zbigniew Malinowski
     *
     */
    private class AddOrEditReportItemPanel extends Panel {

        private ReportItemPanel itemPanel;
        private ReportTemplate tmpReportTemplate;
        private BeanItem<ReportTemplate> beanItem;
        private TextField nameField;
        private ErrorLabelHandler errorHandler;
        private final boolean adding;

        public AddOrEditReportItemPanel(ReportItemPanel itemPanel, boolean adding) {
        	super(new CssLayout());
            this.itemPanel = itemPanel;
            this.adding = adding;
            addStyleName(EDIT_PANEL_STYLE);
            
            CssLayout header = new CssLayout();
            UiFactory.createLabel(VaadinUtil.getValue(adding ? UiIds.LABEL_ADDING : UiIds.LABEL_EDITION) + " - " + itemPanel.reportTemplate.getReportname(), header);
            //setCaption(VaadinUtil.getValue(adding ? UiIds.LABEL_ADDING : UiIds.LABEL_EDITION) + " - " + itemPanel.reportTemplate.getReportname());
            tmpReportTemplate = new ReportTemplate();
            deepCopy(itemPanel.reportTemplate, tmpReportTemplate);
            beanItem = new BeanItem<ReportTemplate>(tmpReportTemplate);
            
            
            
            CssLayout subSubContent = new CssLayout();
            
            UiFactory.createAccordion(this, header, subSubContent);
                    
            HorizontalLayout headerRow = UiFactory.createHLayout(subSubContent,FAction.SET_FULL_WIDTH);
            
            nameField = UiFactory.createTextField(beanItem, REPORTNAME_PROPERTY, headerRow,
                    REPORT_MANAGER_ITEM_EDIT_NAME_PROMPT, FAction.ALIGN_LEFT);

            UiFactory.createSpacer(headerRow);

            HorizontalLayout uploadCell = UiFactory.createHLayout(headerRow, FAction.SET_SPACING, FAction.ALIGN_RIGTH);
            UiFactory.createLabel(beanItem, FILENAME_PROPERTY, uploadCell,
                    FILE_NAME_STYLE, FAction.ALIGN_LEFT);

            ReportReceiver uploadReceiver = new ReportReceiver(tmpReportTemplate);
            uploadReceiver.addListener(new ReportReceivedListener() {

                @Override
                public void reportReceived(ReportTemplate reportTemplate) {
                    requestRepaintAll();
                }
            });

            Upload changeReportupload = new Upload(null, uploadReceiver);
            changeReportupload.addStyleName("btn");
            changeReportupload.setWidth(null);
            changeReportupload.addListener((Upload.SucceededListener) uploadReceiver);
            changeReportupload.addListener((Upload.FailedListener) uploadReceiver);
            changeReportupload.setImmediate(true);
            changeReportupload.setButtonCaption(VaadinUtil.getValue(REPORT_MANAGER_ITEM_UPLOAD_CHANGE));
            uploadCell.addComponent(changeReportupload);

            UiFactory.createTextField(beanItem, DESCRIPTION_PROPERTY, subSubContent, UiIds.AR_MANAGER_REPORT_DESCRIPTION, FAction.SET_FULL_WIDTH);

            //UiFactory.createSpacer(this, null, "5px");
            Label errorLabel = UiFactory.createLabel("", subSubContent);
            //UiFactory.createSpacer(this, null, "5px");

            errorHandler = new ErrorLabelHandler(errorLabel);

            //HorizontalLayout footerRow = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);
            CssLayout footerRow = new CssLayout() ;
            subSubContent.addComponent(footerRow);
            
            //HorizontalLayout checkboxCell = UiFactory.createHLayout(footerRow, FAction.SET_SPACING);
            CssLayout checkboxCell = new CssLayout() ;
            footerRow.addComponent(checkboxCell);
            UiFactory.createCheckBox(UiIds.LABEL_ACTIVE, beanItem, ACTIVE_PROPERTY, checkboxCell);
            UiFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_ONLINE, beanItem, ALLOW_ONLINE_DISPLAY_PROPERTY,
                    checkboxCell);
            UiFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_BACKGROUND, beanItem,
                    ALLOW_BACKGROUND_PROCESSING_PROPERTY, checkboxCell);

            //HorizontalLayout buttonsCell = UiFactory.createHLayout(footerRow, FAction.SET_SPACING, FAction.ALIGN_RIGTH);
            CssLayout buttonsCell = new CssLayout() ;
            footerRow.addComponent(buttonsCell);
            Button tmpButton1 = UiFactory.createButton(UiIds.LABEL_OK, buttonsCell, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    saveChanges();
                }
            });
            tmpButton1.addStyleName("btn");
            Button tmpButton2 = UiFactory.createButton(UiIds.LABEL_CANCEL, buttonsCell, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    discardChanges();
                }
            });
            tmpButton2.addStyleName("btn");
            this.setSizeUndefined();
        }

        protected void discardChanges() {
            if (itemPanel.reportTemplate.getId() == null) {
                list.removeComponent(this);
            } else {
                list.replaceComponent(this, itemPanel);
            }
        }

        protected void saveChanges() {
            if (!checkUniqueName(itemPanel.reportTemplate.getReportname(), tmpReportTemplate)) {
                return;
            }
            itemPanel.requestRepaintAll();
            deepCopy(tmpReportTemplate, itemPanel.reportTemplate);
            
            try {
        	   	//set mandant id
               	com.liferay.portal.model.User liferayUser = UserLocalServiceUtil.getUser(PrincipalThreadLocal.getUserId());
       			long companyid = liferayUser.getCompanyId();           
       			itemPanel.reportTemplate.setCompanyId(String.valueOf(companyid));
			} catch (Exception e) {
				logger.error("Liferay User could not be found -> companyId could not be set", e);
			}                        
            
            ReportTemplateDAO.saveOrUpdate(itemPanel.reportTemplate);
            list.replaceComponent(this, this.itemPanel);
        }

        private boolean checkUniqueName(String originalName, ReportTemplate reportTemplate) {
            String newName = reportTemplate.getReportname();
            if (StringUtils.isEmpty(newName)) {
                errorHandler.setMessage(UiIds.AR_MANAGER_ERR_ENTER_REPORT_NAME);
                return false;
            }

            if (!adding && newName.equals(originalName)) {
                return true;
            }

            //xxx without user, we want to check all reports
            Collection<ReportTemplate> exists = ReportTemplateDAO.fetchByName(user, newName);
            if (exists.size() > 0) {
                nameField.focus();
                errorHandler.setMessage(UiIds.AR_MANAGER_ERR_NAME_ALREDY_EXISTS);
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
        private Panel paramsPanel = null;
        private RolePermissionsPanel permsPanel = null;
        private Button toggleParamsButton;
        private Button togglePermsButton;
        private CssLayout content;

        public ReportItemPanel(ReportTemplate reportTemplate) {
        	super(new CssLayout());
            this.reportTemplate = reportTemplate;
            beanItem = new BeanItem<ReportTemplate>(this.reportTemplate);
            addStyleName(PANEL_STYLE);
            
            CssLayout header = new CssLayout();
            
            //HorizontalLayout headerRow = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);

            UiFactory.createLabel(beanItem, REPORTNAME_PROPERTY, header);
            UiFactoryExt.createDateLabel(beanItem, CREATED_PROPERTY, "float-right",
            		header);
            //UiFactory.createSpacer(headerRow);
                      
            content = new CssLayout();
            
            UiFactory.createAccordion(this, header, content);
            
            UiFactory.createLabel(beanItem, DESCRIPTION_PROPERTY, content, DESC_STYLE, FAction.SET_FULL_WIDTH);
            
            CssLayout footerRow = new CssLayout() ;
            footerRow.addStyleName("button-row");
            content.addComponent(footerRow);
            
            //HorizontalLayout footerRow = UiFactory.createHLayout(this, FAction.SET_SPACING);
            toggleParamsButton = UiFactory.createButton(UiIds.LABEL_PARAMETERS, footerRow, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParamsPanel();
                }
            });
            toggleParamsButton.addStyleName("btn");
            togglePermsButton = UiFactory.createButton(UiIds.LABEL_PERMISSIONS, footerRow, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    togglePermsPanel();
                }
            });
            togglePermsButton.addStyleName("btn");
            Button tmpButton1 = UiFactory.createButton(UiIds.LABEL_EDIT, footerRow, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    addOrEditReport(ReportItemPanel.this, false);
                }
            });
            tmpButton1.addStyleName("btn");
            Button tmpButton2 = UiFactory.createButton(UiIds.LABEL_DOWNLOAD, footerRow, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    download();
                }
            });
            tmpButton2.addStyleName("btn");
            Button tmpButton3 =UiFactory.createButton(UiIds.LABEL_REMOVE, footerRow, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    removeMe();
                }
            });
            tmpButton3.addStyleName("btn");
            this.setSizeUndefined();
        }

        protected void removeMe() {
            remove(this);
        }

        private void toggleParamsPanel() {
            if (paramsPanel == null) {
                paramsPanel = createParamsPanel();
                content.addComponent(paramsPanel);
                toggleParamsButton.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PARAMETERS));
            } else {
            	content.removeComponent(paramsPanel);
                paramsPanel = null;
                toggleParamsButton.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            }
        }

        private void togglePermsPanel() {
            if (permsPanel == null) {
                permsPanel = new RolePermissionsPanel(reportTemplate);
                content.addComponent(permsPanel);
                togglePermsButton.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PERMISSIONS));

                permsPanel.setCloseListener(new CloseListener() {

                    @Override
                    public void close() {
                        togglePermsPanel();
                    }
                });
            } else {
            	content.removeComponent(permsPanel);
                permsPanel = null;
                togglePermsButton.setCaption(VaadinUtil.getValue(UiIds.LABEL_PERMISSIONS));
            }
        }

        // xxx: could be better
        private Panel createParamsPanel() {
        	 CssLayout mainLayout = new CssLayout();
        	 CssLayout paramContent = new CssLayout();
        	 UiFactory.createAccordion(mainLayout, VaadinUtil.getValue(UiIds.LABEL_PARAMETERS), paramContent);
             
           
            final ReportParamPanel panel = new ReportParamPanel(reportTemplate, true);
            //panel.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            CssLayout hl = new CssLayout();            
            panel.addComponent(hl);
            //HorizontalLayout hl = UiFactory.createHLayout(panel, FAction.SET_SPACING, FAction.SET_FULL_WIDTH);
            Button tmpButton =UiFactory.createButton(UiIds.LABEL_GENERATE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!panel.validateForm()) {
                        return;
                    }
                    try {
                        ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(), new ReportTemplateProvider(),user);
                        byte[] reportData = rm.generateAndExportReport(panel.getOuptutFormat(),
                                new HashMap<String, Object>(panel.collectParametersValues()),
                                ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), reportTemplate.getReportname(), reportData,
                                panel.getOuptutFormat());
                    } catch (ARException e) {
                        throw new ARRuntimeException(e);
                    }

                }
            });
            tmpButton.addStyleName("tmp");
            Button backgroundGenerate = UiFactory.createButton(UiIds.AR_MSG_GENERATE_IN_BACKGROUND, hl, BaseTheme.BUTTON_LINK);
            backgroundGenerate.addStyleName("btn");
            final CheckBox sendEmailCheckbox = UiFactory.createCheckBox(UiIds.AR_MSG_SEND_EMAIL, hl);
            backgroundGenerate.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    logger.info("Generate in background action...");
                    Map<String, String> parameters = panel.collectParametersValues();
                    String email = user.getEmail();
                    if (!Boolean.TRUE.equals((Boolean) sendEmailCheckbox.getValue())) {
                        email = null;
                    }
                    ReportOrder reportOrder = ReportOrderBuilder.build(reportTemplate, parameters,
                            panel.getOuptutFormat(), email, user.getLogin(), false);
                    try {
                        ARJmsFacade.sendToGenerateReport(reportOrder);
                    } catch (Exception ex) {
                        throw new ARRuntimeException(ex);
                    }
                }
            });
            if (!backgorundGenerationAvail()) {
                backgroundGenerate.setEnabled(false);
                sendEmailCheckbox.setEnabled(false);
            }
            //UiFactory.createSpacer(hl, FAction.SET_EXPAND_RATIO_1_0);
            /*Button tmpButton2 = UiFactory.createButton(UiIds.LABEL_CLOSE, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParamsPanel();
                }
            }, FAction.ALIGN_RIGTH);
            tmpButton2.addStyleName("btn");*/
            paramContent.addComponent(panel);
            Panel p = new Panel();
            p.setContent(mainLayout);
            return p;
        }

        private boolean backgorundGenerationAvail() {
        		return ARJmsFacade.isJmsAvailable() && Boolean.TRUE.equals(reportTemplate.getAllowBackgroundOrder())
                        && reportTemplate.getActive();
        }

        private void download() {
            byte[] reportContent = Base64.decodeBase64(reportTemplate.getContent().getBytes());
            FileStreamer.openFileInCurrentWindow(getApplication(), reportTemplate.getFilename(), reportContent,
                    "application/octet-stream");
        }
    }

    private void addOrEditReport(ReportItemPanel reportItemPanel, boolean adding) {
        AddOrEditReportItemPanel edit = new AddOrEditReportItemPanel(reportItemPanel, adding);
        list.replaceComponent(reportItemPanel, edit);
    }

    private void removeReportAndDependants(ReportItemPanel panel, ReportTemplate reportTemplate, Collection<CyclicReportConfig> cyclic,
            Collection<ReportOrder> orders) {

        if (cyclic != null && !cyclic.isEmpty()) {
            CyclicReportConfigDAO.remove(cyclic);
        }
        if (orders != null && !orders.isEmpty()) {
            ReportOrderDAO.remove(orders);
        }
        ReportTemplateDAO.remove(reportTemplate);
        list.removeComponent(panel);
    }

    private void remove(final ReportItemPanel reportItemPanel) {
        final ReportTemplate rt = reportItemPanel.reportTemplate;
        final Collection<CyclicReportConfig> cyclic = CyclicReportConfigDAO.fetchByReportId(user, rt.getId());
        final Collection<ReportOrder> orders = ReportOrderDAO.fetchByReportId(user, rt.getId());
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

        private transient ByteArrayOutputStream baos;
        private ReportTemplate reportTemplate;
        private List<ReportReceivedListener> listeners = new LinkedList<ReportManagerComponent.ReportReceivedListener>();

        public ReportReceiver(ReportTemplate reportTemplate) {
            this.reportTemplate = reportTemplate;
        }

        @Override
        public void uploadFailed(FailedEvent event) {
            NotificationUtil.showExceptionNotification(getWindow(),
                    new ARRuntimeException(event.getReason()));
        }

        @Override
        public void uploadSucceeded(SucceededEvent event) {
            if (baos == null) {
                throw new IllegalStateException("baos == null");
            }
            String content = new String(Base64.encodeBase64(baos.toByteArray()));
            try {
                ReportMaster rm = new ReportMaster(content, new ReportTemplateProvider());
                reportTemplate.setReportname(rm.getReportName());
            } catch (ARException e) {
                NotificationUtil.showExceptionNotification(getWindow(), new ARRuntimeException(e));
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
            baos = new ByteArrayOutputStream();
            return baos;
        }

        public void addListener(ReportReceivedListener listener) {
            listeners.add(listener);
        }
    }

    private interface ReportReceivedListener {

        void reportReceived(ReportTemplate reportTemplate);
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
        target.setRolesWithAccess(new HashSet(source.getRolesWithAccess()));
    }
}