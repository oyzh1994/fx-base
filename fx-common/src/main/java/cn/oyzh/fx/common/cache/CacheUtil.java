package cn.oyzh.fx.common.cache;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class CacheUtil {

    public static <K, V> WeakCache<K, V> newWeakCache() {
        return new WeakCache<>();
    }

    public static <K, V> TimedCache<K, V> newTimedCache(long timeout) {
        return new TimedCache<>(timeout);
    }

}
