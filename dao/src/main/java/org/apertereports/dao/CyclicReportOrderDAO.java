package org.apertereports.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.apertereports.dao.utils.WHS;
import org.apertereports.model.CyclicReportOrder;

import java.util.*;

/**
 * DAO methods for retrieving and saving cyclic report orders.
 *
 * @see CyclicReportOrder
 */
public class CyclicReportOrderDAO {

    /**
     * Returns all cyclic reports from database.
     *
     * @return A collection of cyclic reports
     */
    public static Collection<CyclicReportOrder> fetchAllCyclicReportOrders() {
        return new org.apertereports.dao.utils.WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                return sess.createCriteria(CyclicReportOrder.class).list();
            }
        }.p();
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param reportId Primary key value.
     * @return A cyclic report corresponding to the given id
     */
    public static CyclicReportOrder fetchCyclicReportOrder(final Long reportId) {
        return new org.apertereports.dao.utils.WHS<CyclicReportOrder>() {

            @Override
            public CyclicReportOrder lambda() {
                CyclicReportOrder cro = (CyclicReportOrder) sess.createCriteria(CyclicReportOrder.class).add(Restrictions.eq("id", reportId)).uniqueResult();
                return cro;
            }
        }.p();
    }

    /**
     * Returns a unique cyclic report representation from database by processed
     * report order id.
     *
     * @param reportId Processed report order id
     * @return A cyclic report corresponding to the processed order
     */
    public static CyclicReportOrder fetchForReportOrder(final Long reportId) {
        return new org.apertereports.dao.utils.WHS<CyclicReportOrder>() {

            @Override
            public CyclicReportOrder lambda() {
                CyclicReportOrder cro = (CyclicReportOrder) sess.createCriteria(CyclicReportOrder.class).createCriteria("processedOrder").add(Restrictions.eq("id", reportId)).uniqueResult();
                return cro;
            }
        }.p();
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param reports An array of cyclic reports to remove
     */
    public static void removeCyclicReportOrder(final CyclicReportOrder... reports) {
        new org.apertereports.dao.utils.WHS<Boolean>() {

            @Override
            public Boolean lambda() {
                for (CyclicReportOrder reportOrder : reports) {
                    if (reportOrder != null) {
                        sess.delete(reportOrder);
                    }
                }
                return true;
            }
        }.p();
    }

    /**
     * Saves an instance of cyclic report to database. Returns the persisted
     * object's id.
     *
     * @param cyclicReportOrder A cyclic report to save.
     * @return id The cyclic report id
     */
    public static Long saveOrUpdateCyclicReportOrder(final CyclicReportOrder cyclicReportOrder) {
        return new org.apertereports.dao.utils.WHS<Long>() {

            @Override
            public Long lambda() {
                sess.saveOrUpdate(cyclicReportOrder);
                return cyclicReportOrder.getId();
            }
        }.p();
    }

    /**
     * Returns all enabled cyclic report orders from database.
     *
     * @return A collection of enabled cyclic report orders.
     */
    public static Collection<CyclicReportOrder> fetchAllEnabledCyclicReports() {
        return new org.apertereports.dao.utils.WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                return sess.createCriteria(CyclicReportOrder.class).add(Restrictions.eq("enabled", true)).list();
            }
        }.p();
    }

    /**
     * Returns a list of cyclic report orders relevant to given ids.
     *
     * @param reportIds An array of {@link org.apertereports.model.CyclicReportOrder}
     * primary key values.
     * @return A list of cyclic report orders
     */
    public static List<CyclicReportOrder> fetchCyclicReportsByIds(final Long... reportIds) {
        return new org.apertereports.dao.utils.WHS<List<CyclicReportOrder>>(false) {

            @Override
            public List<CyclicReportOrder> lambda() {
                if (reportIds.length == 0) {
                    return new ArrayList<CyclicReportOrder>();
                }
                List<CyclicReportOrder> list = sess.createCriteria(CyclicReportOrder.class).add(Restrictions.in("id", reportIds)).list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<CyclicReportOrder>();
                }
                return list;
            }
        }.p();
    }

    /**
     * Updates only given instances of {@link CyclicReportOrder} and removes not
     * listed from database. Returns a list of deleted (trimmed) cyclic reports.
     *
     * @param reportOrders Instances to update
     * @return A collection of deleted cyclic reports
     */
    public static Collection<CyclicReportOrder> trimAndUpdate(final Collection<CyclicReportOrder> reportOrders) {
        return new org.apertereports.dao.utils.WHS<Collection<CyclicReportOrder>>(true) {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                Set<Long> ids = new HashSet<Long>();
                for (CyclicReportOrder cro : reportOrders) {
                    ids.add(cro.getId());
                    sess.saveOrUpdate(cro);
                }
                Criteria crit = sess.createCriteria(CyclicReportOrder.class);
                if (!ids.isEmpty()) {
                    crit.add(Restrictions.not(Restrictions.in("id", ids)));
                }
                List<CyclicReportOrder> list = crit.list();
                for (CyclicReportOrder cro : list) {
                    sess.delete(cro);
                }
                return list;
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

    public static Collection<CyclicReportOrder> fetch(final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
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
        return session.createCriteria(CyclicReportOrder.class).createAlias("report", "r").add(Restrictions.or(Restrictions.ilike("description", extendedFilter),
                Restrictions.ilike("r.reportname", extendedFilter)));
    }
}
