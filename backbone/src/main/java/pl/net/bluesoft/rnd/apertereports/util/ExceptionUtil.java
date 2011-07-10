package pl.net.bluesoft.rnd.apertereports.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An exception utility class. Contains convenient methods for logging exceptions.
 * Methods are self-explanatory.
 */
public final class ExceptionUtil {
    private static Logger logger = Logger.getLogger(ExceptionUtil.class.getCanonicalName());

    public static void logWarningException(String msg, Exception e) {
        logger.log(Level.WARNING, msg, e);
    }

    public static void logSevereException(Exception e) {
        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    public static void logInfoException(Exception e) {
        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    public static void logSevereException(String msg, Exception e) {
        logger.log(Level.SEVERE, msg, e);
    }

    public static void logDebug(String msg) {
        logger.log(Level.FINE, msg);
    }
}
