package org.apertereports.ws.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import org.apertereports.common.ARConstants;
import org.springframework.ws.WebServiceMessage;

public class WebServiceMessageUtils {

    private static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat(ARConstants.DATETIME_PATTERN);

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
            logger.info("Logging message at: " + DATETIME_FORMAT.format(new Date()) + "\n" + new String(baos.toByteArray()));
        } catch (IOException e) {
            printStackTraceToLog(logger, e);
        }
    }
}
