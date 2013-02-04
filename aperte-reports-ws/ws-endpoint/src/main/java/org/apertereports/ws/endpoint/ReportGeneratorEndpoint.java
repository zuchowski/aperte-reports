package org.apertereports.ws.endpoint;

import java.io.IOException;
import java.util.logging.Logger;

import org.apertereports.common.ConfigurationConstants;
import org.apertereports.common.ARConstants;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.ws.GenerateReportRequest;
import org.apertereports.common.xml.ws.GenerateReportResponse;
import org.apertereports.common.xml.ws.ObjectFactory;
import org.apertereports.common.xml.ws.ReportData;
import org.apertereports.engine.ReportWebServiceHelper;
import org.apertereports.ws.exception.ReportWebServiceException;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class ReportGeneratorEndpoint implements ARConstants, ConfigurationConstants {
    private static final Logger logger = Logger.getLogger(ReportGeneratorEndpoint.class.getName());
    private static final ObjectFactory objectFactory = new ObjectFactory();
    private ReportWebServiceHelper helper = new ReportWebServiceHelper();

    @PayloadRoot(localPart = WS_REQUEST_LOCAL_PART, namespace = WS_NAMESPACE)
    @ResponsePayload
    public GenerateReportResponse generateReport(@RequestPayload GenerateReportRequest request) throws ReportWebServiceException {
        ReportData reportData = request.getReportData();
        String mimeType = ReportMimeType.valueOf(reportData.getFormat()).mimeType();
        GenerateReportResponse response = objectFactory.createGenerateReportResponse();
        response.setMimeType(mimeType);
        response.setReportFormat(reportData.getFormat());

        try {
            byte[] content = helper.generateAndExportReport(reportData);
            response.setContent(ReportGeneratorUtils.wrapBytesInDataHandler(content, mimeType));
        }
        catch (ARException e) {
            throw new ReportWebServiceException(e);
        }
        catch (IOException e) {
            logger.info(e.getMessage());
            throw new ReportWebServiceException(ErrorCode.JASPER_REPORTS_EXCEPTION, "Exception while generating report: "
                    + (reportData.getName() != null ? reportData.getName() : reportData.getId())
                    + ". Detailed message: " + e.getMessage());
        }

        return response;
    }

}

