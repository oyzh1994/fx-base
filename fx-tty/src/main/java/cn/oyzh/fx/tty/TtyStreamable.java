package cn.oyzh.fx.tty;

import com.jediterm.terminal.TtyConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 可流式访问的终端连接器接口，在 {@link TtyConnector} 基础上暴露底层的输入流与输出流。
 *
 * @author oyzh
 * @since 2026-10-03
 */
public interface TtyStreamable extends TtyConnector {

    /**
     * 获取真实的输入流
     *
     * @return 输入流
     */
    InputStream input();

    /**
     * 获取真实的输出流
     *
     * @return 输出流
     */
    OutputStream output();

    /**
     * 写入行
     *
     * @param str 内容
     * @throws IOException 异常
     */
    default void writeLine(String str) throws IOException {
        this.write(str + "\r");
    }
}
