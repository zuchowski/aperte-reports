package pl.net.bluesoft.rnd.apertereports.generators;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.util.DashboardUtil;
import pl.net.bluesoft.rnd.apertereports.util.FileStreamer;
import pl.net.bluesoft.rnd.apertereports.util.OnClickOpenMessageInNewWindow;
import pl.net.bluesoft.rnd.apertereports.util.VaadinUtil;

/**
 * Displays a cyclic report status based on a given report order instance.
 */
public class ReportStatusColumn extends CustomComponent {
    private ReportOrder reportOrder;
    private Table source;

    public ReportStatusColumn(Table source, ReportOrder reportOrder) {
        this.reportOrder = reportOrder;
        this.source = source;
        setCompositionRoot(createCompositionRoot());
    }

    /**
     * Renders a status description or a link containing error details.
     * On success the cell contains a number of available formats a generated report can be downloaded in.
     *
     * @return A status component
     */
    private Component createCompositionRoot() {
        if (reportOrder == null) {
            return new Label(VaadinUtil.getValue("report_order.table.status.new"));
        }
        else if (reportOrder.getReportStatus() == ReportOrder.Status.SUCCEEDED) {
            HorizontalLayout hl = new HorizontalLayout();
            hl.setSpacing(true);
            for (final ReportConstants.ReportType format : ReportConstants.ReportType.values()) {
                Button formatLink = new Button(format.toString());
                formatLink.setStyleName(BaseTheme.BUTTON_LINK);
                formatLink.addListener(new Button.ClickListener() {
                    @Override
                    public void buttonClick(Button.ClickEvent event) {
                        try {
                            FileStreamer.showFile(source.getApplication(), reportOrder.getReport().getReportname(),
                                    DashboardUtil.exportReportOrderData(reportOrder, format), format.toString());
                        }
                        catch (Exception e) {
                            ExceptionUtils.logSevereException(e);
                            throw new VriesRuntimeException("exception.gui.error", e);
                        }
                    }
                });
                hl.addComponent(formatLink);
            }
            return hl;
        }
        else if (reportOrder.getReportStatus() == ReportOrder.Status.PROCESSING) {
            return new Label(VaadinUtil.getValue("report_order.table.status.processing"));
        }
        else if (reportOrder.getReportStatus() == ReportOrder.Status.FAILED) {
            Button label = new Button(VaadinUtil.getValue("report_order.table.status.failed"));
            label.setStyleName(BaseTheme.BUTTON_LINK);
            label.setDescription(reportOrder.getErrorDetails());
            label.addListener(new OnClickOpenMessageInNewWindow(source, TM
                    .get("report_order.table.status.failed.popup.title"), reportOrder.getErrorDetails(), Label.CONTENT_PREFORMATTED));
            return label;
        }
        else {
            return new Label(VaadinUtil.getValue("report_order.table.status.new"));
        }
    }
}