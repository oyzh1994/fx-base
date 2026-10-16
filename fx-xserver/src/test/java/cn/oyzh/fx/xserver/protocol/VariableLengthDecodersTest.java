package cn.oyzh.fx.xserver.protocol;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.X11LittleEndianInputStream;
import com.github.moaxcp.x11.protocol.X11LittleEndianOutputStream;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xproto.Arc;
import com.github.moaxcp.x11.protocol.xproto.Char2b;
import com.github.moaxcp.x11.protocol.xproto.Coloritem;
import com.github.moaxcp.x11.protocol.xproto.FillPoly;
import com.github.moaxcp.x11.protocol.xproto.FreeColors;
import com.github.moaxcp.x11.protocol.xproto.Point;
import com.github.moaxcp.x11.protocol.xproto.PolyArc;
import com.github.moaxcp.x11.protocol.xproto.PolyFillArc;
import com.github.moaxcp.x11.protocol.xproto.PolyFillRectangle;
import com.github.moaxcp.x11.protocol.xproto.PolyLine;
import com.github.moaxcp.x11.protocol.xproto.PolyPoint;
import com.github.moaxcp.x11.protocol.xproto.PolyRectangle;
import com.github.moaxcp.x11.protocol.xproto.PolySegment;
import com.github.moaxcp.x11.protocol.xproto.PolyText16;
import com.github.moaxcp.x11.protocol.xproto.PolyText8;
import com.github.moaxcp.x11.protocol.xproto.PutImage;
import com.github.moaxcp.x11.protocol.xproto.QueryColors;
import com.github.moaxcp.x11.protocol.xproto.QueryTextExtents;
import com.github.moaxcp.x11.protocol.xproto.Rectangle;
import com.github.moaxcp.x11.protocol.xproto.Segment;
import com.github.moaxcp.x11.protocol.xproto.SetClipRectangles;
import com.github.moaxcp.x11.protocol.xproto.StoreColors;
import cn.oyzh.fx.xserver.protocol.VariableLengthDecoders;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Round trips every request that {@link VariableLengthDecoders} handles.
 *
 * <p>The generated writers are correct, so encoding a request with one and decoding it here is a
 * direct check of the custom decoders: a miscounted list shows up immediately as a missing element.
 * This is the test that would have caught the truncated text and missing button outlines that a
 * real client exposed.
 */
class VariableLengthDecodersTest {

    private static final int DRAWABLE = 0x00ABCDEF;
    private static final int GC = 0x00FEDCBA;

    @Test
    void polyPointRoundTrips() throws IOException {
        PolyPoint decoded = roundTrip(PolyPoint.builder()
                .coordinateMode((byte) 1)
                .drawable(DRAWABLE)
                .gc(GC)
                .points(points(2, 3, 4, 5, 6, 7))
                .build());
        assertEquals(3, decoded.getPoints().size());
        assertEquals(6, decoded.getPoints().get(2).getX());
        assertEquals((byte) 1, decoded.getCoordinateMode());
    }

    @Test
    void polyLineRoundTrips() throws IOException {
        PolyLine decoded = roundTrip(PolyLine.builder()
                .coordinateMode((byte) 0)
                .drawable(DRAWABLE)
                .gc(GC)
                .points(points(0, 0, 10, 10))
                .build());
        assertEquals(2, decoded.getPoints().size());
        assertEquals(10, decoded.getPoints().get(1).getY());
    }

    /** A short list is the case the generated decoder used to size negatively. */
    @Test
    void shortPolyLineRoundTrips() throws IOException {
        PolyLine decoded = roundTrip(PolyLine.builder()
                .coordinateMode((byte) 0)
                .drawable(DRAWABLE)
                .gc(GC)
                .points(points(1, 1))
                .build());
        assertEquals(1, decoded.getPoints().size());
    }

    @Test
    void polySegmentRoundTrips() throws IOException {
        PolySegment decoded = roundTrip(PolySegment.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .segments(Lists.immutable.of(
                        Segment.builder().x1((short) 1).y1((short) 2).x2((short) 3).y2((short) 4)
                                .build(),
                        Segment.builder().x1((short) 5).y1((short) 6).x2((short) 7).y2((short) 8)
                                .build()))
                .build());
        assertEquals(2, decoded.getSegments().size());
        assertEquals(8, decoded.getSegments().get(1).getY2());
    }

    @Test
    void polyRectangleRoundTrips() throws IOException {
        PolyRectangle decoded = roundTrip(PolyRectangle.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .rectangles(Lists.immutable.of(
                        Rectangle.builder().x((short) 1).y((short) 2).width((short) 3)
                                .height((short) 4).build()))
                .build());
        assertEquals(1, decoded.getRectangles().size());
        assertEquals(3, decoded.getRectangles().get(0).getWidth());
    }

    @Test
    void polyArcRoundTrips() throws IOException {
        PolyArc decoded = roundTrip(PolyArc.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .arcs(Lists.immutable.of(arc()))
                .build());
        assertEquals(1, decoded.getArcs().size());
        assertEquals(180, decoded.getArcs().get(0).getAngle2());
    }

    @Test
    void fillPolyRoundTrips() throws IOException {
        FillPoly decoded = roundTrip(FillPoly.builder()
                .shape((byte) 2)
                .coordinateMode((byte) 0)
                .drawable(DRAWABLE)
                .gc(GC)
                .points(points(1, 1, 5, 1, 3, 5))
                .build());
        assertEquals(3, decoded.getPoints().size());
        assertEquals((byte) 2, decoded.getShape());
        assertEquals(5, decoded.getPoints().get(2).getY());
    }

    @Test
    void polyFillRectangleRoundTrips() throws IOException {
        PolyFillRectangle decoded = roundTrip(PolyFillRectangle.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .rectangles(Lists.immutable.of(
                        Rectangle.builder().x((short) 7).y((short) 8).width((short) 9)
                                .height((short) 10).build()))
                .build());
        assertEquals(9, decoded.getRectangles().get(0).getWidth());
    }

    @Test
    void polyFillArcRoundTrips() throws IOException {
        PolyFillArc decoded = roundTrip(PolyFillArc.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .arcs(Lists.immutable.of(arc()))
                .build());
        assertEquals(90, decoded.getArcs().get(0).getAngle1());
    }

    @Test
    void setClipRectanglesRoundTrips() throws IOException {
        SetClipRectangles decoded = roundTrip(SetClipRectangles.builder()
                .ordering((byte) 0)
                .gc(GC)
                .clipXOrigin((short) 3)
                .clipYOrigin((short) 4)
                .rectangles(Lists.immutable.of(
                        Rectangle.builder().x((short) 0).y((short) 0).width((short) 20)
                                .height((short) 30).build()))
                .build());
        assertEquals(1, decoded.getRectangles().size());
        assertEquals(20, decoded.getRectangles().get(0).getWidth());
        assertEquals(3, decoded.getClipXOrigin());
    }

    @Test
    void storeColorsRoundTrips() throws IOException {
        StoreColors decoded = roundTrip(StoreColors.builder()
                .cmap(0x20)
                .items(Lists.immutable.of(Coloritem.builder()
                        .pixel(0x1234)
                        .red((short) 0x1111)
                        .green((short) 0x2222)
                        .blue((short) 0x3333)
                        .flags((byte) 0x7)
                        .build()))
                .build());
        assertEquals(1, decoded.getItems().size());
        assertEquals(0x3333, decoded.getItems().get(0).getBlue() & 0xFFFF);
    }

    @Test
    void queryTextExtentsRoundTrips() throws IOException {
        QueryTextExtents decoded = roundTrip(QueryTextExtents.builder()
                .font(0x33)
                .string(Lists.immutable.of(
                        Char2b.builder().byte1((byte) 0).byte2((byte) 'A').build(),
                        Char2b.builder().byte1((byte) 0).byte2((byte) 'B').build(),
                        Char2b.builder().byte1((byte) 0).byte2((byte) 'C').build()))
                .build());
        assertEquals(3, decoded.getString().size());
        assertEquals('C', decoded.getString().get(2).getByte2());
    }

    @Test
    void queryColorsRoundTrips() throws IOException {
        QueryColors decoded = roundTrip(QueryColors.builder()
                .cmap(0x20)
                .pixels(IntLists.immutable.of(0x101010, 0x202020, 0x303030, 0x404040))
                .build());
        assertEquals(4, decoded.getPixels().size());
        assertEquals(0x404040, decoded.getPixels().get(3));
    }

    @Test
    void freeColorsRoundTrips() throws IOException {
        FreeColors decoded = roundTrip(FreeColors.builder()
                .cmap(0x20)
                .planeMask(0xFFFFFFFF)
                .pixels(IntLists.immutable.of(1, 2, 3))
                .build());
        assertEquals(3, decoded.getPixels().size());
    }

    @Test
    void polyText8RoundTrips() throws IOException {
        // One text item: length byte, characters, then the delta for the next item.
        byte[] items = {3, 'a', 'b', 'c', 2, 0};
        PolyText8 decoded = roundTrip(PolyText8.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .x((short) 4)
                .y((short) 5)
                .items(ByteLists.immutable.of(items))
                .build());
        // The request is padded to a four byte boundary; the zero padding reads as the
        // end-of-items marker, which is exactly how the handler stops.
        assertEquals(0, decoded.getItems().get(items.length) & 0xFF);
        assertEquals('a', decoded.getItems().get(1) & 0xFF);
    }

    @Test
    void polyText16RoundTrips() throws IOException {
        byte[] items = {2, 0, 'x', 0, 'y', 1, 0};
        PolyText16 decoded = roundTrip(PolyText16.builder()
                .drawable(DRAWABLE)
                .gc(GC)
                .x((short) 1)
                .y((short) 2)
                .items(ByteLists.immutable.of(items))
                .build());
        assertEquals(0, decoded.getItems().get(items.length) & 0xFF);
    }

    @Test
    void putImageRoundTrips() throws IOException {
        byte[] data = new byte[4 * 2 * 3];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        PutImage decoded = roundTrip(PutImage.builder()
                .format((byte) 2)
                .drawable(DRAWABLE)
                .gc(GC)
                .width((short) 4)
                .height((short) 2)
                .dstX((short) 6)
                .dstY((short) 7)
                .leftPad((byte) 0)
                .depth((byte) 24)
                .data(ByteLists.immutable.of(data))
                .build());
        assertEquals(data.length, decoded.getData().size());
        assertEquals((byte) (data.length - 1), decoded.getData().get(data.length - 1));
        assertEquals(7, decoded.getDstY());
    }

    // ------------------------------------------------------------------
    // Harness
    // ------------------------------------------------------------------

    /**
     * A request too large for the sixteen bit length field, which is therefore sent as a
     * BIG-REQUEST, must decode whole.
     *
     * <p>The field then describes nothing usable - 600000 bytes of image data of which it can name
     * only the low sixteen bits' worth - so the decoder has to size the trailing list from the
     * request length it was handed. Sizing it from the field, which is what this decoder used to do,
     * silently drops most of the image: a client that paints through {@code XPutImage} gets a
     * fraction of its window and no error to explain it.
     */
    @Test
    void putImageLargerThanTheLengthFieldDecodesWhole() throws IOException {
        int width = 500;
        int height = 300;
        byte[] data = new byte[width * height * 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) i;
        }
        PutImage request = PutImage.builder()
                .format((byte) 2)
                .drawable(DRAWABLE)
                .gc(GC)
                .width((short) width)
                .height((short) height)
                .dstX((short) 0)
                .dstY((short) 0)
                .leftPad((byte) 0)
                .depth((byte) 24)
                .data(ByteLists.immutable.of(data))
                .build();
        // The generated writer lays the request out, including the length field, which for a
        // request this size can only hold the low sixteen bits of the true length. That is exactly
        // the field a BIG-REQUESTS request carries, so what it produced is the wire image of one.
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        X11LittleEndianOutputStream out = new X11LittleEndianOutputStream(buffer);
        request.write((byte) 72, out);
        out.flush();
        byte[] encoded = buffer.toByteArray();

        X11Input in = new X11LittleEndianInputStream(
                new ByteArrayInputStream(encoded, 1, encoded.length - 1));
        PutImage decoded = (PutImage) VariableLengthDecoders.decode(72, in, encoded.length);

        assertEquals(data.length, decoded.getData().size(), "the image data was truncated");
        assertEquals(data[data.length - 1], decoded.getData().get(data.length - 1));
    }

    /**
     * A {@code PutImage} decoded from the request's own bytes is the same image the byte loop reads.
     *
     * <p>The request loop hands the decoder the array it built - the data byte, the length and the
     * body - so an image upload can be copied out in one call instead of read a byte at a time
     * through the generated input, which was measured at about 16 ms for a 1300x800 frame against
     * 0.5 ms. This pins that the two routes agree, byte for byte, including the truncated length
     * field of a request larger than the sixteen bit one.
     */
    @Test
    void putImageFromItsOwnBytesMatchesTheByteLoop() throws IOException {
        int width = 300;
        int height = 200;
        byte[] data = new byte[width * height * 4];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i * 31 + 5);
        }
        PutImage request = PutImage.builder()
                .format((byte) 2)
                .drawable(DRAWABLE)
                .gc(GC)
                .width((short) width)
                .height((short) height)
                .dstX((short) 3)
                .dstY((short) 4)
                .leftPad((byte) 0)
                .depth((byte) 24)
                .data(ByteLists.immutable.of(data))
                .build();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        X11LittleEndianOutputStream out = new X11LittleEndianOutputStream(buffer);
        request.write((byte) 72, out);
        out.flush();
        byte[] encoded = buffer.toByteArray();
        // What ClientConnection builds: everything after the opcode.
        byte[] decoderBytes = java.util.Arrays.copyOfRange(encoded, 1, encoded.length);

        PutImage loop = (PutImage) VariableLengthDecoders.decode(72,
                new X11LittleEndianInputStream(
                        new ByteArrayInputStream(encoded, 1, encoded.length - 1)),
                encoded.length);
        PutImage fast = (PutImage) VariableLengthDecoders.decode(72,
                new X11LittleEndianInputStream(
                        new ByteArrayInputStream(encoded, 1, encoded.length - 1)),
                encoded.length, decoderBytes);

        assertEquals(data.length, fast.getData().size(), "the image data was truncated");
        assertEquals(loop.getData(), fast.getData(), "the two routes read different image data");
        assertEquals(request.getDrawable(), fast.getDrawable());
        assertEquals(request.getWidth(), fast.getWidth());
    }

    /**
     * Encodes a request with the generated writer and decodes it with the server's decoder. The
     * decoder expects the input to start just after the opcode byte, which is how the request loop
     * hands it over.
     */
    @SuppressWarnings("unchecked")
    private static <T extends XRequest> T roundTrip(T request) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        X11LittleEndianOutputStream out = new X11LittleEndianOutputStream(buffer);
        request.write((byte) 0, out);
        out.flush();

        byte[] encoded = buffer.toByteArray();
        int opcode = encoded[0] & 0xFF;
        X11Input in = new X11LittleEndianInputStream(
                new ByteArrayInputStream(encoded, 1, encoded.length - 1));
        XRequest decoded = VariableLengthDecoders.decode(opcode, in, encoded.length);
        assertInstanceOf(request.getClass(), decoded,
                "decoder returned the wrong request type for opcode " + opcode);
        return (T) decoded;
    }

    private static org.eclipse.collections.api.list.ImmutableList<Point> points(int... coords) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < coords.length; i += 2) {
            points.add(Point.builder().x((short) coords[i]).y((short) coords[i + 1]).build());
        }
        return Lists.immutable.withAll(points);
    }

    private static Arc arc() {
        return Arc.builder()
                .x((short) 1)
                .y((short) 2)
                .width((short) 10)
                .height((short) 20)
                .angle1((short) 90)
                .angle2((short) 180)
                .build();
    }
}
