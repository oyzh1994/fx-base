package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.screensaver.QueryInfo;
import com.github.moaxcp.x11.protocol.screensaver.QueryInfoReply;
import com.github.moaxcp.x11.protocol.screensaver.QueryVersion;
import com.github.moaxcp.x11.protocol.screensaver.QueryVersionReply;
import com.github.moaxcp.x11.protocol.screensaver.ScreensaverPlugin;
import com.github.moaxcp.x11.protocol.screensaver.SelectInput;
import com.github.moaxcp.x11.protocol.screensaver.SetAttributes;
import com.github.moaxcp.x11.protocol.screensaver.Suspend;
import com.github.moaxcp.x11.protocol.screensaver.UnsetAttributes;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.ScreenSaverState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MIT-SCREEN-SAVER: the screen saver's state, as a client asks for it.
 *
 * <p>Without it a client that wants to say "do not blank while this plays" is told the extension is
 * missing, which is the answer a video player, a presentation tool and a remote-desktop client all
 * act on. What this server can honestly offer is the settings and the state: it keeps the timeout and
 * the preferences a client sets ({@link ScreenSaverState}), reports them back, and tells a subscriber
 * when the state changes. It does not blank anything - the host window owns the display, and on this
 * platform the system's own screen saver is what blanks it - so `QueryInfo`'s saver window is
 * {@code None} here where a server that paints a black window over the screen answers with that
 * window's id.
 *
 * <p><b>The version is 1.1 and that is the reference's answer, not a guess</b> (measured against Xvfb
 * with {@code tools/ScreenSaverProbe}): a client that reads the version and then asks for
 * {@code QueryInfo} gets the fields it expects.
 */
public final class ScreenSaverExtension implements Extension {

    /** Measured against a reference server: it answers 1.1 to a client that asks for 1.1. */
    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 1;

    /** {@code SelectInput}'s mask bits, as the extension numbers them. */
    private static final int NOTIFY_MASK = 0x1;

    private final ScreensaverPlugin plugin = new ScreensaverPlugin();

    /** The clients that asked to be told when the state changes, and the mask each asked with. */
    private final Map<ClientConnection, Integer> subscriptions = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "MIT-SCREEN-SAVER";
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
        ScreenSaverState state = server.screenSaver();
        if (request instanceof QueryVersion) {
            queryVersion(client);
        } else if (request instanceof QueryInfo r) {
            queryInfo(server, client, r, state);
        } else if (request instanceof SelectInput r) {
            subscriptions.put(client, r.getEventMask());
        } else if (request instanceof Suspend r) {
            state.setSuspended(r.getSuspend() != 0);
        } else if (request instanceof SetAttributes || request instanceof UnsetAttributes) {
            // These configure the *saver window* - where it is, how big, what it draws. There is no
            // saver window here (the host window is what shows), so there is nothing to configure;
            // refusing them would be worse than accepting them, since a client does not expect a
            // screen saver to refuse to be positioned.
        } else {
            throw new IllegalArgumentException("unhandled MIT-SCREEN-SAVER request "
                    + request.getClass());
        }
    }

    @Override
    public void clientDisconnected(XServer server, ClientConnection client) {
        subscriptions.remove(client);
    }

    /**
     * Tells the subscribers that the saver's state changed.
     *
     * <p>Raised from the core {@code ForceScreenSaver}, which is the one way the state changes here.
     * The event's own {@code window} field is the drawable the client subscribed with, which is how a
     * client that watches one window tells its notifications from another client's.
     */
    public void notifyState(XServer server, ClientConnection subscriber, int window, int state,
                            byte kind, boolean forced) {
        server.events().send(subscriber, com.github.moaxcp.x11.protocol.screensaver.NotifyEvent
                .builder()
                .firstEventOffset(firstEvent())
                .sentEvent(false)
                .sequenceNumber(subscriber.sequenceNumber())
                .state((byte) state)
                .time(server.events().time())
                .root(server.screenOf(subscriber).root().id())
                .window(window)
                .kind(kind)
                .forced(forced)
                .build());
    }

    /** Whether a client asked to hear about state changes. */
    public boolean wantsNotifications(ClientConnection client) {
        Integer mask = subscriptions.get(client);
        return mask != null && (mask & NOTIFY_MASK) != 0;
    }

    private void queryVersion(ClientConnection client) {
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .serverMajorVersion(MAJOR_VERSION)
                .serverMinorVersion(MINOR_VERSION)
                .build());
    }

    /**
     * The state, and the two countdowns a client uses to decide whether it needs to act.
     *
     * <p>{@code saverWindow} is {@code None}: the honest answer for a server that does not paint a
     * saver window. The countdowns come from the timeout a client set and the time of the last input,
     * so they are numbers a client can plan around rather than constants.
     */
    private void queryInfo(XServer server, ClientConnection client, QueryInfo r,
                           ScreenSaverState state) {
        client.sendReply(QueryInfoReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .state((byte) state.state())
                .saverWindow(0)
                .msUntilServer((int) state.millisUntilSaver())
                .msSinceUserInput((int) state.millisSinceInput())
                .eventMask(subscriptions.getOrDefault(client, 0))
                .kind((byte) 0)                         // Blanked
                .build());
    }

}
