import org.junit.BeforeClass;
import org.junit.Test;
import pl.net.bluesoft.rnd.vries.xml.ReportConfig;
import pl.net.bluesoft.rnd.vries.xml.ReportConfigParameter;
import pl.net.bluesoft.rnd.vries.xml.ReportConfigRoot;
import pl.net.bluesoft.rnd.vries.xml.XmlHelper;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class UtilTest {
    private static String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<reportConfigs><reportConfig id=\"0\" allowRefresh=\"true\" reportId=\"7\" cacheTimeout=\"12\" allowedFormats=\"XLSX\"/></reportConfigs>";

    private static Marshaller marshaller;
    private static Unmarshaller unmarshaller;

    @BeforeClass
    public static void setupJaxb() throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(ReportConfigRoot.class);
        marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        unmarshaller = context.createUnmarshaller();
    }

    @Test
    public void testConfigParser() throws JAXBException {
        List<ReportConfig> res = XmlHelper.stringAsReportConfigs(text);
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

        StringWriter sw = new StringWriter();
        marshaller.marshal(root, sw);

        System.out.print(sw.toString());
    }

    @Test
    public void testUnmarshaller() throws JAXBException {
        StringReader sr = new StringReader(text);
        ReportConfigRoot root = (ReportConfigRoot) unmarshaller.unmarshal(sr);

        StringWriter sw = new StringWriter();
        marshaller.marshal(root, sw);

        System.out.print(sw.toString());
    }

}
