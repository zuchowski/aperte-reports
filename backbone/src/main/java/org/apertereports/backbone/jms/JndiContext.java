package org.apertereports.backbone.jms;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apertereports.common.ARConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class allows to lookup in JNDI context for an object with a given name. When
 * the first lookup operation is performed, this class initializes JNDI context
 * java:comp/env.
 * <p>
 * This class was added to enable running application on JBoss AS 7.
 *
 * @author Tomasz Serafin, BlueSoft Sp. z o.o.
 */
public class JndiContext {

    private static Logger logger = LoggerFactory.getLogger(JndiContext.class);
    private static Context ctx;

    public static Object lookup(String name) throws NamingException {
        init();

        logger.debug("looking for " + name);
        return ctx.lookup(name);
    }

    private static void init() throws NamingException {
        if (ctx != null) {
            return;
        }
        logger.info("Initializing JNDI context " + ARConstants.JNDI_DEFAULT_CONTEXT);
        ctx = (Context) new InitialContext().lookup(ARConstants.JNDI_DEFAULT_CONTEXT);
        logger.info("done");
    }
}
