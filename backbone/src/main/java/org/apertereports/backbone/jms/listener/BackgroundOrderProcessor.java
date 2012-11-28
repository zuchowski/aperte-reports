/**
 *
 */
package org.apertereports.backbone.jms.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.backbone.util.EmailProcessor;
import org.apertereports.backbone.util.ReportOrderProcessor;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton {@link MessageListener} implementation that asynchronously receives
 * generation orders. It then pushes the id of the resulting report order back
 * to JMS for further processing.
 */
public class BackgroundOrderProcessor implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger(BackgroundOrderProcessor.class.getName());
    /**
     * Singleton
     */
    private static BackgroundOrderProcessor instance;

    public static synchronized BackgroundOrderProcessor getInstance() {
        if (instance == null) {
            instance = new BackgroundOrderProcessor();
        }
        return instance;
    }

    private BackgroundOrderProcessor() {
    }

    /**
     * The method generates a new jasper report on message which contains the id
     * of the report order to process. On successful generation the id result is
     * pushed to JMS. If the recipient email address of the report order was set
     * the generated report is send via email.
     *
     * @param message A JMS message
     * @see MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        logger.info("Message incomming...");
        ReportOrder reportOrder = null;
        try {
            Long id = message.getLongProperty(ReportConstants.REPORT_ORDER_ID);
            logger.info("Order id: " + id + ", generating report...");
            reportOrder = ReportOrderDAO.fetchById(id);
            processReport(reportOrder);
            logger.info("Order id: " + id + ", generation finished");
            if (reportOrder != null) {
                forwardResults(reportOrder);
            }
        } catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            if (reportOrder != null) {
                reportOrder.setReportStatus(Status.FAILED);
                reportOrder.setErrorDetails(e.getMessage());
                ReportOrderDAO.saveOrUpdate(reportOrder);
            }
//			throw new AperteReportsRuntimeException(e);
        }
    }

    /**
     * Adds a JMS message containing the id of the generated report order using
     * configured JMS connection factory.
     *
     * @param reportOrder
     */
    private void addToJMS(ReportOrder reportOrder) throws NamingException, JMSException {
        AperteReportsJmsFacade.sendOrderToJms(reportOrder.getId(), reportOrder.getReplyToQ());
    }

    /**
     * Forwards results to JMS and email address.
     *
     * @param reportOrder Generated report order.
     */
    private void forwardResults(ReportOrder reportOrder) throws Exception {
        if (StringUtils.isNotEmpty(reportOrder.getRecipientEmail())) {
            logger.info("Forwarding order, id: " + reportOrder.getId() + ", to " + reportOrder.getRecipientEmail());
            try {
                EmailProcessor.getInstance().processEmail(reportOrder);
            } catch (Exception e) {
                ExceptionUtils.logWarningException("Unable to send email to: " + reportOrder.getRecipientEmail(), e);
                throw e;
            }
        }
        if (StringUtils.isNotEmpty(reportOrder.getReplyToQ())) {
            addToJMS(reportOrder);
        }
    }

    /**
     * Invokes the main workhorse of the listener - the
     * {@link ReportOrderProcessor}.
     *
     * @param reportOrder Processed report order
     * @throws AperteReportsException on error while generating jasper report
     */
    private void processReport(final ReportOrder reportOrder) throws AperteReportsException {
        ReportOrderProcessor.getInstance().processReport(reportOrder);
    }
}
