package org.apertereports.util;

import org.apertereports.components.SimpleHorizontalLayout;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;
import org.apertereports.common.exception.ARRuntimeException;

import static com.vaadin.ui.Window.Notification.*;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;

/**
 * @author MW
 */
public class NotificationUtil {

    public static void notImplementedYet(Window window) {
        window.showNotification(VaadinUtil.getValue("exception.not.implemented.title"),
                VaadinUtil.getValue("exception.not.implemented.description"), TYPE_WARNING_MESSAGE);
    }

    public static void showValidationErrors(Window window, String message) {
        window.showNotification(VaadinUtil.getValue("notification.validation.errors.title"), "<br/>" + message,
                TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix) {
        window.showNotification(VaadinUtil.getValue(prefix + ".title"),
                "<br/>" + VaadinUtil.getValue(prefix + ".description"), TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix, Exception e) {
        window.showNotification(VaadinUtil.getValue(prefix + ".title"),
                "<br/>" + VaadinUtil.getValue(prefix + ".description") + ": " + e.getLocalizedMessage(),
                TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String prefix, Object... details) {
        window.showNotification(VaadinUtil.getValue(prefix + ".title"),
                "<br/>" + VaadinUtil.getValue(prefix + ".description", details), TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, String title, String message) {
        window.showNotification(title, "<br/>" + message, TYPE_ERROR_MESSAGE);
    }

    public static void showExceptionNotification(Window window, ARRuntimeException exception) {
        String errorPrefix = "exception." + exception.getErrorCode().name().toLowerCase();
        String title = VaadinUtil.getValue(errorPrefix + ".title");
        String description = "<br/>"
                + VaadinUtil.getValue(errorPrefix + ".desc", exception.getErrorDetails());
        if (exception.getCause() != null && exception.getCause().getLocalizedMessage() != null) {
            description += "<br/>" + exception.getCause().getLocalizedMessage();
        }
        window.showNotification(title, description, TYPE_ERROR_MESSAGE);
    }

    public static void showErrorNotification(Window window, String messageId) {
        window.showNotification(VaadinUtil.getValue(messageId), TYPE_ERROR_MESSAGE);
    }

    public static void validationErrors(Window window) {
        window.showNotification(VaadinUtil.getValue("exception.validation.errors.title"),
                "<br/>" + VaadinUtil.getValue("exception.validation.errors.description"), TYPE_WARNING_MESSAGE);
    }

    public static void validationErrors(Window window, String message) {
        window.showNotification(VaadinUtil.getValue("exception.validation.errors.title"),
                "<br/><b>" + message + "</b>", TYPE_ERROR_MESSAGE);
    }

    public static void showSavedNotification(Window window) {
        showConfirmNotification(window, "notification.data.saved");
    }

    public static void showCancelledNotification(Window window) {
        showConfirmNotification(window, "notification.data.cancelled");
    }

    public static void showConfirmNotification(Window window, String prefix) {
        Notification notification = new Notification(VaadinUtil.getValue(prefix + ".title"), "<br/><b>"
                + VaadinUtil.getValue(prefix + ".desc") + "</b>", TYPE_HUMANIZED_MESSAGE);
        notification.setPosition(POSITION_CENTERED);
        notification.setDelayMsec(2000);
        window.showNotification(notification);
    }

    public static void showValuesChangedWindow(final Window parent, final ConfirmListener listener) {
        showConfirmWindow(parent, VaadinUtil.getValue("report.table.valuesChanged.title"),
                VaadinUtil.getValue("report.table.valuesChanged.content"), listener);
    }

    public static void showConfirmWindow(final Window parent, String windowTitle, String message,
            final ConfirmListener listener) {
        final Window window = new Window(windowTitle);
        window.setModal(true);
        window.setWidth(300, Sizeable.UNITS_PIXELS);
        window.setResizable(false);

        Button confirmButton = UiFactory.createButton(UiIds.LABEL_YES, null, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                listener.onConfirm();
                parent.removeWindow(window);
            }
        });
        confirmButton.setImmediate(true);

        Button cancelButton = UiFactory.createButton(UiIds.LABEL_NO, null, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                listener.onCancel();
                parent.removeWindow(window);
            }
        });
        cancelButton.setImmediate(true);

        VerticalLayout mainLayout = UiFactory.createVLayout(null, FAction.SET_SPACING);
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
