package cn.oyzh.fx.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author oyzh
 * @since 2024-09-24
 */
@UtilityClass
public class ReflectUtil {

    public static <T> T getFieldValue(Field field, Object object) throws SecurityException, IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(object);
    }

    public static void setFieldValue(Field field, Object value, Object object) throws SecurityException, IllegalAccessException {
        field.setAccessible(true);
        field.set(object, value);
    }

    public static void clearFieldValue(Field field , Object object) throws SecurityException, IllegalAccessException {
        field.setAccessible(true);
        field.set(object, null);
    }

    public static Field getField(Class<?> beanClass, String fieldName) throws SecurityException {
        return getField(beanClass, fieldName, false);
    }

    public static Field getField(Class<?> beanClass, String fieldName, boolean withSuperClassFields) throws SecurityException {
        Assert.notNull(beanClass);
        Class<?> searchType = beanClass;
        Field field = null;
        while (searchType != null) {
            try {
                field = searchType.getField(fieldName);
            } catch (NoSuchFieldException ignore) {
            }
            if (null == field) {
                try {
                    field = searchType.getDeclaredField(fieldName);
                } catch (NoSuchFieldException ignore) {
                }
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }
        return field;
    }

    public static Field[] getFields(Class<?> beanClass) throws SecurityException {
        return getFields(beanClass, false);
    }

    public static Field[] getFields(Class<?> beanClass, boolean withSuperClassFields) throws SecurityException {
        Assert.notNull(beanClass);
        Field[] allFields = null;
        Class<?> searchType = beanClass;
        Field[] declaredFields;
        while (searchType != null) {
            declaredFields = searchType.getDeclaredFields();
            if (null == allFields) {
                allFields = declaredFields;
            } else {
                allFields = ArrayUtil.append(allFields, declaredFields);
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }
        return allFields;
    }

    public static Method getMethod(Class<?> beanClass, String methodName, Class<?>... paramTypes) throws SecurityException {
        return getMethod(beanClass, methodName, false, paramTypes);
    }

    public static Method getMethod(Class<?> beanClass, String methodName, boolean withSuperClassFields, Class<?>... paramTypes) throws SecurityException {
        Assert.notNull(beanClass);
        Class<?> searchType = beanClass;
        Method method = null;
        while (searchType != null) {
            try {
                method = searchType.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ignore) {
            }
            if (null == method) {
                try {
                    method = searchType.getDeclaredMethod(methodName, paramTypes);
                } catch (NoSuchMethodException ignore) {
                }
            }
            searchType = withSuperClassFields ? searchType.getSuperclass() : null;
        }
        return method;
    }
}
