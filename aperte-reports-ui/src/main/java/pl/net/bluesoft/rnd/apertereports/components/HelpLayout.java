package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Module;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Tab;

import java.util.Iterator;

/**
 * Displays a help button wrapped into an expanding layout.
 */
public class HelpLayout extends HorizontalLayout {
    public HelpLayout(Module module, Tab tab) {
        setSpacing(true);
        Label helpText = new Label(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("global.help.title"));
        helpText.setSizeFull();
        addComponent(helpText);
        addComponent(new HelpButton(module, tab));
        for (Iterator<Component> it = getComponentIterator(); it.hasNext(); ) {
            Component comp = it.next();
            setComponentAlignment(comp, Alignment.MIDDLE_CENTER);
        }
        setExpandRatio(getComponent(0), 1.0f);
    }
}
