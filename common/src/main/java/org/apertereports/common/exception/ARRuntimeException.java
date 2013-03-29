package org.apertereports.common.exception;

import org.apertereports.common.ARConstants.ErrorCode;

/**
 * Unchecked exception used to propagate errors and handle them in higher level
 * layers.
 */
public class ARRuntimeException extends RuntimeException {

    private ErrorCode errorCode = ErrorCode.UNKNOWN;
    private String[] errorDetails = {};

    /**
     * Creates new AR runtime exception
     *
     * @param errorCode Error code
     */
    public ARRuntimeException(ErrorCode errorCode) {
        init(errorCode, null, (String[]) null);
    }

    /**
     * Creates new AR runtime exception
     *
     * @param errorCode Error code
     * @param details Error details
     */
    public ARRuntimeException(ErrorCode errorCode, String... details) {
        init(errorCode, null, details);
    }

    /**
     * Creates new AR runtime exception
     *
     * @param errorCode Error code
     * @param cause Cause
     * @param details Error details
     */
    public ARRuntimeException(ErrorCode errorCode, Throwable cause, String... details) {
        init(errorCode, null, details);
    }

    /**
     * Creates new AR runtime exception
     *
     * @param errorCode Error code
     * @param cause Cause
     */
    public ARRuntimeException(ErrorCode errorCode, Throwable cause) {
        init(errorCode, cause, (String[]) null);
    }

    /**
     * Wraps checked exception (business error).
     *
     * @param e Exception
     */
    public ARRuntimeException(ARException e) {
        init(e.getErrorCode(), e, e.getErrorDetails());

    }

    /**
     * Wraps unknown error.
     *
     * @param e Exception
     */
    public ARRuntimeException(Exception e) {
        init(null, e, (String[]) null);
    }

    private void init(ErrorCode code, Throwable cause, String... details) {
        if (code != null) {
            this.errorCode = code;
        }
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
