/**
 *
 */
package org.apertereports.common.utils;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apertereports.common.ARConstants;

/**
 * Utility methods for converting Vaadin value to a simple string.
 */
public final class TextUtils implements ARConstants {

	private TextUtils() {
	}

	/**
	 * Encodes a value to a string so that an SQL query can use it. Converts
	 * collections to a single coma separated string.
	 *
	 * @param rawValue
	 *            given value
	 * @return encoded value
	 */
	public static String encodeObjectToSQL(Object rawValue) {
		return encodeObjectToSQL(rawValue, null);
	}

	public static String encodeObjectToSQL(Object rawValue, SimpleDateFormat dateFormat) {
		StringBuilder value = new StringBuilder();

		/*
		 * change values into Strings
		 */
		if (rawValue instanceof String) {
			value.append((String) rawValue);
		} else if (rawValue instanceof Object[]) {
			String sep = "'";
			for (Object val : (Object[]) rawValue) {
				if (!val.toString().isEmpty()) {
					value.append(sep).append(val).append("'");
					sep = ",'";
				}
			}
		} else if (rawValue instanceof Collection<?>) {
			String sep = "'";
			for (Object val : (Collection<?>) rawValue) {
				if (!val.toString().isEmpty()) {
					value.append(sep).append(val).append("'");
					sep = ",'";
				}
			}
		} else if (rawValue instanceof Date) {
			value.append(TimeUtils.getDateFormatted(((Date) rawValue), dateFormat));
		} else {
			value.append(rawValue.toString());
		}
		return value.toString();
	}

	private static String trimApostrophes(String input) {
		if (input.startsWith("'") && input.endsWith("'")) {
			return input.substring(input.length() > 1 ? 1 : 0, input.length() - 1);
		}
		return input;
	}

	/**
	 * Reads a text file from an <code>InputStream</code> to string.
	 *
	 * @param s
	 *            an InputStream
	 * @return a string containing file contents
	 * @throws IOException
	 *             on read error
	 */
	public static String readTestFileToString(InputStream s) throws IOException {
		StringBuilder ds = new StringBuilder();
		int c;
		while ((c = s.read()) >= 0) {
			ds.append((char) c);
		}
		return ds.toString();
	}

	/**
	 * Decodes a previously encoded value to its base form.
	 *
	 * @param type
	 *            The type of the value.
	 * @param value
	 *            The input string
	 * @return Decoded value object
	 * @throws ParseException
	 *             On date format error
	 */
	public static Object encodeSQLToObject(Class<?> type, String value) throws ParseException {
		if (type == null || String.class.isAssignableFrom(type)) {
			return value;
		}
		if (Date.class.isAssignableFrom(type)) {
			return TimeUtils.getDateFormatted(value);
		}
		if (Object[].class.isAssignableFrom(type)) {
			return trimListApostrophes(value);
		}
		if (Collection.class.isAssignableFrom(type)) {
			String[] values = trimListApostrophes(value);
			return Arrays.asList(values);
		}
		return value;
	}

	private static String[] trimListApostrophes(String input) {
		String[] values = input.split(",");
		for (int i = 0; i < values.length; ++i) {
			values[i] = trimApostrophes(values[i]);
		}
		return values;
	}

	/**
	 * Returns comma-separated string representation of objects in the list
	 *
	 * @param list
	 *            List of objects
	 * @return Comma-separated string representation of objects
	 */
	public static String getCommaSeparatedString(List<Object> list) {
		StringBuilder sb = new StringBuilder();

		if (list.isEmpty()) {
			return "";
		}

		Iterator it = list.iterator();
		sb.append(it.next());
		while (it.hasNext()) {
			sb.append(",").append(it.next());
		}

		return sb.toString();
	}

	// /**
	// * @return {@link void}
	// * @history MW-ITSD;15.12.2016
	// */
	// public static String encodeObjectToSQL(Object rawValue, SimpleDateFormat
	// pattern) {
	// // return encodeObjectToSQL(rawValue);
	// // return pattern.format(rawValue).toString();
	//
	// }
}
