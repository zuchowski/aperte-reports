/**
 *
 */
package org.apertereports.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apertereports.dao.utils.WHS;
import org.apertereports.model.ReportOrder;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

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
        return new org.apertereports.dao.utils.WHS<Collection<ReportOrder>>() {
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
        return new org.apertereports.dao.utils.WHS<ReportOrder>() {
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
    public static Long saveOrUpdateReportOrder(final ReportOrder reportOrder) {
        return new org.apertereports.dao.utils.WHS<Long>() {
            @Override
            public Long lambda() {
                sess.saveOrUpdate(reportOrder);
                return reportOrder.getId();
            }
        }.p();
    }

	public static List<ReportOrder> filter(final String filter) {
		return new WHS<List<ReportOrder>>() {

			@Override
			public List<ReportOrder> lambda() {
				if (filter == null || filter.isEmpty()) {
					return (List<ReportOrder>) fetchAllReportOrders();
				}
				String extendedFilter = "%" + filter + "%";
				List<ReportOrder> list = sess
						.createCriteria(ReportOrder.class)
						.createAlias("report", "r")
						.add(Restrictions.ilike("r.reportname", extendedFilter)).list();
				if (list == null || list.size() == 0) {
					return new ArrayList<ReportOrder>();
				}
				return list;
			}
		}.p();
	}
}
