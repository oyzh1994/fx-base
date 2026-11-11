package cn.oyzh.fx.tty.zmodem;

import cn.oyzh.fx.tty.ghostty.TerminalView;

/**
 * ZModem 传输进度的终端效果抽象。
 *
 * <p>ZModem 处理器只需要“换行、保存光标、写字符、恢复光标”这四个能力。以前它直接依赖
 * JediTerm 的 {@code com.jediterm.terminal.Terminal}，导致 Ghostty 模式无法使用 ZModem。
 * 通过这个接口，JediTerm 和 Ghostty 可以各自实现自己的终端效果：</p>
 *
 * <ul>
 *     <li>{@link #ofJediTerm(com.jediterm.terminal.Terminal)}：调用 JediTerm 终端对象；</li>
 *     <li>{@link #ofGhostty(TerminalView)}：把 VT 序列直接写入 libghostty-vt 的终端状态，
 *     不经过远端连接器；</li>
 *     <li>{@link #noop()}：没有终端时忽略进度。</li>
 * </ul>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public interface TtyZModemTerminal {

    /**
     * 换行。
     */
    void nextLine();

    /**
     * 保存光标位置。
     */
    void saveCursor();

    /**
     * 写入字符。
     *
     * @param text 字符内容
     */
    void writeCharacters(String text);

    /**
     * 清除当前行。
     */
    void clearLine();

    /**
     * 恢复光标位置。
     */
    void restoreCursor();

    /**
     * 创建一个空实现。
     *
     * @return 空实现
     */
    static TtyZModemTerminal noop() {
        return Noop.INSTANCE;
    }

    /**
     * 使用 JediTerm 终端对象创建适配器。
     *
     * @param terminal JediTerm 终端对象，可为 null
     * @return 适配器
     */
    static TtyZModemTerminal ofJediTerm(com.jediterm.terminal.Terminal terminal) {
        if (terminal == null) {
            return noop();
        }
        return new TtyZModemTerminal() {
            @Override
            public void nextLine() {
                terminal.nextLine();
            }

            @Override
            public void saveCursor() {
                terminal.saveCursor();
            }

            @Override
            public void writeCharacters(String text) {
                terminal.writeCharacters(text);
            }

            @Override
            public void clearLine() {
                terminal.carriageReturn();
                terminal.eraseInLine(2);
            }

            @Override
            public void restoreCursor() {
                terminal.restoreCursor();
            }
        };
    }

    /**
     * 使用 Ghostty 终端视图创建适配器。
     *
     * <p>进度通过 {@link TerminalView#writeLocalOutput(String)} 直接写入终端状态，
     * 不会发送给远端进程。</p>
     *
     * @param view Ghostty 终端视图，可为 null
     * @return 适配器
     */
    static TtyZModemTerminal ofGhostty(TerminalView view) {
        if (view == null) {
            return noop();
        }
        return new TtyZModemTerminal() {
            @Override
            public void nextLine() {
                view.writeLocalOutput("\r\n");
            }

            @Override
            public void saveCursor() {
                // DECSC
                view.writeLocalOutput("\u001B7");
            }

            @Override
            public void writeCharacters(String text) {
                view.writeLocalOutput(text);
            }

            @Override
            public void clearLine() {
                view.writeLocalOutput("\r\u001B[2K");
            }

            @Override
            public void restoreCursor() {
                // DECRC
                view.writeLocalOutput("\u001B8");
            }
        };
    }

    /**
     * 空实现。
     */
    final class Noop implements TtyZModemTerminal {

        private static final Noop INSTANCE = new Noop();

        private Noop() {
        }

        @Override
        public void nextLine() {
        }

        @Override
        public void saveCursor() {
        }

        @Override
        public void writeCharacters(String text) {
        }

        @Override
        public void clearLine() {
        }

        @Override
        public void restoreCursor() {
        }
    }
}
