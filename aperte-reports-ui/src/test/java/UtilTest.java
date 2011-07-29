import org.junit.Test;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfig;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfigParameter;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfigRoot;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.XmlReportConfigLoader;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

public class UtilTest {
    private static String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<reportConfigs><reportConfig id=\"0\" allowRefresh=\"true\" reportId=\"7\" cacheTimeout=\"12\" allowedFormats=\"XLSX\"/></reportConfigs>";

    @Test
    public void testConfigParser() throws JAXBException {
        List<ReportConfig> res = XmlReportConfigLoader.getInstance().stringAsReportConfigs(text);
        for (ReportConfig r : res) {
            System.out.println(r.getId());
        }
    }

    @Test
    public void testMarshaller() throws JAXBException {
        ReportConfigRoot root = new ReportConfigRoot();
        root.setReportConfigs(new ArrayList<ReportConfig>());

        List<ReportConfigParameter> params = new ArrayList<ReportConfigParameter>();
        ReportConfigParameter p = new ReportConfigParameter();
        p.setName("nazwa");
        p.setValue("wartosc");
        params.add(p);

        ReportConfig rc = new ReportConfig(1, 2, 3, 4l, true, "CSV,PDF", params);
        root.getReportConfigs().add(rc);

        System.out.println(XmlReportConfigLoader.getInstance().marshall(root));
    }

    @Test
    public void testUnmarshaller() throws JAXBException {
        ReportConfigRoot root = (ReportConfigRoot) XmlReportConfigLoader.getInstance().unmarshall(text);
        System.out.println(XmlReportConfigLoader.getInstance().marshall(root));
    }

}
