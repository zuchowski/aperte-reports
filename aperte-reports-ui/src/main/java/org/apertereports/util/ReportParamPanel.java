package org.apertereports.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apertereports.backbone.jms.ReportOrderPusher;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.components.ReportParametersComponent;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;

import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ReportParamPanel extends Panel {

	private static final String INVOKER_FORM_SEND_EMAIL = "invoker.form.send_email";
	private static final String INVOKER_FORM_GENERATE_IN_BACKGROUND = "invoker.form.generate_in_background";
	private static final String INVOKER_FORM_GENERATE = "invoker.form.generate";

	private ReportTemplate reportTemplate;
	private ReportParametersComponent reportParametersComponent;
	private ReportMaster rm;

	/**
	 * Renders the report parameters. The dialog may contain two buttons:
	 * "generate right now" and "generate in the background". The former simply
	 * creates a report based on current settings and lets user download it. The
	 * latter, on the other hand, posts a request to JMS queue for background
	 * processing.
	 */
	public ReportParamPanel(ReportTemplate reportTemplate, final ReportInvocationListener parent) {
		this.reportTemplate = reportTemplate;
		try {
			rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(),
					new ReportTemplateProvider());
			reportParametersComponent = new ReportParametersComponent(rm);
			final VerticalLayout vl = new VerticalLayout();
			vl.addComponent(reportParametersComponent);
			HorizontalLayout buttons = ComponentFactory.createHLayoutFull(vl);

			Button submitGenerate = ComponentFactory
					.createButton(INVOKER_FORM_GENERATE, BaseTheme.BUTTON_LINK, buttons);
			submitGenerate.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					if (reportParametersComponent.validateForm()) {
						sendForm();
						parent.reportInvoked();
					}
				}
			});
			if (reportTemplate.getAllowOnlineDisplay() != Boolean.TRUE) {
				submitGenerate.setEnabled(false);
			}

			Button submitBackgroundGenerate = ComponentFactory.createButton(INVOKER_FORM_GENERATE_IN_BACKGROUND,
					BaseTheme.BUTTON_LINK, buttons);
			final CheckBox sendEmailCheckbox = new CheckBox(VaadinUtil.getValue(INVOKER_FORM_SEND_EMAIL));
			submitBackgroundGenerate.addListener(new ClickListener() {
				@Override
				public void buttonClick(ClickEvent event) {
					if (reportParametersComponent.validateForm()) {
						String email = UserUtil.getUserEmail();
						if ((Boolean) sendEmailCheckbox.getValue() != Boolean.TRUE)
							email = null;
						sendFormToJMS(UserUtil.getUsername(), email);
						parent.reportInvoked();
					}
				}
			});
			buttons.addComponent(submitBackgroundGenerate);
			buttons.addComponent(sendEmailCheckbox);
			if (reportTemplate.getAllowBackgroundOrder() != Boolean.TRUE || !ReportOrderPusher.isJmsAvailable()) {
				submitBackgroundGenerate.setEnabled(false);
				sendEmailCheckbox.setEnabled(false);
			}

			vl.addComponent(buttons);
			addComponent(vl);
		} catch (Exception e) {
			throw new AperteReportsRuntimeException(e);
		}
	}

	/**
	 * Displays a report download popup.
	 * 
	 */
	private void sendForm() {
		Map<String, String> parameters = reportParametersComponent.collectParametersValues();
		try {
			byte[] reportData = rm.generateAndExportReport(reportParametersComponent.getSelectedFormat(),
					new HashMap<String, Object>(parameters),
					org.apertereports.dao.utils.ConfigurationCache.getConfiguration());
			FileStreamer.showFile(getApplication(), reportTemplate.getReportname(), reportData,
					reportParametersComponent.getSelectedFormat());
		} catch (AperteReportsException e) {
			throw new AperteReportsRuntimeException(e);

		}
	}

	/**
	 * Posts a request to JMS to generate the report in the background.
	 * 
	 * @param username
	 *            order creator's login
	 * @param email
	 *            email to send result or null if without mailing
	 * @see ReportOrderPusher
	 */
	private void sendFormToJMS(String username, String email) {
		Map<String, String> parameters = reportParametersComponent.collectParametersValues();
		ReportOrder reportOrder = ReportOrderPusher.buildNewOrder(reportTemplate, parameters,
				reportParametersComponent.getSelectedFormat(), email, username, null);
		Long id = reportOrder.getId();
		if (id != null) {
			ReportOrderPusher.addToJMS(id);
		}
	}

	public interface ReportInvocationListener {
		void reportInvoked();
	}
}
