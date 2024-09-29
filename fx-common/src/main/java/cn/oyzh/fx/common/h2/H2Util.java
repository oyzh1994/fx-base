package cn.oyzh.fx.common.h2;

import cn.oyzh.fx.common.util.CollectionUtil;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-24
 */
@UtilityClass
public class H2Util {

    public static String wrap(String obj) {
        return "`" + obj.toUpperCase() + "`";
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
        if (CollectionUtil.contains(List.of(Long.class, long.class), javaType)) {
            return "bigint";
        }
        if (CollectionUtil.contains(List.of(Integer.class, int.class, Short.class, short.class), javaType)) {
            return "integer";
        }
        if (CollectionUtil.contains(List.of(Short.class, short.class), javaType)) {
            return "ALLINT";
        }
        if (CollectionUtil.contains(List.of(Byte.class, byte.class, Boolean.class, boolean.class), javaType)) {
            return "TINYINT";
        }
        if (CollectionUtil.contains(List.of(String.class, StringBuilder.class, StringBuilder.class, Character.class, char.class), javaType)) {
            return "varchar";
        }
        if (CollectionUtil.contains(List.of(Float.class, float.class), javaType)) {
            return "real";
        }
        if (CollectionUtil.contains(List.of(Double.class, double.class), javaType)) {
            return "double";
        }
        if (CollectionUtil.contains(List.of(Byte[].class, byte[].class), javaType)) {
            return "blob";
        }
        return "varchar";
    }

    public static boolean checkSqlType(String sqlType, String typeName) {
        if (sqlType.equalsIgnoreCase(typeName)) {
            return true;
        }
        if (sqlType.equalsIgnoreCase("varchar") && typeName.equalsIgnoreCase("CHARACTER VARYING")) {
            return true;
        }
        if (sqlType.equalsIgnoreCase("blob") && typeName.equalsIgnoreCase("BINARY LARGE OBJECT")) {
            return true;
        }
        if (sqlType.equalsIgnoreCase("double") && typeName.equalsIgnoreCase("DOUBLE PRECISION")) {
            return true;
        }
        return false;
    }
}
