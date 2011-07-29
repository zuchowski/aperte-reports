package pl.net.bluesoft.rnd.apertereports.backbone.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( {"/testEnvContext.xml"})
public class MdbTest {

    @Test
    public void testMimeTypes() {
        System.out.println(ReportConstants.ReportMimeType.valueOf("PDF"));
        System.out.println(ReportConstants.ReportMimeType.valueOf("HTML"));
        System.out.println(ReportConstants.ReportMimeType.valueOf("CSV"));
        System.out.println(ReportConstants.ReportMimeType.valueOf("XLS"));
    }

}
