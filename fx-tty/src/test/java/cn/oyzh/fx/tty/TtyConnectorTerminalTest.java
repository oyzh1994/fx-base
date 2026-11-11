package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link TtyConnectorTerminal} 适配器测试。
 */
final class TtyConnectorTerminalTest {

    @Test
    void readsConnectorCharsAsUtf8Bytes() throws Exception {
        var connector = new FakeConnector();
        connector.outputs.add("你好, ghostty");
        var terminal = new TtyConnectorTerminal(connector, 80, 24);
        try (InputStream output = terminal.output()) {
            var bytes = output.readAllBytes();
            assertEquals("你好, ghostty", new String(bytes, StandardCharsets.UTF_8));
        }
        assertTrue(connector.readyCalled, "connector.ready() must be called before reading");
        assertEquals(new TermSize(80, 24), connector.initialSize);
    }

    @Test
    void writesUtf8InputThroughConnectorStringApi() throws Exception {
        var connector = new FakeConnector();
        var terminal = new TtyConnectorTerminal(connector, 80, 24);
        try (OutputStream input = terminal.input()) {
            input.write("hello ".getBytes(StandardCharsets.UTF_8));
            input.write("世界".getBytes(StandardCharsets.UTF_8));
        }
        assertEquals(List.of("hello ", "世界"), connector.written);
    }

    @Test
    void forwardsResizeAndClose() {
        var connector = new FakeConnector();
        var terminal = new TtyConnectorTerminal(connector, 80, 24);
        terminal.resize(120, 40, 1200, 800);
        assertEquals(new TermSize(120, 40), connector.lastSize);
        assertFalse(connector.closed);
        terminal.close();
        assertTrue(connector.closed);
        terminal.close();
        assertEquals(1, connector.closeCount);
    }

    @Test
    void preservesSurrogatePairsAcrossReads() throws Exception {
        var connector = new FakeConnector();
        connector.maxRead = 1;
        connector.outputs.add("😀");
        var terminal = new TtyConnectorTerminal(connector, 80, 24);
        assertArrayEquals("😀".getBytes(StandardCharsets.UTF_8), terminal.output().readAllBytes());
    }

    @Test
    void supportsConnectorsThatAreNotReadyButReadable() throws Exception {
        var connector = new FakeConnector();
        connector.readyResult = false;
        connector.outputs.add("ok");
        var terminal = new TtyConnectorTerminal(connector, 80, 24);
        assertEquals("ok", new String(terminal.output().readAllBytes(), StandardCharsets.UTF_8));
    }

    @Test
    void parsesEngineIds() {
        assertEquals(TtyTerminalEngine.getDefaultEngine(), TtyTerminalEngine.of(null));
        assertEquals(TtyTerminalEngine.getDefaultEngine(), TtyTerminalEngine.of("unknown"));
        assertEquals(TtyTerminalEngine.GHOSTTY, TtyTerminalEngine.of("Ghostty"));
        assertEquals(TtyTerminalEngine.GHOSTTY, TtyTerminalEngine.of(" ghostty "));
        assertEquals(TtyTerminalEngine.JEDITERM, TtyTerminalEngine.of("JediTerm"));
        assertEquals("jediterm", TtyTerminalEngine.JEDITERM.id());
        assertEquals("ghostty", TtyTerminalEngine.GHOSTTY.id());
    }

    private static final class FakeConnector implements TtyConnector {

        private final Deque<String> outputs = new ArrayDeque<>();

        private final List<String> written = new ArrayList<>();

        private boolean readyCalled;

        private boolean readyResult = true;

        private boolean closed;

        private int closeCount;

        private TermSize lastSize;

        private TermSize initialSize;

        private int maxRead = Integer.MAX_VALUE;

        private String pendingOutput;

        private int pendingIndex;

        @Override
        public int read(char[] buffer, int offset, int length) throws IOException {
            if (this.pendingOutput == null || this.pendingIndex >= this.pendingOutput.length()) {
                this.pendingOutput = this.outputs.poll();
                this.pendingIndex = 0;
                if (this.pendingOutput == null) {
                    return -1;
                }
            }
            int count = Math.min(Math.min(length, this.pendingOutput.length() - this.pendingIndex), this.maxRead);
            this.pendingOutput.getChars(this.pendingIndex, this.pendingIndex + count, buffer, offset);
            this.pendingIndex += count;
            return count;
        }

        @Override
        public void write(byte[] bytes) {
            this.written.add(new String(bytes, StandardCharsets.UTF_8));
        }

        @Override
        public void write(String string) {
            this.written.add(string);
        }

        @Override
        public boolean isConnected() {
            return !this.closed;
        }

        @Override
        public void resize(TermSize termSize) {
            this.lastSize = termSize;
            if (this.initialSize == null) {
                this.initialSize = termSize;
            }
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public boolean ready() {
            this.readyCalled = true;
            return this.readyResult;
        }

        @Override
        public String getName() {
            return "fake";
        }

        @Override
        public void close() {
            this.closed = true;
            this.closeCount++;
        }
    }
}
