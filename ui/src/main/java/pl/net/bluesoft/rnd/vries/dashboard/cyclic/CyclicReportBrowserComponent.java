package pl.net.bluesoft.rnd.vries.dashboard.cyclic;

import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.vries.AbstractLazyLoaderComponent;
import pl.net.bluesoft.rnd.vries.data.CyclicReportOrder;
import pl.net.bluesoft.rnd.vries.generators.ReportStatusColumn;
import pl.net.bluesoft.rnd.vries.util.NotificationUtil;
import pl.net.bluesoft.rnd.vries.util.NotificationUtil.ConfirmListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays a cyclic report order table.
 * Invokes {@link #onItemSelected(pl.net.bluesoft.rnd.vries.data.CyclicReportOrder)} on selected item row.
 * Supports lazy loading.
 */
public abstract class CyclicReportBrowserComponent extends AbstractLazyLoaderComponent {
    private Table reportsTable = new Table();

    private VerticalLayout mainPanel = new VerticalLayout();

    private Map<Integer, CyclicReportOrder> reportOrderMap = new HashMap<Integer, CyclicReportOrder>();

    public CyclicReportBrowserComponent(Map<Integer, CyclicReportOrder> reportOrderMap, boolean lazyLoad) {
        this.reportOrderMap = reportOrderMap;
        initView();
        if (!lazyLoad) {
            initData();
        }
        setCompositionRoot(mainPanel);
    }

    /**
     * Initializes the table view.
     */
    private void initView() {
        reportsTable.addContainerProperty(TM.get("cyclic.report.table.idx"), Integer.class, null);
        reportsTable.addContainerProperty(TM.get("cyclic.report.table.report"), String.class, null);
        reportsTable.addContainerProperty(TM.get("cyclic.report.table.when"), String.class, null);
        reportsTable.addContainerProperty(TM.get("cyclic.report.table.desc"), String.class, null);
        reportsTable.addContainerProperty(TM.get("cyclic.report.table.delete"), Button.class, null);
        reportsTable.addGeneratedColumn(TM.get("cyclic.report.table.status"), new Table.ColumnGenerator() {
            @Override
            public Component generateCell(Table source, Object itemId, Object columnId) {
                return new ReportStatusColumn(source, reportOrderMap.get(itemId).getReportOrder());
            }
        });
        reportsTable.addListener(new ItemClickEvent.ItemClickListener() {
            @Override
            public void itemClick(ItemClickEvent event) {
                onItemSelected(reportOrderMap.get(event.getItemId()));
            }
        });
        reportsTable.setPageLength(10);
        reportsTable.setSelectable(true);
        reportsTable.setImmediate(true);
        reportsTable.setLazyLoading(false);
        reportsTable.setWriteThrough(false);
        reportsTable.setSizeUndefined();
        reportsTable.setWidth(600, UNITS_PIXELS);
        mainPanel.addComponent(reportsTable);
        mainPanel.setExpandRatio(reportsTable, 1.0f);
        mainPanel.setSpacing(false);
        mainPanel.setMargin(false);
    }

    /**
     * Loads data to the table.
     */
    private void initData() {
        reportsTable.removeAllItems();
        for (final CyclicReportOrder reportOrder : reportOrderMap.values()) {
            Button button = new Button(TM.get("cyclic.report.table.delete"));
            reportsTable.addItem(new Object[] {
                    reportOrder.getComponentId(),
                    reportOrder.getReport().getReportname(),
                    reportOrder.getCronSpec(),
                    reportOrder.getDescription(),
                    button
            }, reportOrder.getComponentId());
            button.addListener(new Button.ClickListener() {
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    NotificationUtil.showConfirmWindow(getWindow(), TM.get("cyclic.report.table.delete"),
                            TM.get("cyclic.report.table.delete.areyousure"),
                            new ConfirmListener() {
                                @Override
                                public void onConfirm() {
                                    reportsTable.removeItem(reportOrder.getComponentId());
                                    reportOrderMap.remove(reportOrder.getComponentId());
                                    clearSelection();
                                }

                                @Override
                                public void onCancel() {
                                }
                            });
                }
            });
        }
    }

    public abstract void onItemSelected(CyclicReportOrder selectedItem);

    /**
     * Updates the data of the table.
     *
     * @param reportOrderMap The new cyclic report order map
     */
    public void updateItems(Map<Integer, CyclicReportOrder> reportOrderMap) {
        this.reportOrderMap = reportOrderMap;
        initData();
    }

    /**
     * Gets current cyclic reports list.
     *
     * @return A collection of cyclic reports
     */
    public Collection<CyclicReportOrder> getCyclicReportOrders() {
        return reportOrderMap.values();
    }

    /**
     * Clears table selection.
     */
    public void clearSelection() {
        reportsTable.select(null);
    }

    /**
     * Lazy loads the data.
     *
     * @throws Exception on loading error
     */
    @Override
    public void lazyLoad() throws Exception {
        initData();
    }
}
