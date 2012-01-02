package org.apertereports.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;

/**
 * Factory class providing components in frequently used form.
 * 
 * @author Zbigniew Malinowski
 * 
 */
public class ComponentFactory {

	private static final String SEARCH_FILTER_INPUT_PROMPT = "search-filter.input-prompt";
	private static final String REPORT_MANAGER_DATE_FORMAT = "report.manager.date.format";

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
	public static Label createLabel(BeanItem<?> item, String propertyId, String style, ComponentContainer parent) {
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
		parent.addComponent(label);
		label.setWidth(null);
		return label;
	}

	private static Label createLabelByProperty(String style, ComponentContainer parent, Property property) {
		Label label = new Label(property);
		label.setStyleName(style);
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
	public static Button createButton(String captionCode, String style, ComponentContainer container) {
		Button button = new Button(VaadinUtil.getValue(captionCode));
		button.setStyleName(style);
		container.addComponent(button);
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
	public static Label createDateLabel(BeanItem<ReportTemplate> beanItem, String propertyName, String style,
			HorizontalLayout parent) {

		return createLabelByProperty(style, parent, new DateProperty(beanItem.getItemProperty(propertyName)));
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
	public static CheckBox createCheckBox(String captionCode, BeanItem<?> item, String propertyId,
			ComponentContainer parent) {
		CheckBox checkBox = new CheckBox(VaadinUtil.getValue(captionCode), item.getItemProperty(propertyId));
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
	public static TextArea createTextArea(BeanItem<?> item, String propertyId, String promptKey,
			ComponentContainer parent) {
		TextArea area = new TextArea(item.getItemProperty(propertyId));
		area.setInputPrompt(VaadinUtil.getValue(promptKey));
		area.setWidth("100%");
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
		parent.addComponent(hLayout);
		hLayout.setSpacing(true);
		return hLayout;
	}

	@SuppressWarnings({ "unchecked", "serial" })
	private static class DateProperty extends PropertyFormatter {

		private SimpleDateFormat dateFormat;

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
	public static TextField createSearchBox(TextChangeListener listener, ComponentContainer container) {
		TextField search = new TextField();
		search.setInputPrompt(VaadinUtil.getValue(SEARCH_FILTER_INPUT_PROMPT));
		search.setWidth("100%");
		search.setImmediate(true);
		search.setTextChangeTimeout(500);
		search.setTextChangeEventMode(TextChangeEventMode.LAZY);
		search.addListener(listener);
		container.addComponent(search);
		return search;
	}
}