package org.apertereports.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Window;

/**
 * A simple modal window with fixed height. The base component is added to a scrollable panel.
 */
//xxx maybe in the future use modal windows to perform some configuration actions?
public class ModalWindow extends Window {
    public ModalWindow(String caption, Component component) {
        super(caption);

        Panel content = new Panel();
        content.setSizeFull();
        content.setScrollable(true);
        content.setSizeUndefined();
        content.setHeight(700, UNITS_PIXELS);
        content.addComponent(component);

        setContent(content);
        setModal(true);
        setResizable(true);
    }
}
