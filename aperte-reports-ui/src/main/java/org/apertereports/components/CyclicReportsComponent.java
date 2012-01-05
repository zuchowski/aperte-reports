package org.apertereports.components;


import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.math.RandomUtils;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.dao.CyclicReportOrderDAO;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class CyclicReportsComponent extends Panel {

	private static final String CYCLIC_BUTTON_ADD_NEW = "cyclic.button.addNew";

	private VerticalLayout list;

	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE = "report-params.toggle-visibility.true";
	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE = "report-params.toggle-visibility.false";
	
	public CyclicReportsComponent() {
		init();
	}

	private void init() {
		HorizontalLayout header = ComponentFactory.createHLayoutFull(this);
		TextField search = ComponentFactory.createSearchBox(new TextChangeListener() {
			
			@Override
			public void textChange(TextChangeEvent event) {
				filter(event.getText());
			}

			
		}, header);
		header.setExpandRatio(search, 1.0f);
		header.addComponent(new Label());
		Button addButton = ComponentFactory.createButton(CYCLIC_BUTTON_ADD_NEW, null, header);
		addButton.addListener(new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				addNew();
				
			}
		});
		
		list = new VerticalLayout();
		addComponent(list);
		filter(null);
		
	}
	
	private void addNew() {
		CyclicReportOrder order = new CyclicReportOrder();
//		CyclicReportPanel crp = new CyclicReportPanel(order );
		EditCyclicReportPanel ecrp = new EditCyclicReportPanel(order, true);
		list.addComponent(ecrp, 0);
		
	}

	private void filter(String text) {
		List<CyclicReportOrder> orders = CyclicReportOrderDAO.filterReports(text);
		for (CyclicReportOrder order : orders) {
			list.addComponent(new CyclicReportPanel(order ));
		}
		
		
	}
	
	private class CyclicReportPanel extends Panel {
		private CyclicReportOrder order;
		private ReportParamPanel paramsPanel;
		private Button toggleParams;
		
		public CyclicReportPanel(CyclicReportOrder order) {
			this.order = order;
			setWidth("100%");
			BeanItem<CyclicReportOrder> item = new BeanItem<CyclicReportOrder>(order);
			HorizontalLayout row1 = ComponentFactory.createHLayoutFull(this);
			HorizontalLayout row2 = ComponentFactory.createHLayoutFull(this);
			
			
//			Embedded icon = new Embedded(null, new ThemeResource("../runo/icons/16/ok.png"));
			CheckBox icon = ComponentFactory.createCheckBox("", item, "enabled", row1);
			Label name = ComponentFactory.createLabel(new BeanItem<ReportTemplate>(order.getReport()), "reportname", "h4", row1);
			
			Label spacer = new Label();
			row1.addComponent(spacer);
			
			Label format = ComponentFactory.createLabel(item, "outputFormat", "h4", row1);
			row1.setComponentAlignment(format, Alignment.MIDDLE_RIGHT);
			row1.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
			row1.setComponentAlignment(name, Alignment.MIDDLE_LEFT);
			row1.setExpandRatio(spacer, 1.0f);
			
			Label spacer2 = new Label("");
			ComponentFactory.createLabel(item, "recipientEmail", null, row2);
			row2.addComponent(spacer2);
			Label when = ComponentFactory.createLabel(item, "cronSpec", null, row2);
			row2.setComponentAlignment(when, Alignment.MIDDLE_RIGHT);
			
			Label desc = ComponentFactory.createLabel(item, "description", "small", this);
			desc.setWidth("100%");
			
			HorizontalLayout row3 = ComponentFactory.createHLayoutFull(this);
			
			toggleParams = ComponentFactory.createButton("cyclic.button.params", BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					showParams();
					
				}
			});
			ComponentFactory.createButton("cyclic.button.edit", BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					edit();
					
				}
			});
			ComponentFactory.createButton("cyclic.button.delete", BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					remove();
					
				}
			});
			
			
			
		}

		protected void remove() {
			CyclicReportOrderDAO.removeCyclicReportOrder(order);
			list.removeComponent(this);
			
		}

		protected void edit() {
			list.replaceComponent(this, new EditCyclicReportPanel(this.order, false));
			
		}

		protected void showParams() {
			if (paramsPanel == null){
				addComponent(paramsPanel = new ReportParamPanel(order.getReport(), null));
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE));
			}
			else {
				removeComponent(paramsPanel);
				paramsPanel = null;
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE));
			}
			
		}
		
	}
	
	private class EditCyclicReportPanel extends Panel {
		
		private CyclicReportOrder order;
		private EditCyclicReportForm form;
		private boolean newItem;
		
		public EditCyclicReportPanel(CyclicReportOrder order, boolean newItem) {
			this.newItem = newItem;
			this.order = order;
			BeanItem<CyclicReportOrder> item = new BeanItem<CyclicReportOrder>(order);
			setWidth("100%");
			addComponent(form = new EditCyclicReportForm(order));
			HorizontalLayout buttons = ComponentFactory.createHLayoutFull(this);
			ComponentFactory.createButton("cyclic.edit.button.save", BaseTheme.BUTTON_LINK, buttons, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					save();
					
				}
			});
			ComponentFactory.createButton("cyclic.edit.button.cancel", BaseTheme.BUTTON_LINK, buttons, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					cancel();
					
				}
			});
		}

		protected void cancel() {
			form.discard();
			if(!newItem)
			list.replaceComponent(this, new CyclicReportPanel(order));
			else
				list.removeComponent(this);
		}

		protected void save() {
			form.commit();
			list.replaceComponent(this, new CyclicReportPanel(order));
		}
		
	}
	
	private class EditCyclicReportForm extends Form {
		private GridLayout layout;
		
		public EditCyclicReportForm(CyclicReportOrder order) {
			layout = new GridLayout(3, 3);
			layout.setWidth("100%");
			layout.setSpacing(true);
			setLayout(layout);
			setFormFieldFactory(new EditCyclicFormFactory());
			setItemDataSource(new BeanItem<CyclicReportOrder>(order));
			setVisibleItemProperties(Arrays.asList(new String[] { "report",
                    "cronSpec", "recipientEmail", "outputFormat", "description"}));
			setWidth("100%");
			setWriteThrough(false);
			
		}
		@Override
		protected void attachField(Object propertyId, Field field) {
			 if (propertyId.equals("report")) {
				 layout.addComponent(field, 0, 0);
				 layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
			 }else if (propertyId.equals("outputFormat")) {
				 layout.addComponent(field, 2, 0);
				 layout.setComponentAlignment(field, Alignment.MIDDLE_RIGHT);
			 }else if (propertyId.equals("recipientEmail")) {
				 layout.addComponent(field, 0, 1);
				 layout.setComponentAlignment(field, Alignment.MIDDLE_LEFT);
			 }else if (propertyId.equals("cronSpec")) {
				 layout.addComponent(field, 2, 1);
				 layout.setComponentAlignment(field, Alignment.MIDDLE_RIGHT);
			 }else if (propertyId.equals("description")) {
				 layout.addComponent(field, 0, 2, 2, 2);
				 field.setWidth("100%");
			}
		}
	}
	
	private class EditCyclicFormFactory extends DefaultFieldFactory {
		
		
		@Override
		public Field createField(Item item, Object propertyId, Component uiContext) {
			if(propertyId.equals("outputFormat")){
				String value = (String) item.getItemProperty("outputFormat").getValue();
				if(value == null)
					value = ReportType.PDF.name();
				ReportType selectedValue = ReportType.valueOf(value);
				ComboBox format = ComponentFactory.createFormatCombo(selectedValue, "");
				return format;
			}else if (propertyId.equals("report")) {
				ComboBox reportname = ComponentFactory.createReportTemplateCombo((ReportTemplate) item.getItemProperty("report").getValue(), "");
				return reportname;
			}else if (propertyId.equals("description")) {
				TextArea field = ComponentFactory.createTextArea(item, "description", "cyclic.report.edit.description.prompt", null);
//				field.setCaption(VaadinUtil.getValue("cyclic.edit.form." + propertyId));
				return field;
			}else {
				Field field = super.createField(item, propertyId, uiContext);
//				field.setCaption(VaadinUtil.getValue("cyclic.edit.form." + propertyId));
				field.setWidth("100%");
				field.setCaption(null);
				return field;
			}
			
		}
	
	}
}
