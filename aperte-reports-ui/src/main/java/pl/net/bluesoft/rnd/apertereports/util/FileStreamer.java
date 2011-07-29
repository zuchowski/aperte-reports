package pl.net.bluesoft.rnd.apertereports.util;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants.ReportMimeType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A utility class that handles opening of a new window with a report stream.
 */
public class FileStreamer {
    /**
     * Opens a new download window with provided content.
     *
     * @param app         The application
     * @param fileName    The filename linked with the content
     * @param fileContent The content
     * @param mimeType    The exact mime type of the content
     */
    public static void openFileInNewWindow(Application app, String fileName, final byte[] fileContent, String mimeType) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {
            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(fileContent);
            }
        };

        StreamResource resource = new StreamResource(source, fileName, app);
        resource.setMIMEType(mimeType);
        app.getMainWindow().open(resource, "_new");
    }

    /**
     * Opens a new download window with provided content.
     *
     * @param app        The application
     * @param reportName The name of the report
     * @param report     The report content
     * @param format     The format of the report (i.e. PDF)
     */
    public static void showFile(Application app, String reportName, final byte[] report, String format) {
        try {
            String fileName = reportName + "." + StringUtils.lowerCase(format);
            openFileInNewWindow(app, fileName, report, ReportMimeType.valueOf(format).mimeType());
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            NotificationUtil.showExceptionNotification(app.getMainWindow(), "exception.conversion", e);
        }
    }
}
