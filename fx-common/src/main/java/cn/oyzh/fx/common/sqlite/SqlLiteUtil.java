package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-24
 */
public class SqlLiteUtil {

    public static String generateUid() {
        return UUID.fastUUID().toString(true);
    }

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

    public static Object wrapData(Object data) {
        if (data instanceof Number) {
            return data;
        }
        if (data instanceof CharSequence sequence) {
            return "'" + sequence + "'";
        }
        return "'" + data.toString() + "'";
    }

    public static String toSqlType(Field field) {
        Class<?> aClass = field.getType();
        if (CollUtil.contains(List.of(
                Long.class, long.class,
                Integer.class, int.class,
                Short.class, short.class,
                Byte.class, byte.class,
                Boolean.class, boolean.class
        ), aClass)) {
            return "integer";
        }

        if (CollUtil.contains(List.of(
                String.class, StringBuilder.class, StringBuilder.class,
                Character.class, char.class
        ), aClass)) {
            return "text";
        }

        if (CollUtil.contains(List.of(Float.class, Double.class, float.class, double.class), aClass)) {
            return "double";
        }
        return "text";
    }

    public static Object toJavaValue(Field field, Object sqlData) {
        if (sqlData == null) {
            return null;
        }
        if (field.getType() == boolean.class) {
            return toBoolVal(sqlData);
        }
        if (field.getType() == Boolean.class) {
            return toBool(sqlData);
        }
        if (field.getType() == byte.class) {
            return toByteVal(sqlData);
        }
        if (field.getType() == Byte.class) {
            return toByte(sqlData);
        }
        if (field.getType() == String.class) {
            return toString(sqlData);
        }
        if (field.getType() == int.class) {
            return toInt(sqlData);
        }
        if (field.getType() == Integer.class) {
            return toIntVal(sqlData);
        }
        if (field.getType() == long.class) {
            return toLong(sqlData);
        }
        if (field.getType() == Long.class) {
            return toLongVal(sqlData);
        }
        if (field.getType() == double.class) {
            return toDouble(sqlData);
        }
        if (field.getType() == Double.class) {
            return toDoubleVal(sqlData);
        }
        if (field.getType() == float.class) {
            return toFloat(sqlData);
        }
        if (field.getType() == Float.class) {
            return toFloatVal(sqlData);
        }
        if (field.getType() == short.class) {
            return toShort(sqlData);
        }
        if (field.getType() == Short.class) {
            return toShortVal(sqlData);
        }
        return null;
    }
}
