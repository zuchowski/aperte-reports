package org.apertereports.components;

import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apertereports.common.users.UserRole;
import org.apertereports.common.users.UserRoleProvider;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;
import org.apertereports.ui.CloseListener;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;
import org.apertereports.util.VaadinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class defines panel allowing configuration of the roles permission
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class RolePermissionsPanel extends Panel {

    private static final Logger logger = LoggerFactory.getLogger(RolePermissionsPanel.class.getName());
    private LinkedList<UserRoleWrapper> wrappers = new LinkedList<UserRoleWrapper>();
    private CloseListener closeListener = null;

    public RolePermissionsPanel(final ReportTemplate rt) {

        rt.isAccessibleForAllRoles();

        setCaption(VaadinUtil.getValue(UiIds.LABEL_PERMISSIONS));
        VerticalLayout mainLayout = UiFactory.createVLayout(null);
        ((AbstractLayout) mainLayout).setMargin(true, true, true, true);

        setContent(mainLayout);
        boolean all = rt.isAccessibleForAllRoles();
        Set<Long> rolesWithAccess = rt.getRolesWithAccess();

        //check report template configuration
        List<UserRole> roles = UserRoleProvider.getAllRoles();
        for (UserRole ur : roles) {
            UserRoleWrapper urw = new UserRoleWrapper(ur);
            wrappers.add(urw);
            if (all) {
                urw.setSelected(true);
                continue;
            }
            long id = ur.getId();
            for (long rid : rolesWithAccess) {
                if (id == rid) {
                    urw.setSelected(true);
                    break;
                }
            }
        }

        //initializing roles panel (layout)
        final VerticalLayout rolesLayout = UiFactory.createVLayout(mainLayout, FAction.SET_SPACING);

        HorizontalLayout allNoneButtons = UiFactory.createHLayout(rolesLayout, FAction.SET_SPACING);
        UiFactory.createButton(UiIds.LABEL_ALL, allNoneButtons, BaseTheme.BUTTON_LINK, new ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setWrappersSelected(true);
            }
        });
        UiFactory.createButton(UiIds.LABEL_NONE, allNoneButtons, BaseTheme.BUTTON_LINK, new ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setWrappersSelected(false);
            }
        });

        for (UserRoleWrapper w : wrappers) {
            rolesLayout.addComponent(w.checkBox);
        }

        UiFactory.createSpacer(mainLayout, null, "5px");

        HorizontalLayout buttonsLayout = UiFactory.createHLayout(mainLayout, FAction.SET_SPACING, FAction.SET_FULL_WIDTH);
        UiFactory.createSpacer(buttonsLayout, FAction.SET_EXPAND_RATIO_1_0);
        UiFactory.createButton(UiIds.LABEL_OK, buttonsLayout, new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Set<Long> rolesWithAccess = new HashSet<Long>();
                boolean all = true;
                for (UserRoleWrapper urw : wrappers) {
                    all &= urw.selected;
                    if (urw.selected) {
                        rolesWithAccess.add(urw.ur.getId());
                    }
                }
                if (all) {
                    rt.setAccessibleForAllRoles();
                } else {
                    rt.setRolesWithAccess(rolesWithAccess);
                }
                ReportTemplateDAO.saveOrUpdate(rt);

                fireCloseListener();
            }
        }, FAction.ALIGN_RIGTH);
        UiFactory.createButton(UiIds.LABEL_CANCEL, buttonsLayout, new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireCloseListener();
            }
        }, FAction.ALIGN_RIGTH);
    }

    /**
     * Sets close listener used when OK or CANCEL button is used
     *
     * @param closeListener Close listener
     */
    public void setCloseListener(CloseListener closeListener) {
        this.closeListener = closeListener;
    }

    private void fireCloseListener() {
        if (closeListener != null) {
            closeListener.close();
        }
    }

    private void setWrappersSelected(boolean b) {
        for (UserRoleWrapper w : wrappers) {
            w.setSelected(b);
        }
    }

    private class UserRoleWrapper {

        public final UserRole ur;
        public boolean selected;
        public CheckBox checkBox;

        public UserRoleWrapper(final UserRole ur) {
            this.ur = ur;
            selected = ur.isAdministrator();
            checkBox = new CheckBox(ur.getName(), selected);
            checkBox.setImmediate(true);
            this.checkBox.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if (ur.isAdministrator()) {
                        checkBox.setValue(Boolean.TRUE);
                    }
                    selected = (Boolean) checkBox.getValue();
                }
            });
        }

        public void setSelected(boolean b) {
            if (!b && ur.isAdministrator()) {
                return;
            }
            checkBox.setValue(Boolean.valueOf(b));
            selected = b;
        }
    }
}
