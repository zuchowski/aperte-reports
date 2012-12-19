package org.apertereports.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

/**
 * Holds all the Jasper report template data and specific report configuration.
 */
@Entity
@Table(name = "ar_report_template")
public class ReportTemplate implements Serializable {

    private static final long serialVersionUID = -7196776812526154078L;
    /**
     * Indicates this report is active or not.
     */
    @Column
    private Boolean active = true;
    /**
     * JRXML data formatted in Base64 manner.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column
    @Basic(fetch = FetchType.LAZY)
    private String content;
    /**
     * Date of creation.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", updatable = false)
    private Date created = new Date();
    /**
     * Description of the report template.
     */
    @Column
    private String description;
    /**
     * Filename it was uploaded from.
     */
    @Column(nullable = false)
    private String filename;
    /**
     * Should report engine allow online display.
     */
    @Column(name = "allow_online_display")
    private Boolean allowOnlineDisplay = true;
    /**
     * Should report engine allow background order generation.
     */
    @Column(name = "allow_background_order")
    private Boolean allowBackgroundOrder = true;
    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "id", nullable = false, length = 10)
    private Integer id;
    /**
     * Report name taken from JRXML it was uploaded from (the
     * <code>name</code> attribute of
     * <code>jasperReport</code> tag).
     */
    @Column(unique = true, nullable = false)
    private String reportname;
    /**
     * Contains list of user roles with access to the report. This variable can
     * be set to one of the values: <p> <b>all</b> - every user has access<br>
     * <b>none</b> - no user has access (but user with Administrator role
     * does)<br> <b>role1_id,role2id,...</b> - list of roles which have access
     * (plus extra Administrator access)
     */
    @Column(name = "roles_with_access")
    private String rolesWithAccess;

    public String getRolesWithAccess() {
        return rolesWithAccess;
    }

    public void setRolesWithAccess(String rolesWithAccess) {
        this.rolesWithAccess = rolesWithAccess;
    }

    public boolean getActive() {
        return active != null && active;
    }

    public String getContent() {
        return content;
    }

    public Date getCreated() {
        return created;
    }

    public String getDescription() {
        return description;
    }

    public String getFilename() {
        return filename;
    }

    public Integer getId() {
        return id;
    }

    public String getReportname() {
        return reportname;
    }

    public Boolean getAllowBackgroundOrder() {
        return allowBackgroundOrder != null && allowBackgroundOrder;
    }

    public Boolean getAllowOnlineDisplay() {
        return allowOnlineDisplay != null && allowOnlineDisplay;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setAllowBackgroundOrder(Boolean allowBackgroundOrder) {
        this.allowBackgroundOrder = allowBackgroundOrder;
    }

    public void setAllowOnlineDisplay(Boolean allowOnlineDisplay) {
        this.allowOnlineDisplay = allowOnlineDisplay;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setReportname(String reportname) {
        this.reportname = reportname;
    }

    /**
     * Field identifiers for Vaadin tables.
     */
    public enum Fields {

        ACTIVE, CONTENT, CREATED, DESCRIPTION, FILENAME, ALLOW_ONLINE_DISPLAY, ALLOW_BACKGROUND_ORDER, REPORTNAME, ID
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((reportname == null) ? 0 : reportname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ReportTemplate other = (ReportTemplate) obj;
        if (reportname == null) {
            if (other.reportname != null) {
                return false;
            }
        } else if (!reportname.equals(other.reportname)) {
            return false;
        }
        return true;
    }
}
