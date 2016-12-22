package org.apertereports.common.users;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class represents user
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class User {

    private final String login;
    private Set<UserRole> roles = new HashSet<UserRole>();
    private final boolean administrator;
    private final String email;
    private long userid;
    private long groupid;
    private long companyid;
    private Map<String, Object> context;

    public User(String login, Set<UserRole> roles, boolean administrator, String email, long userid, long groupid, long companyid) {
    	this.userid= userid;
    	this.groupid=groupid;
    	this.companyid=companyid;
        this.login = login;
        this.roles = roles;
        this.administrator = administrator;
        this.email = email;
        this.context = null;
    }
    
    public User(String login, Set<UserRole> roles, boolean administrator, String email, long userid, long groupid, long companyid, Map<String, Object> context) {
    	this(login, roles, administrator, email, userid, groupid, companyid);
    	this.context = context;
    }
    /**
     * Return id of the user
     *
     * @return userid
     */
    public long getUserid() {
        return userid;
    }
    /**
     * Return id of the user
     *
     * @return groupid
     */
    public long getGroupid() {
        return groupid;
    }
    /**
     * Return id of the user
     *
     * @return companyid
     */
    public long getCompanyid() {
        return companyid;
    }
    /**
     * Return login of the user
     *
     * @return Login
     */
    public String getLogin() {
        return login;
    }

    /**
     * Returns collection of roles assigned to the user
     *
     * @return Collection of roles
     */
    public Collection<UserRole> getRoles() {
        return roles;
    }

    /**
     * Determines if user is administrator
     *
     * @return true if administrator, false otherwise
     */
    public boolean isAdministrator() {
        return administrator;
    }

    /**
     * Returns email address
     * @return Email address
     */
    public String getEmail() {
        return email;
    }
	public Map<String, Object> getContext() {
		return context;
	}
	public void setContext(Map<String, Object> context) {
		this.context = context;
	}
}