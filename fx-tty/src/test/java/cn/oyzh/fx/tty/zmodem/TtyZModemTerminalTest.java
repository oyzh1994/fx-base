package cn.oyzh.fx.tty.zmodem;

import com.jediterm.terminal.Terminal;
import org.junit.jupiter.api.Test;
import zmodem.FileCopyStreamEvent;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TtyZModemTerminal} 适配器测试。
 */
final class TtyZModemTerminalTest {

    @Test
    void jediTermAdapterDelegatesAllEffects() {
        var calls = new ArrayList<String>();
        var terminal = (Terminal) Proxy.newProxyInstance(
                Terminal.class.getClassLoader(),
                new Class[]{Terminal.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("writeCharacters")) {
                        calls.add("write:" + args[0]);
                    } else if (method.getName().equals("nextLine")
                            || method.getName().equals("saveCursor")
                            || method.getName().equals("restoreCursor")) {
                        calls.add(method.getName());
                    }
                    Class<?> type = method.getReturnType();
                    if (type == boolean.class) {
                        return false;
                    }
                    if (type == int.class) {
                        return 0;
                    }
                    if (type == long.class) {
                        return 0L;
                    }
                    if (type == char.class) {
                        return '\0';
                    }
                    return null;
                });

        var adapter = TtyZModemTerminal.ofJediTerm(terminal);
        adapter.nextLine();
        adapter.saveCursor();
        adapter.writeCharacters("progress");
        adapter.restoreCursor();

        assertEquals(List.of("nextLine", "saveCursor", "write:progress", "restoreCursor"), calls);
    }

    @Test
    void processorUsesTheTerminalAbstraction() {
        var calls = new ArrayList<String>();
        var terminal = new TtyZModemTerminal() {
            @Override
            public void nextLine() {
                calls.add("nextLine");
            }

            @Override
            public void saveCursor() {
                calls.add("saveCursor");
            }

            @Override
            public void writeCharacters(String text) {
                calls.add("write:" + text);
            }

            @Override
            public void restoreCursor() {
                calls.add("restoreCursor");
            }
        };
        byte[] frame = {'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '1'};
        var processor = new TtyZModemProcessor(
                frame,
                new ByteArrayInputStream(new byte[0]),
                new ByteArrayOutputStream(),
                terminal);

        processor.bytesTransferred(new FileCopyStreamEvent(this, "file.bin", 0, 1, 100, 50, 100));

        assertTrue(calls.contains("nextLine"));
        assertTrue(calls.contains("saveCursor"));
        assertTrue(calls.contains("restoreCursor"));
        assertTrue(calls.stream().anyMatch(call -> call.contains("file.bin")));
    }

    @Test
    void nullTerminalsAreNoops() {
        var jediTerm = TtyZModemTerminal.ofJediTerm(null);
        var ghostty = TtyZModemTerminal.ofGhostty(null);
        jediTerm.nextLine();
        jediTerm.saveCursor();
        jediTerm.writeCharacters("ignored");
        jediTerm.restoreCursor();
        ghostty.nextLine();
        ghostty.saveCursor();
        ghostty.writeCharacters("ignored");
        ghostty.restoreCursor();
        assertTrue(true);
    }
}
