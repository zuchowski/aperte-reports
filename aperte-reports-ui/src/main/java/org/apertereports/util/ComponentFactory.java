package org.apertereports.util;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apertereports.common.ReportConstants;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;

/**
 * Factory class providing components in frequently used form.
 * 
 * @author Zbigniew Malinowski
 * 
 */
public abstract class ComponentFactory {

	private static final String SEARCH_FILTER_INPUT_PROMPT = "search-filter.input-prompt";
	private static final String REPORT_MANAGER_DATE_FORMAT = "report.manager.date.format";
	public static final String ICON_PATH = "/icons/16x16/";

	/**
	 * Creates label bound to property,
	 * 
	 * @param item
	 *            bound object
	 * @param propertyId
	 *            object's property
	 * @param style
	 *            style name to apply
	 * @param parent
	 *            container of the component
	 * @return
	 */
	public static Label createLabel(Item item, String propertyId, String style, ComponentContainer parent) {
		Property property = item.getItemProperty(propertyId);
		return createLabelByProperty(style, parent, property);
	}

	/**
	 * Label displaying not bound value.
	 * 
	 * @param valueKey
	 *            text to display
	 * @param style
	 *            style name to apply
	 * @param parent
	 *            container of the component
	 * @return
	 */
	public static Label createSimpleLabel(String valueKey, String style, ComponentContainer parent) {
		Label label = new Label(VaadinUtil.getValue(valueKey));
		label.setStyleName(style);
		if (parent != null)
			parent.addComponent(label);
		label.setWidth(null);
		return label;
	}

	private static Label createLabelByProperty(String style, ComponentContainer parent, Property property) {
		Label label = new Label(property);
		label.setStyleName(style);
		if (parent != null)
			parent.addComponent(label);
		label.setWidth(null);
		return label;
	}

	/**
	 * Creates button with caption code (for localization) and adds it to
	 * parent.
	 * 
	 * @param captionCode
	 *            localization code of button's caption
	 * @param style
	 *            style name to apply
	 * @param container
	 *            container of the component
	 * @return
	 */
	public static Button createButton(String captionCode, String style, ComponentContainer parent) {
		Button button = new Button(VaadinUtil.getValue(captionCode));
		button.setStyleName(style);
		if (parent != null)
			parent.addComponent(button);
		return button;
	}

	/**
	 * Creates button with caption code (for localization), registers listener
	 * and adds it to parent.
	 * 
	 * @param captionCode
	 *            localization code of button's caption
	 * @param style
	 *            style name to apply
	 * @param container
	 *            container of the component
	 * @param listener
	 *            listener to register on click
	 * @return
	 */
	public static Button createButton(String captionCode, String style, ComponentContainer container,
			ClickListener listener) {
		Button button = createButton(captionCode, style, container);
		button.addListener(listener);
		return button;
	}

	/**
	 * Creates label bound to date property.
	 * 
	 * @param beanItem
	 *            bound object
	 * @param propertyName
	 *            date property name
	 * @param style
	 *            style name to apply
	 * @param parent
	 *            container of the component
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
	 * @param captionCode
	 *            caption's localization code
	 * @param item
	 *            bound object
	 * @param propertyId
	 *            object's property name
	 * @param parent
	 *            container of the component
	 * @return
	 */
	public static CheckBox createCheckBox(String captionCode, Item item, String propertyId, ComponentContainer parent) {
		CheckBox checkBox = new CheckBox(VaadinUtil.getValue(captionCode), item.getItemProperty(propertyId));
		if (parent != null)
			parent.addComponent(checkBox);
		return checkBox;
	}

	/**
	 * Creates text area bound to item.
	 * 
	 * @param item
	 *            bound object
	 * @param propertyId
	 *            object's property name
	 * @param promptKey
	 *            input prompt's localization code
	 * @param parent
	 *            container of the component
	 * @return
	 */
	public static TextArea createTextArea(Item item, String propertyId, String promptKey, ComponentContainer parent) {
		TextArea area = new TextArea(item.getItemProperty(propertyId));
		area.setInputPrompt(VaadinUtil.getValue(promptKey));
		area.setWidth("100%");
		if (parent != null)
			parent.addComponent(area);
		return area;
	}

	/**
	 * Creates horizontal layout with width set to 100%.
	 * 
	 * @param parent
	 *            container of the component
	 * @return
	 */
	public static HorizontalLayout createHLayoutFull(ComponentContainer parent) {
		HorizontalLayout hLayout = createHLayout(parent);
		hLayout.setWidth("100%");
		return hLayout;
	}

	/**
	 * Creates horizontal layout with spacing.
	 * 
	 * @param parent
	 * @return
	 */
	public static HorizontalLayout createHLayout(ComponentContainer parent) {
		HorizontalLayout hLayout = new HorizontalLayout();
		if (parent != null)
			parent.addComponent(hLayout);
		hLayout.setSpacing(true);
		return hLayout;
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
	
	@SuppressWarnings({ "unchecked", "serial" })
	private static class DateProperty extends PropertyFormatter {

		protected SimpleDateFormat dateFormat;

		public DateProperty(Property itemProperty) {
			super(itemProperty);

		}

		private void init() {
			if (dateFormat == null)
				dateFormat = new SimpleDateFormat(VaadinUtil.getValue(REPORT_MANAGER_DATE_FORMAT));
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
	 * @param listener
	 *            text change event handler
	 * @param container
	 *            container of the component
	 * @return
	 */
	public static TextField createSearchBox(TextChangeListener listener, ComponentContainer parent) {
		TextField search = new TextField();
		search.setInputPrompt(VaadinUtil.getValue(SEARCH_FILTER_INPUT_PROMPT));
		search.setWidth("100%");
		search.setImmediate(true);
		search.setTextChangeTimeout(500);
		search.setTextChangeEventMode(TextChangeEventMode.LAZY);
		search.addListener(listener);
		if (parent != null)
			parent.addComponent(search);
		return search;
	}

	public static ComboBox createFormatCombo(ReportType selectedValue, String captionKey) {
		Container all = new BeanItemContainer<String>(String.class, Arrays.asList(ReportType.stringValues()));
		ComboBox format = new ComboBox(VaadinUtil.getValue(captionKey),
				all );
		format.setValue(selectedValue.name());
		format.setStyleName("small");
		format.setNullSelectionAllowed(false);
		format.setTextInputAllowed(false);
		format.setWidth("100%");
		return format;
	}

	public static ComboBox createReportTemplateCombo(ReportTemplate selectedValue, String captionKey) {
		
		Collection<ReportTemplate> allReports = ReportTemplateDAO.fetchAllReports(true);
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
		icon.setDescription(value.getClass().getSimpleName()+ ": " + value.name());
		parent.addComponent(icon);
		return icon;
		
	}
	
	public enum AperteIcons {
		
		NEW,
		PROCESSING,
		FAILED,
		SUCCEEDED,
		ERROR,
		;
		
		public static String getIconUrl(Enum<?> e){
			AperteIcons icon;
			try {
				icon = valueOf(e.name());
			} catch (IllegalArgumentException e1) {
				return ICON_PATH + ERROR.name().toLowerCase() +".png";
			}
			return ICON_PATH + icon.name().toLowerCase() +".png";
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
				all );
		reportLocale.setValue(locale);
		reportLocale.setStyleName("small");
		reportLocale.setNullSelectionAllowed(false);
		reportLocale.setTextInputAllowed(true);
		reportLocale.setWidth("100%");
		return reportLocale;
	}
}