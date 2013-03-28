package org.apertereports.engine;

import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.ARConstants.Parameter;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.ws.ReportData;
import org.apertereports.common.xml.ws.ReportExporterParameter;
import org.springframework.util.StringUtils;

public class ReportWebServiceHelper {

	public byte[] generateAndExportReport(ReportData reportData) throws ARException {
		Map<JRExporterParameter, Object> exporterParameters = getExporterParameters(reportData);
		Map<String, String> configuration = getJasperConfiguration(reportData);
		Map<String, Object> reportParameters = getReportParameters(reportData);

		byte[] content;
		try {
			content = ReportGeneratorUtils.unwrapDataHandler(reportData.getSource());

			content = new ReportMaster(content, reportData.getId(), new EmptySubreportProvider())
					.generateAndExportReport(reportData.getFormat(), reportParameters, exporterParameters,
							configuration);
		} catch (ARException e) {
			throw e;
		} 

		return content;
	}

	private Map<String, Object> getReportParameters(ReportData reportData) throws ARException {
		Map<String, Object> reportParameters = new HashMap<String, Object>();
		try {
			for (org.apertereports.common.xml.ws.ReportParameter param : reportData.getReportParameters()) {
				Object object = ReportGeneratorUtils.deserializeObject(param.getValue());
				if (ReportGeneratorUtils.canConvertToInputStreams(object)) {
					object = ReportGeneratorUtils.convertBytesToInputStreams(object);
				}
				reportParameters.put(param.getName(), object);
			}
		} catch (Exception e) {
			throw new ARException(ErrorCode.SERIALIZATION_EXCEPTION,  e);
		}
		if (!reportParameters.containsKey(JRXPathQueryExecuterFactory.XML_DATE_PATTERN)) {
			reportParameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, ARConstants.DATETIME_PATTERN);
		}
		if (StringUtils.hasText(reportData.getLocale())) {
			reportParameters.put(JRParameter.REPORT_LOCALE, reportData.getLocale());
		}
		return reportParameters;
	}

	private Map<String, String> getJasperConfiguration(ReportData reportData) {
		Map<String, String> configuration = new HashMap<String, String>();
		if (StringUtils.hasText(reportData.getCharacterEncoding())) {
			configuration.put(ConfigurationConstants.JASPER_REPORTS_CHARACTER_ENCODING, reportData
					.getCharacterEncoding().trim());
		}
		if (StringUtils.hasText(reportData.getDataSource())) {
			configuration.put(Parameter.DATASOURCE.name(), reportData.getDataSource());
		}
		return configuration;
	}

	private Map<JRExporterParameter, Object> getExporterParameters(ReportData reportData) throws ARException {
		Map<JRExporterParameter, Object> exporterParameters = new HashMap<JRExporterParameter, Object>();
		for (ReportExporterParameter param : reportData.getExporterParameters()) {
			try {
				Object obj = ReportGeneratorUtils.resolveFieldValue(getClass().getClassLoader(), param.getClassName(),
						param.getFieldName());
				exporterParameters.put((JRExporterParameter) obj, param.getValue());
			} catch (Exception e) {
				throw new ARException(e);
			}
		}
		return exporterParameters;
	}
}
