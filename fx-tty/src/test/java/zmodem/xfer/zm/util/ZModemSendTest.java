package zmodem.xfer.zm.util;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.junit.Test;
import zmodem.util.CustomFile;
import zmodem.util.FileAdapter;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketInputStream;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ZModem发送状态机测试。
 */
public class ZModemSendTest {

    @Test
    public void checksumErrorDoesNotReprocessPreviousPacket() throws Exception {
        Path file = Files.createTempFile("zmodem-send-", ".bin");
        Files.write(file, new byte[100]);
        List<FileAdapter> files = Collections.singletonList(new CustomFile(file.toFile()));

        byte[] zrinit = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRINIT, new byte[]{0, 0, 0, 0}));
        byte[] zack = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, new byte[]{0, 0, 0, 0}));
        byte[] zfin = packetBytes(new Header(Format.HEX, ZModemCharacter.ZFIN, new byte[]{0, 0, 0, 0}));
        byte[] corruptAck = zack.clone();
        int crcIndex = corruptAck.length - 4;
        corruptAck[crcIndex] = corruptAck[crcIndex] == '0' ? (byte) '1' : (byte) '0';

        ByteArrayInputStream input = new ByteArrayInputStream(concat(zrinit, corruptAck, zack, zfin));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZModemSend sender = new ZModemSend(() -> files, input, output);
        sender.send(() -> false);

        int zfileCount = 0;
        int dataCount = 0;
        boolean afterZData = false;
        ZMPacketInputStream packets = new ZMPacketInputStream(new ByteArrayInputStream(output.toByteArray()));
        try {
            ZMPacket packet;
            while ((packet = packets.read()) != null) {
                if (packet instanceof Header header) {
                    if (header.type() == ZModemCharacter.ZFILE) {
                        zfileCount++;
                    }
                    afterZData = header.type() == ZModemCharacter.ZDATA;
                } else if (packet instanceof DataPacket && afterZData) {
                    dataCount++;
                    afterZData = false;
                }
            }
        } catch (IOException ignored) {
            // 输出结束
        }

        assertEquals(1, zfileCount);
        assertTrue(dataCount > 0);
        Files.deleteIfExists(file);
    }

    @Test
    public void repositionsToAbsoluteOffsetWhenRetransmitting() throws Exception {
        Path file = Files.createTempFile("zmodem-send-retransmit-", ".bin");
        byte[] content = new byte[20 * 1024];
        for (int i = 0; i < content.length; i++) {
            content[i] = (byte) (i * 37 + 11);
        }
        Files.write(file, content);
        List<FileAdapter> files = Collections.singletonList(new CustomFile(file.toFile()));

        byte[] zrinit = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRINIT, new byte[]{0, 0, 0, 0}));
        byte[] zrpos = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRPOS, 0));
        byte[] zfin = packetBytes(new Header(Format.HEX, ZModemCharacter.ZFIN, new byte[]{0, 0, 0, 0}));
        ByteArrayInputStream input = new ByteArrayInputStream(concat(zrinit, zrpos, zrpos, zfin));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZModemSend sender = new ZModemSend(() -> files, input, output);
        sender.send(() -> false);

        List<byte[]> blocks = new ArrayList<>();
        ZMPacketInputStream packets = new ZMPacketInputStream(new ByteArrayInputStream(output.toByteArray()));
        ZMPacket packet;
        while (blocks.size() < 2 && (packet = packets.read()) != null) {
            if (packet instanceof Header header && header.type() == ZModemCharacter.ZDATA) {
                DataPacket data = (DataPacket) packets.read();
                blocks.add(data.data());
            }
        }

        assertEquals(2, blocks.size());
        assertArrayEquals(blocks.get(0), blocks.get(1));
        Files.deleteIfExists(file);
    }

    @Test
    public void duplicateZrinitDoesNotCloseActiveFile() throws Exception {
        Path file = Files.createTempFile("zmodem-send-duplicate-zrinit-", ".bin");
        Files.write(file, new byte[32 * 1024]);
        List<FileAdapter> files = Collections.singletonList(new CustomFile(file.toFile()));

        byte[] zrinit = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRINIT, new byte[]{0, 0, 0, 0}));
        byte[] zrpos = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRPOS, 0));
        byte[] zack1 = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, 8 * 1024));
        byte[] zack2 = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, 16 * 1024));
        byte[] zack3 = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, 24 * 1024));
        byte[] zack4 = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, 32 * 1024));
        byte[] zfin = packetBytes(new Header(Format.HEX, ZModemCharacter.ZFIN, new byte[]{0, 0, 0, 0}));

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZModemSend sender = new ZModemSend(() -> files,
                new ByteArrayInputStream(concat(zrinit, zrpos, zrinit, zack1, zack2, zack3, zack4, zfin)),
                output);

        sender.send(() -> false);

        int zfileCount = 0;
        int dataCount = 0;
        boolean afterZData = false;
        ZMPacketInputStream packets = new ZMPacketInputStream(new ByteArrayInputStream(output.toByteArray()));
        try {
            ZMPacket packet;
            while ((packet = packets.read()) != null) {
                if (packet instanceof Header header) {
                    if (header.type() == ZModemCharacter.ZFILE) {
                        zfileCount++;
                    }
                    afterZData = header.type() == ZModemCharacter.ZDATA;
                } else if (packet instanceof DataPacket && afterZData) {
                    dataCount++;
                    afterZData = false;
                }
            }
        } catch (IOException ignored) {
            // 输出结束
        }

        assertEquals(2, zfileCount);
        assertEquals(5, dataCount);
        Files.deleteIfExists(file);
    }

    @Test
    public void runtimeFailureSendsCancelSequence() throws Exception {
        Path file = Files.createTempFile("zmodem-send-cancel-", ".bin");
        Files.write(file, new byte[128]);
        List<FileAdapter> files = Collections.singletonList(new CustomFile(file.toFile()));
        byte[] zrinit = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRINIT, new byte[]{0, 0, 0, 0}));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZModemSend sender = new ZModemSend(() -> files, new ByteArrayInputStream(zrinit), output);
        sender.addCopyStreamListener(new CopyStreamListener() {
            @Override
            public void bytesTransferred(CopyStreamEvent event) {
                throw new IllegalStateException("simulated listener failure");
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                throw new IllegalStateException("simulated listener failure");
            }
        });

        try {
            sender.send(() -> false);
        } catch (IllegalStateException expected) {
            assertTrue(endsWithCancel(output.toByteArray()));
            Files.deleteIfExists(file);
            return;
        }
        throw new AssertionError("runtime failure was not propagated");
    }

    @Test
    public void ignoresDuplicateAckAfterEof() throws Exception {
        Path file = Files.createTempFile("zmodem-send-duplicate-ack-", ".bin");
        Files.write(file, new byte[8 * 1024]);
        List<FileAdapter> files = Collections.singletonList(new CustomFile(file.toFile()));
        byte[] zrinit = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRINIT, new byte[]{0, 0, 0, 0}));
        byte[] zrpos = packetBytes(new Header(Format.HEX, ZModemCharacter.ZRPOS, 0));
        byte[] zack = packetBytes(new Header(Format.HEX, ZModemCharacter.ZACK, 8 * 1024));
        byte[] zfin = packetBytes(new Header(Format.HEX, ZModemCharacter.ZFIN, new byte[]{0, 0, 0, 0}));
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ZModemSend sender = new ZModemSend(() -> files,
                new ByteArrayInputStream(concat(zrinit, zrpos, zack, zack, zfin)), output);

        sender.send(() -> false);

        int eofCount = 0;
        ZMPacketInputStream packets = new ZMPacketInputStream(new ByteArrayInputStream(output.toByteArray()));
        try {
            ZMPacket packet;
            while ((packet = packets.read()) != null) {
                if (packet instanceof Header header && header.type() == ZModemCharacter.ZEOF) {
                    eofCount++;
                }
            }
        } catch (IOException ignored) {
            // 输出结束
        }
        assertEquals(1, eofCount);
        Files.deleteIfExists(file);
    }

    private static byte[] packetBytes(ZMPacket packet) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new ZMPacketOutputStream(output).write(packet);
        return output.toByteArray();
    }

    private static byte[] concat(byte[]... parts) {
        int length = 0;
        for (byte[] part : parts) {
            length += part.length;
        }
        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, offset, part.length);
            offset += part.length;
        }
        return result;
    }

    private static boolean endsWithCancel(byte[] data) {
        byte[] cancel = packetBytesUnchecked(new Cancel());
        if (data.length < cancel.length) {
            return false;
        }
        for (int i = 0; i < cancel.length; i++) {
            if (data[data.length - cancel.length + i] != cancel[i]) {
                return false;
            }
        }
        return true;
    }

    private static byte[] packetBytesUnchecked(ZMPacket packet) {
        try {
            return packetBytes(packet);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
