package org.apertereports.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.apertereports.common.users.User;
import org.apertereports.ui.UiIds;

/**
 * Factory class providing components in frequently used form.
 *
 * @author Zbigniew Malinowski
 *
 */
public abstract class ComponentFactory {

    public static final String REPORT_MANAGER_DATE_FORMAT = "report.manager.date.format";
    public static final String ICON_PATH = "/icons/16x16/";

    private static Label createLabelByProperty(String style, ComponentContainer parent, Property property) {
        Label label = new Label(property);
        label.setStyleName(style);
        if (parent != null) {
            parent.addComponent(label);
        }
        label.setWidth(null);
        return label;
    }

    /**
     * Creates label bound to date property.
     *
     * @param beanItem bound object
     * @param propertyName date property name
     * @param style style name to apply
     * @param parent container of the component
     * @return
     */
    public static Label createDateLabel(Item item, String propertyName, String style, ComponentContainer parent) {

        return createLabelByProperty(style, parent, new DateProperty(item.getItemProperty(propertyName)));
    }

    public static Label createCalendarLabel(Item item, String propertyName, String style, ComponentContainer parent) {

        return createLabelByProperty(style, parent, new CalendarProperty(item.getItemProperty(propertyName)));
    }

    /**
     *
     * Creates check box bound to item's boolean property.
     *
     * @param captionCode caption's localization code
     * @param item bound object
     * @param propertyId object's property name
     * @param parent container of the component
     * @return
     */
    public static CheckBox createCheckBox(String captionCode, Item item, String propertyId, ComponentContainer parent) {
        CheckBox checkBox = new CheckBox(VaadinUtil.getValue(captionCode), item.getItemProperty(propertyId));
        if (parent != null) {
            parent.addComponent(checkBox);
        }
        return checkBox;
    }

    /**
     * Creates text field bound to item.
     *
     * @param item bound object
     * @param propertyId object's property name
     * @param promptKey input prompt's localization code
     * @param parent container of the component
     * @return Text field
     */
    public static TextField createTextField(Item item, String propertyId, String promptKey, ComponentContainer parent) {
        TextField field = new TextField(item.getItemProperty(propertyId));
        field.setInputPrompt(VaadinUtil.getValue(promptKey));
        field.setWidth("100%");
        if (parent != null) {
            parent.addComponent(field);
        }
        return field;
    }

    private static class CalendarProperty extends DateProperty {

        public CalendarProperty(Property itemProperty) {
            super(itemProperty);
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

        public DateProperty(Property itemProperty) {
            super(itemProperty);

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
     * Creates search box in LAZY text change mode.
     *
     * @param listener text change event handler
     * @param container container of the component
     * @return
     */
    public static TextField createSearchBox(TextChangeListener listener, ComponentContainer parent) {
        TextField search = new TextField();
        search.setInputPrompt(VaadinUtil.getValue(UiIds.LABEL_FILTER));
        search.setWidth("100%");
        search.setImmediate(true);
        search.setTextChangeTimeout(500);
        search.setTextChangeEventMode(TextChangeEventMode.LAZY);
        search.addListener(listener);
        if (parent != null) {
            parent.addComponent(search);
        }
        return search;
    }

    public static ComboBox createFormatCombo(ReportType selectedValue, String captionKey) {
        Container all = new BeanItemContainer<String>(String.class, Arrays.asList(ReportType.stringValues()));
        ComboBox format = new ComboBox(VaadinUtil.getValue(captionKey),
                all);
        format.setValue(selectedValue.name());
        format.setStyleName("small");
        format.setNullSelectionAllowed(false);
        format.setTextInputAllowed(false);
        format.setWidth("100%");
        return format;
    }

    public static ComboBox createReportTemplateCombo(User user, ReportTemplate selectedValue, String captionKey) {

        Collection<ReportTemplate> allReports = ReportTemplateDAO.fetchAllActive(user);
        ComboBox reports = new ComboBox(VaadinUtil.getValue(captionKey),
                new BeanItemContainer<ReportTemplate>(ReportTemplate.class, allReports));
        reports.setItemCaptionPropertyId("reportname");
        reports.setValue(selectedValue);
        reports.setTextInputAllowed(false);
        reports.setNullSelectionAllowed(false);

        return reports;
    }

    public static Embedded createIcon(Item item, String proprtyId, ComponentContainer parent) {
        Enum<?> value = (Enum<?>) item.getItemProperty(proprtyId).getValue();
        Embedded icon = new Embedded(null, new ClassResource(AperteIcons.getIconUrl(value), parent.getApplication()));
        icon.setDescription(value.getClass().getSimpleName() + ": " + value.name());
        parent.addComponent(icon);
        return icon;

    }

    public enum AperteIcons {

        NEW,
        PROCESSING,
        FAILED,
        SUCCEEDED,
        ERROR,;

        public static String getIconUrl(Enum<?> e) {
            AperteIcons icon;
            try {
                icon = valueOf(e.name());
            } catch (IllegalArgumentException e1) {
                return ICON_PATH + ERROR.name().toLowerCase() + ".png";
            }
            return ICON_PATH + icon.name().toLowerCase() + ".png";
        }
    }

    public static ComboBox createLocaleCombo(Locale locale, String captionKey) {
        List<Locale> allLocales = Arrays.asList(Locale.getAvailableLocales());
        Collections.sort(allLocales, new Comparator<Locale>() {

            @Override
            public int compare(Locale o1, Locale o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        Container all = new BeanItemContainer<Locale>(Locale.class, allLocales);
        ComboBox reportLocale = new ComboBox(VaadinUtil.getValue(captionKey),
                all);
        reportLocale.setValue(locale);
        reportLocale.setStyleName("small");
        reportLocale.setNullSelectionAllowed(false);
        reportLocale.setTextInputAllowed(true);
        return reportLocale;
    }
}