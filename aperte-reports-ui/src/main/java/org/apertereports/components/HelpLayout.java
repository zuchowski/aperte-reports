package org.apertereports.components;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import java.util.Iterator;

import org.apertereports.components.HelpWindow.Module;
import org.apertereports.components.HelpWindow.Tab;
import org.apertereports.util.VaadinUtil;

/**
 * Displays a help button wrapped into an expanding layout.
 */
public class HelpLayout extends HorizontalLayout {
    public HelpLayout(Module module, Tab tab) {
        setSpacing(true);
        Label helpText = new Label(VaadinUtil.getValue("global.help.title"));
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
