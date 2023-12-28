package cn.oyzh.fx.common.util;

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
}
