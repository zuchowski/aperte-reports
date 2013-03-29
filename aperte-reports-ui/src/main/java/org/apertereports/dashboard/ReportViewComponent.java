package org.apertereports.dashboard;

import com.vaadin.ui.Panel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import org.apertereports.AbstractLazyLoaderComponent;
import org.apertereports.AbstractReportingApplication;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.ARConstants.ReportType;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.utils.TextUtils;
import org.apertereports.common.utils.TimeUtils;
import org.apertereports.common.wrappers.Pair;
import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.CyclicReportConfigDAO;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.dao.utils.ConfigurationCache;
import org.apertereports.dashboard.html.HtmlReportBuilder;
import org.apertereports.dashboard.html.ReportDataProvider;
import org.apertereports.engine.ReportMaster;
import org.apertereports.model.CyclicReportConfig;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.DashboardUtil;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;
import org.apertereports.util.cache.MapCache;
import org.apertereports.util.files.TmpDirMgr;
import org.apertereports.util.files.Zipper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component is used to display the generated reports in the portlet. It
 * analyzes the template and report config list loaded from portlet preferences
 * and transforms the HTML according to the discovered
 * <code>report</code> tags.
 * <p/>
 * The contents of every generated report are cached using a {@link MapCache}
 * instance. These caches are cleared after a configured interval thus enabling
 * the component to generate the report again with more up to date data.
 * <p/>
 * The component also supports lazy loading due to the fact that the generated
 * reports can grow to enormous sizes.
 */
public class ReportViewComponent extends AbstractLazyLoaderComponent implements ReportDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(ReportViewComponent.class.getName());
    private static final String AR_DASHBOARD_REPORT_PANEL_STYLE_ID = "ar-dashboard-report-panel";
    private static final String REPORT_DIR = "report";
    private static final String IMAGES_DIR = "images";
    private static final String REPORT_HTML_FILE = "report.html";
    private static final String REPORT_ZIP_FILE = "report.zip";
    private Panel reportPanel = new Panel();
    /**
     * Internal caches of configs, templates and cyclic orders.
     */
    private Map<Integer, ReportTemplate> reportMap = new HashMap<Integer, ReportTemplate>();
    private Map<Integer, ReportConfig> configMap = new HashMap<Integer, ReportConfig>();
    private Map<Long, CyclicReportConfig> cyclicReportMap = new HashMap<Long, CyclicReportConfig>();
    private String template;
    private MapCache cache;
    private AbstractReportingApplication application;

    public ReportViewComponent(AbstractReportingApplication application, MapCache cache, String template, List<ReportConfig> configs,
            boolean lazyLoad) {
        this.application = application;
        this.cache = cache;
        this.template = template;
        initInternalData(configs);

        reportPanel.setSizeFull();
        reportPanel.getContent().setSizeUndefined();
        reportPanel.getContent().setStyleName(AR_DASHBOARD_REPORT_PANEL_STYLE_ID);

        setCompositionRoot(reportPanel);
        if (!lazyLoad) {
            init();
        }
    }

    /**
     * Initializes the view. The dashboard HTML template is searched for a
     * number of
     * <code>report</code> tags. Each tag contains an identifier of a report
     * config stored in portlet preferences.
     * <p/>
     * The
     * <code>report</code> tag is replaced with a custom component reference
     * afterwards which may be an image, a HTML source component or a drilldown
     * link.
     *
     * @see HtmlReportBuilder
     */
    private void init() {
        if (template != null && !template.isEmpty()) {
            final HtmlReportBuilder builder = new HtmlReportBuilder(application, this);
            final int[] index = {0};
            DashboardUtil.executeTemplateMatcherWithList(template, DashboardUtil.REPORT_TAG_PATTERN,
                    new DashboardUtil.MatchHandlerWithList() {

                        @Override
                        public void handleMatch(int start, int end, List<String> matches) {
                            builder.addHtmlChunk(template.substring(index[0], start));
                            index[0] = end;
                            if (matches != null && !matches.isEmpty()) {
                                ReportConfig config = configMap.get(Integer.parseInt(matches.get(0)));
                                ReportConfig xlsConfig = null;
                                if (matches.size() > 2 && matches.get(2) != null) {
                                    xlsConfig = configMap.get(Integer.parseInt(matches.get(2)));
                                }
                                if (config != null) {
                                    builder.addReportChunk(config, xlsConfig);
                                }
                            }
                        }
                    });
            builder.addHtmlChunk(template.substring(index[0]));
            try {
                reportPanel.addComponent(builder.createLayout());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.gui.error"));
                throw new RuntimeException(e);

            }
        }
    }

    /**
     * Generates a temporary drilldown report config from a map of link
     * parameters. The parameters are taken from a generated report.
     *
     * @param params Drilldown parameters
     * @return A temporary {@link ReportConfig}
     */
    @Override
    public ReportConfig generateDrilldownReportConfig(Map<String, List<String>> params) {
        ReportConfig drillConfig = new ReportConfig();

        List<String> reportNames = params.get("reportName");
        if (reportNames == null || reportNames.isEmpty()) {
            throw new ARRuntimeException(ErrorCode.DRILLDOWN_NOT_FOUND);
        }
        String reportName = reportNames.get(0); // bierzemy pierwszy z brzegu
        for (ReportTemplate rt : reportMap.values()) {
            if (rt.getReportname().equals(reportName)) {
                drillConfig.setReportId(rt.getId());
                break;
            }
        }
        if (drillConfig.getReportId() == null) {
            Collection<ReportTemplate> reportTemplates = ReportTemplateDAO.fetchByName(application.getArUser(), reportName);
            if (reportTemplates.isEmpty()) {
                throw new ARRuntimeException(ErrorCode.DRILLDOWN_REPORT_NOT_FOUND);
            }
            ReportTemplate rt = reportTemplates.iterator().next(); // bierzemy pierwszy z brzegu
            drillConfig.setReportId(rt.getId());
        }

        drillConfig.setAllowRefresh(false);
        drillConfig.setCacheTimeout(0);
        drillConfig.setId(DashboardUtil.generateDrilldownId(configMap.keySet()));
        Map<String, String> reportParameters = new HashMap<String, String>();
        for (Entry<String, List<String>> e : params.entrySet()) {
            String key = e.getKey();
            List<String> values = e.getValue();
            if (key.equalsIgnoreCase("allowedFormats")) {
                List<String> allowedFormats = new ArrayList<String>();
                for (String f : values) {
                    String[] splitted = f.split(",");
                    allowedFormats.addAll(Arrays.asList(splitted));
                }
                drillConfig.setAllowedFormatsFromList(allowedFormats);
            } else if (key.equalsIgnoreCase("allowRefresh")) {
                if (values.size() == 1) {
                    drillConfig.setAllowRefresh("true".equalsIgnoreCase(values.get(0)));
                }
            } else if (key.equalsIgnoreCase("cacheTimeout")) {
                if (values.size() == 1) {
                    drillConfig.setCacheTimeout(Integer.parseInt(values.get(0)));
                }
            } else {
                if (values.size() > 0) {
                    String paramValue = values.size() == 1 ? TextUtils.encodeObjectToSQL(values.get(0)) : TextUtils.encodeObjectToSQL(values);
                    reportParameters.put(key, paramValue);
                }
            }
        }
        drillConfig.setParameters(XmlReportConfigLoader.getInstance().mapToParameterList(reportParameters));
        return drillConfig;
    }

    /**
     * Provides the report templates from database for other components based on
     * passed {@link ReportConfig} parameter.
     *
     * @param config A report config
     * @return A relevant report template
     * @see ReportDataProvider
     */
    @Override
    public ReportTemplate provideReportTemplate(ReportConfig config) {
        if (!reportMap.containsKey(config.getReportId())) {
            try {
                ReportTemplate report = ReportTemplateDAO.fetchById(application.getArUser(), config.getReportId());
                if (report != null) {
                    reportMap.put(config.getReportId(), report);
                }
            } catch (ARException ex) {
                throw new ARRuntimeException(ex);
            }
        }
        return reportMap.get(config.getReportId());
    }

    @Override
    public File provideReportFileForHtmlExport(ReportConfig config, boolean cached) {
        JasperPrint jp = getJasperPrint(config, cached);


        TmpDirMgr tmp = new TmpDirMgr();

        File tmpDir = tmp.createNewTmpDir(REPORT_DIR);
        String reportDirPath = tmpDir.getAbsolutePath() + File.separator + REPORT_DIR;

        byte[] data = getReportData(jp, ARConstants.ReportType.HTML, reportDirPath);
        File f = new File(reportDirPath + File.separator + REPORT_HTML_FILE);
        
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(data);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ex) {
                }
            }
            throw new ARRuntimeException(e);
        }

        File imagesDir = new File(reportDirPath + File.separator + IMAGES_DIR);
        if (!imagesDir.isDirectory()) {
            return f;   //only hmtl file
        }

        try {
            File zipF = new File(tmpDir.getAbsolutePath() + File.separator + REPORT_ZIP_FILE);
            Zipper.zip(reportDirPath, zipF.getAbsolutePath());
            return zipF;
        } catch (Exception ex) {
            throw new ARRuntimeException(ex);
        }
    }

    /**
     * Provides a report data from the cache or generates it and caches for
     * later use to boost performance.
     *
     * @param config Input config
     * @param format Output format
     * @param cached Should the data be taken from a cache or generated directly
     * @return A pair of objects - the JasperPrint and the bytes of the
     * generated report
     * @see ReportDataProvider
     */
    @Override
    public Pair<JasperPrint, byte[]> provideReportData(ReportConfig config, ReportType format, boolean cached) {
        JasperPrint jp = getJasperPrint(config, cached);
        byte[] data = getReportData(jp, format, null);
        return new Pair<JasperPrint, byte[]>(jp, data);
    }

    /**
     * Gets JasperPrint object for given report config and fora
     *
     * @param config Report config
     * @param cached Determines if the data should be taken from a cache or
     * generated directly
     * @return JasperPrint object
     */
    private JasperPrint getJasperPrint(ReportConfig config, boolean cached) {
        try {
            if (cached) {
                JasperPrint jp = (JasperPrint) cache.provideData(config.getId().toString());
                if (jp != null) {
                    return jp;
                }
            }

            ReportTemplate report = provideReportTemplate(config);
            if (report == null) {
                throw new ARException(ErrorCode.REPORT_TEMPLATE_NOT_FOUND);
            }
            ReportMaster reportMaster = new ReportMaster(report.getContent(), report.getId().toString(),
                    new ReportTemplateProvider());
            Map<String, Object> parameters;
            if (config.getCyclicReportId() != null) {
                CyclicReportConfig cro = cyclicReportMap.get(config.getCyclicReportId());
                parameters = new HashMap<String, Object>(XmlReportConfigLoader.getInstance().xmlAsMap(
                        cro.getParametersXml() != null ? cro.getParametersXml() : ""));
            } else {
                parameters = new HashMap<String, Object>(XmlReportConfigLoader.getInstance().parameterListToMap(
                        config.getParameters()));
            }
            JasperPrint jp = reportMaster.generateReport(parameters);
            cache.cacheData(config.getId().toString(), TimeUtils.secondsToMilliseconds(config.getCacheTimeout()), jp);
            return jp;
        } catch (ARException e) {
            throw new ARRuntimeException(e);
        }
    }

    /**
     * Gets report data
     *
     * @param jasperPrint JasperPrint object
     * @param format Format of the report data
     * @param htmlExportDirPath Path to images dir for html export, null for
     * other formats
     * @return
     */
    private byte[] getReportData(JasperPrint jasperPrint, ReportType format, String htmlExportDirPath) {
        try {
            Map<JRExporterParameter, Object> customParams = null;

            if (ReportType.HTML.equals(format)) {
                customParams = new HashMap<JRExporterParameter, Object>();
                customParams.put(JRHtmlExporterParameter.IMAGES_URI, DashboardUtil.CHART_SOURCE_PREFIX_TEXT);

                if (htmlExportDirPath != null) {
                    customParams.put(JRHtmlExporterParameter.IS_OUTPUT_IMAGES_TO_DIR, Boolean.TRUE);
                    customParams.put(JRHtmlExporterParameter.IMAGES_DIR_NAME,
                            htmlExportDirPath + File.separator + IMAGES_DIR + File.separator);
                    customParams.put(JRHtmlExporterParameter.IMAGES_URI, IMAGES_DIR + File.separator);
                }
            }
            return ReportMaster.exportReport(jasperPrint, format.name(), customParams,
                    ConfigurationCache.getConfiguration());
        } catch (ARException e) {
            throw new ARRuntimeException(e);
        }
    }

    /**
     * Initializes the internal map caches of report configs and templates
     * relevant to the input report configs.
     *
     * @param configs Input report configs
     */
    private void initInternalData(List<ReportConfig> configs) {
        Set<Integer> configIds = DashboardUtil.getReportConfigIds(template);
        if (!configIds.isEmpty() && configs != null && !configs.isEmpty()) {
            Set<Integer> reportIds = new HashSet<Integer>();
            Map<Long, ReportConfig> cyclicConfigMap = new HashMap<Long, ReportConfig>();
            for (ReportConfig rc : configs) {
                if (configIds.contains(rc.getId())) {
                    configMap.put(rc.getId(), rc);
                    if (rc.getCyclicReportId() != null) {
                        cyclicConfigMap.put(rc.getCyclicReportId(), rc);
                    } else {
                        reportIds.add(rc.getReportId());
                    }
                }
            }
            Collection<CyclicReportConfig> cyclicReports = CyclicReportConfigDAO.fetchByIds(cyclicConfigMap.keySet().toArray(new Long[cyclicConfigMap.keySet().size()]));
            for (CyclicReportConfig rep : cyclicReports) {
                ReportConfig rc = cyclicConfigMap.get(rep.getId());
                rc.setReportId(rep.getReport().getId());
                reportIds.add(rep.getReport().getId());
                cyclicReportMap.put(rep.getId(), rep);
            }

            Collection<ReportTemplate> reports = ReportTemplateDAO.fetchByIds(reportIds.toArray(new Integer[configIds.size()]));
            for (ReportTemplate rep : reports) {
                reportMap.put(rep.getId(), rep);
            }
        }
    }

    /**
     * Initializes the component with lazy loading.
     *
     * @throws Exception on lazy load error
     */
    @Override
    public void lazyLoad() throws Exception {
        init();
    }
}
