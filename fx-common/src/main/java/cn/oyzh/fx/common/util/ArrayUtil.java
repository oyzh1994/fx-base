package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

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

    public static <T> T[] append(T[] arr1, T[] arr2) {
        int len1 = arr1.length;
        int len2 = arr2.length;
        T[] result = Arrays.copyOf(arr1, len1 + len2);
        System.arraycopy(arr2, 0, result, len1, len2);
        return result;
    }

    public <T> boolean isEmpty(T[] arr) {
        return arr == null || arr.length == 0;
    }

    public <T> boolean isNotEmpty(T[] arr) {
        return !isEmpty(arr);
    }

    public static <T> String toString(T[] arr) {
        if (isEmpty(arr)) {
            return "";
        }
        return Arrays.toString(arr);
    }

    public static <T> boolean contains(T[] arr, T obj) {
        if (arr != null && arr.length > 0 && obj != null) {
            for (T t : arr) {
                if (t.equals(obj)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> T[] sub(T[] arr, int start, int end) {
        if (arr == null || start < 0 || end < start || arr.length <= end) {
            return arr;
        }
        return Arrays.copyOfRange(arr, start, end);
    }

    // /**
    //  * 获取子数组
    //  *
    //  * @param arr   数组
    //  * @param start 起始位置
    //  * @param <T>   数据类型
    //  * @return 子数组
    //  */
    // public static <T> T[] sub(T[] arr, int start) {
    //     if (arr != null && arr.length > 0) {
    //         return cn.hutool.core.util.ArrayUtil.sub(arr, start, arr.length);
    //     }
    //     return null;
    // }

    // /**
    //  * 获取子数组
    //  *
    //  * @param arr   数组
    //  * @param start 起始位置
    //  * @param end   结束位置
    //  * @param <T>   数据类型
    //  * @return 子数组
    //  */
    // public static <T> T[] sub(T[] arr, int start, int end) {
    //     if (arr != null && arr.length > 0) {
    //         return cn.hutool.core.util.ArrayUtil.sub(arr, start, end);
    //     }
    //     return null;
    // }
}
