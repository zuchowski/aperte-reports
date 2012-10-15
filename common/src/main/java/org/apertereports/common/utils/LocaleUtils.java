package org.apertereports.common.utils;

import java.util.Locale;

/**
 * Class defines useful locale methods
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class LocaleUtils {

    /**
     * Creates {@link java.util.Locale} object according to provided locale
     * string
     *
     * @param localeString Locale string, e.g. "en_GB", "de"
     * @return Locale object or default one if locale string is incorrect
     */
    public static Locale createLocale(String localeString) {

        String[] val = localeString.split("_");
        if (val.length == 1) {
            return new Locale(val[0]);
        } else if (val.length == 2) {
            return new Locale(val[0], val[1]);
        }

        return Locale.getDefault();
    }
}
