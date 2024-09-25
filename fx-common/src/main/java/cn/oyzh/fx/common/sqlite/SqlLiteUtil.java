package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.common.date.DateUtil;
import cn.oyzh.fx.common.date.LocalDateTimeUtil;
import cn.oyzh.fx.common.date.LocalDateUtil;
import cn.oyzh.fx.common.date.LocalTimeUtil;
import cn.oyzh.fx.common.date.ZonedDateTimeUtil;
import lombok.experimental.UtilityClass;

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

    public static Boolean toBool(Object sqlData) {
        if (sqlData instanceof Boolean) {
            return (Boolean) sqlData;
        }
        if (sqlData instanceof Number n) {
            return n.intValue() == 1;
        }
        if (sqlData instanceof CharSequence n) {
            return StrUtil.equalsAnyIgnoreCase(n, "1", "y", "yes", "true");
        }
        return null;
    }

    public static boolean toBoolVal(Object sqlData) {
        Boolean b = toBool(sqlData);
        return b != null && b;
    }

    public static Byte toByte(Object sqlData) {
        if (sqlData instanceof Byte) {
            return (Byte) sqlData;
        }
        if (sqlData instanceof Number n) {
            return n.byteValue();
        }
        return null;
    }

    public static Byte toByteVal(Object sqlData) {
        Byte b = toByte(sqlData);
        return b == null ? 0 : b;
    }

    public static String toString(Object sqlData) {
        return sqlData == null ? null : sqlData.toString();
    }

    public static StringBuffer toStringBuffer(Object sqlData) {
        return sqlData == null ? null : new StringBuffer(sqlData.toString());
    }

    public static StringBuilder toStringBuilder(Object sqlData) {
        return sqlData == null ? null : new StringBuilder(sqlData.toString());
    }

    public static int toInt(Object sqlData) {
        if (sqlData instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(sqlData.toString());
    }

    public static Integer toIntVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toInt(sqlData);
    }

    public static long toLong(Object sqlData) {
        if (sqlData instanceof Number n) {
            return n.longValue();
        }
        if (sqlData instanceof Date n) {
            return n.getTime();
        }
        return Integer.parseInt(sqlData.toString());
    }

    public static Long toLongVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toLong(sqlData);
    }

    public static double toDouble(Object sqlData) {
        if (sqlData instanceof Number n) {
            return n.doubleValue();
        }
        return Double.parseDouble(sqlData.toString());
    }

    public static Double toDoubleVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toDouble(sqlData);
    }

    public static float toFloat(Object sqlData) {
        if (sqlData instanceof Number n) {
            return n.floatValue();
        }
        return Float.parseFloat(sqlData.toString());
    }

    public static Float toFloatVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toFloat(sqlData);
    }

    public static short toShort(Object sqlData) {
        if (sqlData instanceof Number n) {
            return n.shortValue();
        }
        return Short.parseShort(sqlData.toString());
    }

    public static Short toShortVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toShort(sqlData);
    }

    public static char toChar(Object sqlData) {
        if (sqlData instanceof String n) {
            return n.charAt(0);
        }
        return 0;
    }

    public static Character toCharVal(Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        return toChar(sqlData);
    }

    public static Date toDate(Object sqlData) {
        if (sqlData instanceof Date date) {
            return date;
        }
        if (sqlData instanceof Number n) {
            return DateUtil.of(n);
        }
        return null;
    }

    public static LocalTime toLocalTime(Object sqlData) {
        if (sqlData instanceof Date date) {
            return LocalTimeUtil.of(date);
        }
        return null;
    }

    public static LocalDate toLocalDate(Object sqlData) {
        if (sqlData instanceof Date date) {
            return LocalDateUtil.of(date);
        }
        return null;
    }

    public static LocalDateTime toLocalDateTime(Object sqlData) {
        if (sqlData instanceof Date date) {
            return LocalDateTimeUtil.of(date);
        }
        return null;
    }

    public static ZonedDateTime toZonedDateTime(Object sqlData) {
        if (sqlData instanceof Date date) {
            return ZonedDateTimeUtil.of(date);
        }
        return null;
    }

    public static Object wrapData(Object data) {
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

        return "text";
    }

    public static Object toJavaValue(Class<?> javaType, Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        if (javaType == boolean.class) {
            return toBoolVal(sqlData);
        }
        if (javaType == Boolean.class) {
            return toBool(sqlData);
        }
        if (javaType == byte.class) {
            return toByteVal(sqlData);
        }
        if (javaType == Byte.class) {
            return toByte(sqlData);
        }
        if (javaType == int.class) {
            return toInt(sqlData);
        }
        if (javaType == Integer.class) {
            return toIntVal(sqlData);
        }
        if (javaType == long.class) {
            return toLong(sqlData);
        }
        if (javaType == Long.class) {
            return toLongVal(sqlData);
        }
        if (javaType == double.class) {
            return toDouble(sqlData);
        }
        if (javaType == Double.class) {
            return toDoubleVal(sqlData);
        }
        if (javaType == float.class) {
            return toFloat(sqlData);
        }
        if (javaType == Float.class) {
            return toFloatVal(sqlData);
        }
        if (javaType == short.class) {
            return toShort(sqlData);
        }
        if (javaType == Short.class) {
            return toShortVal(sqlData);
        }
        if (javaType == char.class) {
            return toChar(sqlData);
        }
        if (javaType == Character.class) {
            return toCharVal(sqlData);
        }
        if (javaType == String.class) {
            return toString(sqlData);
        }
        if (javaType == StringBuffer.class) {
            return toStringBuffer(sqlData);
        }
        if (javaType == StringBuilder.class) {
            return toStringBuilder(sqlData);
        }
        if (javaType == Date.class) {
            return toDate(sqlData);
        }
        if (javaType == LocalTime.class) {
            return toLocalTime(sqlData);
        }
        if (javaType == LocalDate.class) {
            return toLocalDate(sqlData);
        }
        if (javaType == LocalDateTime.class) {
            return toLocalDateTime(sqlData);
        }
        if (javaType == ZonedDateTime.class) {
            return toZonedDateTime(sqlData);
        }
        return null;
    }
}
