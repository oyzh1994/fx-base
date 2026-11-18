package cn.oyzh.fx.plus.format;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class DigitalFormatBigIntegerTest {

    private static final String LARGE_INTEGER = "9999999999999999999999999999";

    @Test
    public void formatsBigIntegerWithoutDoubleRounding() {
        assertEquals(LARGE_INTEGER, new DigitalFormat().format(new BigInteger(LARGE_INTEGER)));
    }

    @Test
    public void formatsIntegerStringWithoutDoubleRounding() {
        assertEquals(LARGE_INTEGER, new DigitalFormat().format(LARGE_INTEGER));
    }
}
