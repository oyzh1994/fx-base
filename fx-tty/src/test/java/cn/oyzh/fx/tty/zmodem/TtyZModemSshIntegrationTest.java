package cn.oyzh.fx.tty.zmodem;

import cn.oyzh.fx.tty.TtyStreamConnector;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jediterm.terminal.Terminal;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.junit.Test;
import zmodem.util.CustomFile;
import zmodem.util.FileAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 使用真实 SSH 主机验证 ZModem 收发以及传输结束后的终端读取。
 */
public class TtyZModemSshIntegrationTest {

    private static final String HOST = System.getProperty("zmodem.ssh.host", "192.168.22.152");
    private static final int PORT = Integer.parseInt(System.getProperty("zmodem.ssh.port", "22"));
    private static final String USER = System.getProperty("zmodem.ssh.user", "root");
    private static final String PASSWORD = System.getProperty("zmodem.ssh.password", "user@147");
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    @Test(timeout = 60000)
    public void jschSupportsSzAndRz() throws Exception {
        Session session = new JSch().getSession(USER, HOST, PORT);
        session.setPassword(PASSWORD);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(10000);

        ChannelShell channel = (ChannelShell) session.openChannel("shell");
        channel.setPty(true);
        channel.setPtyType("xterm");
        // 与 ShellTerminalApp2 一致：先创建一个流供 reader 使用。
        InputStream firstInput = channel.getInputStream();
        OutputStream firstOutput = channel.getOutputStream();
        channel.connect(10000);

        try (JschTestConnector connector = new JschTestConnector(channel, firstInput, firstOutput)) {
            runTransfers(connector);
        } finally {
            channel.disconnect();
            session.disconnect();
        }
    }

    @Test(timeout = 60000)
    public void sshjSupportsSzAndRz() throws Exception {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(HOST, PORT);
        ssh.authPassword(USER, PASSWORD);

        net.schmizz.sshj.connection.channel.direct.Session session = ssh.startSession();
        session.allocateDefaultPTY();
        Shell shell = session.startShell();
        InputStream input = shell.getInputStream();
        OutputStream output = shell.getOutputStream();

        try (SshjTestConnector connector = new SshjTestConnector(input, output)) {
            runTransfers(connector);
        } finally {
            shell.close();
            session.close();
            ssh.close();
        }
    }

    private void runTransfers(TestConnector connector) throws Exception {
        Path localDir = Files.createTempDirectory("fx-tty-zmodem-");
        Path receivedFile = localDir.resolve("sample.bin");
        Path uploadFile = localDir.resolve("upload.bin");
        Files.write(uploadFile, randomBytes(32 * 1024));

        String remoteDir = "/tmp/fx-tty-zmodem-" + System.nanoTime();
        TtyZModemTtyConnector zmodemConnector = new TestZModemConnector(
                connector, localDir.toFile(), uploadFile.toFile());
        OutputBuffer terminalOutput = new OutputBuffer();
        AtomicReference<Throwable> readError = new AtomicReference<>();
        AtomicBoolean running = new AtomicBoolean(true);
        Thread reader = startReader(zmodemConnector, terminalOutput, readError, running);

        try {
            send(zmodemConnector, "echo ZMODEM_${NONE:-}READY");
            assertTrue("terminal did not become ready", terminalOutput.waitFor("ZMODEM_READY", 5000));
            System.out.println("terminal ready");

            send(zmodemConnector, "mkdir -p " + remoteDir
                    + " && dd if=/dev/urandom of=" + remoteDir + "/sample.bin bs=4096 count=16 status=none"
                    + " && sz " + remoteDir + "/sample.bin"
                    + "; echo ZMODEM_${NONE:-}SZ_DONE; sha256sum " + remoteDir + "/sample.bin");
            assertNoReadError(readError);
            assertTrue("sz did not finish: " + terminalOutput, terminalOutput.waitFor("ZMODEM_SZ_DONE", 20000));
            System.out.println("sz finished");
            assertTrue("received file is missing", Files.exists(receivedFile));
            assertEquals(64 * 1024, Files.size(receivedFile));
            assertTrue("sz hash mismatch: " + terminalOutput,
                    terminalOutput.waitFor(sha256(receivedFile), 5000));
            System.out.println("sz file verified");

            send(zmodemConnector, "cd " + remoteDir + " && rm -f upload.bin && rz"
                    + "; echo ZMODEM_${NONE:-}RZ_DONE; sha256sum upload.bin");
            assertNoReadError(readError);
            assertTrue("rz did not finish: " + terminalOutput, terminalOutput.waitFor("ZMODEM_RZ_DONE", 20000));
            System.out.println("rz finished");
            assertTrue("rz hash mismatch: " + terminalOutput,
                    terminalOutput.waitFor(sha256(uploadFile), 5000));
            System.out.println("rz file verified");

            assertNoReadError(readError);
        } finally {
            running.set(false);
            reader.interrupt();
        }
    }

    private static Thread startReader(TtyZModemTtyConnector connector, OutputBuffer output,
                                      AtomicReference<Throwable> error, AtomicBoolean running) {
        Thread thread = new Thread(() -> {
            char[] buffer = new char[4096];
            while (running.get()) {
                try {
                    int length = connector.read(buffer, 0, buffer.length);
                    if (length < 0) {
                        return;
                    }
                    if (length > 0) {
                        output.append(buffer, length);
                    }
                } catch (IOException ex) {
                    if (running.get()) {
                        error.compareAndSet(null, ex);
                    }
                    return;
                }
            }
        }, "zmodem-test-reader");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }

    private static void send(TtyZModemTtyConnector connector, String command) throws IOException {
        connector.write(command + "\r");
    }

    private static void assertNoReadError(AtomicReference<Throwable> error) {
        Throwable throwable = error.get();
        if (throwable != null) {
            throw new AssertionError("terminal reader failed", throwable);
        }
    }

    private static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i * 31 + 17);
        }
        return bytes;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(Files.readAllBytes(file)));
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

    private static final class TestZModemConnector extends TtyZModemTtyConnector {

        private final File receiveDir;
        private final File uploadFile;

        private TestZModemConnector(TtyStreamConnector connector, File receiveDir, File uploadFile) {
            super(terminalStub(), connector);
            this.receiveDir = receiveDir;
            this.uploadFile = uploadFile;
        }

        @Override
        protected TtyZModemProcessor createProcessor(char[] frame, InputStream input,
                                                      OutputStream output, Terminal terminal) {
            Supplier<FileAdapter> receiveSupplier = () -> new CustomFile(this.receiveDir);
            Supplier<List<FileAdapter>> sendSupplier = () -> Collections.singletonList(new CustomFile(this.uploadFile));
            return new TtyZModemProcessor(frame, input, output, terminal, receiveSupplier, sendSupplier);
        }
    }

    private static class TestConnector extends TtyStreamConnector implements AutoCloseable {

        private final InputStream firstInput;
        private final OutputStream firstOutput;

        private TestConnector(InputStream input, OutputStream output) {
            super(CHARSET);
            this.firstInput = input;
            this.firstOutput = output;
            this.reader = new InputStreamReader(input, CHARSET);
            this.writer = new OutputStreamWriter(output, CHARSET);
        }

        @Override
        public InputStream input() {
            return this.firstInput;
        }

        @Override
        public OutputStream output() {
            return this.firstOutput;
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public String getName() {
            return "zmodem-test";
        }

        @Override
        public void close() {
            // 测试中由外层关闭SSH channel，避免关闭JSch管道时阻塞。
        }
    }

    /**
     * 模拟 ShellTestTtyConnector.init(ChannelShell) 的行为：reader 持有一个 JSch 流，
     * 但 input() 每次调用都会让 JSch 新建流并替换 channel 的接收端。
     */
    private static final class JschTestConnector extends TestConnector {

        private final ChannelShell channel;

        private JschTestConnector(ChannelShell channel, InputStream input, OutputStream output) {
            super(input, output);
            this.channel = channel;
        }

        @Override
        public InputStream input() {
            try {
                return this.channel.getInputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public OutputStream output() {
            try {
                return this.channel.getOutputStream();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static final class SshjTestConnector extends TestConnector {

        private SshjTestConnector(InputStream input, OutputStream output) {
            super(input, output);
        }
    }

    private static final class OutputBuffer {

        private final StringBuilder content = new StringBuilder();

        private void append(char[] chars, int length) {
            synchronized (this.content) {
                this.content.append(chars, 0, length);
                this.content.notifyAll();
            }
        }

        private boolean waitFor(String text, long timeoutMs) throws InterruptedException {
            long deadline = System.currentTimeMillis() + timeoutMs;
            synchronized (this.content) {
                while (this.content.indexOf(text) < 0) {
                    long remaining = deadline - System.currentTimeMillis();
                    if (remaining <= 0) {
                        return false;
                    }
                    this.content.wait(remaining);
                }
                return true;
            }
        }

        @Override
        public String toString() {
            synchronized (this.content) {
                return this.content.toString();
            }
        }
    }
}
