/**
 *
 */
package pl.net.bluesoft.rnd.vries.exception;

import pl.net.bluesoft.rnd.vries.util.Constants.ErrorCodes;

/**
 * A checked exception thrown when something goes wrong in a report generation process.
 */
public class ReportException extends Exception {

    private ErrorCodes errorCode;
    private String errorDesc;

    public ReportException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportException(ErrorCodes errorCode, String message, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportException(Throwable cause) {
        super(cause);
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

}
