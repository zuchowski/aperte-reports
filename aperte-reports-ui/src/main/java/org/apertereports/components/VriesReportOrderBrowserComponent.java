package org.apertereports.components;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import org.apache.commons.lang.StringUtils;
import org.apertereports.generators.ReportOrderColumnGenerator;
import org.apertereports.util.VaadinUtil;

import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;
import org.apertereports.model.ReportTemplate;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportTemplate;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

/**
 * Displays a table of report orders. Each row contains a processing or finished report order data.
 * A report order can be generated again or downloaded in a desired output format.
 * The row also contains an information about possible failure of the report generation.
 */
public class VriesReportOrderBrowserComponent extends CustomComponent implements Serializable {
    private static final long serialVersionUID = 384175771652213854L;

    private final Columns[] visibleCols = new Columns[] {Columns.REPORT_NAME, Columns.CREATE_DATE,
            Columns.RESULT, Columns.DETAILS, Columns.ACTION};

    private final VerticalLayout filterBox = new VerticalLayout();
    private Panel mainLayout;
    private final Table reportOrderTable = new Table();

    private IndexedContainer reportTableData;
    private Collection<ReportTemplate> reportTemplates;
    private Collection<ReportOrder> allReportOrders;
    private Select reportOrderSelect;
    private DateField createdAfter;
    private DateField createdBefore;

    public VriesReportOrderBrowserComponent() {
        buildMainLayout();
        setCompositionRoot(mainLayout);
        initFilteringControls();
        initReportTable();
    }

    /**
     * Adds a new report order to the indexed container held by the table.
     *
     * @param container The container
     * @param reportId Input report id
     * @param createdAfter Filtering date after
     * @param createdBefore Filtering date before
     */
    private void addItems(IndexedContainer container, Integer reportId, Calendar createdAfter, Calendar createdBefore) {
        for (ReportOrder reportOrder : allReportOrders) {
            if (reportId != null && !reportId.equals(reportOrder.getReport().getId())) {
                continue;
            }
            if (createdAfter != null && !createdAfter.before(reportOrder.getCreateDate())) {
                continue;
            }
            if (createdBefore != null && !createdBefore.after(reportOrder.getCreateDate())) {
                continue;
            }

            Item item = container.addItem(reportOrder.getId());
            item.getItemProperty(Columns.REPORT_NAME).setValue(reportOrder.getReport().getDescription());
            item.getItemProperty(Columns.CREATE_DATE).setValue(
                    reportOrder.getCreateDate() == null ? null : reportOrder.getCreateDate().getTime());
            item.getItemProperty(Columns.REPORT_ORDER).setValue(reportOrder);
            item.getItemProperty(Columns.RESULT).setValue(reportOrder.getReportStatus());
        }
    }

    /**
     * Builds main container component.
     */
    private void buildMainLayout() {
        mainLayout = new Panel();
        mainLayout.setScrollable(true);
        mainLayout.setStyleName("borderless light");
        mainLayout.setSizeUndefined();
        mainLayout.addComponent(filterBox);
        mainLayout.addComponent(reportOrderTable);
    }

    /**
     * Initializes filtering controls. Each filtering control reacts immediately with the user imput.
     */
    private void initFilteringControls() {
        Form form = new Form();
        reportOrderSelect = new Select(VaadinUtil.getValue("report_order.filter.report"));
        reportOrderSelect.setNullSelectionAllowed(true);
        reportTemplates = org.apertereports.dao.ReportTemplateDAO.fetchAllReports(true);
        for (ReportTemplate reportTemplate : reportTemplates) {
            if (reportTemplate == null || StringUtils.isEmpty(reportTemplate.getDescription())) {
                continue;
            }
            reportOrderSelect.addItem(reportTemplate.getId());
            reportOrderSelect.setItemCaption(reportTemplate.getId(), reportTemplate.getReportname() + " (" + reportTemplate.getDescription() + ")");
        }
        reportOrderSelect.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                filterItems();
            }
        });
        form.addField("report_order_select", reportOrderSelect);

        Calendar threeDaysAgo = Calendar.getInstance();
        threeDaysAgo.add(Calendar.DATE, -3);

        createdAfter = new DateField(VaadinUtil.getValue("report_order.filter.created_after"));
        createdAfter.setResolution(DateField.RESOLUTION_MIN);
        createdAfter.setValue(threeDaysAgo.getTime());
        createdAfter.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                filterItems();
            }
        });
        form.addField("created_after", createdAfter);

        createdBefore = new DateField(VaadinUtil.getValue("report_order.filter.created_before"));
        createdBefore.setResolution(DateField.RESOLUTION_MIN);
        createdBefore.addListener(new Property.ValueChangeListener() {
            @Override
            public void valueChange(ValueChangeEvent event) {
                filterItems();
            }
        });
        form.addField("created_before", createdBefore);

        form.setImmediate(true);
        filterBox.addComponent(form);

        Button refreshButton = new Button(VaadinUtil.getValue("report_order.table.refresh"));
        refreshButton.setImmediate(true);
        refreshButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent event) {
                refresh();
            }
        });

        filterBox.addComponent(refreshButton);
    }

    /**
     * Initializes the table view.
     */
    private void initReportTable() {
        reportTableData = prepareData();
        reportOrderTable.setContainerDataSource(reportTableData);
        reportOrderTable.addGeneratedColumn(Columns.RESULT, new ReportOrderColumnGenerator());
        reportOrderTable.addGeneratedColumn(Columns.DETAILS, new ReportOrderColumnGenerator());
        reportOrderTable.addGeneratedColumn(Columns.CREATE_DATE, new ReportOrderColumnGenerator());
        reportOrderTable.addGeneratedColumn(Columns.ACTION, new ReportOrderColumnGenerator());
        reportOrderTable.setVisibleColumns(visibleCols);
        for (Columns col : visibleCols) {
            reportOrderTable.setColumnHeader(col,
                    VaadinUtil.getValue("report_order.table.column." + StringUtils.lowerCase(col.toString())));
        }
        filterItems();
    }

    /**
     * Initializes an indexed container for report orders.
     *
     * @return A new indexed container
     */
    private IndexedContainer prepareData() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty(Columns.REPORT_NAME, String.class, null);
        container.addContainerProperty(Columns.CREATE_DATE, Date.class, null);
        container.addContainerProperty(Columns.REPORT_ORDER, ReportOrder.class, null);
        container.addContainerProperty(Columns.RESULT, Status.class, null);
        allReportOrders = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders();
        return container;
    }

    /**
     * Filters items in the table.
     */
    protected void filterItems() {
        Integer reportId = (Integer) reportOrderSelect.getValue();
        Calendar createdBeforeCal = null;
        if (createdBefore.getValue() != null) {
            createdBeforeCal = Calendar.getInstance();
            createdBeforeCal.setTime((Date) createdBefore.getValue());
        }
        Calendar createdAfterCal = null;
        if (createdAfter.getValue() != null) {
            createdAfterCal = Calendar.getInstance();
            createdAfterCal.setTime((Date) createdAfter.getValue());
        }

        reportTableData.removeAllItems();
        addItems(reportTableData, reportId, createdAfterCal, createdBeforeCal);
        reportOrderTable.setVisibleColumns(visibleCols);
    }

    /**
     * Loads all report orders from database and shows them in the table view.
     */
    protected void refresh() {
        allReportOrders = org.apertereports.dao.ReportOrderDAO.fetchAllReportOrders();
        filterItems();
    }

    /**
     * Table columns.
     */
    public enum Columns {
        REPORT_NAME, CREATE_DATE, DETAILS, RESULT, ACTION, REPORT_ORDER
    }

}
