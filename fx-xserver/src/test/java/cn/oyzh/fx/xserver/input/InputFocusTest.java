package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.ServerOptions;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.display.Screen;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The input focus follows its window: it reverts when that window is destroyed, and a focus of the
 * root means the keys belong to whatever the pointer is in.
 *
 * <p>Without both, a toolkit that moves the focus to a dialog and lets the dialog be destroyed
 * leaves the focus on a window that no longer exists, and every keystroke aimed at the window
 * underneath is swallowed - clicks keep working, so the window looks alive while its text fields
 * cannot be typed into at all.
 */
public class InputFocusTest {

    private static XWindow window(Screen screen, XWindow parent, int id, int x, int y, int w,
                                  int h) {
        return new XWindow(id, null, screen, parent, 24, XWindow.INPUT_OUTPUT,
                screen.rootVisual(), x, y, w, h, 0);
    }

    private static XServer serverWithTwoWindows(String... extra) {
        String[] all = {"--display", "93", "--backend", "headless"};
        String[] merged = new String[all.length + extra.length];
        System.arraycopy(all, 0, merged, 0, all.length);
        System.arraycopy(extra, 0, merged, all.length, extra.length);
        XServer server = new XServer(ServerOptions.parse(merged));
        XWindow outer = window(server.screen(), server.rootWindow(), 0x400001, 10, 10, 60, 60);
        server.resources().add(outer);
        outer.setMapped(true);
        XWindow inner = window(server.screen(), outer, 0x400002, 5, 5, 20, 20);
        server.resources().add(inner);
        inner.setMapped(true);
        return server;
    }

    @Test
    void focusRevertsToTheParentWhenTheFocusedWindowIsDestroyed() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        XWindow inner = (XWindow) server.resources().get(0x400002);
        server.input().setInputFocus(inner, InputState.REVERT_TO_PARENT);

        server.windows().destroyWindowTree(null, inner);

        assertEquals(outer, server.input().inputFocus(), "the focus went to the parent, as asked");
    }

    @Test
    void focusGoesToTheRootWhenTheWindowItWasOnIsDestroyed() {
        XServer server = serverWithTwoWindows();
        XWindow inner = (XWindow) server.resources().get(0x400002);
        server.input().setInputFocus(inner, InputState.REVERT_TO_POINTER_ROOT);

        server.windows().destroyWindowTree(null, inner);

        assertEquals(server.rootWindow(), server.input().inputFocus());
    }

    @Test
    void aRootFocusSendsKeysToTheWindowUnderThePointer() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        // The focus is the root, which means PointerRoot: the focus follows the pointer.
        server.input().setInputFocus(server.rootWindow(), InputState.REVERT_TO_POINTER_ROOT);
        // Inside the outer window but outside the child that covers its top left corner.
        server.input().pointerMoved(60, 60);

        assertEquals(outer, server.input().focusWindow(), "keys belong to the window under the pointer");
    }

    /**
     * The root window asked for by id is a state of its own, and the keys follow the pointer in it
     * just as they do for {@code PointerRoot}.
     *
     * <p>Measured against a reference server with {@code tools/KeyTargetProbe}: with the focus on the
     * root - whether asked for directly or reached by a revert - the {@code KeyPress} is reported
     * against the window the pointer is in, and only {@code None} leaves it unreported. A server that
     * hands the key to the root window instead sends it to a client that never asked for it, which is
     * a text field that looks focused, still reacts to the mouse, and accepts nothing.
     */
    @Test
    void theRootWindowFocusedByIdAlsoSendsKeysToTheWindowUnderThePointer() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        // The three-argument form, because the two-argument one is the shorthand for "the root means
        // PointerRoot": the request handler passes the flag the request carried, and a client that
        // names the root's id asks for the root window, which GetInputFocus then answers.
        server.input().setInputFocus(server.rootWindow(), InputState.REVERT_TO_PARENT, false);
        server.input().pointerMoved(60, 60);

        assertEquals(server.rootWindow(), server.input().inputFocus(), "the root window, not a window"
                + " of its own");
        assertEquals(false, server.input().focusIsPointerRoot(), "and not PointerRoot either");
        assertEquals(outer, server.input().focusWindow(), "keys still belong to the pointer's window");
    }

    /**
     * The gesture a toolkit makes: it moves the focus to the page it just opened and closes the page
     * by unmapping it, so the revert - not the client - is what puts the focus back.
     *
     * <p>{@code RevertToParent} out of a child of the root lands on the root window, and from there
     * the keys have to reach the window under the pointer or the application's own window never sees
     * another keystroke: EasyShell's 片段 page closed this way on 2026-09-29 left its home page's
     * search box unable to accept input until an unrelated dialog was opened and closed.
     */
    @Test
    void afterTheFocusedWindowIsUnmappedTheKeysReachTheWindowUnderThePointer() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        // The page: a toplevel of its own, so the revert has nowhere to go but the root.
        XWindow page = window(server.screen(), server.rootWindow(), 0x400003, 200, 200, 20, 20);
        server.resources().add(page);
        page.setMapped(true);
        server.input().setInputFocus(page, InputState.REVERT_TO_PARENT);
        server.input().pointerMoved(60, 60);

        server.windows().unmapWindow(page, false);

        assertEquals(server.rootWindow(), server.input().inputFocus(), "the revert landed on the root");
        assertEquals(outer, server.input().focusWindow(), "and the keys reach the pointer's window");
    }


    /**
     * The click that lands in a window the focus is not already inside moves the focus there - the
     * window-manager half, and the one an application whose page left the focus on the root window
     * needs to get its menus back. **On by default**.
     *
     * <p>The window focused is the **toplevel** the click landed in, and the revert mode it is given is
     * a window manager's: {@code PointerRoot}, so a window that goes away afterwards hands the focus
     * to the window the pointer is in rather than leaving it on the root.
     */
    @Test
    void aClickMovesTheFocusToTheToplevelItLandsIn() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        // The focus on the root window: where a page that closed left it.
        server.input().setInputFocus(server.rootWindow(), InputState.REVERT_TO_PARENT, false);
        server.input().pointerMoved(60, 60);

        server.input().buttonChanged(1, true, 60, 60);

        assertEquals(outer, server.input().inputFocus(),
                "the click moved the focus into the window it landed in");
        assertEquals(InputState.REVERT_TO_POINTER_ROOT, server.input().focusRevertTo(),
                "and gave it the revert a window manager gives");
    }

    @Test
    void noFocusOnClickIsTheComparisonSwitch() {
        XServer server = serverWithTwoWindows("--no-focus-on-click");
        server.input().setInputFocus(server.rootWindow(), InputState.REVERT_TO_PARENT, false);
        server.input().pointerMoved(60, 60);

        server.input().buttonChanged(1, true, 60, 60);

        assertEquals(server.rootWindow(), server.input().inputFocus(),
                "with it off nothing moves the focus on a click - which is what a reference does");
    }

    /**
     * A click in an override-redirect window leaves the focus alone, so the window a popup belongs to
     * keeps it - which is what keeps the popup itself on screen and its items clickable.
     *
     * <p>A window manager never gives the input focus to an override-redirect window, and the cost of
     * doing it is measured: EasyShell's input box context menu opened, the press on an item moved the
     * focus from the window that owned the menu to the menu's own window, that owner was told
     * {@code FocusOut}, and the toolkit unmapped the popup three milliseconds later. Every item was
     * then dead to the mouse while the keyboard still activated them (A23, 2026-09-30).
     */
    @Test
    void aClickInsideAnOverrideRedirectWindowLeavesTheFocusOnItsOwner() {
        XServer server = serverWithTwoWindows();
        XWindow outer = (XWindow) server.resources().get(0x400001);
        // The popup: an override-redirect child of the root, which is what a menu is.
        XWindow menu = window(server.screen(), server.rootWindow(), 0x400003, 200, 200, 40, 40);
        menu.setOverrideRedirect(true);
        server.resources().add(menu);
        menu.setMapped(true);
        server.input().setInputFocus(outer, InputState.REVERT_TO_PARENT);
        server.input().pointerMoved(210, 210);

        server.input().buttonChanged(1, true, 210, 210);

        assertEquals(outer, server.input().inputFocus(),
                "the focus stays on the window the popup belongs to");
    }

    @Test
    void aFocusOfNoneSendsKeysNowhere() {
        XServer server = serverWithTwoWindows();
        // A focus of null is None, which is a state of its own: no window has the keyboard, so the
        // keys belong to none of them rather than to whichever one the pointer is in.
        server.input().setInputFocus(null, InputState.REVERT_TO_POINTER_ROOT);
        server.input().pointerMoved(60, 60);

        assertEquals(null, server.input().focusWindow(), "a focus of None names no window");
        assertEquals(null, server.input().inputFocus(), "and it is not PointerRoot either");
    }
}
