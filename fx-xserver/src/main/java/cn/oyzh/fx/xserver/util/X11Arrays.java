package cn.oyzh.fx.xserver.util;

import java.util.Collection;

/**
 * Helpers for the primitive array fields used by the X11 protocol codecs.
 *
 * <p>The protocol classes describe wire data as primitive arrays. These methods keep call sites
 * concise and handle both varargs values and existing arrays.</p>
 */
public final class X11Arrays {
    private X11Arrays() {
    }

    public static byte[] bytes(byte... values) {
        return values;
    }

    public static int[] ints(int... values) {
        return values;
    }

    public static short[] shorts(short... values) {
        return values;
    }

    public static byte[] bytesOf(Collection<Byte> values) {
        byte[] result = new byte[values.size()];
        int index = 0;
        for (Byte value : values) {
            result[index++] = value;
        }
        return result;
    }

    public static int[] intsOf(Collection<Integer> values) {
        int[] result = new int[values.size()];
        int index = 0;
        for (Integer value : values) {
            result[index++] = value;
        }
        return result;
    }

    public static short[] shortsOf(Collection<Short> values) {
        short[] result = new short[values.size()];
        int index = 0;
        for (Short value : values) {
            result[index++] = value;
        }
        return result;
    }
}
