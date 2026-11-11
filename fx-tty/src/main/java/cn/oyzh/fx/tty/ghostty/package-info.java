/**
 * Ghostty 终端模拟器的 JavaFX 实现。
 *
 * <p>本包是 <a href="https://github.com/vlaaad/ghosttyfx">ghosttyfx</a> 在 fx-tty 中的移植，
 * 使用 Ghostty 的 {@code libghostty-vt} 作为终端内核，通过 Java FFM 调用本机库，
 * 并由 JavaFX {@code Canvas} 渲染。</p>
 *
 * <p>主要入口：</p>
 * <ul>
 *     <li>{@link cn.oyzh.fx.tty.ghostty.TerminalView}：JavaFX 终端控件；</li>
 *     <li>{@link cn.oyzh.fx.tty.ghostty.GhosttyTerminals}：本地 Shell 便捷工厂；</li>
 *     <li>{@link cn.oyzh.fx.tty.ghostty.PtyTerminal}：基于 pty4j 的 {@link cn.oyzh.fx.tty.ghostty.Terminal} 实现。</li>
 * </ul>
 *
 * <p>6 个平台的本机库位于 {@code /ghostty/<platform>/} 资源目录，运行时自动加载。
 * 启动 JVM 时请添加 {@code --enable-native-access=ALL-UNNAMED}。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
package cn.oyzh.fx.tty.ghostty;
