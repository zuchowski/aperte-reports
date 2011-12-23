package org.apertereports.backbone.util;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSaver;
import org.apertereports.common.exception.VriesException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class for processing reports by JMS listeners.
 */
public class ReportOrderProcessor {
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
     * Processes given report order so that generated jasper report is attached to the persistent instance
     * of this report order.
     *
     * @param reportOrder A processed report order
     * @throws VriesException on JasperReports error
     */
    public void processReport(ReportOrder reportOrder) throws VriesException {
        reportOrder.setStartDate(Calendar.getInstance());
        reportOrder.setReportStatus(ReportOrder.Status.PROCESSING);
        org.apertereports.dao.ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);

        ReportTemplate reportTemplate = reportOrder.getReport();

        Map<String, String> parametersMap = XmlReportConfigLoader.getInstance().xmlAsMap(reportOrder.getParametersXml());

        try {
            ReportMaster reportMaster = new ReportMaster(reportTemplate.getContent(),
                    reportTemplate.getId().toString(), new ReportTemplateProvider());
            JasperPrint jasperPrint = reportMaster.generateReport(new HashMap<String, Object>(parametersMap));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRSaver.saveObject(jasperPrint, baos);
            reportOrder.setReportResult(ReportGeneratorUtils.encodeContent(baos.toByteArray()));
            reportOrder.setFinishDate(Calendar.getInstance());
            reportOrder.setReportStatus(ReportOrder.Status.SUCCEEDED);
            org.apertereports.dao.ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);
        }        
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw new VriesException(e);
        }
    }
}
