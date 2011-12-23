package org.apertereports.ws.client;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.Unmarshaller;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.client.core.WebServiceMessageCallback;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.apertereports.common.xml.ws.GenerateReportRequest;
import org.apertereports.common.xml.ws.GenerateReportResponse;
import org.apertereports.ws.utils.WebServiceMessageUtils;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.net.URI;
import java.net.URLConnection;
import java.util.logging.Logger;

public class ReportGeneratorWebServiceClient extends WebServiceGatewaySupport {
    private static final Logger logger = Logger.getLogger(ReportGeneratorWebServiceClient.class.getName());

    private boolean enableLogging = false;
    private int connectionTimeout = 2000;

    public boolean isEnableLogging() {
        return enableLogging;
    }

    public void setEnableLogging(boolean enableLogging) {
        this.enableLogging = enableLogging;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public ReportGeneratorWebServiceClient(SaajSoapMessageFactory messageFactory, Marshaller marshaller) {
        super(messageFactory);
        setMarshaller(marshaller);
        if (marshaller instanceof Unmarshaller) {
            setUnmarshaller((Unmarshaller) marshaller);
        }
    }

    public ReportGeneratorWebServiceClient(SaajSoapMessageFactory messageFactory, Marshaller marshaller, Unmarshaller unmarshaller) {
        super(messageFactory);
        setMarshaller(marshaller);
        setUnmarshaller(unmarshaller);
    }

    public boolean checkConnection() {
        URI uri = URI.create(getDefaultUri());
        if (uri.getHost() == null) {
            return false;
        }
        try {
            URLConnection connection = uri.toURL().openConnection();
            connection.setConnectTimeout(connectionTimeout);
            connection.connect();
            return true;
        }
        catch (Exception e) {
            if (enableLogging) {
                WebServiceMessageUtils.printStackTraceToLog(logger, e);
            }
            return false;
        }
    }

    public GenerateReportResponse requestGenerateReport(GenerateReportRequest request) {
        return (GenerateReportResponse) getWebServiceTemplate().marshalSendAndReceive(request, new WebServiceMessageCallback() {
            @Override
            public void doWithMessage(WebServiceMessage message) throws IOException, TransformerException {
                if (enableLogging) {
                    WebServiceMessageUtils.printMessageToLog(logger, message);
                }
            }
        });
    }
}
