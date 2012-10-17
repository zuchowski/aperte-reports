package org.apertereports.dao.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;

import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.common.utils.ExceptionUtils;

/**
 * DAO utility class. Initializes Hibernate session factory with annotated
 * classes. Has methods for providing a new Hibernate session.
 */
public class SQLUtil {

    /**
     * Session factory
     */
    protected static SessionFactory sessionFactory;

    /**
     * Static initializer of the session factory.
     */
    static {
        configureSessions();
    }

    private static void configureSessions() {
        try {
            Configuration annotationConfiguration = new AnnotationConfiguration();
            Configuration cfg = annotationConfiguration.configure("hibernate.cfg.xml");

            sessionFactory = cfg.buildSessionFactory();
            sessionFactory.getStatistics().setStatisticsEnabled(true);
        } catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            throw new AperteReportsRuntimeException(e);
        }
    }

    /**
     * Used to open a new Hibernate session.
     *
     * @return A new DAO session
     */
    public static Session getSession() {
        if (sessionFactory == null) {
            configureSessions();
        }
        return sessionFactory.openSession();
    }

    /**
     * Gets the session factory.
     *
     * @return A session factory
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            configureSessions();
        }
        return sessionFactory;
    }

    /**
     * Creates new instance
     */
    protected SQLUtil() {
    }
}
