package org.apertereports.dao;

import org.hibernate.criterion.Restrictions;
import org.apertereports.model.ConfigurationEntry;
import org.apertereports.model.ConfigurationEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class ConfigurationDAO {
    public static Collection<ConfigurationEntry> loadAll() {
        return new org.apertereports.dao.utils.WHS<Collection<ConfigurationEntry>>() {
            @Override
            public Collection<ConfigurationEntry> lambda() {
                return sess.createCriteria(ConfigurationEntry.class).list();
            }
        }.p();
    }

    public static List<ConfigurationEntry> findById(final String... key) {
        return new org.apertereports.dao.utils.WHS<List<ConfigurationEntry>>(false) {
            @Override
            public List<ConfigurationEntry> lambda() {
                if (key.length == 0) {
                    return new ArrayList<ConfigurationEntry>();
                }
                List<ConfigurationEntry> list = sess.createCriteria(ConfigurationEntry.class)
                        .add(Restrictions.in("key", key))
                        .list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<ConfigurationEntry>();
                }
                return list;
            }
        }.p();
    }

    public static HashMap<String, String> loadAllToMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        Collection<ConfigurationEntry> all = loadAll();
        for (ConfigurationEntry entry : all) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
