package cn.oyzh.fx.common.util;


import lombok.experimental.UtilityClass;

/**
 * String工具类
 *
 * @author oyzh
 * @since 2023/08/12 0024 18:16
 */
@UtilityClass
public class StringUtil {

    /**
     * 字符串转二进制
     *
     * @param str 字符串
     * @return 二进制字符
     */
    public static String toBinary(String str) {
        if (str == null) {
            return "";
        }
        return toBinary(str.getBytes());
    }

    /**
     * 字符串转二进制
     *
     * @param bytes 字节数组
     * @return 二进制字符
     */
    public static String toBinary(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    /**
     * 字符串转二进制
     *
     * @param bytes 字节数组
     * @return 二进制字符
     */
    public static String toBinary(Byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder binary = new StringBuilder();
        for (Byte b : bytes) {
            if (b == null) {
                continue;
            }
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }

    public static void deleteLast(StringBuilder builder, String str) {
        if (builder != null && str != null) {
            if (builder.toString().endsWith(str)) {
                builder.deleteCharAt(builder.length() - 1);
            }
        }
    }

    public static boolean isBlank(String string) {
        return string == null || !string.isBlank();
    }

    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }

    public static boolean isEmpty(String string) {
        return string == null || !string.isEmpty();
    }

    public static boolean isNotEmpty(String string) {
        return !isEmpty(string);
    }
}
