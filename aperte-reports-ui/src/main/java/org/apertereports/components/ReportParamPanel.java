package org.apertereports.components;

import com.vaadin.ui.*;
import com.vaadin.ui.Window.Notification;
import java.util.Map;

import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportTemplate;

import java.util.List;
import org.apertereports.common.xml.config.ReportConfigParameter;
import org.apertereports.ui.UiFactory;
import org.apertereports.util.VaadinUtil;

@SuppressWarnings("serial")
public class ReportParamPanel extends Panel {

    private ReportParametersComponent reportParametersComponent;
    private HorizontalLayout buttons;

    public ReportParamPanel(ReportTemplate reportTemplate, boolean showFormat) {
        this(reportTemplate, showFormat, null);
    }

    public ReportParamPanel(ReportTemplate reportTemplate, boolean showFormat, List<ReportConfigParameter> params) {
        try {
            ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(),
                    new ReportTemplateProvider());
            this.reportParametersComponent = new ReportParametersComponent(rm, showFormat, params);
            VerticalLayout vl = UiFactory.createVLayout(this);
            vl.addComponent(reportParametersComponent);
            buttons = UiFactory.createHLayout(vl);
        } catch (AperteReportsException e) {
            throw new AperteReportsRuntimeException(e);
        }
    }

    /**
     * Empty panel
     */
    public ReportParamPanel() {
    }

    public void addButton(Button button) {
        buttons.addComponent(button);
    }

    public void addButtons(HorizontalLayout buttons) {
        replaceComponent(this.buttons, buttons);
        this.buttons = buttons;
    }

    public Map<String, String> collectParametersValues() {
        return reportParametersComponent.collectParametersValues();
    }

    public String getOuptutFormat() {
        return reportParametersComponent.getSelectedFormat();
    }

    public boolean validateForm() {
        boolean result = reportParametersComponent.validateForm();
        if (!result) {
            getWindow().showNotification(VaadinUtil.getValue("notification.fill.in.the.form.correctly"), Notification.TYPE_WARNING_MESSAGE);
            Notification n;
        }
        return result;
    }
}
