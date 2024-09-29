package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

    public static boolean isEmpty(Map<?, ?> map) {
        return map != null && !map.isEmpty();
    }

    public static boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <T> List<List<T>> split(Collection<T> collection, int limit) {
        if (collection == null) {
            return Collections.emptyList();
        }
        if (collection.size() <= limit) {
            return Collections.singletonList(new ArrayList<>(collection));
        }
        List<List<T>> result = new ArrayList<>();
        for (int i = 0; i < collection.size(); i += limit) {
            int end = Math.min(i + limit, collection.size());
            result.add(new ArrayList<>(new ArrayList<>(collection).subList(i, end)));
        }
        return result;
    }

    public static boolean contains(List<?> list, Object t) {
        return list != null && list.contains(t);
    }
}
