package cn.oyzh.fx.terminal.key;

import cn.oyzh.fx.terminal.Terminal;

/**
 * @author oyzh
 * @since 2023/08/28
 */
public interface TerminalKeyHandler<T extends Terminal> {

    /**
     * tab按键处理
     *
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onTabKeyPressed(T terminal ) throws Exception {
        if (terminal.completeHandler() != null) {
            terminal.completeHandler().completion(terminal.getInput(), terminal);
        }
        return false;
    }

    /**
     * 回车按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    boolean onEnterKeyPressed(T terminal) throws Exception;

    /**
     * 向上按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onUpKeyPressed(T terminal) throws Exception {
        if (terminal.historyHandler() != null) {
            String command = terminal.historyHandler().prevCommand(terminal);
            if (command != null) {
                terminal.output(command);
            }
        }
        return false;
    }

    /**
     * 向下按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onDownKeyPressed(T terminal) throws Exception {
        if (terminal.historyHandler() != null) {
            String command = terminal.historyHandler().nextCommand(terminal);
            if (command != null) {
                terminal.output(command);
            }
        }
        return false;
    }

    /**
     * 向左按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onLeftKeyPressed(T terminal) throws Exception {
        return !terminal.checkNop();
    }

    /**
     * 退格按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onBackspaceKeyPressed(T terminal) throws Exception {
        return !terminal.checkNop();
    }

    /**
     * 主页按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onHomeKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * 向上翻页按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onPageUpKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * 向下翻页按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onPageDownKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * Ctrl+A按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlAKeyPressed(T terminal) throws Exception {
        terminal.caretPosition(terminal.getNOP());
        return false;
    }

    /**
     * Ctrl+E按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlEKeyPressed(T terminal) throws Exception {
        terminal.caretPosition(terminal.contentLength());
        return false;
    }

    /**
     * Ctrl+Y按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlYKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * Ctrl+Z按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlZKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * Ctrl+X按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlXKeyPressed(T terminal) throws Exception {
        return false;
    }

    /**
     * Ctrl+V按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlVKeyPressed(T terminal) throws Exception {
        return true;
    }

    /**
     * Ctrl+C按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onCtrlCKeyPressed(T terminal) throws Exception {
        return true;
    }
}
