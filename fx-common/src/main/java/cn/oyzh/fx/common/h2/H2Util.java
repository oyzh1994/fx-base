package cn.oyzh.fx.common.h2;

import cn.hutool.core.collection.CollUtil;
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
        if (CollUtil.contains(List.of(Long.class, long.class), javaType)) {
            return "bigint";
        }
        if (CollUtil.contains(List.of(Integer.class, int.class, Short.class, short.class), javaType)) {
            return "integer";
        }
        if (CollUtil.contains(List.of( Short.class, short.class), javaType)) {
            return "ALLINT";
        }
        if (CollUtil.contains(List.of(Byte.class, byte.class, Boolean.class, boolean.class), javaType)) {
            return "TINYINT";
        }
        if (CollUtil.contains(List.of(String.class, StringBuilder.class, StringBuilder.class, Character.class, char.class), javaType)) {
            return "varchar";
        }
        if (CollUtil.contains(List.of(Float.class, float.class), javaType)) {
            return "real";
        }
        if (CollUtil.contains(List.of(Double.class, double.class), javaType)) {
            return "double";
        }
        if (CollUtil.contains(List.of(Byte[].class, byte[].class), javaType)) {
            return "blob";
        }
        return "varchar";
    }
}
