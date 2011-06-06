package pl.net.bluesoft.rnd.vries.util;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;

import eu.livotov.tpt.gui.windows.TPTWindow;

/**
 * An implementation of a {@link Button.ClickListener} which simply opens a new window with a message.
 * The window is then disposable either by pressing esc or enter button.
 */
public final class OnClickOpenMessageInNewWindow implements Button.ClickListener {
    private final String content;
    private final Component sourceComponent;
    private final String title;
    private final int contentMode;

    public OnClickOpenMessageInNewWindow(Component sourceComponent, String title, String content, int contentMode) {
        this.content = content;
        this.sourceComponent = sourceComponent;
        this.title = title;
        this.contentMode = contentMode;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        TPTWindow subwindow = new TPTWindow(title) {
            @Override
            public void enterKeyPressed() {
                getParent().removeWindow(this);
            }

            @Override
            public void escapeKeyPressed() {
                close();
            }
        };

        Label contentLabel = new Label(content);
        contentLabel.setContentMode(contentMode);
        subwindow.addComponent(contentLabel);
        subwindow.setModal(true);
        subwindow.setWidth("40%");

        sourceComponent.getWindow().addWindow(subwindow);
    }
}
