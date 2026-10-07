package cn.oyzh.fx.terminal.command;

import cn.oyzh.common.util.ArrayUtil;

import java.util.Collections;
import java.util.List;

/**
 * 终端命令
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class TerminalCommand {

    /**
     * 完整内容
     */
    private String content;

    /**
     * 命令
     */
    private String command;

    /**
     * 参数列表
     */
    private String[] args;

    /**
     * 解析参数
     *
     * @param words 词组
     */
    public void parseArgs(String[] words) {
        if (ArrayUtil.isNotEmpty(words)) {
            this.command = words[0];
            this.args = ArrayUtil.sub(words, 1, words.length);
        }
    }

    /**
     * 获取参数列表
     *
     * @return 参数列表
     */
    public List<String> argsList() {
        if (this.args == null || this.args.length == 0) {
            return Collections.emptyList();
        }
        return List.of(this.args);
    }

    /**
     * 获取命令。
     *
     * @return 命令
     */
    public String getCommand() {
        return command;
    }

    /**
     * 设置命令。
     *
     * @param command 命令
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * 获取参数。
     *
     * @return 参数
     */
    public String[] getArgs() {
        return args;
    }

    /**
     * 设置参数。
     *
     * @param args 参数
     */
    public void setArgs(String[] args) {
        this.args = args;
    }

    /**
     * 获取内容。
     *
     * @return 内容
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置内容。
     *
     * @param content 内容
     */
    public void setContent(String content) {
        this.content = content;
    }
}
