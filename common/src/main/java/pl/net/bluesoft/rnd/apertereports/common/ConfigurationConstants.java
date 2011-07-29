package pl.net.bluesoft.rnd.apertereports.common;

/**
 * Contains configuration keys used in the application.
 */
public interface ConfigurationConstants {
    /**
     * Corresponds to a JNDI name used to get the mail session's user property.
     */
    static final String MAIL_SESSION_USER = "mail.session.user";

    /**
     * The key of a message provider base resource bundle name.
     */
    static final String MESSAGE_PROVIDER_RESOURCE = "message.provider.resource";

    /**
     * The key of an email message title. This should be used along with a MessageProvider instance to get
     * the relevant email title from a resource bundle.
     */
    static final String MESSAGE_PROVIDER_BGREPORT_EMAIL_TITLE = "message.provider.bgreport.email.title";

    /**
     * The key of an email message template. This should be used along with a MessageProvider instance to get
     * the relevant email message template from a resource bundle.
     */
    static final String MESSAGE_PROVIDER_BGREPORT_EMAIL_MESSAGE = "message.provider.bgreport.email.msg";

    /**
     * Corresponds to a JNDI name used to get the mail session from an initial context.
     */
    static final String JNDI_MAIL_SESSION = "jndi.mail.session";

    /**
     * The key to retrieve the delay between sending each mail. This is useful when a SMTP server prevents from sending
     * too many emails too quickly.
     */
    static final String MAIL_SEND_DELAY = "mail.send.delay";

    /**
     * The key used to get the report generation JMS queue from configuration.
     */
    static final String JNDI_JMS_QUEUE_GENERATE_REPORT = "jndi.jms.queue.generate_report";

    /**
     * The key used to get the cyclic reports JMS queue from configuration.
     */
    static final String JNDI_JMS_QUEUE_CYCLIC_REPORT = "jndi.jms.queue.cyclic_report_order";

    /**
     * The key used to get the JMS connection factory name from configuration.
     */
    static final String JNDI_JMS_CONNECTION_FACTORY = "jndi.jms.connection_factory";

    /**
     * The key used to set Jasper's character encoding.
     */
    static final String JASPER_REPORTS_CHARACTER_ENCODING = "jasper_reports.character_encoding";

    /**
     * Defines a key corresponding to configuration cache timeout.
     */
    static final String CONFIGURATION_CACHE_TIMEOUT_IN_MINUTES = "configuration.cache.timeout.in_minutes";
}
