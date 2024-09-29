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

    public static boolean equals(String source, String target) {
        if (source != null && target != null) {
            return source.equals(target);
        }
        return false;
    }

    public static boolean equalsIgnoreCase(String source, String target) {
        if (source != null && target != null) {
            return source.equalsIgnoreCase(target);
        }
        return false;
    }

    public static boolean equalsAny(String source, String... strings) {
        if (source != null && strings != null) {
            for (String string : strings) {
                if (source.equals(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean equalsAnyIgnoreCase(String source, String... strings) {
        if (source != null && strings != null) {
            for (String string : strings) {
                if (source.equalsIgnoreCase(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAny(String source, String... strings) {
        if (source != null && strings != null) {
            for (String string : strings) {
                if (source.contains(string)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAnyIgnoreCase(String source, String... strings) {
        if (source != null && strings != null) {
            source = source.toLowerCase();
            for (String string : strings) {
                if (source.contains(string.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean startWithIgnoreCase(String source, String target) {
        if (source != null && target != null) {
            return source.toLowerCase().startsWith(target.toLowerCase());
        }
        return false;
    }

    public static String[] split(String str, int len) {
        if (str == null) {
            return null;
        }
        if (len <= 0) {
            return new String[]{str};
        }
        int mod = str.length() % len;

        int arrLen = mod == 0 ? mod : mod + 1;
        String[] arr = new String[arrLen];

        int aIndex = 0;
        int idx = 0;
        while (true) {
            if (idx + len < str.length()) {
                idx = idx + len;
                arr[aIndex++] = str.substring(idx, idx);
                continue;
            }
            arr[aIndex] = str.substring(idx);
            break;
        }
        return arr;
    }

    public static String blankToDefault(String str, String defaultValue) {
        if (isBlank(str)) {
            return defaultValue;
        }
        return str;
    }

    public static String replace(String src, String search, String replace) {
        if (!isEmpty(src) && !isEmpty(search) && !isEmpty(replace)) {
            return src.replace(search, replace);
        }
        return src;
    }

    public static String delete(String str, int start, int end) {
        StringBuilder builder = new StringBuilder(str);
        builder.delete(start, end);
        return builder.toString();
    }

    public static long count(String s, String lineSeparator) {
        if (s == null || lineSeparator == null) {
            return 0;
        }
        return s.split(lineSeparator).length;
    }


}
