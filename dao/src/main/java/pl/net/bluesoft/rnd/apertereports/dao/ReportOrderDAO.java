/**
 *
 */
package pl.net.bluesoft.rnd.apertereports.dao;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import pl.net.bluesoft.rnd.apertereports.model.ReportOrder;

import java.util.Collection;

/**
 * DAO methods for handling report orders.
 *
 * @see ReportOrder
 */
public class ReportOrderDAO {
    /**
     * Retrieves all report orders from database. The resulting collection is sorted by creation date.
     *
     * @return A collection of report orders
     */
    public static Collection<ReportOrder> fetchAllReportOrders() {
        return new pl.net.bluesoft.rnd.apertereports.dao.utils.WHS<Collection<ReportOrder>>() {
            @Override
            public Collection<ReportOrder> lambda() {
                return sess.createCriteria(ReportOrder.class).addOrder(Order.desc("createDate")).list();
            }
        }.p();
    }

    /**
     * Returns a unique report order representation from database by primary key.
     *
     * @param reportId The primary key value.
     * @return A report order or <code>null</code> if not found
     */
    public static ReportOrder fetchReport(final Long reportId) {
        return new pl.net.bluesoft.rnd.apertereports.dao.utils.WHS<ReportOrder>() {
            @Override
            public ReportOrder lambda() {
                ReportOrder ro = (ReportOrder) sess.createCriteria(ReportOrder.class)
                        .add(Restrictions.eq("id", reportId))
                        .uniqueResult();
                return ro;
            }
        }.p();
    }

    /**
     * Removes all the report orders from the given report order array from database.
     *
     * @param reports An array of report orders to delete.
     */
    public static void removeReportOrder(final ReportOrder... reports) {
        new pl.net.bluesoft.rnd.apertereports.dao.utils.WHS<Void>() {
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
    public static Long saveOrUpdateReportOrder(final ReportOrder reportOrder) {
        return new pl.net.bluesoft.rnd.apertereports.dao.utils.WHS<Long>() {
            @Override
            public Long lambda() {
                sess.saveOrUpdate(reportOrder);
                return reportOrder.getId();
            }
        }.p();
    }
}
