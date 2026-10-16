package cn.oyzh.fx.xserver.handler;

import com.github.moaxcp.x11.protocol.xproto.Bell;
import com.github.moaxcp.x11.protocol.xproto.ChangeHosts;
import com.github.moaxcp.x11.protocol.xproto.ChangeKeyboardControl;
import com.github.moaxcp.x11.protocol.xproto.ChangePointerControl;
import com.github.moaxcp.x11.protocol.xproto.ForceScreenSaver;
import com.github.moaxcp.x11.protocol.xproto.GetKeyboardControl;
import com.github.moaxcp.x11.protocol.xproto.GetKeyboardControlReply;
import com.github.moaxcp.x11.protocol.xproto.GetPointerControl;
import com.github.moaxcp.x11.protocol.xproto.GetPointerControlReply;
import com.github.moaxcp.x11.protocol.xproto.GetScreenSaver;
import com.github.moaxcp.x11.protocol.xproto.GetScreenSaverReply;
import com.github.moaxcp.x11.protocol.xproto.GrabServer;
import com.github.moaxcp.x11.protocol.xproto.Host;
import com.github.moaxcp.x11.protocol.xproto.Kb;
import com.github.moaxcp.x11.protocol.xproto.KillClient;
import com.github.moaxcp.x11.protocol.xproto.ListExtensions;
import com.github.moaxcp.x11.protocol.xproto.ListExtensionsReply;
import com.github.moaxcp.x11.protocol.xproto.ListHosts;
import com.github.moaxcp.x11.protocol.xproto.ListHostsReply;
import com.github.moaxcp.x11.protocol.xproto.QueryExtension;
import com.github.moaxcp.x11.protocol.xproto.QueryExtensionReply;
import com.github.moaxcp.x11.protocol.xproto.SetAccessControl;
import com.github.moaxcp.x11.protocol.xproto.SetCloseDownMode;
import com.github.moaxcp.x11.protocol.xproto.SetScreenSaver;
import com.github.moaxcp.x11.protocol.xproto.Str;
import com.github.moaxcp.x11.protocol.xproto.UngrabServer;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.display.ScreenSaverState;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.input.InputState;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.ByteLists;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Server level requests: extension lookup, screen saver and pointer/keyboard tuning, access
 * control, and the connection control requests.
 */
public final class MiscHandlers {

    private final XServer server;

    public MiscHandlers(XServer server) {
        this.server = server;
    }

    // ------------------------------------------------------------------
    // Extensions
    // ------------------------------------------------------------------

    public void queryExtension(ClientConnection client, QueryExtension r) {
        String name = ascii(r.getName());
        Extension extension = server.extensions().byName(name);
        // An extension an untrusted client may not see is not there, which is what a reference
        // answers (F7): the answer and the request's opcode have to agree, or a client that checks
        // first and then sends would be told yes and refused.
        if (extension != null && !client.isTrusted()
                && !cn.oyzh.fx.xserver.extension.SecurityExtension
                        .visibleToUntrusted(extension)) {
            extension = null;
        }
        boolean present = extension != null;
        client.sendReply(QueryExtensionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .present(present)
                .majorOpcode(present ? extension.majorOpcode() : (byte) 0)
                .firstEvent(present ? extension.firstEvent() : (byte) 0)
                .firstError(present ? extension.firstError() : (byte) 0)
                .build());
    }

    public void listExtensions(ClientConnection client, ListExtensions r) {
        List<Str> names = new ArrayList<>();
        for (String name : client.isTrusted() ? server.extensions().names()
                : visibleToUntrusted()) {
            names.add(Str.builder()
                    .name(ByteLists.immutable.of(name.getBytes(StandardCharsets.ISO_8859_1)))
                    .build());
        }
        client.sendReply(ListExtensionsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .names(Lists.immutable.withAll(names))
                .build());
    }

    /** The extensions an untrusted client may see - two on a reference, measured (F7). */
    private List<String> visibleToUntrusted() {
        List<String> names = new ArrayList<>();
        for (Extension extension : server.extensions().all()) {
            if (cn.oyzh.fx.xserver.extension.SecurityExtension.visibleToUntrusted(extension)) {
                names.add(extension.name());
            }
        }
        return names;
    }

    // ------------------------------------------------------------------
    // Connection control
    // ------------------------------------------------------------------

    public void grabServer(ClientConnection client, GrabServer r) {
        // The request is already running with the lock held; the grab takes one more hold so it
        // survives this request and blocks every other client until UngrabServer.
        server.acquireServerGrab(client);
    }

    public void ungrabServer(ClientConnection client, UngrabServer r) {
        // A client that never grabbed, or one that has already released every hold, is ignored.
        server.releaseServerGrab(client);
    }

    public void setCloseDownMode(ClientConnection client, SetCloseDownMode r) {
        int mode = r.getMode();
        if (mode < 0 || mode > 2) {
            throw ProtocolException.badValue(mode);
        }
        client.setCloseDownMode(mode);
    }

    public void killClient(ClientConnection client, KillClient r) {
        int resource = r.getResource();
        if (resource == 0) {
            // AllTemporary: drop resources a client asked to keep after disconnecting.
            for (ClientConnection other : server.clients()) {
                if (other != client && other.closeDownMode() != 2) {
                    continue;
                }
                server.resources().freeAll(other);
            }
            return;
        }
        for (ClientConnection other : server.clients()) {
            if (other != client && other.ownsIdRange(resource)) {
                other.close();
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Screen saver, pointer and keyboard tuning
    // ------------------------------------------------------------------

    public void bell(ClientConnection client, Bell r) {
        server.input().ringBell(r.getPercent());
    }

    public void getPointerControl(ClientConnection client, GetPointerControl r) {
        client.sendReply(GetPointerControlReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .accelerationNumerator((short) 2)
                .accelerationDenominator((short) 1)
                .threshold((short) 4)
                .build());
    }

    public void changePointerControl(ClientConnection client, ChangePointerControl r) {
        int numerator = r.getAccelerationNumerator();
        int denominator = r.getAccelerationDenominator();
        if (denominator == 0 && numerator != -1) {
            throw ProtocolException.badValue(denominator);
        }
        if (r.getThreshold() < -1) {
            throw ProtocolException.badValue(r.getThreshold());
        }
        // Accepted and ignored: pointer acceleration is a host concern, not the clients'.
    }

    /**
     * The settings the client last asked for, rather than constants.
     *
     * <p>A client that sets a timeout and reads it back has to see its own value, or it cannot tell
     * its own change took - the same rule {@code GetKeyboardControl} follows for auto-repeat. The
     * defaults are X's own (600 seconds), which is what a client that never calls
     * {@code SetScreenSaver} is entitled to.
     */
    public void getScreenSaver(ClientConnection client, GetScreenSaver r) {
        ScreenSaverState state = server.screenSaver();
        client.sendReply(GetScreenSaverReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .timeout((short) state.timeoutSeconds())
                .interval((short) state.intervalSeconds())
                .preferBlanking((byte) (state.preferBlanking() ? 1 : 0))
                .allowExposures((byte) (state.allowExposures() ? 1 : 0))
                .build());
    }

    /**
     * Keeps the settings, and does not act on them: what would blank is the host window, and on this
     * platform the system's own screen saver is what does it. They are kept because MIT-SCREEN-SAVER
     * reports them (and counts down against them), and because a client reads them back.
     */
    public void setScreenSaver(ClientConnection client, SetScreenSaver r) {
        if (r.getTimeout() < -1 || r.getInterval() < -1) {
            throw ProtocolException.badValue(r.getTimeout());
        }
        server.screenSaver().setSettings(Math.max(0, r.getTimeout()), Math.max(0, r.getInterval()),
                r.getPreferBlanking() != 0, r.getAllowExposures() != 0);
    }

    /**
     * The one screen-saver state change this server has, and the one that raises MIT-SCREEN-SAVER's
     * notification.
     *
     * <p>It does not blank: there is no saver window to map, and a client that subscribed is told the
     * state changed rather than told about a window that never appeared. That is the honest report for
     * a server whose display belongs to the host window.
     */
    public void forceScreenSaver(ClientConnection client, ForceScreenSaver r) {
        int mode = r.getMode();
        if (mode != 0 && mode != 1) {
            throw ProtocolException.badValue(mode);
        }
        boolean activate = mode == 1;
        server.screenSaver().setActive(activate);
        int state = server.screenSaver().state();
        for (ClientConnection subscriber : server.clients()) {
            if (server.screenSaverExtension().wantsNotifications(subscriber)) {
                server.screenSaverExtension().notifyState(server, subscriber,
                        server.screenOf(subscriber).root().id(), state, (byte) 0, true);
            }
        }
    }

    public void getKeyboardControl(ClientConnection client, GetKeyboardControl r) {
        InputState input = server.inputFor(client);
        // One bit per keycode, least significant bit of each byte first. The answer is what the
        // client actually set, not a blanket "everything repeats": a client that turned a key off
        // and reads it back has to see that it is off, or it cannot tell its own change took. The
        // bitmap and the global flag are two different things - measured against Xvfb, `xset r off`
        // clears the flag and leaves the bitmap alone - so they are reported separately.
        byte[] autoRepeats = input.autoRepeatBitmap();
        client.sendReply(GetKeyboardControlReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .globalAutoRepeat((byte) (input.repeatKeysEnabled() ? 1 : 0))
                .ledMask(0)
                .keyClickPercent((byte) 0)
                .bellPercent((byte) 50)
                .bellPitch((short) 400)
                .bellDuration((short) 100)
                .autoRepeats(ByteLists.immutable.of(autoRepeats))
                .build());
    }

    /**
     * {@code ChangeKeyboardControl}: only the auto-repeat mode is acted on.
     *
     * <p>LEDs and the bell are accepted and ignored - there is no host keyboard to light up - but
     * repeat has to be honoured, because it is how a client says "this key must not repeat": the
     * server is what generates the repeats, so a client that turns one off and is ignored gets a run
     * of characters it explicitly asked not to have. The value travels as one four byte word: the
     * mode in the first byte, then up to three keycodes - and no keycode at all means the whole
     * keyboard's repeat, which is a different thing from every key's bit (see
     * {@link InputState#setAutoRepeat}, where the measurement is).
     */
    public void changeKeyboardControl(ClientConnection client, ChangeKeyboardControl r) {
        if (!Kb.AUTO_REPEAT_MODE.isEnabled(r.getValueMask())) {
            return;
        }
        int packed = r.getAutoRepeatMode();
        boolean on = (packed & 0xFF) != 0;              // 2 (Default) means the same as 1 (On)
        InputState input = server.inputFor(client);
        int[] keycodes = {(packed >> 8) & 0xFF, (packed >> 16) & 0xFF, (packed >> 24) & 0xFF};
        if (keycodes[0] == 0) {
            input.setAutoRepeat(-1, on);
            return;
        }
        for (int keycode : keycodes) {
            if (keycode == 0) {
                break;
            }
            input.setAutoRepeat(keycode, on);
        }
    }

    // ------------------------------------------------------------------
    // Access control
    // ------------------------------------------------------------------

    public void changeHosts(ClientConnection client, ChangeHosts r) {
        // Host based access control is not what this server does: the display is either open or it
        // wants a cookie, and a host rule would not change that. A reference answers `BadAccess`
        // when access control is on and accepts the request when it is off (`-ac`), which is what
        // this does - accepting it under --auth would tell a client its rule was added when nothing
        // was (F7's probe measures both answers).
        refuseHostRules();
    }

    public void listHosts(ClientConnection client, ListHosts r) {
        List<Host> hosts = new ArrayList<>();
        if (server.options().listenAll()) {
            hosts.add(Host.builder()
                    .family((byte) 0)
                    .address(ByteLists.immutable.empty())
                    .build());
        }
        client.sendReply(ListHostsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .mode((byte) (server.options().authRequired() ? 0 : 1))
                .hosts(Lists.immutable.withAll(hosts))
                .build());
    }

    public void setAccessControl(ClientConnection client, SetAccessControl r) {
        if (r.getMode() != 0 && r.getMode() != 1) {
            throw ProtocolException.badValue(r.getMode());
        }
        refuseHostRules();
    }

    /** The refusal {@code ChangeHosts}/{@code SetAccessControl} deserve when a cookie is required. */
    private void refuseHostRules() {
        if (server.options().authRequired()) {
            throw new ProtocolException(ProtocolException.ACCESS, 0,
                    "host based access control is not available: this display authenticates with"
                            + " cookies (--auth)");
        }
    }

    private static String ascii(org.eclipse.collections.api.list.primitive.ByteList bytes) {
        return new String(bytes.toArray(), StandardCharsets.ISO_8859_1);
    }
}
