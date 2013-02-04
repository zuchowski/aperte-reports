/**
 *
 */
package org.apertereports.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.codec.binary.Base64;
import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.utils.TextUtils;
import org.apertereports.engine.AperteReport;
import org.apertereports.engine.EmptySubreportProvider;
import org.apertereports.engine.MapBasedSubreportProvider;
import org.apertereports.engine.ReportCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.engine.ReportParameter;
import org.apertereports.engine.SubreportProvider;
import org.apertereports.engine.SubreportProvider.Subreport;
import org.junit.Test;

/**
 * @author Think
 */
public class ReportMasterTester {

	private String defaultTestReport = "/test vries 44.jrxml";

	private String malformedTestReport = "/test vries 44_malformed.jrxml";

	private String reportWithSubreport = "/report1.jrxml";
	private String subreport1_ID = "report1_subreport1";
	private String subreport1 = "/report1_subreport1.jrxml";
	private String subsubreport1_ID = "report1_subreport1_subreport1";
	private String subsubreport1 = "/report1_subreport1_subreport1.jrxml";
	private String subsubreport2_ID = "report1_subreport1_subreport2";
	private String subsubreport2 = "/report1_subreport1_subreport2.jrxml";

	/**
	 *
	 * @throws IOException
	 * @throws SubreportNotFoundException
	 * @throws JRException
	 */
	@Test
	public final void testCompileMalformedReport() throws Exception {
		String ds = readTestFileToString(defaultTestReport);
		ds = toBase64(ds);
		AperteReport report = ReportMaster.compileReport(ds, null, new EmptySubreportProvider());
		assertNotNull(report);
	}

	/**
	 * Test method for
	 * {@link org.apertereports.engine.ReportMaster#compileReport(String, String, SubreportProvider)}
	 * .
	 * 
	 * @throws IOException
	 * @throws JRException
	 * @throws SubreportNotFoundException
	 * @throws ReportException
	 */
	@Test
	public final void testCompileReport() throws Exception {
		try {
			String ds = readTestFileToString(malformedTestReport);
			ds = toBase64(ds);
			AperteReport report = ReportMaster.compileReport(ds, null, new EmptySubreportProvider());
			fail("Should throw exception during compilation");
		} catch (AperteReportsException e) {
			e.printStackTrace();
			// fine - should throw this exception
		}
	}

	/**
	 * Test method for
	 * {@link org.apertereports.engine.ReportMaster#getReportName()}
	 * .
	 * 
	 * @throws JRException
	 * @throws IOException
	 * @throws SubreportNotFoundException
	 */
	@Test
	public final void testGetReportName() throws Exception {
		String name = "test apertereports 1";
		ReportMaster rm = createReportMaster(defaultTestReport);
		assertEquals(name, rm.getReportName());
	}

	/**
	 * Test method for
	 * {@link org.apertereports.engine.ReportMaster#compileReport(String, String, SubreportProvider)}
	 * .
	 * 
	 * @throws IOException
	 * @throws JRException
	 * @throws SubreportNotFoundException
	 * @throws ReportException
	 */
	@Test
	public final void testCompileReportWithSubreports() throws Exception {
		final Map<String, Subreport> map = new HashMap<String, Subreport>();
		map.put(subreport1_ID, new Subreport(subreport1_ID, subreport1_ID, readTestFileToString(subreport1).getBytes()));
		map.put(subsubreport1_ID, new Subreport(subsubreport1_ID, subsubreport1_ID, readTestFileToString(subsubreport1)
				.getBytes()));
		map.put(subsubreport2_ID, new Subreport(subsubreport2_ID, subsubreport2_ID, readTestFileToString(subsubreport2)
				.getBytes()));

		String ds = toBase64(readTestFileToString(reportWithSubreport));
		AperteReport report = ReportMaster.compileReport(ds, null, new MapBasedSubreportProvider(map));
		assertEquals(3, report.getAllNestedSubreports().size());
	}

	/**
	 *
	 * @throws SubreportNotFoundException
	 * 
	 * @throws JRException
	 */
	@Test
	public final void testReportMasterString() throws Exception  {
		createReportMaster(defaultTestReport);
	}

	/**
	 * Test method for
	 * {@link org.apertereports.engine.ReportMaster#compileReport(String, String, SubreportProvider)}
	 * .
	 * 
	 * @throws IOException
	 * @throws JRException
	 * @throws SubreportNotFoundException
	 */
	@Test
	public final void testCompileReportWithMissingSubreports() throws Exception {
		final Map<String, Subreport> map = new HashMap<String, Subreport>();
		map.put(subreport1_ID, new Subreport(subreport1_ID, subreport1_ID, readTestFileToString(subreport1).getBytes()));
		map.put(subsubreport1_ID, new Subreport(subsubreport1_ID, subsubreport1_ID, readTestFileToString(subsubreport1)
				.getBytes()));

		try {
			String ds = toBase64(readTestFileToString(reportWithSubreport));
			ReportMaster.compileReport(ds, null, new MapBasedSubreportProvider(map));
			fail("Should have failed");
		} catch (AperteReportsException e) {
			Assert.assertEquals(ErrorCodes.SUBREPORT_NOT_FOUND, e.getErrorCode());
			// Great - subreport not found
		}
	}

	private ReportMaster createReportMaster(String path) throws AperteReportsException, IOException {
		String ds = readTestFileToString(path);
		ds = toBase64(ds);
		return new ReportMaster(ds, new EmptySubreportProvider());
	}

	/**
	 * Test method for
	 * {@link org.apertereports.engine.ReportMaster#compileReport(String, String, SubreportProvider)}
	 *
	 * @throws IOException
	 * @throws JRException
	 * @throws ReportException
	 * @throws SubreportNotFoundException
	 */
	@Test
	public final void testCompileAndGenerateAndExportReportWithSubreports() throws Exception {
		final Map<String, Subreport> map = new HashMap<String, Subreport>();

		map.put(subreport1_ID, new Subreport(subreport1_ID, subreport1_ID, readTestFileToString(subreport1).getBytes()));
		map.put(subsubreport1_ID, new Subreport(subsubreport1_ID, subsubreport1_ID, readTestFileToString(subsubreport1)
				.getBytes()));
		map.put(subsubreport2_ID, new Subreport(subsubreport2_ID, subsubreport2_ID, readTestFileToString(subsubreport2)
				.getBytes()));

		String ds = toBase64(readTestFileToString(reportWithSubreport));
		ReportMaster rm = new ReportMaster(ds, new MapBasedSubreportProvider(map));
		byte[] result = rm.generateAndExportReport(ReportType.PDF.name(), new HashMap<String, Object>(), null);
		String strFilePath = "src/test/resources/result.pdf";
		try {
			FileOutputStream fos = new FileOutputStream(strFilePath);
			fos.write(result);
			fos.close();

		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException : " + ex);
			fail();
		} catch (IOException ioe) {
			System.out.println("IOException : " + ioe);
			fail();
		}
		// assertEquals(3, report.getAllNestedSubreports().size());

	}

	@Test
	public final void testExportReport() throws Exception {
		// fail("Not yet implemented"); // xxx
	}

	/**
	 * @throws JRException
	 * @throws IOException
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
	 * {@link org.apertereports.engine.ReportMaster#getParameters()}
	 * .
	 * 
	 * @throws JRException
	 * @throws IOException
	 * @throws SubreportNotFoundException
	 */
	@Test
	public final void testGetParameters() throws Exception {
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
	 *
	 * @throws JRException
	 * @throws IOException
	 * @throws SubreportNotFoundException
	 */
	@Test
	public final void testReportMasterStringInteger() throws Exception {
		String ds = readTestFileToString(defaultTestReport);
		ds = toBase64(ds);
		String cacheId = "5";

		assertNull(ReportCache.getReport(cacheId));
		new ReportMaster(ds, cacheId, new EmptySubreportProvider());
		assertNotNull(ReportCache.getReport(cacheId));
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
