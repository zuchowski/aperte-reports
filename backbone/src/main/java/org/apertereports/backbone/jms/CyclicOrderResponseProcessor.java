package org.apertereports.backbone.jms;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apertereports.common.ReportConstants;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportOrder;

/**
 * A {@link MessageListener} implementation that asynchronously handles the cyclic report orders.
 * On response to its queue the listener fetches the relevant report order and connects its results
 * with the cyclic report order.
 */
@MessageDriven(mappedName = ReportConstants.CYCLIC_REPORT_ORDER_RESPONSE_Q, activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class CyclicOrderResponseProcessor implements MessageListener {

    /**
     * Processes a cyclic report response from report generator. Sets the generated report order for the
     * cyclic report fields.
     *
     * @param message A JMS message
     * @see MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        try {
            Long id = message.getLongProperty(ReportConstants.REPORT_ORDER_ID);
            ReportOrder reportOrder = ReportOrderDAO.fetchReport(id);
            processReport(reportOrder);
        }
        catch (JMSException e) {
            ExceptionUtils.logSevereException(e);
            throw new AperteReportsRuntimeException(e);
        }
    }

    /**
     * Updateds the generated report order field in corresponding cyclic report.
     *
     * @param reportOrder Generated report order
     */
    private void processReport(final ReportOrder reportOrder) {
        CyclicReportOrder cyclicReportOrder = org.apertereports.dao.CyclicReportOrderDAO.fetchForReportOrder(reportOrder.getId());

        if (cyclicReportOrder == null) {
            return;
        }

        cyclicReportOrder.setProcessedOrder(null);

        if (reportOrder.getReportResult() != null) {
            cyclicReportOrder.setReportOrder(reportOrder);
        }

        org.apertereports.dao.CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cyclicReportOrder);
    }
}
