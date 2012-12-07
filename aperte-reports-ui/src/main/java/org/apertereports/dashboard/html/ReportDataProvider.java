package org.apertereports.dashboard.html;

import net.sf.jasperreports.engine.JasperPrint;
import org.apertereports.common.ReportConstants;
import org.apertereports.common.wrappers.Pair;
import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.model.ReportTemplate;

import java.util.List;
import java.util.Map;

/**
 * Marks a class a controller of report templates and the generator data.
 */
public interface ReportDataProvider {

    /**
     * Provides a generated report out of a {@link ReportConfig}. The data is
     * usually cached somewhere to boost the performance.
     *
     * @param config Input config
     * @param format Output format
     * @param cached Should the data be taken from a cache or generated directly
     * @return A pair of {@link JasperPrint} and bytes of report data
     */
    Pair<JasperPrint, byte[]> provideReportData(ReportConfig config, ReportConstants.ReportType format, boolean cached);

    /**
     * Provides a Jasper Reports template based on a given {@link ReportConfig}.
     *
     * @param config Input config
     * @return A report template
     */
    ReportTemplate provideReportTemplate(ReportConfig config);

    /**
     * Generates a temporary drilldown configuration for given parameters.
     *
     * @param parameters Input parameters
     * @return A report config
     */
    ReportConfig generateDrilldownReportConfig(Map<String, List<String>> parameters);
}
