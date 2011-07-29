package pl.net.bluesoft.rnd.apertereports.generators;

import com.vaadin.data.Property;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import org.apache.commons.lang.StringUtils;

/**
 * Generates a styled checkbox column for Vaadin tables.
 */
public class CheckBoxColumnGenerator implements Table.ColumnGenerator {
    private static final long serialVersionUID = -2879841558851774016L;

    public CheckBoxColumnGenerator() {
    }

    @Override
    public Component generateCell(Table source, Object itemId, Object columnId) {
        // Get the object stored in the cell as a property
        Property prop = source.getItem(itemId).getItemProperty(columnId);
        if (prop.getType().equals(Boolean.class)) {
            CheckBox checkbox = new CheckBox();
            checkbox.setValue(Boolean.TRUE.equals(prop.getValue()));
            checkbox.setReadOnly(true);
            // Set styles for the column: one indicating that it's
            // a value and a more specific one with the column
            // name in it. This assumes that the column name
            // is proper for CSS.
            checkbox.addStyleName("column-type-value");
            checkbox.addStyleName("column-" + StringUtils.lowerCase(columnId.toString()));
            return checkbox;
        }
        return null;
    }
}
