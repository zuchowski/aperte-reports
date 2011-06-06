package pl.net.bluesoft.rnd.vries.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception utility class. Contains convenient methods for logging exceptions.
 * Methods are self-explanatory.
 */
public final class ExceptionUtil {
    private static Logger logger = LoggerFactory.getLogger(ExceptionUtil.class.getCanonicalName());

    public static void logWarningException(String msg, Exception e) {
        logger.warn(msg, e);
    }

    public static void logSevereException(Exception e) {
        logger.error(e.getLocalizedMessage(), e);
    }

    public static void logInfoException(Exception e) {
        logger.info(e.getLocalizedMessage(), e);
    }

    public static void logSevereException(String msg, Exception e) {
        logger.error(msg, e);
    }

    public static void logDebug(String msg) {
        logger.debug(msg);
    }
}
