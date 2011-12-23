package org.apertereports.components;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;

/**
 * A simple horizontal layout that adds a list of components.
 * Each component has the expand ratio set to 1.0F and is aligned to middle-center position.
 */
public class SimpleHorizontalLayout extends HorizontalLayout {
    public SimpleHorizontalLayout(Component... components) {
        super();
        setSizeFull();
        for (Component c : components) {
            addComponent(c);
            setComponentAlignment(c, Alignment.MIDDLE_CENTER);
            setExpandRatio(c, 1.0f);
        }
        setSpacing(true);
    }
}
