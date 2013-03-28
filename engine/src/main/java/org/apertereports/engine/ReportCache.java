package org.apertereports.engine;

import java.util.HashMap;

/**
 * A simple thread-safe cache for Jasper reports.
 */
public class ReportCache {
    static HashMap<String, AperteReport> reports;

    /**
     * Gets a cached report. Returns <code>null</code> if not found.
     *
     * @param reportId
     * @return
     */
    public static AperteReport getReport(String reportId) {
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
     * @param report   A {@link net.sf.jasperreports.engine.JasperReport} to cache
     */
    public static void putReport(String reportId, AperteReport report) {
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
    public static void removeReport(String reportId) {
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
            reports = new HashMap<String, AperteReport>();
        }
    }

}
