package cn.oyzh.fx.tty.ghostty;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Ghostty 终端的便捷工厂。
 *
 * <p>提供开箱即用的本地 Shell 终端，也允许调用方使用自定义命令、工作目录和环境变量。
 * 所有工厂方法都应运行在 JavaFX 应用线程上，因为 {@link TerminalView} 是 JavaFX 节点。</p>
 *
 * <p>示例：</p>
 * <pre>{@code
 * var view = GhosttyTerminals.localShell();
 * tab.setContent(view);
 * tab.textProperty().bind(view.titleProperty());
 * }</pre>
 *
 * <p>使用 FFM 调用本机库时，请在启动 JVM 时添加
 * {@code --enable-native-access=ALL-UNNAMED}。</p>
 *
 * @author oyzh
 * @since 2026-11-11
 */
public final class GhosttyTerminals {

    private GhosttyTerminals() {
    }

    /**
     * 使用当前操作系统默认 Shell 和当前工作目录创建终端视图。
     *
     * @return 终端视图
     */
    public static TerminalView localShell() {
        return localShell(defaultShellCommand(), defaultWorkingDirectory());
    }

    /**
     * 使用指定工作目录和默认 Shell 创建终端视图。
     *
     * @param directory 工作目录
     * @return 终端视图
     */
    public static TerminalView localShell(Path directory) {
        return localShell(defaultShellCommand(), directory);
    }

    /**
     * 使用指定命令和工作目录创建终端视图。
     *
     * <p>若命令是 bash、zsh、fish、PowerShell 或 cmd，会自动注入 Shell 集成脚本，
     * 以支持标题、当前目录、命令边界等能力。</p>
     *
     * @param command   启动命令
     * @param directory 工作目录
     * @return 终端视图
     */
    public static TerminalView localShell(List<String> command, Path directory) {
        return view(command, directory, System.getenv());
    }

    /**
     * 使用指定命令、工作目录和环境变量创建终端视图。
     *
     * @param command     启动命令
     * @param directory   工作目录
     * @param environment 环境变量
     * @return 终端视图
     */
    public static TerminalView view(List<String> command, Path directory, Map<String, String> environment) {
        Objects.requireNonNull(command, "command");
        Objects.requireNonNull(directory, "directory");
        Objects.requireNonNull(environment, "environment");
        if (command.isEmpty()) {
            throw new IllegalArgumentException("command must not be empty");
        }
        return new TerminalView((columns, rows) -> {
            var launcher = Shell.integrate(command, environment);
            return new PtyTerminal(launcher.command(), directory, launcher.environment(), columns, rows);
        });
    }

    /**
     * 返回当前操作系统的默认 Shell 命令。
     *
     * @return Shell 命令
     */
    public static List<String> defaultShellCommand() {
        if (HostPlatform.CURRENT.os() == HostPlatform.OS.WINDOWS) {
            for (var candidate : List.of("pwsh.exe", "powershell.exe", "cmd.exe")) {
                if (isOnPath(candidate)) {
                    return List.of(candidate);
                }
            }
            return List.of("cmd.exe");
        }
        var shell = System.getenv("SHELL");
        if (shell != null && !shell.isBlank() && Files.isExecutable(Path.of(shell))) {
            return List.of(shell);
        }
        var name = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        var candidates = name.contains("mac") || name.contains("darwin")
                ? List.of("/bin/zsh", "/bin/bash", "/bin/sh")
                : List.of("/bin/bash", "/bin/sh");
        for (var candidate : candidates) {
            if (Files.isExecutable(Path.of(candidate))) {
                return List.of(candidate);
            }
        }
        return List.of(candidates.getFirst());
    }

    private static boolean isOnPath(String executable) {
        var path = System.getenv("PATH");
        if (path == null) {
            return false;
        }
        for (var entry : path.split(java.io.File.pathSeparator)) {
            if (!entry.isBlank() && Files.isRegularFile(Path.of(entry, executable))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回终端默认工作目录（当前进程工作目录）。
     *
     * @return 工作目录
     */
    public static Path defaultWorkingDirectory() {
        var directory = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        return Files.isDirectory(directory) ? directory : Path.of(System.getProperty("user.home"));
    }
}
