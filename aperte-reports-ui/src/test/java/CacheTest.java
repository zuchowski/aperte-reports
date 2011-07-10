import org.junit.Test;
import pl.net.bluesoft.rnd.vries.util.cache.MapCache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CacheTest {

    private String data = "data";
    private long twoSeconds = 2000;
    private long neverCache = 0;
    private long neverExpire = -1;
    private String key = "key";

    @Test
    public void testNeverExpire() throws InterruptedException {
        MapCache cache = new MapCache();

        Thread.sleep(1000);

        Object obj = cache.provideData(key);
        assertNull(obj);

        cache.cacheData(key, neverExpire, data);
        obj = cache.provideData(key);
        assertNotNull(obj);
        assertEquals(obj, obj);

        Thread.sleep(1000);
        obj = cache.provideData(key);
        assertNotNull(obj);
        assertEquals(obj, obj);
    }

    @Test
    public void testNeverCache() {
        MapCache cache = new MapCache();

        cache.cacheData(key, neverCache, data);
        Object obj = cache.provideData(key);
        assertNull(obj);
    }

    @Test
    public void testCacheExpire() throws InterruptedException {
        MapCache cache = new MapCache();

        cache.cacheData(key, twoSeconds, data);

        Thread.sleep(1000);
        Object obj = cache.provideData(key);
        assertNotNull(obj);
        assertEquals(data, obj);

        Thread.sleep(2000);
        obj = cache.provideData(key);
        assertNull(obj);
    }
}
