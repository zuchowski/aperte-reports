package org.apertereports.dao;

import org.hibernate.criterion.Restrictions;
import org.apertereports.model.ConfigurationEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO methods for retrieving and saving configuration
 */
public final class ConfigurationDAO {

    private ConfigurationDAO() {
    }

    /**
     * Returns all configuration entries
     *
     * @return A collection containing configuration entries
     */
    public static Collection<ConfigurationEntry> fetchAll() {
        return new org.apertereports.dao.utils.WHS<Collection<ConfigurationEntry>>() {

            @Override
            public Collection<ConfigurationEntry> lambda() {
                return sess.createCriteria(ConfigurationEntry.class).list();
            }
        }.p();
    }

    /**
     * Returns configuration entries with given keys
     *
     * @param keys Keys
     * @return A collection containing configuration entries
     */
    public static List<ConfigurationEntry> fetchByKeys(final String... keys) {
        return new org.apertereports.dao.utils.WHS<List<ConfigurationEntry>>(false) {

            @Override
            public List<ConfigurationEntry> lambda() {
                if (keys.length == 0) {
                    return new ArrayList<ConfigurationEntry>();
                }
                List<ConfigurationEntry> list = sess.createCriteria(ConfigurationEntry.class).add(Restrictions.in("key", keys)).list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<ConfigurationEntry>();
                }
                return list;
            }
        }.p();
    }

    /**
     * Returns configuration entries as a map of key and values
     *
     * @return A map containing configuration keys and values
     */
    public static Map<String, String> fetchAllToMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        Collection<ConfigurationEntry> all = fetchAll();
        for (ConfigurationEntry entry : all) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
