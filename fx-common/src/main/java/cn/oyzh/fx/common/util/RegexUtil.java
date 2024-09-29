package cn.oyzh.fx.common.util;

import cn.hutool.core.lang.PatternPool;
import lombok.experimental.UtilityClass;

import java.util.regex.Matcher;

/**
 * 数字工具类
 *
 * @author oyzh
 * @since 2023/2/24
 */
@UtilityClass
public class RegexUtil {

    /**
     * 是否数字
     *
     * @param str 字符串
     * @return 结果
     */
    public static boolean isDecimal(String str) {
        if (StringUtil.isBlank(str)) {
            return false;
        }
        try {
            Long.parseLong(str);
            return true;
        } catch (Exception ignored) {
        }
        return false;
    }

    /**
     * 是否整数
     *
     * @param str 字符串
     * @return 结果
     */
    public static boolean isNumber(String str) {
        if (StringUtil.isBlank(str)) {
            return false;
        }
        Matcher m = PatternPool.NUMBERS.matcher(str);
        return m.matches();
    }

    /**
     * 是否ipv4
     *
     * @param str 字符串
     * @return 结果
     */
    public static boolean isIPV4(String str) {
        return StringUtil.isNotBlank(str) && PatternPool.IPV4.matcher(str).matches();
    }
}
