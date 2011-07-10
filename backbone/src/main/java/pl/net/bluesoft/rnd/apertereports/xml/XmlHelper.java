package pl.net.bluesoft.rnd.apertereports.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class that provides convenient methods for transforming the XML form of report parameters
 * to collections (lists, maps) and vice versa.
 * <p>The XML representation is marshaled and unmarshaled using JAXB 2.0.
 */
public final class XmlHelper {
    private XmlHelper() {
    }

    private static Marshaller marshaller = null;
    private static Unmarshaller unmarshaller = null;

    /**
     * Initializes the marshaller and unmarshaller.
     *
     * @throws JAXBException on JAXB error
     */
    private static void init() throws JAXBException {
        if (marshaller == null || unmarshaller == null) {
            JAXBContext context = JAXBContext.newInstance(ReportConfigRoot.class, CyclicReportConfig.class);
            marshaller = context.createMarshaller();
            unmarshaller = context.createUnmarshaller();
        }
    }

    /**
     * Converts a list of {@link ReportConfig} to a single string.
     * Basically reverts the result of {@link #stringAsReportConfigs(String)}.
     *
     * @param reportConfigs A list of {@link ReportConfig}
     * @return The XML representation converted to a string
     * @throws JAXBException on JAXB error
     * @see ReportConfig
     */
    public static String reportConfigsAsString(List<ReportConfig> reportConfigs) throws JAXBException {
        init();

        StringWriter sw = new StringWriter();
        if (reportConfigs != null && !reportConfigs.isEmpty()) {
            marshaller.marshal(new ReportConfigRoot(reportConfigs), sw);
        }

        return sw.toString();
    }

    /**
     * Converts a string containing XML to a list of {@link ReportConfig}.
     * Basically reverts the result of {@link #reportConfigsAsString(java.util.List)}.
     *
     * @param input
     * @return
     * @throws JAXBException
     */
    public static List<ReportConfig> stringAsReportConfigs(String input) throws JAXBException {
        init();

        ReportConfigRoot root = null;
        if (input != null && input.trim().length() > 0) {
            root = (ReportConfigRoot) unmarshaller.unmarshal(new StringReader(input));
        }

        return root != null ? root.getReportConfigs() : new ArrayList<ReportConfig>();
    }

    /**
     * Converts a list of {@link ReportConfigParameter} to a single string.
     * Basically reverts the result of {@link #xmlAsParameters(String)}.
     *
     * @param parameters A list of {@link ReportConfigParameter}
     * @return The XML representation converted to a string
     * @throws JAXBException on JAXB error
     * @see ReportConfigParameter
     */
    public static String parametersAsXML(List<ReportConfigParameter> parameters) throws JAXBException {
        init();

        StringWriter sw = new StringWriter();
        if (parameters != null && !parameters.isEmpty()) {
            marshaller.marshal(new CyclicReportConfig(parameters), sw);
        }

        return sw.toString();
    }

    /**
     * Converts a string containing XML to a list of {@link ReportConfigParameter}.
     * Basically reverts the result of {@link #parametersAsXML(java.util.List)}.
     *
     * @param xml A string with XML
     * @return A list of {@link ReportConfigParameter}
     * @throws JAXBException on JAXB error
     * @see ReportConfigParameter
     */
    public static List<ReportConfigParameter> xmlAsParameters(String xml) throws JAXBException {
        init();

        CyclicReportConfig root = null;
        if (xml != null && xml.trim().length() > 0) {
            root = (CyclicReportConfig) unmarshaller.unmarshal(new StringReader(xml));
        }

        return root != null ? root.getParameters() : new ArrayList<ReportConfigParameter>();
    }

    /**
     * Converts a string containing XML representation of {@link ReportConfigParameter} to a map.
     * The key of the map is the parameter's name. The value is {@link pl.net.bluesoft.rnd.apertereports.xml.ReportConfigParameter#getValue()}.
     * Reverts the result of {@link #mapAsXml(java.util.Map)}.
     *
     * @param xml A string with XML
     * @return A map of parameters
     * @throws JAXBException on JAXB error
     */
    public static Map<String, String> xmlAsMap(String xml) throws JAXBException {
        return parameterListToMap(xmlAsParameters(xml));
    }

    /**
     * Converts a map of parameters to a marshaled string containing XML representation of a list of {@link ReportConfigParameter}.
     * Reverts the result of {@link #xmlAsMap(String)}.
     *
     * @param map A map of parameters
     * @return A string with XML
     * @throws JAXBException on JAXB error
     */
    public static String mapAsXml(Map<String, String> map) throws JAXBException {
        return parametersAsXML(mapToParameterList(map));
    }

    /**
     * Converts a map of parameters to a list of {@link ReportConfigParameter}.
     * Reverts the result of {@link #parameterListToMap(java.util.List)}.
     *
     * @param map A map of parameters
     * @return A list of {@link ReportConfigParameter}
     */
    public static List<ReportConfigParameter> mapToParameterList(Map<String, String> map) {
        List<ReportConfigParameter> list = new ArrayList<ReportConfigParameter>(map.values().size());
        for (String key : map.keySet()) {
            ReportConfigParameter p = new ReportConfigParameter();
            p.setName(key);
            p.setValue(map.get(key));
            list.add(p);
        }
        return list;
    }

    /**
     * Converts a list of {@link ReportConfigParameter} to a map.
     * Reverts the result of {@link #mapToParameterList(java.util.Map)}.
     *
     * @param params A list of parameters
     * @return A map of parameters
     */
    public static Map<String, String> parameterListToMap(List<ReportConfigParameter> params) {
        Map<String, String> map = new HashMap<String, String>();
        if (params != null && !params.isEmpty()) {
            for (ReportConfigParameter config : params) {
                map.put(config.getName(), config.getValue());
            }
        }
        return map;
    }
}
