package org.apertereports.backbone.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Properties;

import javax.activation.DataSource;
import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.model.ConfigurationEntry;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for processing email messages. Takes advantage of
 * {@link VelocityEngine} to create email contents from template. Uses a
 * resource bundle message provider for template retrieval and
 * <code>Apache Commons Email</code> library to send emails.
 * <p/>
 * An application container should provide a <code>javax.mail.session</code>
 * instance for it to work properly.
 */
public class EmailProcessor implements ConfigurationConstants {
    
    private static Logger logger = LoggerFactory.getLogger(EmailProcessor.class);
	/**
	 * Default charset of an email message.
	 */
	private String charset = "UTF-8";

	/**
	 * A singleton instance of this class.
	 */
	private static EmailProcessor instance;

	/**
	 * A velocity engine instance.
	 */
	private VelocityEngine velocityEngine = new VelocityEngine();

	/**
	 * Resource bundle message provider.
	 */
	private ResourceBundleMessageProvider messageProvider = null;

	/**
	 * A JNDI name for javax.mail.session instance held in the context of the
	 * application container.
	 */
	private String mailSessionJndi="java:comp/env/mail/session";

	/**
	 * Application container specific mail user key. For instance, this should
	 * be configured as <code>mail.user</code> for Glassfish.
	 */
	private String mailSessionUser;

	/**
	 * Email message title key.
	 */
	private String backgroundReportTitleKey="background.report.email.title";

	/**
	 * Email message contents key.
	 */
	private String backgroundReportMessageKey="background.report.email.message";

	/**
	 * Message provider resource bundle path. This can be useful to initialize
	 * the message provider per database configuration.
	 */
	private String messageProviderResource = "messages.messages";

	/**
	 * The send delay between each email message processed. Useful when the SMTP
	 * server cannot handle too many messages at once.
	 */
	private Integer mailSendDelay = 0;

	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An EmailProcessor instance
	 */
	public static synchronized EmailProcessor getInstance() {
		if (instance == null)
			instance = new EmailProcessor();
		return instance;
	}

	/**
	 * The constructor which initializes the VelocityEngine instance and message
	 * provider from Vries configuration. The configuration should contain keys
	 * specified by {@link org.apertereports.common.ConfigurationConstants}.
	 */
	private EmailProcessor() {
		Collection<ConfigurationEntry> entries = org.apertereports.dao.ConfigurationDAO.fetchAll();
		for (ConfigurationEntry entry : entries) {
			if (entry.getKey().equals(MESSAGE_PROVIDER_RESOURCE)) {
				messageProviderResource = entry.getValue();
			} else if (entry.getKey().equals(MESSAGE_PROVIDER_BGREPORT_EMAIL_TITLE)) {
				backgroundReportTitleKey = entry.getValue();
			} else if (entry.getKey().equals(MESSAGE_PROVIDER_BGREPORT_EMAIL_MESSAGE)) {
				backgroundReportMessageKey = entry.getValue();
			} else if (entry.getKey().equals(JNDI_MAIL_SESSION)) {
				mailSessionJndi = entry.getValue();
			} else if (entry.getKey().equals(MAIL_SESSION_USER)) {
				mailSessionUser = entry.getValue();
			} else if (entry.getKey().equals(MAIL_SEND_DELAY)) {
				mailSendDelay = Integer.valueOf(entry.getValue());
			}
		}
		messageProvider = new ResourceBundleMessageProvider(messageProviderResource);

		Properties velocityProperties = new Properties();
		velocityProperties.setProperty("input.encoding", "UTF-8");
		velocityProperties.setProperty("output.encoding", "UTF-8");
		velocityProperties.setProperty("resource.loader", "file");
		velocityProperties.setProperty("file.resource.loader.class",
				"org.apache.velocity.runtime.resource.loader.FileResourceLoader");
		velocityProperties.setProperty("file.resource.loader.cache", "false");
		velocityProperties.setProperty("file.resource.loader.modificationCheckInterval", "0");
		velocityProperties.setProperty("runtile.log", "velocity.log");
		try {
			velocityEngine.init(velocityProperties);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Processes an email message from report order. The
	 * <code>javax.mail.session</code> instance is taken from the initial
	 * context. This should be configured in the application server hosting
	 * Vries.
	 * <p/>
	 * The email message consists of a title and a proper message - these two
	 * should be returned by the resource bundle message provider. The latter
	 * should be a velocity message template. Current implementation contains
	 * information about the report itself and a generation date.
	 * <p/>
	 * The contents of the generated report are sent as an attachment to the
	 * message.
	 * 
	 * @param reportOrder
	 * @throws Exception
	 */
    public void processEmail(final ReportOrder reportOrder) throws Exception {
		doProcessEmail(reportOrder);
		if (mailSendDelay != null && mailSendDelay > 0) {
			Thread.sleep(mailSendDelay);
		}
	}

	private synchronized void doProcessEmail(ReportOrder reportOrder) throws Exception {
		if (StringUtils.isEmpty(reportOrder.getRecipientEmail())) {
			throw new IllegalStateException("empty email recipient");
		}
		byte[] reportContent = getReportContent(reportOrder);
		String title = messageProvider.getMessage(backgroundReportTitleKey);
		if (StringUtils.isEmpty(title)) {
			throw new IllegalStateException("empty email title");
		}

		Session session = null;
		try {
			session = (javax.mail.Session) new InitialContext().lookup(mailSessionJndi);
		} catch (NamingException e) {
			logger.error(e.getMessage(), e);
		} 

		if (session == null) {
			throw new ARException(ErrorCode.EMAIL_SESSION_NOT_FOUND, mailSessionJndi);
		}

		String finalMessage = generateFinalMessage(reportOrder);

		Properties properties = session.getProperties();
		String senderEmail = properties.containsKey(Email.SENDER_EMAIL) ? properties.getProperty(Email.SENDER_EMAIL)
				: properties.containsKey(Email.MAIL_SMTP_FROM) ? properties.getProperty(Email.MAIL_SMTP_FROM)
						: properties.containsKey(Email.MAIL_SMTP_USER) ? properties.getProperty(Email.MAIL_SMTP_USER)
								: properties.containsKey(mailSessionUser) ? properties.getProperty(mailSessionUser)
										: null;
		String senderName = properties.containsKey(Email.SENDER_NAME) ? properties.getProperty(Email.SENDER_NAME)
				: senderEmail;

		if (senderEmail == null) {
			throw new IllegalStateException("Unable to determine sender's email address!");
		}
		HtmlEmail htmlEmail = new HtmlEmail();
		htmlEmail.setMailSession(session);
		htmlEmail.setFrom(senderEmail, senderName, charset);
		htmlEmail.setSubject(title);
		htmlEmail.setHtmlMsg(finalMessage);
		htmlEmail.setCharset(charset);
		String[] recipients = reportOrder.getRecipientEmail().split(";");
		for (String s : recipients) {
			htmlEmail.addTo(s, s, charset);
		}
		String contentType = ARConstants.ReportMimeType.valueOf(reportOrder.getOutputFormat()).mimeType();
		htmlEmail.attach(getReportOrderDataSource(reportOrder.getReport(), contentType, reportContent), reportOrder
				.getReport().getReportname(), reportOrder.getReport().getDescription(), EmailAttachment.ATTACHMENT);
		htmlEmail.send();
	}

	/**
	 * Generates a final message from a message template and given report order.
	 * 
	 * @param reportOrder
	 *            A report order
	 * @return Filled message template
	 * @throws AperteReportsException
	 *             on Velocity Engine error or when the message template is not
	 *             found
	 */
	private String generateFinalMessage(ReportOrder reportOrder) throws ARException {
		String messageTemplate = messageProvider.getMessage(backgroundReportMessageKey);
		if (StringUtils.isEmpty(messageTemplate)) {
			throw new IllegalStateException("Unable to find message content for email message");
		}

		VelocityContext c = new VelocityContext();
		c.put("reportOrder", reportOrder);

		StringWriter w = new StringWriter();
		try {
			velocityEngine.evaluate(c, w, "velocity_error_log.log", messageTemplate);
		} catch (IOException e) {
			throw new ARException(e);
		}

		return w.toString();
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public ResourceBundleMessageProvider getMessageProvider() {
		return messageProvider;
	}

	public void setMessageProvider(ResourceBundleMessageProvider messageProvider) {
		this.messageProvider = messageProvider;
	}

	/**
	 * Retrieves the bytes of the generated report from the instance of report
	 * order.
	 * 
	 * @param reportOrder
	 *            A report order
	 * @return Bytes of a generated report
	 * @throws Exception
	 *             on ReportMaster error
	 */
	private byte[] getReportContent(ReportOrder reportOrder) throws Exception {
		return ReportGeneratorUtils.decodeContent(reportOrder.getReportResult());
	}

	/**
	 * Wraps the report bytes into a datasource.
	 * 
	 * @param report
	 *            A ReportTemplate the contents were generated from
	 * @param contentType
	 *            Report content type
	 * @param bytes
	 *            Generated report bytes
	 * @return A new datasource for given report data
	 */
	private DataSource getReportOrderDataSource(final ReportTemplate report, final String contentType,
			final byte[] bytes) {
		return new DataSource() {
			@Override
			public String getContentType() {
				return contentType;
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(bytes);
			}

			@Override
			public String getName() {
				return report.getReportname();
			}

			@Override
			public OutputStream getOutputStream() throws IOException {
				throw new IOException("Unsupported operation");
			}
		};
	}
}
