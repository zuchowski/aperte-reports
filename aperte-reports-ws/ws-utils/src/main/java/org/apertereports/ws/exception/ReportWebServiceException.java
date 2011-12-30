package org.apertereports.ws.exception;

import org.apache.commons.lang.StringUtils;
import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.exception.AperteReportsException;
import org.springframework.ws.soap.server.endpoint.annotation.FaultCode;
import org.springframework.ws.soap.server.endpoint.annotation.SoapFault;

@SoapFault(faultCode = FaultCode.SERVER)
public class ReportWebServiceException extends Exception {

    private ErrorCodes errorCode;
    private String errorDesc;

    public ReportWebServiceException(AperteReportsException e) {
        super(e.getMessage(), e.getCause());
        errorCode = e.getErrorCode();
        errorDesc = StringUtils.join(e.getErrorDetails(), ", ");
    }

    public ReportWebServiceException(ErrorCodes errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportWebServiceException(ErrorCodes errorCode, String message, Throwable e) {
        super(message, e);
        this.errorCode = errorCode;
        errorDesc = message;
    }

    public ReportWebServiceException(Throwable cause) {
        super(cause);
    }

    public ErrorCodes getErrorCode() {
        return errorCode;
    }

    public String getErrorDesc() {
        return errorDesc;
    }

}
