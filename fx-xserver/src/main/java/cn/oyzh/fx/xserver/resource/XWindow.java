package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.display.VisualInfo;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.resource.XColormap;
import cn.oyzh.fx.xserver.resource.XProperty;
import cn.oyzh.common.log.JulLog;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A window: a rectangular region of the screen that draws into the screen framebuffer and
 * receives events.
 *
 * <p>Children are stored back to front, so index 0 is the bottom-most child and the last element
 * is on top. That matches the X stacking order and makes obscuring and expose generation a matter
 * of walking the list.
 */
public final class XWindow extends XResource {


    // window classes
    public static final int COPY_FROM_PARENT = 0;
    public static final int INPUT_OUTPUT = 1;
    public static final int INPUT_ONLY = 2;

    // map states
    public static final int IS_UNMAPPED = 0;
    public static final int IS_UNVIEWABLE = 1;
    public static final int IS_VIEWABLE = 2;

    // stacking modes
    public static final int ABOVE = 0;
    public static final int BELOW = 1;
    public static final int TOP_IF = 2;
    public static final int BOTTOM_IF = 3;
    public static final int OPPOSITE = 4;

    private final Screen screen;
    private final int depth;
    private final int windowClass;
    private final VisualInfo visual;

    private XWindow parent;
    private final List<XWindow> children = new CopyOnWriteArrayList<>();

    private int x;
    private int y;
    private int width;
    private int height;
    private int borderWidth;

    private boolean mapped;
    private boolean overrideRedirect;
    private boolean saveUnder;
    private int backingStore;
    private int backingPlanes = 0xFFFFFFFF;
    private int backingPixel;

    /** This window's own pixels, for a window whose visual has an alpha channel - see keepsOwnPixels. */
    private Framebuffer ownPixels;
    /** Whether the "this window is too large to keep pixels" warning has been written once. */
    private boolean warnedAboutStore;

    /** Composite's redirection: the window keeps drawing, but somebody else composites it. */
    private boolean redirected;
    private int redirectUpdate;
    private boolean subwindowsRedirected;
    /**
     * Set on a window the server itself presents - Composite's overlay, and nothing else.
     *
     * <p>A compositor redirects the root's whole subtree, which includes the very window it draws
     * its result on. On a reference that is harmless: the display driver shows the overlay by a path
     * of its own. Here the overlay's *only* path to the screen is being composited, so redirecting
     * it leaves the compositor's result nowhere to go - measured, and it is a blank screen with
     * `picom` running happily.
     */
    private boolean redirectExempt;

    /**
     * Set on the server's own presentation surface - Composite's overlay, and nothing else.
     *
     * <p>It has **no background at all**: it is not painted, and until a compositor draws into it it
     * contributes nothing to the screen. That is what a reference does by creating the overlay with
     * {@code background = None}, and the difference is visible: this server approximates {@code None}
     * by painting the desktop colour (a deviation that is harmless for an ordinary window, which
     * draws over it), so a screen-sized overlay painted its grey over the whole screen the moment it
     * was mapped - measured, {@code tools/CompositeProbe} reading the screen back while a window was
     * redirected, where the reference still shows the window's own pixels.
     */
    private boolean transparentUntilDrawn;

    /**
     * The two gravities, whose default is X's own: {@code NorthWest} (1) for both. It matters that
     * the default is measured rather than assumed - {@code bitGravity} was stored as 0, which is
     * {@code Forget}, and a window resized under {@code Forget} **throws its contents away**: every
     * window in the server would have lost its pixels on its first resize. Measured against a
     * reference server 2026-09-26 with {@code GetWindowAttributes} on a freshly created window:
     * {@code bit_gravity} answers 1 there and answered 0 here.
     */
    private int bitGravity = 1;
    /** {@code winGravity}'s default is the same number; see {@link #bitGravity}. */
    private int winGravity = 1;

    /** Background: either a pixel or a pixmap; {@code backgroundIsPixmap} says which. */
    /**
     * The colours a window starts with: the screen showing through where it has nothing of its own,
     * and a border a shade darker so the window has an edge. A client overrides either by naming a
     * background or border in its {@code CreateWindow} or {@code ChangeWindowAttributes}.
     */
    private int backgroundPixel = Screen.DEFAULT_BACKGROUND_ARGB;
    private XResource backgroundPixmap;
    private boolean backgroundIsPixmap;

    private int borderPixel = Screen.DEFAULT_BORDER_ARGB;
    private XResource borderPixmap;
    private boolean borderIsPixmap;

    private XColormap colormap;
    private XCursor cursor;
    /** Outstanding {@code XFixesHideCursor} requests, see {@link #cursorHideCount()}. */
    private int cursorHideCount;

    /** Properties, keyed by the atom naming them. */
    private final java.util.Map<Integer, XProperty> properties = new java.util.HashMap<>();

    /** This client's own event mask. */
    private int eventMask;
    private int doNotPropagateMask;
    /** Event masks other clients asked for on this window. */
    private final List<OtherClient> otherClients = new ArrayList<>();

    /** True when the window has never been drawn, so a map must produce exposes. */
    private boolean needsFullExpose = true;

    /**
     * The window's bounding shape as rectangles in window coordinates, or null when it has none.
     *
     * <p>Kept here rather than looked up in the shape extension because the region code runs once per
     * drawing request and has no server to ask; the extension hands the region over whenever the
     * shape changes. A bounding shape <em>removes</em> pixels from the window - its corners are not
     * part of it - so it is a subtraction from {@link #visibleRegion()}, not a clip on top of it, and
     * it applies to the children too: a child outside its parent's shape is not on screen either.
     */
    private List<Rectangle> boundingShape;

    /** The window's bounding shape in window coordinates, or null when it has none. */
    public List<Rectangle> boundingShape() {
        return boundingShape;
    }

    public void setBoundingShape(List<Rectangle> shape) {
        this.boundingShape = shape == null || shape.isEmpty() ? null : List.copyOf(shape);
    }

    private boolean redirectToParent;

    public XWindow(int id, ClientConnection owner, Screen screen, XWindow parent, int depth,
                   int windowClass, VisualInfo visual, int x, int y, int width, int height,
                   int borderWidth) {
        super(id, CLASS_WINDOW, owner);
        this.screen = screen;
        this.parent = parent;
        this.depth = depth;
        this.windowClass = windowClass;
        this.visual = visual;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.borderWidth = borderWidth;
        if (parent != null) {
            parent.children.add(this);
        }
    }

    public Screen screen() {
        return screen;
    }

    public int depth() {
        return depth;
    }

    public int windowClass() {
        return windowClass;
    }

    public VisualInfo visual() {
        return visual;
    }

    public boolean isInputOnly() {
        return windowClass == INPUT_ONLY;
    }

    public XWindow parent() {
        return parent;
    }

    public void setParent(XWindow parent) {
        this.parent = parent;
    }

    public List<XWindow> children() {
        return children;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int borderWidth() {
        return borderWidth;
    }

    public void setSize(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.needsFullExpose = true;
    }

    public void setBorderWidth(int borderWidth) {
        this.borderWidth = Math.max(0, borderWidth);
    }

    public boolean isMapped() {
        return mapped;
    }

    public void setMapped(boolean mapped) {
        this.mapped = mapped;
    }

    public boolean isOverrideRedirect() {
        return overrideRedirect;
    }

    public void setOverrideRedirect(boolean overrideRedirect) {
        this.overrideRedirect = overrideRedirect;
    }

    public boolean isSaveUnder() {
        return saveUnder;
    }

    public void setSaveUnder(boolean saveUnder) {
        this.saveUnder = saveUnder;
    }

    public int backingStore() {
        return backingStore;
    }

    public void setBackingStore(int backingStore) {
        this.backingStore = backingStore;
    }

    public int backingPlanes() {
        return backingPlanes;
    }

    public void setBackingPlanes(int backingPlanes) {
        this.backingPlanes = backingPlanes;
    }

    public int backingPixel() {
        return backingPixel;
    }

    public void setBackingPixel(int backingPixel) {
        this.backingPixel = backingPixel;
    }

    /**
     * Whether this window's pixels live in a store of their own rather than only on the screen.
     *
     * <p>True for every window but the root: what a client draws goes into {@link #ownPixels()} and
     * is composited from it onto the screen, clipped to what shows. That is what makes a window's
     * pixels its own - a covered window goes on drawing and keeps what it drew, a revealed one gets
     * them back without an {@code Expose}, and a client that reads a window back gets the window's
     * contents rather than whatever happens to be on the screen at its rectangle (B5).
     *
     * <p>An alpha visual is the other reason, and the one it was built for first: the screen's
     * framebuffer has no alpha ({@code Framebuffer} forces every pixel there opaque, deliberately),
     * so a translucent window can only be composited from a store that keeps it.
     *
     * <p>The root is the exception - it <em>is</em> the screen - and so is an input-only window,
     * which has no pixels at all.
     *
     * <p><b>And so is a window too large to keep pixels for</b> ({@link #MAX_STORE_PIXELS}): a store
     * is the window's own size, so a client that creates a very large window and draws into it asks
     * the server for the same amount of memory - measured 2026-10-01: a 12000x12000 window reached
     * {@code OutOfMemoryError} inside {@code Framebuffer.allocate} while the window was being mapped,
     * which killed that connection and left the whole server one allocation away from the same fate.
     * Every other drawable already has a budget like this one (a pixmap is 16M pixels, an image
     * decode 64M); a window's own store was the one that did not.
     *
     * <p>The window is not refused and nothing about its protocol behaviour changes: it draws onto the
     * screen directly, exactly as the root does, so it simply does not keep what a window on top of
     * it covers. The limit is the largest screen this server can be asked for, so <em>no window that
     * fits on the display is affected at all</em>.
     */
    public boolean keepsOwnPixels() {
        return visual != null && !isInputOnly() && !isRoot() && keepsStore();
    }

    /**
     * The most pixels a window's own store may hold: the screen at its largest ({@code --max-screen},
     * 8192x8192 by default). A window bigger than the display cannot show more than the display, and
     * a store for it is the one allocation in this server that had no bound.
     */
    public static final long MAX_STORE_PIXELS = 8192L * 8192;

    /** Whether this window is small enough to keep its own pixels. */
    private boolean keepsStore() {
        return keepsStore(width(), height());
    }

    /**
     * Whether a window of this size is small enough to keep its own pixels. Static so the rule can be
     * pinned without allocating a window of the size that used to break the server.
     */
    static boolean keepsStore(int width, int height) {
        return (long) Math.max(1, width) * Math.max(1, height) <= MAX_STORE_PIXELS;
    }

    /**
     * Whether this window's pixels carry transparency - a window that shows what is behind it
     * wherever it has not drawn.
     *
     * <p>Used where "is this window in the way" is asked, because a translucent window is not: see
     * {@code subtractWindow}.
     */
    public boolean isTranslucent() {
        return keepsOwnPixels() && visual.hasAlpha();
    }

    /**
     * The window's own pixels: {@code width x height}, filled with the window's background when it
     * is first made, in window coordinates.
     *
     * <p>Kept with the alpha channel only when the visual has one; an ordinary window's store is
     * opaque like the screen, because {@code GXclear} and {@code GXinvert} can produce any 32 bit
     * value and a store that honoured the alpha in them would show holes.
     *
     * <p>A resize reallocates it but <em>keeps what was drawn</em>, aligned to the top left: a
     * window's bit gravity moves those pixels afterwards ({@code WindowHandlers.applyBitGravity}),
     * and it can only move what is still there. Forgetting them here would throw the contents away
     * on the way into the gravity, which is the bug the default gravity was fixed for.
     */
    public Framebuffer ownPixels() {
        if (!isRoot() && !keepsStore() && !warnedAboutStore) {
            // Not reachable from this server's own code - every caller asks keepsOwnPixels first -
            // but this is where the 17 GB allocation of A29 would come from, and a warning in the
            // log is worth more than an OutOfMemoryError with no explanation.
            warnedAboutStore = true;
            JulLog.warn("0x{} is {}x{} and keeps no pixels of its own (over the {} pixel budget), so"
                            + " this allocation is unbounded - see A29 in docs/backlog.md",
                    Integer.toHexString(id()), width(), height(), MAX_STORE_PIXELS);
        }
        if (ownPixels == null) {
            ownPixels = new Framebuffer(Math.max(1, width()), Math.max(1, height()),
                    backgroundPixel, visual != null && visual.hasAlpha());
        } else if (ownPixels.width() != width() || ownPixels.height() != height()) {
            Framebuffer resized = new Framebuffer(Math.max(1, width()), Math.max(1, height()),
                    backgroundPixel, visual != null && visual.hasAlpha());
            resized.copyFrom(ownPixels, 0, 0, 0, 0, Math.min(width(), ownPixels.width()),
                    Math.min(height(), ownPixels.height()));
            ownPixels = resized;
        }
        return ownPixels;
    }

    /**
     * Whether a compositor has redirected this window, so that what it draws no longer reaches the
     * screen (Composite).
     *
     * <p>The store is what makes this cheap here: a window already draws into pixels of its own and
     * is composited from them, so "redirected" is exactly "composited by somebody else" - the
     * drawing path is untouched and only the compositing stops
     * ({@code XServer.compositeOwnPixels}). What the window drew stays readable through
     * {@code NameWindowPixmap}, in the coordinate space a compositor expects.
     *
     * <p>Measured against a reference before it was written, because the alternative reading - that
     * a redirected window keeps drawing to the screen as well - is what this server does by default
     * and would have looked correct in every test that never redirected anything: with a window
     * redirected and then repainted, the window's pixmap held the new colour and the screen still
     * held the old one ({@code tools/CompositeProbe}).
     */
    public boolean isRedirected() {
        if (redirectExempt) {
            return false;
        }
        if (redirected) {
            return true;
        }
        // An ancestor redirected its subwindows, which is what a compositor does to the root: the
        // whole tree is its to place. Walked rather than inherited into a flag on every descendant,
        // because the tree is a few deep and a flag would have to be undone correctly when the
        // ancestor unredirects, or when a window is reparented - the two cases a cached answer gets
        // wrong, and both are silent.
        for (XWindow ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
            if (ancestor.subwindowsRedirected) {
                return true;
            }
        }
        return false;
    }

    /** Composite's {@code Redirect} value: 0 Automatic, 1 Manual. */
    public int redirectUpdate() {
        return redirectUpdate;
    }

    public void setRedirected(boolean redirected, int update) {
        this.redirected = redirected;
        this.redirectUpdate = update;
    }

    /** Whether this window's own request redirects everything below it as well. */
    public boolean isSubwindowsRedirected() {
        return subwindowsRedirected;
    }

    public void setSubwindowsRedirected(boolean subwindowsRedirected) {
        this.subwindowsRedirected = subwindowsRedirected;
    }

    /** Whether this is a surface the server presents itself, which redirection does not touch. */
    public void setRedirectExempt(boolean redirectExempt) {
        this.redirectExempt = redirectExempt;
    }

    /** See {@link #transparentUntilDrawn}: the server's own surface, painted by nobody but a client. */
    public void setTransparentUntilDrawn(boolean transparentUntilDrawn) {
        this.transparentUntilDrawn = transparentUntilDrawn;
    }

    public boolean isTransparentUntilDrawn() {
        return transparentUntilDrawn;
    }

    /** The window's store, or null when nothing has ever been drawn into it. */
    public Framebuffer ownPixelsIfAllocated() {
        return ownPixels;
    }

    public int bitGravity() {
        return bitGravity;
    }

    public void setBitGravity(int bitGravity) {
        this.bitGravity = bitGravity;
    }

    public int winGravity() {
        return winGravity;
    }

    public void setWinGravity(int winGravity) {
        this.winGravity = winGravity;
    }

    public boolean isBackgroundPixmap() {
        return backgroundIsPixmap;
    }

    public void setBackgroundPixmap(XResource pixmap) {
        this.backgroundPixmap = pixmap;
        this.backgroundIsPixmap = true;
    }

    /** Takes a pixel value in this window's visual; stored converted to ARGB. */
    public void setBackgroundPixel(int pixel) {
        this.backgroundPixel = toArgb(pixel);
        this.backgroundIsPixmap = false;
    }

    public void setBackgroundArgb(int argb) {
        this.backgroundPixel = argb;
        this.backgroundIsPixmap = false;
    }

    /**
     * Converts a protocol pixel value to ARGB. Indexed visuals go through their colormap; the
     * default colormap is used until the window has one of its own, which matches the order in
     * which {@code CreateWindow} and {@code ChangeWindowAttributes} are applied.
     */
    private int toArgb(int pixel) {
        if (visual != null && visual.isTrueColor()) {
            return visual.toArgb(pixel);
        }
        XColormap map = colormap != null ? colormap : screen.defaultColormap();
        if (map != null) {
            return map.colorAt(pixel);
        }
        return 0xFF000000 | pixel;
    }

    public XResource backgroundPixmap() {
        return backgroundPixmap;
    }

    public int backgroundPixel() {
        return backgroundPixel;
    }

    public boolean isBorderPixmap() {
        return borderIsPixmap;
    }

    public void setBorderPixmap(XResource pixmap) {
        this.borderPixmap = pixmap;
        this.borderIsPixmap = true;
    }

    /** Takes a pixel value in this window's visual; stored converted to ARGB. */
    public void setBorderPixel(int pixel) {
        this.borderPixel = toArgb(pixel);
        this.borderIsPixmap = false;
    }

    public void setBorderArgb(int argb) {
        this.borderPixel = argb;
        this.borderIsPixmap = false;
    }

    public XResource borderPixmap() {
        return borderPixmap;
    }

    public int borderPixel() {
        return borderPixel;
    }

    public XColormap colormap() {
        return colormap;
    }

    public void setColormap(XColormap colormap) {
        this.colormap = colormap;
    }

    /** Properties of this window, keyed by the atom that names them. */
    public java.util.Map<Integer, XProperty> properties() {
        return properties;
    }

    public XCursor cursor() {
        return cursor;
    }

    public void setCursor(XCursor cursor) {
        this.cursor = cursor;
    }

    /**
     * How many outstanding {@code XFixesHideCursor} requests this window has - the pointer sprite is
     * not drawn while it is over this window or one of its descendants, and {@code XFixesShowCursor}
     * takes one back off.
     *
     * <p>Per window and counted, which is what the extension defines: the requests pair up, a
     * window that asked twice needs two shows, and the cursor is hidden <em>in that window</em>
     * rather than everywhere - a video player hiding the pointer must not hide it over the rest of
     * the screen. The count lives on the window rather than in the extension because that is its
     * lifetime: a window that goes away takes its hidden cursor with it.
     */
    public int cursorHideCount() {
        return cursorHideCount;
    }

    /** {@code XFixesHideCursor}: one more hide, and a negative count is not a state to have. */
    public void hideCursor() {
        cursorHideCount++;
    }

    /** {@code XFixesShowCursor}: one fewer hide, never below zero. */
    public void showCursor() {
        if (cursorHideCount > 0) {
            cursorHideCount--;
        }
    }

    public int eventMask() {
        return eventMask;
    }

    public void setEventMask(int eventMask) {
        this.eventMask = eventMask;
    }

    public int doNotPropagateMask() {
        return doNotPropagateMask;
    }

    public void setDoNotPropagateMask(int mask) {
        this.doNotPropagateMask = mask;
    }

    public List<OtherClient> otherClients() {
        return otherClients;
    }

    public boolean isRedirectToParent() {
        return redirectToParent;
    }

    public void setRedirectToParent(boolean redirectToParent) {
        this.redirectToParent = redirectToParent;
    }

    /** The root window this window belongs to. */
    public XWindow root() {
        XWindow window = this;
        while (window.parent != null) {
            window = window.parent;
        }
        return window;
    }

    public boolean isRoot() {
        return parent == null;
    }

    /**
     * Position of the window's origin - the top left of its contents, inside the border - in screen
     * coordinates.
     *
     * <p>{@code x} and {@code y} locate the window's <em>outer</em> corner on the wire, so they
     * point at the outside of its own border and the contents start one border further in. A
     * child's position is likewise measured from its parent's outer corner, so each step towards
     * the root adds that ancestor's own border as well. Leaving either border out of the sum puts
     * every window that has one - which is most toolkits' toplevels and everything xterm creates -
     * that many pixels up and to the left of where the client asked for it.
     */
    public int absoluteX() {
        int result = x + borderWidth;
        for (XWindow window = parent; window != null; window = window.parent) {
            result += window.x + window.borderWidth;
        }
        return result;
    }

    public int absoluteY() {
        int result = y + borderWidth;
        for (XWindow window = parent; window != null; window = window.parent) {
            result += window.y + window.borderWidth;
        }
        return result;
    }

    /** Inner rectangle in screen coordinates, excluding the border. */
    public Rectangle absoluteBounds() {
        return new Rectangle(absoluteX(), absoluteY(), width, height);
    }

    /** Rectangle including the border, in screen coordinates. */
    public Rectangle absoluteBoundsWithBorder() {
        int bw = borderWidth;
        return new Rectangle(absoluteX() - bw, absoluteY() - bw, width + 2 * bw, height + 2 * bw);
    }

    /**
     * The part of this window that can currently be drawn: its own rectangle intersected with
     * every ancestor's inner rectangle and the screen.
     */
    public Rectangle clipRect() {
        Rectangle clip = absoluteBounds();
        for (XWindow ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
            clip = clip.intersection(ancestor.absoluteBounds());
            if (clip.isEmpty()) {
                return clip;
            }
        }
        return clip.intersection(new Rectangle(0, 0, screen.width(), screen.height()));
    }

    /**
     * The area the window's border may occupy: its own rectangle including the border, clipped by
     * its ancestors and the screen.
     *
     * <p>This differs from {@link #clipRect()} precisely in not intersecting the window's own inner
     * rectangle, which would clip the border away entirely - a window's border lives outside its
     * content by definition.
     */
    public Rectangle outerClipRect() {
        Rectangle clip = absoluteBoundsWithBorder();
        for (XWindow ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
            clip = clip.intersection(ancestor.absoluteBounds());
            if (clip.isEmpty()) {
                return clip;
            }
        }
        return clip.intersection(new Rectangle(0, 0, screen.width(), screen.height()));
    }

    /**
     * The part of this window that is really visible: {@link #clipRect()} minus everything stacked
     * above it.
     *
     * <p>{@code clipRect()} stops at the ancestors, so a window that is covered by a sibling still
     * reports the covered part as its own. Drawing into it there would paint over the window on top,
     * and the covered window would never learn to draw again - which is what makes a dialog
     * disappear the moment the window underneath it repaints. This is the region drawing is clipped
     * to, and the region a revealed window has to be told to repaint.
     *
     * <p>Only the rectangles of the windows above are subtracted, never their subtrees: a child is
     * clipped by its parent, so a covering sibling's own rectangle already covers everything it can
     * put on screen. The result is a list of non-overlapping rectangles in screen coordinates.
     */
    public List<Rectangle> visibleRegion() {
        return visibleRegion(clipRect());
    }

    /** {@link #visibleRegion()} for the border, which lives outside the window's inner rectangle. */
    public List<Rectangle> outerVisibleRegion() {
        return visibleRegion(outerClipRect());
    }

    /**
     * Everything this window puts on the screen: its own rectangle and border, its children, minus
     * whatever is stacked above it.
     *
     * <p>{@link #visibleRegion()} is the window's <em>own</em> pixels and stops where a child
     * begins; this is the area it is seen in, and the difference matters for the shape a toolkit
     * gives every toplevel and dialog: a frame whose content window covers it completely. Such a
     * frame has no visible region of its own at all, so an unmap that gives back "what the window
     * showed" computed that way gives back nothing - and the dialog's picture stays on the screen
     * after its window has gone. A moved or resized window is the same story.
     */
    public List<Rectangle> coveredRegion() {
        return visibleRegion(outerClipRect(), true, false);
    }

    private List<Rectangle> visibleRegion(Rectangle clip) {
        return visibleRegion(clip, false, false);
    }

    /**
     * The pixels this window owns, in <em>window</em> coordinates - the region its own store covers.
     *
     * <p>Its rectangle, with its own and its ancestors' bounding shapes taken out, and with neither
     * its children nor whatever is stacked above it subtracted: this is the window's own drawing
     * surface, and a child is painted into a surface of its own rather than into a hole here. What
     * is <em>shown</em> of it, on the other hand, is {@link #visibleRegion()}, and that is what the
     * composite back onto the screen is clipped to.
     */
    public List<Rectangle> ownRegion() {
        return toLocal(visibleRegion(clipRect(), true, true));
    }

    /**
     * What this window <em>shows</em> of its own pixels, children excluded and whatever is stacked
     * above it excluded, in window coordinates - the region a picture reads with the default
     * {@code ClipByChildren}, and the region a revealed window is told to repaint.
     */
    public List<Rectangle> ownShownRegion() {
        return toLocal(visibleRegion(clipRect(), false, false));
    }

    /**
     * What this window shows with its children counted as part of it, in window coordinates - what a
     * picture reads with {@code IncludeInferiors}. What is stacked above the window is still
     * excluded: those pixels are not its own to hand out, children or not.
     */
    public List<Rectangle> shownRegion() {
        return toLocal(visibleRegion(clipRect(), true, false));
    }

    /** Moves a region of screen rectangles into this window's own coordinates. */
    private List<Rectangle> toLocal(List<Rectangle> screen) {
        if (screen.isEmpty()) {
            return screen;
        }
        int originX = absoluteX();
        int originY = absoluteY();
        List<Rectangle> local = new ArrayList<>(screen.size());
        for (Rectangle part : screen) {
            local.add(new Rectangle(part.x - originX, part.y - originY, part.width, part.height));
        }
        return local;
    }

    /** Returned by {@link #pixelFromInferiors} when no child covers the point. */
    public static final int NO_CHILD_PIXEL = Integer.MIN_VALUE;

    /**
     * The pixel the topmost child of this window puts at a point given in this window's
     * coordinates, or {@link #NO_CHILD_PIXEL} when no child covers it.
     *
     * <p>This is what {@code IncludeInferiors} reads: a window's own store holds its own pixels and
     * not its children's - a child draws into a store of its own - so the child that covers a point
     * has to be found and asked, topmost first, exactly as the screen would have shown it.
     */
    public int pixelFromInferiors(int x, int y) {
        for (int i = children.size() - 1; i >= 0; i--) {
            XWindow child = children.get(i);
            if (!child.isMapped() || child.isInputOnly()) {
                continue;
            }
            int childX = x - child.x();
            int childY = y - child.y();
            if (childX < 0 || childY < 0 || childX >= child.width() || childY >= child.height()) {
                continue;
            }
            if (child.boundingShape() != null
                    && !ShapeState.containsPoint(child.boundingShape(), childX, childY)) {
                continue;
            }
            int deeper = child.pixelFromInferiors(childX, childY);
            if (deeper != NO_CHILD_PIXEL) {
                return deeper;
            }
            if (child.keepsOwnPixels()) {
                // The store answers its background where the child has not drawn, which is what the
                // child shows there.
                return child.ownPixels().pixel(childX, childY);
            }
            return screen().framebuffer().pixel(child.absoluteX() + childX,
                    child.absoluteY() + childY);
        }
        return NO_CHILD_PIXEL;
    }

    /**
     * @param includeChildren true for the area the window covers rather than the pixels it owns:
     *                        its children are painted inside it, so they are part of what it shows
     *                        and must be given back when it goes away, while they are not part of
     *                        its own drawing surface - which stops at the child's rectangle.
     */
    private List<Rectangle> visibleRegion(Rectangle clip, boolean includeChildren,
                                          boolean ignoreObscuring) {
        if (clip.isEmpty()) {
            return List.of();
        }
        List<Rectangle> region = List.of(clip);
        // Walk this window and then each ancestor: at every level, the siblings stacked above the
        // one we came through are the ones that can cover it - and this window's own children,
        // which are drawn inside it, are above its own content just as surely.
        for (XWindow window = this; window != null && !region.isEmpty(); window = window.parent()) {
            if (window.boundingShape() != null) {
                // The shape takes pixels away, at every level: the window's own corners are not
                // part of it, and neither is the part of a child that falls outside its parent's.
                region = ShapeState.intersect(region, ShapeState.translate(window.boundingShape(),
                        window.absoluteX(), window.absoluteY()));
                if (region.isEmpty()) {
                    return region;
                }
            }
            if (window == this && !includeChildren) {
                region = subtractMappedChildren(this, region);
            }
            XWindow parent = window.parent();
            if (parent == null) {
                break;
            }
            if (ignoreObscuring) {
                continue;
            }
            List<XWindow> siblings = parent.children();
            for (int i = siblings.indexOf(window) + 1; i < siblings.size(); i++) {
                region = subtractWindow(region, siblings.get(i));
                if (region.isEmpty()) {
                    return region;
                }
            }
        }
        return region;
    }

    /** A window's children are drawn inside it, so they take their rectangles out of its own. */
    private static List<Rectangle> subtractMappedChildren(XWindow window, List<Rectangle> region) {
        for (XWindow child : window.children()) {
            region = subtractWindow(region, child);
            if (region.isEmpty()) {
                return region;
            }
        }
        return region;
    }

    private static List<Rectangle> subtractWindow(List<Rectangle> region, XWindow above) {
        if (!above.isMapped() || above.isInputOnly()) {
            return region;
        }
        if (above.isTranslucent()) {
            // A window whose pixels carry transparency does not hide what is behind it, so it takes
            // nothing away: what it has not drawn shows the screen underneath, and a window under it
            // that stopped painting there would leave a hole wherever the transparency is.
            //
            // It is also what makes Composite's overlay possible at all (B5): the overlay is a
            // screen-sized window above everything, and if it counted as an obscurer every window
            // below it would report no visible region at all - so no client would be sent an Expose
            // again. The store is what makes the point measurable rather than theoretical.
            return region;
        }
        // Only the child's own rectangle: a child is clipped by its parent, so its whole subtree is
        // inside it and needs no separate subtraction - except where a bounding shape takes part of
        // that rectangle away, because what the window covers is then its shape and the corners it
        // no longer has are the screen underneath's again. A window whose corners kept being
        // subtracted would never be asked to paint them, and they would stay whatever the covered
        // window left there.
        List<Rectangle> shape = above.boundingShape();
        return ShapeState.subtract(region, shape == null
                ? List.of(above.absoluteBoundsWithBorder())
                : ShapeState.translate(shape, above.absoluteX(), above.absoluteY()));
    }

    /**
     * Whether this window is stacked above {@code other}, which decides whose pixels are really on
     * screen where the two overlap.
     *
     * <p>Windows in different trees are unrelated: the root is above nothing.
     */
    public boolean isAbove(XWindow other) {
        if (other == null || other == this) {
            return false;
        }
        if (this.isAncestorOf(other)) {
            // A child is drawn inside and therefore on top of its parent.
            return false;
        }
        if (other.isAncestorOf(this)) {
            return true;
        }
        // Unrelated branches: the first common ancestor is where the two chains meet, and the
        // sibling that comes later in its children list is the one on top.
        for (XWindow mine = this; mine != null; mine = mine.parent()) {
            XWindow parent = mine.parent();
            if (parent == null) {
                return false;
            }
            List<XWindow> siblings = parent.children();
            for (XWindow theirs = other; theirs != null; theirs = theirs.parent()) {
                if (theirs.parent() == parent) {
                    return siblings.indexOf(mine) > siblings.indexOf(theirs);
                }
            }
        }
        return false;
    }

    /** True when the window and all of its ancestors are mapped. */
    public boolean isViewable() {
        if (!mapped) {
            return false;
        }
        for (XWindow window = parent; window != null; window = window.parent) {
            if (!window.mapped) {
                return false;
            }
        }
        return true;
    }

    public int mapState() {
        if (!mapped) {
            return IS_UNMAPPED;
        }
        return isViewable() ? IS_VIEWABLE : IS_UNVIEWABLE;
    }

    public boolean isAncestorOf(XWindow other) {
        for (XWindow window = other; window != null; window = window.parent) {
            if (window == this) {
                return true;
            }
        }
        return false;
    }

    /** Finds the child of this window that contains the given screen point, or null. */
    public XWindow childAt(int screenX, int screenY) {
        for (int i = children.size() - 1; i >= 0; i--) {
            XWindow child = children.get(i);
            if (!child.mapped) {
                continue;
            }
            if (child.absoluteBoundsWithBorder().contains(screenX, screenY)) {
                return child;
            }
        }
        return null;
    }

    /** Deepest mapped descendant containing the point, or this window. */
    public XWindow windowAt(int screenX, int screenY) {
        XWindow window = this;
        while (true) {
            XWindow child = window.childAt(screenX, screenY);
            if (child == null) {
                return window;
            }
            window = child;
        }
    }

    /** Every mask any client has selected on this window, used for {@code GetWindowAttributes}. */
    public int allEventMasks() {
        int mask = eventMask;
        for (OtherClient other : otherClients) {
            mask |= other.mask;
        }
        return mask;
    }

    /** The mask {@code client} has selected on this window, or 0 if it has none. */
    public int eventMaskFor(ClientConnection client) {
        if (owner() == client) {
            return eventMask;
        }
        for (OtherClient other : otherClients) {
            if (other.client == client) {
                return other.mask;
            }
        }
        return 0;
    }

    /** True when any client (including non-owners) selected any bit of {@code mask}. */
    public boolean anyClientSelected(int mask) {
        return (allEventMasks() & mask) != 0;
    }

    /**
     * Registers, updates or removes a non-owning client's event mask. A zero mask removes the
     * registration, which is what {@code ChangeWindowAttributes} does when a client clears it.
     */
    public void setOtherClientMask(ClientConnection client, int mask) {
        for (int i = 0; i < otherClients.size(); i++) {
            OtherClient other = otherClients.get(i);
            if (other.client == client) {
                if (mask == 0) {
                    otherClients.remove(i);
                } else {
                    other.mask = mask;
                }
                return;
            }
        }
        if (mask != 0) {
            otherClients.add(new OtherClient(client, mask));
        }
    }

    public boolean takeNeedsFullExpose() {
        boolean result = needsFullExpose;
        needsFullExpose = false;
        return result;
    }

    public void markNeedsFullExpose() {
        needsFullExpose = true;
    }

    /** Validates that {@code id} names one of our children before reparenting. */
    public void checkNotSelfOrAncestor(XWindow candidate) {
        if (candidate == this || isAncestorOf(candidate)) {
            throw ProtocolException.badMatch();
        }
    }

    public void addChild(XWindow child, int position) {
        if (position < 0 || position >= children.size()) {
            children.add(child);
        } else {
            children.add(position, child);
        }
        child.setParent(this);
    }

    public void removeChild(XWindow child) {
        children.remove(child);
    }

    /** Raises the window to the top of its parent's stack. */
    public void raise() {
        if (parent != null && parent.children.remove(this)) {
            parent.children.add(this);
        }
    }

    /** Lowers the window to the bottom of its parent's stack. */
    public void lower() {
        if (parent != null && parent.children.remove(this)) {
            parent.children.add(0, this);
        }
    }

    @Override
    public String typeName() {
        return "window";
    }

    /** An event mask registered by a client that does not own the window. */
    public static final class OtherClient {
        public final ClientConnection client;
        public int mask;

        public OtherClient(ClientConnection client, int mask) {
            this.client = client;
            this.mask = mask;
        }
    }

    // ------------------------------------------------------------------
    // XInput2 selections (B4)
    // ------------------------------------------------------------------

    /**
     * The XInput2 event selections a client made on this window, one entry per (client, device).
     *
     * <p>Kept here rather than in {@code XInputExtension} for the same reason the core masks are:
     * a selection belongs to the window it was made on, so it is dropped when the window is, and
     * nothing has to be told to clean it up. It is a second list rather than a field on
     * {@link OtherClient} because a client selects XI2 *per device* and may be the window's owner as
     * well - the owner is the common case for a toolkit's own windows.
     *
     * <p>The masks are the wire's own: words of thirty two bits, as {@code XISelectEvents} carries
     * them, because XI2's mask bits are numbered from one and reach past the core protocol's
     * thirty two.
     */
    private final List<Xi2Selection> xi2Selections = new ArrayList<>();

    /**
     * One client's XInput2 selection on a window: which device it named, and the mask words for it.
     *
     * <p>Deliberately a plain record of what the client asked for. What a device id *means* (that
     * zero is every device and one is every master) belongs to the input side, which is where the
     * delivery rule lives; a window only holds what was selected on it.
     */
    public record Xi2Selection(ClientConnection client, int deviceId, int[] mask) {
    }

    /**
     * Records (or clears) a client's XInput2 selection for one device.
     *
     * <p>A zero mask removes the entry, which is how a client unsubscribes - the same rule
     * {@link #setOtherClientMask} follows for the core masks.
     */
    public void selectXi2(ClientConnection client, int deviceId, int[] mask) {
        xi2Selections.removeIf(selection -> selection.client() == client
                && selection.deviceId() == deviceId);
        for (int word : mask) {
            if (word != 0) {
                xi2Selections.add(new Xi2Selection(client, deviceId, mask));
                return;
            }
        }
    }

    /** A copy of the selections on this window, for the delivery rule, a reply, or a test. */
    public List<Xi2Selection> xi2Selections() {
        return List.copyOf(xi2Selections);
    }
}
