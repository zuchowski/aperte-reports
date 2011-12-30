package org.apertereports.dashboard;

import com.vaadin.ui.*;
import org.apertereports.common.xml.config.ReportConfig;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.model.ReportTemplate;

import java.util.*;

import org.apertereports.components.HelpLayout;
import org.apertereports.components.ModalWindow;
import org.apertereports.components.HelpWindow.Module;
import org.apertereports.components.HelpWindow.Tab;
import org.apertereports.util.DashboardUtil;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;

/**
 * Displays the edit mode of the dashboard portlet. The view contains a rich text area and the report table.
 * A user can anchor the report by adding a <code>report</code> tag in the HTML source code of the text area.
 * Each tag has an attribute named <code>idx</code> which is the index of the report from the table.
 * To switch to the source code one should toggle "show source" button.
 * <p/>The report table, on the other hand, contains the configurations of reports that can be used
 * within the dashboard HTML. This should be filled by the user first.
 * <p/>It is possible to anchor multiple reports in a single dashboard by simply adding many <code>report</code>
 * tags in the source HTML.
 */
public class EditDashboardComponent extends AbstractDashboardComponent {
    /**
     * Text areas.
     */
    private RichTextArea richTextArea = new RichTextArea();
    private TextArea simpleTextArea = new TextArea();
    private Panel textPanel;
    private boolean isRichTextEnabled;

    /**
     * Buttons.
     */
    private Button saveButton = new Button(VaadinUtil.getValue("dashboard.edit.save"));
    private Button cancelButton = new Button(VaadinUtil.getValue("dashboard.edit.cancel"));
    private Button showSourceButton = new Button(VaadinUtil.getValue("dashboard.edit.source"));
    private Button addReportButton = new Button(VaadinUtil.getValue("dashboard.edit.add.report"));

    private Panel mainPanel = new Panel();
    private Table reportsTable = new Table();

    /**
     * Displays report details.
     */
    private Window reportDetailsWindow;

    public EditDashboardComponent() {
        mainPanel.setScrollable(true);
        mainPanel.setStyleName("borderless light");
        mainPanel.setSizeUndefined();
        initView();
        initData();
        setCompositionRoot(mainPanel);
    }

    /**
     * Initializes the data of the whole view.
     */
    @Override
    protected void initComponentData() {
        initReportsTable();
        initTextArea();
    }

    /**
     * Initializes the GUI of this component.
     */
    private void initView() {
        HorizontalLayout helpLayout = new HelpLayout(Module.DASHBOARD, Tab.EDIT_REPORT);

        mainPanel.addComponent(helpLayout);

        String idxId = VaadinUtil.getValue("dashboard.edit.table.idx");
        String detailsId = VaadinUtil.getValue("dashboard.edit.table.details");
        String deleteId = VaadinUtil.getValue("dashboard.edit.table.delete");
        String descriptionId = VaadinUtil.getValue("dashboard.edit.table.description");

        reportsTable.addContainerProperty(idxId, Integer.class, null);
        reportsTable.addContainerProperty(VaadinUtil.getValue("dashboard.edit.table.report"), String.class, null);
        reportsTable.addContainerProperty(descriptionId, String.class, null);
        reportsTable.addContainerProperty(VaadinUtil.getValue("dashboard.edit.table.type"), String.class, null);
        reportsTable.addContainerProperty(detailsId, Button.class, null);
        reportsTable.addContainerProperty(deleteId, Button.class, null);

        reportsTable.setColumnWidth(idxId, 20);
        reportsTable.setColumnWidth(detailsId, 80);
        reportsTable.setColumnWidth(deleteId, 50);

        reportsTable.setColumnExpandRatio(descriptionId, 1.0f);
        reportsTable.setWidth(600, UNITS_PIXELS);

        reportsTable.setSelectable(false);
        reportsTable.setImmediate(true);
        reportsTable.setPageLength(10);

        richTextArea.setSizeFull();
        simpleTextArea.setSizeFull();

        textPanel = new Panel();
        textPanel.setScrollable(true);
        textPanel.setStyleName("borderless light");
        textPanel.setSizeUndefined();

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setHeight(200, UNITS_PIXELS);
        textLayout.setWidth(600, UNITS_PIXELS);
        textPanel.setContent(textLayout);

        VerticalLayout table = new VerticalLayout();
        table.setSpacing(true);
        table.setMargin(false);
        table.addComponent(reportsTable);
        table.addComponent(addReportButton);
        table.setComponentAlignment(addReportButton, Alignment.MIDDLE_LEFT);
        table.setExpandRatio(reportsTable, 1.0f);

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSizeUndefined();
        buttons.setSpacing(true);
        buttons.addComponent(saveButton);
        buttons.addComponent(cancelButton);
        buttons.addComponent(showSourceButton);
        for (Iterator<Component> it = buttons.getComponentIterator(); it.hasNext(); ) {
            buttons.setExpandRatio(it.next(), 1.0f);
        }

        VerticalLayout splitLayout = new VerticalLayout();
        splitLayout.setSpacing(true);
        splitLayout.setMargin(false);
        splitLayout.addComponent(textPanel);
        splitLayout.addComponent(buttons);
        splitLayout.addComponent(table);
        splitLayout.setExpandRatio(table, 1.0f);

        mainPanel.addComponent(splitLayout);

        showSourceButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                enableRichTextArea(!isRichTextEnabled);
                if (isRichTextEnabled) {
                    richTextArea.setValue(simpleTextArea.getValue());
                }
                else {
                    simpleTextArea.setValue(richTextArea.getValue());
                }
            }
        });

        addReportButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addNewReport();
            }
        });

        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                template = "" + (isRichTextEnabled ? richTextArea.getValue() : simpleTextArea.getValue());
                if (validate()) {
                    saveData();
                    NotificationUtil.showSavedNotification(getWindow());
                }
                else {
                    NotificationUtil.validationErrors(getWindow(), VaadinUtil.getValue("exception.validation.errors.report.template"));
                }
            }
        });
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                initData();
                NotificationUtil.showCancelledNotification(getWindow());
            }
        });
    }

    /**
     * Changes the view of the rich text area to the source code view and vice versa.
     *
     * @param enable <code>TRUE</code> to enable the rich text area
     */
    private void enableRichTextArea(boolean enable) {
        showSourceButton.setCaption(VaadinUtil.getValue(enable ? "dashboard.edit.source" : "dashboard.edit.source.disable"));
        textPanel.removeAllComponents();
        textPanel.addComponent(enable ? richTextArea : simpleTextArea);
        isRichTextEnabled = enable;
    }

    /**
     * Validates the form. The output dashboard HTML held in the rich text area should contain
     * references to existing report configs from the table. If a non-existent report is referenced
     * the method returns <code>FALSE</code>.
     *
     * @return <code>TRUE</code> if the form is valid
     */
    private boolean validate() {
        Set<Integer> templateIds = DashboardUtil.getReportConfigIds(template);
        Set<Integer> configIds = new HashSet<Integer>();
        for (ReportConfig rc : reportConfigs) {
            configIds.add(rc.getId());
        }
        return configIds.containsAll(templateIds);
    }

    /**
     * Initializes the data of the rich text area.
     */
    private void initTextArea() {
        enableRichTextArea(true);
        richTextArea.setValue(template != null ? template : "");
    }

    /**
     * Initializes the data of the report table.
     */
    private void initReportsTable() {
        Set<Integer> reportIds = new HashSet<Integer>();
        Set<Long> cyclicReportIds = new HashSet<Long>();
        if (reportConfigs != null && !reportConfigs.isEmpty()) {
            for (ReportConfig r : reportConfigs) {
                if (r.getReportId() != null) {
                    reportIds.add(r.getReportId());
                }
                else if (r.getCyclicReportId() != null) {
                    cyclicReportIds.add(r.getCyclicReportId());
                }
            }
        }
        reportsTable.removeAllItems();
        if (!reportIds.isEmpty() || !cyclicReportIds.isEmpty()) {
            List<CyclicReportOrder> cyclicReports = org.apertereports.dao.CyclicReportOrderDAO.fetchCyclicReportsByIds(cyclicReportIds.toArray(new Long[cyclicReportIds.size()]));
            Map<Long, CyclicReportOrder> cyclicReportOrders = new HashMap<Long, CyclicReportOrder>();
            for (CyclicReportOrder rep : cyclicReports) {
                cyclicReportOrders.put(rep.getId(), rep);
                reportIds.add(rep.getReport().getId());
            }
            List<ReportTemplate> reports = org.apertereports.dao.ReportTemplateDAO.fetchReports(reportIds.toArray(new Integer[reportIds.size()]));
            Map<Integer, ReportTemplate> reportOrders = new HashMap<Integer, ReportTemplate>();
            for (ReportTemplate rep : reports) {
                reportOrders.put(rep.getId(), rep);
            }
            List<ReportConfig> configList = new ArrayList<ReportConfig>();
            for (final ReportConfig r : reportConfigs) {
                ReportTemplate rep = null;
                String reportType = null;
                if (r.getReportId() != null) {
                    rep = reportOrders.get(r.getReportId());
                    reportType = VaadinUtil.getValue("dashboard.edit.table.type.online");
                }
                else if (r.getCyclicReportId() != null) {
                    CyclicReportOrder cyclicRep = cyclicReportOrders.get(r.getCyclicReportId());
                    if (cyclicRep != null) {
                        rep = reportOrders.get(cyclicRep.getReport().getId());
                        reportType = VaadinUtil.getValue("dashboard.edit.table.type.cyclic");
                        r.setParameters(XmlReportConfigLoader.getInstance().xmlAsParameters(cyclicRep.getParametersXml()));
                    }
                }
                if (rep != null) {
                    configList.add(r);
                    Button details = new Button(VaadinUtil.getValue("dashboard.edit.table.details"));
                    Button delete = new Button(VaadinUtil.getValue("dashboard.edit.table.delete"));
                    reportsTable.addItem(new Object[] {
                            r.getId(),
                            rep.getReportname(),
                            rep.getDescription(),
                            reportType,
                            details,
                            delete
                    }, r.getId());
                    delete.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            reportsTable.removeItem(r.getId());
                            reportConfigs.remove(r);
                        }
                    });
                    final ReportTemplate finalRep = rep;
                    details.addListener(new Button.ClickListener() {
                        @Override
                        public void buttonClick(Button.ClickEvent event) {
                            showDetails(r, finalRep);
                        }
                    });
                }
            }
            reportConfigs = configList;
        }
    }

    /**
     * Adds an empty report config item to the table.
     */
    private void addNewReport() {
        showDetails(null, null);
    }

    /**
     * Displays the details of the selected report config.
     *
     * @param config Selected report config from the table
     * @param report Corresponding report template
     */
    private void showDetails(final ReportConfig config, final ReportTemplate report) {
        reportDetailsWindow = new ModalWindow(VaadinUtil.getValue("dashboard.edit.report.details"),
                new ReportDetailsComponent(report, config, true) {
                    @Override
                    public void onCancel() {
                        removeReportDetailsWindow();
                    }

                    @Override
                    public void onConfirm() {
                        ReportConfig reportConfig = getReportConfig();
                        if (reportConfigs == null) {
                            reportConfigs = new ArrayList<ReportConfig>();
                        }
                        int index = 0;
                        if (reportConfig.getId() != null) {
                            reportConfigs.remove(config);
                            index = reportConfig.getId();
                        }
                        else if (!reportConfigs.isEmpty()) {
                            int prevId = -1;
                            for (Iterator<ReportConfig> it = reportConfigs.iterator(); it.hasNext(); ) {
                                ReportConfig rc = it.next();
                                int currentId = rc.getId();
                                if (currentId - prevId > 1) {
                                    break;
                                }
                                prevId = currentId;
                            }
                            index = prevId + 1;
                        }
                        reportConfig.setId(index);
                        reportConfigs.add(reportConfig);
                        Collections.sort(reportConfigs, new Comparator<ReportConfig>() {
                            @Override
                            public int compare(ReportConfig o1, ReportConfig o2) {
                                return o1.getId().compareTo(o2.getId());
                            }
                        });
                        removeReportDetailsWindow();
                        initReportsTable();
                    }
                });
        reportDetailsWindow.addListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                removeReportDetailsWindow();
            }
        });
        getWindow().addWindow(reportDetailsWindow);
    }

    /**
     * Clears out the report details window.
     */
    private void removeReportDetailsWindow() {
        if (reportDetailsWindow != null) {
            getWindow().removeWindow(reportDetailsWindow);
            reportDetailsWindow = null;
        }
    }
}

