
package org.apertereports.common.xml.ws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="reportData" type="{http://bluesoft.net.pl/rnd/apertereports/schemas}ReportData"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "reportData"
})
@XmlRootElement(name = "GenerateReportRequest", namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
public class GenerateReportRequest {

    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas", required = true)
    protected ReportData reportData;

    /**
     * Gets the value of the reportData property.
     * 
     * @return
     *     possible object is
     *     {@link ReportData }
     *     
     */
    public ReportData getReportData() {
        return reportData;
    }

    /**
     * Sets the value of the reportData property.
     * 
     * @param value
     *     allowed object is
     *     {@link ReportData }
     *     
     */
    public void setReportData(ReportData value) {
        this.reportData = value;
    }

}
