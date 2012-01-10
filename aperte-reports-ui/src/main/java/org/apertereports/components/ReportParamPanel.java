package org.apertereports.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apertereports.backbone.jms.ReportOrderPusher;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.UserUtil;
import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

@SuppressWarnings("serial")
public class ReportParamPanel extends Panel {

	public Map<String, String> collectParametersValues() {
		return reportParametersComponent.collectParametersValues();
	}

	private static final String INVOKER_FORM_SEND_EMAIL = "invoker.form.send_email";
	private static final String INVOKER_FORM_GENERATE_IN_BACKGROUND = "invoker.form.generate_in_background";
	private static final String INVOKER_FORM_GENERATE = "invoker.form.generate";

	private ReportTemplate reportTemplate;
	private ReportParametersComponent reportParametersComponent;
	private ReportMaster rm;
	private List<CommitListener> listeners = new LinkedList<ReportParamPanel.CommitListener>();

	/**
	 * Renders the report parameters. The dialog may contain two buttons:
	 * "generate right now" and "generate in the background". The former simply
	 * creates a report based on current settings and lets user download it. The
	 * latter, on the other hand, posts a request to JMS queue for background
	 * processing.
	 */
	public ReportParamPanel(ReportTemplate reportTemplate, ParamPanelType type) {
		this.reportTemplate = reportTemplate;
		try {
			rm = new ReportMaster(reportTemplate.getContent(), reportTemplate.getId().toString(),
					new ReportTemplateProvider());
			reportParametersComponent = new ReportParametersComponent(rm, type.showFormat);
			final VerticalLayout vl = new VerticalLayout();
			vl.addComponent(reportParametersComponent);
			HorizontalLayout buttons = ComponentFactory.createHLayout(vl);

			if (type.showGenerate)
				createGenerate(reportTemplate.getAllowOnlineDisplay(), buttons);
			if (type.showGenerateInBackground)
				createGenerateInBackground(reportTemplate.getAllowBackgroundOrder(), buttons);
			if(type.showSave)
				createSave(buttons);

			vl.addComponent(buttons);
			addComponent(vl);
		} catch (Exception e) {
			throw new AperteReportsRuntimeException(e);
		}
	}
	
	public void addCommitListener(CommitListener listener){
		listeners.add(listener);
	}

	private void createSave(HorizontalLayout buttons) {
		Button save = ComponentFactory.createButton("params-form.commit", BaseTheme.BUTTON_LINK, buttons, new ClickListener() {
			
			@Override
			public void buttonClick(ClickEvent event) {
				for (CommitListener l : listeners) {
					l.commited();
				}
				
			}
		});
		
	}

	private void createGenerateInBackground(boolean allowBackgroundOrder, HorizontalLayout buttons) {
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
				}
			}
		});
		buttons.addComponent(submitBackgroundGenerate);
		buttons.addComponent(sendEmailCheckbox);
		if (allowBackgroundOrder != Boolean.TRUE || !ReportOrderPusher.isJmsAvailable()) {
			submitBackgroundGenerate.setEnabled(false);
			sendEmailCheckbox.setEnabled(false);
		}
	}

	private void createGenerate(boolean allowOnlineDisplay, HorizontalLayout buttons) {
		Button submitGenerate = ComponentFactory.createButton(INVOKER_FORM_GENERATE, BaseTheme.BUTTON_LINK, buttons);
		submitGenerate.addListener(new ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				if (reportParametersComponent.validateForm()) {
					sendForm();
				}
			}
		});
		if (allowOnlineDisplay != Boolean.TRUE) {
			submitGenerate.setEnabled(false);
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

	public enum ParamPanelType {
		
		REPORT_MANAGER(true, true, false, true),
		REPORT_INVOKER(true, true, false, true),
		CYCLIC_ORDER_BROWSER(true, false, true, false);
		;
		
		
		private boolean showGenerate;
		private boolean showGenerateInBackground;
		private boolean showSave;
		private boolean showFormat;
		
		private ParamPanelType(boolean showGenerate, boolean showGenerateInBackground, boolean showSave,
				boolean showFormat) {
			this.showGenerate = showGenerate;
			this.showGenerateInBackground = showGenerateInBackground;
			this.showSave = showSave;
			this.showFormat = showFormat;
		}
		
		
	}
	
	public interface CommitListener {

		void commited();
		
	}
}
