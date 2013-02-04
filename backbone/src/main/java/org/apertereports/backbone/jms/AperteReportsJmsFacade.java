package org.apertereports.backbone.jms;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;

import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.dao.utils.ConfigurationCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for JMS used in Aperte Reports. Handles message listeners
 * registration, message pushing and signals JMS availability. Supports JMS
 * usage outside a J2EE container.
 * 
 * @author Zbigniew Malinowski
 * 
 */
public class AperteReportsJmsFacade {
    
        private final static Logger logger = LoggerFactory.getLogger(AperteReportsJmsFacade.class);

	/**
	 * If JMS is initialized.
	 */
	private static boolean initialized = false;

	/**
	 * If not initialized, subscribes configured listeners to proper queues and
	 * initialize.
	 * @throws Exception 
	 */
	private static void subscribeMessageListeners() throws Exception {
		if (initialized)
			return;

		Connection connection = null;
		Session session = null;
		try {
			InitialContext initCtx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
					.lookup(getJndiNameFromConfiguration(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			for (MessageListener listener : JmsSubscribersConfig.getMessageListeners().keySet()) {
				String configurationKey = JmsSubscribersConfig.getMessageListeners().get(listener);
				try {
					String queueJndiName = getJndiNameFromConfiguration(configurationKey);
					MessageConsumer consumer = session.createConsumer((Destination) initCtx.lookup(queueJndiName));
					consumer.setMessageListener(listener);
				} catch (Exception e) {
					logger.error("Cannot find queue in JNDI: " + configurationKey, e);
				}
			}

			connection.start();
			initialized = true;

		} catch (Exception e) {
			logger.warn("Cannot initialize JMS context", e);
			try {
				if (connection != null) {
					connection.close();
				}
				if (session != null) {
					session.close();
				}
			} catch (Exception ex) {
				logger.warn("Cannot close JMS connection after error", ex);
			}
			throw e;
		}
	}

	/**
	 * @return jndi name from configuration, or default value if configuration is not available.
	 */
	private static String getJndiNameFromConfiguration(String configurationKey) {
		String jndiName = ConfigurationCache.getValue(configurationKey);
		if (jndiName == null){
			String defaultJndiName = JmsSubscribersConfig.getDefaultJndiName(configurationKey);
			logger.debug("JNDI name not found for key: " + configurationKey + " using default:" + defaultJndiName);
			return defaultJndiName;
		}
		return jndiName;
	}

	/**
	 * Adds message to queue. Messages contains only report order id.
	 * 
	 * @param orderId
	 *            id of the order to be stored
	 * @param queueName
	 *            configuration key of queue which message will be sent to
	 */
	public static void sendOrderToJms(Long orderId, String queueName) {
		

		Connection connection = null;
		Session session = null;
		try {
			subscribeMessageListeners();
			
			InitialContext initCtx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
					.lookup(getJndiNameFromConfiguration(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			MessageProducer producer = session.createProducer((Destination) initCtx
					.lookup(getJndiNameFromConfiguration(queueName)));

			Message reportOrderMessage = session.createMessage();
			reportOrderMessage.setIntProperty(ARConstants.REPORT_ORDER_ID, orderId.intValue());
			producer.send(reportOrderMessage);
			logger.debug(ARConstants.REPORT_ORDER_ID + ": " + orderId);
		} catch (Exception e) {
			throw new ARRuntimeException(ErrorCode.JMS_UNAVAILABLE, e);
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
				if (session != null) {
					session.close();
				}
			} catch (Exception e) {
				throw new ARRuntimeException(e);
			}
		}
	}

	/**
	 * Checks if JMS ConnectionFactory is registered with JNDI, and if can create
	 * connection and session.
	 * 
	 * @return
	 */
	public static boolean isJmsAvailable() {
		Connection connection = null;
		Session session = null;
		try {
			InitialContext initCtx = new InitialContext();
			ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
					.lookup(getJndiNameFromConfiguration(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY));
			connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

		} catch (Exception e) {
			return false;
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
				if (session != null) {
					session.close();
				}
			} catch (Exception e) {
				logger.warn("Cannot close JMS connection after availability test", e);
			}
		}
		return true;
	}

}
