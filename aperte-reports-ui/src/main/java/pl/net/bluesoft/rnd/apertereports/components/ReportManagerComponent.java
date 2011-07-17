package pl.net.bluesoft.rnd.apertereports.components;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import eu.livotov.tpt.i18n.TM;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Module;
import pl.net.bluesoft.rnd.apertereports.components.HelpWindow.Tab;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate;
import pl.net.bluesoft.rnd.apertereports.data.ReportTemplate.Fields;
import pl.net.bluesoft.rnd.apertereports.engine.ReportCache;
import pl.net.bluesoft.rnd.apertereports.generators.CheckBoxColumnGenerator;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil.ConfirmListener;

import java.io.Serializable;
import java.util.*;

/**
 * Displays the report template manager view.
 * A user can add, remove or modify imported JRXML reports.
 */
public class ReportManagerComponent extends Panel implements Serializable {
    private static final long serialVersionUID = 384175771652213854L;

    private final Fields[] visibleCols = new Fields[] {Fields.ACTIVE, Fields.REPORTNAME,
            Fields.DESCRIPTION, Fields.ALLOW_BACKGROUND_ORDER, Fields.ALLOW_ONLINE_DISPLAY};

    private HorizontalLayout bottomLeftCorner = new HorizontalLayout();

    private Table reportTable = new Table();
    private ReportTemplate currentTableSelection;

    private Button reportDeleteButton;
    private Button reportAddButton;

    private EditorForm reportEditForm;

    private IndexedContainer reportTableData;

    private PriorityQueue<ReportTemplate> allReportTemplates;

    public ReportManagerComponent() {
        buildMainLayout();
        initReportAddRemoveButtons();
        initReportTable();
        initFilteringControls();
    }

    /**
     * Builds main layout.
     */
    private void buildMainLayout() {
        setScrollable(true);
        setStyleName("borderless light");
        setSizeUndefined();

        HelpLayout helpLayout = new HelpLayout(Module.MANAGER, Tab.PARAMS);

        addComponent(helpLayout);

        HorizontalLayout splitPanel = new HorizontalLayout();
        splitPanel.setMargin(false);
        splitPanel.setSpacing(true);

        addComponent(splitPanel);

        reportEditForm = new EditorForm() {
            @Override
            public void onSaveReport(boolean wasNew, ReportTemplate rt) {
                if (!wasNew) {
                    for (Iterator<ReportTemplate> it = allReportTemplates.iterator(); it.hasNext(); ) {
                        ReportTemplate reportTemplate = it.next();
                        if (reportTemplate.getId().equals(rt.getId())) {
                            it.remove();
                            break;
                        }
                    }
                }
                allReportTemplates.add(rt);
                refreshContainer(allReportTemplates);
                NotificationUtil.showSavedNotification(getWindow());
            }
        };
        reportEditForm.setWidth(450, UNITS_PIXELS);

        VerticalLayout table = new VerticalLayout();
        table.setSpacing(true);
        table.setSizeUndefined();
        table.addComponent(reportTable);
        table.addComponent(bottomLeftCorner);
        table.setComponentAlignment(bottomLeftCorner, Alignment.BOTTOM_LEFT);
        table.setExpandRatio(reportTable, 1.0f);

        splitPanel.addComponent(table);
        splitPanel.addComponent(reportEditForm);
        splitPanel.setExpandRatio(table, 1.0f);
    }

    /**
     * Adds filtering text boxes at the bottom of the report template table.
     * The fields react immediately with the user input.
     */
    private void initFilteringControls() {
        final List<TextField> fields = new ArrayList<TextField>();
        for (final Fields pn : new Fields[] {Fields.REPORTNAME, Fields.DESCRIPTION}) {
            final TextField sf = new TextField();
            sf.setImmediate(true);
            sf.setWidth("100%");
            sf.setInputPrompt(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("manager.filter." + StringUtils.lowerCase(pn.toString())));
            sf.addListener(new Property.ValueChangeListener() {
                @Override
                public void valueChange(final ValueChangeEvent event) {
                    reportTableData.removeContainerFilters(pn);
                    if (sf.toString().length() > 0 && !pn.equals(sf.toString())) {
                        reportTableData.addContainerFilter(pn, sf.toString(), true, false);
                    }
                }
            });
            bottomLeftCorner.addComponent(sf);
            fields.add(sf);
        }
        Button clearFiltersButton = new RefreshButton(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("global.clearfilters.button"), new ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                for (TextField sf : fields) {
                    sf.setValue("");
                }
                reportTableData.removeAllContainerFilters();
            }
        });
        bottomLeftCorner.addComponent(clearFiltersButton);
        for (Iterator<Component> it = bottomLeftCorner.getComponentIterator(); it.hasNext(); ) {
            Component c = it.next();
            bottomLeftCorner.setComponentAlignment(c, Alignment.MIDDLE_LEFT);
        }
        bottomLeftCorner.setExpandRatio(bottomLeftCorner.getComponent(bottomLeftCorner.getComponentCount() - 1), 1.0f);
    }

    /**
     * Just clears the table selection. Shows the add new report view.
     */
    private void clearTableSelection() {
        reportTable.setValue(currentTableSelection = null);
        editSelected();
    }

    /**
     * Initializes the add (+) and remove (-) buttons at the bottom of the table.
     */
    private void initReportAddRemoveButtons() {
        reportAddButton = new Button("+", new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                if (reportEditForm.isValueChanged()) {
                    NotificationUtil.showValuesChangedWindow(getWindow(), new ConfirmListener() {
                        @Override
                        public void onConfirm() {
                            reloadCurrentReport();
                            clearTableSelection();
                        }

                        @Override
                        public void onCancel() {
                        }
                    });
                }
                else {
                    clearTableSelection();
                }
            }
        });
        reportAddButton.setImmediate(true);
        reportAddButton.setDescription(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("report.table.add"));

        reportDeleteButton = new Button("-", new Button.ClickListener() {
            @Override
            public void buttonClick(final ClickEvent event) {
                Item item = reportTableData.getItem(reportTable.getValue());
                String reportName = (String) item.getItemProperty(Fields.REPORTNAME).getValue();
                String description = (String) item.getItemProperty(Fields.DESCRIPTION).getValue();
                NotificationUtil.showConfirmWindow(getWindow(), pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("report.table.deleteReport.title"),
                        pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("report.table.deleteReport.content").replaceFirst("%s", reportName + " (" + description + ")"),
                        new ConfirmListener() {
                            @Override
                            public void onConfirm() {
                                deleteReport((ReportTemplate) reportTable.getValue());
                            }

                            @Override
                            public void onCancel() {
                            }
                        });
            }
        });
        reportDeleteButton.setImmediate(true);
        reportDeleteButton.setDescription(pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("report.table.delete"));
        reportDeleteButton.setVisible(false);

        bottomLeftCorner.addComponent(reportAddButton);
        bottomLeftCorner.addComponent(reportDeleteButton);
    }

    /**
     * Deletes a report from database.
     *
     * @param rt Report template to delete
     */
    public final void deleteReport(ReportTemplate rt) {
        allReportTemplates.remove(rt);
        ReportTemplateDAO.remove(rt);
        ReportCache.removeReport(rt.getId());
        refreshContainer(allReportTemplates);

        if (reportTableData.size() > 0) {
            reportTable.setValue(reportTableData.getIdByIndex(0));
        }
        reportTable.setVisibleColumns(visibleCols);
    }

    /**
     * Initializes the report template table.
     */
    private void initReportTable() {
        prepareData();
        reportTable.setSizeUndefined();
        reportTable.setContainerDataSource(reportTableData);
        reportTable.setSelectable(true);
        reportTable.setImmediate(true);
        reportTable.setVisibleColumns(visibleCols);
        reportTable.addGeneratedColumn(Fields.ACTIVE, new CheckBoxColumnGenerator());
        reportTable.addGeneratedColumn(Fields.ALLOW_ONLINE_DISPLAY, new CheckBoxColumnGenerator());
        reportTable.addGeneratedColumn(Fields.ALLOW_BACKGROUND_ORDER, new CheckBoxColumnGenerator());
        for (Fields col : visibleCols) {
            reportTable.setColumnHeader(col, pl.net.bluesoft.rnd.apertereports.util.VaadinUtil.getValue("manager.table.column." + StringUtils.lowerCase(col.toString())));
            if (col.equals(Fields.DESCRIPTION) || col.equals(Fields.REPORTNAME)) {
                reportTable.setColumnExpandRatio(col, 1.0f);
                reportTable.setColumnWidth(col, -1);
                reportTable.setColumnAlignment(col, Table.ALIGN_LEFT);
            }
            else {
                reportTable.setColumnExpandRatio(col, 0.0f);
                reportTable.setColumnWidth(col, 20);
                reportTable.setColumnAlignment(col, Table.ALIGN_CENTER);
            }
        }
        reportTable.setWidth(450, UNITS_PIXELS);

        reportTable.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(final ValueChangeEvent event) {
                ReportTemplate rt = (ReportTemplate) reportTable.getValue();
                if ((currentTableSelection != null ? currentTableSelection.getId() : null) == (rt != null ? rt.getId() : null)) {
                    return;
                }
                if (!reportEditForm.isValueChanged()) {
                    editSelected();
                }
                else {
                    NotificationUtil.showValuesChangedWindow(getWindow(), new ConfirmListener() {
                        @Override
                        public void onConfirm() {
                            reloadCurrentReport();
                            editSelected();
                        }

                        @Override
                        public void onCancel() {
                            reportTable.setValue(currentTableSelection);
                        }
                    });
                }
            }
        });

        clearTableSelection();
    }

    /**
     * Reloads the data of the currently selected report.
     */
    private void reloadCurrentReport() {
        if (currentTableSelection != null && currentTableSelection.getId() != null) {
            allReportTemplates.remove(currentTableSelection);
            allReportTemplates.add(ReportTemplateDAO.fetchReport(currentTableSelection.getId()));
            refreshContainer(allReportTemplates);
        }
    }

    /**
     * Prepares a new indexed container for the report templates. The templates are sorted by description.
     */
    private void prepareData() {
        reportTableData = new IndexedContainer();
        reportTableData.addContainerProperty(Fields.ACTIVE, Boolean.class, null);
        reportTableData.addContainerProperty(Fields.ALLOW_BACKGROUND_ORDER, Boolean.class, null);
        reportTableData.addContainerProperty(Fields.ALLOW_ONLINE_DISPLAY, Boolean.class, null);
        reportTableData.addContainerProperty(Fields.DESCRIPTION, String.class, null);
        reportTableData.addContainerProperty(Fields.REPORTNAME, String.class, null);
        reportTableData.addContainerProperty(Fields.FILENAME, String.class, null);
        reportTableData.addContainerProperty(Fields.ID, Integer.class, null);

        Collection<ReportTemplate> list = ReportTemplateDAO.fetchAllReports(false);
        allReportTemplates = new PriorityQueue<ReportTemplate>(list.size()+1, new Comparator<ReportTemplate>() {
            @Override
            public int compare(ReportTemplate o1, ReportTemplate o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        allReportTemplates.addAll(list);

        refreshContainer(allReportTemplates);
    }

    /**
     * Removes all the templates from table's container and re-adds them again. The results shown
     * in the table are sorted by name.
     *
     * @param reportTemplates A collection of report templates
     */
    private void refreshContainer(Collection<ReportTemplate> reportTemplates) {
        reportTableData.removeAllItems();
        for (ReportTemplate reportTemplate : reportTemplates) {
            Item item = reportTableData.addItem(reportTemplate);
            item.getItemProperty(Fields.ACTIVE).setValue(reportTemplate.getActive());
            item.getItemProperty(Fields.ALLOW_BACKGROUND_ORDER).setValue(reportTemplate.getAllowBackgroundOrder());
            item.getItemProperty(Fields.ALLOW_ONLINE_DISPLAY).setValue(reportTemplate.getAllowOnlineDisplay());
            item.getItemProperty(Fields.DESCRIPTION).setValue(reportTemplate.getDescription());
            item.getItemProperty(Fields.REPORTNAME).setValue(reportTemplate.getReportname());
            item.getItemProperty(Fields.FILENAME).setValue(reportTemplate.getFilename());
            item.getItemProperty(Fields.ID).setValue(reportTemplate.getId());
        }
        reportTable.setSortContainerPropertyId(Fields.REPORTNAME);
    }

    /**
     * Loads currently selected report template to the edit form.
     */
    protected final void editSelected() {
        currentTableSelection = (ReportTemplate) reportTable.getValue();
        reportDeleteButton.setVisible(currentTableSelection != null);
        reportEditForm.loadReport(currentTableSelection == null ? new ReportTemplate() : currentTableSelection);
    }
}
