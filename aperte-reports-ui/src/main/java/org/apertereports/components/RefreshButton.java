package org.apertereports.components;

import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Displays a button with a refresh icon.
 */
public class RefreshButton extends Button {

    private static final int DEFAULT_WIDTH_IN_UNITS = 16;

    public RefreshButton(String caption, Button.ClickListener listener) {
        setDescription(caption);
        addStyleName(BaseTheme.BUTTON_LINK);
        setWidth(DEFAULT_WIDTH_IN_UNITS, UNITS_PIXELS);
        addListener(listener);
    }

    @Override
    public void attach() {
        super.attach();
        setIcon(new ClassResource(RefreshButton.class, "/icons/refresh_16.png", getApplication()));

    }
}
