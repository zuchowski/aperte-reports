package org.apertereports.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

    private static final long serialVersionUID = -7196776812526154079L;
    /**
     * Id indicating access for all roles to the report
     */
    public static String ACCESS_ALL_ROLES_ID = "all";
    transient private String rolesWithAccessS_used = null;
    transient private boolean accessibleForAllRoles = false;
    transient private Set<Long> rolesWithAccessSet = new HashSet<Long>();
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
     * <b>role1_id,role2id,...</b> - list of roles which have access
     */
    @Column(name = "roles_with_access")
    private String rolesWithAccessS;

    public ReportTemplate() {
        System.out.println("   REATING RT ---------------------------------");
    }

    /**
     * For use in application see {@link #getRolesWithAccess()}
     *
     * @return Built-in format of roles with access string
     * @see #isAccessibleForAllRoles()
     * @see #getRolesWithAccess()
     */
    public String getRolesWithAccessS() {
        return rolesWithAccessS;
    }

    /**
     * For use in application see {@link #setRolesWithAccess(java.util.List)}
     *
     * @param Built-in format of roles with access string
     * @see #setAccessibleForAllRoles()
     * @see #setRolesWithAccess(java.util.List)
     */
    public void setRolesWithAccessS(String rolesWithAccessS) {
        System.out.println("set roles with access: " + rolesWithAccessS);
        this.rolesWithAccessS = rolesWithAccessS;
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
     * Determines if the report is accessible for all roles
     *
     * @return true if the report is accessible for all roles, false otherwise
     */
    public boolean isAccessibleForAllRoles() {
        refresh();
        return accessibleForAllRoles;
    }

    /**
     * Returns the set of role ids with access to the report. The set can be
     * ampty when none role has access or all roles have access. First, the
     * method
     * {@link #isAccessibleForAllRoles()} should be used.
     *
     * @return List of role ids
     * @see #isAccessibleForAllRoles()
     */
    public Set<Long> getRolesWithAccess() {
        refresh();
        return rolesWithAccessSet;
    }

    /**
     * Sets the report accessible for all roles in the application
     */
    public void setAccessibleForAllRoles() {
        setRolesWithAccessS(ACCESS_ALL_ROLES_ID);
    }

    /**
     * Sets the access to the report for roles with given ids. If none role has
     * to have the access, null value should be passed or empty set. To set the
     * access for all roles use {@link #setAccessibleForAllRoles()}
     *
     * @param ids List of roles ids. If the list is empty or null value is
     * passed, none role will have the access
     * @see #setAccessibleForAllRoles()
     */
    public void setRolesWithAccess(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            setRolesWithAccessS(ACCESS_ALL_ROLES_ID);
            return;
        }
        StringBuilder sb = new StringBuilder();
        Iterator<Long> it = ids.iterator();
        sb.append(it.next());
        while (it.hasNext()) {
            sb.append(",");
            sb.append(it.next());
        }
        setRolesWithAccessS(sb.toString());
    }

    /**
     * Determines if the role with given id has the access to the report
     *
     * @param roleId Role id
     * @return true if the role has access, false otherwise
     */
    public boolean hasRoleAccess(Long roleId) {
        refresh();
        return accessibleForAllRoles || rolesWithAccessSet.contains(roleId);
    }

    private void refresh() {

        if (rolesWithAccessS_used != null && rolesWithAccessS_used.equals(rolesWithAccessS)) {
            return;
        }
        rolesWithAccessS_used = rolesWithAccessS;

        accessibleForAllRoles = ACCESS_ALL_ROLES_ID.equals(rolesWithAccessS);

        rolesWithAccessSet.clear();
        if (accessibleForAllRoles) {
            return;
        }

        String[] t = rolesWithAccessS.split(",");
        for (String s : t) {
            try {
                Long id = Long.parseLong(s);
                rolesWithAccessSet.add(id);
            } catch (Exception e) {
            }
        }
        System.out.println("refresh s: " + rolesWithAccessSet.size());
    }

    /**
     * Field identifiers for Vaadin tables.
     */
    public enum Fields {
        //todots is it used?

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
