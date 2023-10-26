package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

/**
 * 数组工具类
 *
 * @author oyzh
 * @since 2022/5/7
 */
@UtilityClass
public class ArrUtil {

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
     * 获取子数组
     *
     * @param arr   数组
     * @param start 起始位置
     * @param <T>   数据类型
     * @return 子数组
     */
    public static <T> T[] sub(T[] arr, int start) {
        if (arr != null && arr.length > 0) {
            return cn.hutool.core.util.ArrayUtil.sub(arr, start, arr.length);
        }
        return null;
    }

    /**
     * 获取子数组
     *
     * @param arr   数组
     * @param start 起始位置
     * @param end   结束位置
     * @param <T>   数据类型
     * @return 子数组
     */
    public static <T> T[] sub(T[] arr, int start, int end) {
        if (arr != null && arr.length > 0) {
            return cn.hutool.core.util.ArrayUtil.sub(arr, start, end);
        }
        return null;
    }
}
