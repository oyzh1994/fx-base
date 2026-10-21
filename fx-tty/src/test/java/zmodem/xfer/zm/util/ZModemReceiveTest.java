package zmodem.xfer.zm.util;

import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.junit.Test;
import zmodem.util.CustomFile;
import zmodem.xfer.zm.packet.Cancel;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.packet.Format;
import zmodem.xfer.zm.packet.Header;
import zmodem.zm.io.ZMPacketOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * ZModem接收状态机测试。
 */
public class ZModemReceiveTest {

    @Test
    public void cancellationStopsProcessingRemainingProtocolData() throws Exception {
        Path dir = Files.createTempDirectory("zmodem-receive-cancel-");
        byte[] firstBlock = new byte[128];
        byte[] secondBlock = new byte[128];
        for (int i = 0; i < firstBlock.length; i++) {
            firstBlock[i] = (byte) i;
            secondBlock[i] = (byte) (255 - i);
        }
        String metadata = "128 0 0 0 0 0";
        byte[] fileData = "test.bin\0".getBytes(StandardCharsets.UTF_8);
        byte[] namePacket = new byte[fileData.length + metadata.length() + 1];
        System.arraycopy(fileData, 0, namePacket, 0, fileData.length);
        System.arraycopy(metadata.getBytes(StandardCharsets.UTF_8), 0, namePacket, fileData.length, metadata.length());

        ByteArrayInputStream input = new ByteArrayInputStream(concat(
                packetBytes(new Header(Format.HEX, ZModemCharacter.ZRQINIT, new byte[]{0, 0, 0, 0})),
                packetBytes(new Header(Format.HEX, ZModemCharacter.ZFILE, new byte[]{0, 0, 0, 0})),
                packetBytes(new DataPacket(ZModemCharacter.ZCRCW, namePacket)),
                packetBytes(new Header(Format.HEX, ZModemCharacter.ZDATA, 0)),
                packetBytes(new DataPacket(ZModemCharacter.ZCRCG, firstBlock)),
                packetBytes(new DataPacket(ZModemCharacter.ZCRCG, secondBlock)),
                packetBytes(new Cancel())));
        AtomicInteger transferred = new AtomicInteger();
        ZModemReceive receiver = new ZModemReceive(() -> new CustomFile(dir.toFile()), input,
                new ByteArrayOutputStream());
        receiver.addCopyStreamListener(new CopyStreamListener() {
            @Override
            public void bytesTransferred(CopyStreamEvent event) {
                transferred.incrementAndGet();
            }

            @Override
            public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
            }
        });

        receiver.receive(() -> transferred.get() >= 2);

        Path received = dir.resolve("test.bin");
        assertTrue(input.available() > 0);
        assertEquals(firstBlock.length, Files.size(received));
        assertArrayEquals(firstBlock, Files.readAllBytes(received));
        Files.deleteIfExists(received);
        Files.deleteIfExists(dir);
    }

    private static byte[] packetBytes(zmodem.xfer.zm.util.ZMPacket packet) throws IOException {
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
}
