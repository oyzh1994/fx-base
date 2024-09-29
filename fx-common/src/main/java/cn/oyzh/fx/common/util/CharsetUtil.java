package cn.oyzh.fx.common.util;

import java.nio.charset.Charset;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class CharsetUtil {

    public static Charset defaultCharset() {
        return Charset.defaultCharset();
    }

    public static String defaultCharsetName() {
        return Charset.defaultCharset().displayName();
    }
}
