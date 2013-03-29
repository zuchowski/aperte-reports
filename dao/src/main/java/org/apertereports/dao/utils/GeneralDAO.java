package org.apertereports.dao.utils;

import java.util.Collection;
import java.util.HashSet;
import org.apertereports.common.users.User;
import org.apertereports.common.users.UserRole;
import org.apertereports.model.ReportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class defines general DAO methods, e.g. remove or saveOrUpdate
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public final class GeneralDAO {

    private static final Logger logger = LoggerFactory.getLogger("ar.dao.general");
    public static final User ADMIN_USER = new User("A", new HashSet<UserRole>(), true, null);

    private GeneralDAO() {
    }

    /**
     * Persists given object to database
     *
     * @param o Object to store in database
     */
    public static Void saveOrUpdate(final Object o) {
        return new WHS<Void>() {

            @Override
            public Void lambda() {
                logger.info("saving: " + o);
                sess.saveOrUpdate(o);
                return null;
            }
        }.p();
    }

    /**
     * Removes from database all objects from collection from database
     *
     * @param c Collection of objects to remove
     */
    public static void remove(Collection<ReportOrder> c) {
        remove(c.toArray(new Object[c.size()]));
    }

    /**
     * Removes all the objects from the given report order array from database.
     *
     * @param obejct An array of objects to delete
     */
    public static void remove(final Object... obejct) {
        new WHS<Void>() {

            @Override
            public Void lambda() {
                for (Object o : obejct) {
                    if (o != null) {
                        logger.info("removing: " + o);
                        sess.delete(o);
                    }
                }
                return null;
            }
        }.p();
    }
}
