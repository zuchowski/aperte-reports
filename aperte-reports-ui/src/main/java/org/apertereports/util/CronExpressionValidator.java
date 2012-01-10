package org.apertereports.util;

import org.quartz.CronExpression;

import com.vaadin.data.Validator;

public class CronExpressionValidator implements Validator {

	private String message;

	public CronExpressionValidator(String message) {
		this.message = message;
	}

	@Override
	public void validate(Object value) throws InvalidValueException {
		if(!isValid(value)) {
			throw new InvalidValueException(message);
		}

	}

	@Override
	public boolean isValid(Object value) {
		return CronExpression.isValidExpression((String) value);
	}

}
