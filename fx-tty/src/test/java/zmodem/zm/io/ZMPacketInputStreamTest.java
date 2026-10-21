package zmodem.zm.io;

import org.junit.Test;
import zmodem.xfer.zm.packet.DataPacket;
import zmodem.xfer.zm.util.ZMPacket;
import zmodem.xfer.zm.util.ZModemCharacter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * ZModem协议包解析测试。
 */
public class ZMPacketInputStreamTest {

    @Test
    public void readsDataPacketWhenEveryByteIsEscaped() throws Exception {
        byte[] content = new byte[8 * 1024];
        Arrays.fill(content, ZModemCharacter.ZDLE.value());
        DataPacket packet = new DataPacket(ZModemCharacter.ZCRCW, content);

        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        new ZMPacketOutputStream(encoded).write(packet);
        ZMPacketInputStream input = new ZMPacketInputStream(
                new ByteArrayInputStream(encoded.toByteArray()));

        DataPacket decoded = (DataPacket) input.read();
        assertEquals(ZModemCharacter.ZCRCW, decoded.type());
        assertArrayEquals(content, decoded.data());
    }
}
