package org.apertereports.components;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.exception.AperteReportsException;
import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.components.ReportParamPanel.ParamPanelType;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.engine.ReportCache;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.VaadinUtil;

import com.vaadin.data.util.BeanItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FailedListener;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Component to manage reports.
 * 
 * @author Zbigniew Malinowski
 *
 */
@SuppressWarnings("serial")
public class ReportManagerComponent extends Panel {

	private static final String DESC_STYLE = "small";
	private static final String CHANGED_DATE_STYLE = "h3";
	private static final String REPORT_NAME_STYLE = "h4";
	private static final String FILE_NAME_STYLE = "h3";
	private static final String EDIT_PANEL_STYLE = "bubble";
	private static final String PANEL_STYLE = "borderless";

	private static final String DESCRIPTION_PROPERTY = "description";
	private static final String FILENAME_PROPERTY = "filename";
	private static final String CREATED_PROPERTY = "created";
	private static final String REPORTNAME_PROPERTY = "reportname";
	private static final String ALLOW_BACKGROUND_PROCESSING_PROPERTY = "allowBackgroundProcessing";
	private static final String ALLOW_ONLINE_DISPLAY_PROPERTY = "allowOnlineDisplay";
	private static final String ACTIVE_PROPERTY = "active";

	private static final String REPORT_MANAGER_NEW_REPORT_BUTTON = "report.manager.newReportButton";
	private static final String REPORT_MANAGER_ITEM_DOWNLOAD = "report.manager.item.download";
	private static final String REPORT_MANAGER_ITEM_EDIT = "report.manager.item.edit";
	private static final String REPORT_MANAGER_ITEM_RUN = "report.manager.item.run";
	private static final String REPORT_MANAGER_ITEM_UPLOAD_CHANGE = "report.manager.item.upload.change";
	private static final String REPORT_MANAGER_ITEM_REMOVE = "report.manager.item.remove";
	private static final String REPORT_MANAGER_ITEM_EDIT_DESC_PROMPT = "report.manager.item.edit.desc.prompt";
	private static final String BUTTON_CANCEL = "button.cancel";
	private static final String BUTTON_OK = "button.ok";
	private static final String REPORT_MANAGER_ITEM_EDIT_BACKGROUND = "report.manager.item.edit.background";
	private static final String REPORT_MANAGER_ITEM_EDIT_ONLINE = "report.manager.item.edit.online";
	private static final String REPORT_MANAGER_ITEM_EDIT_ACTIVE = "report.manager.item.edit.active";
	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE = "report-params.toggle-visibility.true";
	private static final String REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE = "report-params.toggle-visibility.false";

	private VerticalLayout list;
	private ReportReceiver newReportReceiver;

	public ReportManagerComponent() {

		init();
	}

	private void init() {
		VerticalLayout mainLayout = new VerticalLayout();
		newReportReceiver = new ReportReceiver(new ReportTemplate());
		newReportReceiver.addListener(new ReportReceivedListener() {

			@Override
			public void reportReceived(ReportTemplate reportTemplate) {
				addNewReport(reportTemplate);
			}

		});
		Upload newReportUpload = new Upload(null, newReportReceiver);
		newReportUpload.addListener((SucceededListener) newReportReceiver);
		newReportUpload.addListener((FailedListener) newReportReceiver);
		newReportUpload.setButtonCaption(VaadinUtil.getValue(REPORT_MANAGER_NEW_REPORT_BUTTON));
		newReportUpload.setImmediate(true);
		HorizontalLayout hl = ComponentFactory.createHLayoutFull(mainLayout);
		list = new VerticalLayout();

		TextField search = ComponentFactory.createSearchBox(new TextChangeListener() {
			
			@Override
			public void textChange(TextChangeEvent event) {
				reloadData(event.getText());
			}
		}, hl);
		hl.setExpandRatio(search, 1.0f);
		hl.addComponent(newReportUpload);
		hl.setComponentAlignment(search, Alignment.MIDDLE_RIGHT);
		hl.setComponentAlignment(newReportUpload, Alignment.MIDDLE_RIGHT);
		mainLayout.addComponent(list);
		addComponent(mainLayout);
		reloadData(null);
		setWidth("100%");
	}

	private void addNewReport(ReportTemplate reportTemplate) {
		ReportItemPanel reportItem = new ReportItemPanel(reportTemplate);
		list.addComponent(reportItem, 0);
		editReportData(reportItem);
		newReportReceiver.reportTemplate = new ReportTemplate();
	}

	private void reloadData(String filter) {
		List<ReportTemplate> raportTemplates = loadReports(filter);
		list.removeAllComponents();
		for (ReportTemplate reportTemplate : raportTemplates) {
			list.addComponent(new ReportItemPanel(reportTemplate));
		}

	}

	/**
	 * List item in edit state.
	 * 
	 * @author Zbigniew Malinowski
	 *
	 */
	private class EditReportItemPanel extends Panel {

		private ReportItemPanel item;
		private ReportTemplate temporaryData;
		private BeanItem<ReportTemplate> beanItem;

		public EditReportItemPanel(ReportItemPanel item) {
			this.item = item;
			setStyleName(EDIT_PANEL_STYLE);
			deepCopy(item.reportTemplate, temporaryData = new ReportTemplate());
			beanItem = new BeanItem<ReportTemplate>(temporaryData);

			HorizontalLayout headerRow = ComponentFactory.createHLayoutFull(this);

			TextField nameField = new TextField(beanItem.getItemProperty(REPORTNAME_PROPERTY));
			headerRow.addComponent(nameField);
			headerRow.setComponentAlignment(nameField, Alignment.MIDDLE_LEFT);

			headerRow.addComponent(new Label());

			HorizontalLayout uploadCell = ComponentFactory.createHLayout(headerRow);

			ComponentFactory.createLabel(beanItem, FILENAME_PROPERTY, FILE_NAME_STYLE, uploadCell);

			ReportReceiver uploadReceiver = new ReportReceiver(temporaryData);
			uploadReceiver.addListener(new ReportReceivedListener() {

				@Override
				public void reportReceived(ReportTemplate reportTemplate) {
					requestRepaintAll();
				}
			});

			Upload changeReportupload = new Upload(null, uploadReceiver);
			changeReportupload.setWidth(null);
			changeReportupload.addListener((Upload.SucceededListener) uploadReceiver);
			changeReportupload.addListener((Upload.FailedListener) uploadReceiver);
			changeReportupload.setImmediate(true);
			changeReportupload.setButtonCaption(VaadinUtil.getValue(REPORT_MANAGER_ITEM_UPLOAD_CHANGE));
			uploadCell.addComponent(changeReportupload);
			headerRow.addComponent(uploadCell);
			headerRow.setComponentAlignment(uploadCell, Alignment.MIDDLE_RIGHT);

			ComponentFactory.createTextArea(beanItem, DESCRIPTION_PROPERTY, REPORT_MANAGER_ITEM_EDIT_DESC_PROMPT, this);

			HorizontalLayout footerRow = ComponentFactory.createHLayoutFull(this);
			HorizontalLayout checkboxCell = ComponentFactory.createHLayout(footerRow);
			ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_ACTIVE, beanItem, ACTIVE_PROPERTY, checkboxCell);
			ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_ONLINE, beanItem, ALLOW_ONLINE_DISPLAY_PROPERTY,
					checkboxCell);
			ComponentFactory.createCheckBox(REPORT_MANAGER_ITEM_EDIT_BACKGROUND, beanItem,
					ALLOW_BACKGROUND_PROCESSING_PROPERTY, checkboxCell);

			footerRow.addComponent(new Label());

			HorizontalLayout buttonsCell = ComponentFactory.createHLayout(footerRow);

			Button save = ComponentFactory.createButton(BUTTON_OK, null, buttonsCell);
			save.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					saveChanges();

				}
			});
			Button cancel = ComponentFactory.createButton(BUTTON_CANCEL, null, buttonsCell);
			cancel.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					discardChanges();

				}
			});

			footerRow.setComponentAlignment(buttonsCell, Alignment.MIDDLE_RIGHT);

		}

		protected void discardChanges() {
			list.replaceComponent(this, item);
		}

		protected void saveChanges() {
			list.replaceComponent(this, this.item);
			deepCopy(temporaryData, item.reportTemplate);
			item.requestRepaintAll();
			ReportTemplateDAO.saveOrUpdate(item.reportTemplate);

		}
	}

	/**
	 * List item in normal state.
	 * 
	 * @author Zbigniew Malinowski
	 *
	 */
	private class ReportItemPanel extends Panel{

		private ReportTemplate reportTemplate;
		private BeanItem<ReportTemplate> beanItem;
		private ReportParamPanel paramsPanel = null;
		private Button toggleParams;

		public ReportItemPanel(ReportTemplate reportTemplate) {
			this.reportTemplate = reportTemplate;
			beanItem = new BeanItem<ReportTemplate>(this.reportTemplate);
			setStyleName(PANEL_STYLE);
			HorizontalLayout headerRow = new HorizontalLayout();
			headerRow.setWidth("100%");
			addComponent(headerRow);

			Label reportNameLabel = ComponentFactory.createLabel(beanItem, REPORTNAME_PROPERTY, REPORT_NAME_STYLE,
					headerRow);

			Label spacer = new Label();
			headerRow.addComponent(spacer);

			Label changedDateLabel = ComponentFactory.createDateLabel(beanItem, CREATED_PROPERTY, CHANGED_DATE_STYLE,
					headerRow);

			headerRow.setComponentAlignment(reportNameLabel, Alignment.MIDDLE_LEFT);
			headerRow.setComponentAlignment(changedDateLabel, Alignment.MIDDLE_RIGHT);

			HorizontalLayout uploadRow = new HorizontalLayout();
			addComponent(uploadRow);

			Label desc = ComponentFactory.createLabel(beanItem, DESCRIPTION_PROPERTY, DESC_STYLE, this);
			desc.setWidth("100%");

			HorizontalLayout footerRow = new HorizontalLayout();
			addComponent(footerRow);
			footerRow.setSpacing(true);
			toggleParams = ComponentFactory.createButton(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE, BaseTheme.BUTTON_LINK, footerRow);
			toggleParams.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					toggleParams();
				}

			});
			Button reportSettingsButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_EDIT,
					BaseTheme.BUTTON_LINK, footerRow);
			reportSettingsButton.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					editReportData(ReportItemPanel.this);
				}

			});

			Button downloadButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_DOWNLOAD, BaseTheme.BUTTON_LINK,
					footerRow);
			downloadButton.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					download();
				}

			});

			Button removeButton = ComponentFactory.createButton(REPORT_MANAGER_ITEM_REMOVE, BaseTheme.BUTTON_LINK,
					footerRow);
			removeButton.addListener(new ClickListener() {

				@Override
				public void buttonClick(ClickEvent event) {
					removeMe();
				}
			});
		}

		protected void removeMe() {
			remove(this);

		}

		private void toggleParams() {
			if (paramsPanel == null){
				addComponent(paramsPanel = new ReportParamPanel(reportTemplate, ParamPanelType.REPORT_MANAGER));
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_FALSE));
			}
			else {
				removeComponent(paramsPanel);
				paramsPanel = null;
				toggleParams.setCaption(VaadinUtil.getValue(REPORT_PARAMS_TOGGLE_VISIBILITY_TRUE));
			}
		}

		private void download() {
			byte[] reportContent = Base64.decodeBase64(reportTemplate.getContent().getBytes());
			FileStreamer.openFileInCurrentWindow(getApplication(), reportTemplate.getFilename(), reportContent,
					"application/octet-stream");
		}

	}

	protected void editReportData(ReportItemPanel reportItemPanel) {
		EditReportItemPanel edit = new EditReportItemPanel(reportItemPanel);
		list.replaceComponent(reportItemPanel, edit);

	}

	private void remove(ReportItemPanel reportItemPanel) {
		ReportTemplateDAO.remove(reportItemPanel.reportTemplate);
		list.removeComponent(reportItemPanel);
	}

	/**
	 * Class handling file upload.
	 * 
	 * @author Zbigniew Malinowski
	 *
	 */
	private class ReportReceiver implements Upload.Receiver, Upload.SucceededListener, Upload.FailedListener {

		private ByteArrayOutputStream baos;
		private ReportTemplate reportTemplate;
		private List<ReportReceivedListener> listeners = new LinkedList<ReportManagerComponent.ReportReceivedListener>();

		public ReportReceiver(ReportTemplate reportTemplate) {
			this.reportTemplate = reportTemplate;

		}

		@Override
		public void uploadFailed(FailedEvent event) {
			throw new AperteReportsRuntimeException(ErrorCodes.DUPLICATE_REPORT_NAME);
		}

		@Override
		public void uploadSucceeded(SucceededEvent event) {
			String content = new String(Base64.encodeBase64(baos.toByteArray()));
			try {
				ReportMaster rm = new ReportMaster(content, new ReportTemplateProvider());
				reportTemplate.setReportname(rm.getReportName());
			} catch (AperteReportsException e) {
				throw new AperteReportsRuntimeException(e);
			}

			reportTemplate.setContent(content);
			reportTemplate.setFilename(event.getFilename());
			if (StringUtils.isEmpty(reportTemplate.getDescription())) {
				reportTemplate.setDescription("");
			}
			if (reportTemplate.getId() != null) {
				ReportCache.removeReport(reportTemplate.getId().toString());
			}
			ReportTemplateDAO.saveOrUpdate(reportTemplate);

			for (ReportReceivedListener l : listeners) {
				l.reportReceived(reportTemplate);
			}

		}

		@Override
		public OutputStream receiveUpload(String filename, String mimeType) {
			return baos = new ByteArrayOutputStream();
		}

		public void addListener(ReportReceivedListener listener) {
			listeners.add(listener);
		}

	}

	private interface ReportReceivedListener {
		public void reportReceived(ReportTemplate reportTemplate);
	}

	private static void deepCopy(ReportTemplate source, ReportTemplate target) {
		target.setActive(source.getActive());
		target.setAllowBackgroundOrder(source.getAllowBackgroundOrder());
		target.setAllowOnlineDisplay(source.getAllowOnlineDisplay());
		target.setContent(source.getContent());
		target.setCreated(source.getCreated());
		target.setDescription(source.getDescription());
		target.setFilename(source.getFilename());
		target.setId(source.getId());
		target.setReportname(source.getReportname());
	}

	private List<ReportTemplate> loadReports(String filter) {
		return (List<ReportTemplate>) ReportTemplateDAO.filterReports(filter);
	}

}
