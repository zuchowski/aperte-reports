package pl.net.bluesoft.rnd.vries.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * An utility for converting dates and time.
 */
public class TimeUtil implements Constants {
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
    public static Date getDateFormatted(String date) throws ParseException {
        return new SimpleDateFormat(getDefaultDateFormat()).parse(date);
    }

    /**
     * Converts a given date to string using a default date format.
     *
     * @param date Input date
     * @return Formatted string
     */
    public static String getDateFormatted(Date date) {
        return new SimpleDateFormat(getDefaultDateFormat()).format(date);
    }

    /**
     * Converts seconds to milliseconds.
     *
     * @param seconds Seconds
     * @return Milliseconds
     */
    public static long secondsToMilliseconds(int seconds) {
        return 1000 * seconds;
    }

}
