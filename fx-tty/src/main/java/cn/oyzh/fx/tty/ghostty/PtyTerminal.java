package cn.oyzh.fx.tty.ghostty;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import com.pty4j.unix.UnixPtyProcess;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 基于 pty4j 的本地终端后端，供 {@link TerminalView} 使用。
 *
 * <p>该类把 pty4j 的伪终端进程适配为 Ghostty 终端所需的 {@link Terminal} 接口。
 * Unix 平台通过 {@link UnixPtyFileDescriptor} 写入行列与像素尺寸，Windows 平台
 * 使用 ConPTY 的窗口尺寸。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public final class PtyTerminal implements Terminal {

    private final PtyProcess process;

    /**
     * 创建并启动一个伪终端进程。
     *
     * @param command     启动命令
     * @param directory   工作目录
     * @param environment 环境变量
     * @param columns     初始列数
     * @param rows        初始行数
     * @throws IOException 进程启动失败
     */
    public PtyTerminal(List<String> command, Path directory, Map<String, String> environment, int columns, int rows) throws IOException {
        this.process = (PtyProcess) new PtyProcessBuilder()
                .setCommand(command.toArray(String[]::new))
                .setConsole(false)
                .setRedirectErrorStream(true)
                .setDirectory(directory.toString())
                .setEnvironment(environment)
                .setInitialColumns(columns)
                .setInitialRows(rows)
                .setUseWinConPty(true)
                .start();
    }

    @Override
    public InputStream output() {
        return this.process.getInputStream();
    }

    @Override
    public OutputStream input() {
        return this.process.getOutputStream();
    }

    @Override
    public void resize(int columns, int rows, int widthPx, int heightPx) {
        if (this.process instanceof UnixPtyProcess unix) {
            UnixPtyFileDescriptor.resize(unix.getPty().getMasterFD(), columns, rows, widthPx, heightPx);
        } else {
            this.process.setWinSize(new WinSize(columns, rows));
        }
    }

    /**
     * 获取底层 pty4j 进程。
     *
     * @return 伪终端进程
     */
    public PtyProcess process() {
        return this.process;
    }

    @Override
    public void close() {
        this.process.destroy();
        try {
            if (!this.process.waitFor(2, TimeUnit.SECONDS)) {
                this.process.destroyForcibly();
                this.process.waitFor();
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        }
    }
}
