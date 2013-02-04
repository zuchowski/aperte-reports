package org.apertereports.backbone.jms.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.apertereports.common.ARConstants;
import org.apertereports.dao.CyclicReportOrderDAO;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton {@link MessageListener} implementation that asynchronously handles the
 * cyclic report orders. On response to its queue the listener fetches the
 * relevant report order and connects its results with the cyclic report order.
 */
public class CyclicOrderResponseProcessor implements MessageListener {

	/**
	 * Singleton
	 */
	private static CyclicOrderResponseProcessor instance;
        private static Logger logger = LoggerFactory.getLogger(CyclicOrderResponseProcessor.class);
	
	public static synchronized CyclicOrderResponseProcessor getInstance() {
		if(instance == null)
			instance = new CyclicOrderResponseProcessor();
		return instance;
	}
	
	private CyclicOrderResponseProcessor() {
		
	}

	/**
	 * Processes a cyclic report response from report generator. Sets the
	 * generated report order for the cyclic report fields.
	 * 
	 * @param message
	 *            A JMS message
	 * @see MessageListener#onMessage(javax.jms.Message)
	 */

	@Override
	public void onMessage(Message message) {
		try {
			Long id = message.getLongProperty(ARConstants.REPORT_ORDER_ID);
			ReportOrder reportOrder = ReportOrderDAO.fetchById(id);
			processReport(reportOrder);
		} catch (JMSException e) {
			logger.error(e.getMessage(), e);
//			throw new AperteReportsRuntimeException(e);
		}
	}

	/**
	 * Updateds the generated report order field in corresponding cyclic report.
	 * 
	 * @param reportOrder
	 *            Generated report order
	 */
	private void processReport(final ReportOrder reportOrder) {
		CyclicReportOrder cyclicReportOrder = CyclicReportOrderDAO.fetchForReportOrder(reportOrder.getId());

		if (cyclicReportOrder == null) {
			return;
		}

		cyclicReportOrder.setProcessedOrder(null);

		if (reportOrder.getReportResult() != null) {
			cyclicReportOrder.setReportOrder(reportOrder);
		}

		CyclicReportOrderDAO.saveOrUpdate(cyclicReportOrder);
	}
}
