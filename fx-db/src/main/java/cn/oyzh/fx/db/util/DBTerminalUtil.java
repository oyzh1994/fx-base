package cn.oyzh.fx.db.util;

/**
 * 终端工具类
 *
 * @author oyzh
 * @since 2024-12-30
 */
public class DBTerminalUtil {

    /**
     * 修正参数
     *
     * @param arg 参数
     * @return 结果
     */
    public static String fixArg(String arg) {
        arg = arg.trim();
        if (arg.endsWith(";")) {
            arg = arg.substring(0, arg.length() - 1).trim();
        }
        return arg;
    }
}
