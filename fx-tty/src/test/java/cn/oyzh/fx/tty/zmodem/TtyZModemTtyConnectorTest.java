package cn.oyzh.fx.tty.zmodem;

import cn.oyzh.fx.tty.TtyStreamConnector;
import com.jediterm.terminal.Terminal;
import org.junit.Test;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * ZModem帧检测测试。
 */
public class TtyZModemTtyConnectorTest {

    @Test
    public void detectsPrefixSplitAcrossReads() throws Exception {
        String frame = "**" + (char) ZModemCharacter.ZDLE.value() + "B01";
        String input = "hello" + frame;
        AtomicReference<char[]> detectedFrame = new AtomicReference<>();

        TtyStreamConnector source = new TtyStreamConnector(StandardCharsets.UTF_8) {
            private final StringReader data = new StringReader(input);

            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                return this.data.read(buffer, offset, Math.min(length, 1));
            }

            @Override
            public InputStream input() {
                return new ByteArrayInputStream(new byte[0]);
            }

            @Override
            public OutputStream output() {
                return new ByteArrayOutputStream();
            }

            @Override
            public boolean isConnected() {
                return true;
            }

            @Override
            public String getName() {
                return "split-prefix";
            }
        };

        TtyZModemTtyConnector connector = new TtyZModemTtyConnector(terminalStub(), source) {
            @Override
            protected TtyZModemProcessor createProcessor(char[] frame, InputStream input,
                                                          OutputStream output, Terminal terminal) {
                detectedFrame.set(frame);
                return new TtyZModemProcessor(frame, input, output, terminal) {
                    @Override
                    public void process() {
                    }
                };
            }
        };

        StringBuilder received = new StringBuilder();
        char[] buffer = new char[32];
        for (int i = 0; i < 20 && detectedFrame.get() == null; i++) {
            int length = connector.read(buffer, 0, buffer.length);
            if (length > 0) {
                received.append(buffer, 0, length);
            }
        }

        assertEquals("hello", received.toString());
        assertArrayEquals(frame.toCharArray(), detectedFrame.get());
    }

    private static Terminal terminalStub() {
        return (Terminal) Proxy.newProxyInstance(Terminal.class.getClassLoader(), new Class[]{Terminal.class},
                (proxy, method, args) -> {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == boolean.class) {
                        return false;
                    }
                    if (returnType == int.class) {
                        return 0;
                    }
                    if (returnType == long.class) {
                        return 0L;
                    }
                    if (returnType == char.class) {
                        return '\0';
                    }
                    return null;
                });
    }
}
