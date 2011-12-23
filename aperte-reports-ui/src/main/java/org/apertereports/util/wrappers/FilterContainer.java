package org.apertereports.util.wrappers;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.ui.Select;
import com.vaadin.ui.VerticalLayout;
import org.apertereports.common.wrappers.DictionaryItem;
import org.apertereports.common.wrappers.DictionaryItemFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/**
 * A container wrapper for multilevel filters in the report parameter forms.
 * Each filter is a select that can be configured independently.
 * The first level is always the main select which carries the final value.
 */
public class FilterContainer extends VerticalLayout {
    HashMap<Integer, Select> levels;
    private List<DictionaryItem> items;

    public FilterContainer() {
        super();
        levels = new HashMap<Integer, Select>();
    }

    /**
     * Adds a level of filtering.
     *
     * @param select Base select that should be filtered
     * @param level  A level of filtering
     * @param items  Items to show in the filter
     */
    public void addFilter(final Select select, final Integer level, final List<DictionaryItem> items) {
        this.addComponent(select);
        levels.put(level, select);
        if (items != null) {
            this.items = items;
        }
        select.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                filterFields(level);
            }
        });
        fillEmptySelects(this.items);
    }

    /**
     * Adds a validator to the first filter - the one the value is taken from eventually.
     *
     * @param validator A Vaadin validator
     */
    public void addValidator(Validator validator) {
        levels.get(0).addValidator(validator);

    }

    /**
     * Gets a list of filtering levels.
     *
     * @return A list of filtering levels
     */
    public List<Select> getLevels() {
        List<Select> selects = new ArrayList<Select>(levels.size());
        for (int i = levels.size() - 1; i >= 0; i--) {
            selects.add(levels.get(i));
        }
        return selects;
    }

    /**
     * Gets a value from the first level.
     *
     * @return A value of the whole filtering widget
     */
    public Object getValue() {
        return levels.get(0).getValue();
    }

    /**
     * Sets the value in the first level.
     *
     * @param value
     */
    public void setValue(Object value) {
        levels.get(0).setValue(value);
    }

    /**
     * Validates the value in the first level.
     */
    public void validate() {
        levels.get(0).validate();
    }

    /**
     * Fills the filter select when added or changed.
     *
     * @param filteredItems Filtered items
     */
    private void fillEmptySelects(List<DictionaryItem> filteredItems) {
        if (filteredItems != null) {
            for (Entry<Integer, Select> entry : levels.entrySet()) {
                Integer key = entry.getKey();
                Select value = entry.getValue();
                if (value.size() == 0) {
                    for (DictionaryItem item : filteredItems) {
                        String itemId;
                        if (key == 0) {
                            itemId = item.getColumn(0);
                        }
                        else {
                            itemId = item.getColumn(key + 1);
                        }
                        value.addItem(itemId);
                        value.setItemCaption(itemId, item.getColumn(key + 1));
                    }
                }
            }
        }
    }

    /**
     * Filter items from higher levels.
     *
     * @param filterValues The values filtered from the previous level.
     * @return A list of filtered items
     */
    private List<DictionaryItem> filterItems(HashMap<Integer, Object> filterValues) {
        List<DictionaryItem> filteredItems = new LinkedList<DictionaryItem>();
        DictionaryItemFilter filter = new DictionaryItemFilter(levels.size() + 1, filterValues);

        for (DictionaryItem item : items) {
            if (filter.appliesTo(item)) {
                filteredItems.add(item);
            }
        }
        return filteredItems;
    }

    /**
     * Filters values beginning from a #startLevel.
     *
     * @param startLevel The level the filtering starts
     */
    protected void filterFields(Integer startLevel) {
        HashMap<Integer, Object> filterValues = new HashMap<Integer, Object>(levels.size());
        if (items != null) {
            for (Entry<Integer, Select> entry : levels.entrySet()) {
                Integer key = entry.getKey();
                Select value = entry.getValue();
                filterValues.put(key, value.getValue());
                if (key < startLevel) {
                    value.removeAllItems();
                }
            }
        }
        List<DictionaryItem> filteredItems = filterItems(filterValues);
        fillEmptySelects(filteredItems);
    }
}
