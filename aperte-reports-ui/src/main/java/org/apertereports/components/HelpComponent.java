package org.apertereports.components;

import org.apertereports.util.VaadinUtil;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;

/**
 * Displays a HTML formatted label.
 */
public class HelpComponent extends CustomComponent {
    public HelpComponent(String helpContentKey) {
        setCompositionRoot(new Label(VaadinUtil.getValue(helpContentKey), Label.CONTENT_XHTML));
    }
}
