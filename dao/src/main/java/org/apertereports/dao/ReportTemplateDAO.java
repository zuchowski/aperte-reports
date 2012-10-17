package org.apertereports.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportTemplate;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * @author MW
 */
public class ReportTemplateDAO {

    public static Collection<ReportTemplate> fetchAllReports(final boolean onlyActive) {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                Criteria criteria = sess.createCriteria(ReportTemplate.class);
                if (onlyActive) {
                    criteria.add(Restrictions.eq("active", true));
                }
                return criteria.list();
            }
        }.p();
    }

    public static List<ReportTemplate> fetchReportsByName(final String reportName) {
        return new WHS<List<ReportTemplate>>(false) {

            @Override
            public List<ReportTemplate> lambda() {
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.eq("reportname", reportName)).addOrder(org.hibernate.criterion.Order.desc("id")).list();
                return list != null ? list : new ArrayList<ReportTemplate>();
            }
        }.p();
    }

    public static List<ReportTemplate> fetchReportsByNames(final String... reportNames) {
        return new WHS<List<ReportTemplate>>(false) {

            @Override
            public List<ReportTemplate> lambda() {
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.in("reportname", reportNames)).list();
                return list != null ? list : new ArrayList<ReportTemplate>();
            }
        }.p();
    }

    public static ReportTemplate fetchReport(final Integer reportId) {
        return new WHS<ReportTemplate>() {

            @Override
            public ReportTemplate lambda() {
                ReportTemplate rt = (ReportTemplate) sess.createCriteria(ReportTemplate.class).add(Restrictions.eq("id", reportId)).add(Restrictions.eq("active", true)).uniqueResult();
                return rt;
            }
        }.p();
    }

    public static List<ReportTemplate> fetchReports(final Integer... reportId) {
        return new WHS<List<ReportTemplate>>() {

            @Override
            public List<ReportTemplate> lambda() {
                if (reportId.length == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.in("id", reportId)).add(Restrictions.eq("active", true)).list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                return list;
            }
        }.p();
    }

    public static void remove(Integer reportId) {
        final ReportTemplate report = fetchReport(reportId);
        if (report != null) {
            new WHS<Void>() {

                @Override
                public Void lambda() {
                    sess.delete(report);
                    return null;
                }
            }.p();
        }
    }

    public static void remove(final ReportTemplate report) {
        new WHS<Void>() {

            @Override
            public Void lambda() {
                sess.delete(report);
                return null;
            }
        }.p();
    }

    public static void saveOrUpdate(final ReportTemplate report) {
        new WHS<Void>() {

            @Override
            public Void lambda() {
                sess.saveOrUpdate(report);
                return null;
            }
        }.p();

    }

    public static Integer countMatching(final String filter) {
        return new WHS<Integer>() {

            @Override
            public Integer lambda() {
                return ((Long) createFilterCriteria(sess, filter).setProjection(Projections.rowCount()).uniqueResult()).intValue();
            }
        }.p();
    }

    public static Collection<ReportTemplate> fetch(final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                Criteria c = createFilterCriteria(sess, filter);
                c.setFirstResult(firstResult);
                c.setMaxResults(maxResults);
                c.addOrder(Order.asc("id"));
                return c.list();
            }
        }.p();
    }

    private static Criteria createFilterCriteria(Session session, String filter) {
        String extendedFilter = "%";
        if (filter != null && !filter.isEmpty()) {
            extendedFilter += filter + "%";
        }
        return session.createCriteria(ReportTemplate.class).add(Restrictions.or(Restrictions.ilike("reportname", extendedFilter),
                Restrictions.ilike("description", extendedFilter))).add(Restrictions.eq("active", true));
    }
}
