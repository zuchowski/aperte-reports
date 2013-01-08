package org.apertereports.ui;

import com.vaadin.ui.Label;
import org.apertereports.util.VaadinUtil;

/**
 * Class defines simple handler for error messages presented on label component
 *
 * @author Tomasz Serafin, BlueSoft sp. z o. o.
 */
public class ErrorLabelHandler {

    private final Label label;

    /**
     * Creates handler
     *
     * @param label Label on which error messages are presented
     */
    public ErrorLabelHandler(Label label) {
        this.label = label;
        this.label.setVisible(false);
        this.label.setContentMode(Label.CONTENT_XHTML);
    }

    /**
     * Sets error message and causes the label to be visible
     *
     * @param msgId Error message id or message. The message shouldn't contain
     * any HTML tags or HTML special characters
     */
    public void setMessage(String msgId) {
        label.setContentMode(Label.CONTENT_XHTML);
        String msg = VaadinUtil.getValue(msgId);
        label.setValue("<div style=\"color:red;font-weight:bold\">" + msg + "</div>");
        label.setVisible(true);
    }

    /**
     * Clears error message and causes the label to be invisible
     */
    public void clearMessage() {
        label.setVisible(false);
        label.setValue("");
    }
}
