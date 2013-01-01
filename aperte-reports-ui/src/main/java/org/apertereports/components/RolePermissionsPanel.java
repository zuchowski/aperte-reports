package org.apertereports.components;

import com.vaadin.ui.*;
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
import org.apertereports.ui.UiIds;
import org.apertereports.util.ComponentFactory;
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
        VerticalLayout mainLayout = new VerticalLayout();
        ((AbstractLayout) mainLayout).setMargin(true, true, false, true);

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
        final VerticalLayout rolesLayout = new VerticalLayout();
        rolesLayout.setSpacing(true);

        HorizontalLayout allNoneButtons = new HorizontalLayout();
        allNoneButtons.setSpacing(true);
        Button allButton = ComponentFactory.createButton(UiIds.LABEL_ALL, BaseTheme.BUTTON_LINK, allNoneButtons);
        allButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setWrappersSelected(true);
            }
        });
        Button noneButton = ComponentFactory.createButton(UiIds.LABEL_NONE, BaseTheme.BUTTON_LINK, allNoneButtons);
        noneButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                setWrappersSelected(false);
            }
        });

        rolesLayout.addComponent(allNoneButtons);

        for (UserRoleWrapper w : wrappers) {
            rolesLayout.addComponent(w.checkBox);
        }

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        Button okButton = ComponentFactory.createButton(UiIds.LABEL_OK, null, buttonsLayout);
        okButton.addListener(new Button.ClickListener() {

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
        });
        Button cancelButton = ComponentFactory.createButton(UiIds.LABEL_CANCEL, null, buttonsLayout);
        cancelButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                fireCloseListener();
            }
        });

        mainLayout.addComponent(rolesLayout);
        Label spaceLabel = new Label();
        spaceLabel.setHeight("5px");
        mainLayout.addComponent(spaceLabel);
        mainLayout.addComponent(buttonsLayout);
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
