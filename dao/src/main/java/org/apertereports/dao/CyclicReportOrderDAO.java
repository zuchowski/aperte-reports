package org.apertereports.dao;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.apertereports.dao.utils.WHS;
import org.apertereports.model.CyclicReportOrder;

import java.util.*;
import org.apertereports.common.users.User;
import org.hibernate.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO methods for retrieving and saving cyclic report orders.
 *
 * @see CyclicReportOrder
 */
public class CyclicReportOrderDAO {

    //todots doc
    private enum SelectType {

        /**
         * Select cyclic report order
         */
        SELECT_CRO,
        /**
         * Count cyclic report orders
         */
        SELECT_COUNT_CRO
    }
    private static final Logger logger = LoggerFactory.getLogger(CyclicReportOrderDAO.class);

    /**
     * Returns all cyclic report orders from database.
     *
     * @return A collection of cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetchAll() {
        //todo user
        User user = null;
        return fetch(user, null, null, null, (Object[]) null);
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param templateId Id of report template used in cyclic reports.
     * @return A cyclic report corresponding to the template with given id.
     */
    public static Collection<CyclicReportOrder> fetchByTemplateId(final Integer templateId) {
        return new WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                return sess.createCriteria(CyclicReportOrder.class).createCriteria("report").add(Restrictions.eq("id", templateId)).list();
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
        return new WHS<CyclicReportOrder>() {

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
     * @param reports A collection of cyclic reports to remove
     */
    public static void remove(Collection<CyclicReportOrder> reports) {
        remove(reports.toArray(new CyclicReportOrder[0]));
    }

    /**
     * Removes all given cyclic report orders from database.
     *
     * @param reports An array of cyclic reports to remove
     */
    public static void remove(final CyclicReportOrder... reports) {
        new WHS<Boolean>() {

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
    public static Long saveOrUpdate(final CyclicReportOrder cyclicReportOrder) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                sess.saveOrUpdate(cyclicReportOrder);
                return cyclicReportOrder.getId();
            }
        }.p();
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param id Primary key value of {@link org.apertereports.model.CyclicReportOrder}
     * @return A cyclic report corresponding to the given id
     */
    public static CyclicReportOrder fetchById(final Long id) {
        //todo user
        User user = null;
        Collection<CyclicReportOrder> c = fetch(user, null, "id = ?", null, id);
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
     * Updates only given instances of {@link CyclicReportOrder} and removes not
     * listed from database. Returns a list of deleted (trimmed) cyclic reports.
     *
     * @param reportOrders Instances to update
     * @return A collection of deleted cyclic reports
     */
    public static Collection<CyclicReportOrder> trimAndUpdate(final Collection<CyclicReportOrder> reportOrders) {
        return new WHS<Collection<CyclicReportOrder>>(true) {

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

    /**
     * Counts cyclic report orders matching given filter
     *
     * @param filter Filter
     * @return Number of matching cyclic report orders
     */
    public static Integer countMatching(final String filter) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                //todo user
                User user = null;
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
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing cyclic report orders
     */
    public static Collection<CyclicReportOrder> fetch(final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<CyclicReportOrder>>() {

            @Override
            public Collection<CyclicReportOrder> lambda() {
                //todo user
                User user = null;
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

        //todots
//        if (user == null) {
//            //only when report has access for all users
//            where.add("? IN elements(rt.rolesWithAccess)");
//            params.add(ReportTemplate.ACCESS_ALL_ROLES_ID);
//        } else if (!user.isAdministrator()) {
//            String part = "( ? IN elements(rt.rolesWithAccess)";
//            params.add(ReportTemplate.ACCESS_ALL_ROLES_ID);
//            for (UserRole r : user.getRoles()) {
//                part += " OR ? IN elements(rt.rolesWithAccess)";
//                params.add(r.getId());
//            }
//            part += " )";
//
//            where.add(part);
//        }   //when the user is administrator then all reports are available for him

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

        //todots
//        logger.info("user: " + (user == null ? "null" : user.getLogin() + (user.isAdministrator() ? ", admin" : "")));
        logger.info("query: " + queryS);
        if (logger.isInfoEnabled() && !params.isEmpty()) {
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
        logger.info("params test: " + params);

        return q;
    }
}
