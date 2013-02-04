package org.apertereports.common.exception;

import org.apertereports.common.ARConstants.ErrorCode;

/**
 * Universal checked exception, indicating errors that must be handled directly.
 */
public class ARException extends Exception {

    private ErrorCode errorCode = ErrorCode.UNKNOWN;
    private String[] errorDetails = {};

    /**
     * Creates AR exception
     *
     * @param errorCode Error code
     */
    public ARException(ErrorCode errorCode) {
        init(errorCode, null, (String[]) null);
    }

    /**
     * Creates AR exception
     *
     * @param errorCode Error code
     * @param details Details
     */
    public ARException(ErrorCode errorCode, String... details) {
        init(errorCode, null, details);
    }

    /**
     * Creates AR exception
     *
     * @param errorCode Error code
     * @param cause Cause
     */
    public ARException(ErrorCode errorCode, Throwable cause) {
        init(errorCode, cause, (String[]) null);
    }

    /**
     * Creates AR exception
     *
     * @param cause Cause
     */
    public ARException(Throwable cause) {
        init(null, cause, (String[]) null);
    }

    private void init(ErrorCode code, Throwable cause, String... details) {
        this.errorCode = code;
        if (cause != null) {
            initCause(cause);
        }
        if (details != null) {
            this.errorDetails = details;
        }
    }

    /**
     * Returns error code. If the code was not set by the constructor, then {@link ErrorCode#UNKNOWN}
     * is returned
     *
     * @return Error code
     */
    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Returns error details
     *
     * @return Error details
     */
    public String[] getErrorDetails() {
        return errorDetails;
    }
}
