package pl.net.bluesoft.rnd.apertereports.util.cache;

import pl.net.bluesoft.rnd.apertereports.util.ExceptionUtil;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * A simple map cache. Each instance has its own generated id. The id is used to identify the cache within the
 * cache-monitoring thread in {@link MapCacheManager}.
 */
public class MapCache {
    private String id;

    private Map<String, Object> cache = new HashMap<String, Object>();

    /**
     * Constructs a new instance with a generated id.
     */
    public MapCache() {
        try {
            id = digest("SHA-1", "" + new Random().nextInt(), "" + System.currentTimeMillis(), "" + (new Random().nextInt() << 13), toString());
        }
        catch (NoSuchAlgorithmException e) {
            ExceptionUtil.logSevereException(e);
            id = new Random().nextInt() + "" + System.currentTimeMillis() + (new Random().nextInt() << 13) + toString(); // unsafe
        }
    }

    /**
     * Caches the data internally and registers the cached object in the {@link MapCacheManager}.
     * Each object is identified with a key and an interval of time the cache is valid.
     * <p/>The interval of value <code>0</code> means the object should never be cached.
     * <p/>On the other hand, the interval of value <code>-1</code> means the object is to be cached forever.
     *
     * @param key      Object key
     * @param interval Interval the cache is valid
     * @param data     Cached object
     */
    public void cacheData(String key, long interval, Object data) {
        if (interval == 0) { // never cache
            return;
        }
        synchronized (cache) {
            cache.put(key, data);
        }
        MapCacheManager.objectCached(this, key, interval);
    }

    /**
     * Provides the object identified by key.
     *
     * @param key Object key
     * @return Cached object
     */
    public Object provideData(String key) {
        Object data = cache.get(key);
        MapCacheManager.objectProvided(this, key);
        return data;
    }

    protected void clearCache() {
        synchronized (cache) {
            cache.clear();
        }
    }

    protected void invalidateObject(String key) {
        synchronized (cache) {
            cache.remove(key);
        }
    }

    public String getId() {
        return id;
    }

    private String digest(String algorithm, String... chunks) throws NoSuchAlgorithmException {
        MessageDigest m = MessageDigest.getInstance(algorithm);
        for (String add : chunks) {
            if (add == null) {
                continue;
            }
            m.update(add.getBytes());
        }

        String result = new BigInteger(1, m.digest()).toString(16);
        while (result.length() < 32) {
            result = "0" + result;
        }
        return result.toUpperCase();
    }
}
