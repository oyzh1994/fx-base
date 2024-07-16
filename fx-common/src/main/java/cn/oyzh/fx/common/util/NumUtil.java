package cn.oyzh.fx.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2023/8/29
 */
@UtilityClass
public class NumUtil {

    /**
     * 格式化大小
     *
     * @param size 大小
     * @return 结果
     */
    public static String formatSize(long size) {
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return size / 1024.d + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / 1024.d / 1024 + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024L) {
            return size / 1024.d / 1024 / 1024 + "GB";
        }
        return size / 1024.d / 1024 / 1024 / 1024 + "TB";
    }

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

    /**
     * 限制大小
     *
     * @param val 值
     * @param min 最小值
     * @param max 最大值
     * @return 结果
     */
    public static double limit(double val, double min, double max) {
        val = Math.max(val, min);
        val = Math.min(val, max);
        return val;
    }
}
