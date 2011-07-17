package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import eu.livotov.tpt.i18n.TM;

/**
 * Displays a HTML formatted label.
 */
public class HelpComponent extends CustomComponent {
    public HelpComponent(String helpContentKey) {
        setCompositionRoot(new Label(TM.get(helpContentKey), Label.CONTENT_XHTML));
    }
}
