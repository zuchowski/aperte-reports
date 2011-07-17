package pl.net.bluesoft.rnd.apertereports.util;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.components.SimpleHorizontalLayout;
import pl.net.bluesoft.rnd.apertereports.exception.VriesException;

import static com.vaadin.ui.Window.Notification.*;

/**
 * @author MW
 */
public class NotificationUtil {
    public static void notImplementedYet(Window window) {
        window.showNotification(TM.get("exception.not.implemented.title"),
                TM.get("exception.not.implemented.description"), TYPE_WARNING_MESSAGE);
    }

    public static void showValidationErrors(Window window, String message) {
        window.showNotification(TM.get("notification.validation.errors.title"), "<br/>" + message, TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix) {
        window.showNotification(TM.get(prefix + ".title"), "<br/>" + TM.get(prefix + ".description"), TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix, Exception e) {
        window.showNotification(TM.get(prefix + ".title"),
                "<br/>" + TM.get(prefix + ".description") + ": " + e.getLocalizedMessage(), TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix, Object... details) {
        window.showNotification(TM.get(prefix + ".title"), "<br/>" + TM.get(prefix + ".description", details),
                TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String title, String message) {
        window.showNotification(title, "<br/>" + message, TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, VriesException exception) {
        showExceptionNotification(window, exception.getTitle(), "<br/>" + exception.getLocalizedMessage());
    }

    public static void validationErrors(Window window) {
        window.showNotification(TM.get("exception.validation.errors.title"), "<br/>" +
                TM.get("exception.validation.errors.description"), TYPE_WARNING_MESSAGE);
    }

    public static void validationErrors(Window window, String message) {
        window.showNotification(TM.get("exception.validation.errors.title"), "<br/><b>" + message + "</b>",
                TYPE_ERROR_MESSAGE);
    }

    public static void showSavedNotification(Window window) {
        showConfirmNotification(window, "notification.data.saved");
    }

    public static void showCancelledNotification(Window window) {
        showConfirmNotification(window, "notification.data.cancelled");
    }

    public static void showConfirmNotification(Window window, String prefix) {
        Notification notification = new Notification(TM.get(prefix + ".title"), "<br/><b>" +
                TM.get(prefix + ".desc") + "</b>", TYPE_HUMANIZED_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setDelayMsec(2000);
        window.showNotification(notification);
    }

    public static void showValuesChangedWindow(final Window parent, final ConfirmListener listener) {
        showConfirmWindow(parent, TM.get("report.table.valuesChanged.title"), TM.get("report.table.valuesChanged.content"), listener);
    }

    public static void showConfirmWindow(final Window parent, String windowTitle, String message, final ConfirmListener listener) {
        final Window window = new Window(windowTitle);
        window.setModal(true);
        window.setWidth(300, Sizeable.UNITS_PIXELS);
        window.setResizable(false);

        Button confirmButton = new Button(TM.get("report.table.confirm"));
        confirmButton.setImmediate(true);
        confirmButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listener.onConfirm();
                parent.removeWindow(window);
            }
        });

        Button cancelButton = new Button(TM.get("report.table.notconfirm"));
        cancelButton.setImmediate(true);
        cancelButton.addListener(new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                listener.onCancel();
                parent.removeWindow(window);
            }
        });

        VerticalLayout mainLayout = new VerticalLayout();
        mainLayout.setSpacing(true);
        mainLayout.addComponent(new Label(message, Label.CONTENT_XHTML));
        mainLayout.addComponent(new SimpleHorizontalLayout(confirmButton, cancelButton));

        window.setContent(mainLayout);
        parent.addWindow(window);
    }

    public static interface ConfirmListener {
        void onConfirm();

        void onCancel();
    }
}
