package pl.net.bluesoft.rnd.apertereports.backbone.jms;

import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.apertereports.common.ConfigurationConstants;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.XmlReportConfigLoader;
import pl.net.bluesoft.rnd.apertereports.domain.ConfigurationCache;
import pl.net.bluesoft.rnd.apertereports.domain.WHS;
import pl.net.bluesoft.rnd.apertereports.domain.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportTemplate;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import java.util.Map;

/**
 * Helper class for creating a report order and pushing it to JMS queue.
 */
public class ReportOrderPusher {
    /**
     * Pushes a message to JMS. The message contains a single integer value which is the report order ID.
     *
     * @param id Report order ID
     */
    public static void addToJMS(Long id) {
        Connection connection = null;
        Session session = null;
        try {
            InitialContext initCtx = new InitialContext();
            Context envContext = (Context) initCtx.lookup("");
            ConnectionFactory connectionFactory = (ConnectionFactory) envContext.lookup(
                    ConfigurationCache.getValue(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer((Destination) envContext.lookup(
                    ConfigurationCache.getValue(ConfigurationConstants.JNDI_JMS_QUEUE_GENERATE_REPORT)));

            Message reportOrderMessage = session.createMessage();
            reportOrderMessage.setIntProperty(ReportConstants.REPORT_ORDER_ID, id.intValue());
            producer.send(reportOrderMessage);
            ExceptionUtils.logDebugMessage(ReportConstants.REPORT_ORDER_ID + ": " + id);
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw new RuntimeException(e);
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
            catch (Exception e) {
                ExceptionUtils.logSevereException(e);
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Builds a new report order with given parameters. Returns <code>null</code> when the paramters
     * cannot be converted to an XML representation or another report order with equal configuration already
     * exists in database.
     *
     * @param report         A Jasper report template
     * @param parameters     Report paramters
     * @param format         Output format
     * @param recipientEmail A recipient email address that should receive the report generation result
     * @param username       Order creator login
     * @param replyToQ       JMS queue name the result should be replied to
     * @return A configured report order
     */
    public static ReportOrder buildNewOrder(final ReportTemplate report, Map<String, String> parameters, String format,
                                            String recipientEmail, String username, String replyToQ) {
        final ReportOrder reportOrder = new ReportOrder();
        reportOrder.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(parameters).toCharArray());
        reportOrder.setOutputFormat(format);
        reportOrder.setRecipientEmail(recipientEmail);
        reportOrder.setUsername(username);
        reportOrder.setReport(report);
        reportOrder.setReplyToQ(replyToQ);
        Boolean alreadyExists = new WHS<Boolean>() {
            @Override
            public Boolean lambda() {
                return !sess.createCriteria(ReportOrder.class).add(Restrictions.eq("report", report))
                        .add(Restrictions.eq("parametersXml", reportOrder.getParametersXml()))
                        .add(Restrictions.eq("outputFormat", reportOrder.getOutputFormat()))
                        .add(Restrictions.eq("username", reportOrder.getUsername()))
                        .add(Restrictions.eq("recipientEmail", reportOrder.getRecipientEmail()))
                        .add(Restrictions.eq("replyToQ", reportOrder.getReplyToQ()))
                        .add(Restrictions.isNull("reportResult")).list().isEmpty();
            }
        }.p();
        if (alreadyExists) {
            return null;
        }
        Long id = ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);
        reportOrder.setId(id);
        return reportOrder;
    }
}
