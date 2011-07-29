package pl.net.bluesoft.rnd.apertereports.backbone.tests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesException;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( {"/testEnvContext.xml"})
public class ReportDaoTest {

    @Test
    public void testReportDao() throws VriesException {
        Set<Integer> ids = new HashSet<Integer>();
        Set<Integer> ids2 = new HashSet<Integer>();

        /*
           * Collection<ReportTemplate> reports =
           * ReportTemplateDao.getInstance().findAll(); for (ReportTemplate r : reports)
           * { ids.add(r.getId()); }
           *
           *
           * Collection<ReportTemplate> reports2 =
           * ReportTemplateDao.getInstance().findAll(ids.toArray(new
           * Integer[ids2.size()])); for (ReportTemplate r : reports2) {
           * ids2.add(r.getId()); }
           */

        assertTrue(ids.containsAll(ids2));
        assertTrue(ids2.containsAll(ids));
    }

}
