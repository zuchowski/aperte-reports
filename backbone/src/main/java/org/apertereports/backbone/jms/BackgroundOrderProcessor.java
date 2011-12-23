/**
 *
 */
package org.apertereports.backbone.jms;

import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.util.EmailProcessor;
import org.apertereports.backbone.util.ReportOrderProcessor;

import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.exception.VriesException;
import org.apertereports.common.exception.VriesRuntimeException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * A {@link MessageListener} implementation that asynchronously receives generation orders.
 * It then pushes the id of the resulting report order back to JMS for further processing.
 */
@MessageDriven(mappedName = ReportConstants.GENERATE_REPORT_Q, activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class BackgroundOrderProcessor implements MessageListener {

    /**
     * The method generates a new jasper report on message which contains the id of the report order to process.
     * On successful generation the id result is pushed to JMS. If the recipient email address of the report order
     * was set the generated report is send via email.
     *
     * @param message A JMS message
     * @see MessageListener#onMessage(javax.jms.Message)
     */
    @Override
    public void onMessage(Message message) {
        ReportOrder reportOrder = null;
        try {
            Long id = message.getLongProperty(ReportConstants.REPORT_ORDER_ID);
            reportOrder = org.apertereports.dao.ReportOrderDAO.fetchReport(id);
            processReport(reportOrder);
            if (reportOrder != null) {
                forwardResults(reportOrder);
            }
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            if (reportOrder != null) {
                reportOrder.setReportStatus(Status.FAILED);
                reportOrder.setErrorDetails(e.getMessage());
                org.apertereports.dao.ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);
            }
            throw new VriesRuntimeException("Error while processing background report order", e);
        }
    }

    /**
     * Adds a JMS message containing the id of the generated report order using configured JMS connection factory.
     *
     * @param reportOrder
     */
    private void addToJMS(ReportOrder reportOrder) throws NamingException, JMSException {
        Connection connection = null;
        Session session = null;
        try {
            InitialContext initCtx = new InitialContext();
            Context envContext = (Context) initCtx.lookup("");
            ConnectionFactory connectionFactory =
                    (ConnectionFactory) envContext.lookup(org.apertereports.dao.utils.ConfigurationCache.getValue(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer =
                    session.createProducer((Destination) envContext.lookup(reportOrder.getReplyToQ()));
            Message reportOrderMessage = session.createMessage();
            reportOrderMessage.setIntProperty(ReportConstants.REPORT_ORDER_ID, reportOrder.getId().intValue());
            producer.send(reportOrderMessage);
            ExceptionUtils.logDebugMessage("sent to " + reportOrder.getReplyToQ() + ": " + reportOrder.getId());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Forwards results to JMS and email address.
     *
     * @param reportOrder Generated report order.
     */
    private void forwardResults(ReportOrder reportOrder) throws Exception {
        if (StringUtils.isNotEmpty(reportOrder.getRecipientEmail())) {
            ExceptionUtils.logDebugMessage("ReportOrder id: " + reportOrder.getId() + " sending email to: " + reportOrder.getRecipientEmail());
            try {
                EmailProcessor.getInstance().processEmail(reportOrder);
            }
            catch (Exception e) {
                ExceptionUtils.logWarningException("Unable to send email to: " + reportOrder.getRecipientEmail(), e);
                throw e;
            }
        }
        if (StringUtils.isNotEmpty(reportOrder.getReplyToQ())) {
            addToJMS(reportOrder);
        }
    }

    /**
     * Invokes the main workhorse of the listener - the {@link ReportOrderProcessor}.
     *
     * @param reportOrder Processed report order
     * @throws VriesException on error while generating jasper report
     */
    private void processReport(final ReportOrder reportOrder) throws VriesException {
        ReportOrderProcessor.getInstance().processReport(reportOrder);
    }
}
