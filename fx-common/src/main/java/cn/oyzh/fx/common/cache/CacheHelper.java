package cn.oyzh.fx.common.cache;

import lombok.experimental.UtilityClass;

/**
 * 缓存辅助类
 *
 * @author oyzh
 * @since 2024/7/11
 */
@UtilityClass
public class CacheHelper {

    private static final Cache<String, Object> CACHE = CacheUtil.newTimedCache(-1);

    public static <T> T get(String key) {
        return (T) CACHE.get(key);
    }

    public static void remove(String key) {
        CACHE.remove(key);
    }

    public static void set(String key, Object val) {
        CACHE.put(key, val);
    }

    public static void clear() {
        CACHE.clear();
    }
}
