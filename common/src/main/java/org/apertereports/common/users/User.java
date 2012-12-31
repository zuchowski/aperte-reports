package org.apertereports.common.users;

import java.util.Collection;
import java.util.HashSet;
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

    public User(String login, Set<UserRole> roles, boolean administrator) {
        this.login = login;
        this.roles = roles;
        this.administrator = administrator;
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
}