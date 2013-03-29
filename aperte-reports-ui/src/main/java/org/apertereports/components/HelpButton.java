package org.apertereports.components;

import org.apertereports.components.HelpWindow.Module;
import org.apertereports.components.HelpWindow.Tab;
import org.apertereports.util.VaadinUtil;

import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.ui.UiIds;

/**
 * Displays an icon button with help. Shows help contents in a new window.
 */
public class HelpButton extends Button {

    private static final int DEFAULT_WIDTH_IN_UNITS = 20;

    public HelpButton(final Module module, final Tab tab) {
        setDescription(VaadinUtil.getValue(UiIds.LABEL_HELP));
        addStyleName(BaseTheme.BUTTON_LINK);
        setWidth(DEFAULT_WIDTH_IN_UNITS, UNITS_PIXELS);
        addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                HelpWindow subwindow = new HelpWindow(module, tab);
                getApplication().getMainWindow().addWindow(subwindow);
            }
        });
    }

    @Override
    public void attach() {
        super.attach();
        setIcon(new ClassResource("/icons/help.png", getApplication()));

    }
}
