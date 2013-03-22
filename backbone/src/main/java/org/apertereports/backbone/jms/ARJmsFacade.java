package org.apertereports.backbone.jms;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import org.apertereports.backbone.jms.listener.CyclicOrderResponseProcessor;
import org.apertereports.backbone.jms.listener.ReportOrderProcessor;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.model.ReportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for JMS used in Aperte Reports. Handles message listeners
 * registration, message pushing and signals JMS availability. Supports JMS
 * usage outside a J2EE container.
 *
 * @author Zbigniew Malinowski
 * @
 *
 */
public class ARJmsFacade {

    private final static Logger logger = LoggerFactory.getLogger("ar.backbone.jms");
    /**
     * If JMS is initialized.
     */
    private static boolean initialized = false;
    
    private static final Map<MessageListener, String> messageListeners = new HashMap<MessageListener, String>();
    static {
        messageListeners.put(ReportOrderProcessor.getInstance(),
                ARConstants.JNDI_JMS_GENERATE_REPORT_QUEUE_NAME);
        messageListeners.put(CyclicOrderResponseProcessor.getInstance(),
                ARConstants.JNDI_JMS_CYCLIC_REPORT_ORDER_QUEUE_NAME);
    }


    /**
     * If not initialized, subscribes configured listeners to proper queues and
     * initialize.
     *
     * @throws Exception
     */
    private static void init() throws Exception {
        if (initialized) {
            return;
        }

        Connection connection = null;
        Session session = null;
        try {
            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
                    .lookup(ARConstants.JNDI_JMS_CONNECTION_FACTORY_NAME);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            for (MessageListener listener : messageListeners.keySet()) {
                String jndiQueueName = messageListeners.get(listener);
                try {
                    MessageConsumer consumer = session.createConsumer((Destination) initCtx.lookup(jndiQueueName));
                    consumer.setMessageListener(listener);
                } catch (Exception e) {
                    logger.error("Cannot find queue in JNDI: " + jndiQueueName, e);
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
     * Adds message to queue. Messages contains only report order id.
     *
     * @param orderId id of the order to be stored
     * @param queueName configuration key of queue which message will be sent to
     */
    //todots przerobic przekazywanie nazwy kolejki
    public static void sendReportOrderId(ReportOrder ro, String queueName) {
        logger.info("Sending report order id: " + ro.getId() + ", queue: " + queueName);
        
        if (!isJmsAvailable()) {
            logger.warn("JMS not available, discarding set report order id");
            return;
        }

        Long id = ro.getId();
        if (id == null) {
            logger.warn("id == null, discarding...");
            return;
        }
        
        Connection connection = null;
        Session session = null;
        try {
            init();

            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
                    .lookup(ARConstants.JNDI_JMS_CONNECTION_FACTORY_NAME);
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageProducer producer = session.createProducer((Destination) initCtx
                    .lookup(queueName));

            Message reportOrderMessage = session.createMessage();
            reportOrderMessage.setIntProperty(ARConstants.REPORT_ORDER_ID, id.intValue());
            producer.send(reportOrderMessage);
        } catch (Exception e) {
            logger.error("EXC: ", e);
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
     * Checks if JMS ConnectionFactory is registered with JNDI, and if can
     * create connection and session.
     *
     * @return
     */
    public static boolean isJmsAvailable() {
        //todots to one method
        Connection connection = null;
        Session session = null;
        try {
            InitialContext initCtx = new InitialContext();
            ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
                    .lookup(ARConstants.JNDI_JMS_CONNECTION_FACTORY_NAME);
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
