package cn.oyzh.fx.xserver.display;

import java.awt.Rectangle;

/**
 * Presents a screen's framebuffer in a real window and feeds keyboard and pointer input back into
 * the server.
 *
 * <p>The server always renders into its own pixel buffer; the backend only copies changed regions
 * to the screen and translates platform input events into X keycodes and coordinates. That keeps
 * the protocol implementation independent of the toolkit, and makes headless operation a matter
 * of choosing a different backend.
 */
public interface DisplayBackend {

    /** Receives input translated into X terms. */
    interface InputSink {
        /** Pointer moved to screen coordinates. */
        void pointerMoved(int x, int y);

        /**
         * Button state changed at the given position. {@code button} is the X button number
         * (1 = left, 2 = middle, 3 = right, 4/5 = wheel).
         */
        void buttonChanged(int button, boolean pressed, int x, int y);

        /** A key changed state. {@code keycode} is an X keycode, not a keysym. */
        void keyChanged(int keycode, boolean pressed, int x, int y);

        /**
         * The host composed a character for the user - an input method's output, or a dead key's
         * accent - which no keycode stands for.
         *
         * <p>Every platform's toolkit reports this separately from the key that produced it: JavaFX
         * as {@code KEY_TYPED}, AWT as {@code keyTyped}, and each of them after the input method has
         * had its turn, so what arrives here is the text the user meant rather than the keys that
         * were pressed. The server types it with a keycode of its own - see
         * {@code InputState.keyTyped} - because that is the only thing an X client can be sent.
         */
        default void keyTyped(char character) {
        }

        /**
         * A character the host's input method is putting *into the application*: the pre-edit it is
         * showing in the field, or the text it committed.
         *
         * <p>Unlike {@link #keyTyped}, this is typed even when a key carries the character: the
         * keystroke that would have carried it went to the input method, so the application never saw
         * it. {@code InputState.inputMethodTyped} is the implementation that knows that.
         */
        default void inputMethodTyped(char character) {
        }

        /**
         * The user dropped files from the host's desktop onto this window, at a point on the
         * screen in framebuffer coordinates.
         *
         * <p>The other half of the drag is XDND and belongs to the server, because X's drag and
         * drop is client to client and the forwarded application is a target that works already:
         * see {@code DropBridge}. This is only the platform's report that a drag ended on the
         * window, which is the part no X client can know.
         */
        default void hostDrop(java.util.List<String> paths, int x, int y) {
        }

        /** The window gained or lost focus; the server tracks this for the input focus. */
        void focusChanged(boolean focused);

        /**
         * The user resized the host window: the client's own window should become this size.
         *
         * <p>This is a request, not an application. The backend calls it from the toolkit's thread,
         * where taking the server lock and resizing a client's window would deadlock against the
         * display tick waiting on the toolkit; the server applies it from the tick instead, which is
         * also what keeps the framebuffer and the window from ever disagreeing about their size.
         */
        default void windowResized(int width, int height) {
        }

        /**
         * The user maximized or restored the host window, and the client's window should take the
         * size the backend worked out for it.
         *
         * <p>Separate from {@link #windowResized} because it is not the same kind of request: a drag
         * is one size and the last one wins, while a maximize is a *state* whose size the platform
         * also reports through the ordinary resize path - the platform's own idea of the maximized
         * geometry (macOS's "standard frame", which for a forwarded window is the size the client's
         * window already had) arrives behind this one and would overwrite it. The server applies
         * this size in preference, once.
         */
        default void windowMaximized(boolean maximized, int width, int height) {
        }

        /**
         * The user moved the host window, in the platform's own screen coordinates.
         *
         * <p>Reported so that where a window was arranged can be remembered (see
         * {@code HostWindowStore}) - the position belongs to the host window, so nothing else knows
         * it. Like {@link #windowResized} this comes from the toolkit's thread and is only a
         * notification: the server records it and writes it out from the display tick.
         */
        default void windowMoved(int x, int y) {
        }
    }

    /** Human readable name, used in logs. */
    String name();

    /**
     * Opens the window and starts delivering input. Called once from the server's start method,
     * which must return before any input arrives.
     */
    void start(Screen screen, int displayNumber, InputSink input);

    /**
     * Copies the damaged region of the framebuffer to the screen, then draws the pointer sprite
     * over it.
     *
     * <p>{@code cursor} is null when there is nothing to draw. It is passed with the frame rather
     * than painted into the framebuffer so that a frame always shows the pointer at the position it
     * had when the frame was taken, and so that what the clients drew is never covered by it.
     */
    void present(Screen screen, Rectangle damage, CursorSprite cursor);

    /**
     * Restricts the window to this rectangle of the screen, in framebuffer coordinates.
     *
     * <p>A window that shows one client's own screen only has to show the part of it that client's
     * windows occupy, which is what keeps a small application from arriving in a screen-sized window
     * with black around it. May be called before {@link #start}: the rectangle is remembered and the
     * window opens at that size.
     *
     * <p>Called from the display tick only, never from a request thread.
     */
    default void setViewport(Rectangle viewport) {
    }

    /**
     * Releases whatever the backend holds open for the whole process, once no window is left. The
     * JavaFX toolkit is the reason this exists: it keeps a non-daemon thread alive, so something has
     * to end it, but ending it on the first window that closes would take every other window with
     * it.
     */
    default void releaseToolkit() {
    }

    /**
     * Tells the window whether any client is connected.
     *
     * <p>With no client the server stops sending frames and the window holds the last one, and the
     * host pointer is handed back: it was hidden in favour of the pointer the server draws from the
     * client's cursor, and with no client there is nothing to draw in its place.
     */
    default void setIdle(boolean idle) {
    }

    /**
     * Sets the title the window shows, taken from the client's own window title. The server's
     * configured title is the fallback for a client that publishes none.
     *
     * <p>Called from the display tick only, never from a request thread.
     */
    default void setWindowTitle(String title) {
    }

    /**
     * Sets the icon the window shows, as non-premultiplied ARGB pixels, or clears it when
     * {@code argb} is null.
     *
     * <p>Called from the display tick only, never from a request thread.
     */
    default void setWindowIcon(int[] argb, int width, int height) {
    }

    /**
     * The largest client area this window can be given, which the user must not be able to drag it
     * past.
     *
     * <p>The screen is what it is: a window stretched beyond the area the client's windows can fill
     * leaves the part it grew into showing nothing, and the frame is pulled back to the content the
     * moment the server next says how big that is - a window that jumps away from the pointer that
     * is still dragging it. A window manager states the limit in {@code WM_NORMAL_HINTS} and the
     * platform enforces it; the backend is where that limit has to be applied here, because the
     * toolkit is what owns the window.
     *
     * <p>Called from the display tick only, never from a request thread.
     */
    default void setMaxClientArea(int width, int height) {
    }

    /**
     * Opens the window at a remembered position, in the platform's own screen coordinates.
     *
     * <p>The backend is what clamps it - a position remembered on a display that is no longer there
     * would otherwise put the window where the user cannot reach it - and a backend with nothing to
     * move (a headless one) simply ignores this.
     *
     * <p>Called from the tick when the window opens, and never while the server lock is held.
     */
    default void setWindowPosition(int x, int y) {
    }

    /**
     * The screen has been resized, so whatever the backend holds sized to it has to be rebuilt.
     *
     * <p>A screen grows when a client's window is dragged past what it could show; the pixels move
     * into a larger array, so a backend that wrapped the old one - a {@code PixelBuffer} over it, a
     * {@code BufferedImage} filled from it - is holding a picture of the size the screen used to be.
     * The framebuffer object is the same one, and {@code screen.framebuffer().pixels()} is the array
     * that is there now.
     *
     * <p>Called from the display tick after the resize, never while the server lock is held.
     */
    default void screenResized(Screen screen) {
    }

    /**
     * Shows the window full screen, or puts it back, because the client asked to fill the display.
     *
     * <p>A client that goes full screen - a terminal, a video player - has asked for the whole
     * display, and the host window is the frame around it: showing anything less would be showing it
     * a window smaller than it asked for. Called from the display tick only.
     */
    default void setFullScreen(boolean fullScreen) {
    }

    /**
     * Brings the window to the front without taking the keyboard, because the client set its urgency
     * hint.
     *
     * <p>Deliberately not a focus change and deliberately not the raise-on-show that {@code C2}
     * describes: this one happens only when a client says it wants the user's attention, which is
     * the one case where pulling a window out from behind another is what was asked for. Called once
     * per hint, never per frame.
     */
    /**
     * A click on the tray icon, in the icon's own pixels.
     *
     * <p>Where a docked notification-area icon's clicks go (C9). The backend reports what the
     * platform's menu bar was clicked at, and the tray turns it into a click on the client's icon
     * window - a menu bar has no X coordinates of its own, so the mapping is the backend's to make.
     */
    @FunctionalInterface
    interface TrayClick {
        void clicked(int x, int y, int button, int clickCount);
    }

    /**
     * Whether this backend has somewhere to put a tray icon: a menu bar, or the notification area of
     * a desktop. False by default, and false for the headless and VNC backends - a screen served over
     * RFB has no host menu bar, and a client's tray icon would have nothing to be shown in.
     */
    default boolean hasTray() {
        return false;
    }

    /**
     * Shows the notification area's icons, or takes the icon away when {@code image} is null.
     *
     * <p>Called from the server's tray poll thread, so an implementation must not block on the
     * toolkit's thread: the AWT one hands the work to the event dispatch thread.
     */
    default void setTrayIcon(java.awt.image.BufferedImage image, String tooltip) {
    }

    /** Where tray icon clicks go. Null means nobody wants them. */
    default void setTrayClickListener(TrayClick listener) {
    }

    default void raise() {
    }

    /**
     * Brings the window to the front *and gives it the keyboard*, for a window that has just been
     * opened for a client that has just started drawing - the one moment the user is asking for it.
     *
     * <p>Kept apart from {@link #raise()} because the two are asked for by different things: a client
     * whose window carries the urgency hint is saying "look at me", which is not the same as asking for
     * the keyboard away from whatever the user is typing into, and the window that has *appeared* is one
     * the user is waiting for. Backends that cannot give it the keyboard raise it instead.
     */
    default void raiseAndFocus() {
        raise();
    }

    /**
     * The pointer's appearance, told every frame whether or not anything was drawn.
     *
     * <p>A backend that shows the pointer itself - the JavaFX and Swing windows, which draw it over
     * the frame they present - has no use for this: the sprite already arrives with
     * {@link #present}. A backend whose viewers are somewhere else does: a viewer that has just
     * connected needs to know what the pointer looks like, and on a screen where nothing is being
     * drawn there is no frame to carry it.
     */
    default void presentCursor(CursorSprite cursor) {
    }

    /** Tears the window down. */
    void stop();

    /** True when the backend reports the window has been closed by the user. */
    boolean isClosed();

    /**
     * True when this backend opens no window on this machine: the headless and VNC ones, which serve
     * a screen without showing it here.
     *
     * <p>Not "draws nothing" - a VNC backend draws for every viewer that connects. What it says is
     * that there is no window on this machine to move, resize, title or bring forward, which is what
     * the server's own log has to be able to tell: a line claiming a window was opened on a machine
     * that cannot show one is the single sentence a user follows to find their application, and it
     * would be false.
     */
    default boolean isHeadless() {
        return false;
    }
}
