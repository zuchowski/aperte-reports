package org.apertereports.dashboard.html;

import com.vaadin.Application;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.terminal.StreamResource;
import com.vaadin.ui.*;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.type.ModeEnum;
import net.sf.jasperreports.engine.util.JRTypeSniffer;
import net.sf.jasperreports.renderers.DataRenderable;
import net.sf.jasperreports.engine.type.ImageTypeEnum;


import org.apertereports.util.DashboardUtil;
import org.apertereports.util.FileStreamer;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;
import org.vaadin.activelink.ActiveLink;
import org.apertereports.common.wrappers.Pair;
import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.model.ReportTemplate;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static com.vaadin.terminal.Sizeable.UNITS_PERCENTAGE;
import java.io.File;
import java.util.Map.Entry;
import static org.apertereports.common.ARConstants.ReportType;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class that manages the creation of the layouts containing generated
 * report HTML data. It transforms the tags from input HTML into
 * <code>div</code> tags and a corresponding report data. The resulting
 * transformed HTML is then fed to a {@link CustomLayout} widget which is filled
 * with generated components.
 */
public class HtmlReportBuilder {

    private static final Logger logger = LoggerFactory.getLogger(HtmlReportBuilder.class);
    /**
     * Tags for identification.
     */
    private static final String BUTTONS_TAG = "buttons";
    private static final String REPORT_TAG = "report";
    private static final String DRILLDOWN_TAG = "drilldown";
    private static final String CHART_TAG = "chart";
    /**
     * Processed HTML buffer.
     */
    private StringBuffer contentBuffer = new StringBuffer();
    /**
     * Generated components for the main custom layout.
     */
    private Map<String, Component> mainComponentMap = new HashMap<String, Component>();
    /**
     * Generated components for reports containing drilldowns.
     */
    private Map<String, Map<String, Component>> customComponentMap = new HashMap<String, Map<String, Component>>();
    /**
     * The bean that provides the data for the reports.
     */
    private ReportDataProvider provider;
    /**
     * Output custom layout
     */
    private CustomLayout layout;
    private Application application;
   
    public HtmlReportBuilder(Application application, ReportDataProvider provider) {
        this.application = application;
        this.provider = provider;
    }

    /**
     * Creates a custom layout from the buffered HTML and generated components.
     *
     * @return A custom layout
     * @throws IOException if the {@link CustomLayout} component initialization
     * fails
     */
    public CustomLayout createLayout() throws IOException {
        layout = new CustomLayout(new ByteArrayInputStream(contentBuffer.toString().getBytes()));
        for (Entry<String, Component> e : mainComponentMap.entrySet()) {
            Component c = e.getValue();
            layout.addComponent(c, e.getKey());
            if (c instanceof CustomLayout) {
                Map<String, Component> map = customComponentMap.get(e.getKey());
                if (map != null && !map.isEmpty()) {
                    for (Entry<String, Component> ee : map.entrySet()) {
                        ((CustomLayout) c).addComponent(ee.getValue(), ee.getKey());
                    }
                }
            }
        }
    
        return layout;
    }

    public void addHtmlChunk(String html) {
        contentBuffer.append(html);
    }

    /**
     * Adds a report chunk based on passed report configs. At first, the method
     * generates a new component for a given config and attaches it to the
     * component map.
     * <p/>
     * The component map is later used to fill in the output {@link CustomLayout}.
     *
     * @param config The main report config
     * @param xlsConfig An optional XLS config
     */
    public void addReportChunk(final ReportConfig config, final ReportConfig xlsConfig) {
        HorizontalLayout buttons = createReportButtons(config, null, xlsConfig, null);

        String componentKey = BUTTONS_TAG + config.getId();
        mainComponentMap.put(componentKey, buttons);
        contentBuffer.append(createDivAnchor(componentKey));

        Component reportComponent = createReportComponent(config, true);
        componentKey = REPORT_TAG + config.getId();
        mainComponentMap.put(componentKey, reportComponent != null ? reportComponent : new Label());
        contentBuffer.append(createDivAnchor(componentKey));
    }

    /**
     * Creates the on demand report generation buttons. These are displayed just
     * above the displayed dashboard.
     * <p/>
     * A special handling procedure was introduced for XLS format. Each report
     * tag can include an optional
     * <code>xlsidx</code> parameter which is the id of the XLS config. When the
     * XLS generation is pressed the other config is used rather than the config
     * which stands for
     * <code>idx</code> parameter.
     *
     * @param config The main report config
     * @param parentConfig The parent report config in case of a drilldowned
     * report
     * @param xlsConfig An optional XLS report config
     * @param componentId The output component id
     * @return A horizontal layout with buttons
     */
    private HorizontalLayout createReportButtons(final ReportConfig config, final ReportConfig parentConfig,
            final ReportConfig xlsConfig, final String componentId) {
        HorizontalLayout buttons = UiFactory.createHLayout(null, FAction.SET_SPACING);
        buttons.setSpacing(true);

        if (parentConfig != null && componentId != null) {
            Button tmpButton = UiFactory.createButton("dashboard.view.drill.up", buttons, new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    returnFromDrill(config, parentConfig, componentId);
                }
            });
            tmpButton.addStyleName("btn");
        }
        List<String> allowedFormats = config.getAllowedFormatsAsList();
        for (final String format : allowedFormats) {
            Button tmpButton = UiFactory.createButton(format, buttons, new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    if ("XLS".equalsIgnoreCase(format) && xlsConfig != null) {
                        handleExportRequest(xlsConfig, format);
                    } else {
                        handleExportRequest(config, format);
                    }
                }
            });
            tmpButton.addStyleName("btn");
        }
        if (Boolean.TRUE.equals(config.getAllowRefresh())) {
            Button tmpButton = UiFactory.createButton(UiIds.LABEL_REFRESH, buttons, new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    refreshReport(config);
                }
            });
            tmpButton.addStyleName("btn");
        }

        boolean first = true;
        for (Iterator<Component> it = buttons.getComponentIterator(); it.hasNext();) {
            Component c = it.next();
            buttons.setComponentAlignment(c, Alignment.MIDDLE_RIGHT);
            if (first) {
                buttons.setExpandRatio(c, 1f);
                first = false;
            }
        }

        buttons.setSizeUndefined();
        buttons.setWidth(100, UNITS_PERCENTAGE);
        return buttons;
    }

    /**
     * Handles the export report request based on a given {@link ReportConfig}
     * and the format. The data is fetched without caching which means the
     * generated report saved provided by the download popup is up to date.
     *
     * @param config A report config
     * @param format Output format
     */
    private void handleExportRequest(ReportConfig config, String format) {
        ReportType reportType = ReportType.valueOf(format);
        ReportTemplate report = provider.provideReportTemplate(config);
        //xxx should be reconstructed
        if (report == null) {
            return;
        }

        if ("HTML".equals(format)) {
            File f = provider.provideReportFileForHtmlExport(config, true);
            boolean newWindow = f.getName().endsWith(".html");
            FileStreamer.openFile(application, f, newWindow);
        } else {
            Pair<JasperPrint, byte[]> reportData = provider.provideReportData(config, reportType, false);
            if (reportData != null) {
                FileStreamer.showFile(application, report.getReportname(), reportData.getEntry(), reportType.name());
            }
        }
    }

    /**
     * Re-creates the report component corresponding with a given {@link ReportConfig}.
     * Used to refresh the view of the dashboard manually.
     *
     * @param config A report config
     */
    private void refreshReport(ReportConfig config) {
        String componentKey = REPORT_TAG + config.getId();
        Map<String, Component> map = customComponentMap.get(componentKey);
        if (map != null) {
            map.clear();
        }

        CustomLayout reportComponent = createReportComponent(config, false);
        if (reportComponent != null) {
            if (map != null && !map.isEmpty()) {
                for (Entry<String, Component> e : map.entrySet()) {
                    reportComponent.addComponent(e.getValue(), e.getKey());
                }
            }
            Component oldComponent = mainComponentMap.get(componentKey);
            mainComponentMap.put(componentKey, reportComponent);
            layout.replaceComponent(oldComponent, reportComponent);
        }
    }

    /**
     * Creates a {@link CustomLayout} widget based on the generated Jasper HTML
     * report and the report config.
     *
     * @param config The report config
     * @param cached
     * <code>TRUE</code> if the data can be fetched from the cache.
     * <code>FALSE</code> otherwise.
     * @return A widget containing
     * <code>div</code> anchors for the report and the drilldowns
     */
    private CustomLayout createReportComponent(ReportConfig config, boolean cached) {
        Pair<JasperPrint, byte[]> reportData;
        try {
            reportData = provider.provideReportData(config, ReportType.HTML, cached);
        } catch (ARRuntimeException e) {
            try {
                String errorLayout = "<div location=\"errorLabel\"></div>";
                String msg = "<b><span style=\"color:red\">"
                        + VaadinUtil.getValue("dashboard.report.creation.error") + "</span></b>";
                msg += "<br>";

                String errorPrefix = "exception." + e.getErrorCode().name().toLowerCase();

                msg += VaadinUtil.getValue(errorPrefix + ".title");
                String description = "<br/>"
                        + VaadinUtil.getValue(errorPrefix + ".desc", e.getErrorDetails());
                if (e.getCause() != null && e.getCause().getLocalizedMessage() != null) {
                    description += "<br/>" + e.getCause().getLocalizedMessage();
                }
                msg += description;
                Label l = new Label(msg, Label.CONTENT_RAW);

                CustomLayout cl = new CustomLayout(new ByteArrayInputStream(errorLayout.getBytes()));
                cl.addComponent(l, "errorLabel");

                cl.setSizeUndefined();
                cl.setWidth(100, UNITS_PERCENTAGE);

                return cl;
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
                throw new RuntimeException(ex);
            }
        }

        if (reportData == null) {
            return null;
        }
        JasperPrint jasperPrint = reportData.getKey();
        String reportHtml = new String(reportData.getEntry());

        reportHtml = createChartComponents(reportHtml, jasperPrint, config);
        reportHtml = createDrilldownComponents(reportHtml, config);

        CustomLayout reportLayout = null;
        try {
            reportLayout = new CustomLayout(new ByteArrayInputStream(reportHtml.getBytes()));
            reportLayout.setSizeUndefined();
            reportLayout.setWidth(100, UNITS_PERCENTAGE);
            
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }

        return reportLayout;
    }

    /**
     * Analyzes the input report HTML source in search of report tags. Each tag
     * is then replaced with a
     * <code>div</code> anchor. The anchor is later substituted by an {@link Embedded}
     * widget that displays the actual generated report.
     * <p/>
     * The resulting HTML (without report tags) is later fed to a {@link CustomLayout}
     * component.
     *
     * @param reportHtml Input report HTML source
     * @param jasperPrint The report data in form of a {@link JasperPrint}
     * @param config The report config corresponding to the HTML source
     * @return Modified HTML with transformed report tags
     */
    private String createChartComponents(final String reportHtml, final JasperPrint jasperPrint, final ReportConfig config) {
        final StringBuilder builder = new StringBuilder();
        final int[] index = {0};
        DashboardUtil.executeTemplateMatcher(reportHtml, DashboardUtil.CHART_TAG_PATTERN, new DashboardUtil.MatchHandler() {

            @Override
            public void handleMatch(int start, int end, String match) {
                builder.append(reportHtml.substring(index[0], start));
                index[0] = end;
                if (match != null) {
                    try {
                        Pair<String, Map<String, String>> tag = DashboardUtil.parseHtmlTag(match, false);
                        Map<String, String> params = tag.getEntry();
                        if (!params.isEmpty() && params.containsKey("src")) {
                            String imageId = params.get("src").substring(DashboardUtil.CHART_SOURCE_PREFIX_TEXT.length());
                            StreamResource resource = getJasperImageStreamResource(imageId, jasperPrint);
                            String alt = params.get("alt");
                            Embedded img = new Embedded(alt != null && alt.trim().length() > 0 ? alt : "", resource);
                            if (params.containsKey("style")) {
                                img.addStyleName(params.get("style"));
                            }
                            img.setType(Embedded.TYPE_IMAGE);

                            String componentId = REPORT_TAG + config.getId() + CHART_TAG + imageId;
                            builder.append(createDivAnchor(componentId));
                            addReportComponent(img, componentId, config);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        NotificationUtil.showExceptionNotification(application.getMainWindow(), VaadinUtil.getValue("exception.report.build.title"),
                                VaadinUtil.getValue("exception.report.build.description").replaceFirst("%s", e.getMessage()));
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        builder.append(reportHtml.substring(index[0]));
        return builder.toString();
    }

    /**
     * Adds a report component based on a given report config to generated
     * components map.
     *
     * @param comp The generated component
     * @param componentId Component identifier
     * @param config The config used to generate the component
     */
    private void addReportComponent(Component comp, String componentId, ReportConfig config) {
        String reportComponentKey = REPORT_TAG + config.getId();
        Map<String, Component> map = customComponentMap.get(reportComponentKey);
        if (map == null) {
            map = new HashMap<String, Component>();
        }
        map.put(componentId, comp);
        customComponentMap.put(reportComponentKey, map);
    }

    /**
     * Analyzes the input report HTML source in search of drilldown tags. Each
     * tag is then replaced with a
     * <code>div</code> anchor. The anchor is later substituted by an active
     * link component when the resulting HTML (without drilldown tags) is fed to
     * a {@link CustomLayout} component.
     *
     * @param reportHtml The report HTML source
     * @param config A report config relevant to the HTML source
     * @return Modified HTML with transformed drilldown tags
     */
    private String createDrilldownComponents(final String reportHtml, final ReportConfig config) {
        final StringBuilder builder = new StringBuilder();
        final int[] index = {0}, drilldownId = {0};
        DashboardUtil.executeTemplateMatcher(reportHtml, DashboardUtil.DRILLDOWN_TAG_PATTERN, new DashboardUtil.MatchHandler() {

            @Override
            public void handleMatch(int start, int end, String match) {
                builder.append(reportHtml.substring(index[0], start));
                index[0] = end;
                if (match != null) {
                    try {
                        match = match.replaceAll("&", "&amp;");
                        Pair<String, Map<String, String>> tag = DashboardUtil.parseHtmlTag(match, true);
                        Map<String, String> params = tag.getEntry();
                        if (!params.isEmpty() && params.containsKey("href")) {
                            final String componentId = REPORT_TAG + config.getId() + DRILLDOWN_TAG + drilldownId[0]++;
                            String href = params.get("href");
                            final Map<String, List<String>> hyperlinkParams = DashboardUtil.parseHyperlinkParameters(href);
                            ActiveLink link = new ActiveLink(tag.getKey().trim().length() > 0 ? tag.getKey() : href, new ExternalResource(href));
                            if (params.containsKey("style")) {
                                link.addStyleName(params.get("style"));
                            }
                            link.addListener(new ActiveLink.LinkActivatedListener() {

                                @Override
                                public void linkActivated(ActiveLink.LinkActivatedEvent event) {
                                    openDrilldown(config, hyperlinkParams, componentId);
                                }
                            });
                            builder.append(createDivAnchor(componentId));
                            addReportComponent(link, componentId, config);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        NotificationUtil.showExceptionNotification(application.getMainWindow(), VaadinUtil.getValue("exception.report.build.title"),
                                VaadinUtil.getValue("exception.report.build.description").replaceFirst("%s", e.getMessage()));
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        builder.append(reportHtml.substring(index[0]));
        return builder.toString();
    }

    /**
     * Replaces the navigated drilldown component with a previously rendered
     * one.
     *
     * @param childConfig The config to return from
     * @param parentConfig The config to return to
     * @param componentId Old component id
     */
    private void returnFromDrill(ReportConfig childConfig, ReportConfig parentConfig, String componentId) {
        customComponentMap.remove(REPORT_TAG + childConfig.getId());

        String componentKey = componentId + REPORT_TAG;
        Component oldComponent = mainComponentMap.get(componentKey);
        Component newComponent = mainComponentMap.get(REPORT_TAG + parentConfig.getId());
        layout.replaceComponent(oldComponent, newComponent);
        mainComponentMap.remove(componentKey);

        componentKey = componentId + BUTTONS_TAG;
        oldComponent = mainComponentMap.get(componentKey);
        newComponent = mainComponentMap.get(BUTTONS_TAG + parentConfig.getId());
        layout.replaceComponent(oldComponent, newComponent);
        mainComponentMap.remove(componentKey);
    }

    /**
     * Navigates to the drilldowned report. Replaces the report that contained
     * the drilldown with a report generated from the drilldown.
     *
     * @param parentConfig The report config that we are leaving
     * @param params Report generator parameters
     * @param componentId New component id
     */
    private void openDrilldown(ReportConfig parentConfig, Map<String, List<String>> params, String componentId) {
        ReportConfig drillConfig = provider.generateDrilldownReportConfig(params);
        CustomLayout drillReport = createReportComponent(drillConfig, false);
        if (drillReport != null) {
            Component drillButtons = createReportButtons(drillConfig, parentConfig, null, componentId);
            Map<String, Component> map = customComponentMap.get(REPORT_TAG + drillConfig.getId());
            if (map != null && !map.isEmpty()) {
                for (Entry<String, Component> e : map.entrySet()) {
                    drillReport.addComponent(e.getValue(), e.getKey());
                }
            }

            String componentKey = componentId + REPORT_TAG;
            mainComponentMap.put(componentKey, drillReport);
            componentKey = componentId + BUTTONS_TAG;
            mainComponentMap.put(componentKey, drillButtons);

            Component oldComponent = mainComponentMap.get(REPORT_TAG + parentConfig.getId());
            layout.replaceComponent(oldComponent, drillReport);
            oldComponent = mainComponentMap.get(BUTTONS_TAG + parentConfig.getId());
            layout.replaceComponent(oldComponent, drillButtons);
        }
    }

    /**
     * Converts a {@link JasperPrint} to a {@link StreamResource} so that {@link Embedded}
     * can use it and display in the dashboard component.
     *
     * @param imageId The identifier of the image
     * @param jasperPrint Input Jasper print
     * @return A stream resource
     * @throws JRException on Jasper error
     */
    private StreamResource getJasperImageStreamResource(String imageId, JasperPrint jasperPrint) throws JRException {
        List<JasperPrint> jasperPrintList = new ArrayList<JasperPrint>();
        jasperPrintList.add(jasperPrint);

        JRPrintImage image = JRHtmlExporter.getImage(jasperPrintList, imageId);
        DataRenderable renderer = (DataRenderable)image.getRenderer();
//        if (renderer.getType() == JRRenderable.TYPE_SVG) {
//            renderer = new WrappingRenderToImageDataRenderer(renderer, new Dimension(image.getWidth(), image.getHeight()),
//                    ModeEnum.OPAQUE == image.getModeValue() ? image.getBackcolor() : null);
//        }
        byte[] data = renderer.getData(new SimpleJasperReportsContext());
        ImageTypeEnum imageMimeType = JRTypeSniffer.getImageTypeValue(data);
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(data);

        StreamResource streamResource = new StreamResource(new StreamResource.StreamSource() {

            @Override
            public InputStream getStream() {
                return inputStream;
            }
        }, imageId, application);

        streamResource.setMIMEType(imageMimeType.getMimeType());
        return streamResource;
    }

    /**
     * Creates a
     * <code>div</code> anchor string which can be attached to a {@link CustomLayout}
     * component.
     *
     * @param componentId Component id to be displayed within the
     * <code>div</code>
     * @return Wrapped string
     */
    private String createDivAnchor(String componentId) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div location=\"").append(componentId).append("\"></div>");
        return sb.toString();
    }
}