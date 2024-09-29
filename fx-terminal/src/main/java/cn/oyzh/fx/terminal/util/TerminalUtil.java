package cn.oyzh.fx.terminal.util;

import cn.oyzh.fx.common.util.StringUtil;

/**
 * 终端工具类
 *
 * @author oyzh
 * @since 2023/7/21
 */
public class TerminalUtil {

    /**
     * 获取命令
     *
     * @param input 输入内容
     * @return 命令
     */
    public static String getCommand(String input) {
        String[] arr = split(input);
        if (arr.length == 0) {
            return null;
        }
        return arr[0];
    }

    /**
     * 切割输入
     *
     * @param input 输入内容
     * @return 内容
     */
    public static String[] split(String input) {
        if (input == null) {
            return new String[]{};
        }
        return input.trim().split(" ");
    }

    /**
     * 是否有帮助参数
     *
     * @param input 输入内容
     * @return 结果
     */
    public static boolean hasHelp(String input) {
        return StringUtil.endWith(input, " -?");
    }
}
