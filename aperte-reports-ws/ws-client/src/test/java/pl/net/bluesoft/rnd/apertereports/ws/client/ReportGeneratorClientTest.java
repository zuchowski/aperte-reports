package org.apertereports.ws.client;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.ws.GenerateReportRequest;
import org.apertereports.common.xml.ws.GenerateReportResponse;
import org.apertereports.common.xml.ws.ReportData;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("aperte-reports-ws-client.xml")
public class ReportGeneratorClientTest {
    @Autowired
    private ReportGeneratorWebServiceClient client;

    @Test
    public void testClient() throws IOException {
        GenerateReportRequest request = new GenerateReportRequest();
        ReportData reportData = new ReportData();
        reportData.setFormat("HTML");
        reportData.setId("someid");
        reportData.setName("helloworld");
        reportData.setCharacterEncoding("utf-8");

        byte[] source = IOUtils.toByteArray(ClassLoader.getSystemResourceAsStream("test_jasper.jrxml"));

        reportData.setSource(ReportGeneratorUtils.wrapBytesInDataHandler(source, "text/html"));
        request.setReportData(reportData);

        GenerateReportResponse response = client.requestGenerateReport(request);
        System.out.println(new String(ReportGeneratorUtils.unwrapDataHandler(response.getContent())));
    }
}
