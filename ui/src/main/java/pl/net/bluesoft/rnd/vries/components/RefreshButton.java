package pl.net.bluesoft.rnd.vries.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;

/**
 * Displays a button with a refresh icon.
 */
public class RefreshButton extends Button {
    public RefreshButton(String caption, Button.ClickListener listener) {
        setDescription(caption);
        setIcon(new ThemeResource("icons/refresh_16.png"));
        addStyleName(BaseTheme.BUTTON_LINK);
        setWidth(16, UNITS_PIXELS);
        addListener(listener);
    }
}
