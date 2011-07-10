package pl.net.bluesoft.rnd.apertereports.engine;

import net.sf.jasperreports.engine.JasperReport;

import java.util.HashMap;

/**
 * A simple thread-safe cache for Jasper reports.
 */
public class ReportCache {
    static HashMap<Integer, JasperReport> reports;

    /**
     * Gets a cached report. Returns <code>null</code> if not found.
     *
     * @param reportId
     * @return
     */
    public static JasperReport getReport(Integer reportId) {
        if (reportId == null) {
            return null;
        }
        init();
        synchronized (ReportCache.class) {
            return reports.get(reportId);
        }
    }

    /**
     * Puts a report in the cache.
     *
     * @param reportId Report cache id
     * @param report   A {@link JasperReport} to cache
     */
    public static void putReport(Integer reportId, JasperReport report) {
        if (reportId == null) {
            return;
        }
        init();
        synchronized (ReportCache.class) {
            reports.put(reportId, report);
        }
    }

    /**
     * Removes a report from the cache.
     *
     * @param reportId The report cache id
     */
    public static void removeReport(Integer reportId) {
        if (reportId == null) {
            return;
        }
        init();
        synchronized (ReportCache.class) {
            reports.remove(reportId);
        }
    }

    /**
     * Initializes the cache.
     */
    synchronized private static void init() {
        if (reports == null) {
            reports = new HashMap<Integer, JasperReport>();
        }
    }

}
