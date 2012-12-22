package org.apertereports.components;

import com.vaadin.ui.*;
import com.vaadin.ui.themes.BaseTheme;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.apertereports.backbone.users.UserRole;
import org.apertereports.backbone.users.UserRoleProvider;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;
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
    private Panel parent = null;

    private RolePermissionsPanel() {
    }

    public RolePermissionsPanel(final Panel parent, final ReportTemplate rt) {

        this.parent = parent;
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
                System.out.println("RWA: " + rt.getRolesWithAccessS());
                ReportTemplateDAO.saveOrUpdate(rt);

                removePanelFromParent();
            }
        });
        Button cancelButton = ComponentFactory.createButton(UiIds.LABEL_CANCEL, null, buttonsLayout);
        cancelButton.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                removePanelFromParent();
            }
        });

        mainLayout.addComponent(rolesLayout);
        Label spaceLabel = new Label();
        spaceLabel.setHeight("5px");
        mainLayout.addComponent(spaceLabel);
        mainLayout.addComponent(buttonsLayout);
    }

    private void removePanelFromParent() {
        //todo it has to be done with a kind of close listener to change the view and behaviour of hide permissions button
        if (parent == null) {
            return;
        }
        parent.removeComponent(this);
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
