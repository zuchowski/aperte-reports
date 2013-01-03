package org.apertereports.ui;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickListener;
import org.apertereports.util.VaadinUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class provides useful methods for creating UI components
 *
 * @author Tomasz Serafin, Bluesoft sp. z o. o.
 */
public abstract class UiFactory {

    private static final Logger logger = LoggerFactory.getLogger(UiFactory.class);

    /**
     * Defines available layouts
     */
    private enum FLayout {

        /**
         * Horizontal layout
         */
        HORIZONTAL,
        /**
         * Vertical layout
         */
        VERTICAL
    }

    /**
     * Defines available actions performed on created components
     */
    public enum FAction {

        /**
         * Set full width action
         */
        SET_FULL_WIDTH,
        /**
         * Set spacing action. Available only for {@link AbstractOrderedLayout}
         * components
         */
        SET_SPACING
    }

    /**
     * Creates label bound to property,
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @return The label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent) {
        return createLabel(item, propertyId, parent, propertyId);
    }

    /**
     * Creates label bound to property,
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @param style Style name
     * @return The label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent, String style) {
        Property property = item.getItemProperty(propertyId);
        return createLabel(property, parent, style);
    }

    /**
     * Creates label bound to property,
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @param style Style name
     * @param actions List of actions performed on created component
     * @return The label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent, String style, FAction... actions) {
        Property property = item.getItemProperty(propertyId);
        return createLabel(property, parent, style, actions);
    }

    /**
     * Label displaying not bound value.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the label is added, can be null
     * @return The label
     */
    public static Label createLabel(String captionId, ComponentContainer parent) {
        return createLabel(captionId, parent, null, new FAction[]{});
    }

    /**
     * Label displaying not bound value.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @return The label
     */
    public static Label createLabel(String captionId, ComponentContainer parent, String style) {
        return createLabel(captionId, parent, null, new FAction[]{});
    }

    /**
     * Label displaying not bound value.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @param actions List of actions performed on created component label
     * @return The label
     */
    public static Label createLabel(String captionId, ComponentContainer parent, String style, FAction... actions) {
        Label label = new Label(VaadinUtil.getValue(captionId));
        if (style != null && !style.isEmpty()) {
            label.setStyleName(style);
        }
        if (parent != null) {
            parent.addComponent(label);
        }
        //todots
        label.setWidth(null);
        performActions(label, actions);
        return label;
    }

    private static Label createLabel(Property property, ComponentContainer parent, String style, FAction... actions) {
        Label label = new Label(property);
        label.setStyleName(style);
        if (parent != null) {
            parent.addComponent(label);
        }
        //todots?
        label.setWidth(null);
        performActions(label, actions);
        return label;
    }

    /**
     * Creates button with caption code (for localization), registers listener
     * and adds it to parent.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @return The button
     */
    public static Button createButton(String captionId, ComponentContainer parent) {
        return createButton(captionId, parent, null, null);
    }

    /**
     * Creates button with caption code (for localization), registers listener
     * and adds it to parent.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @param style Style name
     * @return The button
     */
    public static Button createButton(String captionId, ComponentContainer parent, String style) {
        return createButton(captionId, parent, style, null);
    }

    /**
     * Creates button with caption code (for localization), registers listener
     * and adds it to parent.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @return The button
     */
    public static Button createButton(String captionId, ComponentContainer parent, ClickListener listener) {

        return createButton(captionId, parent, null, listener);
    }

    /**
     * Creates button with caption code (for localization), registers listener
     * and adds it to parent.
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @return The button
     */
    public static Button createButton(String captionId, ComponentContainer parent, String style,
            ClickListener listener) {

        Button button = new Button(VaadinUtil.getValue(captionId));
        if (style != null && !style.isEmpty()) {
            button.setStyleName(style);
        }
        if (parent != null) {
            parent.addComponent(button);
        }
        if (listener != null) {
            button.addListener(listener);
        }
        return button;
    }

    /**
     * Creates horizontal layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @return The horizontal layout
     */
    public static HorizontalLayout createHLayout(ComponentContainer parent) {
        return (HorizontalLayout) createLayout(FLayout.HORIZONTAL, parent, new FAction[]{});
    }

    /**
     * Creates horizontal layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @param actions List of actions performed on created component
     * @return The horizontal layout
     */
    public static HorizontalLayout createHLayout(ComponentContainer parent, FAction... actions) {
        return (HorizontalLayout) createLayout(FLayout.HORIZONTAL, parent, actions);
    }

    /**
     * Creates vertical layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @return The vertical layout
     */
    public static VerticalLayout createVLayout(ComponentContainer parent) {
        return (VerticalLayout) createLayout(FLayout.VERTICAL, parent, new FAction[]{});
    }

    /**
     * Creates vertical layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @param actions List of actions performed on created component
     * @return The horizontal layout
     */
    public static VerticalLayout createVLayout(ComponentContainer parent, FAction... actions) {
        return (VerticalLayout) createLayout(FLayout.VERTICAL, parent, actions);
    }

    private static AbstractOrderedLayout createLayout(FLayout type, ComponentContainer parent, FAction... actions) {
        AbstractOrderedLayout layout = type == FLayout.HORIZONTAL ? new HorizontalLayout() : new VerticalLayout();
        if (parent != null) {
            parent.addComponent(layout);
        }
        performActions(layout, actions);
        return layout;
    }
    
    //todots createPanel(captionId);

    /**
     * Creates simple label without caption and add it to the parent container
     *
     * @param parent Parent container to which the spacer is added, can be null
     * @return The spacer
     */
    public static Label createSpacer(ComponentContainer parent) {
        Label spacer = new Label();
        parent.addComponent(spacer);
        return spacer;
    }

    private static void performActions(Component c, FAction[] actions) {
        for (FAction action : actions) {
            if (action == FAction.SET_FULL_WIDTH) {
                c.setWidth("100%");
            } else if (action == FAction.SET_SPACING) {
                if (c instanceof AbstractOrderedLayout) {
                    ((AbstractOrderedLayout) c).setSpacing(true);
                } else {
                    logger.error("component is not instance of AbstractOrderedLayout");

                }
            } else {
                throw new RuntimeException("action not serviced yet: " + FAction.SET_FULL_WIDTH);
            }
        }
    }
}
