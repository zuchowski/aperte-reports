package org.apertereports.backbone.scheduler;

import java.util.Map;

import org.apertereports.backbone.util.ReportOrderPusher;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportOrder;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Simple Quartz {@link org.quartz.Job} that adapter that picks a cyclic report id from the job
 * context and launches a report generation process.
 * <p/>
 * <p>If somehow the report is not found in the database or another job is being processed at the time of invocation,
 * the generation process is omitted.
 */
public class CyclicReportOrderJob implements Job {

    /**
     * Invokes the report order processing.
     *
     * @param context Scheduler job context
     * @throws JobExecutionException on scheduler error
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        processOrder(context.getJobDetail());
    }

    /**
     * Executes a report generation process based on a cyclic report. The report is fetched from
     * database by the id provided by the job details.
     * <p/>
     * <p>The newly created report order is linked with the cyclic report and pushed to JMS queue
     * for later processing.
     *
     * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
     */
    private ReportOrder processOrder(JobDetail details) {
        String instName = details.getName();
        Long reportId = Long.valueOf(instName);
        CyclicReportOrder cRO = org.apertereports.dao.CyclicReportOrderDAO.fetchCyclicReportOrder(reportId);
        ReportOrder newOrder = null;
        if (cRO != null && cRO.getProcessedOrder() == null) {
            Map<String, String> params = XmlReportConfigLoader.getInstance().xmlAsMap(cRO.getParametersXml());
            newOrder = ReportOrderPusher.buildNewOrder(cRO.getReport(), params, cRO.getOutputFormat(),
                    cRO.getRecipientEmail(), null, ConfigurationConstants.JNDI_JMS_QUEUE_CYCLIC_REPORT);
            cRO.setProcessedOrder(newOrder);
            org.apertereports.dao.CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cRO);
            ReportOrderPusher.addToJMS(newOrder.getId());
        }
        return newOrder;
    }
}
