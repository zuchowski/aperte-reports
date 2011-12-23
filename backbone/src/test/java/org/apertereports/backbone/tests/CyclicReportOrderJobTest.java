package org.apertereports.backbone.tests;

import org.apertereports.backbone.scheduler.CyclicReportOrderJob;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobDetail;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.exception.VriesException;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author MW
 */
public class CyclicReportOrderJobTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        TestUtil.initDB();
    }

    @Test
    public void testParams() {
        Map<String, String> parameters1 = new HashMap<String, String>();
        parameters1.put("id1", "1");
        parameters1.put("xxx", "44");

        char[] xml = XmlReportConfigLoader.getInstance().mapAsXml(parameters1).toCharArray();
        System.out.println(xml);

        Map<String, String> parameters2 = XmlReportConfigLoader.getInstance().xmlAsMap(new String(xml));

        assertTrue(parameters1.keySet().containsAll(parameters2.keySet()));
        assertTrue(parameters2.keySet().containsAll(parameters1.keySet()));
    }

    /**
     * Test method for
     * {@link org.apertereports.backbone.scheduler.CyclicReportOrderJob#processOrder(org.quartz.JobDetail)}
     * .
     *
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws VriesException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    @Test
    public final void testProcessOrder() throws SecurityException, NoSuchMethodException, VriesException,
            IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        ReportOrder rep1 = null;
        ReportTemplate reportTemplate1 = null;
        CyclicReportOrder cro = null;
        try {
            reportTemplate1 = new ReportTemplate();
            org.apertereports.dao.ReportTemplateDAO.saveOrUpdate(reportTemplate1);

            Map<String, String> parameters1 = new HashMap<String, String>();
            parameters1.put("id1", "1");
            String format = "a", recipientEmail = "b";
            cro = new CyclicReportOrder();
            cro.setOutputFormat(format);
            cro.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(parameters1));
            cro.setRecipientEmail(recipientEmail);
            cro.setReport(reportTemplate1);
            Long croId = org.apertereports.dao.CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cro);

            JobDetail jobDetail = new JobDetail(cro.getId().toString(), CyclicReportOrder.class.toString(),
                    CyclicReportOrderJob.class);
            CyclicReportOrderJob croj = new CyclicReportOrderJob();

            Method method = CyclicReportOrderJob.class.getDeclaredMethod("processOrder", JobDetail.class);
            method.setAccessible(true);
            rep1 = (ReportOrder) method.invoke(croj, jobDetail);

            assertNotNull("ReportOrder not created", rep1);

            ReportOrder repOrderFromDB = org.apertereports.dao.ReportOrderDAO.fetchReport(rep1.getId());
            assertEquals(reportTemplate1.getId(), repOrderFromDB.getReport().getId());
            assertEquals(cro.getOutputFormat(), repOrderFromDB.getOutputFormat());
            assertEquals(cro.getParametersXml(), repOrderFromDB.getParametersXml());
            assertEquals(cro.getRecipientEmail(), repOrderFromDB.getRecipientEmail());
            assertEquals(ReportConstants.CYCLIC_REPORT_ORDER_RESPONSE_Q, repOrderFromDB.getReplyToQ());

            CyclicReportOrder cyclicReportOrder1 = org.apertereports.dao.CyclicReportOrderDAO.fetchCyclicReportOrder(croId);
            assertNotNull(cyclicReportOrder1.getProcessedOrder());
            assertEquals(repOrderFromDB.getId(), cyclicReportOrder1.getProcessedOrder().getId());

        }
        finally {
            try {
                org.apertereports.dao.CyclicReportOrderDAO.removeCyclicReportOrder(cro);
            }
            finally {
                try {
                    org.apertereports.dao.ReportOrderDAO.removeReportOrder(rep1);
                }
                finally {
                    org.apertereports.dao.ReportTemplateDAO.remove(reportTemplate1);
                }
            }
        }
    }

}
