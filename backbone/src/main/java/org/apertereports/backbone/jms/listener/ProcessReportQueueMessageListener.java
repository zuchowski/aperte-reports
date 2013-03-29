package org.apertereports.backbone.jms.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.apertereports.common.ARConstants;
import org.apertereports.dao.CyclicReportConfigDAO;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.CyclicReportConfig;
import org.apertereports.model.ReportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton {@link MessageListener} implementation that asynchronously handles
 * the cyclic report orders. On response to its queue the listener fetches the
 * relevant report order and connects its results with the cyclic report order.
 */
public final class ProcessReportQueueMessageListener implements MessageListener {

    /**
     * Singleton
     */
    private static ProcessReportQueueMessageListener instance;
    private static Logger logger = LoggerFactory.getLogger("ar.backbone.jms");

    public static synchronized ProcessReportQueueMessageListener getInstance() {
        if (instance == null) {
            instance = new ProcessReportQueueMessageListener();
        }
        return instance;
    }

    private ProcessReportQueueMessageListener() {
    }

    /**
     * Processes a cyclic report response from report generator. Sets the
     * generated report order for the cyclic report fields.
     *
     * @param message A JMS message
     * @see MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        try {
            Long id = message.getLongProperty(ARConstants.JMS_PROPERTY_REPORT_ORDER_ID);
            logger.info("On message, order id: " + id);
            ReportOrder reportOrder = ReportOrderDAO.fetchById(id);
            processReport(reportOrder);
        } catch (JMSException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Updateds the generated report order field in corresponding cyclic report.
     *
     * @param reportOrder Generated report order
     */
    private void processReport(final ReportOrder reportOrder) {
        CyclicReportConfig config = CyclicReportConfigDAO.fetchForProcessedReportOrder(reportOrder.getId());
        if (config == null) {
            logger.warn("config is null");
            return;
        }

        config.setProcessedOrder(null);
        if (reportOrder.getReportResult() != null) {
            config.setReportOrder(reportOrder);
        }

        CyclicReportConfigDAO.saveOrUpdate(config);
    }
}
