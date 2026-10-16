package cn.oyzh.fx.xserver.protocol;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.XObject;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.render.AddGlyphs;
import com.github.moaxcp.x11.protocol.render.AddTraps;
import com.github.moaxcp.x11.protocol.render.Animcursorelt;
import com.github.moaxcp.x11.protocol.render.Color;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs16;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs32;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs8;
import com.github.moaxcp.x11.protocol.render.CreateAnimCursor;
import com.github.moaxcp.x11.protocol.render.FillRectangles;
import com.github.moaxcp.x11.protocol.render.FreeGlyphs;
import com.github.moaxcp.x11.protocol.render.Glyphinfo;
import com.github.moaxcp.x11.protocol.render.Pointfix;
import com.github.moaxcp.x11.protocol.render.SetPictureClipRectangles;
import com.github.moaxcp.x11.protocol.render.SetPictureFilter;
import com.github.moaxcp.x11.protocol.render.Trap;
import com.github.moaxcp.x11.protocol.render.Trapezoid;
import com.github.moaxcp.x11.protocol.render.Trapezoids;
import com.github.moaxcp.x11.protocol.render.TriFan;
import com.github.moaxcp.x11.protocol.render.TriStrip;
import com.github.moaxcp.x11.protocol.render.Triangle;
import com.github.moaxcp.x11.protocol.render.Triangles;
import com.github.moaxcp.x11.protocol.xproto.Rectangle;
import cn.oyzh.fx.xserver.protocol.VariableLengthDecoders;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Decoders for the RENDER requests whose generated list length comes out negative.
 *
 * <p>This is the same defect {@link VariableLengthDecoders} works around for the core protocol: the
 * generated readers size their last list with {@code length - javaStart}, mixing the request length
 * in four byte units with a byte offset. For a typical {@code AddGlyphs} the argument is negative
 * and the reader throws {@code NegativeArraySizeException}, which reaches the client as a
 * {@code BadRequest} - so Xft fails on its first glyph upload and every toolkit that draws text
 * through RENDER gives up. Fourteen RENDER requests are decoded here instead.
 *
 * <p>Requests whose lists have an explicit count field are not affected and are decoded by the
 * generated code as usual.
 */
public final class RenderDecoders {

    // RENDER minor opcodes, taken from the generated request classes' own OPCODE constants.
    private static final int SET_PICTURE_CLIP_RECTANGLES = 6;
    private static final int TRAPEZOIDS = 10;
    private static final int TRIANGLES = 11;
    private static final int TRI_STRIP = 12;
    private static final int TRI_FAN = 13;
    private static final int ADD_GLYPHS = 20;
    private static final int FREE_GLYPHS = 22;
    private static final int COMPOSITE_GLYPHS_8 = 23;
    private static final int COMPOSITE_GLYPHS_16 = 24;
    private static final int COMPOSITE_GLYPHS_32 = 25;
    private static final int FILL_RECTANGLES = 26;
    private static final int SET_PICTURE_FILTER = 30;
    private static final int CREATE_ANIM_CURSOR = 31;
    private static final int ADD_TRAPS = 32;

    private RenderDecoders() {
    }

    /** True when this RENDER opcode needs the corrected decoder. */
    public static boolean handles(int minorOpcode) {
        return switch (minorOpcode) {
            case CREATE_ANIM_CURSOR, FREE_GLYPHS, COMPOSITE_GLYPHS_8, COMPOSITE_GLYPHS_16,
                 COMPOSITE_GLYPHS_32, FILL_RECTANGLES, SET_PICTURE_CLIP_RECTANGLES, ADD_GLYPHS,
                 ADD_TRAPS, TRAPEZOIDS, TRIANGLES, TRI_STRIP, TRI_FAN, SET_PICTURE_FILTER -> true;
            default -> false;
        };
    }

    /**
     * Decodes one of them. The opcode byte has already been consumed, so the input starts at the
     * minor opcode - the same position the generated reader expects.
     */
    public static XRequest decode(int minorOpcode, X11Input in) throws IOException {
        return switch (minorOpcode) {
            case CREATE_ANIM_CURSOR -> readCreateAnimCursor(in);
            case FREE_GLYPHS -> readFreeGlyphs(in);
            case COMPOSITE_GLYPHS_8 -> readCompositeGlyphs(in, 8);
            case COMPOSITE_GLYPHS_16 -> readCompositeGlyphs(in, 16);
            case COMPOSITE_GLYPHS_32 -> readCompositeGlyphs(in, 32);
            case FILL_RECTANGLES -> readFillRectangles(in);
            case SET_PICTURE_CLIP_RECTANGLES -> readSetPictureClipRectangles(in);
            case ADD_GLYPHS -> readAddGlyphs(in);
            case ADD_TRAPS -> readAddTraps(in);
            case TRAPEZOIDS -> readTrapezoids(in);
            case TRIANGLES -> readTriangles(in);
            case TRI_STRIP -> readTriStripOrFan(in, true);
            case TRI_FAN -> readTriStripOrFan(in, false);
            case SET_PICTURE_FILTER -> readSetPictureFilter(in);
            default -> throw new IllegalArgumentException(
                    "RENDER opcode " + minorOpcode + " is not custom decoded");
        };
    }

    // ------------------------------------------------------------------
    // Drawing requests
    // ------------------------------------------------------------------

    private static FillRectangles readFillRectangles(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        byte op = in.readByte();
        in.readPad(3);
        int dst = in.readCard32();
        Color color = Color.readColor(in);
        int n = count(length, 20, 8);
        List<Rectangle> rects = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            rects.add(Rectangle.readRectangle(in));
        }
        return FillRectangles.builder()
                .op(op)
                .dst(dst)
                .color(color)
                .rects(Lists.immutable.withAll(rects))
                .build();
    }

    private static Trapezoids readTrapezoids(X11Input in) throws IOException {
        DrawHeader header = readDrawHeader(in);
        int n = count(header.length, 24, 40);
        List<Trapezoid> traps = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            traps.add(Trapezoid.readTrapezoid(in));
        }
        return Trapezoids.builder()
                .op(header.op)
                .src(header.src)
                .dst(header.dst)
                .maskFormat(header.maskFormat)
                .srcX(header.srcX)
                .srcY(header.srcY)
                .traps(Lists.immutable.withAll(traps))
                .build();
    }

    private static Triangles readTriangles(X11Input in) throws IOException {
        DrawHeader header = readDrawHeader(in);
        int n = count(header.length, 24, 24);
        List<Triangle> triangles = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            triangles.add(Triangle.readTriangle(in));
        }
        return Triangles.builder()
                .op(header.op)
                .src(header.src)
                .dst(header.dst)
                .maskFormat(header.maskFormat)
                .srcX(header.srcX)
                .srcY(header.srcY)
                .triangles(Lists.immutable.withAll(triangles))
                .build();
    }

    private static XRequest readTriStripOrFan(X11Input in, boolean strip) throws IOException {
        DrawHeader header = readDrawHeader(in);
        int n = count(header.length, 24, 8);
        List<Pointfix> points = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            points.add(Pointfix.readPointfix(in));
        }
        if (strip) {
            return TriStrip.builder()
                    .op(header.op).src(header.src).dst(header.dst)
                    .maskFormat(header.maskFormat).srcX(header.srcX).srcY(header.srcY)
                    .points(Lists.immutable.withAll(points))
                    .build();
        }
        return TriFan.builder()
                .op(header.op).src(header.src).dst(header.dst)
                .maskFormat(header.maskFormat).srcX(header.srcX).srcY(header.srcY)
                .points(Lists.immutable.withAll(points))
                .build();
    }

    private static AddTraps readAddTraps(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int picture = in.readCard32();
        short xOff = in.readInt16();
        short yOff = in.readInt16();
        int n = count(length, 12, 24);
        List<Trap> traps = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            traps.add(Trap.readTrap(in));
        }
        return AddTraps.builder()
                .picture(picture)
                .xOff(xOff)
                .yOff(yOff)
                .traps(Lists.immutable.withAll(traps))
                .build();
    }

    /** The fields every RENDER draw request shares before its geometry: 24 bytes with the opcode. */
    private record DrawHeader(int length, byte op, int src, int dst, int maskFormat, short srcX,
                              short srcY) {
    }

    private static DrawHeader readDrawHeader(X11Input in) throws IOException {
        // The minor opcode byte, which the caller has already identified but the layout still has.
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        byte op = in.readByte();
        in.readPad(3);
        int src = in.readCard32();
        int dst = in.readCard32();
        int maskFormat = in.readCard32();
        short srcX = in.readInt16();
        short srcY = in.readInt16();
        return new DrawHeader(length, op, src, dst, maskFormat, srcX, srcY);
    }

    // ------------------------------------------------------------------
    // Pictures and cursors
    // ------------------------------------------------------------------

    private static SetPictureClipRectangles readSetPictureClipRectangles(X11Input in)
            throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int picture = in.readCard32();
        short clipXOrigin = in.readInt16();
        short clipYOrigin = in.readInt16();
        int n = count(length, 12, 8);
        List<Rectangle> rectangles = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            rectangles.add(Rectangle.readRectangle(in));
        }
        return SetPictureClipRectangles.builder()
                .picture(picture)
                .clipXOrigin(clipXOrigin)
                .clipYOrigin(clipYOrigin)
                .rectangles(Lists.immutable.withAll(rectangles))
                .build();
    }

    private static SetPictureFilter readSetPictureFilter(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int picture = in.readCard32();
        int filterLen = Short.toUnsignedInt(in.readCard16());
        in.readPad(2);
        byte[] filter = new byte[filterLen];
        for (int i = 0; i < filterLen; i++) {
            filter[i] = in.readByte();
        }
        in.readPad(XObject.getSizeForPadAlign(filterLen));
        // The values are whatever is left of the request once the name has been read and padded.
        int header = 4 + 4 + 2 + 2 + filterLen + XObject.getSizeForPadAlign(filterLen);
        int n = count(length, header, 4);
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = in.readInt32();
        }
        return SetPictureFilter.builder()
                .picture(picture)
                .filter(ByteLists.immutable.of(filter))
                .values(IntLists.immutable.of(values))
                .build();
    }

    private static CreateAnimCursor readCreateAnimCursor(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int cid = in.readCard32();
        int n = count(length, 8, 8);
        List<Animcursorelt> cursors = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            cursors.add(Animcursorelt.readAnimcursorelt(in));
        }
        return CreateAnimCursor.builder()
                .cid(cid)
                .cursors(Lists.immutable.withAll(cursors))
                .build();
    }

    // ------------------------------------------------------------------
    // Glyphs
    // ------------------------------------------------------------------

    private static AddGlyphs readAddGlyphs(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int glyphset = in.readCard32();
        int glyphsLen = in.readCard32();
        int[] glyphids = new int[glyphsLen];
        for (int i = 0; i < glyphsLen; i++) {
            glyphids[i] = in.readCard32();
        }
        List<Glyphinfo> glyphs = new ArrayList<>(glyphsLen);
        for (int i = 0; i < glyphsLen; i++) {
            glyphs.add(Glyphinfo.readGlyphinfo(in));
        }
        // The glyph records are followed by their bitmap data, which is everything that is left.
        int header = 4 + 4 + 4 + glyphsLen * 4 + glyphsLen * 12;
        int dataLen = count(length, header, 1);
        byte[] data = new byte[dataLen];
        for (int i = 0; i < dataLen; i++) {
            data[i] = in.readByte();
        }
        return AddGlyphs.builder()
                .glyphset(glyphset)
                .glyphids(IntLists.immutable.of(glyphids))
                .glyphs(Lists.immutable.withAll(glyphs))
                .data(ByteLists.immutable.of(data))
                .build();
    }

    /** CompositeGlyphs8, 16 and 32 differ only in the width of the characters in the command stream. */
    private static XRequest readCompositeGlyphs(X11Input in, int bits) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        byte op = in.readByte();
        in.readPad(3);
        int src = in.readCard32();
        int dst = in.readCard32();
        int maskFormat = in.readCard32();
        int glyphset = in.readCard32();
        short srcX = in.readInt16();
        short srcY = in.readInt16();
        int dataLen = count(length, 28, 1);
        byte[] glyphcmds = new byte[dataLen];
        for (int i = 0; i < dataLen; i++) {
            glyphcmds[i] = in.readByte();
        }
        return switch (bits) {
            case 8 -> CompositeGlyphs8.builder()
                    .op(op).src(src).dst(dst).maskFormat(maskFormat).glyphset(glyphset)
                    .srcX(srcX).srcY(srcY)
                    .glyphcmds(ByteLists.immutable.of(glyphcmds))
                    .build();
            case 16 -> CompositeGlyphs16.builder()
                    .op(op).src(src).dst(dst).maskFormat(maskFormat).glyphset(glyphset)
                    .srcX(srcX).srcY(srcY)
                    .glyphcmds(ByteLists.immutable.of(glyphcmds))
                    .build();
            default -> CompositeGlyphs32.builder()
                    .op(op).src(src).dst(dst).maskFormat(maskFormat).glyphset(glyphset)
                    .srcX(srcX).srcY(srcY)
                    .glyphcmds(ByteLists.immutable.of(glyphcmds))
                    .build();
        };
    }

    private static FreeGlyphs readFreeGlyphs(X11Input in) throws IOException {
        in.readPad(1);
        int length = Short.toUnsignedInt(in.readCard16());
        int glyphset = in.readCard32();
        int n = count(length, 8, 4);
        int[] glyphs = new int[n];
        for (int i = 0; i < n; i++) {
            glyphs[i] = in.readCard32();
        }
        return FreeGlyphs.builder()
                .glyphset(glyphset)
                .glyphs(IntLists.immutable.of(glyphs))
                .build();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /**
     * Elements in the list that fills the rest of a request, where {@code headerBytes} is the fixed
     * part counted from the start of the request including its opcode.
     */
    private static int count(int lengthUnits, int headerBytes, int elementBytes) {
        int available = lengthUnits * 4 - headerBytes;
        if (available <= 0) {
            return 0;
        }
        return available / elementBytes;
    }
}
