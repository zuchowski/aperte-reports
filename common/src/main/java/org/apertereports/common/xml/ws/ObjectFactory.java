
package org.apertereports.common.xml.ws;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apertereports.common.xml.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apertereports.common.xml.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ReportData }
     * 
     */
    public ReportData createReportData() {
        return new ReportData();
    }

    /**
     * Create an instance of {@link ReportParameter }
     * 
     */
    public ReportParameter createReportParameter() {
        return new ReportParameter();
    }

    /**
     * Create an instance of {@link ReportExporterParameter }
     * 
     */
    public ReportExporterParameter createReportExporterParameter() {
        return new ReportExporterParameter();
    }

    /**
     * Create an instance of {@link GenerateReportResponse }
     * 
     */
    public GenerateReportResponse createGenerateReportResponse() {
        return new GenerateReportResponse();
    }

    /**
     * Create an instance of {@link GenerateReportRequest }
     * 
     */
    public GenerateReportRequest createGenerateReportRequest() {
        return new GenerateReportRequest();
    }

}
