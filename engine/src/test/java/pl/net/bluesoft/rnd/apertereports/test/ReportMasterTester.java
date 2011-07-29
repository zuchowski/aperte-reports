/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.test;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import org.apache.commons.codec.binary.Base64;
import org.junit.Test;
import pl.net.bluesoft.rnd.apertereports.common.utils.TextUtils;
import pl.net.bluesoft.rnd.apertereports.engine.ReportCache;
import pl.net.bluesoft.rnd.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.apertereports.engine.ReportParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Think
 */
public class ReportMasterTester {

    private String defaultTestReport = "/test vries 44.jrxml";

    private String malformedTestReport = "/test vries 44_malformed.jrxml";

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#compileReport(String, String)}
     * .
     *
     * @throws IOException
     * @throws Base64DecodingException
     * @throws JRException
     */
    @Test
    public final void testCompileMalformedReport() throws IOException, JRException {
        String ds = readTestFileToString(defaultTestReport);
        ds = toBase64(ds);
        JasperReport report = ReportMaster.compileReport(ds, null);
        assertNotNull(report);
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#compileReport(String, String)}
     * .
     *
     * @throws IOException
     * @throws Base64DecodingException
     * @throws JRException
     */
    @Test
    public final void testCompileReport() throws IOException, JRException {
        try {
            String ds = readTestFileToString(malformedTestReport);
            ds = toBase64(ds);
            JasperReport report = ReportMaster.compileReport(ds, null);
            fail("Should throw exception during compilation");
        }
        catch (JRException e) {
            // fine - should throw this exception
        }
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#exportReport(net.sf.jasperreports.engine.JasperPrint, java.lang.String)}
     * .
     */
    @Test
    public final void testExportReport() throws Exception {
        // fail("Not yet implemented"); // TODO
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#generateReport(java.util.Map, java.lang.String)}
     * .
     *
     * @throws JRException
     * @throws IOException
     * @throws Base64DecodingException
     */
    @Test
    public final void testGenerateReport() throws IOException, JRException {
        // ReportMaster rm = createReportMaster(defaultTestReport);
        // JasperPrint report = rm.generateReport(new HashMap<String,
        // String>());
        // assertNotNull("Report not generated", report);
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#getParameters()}.
     *
     * @throws JRException
     * @throws IOException
     * @throws Base64DecodingException
     */
    @Test
    public final void testGetParameters() throws IOException, JRException {
        ReportMaster rm = createReportMaster(defaultTestReport);
        List<ReportParameter> parameters = rm.getParameters();
        assertNotNull("No parameters fetched", parameters);
        Object parameters_size = 10;
        Object properties_size = 3;
        assertEquals("Wrong number of parameters: " + parameters.size(), parameters.size(), parameters_size);
        assertNotNull("First parameter is null", parameters.get(0));
        assertNotNull("First parameter has no properties", parameters.get(0).getProperties());
        assertEquals("Wrong number of properties for first parameter: ", parameters.get(0).getProperties().size(),
                properties_size);
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#getReportName()}.
     *
     * @throws JRException
     * @throws IOException
     * @throws Base64DecodingException
     */
    @Test
    public final void testGetReportName() throws IOException, JRException {
        String name = "test apertereports 1";
        ReportMaster rm = createReportMaster(defaultTestReport);
        assertEquals(name, rm.getReportName());
    }

    // /**
    // * Test method for
    // * {@link
    // pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#ReportMaster(net.sf.jasperreports.engine.JasperReport)}
    // * .
    // */
    // @Test
    // public final void testReportMasterJasperReport() {
    // // fail("Not yet implemented"); // TODO
    // }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#ReportMaster(java.lang.String)}
     * .
     *
     * @throws JRException
     * @throws Base64DecodingException
     */
    @Test
    public final void testReportMasterString() throws IOException, JRException {
        createReportMaster(defaultTestReport);
    }

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportMaster#ReportMaster(java.lang.String, java.lang.Integer)}
     * .
     *
     * @throws JRException
     * @throws IOException
     * @throws Base64DecodingException
     */
    @Test
    public final void testReportMasterStringInteger() throws IOException, JRException {
        String ds = readTestFileToString(defaultTestReport);
        ds = toBase64(ds);
        String cacheId = "2";

        assertNull(ReportCache.getReport(cacheId));
        new ReportMaster(ds, cacheId);
        assertNotNull(ReportCache.getReport(cacheId));
    }

    private ReportMaster createReportMaster(String path) throws IOException, JRException {
        String ds = readTestFileToString(path);
        ds = toBase64(ds);
        return new ReportMaster(ds);
    }

    /**
     * @return
     * @throws IOException
     */
    private String readTestFileToString(String path) throws IOException {
        return TextUtils.readTestFileToString(getClass().getResourceAsStream(path));
    }

    private String toBase64(String source) throws UnsupportedEncodingException {
        return new String(Base64.encodeBase64(source.getBytes("UTF-8")));

    }

}
