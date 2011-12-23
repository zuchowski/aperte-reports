package org.apertereports.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.PropertyFormatter;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;

public class ComponentFactory {

	private static final String REPORT_MANAGER_DATE_FORMAT = "report.manager.date.format";

	public static Label createLabel(BeanItem<?> item, String propertyId, String style, ComponentContainer parent) {
		Property property = item.getItemProperty(propertyId);
		return createLabelByProperty(style, parent, property);
	}

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

	public static Button createButton(String captionCode, String style, ComponentContainer container) {
		Button button = new Button(VaadinUtil.getValue(captionCode));
		button.setStyleName(style);
		container.addComponent(button);
		return button;
	}

	public static Label createDateLabel(BeanItem<ReportTemplate> beanItem, String string, String reportNameStyle,
			HorizontalLayout headerRow) {

		return createLabelByProperty(reportNameStyle, headerRow, new DateProperty(beanItem.getItemProperty(string)));
	}

	public static CheckBox createCheckBox(String nameKey, BeanItem<?> item, String propertyId, ComponentContainer parent) {
		CheckBox checkBox = new CheckBox(VaadinUtil.getValue(nameKey), item.getItemProperty(propertyId));
		parent.addComponent(checkBox);
		return checkBox;
	}

	public static TextArea createTextArea(BeanItem<?> item, String propertyId, String promptKey,
			ComponentContainer parent) {
		TextArea area = new TextArea(item.getItemProperty(propertyId));
		area.setInputPrompt(VaadinUtil.getValue(promptKey));
		area.setWidth("100%");
		parent.addComponent(area);
		return area;
	}

	public static HorizontalLayout createHLayoutFull(ComponentContainer parent) {
		HorizontalLayout hLayout = createHLayout(parent);
		hLayout.setWidth("100%");
		return hLayout;
	}

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
}