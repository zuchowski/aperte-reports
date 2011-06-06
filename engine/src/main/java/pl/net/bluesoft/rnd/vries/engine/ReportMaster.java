package pl.net.bluesoft.rnd.vries.engine;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import pl.net.bluesoft.rnd.vries.exception.ReportException;
import pl.net.bluesoft.rnd.vries.util.ConfigurationConstants;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.Constants.ErrorCodes;
import pl.net.bluesoft.rnd.vries.util.Constants.Keys;
import pl.net.bluesoft.rnd.vries.util.Constants.Parameter;
import pl.net.bluesoft.rnd.vries.util.Constants.ReportType;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * A workhorse of the Jasper reports engine. This class is responsible for generating, exporting and converting JRXMLs to
 * {@link JasperPrint}s and later to expected formats.
 * <p/>
 * <p>The static methods of this class should be used to convert the report to whatever format one wants.
 * <p>In order to maintain the report generation from a template one should create a new instance of this class.
 */
public class ReportMaster {
    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * Currently processed Jasper report.
     */
    private JasperReport report;

    private static final String FIELD_DELIMITER = ";";

    private static final String RECORD_DELIMITER = "\n\r";

    /**
     * Exports a {@link JasperPrint} to a desired format. The method also takes a <code>customParameters</code> param.
     * These are included unconditionally in the exporter instance used to generate the report.
     * <p/>
     * <p>Currently the only configuration parameter that can be handled is {@link ConfigurationConstants#JASPER_REPORTS_CHARACTER_ENCODING} -
     * the character encoding of the output report.
     *
     * @param jasperPrint      A {@link JasperPrint}
     * @param format           Desired output format (i.e. PDF, HTML etc)
     * @param customParameters Additional custom {@link JRExporterParameter} map
     * @param configuration    Configuration parameters
     * @return Bytes of a generated report
     * @throws ReportException on Jasper error
     */
    public static byte[] exportReport(JasperPrint jasperPrint, String format, Map<JRExporterParameter, Object> customParameters,
                                      Map<String, String> configuration) throws ReportException {
        try {
            ReportType outputFormat = ReportType.valueOf(StringUtils.upperCase(format));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JRExporter exporter;

            String characterEncoding = configuration.get(ConfigurationConstants.JASPER_REPORTS_CHARACTER_ENCODING);
            if (StringUtils.isEmpty(characterEncoding)) {
                characterEncoding = "Cp1250";
            }

            if (outputFormat == ReportType.PDF) {
                exporter = new JRPdfExporter();
                exporter.setParameter(JRPdfExporterParameter.CHARACTER_ENCODING, characterEncoding);
            }
            else if (outputFormat == ReportType.HTML) {
                exporter = new JRHtmlExporter();
                exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
                exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
            }
            else if (outputFormat == ReportType.XLS) {
                exporter = new JRXlsExporter();
                exporter.setParameter(JRXlsExporterParameter.CHARACTER_ENCODING, characterEncoding);
                exporter.setParameter(JRXlsExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_DETECT_CELL_TYPE, Boolean.TRUE);
                exporter.setParameter(JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
            }
            else if (outputFormat == ReportType.CSV) {
                exporter = new JRCsvExporter();
                exporter.setParameter(JRCsvExporterParameter.CHARACTER_ENCODING, characterEncoding);
                exporter.setParameter(JRCsvExporterParameter.RECORD_DELIMITER, RECORD_DELIMITER);
                exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER, FIELD_DELIMITER);
            }
            else {
                String message = "Invalid report type. Permitted types are: HTML, PDF, XLS, CSV";
                throw new ReportException(ErrorCodes.INVALID_REPORT_TYPE, message);
            }

            if (customParameters != null && !customParameters.isEmpty()) {
                for (Iterator<Map.Entry<JRExporterParameter, Object>> it = customParameters.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<JRExporterParameter, Object> entry = it.next();
                    exporter.setParameter(entry.getKey(), entry.getValue());
                }
            }

            ArrayList<JasperPrint> list = new ArrayList<JasperPrint>();
            list.add(jasperPrint);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
            exporter.exportReport();

            return bos.toByteArray();
        }
        catch (JRException e) {
            throw new ReportException(ErrorCodes.JASPER_REPORTS_EXCEPTION, e.getMessage(), e);
        }
    }

    /**
     * Exports a report to a desired format. Omits the custom parameters.
     *
     * @param jasperPrint   A {@link JasperPrint}
     * @param format        Desired output format (i.e. PDF, HTML etc)
     * @param configuration Configuration parameters
     * @return Bytes of a generated report
     * @throws ReportException on Jasper error
     * @see #exportReport(net.sf.jasperreports.engine.JasperPrint, String, java.util.Map, java.util.Map)
     */
    public static byte[] exportReport(JasperPrint jasperPrint, String format, Map<String, String> configuration) throws ReportException {
        return exportReport(jasperPrint, format, null, configuration);
    }

    /**
     * Decodes an input string from Base64 UTF-8 to a {@link ByteArrayInputStream}.
     *
     * @param content A string
     * @return An input stream
     */
    private static ByteArrayInputStream getContentInputStream(String content) {
        try {
            return new ByteArrayInputStream(Base64.decodeBase64(content.getBytes("UTF-8")));
        }
        catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compiles an input JRXML string report source to a {@link JasperPrint}. Uses a {@link ReportCache} to cache the compilation.
     *
     * @param reportSource A JRXML string source
     * @param cacheId      Report cache id
     * @return Compiled report
     * @throws JRException on Jasper error
     */
    public static JasperReport compileReport(String reportSource, Integer cacheId) throws JRException {
        JasperReport compiledReport = ReportCache.getReport(cacheId);
        if (compiledReport == null) {
            ByteArrayInputStream contentInputStream = getContentInputStream(reportSource);
            compiledReport = JasperCompileManager.compileReport(contentInputStream);
            ReportCache.putReport(cacheId, compiledReport);
        }
        return compiledReport;
    }

    /**
     * Constructs a new ReportMaster with a given {@link JasperReport}.
     *
     * @param report A {@link JasperReport}
     */
    public ReportMaster(JasperReport report) {
        super();
        this.report = report;
    }

    /**
     * Creates a new ReportMaster instance that omits the {@link ReportCache} and compiles the report from source directly.
     *
     * @param reportSource A JRXML report source
     * @throws JRException on Jasper error
     */
    public ReportMaster(String reportSource) throws JRException {
        this(reportSource, null);
    }

    /**
     * Creates a new ReportMaster instance that checks the cache for a compiled version of this report.
     * If the compiled report is not found, it creates it using a given JRXML report source.
     *
     * @param reportSource A JRXML report source
     * @param cacheId      A report cache id
     * @throws JRException on Jasper error
     */
    public ReportMaster(String reportSource, Integer cacheId) throws JRException {
        super();
        report = compileReport(reportSource, cacheId);
    }

    /**
     * Generates and exports a report to the desired format from the source passed as a constructor parameter.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @param parameters    Report parameters
     * @param format        Output format
     * @param configuration Exporter configuration
     * @return Bytes of a generated report
     */
    public byte[] generateAndExportReport(Map<String, String> parameters, String format, Map<String, String> configuration) {
        try {
            JasperPrint jasperPrint = generateReport(parameters);
            return exportReport(jasperPrint, format, configuration);
        }
        catch (ReportException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Generates a {@link JasperPrint} using given parameters from the source passed as a constructor parameter.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @param parameters Report parameters
     * @return Output JasperPrint
     */
    public JasperPrint generateReport(Map<String, String> parameters) {
        try {
            JasperPrint jasperPrint = buildJasperPrint(parameters);
            return jasperPrint;
        }
        catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Gets a list of report parameters derived from the compiled Jasper report.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @return a list of {@link ReportParameter}
     */
    public List<ReportParameter> getParameters() {
        if (report == null) {
            logger.error("No active report configuration");
            return null;
        }
        JRParameter[] parameters = report.getParameters();
        List<ReportParameter> outputList = new ArrayList<ReportParameter>();
        for (JRParameter parameter : parameters) {
            ReportParameter outputParameter = new ReportParameter();
            outputParameter.setName(parameter.getName());

            JRPropertiesMap propertiesMap = parameter.getPropertiesMap();
            if (propertiesMap == null || !propertiesMap.hasProperties()) {
                continue;
            }

            String[] propertyNames = propertiesMap.getPropertyNames();
            Map<Keys, ReportProperty> outputProperties = new HashMap<Keys, ReportProperty>(propertyNames.length, 1);

            for (String propertyName : propertyNames) {
                try {
                    Keys key = Keys.valueOf(StringUtils.upperCase(propertyName));
                    ReportProperty property = new ReportProperty(key, propertiesMap.getProperty(propertyName));
                    outputProperties.put(key, property);
                }
                catch (IllegalArgumentException e) {
                    logger.debug("unknown property: " + propertyName);
                }
            }
            outputParameter.setProperties(outputProperties);
            outputList.add(outputParameter);
        }
        return outputList;
    }

    /**
     * Gets current report's name.
     *
     * @return The name of the report
     */
    public String getReportName() {
        return report.getName();
    }

    /**
     * Builds a new {@link JasperPrint} of the current report using given parameters.
     *
     * @param parameters Input report parameters
     * @return A {@link JasperPrint}
     * @throws JRException     on Jasper error
     * @throws NamingException on errors while accessing the initial context
     * @throws SQLException    on errors while accessing a configured datasource
     */
    private JasperPrint buildJasperPrint(Map<String, String> parameters) throws JRException, NamingException, SQLException {
        JasperPrint jasperPrint = null;

        parameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, Constants.DATETIME_PATTERN);
        injectDefaultValues(parameters);

        Map<String, Object> parameters1 = new HashMap<String, Object>(parameters.size(), 1);
        parameters1.putAll(parameters);
        parameters1.put(JRParameter.REPORT_LOCALE, new Locale("pl", "PL"));

        Connection connection = null;
        try {
            connection = getConnectionToDatasource(report);
            if (connection == null) {
                Collection tmp = new ArrayList();
                tmp.add(parameters1);
                jasperPrint = JasperFillManager.fillReport(report, parameters1, new JRMapCollectionDataSource(tmp));
            }
            else {
                jasperPrint = JasperFillManager.fillReport(report, parameters1, connection);
            }
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
        return jasperPrint;
    }

    /**
     * Returns a connection to the configured datasource if the right parameter is found.
     *
     * @param jasperReport Input jasper report
     * @return The connection to the datasource
     * @throws NamingException on errors while accessing the initial context
     * @throws SQLException    on errors while connecting to the datasource
     */
    private Connection getConnectionToDatasource(JasperReport jasperReport) throws NamingException, SQLException {
        JRParameter[] parameters = jasperReport.getParameters();
        Connection con = null;
        for (JRParameter parameter : parameters) {
            if (parameter.getName().equalsIgnoreCase(Parameter.DATASOURCE.toString())) {
                InitialContext ic;
                ic = new InitialContext();
                DataSource ds;
                String jndiName = parameter.getDescription();
                try {
                    ds = (DataSource) ic.lookup(jndiName);
                }
                catch (Exception e1) {
                    String prefix = "java:comp/env/";
                    jndiName = jndiName.matches(prefix + ".*") ? jndiName.substring(prefix.length()) : prefix + jndiName;
                    ds = (DataSource) new InitialContext().lookup(jndiName);
                }
                if (ds == null) {
                    throw new RuntimeException("Unable to find JNDI resource in the initial context: " + jndiName);
                }
                con = ds.getConnection();
            }
        }
        return con;
    }

    /**
     * Injects default values to parameters that have not been filled.
     *
     * @param parameters Input parameters
     */
    private void injectDefaultValues(Map<String, String> parameters) {
        JRParameter[] originalParameters = report.getParameters();
        for (JRParameter parameter : originalParameters) {
            if (parameters.containsKey(parameter.getName()) && StringUtils.isEmpty(parameters.get(parameter.getName()))
                    && parameter.getDefaultValueExpression() != null) {
                String defaultValue = parameter.getDefaultValueExpression().getText();
                if (defaultValue.matches("\".*\"")) {
                    defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                }
                parameters.put(parameter.getName(), defaultValue);
            }
        }
    }

}
