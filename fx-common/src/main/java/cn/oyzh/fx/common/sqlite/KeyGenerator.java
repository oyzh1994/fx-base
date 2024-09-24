package cn.oyzh.fx.common.sqlite;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;

/**
 * @author oyzh
 * @since 2024-09-24
 */
public class KeyGenerator {

    public static final KeyGenerator INSTANCE = new KeyGenerator();

    public Object generator(String columnType) {
        if (StrUtil.equalsAnyIgnoreCase(columnType, "text", "char")) {
            return UUID.fastUUID().toString(true);
        }
        if (StrUtil.equalsAnyIgnoreCase(columnType, "integer")) {
            return System.currentTimeMillis() + Math.round(Math.random() * 1000);
        }
        return null;
    }

    public static Object generatorKey(String columnType) {
        return INSTANCE.generator(columnType);
    }
}
