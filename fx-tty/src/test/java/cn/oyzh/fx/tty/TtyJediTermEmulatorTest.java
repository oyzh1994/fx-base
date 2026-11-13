package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.Terminal;
import com.jediterm.terminal.TerminalDataStream;
import com.jediterm.terminal.TerminalMode;
import com.jediterm.terminal.TtyBasedArrayDataStream;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.emulator.mouse.MouseFormat;
import com.jediterm.terminal.emulator.mouse.MouseMode;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class TtyJediTermEmulatorTest {

    @Test
    void appliesEveryParameterInPrivateModeSetSequence() throws Exception {
        var events = new ArrayList<String>();
        var emulator = createEmulator("\u001B[?1006;1000h", events);

        drain(emulator);

        assertEquals(List.of("format=" + MouseFormat.MOUSE_FORMAT_SGR,
                "mode=" + MouseMode.MOUSE_REPORTING_NORMAL), events);
    }

    @Test
    void appliesHtopMouseModeAndFormatSequence() throws Exception {
        var events = new ArrayList<String>();
        var emulator = createEmulator("\u001B[?1000;1006h", events);

        drain(emulator);

        assertEquals(List.of("mode=" + MouseMode.MOUSE_REPORTING_NORMAL,
                "format=" + MouseFormat.MOUSE_FORMAT_SGR), events);
    }

    @Test
    void appliesEveryParameterInPrivateModeResetSequence() throws Exception {
        var events = new ArrayList<String>();
        var emulator = createEmulator("\u001B[?1000;1006l", events);

        drain(emulator);

        assertEquals(List.of("mode=" + MouseMode.MOUSE_REPORTING_NONE,
                "format=" + MouseFormat.MOUSE_FORMAT_XTERM), events);
    }

    @Test
    void appliesEveryParameterInAnsiModeSetSequence() throws Exception {
        var events = new ArrayList<String>();
        var emulator = createEmulator("\u001B[4;20h", events);

        drain(emulator);

        assertEquals(List.of("terminalMode=" + TerminalMode.InsertMode,
                "terminalMode=" + TerminalMode.AutoNewLine), events);
    }

    @Test
    void handlesModeSequenceSplitAcrossInputReads() throws Exception {
        var events = new ArrayList<String>();
        var dataStream = new TtyBasedArrayDataStream(new FragmentedConnector("\u001B[?1006;1000h"));
        var emulator = new TtyJediTermEmulator(dataStream, recordingTerminal(events));

        drain(emulator);

        assertEquals(List.of("format=" + MouseFormat.MOUSE_FORMAT_SGR,
                "mode=" + MouseMode.MOUSE_REPORTING_NORMAL), events);
    }

    private static TtyJediTermEmulator createEmulator(String input, List<String> events) {
        TerminalDataStream dataStream = new TtyBasedArrayDataStream(new FragmentedConnector(input));
        return new TtyJediTermEmulator(dataStream, recordingTerminal(events));
    }

    private static void drain(TtyJediTermEmulator emulator) throws Exception {
        while (emulator.hasNext()) {
            emulator.next();
        }
    }

    private static Terminal recordingTerminal(List<String> events) {
        return (Terminal) Proxy.newProxyInstance(
                Terminal.class.getClassLoader(),
                new Class<?>[]{Terminal.class},
                (proxy, method, args) -> {
                    if ("setMouseMode".equals(method.getName())) {
                        events.add("mode=" + args[0]);
                    } else if ("setMouseFormat".equals(method.getName())) {
                        events.add("format=" + args[0]);
                    } else if ("setModeEnabled".equals(method.getName())) {
                        events.add("terminalMode=" + args[0]);
                    } else if (method.getReturnType() == boolean.class) {
                        return false;
                    } else if (method.getReturnType() == int.class) {
                        return 0;
                    }
                    return null;
                });
    }

    private static final class FragmentedConnector implements TtyConnector {

        private final String input;

        private int offset;

        private FragmentedConnector(String input) {
            this.input = input;
        }

        @Override
        public int read(char[] buffer, int offset, int length) {
            if (this.offset >= this.input.length()) {
                return -1;
            }
            int count = Math.min(Math.min(length, 2), this.input.length() - this.offset);
            this.input.getChars(this.offset, this.offset + count, buffer, offset);
            this.offset += count;
            return count;
        }

        @Override
        public void write(byte[] bytes) {
        }

        @Override
        public void write(String string) {
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void resize(TermSize termSize) {
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public boolean ready() {
            return true;
        }

        @Override
        public String getName() {
            return "fragmented";
        }

        @Override
        public void close() {
        }
    }
}
