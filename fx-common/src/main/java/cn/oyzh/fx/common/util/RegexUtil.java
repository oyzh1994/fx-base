package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

/**
 * 数字工具类
 *
 * @author oyzh
 * @since 2023/2/24
 */
@UtilityClass
public class RegexUtil {

    /**
     * IPv4的正则表达式
     */
    public static String IPV4_REGEX = "(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$";

    /**
     * IPv4的Pattern对象
     */
    public static Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    /**
     * 匹配整数的正则表达式
     */
    public static String NUMBER_REGEX = "-?\\d+";

    /**
     * 匹配整数的Pattern对象
     */
    public static Pattern NUMBER_PATTERN = Pattern.compile(NUMBER_REGEX);

    /**
     * 匹配小数的正则表达式
     */
    public static String DECIMAL_REGEX = "-?\\d+(\\.\\d*)?";

    /**
     * 匹配小数的Pattern对象
     */
    public static   Pattern DECIMAL_PATTERN = Pattern.compile(DECIMAL_REGEX);

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
        return DECIMAL_PATTERN.matcher(str).matches();
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
        return NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 是否ipv4
     *
     * @param str 字符串
     * @return 结果
     */
    public static boolean isIPV4(String str) {
        if (StringUtil.isBlank(str)) {
            return false;
        }
        return IPV4_PATTERN.matcher(str).matches();
    }
}
