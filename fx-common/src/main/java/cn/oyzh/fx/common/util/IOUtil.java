package cn.oyzh.fx.common.util;

/**
 * @author oyzh
 * @since 2024-09-29
 */
public class IOUtil {

    public static void close(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignored) {
            }
        }
    }
}
