package cn.oyzh.fx.xserver;

import cn.oyzh.fx.xserver.HostWindowStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Where the host window is remembered between runs.
 *
 * <p>This is the first thing this server keeps after it exits, so what is pinned here is mostly its
 * manners: it survives a file it cannot read, it does not lose a position to a file someone edited,
 * and it puts a remembered position somewhere the display can actually show - an unplugged monitor
 * must not leave a window the user cannot reach.
 */
class HostWindowStoreTest {

    @Test
    void aPositionSurvivesTheProcessItWasWrittenIn(@TempDir Path directory) {
        Path file = directory.resolve("host-windows.properties");
        HostWindowStore.at(file).rememberPosition("Termora", 120, 80);

        assertEquals(new Point(120, 80), HostWindowStore.at(file).position("Termora"),
                "a second store over the same file is what a restart looks like");
    }

    @Test
    void aClientThatHasNotBeenSeenBeforeHasNoPosition(@TempDir Path directory) {
        assertNull(HostWindowStore.at(directory.resolve("none.properties")).position("Termora"));
    }

    @Test
    void twoClientsRememberTheirOwnWindows(@TempDir Path directory) {
        HostWindowStore store = HostWindowStore.at(directory.resolve("two.properties"));
        store.rememberPosition("Termora", 10, 20);
        store.rememberPosition("EasyShell", 300, 400);

        assertEquals(new Point(10, 20), store.position("Termora"));
        assertEquals(new Point(300, 400), store.position("EasyShell"));
    }

    @Test
    void aFileThisServerCannotReadIsNotAFailure(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("broken.properties");
        Files.writeString(file, "this is not a properties file\0\0 = = =");

        HostWindowStore store = HostWindowStore.at(file);
        assertNull(store.position("Termora"));
        // And it can still be written to, which is what the next session needs.
        store.rememberPosition("Termora", 5, 6);
        assertEquals(new Point(5, 6), HostWindowStore.at(file).position("Termora"));
    }

    @Test
    void aHandEditedPositionThatIsNotANumberIsIgnored(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("edited.properties");
        Files.writeString(file, "Termora.x=left\nTermora.y=80\n");

        assertNull(HostWindowStore.at(file).position("Termora"));
    }

    @Test
    void rememberedPositionsAreKeptWhereTheDisplayCanShowThem() {
        Rectangle usable = new Rectangle(0, 0, 1440, 900);

        assertEquals(new Point(100, 100),
                HostWindowStore.clamp(new Point(100, 100), 800, 600, usable),
                "a position the display still has is left alone");

        assertEquals(new Point(640, 300),
                HostWindowStore.clamp(new Point(3000, 2000), 800, 600, usable),
                "a monitor that was unplugged must not leave the window off the screen");

        assertEquals(new Point(0, 0),
                HostWindowStore.clamp(new Point(-500, -300), 800, 600, usable),
                "and neither must one that moved to the left or above");

        assertEquals(new Point(0, 0),
                HostWindowStore.clamp(new Point(-40, -40), 2000, 1200, usable),
                "a window larger than the display keeps its top left: that is where its title bar is");
    }
}
