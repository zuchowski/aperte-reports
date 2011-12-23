package org.apertereports.engine;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.query.JRXPathQueryExecuterFactory;
import org.springframework.util.StringUtils;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.exception.ReportException;
import org.apertereports.common.xml.ws.ReportData;
import org.apertereports.common.xml.ws.ReportExporterParameter;
import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.ReportConstants.Parameter;
import org.apertereports.common.utils.ReportGeneratorUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ReportWebServiceHelper {
    private static final Logger logger = Logger.getLogger(ReportWebServiceHelper.class.getName());

    public byte[] generateAndExportReport(ReportData reportData) throws ReportException {
        Map<JRExporterParameter, Object> exporterParameters = getExporterParameters(reportData);
        Map<String, String> configuration = getJasperConfiguration(reportData);
        Map<String, Object> reportParameters = getReportParameters(reportData);

        byte[] content;
        try {
            content = ReportGeneratorUtils.unwrapDataHandler(reportData.getSource());
            if (content.length == 0) {
                throw new ReportException(ErrorCodes.JASPER_REPORTS_EXCEPTION, "Report source cannot be empty for report: "
                        + (reportData.getName() != null ? reportData.getName() : reportData.getId()));
            }
            content = new ReportMaster(content, reportData.getId(), new EmptySubreportProvider())
                    .generateAndExportReport(reportData.getFormat(), reportParameters, exporterParameters, configuration);
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            throw new ReportException(ErrorCodes.JASPER_REPORTS_EXCEPTION, "Exception while generating report: "
                    + (reportData.getName() != null ? reportData.getName() : reportData.getId())
                    + ". Detailed message: " + e.getMessage());
        }

        return content;
    }

    private Map<String, Object> getReportParameters(ReportData reportData) throws ReportException {
        Map<String, Object> reportParameters = new HashMap<String, Object>();
        try {
            for (org.apertereports.common.xml.ws.ReportParameter param : reportData.getReportParameters()) {
                Object object = ReportGeneratorUtils.deserializeObject(param.getValue());
                if (ReportGeneratorUtils.canConvertToInputStreams(object)) {
                    object = ReportGeneratorUtils.convertBytesToInputStreams(object);
                }
                reportParameters.put(param.getName(), object);
            }
        }
        catch (Exception e) {
            logger.info(e.getMessage());
            throw new ReportException(ErrorCodes.SERIALIZATION_EXCEPTION, "Unable to deserialize report parameters", e);
        }
        if (!reportParameters.containsKey(JRXPathQueryExecuterFactory.XML_DATE_PATTERN)) {
            reportParameters.put(JRXPathQueryExecuterFactory.XML_DATE_PATTERN, ReportConstants.DATETIME_PATTERN);
        }
        if (StringUtils.hasText(reportData.getLocale())) {
            reportParameters.put(JRParameter.REPORT_LOCALE, reportData.getLocale());
        }
        return reportParameters;
    }

    private Map<String, String> getJasperConfiguration(ReportData reportData) {
        Map<String, String> configuration = new HashMap<String, String>();
        if (StringUtils.hasText(reportData.getCharacterEncoding())) {
            configuration.put(ConfigurationConstants.JASPER_REPORTS_CHARACTER_ENCODING, reportData.getCharacterEncoding().trim());
        }
        if (StringUtils.hasText(reportData.getDataSource())) {
            configuration.put(Parameter.DATASOURCE.name(), reportData.getDataSource());
        }
        return configuration;
    }

    private Map<JRExporterParameter, Object> getExporterParameters(ReportData reportData) throws ReportException {
        Map<JRExporterParameter, Object> exporterParameters = new HashMap<JRExporterParameter, Object>();
        for (ReportExporterParameter param : reportData.getExporterParameters()) {
            try {
                Object obj = ReportGeneratorUtils.resolveFieldValue(getClass().getClassLoader(), param.getClassName(), param.getFieldName());
                exporterParameters.put((JRExporterParameter) obj, param.getValue());
            }
            catch (Exception e) {
                logger.info(e.getMessage());
                throw new ReportException(ErrorCodes.INVALID_EXPORTER_PARAMETER, "Unable to determine an exporter parameter: "
                        + param.getFieldName() + " of type: " + param.getClassName(), e);
            }
        }
        return exporterParameters;
    }
}
