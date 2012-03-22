package org.apertereports.backbone.util;

import java.util.Map;

import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.hibernate.criterion.Restrictions;

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
    	AperteReportsJmsFacade.sendOrderToJms(id, ConfigurationConstants.JNDI_JMS_QUEUE_GENERATE_REPORT);
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
        reportOrder.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(parameters));
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
