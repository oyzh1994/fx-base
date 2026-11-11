package cn.oyzh.fx.tty.ghostty;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link GhosttyTerminals} 便捷工厂测试。
 */
final class GhosttyTerminalsTest {

    private static final boolean WINDOWS =
            System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    @BeforeAll
    static void initializeJavaFxRuntime() throws Exception {
        var started = new CountDownLatch(1);
        try {
            Platform.startup(() -> {
                Platform.setImplicitExit(false);
                started.countDown();
            });
        } catch (IllegalStateException _) {
            Platform.runLater(started::countDown);
        }
        assertTrue(started.await(30, TimeUnit.SECONDS), "Timed out waiting for JavaFX runtime startup");
    }

    @Test
    void createsAndClosesLocalShellView() throws Exception {
        var command = WINDOWS ? List.of("cmd.exe") : List.of("/bin/sh");
        var view = callOnFxThread(() -> GhosttyTerminals.view(
                command, Path.of(System.getProperty("user.home")), Map.of()));
        assertNotNull(view);
        assertEquals(80, view.getTerminalSize().columns());
        assertEquals(24, view.getTerminalSize().rows());
        callOnFxThread(() -> {
            view.close();
            return null;
        });
    }

    private static <T> T callOnFxThread(Callable<T> action) throws Exception {
        var future = new FutureTask<>(action);
        Platform.runLater(future);
        return future.get(30, TimeUnit.SECONDS);
    }
}
