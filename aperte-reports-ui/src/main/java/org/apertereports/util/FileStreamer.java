package org.apertereports.util;

import com.vaadin.Application;
import com.vaadin.terminal.StreamResource;
import org.apache.commons.lang.StringUtils;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.common.ReportConstants.ReportMimeType;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * A utility class that handles opening of a new window with a report stream.
 */
public class FileStreamer {

    /**
     * Opens a new download window with provided content.
     *
     * @param app The application
     * @param fileName The filename linked with the content
     * @param fileContent The content
     * @param mimeType The exact mime type of the content
     */
    public static void openFileInNewWindow(Application app, String fileName, final byte[] fileContent, String mimeType) {
        openFile(app, fileName, fileContent, mimeType, "_new");
    }

    private static void openFile(Application app, String fileName,
            final byte[] fileContent, String mimeType, String windowName) {
        StreamResource.StreamSource source = new StreamResource.StreamSource() {

            @Override
            public InputStream getStream() {
                return new ByteArrayInputStream(fileContent);
            }
        };

        StreamResource resource = new StreamResource(source, fileName, app);
        resource.setMIMEType(mimeType);
        resource.setCacheTime(-1);
        app.getMainWindow().open(resource, windowName);
    }

    /**
     * Opens a new download window with provided content.
     *
     * @param app The application
     * @param reportName The name of the report
     * @param report The report content
     * @param format The format of the report (i.e. PDF)
     */
    public static void showFile(Application app, String reportName, final byte[] report, String format) {
        try {
            String fileName = reportName + "." + StringUtils.lowerCase(format);
            openFileInNewWindow(app, fileName, report, ReportMimeType.valueOf(format).mimeType());
        } catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            NotificationUtil.showExceptionNotification(app.getMainWindow(), "exception.conversion", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Starts download in current window with provided content.
     *
     * @param app The application
     * @param reportName The name of the report
     * @param report The report content
     * @param format The format of the report (i.e. PDF)
     */
    public static void openFileInCurrentWindow(Application application,
            String filename, byte[] reportContent, String mimeType) {
        openFile(application, filename, reportContent, mimeType, "_self");

    }
}
