///**
// *
// */
//package org.apertereports.backbone.tests;
//
//import org.apertereports.backbone.jms.ReportOrderPusher;
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.TestExecutionListeners;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.apertereports.common.utils.ExceptionUtils;
//import org.apertereports.common.xml.config.XmlReportConfigLoader;
//import org.apertereports.model.ReportOrder;
//import org.apertereports.model.ReportTemplate;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.junit.Assert.*;
//
///**
// * @author MW
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration( {"/testEnvContext.xml"})
//@TestExecutionListeners
//public class ReportOrderPusherTest {
//    /**
//     * @throws java.lang.Exception
//     */
//    @BeforeClass
//    public static void setUpDB() throws Exception {
//        TestUtil.initDB();
//    }
//
//    /**
//     * Test method for
//     * {@link org.apertereports.backbone.jms.ReportOrderPusher#buildNewOrder(org.apertereports.model.ReportTemplate,
//     * java.util.Map, String, String, String, String)}
//     * .
//     *
//     * @throws Exception
//     */
//    @Test
//    public final void testBuildNewOrder() throws Exception {
//        ReportOrder rep1 = null, rep2 = null, rep3 = null, rep4 = null, rep5 = null;
//        ReportTemplate reportTemplate1 = null, reportTemplate2 = null;
//        try {
//            reportTemplate1 = new ReportTemplate();
//            // reportTemplate1.setId(-1);
//            Map<String, String> parameters1 = new HashMap<String, String>();
//            parameters1.put("id1", "1");
//            String format = "a", recipientEmail = "b", username = "c", replyToQ = "d";
//
//            reportTemplate2 = new ReportTemplate();
//            // reportTemplate1.setId(-2);
//            Map<String, String> parameters2 = new HashMap<String, String>();
//            parameters2.put("id1", "1");
//            parameters2.put("id2", "2");
//
//            org.apertereports.dao.ReportTemplateDAO.saveOrUpdate(reportTemplate1);
//            org.apertereports.dao.ReportTemplateDAO.saveOrUpdate(reportTemplate2);
//
//            int reportOrders0 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            rep1 = ReportOrderPusher.buildNewOrder(reportTemplate1, parameters1, "", "", "", "");
//
//            int reportOrders1 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            rep2 = ReportOrderPusher.buildNewOrder(reportTemplate2, parameters1, "", "", "", "");
//
//            int reportOrders2 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            rep3 = ReportOrderPusher.buildNewOrder(reportTemplate1, parameters1, "", "", "", "");
//
//            int reportOrders3 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            rep4 = ReportOrderPusher.buildNewOrder(reportTemplate1, parameters2, "", "", "", "");
//
//            int reportOrders4 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            rep5 = ReportOrderPusher.buildNewOrder(reportTemplate1, parameters1, format, recipientEmail, username,
//                    replyToQ);
//
//            int reportOrders5 = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders().size();
//
//            assertNotNull("reportOrder1 not created", rep1);
//            assertNotNull("reportOrder2 not created", rep2);
//            assertNull("created duplicate reportOrder", rep3);
//            assertNotNull("reportOrder4 not created");
//            assertNotNull("reportOrder5 not created");
//
//            assertEquals("parameters1 saved incorrectly", XmlReportConfigLoader.getInstance().mapAsXml(parameters1),
//                    rep1.getParametersXml());
//            assertEquals("parameters2 saved incorrectly", XmlReportConfigLoader.getInstance().mapAsXml(parameters2),
//                    rep4.getParametersXml());
//            assertEquals("format not saved", format, rep5.getOutputFormat());
//            assertEquals("email not saved", recipientEmail, rep5.getRecipientEmail());
//            assertEquals("username not saved", username, rep5.getUsername());
//            assertEquals("replyToQ not saved", replyToQ, rep5.getReplyToQ());
//
//            assertEquals("reportOrder1 not created in DB", reportOrders0 + 1, reportOrders1);
//            assertEquals("reportOrder2 not created in DB", reportOrders1 + 1, reportOrders2);
//            assertEquals("duplicate reportOrder3 created in DB", reportOrders2, reportOrders3);
//            assertEquals("reportOrder4 not created in DB", reportOrders3 + 1, reportOrders4);
//            assertEquals("reportOrder5 not created in DB", reportOrders4 + 1, reportOrders5);
//        }
//        catch (Exception e) {
//            ExceptionUtils.logSevereException(e);
//            throw e;
//        }
//        finally {
//            try {
//                org.apertereports.dao.ReportOrderDAO.removeReportOrder(rep1, rep2, rep3, rep4, rep5);
//            }
//            finally {
//                try {
//                    org.apertereports.dao.ReportTemplateDAO.remove(reportTemplate1.getId());
//                }
//                finally {
//                    org.apertereports.dao.ReportTemplateDAO.remove(reportTemplate2.getId());
//                }
//            }
//        }
//
//    }
//}
