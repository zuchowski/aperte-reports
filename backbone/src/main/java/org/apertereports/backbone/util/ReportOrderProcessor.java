package org.apertereports.backbone.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for processing reports by JMS listeners.
 */
public class ReportOrderProcessor {

    private static Logger logger = LoggerFactory.getLogger(ReportOrderProcessor.class);
    /**
     * An instance of this class.
     */
    private static final ReportOrderProcessor instance = new ReportOrderProcessor();

    /**
     * Gets a singleton instance of this class.
     *
     * @return A ReportOrderProcessor instance
     */
    public static ReportOrderProcessor getInstance() {
        return instance;
    }

    /**
     * Processes given report order so that generated jasper report is attached
     * to the persistent instance of this report order.
     *
     * @param reportOrder A processed report order
     * @throws AperteReportsException on JasperReports error
     */
    public void processReport(ReportOrder reportOrder) throws ARException {
        reportOrder.setStartDate(Calendar.getInstance());
        reportOrder.setReportStatus(ReportOrder.Status.PROCESSING);
        org.apertereports.dao.ReportOrderDAO.saveOrUpdate(reportOrder);

        ReportTemplate reportTemplate = reportOrder.getReport();

        Map<String, String> parametersMap = XmlReportConfigLoader.getInstance().xmlAsMap(reportOrder.getParametersXml());

        try {
            ReportMaster rm = new ReportMaster(reportTemplate.getContent(),
                    reportTemplate.getId().toString(), new ReportTemplateProvider());

            byte[] reportData = rm.generateAndExportReport(reportOrder.getOutputFormat(),
                    new HashMap<String, Object>(parametersMap),
                    ConfigurationCache.getConfiguration());

            reportOrder.setReportResult(ReportGeneratorUtils.encodeContent(reportData));
            reportOrder.setFinishDate(Calendar.getInstance());
            reportOrder.setReportStatus(ReportOrder.Status.SUCCEEDED);
            org.apertereports.dao.ReportOrderDAO.saveOrUpdate(reportOrder);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ARException(e);
        }
    }
}
