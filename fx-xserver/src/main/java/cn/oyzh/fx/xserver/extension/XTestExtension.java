package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xtest.CompareCursor;
import com.github.moaxcp.x11.protocol.xtest.CompareCursorReply;
import com.github.moaxcp.x11.protocol.xtest.FakeInput;
import com.github.moaxcp.x11.protocol.xtest.GetVersion;
import com.github.moaxcp.x11.protocol.xtest.GetVersionReply;
import com.github.moaxcp.x11.protocol.xtest.GrabControl;
import com.github.moaxcp.x11.protocol.xtest.XtestPlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.event.XInputMasks;
import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.resource.XCursor;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XTEST.
 *
 * <p>Lets a client inject synthetic input: {@code xdotool}, UI test suites and anything that drives
 * a desktop remotely need it. The events are delivered through exactly the same path as real input,
 * so a faked keystroke is indistinguishable from a typed one for every client on the server.
 *
 * <p>{@code FakeInput} is the whole point of the extension; {@code GrabControl} is accepted and
 * ignored because this server has no notion of an impervious grab, and the extension defines no
 * events and no errors of its own (the one error code reserved for it is never raised).
 */
public final class XTestExtension implements Extension {

    private static final Logger log = LoggerFactory.getLogger(XTestExtension.class);

    /** XTEST 2.2 is what the generated plugin reports and what clients expect. */
    private static final byte MAJOR_VERSION = 2;
    private static final short MINOR_VERSION = 2;

    /**
     * FakeInput event types. These are the core protocol's own event codes - KeyPress is 2 and
     * MotionNotify is 6 - not a numbering of XTEST's own, so a client's button press arrives as a
     * press rather than as whatever the next type along would have been.
     */
    private static final int KEY_PRESS = 2;
    private static final int KEY_RELEASE = 3;
    private static final int BUTTON_PRESS = 4;
    private static final int BUTTON_RELEASE = 5;
    private static final int MOTION_NOTIFY = 6;

    /** FakeInput detail for motion: absolute means "x and y are screen coordinates". */
    private static final int MOTION_ABSOLUTE = 0;
    private static final int MOTION_RELATIVE = 1;

    /** CompareCursor pseudo cursors: 0 is None, 1 asks for the cursor currently displayed. */
    private static final int CURSOR_NONE = 0;
    private static final int CURSOR_CURRENT = 1;

    private final XtestPlugin plugin = new XtestPlugin();

    @Override
    public String name() {
        return "XTEST";
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

    @Override
    public XProtocolPlugin plugin() {
        return plugin;
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        if (request instanceof GetVersion getVersion) {
            getVersion(client, getVersion);
        } else if (request instanceof CompareCursor compareCursor) {
            compareCursor(server, client, compareCursor);
        } else if (request instanceof FakeInput fakeInput) {
            fakeInput(server, client, fakeInput);
        } else if (request instanceof GrabControl grabControl) {
            grabControl(grabControl);
        } else {
            throw new IllegalArgumentException("unhandled XTEST request " + request.getClass());
        }
    }

    private void getVersion(ClientConnection client, GetVersion request) {
        client.sendReply(GetVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .majorVersion(MAJOR_VERSION)
                .minorVersion(MINOR_VERSION)
                .build());
    }

    private void compareCursor(XServer server, ClientConnection client, CompareCursor request) {
        XWindow window = (XWindow) server.resources().require(client, request.getWindow(),
                XResource.CLASS_WINDOW, false);
        XCursor cursor;
        int requested = request.getCursor();
        if (requested == CURSOR_NONE) {
            cursor = null;
        } else if (requested == CURSOR_CURRENT) {
            cursor = cursorFor(server, window);
        } else {
            cursor = (XCursor) server.resources().require(client, requested, XResource.CLASS_CURSOR,
                    false);
        }
        XCursor actual = cursorFor(server, window);
        boolean same = cursor != null && actual != null && cursor.id() == actual.id();
        client.sendReply(CompareCursorReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .same(same)
                .build());
    }

    private void fakeInput(XServer server, ClientConnection client, FakeInput request) {
        int type = Byte.toUnsignedInt(request.getType());
        int detail = Byte.toUnsignedInt(request.getDetail());
        int x = request.getRootX();
        int y = request.getRootY();
        // Synthetic input belongs to the screen of the client that asked for it, which is what makes
        // it land on that client's own windows rather than on another connection's screen.
        InputState input = server.inputFor(client);
        switch (type) {
            // Only a motion event carries a position: Xlib zeroes the coordinates for every other
            // type, and a real server delivers a key or button event without moving the pointer.
            // Passing the pointer's current position keeps it exactly where it is.
            //
            // **And it is the XTEST *slave* that produces it** (A48): XInput's source id is where a
            // client learns which device did this, and a reference answers 4/5 here - its XTEST
            // pointer and keyboard, which are the two devices this server now also has.
            case KEY_PRESS, KEY_RELEASE -> asXtest(input, XInputMasks.XTEST_KEYBOARD_DEVICE,
                    () -> input.keyChanged(detail, type == KEY_PRESS,
                            input.pointerX(), input.pointerY()));
            case BUTTON_PRESS, BUTTON_RELEASE -> asXtest(input, XInputMasks.XTEST_POINTER_DEVICE,
                    () -> input.buttonChanged(detail, type == BUTTON_PRESS,
                            input.pointerX(), input.pointerY()));
            case MOTION_NOTIFY -> {
                if (detail == MOTION_RELATIVE) {
                    // A relative motion is an offset from wherever the pointer is now.
                    asXtest(input, XInputMasks.XTEST_POINTER_DEVICE, () -> input.pointerMoved(
                            input.pointerX() + x, input.pointerY() + y));
                } else if (detail == MOTION_ABSOLUTE) {
                    asXtest(input, XInputMasks.XTEST_POINTER_DEVICE, () -> input.pointerMoved(x, y));
                } else {
                    log.debug("XTEST FakeInput: ignoring unknown motion detail {}", detail);
                }
            }
            default -> log.debug("XTEST FakeInput: ignoring unknown event type {}", type);
        }
    }

    /** Runs one faked event as the XTEST slave it came from, and gives the device back after it. */
    private static void asXtest(InputState input, int device, Runnable action) {
        input.setSourceDevice(device);
        try {
            action.run();
        } finally {
            input.setSourceDevice(0);
        }
    }

    private void grabControl(GrabControl request) {
        // An impervious grab would keep other clients from taking a server grab. This server has no
        // such mode, so the request is accepted and otherwise ignored.
        log.debug("XTEST GrabControl impervious={}: accepted and ignored", request.isImpervious());
    }

    /** The cursor a window shows: its own, or the closest ancestor's, or the root cursor. */
    private static XCursor cursorFor(XServer server, XWindow window) {
        for (XWindow candidate = window; candidate != null; candidate = candidate.parent()) {
            XCursor cursor = candidate.cursor();
            if (cursor != null) {
                return cursor;
            }
        }
        return server.rootCursor();
    }
}
