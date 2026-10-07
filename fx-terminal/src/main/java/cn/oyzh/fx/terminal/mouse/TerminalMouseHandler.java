package cn.oyzh.fx.terminal.mouse;

import cn.oyzh.fx.terminal.Terminal;

/**
 * 终端鼠标按键处理器
 *
 * @author oyzh
 * @since 2023-10-09
 */
public interface TerminalMouseHandler<T extends Terminal> {

    /**
     * 主要按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onPrimaryMousePressed(T terminal) throws Exception {
        return true;
    }

    /**
     * 次要按键处理
     *
     * @param terminal 终端
     * @return 结果
     * @throws Exception 异常
     */
    default boolean onSecondMousePressed(T terminal) throws Exception {
        // terminal.pasteContent();
        return true;
    }
}
