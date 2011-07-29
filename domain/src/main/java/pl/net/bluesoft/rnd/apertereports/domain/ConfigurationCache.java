package pl.net.bluesoft.rnd.apertereports.domain;

import pl.net.bluesoft.rnd.apertereports.common.ConfigurationConstants;
import pl.net.bluesoft.rnd.apertereports.domain.dao.VriesConfigurationDAO;

import java.util.Calendar;
import java.util.Map;

/**
 * A static cache for configuration entries. Thread-safe.
 */
public class ConfigurationCache implements ConfigurationConstants {
    /**
     * Map cache.
     */
    static Map<String, String> configuration;
    /**
     * Date when the cache expires.
     */
    static Calendar validUntil;

    /**
     * Gets a single configuration entry corresponding to given key.
     *
     * @param key Entry key
     * @return Entry value
     */
    public static String getValue(String key) {
        if (key == null) {
            return null;
        }
        init();

        synchronized (ConfigurationCache.class) {
            if (configuration.containsKey(key)) {
                return configuration.get(key);
            }
            else {
                return null;
            }
        }
    }

    /**
     * Initializes the cache. Retrieves the configuration from database and sets the expiration date.
     */
    synchronized private static void init() {
        if (configuration == null || Calendar.getInstance().after(validUntil)) {
            configuration = VriesConfigurationDAO.loadAllToMap();
            String timeout = configuration.get(CONFIGURATION_CACHE_TIMEOUT_IN_MINUTES);
            if (timeout == null) timeout = "15";
            validUntil = Calendar.getInstance();
            validUntil.add(Calendar.MINUTE, Integer.valueOf(timeout));
        }
    }

    /**
     * Returns all the configuration entries as map.
     *
     * @return the configuration The configuration entries as map
     */
    public static Map<String, String> getConfiguration() {
        init();
        return configuration;
    }

}
