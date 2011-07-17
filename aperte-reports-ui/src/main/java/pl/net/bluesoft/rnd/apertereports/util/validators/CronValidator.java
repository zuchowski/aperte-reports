package pl.net.bluesoft.rnd.apertereports.util.validators;

import com.vaadin.data.validator.AbstractStringValidator;
import org.quartz.CronExpression;

/**
 * A cron expression validator to use in Vaadin components.
 */
public class CronValidator extends AbstractStringValidator {

    /**
     * Constructs a validator for strings.
     * <p/>
     * <p>
     * Null and empty string values are always accepted. To reject empty values,
     * set the field being validated as required.
     * </p>
     *
     * @param errorMessage the message to be included in an {@link com.vaadin.data.Validator.InvalidValueException}
     *                     (with "{0}" replaced by the value that failed validation).
     */
    public CronValidator(String errorMessage) {
        super(errorMessage);
    }

    @Override
    protected boolean isValidString(String value) {
        return CronExpression.isValidExpression(value);
    }
}
