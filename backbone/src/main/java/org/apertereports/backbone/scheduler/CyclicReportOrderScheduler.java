package org.apertereports.backbone.scheduler;

import java.text.ParseException;
import java.util.Collection;
import java.util.logging.Logger;

import org.apertereports.common.utils.ExceptionUtils;
import org.apertereports.model.CyclicReportOrder;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.calendar.HolidayCalendar;

/**
 * A utility class that can schedule cyclic report generation with a configured {@link org.quartz.CronExpression}.
 * <p/>
 * <p> The <tt>scheduleCyclicReportOrder</tt> method simply takes the {@link CyclicReportOrder} object and
 * use the cron expression it contains to schedule a {@link CyclicReportOrderJob} task.
 * <p/>
 * <p> The scheduler starts immediately after application starts and invokes <tt>scanForCyclicReportOrders</tt>.
 * The method unschedules all jobs previously scheduled, fetches the cyclic reports form database and schedules
 * them again. This way the previous jobs named after the instance id are removed. The database identifier
 * can vary over time.
 */
public class CyclicReportOrderScheduler {
    private static final Logger logger = Logger.getLogger(CyclicReportOrderScheduler.class.getName());

    /**
     * A default calendar name.
     */
    private static final String calendarName = "cyclicCalendar";

    /**
     * Scan for cyclic reports once and for all.
     */
    static {
        scanForCyclicReportOrders();
    }

    /**
     * The Quartz scheduler.
     */
    static Scheduler sched;

    /**
     * Reschedule a given report order.
     *
     * @param order A report order
     * @throws SchedulerException on Quartz error
     */
    public static void rescheduleCyclicReportOrder(CyclicReportOrder order) throws SchedulerException {
        unscheduleCyclicReportOrder(order);
        scheduleCyclicReportOrder(order);
    }

    /**
     * Schedules a {@link CyclicReportOrderJob} for a given cyclic report order. The {@link CronTrigger} is initialized
     * with a cron specification taken from the instance.
     *
     * @param order The instance of cyclic report order
     * @throws SchedulerException on Quartz error
     */
    public static void scheduleCyclicReportOrder(CyclicReportOrder order) throws SchedulerException {
        init();

        CronTrigger trigger;
        try {
            trigger = new CronTrigger(order.getId().toString(), CyclicReportOrder.class.toString(), order.getCronSpec());
            trigger.setCalendarName(calendarName);
        }
        catch (ParseException e) {
            throw new SchedulerException(e);
        }
        JobDetail jobDetail = new JobDetail(order.getId().toString(), CyclicReportOrder.class.toString(),
                CyclicReportOrderJob.class);

        sched.scheduleJob(jobDetail, trigger);
        logger.info("New job scheduled: " + jobDetail.getName() + " start at: " + trigger.getStartTime());
    }

    /**
     * Unschedules previously scheduled cyclic report order.
     *
     * @param order An instance of cyclic report order to unschedule
     * @throws SchedulerException on Quartz error
     */
    public static void unscheduleCyclicReportOrder(CyclicReportOrder order) throws SchedulerException {
        init();
        logger.info("Deleting job by name:" + order.getId().toString());
        sched.deleteJob(order.getId().toString(), CyclicReportOrder.class.toString());
    }

    /**
     * Initializes the scheduler.
     *
     * @throws SchedulerException on Quartz error
     */
    private static void init() throws SchedulerException {
        if (sched == null) {
            sched = StdSchedulerFactory.getDefaultScheduler();
            sched.addCalendar(calendarName, new HolidayCalendar(), true, true);
            sched.start();
            logger.info("Cyclic report scheduler initialized");
        }
    }

    /**
     * Loads all cyclic reports from database and schedules them in a Quartz scheduler.
     * The method unschedules all jobs queued in the scheduler prior to scheduling.
     * This prevents from a schedule leak when a report order is deleted
     * from database and created again afterwards (the id changes).
     */
    public static void scanForCyclicReportOrders() {
        logger.info("Scanning for cyclic reports");
        try {
            init();
            String[] jobNames = sched.getJobNames(CyclicReportOrder.class.toString());
            for (String jobName : jobNames) {
                sched.deleteJob(jobName, CyclicReportOrder.class.toString());
            }
            Collection<CyclicReportOrder> cROs = org.apertereports.dao.CyclicReportOrderDAO.fetchAllCyclicReportOrders();
            for (CyclicReportOrder cRO : cROs) {
                scheduleCyclicReportOrder(cRO);
            }
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
        }
    }
}
