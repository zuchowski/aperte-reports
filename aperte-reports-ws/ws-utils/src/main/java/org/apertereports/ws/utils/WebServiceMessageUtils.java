package org.apertereports.ws.utils;

import org.springframework.ws.WebServiceMessage;
import org.apertereports.common.ReportConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Logger;

public class WebServiceMessageUtils {
    public static void printStackTraceToLog(Logger logger, Exception e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        sw.flush();
        logger.info(e.getMessage());
        logger.info(sw.toString());
    }

    public static void printMessageToLog(Logger logger, WebServiceMessage message) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            message.writeTo(baos);
            logger.info("Logging message at: " + ReportConstants.DATETIME_FORMAT.format(new Date()) + "\n" + new String(baos.toByteArray()));
        }
        catch (IOException e) {
            printStackTraceToLog(logger, e);
        }
    }
}
