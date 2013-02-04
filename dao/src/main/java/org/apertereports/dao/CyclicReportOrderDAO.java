package org.apertereports.dao;

import org.apertereports.dao.utils.GeneralDAO;
import org.hibernate.Session;
import org.apertereports.dao.utils.WHS;
import org.apertereports.model.CyclicReportOrder;

import java.util.*;
import org.apertereports.common.users.User;
import org.apertereports.common.utils.TextUtils;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO methods for retrieving and saving cyclic report orders.
 *
 * @see CyclicReportOrder
 */
public class CyclicReportOrderDAO {

    private enum SelectType {

        /**
         * Select cyclic report orders
         */
        SELECT_CRO,
        /**
         * Count cyclic report orders
         */
        SELECT_COUNT_CRO
    }
    private static final Logger logger = LoggerFactory.getLogger(CyclicReportOrderDAO.class);

    /**
     * Returns all cyclic report orders from database corresponding to the
     * active reports to which user has permissions
     *
     * @return A collection of cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetch() {
        return fetch(GeneralDAO.ADMIN_USER, null, null, null, (Object[]) null);
    }

    /**
     * Returns a list of cyclic report orders related to the report with given
     * id (only when user has access to the report with that id)
     *
     * @param user user
     * @param reportId Report id
     * @return List of cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetchByReportId(User user, final Integer reportId) {
        return fetch(user, null, "report.id = ?", null, reportId);
    }

    /**
     * Returns a unique cyclic report representation from database by processed
     * report order id.
     *
     * @param reportOrderId Processed report order id
     * @return A cyclic report corresponding to the processed order
     */
    public static CyclicReportOrder fetchForReportOrder(final Long reportOrderId) {
        Collection<CyclicReportOrder> c = fetch(null, null, "processedOrder.id = ?", null, reportOrderId);
        if (c.size() == 1) {
            return c.iterator().next();
        }

        if (c.size() > 1) {
            logger.info("size > 1, return null");
        }
        return null;
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param reports A collection of cyclic reports to remove
     */
    public static void remove(Collection<CyclicReportOrder> reports) {
        GeneralDAO.remove(reports);
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param cyclicReportOrders An array of cyclic reports to remove
     */
    public static void remove(final CyclicReportOrder... cyclicReportOrders) {
        GeneralDAO.remove((Object[]) cyclicReportOrders);
    }

    /**
     * Saves an instance of cyclic report to database. Returns the persisted
     * object's id.
     *
     * @param cyclicReportOrder A cyclic report to save.
     * @return id The cyclic report id
     */
    public static Long saveOrUpdate(final CyclicReportOrder cyclicReportOrder) {
        GeneralDAO.saveOrUpdate(cyclicReportOrder);
        return cyclicReportOrder.getId();
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param id Primary key value of {@link org.apertereports.model.CyclicReportOrder}
     * @return A cyclic report corresponding to the given id
     */
    public static CyclicReportOrder fetchById(final Long id) {
        Collection<CyclicReportOrder> c = fetch(GeneralDAO.ADMIN_USER, null, "id = ?", null, id);
        if (c.size() == 1) {
            return c.iterator().next();
        }
        return null;
    }

    /**
     * Returns a list of cyclic report orders relevant to given ids.
     *
     * @param ids List of {@link org.apertereports.model.CyclicReportOrder}
     * primary key values.
     * @return Cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetchByIds(final Long... ids) {
        if (ids.length == 0) {
            return new LinkedList<CyclicReportOrder>();
        }
        return fetch(null, null, "id IN (?)", null, (Object[]) ids);
    }

    /**
     * Counts cyclic report orders matching given filter
     *
     * @param user user
     * @param filter Filter
     * @return Number of matching cyclic report orders
     */
    public static Integer count(final User user, final String filter) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                Query query = createQuery(SelectType.SELECT_COUNT_CRO, sess, user, filter, null, null, (Object[]) null);
                Long count = (Long) query.uniqueResult();
                logger.info("count: " + count);
                return count;
            }
        }.p().intValue();
    }

    /**
     * Fetches cyclic report orders matching given filter starting from
     * firstResult position. No more than maxResults is returned.
     *
     * @param user User
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetch(final User user, final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                Query query = createQuery(SelectType.SELECT_CRO, sess, user, filter, null, null, (Object[]) null);
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

    private static Collection<CyclicReportOrder> fetch(final User user, final String nameFilter, final String hqlRestriction, final String hqlOther, final Object... parameters) {
        return new WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                Query query = createQuery(SelectType.SELECT_CRO, sess, user, nameFilter, hqlRestriction, hqlOther, parameters);
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

        String select = type == SelectType.SELECT_CRO ? "cro" : "count(cro)";

        String queryS = "SELECT " + select + " FROM CyclicReportOrder cro";
        if (!nameFilter.isEmpty()) {
            where.add("cro.report.reportname LIKE ? ");
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
            where.add("cro.report.id IN (" + sb + ")");
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
            logger.info("params: [" + TextUtils.getCommaSeparatedString(params) + "]");
        }

        return q;
    }
}
