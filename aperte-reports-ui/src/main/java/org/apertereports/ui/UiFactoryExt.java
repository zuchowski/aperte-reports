package org.apertereports.ui;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Label;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.util.VaadinUtil;

/**
 * Class provides additional useful methods for creating UI components
 *
 * @see UiFactory
 * @author Zbigniew Malinowski
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public abstract class UiFactoryExt {

    private static final String REPORT_MANAGER_DATE_FORMAT = "report.manager.date.format";

    /**
     * Creates date label bound to date property.
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @return Label
     */
    public static Label createDateLabel(Item item, String propertyId, String style, ComponentContainer parent) {
        Property property = new DateProperty(item.getItemProperty(propertyId));
        return createLabel(property, style, parent, UiFactory.EMPTY_ACTION_TABLE);
    }

    /**
     * Creates date label bound to date property.
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @param actions List of actions performed on created component
     * @return Label
     */
    public static Label createDateLabel(Item item, String propertyId, String style, ComponentContainer parent, FAction... actions) {
        Property property = new DateProperty(item.getItemProperty(propertyId));
        return createLabel(property, style, parent, actions);
    }

    /**
     * Creates calendar label bound to time property.
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @return Label
     */
    public static Label createCalendarLabel(Item item, String propertyId, ComponentContainer parent) {
        Property property = new CalendarProperty(item.getItemProperty(propertyId));
        return createLabel(property, null, parent, UiFactory.EMPTY_ACTION_TABLE);
    }

    /**
     * Creates calendar label bound to time property.
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @param actions List of actions performed on created component
     * @return Label
     */
    public static Label createCalendarLabel(Item item, String propertyId, String style, ComponentContainer parent, FAction... actions) {
        Property property = new CalendarProperty(item.getItemProperty(propertyId));
        return createLabel(property, style, parent, actions);
    }

    private static Label createLabel(Property property, String style, ComponentContainer parent, FAction... actions) {
        Label label = new Label(property);
        label.setStyleName(style);
        if (parent != null) {
            parent.addComponent(label);
        }
        label.setWidth(null);
        UiFactory.performActions(label, actions);
        return label;
    }

    private static class CalendarProperty extends DateProperty {

        public CalendarProperty(Property property) {
            super(property);
        }

        @Override
        public String format(Object value) {
            return super.format(((Calendar) value).getTime());
        }

        @Override
        public Object parse(String formattedValue) throws Exception {
            GregorianCalendar cal = new GregorianCalendar();
            cal.setTime(dateFormat.parse(formattedValue));
            return cal;
        }
    }

    @SuppressWarnings({"unchecked", "serial"})
    private static class DateProperty extends PropertyFormatter {

        protected SimpleDateFormat dateFormat;

        public DateProperty(Property property) {
            super(property);
        }

        private void init() {
            if (dateFormat == null) {
                dateFormat = new SimpleDateFormat(VaadinUtil.getValue(REPORT_MANAGER_DATE_FORMAT));
            }
        }

        @Override
        public String format(Object value) {
            init();
            return dateFormat.format((Date) value);
        }

        @Override
        public Object parse(String formattedValue) throws Exception {
            init();
            return dateFormat.parse(formattedValue);
        }
    }

    /**
     * Creates combo box for locale selection
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param locale Current locale
     * @return Combo
     */
    public static ComboBox createLocaleCombo(String captionId, Locale locale) {
        List<Locale> allLocales = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(allLocales, new Comparator<Locale>() {

            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        Container all = new BeanItemContainer<Locale>(Locale.class, allLocales);
        ComboBox reportLocale = new ComboBox(VaadinUtil.getValue(captionId),
                all);
        reportLocale.setValue(locale);
        reportLocale.setStyleName("small");
        reportLocale.setNullSelectionAllowed(false);
        reportLocale.setTextInputAllowed(true);
        return reportLocale;
    }
}
