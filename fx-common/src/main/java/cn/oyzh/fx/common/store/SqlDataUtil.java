package cn.oyzh.fx.common.store;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

/**
 * @author oyzh
 * @since 2024-09-24
 */
public class SqlDataUtil {

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

    public static Object wrapData(Object data) {
        if (data instanceof Number) {
            return data;
        }
        if (data instanceof CharSequence sequence) {
            return "'" + sequence + "'";
        }
        return "'" + data.toString() + "'";
    }
}
