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

    /**
     * 检查边距
     *
     * @param start       开始
     * @param end         结束
     * @param targetStart 目标开始
     * @param targetEnd   目标结束
     * @return 是否交叉
     */
    public static boolean checkBound(double start, double end, double targetStart, double targetEnd) {
        // 单点
        if (targetStart == targetEnd && start == targetStart) {
            return true;
        }
        // 相同
        if (start == targetStart && end == targetEnd) {
            return true;
        }
        // 包含
        if (start < targetStart && end > targetEnd) {
            return true;
        }
        // 被包含
        if (start > targetStart && end < targetEnd) {
            return true;
        }
        // 左交叉
        if (start > targetStart && end >= targetEnd && start < targetEnd) {
            return true;
        }
        // 右交叉
        if (start <= targetStart && end < targetEnd && end > targetStart) {
            return true;
        }
        return false;
    }
}
