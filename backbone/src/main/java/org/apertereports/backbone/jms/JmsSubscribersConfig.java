package org.apertereports.backbone.jms;

import java.util.HashMap;
import java.util.Map;

import javax.jms.MessageListener;

import org.apertereports.backbone.jms.listener.BackgroundOrderProcessor;
import org.apertereports.backbone.jms.listener.CyclicOrderResponseProcessor;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ARConstants;

/**
 * Configuration of JMS subscribers. Contains MessageListener/queue mapping and
 * makes AperteReportsJmsFacade independent of MessageListener implementations.
 * 
 * @author Zbigniew Malinowski
 * 
 */
public class JmsSubscribersConfig {

	private static boolean initialized;
	private static Map<MessageListener, String> messageListeners = new HashMap<MessageListener, String>();
	private static Map<String, String> defaultJndiNames = new HashMap<String, String>();

	/**
	 *
	 * @return MessageListener/queue mappings
	 */
	public static Map<MessageListener, String> getMessageListeners() {
		if (!initialized)
			readConfiguration();
		return messageListeners;
	}

	/**
	 * Registers MessageListener implementations with proper queues.
	 */
	private static void readConfiguration() {
		
		/**
		 * Message listeners
		 */
		
		messageListeners.put(BackgroundOrderProcessor.getInstance(),
				ConfigurationConstants.JNDI_JMS_QUEUE_GENERATE_REPORT);
		messageListeners.put(CyclicOrderResponseProcessor.getInstance(),
				ConfigurationConstants.JNDI_JMS_QUEUE_CYCLIC_REPORT);

		/**
		 * Default JNDI names
		 */
		defaultJndiNames.put(ConfigurationConstants.JNDI_JMS_CONNECTION_FACTORY, ARConstants.JMS_CONNECTION_FACTORY_DEFAULT_JNDI_NAME);
		defaultJndiNames.put(ConfigurationConstants.JNDI_JMS_QUEUE_CYCLIC_REPORT, ARConstants.CYCLIC_REPORT_ORDER_QUEUE_DEFAULT_JNDI_NAME);
		defaultJndiNames.put(ConfigurationConstants.JNDI_JMS_QUEUE_GENERATE_REPORT, ARConstants.GENERATE_REPORT_QUEUE_DEFAULT_JNDI_NAME);
		
		
		initialized = true;
	}
	
	/**
	 * Default jndi name for a key
	 */
	public static String getDefaultJndiName(String key){
		if (!initialized)
			readConfiguration();
		return defaultJndiNames.get(key);
	}
	
}
