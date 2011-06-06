package pl.net.bluesoft.rnd.vries.test;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import org.apache.commons.codec.binary.Base64;
import org.junit.BeforeClass;
import org.junit.Test;
import pl.net.bluesoft.rnd.vries.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.vries.data.ReportTemplate;
import pl.net.bluesoft.rnd.vries.engine.ReportMaster;
import pl.net.bluesoft.rnd.vries.exception.ReportException;
import pl.net.bluesoft.rnd.vries.util.Constants;
import pl.net.bluesoft.rnd.vries.util.ExceptionUtil;
import pl.net.bluesoft.rnd.vries.util.TestUtil;
import pl.net.bluesoft.rnd.vries.util.TextUtil;

import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class EngineTest {

    @BeforeClass
    public static void initDB() throws NamingException, IllegalStateException, IOException {
        TestUtil.initDB();
    }

    private String outputPath = "/tmp/jasper/";
    private String imagesPath = outputPath + "images/";

    private void generateReport(Constants.ReportType type, String path, boolean htmlChart) throws IOException, JRException, ReportException {
        String reportText = TextUtil.readTestFileToString(getClass().getResourceAsStream(path));
        String reportData = String.valueOf(Base64.encodeBase64(reportText.getBytes("UTF-8")));
        JasperReport report = ReportMaster.compileReport(reportData, null);
        ReportMaster rm = new ReportMaster(report);
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("r_max_order_id", "'10300'");

        File output = new File(imagesPath);
        if (!output.exists()) {
            output.mkdirs();
        }
        output = new File(outputPath);
        if (!output.exists()) {
            output.mkdirs();
        }

        JasperPrint print = rm.generateReport(parameters);
        byte[] result;
        if (htmlChart) {
            result = exportReport(print);
        }
        else {
            result = ReportMaster.exportReport(print, type.name(), new HashMap<String, String>());
        }

        File file = new File(outputPath + "jasperprint_" + System.currentTimeMillis() + "." + type.name());
        if (file.createNewFile()) {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(result);
            fos.close();
        }
        ExceptionUtil.logDebug("File written: " + file.getPath());
    }

    private byte[] exportReport(JasperPrint jasperPrint) throws ReportException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            JRExporter exporter = new JRHtmlExporter();
            exporter.setParameter(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, Boolean.FALSE);
            exporter.setParameter(JRHtmlExporterParameter.IGNORE_PAGE_MARGINS, Boolean.TRUE);
            exporter.setParameter(JRHtmlExporterParameter.IMAGES_DIR_NAME, imagesPath);
            exporter.setParameter(JRHtmlExporterParameter.IMAGES_URI, imagesPath);
            exporter.setParameter(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);

            ArrayList<JasperPrint> list = new ArrayList<JasperPrint>();
            list.add(jasperPrint);
            exporter.setParameter(JRExporterParameter.JASPER_PRINT_LIST, list);
            exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, bos);
            exporter.exportReport();

            return bos.toByteArray();
        }
        catch (JRException e) {
            ExceptionUtil.logSevereException(e);
            throw new ReportException(Constants.ErrorCodes.JASPER_REPORTS_EXCEPTION, e.getMessage(), e);
        }
    }

    @Test
    public void testEngine() throws Exception {
        Collection<ReportTemplate> reports = ReportTemplateDAO.fetchAllReports(true);

        StringBuffer sb = new StringBuffer();
        for (ReportTemplate rt : reports) {
            try {
                ReportMaster rm = new ReportMaster(String.valueOf(rt.getContent()), rt.getId());
                sb.append(String.valueOf(rm.generateAndExportReport(new HashMap<String, String>(), Constants.ReportType.HTML.name(),
                        new HashMap<String, String>())));
                if (!sb.toString().isEmpty()) {
                    break;
                }
            }
            catch (Exception e) {
                ExceptionUtil.logSevereException(e);
            }
        }

        ExceptionUtil.logDebug(sb.toString());
    }

}
