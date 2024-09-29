package cn.oyzh.fx.common.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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

    public static byte[] readBytes(InputStream stream) {
        if (stream != null) {
            try {
                return stream.readAllBytes();
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    public static InputStream toStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }
}
