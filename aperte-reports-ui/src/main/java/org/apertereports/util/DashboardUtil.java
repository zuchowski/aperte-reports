package org.apertereports.util;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRLoader;

import org.apache.commons.codec.binary.Base64;
import org.apertereports.common.ARConstants.ReportType;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.wrappers.Pair;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Utility class with methods for report tags retrieval, regexp matching etc.
 */
public final class DashboardUtil {
    private DashboardUtil() {
    }

    /**
     * Regexp that identifies the anchor of the report drilldown in dashboard HTML.
     */
    public static final String CHART_SOURCE_PREFIX_REGEXP = "dashboardimage\\?image=";
    /**
     * Used as a JRExporter parameter to identify drilldown reports.
     */
    public static final String CHART_SOURCE_PREFIX_TEXT = "dashboardimage?image=";

    /**
     * Regexp that identifies the anchor of the report in dashboard HTML.
     */
    public static final Pattern REPORT_TAG_PATTERN = Pattern.compile("<REPORT\\sidx=\\\"([0-9]+)\\\"\\s?(xlsidx=\\\"([0-9]+)\\\")?\\s?[/]{0,1}>(?!</REPORT>)|<REPORT\\sidx=\\\"([0-9]+)\\\"\\s?(xlsidx=\\\"([0-9]+)\\\")?\\s?[/]{0,1}></REPORT>", Pattern.CASE_INSENSITIVE);

    /**
     * Regexp that identifies a complex drilldown anchor.
     */
    public static final Pattern CHART_TAG_PATTERN = Pattern.compile("(<img\\s+src=\"" + CHART_SOURCE_PREFIX_REGEXP +
            "[^>]+?/>)|(<img\\s+src=\"" + CHART_SOURCE_PREFIX_REGEXP + "[^>]+?>.*</img>)", Pattern.CASE_INSENSITIVE);
    public static final Pattern DRILLDOWN_TAG_PATTERN = Pattern.compile("(<a\\s+href=\"drilldown\\?reportName=[^>]+?/>)|(<a\\s+href=\"drilldown\\?reportName=[^>]+?>.*</a>)", Pattern.CASE_INSENSITIVE);

    /**
     * Generates a temporary drilldown component id based on actual ids.
     *
     * @param actualIds Used ids
     * @return A generated id
     */
    public static Integer generateDrilldownId(Set<Integer> actualIds) {
        Integer maxId = 0;
        for (Integer id : actualIds) {
            if (maxId < id) {
                maxId = id;
            }
        }
        return maxId + 1;
    }

    /**
     * This method parses a HTML tag.
     * Each (i.e. <code>href</code>) tag that has been identified is later
     * transformed to a report generation link or drilldown. The parameters it contains are passed
     * to an {@link com.vaadin.ui.Embedded} instance holding the report.
     *
     * @param tag       A HTML tag
     * @param spanStyle <code>TRUE</code> to include span styles
     * @return A pair of tag text and the parameters
     * @throws Exception On XML exception
     */
    public static Pair<String, Map<String, String>> parseHtmlTag(String tag, boolean spanStyle) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = db.parse(new ByteArrayInputStream(tag.getBytes()));
        Element el = doc.getDocumentElement();
        String text = el.getTextContent();
        Map<String, String> params = new HashMap<String, String>();
        fillAttributes(el, params);
        if (spanStyle) {
            NodeList children = el.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element child = (Element) children.item(i);
                    if ("span".equalsIgnoreCase(child.getNodeName())) {
                        fillAttributes(child, params);
                    }
                }
            }
        }
        return new Pair<String, Map<String, String>>(text != null ? text : "", params);
    }

    /**
     * Just appends element attributes to a given map.
     *
     * @param el     XML Element
     * @param params Map of parameters
     */
    private static void fillAttributes(Element el, Map<String, String> params) {
        NamedNodeMap map = el.getAttributes();
        for (int i = 0; i < map.getLength(); ++i) {
            Node node = map.item(i);
            params.put(node.getNodeName(), node.getNodeValue());
        }
    }

    /**
     * This method parses a <code>href</code> HTML tag's parameters.
     * These are used as a report generation request parameters when a drilldown link was clicked.
     *
     * @param hyperlink Input hyperlink
     * @return Map of request parameters
     * @throws UnsupportedEncodingException On encoding error
     */
    public static Map<String, List<String>> parseHyperlinkParameters(String hyperlink) throws UnsupportedEncodingException {
        Map<String, List<String>> params = new HashMap<String, List<String>>();
        String[] urlParts = hyperlink.split("\\?");
        if (urlParts.length > 1) {
            String query = urlParts[1];
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                String key = URLDecoder.decode(pair[0], "UTF-8");
                String value = URLDecoder.decode(pair[1], "UTF-8");
                List<String> values = params.get(key);
                if (values == null) {
                    values = new ArrayList<String>();
                    params.put(key, values);
                }
                values.add(value);
            }
        }
        return params;
    }

    /**
     * Parses the input HTML and extracts the <code>report</code> tag. These are used as report config ids.
     *
     * @param template Input HTML
     * @return A set of identified report configs
     */
    public static Set<Integer> getReportConfigIds(String template) {
        final Set<Integer> configIds = new HashSet<Integer>();
        executeTemplateMatcherWithList(template, REPORT_TAG_PATTERN, new MatchHandlerWithList() {
            @Override
            public void handleMatch(int start, int end, List<String> matches) {
                if (matches != null) {
                    if (matches.size() > 0) {
                        configIds.add(Integer.parseInt(matches.get(0)));
                    }
                    if (matches.size() > 2 && matches.get(2) != null) {
                        configIds.add(Integer.parseInt(matches.get(2)));
                    }
                }
            }
        });
        return configIds;
    }

    /**
     * Searches a given template for a regexp pattern. If a match was found the result
     * is passed to a {@link MatchHandler} instance. Extracts only 1 group.
     *
     * @param template Input template
     * @param pattern  Search pattern
     * @param handler  Match handler
     */
    public static void executeTemplateMatcher(String template, Pattern pattern, MatchHandler handler) {
        if (template != null && !template.isEmpty()) {
            Matcher m = pattern.matcher(template);
            while (m.find()) {
                String match = null;
                if (m.start(0) < m.end(0)) {
                    match = m.group(1);
                    if (match == null) {
                        match = m.group(0);
                    }
                }
                handler.handleMatch(m.start(0), m.end(0), match);
            }
        }
    }

    /**
     * Searches a given template for a regexp pattern. If a match was found the resulting groups
     * are added to a list. The list is passed to given {@link MatchHandlerWithList} instance.
     *
     * @param template Input template
     * @param pattern  Search pattern
     * @param handler  Match handler
     */
    public static void executeTemplateMatcherWithList(String template, Pattern pattern, MatchHandlerWithList handler) {
        if (template != null && !template.isEmpty()) {
            Matcher m = pattern.matcher(template);
            while (m.find()) {
                List<String> matches = new LinkedList<String>();
                if (m.start(0) < m.end(0)) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        if (m.group(i) != null) {
                            matches.add(m.group(i));
                        }
                    }
                }
                handler.handleMatch(m.start(0), m.end(0), matches);
            }
        }
    }

    /**
     * Extracts the report data from given {@link org.apertereports.model.ReportOrder} instance.
     *
     * @param reportOrder Input report order
     * @param format      Output format
     * @return Bytes of a generated report
     * @throws org.apertereports.common.exception.ReportException
     *          On JRExporter error
     * @throws net.sf.jasperreports.engine.JRException
     *          On JasperPrint load error
     */
    public static byte[] exportReportOrderData(ReportOrder reportOrder, ReportType format) throws ARException {
        JasperPrint jasperPrint;
		try {
			jasperPrint = (JasperPrint) JRLoader.loadObject(
			        new ByteArrayInputStream(Base64.decodeBase64(reportOrder.getReportResult())));
		} catch (JRException e) {
			throw new ARException(e);
		}
        return ReportMaster.exportReport(jasperPrint, format.toString(), org.apertereports.dao.utils.ConfigurationCache.getConfiguration());
    }

    /**
     * Handles single group match.
     */
    public abstract static class MatchHandler {
        public abstract void handleMatch(int start, int end, String match);
    }

    /**
     * Handles multiple groups match.
     */
    public abstract static class MatchHandlerWithList {
        public abstract void handleMatch(int start, int end, List<String> match);
    }
}
