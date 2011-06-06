/**
 *
 */
package pl.net.bluesoft.rnd.vries.mdb.test;

import org.apache.activemq.command.ActiveMQMessage;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.vries.ReportOrderPusher;
import pl.net.bluesoft.rnd.vries.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.vries.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.vries.data.ReportOrder;
import pl.net.bluesoft.rnd.vries.data.ReportTemplate;
import pl.net.bluesoft.rnd.vries.mdb.BackgroundOrderProcessor;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.ExceptionUtil;
import pl.net.bluesoft.rnd.vries.util.TestUtil;
import pl.net.bluesoft.rnd.vries.util.TextUtil;

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
            String content = TextUtil.readTestFileToString(getClass().getResourceAsStream(defaultTestReport));
            char[] contentCA = String.valueOf(Base64.encodeBase64(content.getBytes("UTF-8"))).toCharArray();
            reportTemplate.setContent(contentCA);
            ReportTemplateDAO.saveOrUpdate(reportTemplate);
            reportOrder = ReportOrderPusher
                    .buildNewOrder(reportTemplate, new HashMap<String, String>(), "", "", "", "");
            BackgroundOrderProcessor bop = new BackgroundOrderProcessor();
            Message message = new ActiveMQMessage();
            message.setIntProperty(Constants.REPORT_ORDER_ID, reportOrder.getId().intValue());
            bop.onMessage(message);

           /* ReportOrder reportOrder1 = ReportOrderDAO.fetchReport(reportOrder.getId());
            assertEquals(reportOrder1.getErrorDetails(), Status.SUCCEEDED, reportOrder1.getReportStatus());
            assertNotNull(reportOrder1.getFinishDate());
            assertNotNull(reportOrder1.getStartDate());
            assertNotNull(reportOrder1.getReportResult());
*/
        }
        catch (Exception e) {
            ExceptionUtil.logSevereException(e);
            throw e;
        }
        finally {
            try {
                ReportOrderDAO.removeReportOrder(reportOrder);
            }
            finally {
                ReportTemplateDAO.remove(reportTemplate.getId());
            }
        }
    }
}
