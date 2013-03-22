package org.apertereports.dao;

import java.util.*;
import org.apertereports.common.users.User;
import org.apertereports.common.utils.TextUtils;
import org.apertereports.dao.utils.GeneralDAO;
import org.apertereports.dao.utils.WHS;
import org.apertereports.model.CyclicReportConfig;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DAO methods for retrieving and saving cyclic report configurations.
 *
 * @see CyclicReportConfig
 */
public class CyclicReportConfigDAO {

    private enum SelectType {

        /**
         * Select cyclic report configurations
         */
        SELECT_CRC,
        /**
         * Count cyclic report configurations
         */
        SELECT_COUNT_CRC
    }
    private static final Logger logger = LoggerFactory.getLogger("ar.dao.crc");

    /**
     * Returns all cyclic report configurations from database corresponding to the
     * active reports to which user has permissions
     *
     * @return A collection of cyclic report configurations
     */
    public static Collection<CyclicReportConfig> fetch() {
        return fetch(GeneralDAO.ADMIN_USER, null, null, null, (Object[]) null);
    }

    /**
     * Returns a list of cyclic report configurations related to the report with given
     * id (only when user has access to the report with that id)
     *
     * @param user user
     * @param reportId Report id
     * @return List of cyclic report configurations
     */
    public static Collection<CyclicReportConfig> fetchByReportId(User user, final Integer reportId) {
        return fetch(user, null, "report.id = ?", null, reportId);
    }

    /**
     * Returns a unique cyclic report configurations by processed
     * report order id.
     *
     * @param reportOrderId Processed report order id
     * @return A cyclic report configuration corresponding to the processed report order
     */
    public static CyclicReportConfig fetchForProcessedReportOrder(final Long reportOrderId) {
        Collection<CyclicReportConfig> c = fetch(GeneralDAO.ADMIN_USER, null, "crc.processedOrder.id = ?", null, reportOrderId);
        if (c.size() == 1) {
            return c.iterator().next();
        }

        if (c.size() > 1) {
            logger.info("size > 1, return null");
        }
        return null;
    }

    /**
     * Removes all given cyclic report configurations from database.
     *
     * @param reports A collection of cyclic reports to remove
     */
    public static void remove(Collection<CyclicReportConfig> reports) {
        GeneralDAO.remove(reports);
    }

    /**
     * Removes all given cyclic report configurations from database.
     *
     * @param configs An array of cyclic report configurations to remove
     */
    public static void remove(final CyclicReportConfig... configs) {
        GeneralDAO.remove((Object[]) configs);
    }

    /**
     * Saves an instance of cyclic report to database. Returns the persisted
     * object's id.
     *
     * @param config A cyclic report configuration to save.
     * @return id The cyclic report id
     */
    public static Long saveOrUpdate(final CyclicReportConfig config) {
        GeneralDAO.saveOrUpdate(config);
        return config.getId();
    }

    /**
     * Returns a unique cyclic report representation from database by primary
     * key.
     *
     * @param id Primary key value of {@link org.apertereports.model.CyclicReportConfig}
     * @return A cyclic report corresponding to the given id
     */
    public static CyclicReportConfig fetchById(final Long id) {
        Collection<CyclicReportConfig> c = fetch(GeneralDAO.ADMIN_USER, null, "id = ?", null, id);
        if (c.size() == 1) {
            return c.iterator().next();
        }
        return null;
    }

    /**
     * Returns a list of cyclic report configurations relevant to given ids.
     *
     * @param ids List of {@link org.apertereports.model.CyclicReportConfig}
     * primary key values.
     * @return Cyclic report configurations
     */
    public static Collection<CyclicReportConfig> fetchByIds(final Long... ids) {
        if (ids.length == 0) {
            return new LinkedList<CyclicReportConfig>();
        }
        return fetch(null, null, "id IN (?)", null, (Object[]) ids);
    }

    /**
     * Counts cyclic report configurations matching given filter
     *
     * @param user user
     * @param filter Filter
     * @return Number of matching cyclic report configurations
     */
    public static Integer count(final User user, final String filter) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                Query query = createQuery(SelectType.SELECT_COUNT_CRC, sess, user, filter, null, null, (Object[]) null);
                Long count = (Long) query.uniqueResult();
                logger.info("count: " + count);
                return count;
            }
        }.p().intValue();
    }

    /**
     * Fetches cyclic report configuraitons matching given filter starting from
     * firstResult position. No more than maxResults is returned.
     *
     * @param user User
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing cyclic report configurations
     */
    public static Collection<CyclicReportConfig> fetch(final User user, final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<CyclicReportConfig>>() {

            @Override
            public Collection<CyclicReportConfig> lambda() {
                Query query = createQuery(SelectType.SELECT_CRC, sess, user, filter, null, null, (Object[]) null);
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

    private static Collection<CyclicReportConfig> fetch(final User user, final String nameFilter, final String hqlRestriction, final String hqlOther, final Object... parameters) {
        return new WHS<Collection<CyclicReportConfig>>() {

            @Override
            public Collection<CyclicReportConfig> lambda() {
                Query query = createQuery(SelectType.SELECT_CRC, sess, user, nameFilter, hqlRestriction, hqlOther, parameters);
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

        String select = type == SelectType.SELECT_CRC ? "crc" : "count(crc)";

        String queryS = "SELECT " + select + " FROM CyclicReportConfig crc";
        if (!nameFilter.isEmpty()) {
            where.add("crc.report.reportname LIKE ? ");
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
            if (!ids.isEmpty()) {

                boolean first = true;
                for (Integer id : ids) {
                    if (!first) {
                        sb.append(',');
                    }
                    first = false;
                    sb.append(id);
                }
            } else {
                sb.append("-1");    //no report found
            }
            where.add("crc.report.id IN (" + sb + ")");
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

        if (logger.isInfoEnabled()) {
            String userInfo = " U: " + (user == null ? "null" : user.getLogin() + (user.isAdministrator() ? ", admin" : ""));
            String paramsInfo = "";
            if (!params.isEmpty()) {
                paramsInfo = " [" + TextUtils.getCommaSeparatedString(params) + "]";
            }
            logger.info(queryS + ";" + paramsInfo + userInfo);
        }

        return q;
    }
}
