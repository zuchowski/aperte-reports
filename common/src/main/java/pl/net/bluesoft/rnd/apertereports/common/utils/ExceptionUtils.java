package pl.net.bluesoft.rnd.apertereports.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception utility class. Contains convenient methods for logging
 * exceptions. Methods are self-explanatory.
 */
public final class ExceptionUtils {
	private static Logger logger = LoggerFactory.getLogger(ExceptionUtils.class);

	public static void logWarningException(String msg, Exception e) {
		logger.warn(msg, e);
	}

	public static void logSevereException(Exception e) {
		logger.error(e.getLocalizedMessage(), e);
	}

	public static void logSevereException(String msg, Exception e) {
		logger.error(msg, e);
	}

	public static void logDebugMessage(String msg) {
		logger.debug(msg);
	}
}
