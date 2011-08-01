package pl.net.bluesoft.rnd.apertereports.dao;

import pl.net.bluesoft.rnd.apertereports.common.wrappers.DictionaryItem;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

/**
 * DAO methods for retrieving custom dictionaries from database and report parameter strings.
 *
 * @see DictionaryItem
 */
public class DictionaryDAO {
    /**
     * This method returns a list of dictionary items returned by the query. The query contains
     * a JNDI resource name separated by a semicolon from a proper SQL query.
     *
     * @param dictQuery A JNDI resource name separated by a semicolon from an SQL query.
     * @return A list of dictionary items
     */
    public static List<DictionaryItem> fetchDictionary(String dictQuery) {
        String[] dictQueryArray = dictQuery.split(";", 2);
        String jndiName = dictQueryArray[0];
        String procedureName = dictQueryArray[1];
        try {
            List<DictionaryItem> res = new LinkedList<DictionaryItem>();

            DataSource ds;
            try {
                ds = (DataSource) new InitialContext().lookup(jndiName);
            }
            catch (Exception e1) {
                String prefix = "java:comp/env/";
                if (jndiName.matches(prefix + ".*")) {
                    ds = (DataSource) new InitialContext().lookup(jndiName.substring(prefix.length()));
                }
                else {
                    ds = (DataSource) new InitialContext().lookup(prefix + jndiName);
                }
            }
            Connection c = ds.getConnection();
            try {
                c.setAutoCommit(false);
                PreparedStatement ps = c.prepareStatement("select * from ( " + procedureName + " ) as data");
                try {
                    final ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            DictionaryItem e = new DictionaryItem(rs.getMetaData().getColumnCount());
                            e.setCode(rs.getString(1));
                            e.setDescription(rs.getString(2));
                            res.add(e);
                            for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                e.setColumn(i - 1, rs.getString(i));
                            }
                        }
                    }
                    finally {
                        rs.close();
                    }
                }
                finally {
                    ps.close();
                }
            }
            finally {
                c.rollback(); // żeby komuś nie przyszło coś głupiego do głowy
                c.close();
            }
            Collections.sort(res, new Comparator<DictionaryItem>() {
                @Override
                public int compare(DictionaryItem o1, DictionaryItem o2) {
                    return (o1.getDescription() + "").compareToIgnoreCase(o2.getDescription() + "");
                }
            });
            return res;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Separates an input string into dictionary items. This is used to parse a report parameter
     * containing static dict values.
     *
     * @param dictItemList A dictionary list report property value
     * @return A list of dictionary items
     */
    public static List<DictionaryItem> readDictionaryFromString(String dictItemList) {
        String[] items = dictItemList.split(",|;");
        List<DictionaryItem> itemList = new ArrayList<DictionaryItem>(items.length);
        for (String item : items) {
            String[] columns = item.split(":|=>");
            DictionaryItem dictItem = new DictionaryItem(columns[0], columns[1], columns);
            itemList.add(dictItem);
        }
        return itemList;
    }
}
