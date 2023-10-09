package cn.oyzh.fx.common.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Collection;

/**
 * 数组/集合工具类
 *
 * @author oyzh
 * @since 2022/5/7
 */
@UtilityClass
public class Arrays {

    /**
     * 获取首个数据
     *
     * @param collection 集合
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T first(Collection<T> collection) {
        if (collection != null && !collection.isEmpty()) {
            for (T t1 : collection) {
                return t1;
            }
        }
        return null;
    }

    /**
     * 获取最后一个数据
     *
     * @param collection 集合
     * @param <T>        数据类型
     * @return 数据
     */
    public static <T> T last(Collection<T> collection) {
        if (collection != null && !collection.isEmpty()) {
            Object[] arr = collection.toArray();
            return (T) arr[arr.length - 1];
        }
        return null;
    }

    /**
     * 获取首个数据
     *
     * @param arr 数组
     * @param <T> 数据类型
     * @return 数据
     */
    public static <T> T first(T[] arr) {
        if (arr != null && arr.length > 0) {
            return arr[0];
        }
        return null;
    }

    /**
     * 获取最后一个数据
     *
     * @param arr 数组
     * @param <T> 数据类型
     * @return 数据
     */
    public static <T> T last(T[] arr) {
        if (arr != null && arr.length > 0) {
            return arr[arr.length - 1];
        }
        return null;
    }

    /**
     * 获取数据在集合的索引位置
     *
     * @param collection 集合
     * @param obj        对象
     * @return 数据
     */
    public static int index(@NonNull Collection<?> collection, Object obj) {
        int index = 0;
        for (Object o : collection) {
            if (o == obj) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
