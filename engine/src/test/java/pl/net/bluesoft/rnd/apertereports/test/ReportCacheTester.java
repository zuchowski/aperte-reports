/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.test;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import org.junit.Test;

import pl.net.bluesoft.rnd.apertereports.engine.AperteReport;
import pl.net.bluesoft.rnd.apertereports.engine.ReportCache;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author MW
 */
public class ReportCacheTester {

    private String defaultTestReport = "/test vries 44.jrxml";

    /**
     * Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportCache#getReport(java.lang.String)}
     * . Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportCache#putReport(java.lang.String, net.sf.jasperreports.engine.JasperReport)}
     * . Test method for
     * {@link pl.net.bluesoft.rnd.apertereports.engine.ReportCache#removeReport(java.lang.String)}
     * .
     *
     * @throws IOException
     * @throws JRException
     */
    @Test
    public final void testGetReport() throws IOException, JRException {
        String ds = readTestFileToString(defaultTestReport);
        ByteArrayInputStream contentInputStream = new ByteArrayInputStream(ds.getBytes());
        AperteReport testReport = new AperteReport(JasperCompileManager.compileReport(contentInputStream));
        String report1 = "1";
        String report2 = "2";

        /* check if empty */
        assertNull(ReportCache.getReport(report1));

        /* add item and check if it's there */
        ReportCache.putReport(report1, testReport);
        assertEquals(ReportCache.getReport(report1), testReport);

        /* check if other items are empty */
        assertNull(ReportCache.getReport(report2));

        /* add another item and check if it's there */
        ReportCache.putReport(report2, testReport);
        assertEquals(ReportCache.getReport(report2), testReport);

        /* remove item and check if it's empty */
        ReportCache.removeReport(report1);
        assertNull(ReportCache.getReport(report1));

        /* check if other items are unaffected */
        assertEquals(ReportCache.getReport(report2), testReport);

    }

    /**
     * @return
     * @throws IOException
     */
    private String readTestFileToString(String path) throws IOException {
        StringBuffer ds = new StringBuffer();
        InputStream s = getClass().getResourceAsStream(path);
        int c = 0;
        while ((c = s.read()) >= 0) {
            ds.append((char) c);
        }
        return ds.toString();
    }
}
