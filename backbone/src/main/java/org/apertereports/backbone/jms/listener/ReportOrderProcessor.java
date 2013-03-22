package org.apertereports.backbone.jms.listener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;
import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.jms.ARJmsFacade;
import org.apertereports.backbone.util.EmailProcessor;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ARConstants;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;
import org.apertereports.model.ReportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton {@link MessageListener} implementation that asynchronously receives
 * generation orders. It then pushes the id of the resulting report order back
 * to JMS for further processing.
 */
public class ReportOrderProcessor implements MessageListener {

    private static final Logger logger = LoggerFactory.getLogger("ar.backbone.jms");
    private static ReportOrderProcessor instance;

    public static synchronized ReportOrderProcessor getInstance() {
        if (instance == null) {
            instance = new ReportOrderProcessor();
        }
        return instance;
    }

    private ReportOrderProcessor() {
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
        ReportOrder ro = null;
        try {
            Long id = message.getLongProperty(ARConstants.REPORT_ORDER_ID);
            logger.info("On message, order id: " + id + ", generating report...");
            ro = ReportOrderDAO.fetchById(id);
            if (ro == null) {
                logger.error("ro is null");
                return;
            }
            processReport(ro);
            logger.info("Order id: " + id + ", generation finished");
            forwardResults(ro);
        } catch (Exception e) {
            logger.error("on message error", e);
            if (ro != null) {
                ro.setReportStatus(Status.FAILED);
                ro.setErrorDetails(e.getMessage());
                ReportOrderDAO.saveOrUpdate(ro);
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
        ARJmsFacade.sendReportOrderId(reportOrder, reportOrder.getReplyToQ());
    }

    /**
     * Forwards results to JMS and email address.
     *
     * @param ro Generated report order
     */
    private void forwardResults(ReportOrder ro) throws Exception {
        String email = ro.getRecipientEmail();
        if (StringUtils.isNotEmpty(email)) {
            logger.info("Sending order, id: " + ro.getId() + ", to " + email);
            try {
                EmailProcessor.getInstance().processEmail(ro);
            } catch (Exception e) {
                logger.warn("Unable to send email to: " + email, e);
            }
        }
        String replyQueue = ro.getReplyToQ();
        logger.info("Reply queue: " + replyQueue);
        if (StringUtils.isNotEmpty(replyQueue)) {
            logger.info("add to jms: " + replyQueue);
            addToJMS(ro);
        }
    }

    /**
     * Invokes the main workhorse of the listener - the
     * {@link ReportOrderProcessor}.
     *
     * @param reportOrder Processed report order
     * @throws AperteReportsException on error while generating jasper report
     */
    private void processReport(final ReportOrder reportOrder) throws ARException {
        reportOrder.setStartDate(Calendar.getInstance());
        reportOrder.setReportStatus(ReportOrder.Status.PROCESSING);
        org.apertereports.dao.ReportOrderDAO.saveOrUpdate(reportOrder);

        ReportTemplate reportTemplate = reportOrder.getReport();

        Map<String, String> parametersMap = XmlReportConfigLoader.getInstance().xmlAsMap(reportOrder.getParametersXml());

        try {
            ReportMaster rm = new ReportMaster(reportTemplate.getContent(),
                    reportTemplate.getId().toString(), new ReportTemplateProvider());

            byte[] reportData = rm.generateAndExportReport(reportOrder.getOutputFormat(),
                    new HashMap<String, Object>(parametersMap),
                    ConfigurationCache.getConfiguration());

            reportOrder.setReportResult(ReportGeneratorUtils.encodeContent(reportData));
            reportOrder.setFinishDate(Calendar.getInstance());
            reportOrder.setReportStatus(ReportOrder.Status.SUCCEEDED);
            org.apertereports.dao.ReportOrderDAO.saveOrUpdate(reportOrder);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ARException(e);
        }
    }
}
