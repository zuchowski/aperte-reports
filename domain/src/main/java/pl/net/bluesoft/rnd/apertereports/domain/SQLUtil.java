package pl.net.bluesoft.rnd.apertereports.domain;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.domain.model.CyclicReportOrder;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportOrder;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.domain.model.VriesConfigurationEntry;

/**
 * DAO utility class. Initializes Hibernate session factory with annotated classes. Has methods for providing a new Hibernate session.
 */
public class SQLUtil {
    protected static SessionFactory sessions;

    /**
     * Static initializer of the session factory.
     */
    static {
        try {
            final AnnotationConfiguration annotationConfiguration = new AnnotationConfiguration();

            annotationConfiguration.addAnnotatedClass(ReportTemplate.class);
            annotationConfiguration.addAnnotatedClass(ReportOrder.class);
            annotationConfiguration.addAnnotatedClass(CyclicReportOrder.class);
            annotationConfiguration.addAnnotatedClass(VriesConfigurationEntry.class);

            Configuration cfg = null;
            try {
                cfg = annotationConfiguration.configure("../hibernate.cfg.xml");
            }
            catch (HibernateException e) {
                ExceptionUtils.logSevereException(e);
                try {
                    cfg = annotationConfiguration.configure("hibernate.cfg.xml");
                }
                catch (HibernateException ex) {
                    ExceptionUtils.logSevereException(ex);
                }
            }

            cfg.setProperty("hibernate.connection.autocommit", "false");
            cfg.setProperty("hibernate.hbm2ddl.auto", "update");
            cfg.setProperty("hibernate.hbm2ddl", "update");
            cfg.setProperty("hibernate.jdbc.batch_size", "50");
            cfg.setProperty("hibernate.show_sql", "true");
            cfg.setProperty("hibernate.use_outer_join", "true");
            cfg.setProperty("hibernate.cglib.use_reflection_optimizer", "true");

            sessions = cfg.buildSessionFactory();
            sessions.getStatistics().setStatisticsEnabled(true);
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
        }

    }

    /**
     * Used to open a new Hibernate session.
     *
     * @return A new DAO session
     */
    public static Session getSession() {
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
