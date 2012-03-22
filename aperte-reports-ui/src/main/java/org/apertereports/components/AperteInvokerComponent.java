package org.apertereports.components;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apertereports.backbone.jms.AperteReportsJmsFacade;
import org.apertereports.backbone.util.ReportOrderPusher;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.UserUtil;
import org.apertereports.util.VaadinUtil;

import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.AbstractLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Displays a component with a list of available report templates and lets
 * manually generate a report with temporal parameters.
 */
@SuppressWarnings("serial")
public class AperteInvokerComponent extends Panel {

	private static final int PAGE_SIZE = 10;
	private static final String COMPONENT_STYLE_NAME = "borderless light";
	private static final String PARAMS_FORM_SEND_EMAIL = "params-form.send-email";
	private PaginatedPanelList<ReportTemplate, ReportPanel> reportList;

	public AperteInvokerComponent() {

		init();
	}

	/**
	 * List item component.
	 * 
	 * @author Zbigniew Malinowski
	 * 
	 */
	private class ReportPanel extends Panel {

		private static final String PARAMS_FORM_BACKGROUND_GENERATE = "params-form.background-generate";
		private static final String PARAMS_FORM_GENERATE = "params-form.generate";
		private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE = "report-params.toggle-visibility.true";
		private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE = "report-params.toggle-visibility.false";
		private static final String REPORT_NAME_STYLE_NAME = "h4";
		private static final String REPORT_DESC_STYLE_NAME = "tiny";
		private static final String PANEL_STYLE_NAME = "borderless light";

		private ReportParamPanel paramsPanel = null;
		private Button toggleParams;
		private ReportTemplate reportTemplate;

		public ReportPanel(final ReportTemplate report) {
			this.reportTemplate = report;
			setStyleName(PANEL_STYLE_NAME);
			((AbstractLayout) getContent()).setMargin(true, false, false, false);
			HorizontalLayout row = ComponentFactory.createHLayoutFull(this);
			Label name = ComponentFactory.createSimpleLabel(report.getReportname(), REPORT_NAME_STYLE_NAME, row);

			Label spacer = new Label();
			row.addComponent(spacer);
			toggleParams = ComponentFactory.createButton(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE),
					BaseTheme.BUTTON_LINK, row);
			toggleParams.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					toggleParams();

				}

			});
			Label desc = ComponentFactory.createSimpleLabel(report.getDescription(), REPORT_DESC_STYLE_NAME, this);
			desc.setWidth("100%");
			row.setExpandRatio(spacer, 1.0f);
			row.setComponentAlignment(name, Alignment.MIDDLE_RIGHT);
			row.setSpacing(true);
			setWidth("100%");
		}

		private void toggleParams() {
			if (paramsPanel == null) {
				addComponent(paramsPanel = createParamsPanel());
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE));
			} else {
				removeComponent(paramsPanel);
				paramsPanel = null;
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE));
			}
		}

//		TODO: could be better
		private ReportParamPanel createParamsPanel() {
			final ReportParamPanel panel = new ReportParamPanel(reportTemplate, true);
			HorizontalLayout hl = ComponentFactory.createHLayout(panel);
			ComponentFactory.createButton(PARAMS_FORM_GENERATE, BaseTheme.BUTTON_LINK, hl, new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					try {
						ReportMaster rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId()
								.toString(), new ReportTemplateProvider());
						byte[] reportData = rm.generateAndExportReport(panel.getOuptutFormat(),
								new HashMap<String, Object>(panel.collectParametersValues()),
								org.apertereports.dao.utils.ConfigurationCache.getConfiguration());
						FileStreamer.showFile(getApplication(), reportTemplate.getReportname(), reportData,
								panel.getOuptutFormat());
					} catch (AperteReportsException e) {
						throw new AperteReportsRuntimeException(e);

					}

				}
			});

			Button backgroundGenerate = ComponentFactory.createButton(PARAMS_FORM_BACKGROUND_GENERATE,
					BaseTheme.BUTTON_LINK, hl);
			final CheckBox sendEmailCheckbox = new CheckBox(VaadinUtil.getValue(PARAMS_FORM_SEND_EMAIL));
			hl.addComponent(sendEmailCheckbox);
			backgroundGenerate.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					Map<String, String> parameters = panel.collectParametersValues();
					String email = UserUtil.getUserEmail();
					if ((Boolean) sendEmailCheckbox.getValue() != Boolean.TRUE)
						email = null;
					ReportOrder reportOrder = ReportOrderPusher.buildNewOrder(reportTemplate, parameters,
							panel.getOuptutFormat(), email, UserUtil.getUsername(), null);
					Long id = reportOrder.getId();
					if (id != null) {
						ReportOrderPusher.addToJMS(id);
					}
				}
			});
			if (!backgorundGenerationAvail()) {
				backgroundGenerate.setEnabled(false);
				sendEmailCheckbox.setEnabled(false);
			}

			panel.addComponent(hl);
			return panel;
		}

		private boolean backgorundGenerationAvail() {

			return AperteReportsJmsFacade.isJmsAvailable() && reportTemplate.getAllowBackgroundOrder() == Boolean.TRUE
					&& reportTemplate.getActive();
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
				reportList.filter(event.getText());

			}
		}, this);
		reportList = new PaginatedPanelList<ReportTemplate, AperteInvokerComponent.ReportPanel>(PAGE_SIZE) {
			
			@Override
			protected ReportPanel transform(ReportTemplate object) {
				return new ReportPanel(object);
			}
			
			@Override
			protected int getListSize(String filter) {
				return ReportTemplateDAO.countMatching(filter);
			}
			
			@Override
			protected Collection<ReportTemplate> fetch(String filter, int firstResult, int maxResults) {
				return ReportTemplateDAO.fetch(filter, firstResult, maxResults);
			}
		};

		addComponent(reportList);
		reportList.filter(null);

	}

}
