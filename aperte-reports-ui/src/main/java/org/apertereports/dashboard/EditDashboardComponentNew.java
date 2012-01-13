package org.apertereports.dashboard;

import java.util.LinkedList;

import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.components.ReportParamPanel;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.Item;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;

@SuppressWarnings("serial")
public class EditDashboardComponentNew extends AbstractDashboardComponent {

	private static final String DASHBOARD_BUTTON_SAVE_CAPTION = "dashboard.button.save.caption";
	private static final String CACHE_TIMEOUT = "cacheTimeout";
	private static final String REPORT = "report";
	private static final String DASHBOARD_EDIT_CAPTION_CACHE_TIMEOUT = "dashboard.edit.caption.cacheTimeout";
	private static final String DASHBOARD_EDIT_REQUIRED_ERROR_CACHE_TIMEOUT = "dashboard.edit.required-error.cacheTimeout";
	private static final String DASHBOARD_EDIT_INPUT_PROMPT_REPORT_ID = "dashboard.edit.input-prompt.reportId";
	private static final String DASHBOARD_EDIT_REQUIRED_ERROR_REPORT_ID = "dashboard.edit.required-error.reportId";
	private static final String DASHBOARD_EDIT_CAPTION_REPORT_ID = "dashboard.edit.caption.reportId";

	private Panel mainPanel;
	private ReportParamPanel params = new ReportParamPanel();
	private Button save;
	private ReportConfig config;
	private Form form;
	private Item datasource;

	public EditDashboardComponentNew() {
		template = "<report idx=\"0\"></report>";
		reportConfigs = new LinkedList<ReportConfig>();
		ReportConfig rc = new ReportConfig();
		reportConfigs.add(rc);
		rc.setId(0);
		config = rc;
	}

	@Override
	protected void initComponentData() {
		mainPanel = new Panel();
		setCompositionRoot(mainPanel);
		HorizontalLayout reportRow = ComponentFactory.createHLayoutFull(mainPanel);
		reportRow.addComponent(form = new EditDashboardForm());
		mainPanel.addComponent(params);
		
		save = ComponentFactory.createButton(DASHBOARD_BUTTON_SAVE_CAPTION, "", mainPanel, new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				saveConfiguration();
			}

		});

	}
	
	private class EditDashboardForm extends Form {
		
		private GridLayout layout ;
		
		
		public EditDashboardForm() {
			layout = new GridLayout(2, 1);
			layout.setSpacing(true);
			layout.setWidth("100%");
			setLayout(layout);
			setFormFieldFactory(new EditDashboardFieldFactory());
			datasource = new PropertysetItem();
			datasource.addItemProperty(REPORT, new ObjectProperty<ReportTemplate>(null, ReportTemplate.class));
			datasource.addItemProperty(CACHE_TIMEOUT, new ObjectProperty<Integer>(0));
			setItemDataSource(datasource );
			
		}
		
		@Override
		protected void attachField(Object propertyId, Field field) {
			if(propertyId.equals(REPORT)){
				layout.addComponent(field, 0, 0);
			}else if(propertyId.equals(CACHE_TIMEOUT)){
				layout.addComponent(field, 1, 0);
				layout.setComponentAlignment(field, Alignment.MIDDLE_RIGHT );
			}
		}
		
		private class EditDashboardFieldFactory extends DefaultFieldFactory {
			
			@Override
			public Field createField(Item item, Object propertyId, Component uiContext) {
				if(propertyId.equals(REPORT)){
					ComboBox field = ComponentFactory.createReportTemplateCombo(null, DASHBOARD_EDIT_CAPTION_REPORT_ID);
					field.setRequired(true);
					field.setRequiredError(VaadinUtil.getValue(DASHBOARD_EDIT_REQUIRED_ERROR_REPORT_ID));
					field.setInputPrompt(VaadinUtil.getValue(DASHBOARD_EDIT_INPUT_PROMPT_REPORT_ID));
					field.setWidth("100%");
					field.setImmediate(true);
					field.addListener(new ValueChangeListener() {
						
						@Override
						public void valueChange(com.vaadin.data.Property.ValueChangeEvent event) {
							reloadParams((ReportTemplate) event.getProperty().getValue());
							
						}
					});
					return field;
				}else if(propertyId.equals(CACHE_TIMEOUT)){
					Field field = super.createField(item, propertyId, uiContext);
					field.setRequired(true);
					field.setRequiredError(VaadinUtil.getValue(DASHBOARD_EDIT_REQUIRED_ERROR_CACHE_TIMEOUT));
					field.setCaption(VaadinUtil.getValue(DASHBOARD_EDIT_CAPTION_CACHE_TIMEOUT));
					return field;
				}
				return null;
			}
		}
		
	}

	protected void reloadParams(ReportTemplate value) {
		ReportParamPanel newParams = new ReportParamPanel(value, false);
		mainPanel.replaceComponent(params, newParams);
		params = newParams;

	}

	private void saveConfiguration() {
		try {
			form.commit();
		} catch (InvalidValueException e) {
			return;
		}
		ReportTemplate report = (ReportTemplate) datasource.getItemProperty("report").getValue();
		config.setReportId(report.getId());
		config.setCacheTimeout((Integer) datasource.getItemProperty("cacheTimeout").getValue());
		config.setParameters(XmlReportConfigLoader.getInstance().mapToParameterList(params.collectParametersValues()));
		saveData();
	}

}
