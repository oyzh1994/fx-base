package cn.oyzh.fx.tty;

import java.nio.charset.Charset;

/**
 * 字符集提供者，用于获取终端使用的字符集。
 *
 * @author oyzh
 * @since 2026-10-03
 */
public interface TtyCharsetble {

    /**
     * 获取字符集
     *
     * @return 字符集
     */
    Charset charset();

}
