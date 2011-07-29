package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import pl.net.bluesoft.rnd.apertereports.util.VaadinUtil;

/**
 * Displays a HTML formatted label.
 */
public class HelpComponent extends CustomComponent {
    public HelpComponent(String helpContentKey) {
        setCompositionRoot(new Label(VaadinUtil.getValue(helpContentKey), Label.CONTENT_XHTML));
    }
}
