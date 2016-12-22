package org.apertereports.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apertereports.common.ARConstants;

/**
 * An utility for converting dates and time.
 */
public final class TimeUtils implements ARConstants {

	private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(DATETIME_PATTERN);

	private TimeUtils() {
	}

	/**
	 * Returns a default date format.
	 *
	 * @return Datetime format
	 */
	public static String getDefaultDateFormat() {

		return DATETIME_PATTERN;
	}

	/**
	 * Method that formats a date from string using a default date format.
	 *
	 * @param date
	 *            Input date string
	 * @return A date object
	 * @throws ParseException
	 *             On date parser error
	 */
	public static synchronized Date getDateFormatted(String date) throws ParseException {
		return DATETIME_FORMAT.parse(date);
	}

	/**
	 * Converts a given date to string using a default date format.
	 *
	 * @param date
	 *            Input date
	 * @return Formatted string
	 */
	public static synchronized String getDateFormatted(Date date) {
		// return DATETIME_FORMAT.format(date);
		return getDateFormatted(date, null);
	}

	/**
	 * get formatted date with given pattern. If pattern is null use default
	 * pattern
	 * 
	 * @param {@link Date} date
	 * @param {@link {@link SimpleDateFormat} datePattern
	 * @return {@link String}
	 * @history MW-ITSD;15.12.2016
	 */
	public static synchronized String getDateFormatted(Date date, SimpleDateFormat datePattern) {
		if (datePattern != null) {
			return datePattern.format(date);
		} else {
			return DATETIME_FORMAT.format(date);
		}
	}

	/**
	 * Converts seconds to milliseconds.
	 *
	 * @param seconds
	 *            Seconds
	 * @return Milliseconds
	 */
	public static long secondsToMilliseconds(int seconds) {
		return 1000L * seconds;
	}
}
