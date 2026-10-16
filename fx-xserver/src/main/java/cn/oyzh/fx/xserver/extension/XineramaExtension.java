package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xinerama.IsActive;
import com.github.moaxcp.x11.protocol.xinerama.IsActiveReply;
import com.github.moaxcp.x11.protocol.xinerama.QueryScreens;
import com.github.moaxcp.x11.protocol.xinerama.QueryScreensReply;
import com.github.moaxcp.x11.protocol.xinerama.QueryVersion;
import com.github.moaxcp.x11.protocol.xinerama.QueryVersionReply;
import com.github.moaxcp.x11.protocol.xinerama.ScreenInfo;
import com.github.moaxcp.x11.protocol.xinerama.XineramaPlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.extension.Extension;

/**
 * Xinerama, the smallest it can be: one screen.
 *
 * <p>Xinerama is how a client asks whether a display is several monitors acting as one, and the
 * answer here is always "no": this server exposes a single screen per client and has no way to span
 * more than one. That makes the extension three requests deep - the version, whether it is active,
 * and the list of screens - and all three answer honestly rather than being absent.
 *
 * <p><b>It exists because its absence segfaults a real client.</b> {@code xdotool} calls
 * {@code XineramaIsActive} as it starts; Xlib's own implementation of that asks for the extension and
 * does not survive being told there is none, so every {@code xdotool} subcommand died in
 * {@code xdo_new_with_opened_display} before it sent a request of its own. Measured against Xvfb,
 * which has the extension and where the same binary works. A server that says "there is one screen,
 * here it is" costs three small replies and takes a whole swathe of clients out of that hole: what
 * they are asking is a question every toolkit asks.
 *
 * <p>Deliberately not here: Xinerama 1.1's {@code GetState}, {@code GetScreenCount} and
 * {@code GetScreenSize} - they are for a client that already knows it is on a Xinerama display, which
 * no client of this one is. A client that asks for them gets {@code BadRequest} from the dispatcher,
 * which is the honest answer.
 */
public final class XineramaExtension implements Extension {

    /** Xinerama 1.1 is what Xlib's own helpers speak. */
    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 1;

    private final XineramaPlugin plugin = new XineramaPlugin();

    @Override
    public String name() {
        return "XINERAMA";
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
        if (request instanceof QueryVersion) {
            queryVersion(client);
        } else if (request instanceof IsActive) {
            isActive(client);
        } else if (request instanceof QueryScreens) {
            queryScreens(server, client);
        } else {
            throw new IllegalArgumentException("unhandled XINERAMA request " + request.getClass());
        }
    }

    /**
     * The reply is capped at the version the client asked for, as X defines it. A client that asks
     * for more than we have is told what we have, which is what the request is for.
     */
    private void queryVersion(ClientConnection client) {
        client.sendReply(QueryVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .major(MAJOR_VERSION)
                .minor(MINOR_VERSION)
                .build());
    }

    /** Whether the display is several screens acting as one. It is not, and saying so is the answer. */
    private void isActive(ClientConnection client) {
        client.sendReply(IsActiveReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .state(0)
                .build());
    }

    /**
     * The one screen, as Xinerama describes a screen: a rectangle at the origin of the display.
     *
     * <p>Answered even though {@link #isActive} says no, because a client is free to ask the list
     * without asking whether there is one - and one entry is the truthful list for a display that is
     * one screen.
     */
    private void queryScreens(XServer server, ClientConnection client) {
        ScreenInfo screen = ScreenInfo.builder()
                .xOrg((short) 0)
                .yOrg((short) 0)
                .width((short) server.screenOf(client).width())
                .height((short) server.screenOf(client).height())
                .build();
        client.sendReply(QueryScreensReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .screenInfo(org.eclipse.collections.impl.factory.Lists.immutable.of(screen))
                .build());
    }
}
