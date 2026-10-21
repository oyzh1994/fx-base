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
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ZModem帧检测测试。
 */
public class TtyZModemTtyConnectorTest {

    @Test(timeout = 5000)
    public void detectsPrefixSplitAcrossReads() throws Exception {
        byte[] frame = {'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '1'};
        byte[] input = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[input.length + frame.length];
        System.arraycopy(input, 0, data, 0, input.length);
        System.arraycopy(frame, 0, data, input.length, frame.length);
        AtomicReference<byte[]> detectedFrame = new AtomicReference<>();

        TtyStreamConnector source = new TtyStreamConnector(StandardCharsets.UTF_8) {
            @Override
            public InputStream input() {
                return new ByteArrayInputStream(data);
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
            protected TtyZModemProcessor createProcessor(byte[] frame, InputStream input,
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
        assertArrayEquals(frame, detectedFrame.get());
    }

    @Test(timeout = 5000)
    public void ignoresTerminalInputDuringTransfer() throws Exception {
        byte[] frame = {'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '1'};
        AtomicBoolean cancelled = new AtomicBoolean(false);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicReference<Throwable> readError = new AtomicReference<>();
        ByteArrayOutputStream protocolOutput = new ByteArrayOutputStream();

        TtyStreamConnector source = new TtyStreamConnector(StandardCharsets.UTF_8) {
            @Override
            public InputStream input() {
                return new ByteArrayInputStream(frame);
            }

            @Override
            public OutputStream output() {
                return protocolOutput;
            }

            @Override
            public boolean isConnected() {
                return true;
            }

            @Override
            public String getName() {
                return "input-guard";
            }
        };

        TtyZModemTtyConnector connector = new TtyZModemTtyConnector(terminalStub(), source) {
            @Override
            protected TtyZModemProcessor createProcessor(byte[] frame, InputStream input,
                                                          OutputStream output, Terminal terminal) {
                return new TtyZModemProcessor(frame, input, output, terminal) {
                    @Override
                    public void process() {
                        entered.countDown();
                        try {
                            release.await(5, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }

                    @Override
                    public void cancel() {
                        cancelled.set(true);
                    }
                };
            }
        };

        Thread reader = new Thread(() -> {
            try {
                char[] buffer = new char[32];
                while (connector.read(buffer, 0, buffer.length) >= 0) {
                    // 读取到流结束
                }
            } catch (Throwable e) {
                readError.set(e);
            }
        });
        reader.start();

        assertTrue("processor was not entered", entered.await(5, TimeUnit.SECONDS));
        connector.write("echo SHOULD_NOT_LEAK\n");
        connector.write(new byte[]{0x03});

        assertEquals(0, protocolOutput.size());
        assertTrue(cancelled.get());

        release.countDown();
        reader.join(5000);
        assertEquals(null, readError.get());
    }

    @Test
    public void closeDelegatesToUnderlyingConnector() {
        AtomicBoolean closed = new AtomicBoolean(false);
        TtyStreamConnector source = new TtyStreamConnector(StandardCharsets.UTF_8) {
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
                return "close-delegate";
            }

            @Override
            public void close() {
                closed.set(true);
            }
        };

        new TtyZModemTtyConnector(terminalStub(), source).close();
        assertTrue(closed.get());
    }

    @Test(timeout = 5000)
    public void rejectsNonInitZModemCandidateWithoutConsumingOutput() throws Exception {
        byte[] candidate = {'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '4'};
        byte[] prefix = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] data = new byte[prefix.length + candidate.length];
        System.arraycopy(prefix, 0, data, 0, prefix.length);
        System.arraycopy(candidate, 0, data, prefix.length, candidate.length);
        AtomicBoolean detected = new AtomicBoolean(false);

        TtyZModemTtyConnector connector = connector(data, detected);

        StringBuilder received = new StringBuilder();
        char[] buffer = new char[64];
        int length;
        while ((length = connector.read(buffer, 0, buffer.length)) >= 0) {
            received.append(buffer, 0, length);
        }

        assertEquals("hello**" + (char) ZModemCharacter.ZDLE.value() + "B04", received.toString());
        assertEquals(false, detected.get());
    }

    @Test(timeout = 5000)
    public void detectsOverlappingPrefixAfterRejectedCandidate() throws Exception {
        byte[] data = {'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'X',
                '*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '1'};
        AtomicBoolean detected = new AtomicBoolean(false);
        AtomicReference<byte[]> detectedFrame = new AtomicReference<>();

        TtyZModemTtyConnector connector = connector(data, detectedFrame, detected);

        StringBuilder received = new StringBuilder();
        char[] buffer = new char[64];
        int length;
        while ((length = connector.read(buffer, 0, buffer.length)) >= 0) {
            received.append(buffer, 0, length);
        }

        assertTrue(detected.get());
        assertArrayEquals(new byte[]{'*', '*', (byte) ZModemCharacter.ZDLE.value(), 'B', '0', '1'},
                detectedFrame.get());
        assertEquals("**" + (char) ZModemCharacter.ZDLE.value() + "X", received.toString());
    }

    private static TtyZModemTtyConnector connector(byte[] data, AtomicBoolean detected) {
        return connector(data, new AtomicReference<>(), detected);
    }

    private static TtyZModemTtyConnector connector(byte[] data, AtomicReference<byte[]> detectedFrame,
                                                   AtomicBoolean detected) {
        TtyStreamConnector source = new TtyStreamConnector(StandardCharsets.UTF_8) {
            @Override
            public InputStream input() {
                return new ByteArrayInputStream(data);
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
                return "detection-guard";
            }
        };
        return new TtyZModemTtyConnector(terminalStub(), source) {
            @Override
            protected TtyZModemProcessor createProcessor(byte[] frame, InputStream input,
                                                          OutputStream output, Terminal terminal) {
                detected.set(true);
                detectedFrame.set(frame);
                return new TtyZModemProcessor(frame, input, output, terminal) {
                    @Override
                    public void process() {
                    }
                };
            }
        };
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
