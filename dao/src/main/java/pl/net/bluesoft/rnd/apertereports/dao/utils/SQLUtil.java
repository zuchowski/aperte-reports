package pl.net.bluesoft.rnd.apertereports.dao.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;

/**
 * DAO utility class. Initializes Hibernate session factory with annotated
 * classes. Has methods for providing a new Hibernate session.
 */
public class SQLUtil {
	protected static SessionFactory sessions;

	/**
	 * Static initializer of the session factory.
	 */
	static {
		configureSessions();
	}

	private static void configureSessions() {
		try {
			AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration();
			Configuration cfg = annotationConfiguration.configure("../hibernate.cfg.xml");

			sessions = cfg.buildSessionFactory();
			sessions.getStatistics().setStatisticsEnabled(true);
		} catch (Exception e) {
			ExceptionUtils.logSevereException(e);
		}
	}

	/**
	 * Used to open a new Hibernate session.
	 * 
	 * @return A new DAO session
	 */
	public static Session getSession() {
		if (sessions == null)
			configureSessions();
		return sessions.openSession();
	}

	/**
	 * Gets the session factory.
	 * 
	 * @return A session factory
	 */
	public static SessionFactory getSessionFactory() {
		return sessions;
	}

	protected SQLUtil() {
	}

}
