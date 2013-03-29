package org.apertereports.util.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apertereports.common.ARConstants;

import java.util.*;

/**
 * A utility for handling cache expiration. Periodically checks the cache for invalid objects with a single thread.
 */
public final class MapCacheManager {
    private MapCacheManager() {
    }

    /**
     * A cache invalidating thread. It checks the cache for invalid objects and removes them if found.
     * Then it sleeps for a short interval.
     */
    public static class InvalidatingThread extends Thread {
        @Override
        public void run() {
            logger.info(MapCacheManager.class.getName() + ": starting Cache Manager, thread: " + getName());
            while (true) {
                checkCaches(System.currentTimeMillis());
                try {
                    sleep(ARConstants.CACHE_MANAGER_CHECK_INTERVAL);
                }
                catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                    invalidatingThread = null;
                }
            }
        }
    }

    /**
     * A helper bean for control of the cached objects.
     */
    private final static class IntervalControlBean {
        private String objKey;
        private long interval;
        private long cachedTime;

        private IntervalControlBean(String objKey, long interval, long cachedTime) {
            this.objKey = objKey;
            this.interval = interval;
            this.cachedTime = cachedTime;
        }

        public String getObjKey() {
            return objKey;
        }

        public long getInterval() {
            return interval;
        }

        public long getCachedTime() {
            return cachedTime;
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(MapCacheManager.class);
    private static Thread invalidatingThread = null;

    /**
     * Registered caches.
     */
    private static final Map<String, MapCache> cacheMap = new HashMap<String, MapCache>();
    /**
     * Registered objects.
     */
    private static final Map<String, Map<String, IntervalControlBean>> intervalMap = new HashMap<String, Map<String, IntervalControlBean>>();

    /**
     * Checks the registered objects map for invalid objects. If such are found, the cache clears appropriate registered cache maps.'
     * <p/>If the interval of the cached object was set to negative (i.e. -1) on object registration the object is never cleared.
     *
     * @param currentTime Current time of the invocation
     */
    private static void checkCaches(long currentTime) {
        synchronized (MapCacheManager.class) {
            Set<String> nonEmptyCaches = new HashSet<String>();
            for (Iterator<Map.Entry<String, Map<String, IntervalControlBean>>> it = intervalMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Map<String, IntervalControlBean>> entry = it.next();
                String key = entry.getKey();
                Map<String, IntervalControlBean> intervals = entry.getValue();
                if (intervals != null && !intervals.isEmpty()) {
                    for (Iterator<Map.Entry<String, IntervalControlBean>> it2 = intervals.entrySet().iterator(); it2.hasNext(); ) {
                        IntervalControlBean ic = it2.next().getValue();
                        if (ic.getInterval() < 0) { // never expires
                            continue;
                        }
                        long elapsedTime = currentTime - ic.getCachedTime();
                        if (elapsedTime > ic.getInterval()) {
                            MapCache cache = cacheMap.get(key);
                            if (cache != null) {
                                logger.info(MapCacheManager.class.getName() + ": Invalidating object: " + ic.getObjKey()
                                        + " from: " + key + " after: " + elapsedTime);
                                cache.invalidateObject(ic.getObjKey());
                                it2.remove();
                            }
                            else {
                                logger.info(MapCacheManager.class.getName() + ": Removing all intervals after cache mismatch key: " + key);
                                intervals.clear();
                                break;
                            }
                        }
                    }
                    if (!intervals.isEmpty()) {
                        nonEmptyCaches.add(key);
                    }
                    else {
                        it.remove();
                    }
                }
            }

            for (Iterator<Map.Entry<String, MapCache>> it = cacheMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, MapCache> entry = it.next();
                String key = entry.getKey();
                if (!nonEmptyCaches.contains(key)) {
                    MapCache cache = entry.getValue();
                    if (cache != null) {
                        it.remove();
                        cache.clearCache();
                        logger.info(MapCacheManager.class.getName() + ": Cleared cache: " + key);
                    }
                }
            }
        }
    }

    /**
     * Indicates the object was provided by one of the registered caches. If the cache was not registered before
     * it is added to the cache control map.
     *
     * @param cache The {@link MapCache} that provided the object
     * @param key   Object key
     */
    public static void objectProvided(MapCache cache, String key) {
        logger.info(MapCacheManager.class.getName() + ": Object provided: " + key + " from: " + cache.getId());
        synchronized (MapCacheManager.class) {
            registerCache(cache);
        }
    }

    /**
     * Registers an object for periodic interval check.
     *
     * @param cache    The {@link MapCache} that cached the object
     * @param key      Object key
     * @param interval Interval the cache is valid for
     */
    public static void objectCached(MapCache cache, String key, long interval) {
        logger.info(MapCacheManager.class.getName() + ": Object cached: " + key + " in: " + cache.getId() + " for: " + interval);
        synchronized (MapCacheManager.class) {
            registerCache(cache);
            Map<String, IntervalControlBean> intervals = intervalMap.get(cache.getId());
            if (intervals == null) {
                intervals = new HashMap<String, IntervalControlBean>();
            }
            intervals.put(key, new IntervalControlBean(key, interval, System.currentTimeMillis()));
            intervalMap.put(cache.getId(), intervals);
        }
    }

    /**
     * Registers an instance of {@link MapCache} in the cache control map.
     *
     * @param cache The {@link MapCache} to register
     */
    private static void registerCache(MapCache cache) {
        init();
        if (!cacheMap.containsKey(cache.getId())) {
            cacheMap.put(cache.getId(), cache);
        }
    }

    /**
     * Clears all registered caches from all objects and the interval map.
     */
    public static void invalidateAllCaches() {
        logger.info(MapCacheManager.class.getName() + ": Clearing all caches...");
        synchronized (MapCacheManager.class) {
            init();
            for (MapCache cache : cacheMap.values()) {
                cache.clearCache();
                logger.info(MapCacheManager.class.getName() + ": Cleared cache: " + cache.getId());
            }
            cacheMap.clear();
            intervalMap.clear();
        }
    }

    /**
     * Initializes the interval checking thread.
     */
    private static synchronized void init() {
        if (invalidatingThread == null) {
            invalidatingThread = new InvalidatingThread();
            invalidatingThread.start();
        }
    }
}
