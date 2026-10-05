package cn.oyzh.fx.tty;

import com.jediterm.terminal.TtyConnector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
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
