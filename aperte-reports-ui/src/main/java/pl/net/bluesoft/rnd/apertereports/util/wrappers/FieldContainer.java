package pl.net.bluesoft.rnd.apertereports.util.wrappers;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Validator;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.ui.*;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.util.Constants;

import java.util.List;

/**
 * This is a wrapper container for report fields. It includes a Vaadin component and a bunch of configuration params.
 * <p/>Each of the containers has a certain type of {@link Constants.InputTypes}, a name, order in the form and a flag
 * that decides whether to show a "select all" checkbox or not.
 */
public class FieldContainer {
    private Integer order;
    private String name;
    private Component fieldComponent;
    private boolean selectAll;
    private Constants.InputTypes componentType;

    public FieldContainer() {
    }

    public FieldContainer(Integer order, String name, Field field) {
        super();
        this.order = order;
        this.name = name;
        fieldComponent = field;
    }

    /**
     * Adds a validator to the current field component.
     *
     * @param validator A Vaadin validator
     */
    public void addValidator(Validator validator) {
        if (fieldComponent != null) {
            if (fieldComponent instanceof Field) {
                ((Field) fieldComponent).addValidator(validator);
            }
            else if (fieldComponent instanceof FilterContainer) {
                ((FilterContainer) fieldComponent).addValidator(validator);
            }
        }
    }

    public Constants.InputTypes getComponentType() {
        return componentType;
    }

    public Component getFieldComponent() {
        return fieldComponent;
    }

    public String getName() {
        return name;
    }

    public Integer getOrder() {
        return order;
    }

    /**
     * Gets a value from current component.
     *
     * @return A value of component
     */
    public Object getValue() {
        Object value = null;
        if (fieldComponent == null) {
            return null;
        }
        if (fieldComponent instanceof Field) {
            value = ((Field) fieldComponent).getValue();
        }
        else if (fieldComponent instanceof FilterContainer) {
            value = ((FilterContainer) fieldComponent).getValue();
        }
        return value;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    /**
     * Fills the form with current field component. Adds additional widgets if needed (i.e. "select all" box)
     *
     * @param form   The form to place the field in
     * @param layout The layout that displays the field
     */
    public void placeYourselfInForm(Form form, FormLayout layout) {
        if (fieldComponent == null) {
            return;
        }

        if (fieldComponent instanceof Field) {
            form.addField(name, (Field) fieldComponent);
        }
        else if (fieldComponent instanceof FilterContainer) {
            for (Select select : ((FilterContainer) fieldComponent).getLevels()) {
                form.addField(select.getCaption(), select);
            }
        }
        else {
            layout.addComponent(fieldComponent);
        }

        if (selectAll) {
            final CheckBox saCheckbox = new CheckBox(TM.get("invoker.form.select_all"));
            saCheckbox.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(ValueChangeEvent event) {
                    boolean selected = (Boolean) saCheckbox.getValue();
                    if (fieldComponent instanceof Select) {
                        for (Object itemId : ((Select) fieldComponent).getItemIds()) {
                            if (selected) {
                                ((Select) fieldComponent).select(itemId);
                            }
                            else {
                                ((Select) fieldComponent).unselect(itemId);
                            }
                        }
                    }
                    if (fieldComponent instanceof FilterContainer) {
                        List<Select> selectList = ((FilterContainer) fieldComponent).getLevels();
                        Select select = selectList.get(selectList.size() - 1);
                        for (Object itemId : select.getItemIds()) {
                            if (selected) {
                                select.select(itemId);
                            }
                            else {
                                select.unselect(itemId);
                            }
                        }
                    }
                }
            });
            form.addField(name + "_all", saCheckbox);
        }
    }

    public void setComponentType(Constants.InputTypes componentType) {
        this.componentType = componentType;
    }

    public void setFieldComponent(Component field) {
        fieldComponent = field;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void setSelectAll(boolean selectAll) {
        this.selectAll = selectAll;
    }

    /**
     * Sets the value of the current field component.
     *
     * @param value The value to set
     */
    public void setValue(Object value) {
        if (fieldComponent != null) {
            if (fieldComponent instanceof Field) {
                Field field = ((Field) fieldComponent);
                field.setValue(value);
                if (field instanceof Select) {
                    ((Select) fieldComponent).select(value);
                }
            }
            else if (fieldComponent instanceof FilterContainer) {
                ((FilterContainer) fieldComponent).setValue(value);
            }
        }
    }

    /**
     * Invokes the validation of the current field component.
     * Adds the error style on validation exception.
     */
    public void validate() {
        try {
            if (fieldComponent instanceof Field) {
                ((Field) fieldComponent).validate();
            }
            else if (fieldComponent instanceof FilterContainer) {
                ((FilterContainer) fieldComponent).validate();
            }
        }
        catch (InvalidValueException e) {
            if (fieldComponent instanceof Field) {
                ((Field) fieldComponent).addStyleName("error");
                ((Field) fieldComponent).removeStyleName("error");
            }
            else if (fieldComponent instanceof FilterContainer) {
                ((FilterContainer) fieldComponent).addStyleName("error");
                ((FilterContainer) fieldComponent).removeStyleName("error");
            }
        }
    }
}
