package org.apertereports.dao;

import java.util.*;
import org.apertereports.common.users.User;
import org.apertereports.common.users.UserRole;

import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportOrder;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO methods for handling report orders.
 *
 * @see ReportOrder
 */
public class ReportOrderDAO {

    //todots doc
    private enum SelectType {

        /**
         * Select report orders
         */
        SELECT_RO,
        /**
         * Count report orders
         */
        SELECT_COUNT_RO
    }
    private static final Logger logger = LoggerFactory.getLogger(CyclicReportOrderDAO.class);
    private static final User ADMIN_USER = new User("--", new HashSet<UserRole>(), true, null);

    /**
     * Returns a unique report order representation from database by primary
     * key.
     *
     * @param id Primary key value of {@link org.apertereports.model.ReportOrder}
     * @return A report order corresponding to the given id
     */
    public static ReportOrder fetchById(final Long id) {
        Collection<ReportOrder> c = fetch(ADMIN_USER, null, "id = ?", null, id);
        if (c.size() == 1) {
            return c.iterator().next();
        }
        return null;
    }

    /**
     * Returns a list of report orders related to the report with given id (only
     * when user has access to the report with that id)
     *
     * @param user user
     * @param reportId Report id
     * @return List of cyclic report orders
     */
    public static Collection<ReportOrder> fetchByReportId(User user, final Integer reportId) {
        return fetch(user, null, "report.id = ?", null, reportId);
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param reports A collection of cyclic reports to remove
     */
    //todo generalDao
    public static void remove(Collection<ReportOrder> reports) {
        remove(reports.toArray(new ReportOrder[0]));
    }

    /**
     * Removes all the report orders from the given report order array from
     * database.
     *
     * @param reports An array of report orders to delete.
     */
    //todo generalDao
    public static void remove(final ReportOrder... reports) {
        new WHS<Void>() {

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
    //todo generalDAO
    public static Long saveOrUpdate(final ReportOrder reportOrder) {
        return new WHS<Long>() {

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
     * @param user user
     * @param filter Filter
     * @return Number of matching report orders
     */
    public static Integer count(final User user, final String filter) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                Query query = createQuery(SelectType.SELECT_COUNT_RO, sess, user, filter, null, null, (Object[]) null);
                Long count = (Long) query.uniqueResult();
                logger.info("count: " + count);
                return count;
            }
        }.p().intValue();
    }

    /**
     * Fetches report orders matching given filter starting from firstResult
     * position. No more than maxResults is returned
     *
     * @param user User
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing cyclic report orders
     */
    public static Collection<ReportOrder> fetch(final User user, final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<ReportOrder>>() {

            @Override
            public Collection<ReportOrder> lambda() {
                Query query = createQuery(SelectType.SELECT_RO, sess, user, filter, null, "order by id", (Object[]) null);
                query.setFirstResult(firstResult);
                query.setMaxResults(maxResults);
                Collection c = query.list();
                if (c == null) {
                    c = new LinkedList();
                }
                logger.info("found: " + c.size());
                return c;
            }
        }.p();
    }

    private static Collection<ReportOrder> fetch(final User user, final String nameFilter, final String hqlRestriction, final String hqlOther, final Object... parameters) {
        return new WHS<Collection<ReportOrder>>() {

            @Override
            public Collection<ReportOrder> lambda() {
                Query query = createQuery(SelectType.SELECT_RO, sess, user, nameFilter, hqlRestriction, hqlOther, parameters);
                Collection c = query.list();
                if (c == null) {
                    c = new LinkedList();
                }
                logger.info("found: " + c.size());
                return c;
            }
        }.p();
    }

    private static Query createQuery(SelectType type, Session session, User user, String nameFilter,
            String hqlRestriction, String hqlOther, Object... parameters) {

        if (nameFilter == null) {
            nameFilter = "";
        }
        nameFilter = nameFilter.trim();
        LinkedList<String> where = new LinkedList<String>();
        LinkedList params = new LinkedList();

        String select = type == SelectType.SELECT_RO ? "ro" : "count(ro)";

        String queryS = "SELECT " + select + " FROM ReportOrder ro";
        if (!nameFilter.isEmpty()) {
            where.add("ro.report.reportname LIKE ? ");
            params.add('%' + nameFilter.toLowerCase() + '%');
        }
        if (hqlRestriction != null && !hqlRestriction.isEmpty()) {
            where.add(hqlRestriction);
        }
        if (parameters != null) {
            params.addAll(Arrays.asList(parameters));
        }

        if (user == null || !user.isAdministrator()) {
            Collection<Integer> ids = ReportTemplateDAO.fetchActiveIds(user);
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Integer id : ids) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                sb.append(id);
            }
            where.add("ro.report.id IN (" + sb + ")");
        }   //when the user is administrator then all reports are available for him

        if (!where.isEmpty()) {
            Iterator it = where.iterator();
            queryS += " WHERE " + it.next();
            while (it.hasNext()) {
                queryS += " AND " + it.next();
            }
        }

        if (hqlOther != null && !hqlOther.isEmpty()) {
            queryS += " " + hqlOther;
        }

        Query q = session.createQuery(queryS);
        for (int i = 0; i < params.size(); i++) {
            q.setParameter(i, params.get(i));
        }

        logger.info("user: " + (user == null ? "null" : user.getLogin() + (user.isAdministrator() ? ", admin" : "")));
        logger.info("query: " + queryS);
        if (logger.isInfoEnabled() && !params.isEmpty()) {
            //todo string list utils
            String paramsS = "[";
            if (!params.isEmpty()) {
                Iterator it = params.iterator();
                paramsS += it.next();
                while (it.hasNext()) {
                    paramsS += "," + it.next();
                }
            }
            paramsS += "]";
            logger.info("params: " + paramsS);
        }

        return q;
    }
}
