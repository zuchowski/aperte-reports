//package org.apertereports.components;
//
//import com.vaadin.ui.CustomComponent;
//import com.vaadin.ui.HorizontalLayout;
//import com.vaadin.ui.Layout;
//import com.vaadin.ui.Upload;
//import com.vaadin.ui.Upload.FailedEvent;
//import com.vaadin.ui.Upload.SucceededEvent;
//import org.apache.commons.codec.binary.Base64;
//import org.apache.commons.lang.StringUtils;
//import org.apertereports.util.NotificationUtil;
//import org.apertereports.util.VaadinUtil;
//
//import org.apertereports.backbone.util.ReportTemplateProvider;
//import org.apertereports.common.exception.ReportException;
//import org.apertereports.common.exception.SubreportNotFoundException;
//import org.apertereports.common.utils.ExceptionUtils;
//import org.apertereports.engine.ReportCache;
//import org.apertereports.engine.ReportMaster;
//import org.apertereports.model.ReportTemplate;
//
//import java.io.ByteArrayOutputStream;
//import java.io.OutputStream;
//
///**
// * A component that handles a report upload from a user.
// *
// * @see Upload
// */
//public class ReportUploader extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener,
//        Upload.Receiver {
//
//    private ReportTemplate report;
//    private Layout root;
//    private ReportEditorForm reportEditForm;
//    private ByteArrayOutputStream byteArray;
//
//    public ReportUploader(ReportEditorForm reportEditForm) {
//        buildMainLayout();
//        setCompositionRoot(root);
//
//        this.reportEditForm = reportEditForm;
//    }
//
//    /**
//     * Initializes the byte buffer when the upload starts.
//     *
//     * @param filename Input file name
//     * @param MIMEType Content mime type
//     * @return An output stream.
//     */
//    @Override
//    public OutputStream receiveUpload(String filename, String MIMEType) {
//        return byteArray = new ByteArrayOutputStream();
//
//    }
//
//    /**
//     * Sets current report template.
//     *
//     * @param report Report template
//     */
//    public void setReport(ReportTemplate report) {
//        this.report = report;
//    }
//
//    /**
//     * Shows a notification when the upload failed.
//     *
//     * @param event Failure event
//     */
//    @Override
//    public void uploadFailed(FailedEvent event) {
//        ExceptionUtils.logSevereException(event.getReason());
//        NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.upload_failed.title"),
//                VaadinUtil.getValue("exception.upload_failed.description"));
//    }
//
//    /**
//     * Invoked when the report has been received successfully.
//     * The data is then compiled by the Jasper engine and updated
//     * in the report edit form.
//     *
//     * @param event Success event
//     */
//    @Override
//    public void uploadSucceeded(SucceededEvent event) {
//        String content = new String(Base64.encodeBase64(byteArray.toByteArray()));
//		try {
//			ReportMaster rm = new ReportMaster(content, new ReportTemplateProvider());
//			report.setReportname(rm.getReportName());
//		}catch (SubreportNotFoundException e) {
//			ExceptionUtils.logSevereException(e);
//			NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.subreport_not_found.title"),
//                    VaadinUtil.getValue("exception.subreport_not_found.description" + StringUtils.join(e.getReportName(), ", ")));
//            throw new RuntimeException(e);
//		}catch (ReportException e) {
//			ExceptionUtils.logSevereException(e);
//			NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.compilation_failed.title"),
//                    VaadinUtil.getValue("exception.compilation_failed.description"));
//            throw new RuntimeException(e);
//
//		}
//		report.setContent(content);
//		report.setFilename(event.getFilename());
//		if (StringUtils.isEmpty(report.getDescription())) {
//			report.setDescription("");
//		}
////            if (report.getActive()) {
////                report.setActive(true);
////            }
////            else {
////                report.setActive(false);
////            }
//
//		if (report.getId() != null) {
//			ReportCache.removeReport(report.getId().toString());
//		}
//		reportEditForm.reload();
//
//    }
//
//    /**
//     * Builds main view.
//     *
//     * @return A layout
//     */
//    private Layout buildMainLayout() {
//        root = new HorizontalLayout();
//
//        final Upload upload = new Upload(VaadinUtil.getValue("manager.form.upload.prompt"), this);
//        upload.setButtonCaption(VaadinUtil.getValue("manager.form.upload.button"));
//        upload.setSizeFull();
//        upload.addListener((Upload.SucceededListener) this);
//        upload.addListener((Upload.FailedListener) this);
//        root.addComponent(upload);
//
//        setWidth("100.0%");
//        setHeight("100.0%");
//
//        return root;
//    }
//}
