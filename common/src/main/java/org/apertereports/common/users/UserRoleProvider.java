package org.apertereports.common.users;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.service.RoleLocalServiceUtil;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.service.UserLocalServiceUtil;


/**
 * Class provides functionality of listing roles accessible in the application
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public final class UserRoleProvider {

    private UserRoleProvider() {
    }
    
    /**
     * Returns all available roles
     *
     * @return List of roles
     */
    public static List<UserRole> getAllRoles() {
        LinkedList<UserRole> roles = new LinkedList<UserRole>();

        List<Role> list;
        try {
            com.liferay.portal.model.User liferayUser = UserLocalServiceUtil.getUser(PrincipalThreadLocal.getUserId());
   			long companyid = liferayUser.getCompanyId();
   			list = RoleLocalServiceUtil.getRoles(companyid);        	        	
        } catch (Exception ex) {
            Logger.getLogger(UserRoleProvider.class.getName()).log(Level.SEVERE, null, ex);
            return roles;
        }

        for (Role r : list) {
            boolean admin = "Administrator".equals(r.getName());
            UserRole ur = new UserRole(r.getName(), r.getRoleId(), admin);
            roles.add(ur);
        }

        return roles;
    }
}
