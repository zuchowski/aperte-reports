package pl.net.bluesoft.rnd.apertereports.ws.endpoint;

import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import pl.net.bluesoft.rnd.apertereports.common.ConfigurationConstants;
import pl.net.bluesoft.rnd.apertereports.common.ReportConstants;
import pl.net.bluesoft.rnd.apertereports.common.exception.ReportException;
import pl.net.bluesoft.rnd.apertereports.common.xml.ws.GenerateReportRequest;
import pl.net.bluesoft.rnd.apertereports.common.xml.ws.GenerateReportResponse;
import pl.net.bluesoft.rnd.apertereports.common.xml.ws.ObjectFactory;
import pl.net.bluesoft.rnd.apertereports.common.xml.ws.ReportData;
import pl.net.bluesoft.rnd.apertereports.common.utils.ReportGeneratorUtils;
import pl.net.bluesoft.rnd.apertereports.engine.ReportWebServiceHelper;
import pl.net.bluesoft.rnd.apertereports.ws.exception.ReportWebServiceException;

import java.io.IOException;
import java.util.logging.Logger;

@Endpoint
public class ReportGeneratorEndpoint implements ReportConstants, ConfigurationConstants {
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
        catch (ReportException e) {
            logger.info(e.getErrorDesc());
            throw new ReportWebServiceException(e);
        }
        catch (IOException e) {
            logger.info(e.getMessage());
            throw new ReportWebServiceException(ErrorCodes.JASPER_REPORTS_EXCEPTION, "Exception while generating report: "
                    + (reportData.getName() != null ? reportData.getName() : reportData.getId())
                    + ". Detailed message: " + e.getMessage());
        }

        return response;
    }

}

