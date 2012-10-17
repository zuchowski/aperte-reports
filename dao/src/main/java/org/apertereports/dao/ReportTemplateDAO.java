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

    /**
     * Returns all report templates from database.
     *
     * @return A collection of report templates
     */
    public static Collection<ReportTemplate> fetchAll() {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                return sess.createCriteria(ReportTemplate.class).list();
            }
        }.p();
    }

    /**
     * Returns all active report templates
     *
     * @return A collection active report templates
     */
    public static Collection<ReportTemplate> fetchAllActive() {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                return sess.createCriteria(ReportTemplate.class).add(Restrictions.eq("active", true)).list();
            }
        }.p();
    }

    /**
     * Returns a list of report templates with given name
     *
     * @param name Name of report template to find
     * @return A list of report templates
     */
    public static List<ReportTemplate> fetchByName(final String name) {
        return new WHS<List<ReportTemplate>>(false) {

            @Override
            public List<ReportTemplate> lambda() {
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.eq("reportname", name)).addOrder(org.hibernate.criterion.Order.desc("id")).list();
                return list != null ? list : new ArrayList<ReportTemplate>();
            }
        }.p();
    }


    /**
     * Returns a list of report templates with given names
     *
     * @param names Names of report templates to find
     * @return A list of report templates
     */
    public static List<ReportTemplate> fetchByNames(final String... names) {
        return new WHS<List<ReportTemplate>>(false) {

            @Override
            public List<ReportTemplate> lambda() {
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.in("reportname", names)).list();
                return list != null ? list : new ArrayList<ReportTemplate>();
            }
        }.p();
    }

    /**
     * Returns a unique report template from database by primary key.
     *
     * @param id Primary key value of {@link org.apertereports.model.ReportTemplate}
     * @return A report template corresponding to the given id
     */
    public static ReportTemplate fetchById(final Integer id) {
        return new WHS<ReportTemplate>() {

            @Override
            public ReportTemplate lambda() {
                ReportTemplate rt = (ReportTemplate) sess.createCriteria(ReportTemplate.class).add(Restrictions.eq("id", id)).add(Restrictions.eq("active", true)).uniqueResult();
                return rt;
            }
        }.p();
    }

    /**
     * Returns a list of report templates relevant to given ids.
     *
     * @param ids An array of {@link org.apertereports.model.CyclicReportOrder}
     * primary key values.
     * @return A list of cyclic report orders
     */
    public static List<ReportTemplate> fetchByIds(final Integer... ids) {
        return new WHS<List<ReportTemplate>>() {

            @Override
            public List<ReportTemplate> lambda() {
                if (ids.length == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                List<ReportTemplate> list = sess.createCriteria(ReportTemplate.class).add(Restrictions.in("id", ids)).add(Restrictions.eq("active", true)).list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<ReportTemplate>();
                }
                return list;
            }
        }.p();
    }

    /**
     * Removes report template with given id
     *
     * @param id Id of report template to remove
     */
    public static void remove(Integer id) {
        final ReportTemplate report = fetchById(id);
        if (report != null) {
            remove(report);
        }
    }

    /**
     * Removes given report template
     *
     * @param reportTemplate Report template to remove
     */
    public static void remove(final ReportTemplate reportTemplate) {
        new WHS<Void>() {

            @Override
            public Void lambda() {
                sess.delete(reportTemplate);
                return null;
            }
        }.p();
    }

    /**
     * Saves an instance of report template to database. Returns the persisted
     * object's id.
     *
     * @param reportTemplate A report template to save
     */
    public static void saveOrUpdate(final ReportTemplate reportTemplate) {
        new WHS<Void>() {

            @Override
            public Void lambda() {
                sess.saveOrUpdate(reportTemplate);
                return null;
            }
        }.p();

    }

    /**
     * Counts report templates matching given filter
     *
     * @param filter Filter
     * @return Number of matching report templates
     */
    public static Integer countMatching(final String filter) {
        return new WHS<Integer>() {

            @Override
            public Integer lambda() {
                return ((Long) createFilterCriteria(sess, filter).setProjection(Projections.rowCount()).uniqueResult()).intValue();
            }
        }.p();
    }

    /**
     * Fetches report templates matching given filter starting from
     * firstResult position. No more than maxResults is returned.
     *
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing report templates
     */
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
