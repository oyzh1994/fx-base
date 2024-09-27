package cn.oyzh.fx.common.jdbc;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

/**
 * @author oyzh
 * @since 2024-09-24
 */
public class KeyGenerator {

    public static final KeyGenerator INSTANCE = new KeyGenerator();

    public Object generator(String columnType) {
        if (StrUtil.containsAnyIgnoreCase(columnType, "text", "LONGVARCHAR", "NVARCHAR", "NCHAR", "varchar", "char")) {
            return UUID.fastUUID().toString(true);
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "integer", "int", "bigint", "TINYINT", "ALLINT")) {
            return System.currentTimeMillis() + Math.round(Math.random() * 1000);
        }
        return null;
    }

    public static Object generatorKey(String columnType) {
        return INSTANCE.generator(columnType);
    }
}
