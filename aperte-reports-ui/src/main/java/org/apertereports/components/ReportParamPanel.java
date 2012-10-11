package org.apertereports.components;

import java.util.Map;

import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;

import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.util.List;
import org.apertereports.common.xml.config.ReportConfigParameter;

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
            VerticalLayout vl = new VerticalLayout();
            vl.addComponent(reportParametersComponent);
            buttons = ComponentFactory.createHLayout(vl);
            addComponent(vl);
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
}
