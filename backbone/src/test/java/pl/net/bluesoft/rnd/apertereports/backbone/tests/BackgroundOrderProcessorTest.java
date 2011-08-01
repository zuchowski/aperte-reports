/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.backbone.tests;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.apertereports.backbone.jms.BackgroundOrderProcessor;
import pl.net.bluesoft.rnd.apertereports.backbone.jms.ReportOrderPusher;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.common.utils.TextUtils;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.model.ReportTemplate;

import javax.jms.Message;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.HashMap;

/**
 * @author MW
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( {"/testEnvContext.xml"})
@TestExecutionListeners
public class BackgroundOrderProcessorTest {

    @BeforeClass
    public static void initDB() throws NamingException, IllegalStateException, IOException {
        TestUtil.initDB();
    }

    private String defaultTestReport = "/test vries 44.jrxml";

    /**
     * @throws Exception
     */
    @Test
    public final void testOnMessage() throws Exception {
        ReportTemplate reportTemplate = null;

        ReportOrder reportOrder = null;
        try {
            reportTemplate = new ReportTemplate();
            String content = TextUtils.readTestFileToString(getClass().getResourceAsStream(defaultTestReport));
            char[] contentCA = new String(Base64.encodeBase64(content.getBytes("UTF-8"))).toCharArray();
            reportTemplate.setContent(contentCA);
            pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO.saveOrUpdate(reportTemplate);
            reportOrder = ReportOrderPusher
                    .buildNewOrder(reportTemplate, new HashMap<String, String>(), "", "", "", "");
            BackgroundOrderProcessor bop = new BackgroundOrderProcessor();
            Message message = new ActiveMQMessage();
            message.setIntProperty(ReportConstants.REPORT_ORDER_ID, reportOrder.getId().intValue());
            bop.onMessage(message);

           /* ReportOrder reportOrder1 = ReportOrderDAO.fetchReport(reportOrder.getId());
            assertEquals(reportOrder1.getErrorDetails(), Status.SUCCEEDED, reportOrder1.getReportStatus());
            assertNotNull(reportOrder1.getFinishDate());
            assertNotNull(reportOrder1.getStartDate());
            assertNotNull(reportOrder1.getReportResult());
*/
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw e;
        }
        finally {
            try {
                pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO.removeReportOrder(reportOrder);
            }
            finally {
                pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO.remove(reportTemplate.getId());
            }
        }
    }
}
