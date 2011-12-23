package org.apertereports.common.wrappers;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * This class filters dictionary items by levels.
 */
public class DictionaryItemFilter {
    private List<String>[] columns;

    public DictionaryItemFilter(int columnCount, HashMap<Integer, Object> filterValues) {
        columns = new List[columnCount];
        for (Entry<Integer, Object> filter : filterValues.entrySet()) {
            if (filter.getValue() instanceof Collection) {
                Collection<String> col = (Collection<String>) filter.getValue();
                if (!col.isEmpty()) {
                    columns[filter.getKey()] = new ArrayList<String>(col);
                }
            }
            else if (filter.getValue() instanceof String) {
                String string = (String) filter.getValue();
                if (StringUtils.isNotEmpty(string)) {
                    columns[filter.getKey()] = new ArrayList<String>();
                    columns[filter.getKey()].add(string);
                }
            }
        }
    }

    public boolean appliesTo(Object obj) {
        if (obj instanceof DictionaryItem) {
            int idx = 1;
            for (List<String> list : columns) {
                if (list != null && !list.contains(((DictionaryItem) obj).getColumn(idx))) {
                    return false;
                }
                idx++;
            }
            return true;
        }
        return super.equals(obj);
    }

    public List<String> getColumn(Integer idx) {
        return columns[idx];
    }

    public void setColumn(Integer idx, List<String> value) {
        columns[idx] = value;
    }

}
