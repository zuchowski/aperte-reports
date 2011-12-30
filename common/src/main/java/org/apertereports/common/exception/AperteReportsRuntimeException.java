package org.apertereports.common.exception;

import org.apertereports.common.ReportConstants.ErrorCodes;

/**
 * Unchecked exception used to propagate errors and handle them in higher level
 * layers.
 */
public class AperteReportsRuntimeException extends RuntimeException {
	private ErrorCodes errorCode = ErrorCodes.TECHNICAL_ERROR;
	private String[] errorDetails = {};

	public AperteReportsRuntimeException(ErrorCodes errorCode) {
		this.errorCode = errorCode;
	}

	public AperteReportsRuntimeException(String message, ErrorCodes errorCode) {
		this.errorCode = errorCode;
		this.errorDetails = new String[] { message };
	}

	public AperteReportsRuntimeException(String message, ErrorCodes errorCode, Throwable cause) {
		this.errorDetails = new String[] { message };
		this.errorCode = errorCode;
	}

	public AperteReportsRuntimeException(ErrorCodes errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}

	/**
	 * Wraps checked exception (business error).
	 * 
	 * @param e
	 */
	public AperteReportsRuntimeException(AperteReportsException e) {
		super(e);
		this.errorCode = e.getErrorCode();
		this.errorDetails = e.getErrorDetails();

	}

	/**
	 * Wraps technical error.
	 * 
	 * @param e
	 */
	public AperteReportsRuntimeException(Exception e) {
		if (e instanceof AperteReportsException) {
			AperteReportsException are = (AperteReportsException) e;
			initCause(are);
		} else
			initCause(e);
	}

	public ErrorCodes getErrorCode() {
		return errorCode;
	}

	public String[] getErrorDetails() {
		return errorDetails;
	}

	public String getLocalizationPrefix() {
		return "exception." + errorCode.name().toLowerCase();
	}

}
