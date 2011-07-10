package pl.net.bluesoft.rnd.apertereports.mdb;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.util.JRSaver;
import org.apache.commons.codec.binary.Base64;
import pl.net.bluesoft.rnd.apertereports.dao.ReportOrderDAO;
import pl.net.bluesoft.rnd.apertereports.data.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.apertereports.exception.VriesException;
import pl.net.bluesoft.rnd.apertereports.util.DeCoder;
import pl.net.bluesoft.rnd.apertereports.util.ExceptionUtil;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
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
        ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);

        ReportTemplate reportTemplate = reportOrder.getReport();

        Map<String, String> parametersMap = DeCoder.deserializeParameters(reportOrder.getParametersXml());

        try {
            ReportMaster reportMaster = new ReportMaster(String.valueOf(reportTemplate.getContent()),
                    reportTemplate.getId());
            JasperPrint jasperPrint = reportMaster.generateReport(parametersMap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            JRSaver.saveObject(jasperPrint, baos);
            reportOrder.setReportResult(String.valueOf(Base64.encodeBase64(baos.toByteArray())).toCharArray());
            reportOrder.setFinishDate(Calendar.getInstance());
            reportOrder.setReportStatus(ReportOrder.Status.SUCCEEDED);
            ReportOrderDAO.saveOrUpdateReportOrder(reportOrder);
        }
        catch (JRException e) {
            ExceptionUtil.logSevereException(e);
            throw new VriesException(e);
        }
    }
}
