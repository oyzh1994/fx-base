package cn.oyzh.fx.tty.ghostty;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PtyTerminal} 与 {@link GhosttyTerminals} 的基础测试。
 */
final class PtyTerminalTest {

    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    @Test
    void defaultShellCommandIsNotEmpty() {
        assertFalse(GhosttyTerminals.defaultShellCommand().isEmpty());
    }

    @Test
    void defaultWorkingDirectoryExists() {
        assertTrue(Files.isDirectory(GhosttyTerminals.defaultWorkingDirectory()));
    }

    @Test
    void readsProcessOutputAndCloses() throws Exception {
        var command = WINDOWS
                ? List.of("cmd.exe", "/c", "echo hello-from-pty")
                : List.of("/bin/sh", "-c", "printf hello-from-pty");
        var terminal = new PtyTerminal(command, Path.of(System.getProperty("user.home")), Map.of(), 80, 24);
        try (InputStream output = terminal.output()) {
            var buffer = new byte[1024];
            var total = 0;
            var read = output.read(buffer);
            while (read >= 0) {
                total += read;
                read = output.read(buffer);
            }
            assertTrue(total > 0, "Expected PTY output");
            assertTrue(new String(buffer, 0, Math.min(total, buffer.length), StandardCharsets.UTF_8)
                    .contains("hello-from-pty"));
        } finally {
            terminal.close();
        }
        assertEquals(-1, terminal.process().getInputStream().read());
    }

    @Test
    void resizesRunningPty() throws Exception {
        if (WINDOWS) {
            return;
        }
        var command = List.of("/bin/sh", "-c", "sleep 2");
        var terminal = new PtyTerminal(command, Path.of(System.getProperty("user.home")), Map.of(), 80, 24);
        try {
            terminal.resize(100, 30, 1000, 600);
        } finally {
            terminal.close();
        }
    }
}
