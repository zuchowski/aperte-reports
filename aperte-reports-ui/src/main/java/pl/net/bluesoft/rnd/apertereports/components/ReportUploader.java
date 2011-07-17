package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.engine.ReportCache;
import pl.net.bluesoft.rnd.apertereports.engine.ReportMaster;
import pl.net.bluesoft.rnd.apertereports.util.ExceptionUtil;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * A component that handles a report upload from a user.
 *
 * @see Upload
 */
public class ReportUploader extends CustomComponent implements Upload.SucceededListener, Upload.FailedListener,
        Upload.Receiver {

    private ReportTemplate report;
    private Layout root;
    private EditorForm reportEditForm;
    private ByteArrayOutputStream byteArray;

    public ReportUploader(EditorForm reportEditForm) {
        buildMainLayout();
        setCompositionRoot(root);

        this.reportEditForm = reportEditForm;
    }

    /**
     * Initializes the byte buffer when the upload starts.
     *
     * @param filename Input file name
     * @param MIMEType Content mime type
     * @return An output stream.
     */
    @Override
    public OutputStream receiveUpload(String filename, String MIMEType) {
        return byteArray = new ByteArrayOutputStream();

    }

    /**
     * Sets current report template.
     *
     * @param report Report template
     */
    public void setReport(ReportTemplate report) {
        this.report = report;
    }

    /**
     * Shows a notification when the upload failed.
     *
     * @param event Failure event
     */
    @Override
    public void uploadFailed(FailedEvent event) {
        ExceptionUtil.logSevereException(event.getReason());
        NotificationUtil.showExceptionNotification(getWindow(), pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("exception.upload_failed.title"),
                pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("exception.upload_failed.description"));
    }

    /**
     * Invoked when the report has been received successfully.
     * The data is then compiled by the Jasper engine and updated
     * in the report edit form.
     *
     * @param event Success event
     */
    @Override
    public void uploadSucceeded(SucceededEvent event) {
        String content = new String(Base64.encodeBase64(byteArray.toByteArray()));
		try {
			ReportMaster rm = new ReportMaster(content);
			report.setReportname(rm.getReportName());
		}catch (JRException e) {
			ExceptionUtil.logSevereException(e);
			NotificationUtil.showExceptionNotification(getWindow(), pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("exception.compilation_failed.title"),
					pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("exception.compilation_failed.description"));
		}
		report.setContent(content.toCharArray());
		report.setFilename(event.getFilename());
		if (StringUtils.isEmpty(report.getDescription())) {
			report.setDescription("");
		}
//            if (report.getActive()) {
//                report.setActive(true);
//            }
//            else {
//                report.setActive(false);
//            }

		if (report.getId() != null) {
			ReportCache.removeReport(report.getId());
		}
		reportEditForm.reload();

    }

    /**
     * Builds main view.
     *
     * @return A layout
     */
    private Layout buildMainLayout() {
        root = new HorizontalLayout();

        final Upload upload = new Upload(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("manager.form.upload.prompt"), this);
        upload.setButtonCaption(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("manager.form.upload.button"));
        upload.setSizeFull();
        upload.addListener((Upload.SucceededListener) this);
        upload.addListener((Upload.FailedListener) this);
        root.addComponent(upload);

        setWidth("100.0%");
        setHeight("100.0%");

        return root;
    }
}
