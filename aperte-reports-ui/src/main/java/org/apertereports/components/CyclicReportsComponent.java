package org.apertereports.components;


import java.util.Arrays;
import java.util.List;

import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.components.ReportParamPanel.CommitListener;
import org.apertereports.components.ReportParamPanel.ParamPanelType;
import org.apertereports.dao.CyclicReportOrderDAO;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.CronExpressionValidator;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.EmailValidator;
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

	private VerticalLayout list;

	private static final String DESCRIPTION_STYLE = "small";
	private static final String FORMAT_STYLE = "h4";
	
	private static final String ORDER_DESCRIPTION = "description";
	private static final String ORDER_CRON_SPEC = "cronSpec";
	private static final String ORDER_RECIPIENT_EMAIL = "recipientEmail";
	private static final String ORDER_OUTPUT_FORMAT = "outputFormat";
	private static final String ORDER_REPORT_REPORTNAME = "reportname";
	private static final String ORDER_ENABLED = "enabled";
	private static final String ORDER_REPORT = "report";
	
	private static final String CYCLIC_BUTTON_ADD_NEW = "cyclic.button.addNew";
	private static final String CYCLIC_EDIT_BUTTON_CANCEL = "cyclic.edit.button.cancel";
	private static final String CYCLIC_EDIT_BUTTON_SAVE = "cyclic.edit.button.save";
	private static final String VALIDATION_EMAIL = "validation.email";
	private static final String VALIDATION_CRON_EXPRESSION = "validation.cronExpression";
	private static final String CYCYLIC_EDIT_REQUIRED_ERROR = "cycylic.edit.required-error.";
	private static final String CYCLIC_EDIT_INPUT_PROMPT = "cyclic.edit.input-prompt.";
	private static final String CYCLIC_REPORT_EDIT_INPUT_PROMPT_DESC = "cyclic.report.edit.input-prompt.desc";
	private static final String CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME = "cyclic.edit.input-prompt.reportname";
	private static final String CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME = "cycylic.edit.required-error.reportname";
	private static final String CYCLIC_EDIT_INPUT_PROMPT_FORMAT = "cyclic.edit.input-prompt.format";
	private static final String CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT = "cycylic.edit.required-error.format";
	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE = "report-params.toggle-visibility.true";
	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE = "report-params.toggle-visibility.false";
	private static final String CYCLIC_BUTTON_DELETE = "cyclic.button.delete";
	private static final String CYCLIC_BUTTON_EDIT = "cyclic.button.edit";
	
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
			
			CheckBox icon = ComponentFactory.createCheckBox("", item, ORDER_ENABLED, row1);
			Label name = ComponentFactory.createLabel(new BeanItem<ReportTemplate>(order.getReport()), ORDER_REPORT_REPORTNAME, FORMAT_STYLE, row1);
			
			Label spacer = new Label();
			row1.addComponent(spacer);
			
			Label format = ComponentFactory.createLabel(item, ORDER_OUTPUT_FORMAT, FORMAT_STYLE, row1);
			row1.setComponentAlignment(format, Alignment.MIDDLE_RIGHT);
			row1.setComponentAlignment(icon, Alignment.MIDDLE_LEFT);
			row1.setComponentAlignment(name, Alignment.MIDDLE_LEFT);
			row1.setExpandRatio(spacer, 1.0f);
			
			Label spacer2 = new Label("");
			ComponentFactory.createLabel(item, ORDER_RECIPIENT_EMAIL, null, row2);
			row2.addComponent(spacer2);
			Label when = ComponentFactory.createLabel(item, ORDER_CRON_SPEC, null, row2);
			row2.setComponentAlignment(when, Alignment.MIDDLE_RIGHT);
			
			Label desc = ComponentFactory.createLabel(item, ORDER_DESCRIPTION, DESCRIPTION_STYLE, this);
			desc.setWidth("100%");
			
			HorizontalLayout row3 = ComponentFactory.createHLayout(this);
			
			toggleParams = ComponentFactory.createButton(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE, BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					showParams();
					
				}
			});
			ComponentFactory.createButton(CYCLIC_BUTTON_EDIT, BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					edit();
					
				}
			});
			ComponentFactory.createButton(CYCLIC_BUTTON_DELETE, BaseTheme.BUTTON_LINK, row3, new ClickListener() {
				
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
				addComponent(paramsPanel = new ReportParamPanel(order.getReport(), ParamPanelType.CYCLIC_ORDER_BROWSER));
				paramsPanel.addCommitListener(new CommitListener() {
					
					@Override
					public void commited() {
						order.setParametersXml(XmlReportConfigLoader.getInstance().mapAsXml(
				paramsPanel.collectParametersValues()));
						CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(order);
						
					}
				});
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
			setWidth("100%");
			addComponent(form = new EditCyclicReportForm(order));
			HorizontalLayout buttons = ComponentFactory.createHLayoutFull(this);
			ComponentFactory.createButton(CYCLIC_EDIT_BUTTON_SAVE, BaseTheme.BUTTON_LINK, buttons, new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					save();
					
				}
			});
			ComponentFactory.createButton(CYCLIC_EDIT_BUTTON_CANCEL, BaseTheme.BUTTON_LINK, buttons, new ClickListener() {
				
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
			try {
				form.commit();
				CyclicReportOrderDAO.saveOrUpdateCyclicReportOrder(order);
				list.replaceComponent(this, new CyclicReportPanel(order));
			} catch (InvalidValueException e) {
				ExceptionUtils.logWarningException("Edit cyclic report: invalid user input", e);
			}
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
			setVisibleItemProperties(Arrays.asList(new String[] { ORDER_REPORT,
                    ORDER_CRON_SPEC, ORDER_RECIPIENT_EMAIL, ORDER_OUTPUT_FORMAT, ORDER_DESCRIPTION}));
			setWidth("100%");
			setWriteThrough(false);
			
		}
		@Override
		protected void attachField(Object propertyId, Field field) {
			 if (propertyId.equals(ORDER_REPORT)) {
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
			if(propertyId.equals(ORDER_OUTPUT_FORMAT)){
				String value = (String) item.getItemProperty(ORDER_OUTPUT_FORMAT).getValue();
				if(value == null)
					value = ReportType.PDF.name();
				ReportType selectedValue = ReportType.valueOf(value);
				ComboBox format = ComponentFactory.createFormatCombo(selectedValue, "");
				format.setRequired(true);
				format.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_FORMAT));
				format.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_FORMAT));
				return format;
			}else if (propertyId.equals(ORDER_REPORT)) {
				ComboBox reportname = ComponentFactory.createReportTemplateCombo((ReportTemplate) item.getItemProperty(ORDER_REPORT).getValue(), "");
				reportname.setRequired(true);
				reportname.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR_REPORTNAME));
				reportname.setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT_REPORTNAME));
				return reportname;
			}else if (propertyId.equals(ORDER_DESCRIPTION)) {
				TextArea field = ComponentFactory.createTextArea(item, ORDER_DESCRIPTION, CYCLIC_REPORT_EDIT_INPUT_PROMPT_DESC, null);
				field.setNullRepresentation("");
				return field;
			}else {
				Field field = super.createField(item, propertyId, uiContext);
				field.setWidth("100%");
				field.setCaption(null);
				field.setRequired(true);
				if (propertyId.equals(ORDER_CRON_SPEC) || propertyId.equals(ORDER_RECIPIENT_EMAIL)){
					((TextField) field).setNullRepresentation("");
					((TextField) field).setInputPrompt(VaadinUtil.getValue(CYCLIC_EDIT_INPUT_PROMPT + propertyId));
				}
				field.setRequired(true);
				field.setRequiredError(VaadinUtil.getValue(CYCYLIC_EDIT_REQUIRED_ERROR + propertyId));
				if (propertyId.equals(ORDER_CRON_SPEC)){
					field.addValidator(new CronExpressionValidator(VaadinUtil.getValue(VALIDATION_CRON_EXPRESSION)));
				}
				if (propertyId.equals(ORDER_RECIPIENT_EMAIL)){
					field.addValidator(new EmailValidator(VaadinUtil.getValue(VALIDATION_EMAIL)));
				}
				return field;
			}
		}
	}
}
