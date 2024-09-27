package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.date.DateUtil;
import cn.oyzh.fx.common.date.LocalDateTimeUtil;
import cn.oyzh.fx.common.date.LocalDateUtil;
import cn.oyzh.fx.common.date.LocalTimeUtil;
import cn.oyzh.fx.common.date.ZonedDateTimeUtil;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Date;
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
        if (data instanceof byte[] bytes) {
            return data;
        }
        if (data instanceof Byte[] bytes) {
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
        if (CollUtil.contains(List.of(
                Long.class, long.class,
                Integer.class, int.class,
                Short.class, short.class,
                Byte.class, byte.class,
                Boolean.class, boolean.class
        ), javaType)) {
            return "integer";
        }

        if (CollUtil.contains(List.of(
                String.class, StringBuilder.class, StringBuilder.class,
                Character.class, char.class
        ), javaType)) {
            return "text";
        }

        if (CollUtil.contains(List.of(
                Float.class, float.class,
                Double.class, double.class
        ), javaType)) {
            return "double";
        }

        if (CollUtil.contains(List.of(
                Byte[].class, byte[].class
        ), javaType)) {
            return "blob";
        }

        return "text";
    }




}
