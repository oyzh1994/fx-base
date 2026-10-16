package cn.oyzh.fx.xserver.display;

import java.awt.Rectangle;

/**
 * Runs the server without a window.
 *
 * <p>The framebuffer still exists and clients can still connect, draw and read pixels back; only
 * the copy to a real display is missing. That makes this the backend for tests, for CI and for
 * machines that have no display but still need an X server to talk to, the role Xvfb plays.
 */
public final class HeadlessBackend implements DisplayBackend {

    @Override
    public String name() {
        return "headless";
    }

    @Override
    public boolean isHeadless() {
        return true;
    }

    @Override
    public void start(Screen screen, int displayNumber, InputSink input) {
        // Nothing to open: there is no window and no input source.
    }

    @Override
    public void present(Screen screen, Rectangle damage, CursorSprite cursor) {
        // Deliberately empty, and cheap enough to be called on every server tick. The pointer
        // sprite goes the same way: with no window there is nowhere to draw it.
    }

    @Override
    public void stop() {
        // Nothing to tear down.
    }

    @Override
    public boolean isClosed() {
        // Nothing can close a window that was never opened, so the server keeps running until it
        // is stopped from the outside.
        return false;
    }
}
