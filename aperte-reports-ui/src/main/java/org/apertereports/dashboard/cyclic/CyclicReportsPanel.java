package org.apertereports.dashboard.cyclic;

import com.vaadin.ui.*;
import eu.livotov.tpt.gui.widgets.TPTLazyLoadingLayout;

import org.apertereports.components.HelpLayout;
import org.apertereports.components.SimpleHorizontalLayout;
import org.apertereports.components.HelpWindow.Module;
import org.apertereports.components.HelpWindow.Tab;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;
import org.quartz.CronExpression;
import org.quartz.SchedulerException;
import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.model.CyclicReportOrder;
import org.apertereports.backbone.scheduler.CyclicReportOrderScheduler;
import org.apertereports.model.CyclicReportOrder;

import java.util.*;

/**
 * This component is used to display and configure cyclic reports in the portlet.
 * <p/>A user can define a new cyclic report based on an existing report template.
 * The due time is specified with a cron expression. 
 */
public class CyclicReportsPanel extends CustomComponent {
    /**
     * Cyclic reports map from database.
     */
    private Map<Integer, CyclicReportOrder> reportOrderMap = new HashMap<Integer, CyclicReportOrder>();

    private Panel mainPanel = new Panel();

    /**
     * Report browser table.
     */
    private CyclicReportBrowserComponent browserComponent;
    /**
     * Self-explanatory buttons.
     */
    private Button saveButton = new Button(VaadinUtil.getValue("dashboard.edit.save"));
    private Button cancelButton = new Button(VaadinUtil.getValue("dashboard.edit.cancel"));
    private Button addButton = new Button(VaadinUtil.getValue("cyclic.report.add"));

    private VerticalLayout detailsPanel = new VerticalLayout();
    private VerticalLayout browserPanel = new VerticalLayout();

    public CyclicReportsPanel() {
        initView();
        initData();
        setCompositionRoot(mainPanel);
    }

    /**
     * Initializes the view.
     */
    private void initView() {
        mainPanel.setScrollable(true);
        mainPanel.setStyleName("borderless light");
        mainPanel.setSizeUndefined();

        HorizontalLayout helpLayout = new HelpLayout(Module.DASHBOARD, Tab.CYCLIC_REPORTS);

        mainPanel.addComponent(helpLayout);
        mainPanel.addComponent(browserPanel);
        mainPanel.addComponent(detailsPanel);

        Component panel = new SimpleHorizontalLayout(saveButton, cancelButton, addButton);
        browserPanel.addComponent(panel);

        saveButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                checkAndRegisterCyclicReport();
            }
        });
        cancelButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                onCancel();
            }
        });
        addButton.addListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                clearAll();
            }
        });
        createDetailsPanel(null);
    }

    /**
     * Checks the existing report orders for errors and adds a new report order to database.
     * New reports are rescheduled.
     */
    private void checkAndRegisterCyclicReport() {
        try {
            List<Integer> errorOrders = new ArrayList<Integer>();
            Collection<CyclicReportOrder> reportOrders = browserComponent.getCyclicReportOrders();
            for (CyclicReportOrder cro : reportOrders) {
                if (!CronExpression.isValidExpression(cro.getCronSpec())) {
                    errorOrders.add(cro.getComponentId());
                }
            }
            if (!errorOrders.isEmpty()) {
                StringBuilder sb = new StringBuilder().append(errorOrders.get(0));
                for (int i = 1; i < errorOrders.size(); ++i) {
                    sb.append(", ").append(errorOrders.get(i));
                }
                NotificationUtil.validationErrors(getWindow(), VaadinUtil.getValue("cyclic.report.cron.validation.error")
                        + " " + sb.toString());
                return;
            }

            Collection<CyclicReportOrder> deletedCyclicReports = org.apertereports.dao.CyclicReportOrderDAO.trimAndUpdate(reportOrders);
            for (CyclicReportOrder cro : deletedCyclicReports) {
                CyclicReportOrderScheduler.unscheduleCyclicReportOrder(cro);
            }
            for (CyclicReportOrder cro : reportOrders) {
                CyclicReportOrderScheduler.rescheduleCyclicReportOrder(cro);
            }
            onConfirm();
        }
        catch (SchedulerException e) {
            ExceptionUtils.logSevereException(e);
            NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.gui.error"));
            throw new RuntimeException(e);
        }
    }

    /**
     * Shows a confirm popup.
     */
    public void onConfirm() {
        NotificationUtil.showSavedNotification(getWindow());
    }

    /**
     * Shows a cancelled popup.
     */
    public void onCancel() {
        NotificationUtil.showCancelledNotification(getWindow());
    }

    /**
     * Clears the selection on cancel.
     */
    private void clearAll() {
        browserComponent.clearSelection();
        createDetailsPanel(null);
    }

    /**
     * Creates a cyclic report details panel where user can change the parameters.
     *
     * @param item The source report order
     */
    private void createDetailsPanel(final CyclicReportOrder item) {
        detailsPanel.removeAllComponents();
        detailsPanel.addComponent(new CyclicReportDetailsComponent(item) {
            @Override
            public void onConfirm() {
                CyclicReportOrder cro = getCyclicReportOrder();

                List<CyclicReportOrder> orders = new ArrayList<CyclicReportOrder>();
                if (cro.getComponentId() == null) {
                    orders.add(cro);
                }
                else {
                    reportOrderMap.put(cro.getComponentId(), cro);
                }
                orders.addAll(reportOrderMap.values());

                reportOrderMap.clear();
                int i = 0;
                for (CyclicReportOrder rep : orders) {
                    rep.setComponentId(i++);
                    reportOrderMap.put(rep.getComponentId(), rep);
                }
                browserComponent.updateItems(reportOrderMap);
                clearAll();
            }

            @Override
            public void onCancel() {
                clearAll();
            }
        });
    }

    /**
     * Initializes the data displayed in the view.
     */
    private void initData() {
        int i = 0;
        Collection<CyclicReportOrder> reportOrders = org.apertereports.dao.CyclicReportOrderDAO.fetchAllEnabledCyclicReports();
        for (final CyclicReportOrder reportOrder : reportOrders) {
            reportOrder.setComponentId(i++);
            reportOrderMap.put(reportOrder.getComponentId(), reportOrder);
        }
        if (browserComponent != null && browserPanel.getComponentIndex(browserComponent) != -1) {
            browserPanel.removeComponent(browserComponent);
        }
        browserPanel.addComponentAsFirst(new TPTLazyLoadingLayout(
                browserComponent = new CyclicReportBrowserComponent(reportOrderMap, false) {
                    @Override
                    public void onItemSelected(CyclicReportOrder selectedItem) {
                        createDetailsPanel(selectedItem);
                    }
                }, true));
    }
}
