package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.render.AddGlyphs;
import com.github.moaxcp.x11.protocol.render.AddTraps;
import com.github.moaxcp.x11.protocol.render.ChangePicture;
import com.github.moaxcp.x11.protocol.render.Color;
import com.github.moaxcp.x11.protocol.render.Composite;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs16;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs32;
import com.github.moaxcp.x11.protocol.render.CompositeGlyphs8;
import com.github.moaxcp.x11.protocol.render.Cp;
import com.github.moaxcp.x11.protocol.render.CreateAnimCursor;
import com.github.moaxcp.x11.protocol.render.CreateConicalGradient;
import com.github.moaxcp.x11.protocol.render.CreateCursor;
import com.github.moaxcp.x11.protocol.render.CreateGlyphSet;
import com.github.moaxcp.x11.protocol.render.CreateLinearGradient;
import com.github.moaxcp.x11.protocol.render.CreatePicture;
import com.github.moaxcp.x11.protocol.render.CreateRadialGradient;
import com.github.moaxcp.x11.protocol.render.CreateSolidFill;
import com.github.moaxcp.x11.protocol.render.Directformat;
import com.github.moaxcp.x11.protocol.render.FillRectangles;
import com.github.moaxcp.x11.protocol.render.FreeGlyphSet;
import com.github.moaxcp.x11.protocol.render.FreeGlyphs;
import com.github.moaxcp.x11.protocol.render.FreePicture;
import com.github.moaxcp.x11.protocol.render.Glyphinfo;
import com.github.moaxcp.x11.protocol.render.Indexvalue;
import com.github.moaxcp.x11.protocol.render.Linefix;
import com.github.moaxcp.x11.protocol.render.PictOp;
import com.github.moaxcp.x11.protocol.render.Pictdepth;
import com.github.moaxcp.x11.protocol.render.Pictforminfo;
import com.github.moaxcp.x11.protocol.render.Pictscreen;
import com.github.moaxcp.x11.protocol.render.Pictvisual;
import com.github.moaxcp.x11.protocol.render.Pointfix;
import com.github.moaxcp.x11.protocol.render.QueryFilters;
import com.github.moaxcp.x11.protocol.render.QueryFiltersReply;
import com.github.moaxcp.x11.protocol.render.QueryPictFormats;
import com.github.moaxcp.x11.protocol.render.QueryPictFormatsReply;
import com.github.moaxcp.x11.protocol.render.QueryPictIndexValues;
import com.github.moaxcp.x11.protocol.render.QueryPictIndexValuesReply;
import com.github.moaxcp.x11.protocol.render.QueryVersion;
import com.github.moaxcp.x11.protocol.render.QueryVersionReply;
import com.github.moaxcp.x11.protocol.render.ReferenceGlyphSet;
import com.github.moaxcp.x11.protocol.render.RenderPlugin;
import com.github.moaxcp.x11.protocol.render.Repeat;
import com.github.moaxcp.x11.protocol.render.SetPictureClipRectangles;
import com.github.moaxcp.x11.protocol.render.SetPictureFilter;
import com.github.moaxcp.x11.protocol.render.SetPictureTransform;
import com.github.moaxcp.x11.protocol.render.SubPixel;
import com.github.moaxcp.x11.protocol.render.Transform;
import com.github.moaxcp.x11.protocol.render.Trapezoid;
import com.github.moaxcp.x11.protocol.render.Trapezoids;
import com.github.moaxcp.x11.protocol.render.TriFan;
import com.github.moaxcp.x11.protocol.render.TriStrip;
import com.github.moaxcp.x11.protocol.render.Triangle;
import com.github.moaxcp.x11.protocol.render.Triangles;
import com.github.moaxcp.x11.protocol.xproto.Str;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Compositor;
import cn.oyzh.fx.xserver.draw.PixelStore;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.resource.GlyphSet;
import cn.oyzh.fx.xserver.resource.PictFormat;
import cn.oyzh.fx.xserver.resource.Picture;
import cn.oyzh.fx.xserver.resource.XColormap;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;
import org.eclipse.collections.impl.factory.primitive.IntLists;
import org.eclipse.collections.impl.factory.primitive.ShortLists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RENDER.
 *
 * <p>RENDER is the extension modern toolkits cannot work without. GTK 3, Qt 5, cairo and Xft all
 * probe it before anything else, and a server that answers "not present" gets no toolkit at all:
 * the client exits, or falls back to drawing nothing. It is what turns an X server into one that
 * can show a window.
 *
 * <p>Two ideas carry the whole extension. The first is the <em>picture</em>: a drawable plus a
 * {@link PictFormat}, which says how its bits divide into colour and alpha. The second is
 * <em>compositing</em>: every drawing request reduces to combining a source with the destination
 * through a {@code PictOp}, attenuated by a mask. Text, filled paths, images and cursors are all
 * that one operation with a different source or mask, which is why cairo can express a whole GUI
 * in it.
 *
 * <h2>What is implemented</h2>
 *
 * <ul>
 *   <li>The version queries, including {@code QueryPictFormats}, whose reply clients use to decide
 *       whether the server is worth talking to. The format table is built from the screen's visuals
 *       and adds the standard A1, A4, A8, RGB24 and ARGB32 formats by exact mask layout, because
 *       cairo and Xft look those up by shape rather than by id and quietly conclude the server has
 *       no alpha when one is missing.</li>
 *   <li>Pictures, with every attribute: repeat, alpha map, clip mask, clip rectangles, clip origin,
 *       transform, filter and component alpha.</li>
 *   <li>{@code Composite} with every operator in {@link PictOp}, evaluated exactly as the
 *       specification's {@code Fa}/{@code Fb} table defines it - on premultiplied channels, with
 *       the server's non-premultiplied ARGB at either end.</li>
 *   <li>{@code FillRectangles}, and {@code Trapezoids}, {@code Triangles}, {@code TriStrip} and
 *       {@code TriFan} as antialiased filled polygons, which is the form cairo draws every path
 *       in.</li>
 *   <li>Glyph sets and text: {@code AddGlyphs} decodes A1, A4, A8 and ARGB32 glyph images and
 *       {@code CompositeGlyphs8/16/32} draws them as coverage masks. This is the Xft path; without
 *       it, text does not appear at all.</li>
 *   <li>{@code CreateSolidFill} and the linear, radial and conical gradients, evaluated in picture
 *       space on demand.</li>
 *   <li>{@code CreateCursor}, {@code CreateAnimCursor}, {@code AddTraps}, and the clip and
 *       transform setters.</li>
 * </ul>
 *
 * <h2>Approximations and gaps</h2>
 *
 * <ul>
 *   <li>{@code QueryDithers} (opcode 3) is not decoded by the generated protocol plugin, so this
 *       class never sees it and cannot answer it. It was deprecated before RENDER 1.0 and nothing
 *       has used it this century.</li>
 *   <li>{@code CreateAnimCursor} builds a cursor from the first element of the animation: the
 *       server's {@link XCursor} holds one image and has no clock, so a multi-frame cursor shows
 *       its first frame rather than cycling.</li>
 *   <li>Subwindow mode is recorded but not honoured, because the server keeps one image per screen
 *       and has no separate "contents including inferiors" to read. See {@link Picture}.</li>
 *   <li>Filters are recorded, reported and resolved: {@code nearest} and {@code bilinear} are both
 *       really implemented, and every alias maps onto one of them. A filter name the server does
 *       not know is sampled nearest rather than refused.</li>
 *   <li>The four non-separable HSL blend operators are treated as {@code Over}. The other eight
 *       blend modes are exact.</li>
 * </ul>
 *
 * <p>Errors use the extension's own codes, {@code BadPictFormat}, {@code BadPicture},
 * {@code BadPictOp}, {@code BadGlyphSet} and {@code BadGlyph}, allocated in that order from
 * {@link #firstError()}. The last is reserved rather than raised, for the reason given on
 * {@link #ERROR_PICT_FORMAT}.
 */
public final class RenderExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(RenderExtension.class);

    /** 0.11 is the last release with a specification, and everything it adds is answered here. */
    private static final int MAJOR_VERSION = 0;
    private static final int MINOR_VERSION = 11;

    /**
     * Offsets from {@link #firstError()}, in the order the generated plugin reads them back. The
     * fifth, {@code BadGlyph}, is reserved but never raised: RENDER lists it for
     * {@code CompositeGlyphs}, yet the reference server drops a glyph a set does not contain
     * instead of failing the request, and a client that hits one wants the rest of the string
     * drawn. The code still has to stay in the range so the plugin's table lines up.
     */
    private static final int ERROR_PICT_FORMAT = 0;
    private static final int ERROR_PICTURE = 1;
    private static final int ERROR_PICT_OP = 2;
    private static final int ERROR_GLYPH_SET = 3;
    private static final int ERROR_COUNT = 5;

    /** The largest cursor this server will build from a picture, as a real server bounds it too. */
    private static final int MAX_CURSOR_SIZE = 256;

    /** The filters the server answers to, in the order {@link #FILTER_ALIASES} indexes them. */
    private static final List<String> FILTER_NAMES =
            List.of("nearest", "bilinear", "convolution", "fast", "good", "best");

    /** Alias to filter index: fast is nearest, and both good and best are bilinear. */
    private static final short[] FILTER_ALIASES = {0, 1, 1};

    /** What a glyph element's length byte means when it is really a glyph set switch. */
    private static final int GLYPH_ITEM = 255;

    /** The eight byte element header: length, three bytes of padding, and two deltas. */
    private static final int GLYPH_HEADER = 8;

    /** A glyph set switch is that header, with the deltas zero, followed by the new set id. */
    private static final int GLYPH_SWITCH = GLYPH_HEADER + 4;

    private final RenderPlugin plugin = new RenderPlugin();

    private XServer server;
    private Compositor compositor;

    private List<PictFormat> formats = List.of();
    private Map<Integer, PictFormat> formatsById = Map.of();
    private Map<Integer, PictFormat> formatsByVisual = Map.of();
    private PictFormat fallback;

    @Override
    public String name() {
        return "RENDER";
    }

    @Override
    public byte majorOpcode() {
        return plugin.getMajorOpcode();
    }

    @Override
    public byte firstEvent() {
        return plugin.getFirstEvent();
    }

    @Override
    public byte firstError() {
        return plugin.getFirstError();
    }

    /**
     * Event codes this extension defines. RENDER has no events at all, so the range it asks the
     * registry for is empty.
     */
    public int eventCount() {
        return 0;
    }

    /**
     * Error codes this extension defines: {@code BadPictFormat}, {@code BadPicture},
     * {@code BadPictOp}, {@code BadGlyphSet} and {@code BadGlyph}, in that order. This is the
     * number to register the extension with.
     */
    public int errorCount() {
        return ERROR_COUNT;
    }

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    @Override
    public void init(XServer server) {
        this.server = server;
        this.compositor = new Compositor(server);
        buildFormats(server);
        log.info("RENDER ready: {} picture formats, fallback 0x{}", formats.size(),
                Integer.toHexString(fallback.id()));
    }

    @Override
    public com.github.moaxcp.x11.protocol.XRequest decodeRequest(int minorOpcode,
                                                                 com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        // Fourteen RENDER requests have a generated reader that throws on valid input; see
        // RenderDecoders. Everything else decodes normally.
        if (cn.oyzh.fx.xserver.protocol.RenderDecoders.handles(minorOpcode)) {
            return cn.oyzh.fx.xserver.protocol.RenderDecoders.decode(minorOpcode, in);
        }
        return null;
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof QueryVersion queryVersion) {
            queryVersion(client, queryVersion);
        } else if (request instanceof QueryPictFormats queryPictFormats) {
            queryPictFormats(client, queryPictFormats);
        } else if (request instanceof QueryPictIndexValues queryPictIndexValues) {
            queryPictIndexValues(client, queryPictIndexValues);
        } else if (request instanceof QueryFilters queryFilters) {
            queryFilters(client, queryFilters);
        } else if (request instanceof CreatePicture createPicture) {
            createPicture(server, client, createPicture);
        } else if (request instanceof ChangePicture changePicture) {
            changePicture(server, client, changePicture);
        } else if (request instanceof FreePicture freePicture) {
            freePicture(client, freePicture);
        } else if (request instanceof SetPictureClipRectangles setClip) {
            setPictureClipRectangles(client, setClip);
        } else if (request instanceof SetPictureTransform setTransform) {
            setPictureTransform(client, setTransform);
        } else if (request instanceof SetPictureFilter setFilter) {
            setPictureFilter(client, setFilter);
        } else if (request instanceof Composite composite) {
            composite(server, client, composite);
        } else if (request instanceof FillRectangles fillRectangles) {
            fillRectangles(server, client, fillRectangles);
        } else if (request instanceof Trapezoids trapezoids) {
            trapezoids(server, client, trapezoids);
        } else if (request instanceof Triangles triangles) {
            triangles(server, client, triangles);
        } else if (request instanceof TriStrip triStrip) {
            triStrip(server, client, triStrip);
        } else if (request instanceof TriFan triFan) {
            triFan(server, client, triFan);
        } else if (request instanceof AddTraps addTraps) {
            addTraps(server, client, addTraps);
        } else if (request instanceof CreateSolidFill solidFill) {
            createSolidFill(server, client, solidFill);
        } else if (request instanceof CreateLinearGradient linearGradient) {
            createLinearGradient(server, client, linearGradient);
        } else if (request instanceof CreateRadialGradient radialGradient) {
            createRadialGradient(server, client, radialGradient);
        } else if (request instanceof CreateConicalGradient conicalGradient) {
            createConicalGradient(server, client, conicalGradient);
        } else if (request instanceof CreateGlyphSet createGlyphSet) {
            createGlyphSet(server, client, createGlyphSet);
        } else if (request instanceof ReferenceGlyphSet referenceGlyphSet) {
            referenceGlyphSet(server, client, referenceGlyphSet);
        } else if (request instanceof FreeGlyphSet freeGlyphSet) {
            freeGlyphSet(client, freeGlyphSet);
        } else if (request instanceof AddGlyphs addGlyphs) {
            addGlyphs(server, client, addGlyphs);
        } else if (request instanceof FreeGlyphs freeGlyphs) {
            freeGlyphs(client, freeGlyphs);
        } else if (request instanceof CompositeGlyphs8 glyphs) {
            compositeGlyphs(client, glyphs.getOp(), glyphs.getSrc(), glyphs.getDst(),
                    glyphs.getMaskFormat(), glyphs.getGlyphset(), glyphs.getSrcX(), glyphs.getSrcY(),
                    glyphs.getGlyphcmds().toArray(), 1);
        } else if (request instanceof CompositeGlyphs16 glyphs) {
            compositeGlyphs(client, glyphs.getOp(), glyphs.getSrc(), glyphs.getDst(),
                    glyphs.getMaskFormat(), glyphs.getGlyphset(), glyphs.getSrcX(), glyphs.getSrcY(),
                    glyphs.getGlyphcmds().toArray(), 2);
        } else if (request instanceof CompositeGlyphs32 glyphs) {
            compositeGlyphs(client, glyphs.getOp(), glyphs.getSrc(), glyphs.getDst(),
                    glyphs.getMaskFormat(), glyphs.getGlyphset(), glyphs.getSrcX(), glyphs.getSrcY(),
                    glyphs.getGlyphcmds().toArray(), 4);
        } else if (request instanceof CreateCursor createCursor) {
            createCursor(server, client, createCursor);
        } else if (request instanceof CreateAnimCursor createAnimCursor) {
            createAnimCursor(server, client, createAnimCursor);
        } else {
            // Every request the generated plugin decodes is handled above, so reaching here means
            // the plugin and this class have drifted apart.
            throw ProtocolException.unimplemented(
                    "RENDER request " + request.getClass().getSimpleName());
        }
    }

    // ------------------------------------------------------------------
    // The picture format table
    // ------------------------------------------------------------------

    /**
     * Builds the format table.
     *
     * <p>The order of the table does not matter, because a client looks formats up by shape rather
     * than by position: it asks for "a depth 8 direct format with an alpha mask of 0xff" and takes
     * whichever entry matches. What matters is that each shape is present exactly once, with the
     * masks a real server publishes.
     */
    private void buildFormats(XServer server) {
        List<PictFormat> all = new ArrayList<>();
        Map<Integer, PictFormat> byId = new LinkedHashMap<>();
        Map<Integer, PictFormat> byVisual = new LinkedHashMap<>();
        int nextId = 1;

        for (Screen screen : server.screens()) {
            XColormap colormap = screen.defaultColormap();
            for (VisualInfo visual : screen.visuals()) {
                PictFormat format;
                if (visual.isIndexed()) {
                    format = PictFormat.indexed(nextId++, visual, colormap);
                } else {
                    // Depth 32 is the visual that owns an alpha channel; every other visual's
                    // format declares its pixels opaque, which is what "alpha == 1 everywhere"
                    // means. Without this an ARGB32 surface would be composited as if solid, and
                    // every translucent window would be an opaque grey rectangle.
                    int alphaMask = visual.depth() == 32 ? 0xFF000000 : 0;
                    format = PictFormat.direct(nextId++, visual, alphaMask);
                }
                all.add(format);
                byId.put(format.id(), format);
                byVisual.put(visual.id(), format);
            }
        }

        // The standard formats. The specification requires direct formats with 1, 4 and 8 bits of
        // alpha and none of red, green or blue, plus RGB24 and ARGB32, and cairo and Xft find them
        // by looking for exactly these mask layouts. A screen that already has a matching visual
        // keeps its own format rather than gaining a duplicate.
        nextId = addStandard(all, byId, nextId, 1, 0x1);
        nextId = addStandard(all, byId, nextId, 4, 0xF);
        nextId = addStandard(all, byId, nextId, 8, 0xFF);
        nextId = addStandard(all, byId, nextId, 24, 0);
        addStandard(all, byId, nextId, 32, 0xFF000000);

        PictFormat argb32 = findByIdentity(all, 32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000);
        // The fallback is the format the server would use as an intermediate. ARGB32 when it
        // exists, because it can hold anything; otherwise the root visual's.
        this.fallback = argb32 != null ? argb32 : byVisual.get(server.rootWindow().visual().id());
        this.formats = List.copyOf(all);
        this.formatsById = Map.copyOf(byId);
        this.formatsByVisual = Map.copyOf(byVisual);
    }

    /**
     * Adds one of the standard formats unless an identical one is already present.
     *
     * @return the next free format id
     */
    private static int addStandard(List<PictFormat> all, Map<Integer, PictFormat> byId, int nextId,
                                   int depth, int alphaMask) {
        boolean colour = depth >= 24;
        int redMask = colour ? 0x00FF0000 : 0;
        int greenMask = colour ? 0x0000FF00 : 0;
        int blueMask = colour ? 0x000000FF : 0;
        if (findByIdentity(all, depth, redMask, greenMask, blueMask, alphaMask) != null) {
            return nextId;
        }
        PictFormat format = colour
                ? PictFormat.direct(nextId, standardVisual(depth, redMask, greenMask, blueMask),
                        alphaMask)
                : PictFormat.alphaOnly(nextId, depth, alphaMask);
        all.add(format);
        byId.put(format.id(), format);
        return nextId + 1;
    }

    /**
     * A visual stand-in for a standard format, which exists only so the format can report the masks
     * its pixels use. Its id is zero, which no screen visual has, so it can never be looked up as
     * one.
     */
    private static VisualInfo standardVisual(int depth, int redMask, int greenMask, int blueMask) {
        return new VisualInfo(0, depth, VisualInfo.TRUE_COLOR, (byte) 8, (short) 256, redMask,
                greenMask, blueMask);
    }

    private static PictFormat findByIdentity(List<PictFormat> formats, int depth, int redMask,
                                             int greenMask, int blueMask, int alphaMask) {
        for (PictFormat format : formats) {
            if (format.isDirect() && format.depth() == depth && format.redMask() == redMask
                    && format.greenMask() == greenMask && format.blueMask() == blueMask
                    && format.alphaMask() == alphaMask) {
                return format;
            }
        }
        return null;
    }

    private PictFormat requireFormat(int id) {
        PictFormat format = formatsById.get(id);
        if (format == null) {
            throw badPictFormat(id);
        }
        return format;
    }

    // ------------------------------------------------------------------
    // Version and format queries
    // ------------------------------------------------------------------

    /**
     * {@code QueryVersion}. The reply is the server's own version whatever the client offered,
     * which is what a real server does: a client that asked for less gets a superset of what it
     * expects, and one that asked for more is told what it can actually rely on.
     */
    private void queryVersion(ClientConnection client, QueryVersion request) {
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .majorVersion(MAJOR_VERSION)
                .minorVersion(MINOR_VERSION)
                .build());
    }

    /**
     * {@code QueryPictFormats}: the reply a client decides everything else on. It reports every
     * format, then for each screen the depths it supports and which format each visual at that
     * depth maps to, and finally the subpixel order.
     */
    private void queryPictFormats(ClientConnection client, QueryPictFormats request) {
        List<Pictforminfo> formatInfos = new ArrayList<>(formats.size());
        for (PictFormat format : formats) {
            formatInfos.add(toProtocol(format));
        }

        int totalDepths = 0;
        int totalVisuals = 0;
        if (log.isDebugEnabled()) {
            for (PictFormat format : formats) {
                log.debug("RENDER format 0x{} depth {} {} alpha 0x{} visual {}",
                        Integer.toHexString(format.id()), format.depth(),
                        format.type() == PictFormat.DIRECT ? "direct" : "indexed",
                        Integer.toHexString(format.alphaMask()),
                        format.visual() == null ? "none"
                                : "0x" + Integer.toHexString(format.visual().id()));
            }
        }
        List<Pictscreen> screens = new ArrayList<>();
        for (Screen screen : server.screens()) {
            List<Pictdepth> depths = new ArrayList<>();
            for (Screen.DepthInfo depth : screen.depths()) {
                List<Pictvisual> visuals = new ArrayList<>(depth.visuals().size());
                for (VisualInfo visual : depth.visuals()) {
                    PictFormat format = formatsByVisual.get(visual.id());
                    if (format == null) {
                        continue;
                    }
                    visuals.add(Pictvisual.builder()
                            .visual(visual.id())
                            .format(format.id())
                            .build());
                }
                depths.add(Pictdepth.builder()
                        .depth((byte) depth.depth())
                        .visuals(Lists.immutable.ofAll(visuals))
                        .build());
                totalDepths++;
                totalVisuals += visuals.size();
            }
            screens.add(Pictscreen.builder()
                    .fallback(fallback.id())
                    .depths(Lists.immutable.ofAll(depths))
                    .build());
        }

        // One subpixel entry per screen. Glyphs here are rendered from grayscale coverage, so the
        // honest answer is None; a client told otherwise would ask for subpixel text and get no
        // fringes, or worse, would ask for a stripe order this server does not keep.
        int[] subpixelValues = new int[server.screens().size()];
        Arrays.fill(subpixelValues, SubPixel.NONE.getValue());

        client.sendReply(QueryPictFormatsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .numDepths(totalDepths)
                .numVisuals(totalVisuals)
                .formats(Lists.immutable.ofAll(formatInfos))
                .screens(Lists.immutable.ofAll(screens))
                .subpixels(IntLists.immutable.of(subpixelValues))
                .build());
    }

    private static Pictforminfo toProtocol(PictFormat format) {
        // A channel's mask is transmitted as the width of its field, not as the bit of the pixel
        // it occupies: the 8 bit red channel of RGB24 travels as 0xff beside a shift of 16, not as
        // 0xff0000. The two are not interchangeable - a client rebuilds the pixel mask as
        // mask << shift, so a truncated full mask says the channel is zero bits wide - and clients
        // look formats up by this shape rather than by id. Xft, for one, finds the 32 bit format
        // whose four masks are each 0xff to get the picture it fills its glyphs with; without it
        // every XftDrawString silently draws nothing.
        Directformat direct = Directformat.builder()
                .redShift((short) format.redShift())
                .redMask(fieldMask(format.redMask(), format.redShift()))
                .greenShift((short) format.greenShift())
                .greenMask(fieldMask(format.greenMask(), format.greenShift()))
                .blueShift((short) format.blueShift())
                .blueMask(fieldMask(format.blueMask(), format.blueShift()))
                .alphaShift((short) format.alphaShift())
                .alphaMask(fieldMask(format.alphaMask(), format.alphaShift()))
                .build();
        XColormap colormap = format.colormap();
        return Pictforminfo.builder()
                .id(format.id())
                .type((byte) format.type())
                .depth((byte) format.depth())
                .direct(direct)
                .colormap(colormap == null ? 0 : colormap.id())
                .build();
    }

    /**
     * A channel's mask as {@code Pictforminfo} carries it: the field's own width, shifted down off
     * the pixel. The shift is transmitted beside it and is what puts the field back.
     */
    private static short fieldMask(int mask, int shift) {
        return (short) (mask >>> shift);
    }

    /**
     * {@code QueryPictIndexValues}: the pixel to colour mapping of an indexed format. A client
     * drawing into an indexed picture needs it because only the listed pixel values are legal
     * there, and a client reading one needs it to know what the pixels mean.
     */
    private void queryPictIndexValues(ClientConnection client, QueryPictIndexValues request) {
        PictFormat format = requireFormat(request.getFormat());
        if (!format.isIndexed()) {
            throw ProtocolException.badMatch();
        }
        XColormap colormap = format.colormap();
        int size = colormap == null ? 0 : colormap.size();
        List<Indexvalue> values = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            int argb = colormap.colorAt(index);
            values.add(Indexvalue.builder()
                    .pixel(index)
                    .red(toCard16((argb >> 16) & 0xFF))
                    .green(toCard16((argb >> 8) & 0xFF))
                    .blue(toCard16(argb & 0xFF))
                    .alpha((short) 0xFFFF)
                    .build());
        }
        client.sendReply(QueryPictIndexValuesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .values(Lists.immutable.ofAll(values))
                .build());
    }

    /**
     * {@code QueryFilters}: the filters the server offers, and the aliases that map onto them.
     *
     * <p>The protocol makes the list a property of the drawable, but this server's answer does not
     * depend on it - the same filters work everywhere - so the drawable is not consulted. The reply
     * is fixed because the set is: {@code nearest} and {@code bilinear} are both really
     * implemented, and every alias resolves to one of them.
     */
    private void queryFilters(ClientConnection client, QueryFilters request) {
        List<Str> filters = new ArrayList<>(FILTER_NAMES.size());
        for (String name : FILTER_NAMES) {
            filters.add(Str.builder()
                    .name(ByteLists.immutable.of(name.getBytes(StandardCharsets.ISO_8859_1)))
                    .build());
        }
        client.sendReply(QueryFiltersReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .aliases(ShortLists.immutable.of(FILTER_ALIASES))
                .filters(Lists.immutable.ofAll(filters))
                .build());
    }

    // ------------------------------------------------------------------
    // Pictures
    // ------------------------------------------------------------------

    private void createPicture(XServer server, ClientConnection client, CreatePicture request) {
        checkNewId(client, request.getPid());
        XResource drawable = server.resources().requireDrawable(client, request.getDrawable());
        PictFormat format = requireFormat(request.getFormat());
        if (format.depth() != depthOf(drawable)) {
            throw ProtocolException.badMatch();
        }
        if (drawable instanceof XWindow window && !format.matchesVisual(window.visual())) {
            // A window's pixels are interpreted through its visual, so a format that packs colour
            // differently would read and write nonsense.
            throw ProtocolException.badMatch();
        }

        PixelStore store;
        int originX;
        int originY;
        XWindow window = null;
        Screen screen;
        if (drawable instanceof XWindow target) {
            // A window's own pixels live in a store of its own (B5), so the picture addresses that
            // store in the window's own coordinates and the drawing is composited back onto the
            // screen when the request ends (Compositor.finish). The root is the exception: it is the
            // screen, so its picture still carries the translation to screen coordinates.
            screen = target.screen();
            if (target.keepsOwnPixels()) {
                store = target.ownPixels();
                originX = 0;
                originY = 0;
            } else {
                store = screen.framebuffer();
                originX = target.absoluteX();
                originY = target.absoluteY();
            }
            window = target;
        } else {
            XPixmap pixmap = (XPixmap) drawable;
            screen = pixmap.screen();
            store = pixmap;
            originX = 0;
            originY = 0;
        }

        Picture picture = new Picture(request.getPid(), client, drawable, format, screen, store,
                originX, originY, window);
        applyAttributes(server, client, picture, PictureValues.of(request));
        server.resources().add(picture);
    }

    private void changePicture(XServer server, ClientConnection client, ChangePicture request) {
        Picture picture = requirePicture(request.getPicture());
        applyAttributes(server, client, picture, PictureValues.of(request));
    }

    private void freePicture(ClientConnection client, FreePicture request) {
        Picture picture = requirePicture(request.getPicture());
        server.resources().free(picture.id());
    }

    /**
     * Applies the attribute list shared by {@code CreatePicture} and {@code ChangePicture}.
     *
     * <p>The generated request leaves absent values at zero, so every field has to be read through
     * its mask bit. Reading one unconditionally would silently reset an attribute the client never
     * mentioned, which for a repeat mode or a clip mask is the difference between a textured window
     * and a blank one.
     */
    private void applyAttributes(XServer server, ClientConnection client, Picture picture,
                                 PictureValues values) {
        if (values.has(Cp.REPEAT)) {
            int repeat = values.repeat();
            if (repeat < Repeat.NONE.getValue() || repeat > Repeat.REFLECT.getValue()) {
                throw ProtocolException.badValue(repeat);
            }
            picture.setRepeat(repeat);
            if (log.isDebugEnabled()) {
                // The repeat mode is what decides whether a source smaller than the composite is
                // tiled or reads as empty. A compositor's alpha mask is a **1x1** picture it samples
                // at an offset *outside* it, so a repeat of None reads as nothing and the frame comes
                // out blank - which is a bug report that looks nothing like its cause.
                log.debug("SetPicture repeat={} on {} ({})", repeat, picture.drawable(),
                        picture.width() + "x" + picture.height());
            }
        }
        if (values.has(Cp.ALPHA_MAP)) {
            Picture alphaMap = values.alphaMap() == 0 ? null : requirePicture(values.alphaMap());
            if (alphaMap != null && alphaMap.window() != null) {
                // The alpha map replaces the drawable's own alpha, which only makes sense for a
                // pixmap: a window has no stable contents to take it from.
                throw ProtocolException.badMatch();
            }
            picture.setAlphaMap(alphaMap);
        }
        if (values.has(Cp.ALPHA_X_ORIGIN) || values.has(Cp.ALPHA_Y_ORIGIN)) {
            // The origin is one attribute in two halves, so the bit that is not being set keeps
            // the value the picture already has.
            picture.setAlphaOrigin(
                    values.has(Cp.ALPHA_X_ORIGIN) ? values.alphaXOrigin() : picture.alphaXOrigin(),
                    values.has(Cp.ALPHA_Y_ORIGIN) ? values.alphaYOrigin() : picture.alphaYOrigin());
        }
        if (values.has(Cp.CLIP_X_ORIGIN) || values.has(Cp.CLIP_Y_ORIGIN)) {
            picture.setClipOrigin(
                    values.has(Cp.CLIP_X_ORIGIN) ? values.clipXOrigin() : picture.clipXOrigin(),
                    values.has(Cp.CLIP_Y_ORIGIN) ? values.clipYOrigin() : picture.clipYOrigin());
        }
        if (values.has(Cp.CLIP_MASK)) {
            if (values.clipMask() == 0) {
                picture.setClipMask(null);
            } else {
                XPixmap mask = (XPixmap) server.resources().require(client, values.clipMask(),
                        XResource.CLASS_PIXMAP, false);
                if (mask.depth() != 1) {
                    // A clip mask is a stencil: any other depth would silently mean something
                    // else.
                    throw ProtocolException.badMatch();
                }
                picture.setClipMask(mask);
            }
        }
        if (values.has(Cp.SUBWINDOW_MODE)) {
            int mode = values.subwindowMode();
            if (mode != Picture.CLIP_BY_CHILDREN && mode != Picture.INCLUDE_INFERIORS) {
                throw ProtocolException.badValue(mode);
            }
            picture.setSubwindowMode(mode);
        }
        if (values.has(Cp.POLY_EDGE)) {
            int edge = values.polyEdge();
            if (edge != Picture.POLY_EDGE_SHARP && edge != Picture.POLY_EDGE_SMOOTH) {
                throw ProtocolException.badValue(edge);
            }
            picture.setPolyEdge(edge);
        }
        if (values.has(Cp.POLY_MODE)) {
            int mode = values.polyMode();
            if (mode != Picture.POLY_MODE_PRECISE && mode != Picture.POLY_MODE_IMPRECISE) {
                throw ProtocolException.badValue(mode);
            }
            picture.setPolyMode(mode);
        }
        if (values.has(Cp.COMPONENT_ALPHA)) {
            picture.setComponentAlpha(values.componentAlpha() != 0);
        }
        // Dither and graphics-exposures are accepted and ignored: this server dithers nothing, and
        // RENDER never generates a graphics exposure for a client to receive.
    }

    private void setPictureClipRectangles(ClientConnection client,
                                          SetPictureClipRectangles request) {
        Picture picture = requirePicture(request.getPicture());
        List<Rectangle> rectangles = new ArrayList<>(request.getRectangles().size());
        for (com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle : request.getRectangles()) {
            rectangles.add(new Rectangle(rectangle.getX(), rectangle.getY(),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight())));
        }
        picture.setClipOrigin(request.getClipXOrigin(), request.getClipYOrigin());
        picture.setClipRectangles(rectangles);
    }

    private void setPictureTransform(ClientConnection client, SetPictureTransform request) {
        Picture picture = requirePicture(request.getPicture());
        Transform matrix = request.getTransform();
        Picture.Transform transform = new Picture.Transform(matrix.getMatrix11(),
                matrix.getMatrix12(), matrix.getMatrix13(), matrix.getMatrix21(),
                matrix.getMatrix22(), matrix.getMatrix23(), matrix.getMatrix31(),
                matrix.getMatrix32(), matrix.getMatrix33());
        if (!transform.isInvertible()) {
            // A singular matrix leaves the source coordinate of a destination pixel undefined, so
            // the protocol makes it a value error rather than letting the request through.
            throw ProtocolException.badValue(0);
        }
        picture.setTransform(transform.isIdentity() ? null : transform);
    }

    private void setPictureFilter(ClientConnection client, SetPictureFilter request) {
        Picture picture = requirePicture(request.getPicture());
        String name = new String(request.getFilter().toArray(), StandardCharsets.ISO_8859_1);
        picture.setFilter(resolveFilter(name), request.getValues().toArray());
    }

    /**
     * Resolves a filter name to one this server implements. The aliases are mapped as the protocol
     * requires. An unknown name samples nearest rather than erroring: a client that asked for a
     * filter of its own invention wants an image, not a failure.
     */
    private static String resolveFilter(String name) {
        return switch (name) {
            case "nearest", "bilinear" -> name;
            case "good", "best" -> "bilinear";
            default -> "nearest";
        };
    }

    // ------------------------------------------------------------------
    // Drawing
    // ------------------------------------------------------------------

    /** One rectangle as text, for the diagnostics above. */
    /**
     * A picture's format in one word, because the request alone does not say it and the format is
     * what decides how its pixels are read: an A8 mask is coverage, ARGB32 is colour.
     */
    private static String describe(PictFormat format) {
        if (format == null) {
            return "?";
        }
        String kind = format.isIndexed() ? "indexed" : format.isAlphaOnly() ? "alpha-only" : "colour";
        return "depth " + format.depth() + " " + kind;
    }

    private static String describe(com.github.moaxcp.x11.protocol.xproto.Rectangle r) {
        return r.getX() + "," + r.getY() + " " + Short.toUnsignedInt(r.getWidth()) + "x"
                + Short.toUnsignedInt(r.getHeight());
    }

    private void composite(XServer server, ClientConnection client, Composite request) {
        Picture src = requirePicture(request.getSrc());
        Picture mask = request.getMask() == 0 ? null : requirePicture(request.getMask());
        Picture dst = requirePicture(request.getDst());
        if (log.isDebugEnabled()) {
            // The arguments matter more than the request name here: a composite that reaches the
            // wrong drawable, or one whose source is empty, looks identical in a request log.
            log.debug("Composite op={} src={}({},filter {}) mask={} dst={} "
                            + "srcAt ({},{}) maskAt ({},{}) size {}x{} dstAt ({},{})",
                    Byte.toUnsignedInt(request.getOp()), src.drawable(), describe(src.format()),
                    src.filter(),
                    mask == null ? "none" : mask.drawable() + "(" + describe(mask.format())
                            + ",filter " + mask.filter() + ")",
                    dst.drawable(), request.getSrcX(), request.getSrcY(),
                    request.getMaskX(), request.getMaskY(),
                    Short.toUnsignedInt(request.getWidth()),
                    Short.toUnsignedInt(request.getHeight()), request.getDstX(),
                    request.getDstY());
        }
        compositor.composite(checkOp(Byte.toUnsignedInt(request.getOp())), src, mask, dst,
                request.getSrcX(), request.getSrcY(), request.getMaskX(), request.getMaskY(),
                request.getDstX(), request.getDstY(), Short.toUnsignedInt(request.getWidth()),
                Short.toUnsignedInt(request.getHeight()));
        if (log.isDebugEnabled()) {
            // How many pixels the request actually wrote. A composite that is accepted and writes
            // nothing is a different bug from one that writes the wrong colour, and the difference is
            // invisible in the request log - it is what a compositor's blank frame turns out to be.
            log.debug("  -> wrote {} pixels, {} blocked", compositor.written(), compositor.blocked());
        }
    }

    private void fillRectangles(XServer server, ClientConnection client, FillRectangles request) {
        Picture dst = requirePicture(request.getDst());
        if (log.isDebugEnabled()) {
            log.debug("FillRectangles dst={} colour=#{} rects={} first={}", dst.drawable(),
                    Integer.toHexString(colorArgb(request.getColor())), request.getRects().size(),
                    request.getRects().isEmpty() ? "none" : describe(request.getRects().get(0)));
        }
        int argb = colorArgb(request.getColor());
        List<Rectangle> rectangles = new ArrayList<>(request.getRects().size());
        for (com.github.moaxcp.x11.protocol.xproto.Rectangle rectangle : request.getRects()) {
            rectangles.add(new Rectangle(rectangle.getX(), rectangle.getY(),
                    Short.toUnsignedInt(rectangle.getWidth()),
                    Short.toUnsignedInt(rectangle.getHeight())));
        }
        compositor.fillRectangles(checkOp(Byte.toUnsignedInt(request.getOp())), dst, argb,
                rectangles);
    }

    private void trapezoids(XServer server, ClientConnection client, Trapezoids request) {
        Picture src = requirePicture(request.getSrc());
        Picture dst = requirePicture(request.getDst());
        List<Shape> shapes = new ArrayList<>(request.getTraps().size());
        for (Trapezoid trap : request.getTraps()) {
            Shape shape = trapezoidShape(trap);
            if (shape != null) {
                shapes.add(shape);
            }
        }
        Pointfix registration = request.getTraps().isEmpty() ? null
                : request.getTraps().get(0).getLeft().getP1();
        drawPolygons(src, dst, shapes, request.getSrcX(), request.getSrcY(), registration,
                checkOp(Byte.toUnsignedInt(request.getOp())), request.getMaskFormat());
    }

    private void triangles(XServer server, ClientConnection client, Triangles request) {
        Picture src = requirePicture(request.getSrc());
        Picture dst = requirePicture(request.getDst());
        List<Shape> shapes = new ArrayList<>(request.getTriangles().size());
        for (Triangle triangle : request.getTriangles()) {
            shapes.add(triangleShape(triangle.getP1(), triangle.getP2(), triangle.getP3()));
        }
        Pointfix registration = request.getTriangles().isEmpty() ? null
                : request.getTriangles().get(0).getP1();
        drawPolygons(src, dst, shapes, request.getSrcX(), request.getSrcY(), registration,
                checkOp(Byte.toUnsignedInt(request.getOp())), request.getMaskFormat());
    }

    private void triStrip(XServer server, ClientConnection client, TriStrip request) {
        Picture src = requirePicture(request.getSrc());
        Picture dst = requirePicture(request.getDst());
        ImmutableList<Pointfix> points = request.getPoints();
        List<Shape> shapes = new ArrayList<>();
        // "Initially the first three points, then eliminate the first and append the next": every
        // consecutive triple in order, not the alternating rewinding a triangle strip usually is.
        for (int i = 0; i + 2 < points.size(); i++) {
            shapes.add(triangleShape(points.get(i), points.get(i + 1), points.get(i + 2)));
        }
        // The strip and the fan both start with the triangle made of the first three points, so
        // the pattern is pinned to the first point, exactly as it is for Triangles.
        drawPolygons(src, dst, shapes, request.getSrcX(), request.getSrcY(),
                points.isEmpty() ? null : points.get(0), checkOp(Byte.toUnsignedInt(request.getOp())),
                request.getMaskFormat());
    }

    private void triFan(XServer server, ClientConnection client, TriFan request) {
        Picture src = requirePicture(request.getSrc());
        Picture dst = requirePicture(request.getDst());
        ImmutableList<Pointfix> points = request.getPoints();
        List<Shape> shapes = new ArrayList<>();
        // "Eliminate the second point": the fan keeps the first point and pivots on it.
        for (int i = 1; i + 1 < points.size(); i++) {
            shapes.add(triangleShape(points.get(0), points.get(i), points.get(i + 1)));
        }
        drawPolygons(src, dst, shapes, request.getSrcX(), request.getSrcY(),
                points.isEmpty() ? null : points.get(0), checkOp(Byte.toUnsignedInt(request.getOp())),
                request.getMaskFormat());
    }

    /**
     * Common tail of the four polygon requests.
     *
     * <p>The pattern is registered against {@code registration}, which the protocol defines as the
     * first trapezoid's left edge origin and which the reference server takes as the first
     * triangle's first vertex. Registering against the request's bounding box instead - the obvious
     * thing to do - shifts a textured source by however far the first shape happens to be from the
     * corner of the union, and only the root-most shape would be right.
     */
    private void drawPolygons(Picture src, Picture dst, List<Shape> shapes, int srcX, int srcY,
                              Pointfix registration, int op, int maskFormatId) {
        boolean accumulate = maskFormatId != 0;
        boolean sharp;
        if (accumulate) {
            // A mask format with a single bit of alpha is a stencil, and stencils have hard edges.
            sharp = Integer.bitCount(requireFormat(maskFormatId).alphaMask()) == 1;
        } else {
            // Otherwise the destination picture decides, which is what the reference server does.
            sharp = dst.polyEdge() == Picture.POLY_EDGE_SHARP;
        }
        int registerX = registration == null ? 0 : registration.getX() >> 16;
        int registerY = registration == null ? 0 : registration.getY() >> 16;
        compositor.polygons(op, src, dst, shapes, srcX, srcY, registerX, registerY, accumulate,
                sharp);
    }

    private void addTraps(XServer server, ClientConnection client, AddTraps request) {
        Picture dst = requirePicture(request.getPicture());
        if (!dst.format().isAlphaOnly()) {
            // The traps accumulate into the destination's alpha; a colour picture has nothing
            // meaningful to accumulate into.
            throw ProtocolException.badMatch();
        }
        int offsetX = request.getXOff();
        int offsetY = request.getYOff();
        List<Shape> shapes = new ArrayList<>(request.getTraps().size());
        for (var trap : request.getTraps()) {
            double topY = fixed(trap.getTop().getY()) + offsetY;
            double bottomY = fixed(trap.getBot().getY()) + offsetY;
            if (bottomY <= topY) {
                continue;
            }
            Path2D.Double path = new Path2D.Double();
            path.moveTo(fixed(trap.getTop().getL()) + offsetX, topY);
            path.lineTo(fixed(trap.getTop().getR()) + offsetX, topY);
            path.lineTo(fixed(trap.getBot().getR()) + offsetX, bottomY);
            path.lineTo(fixed(trap.getBot().getL()) + offsetX, bottomY);
            path.closePath();
            shapes.add(path);
        }
        compositor.addTraps(dst, shapes, dst.polyEdge() == Picture.POLY_EDGE_SHARP);
    }

    private void createSolidFill(XServer server, ClientConnection client, CreateSolidFill request) {
        checkNewId(client, request.getPicture());
        Picture picture = createdSource(request.getPicture(), client);
        picture.setSolid(colorArgb(request.getColor()));
        server.resources().add(picture);
    }

    private void createLinearGradient(XServer server, ClientConnection client,
                                      CreateLinearGradient request) {
        checkNewId(client, request.getPicture());
        Pointfix p1 = request.getP1();
        Pointfix p2 = request.getP2();
        if (p1.getX() == p2.getX() && p1.getY() == p2.getY()) {
            throw ProtocolException.badValue(0);
        }
        Picture picture = createdSource(request.getPicture(), client);
        picture.setGradient(new Picture.Gradient(Picture.Gradient.LINEAR, fixed(p1.getX()),
                fixed(p1.getY()), fixed(p2.getX()), fixed(p2.getY()), 0, 0, 0,
                stopsOf(request.getStops()), colorsOf(request.getColors())));
        server.resources().add(picture);
    }

    private void createRadialGradient(XServer server, ClientConnection client,
                                      CreateRadialGradient request) {
        checkNewId(client, request.getPicture());
        double innerRadius = fixed(request.getInnerRadius());
        double outerRadius = fixed(request.getOuterRadius());
        if (outerRadius < innerRadius) {
            throw ProtocolException.badValue(request.getOuterRadius());
        }
        Pointfix inner = request.getInner();
        Pointfix outer = request.getOuter();
        Picture picture = createdSource(request.getPicture(), client);
        picture.setGradient(new Picture.Gradient(Picture.Gradient.RADIAL, fixed(inner.getX()),
                fixed(inner.getY()), fixed(outer.getX()), fixed(outer.getY()), innerRadius,
                outerRadius, 0, stopsOf(request.getStops()), colorsOf(request.getColors())));
        server.resources().add(picture);
    }

    private void createConicalGradient(XServer server, ClientConnection client,
                                       CreateConicalGradient request) {
        checkNewId(client, request.getPicture());
        Pointfix center = request.getCenter();
        Picture picture = createdSource(request.getPicture(), client);
        picture.setGradient(new Picture.Gradient(Picture.Gradient.CONICAL, fixed(center.getX()),
                fixed(center.getY()), 0, 0, 0, 0, Math.toRadians(fixed(request.getAngle())),
                stopsOf(request.getStops()), colorsOf(request.getColors())));
        server.resources().add(picture);
    }

    /**
     * The picture behind a request created source: a solid fill or a gradient.
     *
     * <p>It has no drawable of its own, so it is anchored to the root window - not to draw on it,
     * but because a picture needs a size, a screen and a format, and the root has all three. Its
     * pixels come from the colour or the gradient and never touch the framebuffer.
     */
    private Picture createdSource(int id, ClientConnection client) {
        XWindow root = server.rootWindow();
        return new Picture(id, client, root, fallback, root.screen(), root.screen().framebuffer(),
                0, 0, null);
    }

    private static double[] stopsOf(IntList raw) {
        double[] stops = new double[raw.size()];
        for (int i = 0; i < stops.length; i++) {
            stops[i] = fixed(raw.get(i));
            // The protocol requires the stops to be inside [0,1] and increasing; a client that
            // sent otherwise would get a gradient that reads backwards or wraps.
            if (stops[i] < 0 || stops[i] > 1 || (i > 0 && stops[i] < stops[i - 1])) {
                throw ProtocolException.badValue(raw.get(i));
            }
        }
        return stops;
    }

    private static int[] colorsOf(ImmutableList<Color> raw) {
        int[] colors = new int[raw.size()];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = colorArgb(raw.get(i));
        }
        return colors;
    }

    // ------------------------------------------------------------------
    // Glyph sets
    // ------------------------------------------------------------------

    private void createGlyphSet(XServer server, ClientConnection client, CreateGlyphSet request) {
        checkNewId(client, request.getGsid());
        PictFormat format = requireFormat(request.getFormat());
        if (!format.isDirect()) {
            // A glyph image is read as a bit field. An indexed format would make each glyph pixel
            // a colormap lookup, which the protocol does not allow.
            throw ProtocolException.badMatch();
        }
        server.resources().add(new GlyphSet(request.getGsid(), client, format));
    }

    private void referenceGlyphSet(XServer server, ClientConnection client,
                                   ReferenceGlyphSet request) {
        checkNewId(client, request.getGsid());
        GlyphSet existing = requireGlyphSet(request.getExisting());
        server.resources().add(existing.newAlias(request.getGsid(), client));
    }

    private void freeGlyphSet(ClientConnection client, FreeGlyphSet request) {
        // Resolving first turns a stale id into BadGlyphSet rather than a silent no-op, and the
        // request's own id is what gets freed even when it named an alias of the set.
        requireGlyphSet(request.getGlyphset());
        server.resources().free(request.getGlyphset());
    }

    private void addGlyphs(XServer server, ClientConnection client, AddGlyphs request) {
        GlyphSet set = requireGlyphSet(request.getGlyphset());
        PictFormat format = set.format();
        IntList ids = request.getGlyphids();
        ImmutableList<Glyphinfo> infos = request.getGlyphs();
        if (ids.size() != infos.size()) {
            throw ProtocolException.badLength();
        }
        byte[] data = request.getData().toArray();
        log.debug("RENDER AddGlyphs: {} glyphs, {} data bytes, format depth {}",
                infos.size(), data.length, format.depth());
        int offset = 0;
        for (int i = 0; i < infos.size(); i++) {
            Glyphinfo info = infos.get(i);
            int width = Short.toUnsignedInt(info.getWidth());
            int height = Short.toUnsignedInt(info.getHeight());
            if (width == 0 || height == 0) {
                set.put(ids.get(i), new GlyphSet.Glyph(0, 0, info.getX(), info.getY(),
                        info.getXOff(), info.getYOff(), new int[0]));
                continue;
            }
            // "A separate Z-format image padded to a 32-bit boundary": each scanline is padded,
            // which is what a client lays out and what makes a depth 1 bitmap addressable.
            int stride = ((width * format.depth() + 31) / 32) * 4;
            if (offset + (long) stride * height > data.length) {
                throw ProtocolException.badLength();
            }
            set.put(ids.get(i), new GlyphSet.Glyph(width, height, info.getX(), info.getY(),
                    info.getXOff(), info.getYOff(),
                    decodeGlyph(data, offset, width, height, stride, format,
                            client.isBigEndian())));
            offset += stride * height;
        }
    }

    /**
     * Decodes one glyph image into non-premultiplied ARGB.
     *
     * <p>An alpha-only format - A1, A4 or A8, which is nearly every glyph set there is - becomes
     * white with the sample in the alpha channel, so the glyph can be used as a mask directly. A
     * format with colour, meaning ARGB32, keeps its colour, which is what colour glyphs and
     * component-alpha text need.
     */
    private static int[] decodeGlyph(byte[] data, int offset, int width, int height, int stride,
                                     PictFormat format, boolean bigEndian) {
        int depth = format.depth();
        boolean alphaOnly = format.isAlphaOnly();
        int[] argb = new int[width * height];
        for (int y = 0; y < height; y++) {
            int rowStart = offset + y * stride;
            for (int x = 0; x < width; x++) {
                int pixel = sampleBits(data, rowStart, x, depth, bigEndian);
                argb[y * width + x] = alphaOnly
                        ? (scaleToByte(pixel, depth) << 24) | 0x00FFFFFF
                        : format.toArgb(pixel);
            }
        }
        return argb;
    }

    /**
     * Reads the {@code depth}-bit sample at {@code x} of a scanline. Bits inside a byte are always
     * most significant first, whatever the connection's byte order is; samples wider than a byte
     * are a number like any other and follow it.
     */
    private static int sampleBits(byte[] data, int rowStart, int x, int depth, boolean bigEndian) {
        int bit = x * depth;
        int index = rowStart + (bit >> 3);
        return switch (depth) {
            case 1 -> (data[index] >> (7 - (bit & 7))) & 0x1;
            case 4 -> (data[index] >> ((bit & 7) == 0 ? 4 : 0)) & 0xF;
            case 8 -> data[index] & 0xFF;
            case 16 -> readCard16(data, index, bigEndian);
            case 32 -> readCard32(data, index, bigEndian);
            default -> 0;
        };
    }

    private static int scaleToByte(int value, int depth) {
        if (depth >= 8) {
            return value & 0xFF;
        }
        int max = (1 << depth) - 1;
        return max == 0 ? 0 : value * 255 / max;
    }

    private void freeGlyphs(ClientConnection client, FreeGlyphs request) {
        GlyphSet set = requireGlyphSet(request.getGlyphset());
        for (int i = 0; i < request.getGlyphs().size(); i++) {
            if (set.remove(request.getGlyphs().get(i)) == null) {
                // The protocol makes removing a glyph that was never added a match error.
                throw ProtocolException.badMatch();
            }
        }
    }

    /**
     * {@code CompositeGlyphs8/16/32}: draw a string.
     *
     * <p>The command stream is a sequence of elements. Each has an eight byte header - a length, a
     * gap, and two deltas - followed by that many glyph indices, padded so the next header starts
     * on a four byte boundary. A length of 255 is not a count but a switch to a different glyph
     * set, which is how one string mixes fonts. The deltas are added to the glyph origin and so
     * accumulate across elements.
     *
     * <p>The request carries no destination of its own: the caller's starting position arrives in
     * the first element's delta. That starting position is also what the source pattern is
     * registered against, so it is captured separately - it is the subtlety to remember when text
     * lands in the wrong place, or when a textured source slides relative to the glyphs.
     *
     * <p>A glyph the set does not contain is skipped rather than reported. The specification lists
     * a {@code BadGlyph} error here, but the reference server drops missing glyphs instead, and a
     * client that hits one wants the rest of the string drawn: raising the error would truncate it.
     *
     * @param glyphSize bytes per glyph index: 1, 2 or 4
     */
    private void compositeGlyphs(ClientConnection client, byte rawOp, int srcId, int dstId,
                                 int maskFormatId, int glyphSetId, short srcX, short srcY,
                                 byte[] commands, int glyphSize) {
        Picture src = requirePicture(srcId);
        Picture dst = requirePicture(dstId);
        int op = checkOp(Byte.toUnsignedInt(rawOp));
        if (maskFormatId != 0) {
            requireFormat(maskFormatId);
        }
        GlyphSet set = requireGlyphSet(glyphSetId);
        boolean componentAlpha = set.isComponentAlpha();

        log.debug("RENDER CompositeGlyphs: {} command bytes, glyphSize {}, set size {}, src 0x{}"
                        + " dst 0x{}",
                commands.length, glyphSize, set.size(), Integer.toHexString(srcId),
                Integer.toHexString(dstId));
        // Everything in the command stream but the glyph set ids is in the connection's byte
        // order, the deltas and the 16 and 32 bit glyph indices included; only the GLYPHSET items
        // are defined to be most significant byte first.
        boolean bigEndian = client.isBigEndian();
        List<Compositor.PlacedGlyph> placements = new ArrayList<>();
        int originX = 0;
        int originY = 0;
        int registrationX = 0;
        int registrationY = 0;
        boolean registered = false;
        int index = 0;
        while (index < commands.length) {
            if ((commands[index] & 0xFF) == GLYPH_ITEM) {
                if (index + GLYPH_SWITCH > commands.length) {
                    break;
                }
                // The one field that is not in the connection's byte order: "All contained
                // GLYPHSETs are always transmitted most significant byte first".
                set = requireGlyphSet(readCard32(commands, index + GLYPH_HEADER, true));
                index += GLYPH_SWITCH;
                // "Switching among glyphsets does not affect the next glyph origin."
                continue;
            }
            if (index + GLYPH_HEADER > commands.length) {
                break;
            }
            int count = commands[index] & 0xFF;
            originX += readInt16(commands, index + 4, bigEndian);
            originY += readInt16(commands, index + 6, bigEndian);
            index += GLYPH_HEADER;
            int glyphBytes = count * glyphSize;
            if (index + (long) glyphBytes > commands.length) {
                throw ProtocolException.badLength();
            }
            if (!registered) {
                // The first element's delta is where the caller asked the run to start, and it is
                // the point the source is registered against.
                registrationX = originX;
                registrationY = originY;
                componentAlpha = set.isComponentAlpha();
                registered = true;
            }
            for (int i = 0; i < count; i++) {
                int glyphId = readGlyphId(commands, index + i * glyphSize, glyphSize, bigEndian);
                GlyphSet.Glyph glyph = set.glyph(glyphId);
                if (glyph == null) {
                    // A glyph the set does not hold contributes nothing, and does not advance the
                    // origin either. See the note above on why this is not a BadGlyph.
                    log.debug("glyph set 0x{} has no glyph 0x{}", Integer.toHexString(set.id()),
                            Integer.toHexString(glyphId));
                    continue;
                }
                // The image sits at the origin minus the glyph's bearing, which is the convention
                // Xft fills in and the one the reference server draws with.
                placements.add(new Compositor.PlacedGlyph(glyph, originX - glyph.x(),
                        originY - glyph.y()));
                originX += glyph.xOff();
                originY += glyph.yOff();
            }
            // Each element's glyphs are padded to a four byte boundary.
            index += (glyphBytes + 3) & ~3;
        }

        if (!placements.isEmpty()) {
            // There is no separate mask picture: the glyph coverage is the mask, and a mask format
            // tells the server to add the glyphs into one mask before compositing.
            compositor.glyphs(op, src, dst, srcX - registrationX, srcY - registrationY, placements,
                    maskFormatId != 0, componentAlpha);
        }
    }

    /**
     * A glyph index of the width {@code GLYPHELT8}, {@code 16} or {@code 32} names. A one byte
     * index has no byte order, and the wider ones follow the connection's, as every other
     * multi-byte value in the request does.
     */
    private static int readGlyphId(byte[] data, int offset, int size, boolean bigEndian) {
        return switch (size) {
            case 1 -> data[offset] & 0xFF;
            case 2 -> readCard16(data, offset, bigEndian);
            default -> readCard32(data, offset, bigEndian);
        };
    }

    // ------------------------------------------------------------------
    // Cursors
    // ------------------------------------------------------------------

    /**
     * {@code CreateCursor}: build a cursor from a picture. A copy is taken, because the
     * specification says the source may be freed or drawn into immediately afterwards.
     */
    private void createCursor(XServer server, ClientConnection client, CreateCursor request) {
        checkNewId(client, request.getCid());
        Picture source = requirePicture(request.getSource());
        int hotX = Short.toUnsignedInt(request.getX());
        int hotY = Short.toUnsignedInt(request.getY());
        int width = source.width();
        int height = source.height();
        // The hotspot is *not* required to be inside the source: a reference accepts one outside it
        // (measured with a 1x1 source and hotspot (1,1)), and a toolkit installs a RENDER cursor that
        // way. Refusing it arrived as an X error and killed the application - see
        // ResourceHandlers.createCursor for the whole measurement.
        if (width > MAX_CURSOR_SIZE || height > MAX_CURSOR_SIZE) {
            // A cursor is drawn on every pointer move, so an enormous one is a denial of service
            // rather than a cursor. Real servers bound this too.
            throw ProtocolException.badMatch();
        }
        // The picture's clip is renewed per drawing request; reading its pixels here is not one, so
        // it is renewed explicitly or a stale clip from an earlier request would be used.
        source.refreshClip();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = source.pixelAt(x, y);
            }
        }
        server.resources().add(new XCursor(request.getCid(), client, width, height, hotX, hotY,
                pixels, false));
    }

    /**
     * {@code CreateAnimCursor}. The server's cursor type holds one image and has no clock, so the
     * animation is not reproduced: the result is the cursor the first element names. Every cursor
     * in the list is still resolved, so a client with a stale id is told rather than silently
     * handed nothing.
     */
    private void createAnimCursor(XServer server, ClientConnection client,
                                  CreateAnimCursor request) {
        checkNewId(client, request.getCid());
        if (request.getCursors().isEmpty()) {
            throw ProtocolException.badValue(0);
        }
        XCursor first = null;
        for (var element : request.getCursors()) {
            XCursor cursor = (XCursor) server.resources().require(client, element.getCursor(),
                    XResource.CLASS_CURSOR, false);
            if (first == null) {
                first = cursor;
            }
        }
        int width = first.width();
        int height = first.height();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = first.pixel(x, y);
            }
        }
        server.resources().add(new XCursor(request.getCid(), client, width, height,
                first.hotspotX(), first.hotspotY(), pixels, first.isRecolorable()));
    }

    // ------------------------------------------------------------------
    // Geometry
    // ------------------------------------------------------------------

    /**
     * The quad a trapezoid describes.
     *
     * <p>A trapezoid is a vertical span plus two lines, and the shape is the part of the span
     * between them. Only the endpoints at the top and bottom are needed, because the lines are
     * straight, so the shape is exact rather than sampled. The protocol guarantees convexity, so
     * nothing else has to be clipped away.
     */
    private static Shape trapezoidShape(Trapezoid trap) {
        double top = fixed(trap.getTop());
        double bottom = fixed(trap.getBottom());
        if (bottom <= top) {
            return null;
        }
        Path2D.Double path = new Path2D.Double();
        path.moveTo(lineX(trap.getLeft(), top), top);
        path.lineTo(lineX(trap.getRight(), top), top);
        path.lineTo(lineX(trap.getRight(), bottom), bottom);
        path.lineTo(lineX(trap.getLeft(), bottom), bottom);
        path.closePath();
        return path;
    }

    /** The x of a line at a given y, interpolated between its two fixed point endpoints. */
    private static double lineX(Linefix line, double y) {
        double x1 = fixed(line.getP1().getX());
        double y1 = fixed(line.getP1().getY());
        double x2 = fixed(line.getP2().getX());
        double y2 = fixed(line.getP2().getY());
        if (y2 == y1) {
            // A horizontal edge has no single x; the smaller one keeps the quad from inverting.
            return Math.min(x1, x2);
        }
        return x1 + (x2 - x1) * (y - y1) / (y2 - y1);
    }

    private static Shape triangleShape(Pointfix p1, Pointfix p2, Pointfix p3) {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(fixed(p1.getX()), fixed(p1.getY()));
        path.lineTo(fixed(p2.getX()), fixed(p2.getY()));
        path.lineTo(fixed(p3.getX()), fixed(p3.getY()));
        path.closePath();
        return path;
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Picture requirePicture(int id) {
        XResource resource = server.resources().get(id);
        if (!(resource instanceof Picture picture) || picture.isDestroyed()) {
            // An id that names something else, or nothing, is this extension's own error rather
            // than the core protocol's BadDrawable.
            throw badPicture(id);
        }
        return picture;
    }

    private GlyphSet requireGlyphSet(int id) {
        XResource resource = server.resources().get(id);
        if (resource instanceof GlyphSet set && !set.isDestroyed()) {
            return set;
        }
        if (resource instanceof GlyphSet.Alias alias && !alias.isDestroyed()) {
            return alias.target();
        }
        throw badGlyphSet(id);
    }

    private int checkOp(int op) {
        if (PictOp.getByCode(op) == null) {
            throw badPictOp(op);
        }
        return op;
    }

    private void checkNewId(ClientConnection client, int id) {
        if (id == 0 || !client.ownsIdRange(id) || server.resources().contains(id)) {
            throw ProtocolException.badIdChoice(id);
        }
    }

    private static int depthOf(XResource drawable) {
        return drawable instanceof XWindow window ? window.depth() : ((XPixmap) drawable).depth();
    }

    /** Converts a 16-bit protocol colour to the server's non-premultiplied ARGB. */
    private static int colorArgb(Color color) {
        return (toByte(color.getAlpha()) << 24) | (toByte(color.getRed()) << 16)
                | (toByte(color.getGreen()) << 8) | toByte(color.getBlue());
    }

    private static int toByte(short value) {
        return (Short.toUnsignedInt(value) * 255 + 32767) / 65535;
    }

    private static short toCard16(int value) {
        return (short) (value * 65535 / 255);
    }

    /** A protocol 16.16 fixed point value as a double. */
    private static double fixed(int value) {
        return value / 65536.0;
    }

    private static int readCard16(byte[] data, int offset, boolean bigEndian) {
        return bigEndian
                ? ((data[offset] & 0xFF) << 8) | (data[offset + 1] & 0xFF)
                : ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF);
    }

    /**
     * A glyph element's delta, a plain {@code INT16} and so in the connection's byte order. Read
     * the other way round, a delta of two pixels arrives as five hundred and twelve, and every
     * glyph lands outside the drawable.
     */
    private static int readInt16(byte[] data, int offset, boolean bigEndian) {
        return (short) readCard16(data, offset, bigEndian);
    }

    private static int readCard32(byte[] data, int offset, boolean bigEndian) {
        return bigEndian
                ? ((data[offset] & 0xFF) << 24) | ((data[offset + 1] & 0xFF) << 16)
                        | ((data[offset + 2] & 0xFF) << 8) | (data[offset + 3] & 0xFF)
                : ((data[offset + 3] & 0xFF) << 24) | ((data[offset + 2] & 0xFF) << 16)
                        | ((data[offset + 1] & 0xFF) << 8) | (data[offset] & 0xFF);
    }

    private ProtocolException badPictFormat(int id) {
        return error(ERROR_PICT_FORMAT, id, "pictformat");
    }

    private ProtocolException badPicture(int id) {
        return error(ERROR_PICTURE, id, "picture");
    }

    private ProtocolException badPictOp(int op) {
        return error(ERROR_PICT_OP, op, "pictop");
    }

    private ProtocolException badGlyphSet(int id) {
        return error(ERROR_GLYPH_SET, id, "glyphset");
    }

    private ProtocolException error(int offset, int badValue, String what) {
        return new ProtocolException((byte) ((plugin.getFirstError() & 0xFF) + offset), badValue,
                "bad " + what + " 0x" + Integer.toHexString(badValue));
    }

    /**
     * The attribute values of a {@code CreatePicture} or a {@code ChangePicture}, read through
     * their mask bits. The two requests carry exactly the same fields, so one adapter serves both
     * and the logic that applies them exists once.
     */
    private record PictureValues(int mask, int repeat, int alphaMap, int alphaXOrigin,
                                 int alphaYOrigin, int clipXOrigin, int clipYOrigin, int clipMask,
                                 int subwindowMode, int polyEdge, int polyMode,
                                 int componentAlpha) {

        static PictureValues of(CreatePicture request) {
            return new PictureValues(request.getValueMask(), request.getRepeat(),
                    request.getAlphamap(), request.getAlphaxorigin(), request.getAlphayorigin(),
                    request.getClipxorigin(), request.getClipyorigin(), request.getClipmask(),
                    request.getSubwindowmode(), request.getPolyedge(), request.getPolymode(),
                    request.getComponentalpha());
        }

        static PictureValues of(ChangePicture request) {
            return new PictureValues(request.getValueMask(), request.getRepeat(),
                    request.getAlphamap(), request.getAlphaxorigin(), request.getAlphayorigin(),
                    request.getClipxorigin(), request.getClipyorigin(), request.getClipmask(),
                    request.getSubwindowmode(), request.getPolyedge(), request.getPolymode(),
                    request.getComponentalpha());
        }

        boolean has(Cp attribute) {
            return attribute.isEnabled(mask);
        }
    }
}
