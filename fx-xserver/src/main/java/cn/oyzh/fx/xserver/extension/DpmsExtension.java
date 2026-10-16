package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.dpms.Capable;
import com.github.moaxcp.x11.protocol.dpms.CapableReply;
import com.github.moaxcp.x11.protocol.dpms.Disable;
import com.github.moaxcp.x11.protocol.dpms.DpmsPlugin;
import com.github.moaxcp.x11.protocol.dpms.Enable;
import com.github.moaxcp.x11.protocol.dpms.ForceLevel;
import com.github.moaxcp.x11.protocol.dpms.GetTimeouts;
import com.github.moaxcp.x11.protocol.dpms.GetTimeoutsReply;
import com.github.moaxcp.x11.protocol.dpms.GetVersion;
import com.github.moaxcp.x11.protocol.dpms.GetVersionReply;
import com.github.moaxcp.x11.protocol.dpms.Info;
import com.github.moaxcp.x11.protocol.dpms.InfoReply;
import com.github.moaxcp.x11.protocol.dpms.SetTimeouts;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.display.ScreenSaverState;
import cn.oyzh.fx.xserver.extension.Extension;

/**
 * DPMS: the energy-saving state a client can ask about and ask to change.
 *
 * <p>It is advertised because its absence is an error a client acts on - {@code xset dpms} prints
 * "extension not present", and a client that checks whether it may power the display down has to guess
 * when the extension is missing. What it answers is the honest truth about this display:
 *
 * <ul>
 *   <li><b>{@code Capable} is no.</b> The display is the host window on a machine whose own power
 *       management is the system's business, and a server that claims to be able to put it into
 *       standby and then does not is worse than one that says it cannot: a client that checks
 *       capability skips its DPMS logic, which is exactly right here. This is the same shape as
 *       {@code XINERAMA}'s {@code IsActive} answering no.
 *   <li><b>The timeouts and the enable flag are kept and reported back</b>, so a client that sets them
 *       and reads them sees its own values rather than defaults it cannot explain.
 *   <li><b>{@code ForceLevel} is recorded and does not blank anything.</b> Nothing here paints a black
 *       screen, for the same reason B7's screen saver does not.
 * </ul>
 *
 * <p><b>There is no reference for this one, and the item records it</b>: Xvfb does not have DPMS at
 * all (measured - {@code QueryExtension} answers 0), so every reply here follows the protocol
 * description and the two version numbers the extension has ever had, rather than a diff against
 * another server.
 */
public final class DpmsExtension implements Extension {

    /**
     * DPMS 1.2 is what every server since 1999 has answered; the extension has not changed since.
     */
    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 2;

    private final DpmsPlugin plugin = new DpmsPlugin();

    @Override
    public String name() {
        return "DPMS";
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
        if (request instanceof GetVersion) {
            getVersion(client);
        } else if (request instanceof Capable) {
            capable(client);
        } else if (request instanceof GetTimeouts) {
            getTimeouts(client, state);
        } else if (request instanceof SetTimeouts r) {
            state.setDpmsTimeouts(Short.toUnsignedInt(r.getStandbyTimeout()),
                    Short.toUnsignedInt(r.getSuspendTimeout()),
                    Short.toUnsignedInt(r.getOffTimeout()));
        } else if (request instanceof Enable) {
            state.setDpmsEnabled(true);
        } else if (request instanceof Disable) {
            state.setDpmsEnabled(false);
        } else if (request instanceof ForceLevel r) {
            // Recorded rather than acted on: there is no power level to change, and a client that
            // reads it back sees the level it asked for instead of one that never moved.
            state.setPowerLevel(Short.toUnsignedInt(r.getPowerLevel()));
        } else if (request instanceof Info) {
            info(client, state);
        } else {
            throw new IllegalArgumentException("unhandled DPMS request " + request.getClass());
        }
    }

    private void getVersion(ClientConnection client) {
        client.sendReply(GetVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .serverMajorVersion(MAJOR_VERSION)
                .serverMinorVersion(MINOR_VERSION)
                .build());
    }

    /** Whether the display can be powered down by this server. It cannot. */
    private void capable(ClientConnection client) {
        client.sendReply(CapableReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .capable(false)
                .build());
    }

    private void getTimeouts(ClientConnection client, ScreenSaverState state) {
        client.sendReply(GetTimeoutsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .standbyTimeout((short) state.standbySeconds())
                .suspendTimeout((short) state.suspendSeconds())
                .offTimeout((short) state.offSeconds())
                .build());
    }

    private void info(ClientConnection client, ScreenSaverState state) {
        client.sendReply(InfoReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .powerLevel((short) state.powerLevel())
                .state(state.dpmsEnabled())
                .build());
    }
}
