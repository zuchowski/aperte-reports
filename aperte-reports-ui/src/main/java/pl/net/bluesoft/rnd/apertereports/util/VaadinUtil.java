package pl.net.bluesoft.rnd.apertereports.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Formatter;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class VaadinUtil {

    private static Map<Thread, Locale> LOCALE_THREAD_MAP = new HashMap<Thread, Locale>();

    public static synchronized void setThreadLocale(Locale l) {
        LOCALE_THREAD_MAP.put(Thread.currentThread(), l);
    }

    public static synchronized void unsetThreadLocale() {
        LOCALE_THREAD_MAP.remove(Thread.currentThread());
    }

    public static String getValue(String key, Object... params) {
        String v = getValue(key);
        StringBuilder sb = new StringBuilder();
        Formatter fm = new Formatter(sb, nvl(LOCALE_THREAD_MAP.get(Thread.currentThread()), Locale.getDefault()));
        fm.format(v, params);
        return sb.toString();
    }
    public static String getValue(String key) {
        ResourceBundle bundle = ResourceBundle.getBundle("ui-messages",
                nvl(LOCALE_THREAD_MAP.get(Thread.currentThread()), Locale.getDefault()));
        if (bundle.containsKey(key))
            return nvl(bundle.getString(key), key);
        return key;
    }

    public static<T> T nvl(T... values) {
        for (T t: values) {
            if (t != null) return t;
        }
        return null;
    }

}
