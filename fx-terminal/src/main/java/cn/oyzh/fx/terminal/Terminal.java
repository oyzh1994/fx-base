package cn.oyzh.fx.terminal;

import cn.oyzh.fx.terminal.complete.TerminalCompleteHandler;
import cn.oyzh.fx.terminal.help.TerminalHelpHandler;
import cn.oyzh.fx.terminal.histroy.TerminalHistoryHandler;
import cn.oyzh.fx.terminal.key.TerminalKeyHandler;
import cn.oyzh.fx.terminal.mouse.TerminalMouseHandler;

/**
 * 命令行文本域
 *
 * @author oyzh
 * @since 2023/05/28
 */
public interface Terminal {

    /**
     * 清除内容
     */
    void clear();

    /**
     * 获取输入内容
     *
     * @return 输入内容
     */
    String getInput();

    /**
     * 清除输入内容
     */
    void clearInput();

    /**
     * 检查边界
     *
     * @return 是否在边界内
     */
    boolean checkNop();

    /**
     * 刷新边界
     */
    void flushNOP();

    /**
     * 获取不可操作边界
     *
     * @return 不可操作边界
     */
    int getNOP();

    /**
     * 覆盖当前输入内容
     *
     * @param input 新内容
     */
    void coverInput(String input);

    /**
     * 输出
     *
     * @param output 输出内容
     */
    void output(String output);

    /**
     * 输出行
     *
     * @param output 输出内容
     */
    void outputLine(String output);

    /**
     * 输出换行符
     */
    default void outputLineBreak() {
        if (!this.content().endsWith("\n")) {
            this.output("\n");
        }
    }

    /**
     * 输出，提示符模式
     *
     * @param output 输出内容
     */
    void outputByPrompt(String output);

    /**
     * 输出，追加模式
     *
     * @param output 输出内容
     */
    void outputByAppend(String output);

    /**
     * 输出，提示符+追加模式
     *
     * @param output 输出内容
     */
    void appendByPrompt(String output);

    /**
     * 启用输入
     */
    void enableInput();

    /**
     * 仅有输入
     */
    void disableInput();

    /**
     * 收到指令事件
     *
     * @param input 输入
     * @throws RuntimeException 异常
     */
    void onCommand(String input) throws Exception;

    /**
     * 获取帮助实现
     *
     * @return 补全实现
     */
    TerminalHelpHandler helpHandler();

    /**
     * 设置帮助实现
     *
     * @param handler 帮助实现
     */
    void helpHandler(TerminalHelpHandler handler);

    /**
     * 获取补全实现
     *
     * @return 补全实现
     */
    TerminalCompleteHandler completeHandler();

    /**
     * 设置补全实现
     *
     * @param handler 补全实现
     */
    void completeHandler(TerminalCompleteHandler handler);

    /**
     * 获取键盘按键实现
     *
     * @return 键盘按键实现
     */
    TerminalKeyHandler keyHandler();

    /**
     * 设置键盘按键实现
     *
     * @param handler 键盘按键实现
     */
    void keyHandler(TerminalKeyHandler handler);

    /**
     * 获取鼠标按键实现
     *
     * @return 鼠标按键实现
     */
    TerminalMouseHandler mouseHandler();

    /**
     * 设置鼠标按键实现
     *
     * @param handler 鼠标按键实现
     */
    void mouseHandler(TerminalMouseHandler handler);

    /**
     * 获取命令历史处理器
     *
     * @return 命令历史处理器
     */
    TerminalHistoryHandler historyHandler();

    /**
     * 设置命令历史处理器
     *
     * @param handler 命令历史处理器
     */
    void historyHandler(TerminalHistoryHandler handler);

    /**
     * 保存历史
     *
     * @param input 内容
     */
    default void saveHistory(String input) {
        if (this.historyHandler() != null) {
            this.historyHandler().saveHistory(input);
        }
    }

    /**
     * 刷新光标
     */
    void flushCaret();

    /**
     * 移动光标到末尾
     */
    void moveCaretEnd();

    /**
     * 获取光标位置
     */
    int caretPosition();

    /**
     * 设置光标位置
     *
     * @param caretPosition 光标位置
     */
    void caretPosition(int caretPosition);

    /**
     * 获取字体类型
     *
     * @return 字体类型
     */
    String getFontFamily();

    /**
     * 设置字体类型
     *
     * @param fontFamily 字体类型
     */
    void setFontFamily(String fontFamily);

    /**
     * 获取字体大小
     *
     * @return 字体大小
     */
    double getFontSize();

    /**
     * 设置字体大小
     *
     * @param fontSize 字体大小
     */
    void setFontSize(double fontSize);

    /**
     * 字体大小递增
     */
     void fontSizeIncr() ;

    /**
     * 字体大小递减
     */
     void fontSizeDecr() ;

    /**
     * 获取提示文本
     *
     * @return 提示文本
     */
    String prompt();

    /**
     * 设置提示文本
     *
     * @param prompt 提示文本
     */
    void prompt(String prompt);

    /**
     * 获取提示文本长度
     *
     * @return 提示文本长度
     */
    default int promptLength() {
        return this.prompt().length();
    }

    /**
     * 输出提示文本
     */
    void outputPrompt();

    /**
     * 刷新提示文本
     */
    void flushPrompt();

    /**
     * 错误处理
     *
     * @param ex 异常
     */
    default void onError(Throwable ex) {
        if (ex != null) {
            this.onError(ex.getMessage());
        }
    }

    /**
     * 错误处理
     *
     * @param errMsg 异常消息
     */
    default void onError(String errMsg) {
        if (errMsg != null) {
            this.outputByPrompt(errMsg);
        }
    }

    /**
     * 获取内容
     *
     * @return 内容
     */
    String content();

    /**
     * 获取内容长度
     *
     * @return 内容长度
     */
    int contentLength();

    /**
     * 剪切内容
     */
    void cutContent();

    /**
     * 粘贴内容
     */
    void pasteContent();

    /**
     * 选择内容
     *
     * @param start 开始位置
     * @param end   结束位置
     */
    void selectContent(int start, int end);
}
