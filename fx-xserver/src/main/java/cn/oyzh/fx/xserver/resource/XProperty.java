package cn.oyzh.fx.xserver.resource;

/**
 * A window property: a named, typed blob of data that clients publish and read.
 *
 * <p>Window managers live on these - {@code WM_NAME}, {@code WM_NORMAL_HINTS},
 * {@code _NET_WM_STATE} - and toolkits use them to pass clipboard contents around, so they are
 * stored verbatim.
 *
 * <p>Values are held in big-endian order regardless of which client wrote them, and converted to
 * the reading client's byte order on the way out. A property written by a little-endian client and
 * read by a big-endian one would otherwise come back byte swapped, which breaks the 16 and 32 bit
 * formats that toolkits rely on.
 */
public final class XProperty {

    public static final int FORMAT_8 = 8;
    public static final int FORMAT_16 = 16;
    public static final int FORMAT_32 = 32;

    private final int nameAtom;
    private int typeAtom;
    private int format;
    private byte[] data;

    public XProperty(int nameAtom, int typeAtom, int format, byte[] data) {
        this.nameAtom = nameAtom;
        this.typeAtom = typeAtom;
        this.format = format;
        this.data = data;
    }

    public int nameAtom() {
        return nameAtom;
    }

    public int typeAtom() {
        return typeAtom;
    }

    public void setTypeAtom(int typeAtom) {
        this.typeAtom = typeAtom;
    }

    public int format() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    /** Raw value in big-endian order. */
    public byte[] data() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    /** Number of items, where an item is 1, 2 or 4 bytes depending on the format. */
    public int itemCount() {
        int unit = format / 8;
        return unit == 0 ? 0 : data.length / unit;
    }

    /** Size in bytes of {@code count} items of this property's format. */
    public int byteLength(int count) {
        return count * (format / 8);
    }
}
