package pl.net.bluesoft.rnd.apertereports.engine;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.*;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.common.ConfigurationConstants;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.exception.ReportException;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.common.utils.ReportGeneratorUtils;
import pl.net.bluesoft.util.lang.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * A workhorse of the Jasper reports engine. This class is responsible for generating, exporting and converting JRXMLs to
 * {@link JasperPrint}s and later to expected formats.
 * <p/>
 * <p>The static methods of this class should be used to convert the report to whatever format one wants.
 * <p>In order to maintain the report generation from a template one should create a new instance of this class.
 */
public class ReportMaster implements ReportConstants, ConfigurationConstants {
    private static final Logger logger = Logger.getLogger(ReportMaster.class.getName());

    /**
     * Currently processed Jasper report.
     */
    private JasperReport report;

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
    public ReportMaster(String reportSource, String cacheId) throws JRException {
        super();
        report = compileReport(reportSource, cacheId);
    }

    /**
     * Creates a new ReportMaster instance that checks the cache for a compiled version of this report.
     * If the compiled report is not found, it creates it using a given bytes of a JRXML report source.
     *
     * @param reportSource Bytes of a JRXML report source
     * @param cacheId      A report cache id
     * @throws JRException on Jasper error
     */
    public ReportMaster(byte[] reportSource, String cacheId) throws JRException {
        super();
        report = compileReport(reportSource, cacheId);
    }

    /**
     * Exports a {@link JasperPrint} to a desired format. The method also takes a <code>customParameters</code> param.
     * These are included unconditionally in the exporter instance used to generate the report.
     * <p/>
     * <p>Currently the only configuration parameter that can be handled is {@link pl.net.bluesoft.rnd.apertereports.common.ConfigurationConstants#JASPER_REPORTS_CHARACTER_ENCODING} -
     * the character encoding of the output report.
     *
     * @param jasperPrint      A {@link JasperPrint}
     * @param format           Desired output format (i.e. PDF, HTML etc)
     * @param exporterParameters Additional custom {@link JRExporterParameter} map
     * @param configuration    Configuration parameters
     * @return Bytes of a generated report
     * @throws ReportException on Jasper error
     */
    public static byte[] exportReport(JasperPrint jasperPrint, String format, Map<JRExporterParameter, Object> exporterParameters,
                                      Map<String, String> configuration) throws ReportException {
        try {
            ReportType outputFormat = ReportType.valueOf(StringUtils.upperCase(format));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JRExporter exporter;

            String characterEncoding = configuration.get(JASPER_REPORTS_CHARACTER_ENCODING);
            if (!StringUtil.hasText(characterEncoding)) {
                characterEncoding = "Cp1250";
                logger.info("Injecting default character encoding: " + characterEncoding);
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
                logger.info(message);
                throw new ReportException(ErrorCodes.INVALID_REPORT_TYPE, message);
            }

            if (exporterParameters != null && !exporterParameters.isEmpty()) {
                for (Iterator<Map.Entry<JRExporterParameter, Object>> it = exporterParameters.entrySet().iterator(); it.hasNext(); ) {
                    Map.Entry<JRExporterParameter, Object> entry = it.next();
                    exporter.setParameter(entry.getKey(), entry.getValue());
                }
            }

            exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, Collections.singletonList(jasperPrint));
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
            exporter.exportReport();
            return bos.toByteArray();
        }
        catch (JRException e) {
            ExceptionUtils.logSevereException("Error while generating report", e);
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
     * Compiles an input JRXML string report source to a {@link JasperPrint}. Uses a {@link ReportCache} to cache the compilation.
     *
     *
     * @param reportSource A JRXML string source
     * @param cacheId      Report cache id
     * @return Compiled report
     * @throws JRException on Jasper error
     */
    public static JasperReport compileReport(byte[] reportSource, String cacheId) throws JRException {
        logger.info("Trying to fetch report '" + cacheId + "' from cache");
        JasperReport compiledReport = ReportCache.getReport(cacheId);
        if (compiledReport == null) {
            logger.info("Report not found. Compiling...");
            compiledReport = JasperCompileManager.compileReport(new ByteArrayInputStream(reportSource));
            ReportCache.putReport(cacheId, compiledReport);
            logger.info("Finished compiling");
        }
        else {
            logger.info("Report found");
        }
        return compiledReport;
    }

    public static JasperReport compileReport(String reportSource, String cacheId) throws JRException {
        try {
            return compileReport(ReportGeneratorUtils.decodeContent(reportSource), cacheId);
        }
        catch (UnsupportedEncodingException e) {
            ExceptionUtils.logSevereException("Unsupported encoding", e);
            throw new VriesRuntimeException(e.getMessage(), e);
        }
    }

    public byte[] generateAndExportReport(String format, Map<String, Object> reportParameters,
                                          Map<JRExporterParameter, Object> exporterParameters,
                                          Map<String, String> configuration) throws ReportException {
        JasperPrint jasperPrint = generateReport(reportParameters, configuration);
        return exportReport(jasperPrint, format, exporterParameters, configuration);
    }

    /**
     * Generates and exports a report to the desired format from the source passed as a constructor parameter.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @param reportParameters    Report parameters
     * @param format        Output format
     * @param configuration Exporter configuration
     * @return Bytes of a generated report
     */
    public byte[] generateAndExportReport(Map<String, Object> reportParameters, String format,
                                          Map<String, String> configuration) throws ReportException {
        return generateAndExportReport(format, reportParameters, null, configuration);
    }

    /**
     * Generates a {@link JasperPrint} using given parameters from the source passed as a constructor parameter.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @param reportParameters Report parameters
     * @return Output JasperPrint
     */
    public JasperPrint generateReport(Map<String, Object> reportParameters, Map<String, String> configuration) throws ReportException {
        try {
            JasperPrint jasperPrint = buildJasperPrint(reportParameters, configuration);
            return jasperPrint;
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw new ReportException(e);
        }
    }
    public JasperPrint generateReport(Map<String, Object> parameters) {
        try {
            JasperPrint jasperPrint = buildJasperPrint(parameters, new HashMap<String, String>());
            return jasperPrint;
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw new VriesRuntimeException("Exception while generating report", e);
        }
    }

    /**
     * Gets a list of report parameters derived from the compiled Jasper report.
     * Returns <code>null</code> on error. The error is noticed by a {@link Logger} instance.
     *
     * @return a list of {@link ReportParameter}
     */
    public List<ReportParameter> getParameters() {
        if (report == null) {
            logger.info("No active report configuration");
            return null;
        }
        JRParameter[] parameters = report.getParameters();
        List<ReportParameter> outputList = new ArrayList<ReportParameter>();
        for (JRParameter parameter : parameters) {
            ReportParameter outputParameter = new ReportParameter();
            outputParameter.setType(parameter.getValueClassName());
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
                    String msg = "Unknown property: " + propertyName;
                    ExceptionUtils.logSevereException(msg, e);
                    throw new VriesRuntimeException(msg, e);
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
     * @param reportParameters Input report parameters
     * @param configuration Jasper configuration parameters
     * @return A {@link JasperPrint}
     * @throws JRException     on Jasper error
     * @throws NamingException on errors while accessing the initial context
     * @throws SQLException    on errors while accessing a configured datasource
     */
    private JasperPrint buildJasperPrint(Map<String, Object> reportParameters, Map<String, String> configuration)
            throws JRException, NamingException, SQLException {
        logger.info("Starting building jasper print");
        JasperPrint jasperPrint = null;

        injectDefaultValues(reportParameters);

        String dataSource = configuration.get(Parameter.DATASOURCE.name());
        Connection connection = null;
        try {
            connection = dataSource != null ? getConnectionByJNDI(dataSource) : getConnectionFromReport(report);
            jasperPrint = connection != null ? JasperFillManager.fillReport(report, reportParameters, connection)
                    : JasperFillManager.fillReport(report, reportParameters,
                            new JRMapCollectionDataSource(Collections.singletonList(reportParameters)));
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
        logger.info("Finished building jasper print");
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
    private Connection getConnectionFromReport(JasperReport jasperReport) throws NamingException, SQLException {
        logger.info("Getting database connection from report");
        JRParameter[] parameters = jasperReport.getParameters();
        Connection con = null;
        for (JRParameter parameter : parameters) {
            if (parameter.getName().equalsIgnoreCase(Parameter.DATASOURCE.toString())) {
                String jndiName = parameter.getDescription();
                con = getConnectionByJNDI(jndiName);
                break;
            }
        }
        return con;
    }

    /**
     * Tries to lookup a data source from the initial context.
     *
     * @param jndiName Connection JNDI name
     * @return Connection to a datasource
     * @throws NamingException on errors while accessing the initial context
     * @throws SQLException    on errors while connecting to the datasource
     */
    private Connection getConnectionByJNDI(String jndiName) throws NamingException, SQLException {
        logger.info("Getting database connection by JNDI");
        DataSource ds;
        try {
            ds = (DataSource) new InitialContext().lookup(jndiName);
        }
        catch (Exception e1) {
            String prefix = "java:comp/env/"; // possibly tomcat
            if (jndiName.matches(prefix + ".*")) {
                ds = (DataSource) new InitialContext().lookup(jndiName.substring(prefix.length()));
            }
            else {
                ds = (DataSource) new InitialContext().lookup(prefix + jndiName);
            }
        }

        if (ds == null) {
            String message = "Unable to lookup datasource: " + jndiName;
            logger.info(message);
            throw new RuntimeException(message);
        }
        return ds.getConnection();
    }

    /**
     * Injects default values to parameters that have not been filled.
     *
     * @param reportParameters Input parameters
     */
    private void injectDefaultValues(Map<String, Object> reportParameters) {
        JRParameter[] originalParameters = report.getParameters();
        for (JRParameter parameter : originalParameters) {
            if (reportParameters.containsKey(parameter.getName()) && StringUtils.isEmpty(reportParameters.get(parameter.getName()).toString())
                    && parameter.getDefaultValueExpression() != null) {
                String defaultValue = parameter.getDefaultValueExpression().getText();
                if (defaultValue.matches("\".*\"")) {
                    defaultValue = defaultValue.substring(1, defaultValue.length() - 1);
                }
                reportParameters.put(parameter.getName(), defaultValue);
            }
        }
        if (!reportParameters.containsKey(JRXPathQueryExecuterFactory.XML_DATE_PATTERN)) {
            reportParameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, DATETIME_PATTERN);
            logger.info("Injecting default date format: " + DATETIME_PATTERN);
        }

        Object locale = reportParameters.get(JRParameter.REPORT_LOCALE);
        if (locale != null) {
            if (locale instanceof String) {
                String[] val = ((String) locale).split("_");
                reportParameters.put(JRParameter.REPORT_LOCALE, new Locale(val[0], val[1]));
            }
            else if (!(locale instanceof Locale)) {
                locale = null;
            }
        }
        if (locale == null) {
            Locale defaultLocale = new Locale("pl", "PL");
            reportParameters.put(JRParameter.REPORT_LOCALE, defaultLocale);
            logger.info("Unable to find locale parameter. Injecting default locale: " + defaultLocale);
        }
    }
}
