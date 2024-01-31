package cn.oyzh.fx.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2023/8/29
 */
@UtilityClass
public class NumUtil {

    // private static int compare(String s1, String s2) {
    //     if (s1.startsWith("-") && !s2.startsWith("-")) {
    //         return -1;
    //     }
    //     if (!s1.startsWith("-") && s2.startsWith("-")) {
    //         return 1;
    //     }
    //     return s1.compareTo(s2);
    // }
    //
    // private static int compare(Number n1, Number n2) {
    //     if (n1 == null || n2 == null) {
    //         return -1;
    //     }
    //     if (n1 instanceof Float d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = BigDecimal.valueOf(n2.floatValue());
    //         return b2.toPlainString().compareTo(b1.toPlainString());
    //     }
    //     if (n1 instanceof Double d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = BigDecimal.valueOf(n2.doubleValue());
    //         return compare(b1.toPlainString(), b2.toPlainString());
    //     }
    //     if (n1 instanceof Integer d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = new BigDecimal(n2.intValue());
    //         return b2.toPlainString().compareTo(b1.toPlainString());
    //     }
    //     if (n1 instanceof Long d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = new BigDecimal(n2.longValue());
    //         return b2.toPlainString().compareTo(b1.toPlainString());
    //     }
    //     if (n1 instanceof Short d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = new BigDecimal(n2.shortValue());
    //         return b2.toPlainString().compareTo(b1.toPlainString());
    //     }
    //     if (n1 instanceof Byte d1) {
    //         BigDecimal b1 = new BigDecimal(d1);
    //         BigDecimal b2 = new BigDecimal(n2.byteValue());
    //         return b2.toPlainString().compareTo(b1.toPlainString());
    //     }
    //     return -1;
    // }

    public static boolean isLT(Number n1, Number n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        return n1.doubleValue() < n2.doubleValue();
    }

    public static boolean isLTEq(Number n1, Number n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        return n1.doubleValue() <= n2.doubleValue();
    }

    public static boolean isEq(Number n1, Number n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        return n1.doubleValue() == n2.doubleValue();
    }

    public static boolean isGT(Number n1, Number n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        return n1.doubleValue() > n2.doubleValue();
    }

    public static boolean isGTEq(Number n1, Number n2) {
        if (n1 == null || n2 == null) {
            return false;
        }
        return n1.doubleValue() >= n2.doubleValue();
    }

    /**
     * byte转bit字符串
     *
     * @param bytes 数据
     * @return bit字符串
     */
    public static String byteToBitStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (byte aByte : bytes) {
            builder.append(byteToBitStr(aByte));
        }
        return builder.toString();
    }

    /**
     * byte转bit字符串
     *
     * @param b 数据
     * @return bit字符串
     */
    public static String byteToBitStr(byte b) {
        StringBuilder builder = new StringBuilder();
        // 将字节与0xFF进行按位与运算，保留最低8位
        int bitValue = b & 0xFF;
        for (int i = 7; i >= 0; i--) {
            builder.append((bitValue & (1 << i)) != 0 ? "1" : "0");
        }
        return builder.toString();
    }

    /**
     * bit字符串转byte
     *
     * @param bitStr bit字符串
     * @return byte数组
     */
    public static byte[] bitStrToByte(String bitStr) {
        if (bitStr == null || bitStr.isEmpty()) {
            return null;
        }
        String[] bits = StrUtil.split(bitStr, 8);
        byte[] bytes = new byte[bits.length];
        int i = 0;
        for (String bit : bits) {
            // 将二进制字符串转换为十进制整数
            int decimalValue = Integer.parseInt(bit, 2);
            // 将十进制整数转换为字节
            byte byteValue = Byte.parseByte(String.valueOf(decimalValue));
            bytes[i++] = byteValue;
        }
        return bytes;
    }
}
