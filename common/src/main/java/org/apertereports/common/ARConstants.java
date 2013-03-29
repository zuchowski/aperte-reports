package org.apertereports.common;

import java.text.SimpleDateFormat;

/**
 * Contains constant fields used in the report generation process.
 */
public interface ARConstants {

    /**
     * Web service namespace.
     */
    String WS_NAMESPACE = "http://bluesoft.net.pl/rnd/apertereports/schemas";
    /**
     * Web service request local part.
     */
    String WS_REQUEST_LOCAL_PART = "GenerateReportRequest";
    /**
     * File prefix for log purposes.
     */
    String FILE_PREFIX = "AperteReports_";
    /**
     * A date format for writing in log files.
     */
    SimpleDateFormat FILE_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy_hhmmss");
    /**
     * CSV field delimiter.
     */
    String FIELD_DELIMITER = ";";
    /**
     * CSV record delimiter.
     */
    String RECORD_DELIMITER = "\n\r";
    /**
     * Cache manager's thread interval between each cache analysis. Set to 1
     * second (1000 milliseconds).
     */
    Integer CACHE_MANAGER_CHECK_INTERVAL = 1000;
    /**
     * A simple datetime pattern.
     */
    String DATETIME_PATTERN = "dd-MM-yyyy HH:mm";
    /**
     * JNDI JMS process report queue id
     */
    String JNDI_JMS_PROCESS_REPORT_QUEUE_ID = "java:comp/env/jms/ProcessReportQueue";
    /**
     * JNDI JMS generate report queue id
     */
    String JNDI_JMS_GENERATE_REPORT_QUEUE_ID = "java:comp/env/jms/GenerateReportQueue";
    /**
     * JMS connection factory id
     */
    String JNDI_JMS_CONNECTION_FACTORY_ID = "java:comp/env/jms/ConnectionFactory";
    /**
     * Report order id property name.
     */
    String JMS_PROPERTY_REPORT_ORDER_ID = "reportOrderId";
    /**
     * Key used to pass map with compiled subreports to JasperFiller
     */
    String SUBREPORT_MAP_PARAMETER_NAME = "SUBREPORT_MAP";

    /**
     * Represents a boolean report property.
     */
    public static enum BooleanValues {

        FALSE, TRUE
    }

    /**
     * Report generation error codes.
     */
    public static enum ErrorCode {

        DRILLDOWN_NOT_FOUND,
        DRILLDOWN_REPORT_NOT_FOUND,
        EMAIL_SESSION_NOT_FOUND,
        EMPTY_REPORT_SOURCE,
        /**
         * Font not found
         */
        FONT_NOT_FOUND,
        INVALID_DATASOURCE_TYPE,
        INVALID_EXPORTER_PARAMETER,
        /**
         * Invalid report type
         */
        INVALID_REPORT_TYPE,
        JASPER_REPORTS_EXCEPTION,
        JMS_UNAVAILABLE,
        /**
         * Access denied to the report
         */
        REPORT_ACCESS_DENIED,
        REPORT_SOURCE_EXCEPTION,
        /**
         * Report template not found
         */
        REPORT_TEMPLATE_NOT_FOUND,
        SERIALIZATION_EXCEPTION,
        SUBREPORT_NOT_FOUND,
        /**
         * Unknown error
         */
        UNKNOWN,
        UNSUPPORTED_ENCODING,
        UNKNOWN_PROPERTY_NAME
    }

    /**
     * Represents report parameter input types. This is transformed into an
     * adequate Vaadin input widget.
     */
    public static enum InputTypes {

        TEXT, DATE, TEXTAREA, SELECT, MULTISELECT, RADIOBUTTONS, CHECKBOXES, CHECKBOX, SPECIAL_CONTROL, FILTER, FILTERED_SELECT
    }

    /**
     * Different types of report parameters.
     */
    public static enum Keys {

        INPUT_TYPE, DICT_QUERY, WIDTH, MAXCHARS, REQUIRED, REQUIRED_ERROR, REGEXP, REGEXP_ERROR, ORDER, LABEL, SCRIPT_LANGUAGE, SPECIAL_CONTROL_CODE, SPECIAL_VALIDATION_CODE, SPECIAL_VALIDATION_ERROR, SPECIAL_DATA_QUERY_CODE, LEVEL, FILTER_GROUP, MULTIPLE_CHOICE, SELECT_ALL, DICT_ITEM_LIST
    }

    /**
     * Datasource parameter.
     */
    public static enum Parameter {

        DATASOURCE
    }

    /**
     * Allowed report formats.
     */
    public static enum ReportType {

        CSV, HTML, PDF, XLS;

        public static String[] stringValues() {
            String[] values = new String[values().length];
            for (int i = 0; i < values.length; i++) {
                values[i] = values()[i].name();
            }
            return values;
        }
    }

    /**
     * Simple enum for report type conversion to a mime type.
     */
    public static enum ReportMimeType {

        CSV("application/csv"), HTML("text/html"), PDF("application/pdf"), XLS("application/vnd.ms-excel");
        private final String mimeType;

        ReportMimeType(String mimeType) {
            this.mimeType = mimeType;
        }

        public String mimeType() {
            return mimeType;
        }
    }
}
