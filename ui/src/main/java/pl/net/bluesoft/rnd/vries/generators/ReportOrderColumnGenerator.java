package pl.net.bluesoft.rnd.vries.generators;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.themes.BaseTheme;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.vries.ReportOrderPusher;
import pl.net.bluesoft.rnd.vries.components.VriesReportOrderBrowserComponent.Columns;
import pl.net.bluesoft.rnd.vries.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.vries.data.ReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportOrder.Status;
import pl.net.bluesoft.rnd.vries.exception.VriesException;
import pl.net.bluesoft.rnd.vries.util.*;
import pl.net.bluesoft.rnd.vries.util.Constants.ReportType;
import pl.net.bluesoft.rnd.vries.util.NotificationUtil.ConfirmListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Generates a table cell for various report order properties.
 */
public class ReportOrderColumnGenerator implements ColumnGenerator {

    /**
     * Generates a status cell for {@link Columns#RESULT}, detailed description for {@link Columns#DETAILS},
     * a rerun action button for {@link Columns#ACTION} or creation date for {@link Columns#CREATE_DATE}.
     * Should be used with a {@link ReportOrder} data bean.
     *
     * @param source Source table
     * @param itemId Item id
     * @param columnId Column id
     * @return A resulting cell component
     */
    @Override
    public Component generateCell(final Table source, final Object itemId, final Object columnId) {
        final ReportOrder reportOrder = (ReportOrder) source.getItem(itemId).getItemProperty(Columns.REPORT_ORDER)
                .getValue();
        if (reportOrder == null) {
            return null;
        }
        switch ((Columns) columnId) {
            case RESULT:
                if (reportOrder.getReportStatus() == Status.SUCCEEDED) {
                    HorizontalLayout hl = new HorizontalLayout();
                    hl.setSpacing(true);
                    for (final ReportType format : Constants.ReportType.values()) {
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
                                    ExceptionUtil.logSevereException(e);
                                    NotificationUtil.showExceptionNotification(source.getWindow(), new VriesException(e));
                                }
                            }
                        });
                        hl.addComponent(formatLink);
                    }
                    return hl;
                }
                else if (reportOrder.getReportStatus() == Status.PROCESSING) {
                    return new Label(TM.get("report_order.table.status.processing"));
                }
                else if (reportOrder.getReportStatus() == Status.FAILED) {
                    Button label = new Button(TM.get("report_order.table.status.failed"));
                    label.setStyleName(BaseTheme.BUTTON_LINK);
                    label.setDescription(reportOrder.getErrorDetails());
                    label.addListener(new OnClickOpenMessageInNewWindow(source, TM
                            .get("report_order.table.status.failed.popup.title"),
                            String.valueOf(reportOrder.getErrorDetails()), Label.CONTENT_PREFORMATTED));
                    return label;
                }
                else {
                    return new Label(TM.get("report_order.table.status.new"));
                }
            case DETAILS:
                Button paramsLink = new Button(TM.get("report_order.table.parameters"));
                paramsLink.setStyleName(BaseTheme.BUTTON_LINK);
                paramsLink.addListener(new OnClickOpenMessageInNewWindow(source, TM
                        .get("report_order.table.parameters.popup.title"), String.valueOf(reportOrder.getParametersXml()),
                        Label.CONTENT_PREFORMATTED));
                return paramsLink;
            case ACTION:
                if (reportOrder.getReportStatus() == Status.FAILED || reportOrder.getReportStatus() == Status.SUCCEEDED) {
                    HorizontalLayout hl = new HorizontalLayout();
                    hl.setSpacing(true);
                    Button rerunLink = new Button(TM.get("report_order.table.rerun"));
                    rerunLink.setStyleName(BaseTheme.BUTTON_LINK);
                    rerunLink.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(ClickEvent event) {
                            NotificationUtil.showConfirmWindow(source.getApplication().getMainWindow(),
                                    TM.get("report_order.table.rerun.popup.title"),
                                    TM.get("report_order.table.rerun.popup.question"), new ConfirmListener() {
                                @Override
                                public void onConfirm() {
                                    ReportOrder shallowCopy = reportOrder.shallowCopy();
                                    Long newReportId = ReportOrderDAO.saveOrUpdateReportOrder(shallowCopy);
                                    ReportOrderPusher.addToJMS(newReportId);
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                        }
                    });
                    hl.addComponent(rerunLink);
                    return hl;
                }
                break;
            case CREATE_DATE:
                Calendar createDateCal = reportOrder.getCreateDate();
                if (createDateCal != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATETIME_PATTERN);
                    return new Label(sdf.format(createDateCal.getTime()));
                }
                break;
        }
        return null;
    }
}
