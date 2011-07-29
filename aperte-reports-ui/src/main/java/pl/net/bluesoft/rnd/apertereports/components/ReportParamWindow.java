package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.AbstractReportingApplication;
import pl.net.bluesoft.rnd.apertereports.backbone.jms.ReportOrderPusher;
import pl.net.bluesoft.rnd.apertereports.common.exception.ReportException;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesException;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.dashboard.html.ReportStreamReceiver;
import pl.net.bluesoft.rnd.apertereports.domain.ConfigurationCache;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.apertereports.util.FileStreamer;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil;
import pl.net.bluesoft.rnd.apertereports.util.VaadinUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This window contains a configuration of a report generation. It contains report parameters,
 * selectable output formats etc.
 */
public class ReportParamWindow extends Window {
    private ReportMaster rm;
    private ReportTemplate report;

    private ReportParametersComponent reportParametersComponent;

    private ReportStreamReceiver receiver = null;
    private Button submitBackgroundGenerate = new Button(VaadinUtil.getValue("invoker.form.generate_in_background"));
    private CheckBox sendEmailCheckbox = new CheckBox(VaadinUtil.getValue("invoker.form.send_email"));
    private AbstractReportingApplication app;

    public ReportParamWindow(ReportTemplate report, String caption, ReportStreamReceiver receiver) {
        super(caption);
        this.report = report;
        this.receiver = receiver;
        setModal(true);
        setWidth("50%");
        initDialog();
    }

    /**
     * Collects parameter values from the form and attempts to generate the report.
     *
     * @return Bytes of generated report
     */
    private byte[] getReportAsBytes() {
        Map<String, String> parameters = reportParametersComponent.collectParametersValues();
        try {
            return rm.generateAndExportReport(new HashMap<String, Object>(parameters), reportParametersComponent.getSelectedFormat(),
                    ConfigurationCache.getConfiguration());
        }
        catch (ReportException e) {
            ExceptionUtils.logSevereException(e);
            throw new VriesRuntimeException("Error while generating report", e);
        }
    }

    /**
     * Displays a report download popup.
     */
    private void sendForm() {
        Map<String, String> parameters = reportParametersComponent.collectParametersValues();
        try {
            byte[] reportData = rm.generateAndExportReport(new HashMap<String, Object>(parameters), reportParametersComponent.getSelectedFormat(),
                    ConfigurationCache.getConfiguration());
            FileStreamer.showFile(getApplication(), this.report.getReportname(), reportData,
                    reportParametersComponent.getSelectedFormat());
        }
        catch (ReportException e) {
            ExceptionUtils.logSevereException(e);
            throw new VriesRuntimeException("Error while generating report", e);
        }
    }

    /**
     * Posts a request to JMS to generate the report in the background.
     *
     * @param sendEmail <code>TRUE</code> to send the result by email
     * @see ReportOrderPusher
     */
    private void sendFormToJMS(Boolean sendEmail) {
        Map<String, String> parameters = reportParametersComponent.collectParametersValues();
        String email = null;
        if (sendEmail) {
            email = app.getLiferayUser().getEmailAddress();
        }
        String username = app.getLiferayUser().getFullName();
        ReportOrder reportOrder = ReportOrderPusher.buildNewOrder(report, parameters,
                reportParametersComponent.getSelectedFormat(), email, username, null);
        Long id = reportOrder.getId();
        if (id != null) {
            ReportOrderPusher.addToJMS(id);
        }
    }

    /**
     * Renders the report parameters. The dialog may contain two buttons: "generate right now" and "generate in the background".
     * The former simply creates a report based on current settings and lets user download it.
     * The latter, on the other hand, posts a request to JMS queue for background processing.
     */
    protected void initDialog() {
        VerticalLayout vl = new VerticalLayout();
        try {
            rm = new ReportMaster(new String(report.getContent()), report.getId().toString());
            reportParametersComponent = new ReportParametersComponent(rm);

            HorizontalLayout buttons = new HorizontalLayout();
            if (Boolean.TRUE.equals(report.getAllowOnlineDisplay())) {
                final Button submitGenerate = new Button(VaadinUtil.getValue("invoker.form.generate"));
                submitGenerate.addListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (reportParametersComponent.validateForm()) {
                            sendForm();
                        }
                    }
                });
                buttons.addComponent(submitGenerate);
            }

            if (Boolean.TRUE.equals(report.getAllowBackgroundOrder())) {
                submitBackgroundGenerate.addListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (reportParametersComponent.validateForm()) {
                            sendFormToJMS((Boolean) sendEmailCheckbox.getValue());
                            close();
                            showNotification(VaadinUtil.getValue("invoker.form.generate_in_background.succeeded"));
                        }
                    }
                });
                buttons.addComponent(submitBackgroundGenerate);
                buttons.addComponent(sendEmailCheckbox);
            }

            if (receiver != null) {
                final Button submitGenerate = new Button(VaadinUtil.getValue("invoker.form.generate_stream"));
                submitGenerate.addListener(new ClickListener() {
                    @Override
                    public void buttonClick(ClickEvent event) {
                        if (reportParametersComponent.validateForm()) {
                            receiver.receiveStream(report, getReportAsBytes());
                            close();
                        }
                    }
                });
                buttons.addComponent(submitGenerate);
            }

            vl.addComponent(reportParametersComponent);
            vl.addComponent(buttons);
            addComponent(vl);
        }
        catch (Exception e) {
            NotificationUtil.showExceptionNotification(getWindow(), new VriesException(e));
            ExceptionUtils.logSevereException(e);
        }
    }

    /**
     * Attaches the component to some container. The main purpose to override this method is
     * to access the currently logged user and fetch his email address.
     */
    @Override
    public void attach() {
        super.attach();
        if (Boolean.TRUE.equals(report.getAllowBackgroundOrder())) {
            app = (AbstractReportingApplication) getApplication();
            if (app != null && app.getContext() instanceof PortletApplicationContext2) {
                if (app.getLiferayUser() == null) {
                    submitBackgroundGenerate.setEnabled(false);
                    sendEmailCheckbox.setEnabled(false);
                    sendEmailCheckbox.setCaption(VaadinUtil.getValue("invoker.form.send_email.with_error.no_user"));
                }
                else if (StringUtils.isEmpty(app.getLiferayUser().getEmailAddress())) {
                    sendEmailCheckbox.setEnabled(false);
                    sendEmailCheckbox.setCaption(VaadinUtil.getValue("invoker.form.send_email.with_error.no_email"));
                }
                else {
                    sendEmailCheckbox.setEnabled(true);
                    sendEmailCheckbox.setCaption(VaadinUtil.getValue("invoker.form.send_email.with_email", app.getLiferayUser()
                            .getEmailAddress()));
                }
            }
            else {
                sendEmailCheckbox.setEnabled(false);
                submitBackgroundGenerate.setEnabled(false);
                sendEmailCheckbox.setCaption(VaadinUtil.getValue("invoker.form.send_email.with_error.no_context"));
            }
        }
    }

}
