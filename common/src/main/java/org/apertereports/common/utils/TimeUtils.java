package org.apertereports.common.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apertereports.common.ARConstants;

/**
 * An utility for converting dates and time.
 */
public class TimeUtils implements ARConstants {

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(DATETIME_PATTERN);

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
     * @param date Input date string
     * @return A date object
     * @throws ParseException On date parser error
     */
    public static synchronized Date getDateFormatted(String date) throws ParseException {
        return DATETIME_FORMAT.parse(date);
    }

    /**
     * Converts a given date to string using a default date format.
     *
     * @param date Input date
     * @return Formatted string
     */
    public static synchronized String getDateFormatted(Date date) {
        return DATETIME_FORMAT.format(date);
    }

    /**
     * Converts seconds to milliseconds.
     *
     * @param seconds Seconds
     * @return Milliseconds
     */
    public static long secondsToMilliseconds(int seconds) {
        return 1000L * seconds;
    }
}
