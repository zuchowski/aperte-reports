package org.apertereports.ui;

import com.vaadin.data.Item;
import com.vaadin.event.FieldEvents.TextChangeListener;
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
    static final FAction[] EMPTY_ACTION_TABLE = {};

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
         * Align to left. Available only for {@link AbstractOrderedLayout}
         * parent component
         */
        ALIGN_LEFT,
        /**
         * Align to center. Available only for {@link AbstractOrderedLayout}
         * parent component
         */
        ALIGN_CENTER,
        /**
         * Align to right. Available only for {@link AbstractOrderedLayout}
         * parent component
         */
        ALIGN_RIGTH,
        /**
         * Set expand ration to 1.0. Available only for {@link AbstractOrderedLayout}
         * parent component
         */
        SET_EXPAND_RATIO_1_0,
        /**
         * Set full width action
         */
        SET_FULL_WIDTH,
        /**
         * Set the component invisible
         */
        SET_INVISIBLE,
        /**
         * Set spacing action. Available only for {@link AbstractOrderedLayout}
         * components
         */
        SET_SPACING
    }

    /**
     * Creates label bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @return Label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent) {
        return createLabel(item, propertyId, parent, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates label bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @param actions List of actions performed on created component
     * @return Label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent, FAction... actions) {
        return createLabel(item, propertyId, parent, null, actions);
    }

    /**
     * Creates label bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @param style Style name
     * @return Label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent, String style) {
        return createLabel(item, propertyId, parent, style, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates label bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the label is added, can be null
     * @param style Style name
     * @param actions List of actions performed on created component
     * @return Label
     */
    public static Label createLabel(Item item, String propertyId, ComponentContainer parent, String style,
            FAction... actions) {

        Label label = new Label(item.getItemProperty(propertyId));
        if (style != null && !style.isEmpty()) {
            label.setStyleName(style);
        }
        if (parent != null) {
            parent.addComponent(label);
        }
        label.setWidth(null);
        performActions(label, actions);
        return label;
    }

    /**
     * Creates label
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the label is added, can be null
     * @return Label
     */
    public static Label createLabel(String captionId, ComponentContainer parent) {
        return createLabel(captionId, parent, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates label
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @return Label
     */
    public static Label createLabel(String captionId, ComponentContainer parent, String style) {
        return createLabel(captionId, parent, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates label
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the label is added, can be null
     * @param actions List of actions performed on created component label
     * @return Label
     */
    public static Label createLabel(String captionId, ComponentContainer parent, String style, FAction... actions) {
        Label label = new Label(VaadinUtil.getValue(captionId));
        if (style != null && !style.isEmpty()) {
            label.setStyleName(style);
        }
        if (parent != null) {
            parent.addComponent(label);
        }
        label.setWidth(null);
        performActions(label, actions);
        return label;
    }

    /**
     * Creates button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent) {
        return createButton(captionId, parent, null, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Create button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @param style Style name
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent, String style) {
        return createButton(captionId, parent, style, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent, ClickListener listener) {
        return createButton(captionId, parent, null, listener, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @param actions List of actions performed on created component
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent, ClickListener listener, FAction... actions) {
        return createButton(captionId, parent, null, listener, actions);
    }

    /**
     * Creates button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent, String style,
            ClickListener listener) {
        return createButton(captionId, parent, style, listener, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates button
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param style Style name
     * @param parent Parent container to which the button is added, can be null
     * @param listener Event listener
     * @param actions List of actions performed on created component
     * @return Button
     */
    public static Button createButton(String captionId, ComponentContainer parent, String style,
            ClickListener listener, FAction... actions) {

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
     * Creates check box
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param item Bound Object
     * @param propertyId Property id
     * @param parent Parent container to which the component is added, can be
     * null
     * @return Check box
     */
    public static CheckBox createCheckBox(String captionId, Item item, String propertyId, ComponentContainer parent) {
        CheckBox checkBox = new CheckBox(VaadinUtil.getValue(captionId), item.getItemProperty(propertyId));
        if (parent != null) {
            parent.addComponent(checkBox);
        }
        return checkBox;
    }

    /**
     * Creates check box
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @param parent Parent container to which the component is added, can be
     * null
     * @return Check box
     */
    public static CheckBox createCheckBox(String captionId, ComponentContainer parent) {
        CheckBox checkBox = new CheckBox(VaadinUtil.getValue(captionId));
        if (parent != null) {
            parent.addComponent(checkBox);
        }
        return checkBox;
    }

    /**
     * Creates text field bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param parent Parent container to which the component is added, can be
     * null
     * @return Text field
     */
    public static TextField createTextField(Item item, String propertyId, ComponentContainer parent) {
        return createTextField(item, propertyId, parent, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates text field bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param promptId Id of the prompt taken from the localized resources or
     * prompt
     * @param parent Parent container to which the component is added, can be
     * null
     * @return Text field
     */
    public static TextField createTextField(Item item, String propertyId, ComponentContainer parent, String promptId) {
        return createTextField(item, propertyId, parent, promptId, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates text field bound to property
     *
     * @param item Bound object
     * @param propertyId Property id
     * @param promptId Id of the prompt taken from the localized resources or
     * prompt
     * @param parent Parent container to which the component is added, can be
     * null
     * @param actions List of actions performed on created component
     * @return Text field
     */
    public static TextField createTextField(Item item, String propertyId, ComponentContainer parent, String promptId, FAction... actions) {
        TextField tf = new TextField(item.getItemProperty(propertyId));
        if (parent != null) {
            parent.addComponent(tf);
        }
        if (promptId != null) {
            tf.setInputPrompt(VaadinUtil.getValue(promptId));
        }
        performActions(tf, actions);
        return tf;
    }

    /**
     * Creates text field with listener
     *
     * @param promptId Id of the prompt taken from the localized resources or
     * prompt
     * @param parent Parent container to which the component is added, can be
     * null
     * @param listener Event listener
     * @return
     */
    public static TextField createSearchBox(String promptId, ComponentContainer parent, TextChangeListener listener) {
        TextField tf = new TextField();
        tf.setInputPrompt(VaadinUtil.getValue(promptId));
        tf.setImmediate(true);
        tf.setTextChangeTimeout(500);
        tf.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.LAZY);
        tf.addListener(listener);
        if (parent != null) {
            parent.addComponent(tf);
        }
        return tf;
    }

    /**
     * Creates horizontal layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @return horizontal layout
     */
    public static HorizontalLayout createHLayout(ComponentContainer parent) {
        return (HorizontalLayout) createLayout(FLayout.HORIZONTAL, parent, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates horizontal layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @param actions List of actions performed on created component
     * @return Horizontal layout
     */
    public static HorizontalLayout createHLayout(ComponentContainer parent, FAction... actions) {
        return (HorizontalLayout) createLayout(FLayout.HORIZONTAL, parent, actions);
    }

    /**
     * Creates vertical layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @return Vertical layout
     */
    public static VerticalLayout createVLayout(ComponentContainer parent) {
        return (VerticalLayout) createLayout(FLayout.VERTICAL, parent, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates vertical layout
     *
     * @param parent Parent container to which the layout is added, can be null
     * @param actions List of actions performed on created component
     * @return Vertical layout
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

    /**
     * Creates panel
     *
     * @param captionId Id of the caption taken from the localized resources or
     * caption
     * @return Panel
     */
    public static Panel createPanel(String captionId) {
        return new Panel(VaadinUtil.getValue(captionId));
    }

    /**
     * Creates simple label without caption and add it to the parent container
     *
     * @param parent Parent container to which the spacer is added, can be null
     * @return Spacer
     */
    public static Label createSpacer(ComponentContainer parent) {
        return createSpacer(parent, null, null, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates simple label without caption and add it to the parent container
     *
     * @param parent Parent container to which the spacer is added, can be null
     * @param actions List of actions performed on created component
     * @return Spacer
     */
    public static Label createSpacer(ComponentContainer parent, FAction... actions) {
        return createSpacer(parent, null, null, actions);
    }

    /**
     * Creates simple label without caption and add it to the parent container
     *
     * @param parent Parent container to which the spacer is added, can be null
     * @param width Width of the component, can be null
     * @param height Height of the component, can be null
     * @return Spacer
     */
    public static Label createSpacer(ComponentContainer parent, String width, String height) {
        return createSpacer(parent, width, height, EMPTY_ACTION_TABLE);
    }

    /**
     * Creates simple label without caption and add it to the parent container
     *
     * @param parent Parent container to which the spacer is added, can be null
     * @param width Width of the component, can be null
     * @param height Height of the component, can be null
     * @param actions List of actions performed on created component
     * @return Spacer
     */
    public static Label createSpacer(ComponentContainer parent, String width, String height, FAction... actions) {
        Label spacer = new Label();
        if (width != null) {
            spacer.setWidth(width);
        }
        if (height != null) {
            spacer.setHeight(height);
        }
        parent.addComponent(spacer);
        performActions(spacer, actions);
        return spacer;
    }

    static void performActions(Component c, FAction[] actions) {
        for (FAction action : actions) {
            if (action == FAction.ALIGN_LEFT || action == FAction.ALIGN_CENTER || action == FAction.ALIGN_RIGTH
                    || action == FAction.SET_EXPAND_RATIO_1_0) {
                Component parent = c.getParent();
                if (parent == null) {
                    throw new RuntimeException("there is no parent component");
                }
                if (!(parent instanceof AbstractOrderedLayout)) {
                    throw new RuntimeException("component is not instance of AbstractOrderedLayout");
                }

                AbstractOrderedLayout layout = ((AbstractOrderedLayout) parent);

                if (action == FAction.SET_EXPAND_RATIO_1_0) {
                    layout.setExpandRatio(c, 1.0f);
                } else {
                    Alignment a = action == FAction.ALIGN_LEFT ? Alignment.MIDDLE_LEFT : action == FAction.ALIGN_RIGTH
                            ? Alignment.MIDDLE_RIGHT : Alignment.MIDDLE_CENTER;
                    layout.setComponentAlignment(c, a);
                }
            } else if (action == FAction.SET_FULL_WIDTH) {
                c.setWidth("100%");
            } else if (action == FAction.SET_INVISIBLE) {
                c.setVisible(false);
            } else if (action == FAction.SET_SPACING) {
                if (!(c instanceof AbstractOrderedLayout)) {
                    throw new RuntimeException("component is not instance of AbstractOrderedLayout");
                }
                ((AbstractOrderedLayout) c).setSpacing(true);
            } else {
                throw new RuntimeException("action not serviced yet: " + action);
            }
        }
    }
}
