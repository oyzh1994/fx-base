// package cn.oyzh.fx.plus.util;
//
// import lombok.NonNull;
// import lombok.experimental.UtilityClass;
//
// import java.util.HashMap;
// import java.util.Map;
//
// /**
//  * 数据管理
//  *
//  * @author oyzh
//  * @since 2023/5/12
//  */
// @UtilityClass
// public class DataManager {
//
//     /**
//      * 数据集合
//      * key: 数据key value: 数据集合
//      */
//     private static final Map<Object, Map<String, Object>> DRAG_BOARD_DATA = new HashMap<>(4);
//
//     /**
//      * 添加
//      *
//      * @param obj  对象
//      * @param key  key
//      * @param data 数据
//      */
//     public static void add(@NonNull Object obj, @NonNull String key, Object data) {
//         if (!DRAG_BOARD_DATA.containsKey(obj)) {
//             DRAG_BOARD_DATA.put(obj, new HashMap<>());
//         }
//         DRAG_BOARD_DATA.get(obj).put(key, data);
//     }
//
//     /**
//      * 移除
//      *
//      * @param obj 对象
//      * @param key key
//      */
//     public static void remove(@NonNull Object obj, @NonNull String key) {
//         if (DRAG_BOARD_DATA.containsKey(obj)) {
//             DRAG_BOARD_DATA.get(obj).remove(key);
//         }
//     }
//
//     /**
//      * 是否存在
//      *
//      * @param obj 对象
//      * @param key key
//      * @return 结果
//      */
//     public static boolean has(Object obj, String key) {
//         if (obj != null && key != null) {
//             return DRAG_BOARD_DATA.containsKey(obj) && DRAG_BOARD_DATA.get(obj).containsKey(key);
//         }
//         return false;
//     }
//
//     /**
//      * 获取数据
//      *
//      * @param obj 对象
//      * @param key key
//      * @return 数据
//      */
//     public static <T> T get(Object obj, String key) {
//         if (obj != null && key != null) {
//             if (DRAG_BOARD_DATA.containsKey(obj)) {
//                 return (T) DRAG_BOARD_DATA.get(obj).get(key);
//             }
//         }
//         return null;
//     }
//
//     /**
//      * 清除数据
//      *
//      * @param obj 对象
//      */
//     public static void clear(Object obj) {
//         if (obj != null) {
//             DRAG_BOARD_DATA.remove(obj);
//         }
//     }
// }
