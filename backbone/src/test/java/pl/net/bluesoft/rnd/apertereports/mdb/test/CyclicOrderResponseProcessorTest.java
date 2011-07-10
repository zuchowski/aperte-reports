/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.mdb.test;

import org.apache.activemq.command.ActiveMQMessage;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.apertereports.ReportOrderPusher;
import pl.net.bluesoft.rnd.apertereports.dao.CyclicReportOrderDAO;
import pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.data.CyclicReportOrder;
import pl.net.bluesoft.rnd.apertereports.data.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.data.ReportOrder.Status;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.mdb.CyclicOrderResponseProcessor;
import pl.net.bluesoft.rnd.apertereports.util.Constants;
import pl.net.bluesoft.rnd.apertereports.util.ExceptionUtil;
import pl.net.bluesoft.rnd.apertereports.util.TestUtil;

import javax.jms.Message;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

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
     * {@link pl.net.bluesoft.rnd.apertereports.mdb.CyclicOrderResponseProcessor#onMessage(javax.jms.Message)}
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

        char[] testResult = "success".toCharArray();

        try {
            reportTemplate = new ReportTemplate();
            ReportTemplateDAO.saveOrUpdate(reportTemplate);
            reportOrder1 = ReportOrderPusher.buildNewOrder(reportTemplate, new HashMap<String, String>(), "a", "", "",
                    "");

            reportOrder2 = ReportOrderPusher.buildNewOrder(reportTemplate, new HashMap<String, String>(), "b", "", "",
                    "");
            reportOrder1.setReportStatus(Status.SUCCEEDED);
            reportOrder1.setReportResult(testResult);
            reportOrder2.setReportStatus(Status.FAILED);

            ReportOrderDAO.saveOrUpdateReportOrder(reportOrder1);
            ReportOrderDAO.saveOrUpdateReportOrder(reportOrder2);

            cyclicReportOrder = new CyclicReportOrder();
            cyclicReportOrder.setProcessedOrder(reportOrder1);
            Long cyclicId = CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cyclicReportOrder);

            CyclicOrderResponseProcessor corp = new CyclicOrderResponseProcessor();
            Message message = new ActiveMQMessage();
            message.setIntProperty(Constants.REPORT_ORDER_ID, reportOrder1.getId().intValue());
            corp.onMessage(message);

            CyclicReportOrder cyclicReportOrder1 = CyclicReportOrderDAO.fetchCyclicReportOrder(cyclicId);

            assertNull(cyclicReportOrder1.getProcessedOrder());
            assertNotNull(cyclicReportOrder1.getReportOrder());
            assertEquals(reportOrder1.getId(), cyclicReportOrder1.getReportOrder().getId());

            cyclicReportOrder1.setProcessedOrder(reportOrder2);
            CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(cyclicReportOrder1);

            message = new ActiveMQMessage();
            message.setIntProperty(Constants.REPORT_ORDER_ID, reportOrder2.getId().intValue());
            corp.onMessage(message);

            CyclicReportOrder cyclicReportOrder2 = CyclicReportOrderDAO.fetchCyclicReportOrder(cyclicId);
            assertNull(cyclicReportOrder2.getProcessedOrder());
            assertNotNull(cyclicReportOrder2.getReportOrder());
            assertEquals(reportOrder1.getId(), cyclicReportOrder2.getReportOrder().getId());

        }
        catch (Exception e) {
            ExceptionUtil.logSevereException(e);
            throw e;
        }
        finally {
            try {
                CyclicReportOrderDAO.removeCyclicReportOrder(cyclicReportOrder);
            }
            finally {
                try {
                    ReportOrderDAO.removeReportOrder(reportOrder1, reportOrder2);
                }
                finally {
                    ReportTemplateDAO.remove(reportTemplate);
                }
            }
        }
    }

}
