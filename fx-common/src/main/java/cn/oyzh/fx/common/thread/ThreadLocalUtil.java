package cn.oyzh.fx.common.thread;

import lombok.experimental.UtilityClass;

import java.util.HashMap;

/**
 * 线程工具类
 *
 * @author oyzh
 * @since 2023/1/3
 */
@UtilityClass
public class ThreadLocalUtil {

    private static final ThreadLocal<Object> LOCAL = new ThreadLocal<>();

    public static void setVal(String key, Object obj) {
        ThreadLocalMap localMap;
        Object object = LOCAL.get();
        if (!(object instanceof ThreadLocalMap)) {
            localMap = new ThreadLocalMap();
            LOCAL.set(localMap);
        } else {
            localMap = (ThreadLocalMap) object;
        }
        localMap.put(key, obj);
    }

    public static <T> T getVal(String key) {
        Object object = LOCAL.get();
        if (object instanceof ThreadLocalMap localMap) {
            return (T) localMap.get(key);
        }
        return null;
    }

    private class ThreadLocalMap extends HashMap<String, Object> {

    }
}
