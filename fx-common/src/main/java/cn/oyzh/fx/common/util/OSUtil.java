package cn.oyzh.fx.common.util;

import lombok.experimental.UtilityClass;

/**
 * 系统工具类
 *
 * @author oyzh
 * @since 2023/3/9
 */
@UtilityClass
public class OSUtil {

    /**
     * 获取系统类型
     *
     * @return 系统类型
     */
    public static String getOSType() {
        return System.getProperty("os.name").toUpperCase();
    }

    /**
     * 是否linux
     *
     * @return 结果
     */
    public static boolean isLinux() {
        return getOSType().contains("LINUX");
    }

    /**
     * 是否windows
     *
     * @return 结果
     */
    public static boolean isWindows() {
        return getOSType().contains("WINDOWS");
    }

    /**
     * 是否macos
     *
     * @return 结果
     */
    public static boolean isMacOS() {
        return getOSType().contains("Mac");

    }
}
