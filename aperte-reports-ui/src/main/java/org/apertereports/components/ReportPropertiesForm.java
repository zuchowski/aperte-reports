package org.apertereports.components;

import java.util.LinkedList;
import java.util.List;

import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.Form;

public class ReportPropertiesForm extends Form {
	
	private ReportTemplate reportTemplate;
	private List<PropertySetChangeListener> listeners = new LinkedList<PropertySetChangeListener>();

	public ReportPropertiesForm() {
		setImmediate(true);
	}
	
	@Override
	public void commit() throws SourceException, InvalidValueException {
		super.commit();
		for (PropertySetChangeListener l : listeners) {
			l.itemPropertySetChange(new PropertySetChangeEvent() {
				
				@Override
				public Item getItem() {
					return getItemDataSource();
				}
			});
		}
		
	}
	
	public void addListener(PropertySetChangeListener listener) {
		listeners.add(listener);
	}
	
	
	
}
