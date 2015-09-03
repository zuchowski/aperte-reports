package org.apertereports.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apertereports.backbone.jms.ARJmsFacade;
import org.apertereports.backbone.util.ReportOrderBuilder;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.VaadinUtil;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.common.users.User;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;

/**
 * Displays a component with a list of available report templates and lets
 * manually generate a report with temporal parameters.
 */
@SuppressWarnings("serial")
public class AperteInvokerComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String COMPONENT_STYLE_NAME = "borderless light";
    private CssPaginatedPanelList<ReportTemplate, ReportPanel> reportList;
    private User user;

    public AperteInvokerComponent() {
    	super(new CssLayout());
        init();
    }

    public void initData(User user) {
        this.user = user;
        reportList.filter(null);
    }

    /**
     * List item component.
     *
     * @author Zbigniew Malinowski
     *
     */
    private class ReportPanel extends Panel {

        private static final String REPORT_NAME_STYLE = "h4";
        private static final String REPORT_DESCR_STYLE = "tiny";
        private static final String PANEL_STYLE_NAME = "borderless light";
        private ReportParamPanel paramsPanel = null;
        private Button toggleParams;
        private ReportTemplate reportTemplate;
        
        private LiferayAccordion accordion;

        public ReportPanel(final ReportTemplate report) {
        	super(new CssLayout());
            this.reportTemplate = report;
            addStyleName(PANEL_STYLE_NAME);  
            addStyleName("invoker"); 
            //((AbstractLayout) getContent()).setMargin(false, false, false, false);
            //UiFactory.createSpacer(this, null, "6px");
            CssLayout header = new CssLayout();         
            UiFactory.createLabel(report.getReportname(), header);
            toggleParams = UiFactory.createButton(UiIds.LABEL_PARAMETERS, header, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();

                }
            });
            accordion = UiFactory.createAccordion(this, header);
            this.setSizeUndefined();
        }

        private void toggleParams() {
            if (paramsPanel == null) {
                paramsPanel = createParamsPanel();
                accordion.addContent(paramsPanel);
                toggleParams.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PARAMETERS));
            } else {
                accordion.removeContent();
                paramsPanel = null;
                toggleParams.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            }
        }

//		xxx: could be better
        private ReportParamPanel createParamsPanel() {
        	final ReportParamPanel panel = new ReportParamPanel(reportTemplate, true);
            panel.setStyleName("borderless");
            //HorizontalLayout hl = UiFactory.createHLayout(panel, FAction.SET_SPACING);
            CssLayout hl = new CssLayout();
            panel.addComponent(hl);
            Button reportParamBtn = UiFactory.createButton(UiIds.LABEL_GENERATE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        if (!panel.validateForm()) {
                            return;
                        }
                
                        ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(), new ReportTemplateProvider(), user);
                       
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
            reportParamBtn.addStyleName("btn");
            Button backgroundGenerate = UiFactory.createButton(UiIds.AR_MSG_GENERATE_IN_BACKGROUND,
                    hl, BaseTheme.BUTTON_LINK);
            backgroundGenerate.addStyleName("btn");
            final CheckBox sendEmailCheckbox = UiFactory.createCheckBox(UiIds.AR_MSG_SEND_EMAIL, hl);
            backgroundGenerate.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!panel.validateForm()) {
                        return;
                    }
                    Map<String, String> parameters = panel.collectParametersValues();
                    String email = user.getEmail();
                    if (!Boolean.TRUE.equals(sendEmailCheckbox.getValue())) {
                        email = null;
                    }
                    ReportOrder reportOrder = ReportOrderBuilder.build(reportTemplate, parameters,
                            panel.getOuptutFormat(), email, user.getLogin(), false);
                    try {
                        ARJmsFacade.sendToGenerateReport(reportOrder);
                    } catch (ARException ex) {
                        throw new ARRuntimeException(ex);
                    }
                }
            });
            if (!backgorundGenerationAvail()) {
                backgroundGenerate.setEnabled(false);
                sendEmailCheckbox.setEnabled(false);
            }

            //UiFactory.createSpacer(hl, FAction.SET_EXPAND_RATIO_1_0);
            /*Button reportParamBtn2  =UiFactory.createButton(UiIds.LABEL_CLOSE, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();
                }
            }, FAction.ALIGN_RIGTH);
            reportParamBtn2.addStyleName("btn");
            */
            return panel;
        }

        private boolean backgorundGenerationAvail() {

            return ARJmsFacade.isJmsAvailable() && Boolean.TRUE.equals(reportTemplate.getAllowBackgroundOrder())
                    && reportTemplate.getActive();
        }
    }

    /**
     * Build the main layout.
     */
    private void init() {
        setScrollable(true);
        addStyleName(COMPONENT_STYLE_NAME);
        addStyleName("accordion");
        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, this, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                reportList.filter(event.getText());

            }
        });
        filterField.setWidth("150px");

        reportList = new CssPaginatedPanelList<ReportTemplate, AperteInvokerComponent.ReportPanel>(PAGE_SIZE) {

            @Override
            protected ReportPanel transform(ReportTemplate object) {
                return new ReportPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportTemplateDAO.countActive(user, filter);
            }

            @Override
            protected Collection<ReportTemplate> fetch(String filter, int firstResult, int maxResults) {
                return ReportTemplateDAO.fetchActive(user, filter, firstResult, maxResults);
            }
        };

        addComponent(reportList);
        
        reportList.filter(null);
    }
}
