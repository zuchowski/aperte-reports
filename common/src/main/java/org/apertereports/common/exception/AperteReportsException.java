package org.apertereports.common.exception;

import org.apertereports.common.ReportConstants.ErrorCodes;

/**
 * Universal checked exception, indicating errors that must be handled directly.
 */
public class AperteReportsException extends Exception {
    private ErrorCodes errorCode = ErrorCodes.TECHNICAL_ERROR;
    private String[] errorDetails = new String[]{};
   
	public AperteReportsException(ErrorCodes errorCode) {
		this.errorCode = errorCode;
	}
	
	
	public AperteReportsException(ErrorCodes errorCode, String[] errorDetails) {
		this(errorCode);
		this.errorDetails = errorDetails;
	}

	public AperteReportsException(ErrorCodes errorCode, Throwable cause) {
		super(cause);
		this.errorCode = errorCode;
	}
	
	
	
	public AperteReportsException(ErrorCodes errorCode, String[] errorDetails, Throwable cause) {
		super(cause);
		this.errorCode = errorCode; 
		this.errorDetails = errorDetails;
	}
	
	public AperteReportsException(Throwable cause) {
		super(cause);
	}


	public AperteReportsException(ErrorCodes errorCode, String detail) {
		this(errorCode, new String[]{detail});
	}


	public ErrorCodes getErrorCode() {
		return errorCode;
	}
	public String[] getErrorDetails() {
		return errorDetails;
	}



    

}
