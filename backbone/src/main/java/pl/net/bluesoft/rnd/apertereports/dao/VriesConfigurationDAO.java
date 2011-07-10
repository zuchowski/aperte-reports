package pl.net.bluesoft.rnd.apertereports.dao;

import pl.net.bluesoft.rnd.apertereports.data.VriesConfigurationEntry;
import pl.net.bluesoft.rnd.apertereports.util.WHS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.hibernate.criterion.Restrictions;

public class VriesConfigurationDAO {
    public static Collection<VriesConfigurationEntry> loadAll() {
        return new WHS<Collection<VriesConfigurationEntry>>() {
            @Override
            public Collection<VriesConfigurationEntry> lambda() {
                return sess.createCriteria(VriesConfigurationEntry.class).list();
            }
        }.p();
    }

    public static List<VriesConfigurationEntry> findById(final String... key) {
        return new WHS<List<VriesConfigurationEntry>>(false) {
            @Override
            public List<VriesConfigurationEntry> lambda() {
                if (key.length == 0) {
                    return new ArrayList<VriesConfigurationEntry>();
                }
                List<VriesConfigurationEntry> list = sess.createCriteria(VriesConfigurationEntry.class)
                        .add(Restrictions.in("key", key))
                        .list();
                if (list == null || list.size() == 0) {
                    return new ArrayList<VriesConfigurationEntry>();
                }
                return list;
            }
        }.p();
    }

    public static HashMap<String, String> loadAllToMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        Collection<VriesConfigurationEntry> all = loadAll();
        for (VriesConfigurationEntry entry : all) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
}
