package cn.oyzh.fx.xserver.display;

import cn.oyzh.fx.xserver.display.AwtTrayIcon;
import cn.oyzh.fx.xserver.display.DisplayBackend;
import cn.oyzh.fx.xserver.draw.Framebuffer;
import cn.oyzh.fx.xserver.input.Keymap;
import javafx.animation.AnimationTimer;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Presents the screen in a JavaFX window.
 *
 * <p>The window shows a {@link WritableImage} whose pixel buffer wraps the framebuffer's own
 * {@code int[]}: sharing the array means presenting a frame is a matter of saying which rectangle
 * changed, never of copying pixels. What the window shows is a *sub-rectangle* of that image - the
 * part of the screen its client's windows occupy - so scene coordinates are screen coordinates
 * shifted by the rectangle's origin, and both the blit and the input path do that shift.
 *
 * <p>The runtime is started with {@link Platform#startup} rather than {@code Application.launch}:
 * this project loads JavaFX from the classpath, and launching through an {@code Application}
 * subclass is only supported when the jars are on the module path. Starting the toolkit directly
 * also lets the server keep its own main loop instead of handing the main thread to JavaFX.
 */
public final class JavaFxBackend implements DisplayBackend {

    private static final Logger log = LoggerFactory.getLogger(JavaFxBackend.class);

    static {
        // **The window's own title bar is a preview feature of JavaFX 27** (`StageStyle.EXTENDED`
        // and the header bar that goes with it), and opting in has to happen before the toolkit reads
        // the property - which is why it is here rather than on a command line: this class is loaded
        // before the toolkit starts, so this is the earliest the backend can speak for itself. An
        // application that has already answered for itself keeps its answer, and one that says no
        // gets the platform's own decorated window instead (see the stage in {@link #openWindow}).
        if (System.getProperty("javafx.enablePreview") == null) {
            System.setProperty("javafx.enablePreview", "true");
        }
    }

    /**
     * How long the toolkit gets to run a task before it counts as unusable.
     */
    private static final long STARTUP_TIMEOUT_SECONDS = 3;

    /**
     * True once the toolkit has been started and has run a task for us.
     */
    private static boolean toolkitStarted;

    private final String title;
    private final AtomicBoolean stopped = new AtomicBoolean();

    /**
     * Guards {@link #pendingDamage} and {@link #flushScheduled}.
     */
    private final Object damageLock = new Object();

    /**
     * The damage waiting to be flushed, grown by every {@link #present} until the FX thread runs.
     */
    private Rectangle pendingDamage;
    /**
     * The sprite that goes with that damage; the latest one wins, since only it is on screen.
     */
    private CursorSprite pendingCursor;
    private boolean flushScheduled;

    private volatile boolean closed;

    private InputSink input;

    /**
     * The screen's framebuffer, used by the diagnostic dump and nothing else.
     */
    private Framebuffer framebuffer;

    // Everything below is only ever touched on the JavaFX Application Thread.
    private Stage stage;
    private Scene scene;
    /** The canvas the screen is drawn into - the window's *content*, below the header bar. */
    private Canvas canvas;

    /**
     * The node the window's content lives in, which is also what owns the scene's focus and its input
     * method - see the setup in {@link #openWindow} for why the *focus owner* has to carry the input
     * method handlers for the platform to compose anything at all.
     */
    private Pane content;

    /**
     * A node that holds the focus for exactly as long as it takes to hand it back to {@link #content}.
     *
     * <p>See the focus listener in {@link #openWindow}: JavaFX decides whether the platform should be
     * asked for input method text only when the scene's focus owner <em>changes</em>, so a focus the
     * content never loses is an input method that is never re-enabled.
     */
    private Pane focusParking;

    /**
     * Where the pointer last was, in scene coordinates: the place the platform's input method is told
     * to put its candidate window, since this side has no caret - the text is drawn by an X client.
     */
    private double pointerSceneX;
    private double pointerSceneY;

    /**
     * How many characters of the input method's pre-edit the client's field is currently showing.
     *
     * <p>The composition belongs to the platform and the application never sees it, so the server
     * types it into the field itself and has to know how much to take back when it changes - see
     * {@link #inputMethodText}.
     */
    private int preEditLength;

    /**
     * The key that leaves full screen, which is the platform's own convention rather than Escape (see
     * the call in {@link #openWindow} for why Escape is not it).
     *
     * <p>**This is the one place in the backend that reads the platform's name, and it has to**: macOS
     * leaves full screen with Control-Command-F and Windows and the X desktops use F11, and a server
     * that offered only the macOS one leaves a Windows or Linux user with no way out of a full screen
     * window at all - which is what the user of this project found ("我记得你有个全屏退出快捷键就是仅
     * 适配macos"). Package-private and taking the name as a value so both answers can be pinned.
     */
    static javafx.scene.input.KeyCombination fullScreenExitKey(String osName) {
        return javafx.scene.input.KeyCombination.valueOf(keyFor(osName));
    }

    /** The name of that key: {@code Ctrl+Meta+F} on macOS, {@code F11} everywhere else. */
    static String keyFor(String osName) {
        return osName != null && osName.toLowerCase(java.util.Locale.ROOT).startsWith("mac")
                ? "Ctrl+Meta+F"
                : "F11";
    }

    /**
     * The platform's own full screen key as it is matched against a keystroke, kept rather than
     * rebuilt because one is read for every key the window receives. What the key is, and why it is
     * the platform's rather than Escape, is {@link #fullScreenExitKey}.
     */
    private javafx.scene.input.KeyCombination fullScreenKey;

    /**
     * When the window last entered or left full screen, in {@code System.currentTimeMillis()}. Read
     * by the full screen key so a press that JavaFX has already answered is not answered twice - see
     * {@link #keyChanged(javafx.scene.input.KeyEvent, boolean)}.
     */
    private long fullScreenChangedAt;

    /**
     * How long a full screen state change is taken to belong to the press that made it, in
     * milliseconds. JavaFX's own full screen exit runs before the scene's key filter and does not
     * consume the event it answers, so a press of {@link #fullScreenKey} on a full screen window
     * arrives at {@link #keyChanged(javafx.scene.input.KeyEvent, boolean)} with the window already
     * out of full screen; without a window this wide the second half of that one press would put it
     * straight back in (measured 2026-10-13: one F11 press left full screen and re-entered it).
     */
    static final long FULL_SCREEN_PRESS_MILLIS = 500;

    /**
     * The client-area header bar: the window's title bar, drawn by JavaFX (JavaFX 27's
     * {@link StageStyle#EXTENDED}), or null where the platform has no extended windows and the
     * platform's own title bar is what the window has.
     */
    private HeaderBar headerBar;
    private Label titleLabel;
    /**
     * True while the platform pointer is over what this window *shows* - the client's page, not the
     * window's chrome - so {@link #hostCursor} can decide. Taken from where the pointer really is
     * rather than from the enter and leave events alone - see {@link #updatePointerOverWindow}.
     */
    private boolean pointerOverWindow;
    /** Whether the platform pointer is currently the one this window asked to be hidden. */
    private boolean cursorHidden;
    /** Whether the window is on the screen: it is built before it is shown - see {@link #showWindow}. */
    private boolean shown;
    /** What to place the window by when it is first shown, in framebuffer pixels. */
    private Dimension windowSize = new Dimension();
    /**
     * Whether a mouse button is down on this window, wherever it went down.
     *
     * <p>A press over the window's chrome is not the client's business and never reaches it, but the
     * window is still being moved by it - and a move is a different gesture from a resize of an edge,
     * which is the distinction {@link #windowMovedByUser} needs to make.
     */
    private boolean hostButtonHeld;
    /**
     * True while no client is connected, so the window has no pointer of its own to show.
     */
    private boolean idle;
    /**
     * True from the moment this backend decides its window is going until the platform has taken it
     * away - see {@link #handBackPointerThenClose}, which clears {@link #stage} at the decision so
     * that nothing can schedule the close twice, and this so that there is still an answer to "is
     * the window there". Touched on the FX thread only.
     */
    private boolean closePending;
    /**
     * Whether this window is counted in {@link #FOCUSED_WINDOWS}. Touched on the FX thread only, and
     * kept so a window cannot be counted twice or lost from the count when it closes.
     */
    private boolean countedFocused;
    /**
     * The last sprite the server handed this window, so the canvas can be repainted around it when
     * the focus changes and it stops or starts being drawn. {@code pendingCursor} cannot answer that:
     * a flush clears it, and most flushes carry no sprite because the sprite did not change.
     */
    private volatile CursorSprite lastCursorSprite;
    /**
     * True while the backend is resizing the window itself, so the size listener does not report
     * the change back as a resize the user made.
     */
    private boolean adjustingWindow;
    /**
     * True while the backend is placing the window itself, so the move listeners do not report a
     * remembered position back as a move the user made.
     */
    private boolean placingWindow;
    /**
     * The part of the screen this window shows, in framebuffer coordinates. Zero-sized until the
     * server sets it, which covers the whole screen: that is what single-window mode wants.
     */
    private Rectangle viewport = new Rectangle();
    /**
     * How many windows are open, so each new one can be placed beside the last.
     */
    private static final AtomicInteger LIVE_WINDOWS =
            new AtomicInteger();
    private GraphicsContext graphics;
    private WritableImage image;
    private PixelBuffer<IntBuffer> pixelBuffer;

    /**
     * The pointer sprite as a JavaFX image, rebuilt when a client replaces the cursor.
     */
    private WritableImage cursorImage;
    private int[] cursorPixels;

    /**
     * The image the framebuffer is presented through. Exposed so a test can read it back and prove
     * the pixels reached JavaFX, which a window on a hidden display cannot show.
     */
    public WritableImage image() {
        return image;
    }

    /**
     * How many framebuffer pixels one logical one is worth, from the screen. A display that draws
     * two device pixels per logical one needs a screen of twice the pixels to be sharp in the same
     * space, and the window then shows that larger picture at the smaller logical size - one
     * framebuffer pixel per device pixel, which is the whole point.
     */
    private int scale = 1;
    /**
     * How much smaller than its own layout the content is drawn, when the whole of what the window
     * shows will not fit on the display. 1 - nothing scaled, one framebuffer pixel per layout pixel -
     * whenever it fits, which is every ordinary case.
     *
     * <p><b>Windows does not show a window that is bigger than the screen, and that is the whole
     * reason this exists.</b> The window is sized to what it shows (see {@link #sizeFrameToContent}),
     * and what it shows is the part of the screen the client's windows occupy - which for a client
     * that draws itself at the size of the screen it was told about is very nearly the whole screen.
     * A window that comes out wider or taller than the display's usable area is *clipped* by the
     * platform: the part outside the display is not drawn and cannot be reached, which measured as
     * the sides of the client's own window going missing (2026-10-13: a 1800x900 xclock on a display
     * whose usable area is 1707x912 points gave a frame 1814x667, and the right 107 points of it, the
     * client's own clock face included, were simply off screen). macOS is why this was not seen
     * there: it is happy to draw a window larger than the display and lets the user move it, so the
     * same server looked correct.
     *
     * <p>The window is not made to fit by cropping it - that is the bug - and not by asking the
     * client to draw less: the picture is drawn into the canvas at this factor instead, so all of it
     * stays visible and only its size on screen changes. It is what a VNC viewer does when the remote
     * screen is bigger than its own window.
     */
    private double viewScale = 1.0;
    private int screenWidth;
    private int screenHeight;
    private int pointerX;
    private int pointerY;
    private int heldButtons;

    public JavaFxBackend(String title) {
        this.title = title;
    }

    /**
     * Starts the JavaFX runtime and waits until it is really running, throwing when JavaFX cannot
     * be used at all. {@code BackendFactory} calls this before choosing this backend so the server
     * can fall back to Swing instead of failing; further calls are no-ops.
     */
    public static void verifyAvailable() {
        startupToolkit();
    }

    @Override
    public String name() {
        return "javafx";
    }

    @Override
    public void start(Screen screen, int displayNumber, InputSink input) {
        startupToolkit();
        this.input = input;
        this.scale = screen.scale();
        Framebuffer framebuffer = screen.framebuffer();
        this.framebuffer = framebuffer;
        int width = screen.width();
        int height = screen.height();
        // The framebuffer is row major with stride == width, so its array is already laid out the
        // way the pixel buffer wants it and can be wrapped rather than copied.
        IntBuffer pixels = IntBuffer.wrap(framebuffer.pixels());
        try {
            runOnFxThreadAndWait(() -> openWindow(width, height, pixels));
        } catch (RuntimeException | Error e) {
            // An open toolkit would keep the JVM alive through its non-daemon FX thread even though
            // this backend never got as far as showing anything.
//            Platform.exit();
            throw e;
        }
        javafx.stage.Screen display = javafx.stage.Screen.getPrimary();
        log.info("javafx window {}x{} for display :{}, on a {}x{} display at scale {}x{}",
                width, height, displayNumber, display.getBounds().getWidth(),
                display.getBounds().getHeight(), display.getOutputScaleX(), display.getOutputScaleY());
    }

    @Override
    public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
        if (closed || damage == null) {
            return;
        }
        lastCursorSprite = cursor;                   // read on the FX thread by repaintCursorArea
        // The pixels live in the buffer the framebuffer draws into, so nothing has to be copied
        // here: only the union of the damage since the last flush is handed to the FX thread.
        synchronized (damageLock) {
            pendingDamage = pendingDamage == null
                    ? new Rectangle(damage)
                    : pendingDamage.union(damage);
            pendingCursor = cursor;
            if (flushScheduled) {
                return;
            }
            flushScheduled = true;
        }
        if (Platform.isFxApplicationThread()) {
            flushDamage();
            return;
        }
        try {
            Platform.runLater(this::flushDamage);
        } catch (IllegalStateException runtimeStopped) {
            // Platform.exit() raced with the last frame; there is nothing left to show anyway.
            closed = true;
        }
    }

    @Override
    public void setViewport(Rectangle wanted) {
        runOnFxThreadAndWait(() -> {
            if (wanted.equals(viewport)) {
                return;
            }
            Rectangle previous = viewport;
            viewport = new Rectangle(wanted);
            Stage open = stage;
            if (open == null) {
                // Called before the window exists: openWindow will size itself from the rectangle.
                return;
            }
            if (platformOwnsFrame()) {
                // **A drag in flight: the pixels move, the window's size does not.** While the user has
                // the frame's edge under the pointer, the platform is the one computing the size from
                // the cursor, and its reference for the next step is the window it just made. Sizing
                // the window back to what the client's window is *now* (which is always one step
                // behind) shifts that reference - and for a left or top edge, where the origin is part
                // of the arithmetic, the two sides then fight: measured with xclock, dragging the left
                // edge 40 px moved the window 40 px and shrank it by 5, and the requested sizes bounced
                // 265 -> 298 -> 297 -> 295 instead of falling. Only the *canvas* follows here, so the
                // app is drawn at the size it is becoming; the window is left to the drag. A full
                // screen window is the same rule for the same reason - see
                // {@link #platformOwnsFrame} - and this is also the path that keeps a viewport set
                // during one from being recorded as a size to put back later.
                canvas.setWidth(logical(viewport.width));
                canvas.setHeight(logical(viewport.height));
                graphics = canvas.getGraphicsContext2D();
                drawViewport();
                return;
            }
            // Sizing the window to the content is the backend's own doing, and the size listener
            // would otherwise read it as the user dragging the edge and ask for that size again.
            adjustingWindow = true;
            try {
                // **The platform applies a window size in two steps, and the step in between is
                // neither the old size nor the new one.** Measured with an X client that keeps its
                // own window (xclock, single-window mode, 2026-10-04): the resize callback arrived
                // with the *new width and the old height* (202x800 for a viewport of 202x202), the
                // window was read as "the user dragged" and the client's window was resized to it,
                // which moved the content bounds by the window's border - and the two of them then
                // walked the window up two pixels at a time for ever, flickering, which is what the
                // user reported. So until the platform reports the size this server set, its reports
                // are ignored and the size is put back; the flag is cleared the moment the two agree.
                viewportPending = true;
                pendingFromWidth = previous.width;
                pendingFromHeight = previous.height;
                canvas.setWidth(logical(viewport.width));
                canvas.setHeight(logical(viewport.height));
                graphics = canvas.getGraphicsContext2D();
                drawViewport();
                // The window is sized to what it shows, by setting its size rather than by
                // sizeToScene(): the canvas is unmanaged - deliberately, so it cannot put a floor
                // under the window the user cannot drag below - and an unmanaged child contributes
                // nothing to its parent's preferred size, so sizeToScene() leaves the window at the
                // size the scene was built with. A window whose content changed therefore used to
                // keep the size the user had dragged it to.
                //
                // The decoration is not re-measured here: the platform applies a new window size
                // asynchronously, so a measurement taken now would read the old size and be wrong by
                // a title bar's worth - once per viewport change, which walks the window down a
                // title bar at a time. It was measured when the window was opened, where it is known
                // to fit its content, and it does not change while the window lives.
                sizeFrameToContent();
                if (log.isDebugEnabled()) {
                    log.debug("window sized to {}x{} for content {}x{}",
                            open.getWidth(), open.getHeight(), viewport.width, viewport.height);
                }
                keepOnScreen();
            } finally {
                adjustingWindow = false;
            }
        });
    }

    @Override
    public void setWindowTitle(String title) {
        runOnFxThreadAndWait(() -> {
            // Null means the client publishes no title: the server's own name is the fallback.
            String wanted = title == null || title.isEmpty() ? this.title : title;
            if (stage != null) {
                // The stage's title is still worth setting: the platform uses it for the miniaturized
                // preview, which has a title of its own next to the picture.
                stage.setTitle(wanted);
            }
            if (titleLabel != null) {
                // And this is where it is actually read, since the platform's title bar is not there.
                titleLabel.setText(wanted);
            }
        });
    }

    @Override
    public void setWindowIcon(int[] argb, int width, int height) {
        runOnFxThreadAndWait(() -> {
            if (stage == null) {
                return;
            }
            if (argb == null || width <= 0 || height <= 0) {
                stage.getIcons().clear();
                return;
            }
            WritableImage icon = new WritableImage(width, height);
            icon.getPixelWriter().setPixels(0, 0, width, height,
                    PixelFormat.getIntArgbInstance(), argb, 0, width);
            stage.getIcons().setAll(icon);
        });
    }

    @Override
    public void setMaxClientArea(int width, int height) {
        runOnFxThreadAndWait(() -> {
            if (stage == null || width <= 0 || height <= 0) {
                return;
            }
            // Remembered whether or not it can be applied now: a limit is a constraint on the frame,
            // and putting one on a full screen window is a way to have the platform resize it - the
            // same thing that takes the window out of its Space (see {@link #sizeFrameToContent}).
            // The server only sends this when the number changes, so the one that arrives during
            // full screen is applied on the way out instead.
            maxClientAreaWidth = width;
            maxClientAreaHeight = height;
            applyMaxClientArea();
            if (log.isDebugEnabled()) {
                // How far the window may be dragged is otherwise only visible by dragging it, and a
                // limit that is somehow too small cannot be resized out of.
                log.debug("window may be dragged up to {}x{} of content, plus {}x{} around it",
                        width, height, sceneBorderWidth, headerHeight() + sceneBorderHeight);
            }
        });
    }

    /** The limit the server last asked for, until a moment when the window can take it. */
    private int maxClientAreaWidth;
    private int maxClientAreaHeight;

    /**
     * Applies the remembered limit, unless the window is full screen. Runs on the FX thread.
     */
    private void applyMaxClientArea() {
        if (stage == null || maxClientAreaWidth <= 0 || maxClientAreaHeight <= 0
                || stage.isFullScreen()) {
            return;
        }
        stage.setMaxWidth(frameWidth(logical(maxClientAreaWidth) * viewScale));
        stage.setMaxHeight(frameHeight(logical(maxClientAreaHeight) * viewScale));
    }

    /**
     * What the platform adds around the *scene* - borders, where the platform has any - measured
     * while the window fits the content exactly.
     *
     * <p>This is not the title bar any more: since JavaFX 27 the window's title bar is a
     * {@link HeaderBar} **inside** the scene (see {@link #headerHeight}), and what is left outside
     * the scene is only the platform's own frame - nothing at all on macOS.
     */
    private void rememberSceneBorder() {
        if (stage == null || scene == null) {
            return;
        }
        sceneBorderWidth = Math.max(0, stage.getWidth() - scene.getWidth());
        sceneBorderHeight = Math.max(0, stage.getHeight() - scene.getHeight());
    }

    private double sceneBorderWidth;
    private double sceneBorderHeight;

    /**
     * The header bar's height in points - the window's own title bar - or zero while the window has
     * none: a platform without extended windows, where the platform's title bar is the one outside
     * the scene, and full screen, where the platform hides its chrome and the picture takes the
     * whole display.
     */
    private double headerHeight() {
        return headerBar != null && headerBar.isManaged() ? HEADER_HEIGHT : 0;
    }

    /** The window's width for a content width, in points: the content plus the platform's frame. */
    private double frameWidth(double contentWidth) {
        return contentWidth + sceneBorderWidth;
    }

    /** The window's height for a content height, in points: the content plus the header and frame. */
    private double frameHeight(double contentHeight) {
        return windowHeight(contentHeight, headerHeight(), sceneBorderHeight);
    }

    /**
     * The window's height for a content height, in points: the content, the header bar above it, and
     * whatever the platform puts around the scene.
     *
     * <p>Two different things are above the content and both are easy to forget: with an extended
     * window the header bar is *inside* the scene (JavaFX 27) and without one the platform's title bar
     * is *outside* it. Either way the client is not shown it, so either way it belongs to this sum.
     * Package-private and static so the one piece of arithmetic the header bar changed can be pinned
     * without a window.
     */
    static double windowHeight(double contentHeight, double headerHeight, double sceneBorderHeight) {
        return contentHeight + headerHeight + sceneBorderHeight;
    }

    /**
     * Until this millisecond, a content resize is a window on its way open rather than the user
     * dragging it: the sizes a window passes through before the platform has applied the size this
     * backend asked for, which arrive on the scene's size listeners all the same.
     */
    private long openingUntil;

    /**
     * Until this millisecond, the frame's own geometry is the platform's doing - a zoom of the
     * window's title bar, or full screen - and not a drag of its edge.
     *
     * <p>Both are reported as a resize like any other and the client should take the size the window
     * has; what they must not do is *anchor* it: a drag's far edge is put back where the user left it
     * (A39), and a zoom moves the window to the corner of the display on purpose, so a stale anchor
     * would drag it back out of it.
     */
    private long platformGestureUntil;

    /**
     * The content size the platform's own gesture has stopped at, waiting for the frame to hold still
     * long enough to be sure the animation is over - see {@link #clientAreaChanged}.
     */
    private Dimension platformSettledContent;

    /**
     * Fires when the platform's gesture has stopped changing the frame, and tells the client the size
     * it stopped at. Restarted by every size the animation passes through.
     */
    private final javafx.animation.PauseTransition platformSettle =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(160));

    /**
     * True while a size this server set is still being applied by the platform. Its resize reports
     * in that window are its own halves of the change, not the user's; see {@link #setViewport}.
     */
    private boolean viewportPending;

    /** The viewport size before the pending one, so the platform's half-applied step is recognisable. */
    private int pendingFromWidth;
    private int pendingFromHeight;

    /**
     * True from the moment a user resize is reported, until a second has passed with no further one:
     * the platform is driving the window's size and this backend must not size it back. The deadline
     * is refreshed by every report and read by {@link #dragInFlight}, so it means "the drag has
     * stopped" - which is also a safety net for a client that never takes the size it was given, and
     * the only end-of-drag signal there is (macOS's live resize delivers no mouse event to the
     * window; see {@link #dragInFlight}).
     */
    private boolean userDragPending;
    private long userDragUntil;

    /**
     * Where the frame stood the last time no drag was in flight: its origin and its own size, in
     * points - the window's, not the content's, because the far edge that has to be put back is the
     * window's. The far edge of a gesture is worked out from this rather than from the frame as the
     * platform has just left it, because the platform's own step has already moved the near edge.
     */
    private double settledFrameX;
    private double settledFrameY;
    private double settledFrameWidth;
    private double settledFrameHeight;

    /** Which edge the last drag pulled, and whether its far edge still has to be put back. */
    private boolean draggedLeft;
    private boolean draggedTop;
    private boolean dragJustEnded;

    /**
     * Fires a little after the last resize the platform reported, and puts the frame back to the
     * content then.
     *
     * <p>Waiting for the next flushed frame instead is not enough: {@link #fitWindowToViewport} only
     * runs when there is something to present, and a client that has stopped drawing - which is what
     * a window someone just finished resizing usually is - leaves the frame at the size the drag
     * asked for and the client refused, with a band of nothing along one edge until it happens to
     * repaint. Measured 2026-10-04: the band from one xterm drag was still there two and a half
     * seconds later. Restarting it on every report is what makes the deadline mean "the drag has
     * stopped", with the same margin {@link #dragInFlight} uses.
     */
    private final javafx.animation.PauseTransition dragSettle =
            new javafx.animation.PauseTransition(javafx.util.Duration.millis(1100));

    /**
     * The window's content area changed size. Most such changes are the backend's own, sizing the
     * window to the client's window; {@link #isUserResize} tells the user's apart from those, and
     * only the user's are reported.
     *
     * <p>What the client's window should become is the size of the area the client is drawn in, not
     * the size of the window: the window carries a title bar and borders, and a client resized to
     * include those would be a title bar too tall.
     */
    private void clientAreaChanged() {
        if (scene == null || input == null || stage == null) {
            return;
        }
        int width = contentWidth();
        int height = contentHeight();
        if (viewportPending) {
            // **The platform applies a window size in two steps, and the step in between is a mixture
            // of the old size and the new one.** Anything that is *only* such a mixture - each
            // dimension either what this server just set or what it was before - is the platform's own
            // half of the change, and reporting it as a drag is what made the window walk upwards two
            // pixels at a time. Anything else is the user, and clears the mark: the guard must not be
            // able to swallow a real drag.
            boolean matchesOldOrNew = (width == viewport.width || width == pendingFromWidth)
                    && (height == viewport.height || height == pendingFromHeight);
            if (matchesOldOrNew) {
                enforceWindowSize();
                return;
            }
            viewportPending = false;
        }
        if (System.currentTimeMillis() < openingUntil) {
            // **A window on its way open passes through sizes that are nobody's doing.** The scene
            // shrinks to its root's preferred size before the platform has applied the size this
            // backend asked for - with the content in a BorderPane that is the header bar's own
            // height, and then a frame the platform has not finished with - and every one of those
            // arrives here. They are the frame settling, not the user dragging it, so the frame is put
            // back to the size the server has and the client is told nothing. Measured 2026-10-04:
            // scene 0x374, then 0x66, then 784x66, and without this the client's window was shrunk to
            // 21 points tall before the window had finished opening.
            enforceWindowSize();
            return;
        }
        if (System.currentTimeMillis() < platformGestureUntil) {
            // **The platform is animating a zoom of the window, and every size it passes through
            // arrives here** - measured 2026-10-04: 785, 787, 798, 858, 875, ... on the way to 1511.
            // Telling the client each of them makes it chase an animation, while its own re-assertion
            // after each one leaves the window flapping between two sizes (1511 and 1499, twice a
            // second). So the size the frame *stops* at is what the client is told, once, when it has
            // held still; the frame itself is the platform's while the gesture lasts.
            //
            // Only a zoom arms this - see the full screen listener for why full screen is not held
            // back the same way. A window on its way open is the other case that needs no reporting,
            // and that is the guard above.
            platformSettledContent = new Dimension(width, height);
            platformSettle.playFromStart();
            platformGestureUntil = System.currentTimeMillis() + 400;
            userDragPending = true;
            userDragUntil = System.currentTimeMillis() + 1000;
            return;
        }
        if (!isUserResize(adjustingWindow, width, height, viewport)) {
            return;
        }
        // The platform is resizing the frame from the pointer; setViewport must not size it back until
        // the client's window has taken the size (or the deadline passes) - see setViewport.
        userDragPending = true;
        userDragUntil = System.currentTimeMillis() + 1000;
        dragSettle.playFromStart();
        input.windowResized(width, height);
    }

    /** The window was moved by the user, unless this backend was the one moving it. */
    private void windowMovedByUser() {
        if (placingWindow || adjustingWindow || stage == null || input == null
                || stage.isFullScreen()) {
            // A full screen window's move is the platform taking it into its Space and putting it at
            // the top left of the display: not an arrangement the user made, and remembering it would
            // reopen the window in the corner the next time the client is started.
            return;
        }
        if (hostButtonHeld) {
            // **The user is moving the window by its title bar, and that ends any resize gesture.**
            // The two are told apart by the question the platform already answers for us: a live resize
            // of an edge delivers no mouse events at all (measured 2026-10-04, and it is what A39's
            // whole design rests on), while a title-bar drag delivers the press and the moves - so a
            // button that is down here means a *move*, and the same change of the frame's x or y that a
            // left or top edge drag makes means something else.
            //
            // What has to go with it is everything the resize left pending, its anchor above all: that
            // anchor is a *far edge* to put back, worked out from where the frame stood when the resize
            // started, and a move that leaves it armed is pulled back to where the resize began -
            // reported 2026-10-04: "如果拉伸过窗口，这时候按住标题栏拉伸窗口，窗口经常弹回去到旧位置".
            userDragPending = false;
            dragJustEnded = false;
            platformGestureUntil = 0;
        }
        // A move is the one change of the frame's geometry that is neither a drag of an edge nor this
        // backend's own doing, and it moves both ends: what a later drag has to put back is where the
        // window is *now*.
        rememberSettledFrame();
        input.windowMoved((int) Math.round(stage.getX()), (int) Math.round(stage.getY()));
    }

    @Override
    public void setWindowPosition(int x, int y) {
        runOnFxThreadAndWait(() -> {
            if (stage == null) {
                return;
            }
            // Clamped here rather than where the position was remembered, because only the backend
            // knows what the display has room for now: a remembered position from a monitor that is
            // no longer plugged in would put the window somewhere the user cannot reach it.
            javafx.geometry.Rectangle2D usable =
                    javafx.stage.Screen.getPrimary().getVisualBounds();
            Point placed = cn.oyzh.fx.xserver.HostWindowStore.clamp(
                    new Point(x, y), (int) Math.round(stage.getWidth()),
                    (int) Math.round(stage.getHeight()),
                    new Rectangle((int) usable.getMinX(), (int) usable.getMinY(),
                            (int) usable.getWidth(), (int) usable.getHeight()));
            placingWindow = true;
            try {
                stage.setX(placed.x);
                stage.setY(placed.y);
            } finally {
                placingWindow = false;
            }
            rememberSettledFrame();
        });
    }

    /**
     * Whether the size the content area has taken is a resize the user made.
     *
     * <p>Most changes of that size are not: the backend sizes the window to the client's window
     * itself, and every one of those would otherwise be reported back as a resize, which the display
     * tick would answer by resizing the client to the size it already has - for ever. The two that
     * are not the backend's own are told apart by {@code adjusting} while it is working and by the
     * size being one the client has not already been told.
     */
    static boolean isUserResize(boolean adjusting, int width, int height, Rectangle viewport) {
        if (adjusting || width <= 0 || height <= 0) {
            return false;
        }
        return width != viewport.width || height != viewport.height;
    }

    /**
     * Puts the window back to the size of what it shows.
     *
     * <p>Called while a maximize or a restore is settling: the platform applies its own geometry for
     * those gestures a moment after this server has applied its own, and macOS reports that as a
     * content resize. Setting the size back is a no-op in every other case (the two sizes agree).
     * A window the platform has maximized is left alone - its frame is the platform's, and putting a
     * size on it is what took the maximize away (see {@link #platformOwnsFrame}). Runs on the FX
     * thread.
     */
    private void enforceWindowSize() {
        if (stage == null || stage.isFullScreen() || stage.isMaximized()) {
            return;
        }
        double wantedWidth = frameWidth(logical(viewport.width) * viewScale);
        double wantedHeight = frameHeight(logical(viewport.height) * viewScale);
        if (Math.abs(stage.getWidth() - wantedWidth) <= 1.0
                && Math.abs(stage.getHeight() - wantedHeight) <= 1.0) {
            return;
        }
        adjustingWindow = true;
        try {
            stage.setWidth(wantedWidth);
            stage.setHeight(wantedHeight);
        } finally {
            adjustingWindow = false;
        }
        rememberSettledFrame(stage.getX(), stage.getY(), wantedWidth, wantedHeight);
    }

    /**
     * Paints the whole window from the framebuffer. Runs on the FX thread.
     */
    private void drawViewport() {
        if (graphics == null || image == null) {
            return;
        }
        graphics.drawImage(image, viewport.x, viewport.y, viewport.width, viewport.height,
                0, 0, logical(viewport.width), logical(viewport.height));
    }

    /**
     * Moves the window so that it stays inside the usable part of the display after a resize, which
     * a window that grew past the bottom right corner needs.
     */
    private void keepOnScreen() {
        if (stage == null || stage.isFullScreen()) {
            return;
        }
        javafx.geometry.Rectangle2D usable = javafx.stage.Screen.getPrimary().getVisualBounds();
        double x = Math.min(stage.getX(), usable.getMaxX() - stage.getWidth());
        double y = Math.min(stage.getY(), usable.getMaxY() - stage.getHeight());
        stage.setX(Math.max(usable.getMinX(), x));
        stage.setY(Math.max(usable.getMinY(), y));
    }

    /**
     * Whether the platform, rather than this backend, is the one that owns the frame's size at this
     * moment.
     *
     * <p>Five situations say yes, and they are one rule: a drag of the frame's edge, where the
     * platform is computing the size from the pointer; a full screen window, whose frame belongs to
     * the platform's Space; a maximized window, whose frame is the display's work area; the moment
     * after the platform has *begun* a zoom or a restore and not finished applying it
     * ({@link #platformGestureUntil}); and the frame a zoom has already made but not yet reported
     * ({@link #zoomedByPlatform}). In all of them, sizing or moving the window takes the platform's
     * own arithmetic away from it - for a drag that is the drift {@link #dragInFlight} describes, for
     * full screen a window that leaves its Space without giving it back (the menu bar and the Dock
     * stay hidden until the session is locked and unlocked), and for a maximized window the size it
     * comes back to: measured 2026-10-13, a size set on a window the platform has maximized
     * *un-maximizes* it on Windows, and the size it was left at - the content plus the frame, which
     * is not the work area - becomes the one the platform restores to. The client's window is never
     * told anything, because the frame it is drawn in never changed size, and a maximized window the
     * user restored stays as large as the maximized one was. The canvas still follows the viewport;
     * the window does not.
     *
     * <p>**A zoom is reported in two pieces, and either of them can arrive first.** Measured
     * 2026-10-13 on the real machine, and both orders cost the same thing - a frame this backend
     * wrote while the platform was working.
     *
     * <ul><li>**A restore by the header bar's own control** - a JavaFX {@code EXTENDED} window's
     * double-click or restore button - flips {@code maximized} to false *before* the platform has put
     * the window back, and the frame that runs in between is the one {@link #fitWindowToViewport}
     * sizes: the viewport is still the maximized one, so the window was set to 1721x919 against the
     * work area's 1721x927, and that is the size it kept and the size the platform then restored to -
     * a stretched window left as large as the maximized one, its position the only thing that came
     * back. The same restore driven from outside the window (`ShowWindow`) is unaffected, because
     * there the window is already back at its size when the flag flips.
     * <li>**On the way in**, the platform moves and resizes the window *before* it reports the
     * maximize, and the frame written in that gap is what every later restore came back to: measure
     * it by the frame's own rectangle, because a restore lands on it exactly - a 499x421 frame at the
     * work area's corner, where the user's window was 498x421 somewhere else entirely, and 499 is
     * this backend's own arithmetic rather than the platform's. {@link #zoomedByPlatform} is what
     * says so in that gap, since nothing else does: the property still says "not maximized".
     * </ul>
     *
     * <p>Left alone for the length of the gesture, the platform's own restore wins and the client is
     * told the size it stopped at by {@link #platformSettle}.
     */
    private boolean platformOwnsFrame() {
        return platformOwnsFrame(stage.isFullScreen(), stage.isMaximized(), dragInFlight(),
                System.currentTimeMillis() < platformGestureUntil, zoomedByPlatform());
    }

    /**
     * Whether the frame this instant is the one a zoom makes of it: the work area's size, at the work
     * area's corner and a little outside it.
     *
     * <p>**It is the only thing that says a zoom is happening while it is happening.** The platform
     * moves and resizes the window first and reports the state afterwards - measured 146 ms apart -
     * and in that gap a window the property still calls "not maximized" and the display says fills
     * the screen is read as one the user made: its size is corrected to the viewport, which on
     * Windows *un-maximizes* it, and the rectangle it is left at becomes the window's own.
     *
     * <p>What a maximized frame looks like on Windows is the work area's size *plus the invisible
     * resize border*, placed at the work area's corner *minus that border* - so its origin is a few
     * pixels above and left of the usable area, which is where the platform will not let a window be
     * dragged to (the caption is what a drag holds, and it is clamped below the top of the work
     * area). On a platform whose zoomed frame is exactly the usable area - macOS - the origin test is
     * false and this is inert: that platform reports the zoom before its geometry, which the cases
     * above already cover.
     */
    private boolean zoomedByPlatform() {
        Stage open = stage;
        if (open == null || open.isFullScreen() || open.isMaximized()) {
            return false;
        }
        javafx.geometry.Rectangle2D usable = javafx.stage.Screen.getPrimary().getVisualBounds();
        return open.getX() < usable.getMinX() - 2 && open.getY() < usable.getMinY() - 2
                && open.getWidth() >= usable.getWidth() - 2;
    }

    /**
     * Whether the platform is the one that owns the frame's size, from the five things that say so.
     *
     * <p>Package-private and static so the rule is pinned without a window, next to the arithmetic
     * {@link #frameWidth} and {@link #windowHeight} are: the cases are one rule, and forgetting one of
     * them is what a maximized window that would not come back to its size looked like - three times
     * over, in the end.
     */
    static boolean platformOwnsFrame(boolean fullScreen, boolean maximized, boolean dragging,
                                     boolean platformGesture, boolean zoomedGeometry) {
        return fullScreen || maximized || dragging || platformGesture || zoomedGeometry;
    }

    /**
     * Whether the platform is still driving a drag of the frame's edge.
     *
     * <p>The deadline is refreshed by every resize the platform reports, so it means "no resize for a
     * second" - the only end-of-drag signal there is. macOS's live resize does not deliver the mouse
     * press or the release to the window's own event stream: measured 2026-10-04, a drag that took
     * the window from 784 to 744 points wide produced no mouse event in JavaFX at all.
     */
    private boolean dragInFlight() {
        if (!userDragPending) {
            return false;
        }
        if (System.currentTimeMillis() < userDragUntil) {
            return true;
        }
        userDragPending = false;
        // Which edge moved decides which edge has to be put back. A drag of the left or the top edge
        // is the one that moves the window's own origin, and so the one whose far edge the
        // platform's arithmetic can carry away with it; a drag of the right or the bottom edge
        // leaves the origin alone and needs no correction.
        //
        // Unless the platform was the one moving the frame: a zoom or full screen puts the window in
        // the corner of the display on purpose, and anchoring its far edge there would drag it back
        // out of it.
        boolean platformMoved = System.currentTimeMillis() < platformGestureUntil;
        draggedLeft = !platformMoved && Math.abs(stage.getX() - settledFrameX) > 1;
        draggedTop = !platformMoved && Math.abs(stage.getY() - settledFrameY) > 1;
        dragJustEnded = draggedLeft || draggedTop;
        return false;
    }

    /** Remembers the frame as it stands while no drag is in flight; see {@link #settledFrameX}. */
    private void rememberSettledFrame() {
        if (stage == null || scene == null || userDragPending) {
            return;
        }
        rememberSettledFrame(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
    }

    /** The same, for a geometry this backend has just asked for and the platform has not shown yet. */
    private void rememberSettledFrame(double x, double y, double frameWidth, double frameHeight) {
        settledFrameX = x;
        settledFrameY = y;
        settledFrameWidth = frameWidth;
        settledFrameHeight = frameHeight;
    }

    /**
     * Sizes the frame to the viewport, and puts back the edge the user was not dragging when a drag
     * has just ended.
     *
     * <p>A frame whose content is a client's window cannot always take the size the pointer asks
     * for: a client with resize increments snaps to a whole number of them (measured with xterm,
     * 2026-10-04: 744 asked for, 732 taken). Sizing the frame back to the content is what removes the
     * band of nothing the snap leaves, and it is also what slid windows to the right while their
     * left edge was pulled - the frame lost the snapped 12 points off its *right* end, which is the
     * edge the user was holding still. Putting the far edge back is what makes all four edges of the
     * frame behave the same way.
     */
    private void sizeFrameToContent() {
        // Before the early return: a window the platform has taken still needs its content drawn
        // small enough to fit it, and a maximized frame is the work area like any other.
        applyViewScale();
        if (stage.isFullScreen() || stage.isMaximized()) {
            // **The one window this backend never sizes is one the platform has taken.** A full screen
            // window's frame belongs to the platform's Space, and setting a size on it (or moving it)
            // leaves the Space standing with no window in it - the picture jumps back to the old
            // place, the front application becomes whoever was behind, and the Dock and menu bar do
            // not come back until the session is locked and unlocked. A maximized window's frame is
            // the display's work area, and a size set on it un-maximizes the window and leaves the
            // platform restoring to that size instead (see {@link #platformOwnsFrame}). The canvas
            // still follows the viewport, which is what {@link #setViewport} does for a drag in flight
            // as well.
            return;
        }
        double width = frameWidth(logical(viewport.width) * viewScale);
        double height = frameHeight(logical(viewport.height) * viewScale);
        double x = stage.getX();
        double y = stage.getY();
        if (dragJustEnded) {
            dragJustEnded = false;
            if (draggedLeft) {
                x = settledFrameX + settledFrameWidth - width;
            }
            if (draggedTop) {
                y = settledFrameY + settledFrameHeight - height;
            }
        }
        if (Math.abs(stage.getWidth() - width) < 1 && Math.abs(stage.getHeight() - height) < 1
                && Math.abs(stage.getX() - x) < 1 && Math.abs(stage.getY() - y) < 1) {
            return;
        }
        // The caller may already have claimed the flag for its own bookkeeping - the size listener
        // must not read this change as the user dragging the edge either way.
        boolean wasAdjusting = adjustingWindow;
        adjustingWindow = true;
        try {
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setX(x);
            stage.setY(y);
        } finally {
            adjustingWindow = wasAdjusting;
        }
        rememberSettledFrame(x, y, width, height);
    }

    @Override
    public void stop() {
        closed = true;
        if (!stopped.compareAndSet(false, true) || !toolkitStarted) {
            return;
        }
        try {
            runOnFxThreadAndWait(() -> {
                setWindowFocused(false);
                handBackPointerThenClose();
            });
        } catch (RuntimeException e) {
            log.debug("JavaFX had already stopped", e);
        }
        // The toolkit is deliberately left running: another client's window may still be open, and
        // Platform.exit() is one-way - it would take every window with it and refuse to start again.
    }

    @Override
    public void releaseToolkit() {
        if (!toolkitStarted) {
            return;
        }
        try {
            // The FX thread is not a daemon, so without this the JVM would stay alive after the
            // server has finished.
            runOnFxThreadAndWait(Platform::exit);
        } catch (RuntimeException e) {
            log.debug("JavaFX had already stopped", e);
        }
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        // **Run later, not now, and it is not a detail: `stage.setFullScreen` blocks the toolkit thread
        // for the whole of the Space animation.** Measured 2026-10-04: the call was still inside when
        // the window had been full screen for 658 ms. The caller is the display tick, and a tick that
        // waits that long is a tick that does not resize the client - the size the window went to was
        // reported 19 ms after the full screen state changed and the client only heard about it 700 ms
        // later ("全屏反应速度有点慢，正常窗口200ms就完成了，这个xserver要超过1秒"). Nothing waits for
        // this call: the server has already decided, and the toolkit runs these in the order they were
        // submitted, so the window is still full screen before anything else this backend is asked to
        // do with it.
        Platform.runLater(() -> {
            if (stage == null || stage.isFullScreen() == fullScreen) {
                return;
            }
            // On macOS this is the platform's own full screen - another Space, with the animation -
            // rather than a window stretched to cover the menu bar: it is what the system does for
            // an application that asks to fill the display, and the user can leave it the usual way.
            stage.setFullScreen(fullScreen);
            log.debug("window is {}full screen", fullScreen ? "now " : "no longer ");
        });
    }

    /** The menu bar, for a forwarded application's notification-area icon (C9). */
    private final AwtTrayIcon tray = new AwtTrayIcon();

    @Override
    public boolean hasTray() {
        return tray.available();
    }

    @Override
    public void setTrayIcon(java.awt.image.BufferedImage image, String tooltip) {
        tray.set(image, tooltip);
    }

    @Override
    public void setTrayClickListener(TrayClick listener) {
        tray.setListener(listener);
    }

    @Override
    public void raise() {
        runOnFxThreadAndWait(() -> {
            if (stage != null) {
                // toFront and not requestFocus: a client asking for attention has not asked to take
                // the keyboard away from whatever the user is typing into.
                stage.toFront();
            }
        });
    }

    @Override
    public void raiseAndFocus() {
        runOnFxThreadAndWait(() -> {
            if (stage != null) {
                // **A window that has just appeared is one the user is waiting for**, so it takes the
                // front and the keyboard - unlike {@link #raise()}, which is the urgency hint and does
                // not. The platform still has the last word: an application in the background cannot
                // always take the keyboard, and macOS leaves that to the user's own settings.
                stage.toFront();
                stage.requestFocus();
            }
        });
    }

    @Override
    public void screenResized(Screen screen) {
        Framebuffer framebuffer = screen.framebuffer();
        int width = screen.width();
        int height = screen.height();
        // Read the array here, on the tick thread, and hand it to the FX thread: the framebuffer
        // cannot be resized again while the tick is between the two, and the FX thread must not read
        // it while a client is drawing into it.
        IntBuffer pixels = IntBuffer.wrap(framebuffer.pixels());
        runOnFxThreadAndWait(() -> showNewScreenSize(width, height, pixels));
    }

    /**
     * Runs on the FX thread. The canvas is deliberately not rebuilt: it draws the part of the screen
     * the viewport shows, so its own size is the viewport's and does not change with the screen's -
     * only the image it draws from is a different size now.
     */
    private void showNewScreenSize(int width, int height, IntBuffer pixels) {
        if (closed) {
            return;
        }
        screenWidth = width;
        screenHeight = height;
        pixelBuffer = new PixelBuffer<>(width, height, pixels, PixelFormat.getIntArgbPreInstance());
        image = new WritableImage(pixelBuffer);
        drawViewport();
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    // ------------------------------------------------------------------
    // Window
    // ------------------------------------------------------------------

    /**
     * The window's title bar's height, in points, fixed. Thirty is the usual height of a platform
     * title bar the buttons in it are drawn for, so the window's own header buttons sit in it without
     * crowding the title.
     */
    private static final double HEADER_HEIGHT = 30;

    /**
     * Whether this window has the toolkit's own title bar ({@link StageStyle#EXTENDED}, JavaFX 27)
     * rather than the platform's.
     *
     * <p>One decision in one place, because everything about the window's geometry depends on it: the
     * stage's style, whether there is a header bar at all, and therefore what counts as content. A
     * platform that does not support it downgrades the style to {@code DECORATED} rather than
     * failing, and this is the same question asked before the window is built - so the answer is
     * remembered for the life of the window rather than asked twice.
     */
    private boolean extendedWindow() {
        return Platform.isSupported(ConditionalFeature.EXTENDED_WINDOW);
    }

    /**
     * The client-area header bar: the window's title, its icon, and the strip that dragging moves the
     * window by - all of it the application's own, since the platform's title bar is not there (see
     * {@code StageStyle.EXTENDED}).
     *
     * <p>Fixed at {@link #HEADER_HEIGHT}: the platform's height for its own title bar is a function of
     * the window's style and the display, and the point of drawing this one here is that the page says
     * how tall it is. The title is centred on the window and **bold**, which is what a title bar's own
     * title looks like; there is no icon, which is a matter of taste rather than of the platform.
     *
     * <p>Runs on the FX thread.
     */
    private HeaderBar buildHeaderBar() {
        HeaderBar bar = new HeaderBar();
        bar.setMinHeight(HEADER_HEIGHT);
        bar.setPrefHeight(HEADER_HEIGHT);
        bar.setMaxHeight(HEADER_HEIGHT);
        //bar.setMinWidth(Short.MAX_VALUE);
        //bar.setMaxWidth(Short.MAX_VALUE);
        //bar.setPrefWidth(Short.MAX_VALUE);

        titleLabel = new Label(this.title);
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        // Bold by stylesheet rather than by a font: the platform's own title is the system UI font in
        // bold, and naming a family here would freeze the size and the family with it. The weight is
        // the one thing the platform's title bar adds on top of the default text.
        titleLabel.setStyle("-fx-font-weight: bold;");
        // A label is content, and content is not draggable by default: without this a drag that
        // starts on the window's name does nothing, and the name is exactly where a hand goes.
        HeaderBar.setDragType(titleLabel, HeaderDragType.DRAGGABLE_SUBTREE);
        bar.setCenter(titleLabel);
        return bar;
    }

    /**
     * Runs on the FX thread.
     */
    private void openWindow(int width, int height, IntBuffer pixels) {
        screenWidth = width;
        screenHeight = height;
        // **A window being opened passes through sizes that are nobody's doing.** The scene shrinks to
        // its root's preferred size before the platform has applied the size this backend asks for -
        // with the content in a BorderPane that is the header bar's own height and then a frame the
        // platform has not finished with - and every one of those arrives on the scene's size
        // listeners. They are the frame settling, not the user dragging it, so the settle starts
        // before the scene is even built; see the same window in {@link #clientAreaChanged}.
        openingUntil = System.currentTimeMillis() + 700;
        if (viewport.isEmpty()) {
            viewport = new Rectangle(0, 0, width, height);
        }
        // Keyboard events carry the position of the pointer, which starts wherever the pointer is
        // (the platform does not tell us); the middle of what the window shows is the least
        // misleading guess.
        pointerX = viewport.x + viewport.width / 2;
        pointerY = viewport.y + viewport.height / 2;

        // Premultiplied is not a preference: a PixelBuffer only accepts INT_ARGB_PRE or
        // BYTE_BGRA_PRE and rejects plain INT_ARGB outright. It makes no difference to the screen
        // itself, whose pixels are opaque, and the framebuffer array is shared rather than
        // converted either way.
        pixelBuffer = new PixelBuffer<>(width, height, pixels, PixelFormat.getIntArgbPreInstance());
        image = new WritableImage(pixelBuffer);
        canvas = new Canvas(logical(viewport.width), logical(viewport.height));
        graphics = canvas.getGraphicsContext2D();
        drawViewport();

        // A Pane rather than a StackPane, and the canvas left unmanaged: a StackPane centres what it
        // holds, and the canvas is only ever the same size as the scene's content until the user
        // drags the window - at which point centring it would shift every coordinate this backend
        // reports by half the difference. Unmanaged, too, so the canvas cannot set a minimum size on
        // the window: a canvas is not resizable, and one that is 1200 wide would stop the user from
        // ever shrinking the window below that.
        //
        // The canvas sits in the *centre* of a BorderPane whose top is the header bar, so the
        // content starts below the title bar by the layout itself and nothing here has to place it.
        headerBar = extendedWindow() ? buildHeaderBar() : null;
        content = new Pane(canvas);
        canvas.setManaged(false);
        BorderPane root = new BorderPane();
        if (headerBar != null) {
            root.setTop(headerBar);
        }
        root.setCenter(content);
        // Before the window is built rather than after: the scene's own size is what the platform
        // sizes the window to, so a screen that has to be drawn smaller has to be known by now.
        applyViewScale();
        Scene scene = new Scene(root, logical(viewport.width) * viewScale,
                frameHeight(logical(viewport.height) * viewScale), Color.BLACK);
        this.scene = scene;
        // The platform would otherwise draw its own arrow over the window, on top of the sprite the
        // server renders from the client's cursor: two pointers, and the host one always wins.
        // Leaving the pointer to the X client is also what makes a text cursor, a resize arrow or
        // an application that hides the pointer while it types show up as the client intended.
        // Every change of pointer or focus re-decides it, because hiding the platform pointer is
        // only ever a loan - see hostCursor for what that costs.
        scene.setOnMouseEntered(event -> {
            enterWindow();
        });
        scene.setOnMouseExited(event -> {
            pointerOverWindow = false;
            applyHostCursor();
        });
        scene.setOnMouseMoved(event -> {
            enterWindow();
            pointerMoved(event);
            mouseButton(event, true);
        });
        scene.setOnMouseDragged(this::pointerMoved);
        scene.setOnMousePressed(event -> {
            hostEvent("pressed", event);
            // Recorded before the content guard below: a press on the window's chrome is not the
            // client's business and is dropped from the X side, but the window is still being moved
            // with it, and that is the one thing this flag is for - see {@link #windowMovedByUser}.
            hostButtonHeld = true;
            enterWindow();
            // **A press on the client's page takes the scene's focus back to the content.** Pressing
            // one of the header bar's buttons makes that button the scene's focus owner, and the
            // platform only routes composed text to the focus owner - so without this a click on 最小化
            // would silently turn the input method off for everything typed afterwards.
            if (overContent(event)) {
                content.setFocusTraversable(true);
                content.requestFocus();
            }
            mouseButton(event, true);
        });
        scene.setOnMouseReleased(event -> {
            hostButtonHeld = false;
            hostEvent("released", event);
            mouseButton(event, false);
        });
        scene.setOnScroll(this::scrolled);
        // A drag from the host's desktop onto this window. Accepting it on drag-over is what makes
        // the platform report the drop at all, and the files it carries are the whole of what the X
        // side needs - see InputSink.hostDrop.
        scene.setOnDragOver(event -> {
            if (event.getDragboard().hasFiles()) {
                event.acceptTransferModes(javafx.scene.input.TransferMode.COPY);
            }
            // Logged because the two halves of a host drag fail in ways that look identical from
            // outside: the platform never reporting the drag at all, and the platform reporting it
            // and the X side not delivering it. This is the first of the two.
            log.debug("a host drag is over the window with {} file(s); accepted={}",
                    event.getDragboard().getFiles().size(), event.getDragboard().hasFiles());
            event.consume();
        });
        scene.setOnDragDropped(event -> {
            boolean taken = false;
            if (event.getDragboard().hasFiles()) {
                java.util.List<String> paths = new java.util.ArrayList<>();
                for (java.io.File file : event.getDragboard().getFiles()) {
                    paths.add(file.getAbsolutePath());
                }
                log.debug("the host dropped {} file(s) at {},{}", paths.size(),
                        screenX(event.getSceneX()), screenY(event.getSceneY()));
                input.hostDrop(paths, screenX(event.getSceneX()), screenY(event.getSceneY()));
                taken = true;
            }
            event.setDropCompleted(taken);
            event.consume();
        });
        scene.setOnKeyPressed(event -> keyChanged(event, true));
        scene.setOnKeyReleased(event -> keyChanged(event, false));
        // The character the input method or the dead key produced, which is not the key that was
        // pressed: JavaFX reports it here, after the platform's own composition has run.
        scene.setOnKeyTyped(event -> {
            log.debug("key typed '{}' (code {})", event.getCharacter(), event.getCode());
            input.keyTyped(event.getCharacter().isEmpty() ? 0 : event.getCharacter().charAt(0));
        });
        // **An input method's result arrives at the scene's focus owner, and the platform is only
        // asked to compose anything while that owner carries an input method of its own.** JavaFX's
        // {@code Scene.InputMethodStateManager} calls {@code Scene.enableInputMethodEvents} exactly
        // when some scene's {@code getFocusOwner()} is non-null, its {@code getInputMethodRequests()}
        // is non-null *and* its {@code getOnInputMethodTextChanged()} is non-null - so a window with
        // no focus owner, or one whose focus owner has no handler of its own, never becomes an input
        // method client at all, and the platform never composes: the keystrokes arrive as the letters
        // printed on the keys. That is the report of 2026-10-08 ("中文输入实际上都是英文字符，好像也没
        // 看到 input method committed 这种消息"), reproduced with the Mac's own "Pinyin - Simplified"
        // source and real keycodes: a forwarded text field received `nihao` where the input method
        // meant 你好. The content is the focus owner and carries the handler; the scene must **not**
        // also carry one, because the event is fired *at the focus owner* and then bubbles to the
        // scene, so two handlers would type every committed character twice.
        //
        // **Not traversable yet.** A scene's first focusable node is made the focus owner the moment
        // the scene is shown - before the window is up and key - and the platform only enables input
        // method events when that owner *changes*, so an owner taken that early is an input method
        // that never turns on (measured 2026-10-08: the same build came up composing on one run and
        // not the next). The focus is taken in the stage's own focus listener instead, where the
        // window is up; see there.
        content.setFocusTraversable(false);
        content.setInputMethodRequests(new ContentInputMethodRequests());
        content.setOnInputMethodTextChanged(this::inputMethodText);
        // The node the focus is parked on for the moment it takes to hand it back - see the focus
        // listener below. Unmanaged, so it takes no part in the layout and cannot show anything.
        focusParking = new Pane();
        focusParking.setManaged(false);
        focusParking.setFocusTraversable(true);
        root.getChildren().add(focusParking);
        log.debug("input method owner: requests={} handler={} traversable={}",
                content.getInputMethodRequests() != null,
                content.getOnInputMethodTextChanged() != null, content.isFocusTraversable());

        // **The title bar is JavaFX's own from 27 on** (StageStyle.EXTENDED): the window's header
        // buttons stay the platform's, drawn over the top of the scene, and the strip they are in
        // belongs to this application - which is the point of it, because a forwarded client's
        // window is a page whose title bar is worth drawing in the same toolkit that draws the page.
        // A platform without extended windows keeps its own decorated window and no header bar of
        // ours, which is the only way not to end up with two title bars.
        stage = new Stage(extendedWindow() ? StageStyle.EXTENDED : StageStyle.DECORATED);
        stage.setTitle(title);
        stage.setScene(scene);
        // **The key that leaves full screen is the platform's own, not Escape.** JavaFX's default is
        // Escape, and it is consumed by JavaFX before the scene sees it: a forwarded application in a
        // full screen window would never be given its own Escape key - which a terminal, an editor
        // and an IDE all need - and pressing it would throw the window out of full screen instead.
        // Which key to use instead is the platform's own convention, not this server's (see
        // {@link #fullScreenExitKey}).
        fullScreenKey = fullScreenExitKey(System.getProperty("os.name"));
        stage.setFullScreenExitKeyCombination(fullScreenKey);
        // The window is the frame around the client's window, so pulling its edge is the gesture a
        // window manager's frame has always had: the client's window is resized to match. A scene
        // listener rather than a stage one, because the content area is the client's size and the
        // stage's includes the decoration the platform adds.
        stage.setResizable(true);
        dragSettle.setOnFinished(event -> {
            if (stage != null && !platformOwnsFrame()) {
                // The drag has stopped: the frame goes back to the size of what it shows, with the
                // edge the user did not pull left where it was - see {@link #sizeFrameToContent}.
                sizeFrameToContent();
            }
        });
        platformSettle.setOnFinished(event -> {
            Dimension settled = platformSettledContent;
            platformSettledContent = null;
            if (stage == null || settled == null || closed) {
                return;
            }
            // The platform's gesture has stopped: this is the size the frame is, and the client's
            // window should take it. The frame stays the platform's - a zoom or a full screen window
            // whose size is set by us is the fight A41 and A42 are about - so the anchor a drag would
            // leave behind is kept out of the way for a while longer than the drag deadline.
            platformGestureUntil = System.currentTimeMillis() + 1500;
            rememberSettledFrame();
            log.debug("platform gesture settled at {}x{}", settled.width, settled.height);
            input.windowResized(settled.width, settled.height);
        });
        scene.widthProperty().addListener((observable, was, is) -> clientAreaChanged());
        scene.heightProperty().addListener((observable, was, is) -> clientAreaChanged());
        stage.maximizedProperty().addListener((observable, was, is) -> {
            log.debug("host window maximized {} -> {} ({}x{})", was, is,
                    contentWidth(), contentHeight());
            // **The zoom is the platform's, geometry and all, from JavaFX 27 on.** It used to be
            // replaced by a fill of this backend's own - the window set to the display's usable area,
            // the client told to match - because macOS worked its zoomed frame out from the window's
            // *content*, which for a forwarded window is a client's X window at whatever size the
            // client last asserted (measured 2026-10-03: 1280x828 -> 1280x886 -> 1512x886, restoring
            // nothing). That is no longer what it does: measured 2026-10-04, a 784x344 window zooms to
            // 1504x853, which is the display and not the client. And the fill has a cost of its own -
            // setting a window's size while macOS has it zoomed *un-zooms* it, so the fill was undone
            // within the second, the state flipped back, and the window oscillated: the second double
            // click restored nothing and the window was left at neither size. So the zoom is left
            // alone. Its resize arrives on the scene like any other, {@link #clientAreaChanged}
            // reports it, and the client's window takes the size the window now has; the restore is
            // the platform putting its own frame back, which reaches the client the same way - exactly
            // as full screen does (A41).
            dragJustEnded = false;
            // The zoom's own resize reports are wanted - the client takes the size the window has -
            // but they are not a drag of the frame's edge, and the anchor below must not treat them
            // as one: a zoom puts the window in the corner of the display on purpose.
            platformGestureUntil = System.currentTimeMillis() + 2000;
        });
        stage.fullScreenProperty().addListener((observable, was, is) -> {
            log.debug("host window fullScreen {} -> {}", was, is);
            fullScreenChangedAt = System.currentTimeMillis();
            // Whatever edge a drag pulled before the window went full screen, and wherever the window
            // stood, has nothing to do with where it comes back to: the Space puts it at the top left
            // of the display, and the platform's own frame is what it returns to.
            dragJustEnded = false;
            // **And full screen is *not* held back for its final size, because it does not pass
            // through any.** A zoom is animated - a dozen sizes on the way - and the settle below is
            // there so the client is told only the size it stops at; full screen arrives as the one
            // size the platform went to, so holding it would be delay for nothing. Measured 2026-10-04:
            // with the same settle applied to it, the client waited for the Space animation to finish
            // before it was told anything, which is the "全屏反应速度有点慢，正常窗口 200ms 就完成了，
            // 这个 xserver 要超过 1 秒" of the report. The anchor is still kept away from a full screen
            // move - the property change above clears it, and the frame is the platform's while it is
            // full screen anyway.
            //if (headerBar != null) {
            //    // **The header bar is chrome, and full screen is where chrome goes.** The platform
            //    // hides its own title bar there and the picture is meant to have the whole display -
            //    // thirty points of title bar would be thirty points of screen the client cannot use.
            //    // Unmanaged rather than only invisible: an invisible managed node still takes its
            //    // place in the layout, so the content would keep the gap.
            //    headerBar.setManaged(!is);
            //    headerBar.setVisible(!is);
            //}
            if (!is) {
                // A limit that arrived while the window was full screen has been waiting for a
                // moment the window could take it.
                applyMaxClientArea();
            }
        });
        // Where the user drags the frame to is the one thing about the host window that no client
        // knows: it is remembered per client so the next session opens where this one was left.
        stage.xProperty().addListener((observable, was, is) -> windowMovedByUser());
        stage.yProperty().addListener((observable, was, is) -> windowMovedByUser());
        // **A window that moves or changes size under a pointer that does not move is the one case the
        // enter and leave events miss**, and full screen is made of them: the window grows over the
        // pointer on the way in and shrinks away from it on the way out, with no mouse event in
        // between. The pointer's side of the decision is taken again from where it really is, so the
        // arrow is handed back on the way out as it is on any other leave.
        stage.xProperty().addListener((observable, was, is) -> applyHostCursor());
        stage.yProperty().addListener((observable, was, is) -> applyHostCursor());
        stage.widthProperty().addListener((observable, was, is) -> applyHostCursor());
        stage.heightProperty().addListener((observable, was, is) -> applyHostCursor());
        // **The window's size is the content's size plus the header bar, set here rather than asked
        // for.** {@code sizeToScene()} used to stand in for this and cannot any more: what it measures
        // is the root's *preferred* size, and the canvas - which is the whole of what the window shows
        // - is unmanaged so that it cannot put a floor under the window, so a BorderPane root whose
        // centre it is has a preferred height of just the header bar. The window opened as a strip
        // thirty points tall, the platform's own resize of it arrived as "the user dragged the frame",
        // and the client's window was shrunk to it - measured before this line existed.
        sizeFrameToContent();
        // Scene has no focus property of its own, so focus comes from the window.
        stage.focusedProperty().addListener((observable, wasFocused, focused) -> {
            Node ownerBefore = scene.getFocusOwner();
            setWindowFocused(focused);
            input.focusChanged(focused);
            applyHostCursor();
            // The sprite starts or stops being drawn with the focus, and no client has to draw
            // anything for the canvas to be wrong: repaint its own box.
            repaintCursorArea();
            // **The input method's owner takes the focus when the window does, not when it is shown.**
            // JavaFX enables input method events only when the scene's focus owner *changes*
            // (`Scene.InputMethodStateManager` re-decides on every focus change), and asking in
            // {@link #showWindow} - the moment the window appears - does not always stick: measured
            // 2026-10-08, the same build could come up with the content as the focus owner and no
            // input method, and a Mac's Chinese input method then composed nothing at all. Taking the
            // focus here, while the window is up and key, is the moment the platform has a view to
            // route the composition to.
            if (focused && content != null) {
                content.setFocusTraversable(true);
                if (scene.getFocusOwner() == content && focusParking != null) {
                    // The content already had the focus, and a focus that does not *change* is one
                    // JavaFX does not re-decide on - so it is given up and taken again, which is the
                    // change the platform waits for. Without this the composition works when the
                    // window happens to be focused at the right moment and not otherwise.
                    focusParking.requestFocus();
                }
                content.requestFocus();
            }
            log.debug("stage focused {} -> {}, input method owner is {} (was {})", wasFocused, focused,
                    scene.getFocusOwner(), ownerBefore);
        });
        stage.setOnCloseRequest(event -> {
            // **The close is finished here, once the toolkit has had a pulse to apply the released
            // pointer**, rather than by the platform the moment this handler returns - see
            // handBackPointerThenClose, which is the whole of why: releasing the pointer is a queued
            // task that needs the window to still be there, and the platform's own close takes it away
            // in the same turn. The event is consumed so that the close really is ours to finish; the
            // window is closed a pulse later, which is not something a user can see.
            event.consume();
            closed = true;
            setWindowFocused(false);
            handBackPointerThenClose();
        });
        // **And the window is not shown yet.** Whether it should be is the server's answer, given by
        // {@link #setIdle}, which the server calls with the *session's* answer before this method even
        // returns (see XServer.openSessionBackend) - it says idle for a session with nothing to show,
        // which is what the master screen of single-window mode is until a client connects.
        //
        // **Not `if (!idle) showWindow();`**, which is what this said first and what the user caught:
        // this backend's own `idle` is false until it is told, and it was told on a *later* tick, so the
        // window was shown here and hidden a moment later - the very flash being fixed ("我看了下还是会
        // 极短地闪一下窗口"). The first setIdle decides, and it always comes.
        windowSize = new Dimension(width, height);
    }

    /**
     * What the platform pointer should be: hidden over the window, so the sprite the server draws
     * from the client's cursor is the only pointer there, and the platform's own arrow everywhere
     * else - outside the window, and whenever the window is not the active one.
     *
     * <p>Hiding it means {@link Cursor#NONE}, which on macOS is not an empty cursor but a
     * process-wide {@code [NSCursor hide]} ({@code MacCursor.setVisible_impl}): left in place it
     * takes the pointer off the desktop and off every other application until the window's cursor
     * changes again. Handing the arrow back is therefore as much a part of the policy as hiding it,
     * which is why leaving the window and losing the focus both re-decide it.
     *
     * <p>An image cursor would avoid the global hide and does not work here: JavaFX hands the image
     * to the platform without complaint - {@code ImageCursor.getCurrentFrame()} returns an image
     * frame, not the default fallback - and macOS still draws its own arrow over the window. That is
     * true of a fully opaque image as much as a transparent one, so no choice of pixels fixes it:
     * an opaque red 32x32 cursor was measured drawing the standard arrow.
     */
    static Cursor hostCursor(boolean pointerOverWindow, boolean applicationActive) {
        return pointerOverWindow && applicationActive ? Cursor.NONE : Cursor.DEFAULT;
    }

    /**
     * How many of this process's windows are focused - with none, the application is in the
     * background, and the platform will not let it keep the pointer hidden.
     */
    private static final AtomicInteger FOCUSED_WINDOWS = new AtomicInteger();

    /**
     * Whether the pointer can be hidden at all: it belongs to the **active** application.
     *
     * <p>Measured on macOS, 2026-10-01, and it is the whole reason {@link #hostCursor} takes a second
     * argument. A native probe that called {@code [NSCursor hide]} and then watched
     * {@code CGCursorIsVisible} saw it go 0 -> 1 the moment another application was activated, and it
     * never went back: the platform re-shows the cursor for whoever is in front, and a background
     * application's hide is gone (JavaFX's own record of it is stale, so it does not even try again).
     * For this server that means the sprite cannot be the only pointer while the application is in the
     * background - so it stays **off the canvas** then, and the user keeps exactly one pointer: the
     * platform's, which is the one that belongs to whatever they are actually using.
     *
     * <p>Which is what two reported bugs were: moving the mouse over an unfocused window showed the
     * sprite *and* the system cursor, and dragging another application's window over this one brought
     * the system cursor back while the sprite stayed.
     */
    static boolean applicationActive() {
        return FOCUSED_WINDOWS.get() > 0;
    }

    /** Records this window's focus, for {@link #applicationActive}. Runs on the FX thread. */
    private void setWindowFocused(boolean focused) {
        if (focused == countedFocused) {
            return;
        }
        countedFocused = focused;
        if (focused) {
            FOCUSED_WINDOWS.incrementAndGet();
        } else {
            FOCUSED_WINDOWS.decrementAndGet();
        }
    }

    /**
     * Which window currently holds the platform pointer hidden. There is one platform pointer for
     * the whole process, so "this window wants it hidden" is a claim on shared state: with two
     * windows open, the second one losing focus would otherwise hand the pointer back while it is
     * still over the first.
     */
    private static final AtomicReference<JavaFxBackend> CURSOR_OWNER = new AtomicReference<>();

    /**
     * Applies {@link #hostCursor} to the window. Runs on the FX thread.
     */
    private void applyHostCursor() {
        if (scene == null || stage == null) {
            return;
        }
        updatePointerOverWindow();
        boolean wantHidden = !idle && hostCursor(pointerOverWindow, applicationActive()) == Cursor.NONE;
        if (wantHidden != cursorHidden) {
            // One line per change of the decision, which is the whole of what a pointer that is not
            // where it should be leaves behind: whether it was hidden or handed back, and the three
            // things that decide it.
            log.debug("host pointer {}: over the client's page={}, application active={}, idle={}",
                    wantHidden ? "hidden" : "handed back", pointerOverWindow, applicationActive(),
                    idle);
            cursorHidden = wantHidden;
            // And the sprite goes with the decision: it is the client's pointer, so a move that takes
            // the pointer off the page has to take it off the canvas too, and the frame that is
            // redrawn without it is what erases it (the damage box covers where it was).
            repaintCursorArea();
        }
        if (wantHidden) {
            CURSOR_OWNER.set(this);
        }
        if (CURSOR_OWNER.get() == this) {
            scene.setCursor(wantHidden ? Cursor.NONE : Cursor.DEFAULT);
            if (!wantHidden) {
                CURSOR_OWNER.compareAndSet(this, null);
            }
        } else {
            // Another window owns the pointer: this one leaves it alone rather than handing it back.
            scene.setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Hands the platform pointer back, whatever state the window is in. Runs on the FX thread.
     */
    private void releaseHostCursor() {
        if (scene != null) {
            scene.setCursor(Cursor.DEFAULT);
        }
    }

    /**
     * Hands the platform pointer back and closes the window, a pulse apart - the release first, and
     * the window only once it has landed.
     *
     * <p>**The release is not a native call in this turn.** It goes through the scene's cursor, and
     * the toolkit applies that at a *pulse*: {@code Scene.doLayoutPass} ->
     * {@code MouseHandler.updateCursorFrame} -> {@code ViewScene.setCursor} ->
     * {@code Application.invokeLater} - and the task that last step posts reads the scene's
     * {@code platformView} and does **nothing at all** once the window has been disposed. A window
     * closed in the same turn as the release therefore loses the race, and the process-wide
     * {@code [NSCursor hide]} stays in place with nothing left to lift it: measured 2026-10-13 with
     * the toolkit's own record of it ({@code MacCursor.isNSCursorVisible} false before the close and
     * still false a minute after it), which is the report "点击关闭窗口…鼠标也跟随消失了" - the frame
     * went, the desktop had no pointer, and only activating another application brought it back.
     *
     * <p>Closing on the **second** pulse after the release is enough by the queue's own order: the
     * scene's pass runs inside a pulse and the task it queues is drained before the next pulse
     * ({@code Platform.runLater} and {@code Application.invokeLater} are the same queue), so by then
     * the window the pointer is over has taken the arrow back. A pulse later than that costs nothing
     * either way - the window is on its way out and the pointer is already the platform's.
     *
     * <p>Runs on the FX thread, and takes the window with it: {@link #stage} is cleared here, so a
     * `stop()` that arrives behind this one finds no window to close a second time.
     */
    private void handBackPointerThenClose() {
        Stage closing = stage;
        stage = null;
        // Whatever happens next, the pointer is the platform's from here on: nothing in this backend
        // can decide to hide it again, because every decision needs the stage that has just gone.
        releaseHostCursor();
        if (closing == null) {
            return;
        }
        closePending = true;
        // Counted as gone the moment it is decided, not when the platform finishes taking it away:
        // the count is only how far along the next window is placed, and a toolkit that is exiting
        // (releaseToolkit, on the way out of the process) never reaches the timer below.
        LIVE_WINDOWS.decrementAndGet();
        new AnimationTimer() {
            private int pulses;

            @Override
            public void handle(long now) {
                if (++pulses < 2) {
                    return;
                }
                stop();
                closePending = false;
                closing.close();
            }
        }.start();
    }

    /**
     * Whether the platform pointer is over what this window *shows*, taken from where the pointer
     * really is.
     *
     * <p>Entering and leaving arrive as mouse events, and those were all this went on - but a window
     * whose geometry changes under a pointer that does not move can leave the answer stale, and this
     * answer is what decides whether the platform pointer is handed back at all. Measured 2026-10-04:
     * leaving full screen shrank the window away from a stationary pointer, no {@code MOUSE_EXITED}
     * arrived, and the platform pointer stayed hidden - the cursor then vanished over the desktop and
     * came back only once it was moved into the window again, and stayed right on the next round trip
     * (which is exactly the report: "full screen is fine, after leaving full screen the mouse
     * disappears unless it is over the xserver window"). Asking the platform where the pointer is
     * makes the answer independent of an event arriving; the events still give it the moment they
     * happen, and {@link #applyHostCursor} re-decides whenever the window's geometry changes -
     * which is the entry and the exit of full screen, and every drag of an edge.
     *
     * <p>The content, not the window: the header bar is part of the window and the client's cursor is
     * not drawn there, so a pointer over the title bar is one the platform's own arrow owns - which is
     * also the arrow the window's buttons are pressed with.
     */
    private void updatePointerOverWindow() {
        if (stage == null || !stage.isShowing()) {
            pointerOverWindow = false;
            return;
        }
        Point pointer;
        try {
            PointerInfo info = MouseInfo.getPointerInfo();
            if (info == null) {
                return;                         // no pointer to ask about: keep the events' answer
            }
            pointer = info.getLocation();
        } catch (RuntimeException | Error refused) {
            // A platform that will not answer (headless, or a security manager): the events are all
            // there is, which is what this did before.
            return;
        }
        // The scene's own frame around the window is not part of the content either, and the content
        // itself is placed inside the scene by the layout (below the header bar).
        double sceneX = stage.getX() + (stage.getWidth() - scene.getWidth()) / 2;
        double sceneY = stage.getY() + (stage.getHeight() - scene.getHeight()) / 2;
        javafx.geometry.Bounds content = canvas.localToScene(canvas.getBoundsInLocal());
        pointerOverWindow = pointerInside(sceneX + content.getMinX(), sceneY + content.getMinY(),
                content.getWidth(), content.getHeight(), pointer.x, pointer.y);
    }

    /**
     * Whether a point on the display is inside a window's frame. The edges belong to the window the
     * way a window server counts them: the left and top edge are inside, the right and bottom edge
     * are the first pixel of what is outside.
     *
     * <p>Package-private and static because it is the half of the pointer decision a test can pin -
     * the other half is where the platform says the pointer is.
     */
    static boolean pointerInside(double windowX, double windowY, double windowWidth,
                                 double windowHeight, int pointX, int pointY) {
        return pointX >= windowX && pointX < windowX + windowWidth
                && pointY >= windowY && pointY < windowY + windowHeight;
    }

    @Override
    public void setIdle(boolean idle) {
        runOnFxThreadAndWait(() -> {
            this.idle = idle;
            if (idle) {
                // **Nothing to show means no window.** The window is the frame around a client's
                // output, so with none of it there is nothing to frame and no reason to keep a window
                // on the user's desktop: it is hidden here, the moment the session goes idle, and shown
                // again by the first thing a client draws - exactly as a per-client window goes when
                // its client disconnects. Hidden rather than closed: the backend is the session's, and
                // the server is the one that decides when a window stops existing (it closes the
                // backend of a session that has had nothing to show for a while).
                hideWindow();
            } else {
                showWindow();
            }
            applyHostCursor();
        });
    }

    /**
     * Shows the window, sizes it to what it shows and places it, once. Runs on the FX thread.
     *
     * <p>The size and the placement are asked for here rather than when the window was built because
     * until it is shown the platform has no frame to measure: its decoration is part of what the window
     * is, and the window is placed inside the usable area of the display, which is only meaningful for
     * a window that is on it.
     */
    private void showWindow() {
        if (stage == null || shown) {
            return;
        }
        shown = true;
        stage.show();
        rememberSceneBorder();
        // The frame the platform settled on is what the border was measured against, so the size is
        // put back once more: the first sizing asked for content plus a border that had not been
        // measured yet (nothing at all on macOS, but a border is a border).
        sizeFrameToContent();
        // The window is on its way open: the sizes it passes through before it has settled are not the
        // user either - see the settle in {@link #clientAreaChanged}.
        openingUntil = System.currentTimeMillis() + 700;
        startPulseProbe();
        placeOnScreen(windowSize.width, windowSize.height);
        rememberSettledFrame();
        log.debug("window shown ({}x{})", windowSize.width, windowSize.height);
    }

    /** Takes the window off the screen without closing it. Runs on the FX thread. */
    private void hideWindow() {
        if (stage == null || !shown) {
            return;
        }
        shown = false;
        stage.hide();
        releaseHostCursor();
        log.debug("window hidden: nothing to show in it");
    }

    /**
     * The cursor installed on the window, for a test that has no pointer to check it with.
     */
    public Cursor windowCursor() {
        return scene == null ? null : scene.getCursor();
    }

    /**
     * Whether a window is still this backend's, for a test of the one thing about the close that can
     * be seen from in here: that it does not happen in the same turn the pointer is handed back - see
     * {@link #handBackPointerThenClose}. Runs on the FX thread.
     */
    boolean windowStillOpen() {
        return stage != null || closePending;
    }

    /**
     * Reports how fast the toolkit is actually presenting and how long a pulse takes, at debug
     * level. Together with the flush counter this says whether a window that feels slow is missing
     * frames or spending too long on each one - and both are cheaper to read than to guess.
     *
     * <p><b>Only while debug logging is on.</b> An AnimationTimer that never stops is not free: a
     * running one asks the toolkit for a pulse every frame for as long as the window lives, which
     * measured 120 pulses a second on a scene with nothing to redraw - a laptop on battery paying
     * for a diagnostic nobody is reading. The same rule keeps the counter honest for whoever is
     * reading it: what it reports is the rate the window was being presented at anyway.
     */
    private void startPulseProbe() {
        if (!log.isDebugEnabled()) {
            return;
        }
        long[] last = {System.nanoTime()};
        int[] count = {0};
        long[] total = {0};
        new javafx.animation.AnimationTimer() {
            @Override
            public void handle(long now) {
                total[0] += now - last[0];
                last[0] = now;
                if (log.isDebugEnabled() && ++count[0] % 120 == 0) {
                    log.debug("pulse {} frames, {} ms/frame, {} fps", count[0],
                            total[0] / 1_000_000.0 / count[0], 1e9 / (total[0] / (double) count[0]));
                }
            }
        }.start();
    }

    /**
     * Notes that the pointer is over the window, and re-decides the platform's arrow if that is new.
     *
     * <p>Not left to the enter event alone: a window that opens under a pointer which never moves never
     * gets one, and the arrow would sit on top of the client's cursor until it does. Whether the
     * pointer is then over the *page* rather than over the header bar is not this method's to answer -
     * {@link #applyHostCursor} asks the platform where the pointer is, and the flag set here is only
     * the event's opinion of it.
     */
    private void enterWindow() {
        if (!pointerOverWindow) {
            pointerOverWindow = true;
            applyHostCursor();
        }
    }

    /**
     * Moves and resizes the window so all of it lands inside the usable part of the display.
     */
    private void placeOnScreen(int width, int height) {
        // Fully qualified: this class is in the same package as the server's own Screen.
        Rectangle2D usable = javafx.stage.Screen.getPrimary().getVisualBounds();
        // The window is taller than what it shows: the header bar is part of it, and a window placed
        // as if it were only the content hangs that much further down the display.
        double w = Math.min(frameWidth(logical(width)), usable.getWidth());
        double h = Math.min(frameHeight(logical(height)), usable.getHeight());
        double offset = (LIVE_WINDOWS.getAndIncrement() % 8) * 32.0;
        stage.setX(usable.getMinX() + Math.max(0, (usable.getWidth() - w) / 2) + offset);
        stage.setY(usable.getMinY() + Math.max(0, (usable.getHeight() - h) / 2) + offset);
    }

    /**
     * Frames flushed and the time spent blitting them, reported at debug level. A window that feels
     * slow is either not being given frames or taking too long over each one, and this is the line
     * that says which.
     */
    private static final java.util.concurrent.atomic.AtomicLong FRAMES =
            new java.util.concurrent.atomic.AtomicLong();
    private static final java.util.concurrent.atomic.AtomicLong BLIT_NANOS =
            new java.util.concurrent.atomic.AtomicLong();

    /**
     * Runs on the FX thread.
     */
    /**
     * The rectangle a frame has to repaint: the damage, the sprite's own box, and the clip the canvas
     * imposes on both.
     *
     * <p>Package-private and static so it can be pinned without a window: the reason the sprite's box
     * is in here is the A21 flicker (see {@link #flushDamage}), and a rule about rectangles is worth a
     * test that does not need a display.
     *
     * <p><b>The clip is not decoration.</b> A sprite's box is the sprite's own rectangle, and the
     * pointer can sit on the very last pixel of the screen, so that box - and the union of it with
     * damage that has already been clipped - can reach past what the canvas covers. The
     * {@code drawImage} that follows then asks Prism to update a destination region outside the
     * texture and JavaFX throws
     * {@code IllegalArgumentException: Destination region (x=1268, y=489, w=48, h=25) must fit within
     * texture content bounds (contentWidth=1300, contentHeight=800)}. That is not a lost frame: the
     * paint that throws is usually the **first** one, so the window never opens at all - measured as
     * "sometimes, when the pointer is at the right edge". 48 wide is exactly the union of the sprite's
     * box with the 24 wide position it came from, which is how this was recognised.
     */
    static Rectangle damageWithSprite(Rectangle damage, CursorSprite cursor, Rectangle bounds) {
        Rectangle wanted = cursor == null
                ? damage
                : damage.union(new Rectangle(cursor.x(), cursor.y(), cursor.width(),
                        cursor.height()));
        return wanted.intersection(bounds);
    }

    /**
     * The part of the sprite the canvas covers - the sprite alone, because the pointer can hang over
     * the edge of the screen or of this window's viewport, and drawing the whole of it asks Prism for
     * a destination region outside the canvas, which is the same exception {@link #damageWithSprite}
     * exists to avoid. An empty rectangle means the pointer is not on this window at all.
     */
    static Rectangle visibleSprite(CursorSprite sprite, Rectangle viewport) {
        if (sprite == null) {
            return new Rectangle();
        }
        return new Rectangle(sprite.x(), sprite.y(), sprite.width(), sprite.height())
                .intersection(viewport);
    }

    private void flushDamage() {
        fitWindowToViewport();
        Rectangle region;
        CursorSprite cursor;
        synchronized (damageLock) {
            region = pendingDamage;
            cursor = pendingCursor;
            pendingDamage = null;
            flushScheduled = false;
        }
        if (region == null || pixelBuffer == null) {
            return;
        }
        // What the canvas covers, and so the clip for everything drawn into it: the part of the
        // screen this window shows.
        Rectangle bounds = new Rectangle(0, 0, screenWidth, screenHeight).intersection(viewport);
        region = region.intersection(bounds);
        if (region.isEmpty()) {
            return;
        }
        // **The sprite's own box is always part of what gets repainted from the framebuffer.**
        //
        // <p>The sprite is drawn over the frame and never into the framebuffer, so the canvas keeps
        // the copy drawn last frame wherever this frame's damage does not reach it - and
        // {@link #drawCursor} then composites that one again, which darkens the antialiased edges of
        // the arrow: measured with a client that repaints (EasyShell's SSH tab does it thirty times a
        // second), the sprite's own pixels alternate between 17 and 102 below a tenth of full
        // brightness, and the pointer visibly flickers. A prompt that repaints the *whole* window
        // never shows it, which is why this went unnoticed: with a whole-window damage the sprite's
        // box is covered by accident, and `main` damaged the whole screen every frame so it could not
        // happen at all. With this the sprite in the canvas is exactly one frame old, always,
        // whatever the client underneath is repainting.
        Rectangle damage = damageWithSprite(region, cursor, bounds);
        // The buffer position must be zero when JavaFX uploads it. Anything that reads through the
        // buffer - a diagnostic, a future feature - can leave it at the end, and the upload then
        // fails with "only 0 elements remain", leaving the window frozen on an old frame.
        pixelBuffer.getBuffer().rewind();
        // updateBuffer repaints exactly the rectangle the callback reports, and the callback has
        // nothing to do because the pixels are already in place.
        pixelBuffer.updateBuffer(pb -> {
            pb.getBuffer().rewind();
            return new Rectangle2D(damage.x, damage.y, damage.width, damage.height);
        });
        if (System.getProperty("xserver.dumpFrame") != null) {
            scheduleDump();
        }
        long tempStart = System.nanoTime();
        graphics.drawImage(image, damage.x, damage.y, damage.width, damage.height,
                logical(damage.x - viewport.x), logical(damage.y - viewport.y),
                logical(damage.width), logical(damage.height));
        drawCursor(cursor);
        BLIT_NANOS.addAndGet(System.nanoTime() - tempStart);
        long frames = FRAMES.incrementAndGet();
        if (log.isDebugEnabled() && frames % 120 == 0) {
            log.debug("{} frames presented, {} ms per frame blitted", frames,
                    BLIT_NANOS.get() / 1_000_000.0 / frames);
        }
    }

    /**
     * Draws the pointer sprite over the part of the frame that was just blitted.
     *
     * <p>The canvas keeps what was drawn into it, so the sprite is painted last and the damage
     * region - which the server grows to cover the sprite's previous position as well - has already
     * been repainted from the framebuffer underneath it. That is what makes a moving sprite leave
     * nothing behind without touching the framebuffer itself.
     *
     * <p>Runs on the FX thread.
     */
    private void drawCursor(CursorSprite sprite) {
        if (sprite == null || graphics == null) {
            return;
        }
        if (!applicationActive()) {
            // This process is in the background, so the platform is drawing the pointer (and will not
            // let it be hidden - see applicationActive). Drawing the sprite too is exactly the
            // "two cursors" the user reported.
            return;
        }
        if (!pointerOverWindow) {
            // The pointer is over the window's chrome - the header bar - and not over the client's
            // page. The sprite is the *client's* pointer, so it has no place there: drawn at the point
            // the pointer left the content, it is a second pointer beside the platform's arrow, on the
            // strip where the window's own buttons are.
            return;
        }
        if (sprite.pixels() != cursorPixels) {
            // The cursor was replaced by a client: build the image once for the new one. Its pixels
            // are cached per cursor, so a moving pointer keeps the same array.
            cursorPixels = sprite.pixels();
            cursorImage = new WritableImage(sprite.width(), sprite.height());
            cursorImage.getPixelWriter().setPixels(0, 0, sprite.width(), sprite.height(),
                    PixelFormat.getIntArgbInstance(), sprite.pixels(), 0, sprite.width());
        }
        Rectangle visible = visibleSprite(sprite, viewport);
        if (visible.isEmpty()) {
            // The pointer is outside what this window shows: another window's business, and drawing
            // it here would paint it twice.
            return;
        }
        // Only the visible part, with the source offset that goes with it: a pointer at the edge of
        // the screen has a sprite box that reaches past the canvas, and asking Prism to draw that
        // throws instead of clipping.
        graphics.drawImage(cursorImage, visible.x - sprite.x(), visible.y - sprite.y(),
                visible.width, visible.height,
                logical(visible.x - viewport.x), logical(visible.y - viewport.y),
                logical(visible.width), logical(visible.height));
    }

    /**
     * Schedules a repaint of the sprite's own box, because the focus changed and with it whether the
     * sprite is drawn at all.
     *
     * <p>The server damages what a <em>client</em> draws, and nothing a client does changes when the
     * host window is focused - so without this the canvas keeps the sprite after the application goes
     * to the background, and keeps not having it after it comes back. Runs on the FX thread.
     */
    private void repaintCursorArea() {
        CursorSprite sprite = lastCursorSprite;
        if (sprite == null) {
            return;
        }
        synchronized (damageLock) {
            Rectangle box = new Rectangle(sprite.x(), sprite.y(), sprite.width(), sprite.height());
            pendingDamage = pendingDamage == null ? box : pendingDamage.union(box);
            // The flush needs the sprite itself: it clears `pendingCursor`, and a focus change is not
            // a frame the server sent one with.
            pendingCursor = sprite;
            if (flushScheduled) {
                return;
            }
            flushScheduled = true;
        }
        flushDamage();
    }

    /**
     * Diagnostic aid: with {@code -Dxserver.dumpFrame=<prefix>} the window's image and the
     * framebuffer behind it are written to PNG files once a second.
     *
     * <p>This exists because a window that shows nothing is otherwise hard to tell apart from a
     * server that drew nothing: comparing the two images says which side is wrong. It is read only,
     * and rewinds the shared buffer afterwards so the upload that follows is unaffected.
     */
    private void scheduleDump() {
        if (!dumpScheduled.compareAndSet(false, true)) {
            return;
        }
        String prefix = System.getProperty("xserver.dumpFrame");
        Thread thread = new Thread(() -> {
            for (int i = 0; i < DUMP_FRAMES; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    return;
                }
                int frame = i;
                Platform.runLater(() -> {
                    dumpImage(prefix + "-image-" + frame + ".png");
                    dumpFramebuffer(prefix + "-buffer-" + frame + ".png");
                    dumpScene(prefix + "-scene-" + frame + ".png");
                });
            }
        }, "xserver-frame-dump");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * How many frames the dump captures, one a second. A client that takes a while to paint its
     * first frame can miss a thirty second window, so {@code -Dxserver.dumpFrames} lengthens it.
     */
    private static final int DUMP_FRAMES = Integer.getInteger("xserver.dumpFrames", 30);

    private final AtomicBoolean dumpScheduled = new AtomicBoolean();

    /**
     * Runs on the JavaFX thread.
     */
    private void dumpImage(String path) {
        if (image == null) {
            return;
        }
        int w = screenWidth;
        int h = screenHeight;
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        javafx.scene.image.PixelReader reader = image.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        write(out, path);
    }

    /**
     * Runs on the JavaFX thread; renders the scene the window is showing into an image.
     *
     * <p>The image dump above reads the {@code WritableImage} the framebuffer is wrapped in, which
     * is what this backend hands to the scene - and so it says nothing about what the window makes
     * of it. A scene that failed to repaint its canvas, or a canvas whose pixels never reached it,
     * leaves both of the other dumps correct while the window on screen shows something else
     * entirely, which is the one failure a window on a display nobody can look at hides.
     */
    private void dumpScene(String path) {
        Scene current = stage == null ? null : stage.getScene();
        if (current == null) {
            return;
        }
        javafx.scene.image.WritableImage shot = current.snapshot(null);
        int w = (int) shot.getWidth();
        int h = (int) shot.getHeight();
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(w, h,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        javafx.scene.image.PixelReader reader = shot.getPixelReader();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                out.setRGB(x, y, reader.getArgb(x, y));
            }
        }
        write(out, path);
    }

    /**
     * Keeps the window the size of what it shows. Runs on the FX thread, once a frame.
     *
     * <p>{@link #setViewport} sets the size when the content changes, but the platform applies a
     * window size asynchronously and a size the user is in the middle of dragging with wins. A drag
     * that the client refuses is the case that matters: the content does not grow with it, so the
     * viewport stops changing and nothing else would ever bring the frame back to what it frames -
     * the window would sit there larger than its content, with a band of nothing around it. This is
     * also what a window manager's frame does, and it is why the comparison has to be made every
     * frame rather than only when the viewport moves.
     */
    private void fitWindowToViewport() {
        Stage open = stage;
        if (open == null || adjustingWindow || viewport.isEmpty()) {
            return;
        }
        if (platformOwnsFrame()) {
            // **The platform owns the frame's geometry - a drag in flight, or a full screen window.**
            // This runs on every flushed frame, and the viewport is the client's window - which during
            // a drag is the size the client was *last* able to take, one round trip and one snap behind
            // what the pointer is asking for. Sizing the frame to it here puts the platform's live
            // resize back to a smaller frame on every frame, and the platform then works its next step
            // out from that: measured 2026-10-04, a 40 pt pull of the left edge moved the window's
            // origin by 40 pt but shrank it by 156 (784 -> 628) in steps of one character cell (13),
            // because each frame the frame was pulled down to the client's snapped size and the drag
            // was driving off it again. {@link #setViewport} refuses to size the window for the same
            // reason; this is the other half of the same rule.
            return;
        }
        sizeFrameToContent();
    }

    /**
     * Runs on the JavaFX thread; reads the framebuffer array, not the shared IntBuffer.
     */
    private void dumpFramebuffer(String path) {
        int[] source = framebuffer == null ? null : framebuffer.pixels();
        if (source == null) {
            return;
        }
        java.awt.image.BufferedImage out = new java.awt.image.BufferedImage(screenWidth,
                screenHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        out.setRGB(0, 0, screenWidth, screenHeight, source, 0, screenWidth);
        write(out, path);
    }

    private static void write(java.awt.image.BufferedImage image, String path) {
        try {
            javax.imageio.ImageIO.write(image, "png", new java.io.File(path));
        } catch (Exception e) {
            log.warn("frame dump to {} failed", path, e);
        }
    }

    // ------------------------------------------------------------------
    // Input
    // ------------------------------------------------------------------

    /**
     * Reports a mouse event the host window itself received, before anything is done with it.
     *
     * <p>This exists because "the click did nothing" has two very different causes that look the same
     * from the client: the host window never passed the event on (the platform took it, or the
     * toolkit did not deliver it), or it passed on an event the client then ignored. The client side
     * is logged too, by {@code InputState} and the request log; this is the other end of the wire.
     */
    private void hostEvent(String what, MouseEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("host window {} {} at scene ({},{}), stage focused {}", what, event.getButton(),
                    event.getX(), event.getY(), stage != null && stage.isFocused());
        }
    }

    /**
     * Whether a point of the scene is over the window's content rather than its chrome.
     *
     * <p>The header bar is <em>inside</em> this window, so the toolkit delivers events that happen on
     * it to the scene - and the client must not see those. A press or a drag there is the window being
     * moved or maximized, which is precisely what the client is not being asked about: forwarding it
     * would deliver a click at the top row of the screen for every attempt to move the window. A
     * button that is already held is the exception - a drag that wanders out of the content keeps
     * going until it is released, which is what a client with a selection riding on it expects.
     */
    private boolean overContent(MouseEvent event) {
        return overContent(event.getSceneX(), event.getSceneY());
    }

    /** The same, for events that are not mouse events: a scroll reports its own coordinates. */
    private boolean overContent(double sceneX, double sceneY) {
        Point2D local = canvas.sceneToLocal(sceneX, sceneY);
        return local.getX() >= 0 && local.getY() >= 0
                && local.getX() < canvas.getWidth() && local.getY() < canvas.getHeight();
    }

    private void pointerMoved(MouseEvent event) {
        // **A move is where the pointer can cross between the client's page and the window's chrome,
        // and that crossing is neither an enter nor a leave of the window.** The platform's arrow is
        // hidden while the pointer is over the content and must come back over the header bar, whose
        // own buttons are pressed with it - reported 2026-10-04: "整个标题栏区域，鼠标是隐藏的…
        // 导致我想按一下最小化或者全屏按钮都很困难". Re-decided from where the pointer is, which is
        // what {@link #updatePointerOverWindow} asks about; cheap, and a no-op when nothing changed.
        applyHostCursor();
        // Remembered before the chrome guard: where the platform's input method candidate window
        // belongs is where the user is looking, whether that is over the page or over the title bar.
        pointerSceneX = event.getSceneX();
        pointerSceneY = event.getSceneY();
        if (heldButtons == 0 && !overContent(event)) {
            // Over the header bar, which is the window's chrome rather than the client's page: the
            // pointer is not on the screen the client is being shown, and moving the window by its
            // title must not be reported as the pointer wandering along the top row of the screen.
            return;
        }
        int x = screenX(event.getX());
        int y = screenY(event.getY());
        pointerX = x;
        pointerY = y;
        input.pointerMoved(x, y);
    }

    private void mouseButton(MouseEvent event, boolean pressed) {
        pointerSceneX = event.getSceneX();
        pointerSceneY = event.getSceneY();
        int x = screenX(event.getX());
        int y = screenY(event.getY());
        pointerX = x;
        pointerY = y;
        int button = xButton(event.getButton());
        if (pressed) {
            if (button == 0 || !overContent(event)) {
                return;
            }
            heldButtons |= 1 << button;
            input.buttonChanged(button, true, x, y);
            return;
        }
        if (button != 0) {
            if ((heldButtons & (1 << button)) == 0) {
                // A release for a press this window never handed on: the gesture began on the header
                // bar, or on another window. There is nothing to let go of.
                return;
            }
            heldButtons &= ~(1 << button);
            input.buttonChanged(button, false, x, y);
            return;
        }
        // A release reports no button when the gesture started outside the window. Let go of
        // everything rather than leave a button stuck down in the server.
        for (int held = 1; held <= 3; held++) {
            if ((heldButtons & (1 << held)) != 0) {
                heldButtons &= ~(1 << held);
                input.buttonChanged(held, false, x, y);
            }
        }
    }

    private void scrolled(ScrollEvent event) {
        if (!overContent(event.getSceneX(), event.getSceneY())) {
            return;
        }
        int x = screenX(event.getX());
        int y = screenY(event.getY());
        pointerX = x;
        pointerY = y;
        // X has no notion of a partial wheel: every notch is a press and release of 4/5, or of 6/7
        // for horizontal scrolling.
        if (event.getDeltaY() != 0) {
            wheel(event.getDeltaY() > 0 ? 4 : 5, x, y);
        } else if (event.getDeltaX() != 0) {
            wheel(event.getDeltaX() > 0 ? 7 : 6, x, y);
        }
    }

    private void wheel(int button, int x, int y) {
        input.buttonChanged(button, true, x, y);
        input.buttonChanged(button, false, x, y);
    }

    private void keyChanged(javafx.scene.input.KeyEvent event, boolean pressed) {
        if (carriesACharacterNoKeyHas(event)) {
            // **The platform is telling us the character, not the key.** An input method that hands
            // its result over as a key event - which is what macOS does for composed text, and what
            // tools/host-input.c reproduces - arrives as the keycode of whatever key sits at nought
            // (`a` on a Mac keyboard) with the real text in the event. Sending that keycode types an
            // `a` and nothing else: measured 2026-10-08, `host-input type "中文输入abc"` put `aaaaaaa`
            // into a forwarded text field. The character itself comes through KEY_TYPED, which is
            // already wired to {@link InputSink#keyTyped}, so the key event is dropped instead.
            return;
        }
        if (pressed && fullScreenKey != null && fullScreenKey.match(event)) {
            // **The platform's full screen key goes both ways, and this is the half JavaFX does not
            // have.** `setFullScreenExitKeyCombination` is read as the way *out* of full screen only:
            // the key it names leaves the window, and one pressed on a window that is not full screen
            // does nothing at all - measured 2026-10-13 on the real machine, where F11 was expected
            // to fill the display and produced no state change anywhere. So the same key is handled
            // here for the other direction, and the exit is still JavaFX's own.
            //
            // **JavaFX's own handling runs first and does not consume the event**, which is the one
            // thing this branch has to allow for: measured the same day, one press of F11 on a full
            // screen window left full screen and then entered it again - the state had already been
            // put back to false when this ran, so `!isFullScreen` was true. The two are told apart by
            // time rather than by the event, because the event carries no mark: a press that finds
            // the state *just* changed is the same press that changed it, and is not answered again.
            //
            // **The key belongs to the window's chrome while this window has the keyboard** - it is
            // the same key the platform's own full screen convention names - so it is taken rather
            // than forwarded, exactly as it already is while the window is full screen.
            if (System.currentTimeMillis() - fullScreenChangedAt > FULL_SCREEN_PRESS_MILLIS) {
                setFullScreen(!stage.isFullScreen());
            }
            return;
        }
        keyChanged(event.getCode(), pressed);
    }

    /** A key this side decided to press, with no event behind it: the input method's own BackSpaces. */
    private void keyChanged(KeyCode code, boolean pressed) {
        int keycode = xKeycode(code);
        if (keycode == 0) {
            // No mapping means no keycode: reporting one would invent a key the client never has.
            return;
        }
        input.keyChanged(keycode, pressed, pointerX, pointerY);
    }

    /**
     * Whether the event's own text is a character no key on this keyboard can produce - which is how
     * an input method's result is told apart from a key press.
     *
     * <p>What a key press carries otherwise is text too (Shift+1 is {@code !}), so the test cannot be
     * "the text differs from the key": it is whether the *keyboard* could have typed it at all, which
     * is the same question {@link Keymap#keycodeForKeysym} answers for every character.
     */
    private static boolean carriesACharacterNoKeyHas(javafx.scene.input.KeyEvent event) {
        String text = event.getText();
        if (text == null || text.isEmpty()) {
            return false;
        }
        for (int i = 0; i < text.length(); i++) {
            int keysym = Keymap.keysymFor(text.charAt(i));
            if (keysym != 0 && Keymap.keycodeForKeysym(keysym) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * The text an input method composed, which is the half of typing no keycode can carry: a keycode
     * says which key was pressed and a client turns that into a character with its own keymap, which
     * works for everything printed on a keyboard and for nothing else. The composition itself is the
     * platform's own window and never reaches the X client; what goes is the committed text, and
     * {@link InputSink#keyTyped} is what turns each character into a keystroke a client can read.
     */
    private void inputMethodText(InputMethodEvent event) {
        String committed = event.getCommitted() == null ? "" : event.getCommitted();
        StringBuilder composed = new StringBuilder();
        for (javafx.scene.input.InputMethodTextRun run : event.getComposed()) {
            composed.append(run.getText());
        }
        log.debug("input method: composed='{}' committed='{}'", composed, committed);
        // **What the client is being shown as the pre-edit comes off first.** The composition is a
        // conversation between the platform and the person; the application on the other side of this
        // server never sees it - the composition window is the platform's own - so the only way the
        // field can show what is being typed is for the server to type it, and the only way that text
        // can change is to take it back and put the new text down. That is what a local input method
        // does to a field, one BackSpace at a time, and it is what makes `a a` appear while the
        // person types and 啊啊 replace it when they press space.
        for (int i = 0; i < preEditLength; i++) {
            pressBackspace();
        }
        preEditLength = 0;
        if (!committed.isEmpty()) {
            log.debug("input method committed {} characters", committed.length());
            for (int i = 0; i < committed.length(); i++) {
                input.inputMethodTyped(committed.charAt(i));
            }
            return;
        }
        for (int i = 0; i < composed.length(); i++) {
            input.inputMethodTyped(composed.charAt(i));
        }
        preEditLength = composed.length();
    }

    /** Takes one character of the pre-edit back out of the client's field. */
    private void pressBackspace() {
        keyChanged(KeyCode.BACK_SPACE, true);
        keyChanged(KeyCode.BACK_SPACE, false);
    }

    /**
     * The input method's half of a window whose client is a whole desktop: where the platform should
     * put its candidate list.
     *
     * <p>There is no caret on this side - the text goes to an X client that draws its own, and this
     * window only ever shows the pixels - so the pointer is the honest answer. The coordinates are
     * what JavaFX asks for, which are the screen's own: the same sum {@code TextInputControlSkin} does
     * for a text field (the window's position, the scene's inside it, then the point in the scene).
     */
    private final class ContentInputMethodRequests implements InputMethodRequests {

        @Override
        public Point2D getTextLocation(int offset) {
            double x = pointerSceneX;
            double y = pointerSceneY;
            if (stage != null && scene != null) {
                x += stage.getX() + scene.getX();
                y += stage.getY() + scene.getY();
            }
            return new Point2D(x, y);
        }

        @Override
        public int getLocationOffset(int x, int y) {
            // Asked where in the *composed* text the platform's own point falls; this side never sees
            // the composition, so there is no offset it could answer with.
            return 0;
        }

        @Override
        public void cancelLatestCommittedText() {
            // The text has already gone to the client; there is nothing here to take back.
        }

        @Override
        public String getSelectedText() {
            // Nothing is ever selected on this side.
            return null;
        }
    }

    /**
     * Turns a coordinate in the window into one on the screen.
     *
     * <p>The window shows a sub-rectangle of the screen - the part the client's windows occupy - so
     * the top left of the scene is the top left of that rectangle, not of the screen. Without the
     * offset every pointer position is reported as far off as that corner is from the origin, which
     * is nothing when the client's window reaches the origin and twenty pixels or more when it does
     * not: a menu then stops recognising the pointer near its edges and closes as soon as the
     * pointer is moved, because the position the client is told about has left the menu even though
     * the pointer has not.
     */
    static int screenCoordinate(double sceneCoordinate, int viewportOrigin, int scale,
                                int screenSize) {
        return clamp(viewportOrigin + sceneCoordinate * scale, screenSize);
    }

    /** A framebuffer measurement in the logical units the window is laid out in. */
    private double logical(double framebufferPixels) {
        return framebufferPixels / scale;
    }

    /**
     * Recomputes {@link #viewScale} and puts it on the canvas. Runs on the FX thread.
     *
     * <p>Cheap and idempotent - the transform is only replaced when the factor actually changed - so
     * the places that know the window's geometry may have moved (the frame being sized, the viewport
     * being set, the window being shown) can all call it, and the frames where nothing did pay
     * nothing.
     */
    private void applyViewScale() {
        double wanted = fitScale(logical(viewport.width), logical(viewport.height));
        if (Math.abs(wanted - viewScale) < 0.0005) {
            return;
        }
        viewScale = wanted;
        if (canvas != null) {
            canvas.getTransforms().setAll(
                    new javafx.scene.transform.Scale(viewScale, viewScale, 0, 0));
        }
        if (viewScale < 1) {
            log.info("this display cannot show the whole screen: it is drawn at {}% of its size, "
                            + "because a window bigger than the display is clipped by it and the "
                            + "rest of it could not be reached", Math.round(viewScale * 100));
        } else {
            log.info("the whole screen fits the display again: it is drawn at its own size");
        }
    }

    /**
     * The largest factor the content may be drawn at so that the whole window - the content, the
     * frame the platform puts around it and this backend's own header bar - stays inside the
     * display's usable area. 1 when it already does, and 1 for a full screen window, whose frame is
     * the display's own and is as large as the display can make it.
     */
    private double fitScale(double contentWidth, double contentHeight) {
        if (stage != null && stage.isFullScreen()) {
            return 1;
        }
        javafx.geometry.Rectangle2D usable = javafx.stage.Screen.getPrimary().getVisualBounds();
        // The chrome is not scaled with the content: the header bar is inside the scene but has a
        // height of its own in points, and the frame is outside the scene altogether.
        return fitScale(contentWidth, contentHeight, usable.getWidth() - sceneBorderWidth,
                usable.getHeight() - sceneBorderHeight - headerHeight());
    }

    /**
     * The same for a content of a given size and an area of a given size, which is the half a test
     * can pin without a display. Zero and negative numbers answer 1: a window with no content, or a
     * display with no room, is not a window to draw smaller.
     */
    static double fitScale(double contentWidth, double contentHeight, double availableWidth,
                           double availableHeight) {
        if (contentWidth <= 0 || contentHeight <= 0 || availableWidth <= 0 || availableHeight <= 0) {
            return 1;
        }
        return Math.min(1, Math.min(availableWidth / contentWidth, availableHeight / contentHeight));
    }

    /**
     * The size of the window's content - what the screen is drawn into - in framebuffer pixels, read
     * from the window as it stands.
     *
     * <p>The scene <em>is</em> the window's client area: what the platform puts around it - the frame
     * {@link #rememberSceneBorder} measures, and adds back in {@link #frameWidth}/{@link #frameHeight}
     * - lies <em>outside</em> the scene, so it must not be taken off again here. Only the header bar
     * is inside the scene and does come off the height (see {@link #headerHeight()}).
     *
     * <p>Subtracting the frame a second time is what this did until 2026-10-13, and it was invisible
     * on macOS because an extended window there has no frame at all - {@code sceneBorderWidth} and
     * {@code sceneBorderHeight} are zero. On Windows an extended window keeps an invisible resize
     * border, so the content came out 15x7 small: the client's window was resized to the short number
     * on every maximize and restore (it shrank, and never came back to the size it started at), and a
     * band of the scene was left showing along the right and the bottom of the picture.
     */
    private int contentWidth() {
        return contentExtent(scene.getWidth() / viewScale, scale);
    }

    private int contentHeight() {
        return contentExtent((scene.getHeight() - headerHeight()) / viewScale, scale);
    }

    /**
     * A length the window is laid out in, in the framebuffer pixels a client is told about.
     *
     * <p>Package-private and static so the one piece of arithmetic {@link #contentWidth} and
     * {@link #contentHeight} are is pinned without a window, next to {@link #windowHeight}.
     */
    static int contentExtent(double logical, int scale) {
        return (int) Math.round(Math.max(0, logical) * scale);
    }

    private int screenX(double sceneX) {
        return screenCoordinate((sceneX - contentOrigin().getX()) / viewScale, viewport.x, scale,
                screenWidth);
    }

    private int screenY(double sceneY) {
        return screenCoordinate((sceneY - contentOrigin().getY()) / viewScale, viewport.y, scale,
                screenHeight);
    }

    /**
     * Where the content starts in the scene: below the header bar, which is inside the window rather
     * than around it, so scene coordinates and content coordinates are not the same thing any more.
     * Asked of the canvas instead of assumed to be {@link #HEADER_HEIGHT} so that full screen, where
     * the header bar is gone, needs no special case.
     */
    private Point2D contentOrigin() {
        return canvas == null ? Point2D.ZERO : canvas.localToScene(0, 0);
    }

    /**
     * Clamps a scene coordinate to the screen; a drag can leave it by a pixel or two.
     */
    private static int clamp(double value, int limit) {
        return Math.max(0, Math.min(limit - 1, (int) value));
    }

    private static int xButton(MouseButton button) {
        return switch (button) {
            case PRIMARY -> 1;
            case MIDDLE -> 2;
            case SECONDARY -> 3;
            default -> 0;
        };
    }

    // ------------------------------------------------------------------
    // Key codes
    // ------------------------------------------------------------------

    /**
     * The X keycode a JavaFX key produces, or 0 when it has none.
     *
     * <p>The values are the standard X11 keycodes for a US layout, which is the mapping the
     * server's {@code Keymap} describes. Letters are not contiguous there, so they are looked up
     * by character: {@code a} is 38 and {@code z} is 52, with the punctuation keys of the home row
     * in between.
     *
     * <p>JavaFX reports one key code for both copies of a modifier, so Shift, Control and Alt
     * always produce the left variant (50, 37, 64) and the right handed keycodes are never sent.
     */
    static int xKeycode(KeyCode code) {
        int keycode = switch (code) {
            case ESCAPE -> 9;
            case DIGIT0 -> 19;
            case DIGIT1 -> 10;
            case DIGIT2 -> 11;
            case DIGIT3 -> 12;
            case DIGIT4 -> 13;
            case DIGIT5 -> 14;
            case DIGIT6 -> 15;
            case DIGIT7 -> 16;
            case DIGIT8 -> 17;
            case DIGIT9 -> 18;
            case MINUS -> 20;
            case EQUALS -> 21;
            case BACK_SPACE -> 22;
            case TAB -> 23;
            case OPEN_BRACKET -> 34;
            case CLOSE_BRACKET -> 35;
            case ENTER -> 36;
            case CONTROL -> 37;
            case SEMICOLON -> 47;
            case QUOTE -> 48;
            case BACK_QUOTE -> 49;
            case SHIFT -> 50;
            case BACK_SLASH -> 51;
            case COMMA -> 59;
            case PERIOD -> 60;
            case SLASH -> 61;
            case MULTIPLY -> 63;
            case ALT -> 64;
            case SPACE -> 65;
            case CAPS -> 66;
            case F1 -> 67;
            case F2 -> 68;
            case F3 -> 69;
            case F4 -> 70;
            case F5 -> 71;
            case F6 -> 72;
            case F7 -> 73;
            case F8 -> 74;
            case F9 -> 75;
            case F10 -> 76;
            case NUM_LOCK -> 77;
            case SCROLL_LOCK -> 78;
            case NUMPAD7 -> 79;
            case NUMPAD8 -> 80;
            case NUMPAD9 -> 81;
            case SUBTRACT -> 82;
            case NUMPAD4 -> 83;
            case NUMPAD5 -> 84;
            case NUMPAD6 -> 85;
            case ADD -> 86;
            case NUMPAD1 -> 87;
            case NUMPAD2 -> 88;
            case NUMPAD3 -> 89;
            case NUMPAD0 -> 90;
            case DECIMAL -> 91;
            case F11 -> 95;
            case F12 -> 96;
            case DIVIDE -> 106;
            case PRINTSCREEN -> 107;
            case ALT_GRAPH -> 108;
            case HOME -> 110;
            case UP -> 111;
            case PAGE_UP -> 112;
            case LEFT -> 113;
            case RIGHT -> 114;
            case END -> 115;
            case DOWN -> 116;
            case PAGE_DOWN -> 117;
            case INSERT -> 118;
            case DELETE -> 119;
            case PAUSE -> 127;
            case META -> 133;
            case CONTEXT_MENU -> 135;
            default -> 0;
        };
        // Keys the table does not name are still usable when they are a plain letter or digit.
        return keycode != 0 ? keycode : xKeycodeOfChar(code.getChar());
    }

    private static int xKeycodeOfChar(String character) {
        if (character == null || character.length() != 1) {
            return 0;
        }
        char value = character.charAt(0);
        if (value >= '0' && value <= '9') {
            return value == '0' ? 19 : 9 + (value - '0');
        }
        return Keymap.keycodeForLetter(value);
    }

    // ------------------------------------------------------------------
    // Toolkit
    // ------------------------------------------------------------------

    /**
     * How long the toolkit is given to come up before the server decides it cannot show anything.
     *
     * <p>Generous, because a cold JavaFX startup on a loaded machine is seconds rather than
     * milliseconds, and the cost of waiting is a delay while the cost of giving up too early is a
     * server that refuses to run on a machine that is merely slow.
     */
    private static final long TOOLKIT_START_TIMEOUT_MILLIS = 10_000;

    /**
     * Brings the toolkit up if it is not up already, and gives up if it will not come up.
     *
     * <p><b>The wait has to be bounded, and this is what the platform does without one.</b>
     * {@code Platform.startup} blocks until the toolkit is running, and where there is no display it
     * can block *for ever*: measured with {@code -Djava.awt.headless=true} on macOS,
     * {@code QuantumToolkit.startup} parks on a latch whose count never reaches zero, because the
     * render thread failed first ({@code ArrayIndexOutOfBoundsException: Index 0 out of bounds for
     * length 0}, an empty screen list). Nothing in the API reports that - no exception, no return
     * value - so the server used to come up, answer clients, and never show a frame, with a stack
     * trace from the render thread as its only explanation. The wait here is what turns that into a
     * failure the caller can act on.
     */
    private static synchronized void startupToolkit() {
        if (toolkitStarted) {
            return;
        }
        CountDownLatch ready = new CountDownLatch(1);
        AtomicReference<Throwable> failure =
                new AtomicReference<>();
        Thread starter = new Thread(() -> {
            try {
                // The runnable is what proves it: Platform.startup calls it on the FX thread once
                // the toolkit is up, so counting it down is the difference between "the call
                // returned" and "JavaFX works".
                Platform.startup(ready::countDown);
            } catch (IllegalStateException alreadyRunning) {
                // Someone else started the runtime first, which is the state we wanted anyway.
                ready.countDown();
            } catch (Throwable t) {
                failure.set(t);
                ready.countDown();
            }
        }, "javafx-startup");
        starter.setDaemon(true);
        starter.start();
        try {
            if (!ready.await(TOOLKIT_START_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)) {
                throw new IllegalStateException("the JavaFX toolkit did not start within "
                        + (TOOLKIT_START_TIMEOUT_MILLIS / 1000) + "s: this machine cannot show a"
                        + " window, so nothing would be forwarded. Run with --backend headless to"
                        + " serve clients without one.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while starting the JavaFX toolkit", e);
        }
        if (failure.get() != null) {
            throw new IllegalStateException("the JavaFX toolkit could not start: " + failure.get(),
                    failure.get());
        }
        // The server decides when the process is done, not the last window being closed.
        Platform.setImplicitExit(false);
        toolkitStarted = true;
    }

    /**
     * Runs the action on the FX thread and waits for it, rethrowing whatever it threw.
     */
    private static void runOnFxThreadAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Throwable t) {
                failure.set(t);
            } finally {
                done.countDown();
            }
        });
        try {
            if (!done.await(STARTUP_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                throw new IllegalStateException("the JavaFX toolkit did not start within "
                        + STARTUP_TIMEOUT_SECONDS + "s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted while waiting for the JavaFX thread", e);
        }
        Throwable thrown = failure.get();
        if (thrown instanceof RuntimeException runtime) {
            throw runtime;
        }
        if (thrown instanceof Error error) {
            throw error;
        }
        if (thrown != null) {
            throw new IllegalStateException("the JavaFX thread failed", thrown);
        }
    }
}
