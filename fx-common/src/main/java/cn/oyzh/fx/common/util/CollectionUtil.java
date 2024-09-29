package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;
import org.apache.poi.ss.formula.functions.T;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author oyzh
 * @since 2024/7/1
 */
@UtilityClass
public class CollectionUtil {

    public static <T> T indexOf(Collection<T> collection, int index) {
        if (collection != null && !collection.isEmpty() && index > 0 && index < collection.size()) {
            int i = 0;
            for (T t : collection) {
                if (i++ == index) {
                    return t;
                }
            }
        }
        return null;
    }

    public static <T> T get(List<T> list, int index) {
        if (list != null && !list.isEmpty() && index > 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    public static boolean isEmpty(Collection<?> list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return !isEmpty(collection);
    }

    public static boolean isEmpty(Map<?,?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?,?> map) {
        return !isEmpty(map);
    }
}
