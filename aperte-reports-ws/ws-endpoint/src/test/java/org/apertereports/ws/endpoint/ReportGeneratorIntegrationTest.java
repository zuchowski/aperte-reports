package org.apertereports.ws.endpoint;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.oxm.Marshaller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.ws.test.server.ResponseMatcher;
import org.springframework.xml.transform.StringSource;
import org.apertereports.common.xml.ws.GenerateReportRequest;
import org.apertereports.common.xml.ws.ReportData;
import org.apertereports.common.utils.ReportGeneratorUtils;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

import static org.springframework.ws.test.server.RequestCreators.withPayload;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:test-context.xml")
public class ReportGeneratorIntegrationTest {
    @Autowired
    private ApplicationContext applicationContext;


    private MockWebServiceClient mockClient;

    @Autowired
    private Marshaller marshaller;

    @Before
    public void createClient() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    public void customerEndpoint() throws Exception {
        GenerateReportRequest request = new GenerateReportRequest();
        ReportData reportData = new ReportData();
        reportData.setFormat("HTML");
        reportData.setId("someid");
        reportData.setName("helloworld");
        reportData.setCharacterEncoding("utf-8");

        byte[] source = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("test_jasper.jrxml"));

        reportData.setSource(ReportGeneratorUtils.wrapBytesInDataHandler(source, "text/html"));

        request.setReportData(reportData);

        StringWriter sw = new StringWriter();
        marshaller.marshal(request, new StreamResult(sw));
        System.out.println(sw.toString());
        mockClient.sendRequest(withPayload(new StringSource(sw.toString()))).andExpect(new ResponseMatcher() {
            @Override
            public void match(WebServiceMessage request, WebServiceMessage response) throws IOException, AssertionError {
                System.out.println("***** REQUEST ****");
                request.writeTo(System.out);
                System.out.println("\n***** RESPONSE ****");
                response.writeTo(System.out);
            }
        });
    }
}
