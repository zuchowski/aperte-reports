package org.apertereports.backbone.scheduler;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.apertereports.backbone.jms.ARJmsFacade;
import org.apertereports.backbone.util.ReportOrderBuilder;
import org.apertereports.common.ARConstants;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.CyclicReportConfigDAO;
import org.apertereports.model.CyclicReportConfig;
import org.apertereports.model.ReportOrder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Quartz {@link org.quartz.Job} that picks a cyclic report configuration
 * id from the job context and launches a report generation process.
 * <p/>
 * <p>If somehow the report is not found in the database or another job is being
 * processed at the time of invocation, the generation process is omitted.
 */
public class ReportOrderJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger("ar.backbone.scheduler");

    /**
     * Invokes the report order processing.
     *
     * @param context Scheduler job context
     * @throws JobExecutionException on scheduler error
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDetail detail = context.getJobDetail();
        logger.info("Report order job, config id: " + detail.getName() + ", executing...");
        processOrder(detail);
        logger.info("report order job, config id: " + detail.getName() + ", finished");
    }

    /**
     * Executes a report generation process based on a cyclic report. The report
     * is fetched from database by the id provided by the job details.
     * <p/>
     * <p>The newly created report order is linked with the cyclic report and
     * pushed to JMS queue for later processing.
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    private ReportOrder processOrder(JobDetail details) {
        String instName = details.getName();
        Long reportId = Long.valueOf(instName);
        CyclicReportConfig config = CyclicReportConfigDAO.fetchById(reportId);

        if (config == null) {
            logger.warn("config is null");
            return null;
        }
        java.text.SimpleDateFormat sdf= new java.text.SimpleDateFormat("HH:mm:ss");
        

        ReportOrder ro = config.getProcessedOrder();
        Calendar cd = ro.getCreateDate();
        logger.info("cd: " + sdf.format(cd.getTime()));
        Calendar cu = Calendar.getInstance();
        cu.setTime(new Date());
        logger.info("cu: " + sdf.format(cu.getTime()));
        
        if (ro != null) {
            logger.info("processedOrder != null");
            return null;
        }

        Map<String, String> params = XmlReportConfigLoader.getInstance().xmlAsMap(config.getParametersXml());
        ro = ReportOrderBuilder.build(config.getReport(), params, config.getOutputFormat(),
                config.getRecipientEmail(), null, ARConstants.JNDI_JMS_CYCLIC_REPORT_ORDER_QUEUE_NAME);
        config.setProcessedOrder(ro);
        CyclicReportConfigDAO.saveOrUpdate(config);
        ARJmsFacade.sendReportOrderId(ro, ARConstants.JNDI_JMS_GENERATE_REPORT_QUEUE_NAME);
        return ro;
    }
}
