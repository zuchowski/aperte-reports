package org.apertereports.common.users;

/**
 * Class represents user role
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class UserRole {

    private String name;
    private long id;
    private final boolean administrator;

    /**
     * Creates new user role
     *
     * @param name Name of role
     * @param id Id of role
     */
    public UserRole(String name, long id, boolean administrator) {
        this.name = name;
        this.id = id;
        this.administrator = administrator;
    }

    /**
     * Returns id
     *
     * @return Id
     */
    public long getId() {
        return id;
    }

    /**
     * Returns name
     *
     * @return Name
     */
    public String getName() {
        return name;
    }

    /**
     * Determines if this role is administrator role
     *
     * @return true if administrator role, false otherwise
     */
    public boolean isAdministrator() {
        return administrator;
    }
}
