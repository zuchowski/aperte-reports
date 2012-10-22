package org.apertereports.dao;

import java.util.Collection;

import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportOrder;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

/**
 * DAO methods for handling report orders.
 *
 * @see ReportOrder
 */
public class ReportOrderDAO {

    /**
     * Retrieves all report orders from database. The resulting collection is
     * sorted by creation date.
     *
     * @return A collection of report orders
     */
    public static Collection<ReportOrder> fetchAll() {
        return new org.apertereports.dao.utils.WHS<Collection<ReportOrder>>() {

            @Override
            public Collection<ReportOrder> lambda() {
                return sess.createCriteria(ReportOrder.class).addOrder(Order.desc("createDate")).list();
            }
        }.p();
    }

    /**
     * Returns a unique report order representation from database by primary
     * key.
     *
     * @param id The primary key value.
     * @return A report order or
     * <code>null</code> if not found
     */
    public static ReportOrder fetchById(final Long id) {
        return new org.apertereports.dao.utils.WHS<ReportOrder>() {

            @Override
            public ReportOrder lambda() {
                ReportOrder ro = (ReportOrder) sess.createCriteria(ReportOrder.class).add(Restrictions.eq("id", id)).uniqueResult();
                return ro;
            }
        }.p();
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param templateId Id of report template used in cyclic reports.
     * @return A cyclic report corresponding to the template with given id.
     */
    public static Collection<ReportOrder> fetchByTemplateId(final Integer templateId) {
        return new org.apertereports.dao.utils.WHS<Collection<ReportOrder>>() {

            @Override
            public Collection<ReportOrder> lambda() {
                return sess.createCriteria(ReportOrder.class).createCriteria("report").add(Restrictions.eq("id", templateId)).list();
            }
        }.p();
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param reports A collection of cyclic reports to remove
     */
    public static void remove(Collection<ReportOrder> reports) {
        remove(reports.toArray(new ReportOrder[0]));
    }

    /**
     * Removes all the report orders from the given report order array from
     * database.
     *
     * @param reports An array of report orders to delete.
     */
    public static void remove(final ReportOrder... reports) {
        new org.apertereports.dao.utils.WHS<Void>() {

            @Override
            public Void lambda() {
                for (ReportOrder reportOrder : reports) {
                    if (reportOrder != null) {
                        sess.delete(reportOrder);
                    }
                }
                return null;
            }
        }.p();
    }

    /**
     * Persists a given report order instance to database. Returns its id.
     *
     * @param reportOrder A report order
     * @return The id
     */
    public static Long saveOrUpdate(final ReportOrder reportOrder) {
        return new org.apertereports.dao.utils.WHS<Long>() {

            @Override
            public Long lambda() {
                sess.saveOrUpdate(reportOrder);
                return reportOrder.getId();
            }
        }.p();
    }

    /**
     * Counts report orders matching given filter
     *
     * @param filter Filter
     * @return Number of matching report orders
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
     * Fetches cyclic report orders matching given filter starting from
     * firstResult position. No more than maxResults is returned.
     *
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing report orders
     */
    public static Collection<ReportOrder> fetch(final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<ReportOrder>>() {

            @Override
            public Collection<ReportOrder> lambda() {
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
        return session.createCriteria(ReportOrder.class).createAlias("report", "r").add(Restrictions.ilike("r.reportname", extendedFilter));
    }
}
