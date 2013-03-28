package org.apertereports.backbone.jms;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.InitialContext;
import org.apertereports.backbone.jms.listener.GenerateReportQueueMessageListener;
import org.apertereports.backbone.jms.listener.ProcessReportQueueMessageListener;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARException;
import org.apertereports.model.ReportOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for JMS used in Aperte Reports. Handles message listeners
 * registration, message pushing and signals JMS availability. Supports JMS
 * usage outside a J2EE container.
 *
 * @author Zbigniew Malinowski
 * @author Tomasz Serafin, BlueSoft Sp. z o.o.
 */
public class ARJmsFacade {

    private static final Logger logger = LoggerFactory.getLogger("ar.backbone.jms");
    /**
     * If JMS is initialized.
     */
    private static boolean initialized = false;
    private static final Map<String, MessageListener> queueToListenerMap = new HashMap<String, MessageListener>();
    private static JmsContext mainCtx;

    static {
        queueToListenerMap.put(ARConstants.JNDI_JMS_GENERATE_REPORT_QUEUE_ID,
                GenerateReportQueueMessageListener.getInstance());
        queueToListenerMap.put(ARConstants.JNDI_JMS_PROCESS_REPORT_QUEUE_ID,
                ProcessReportQueueMessageListener.getInstance());
    }

    /**
     * If not initialized, subscribes configured listeners to proper queues and
     * initialize.
     *
     * @return true if the facade has been initialized, false otherwise
     * @throws Exception
     */
    private static synchronized boolean init() {
        if (initialized) {
            return true;
        }

        logger.info("Initializing JMS context...");
        try {
            mainCtx = JmsContext.getNewContext();
            InitialContext initCtx = new InitialContext();
            for (String queueName : queueToListenerMap.keySet()) {
                MessageListener listener = queueToListenerMap.get(queueName);
                try {
                    MessageConsumer consumer = mainCtx.getSession().createConsumer(
                            (Destination) initCtx.lookup(queueName));
                    consumer.setMessageListener(listener);
                } catch (Exception e) {
                    logger.error("Cannot find queue in JNDI: " + queueName, e);
                }
            }

            mainCtx.getConnection().start();
            initialized = true;
        } catch (Exception e) {
            logger.error("Cannot initialize JMS context", e);
            if (mainCtx != null) {
                mainCtx.close();
            }
            return false;
        }
        logger.info("Initializing JMS context, done.");
        return initialized;
    }

    /**
     * Sends message to JMS in order to generate report from the given report
     * order
     *
     * @param ro Report order as a base for report to generate
     * @throws ARException When there is a problem to send message to JMS
     */
    public static synchronized void sendToGenerateReport(ReportOrder ro) throws ARException {
        sendReportOrderId(ro, ARConstants.JNDI_JMS_GENERATE_REPORT_QUEUE_ID);
    }

    /**
     * Sends message to JMS in order to process generated report from the given
     * report order
     *
     * @param ro Report order as a base for generated report
     * @throws ARException When there is a problem to send message to JMS
     */
    public static synchronized void sendToProcessReport(ReportOrder ro) throws ARException {
        sendReportOrderId(ro, ARConstants.JNDI_JMS_PROCESS_REPORT_QUEUE_ID);
    }

    /**
     * Adds message to queue. Messages contains only report order id.
     *
     * @param orderId id of the order to be stored
     * @param queueName configuration key of queue which message will be sent to
     */
    private static synchronized void sendReportOrderId(ReportOrder ro, String queueName) throws ARException {
        logger.info("Sending report order id: " + ro.getId() + " to " + queueName);

        Long id = ro.getId();
        if (id == null) {
            logger.warn("id == null, discarding...");
            return;
        }

        if (!init()) {
            throw new ARException(ErrorCode.JMS_UNAVAILABLE);
        }

        JmsContext ctx = null;
        try {
            ctx = JmsContext.getNewContext();
            InitialContext initCtx = new InitialContext();

            MessageProducer producer = ctx.getSession().createProducer((Destination) initCtx
                    .lookup(queueName));

            Message reportOrderMessage = ctx.getSession().createMessage();
            reportOrderMessage.setLongProperty(ARConstants.JMS_PROPERTY_REPORT_ORDER_ID, id.intValue());
            producer.send(reportOrderMessage);
        } catch (Exception e) {
            logger.error("Error while sending report order id", e);
            throw new ARException(ErrorCode.JMS_UNAVAILABLE, e);
        } finally {
            if (ctx != null) {
                ctx.close();
            }
        }
    }

    /**
     * Checks if JMS ConnectionFactory is registered with JNDI, and if can
     * create connection and session.
     *
     * @return
     */
    public static synchronized boolean isJmsAvailable() {
        JmsContext ctx;
        try {
            ctx = JmsContext.getNewContext();
        } catch (Exception e) {
            return false;
        }
        ctx.close();
        return true;
    }

    private static class JmsContext {

        private Connection connection = null;
        private Session session = null;

        /**
         * Initializes JMS connection and session. When there is a problem to
         * init the context, an exception is thrown - in such case there is no
         * need to use {@link #close()} method <p> After using properly
         * initalized context, it should be closed by calling {@link #close()}
         * method
         *
         * @throws Exception
         */
        public static JmsContext getNewContext() throws Exception {
            JmsContext ctx = new JmsContext();
            try {
                InitialContext initCtx = new InitialContext();
                ConnectionFactory connectionFactory = (ConnectionFactory) initCtx
                        .lookup(ARConstants.JNDI_JMS_CONNECTION_FACTORY_ID);
                ctx.connection = connectionFactory.createConnection();
                ctx.session = ctx.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            } catch (Exception e) {
                logger.warn("Cannot initialize JMS context", e);
                ctx.close();
                throw e;
            }
            return ctx;
        }

        /**
         * Closes connection and session objects
         */
        public void close() {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ex) {
                    logger.error("while closing connection", ex);
                }
                connection = null;
            }
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException ex) {
                    logger.error("while closing session", ex);
                }
                session = null;
            }
        }

        /**
         * Returns session object. The session shouldn't be closed externally
         *
         * @return Session object. It can be null when context hasn't been
         * initialized properly
         */
        public Session getSession() {
            return session;
        }

        /**
         * Returns connection object. The connection shouldn't be closed
         * externally
         *
         * @return Connection object. It can be null when context hasn't been
         * initialized properly
         */
        public Connection getConnection() {
            return connection;
        }
    }
}
