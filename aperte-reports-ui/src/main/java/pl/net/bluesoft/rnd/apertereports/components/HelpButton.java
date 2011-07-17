package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.Application;
import com.vaadin.terminal.ClassResource;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
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
        setDescription(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("global.help.button"));
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

    @Override
    public void attach() {
        super.attach();
        setIcon(new ClassResource("/icons/help.png", getApplication()));

    }
}
