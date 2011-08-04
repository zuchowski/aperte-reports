package pl.net.bluesoft.rnd.apertereports.backbone.tests;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import pl.net.bluesoft.rnd.apertereports.backbone.util.ReportTemplateProvider;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.engine.SubreportNotFoundException;
import pl.net.bluesoft.rnd.apertereports.engine.SubreportProvider.Subreport;
import pl.net.bluesoft.rnd.apertereports.model.ReportTemplate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/testEnvContext.xml" })
public class ReportTemplateProviderTest{
	private static final String NONEXISTING_TEST_REPORT = "nonexisting_test_report";
	private static final String TEST_REPORT = "test_report";
	final public static String dbConf = "/db.properties";

	@Before
	public final void init() throws IllegalStateException, NamingException, IOException{
		TestUtil.initDB();
	}

	@Test
	public final void testGetSubreports() throws SubreportNotFoundException {
		newReportTemplate();
		ReportTemplateProvider rtp = new ReportTemplateProvider();
		Map<String, Subreport> subs = rtp.getSubreports(TEST_REPORT);
		Assert.assertTrue("Subreport not found", subs.containsKey(TEST_REPORT));
		Subreport tr = subs.get(TEST_REPORT);
		Assert.assertTrue("Subreport content not loaded", tr.getContent().length > 0);
	}

	@Test
	public final void testGetMissingSubreports() {
		try {
			newReportTemplate();
			ReportTemplateProvider rtp = new ReportTemplateProvider();
			Map<String, Subreport> subs = rtp.getSubreports(NONEXISTING_TEST_REPORT);
			Assert.assertTrue("Subreport not found", subs.containsKey(NONEXISTING_TEST_REPORT));
			fail("Report should not exist");
		} catch (SubreportNotFoundException e) {
			System.out.println(e.getMessage()); // expected Exception
		}
	}

	private void newReportTemplate() {
		try {
			if (ReportTemplateDAO.fetchReportsByName(TEST_REPORT) == null) {
				ReportTemplate rt = new ReportTemplate();
				rt.setReportname(TEST_REPORT);
				rt.setContent("test content".toCharArray());
				ReportTemplateDAO.saveOrUpdate(rt);
			}
		} catch (VriesRuntimeException e) {
			System.err.println("duplicated entity");
			// it's OK, we don't care about duplicates
		}
	}

}
