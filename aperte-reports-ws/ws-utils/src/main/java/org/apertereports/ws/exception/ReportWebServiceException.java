package org.apertereports.ws.exception;

import org.apache.commons.lang.StringUtils;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARException;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.SERVER)
public class ReportWebServiceException extends Exception {

    private ErrorCode errorCode;
    private String errorDesc;

    public ReportWebServiceException(ARException e) {
        super(e.getMessage(), e.getCause());
        errorCode = e.getErrorCode();
        errorDesc = StringUtils.join(e.getErrorDetails(), ", ");
    }

    public ReportWebServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportWebServiceException(ErrorCode errorCode, String message, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportWebServiceException(Throwable cause) {
        super(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

}
