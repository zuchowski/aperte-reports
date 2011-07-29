import org.junit.Test;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfig;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfigParameter;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.ReportConfigRoot;
import pl.net.bluesoft.rnd.apertereports.common.xml.config.XmlReportConfigLoader;

import javax.xml.bind.JAXBException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class UtilTest {
    private static String TEXT_1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<reportConfigs><reportConfig id=\"0\" allowRefresh=\"true\" reportId=\"7\" cacheTimeout=\"12\" allowedFormats=\"XLSX\"/></reportConfigs>";
    private static String TEXT_2 = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<reportConfigs>\n" +
            "    <reportConfig allowedFormats=\"PDF\" allowRefresh=\"true\" cacheTimeout=\"0\" reportId=\"128077\" id=\"0\">\n" +
            "        <reportParameter value=\"31-08-2011 22:16\" name=\"startDateTo\"/>\n" +
            "        <reportParameter value=\"31-08-2011 22:16\" name=\"endDateTo\"/>\n" +
            "        <reportParameter value=\"31-07-2011 22:16\" name=\"dueDateTo\"/>\n" +
            "        <reportParameter value=\"01-06-2011 22:16\" name=\"startDateFrom\"/>\n" +
            "        <reportParameter value=\"art\" name=\"login\"/>\n" +
            "        <reportParameter value=\"01-06-2011 22:16\" name=\"endDateFrom\"/>\n" +
            "        <reportParameter value=\"01-06-2011 22:16\" name=\"dueDateFrom\"/>\n" +
            "    </reportConfig>\n" +
            "</reportConfigs>";

    @Test
    public void testUnmarshall_2() {
        ReportConfigRoot root = (ReportConfigRoot) XmlReportConfigLoader.getInstance().unmarshall(TEXT_2);
        assertNotNull(root);
    }

    @Test
    public void testConfigParser() throws JAXBException {
        List<ReportConfig> res = XmlReportConfigLoader.getInstance().stringAsReportConfigs(TEXT_1);
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
        ReportConfigRoot root = (ReportConfigRoot) XmlReportConfigLoader.getInstance().unmarshall(TEXT_1);
        System.out.println(XmlReportConfigLoader.getInstance().marshall(root));
    }

}
