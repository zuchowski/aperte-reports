/**
 *
 */
package org.apertereports.backbone.tests;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author MW
 */
public class TestUtil {
    final public static String dbConf = "/db.properties";

    public static SimpleNamingContextBuilder initDB() throws IllegalStateException, NamingException, IOException {

        SimpleNamingContextBuilder ic = new SimpleNamingContextBuilder();
        BasicDataSource ds = new BasicDataSource();

        InputStream stream = TestUtil.class.getResourceAsStream(dbConf);
        Properties properties = new Properties();
        properties.load(stream);

        ds.setUrl(properties.getProperty("url"));
        ds.setUsername(properties.getProperty("username"));
        ds.setPassword(properties.getProperty("password"));
        /*ds.setUrl("jdbc:postgresql://192.168.0.240:5432/unirep");
          ds.setUsername("unirep");
          ds.setPassword("unirep");*/
        ds.setDriverClassName("org.postgresql.Driver");
        ic.bind("java:comp/env/jndids", ds);
        ic.bind("jdbc/unirep", ds);
        ic.activate();

        return ic;
    }

    public TestUtil() {

    }

    @Test
    public void testInitialization() throws IllegalStateException, NamingException, IOException {
        initDB();
        org.apertereports.dao.utils.SQLUtil.getSession();
    }
}
