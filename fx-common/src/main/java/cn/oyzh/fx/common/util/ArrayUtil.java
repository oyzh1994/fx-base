package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

/**
 * 数组工具类
 *
 * @author oyzh
 * @since 2022/5/7
 */
@UtilityClass
public class ArrayUtil {

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
     * 是否为空
     *
     * @param arr 数组
     * @param <T> 数据类型
     * @return 结果
     */
    @Deprecated
    public static <T> boolean isEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    /**
     * 是否不为空
     *
     * @param arr 数组
     * @param <T> 数据类型
     * @return 结果
     */
    @Deprecated
    public static <T> boolean isNotEmpty(T[] arr) {
        return !isEmpty(arr);
    }
}
