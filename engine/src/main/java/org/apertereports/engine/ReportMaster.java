package org.apertereports.engine;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporter;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;
import net.sf.jasperreports.engine.JRPropertiesUtil;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.SimpleJasperReportsContext;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRCsvExporterParameter;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.JRXlsExporterParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import net.sf.jasperreports.engine.query.QueryExecuterFactory;
import net.sf.jasperreports.engine.util.JRFontNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.users.User;
import org.apertereports.common.utils.LocaleUtils;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.engine.SubreportProvider.Subreport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.net.bluesoft.util.lang.StringUtil;

import com.jaspersoft.webservice.data.WebServiceDataAdapterService;
import com.jaspersoft.webservice.data.authenitcator.BasicAuthenticator;
import com.jaspersoft.webservice.data.enumerations.AuthType;
import com.jaspersoft.webservice.data.enumerations.LanguageType;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.util.InfrastructureUtil;

/**
 * A workhorse of the Jasper reports engine. This class is responsible for
 * generating, exporting and converting JRXMLs to {@link JasperPrint}s and later
 * to expected formats.
 * <p/>
 * <p>
 * The static methods of this class should be used to convert the report to
 * whatever format one wants.
 * <p>
 * In order to maintain the report generation from a template one should create
 * a new instance of this class.
 */
public class ReportMaster implements ARConstants, ConfigurationConstants {

	private static final Logger logger = LoggerFactory
			.getLogger(ReportMaster.class.getName());
	private static Pattern subreportPattern = Pattern
			.compile("<subreportExpression class\\=\"java\\.lang\\.String\"\\>\\<\\!\\[CDATA\\[\\$P\\{[^}]*\\} [^\"]*\"([^\"]*)\\.jasper\"");
	private static Pattern jasperReportPattern = Pattern.compile(
			"<jasperReport[^>]+>(\\s+<property[^>]+>)*", Pattern.MULTILINE);
	private static Pattern subreportReportElementPattern = Pattern.compile(
			"<subreport>\\s*<reportElement[^>]+>(\\s*)", Pattern.MULTILINE);
	private static String subreportMapParameter = "\n\t<parameter name=\""
			+ SUBREPORT_MAP_PARAMETER_NAME
			+ "\" class=\"java.util.Map\" isForPrompting=\"false\"/>";
	/**
	 * Currently processed Jasper report.
	 */
	private AperteReport report;
	private User user;

	/**
	 * Constructs a new ReportMaster with a given {@link JasperReport}.
	 *
	 * @param report
	 *            A {@link JasperReport}
	 */
	public ReportMaster(AperteReport report) {
		super();
		this.report = report;
	}

	/**
	 * Creates a new ReportMaster instance that omits the {@link ReportCache}
	 * and compiles the report from source directly.
	 *
	 * @param reportSource
	 *            A JRXML report source
	 * @param subreportProvider
	 *            Subreport provider
	 * @throws AperteReportsException
	 *             on error
	 */
	public ReportMaster(String reportSource, SubreportProvider subreportProvider)
			throws ARException {
		this(reportSource, null, subreportProvider);
	}

	/**
	 * Creates a new ReportMaster instance that checks the cache for a compiled
	 * version of this report. If the compiled report is not found, it creates
	 * it using a given JRXML report source.
	 *
	 * @param reportSource
	 *            A JRXML report source
	 * @param cacheId
	 *            A report cache id
	 * @param subreportProvider
	 *            Subreport provider
	 * @throws JRException
	 *             on Jasper error
	 * @throws SubreportNotFoundException
	 */
	public ReportMaster(String reportSource, String cacheId,
			SubreportProvider subreportProvider) throws ARException {
		super();
		report = compileReport(reportSource, cacheId, subreportProvider);
	}

	/**
	 * Creates a new ReportMaster instance that checks the cache for a compiled
	 * version of this report. If the compiled report is not found, it creates
	 * it using a given JRXML report source.
	 *
	 * @param reportSource
	 *            A JRXML report source
	 * @param cacheId
	 *            A report cache id
	 * @param subreportProvider
	 *            Subreport provider
	 * @param user
	 *            User
	 * @throws JRException
	 *             on Jasper error
	 * @throws SubreportNotFoundException
	 */
	public ReportMaster(String reportSource, String cacheId,
			SubreportProvider subreportProvider, User user) throws ARException {
		super();
		if (user != null) {
			this.user = user;
		}
		report = compileReport(reportSource, cacheId, subreportProvider);
	}

	/**
	 * Creates a new ReportMaster instance that checks the cache for a compiled
	 * version of this report. If the compiled report is not found, it creates
	 * it using a given bytes of a JRXML report source.
	 *
	 * @param reportSource
	 *            Bytes of a JRXML report source
	 * @param cacheId
	 *            A report cache id
	 * @param subreportProvider
	 *            Subreport provider
	 * @throws AperteReportsException
	 *             on error
	 */
	public ReportMaster(byte[] reportSource, String cacheId,
			SubreportProvider subreportProvider) throws ARException {
		super();
		report = compileReport(reportSource, cacheId, subreportProvider);
	}

	/**
	 * Exports a {@link JasperPrint} to a desired format. The method also takes
	 * a <code>customParameters</code> param. These are included unconditionally
	 * in the exporter instance used to generate the report.
	 * <p/>
	 * <p>
	 * Currently the only configuration parameter that can be handled is
	 * {@link org.apertereports.common.ConfigurationConstants#JASPER_REPORTS_CHARACTER_ENCODING}
	 * - the character encoding of the output report.
	 *
	 * @param jasperPrint
	 *            A {@link JasperPrint}
	 * @param format
	 *            Desired output format (i.e. PDF, HTML etc)
	 * @param customExporterParams
	 *            Additional custom {@link JRExporterParameter} map
	 * @param configuration
	 *            Configuration parameters
	 * @return Bytes of a generated report
	 * @throws AperteReportsException
	 *             on error
	 */
	public static byte[] exportReport(JasperPrint jasperPrint, String format,
			Map<JRExporterParameter, Object> customExporterParams,
			Map<String, String> configuration) throws ARException {

		if (configuration == null) {
			configuration = new HashMap<String, String>();
		}
		if (customExporterParams == null) {
			customExporterParams = new HashMap<JRExporterParameter, Object>();
		}

		try {
			ReportType outputFormat = ReportType.valueOf(StringUtils
					.upperCase(format));
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			JRExporter exporter;

			String characterEncoding = configuration
					.get(JASPER_REPORTS_CHARACTER_ENCODING);
			if (!StringUtil.hasText(characterEncoding)) {
				characterEncoding = "Cp1250";
				logger.info("Injecting default character encoding: "
						+ characterEncoding);
			}

			if (outputFormat == ReportType.PDF) {
				exporter = new JRPdfExporter();
				exporter.setParameter(
						JRPdfExporterParameter.CHARACTER_ENCODING,
						characterEncoding);
			} else if (outputFormat == ReportType.HTML) {
				exporter = new JRHtmlExporter();
				exporter.setParameter(
						JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN,
						Boolean.FALSE);
				exporter.setParameter(
						JRHtmlExporterParameter.IGNORE_PAGE_MARGINS,
						Boolean.TRUE);
			} else if (outputFormat == ReportType.XLS) {
				exporter = new JRXlsExporter();
				exporter.setParameter(
						JRXlsExporterParameter.CHARACTER_ENCODING,
						characterEncoding);
				exporter.setParameter(
						JRXlsExporterParameter.IGNORE_PAGE_MARGINS,
						Boolean.TRUE);
				exporter.setParameter(
						JRXlsExporterParameter.IS_DETECT_CELL_TYPE,
						Boolean.TRUE);
				exporter.setParameter(
						JRXlsExporterParameter.IS_WHITE_PAGE_BACKGROUND, false);
			} else if (outputFormat == ReportType.CSV) {
				exporter = new JRCsvExporter();
				exporter.setParameter(
						JRCsvExporterParameter.CHARACTER_ENCODING,
						characterEncoding);
				exporter.setParameter(JRCsvExporterParameter.RECORD_DELIMITER,
						RECORD_DELIMITER);
				exporter.setParameter(JRCsvExporterParameter.FIELD_DELIMITER,
						FIELD_DELIMITER);
			} else {
				throw new IllegalStateException(
						"Invalid report type. Permitted types are: HTML, PDF, XLS, CSV");
			}

			if (customExporterParams != null && !customExporterParams.isEmpty()) {
				for (Iterator<Map.Entry<JRExporterParameter, Object>> it = customExporterParams
						.entrySet().iterator(); it.hasNext();) {
					Map.Entry<JRExporterParameter, Object> entry = it.next();
					exporter.setParameter(entry.getKey(), entry.getValue());
				}
			}

			exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST,
					Collections.singletonList(jasperPrint));
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
			exporter.exportReport();
			return bos.toByteArray();
		} catch (JRException e) {
			throw new ARException(ErrorCode.JASPER_REPORTS_EXCEPTION, e);
		}
	}

	/**
	 * Exports a report to a desired format. Omits the custom parameters.
	 *
	 * @param jasperPrint
	 *            A {@link JasperPrint}
	 * @param format
	 *            Desired output format (i.e. PDF, HTML etc)
	 * @param configuration
	 *            Configuration parameters
	 * @return Bytes of a generated report
	 * @throws AperteReportsException
	 *             on error
	 * @see #exportReport(net.sf.jasperreports.engine.JasperPrint, String,
	 *      java.util.Map, java.util.Map)
	 */
	public static byte[] exportReport(JasperPrint jasperPrint, String format,
			Map<String, String> configuration) throws ARException {
		return exportReport(jasperPrint, format, null, configuration);
	}

	/**
	 * Compiles an input JRXML string report source to a {@link JasperPrint}.
	 * Uses a {@link ReportCache} to cache the compilation.
	 *
	 *
	 * @param reportSource
	 *            A JRXML string source
	 * @param cacheId
	 *            Report cache id
	 * @param subreportProvider
	 * @return Compiled report
	 * @throws JRException
	 *             on Jasper error
	 * @throws SubreportNotFoundException
	 */
	public static AperteReport compileReport(byte[] reportSource,
			String cacheId, SubreportProvider subreportProvider)
			throws ARException {
		return compileReport(reportSource, cacheId, subreportProvider, false);
	}

	private static AperteReport compileReport(byte[] reportSource,
			String cacheId, SubreportProvider subreportProvider,
			boolean hasParent) throws ARException {
		logger.info("Trying to fetch report '" + cacheId + "' from cache");
		AperteReport compiledReport = ReportCache.getReport(cacheId);
		Set<String> subreportNames = new HashSet<String>();
		if (compiledReport == null) {
			logger.info("Report not found. Compiling...");

			String source = processSubreports(hasParent, new String(
					reportSource), subreportNames);
			ByteArrayInputStream bis = new ByteArrayInputStream(
					source.getBytes());
			try {
				JRPropertiesUtil util = JRPropertiesUtil
						.getInstance(new SimpleJasperReportsContext());
				util.setProperty(
						QueryExecuterFactory.QUERY_EXECUTER_FACTORY_PREFIX
								+ "WebServiceQuery",
						"com.jaspersoft.webservice.data.query.WebServiceQueryExecuter");
				compiledReport = new AperteReport(
						JasperCompileManager.compileReport(bis));
				logger.info("Compiled.");
			} catch (JRException e) {
				logger.error("report source exception", e);
				throw new ARException(ErrorCode.REPORT_SOURCE_EXCEPTION, e);
			}
		} else {
			logger.info("Report found");

			subreportNames.addAll(compiledReport.getSubreports().keySet());
			compiledReport.getSubreports().clear();
		}
		compileSubreports(subreportProvider, compiledReport, subreportNames);

		ReportCache.putReport(cacheId, compiledReport);

		return compiledReport;
	}

	private static void compileSubreports(SubreportProvider subreportProvider,
			AperteReport compiledReport, Set<String> subreportNames)
			throws ARException {
		if (subreportNames.size() > 0) {
			if (subreportProvider == null) {
				subreportProvider = new EmptySubreportProvider();
			}
			Map<String, Subreport> subreports = subreportProvider
					.getSubreports((String[]) subreportNames
							.toArray(new String[subreportNames.size()]));
			Map<String, AperteReport> compiledSubreports = new HashMap<String, AperteReport>(
					subreports.size(), 1);
			for (Subreport subreport : subreports.values()) {
				AperteReport compiledSubreport = compileReport(
						subreport.getContent(), subreport.getCacheId(),
						subreportProvider, true);
				compiledSubreports.put(subreport.getName(), compiledSubreport);
			}
			compiledReport.setSubreports(compiledSubreports);
		}
	}

	public static AperteReport compileReport(String reportSource,
			String cacheId, SubreportProvider subreportProvider)
			throws ARException {
		try {
			return compileReport(
					ReportGeneratorUtils.decodeContent(reportSource), cacheId,
					subreportProvider);
		} catch (UnsupportedEncodingException e) {
			throw new ARRuntimeException(ErrorCode.UNSUPPORTED_ENCODING, e);
		}
	}

	/**
	 * TODO: move ids to constants/ properties
	 * 
	 * @return {@link byte[]}
	 */
	public byte[] generateAndExportReport(String format,
			Map<String, Object> reportParameters,
			Map<JRExporterParameter, Object> exporterParameters,
			Map<String, String> configuration) throws ARException {
		if (user != null) {
			long userid = user.getUserid();
			long groupid = user.getGroupid();
			long companyid = user.getCompanyid();

			reportParameters.put("userId", userid);
			reportParameters.put("groupId", groupid);
			reportParameters.put("companyid", companyid);
			reportParameters.put("p_auth", user.getContext().get("p_auth"));

			reportParameters.put(WebServiceDataAdapterService.URI_KEY,
					"http://steag.localhost:8080");
			// for (String key : reportParameters.keySet()) {
			// System.out.print("Key: " + key + " - ");
			// System.out.print("Value: " + reportParameters.get(key) + "\n");
			// }
		}
		JasperPrint jasperPrint = generateReport(reportParameters,
				configuration);
		return exportReport(jasperPrint, format, exporterParameters,
				configuration);
	}

	/**
	 * Generates and exports a report to the desired format from the source
	 * passed as a constructor parameter. Returns <code>null</code> on error.
	 * The error is noticed by a {@link Logger} instance.
	 *
	 *
	 * @param format
	 *            Output format
	 * @param reportParameters
	 *            Report parameters
	 * @param configuration
	 *            Exporter configuration
	 * @return Bytes of a generated report
	 */
	public byte[] generateAndExportReport(String format,
			Map<String, Object> reportParameters,
			Map<String, String> configuration) throws ARException {

		return generateAndExportReport(format, reportParameters, null,
				configuration);
	}

	private static String processSubreports(boolean hasParent, String source,
			Set<String> subreportNames) {
		Matcher m = subreportPattern.matcher(source);
		while (m.find()) {
			String subReportName = m.group(1);
			subreportNames.add(subReportName);
			source = m
					.replaceFirst("<subreportExpression class=\"net.sf.jasperreports.engine.JasperReport\"><![CDATA[\\$P{"
							+ SUBREPORT_MAP_PARAMETER_NAME
							+ "}.get(\""
							+ subReportName + "\").getJasperReport()");
			m.reset(source);
		}
		m = subreportReportElementPattern.matcher(source);

		if (m.find()) {
			source = m.replaceAll("$0<subreportParameter name=\""
					+ SUBREPORT_MAP_PARAMETER_NAME + "\">"
					+ "<subreportParameterExpression>" + "<![CDATA[\\$P{"
					+ SUBREPORT_MAP_PARAMETER_NAME
					+ "}]]></subreportParameterExpression>"
					+ "</subreportParameter>$1");
		}

		logger.info(subreportNames.size() + " subreports found");

		if (subreportNames.size() > 0 || hasParent) {
			m = jasperReportPattern.matcher(source);
			m.find();
			source = m.replaceFirst(m.group() + subreportMapParameter);
		}
		return source;
	}

	/**
	 * Generates a {@link JasperPrint} using given parameters from the source
	 * passed as a constructor parameter. Returns <code>null</code> on error.
	 * The error is noticed by a {@link Logger} instance.
	 *
	 * @param reportParameters
	 *            Report parameters
	 * @param configuration
	 *            Configuration, can be null
	 * @return Output JasperPrint
	 */
	private JasperPrint generateReport(Map<String, Object> reportParameters,
			Map<String, String> configuration) throws ARException {
		try {
			JasperPrint jasperPrint = buildJasperPrint(reportParameters,
					configuration);
			return jasperPrint;
		} catch (JRFontNotFoundException e) {
			throw new ARException(ErrorCode.FONT_NOT_FOUND, e);
		} catch (Exception e) {
			logger.error("Unknown error", e);
			throw new ARException(e);
		}
	}

	public JasperPrint generateReport(Map<String, Object> reportParameters)
			throws ARException {
		return generateReport(reportParameters, null);
	}

	/**
	 * Gets a list of report parameters derived from the compiled Jasper report.
	 * Returns <code>null</code> on error. The error is noticed by a
	 * {@link Logger} instance.
	 *
	 * @return a list of {@link ReportParameter}
	 */
	public List<ReportParameter> getParameters() {
		if (report == null) {
			logger.info("No active report configuration");
			return null;
		}
		JRParameter[] parameters = getJasperReport().getParameters();
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
			Map<Keys, ReportProperty> outputProperties = new HashMap<Keys, ReportProperty>(
					propertyNames.length, 1);
			for (String propertyName : propertyNames) {
				try {
					Keys key = Keys
							.valueOf(StringUtils.upperCase(propertyName));
					ReportProperty property = new ReportProperty(key,
							propertiesMap.getProperty(propertyName));
					outputProperties.put(key, property);
				} catch (IllegalArgumentException e) {
					throw new ARRuntimeException(
							ErrorCode.UNKNOWN_PROPERTY_NAME, e, propertyName);
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
		return getJasperReport().getName();
	}

	public AperteReport getAperteReport() {
		return report;
	}

	private DataSource ds;

	/**
	 * Builds a new {@link JasperPrint} of the current report using given
	 * parameters.
	 *
	 * @param reportParameters
	 *            Input report parameters
	 * @param configuration
	 *            Jasper configuration parameters, can be null
	 * @return A {@link JasperPrint}
	 * @throws JRException
	 *             on Jasper error
	 * @throws NamingException
	 *             on errors while accessing the initial context
	 * @throws SQLException
	 *             on errors while accessing a configured datasource
	 * @throws ClassNotFoundException
	 */
	private JasperPrint buildJasperPrint(Map<String, Object> reportParameters,
			Map<String, String> configuration) throws JRException,
			NamingException, SQLException, ClassNotFoundException {

		// previously data source was passed as a method argument, but it was
		// not used nowhere
		// xxx dataSource condition
		// Object dataSource = null;

		if (configuration == null) {
			configuration = new HashMap<String, String>();
		}
		if (reportParameters == null) {
			reportParameters = new HashMap<String, Object>();
		}

		logger.info("Starting building jasper print");
		JasperPrint jasperPrint = null;

		injectDefaultValues(reportParameters);

		if (report.getSubreports().size() > 0) {
			reportParameters.put(SUBREPORT_MAP_PARAMETER_NAME,
					report.getAllNestedSubreports());
		}

		Connection connection = null;
		try {
			String lang = getJasperReport().getQuery().getLanguage();
			if (lang.equals("SQL")) {
				// if (dataSource == null) {
				String jndiDataSource = configuration.get(Parameter.DATASOURCE
						.name());
				connection = jndiDataSource != null ? getConnectionByJNDI(jndiDataSource)
						: getConnectionFromReport(getJasperReport());

				if (connection != null) {
					jasperPrint = JasperFillManager.fillReport(
							getJasperReport(), reportParameters, connection);
				} else {
					Collection<Map<String, ?>> rParameters = new LinkedList<Map<String, ?>>();
					rParameters.add(reportParameters);
					jasperPrint = JasperFillManager.fillReport(
							getJasperReport(), reportParameters,
							new JRMapCollectionDataSource(rParameters));
				}

			} else if (lang.equals("WebServiceQuery")) {

				reportParameters.put(WebServiceDataAdapterService.VERB_KEY,
						"post");
				reportParameters.put(WebServiceDataAdapterService.LANGUAGE_KEY,
						LanguageType.JSON);

				reportParameters.put("parameter_properties",
						parameterProperties(getJasperReport().getParameters()));

				Map<String, String> authenticationMap = new HashMap<String, String>();

				// Not Needed for RESTJSONService
				reportParameters.put(
						WebServiceDataAdapterService.AUTH_TYPE_KEY,
						AuthType.BASIC.toString());
				authenticationMap.put(BasicAuthenticator.USERNAME_KEY,
						"aperteuser");
				authenticationMap
						.put(BasicAuthenticator.PASSWORD_KEY, "aperte");
				reportParameters.put(
						WebServiceDataAdapterService.AUTH_PARAMETERS_KEY,
						authenticationMap);

				jasperPrint = JasperFillManager.fillReport(getJasperReport(),
						reportParameters);
			} else {
				logger.error("Unsupported Query Language!");
			}

		} finally {
			if (connection != null) {
				connection.close();
			}

		}
		logger.info("Finished building jasper print");
		return jasperPrint;
	}

	/**
	 * 
	 * Returns parameter properties as escaped JSON-String Example: [{\
	 * "bis\":[{\"input_type\":\"date\",\"label\":\"bis\"}],\"von\":[{\"input_typ
	 * e \ " : \ " d a t e \ " , \ " l a b e l \ " : \ " v o n \ " } ] } ]
	 * 
	 * @param {@link JRParameter[]} jRParameters
	 * 
	 * 
	 * @return {@link Object}
	 * @history MW-ITSD;20.12.2016
	 */
	private Object parameterProperties(JRParameter[] jRParameters) {
		JSONObject jsonParameter = JSONFactoryUtil.createJSONObject();
		for (JRParameter parameter : jRParameters) {

			if (!parameter.getPropertiesMap().isEmpty()) {
				JSONArray parameters = JSONFactoryUtil.createJSONArray();

				String parameterName = parameter.getName();
				jsonParameter.put(parameterName, parameters);

				JSONObject properties = JSONFactoryUtil.createJSONObject();
				for (String name : parameter.getPropertiesMap()
						.getPropertyNames()) {
					String value = parameter.getPropertiesMap().getProperty(
							name);
					properties.put(name, value);

				}
				parameters.put(properties);
			}

		}

		return jsonParameter.toString().replace("\"", "\\\"");
	}

	/**
	 * Returns a connection to the configured datasource if the right parameter
	 * is found.
	 *
	 * @param jasperReport
	 *            Input jasper report
	 * @return The connection to the datasource
	 * @throws NamingException
	 *             on errors while accessing the initial context
	 * @throws SQLException
	 *             on errors while connecting to the datasource
	 * @throws ClassNotFoundException
	 */
	private Connection getConnectionFromReport(JasperReport jasperReport)
			throws NamingException, SQLException, ClassNotFoundException {
		JRParameter[] parameters = jasperReport.getParameters();
		Connection con = null;
		for (JRParameter parameter : parameters) {
			if (parameter.getName().equalsIgnoreCase(
					Parameter.DATASOURCE.name())) {
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
	 * @param jndiName
	 *            Connection JNDI name
	 * @return Connection to a datasource
	 * @throws NamingException
	 *             on errors while accessing the initial context
	 * @throws SQLException
	 *             on errors while connecting to the datasource
	 * @throws ClassNotFoundException
	 */
	private Connection getConnectionByJNDI(String jndiName)
			throws NamingException, SQLException, ClassNotFoundException {
		logger.info("Getting database connection, jndiName: " + jndiName);
		DataSource ds;
		try {
			ds = (DataSource) new InitialContext().lookup(jndiName);
		} catch (Exception e1) {
			String prefix = "java:comp/env/"; // possibly tomcat
			if (jndiName.matches(prefix + ".*")) {
				ds = (DataSource) new InitialContext().lookup(jndiName
						.substring(prefix.length()));
			} else {
				ds = (DataSource) new InitialContext()
						.lookup(prefix + jndiName);
			}
		}
		try {
			Connection c = ds.getConnection();

			return c;
		} catch (Exception e2) {
			/*
			 * String URL = "jdbc:mysql://localhost:3306/social_office_head";
			 * String USER = "root"; String PASS = "admin";
			 * Class.forName("com.mysql.jdbc.Driver"); Connection conn =
			 * DriverManager.getConnection(URL, USER, PASS);
			 */
			DataSource dataSource = InfrastructureUtil.getDataSource();
			Connection conn = dataSource.getConnection();

			if (ds == null) {
				String message = "Unable to lookup datasource: " + jndiName;
				logger.info(message);
				throw new RuntimeException(message);
			}
			return conn;
		}

		// return c;

	}

	/**
	 * Injects default values to parameters that have not been filled.
	 *
	 * @param reportParameters
	 *            Input parameters
	 */
	private void injectDefaultValues(Map<String, Object> reportParameters) {
		JRParameter[] originalParameters = getJasperReport().getParameters();
		for (JRParameter parameter : originalParameters) {

			if (reportParameters.containsKey(parameter.getName())
					&& StringUtils.isEmpty(reportParameters.get(
							parameter.getName()).toString())
					&& parameter.getDefaultValueExpression() != null) {
				String defaultValue = parameter.getDefaultValueExpression()
						.getText();
				if (defaultValue.matches("\".*\"")) {
					defaultValue = defaultValue.substring(1,
							defaultValue.length() - 1);
				}
				reportParameters.put(parameter.getName(), defaultValue);
			}
		}

		if (!reportParameters
				.containsKey(JRXPathQueryExecuterFactory.XML_DATE_PATTERN)) {
			reportParameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN,
					DATETIME_PATTERN);
			logger.info("Injecting default date format: " + DATETIME_PATTERN);
		}

		Object locale = reportParameters.get(JRParameter.REPORT_LOCALE);
		if (locale != null) {
			if (locale instanceof String) {
				locale = LocaleUtils.createLocale((String) locale);
			} else if (!(locale instanceof Locale)) {
				locale = null;
			}
		}
		if (locale == null) {
			Locale defaultLocale = Locale.getDefault();
			logger.info("Unable to find locale parameter. Injecting default locale: "
					+ defaultLocale);
		}
		reportParameters.put(JRParameter.REPORT_LOCALE, locale);
	}

	private JasperReport getJasperReport() {
		return report.getJasperReport();
	}
}