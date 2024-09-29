package cn.oyzh.fx.common.sqlite;

import cn.oyzh.fx.common.util.CollectionUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-24
 */
@UtilityClass
public class SqlLiteUtil {

    public static String wrap(String obj) {
        return "\"" + obj + "\"";
    }

    public static Object wrapData(Object data) {
        if (data instanceof byte[]) {
            return data;
        }
        if (data instanceof Byte[]) {
            return data;
        }
        if (data instanceof Number) {
            return data;
        }
        if (data instanceof CharSequence sequence) {
            return "'" + sequence + "'";
        }
        return "'" + data.toString() + "'";
    }

    public static String toSqlType(Class<?> javaType) {
        if (CollectionUtil.contains(List.of(
                Long.class, long.class,
                Integer.class, int.class,
                Short.class, short.class,
                Byte.class, byte.class,
                Boolean.class, boolean.class
        ), javaType)) {
            return "integer";
        }

        if (CollectionUtil.contains(List.of(
                String.class, StringBuilder.class, StringBuilder.class,
                Character.class, char.class
        ), javaType)) {
            return "text";
        }

        if (CollectionUtil.contains(List.of(
                Float.class, float.class,
                Double.class, double.class
        ), javaType)) {
            return "double";
        }

        if (CollectionUtil.contains(List.of(
                Byte[].class, byte[].class
        ), javaType)) {
            return "blob";
        }

        return "text";
    }
}
