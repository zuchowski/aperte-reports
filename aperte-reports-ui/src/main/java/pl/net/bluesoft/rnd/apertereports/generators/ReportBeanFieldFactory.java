package pl.net.bluesoft.rnd.apertereports.generators;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.Validator;
import com.vaadin.ui.*;
import pl.net.bluesoft.rnd.apertereports.domain.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.domain.model.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.util.VaadinUtil;

import java.util.List;

/**
 * A non-default Vaadin field factory (extends {@link DefaultFieldFactory}).
 * It's main purpose is to generate fields for a {@link ReportTemplate} instance.
 * It can also handle a value change of every generated field. This is due to non-commit-wise
 * of the forms we use.
 */
public class ReportBeanFieldFactory extends DefaultFieldFactory {
    public static abstract class FieldChangeNotifier {
        public abstract void fieldValueChanged(Item item, Object propertyId, Field field);
    }

    /**
     * A notifier for handling field value changes.
     */
    private FieldChangeNotifier notifier;
    /**
     * Current bean.
     */
    private ReportTemplate report;
    /**
     * Visible bean properties.
     */
    private Object[] visibleFields;

    public ReportBeanFieldFactory(FieldChangeNotifier notifier, Object[] visibleFields) {
        this.visibleFields = visibleFields;
        this.notifier = notifier;
    }

    public ReportBeanFieldFactory(Object[] visibleFields) {
        this.visibleFields = visibleFields;
    }

    /**
     * Tests whether a field should be visible.
     *
     * @param propertyId A property id
     * @return <code>TRUE</code> when the field should be visible
     */
    private boolean showField(Object propertyId) {
        for (Object field : visibleFields) {
            if (field.toString().equals(propertyId.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Sets current bean.
     *
     * @param report The report
     */
    public void setReport(ReportTemplate report) {
        this.report = report;
    }

    /**
     * Creates fields from a given bean.
     *
     * @see DefaultFieldFactory
     * @param item Bean item
     * @param propertyId Property id
     * @param uiContext Container
     * @return A field
     */
    @Override
    public Field createField(final Item item, final Object propertyId, Component uiContext) {
        Field field = null;
        if (showField(propertyId)) {
            if ("active".equals(propertyId)) {
                field = new CheckBox(VaadinUtil.getValue("manager.form.active.label"));
            }
            else if ("allowOnlineDisplay".equals(propertyId)) {
                field = new CheckBox(VaadinUtil.getValue("manager.form.allowOnlineDisplay.label"));
            }
            else if ("allowBackgroundOrder".equals(propertyId)) {
                field = new CheckBox(VaadinUtil.getValue("manager.form.allowBackgroundOrder.label"));
            }
            else if ("filename".equals(propertyId)) {
                TextField filenameField = new TextField("filename");
                filenameField.setCaption(VaadinUtil.getValue("manager.form.filename.label"));
                filenameField.setWidth("100%");
                filenameField.setReadOnly(true);
                field = filenameField;
            }
            else if ("reportname".equals(propertyId)) {
                TextField nameField = new TextField("reportname");
                nameField.setCaption(VaadinUtil.getValue("manager.form.reportname.label"));
                nameField.setWidth("100%");
                nameField.setReadOnly(true);
                nameField.addValidator(new Validator() {
                    @Override
                    public void validate(Object value) throws InvalidValueException {
                        if (!isValid(value)) {
                            if (value == null || !(value instanceof String) || ((String) value).length() == 0) {
                                throw new InvalidValueException(VaadinUtil.getValue("notification.validation.no.report.name"));
                            }
                            else {
                                throw new InvalidValueException(VaadinUtil.getValue("notification.validation.duplicate.report.name"));
                            }
                        }
                    }

                    @Override
                    public boolean isValid(Object value) {
                        if (value == null || !(value instanceof String) || ((String) value).length() == 0) {
                            return false;
                        }
                        if (report == null) {
                            return true;
                        }
                        List<ReportTemplate> reports = ReportTemplateDAO.fetchReportsByName((String) value);
                        if (reports != null && !reports.isEmpty()) {
                            ReportTemplate rt = reports.get(0);
                            return rt.getId().equals(report.getId());
                        }
                        return true;
                    }
                });
                field = nameField;
            }
            else if ("description".equals(propertyId)) {
                TextField descriptionField = new TextField("description");
                descriptionField.setRequiredError(VaadinUtil.getValue("manager.form.active.required_error"));
                descriptionField.setRequired(true);
                descriptionField.setCaption(VaadinUtil.getValue("manager.form.description.label"));
                descriptionField.setWidth("100%");
                field = descriptionField;
            }
            if (field != null) {
                if (field instanceof AbstractComponent) {
                    AbstractComponent comp = (AbstractComponent) field;
                    comp.setImmediate(true);
                }
                field.setPropertyDataSource(item.getItemProperty(propertyId));
                final Field finalField = field;
                field.addListener(new ValueChangeListener() {
                    @Override
                    public void valueChange(ValueChangeEvent event) {
                        if (notifier != null) {
                            notifier.fieldValueChanged(item, propertyId, finalField);
                        }
                    }
                });
            }
        }
        return field;
    }
}
