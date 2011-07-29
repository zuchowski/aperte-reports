package pl.net.bluesoft.rnd.apertereports.common.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An exception utility class. Contains convenient methods for logging exceptions.
 * Methods are self-explanatory.
 */
public final class ExceptionUtils {
    private static Logger logger = Logger.getLogger(ExceptionUtils.class.getCanonicalName());

    public static void logWarningException(String msg, Exception e) {
        logger.log(Level.WARNING, msg, e);
    }

    public static void logSevereException(Exception e) {
        logger.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }

    public static void logSevereException(String msg, Exception e) {
        logger.log(Level.SEVERE, msg, e);
    }

    public static void logDebugMessage(String msg) {
        logger.log(Level.FINE, msg);
    }
}
