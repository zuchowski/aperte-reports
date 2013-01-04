package org.apertereports.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.backbone.util.ReportOrderPusher;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.VaadinUtil;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
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
    private PaginatedPanelList<ReportTemplate, ReportPanel> reportList;
    private User user;

    public AperteInvokerComponent() {

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

        public ReportPanel(final ReportTemplate report) {
            this.reportTemplate = report;
            setStyleName(PANEL_STYLE_NAME);
            ((AbstractLayout) getContent()).setMargin(true, false, false, false);
            HorizontalLayout row = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH, FAction.SET_SPACING);
            Label nameLabel = UiFactory.createLabel(report.getReportname(), row, REPORT_NAME_STYLE, FAction.ALIGN_RIGTH);

            Label spacerLabel = UiFactory.createSpacer(row);
            toggleParams = UiFactory.createButton(UiIds.LABEL_PARAMETERS, row, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();

                }
            });
            UiFactory.createLabel(report.getDescription(), this, REPORT_DESCR_STYLE, FAction.SET_FULL_WIDTH);

            row.setExpandRatio(spacerLabel, 1.0f);
            setWidth("100%");
        }

        private void toggleParams() {
            if (paramsPanel == null) {
                addComponent(paramsPanel = createParamsPanel());
                toggleParams.setCaption(VaadinUtil.getValue(UiIds.AR_MSG_HIDE_PARAMETERS));
            } else {
                removeComponent(paramsPanel);
                paramsPanel = null;
                toggleParams.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            }
        }

//		TODO: could be better
        private ReportParamPanel createParamsPanel() {
            final ReportParamPanel panel = new ReportParamPanel(reportTemplate, true);
            panel.setCaption(VaadinUtil.getValue(UiIds.LABEL_PARAMETERS));
            HorizontalLayout hl = UiFactory.createHLayout(panel, FAction.SET_SPACING);
            UiFactory.createButton(UiIds.LABEL_GENERATE, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    try {
                        if (!panel.validateForm()) {
                            return;
                        }
                        ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(), new ReportTemplateProvider());
                        byte[] reportData = rm.generateAndExportReport(panel.getOuptutFormat(),
                                new HashMap<String, Object>(panel.collectParametersValues()),
                                ConfigurationCache.getConfiguration());
                        FileStreamer.showFile(getApplication(), reportTemplate.getReportname(), reportData,
                                panel.getOuptutFormat());
                    } catch (AperteReportsException e) {
                        throw new AperteReportsRuntimeException(e);

                    }

                }
            });

            Button backgroundGenerate = UiFactory.createButton(UiIds.AR_MSG_GENERATE_IN_BACKGROUND,
                    hl, BaseTheme.BUTTON_LINK);
            final CheckBox sendEmailCheckbox = UiFactory.createCheckBox(UiIds.AR_MSG_SEND_EMAIL, hl);
            backgroundGenerate.addListener(new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    if (!panel.validateForm()) {
                        return;
                    }
                    Map<String, String> parameters = panel.collectParametersValues();
                    String email = user.getEmail();
                    if ((Boolean) sendEmailCheckbox.getValue() != Boolean.TRUE) {
                        email = null;
                    }
                    ReportOrder reportOrder = ReportOrderPusher.buildNewOrder(reportTemplate, parameters,
                            panel.getOuptutFormat(), email, user.getLogin(), null);
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

            return panel;
        }

        private boolean backgorundGenerationAvail() {

            return AperteReportsJmsFacade.isJmsAvailable() && reportTemplate.getAllowBackgroundOrder() == Boolean.TRUE
                    && reportTemplate.getActive();
        }
    }

    /**
     * Build the main layout.
     */
    private void init() {
        setScrollable(true);
        setStyleName(COMPONENT_STYLE_NAME);

        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, this, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                reportList.filter(event.getText());

            }
        });
        filterField.setWidth("150px");

        reportList = new PaginatedPanelList<ReportTemplate, AperteInvokerComponent.ReportPanel>(PAGE_SIZE) {

            @Override
            protected ReportPanel transform(ReportTemplate object) {
                return new ReportPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportTemplateDAO.countActiveMatching(user, filter);
            }

            @Override
            protected Collection<ReportTemplate> fetch(String filter, int firstResult, int maxResults) {
                return ReportTemplateDAO.fetchActive(user, filter, firstResult, maxResults);
            }
        };

        addComponent(reportList);
    }
}
