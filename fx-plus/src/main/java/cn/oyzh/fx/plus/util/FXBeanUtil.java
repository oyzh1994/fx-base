package cn.oyzh.fx.plus.util;

import cn.oyzh.common.bean.BeanUtil;

import java.lang.reflect.Method;

/**
 * @author oyzh
 * @since 2024-12-05
 */
public class FXBeanUtil {

    public static Method getSetterMethod(Class<?> clazz, String name) {
        return BeanUtil.getSetterMethod(clazz, name, null, true);
    }

    public static Method getGetterMethod(Class<?> clazz, String name) {
        return BeanUtil.getGetterMethod(clazz, name, true);
    }

    public void setValue(Method method, Object bean, String value) {
        try {
            method.setAccessible(true);
            Class<?> parameterType = method.getParameterTypes()[0];
            if (parameterType == String.class) {
                method.invoke(bean, value);
            } else if (parameterType == Long.class || parameterType == long.class) {
                method.invoke(bean, Long.valueOf(value));
            } else if (parameterType == Integer.class || parameterType == int.class) {
                method.invoke(bean, Integer.valueOf(value));
            } else if (parameterType == Double.class || parameterType == double.class) {
                method.invoke(bean, Double.valueOf(value));
            } else if (parameterType == Float.class || parameterType == float.class) {
                method.invoke(bean, Float.valueOf(value));
            } else if (parameterType == Byte.class || parameterType == byte.class) {
                method.invoke(bean, Byte.valueOf(value));
            } else if (parameterType == Short.class || parameterType == short.class) {
                method.invoke(bean, Short.valueOf(value));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
