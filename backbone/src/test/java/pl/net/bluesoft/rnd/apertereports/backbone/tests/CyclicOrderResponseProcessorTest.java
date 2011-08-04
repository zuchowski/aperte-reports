/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.backbone.tests;

import org.apache.activemq.command.ActiveMQMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.apertereports.backbone.jms.CyclicOrderResponseProcessor;
import pl.net.bluesoft.rnd.apertereports.backbone.jms.ReportOrderPusher;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.model.CyclicReportOrder;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder.Status;
import pl.net.bluesoft.rnd.apertereports.model.ReportTemplate;

import javax.jms.Message;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.HashMap;

import static org.apache.maven.surefire.assertion.SurefireAssert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author MW
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( {"/testEnvContext.xml"})
@TestExecutionListeners
public class CyclicOrderResponseProcessorTest {

    @BeforeClass
    public static void initDB() throws NamingException, IllegalStateException, IOException {
        TestUtil.initDB();
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.backbone.jms.CyclicOrderResponseProcessor#onMessage(javax.jms.Message)}
     * .
     *
     * @throws Exception
     */
    @Test
    public final void testOnMessage() throws Exception {
        ReportTemplate reportTemplate = null;
        CyclicReportOrder cyclicReportOrder = null;
        ReportOrder reportOrder1 = null;
        ReportOrder reportOrder2 = null;

        String testResult = "success";

        try {
            reportTemplate = new ReportTemplate();
            pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO.saveOrUpdate(reportTemplate);
            reportOrder1 = ReportOrderPusher.buildNewOrder(reportTemplate, new HashMap<String, String>(), "a", "", "",
                    "");

            reportOrder2 = ReportOrderPusher.buildNewOrder(reportTemplate, new HashMap<String, String>(), "b", "", "",
                    "");
            reportOrder1.setReportStatus(Status.SUCCEEDED);
            reportOrder1.setReportResult(testResult);
            reportOrder2.setReportStatus(Status.FAILED);

            pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO.saveOrUpdateReportOrder(reportOrder1);
            pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO.saveOrUpdateReportOrder(reportOrder2);

            cyclicReportOrder = new CyclicReportOrder();
            cyclicReportOrder.setProcessedOrder(reportOrder1);
            Long cyclicId = pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cyclicReportOrder);

            CyclicOrderResponseProcessor corp = new CyclicOrderResponseProcessor();
            Message message = new ActiveMQMessage();
            message.setIntProperty(ReportConstants.REPORT_ORDER_ID, reportOrder1.getId().intValue());
            corp.onMessage(message);

            CyclicReportOrder cyclicReportOrder1 = pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO.fetchCyclicReportOrder(cyclicId);

            assertNull(cyclicReportOrder1.getProcessedOrder());
            assertNotNull(cyclicReportOrder1.getReportOrder());
            assertEquals(reportOrder1.getId(), cyclicReportOrder1.getReportOrder().getId());

            cyclicReportOrder1.setProcessedOrder(reportOrder2);
            pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cyclicReportOrder1);

            message = new ActiveMQMessage();
            message.setIntProperty(ReportConstants.REPORT_ORDER_ID, reportOrder2.getId().intValue());
            corp.onMessage(message);

            CyclicReportOrder cyclicReportOrder2 = pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO.fetchCyclicReportOrder(cyclicId);
            assertNull(cyclicReportOrder2.getProcessedOrder());
            assertNotNull(cyclicReportOrder2.getReportOrder());
            assertEquals(reportOrder1.getId(), cyclicReportOrder2.getReportOrder().getId());

        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw e;
        }
        finally {
            try {
                pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO.removeCyclicReportOrder(cyclicReportOrder);
            }
            finally {
                try {
                    pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO.removeReportOrder(reportOrder1, reportOrder2);
                }
                finally {
                    pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO.remove(reportTemplate);
                }
            }
        }
    }

}
