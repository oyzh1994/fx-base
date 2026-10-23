package cn.oyzh.fx.tty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TerminalDataStream;
import com.jediterm.terminal.TtyConnector;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * 终端首屏读取与重绘同步测试。
 */
public class TtyBufferedArrayDataStreamTest {

    @Test
    public void readsFirstScreenInOneChunkAndHoldsRepaintUntilParsed() throws Exception {
        String text = "x".repeat(1390) + "\r\n";
        FakeConnector connector = new FakeConnector(text);
        TtyRepaintGate repaintGate = new TtyRepaintGate();
        TtyBufferedArrayDataStream stream = new TtyBufferedArrayDataStream(connector, null, repaintGate);

        assertEquals('x', stream.getChar());
        assertEquals(16 * 1024, connector.lastRequestedLength);
        assertEquals(text.length(), connector.totalRead);
        assertTrue(repaintGate.isPaused());

        for (int i = 1; i < text.length(); i++) {
            stream.getChar();
        }
        assertTrue(repaintGate.isPaused());

        try {
            stream.getChar();
            fail("expected EOF");
        } catch (TerminalDataStream.EOF expected) {
            assertFalse(repaintGate.isPaused());
        }
    }

    private static final class FakeConnector implements TtyConnector {

        private final String text;

        private int offset;

        private int lastRequestedLength;

        private int totalRead;

        private FakeConnector(String text) {
            this.text = text;
        }

        @Override
        public int read(char[] buf, int offset, int length) {
            this.lastRequestedLength = length;
            if (this.offset >= this.text.length()) {
                return -1;
            }
            int count = Math.min(length, this.text.length() - this.offset);
            this.text.getChars(this.offset, this.offset + count, buf, offset);
            this.offset += count;
            this.totalRead += count;
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
        public boolean ready() throws IOException {
            return true;
        }

        @Override
        public String getName() {
            return "test";
        }

        @Override
        public void close() {
        }
    }
}
