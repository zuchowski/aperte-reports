/**
 *
 */
package pl.net.bluesoft.rnd.vries.mdb;

import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.vries.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.vries.data.ReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportOrder.Status;
import pl.net.bluesoft.rnd.vries.exception.VriesException;
import pl.net.bluesoft.rnd.vries.util.ConfigurationCache;
import pl.net.bluesoft.rnd.vries.util.ConfigurationConstants;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.ExceptionUtil;

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
@MessageDriven(mappedName = Constants.GENERATE_REPORT_Q, activationConfig = {
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
            Long id = message.getLongProperty(Constants.REPORT_ORDER_ID);
            reportOrder = ReportOrderDAO.fetchReport(id);
            processReport(reportOrder);
        }
        catch (JMSException e) {
            ExceptionUtil.logSevereException(e);
        }
        catch (VriesException e) {
            ExceptionUtil.logSevereException(e);
            if (reportOrder != null) {
                reportOrder.setReportStatus(Status.FAILED);
                reportOrder.setErrorDetails(e.getMessage());
                ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);
            }
        }

        if (reportOrder != null) {
            forwardResults(reportOrder);
        }

    }

    /**
     * Adds a JMS message containing the id of the generated report order using configured JMS connection factory.
     *
     * @param reportOrder
     */
    private void addToJMS(ReportOrder reportOrder) {
        Connection connection = null;
        Session session = null;
        try {
            InitialContext initCtx = new InitialContext();
            Context envContext = (Context) initCtx.lookup("");
            ConnectionFactory connectionFactory =
                    (ConnectionFactory) envContext.lookup(ConfigurationCache.getValue(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer =
                    session.createProducer((Destination) envContext.lookup(reportOrder.getReplyToQ()));
            Message reportOrderMessage = session.createMessage();
            reportOrderMessage.setIntProperty(Constants.REPORT_ORDER_ID, reportOrder.getId().intValue());
            producer.send(reportOrderMessage);

            ExceptionUtil.logDebug("sent to " + reportOrder.getReplyToQ() + ": " + reportOrder.getId());
        }
        catch (NamingException e) {
            ExceptionUtil.logSevereException(e);
        }
        catch (JMSException e) {
            ExceptionUtil.logSevereException(e);
        }
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (session != null) {
                    session.close();
                }
            }
            catch (JMSException e) {
                ExceptionUtil.logSevereException(e);
            }
        }
    }

    /**
     * Forwards results to JMS and email address.
     *
     * @param reportOrder Generated report order.
     */
    private void forwardResults(ReportOrder reportOrder) {
        if (StringUtils.isNotEmpty(reportOrder.getRecipientEmail())) {
            ExceptionUtil.logDebug("ReportOrder id: " + reportOrder.getId() + " sending email to: " + reportOrder.getRecipientEmail());
            try {
                EmailProcessor.getInstance().processEmail(reportOrder);
            }
            catch (Exception e) {
                ExceptionUtil.logWarningException("Unable to send email to: " + reportOrder.getRecipientEmail(), e);
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
