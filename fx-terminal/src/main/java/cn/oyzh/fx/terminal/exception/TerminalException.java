package cn.oyzh.fx.terminal.exception;

/**
 * 终端异常
 *
 * @author oyzh
 * @since 2023-10-09
 */
public class TerminalException extends RuntimeException {

    /**
     * 构造终端异常对象。
     *
     * @param msg 消息
     */
    public TerminalException(String msg) {
        super(msg);
    }
}
