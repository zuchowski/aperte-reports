package pl.net.bluesoft.rnd.vries.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.vries.util.Constants;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( {"/testEnvContext.xml"})
public class MdbTest {

    @Test
    public void testMimeTypes() {
        System.out.println(Constants.ReportMimeType.valueOf("PDF"));
        System.out.println(Constants.ReportMimeType.valueOf("HTML"));
        System.out.println(Constants.ReportMimeType.valueOf("CSV"));
        System.out.println(Constants.ReportMimeType.valueOf("XLS"));
    }

}
