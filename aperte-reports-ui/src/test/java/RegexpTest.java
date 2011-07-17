import org.junit.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import pl.net.bluesoft.rnd.apertereports.util.DashboardUtil;
import pl.net.bluesoft.rnd.apertereports.wrappers.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

public class RegexpTest implements TestTexts {
    private static String pattern = DashboardUtil.REPORT_TAG_PATTERN.toString();//"<REPORT\\sidx=\"([0-9]+)\"/>";

    @Test
    public void testPattern() {
        Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text2);
        StringBuffer sb = new StringBuffer();
        int begin = 0;
        while (m.find()) {
            sb.append(text2.substring(begin, m.start(0)));
            begin = m.end(0);
            System.out.println("Found '" + m.group(0) + "' at position " + m.start(0) + "-" + m.end(0));
            if (m.start(0) < m.end(0)) {
                System.out.println("Suffix is " + m.group(1));
            }
        }
        sb.append(text2.substring(begin));
        System.out.println(sb.toString());
    }

    @Test
    public void testDrilldown2() throws Exception {
        testDrill(text6);
    }

    @Test
    public void testDrilldown3() throws Exception {
        Pair<String, Map<String, String>> pair = DashboardUtil.parseHtmlTag(text6, true);
        System.out.println("text = " + pair.getKey());
        System.out.println("params = " + pair.getEntry());
    }

    @Test
    public void testDrill3() throws Exception {
        DashboardUtil.executeTemplateMatcher(fulltext3, DashboardUtil.DRILLDOWN_TAG_PATTERN, new DashboardUtil.MatchHandler() {
            @Override
            public void handleMatch(int start, int end, String match) {
                if (match != null) {
                    System.out.println("MATCH: " + match);
                }
            }
        });
    }

    @Test
    public void testReport() throws Exception {
        DashboardUtil.executeTemplateMatcher(text2, DashboardUtil.REPORT_TAG_PATTERN, new DashboardUtil.MatchHandler() {
            @Override
            public void handleMatch(int start, int end, String match) {
                if (match != null) {
                    System.out.println("MATCH: " + match);
                }
            }
        });
    }

    @Test
    public void testReportWithXLS() throws Exception {
        DashboardUtil.executeTemplateMatcherWithList(text2xls, DashboardUtil.REPORT_TAG_PATTERN, new DashboardUtil.MatchHandlerWithList() {
            @Override
            public void handleMatch(int start, int end, List<String> matches) {
                for (String match : matches) {
                    System.out.println("MATCH: " + match);
                }
            }
        });
    }

    @Test
    public void testReportTagsWithXLS() throws Exception {
        final List<String> m1 = new ArrayList<String>();
        DashboardUtil.executeTemplateMatcherWithList(text2xls, DashboardUtil.REPORT_TAG_PATTERN, new DashboardUtil.MatchHandlerWithList() {
            @Override
            public void handleMatch(int start, int end, List<String> matches) {
                m1.addAll(matches);
            }
        });
        final List<String> m2 = new ArrayList<String>();
        DashboardUtil.executeTemplateMatcherWithList(text2xls2, DashboardUtil.REPORT_TAG_PATTERN, new DashboardUtil.MatchHandlerWithList() {
            @Override
            public void handleMatch(int start, int end, List<String> matches) {
                m2.addAll(matches);
            }
        });
        final List<String> m3 = new ArrayList<String>();
        DashboardUtil.executeTemplateMatcherWithList(text2xls3, DashboardUtil.REPORT_TAG_PATTERN, new DashboardUtil.MatchHandlerWithList() {
            @Override
            public void handleMatch(int start, int end, List<String> matches) {
                m3.addAll(matches);
            }
        });

        assertTrue(m1.containsAll(m2));
        assertTrue(m2.containsAll(m1));
        assertTrue(m1.containsAll(m3));
        assertTrue(m3.containsAll(m1));
        assertTrue(m2.containsAll(m3));
        assertTrue(m3.containsAll(m2));
    }

    private void testDrill(String text) throws ParserConfigurationException, SAXException, IOException {
        Matcher m = DashboardUtil.DRILLDOWN_TAG_PATTERN.matcher(text);
        assertTrue(m.matches());

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Map<String, String> params = parseTag(db, text);

        Map<String, List<String>> hyperlinkParams = DashboardUtil.parseHyperlinkParameters(params.get("href"));
        System.out.println(hyperlinkParams);
    }

    @Test
    public void testDrill() throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Map<String, String> params = parseTag(db, text7);

        Map<String, List<String>> hyperlinkParams = DashboardUtil.parseHyperlinkParameters(params.get("href"));
        System.out.println(hyperlinkParams);
    }

    @Test
    public void testDrilldown() throws Exception {
        testDrill(text5);
    }

    @Test
    public void testFullText() throws Exception {
        Matcher m = DashboardUtil.CHART_TAG_PATTERN.matcher(fulltext);
        StringBuffer sb = new StringBuffer();
        int begin = 0, i = 0;
        while (m.find()) {
            sb.append(fulltext.substring(begin, m.start(0)));
            begin = m.end(0);
            if (m.start(0) < m.end(0)) {
                sb.append("<div location=\"component" + i + "\"></div>");
                System.out.println("Suffix is " + m.group(1));
            }
        }
        sb.append(fulltext.substring(begin));
        System.out.println(sb.toString());
    }

    @Test
    public void testImgPattern() throws Exception {
        Matcher m = DashboardUtil.CHART_TAG_PATTERN.matcher(text3);
        assertTrue(m.matches());

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        String[] texts = new String[] {text3, text4};
        for (String t : texts) {
            parseTag(db, t);
        }
    }

    private Map<String, String> parseTag(DocumentBuilder db, String t) throws SAXException, IOException {
        ByteArrayInputStream input = new ByteArrayInputStream(t.getBytes());
        Document doc = db.parse(input);
        Element el = doc.getDocumentElement();
        String text = el.getTextContent();
        NamedNodeMap map = el.getAttributes();
        Map<String, String> params = new HashMap<String, String>();
        for (int i = 0; i < map.getLength(); ++i) {
            Node node = map.item(i);
            params.put(node.getNodeName(), node.getNodeValue());
        }
        NodeList nodes = el.getChildNodes();
        for (int i = 0; i < nodes.getLength(); ++i) {
            if (nodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) nodes.item(i);
                System.out.println(e.toString());
            }
        }

        System.out.println("text = " + t);
        System.out.println("params = " + params);
        System.out.println("value = " + text);
        return params;
    }

}
