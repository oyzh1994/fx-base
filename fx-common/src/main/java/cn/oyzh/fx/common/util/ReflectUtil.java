package cn.oyzh.fx.common.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.ArrayUtil;
import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

/**
 * @author oyzh
 * @since 2024-09-24
 */
@UtilityClass
public class ReflectUtil {

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
}
