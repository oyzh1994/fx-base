package cn.oyzh.fx.common.util;// package cn.oyzh.common.util;
//
// import cn.hutool.core.collection.CollUtil;
// import lombok.Generated;
// import lombok.NonNull;
// import lombok.experimental.UtilityClass;
//
// import java.util.Collection;
// import java.util.List;
// import java.util.stream.Collectors;
//
// /**
//  * 集合工具类
//  *
//  * @author oyzh
//  * @since 2023/2/24
//  */
// @UtilityClass
// public class CollectionUtil {
//
//     /**
//      * 是否为空
//      *
//      * @param collection 集合
//      * @return 结果
//      */
//     @Generated
//     public static boolean isEmpty(Collection<?> collection) {
//         return collection == null || collection.isEmpty();
//     }
//
//     /**
//      * 是否不为空
//      *
//      * @param collection 集合
//      * @return 结果
//      */
//     @Generated
//     public static boolean isNotEmpty(Collection<?> collection) {
//         return !isEmpty(collection);
//     }
//
//     /**
//      * 获取首个数据
//      *
//      * @param collection 集合
//      * @param <T>        数据类型
//      * @return 数据
//      */
//     @Generated
//     public static <T> T first(Collection<T> collection) {
//         if (isNotEmpty(collection)) {
//             for (T t1 : collection) {
//                 return t1;
//             }
//         }
//         return null;
//     }
//
//     /**
//      * 获取最后一个数据
//      *
//      * @param collection 集合
//      * @param <T>        数据类型
//      * @return 数据
//      */
//     @Generated
//     public static <T> T last(Collection<T> collection) {
//         if (isNotEmpty(collection)) {
//             Object[] arr = collection.toArray();
//             return (T) arr[arr.length - 1];
//         }
//         return null;
//     }
//
//     /**
//      * 获取数据在集合的索引位置
//      *
//      * @param collection 集合
//      * @param obj        对象
//      * @return 数据
//      */
//     @Generated
//     public static int index(@NonNull Collection<?> collection, Object obj) {
//         int index = 0;
//         for (Object o : collection) {
//             if (o == obj) {
//                 return index;
//             }
//             index++;
//         }
//         return -1;
//     }
//
//     /**
//      * 拼接
//      *
//      * @param collection 集合
//      * @param separator  拼接字符
//      * @return 拼接后的字符串
//      */
//     @Generated
//     public static String join(@NonNull Collection<String> collection, @NonNull String separator) {
//         return collection.parallelStream().collect(Collectors.joining(separator));
//     }
//
//     /**
//      * 获取子集合
//      *
//      * @param list  集合
//      * @param start 开始位置
//      * @param len   获取元素数量
//      * @param <T>   数据类型
//      * @return 子集合
//      */
//     @Generated
//     public static <T> List<T> sub(@NonNull List<T> list, int start, int len) {
//         return CollUtil.sub(list, start, start + len);
//     }
//
//     /**
//      * 获取子集合，从头部开始
//      *
//      * @param list 集合
//      * @param len  获取元素数量
//      * @param <T>  数据类型
//      * @return 子集合
//      */
//     public static <T> List<T> subHead(@NonNull List<T> list, int len) {
//         return CollUtil.sub(list, 0, len);
//     }
//
//     /**
//      * 获取子集合，从尾部开始
//      *
//      * @param list 集合
//      * @param len  获取元素数量
//      * @param <T>  数据类型
//      * @return 子集合
//      */
//     public static <T> List<T> subTail(@NonNull List<T> list, int len) {
//         return  CollUtil.sub(list, list.size() - len, len);
//     }
// }
