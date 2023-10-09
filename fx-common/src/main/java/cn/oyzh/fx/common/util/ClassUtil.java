package cn.oyzh.fx.common.util;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 类工具类
 *
 * @author oyzh
 * @since 2023/05/18
 */
@UtilityClass
public class ClassUtil {

    /**
     * 获取类的所有接口
     *
     * @param clazz 类
     * @return 接口列表
     */
    public static List<Class<?>> getInterfaces(@NonNull Class<?> clazz) {
        Set<Class<?>> interfaces = new HashSet<>();
        do {
            getInterfaces(clazz, interfaces);
            clazz = clazz.getSuperclass();
        } while (clazz != Object.class);
        return new ArrayList<>(interfaces);
    }

    /**
     * 获取接口
     *
     * @param clazz      类
     * @param interfaces 接口列表
     */
    private static void getInterfaces(Class<?> clazz, Set<Class<?>> interfaces) {
        for (Class<?> aClass : clazz.getInterfaces()) {
            interfaces.add(aClass);
            getInterfaces(aClass, interfaces);
        }
    }
}
