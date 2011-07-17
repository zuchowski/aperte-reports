package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.themes.BaseTheme;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Module;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Tab;

/**
 * Displays an icon button with help. Shows help contents in a new window.
 */
public class HelpButton extends Button {
    public HelpButton(final Module module, final Tab tab) {
        setDescription(TM.get("global.help.button"));
        setIcon(new ThemeResource("icons/help.png"));
        addStyleName(BaseTheme.BUTTON_LINK);
        setWidth(20, UNITS_PIXELS);
        addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                HelpWindow subwindow = new HelpWindow(module, tab);
                getApplication().getMainWindow().addWindow(subwindow);
            }
        });
    }
}
