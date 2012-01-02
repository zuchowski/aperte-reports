package org.apertereports.components;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.VaadinUtil;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Displays a component with a list of available report templates and lets
 * manually generate a report with temporal parameters.
 */
@SuppressWarnings("serial")
public class AperteInvokerComponent extends Panel {

	private static final String COMPONENT_STYLE_NAME = "borderless light";
	private VerticalLayout reportList;

	public AperteInvokerComponent() {
		
		init();
	}

	/**
	 * List item component.
	 * 
	 * @author zmalinowski
	 *
	 */
	private class ReportPanel extends Panel {
		
		private static final String INVOKER_WINDOW_TITLE = "invoker.window.title";
		private static final String INVOKER_INTRO_GENERATE = "invoker.intro.generate";
		private static final String DESCRIPTION_STYLE_NAME = "tiny";
		private static final String REPORT_NAME_STYLE_NAME = "h4";
		private static final String PANEL_STYLE_NAME = COMPONENT_STYLE_NAME;

		public ReportPanel(final ReportTemplate report) {
			setStyleName(PANEL_STYLE_NAME);
			HorizontalLayout row = ComponentFactory.createHLayoutFull(this);
			Label name = ComponentFactory.createSimpleLabel(report.getReportname(), REPORT_NAME_STYLE_NAME, row);
			Label desc = ComponentFactory.createSimpleLabel(report.getDescription(), DESCRIPTION_STYLE_NAME, row);
			Label spacer = new Label();
			row.addComponent(spacer);
			Button invoke = ComponentFactory.createButton(VaadinUtil.getValue(INVOKER_INTRO_GENERATE), BaseTheme.BUTTON_LINK, row);
			invoke.addListener(new ClickListener() {
				
				@Override
				public void buttonClick(ClickEvent event) {
					ReportParamWindow reportParamWindow = new ReportParamWindow(report, VaadinUtil.getValue(INVOKER_WINDOW_TITLE),
							null);
					getWindow().addWindow(reportParamWindow);
					
				}
			});
			addComponent(row);
			row.setExpandRatio(spacer, 1.0f);
			row.setComponentAlignment(name, Alignment.MIDDLE_RIGHT);
			row.setComponentAlignment(desc, Alignment.MIDDLE_RIGHT);
			row.setSpacing(true);
		}
	}

	/**
	 * Build the main layout.
	 */
	private void init() {
		setScrollable(true);
		setStyleName(COMPONENT_STYLE_NAME);

		ComponentFactory.createSearchBox(new TextChangeListener() {
			
			@Override
			public void textChange(TextChangeEvent event) {
				refreshList(event.getText());
				
			}}, this);
		reportList = new VerticalLayout();
		
		addComponent(reportList);
		refreshList(null);
		
		
		

	}

	private void refreshList(String filter) {
		reportList.removeAllComponents();
		List<ReportTemplate> list = (List<ReportTemplate>) ReportTemplateDAO.filterReports(filter);
		Collections.sort(list, new Comparator<ReportTemplate>() {

			@Override
			public int compare(ReportTemplate o1, ReportTemplate o2) {
				return o1.getCreated().compareTo(o2.getCreated());
			}
			
		});
		for (ReportTemplate reportTemplate : list) {
			reportList.addComponent(new ReportPanel(reportTemplate));
		}
		
	}

	
}
