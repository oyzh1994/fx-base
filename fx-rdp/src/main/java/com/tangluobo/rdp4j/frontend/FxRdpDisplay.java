package com.tangluobo.rdp4j.frontend;

import cn.oyzh.common.log.JulLog;
import com.tangluobo.rdp4j.graphics.Display;
import com.tangluobo.rdp4j.graphics.RdesktopCanvas;
import com.tangluobo.rdp4j.graphics.RdpCursor;
import com.tangluobo.rdp4j.graphics.RdpPalette;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;

import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

/** Pure JavaFX presentation of the protocol's raster backing store. */
public final class FxRdpDisplay implements Display {

    private static final String HIDDEN_CURSOR_NAME = "hidden";
    private static final int REMOTE_ECHO_TOLERANCE = 2;
    private static final double MIN_SCREEN_WARP_DISTANCE = 4.0;
    /** How long a server pointer-position recapture may hide the cursor unrenewed. */
    private static final long POINTER_POSITION_SUPPRESSION_NANOS = 2_000_000_000L;
    private static final int LEGACY_VM_CURSOR_MAX_SIZE = 24;
    private static final int MOVEMENT_PROBE_RADIUS = 24;
    private static final int MOVEMENT_EVIDENCE_THRESHOLD = 3;
    private static final long MOVEMENT_PROBE_INTERVAL_NANOS = 40_000_000L;
    private static final long MOVEMENT_PROBE_TIMEOUT_NANOS = 400_000_000L;
    /** Smallest frame change that can be a relocated pointer rather than noise. */
    private static final int CURSOR_CHANGE_MIN_PIXELS = 6;
    /**
     * Slack around the pointer rectangle when classifying a frame change. An
     * app repaint under the pointer (a menu item highlight, a hover effect, a
     * selection band) spans the widget, not the pointer, so it exceeds this.
     */
    private static final int CURSOR_SHAPE_MARGIN = 8;

    private final Object imageLock = new Object();
    private final Object refreshLock = new Object();
    private final AtomicBoolean refreshPending = new AtomicBoolean();
    private final AtomicBoolean cursorProbePending = new AtomicBoolean();
    private final StackPane view = new StackPane();
    private final ImageView imageView = new ImageView();
    private final BiConsumer<Integer, Integer> serverPointerMovedListener;
    private final Deque<MovementProbe> movementProbes = new ArrayDeque<>();
    private final AnimationTimer movementProbeTimer = new AnimationTimer() {
        @Override
        public void handle(long now) {
            expireMovementProbes(now);
            decayMovementEvidence(now);
            // Keep ticking while evidence is outstanding: decays have to run
            // even when no probe is left to expire.
            if (movementProbes.isEmpty() && movementEvidence <= 0) {
                stop();
            }
        }
    };
    /**
     * Immutable snapshot of the backing store. Publishing the buffer, its width
     * and its height through a single volatile reference makes the swap atomic:
     * a reader can never observe the width of one frame with the pixels of
     * another. Callers compare frames by identity, so {@link #resizeDisplay}
     * must allocate a new {@code Frame} rather than mutate the array.
     */
    private record Frame(int[] pixels, int width, int height) {
    }

    private volatile Frame frame;
    private volatile Frame displayedFrame;
    private volatile PixelBuffer<IntBuffer> pixelBuffer;
    private volatile RdpPalette palette;
    private volatile Runnable firstRemoteUpdateListener;
    private Robot pointerRobot;
    private int lastLocalPointerX = Integer.MIN_VALUE;
    private int lastLocalPointerY = Integer.MIN_VALUE;
    private boolean hasSeenHiddenCursor;
    private String lastCursorMode;
    private String serverCursorMode = "default";
    private Cursor lastVisibleFxCursor = Cursor.DEFAULT;
    private boolean softwareCursorProbeEligible;
    private boolean frameCursorSuppressed;
    private boolean pointerPositionSuppressed;
    private long pointerPositionSuppressedNanos;
    private int remoteCursorWidth;
    private int remoteCursorHeight;
    /** Raster identity of the last visible pointer, to tell a new shape from a re-send. */
    private long lastVisibleCursorSignature;
    private int movementEvidence;
    private long lastEvidenceNanos;
    private int lastProbePointerX = Integer.MIN_VALUE;
    private int lastProbePointerY = Integer.MIN_VALUE;
    private long lastMovementProbeNanos;
    private Frame dirtyFrame;
    private int dirtyLeft = Integer.MAX_VALUE;
    private int dirtyTop = Integer.MAX_VALUE;
    private int dirtyRight = -1;
    private int dirtyBottom = -1;

    public FxRdpDisplay(int width, int height) {
        this(width, height, null);
    }

    public FxRdpDisplay(int width, int height,
                        BiConsumer<Integer, Integer> serverPointerMovedListener) {
        this.serverPointerMovedListener = serverPointerMovedListener == null
                ? (x, y) -> { } : serverPointerMovedListener;
        Frame initial = new Frame(new int[Math.max(1, width) * Math.max(1, height)],
                Math.max(1, width), Math.max(1, height));
        frame = initial;
        imageView.setFocusTraversable(true);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(false);
        view.setStyle("-fx-background-color: black;");
        view.setMinSize(initial.width(), initial.height());
        view.setPrefSize(initial.width(), initial.height());
        view.getChildren().add(imageView);
        installBuffer(initial);
    }

    public StackPane getView() {
        return view;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setScaleToFit(boolean scaleToFit) {
        if (scaleToFit) {
            view.setMinSize(0, 0);
            view.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
            imageView.fitWidthProperty().bind(view.widthProperty());
            imageView.fitHeightProperty().bind(view.heightProperty());
        } else {
            imageView.fitWidthProperty().unbind();
            imageView.fitHeightProperty().unbind();
            imageView.setFitWidth(0);
            imageView.setFitHeight(0);
            Frame current = frame;
            view.setMinSize(current.width(), current.height());
            view.setPrefSize(current.width(), current.height());
        }
    }

    @Override
    public int checkColor(int color) {
        RdpPalette current = palette;
        return current == null ? color : current.getRGB(color & 0xff);
    }

    @Override
    public RdpCursor createCursor(String name, int hotspotX, int hotspotY,
                                  int[] pixels, int width, int height) {
        return new RdpCursor(name, hotspotX, hotspotY, pixels, width, height);
    }

    /**
     * Minimal binding for the Windows bell. JNA's {@code User32} interface does
     * not declare {@code MessageBeep}, and this is the closest equivalent to the
     * {@code java.awt.Toolkit.beep()} this replaces.
     */
    private interface User32Beep extends com.sun.jna.Library {
        User32Beep INSTANCE = com.sun.jna.Native.load("user32", User32Beep.class);

        /** @param type ignored; 0 selects the default system sound */
        int MessageBeep(int type);
    }

    /**
     * Sounds the RDP BELL PDU. JavaFX has no bell primitive, so this uses the
     * Windows {@code MessageBeep} API through JNA (already a direct dependency of
     * this module). Other platforms have no equivalent without bundling an audio
     * asset, so the bell is a no-op there.
     */
    @Override
    public void beep() {
        runOnFxThread(() -> {
            if (!com.sun.jna.Platform.isWindows()) {
                return;
            }
            try {
                User32Beep.INSTANCE.MessageBeep(0);
            } catch (Throwable t) {
                // Loading user32 can fail in constrained environments; a missing
                // bell must never disturb the session.
                JulLog.debug("Bell unavailable: " + t.getMessage());
            }
        });
    }

    @Override
    public int getDisplayHeight() {
        return frame.height();
    }

    @Override
    public int getDisplayWidth() {
        return frame.width();
    }

    @Override
    public int getRGB(int x, int y) {
        Frame current = frame;
        int stored = current.pixels()[y * current.width() + x];
        RdpPalette currentPalette = palette;
        if (currentPalette == null) {
            return stored;
        }
        // With a palette installed this returns the INDEX, not RGB: RasterOp
        // performs its raster operations in index space.
        return currentPalette.getIndexForRgb(stored & 0x00ffffff);
    }

    @Override
    public int[] getRGB(int x, int y, int cx, int cy, int[] data, int offset, int scanWidth) {
        Frame current = frame;
        if (data == null) {
            data = new int[cx * cy];
        }
        for (int row = 0; row < cy; row++) {
            System.arraycopy(current.pixels(), (y + row) * current.width() + x,
                    data, offset + row * scanWidth, cx);
        }
        return data;
    }

    @Override
    public void init(RdesktopCanvas canvas) {
        // The JavaFX input object is installed by FxRdpFrontend.
    }

    @Override
    public void repaint() {
        requestRefresh();
    }

    @Override
    public void repaint(int x, int y, int width, int height) {
        requestRefresh(x, y, width, height);
    }

    @Override
    public void repaintRemote(int x, int y, int width, int height) {
        requestRefresh(x, y, width, height);
        requestMovementCorrelationCheck();
        if (width <= 0 || height <= 0) {
            return;
        }
        Runnable listener = firstRemoteUpdateListener;
        if (listener != null) {
            synchronized (this) {
                if (firstRemoteUpdateListener != listener) {
                    return;
                }
                firstRemoteUpdateListener = null;
            }
            listener.run();
        }
    }

    @Override
    public void setFirstRemoteUpdateListener(Runnable listener) {
        firstRemoteUpdateListener = listener;
    }

    @Override
    public void resizeDisplay(int width, int height) {
        int newWidth = Math.max(1, width);
        int newHeight = Math.max(1, height);
        Frame source;
        synchronized (imageLock) {
            source = frame;
        }
        // Copy top-left anchored, without clearing or scaling. Newly exposed rows
        // and columns stay zero (transparent black), matching the old
        // BufferedImage + Graphics.drawImage behaviour. A plain row copy is
        // bit-identical to the SrcOver blit it replaces because every stored
        // pixel is opaque.
        int[] replacement = new int[newWidth * newHeight];
        int copyWidth = Math.min(source.width(), newWidth);
        int copyHeight = Math.min(source.height(), newHeight);
        for (int row = 0; row < copyHeight; row++) {
            System.arraycopy(source.pixels(), row * source.width(),
                    replacement, row * newWidth, copyWidth);
        }
        synchronized (imageLock) {
            // A fresh Frame, never an in-place edit: flushRefresh decides between
            // reinstalling the image and updating a dirty region by comparing
            // frame identity.
            frame = new Frame(replacement, newWidth, newHeight);
        }
        requestRefresh();
    }

    @Override
    public void setCursor(RdpCursor cursor) {
        Runnable update = () -> {
            if (cursor == null || cursor.getPixels() == null) {
                serverCursorMode = "default";
                lastVisibleFxCursor = Cursor.DEFAULT;
                clearCursorSuppression();
                view.setCursor(Cursor.DEFAULT);
                logCursorMode("default");
                return;
            }
            if (HIDDEN_CURSOR_NAME.equals(cursor.getName())
                    || cursor.isFullyTransparent()) {
                // A transparent ImageCursor is rendered as a black rectangle by
                // some Windows/Glass cursor paths and can leave the preceding
                // resize cursor visible. Both the RDP null-system-pointer and a
                // fully transparent VM cursor mean the native cursor is hidden.
                hasSeenHiddenCursor = true;
                serverCursorMode = "hidden";
                clearCursorSuppression();
                view.setCursor(Cursor.NONE);
                logCursorMode(HIDDEN_CURSOR_NAME.equals(cursor.getName())
                        ? "hidden-system" : "hidden-transparent");
                return;
            }
            int sourceWidth = Math.max(1, cursor.getWidth());
            int sourceHeight = Math.max(1, cursor.getHeight());
            Dimension2D bestSize = ImageCursor.getBestSize(sourceWidth, sourceHeight);
            int nativeWidth = Math.max(sourceWidth,
                    bestSize.getWidth() > 0 ? (int) Math.round(bestSize.getWidth()) : sourceWidth);
            int nativeHeight = Math.max(sourceHeight,
                    bestSize.getHeight() > 0 ? (int) Math.round(bestSize.getHeight()) : sourceHeight);
            // JavaFX/Windows otherwise scales unsupported sizes (for example
            // 24x24 or the 1x1 hidden pointer) to the native cursor size. A
            // transparent native-size canvas keeps the remote shape 1:1 and
            // prevents the newly allocated area from appearing as a black box.
            WritableImage image = createFxCursorImage(cursor.getPixels(), sourceWidth, sourceHeight,
                    nativeWidth, nativeHeight);
            double x = Math.max(0, Math.min(cursor.getHotspotX(), sourceWidth - 1));
            double y = Math.max(0, Math.min(cursor.getHotspotY(), sourceHeight - 1));
            Cursor fxCursor = new ImageCursor(image, x, y);
            long signature = cursorSignature(cursor);
            boolean pointerShapeChanged = signature != lastVisibleCursorSignature;
            lastVisibleCursorSignature = signature;
            remoteCursorWidth = sourceWidth;
            remoteCursorHeight = sourceHeight;
            serverCursorMode = "custom";
            lastVisibleFxCursor = fxCursor;
            softwareCursorProbeEligible = isLegacyVmSoftwareCursorCandidate(
                    sourceWidth, sourceHeight);
            pointerPositionSuppressed = false;
            if (!softwareCursorProbeEligible) {
                // Frame-change inference is only plausible for the small legacy
                // pointer of a nested console. Drop any suppression an earlier
                // pointer accumulated.
                forgetCursorEvidence();
            } else if (pointerShapeChanged) {
                // A pointer the server just redefined is authoritative: never
                // keep suppressing it. Frame evidence re-arms itself within a
                // few moves if the guest really does paint its own pointer.
                forgetCursorEvidence();
            }
            if (softwareCursorProbeEligible && frameCursorSuppressed
                    && movementEvidence >= MOVEMENT_EVIDENCE_THRESHOLD) {
                view.setCursor(Cursor.NONE);
                logCursorMode("hidden-software-cursor");
            } else {
                frameCursorSuppressed = false;
                view.setCursor(fxCursor);
                logCursorMode("custom-" + sourceWidth + "x" + sourceHeight);
            }
        };
        // The AWT implementation could throw HeadlessException here; the callers
        // used to guard against it. Keep cursor presentation non-fatal, since a
        // missing pointer must never break the session.
        runOnFxThread(() -> {
            try {
                update.run();
            } catch (RuntimeException e) {
                JulLog.warn("Cursor presentation failed: " + e.getMessage());
            }
        });
    }

    /** Re-applies the last server cursor after this window regains mouse/focus. */
    public void restoreCursorPresentation() {
        runOnFxThread(() -> {
            if ("hidden".equals(serverCursorMode)
                    || frameCursorSuppressed || pointerPositionSuppressed) {
                view.setCursor(Cursor.NONE);
            } else if ("custom".equals(serverCursorMode)) {
                view.setCursor(lastVisibleFxCursor);
            } else {
                view.setCursor(Cursor.DEFAULT);
            }
        });
    }

    @Override
    public void movePointer(int x, int y) {
        runOnFxThread(() -> {
            try {
                int remoteX = clamp(x, 0, getDisplayWidth() - 1);
                int remoteY = clamp(y, 0, getDisplayHeight() - 1);
                serverPointerMovedListener.accept(remoteX, remoteY);
                if (shouldHideForServerPointerPosition(hasSeenHiddenCursor, lastCursorMode)) {
                    // A server pointer-position PDU represents a programmatic
                    // pointer move, not an acknowledgement of ordinary client
                    // movement. Nested VM consoles use even a same-coordinate
                    // or very small move when they recapture the pointer. Apply
                    // the visibility transition before filtering physical Robot
                    // motion, otherwise the restored outer RDP arrow remains on
                    // top of the guest's software cursor after re-entry.
                    pointerPositionSuppressed = true;
                    pointerPositionSuppressedNanos = System.nanoTime();
                    frameCursorSuppressed = false;
                    view.setCursor(Cursor.NONE);
                    logCursorMode("hidden-recapture-position");
                }
                if (isLocalPointerEcho(remoteX, remoteY,
                        lastLocalPointerX, lastLocalPointerY)) {
                    // Most pointer-position updates only acknowledge the
                    // absolute coordinate just sent by this client. Warping on
                    // those one-pixel echoes pins slow movement to window edges.
                    return;
                }
                Bounds screenBounds = imageView.localToScreen(imageView.getBoundsInLocal());
                if (screenBounds == null || screenBounds.getWidth() <= 0 || screenBounds.getHeight() <= 0) {
                    return;
                }
                if (pointerRobot == null) {
                    pointerRobot = new Robot();
                }
                double remoteWidth = Math.max(1, getDisplayWidth() - 1);
                double remoteHeight = Math.max(1, getDisplayHeight() - 1);
                double screenX = screenBounds.getMinX()
                        + remoteX
                        * Math.max(0, screenBounds.getWidth() - 1) / remoteWidth;
                double screenY = screenBounds.getMinY()
                        + remoteY
                        * Math.max(0, screenBounds.getHeight() - 1) / remoteHeight;
                var currentPosition = pointerRobot.getMousePosition();
                if (!isMeaningfulScreenWarp(currentPosition.getX(), currentPosition.getY(),
                        screenX, screenY)) {
                    // Scaling and integer coordinate round-trips can differ by
                    // a few local pixels. Treat that as an echo as well.
                    return;
                }
                // Do not suppress the JavaFX MouseEvent generated by Robot.
                // Echoing the requested absolute position keeps the RDP server's
                // pointer state synchronized, matching the Swing implementation.
                pointerRobot.mouseMove(screenX, screenY);
            } catch (RuntimeException ignored) {
                // Pointer repositioning is optional on restricted desktops.
            }
        });
    }

    void recordLocalPointerPosition(int x, int y) {
        int previousX = lastLocalPointerX;
        int previousY = lastLocalPointerY;
        lastLocalPointerX = clamp(x, 0, getDisplayWidth() - 1);
        lastLocalPointerY = clamp(y, 0, getDisplayHeight() - 1);
        long now = System.nanoTime();
        expirePointerPositionSuppression(now);
        if (!softwareCursorProbeEligible || !"custom".equals(serverCursorMode)
                || pointerPositionSuppressed
                || previousX == Integer.MIN_VALUE || previousY == Integer.MIN_VALUE
                || now - lastMovementProbeNanos < MOVEMENT_PROBE_INTERVAL_NANOS
                || squaredDistance(lastProbePointerX, lastProbePointerY,
                        lastLocalPointerX, lastLocalPointerY) < 9) {
            return;
        }
        Frame current = frame;
        MovementProbe probe = new MovementProbe(now,
                FramePatch.capture(current, previousX, previousY, MOVEMENT_PROBE_RADIUS),
                FramePatch.capture(current, lastLocalPointerX, lastLocalPointerY,
                        MOVEMENT_PROBE_RADIUS));
        while (movementProbes.size() >= 8) {
            movementProbes.removeFirst();
        }
        movementProbes.addLast(probe);
        lastProbePointerX = lastLocalPointerX;
        lastProbePointerY = lastLocalPointerY;
        lastMovementProbeNanos = now;
        movementProbeTimer.start();
    }

    void recordLocalPointerButtonPosition(int x, int y) {
        lastLocalPointerX = clamp(x, 0, getDisplayWidth() - 1);
        lastLocalPointerY = clamp(y, 0, getDisplayHeight() - 1);
        // A click proves the user is driving this pointer, so it must never stay
        // hidden by an earlier inference. Frame evidence decides again from
        // scratch afterwards.
        if ("custom".equals(serverCursorMode)
                && (frameCursorSuppressed || pointerPositionSuppressed)) {
            forgetCursorEvidence();
            pointerPositionSuppressed = false;
            view.setCursor(lastVisibleFxCursor);
            logCursorMode("custom-pointer-button");
            return;
        }
        resetMovementCorrelation();
    }

    public static boolean isLegacyVmSoftwareCursorCandidate(int width, int height) {
        return width > 0 && height > 0
                && width <= LEGACY_VM_CURSOR_MAX_SIZE
                && height <= LEGACY_VM_CURSOR_MAX_SIZE;
    }

    public static boolean isLocalPointerEcho(int serverX, int serverY,
                                             int lastLocalX, int lastLocalY) {
        return lastLocalX != Integer.MIN_VALUE && lastLocalY != Integer.MIN_VALUE
                && Math.abs((long) serverX - lastLocalX) <= REMOTE_ECHO_TOLERANCE
                && Math.abs((long) serverY - lastLocalY) <= REMOTE_ECHO_TOLERANCE;
    }

    public static boolean isMeaningfulScreenWarp(double currentX, double currentY,
                                                  double targetX, double targetY) {
        double deltaX = targetX - currentX;
        double deltaY = targetY - currentY;
        return deltaX * deltaX + deltaY * deltaY
                > MIN_SCREEN_WARP_DISTANCE * MIN_SCREEN_WARP_DISTANCE;
    }

    public static boolean shouldHideForServerPointerPosition(boolean hasSeenHiddenCursor,
                                                              String currentMode) {
        return hasSeenHiddenCursor
                && (currentMode == null || !currentMode.startsWith("hidden-"));
    }

    private void requestMovementCorrelationCheck() {
        if (!cursorProbePending.compareAndSet(false, true)) {
            return;
        }
        runOnFxThread(() -> {
            cursorProbePending.set(false);
            evaluateMovementCorrelation();
        });
    }

    private void evaluateMovementCorrelation() {
        if (!softwareCursorProbeEligible || !"custom".equals(serverCursorMode)
                || pointerPositionSuppressed
                || movementProbes.isEmpty()) {
            return;
        }
        Frame current = frame;
        int cursorWidth = Math.max(1, remoteCursorWidth);
        int cursorHeight = Math.max(1, remoteCursorHeight);
        MovementProbe matched = null;
        for (var iterator = movementProbes.descendingIterator(); iterator.hasNext();) {
            MovementProbe candidate = iterator.next();
            if (candidate.matches(current, cursorWidth, cursorHeight)) {
                matched = candidate;
                break;
            }
        }
        if (matched == null) {
            return;
        }
        while (!movementProbes.isEmpty()) {
            MovementProbe removed = movementProbes.removeFirst();
            if (removed == matched) {
                break;
            }
        }
        lastEvidenceNanos = System.nanoTime();
        movementEvidence = Math.min(MOVEMENT_EVIDENCE_THRESHOLD + 2, movementEvidence + 1);
        if (movementEvidence >= MOVEMENT_EVIDENCE_THRESHOLD && !frameCursorSuppressed) {
            frameCursorSuppressed = true;
            view.setCursor(Cursor.NONE);
            logCursorMode("hidden-software-cursor");
        }
    }

    private void expireMovementProbes(long now) {
        while (!movementProbes.isEmpty()
                && now - movementProbes.peekFirst().createdNanos() >= MOVEMENT_PROBE_TIMEOUT_NANOS) {
            movementProbes.removeFirst();
        }
    }

    /**
     * Decay accumulated evidence once no fresh match has arrived for a timeout
     * window. Decaying on the timer rather than on probe expiry matters: a match
     * consumes every queued probe, which used to stop the animation timer with
     * the evidence still above the threshold, leaving the pointer hidden until
     * the next move happened to produce non-matching probes.
     */
    private void decayMovementEvidence(long now) {
        if (movementEvidence <= 0) {
            return;
        }
        long steps = (now - lastEvidenceNanos) / MOVEMENT_PROBE_TIMEOUT_NANOS;
        if (steps <= 0) {
            return;
        }
        movementEvidence -= (int) Math.min(movementEvidence, steps);
        lastEvidenceNanos = now;
        if (movementEvidence == 0 && frameCursorSuppressed
                && "custom".equals(serverCursorMode) && !pointerPositionSuppressed) {
            frameCursorSuppressed = false;
            view.setCursor(lastVisibleFxCursor);
            logCursorMode("custom-hardware-cursor");
        }
    }

    private void resetMovementCorrelation() {
        movementProbes.clear();
        movementProbeTimer.stop();
        movementEvidence = 0;
        lastEvidenceNanos = 0;
        lastProbePointerX = Integer.MIN_VALUE;
        lastProbePointerY = Integer.MIN_VALUE;
        lastMovementProbeNanos = 0;
    }

    /**
     * Release a recapture suppression that the server stopped renewing.
     *
     * <p>A pointer-position PDU is treated as a nested console recapturing the
     * pointer, which hides the client cursor. A desktop guest can send one for
     * an unrelated reason (a popup that warps the pointer onto itself), and the
     * server is under no obligation to send a further cursor update afterwards,
     * so without this the pointer could stay invisible for the rest of the
     * session. A real recapture keeps arriving and re-arms it, so the expiry is
     * only reached once the recapture is over.
     */
    private void expirePointerPositionSuppression(long now) {
        if (!pointerPositionSuppressed
                || now - pointerPositionSuppressedNanos < POINTER_POSITION_SUPPRESSION_NANOS) {
            return;
        }
        pointerPositionSuppressed = false;
        if ("custom".equals(serverCursorMode)) {
            view.setCursor(lastVisibleFxCursor);
            logCursorMode("custom-position-timeout");
        }
    }

    /** Forget the frame evidence but keep the server-driven cursor state. */
    private void forgetCursorEvidence() {
        frameCursorSuppressed = false;
        resetMovementCorrelation();
    }

    /** Drop every client-side guess about a guest-painted pointer. */
    private void clearCursorSuppression() {
        softwareCursorProbeEligible = false;
        frameCursorSuppressed = false;
        pointerPositionSuppressed = false;
        pointerPositionSuppressedNanos = 0;
        remoteCursorWidth = 0;
        remoteCursorHeight = 0;
        resetMovementCorrelation();
    }

    /** Raster identity of a decoded pointer, used to detect a redefined shape. */
    private static long cursorSignature(RdpCursor cursor) {
        return ((long) cursor.getWidth() << 48)
                ^ ((long) cursor.getHeight() << 32)
                ^ (Integer.toUnsignedLong(Arrays.hashCode(cursor.getPixels())));
    }

    private static long squaredDistance(int firstX, int firstY, int secondX, int secondY) {
        if (firstX == Integer.MIN_VALUE || firstY == Integer.MIN_VALUE) {
            return Long.MAX_VALUE;
        }
        long deltaX = (long) secondX - firstX;
        long deltaY = (long) secondY - firstY;
        return deltaX * deltaX + deltaY * deltaY;
    }

    /**
     * Decide whether a changed region can be a relocated pointer sprite.
     *
     * <p>A guest-painted pointer erases its old sprite and blits the same shape
     * at the new position, so both changes stay inside the pointer rectangle.
     * Counting changed pixels alone is not enough: a menu item highlight, a
     * hover effect or a selection band that happens to sit under the pointer
     * changes hundreds of pixels too, which is what used to hide a perfectly
     * healthy hardware cursor on desktops whose theme uses small pointers. The
     * bounding box is the discriminator, because those repaints span a widget
     * rather than a pointer.
     *
     * @param changed      number of pixels that differ
     * @param boxWidth     width of the changed region, 0 when nothing changed
     * @param boxHeight    height of the changed region, 0 when nothing changed
     * @param cursorWidth  width of the remote pointer
     * @param cursorHeight height of the remote pointer
     * @return true when the change is shaped like a pointer
     */
    public static boolean isCursorShapedChange(int changed, int boxWidth, int boxHeight,
                                               int cursorWidth, int cursorHeight) {
        if (changed < CURSOR_CHANGE_MIN_PIXELS) {
            return false;
        }
        // Allow for antialiasing, a shadow and a theme pointer that is larger
        // than the transmitted raster, but not for a widget-sized repaint.
        int budget = Math.max(CURSOR_CHANGE_MIN_PIXELS,
                Math.max(cursorWidth, 1) * Math.max(cursorHeight, 1) * 2);
        return changed <= budget
                && boxWidth <= cursorWidth + CURSOR_SHAPE_MARGIN
                && boxHeight <= cursorHeight + CURSOR_SHAPE_MARGIN;
    }

    private record MovementProbe(long createdNanos, FramePatch oldPosition, FramePatch newPosition) {
        boolean matches(Frame frame, int cursorWidth, int cursorHeight) {
            return oldPosition.looksLikeCursorShape(frame, cursorWidth, cursorHeight)
                    && newPosition.looksLikeCursorShape(frame, cursorWidth, cursorHeight);
        }
    }

    private record FramePatch(int x, int y, int width, int height, int[] pixels) {
        static FramePatch capture(Frame frame, int centerX, int centerY, int radius) {
            int x = clamp(centerX - radius, 0, frame.width() - 1);
            int y = clamp(centerY - radius, 0, frame.height() - 1);
            int right = clamp(centerX + radius, 0, frame.width() - 1);
            int bottom = clamp(centerY + radius, 0, frame.height() - 1);
            int width = right - x + 1;
            int height = bottom - y + 1;
            int stride = frame.width();
            int[] source = frame.pixels();
            int[] patch = new int[width * height];
            for (int row = 0; row < height; row++) {
                System.arraycopy(source, (y + row) * stride + x, patch, row * width, width);
            }
            return new FramePatch(x, y, width, height, patch);
        }

        /** True when this region now looks like a pointer sprite was erased or drawn. */
        boolean looksLikeCursorShape(Frame frame, int cursorWidth, int cursorHeight) {
            int stride = frame.width();
            int[] source = frame.pixels();
            int changed = 0;
            int left = Integer.MAX_VALUE;
            int top = Integer.MAX_VALUE;
            int right = -1;
            int bottom = -1;
            int index = 0;
            for (int row = 0; row < height; row++) {
                int base = (y + row) * stride + x;
                for (int column = 0; column < width; column++) {
                    if ((pixels[index++] & 0x00ffffff) != (source[base + column] & 0x00ffffff)) {
                        changed++;
                        if (column < left) {
                            left = column;
                        }
                        if (column > right) {
                            right = column;
                        }
                        if (row < top) {
                            top = row;
                        }
                        if (row > bottom) {
                            bottom = row;
                        }
                    }
                }
            }
            return isCursorShapedChange(changed,
                    right < left ? 0 : right - left + 1,
                    bottom < top ? 0 : bottom - top + 1,
                    cursorWidth, cursorHeight);
        }
    }

    private void logCursorMode(String mode) {
        if (!mode.equals(lastCursorMode)) {
            lastCursorMode = mode;
            JulLog.info("FX remote cursor mode=" + mode);
        }
    }

    @Override
    public void setPalette(RdpPalette palette) {
        this.palette = palette;
    }

    @Override
    public void setRGB(int x, int y, int color) {
        Frame current = frame;
        RdpPalette currentPalette = palette;
        int rgb = currentPalette == null ? color : currentPalette.getRGB(color & 0xff);
        current.pixels()[y * current.width() + x] = opaque(rgb);
    }

    @Override
    public void setRGB(int x, int y, int width, int height, int[] data, int offset, int scanWidth) {
        Frame current = frame;
        int[] target = current.pixels();
        int stride = current.width();
        RdpPalette currentPalette = palette;
        if (currentPalette == null) {
            copyOpaquePixels(target, stride, x, y,
                    data, offset, scanWidth, width, height);
            return;
        }
        for (int row = 0; row < height; row++) {
            int sourceIndex = offset + row * scanWidth;
            int targetIndex = (y + row) * stride + x;
            for (int column = 0; column < width; column++) {
                target[targetIndex + column] = opaque(currentPalette.getRGB(data[sourceIndex + column] & 0xff));
            }
        }
    }

    @Override
    public void setRGBNoConversion(int x, int y, int width, int height, int[] data, int offset, int scanWidth) {
        Frame current = frame;
        copyOpaquePixels(current.pixels(), current.width(), x, y,
                data, offset, scanWidth, width, height);
    }

    private void requestRefresh() {
        Frame current = frame;
        requestRefresh(0, 0, current.width(), current.height());
    }

    private void requestRefresh(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        Frame current = frame;
        int imageWidth = current.width();
        int imageHeight = current.height();
        int left = clamp(x, 0, imageWidth);
        int top = clamp(y, 0, imageHeight);
        int right = (int) Math.max(0, Math.min((long) imageWidth, (long) x + width));
        int bottom = (int) Math.max(0, Math.min((long) imageHeight, (long) y + height));
        if (right <= left || bottom <= top) {
            return;
        }

        boolean schedule;
        synchronized (refreshLock) {
            if (dirtyFrame != current) {
                dirtyFrame = current;
                clearDirtyRegion();
            }
            dirtyLeft = Math.min(dirtyLeft, left);
            dirtyTop = Math.min(dirtyTop, top);
            dirtyRight = Math.max(dirtyRight, right);
            dirtyBottom = Math.max(dirtyBottom, bottom);
            schedule = refreshPending.compareAndSet(false, true);
        }
        if (schedule) {
            Platform.runLater(this::flushRefresh);
        }
    }

    private void flushRefresh() {
        Frame current;
        Rectangle2D dirtyRegion;
        synchronized (refreshLock) {
            current = frame;
            if (dirtyFrame != current) {
                dirtyFrame = current;
                dirtyLeft = 0;
                dirtyTop = 0;
                dirtyRight = current.width();
                dirtyBottom = current.height();
            }
            dirtyRegion = dirtyRight > dirtyLeft && dirtyBottom > dirtyTop
                    ? new Rectangle2D(dirtyLeft, dirtyTop,
                            dirtyRight - dirtyLeft, dirtyBottom - dirtyTop)
                    : null;
            clearDirtyRegion();
            refreshPending.set(false);
        }
        if (displayedFrame != current) {
            installBuffer(current);
        } else if (dirtyRegion != null) {
            PixelBuffer<IntBuffer> currentBuffer = pixelBuffer;
            if (currentBuffer != null) {
                currentBuffer.updateBuffer(ignored -> dirtyRegion);
            }
        }
    }

    private void clearDirtyRegion() {
        dirtyLeft = Integer.MAX_VALUE;
        dirtyTop = Integer.MAX_VALUE;
        dirtyRight = -1;
        dirtyBottom = -1;
    }

    /**
     * Wrap a frame's pixel array for display. A fresh {@link PixelBuffer} is
     * allocated every time: two PixelBuffers must never wrap the same array, and
     * this must run on the JavaFX thread (it is reached only from the
     * constructor and from {@code flushRefresh}, which is a
     * {@code Platform.runLater} body).
     */
    private void installBuffer(Frame current) {
        PixelBuffer<IntBuffer> replacement = new PixelBuffer<>(current.width(), current.height(),
                IntBuffer.wrap(current.pixels()), PixelFormat.getIntArgbPreInstance());
        pixelBuffer = replacement;
        displayedFrame = current;
        imageView.setImage(new WritableImage(replacement));
        if (!imageView.fitWidthProperty().isBound()) {
            view.setMinSize(current.width(), current.height());
            view.setPrefSize(current.width(), current.height());
        }
    }

    private static int opaque(int color) {
        return color | 0xff000000;
    }

    static void copyOpaquePixels(int[] target, int targetStride, int targetX, int targetY,
                                 int[] source, int sourceOffset, int sourceStride,
                                 int width, int height) {
        for (int row = 0; row < height; row++) {
            int sourceIndex = sourceOffset + row * sourceStride;
            int targetIndex = (targetY + row) * targetStride + targetX;
            for (int column = 0; column < width; column++) {
                target[targetIndex + column] = opaque(source[sourceIndex + column]);
            }
        }
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    /**
     * Premultiply a non-premultiplied {@code 0xAARRGGBB} cursor raster into a
     * {@link WritableImage} laid out on a possibly larger transparent canvas.
     *
     * @param sourcePixels non-premultiplied cursor pixels, {@code width * height}
     * @param width        cursor width
     * @param height       cursor height
     * @param canvasWidth  native canvas width, at least {@code width}
     * @param canvasHeight native canvas height, at least {@code height}
     * @return the cursor image
     */
    public static WritableImage createFxCursorImage(int[] sourcePixels,
                                                    int width, int height,
                                                    int canvasWidth, int canvasHeight) {
        if (canvasWidth < width || canvasHeight < height) {
            throw new IllegalArgumentException("Cursor canvas must contain the source image without scaling");
        }
        int[] nativePixels = new int[canvasWidth * canvasHeight];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = sourcePixels[y * width + x];
                int alpha = argb >>> 24;
                if (alpha == 0) {
                    // Glass expects transparent cursor padding to contain no
                    // residual RGB; some Windows cursor paths render it black.
                    nativePixels[y * canvasWidth + x] = 0;
                    continue;
                }
                int red = ((argb >>> 16) & 0xff) * alpha / 255;
                int green = ((argb >>> 8) & 0xff) * alpha / 255;
                int blue = (argb & 0xff) * alpha / 255;
                nativePixels[y * canvasWidth + x] = (alpha << 24)
                        | (red << 16) | (green << 8) | blue;
            }
        }
        PixelBuffer<IntBuffer> cursorBuffer = new PixelBuffer<>(canvasWidth, canvasHeight,
                IntBuffer.wrap(nativePixels), PixelFormat.getIntArgbPreInstance());
        return new WritableImage(cursorBuffer);
    }

    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
