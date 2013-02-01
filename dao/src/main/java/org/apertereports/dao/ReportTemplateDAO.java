package org.apertereports.dao;

import java.util.*;
import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.users.User;
import org.apertereports.common.users.UserRole;

import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportTemplate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author MW
 */
public class ReportTemplateDAO {

    //todots doc
    private enum SelectType {

        /**
         * Select report
         */
        SELECT_RT,
        /**
         * Count reports
         */
        SELECT_COUNT_RT,
        /**
         * Select report ids
         */
        SELECT_RT_ID
    }
    private static final Logger logger = LoggerFactory.getLogger(ReportTemplateDAO.class);
    private static final User ADMIN_USER = new User("--", new HashSet<UserRole>(), true, null);

    /**
     * Returns all active report templates for given user
     *
     * @param user User
     * @return A collection active report templates
     */
    public static Collection<ReportTemplate> fetchAllActive(User user) {
        return fetch(user, null, "active = true");
    }

    /**
     * Returns a list of report templates with given name for given user
     *
     * @param user User
     * @param name Name of report template to find
     * @return A list of report templates
     */
    public static Collection<ReportTemplate> fetchByName(User user, String name) {
        return fetch(user, null, "reportname = ?", "order by id", name);
    }

    /**
     * Returns a list of report templates with given names with access for given
     * user
     *
     * @param user User
     * @param names Names of report templates to find
     * @return A list of report templates
     */
    public static Collection<ReportTemplate> fetchByNames(User user, String... names) {
        if (names.length == 0) {
            return new LinkedList<ReportTemplate>();
        }
        return fetch(user, null, "reportname IN (?)", null, (Object[]) names);
    }

    /**
     * Returns a unique report template from database by primary key with access
     * for given user
     *
     * @param user User
     * @param id Primary key value of {@link org.apertereports.model.ReportTemplate}
     * @return A report template corresponding to the given id or null if not
     * @throws AperteReportsException With {@link ErrorCodes#REPORT_ACCESS_DENIED}
     * code when user has no permissions
     */
    public static ReportTemplate fetchById(User user, Integer id) throws AperteReportsException {
        Collection<ReportTemplate> c = fetch(user, null, "active = true AND id = ?", null, id);
        if (c.size() == 1) {
            return c.iterator().next();
        }
        //check if exists...
        Integer count = count(ADMIN_USER, null, "active = true AND id = ?", null, id);
        if (count > 0) {
            throw new AperteReportsException(ErrorCodes.REPORT_ACCESS_DENIED);
        }
        //not exists
        return null;
    }

    private static Collection<ReportTemplate> fetch(User user, String nameFilter, String hqlRestriction) {
        return fetch(user, nameFilter, hqlRestriction, null, new Object[]{});
    }

    private static Collection<ReportTemplate> fetch(User user, String nameFilter, String hqlRestriction, String hqlOther) {
        return fetch(user, nameFilter, hqlRestriction, hqlOther, new Object[]{});
    }

    private static Collection<ReportTemplate> fetch(final User user, final String nameFilter, final String hqlRestriction, final String hqlOther, final Object... parameters) {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                Query query = createQuery(SelectType.SELECT_RT, sess, user, nameFilter, hqlRestriction, hqlOther, parameters);
                Collection c = query.list();
                if (c == null) {
                    c = new LinkedList();
                }
                logger.info("found: " + c.size());
                return c;
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
    public static Collection<ReportTemplate> fetchByIds(Integer... ids) {
        if (ids.length == 0) {
            return new LinkedList<ReportTemplate>();
        }
        return fetch(null, null, "id IN (?)", null, (Object[]) ids);
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
     * Counts report templates matching given filter for given user
     *
     * @param user User
     * @param nameFilter Name filter
     * @return Number of matching report templates
     */
    public static Integer countActive(final User user, final String nameFilter) {
        return count(user, nameFilter, "active = true", null, (Object[]) null);

    }

    /**
     * Counts report templates matching given filter for given user
     *
     * @param user User
     * @param nameFilter Name filter
     * @return Number of matching report templates
     */
    private static Integer count(final User user, final String nameFilter, final String hqlRestriction, final String hqlOther, final Object... parameters) {
        return new WHS<Long>() {

            @Override
            public Long lambda() {
                Query query = createQuery(SelectType.SELECT_COUNT_RT, sess, user, nameFilter, hqlRestriction, hqlOther, parameters);
                Long count = (Long) query.uniqueResult();
                logger.info("count: " + count);
                return count;
            }
        }.p().intValue();
    }

    /**
     * Returns active report templates matching given filter starting from
     * firstResult position. No more than maxResults is returned. Only reports
     * with access for given user are available.
     *
     * @param user User
     * @param filter Filter
     * @param firstResult Index of the first result
     * @param maxResults Number of maximum results
     * @return A collection of mathing report templates
     */
    public static Collection<ReportTemplate> fetchActive(final User user, final String filter, final int firstResult, final int maxResults) {
        return new WHS<Collection<ReportTemplate>>() {

            @Override
            public Collection<ReportTemplate> lambda() {
                Query query = createQuery(SelectType.SELECT_RT, sess, user, filter, "active = true", "order by id");
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

    /**
     * Returns collection of ids of all active report templates
     *
     * @param user User
     * @return Collection of ids
     */
    public static Collection<Integer> fetchActiveIds(final User user) {
        return new WHS<Collection<Integer>>() {

            @Override
            public Collection<Integer> lambda() {
                Query query = createQuery(SelectType.SELECT_RT_ID, sess, user, null, "active = true", "order by id");
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

        String select = type == SelectType.SELECT_RT ? "rt" : type == SelectType.SELECT_COUNT_RT
                ? "count(rt)" : "rt.id";

        String queryS = "SELECT " + select + " FROM ReportTemplate rt";
        if (!nameFilter.isEmpty()) {
            where.add("rt.reportname LIKE ? ");
            params.add('%' + nameFilter.toLowerCase() + '%');
        }
        if (hqlRestriction != null && !hqlRestriction.isEmpty()) {
            where.add(hqlRestriction);
        }
        if (parameters != null) {
            params.addAll(Arrays.asList(parameters));
        }

        if (user == null) {
            //only when report has access for all users
            where.add("? IN elements(rt.rolesWithAccess)");
            params.add(ReportTemplate.ACCESS_ALL_ROLES_ID);
        } else if (!user.isAdministrator()) {
            String part = "( ? IN elements(rt.rolesWithAccess)";
            params.add(ReportTemplate.ACCESS_ALL_ROLES_ID);
            for (UserRole r : user.getRoles()) {
                part += " OR ? IN elements(rt.rolesWithAccess)";
                params.add(r.getId());
            }
            part += " )";

            where.add(part);
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
