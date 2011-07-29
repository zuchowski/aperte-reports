
package pl.net.bluesoft.rnd.apertereports.common.xml.ws;

import java.util.ArrayList;
import java.util.List;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ReportData complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ReportData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="format" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="locale" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataSource" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="characterEncoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="exporterParameters" type="{http://bluesoft.net.pl/rnd/apertereports/schemas}ReportExporterParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="reportParameters" type="{http://bluesoft.net.pl/rnd/apertereports/schemas}ReportParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReportData", namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas", propOrder = {
    "id",
    "name",
    "source",
    "format",
    "locale",
    "dataSource",
    "characterEncoding",
    "exporterParameters",
    "reportParameters"
})
public class ReportData {

    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas", required = true)
    protected String id;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected String name;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas", required = true)
    @XmlMimeType("*/*")
    protected DataHandler source;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas", required = true)
    protected String format;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected String locale;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected String dataSource;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected String characterEncoding;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected List<ReportExporterParameter> exporterParameters;
    @XmlElement(namespace = "http://bluesoft.net.pl/rnd/apertereports/schemas")
    protected List<ReportParameter> reportParameters;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the source property.
     * 
     * @return
     *     possible object is
     *     {@link DataHandler }
     *     
     */
    public DataHandler getSource() {
        return source;
    }

    /**
     * Sets the value of the source property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataHandler }
     *     
     */
    public void setSource(DataHandler value) {
        this.source = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFormat(String value) {
        this.format = value;
    }

    /**
     * Gets the value of the locale property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Sets the value of the locale property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocale(String value) {
        this.locale = value;
    }

    /**
     * Gets the value of the dataSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDataSource() {
        return dataSource;
    }

    /**
     * Sets the value of the dataSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDataSource(String value) {
        this.dataSource = value;
    }

    /**
     * Gets the value of the characterEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    /**
     * Sets the value of the characterEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCharacterEncoding(String value) {
        this.characterEncoding = value;
    }

    /**
     * Gets the value of the exporterParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the exporterParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExporterParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReportExporterParameter }
     * 
     * 
     */
    public List<ReportExporterParameter> getExporterParameters() {
        if (exporterParameters == null) {
            exporterParameters = new ArrayList<ReportExporterParameter>();
        }
        return this.exporterParameters;
    }

    /**
     * Gets the value of the reportParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the reportParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReportParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ReportParameter }
     * 
     * 
     */
    public List<ReportParameter> getReportParameters() {
        if (reportParameters == null) {
            reportParameters = new ArrayList<ReportParameter>();
        }
        return this.reportParameters;
    }

}
