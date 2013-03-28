package org.apertereports;

import com.vaadin.ui.Window;
import org.apertereports.backbone.scheduler.CyclicReportScheduler;
import org.apertereports.common.exception.ARRuntimeException;
import org.apertereports.common.users.User;
import org.apertereports.components.CyclicReportsComponent;
import org.apertereports.util.VaadinUtil;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This portlet provides the tabular view of the cyclic reports.
 * <p/>
 * A user can add new, modify, delete and browse existing cyclic reports. Each
 * cyclic report is configured on top of an existing report template. The moment
 * it is generated is specified by a cron expression. Every change in the table
 * should be confirmed by pushing the "Save" button.
 */
public class CyclicReportsApplication extends AbstractReportingApplication<CyclicReportsComponent> {
    
    private static final Logger logger = LoggerFactory.getLogger(CyclicReportsApplication.class);

    /**
     * Initializes the portlet GUI.
     */
    @Override
    public void portletInit() {
        mainPanel = new CyclicReportsComponent();
        mainWindow = new Window(VaadinUtil.getValue("dashboard.edit.cyclicReports"), mainPanel);
    }

    /**
     * This method is used to make sure the cyclic reports scanner and scheduler
     * is initialized at startup.
     */
    @Override
    public void firstApplicationStartup() {
        logger.info("------------------------------------");
        logger.info("FIRST APP STARTUP ------------------");
        logger.info("------------------------------------");
        invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    CyclicReportScheduler.init();
                } catch (SchedulerException ex) {
                    throw new ARRuntimeException(ex);
                }
            }
        });
    }

    @Override
    protected void reinitUserData(User user) {
        mainPanel.initData(user);
    }
}
