package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xkb.GetControls;
import com.github.moaxcp.x11.protocol.xkb.GetMap;
import com.github.moaxcp.x11.protocol.xkb.GetState;
import com.github.moaxcp.x11.protocol.xkb.LatchLockState;
import com.github.moaxcp.x11.protocol.xkb.PerClientFlags;
import com.github.moaxcp.x11.protocol.xkb.SetControls;
import com.github.moaxcp.x11.protocol.xkb.UseExtension;
import com.github.moaxcp.x11.protocol.xkb.XkbPlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.input.InputState;
import cn.oyzh.fx.xserver.input.Keymap;
import cn.oyzh.fx.xserver.input.XkbMap;
import cn.oyzh.common.log.JulLog;

import java.io.IOException;
import java.util.List;

/**
 * XKEYBOARD, as much of it as a client needs before it will believe the keyboard is real.
 *
 * <p><b>It exists because its absence segfaults a real client</b>, and this time the client does not
 * get far enough to fall back. {@code xdotool} builds its character map from {@code XkbGetMap}'s
 * answer and does not check it for null; Xlib's {@code XkbGetMap} returns null when the extension is
 * not there, so the first thing xdo does with it - {@code xkb->map->key_sym_map[i]} - faults. Every
 * subcommand died in {@code xdo_new_with_opened_display}, including ones that need nothing from the
 * keyboard. Measured with a gdb backtrace and a trace of the XKB entry points; the same binary works
 * against Xvfb, which has the extension. Xinerama's absence had the same shape and was ruled out
 * first (B3).
 *
 * <p>What is here is the requests a client makes on the way to a usable keyboard, and no more:
 *
 * <ul>
 *   <li>{@code UseExtension}: the version handshake, which is what makes Xlib's own helpers proceed.
 *   <li>{@code GetMap}: the keymap. {@link XkbMap} builds it from {@link cn.oyzh.fx.xserver.input.Keymap},
 *       so what a client reads here and what it reads from {@code GetKeyboardMapping} cannot disagree.
 *   <li>{@code GetState}: the modifier state, as the mask this server is delivering events with.
 *   <li>{@code SelectEvents}: accepted and not answered, because this server sends no XKB events.
 *   <li>{@code SetMap}: the keysyms of a map a client compiled, applied to the one table both rails
 *       read - which is what lets {@code xkbcomp} put a chosen layout on this keyboard.
 *   <li>{@code GetControls}: the keyboard's controls - the repeat delay, the rate, whether repeat is
 *       on, and the per-key bitmap - which is the only place a client can read them.
 *   <li>{@code SetControls}: how a client *changes* them ({@code XkbSetAutoRepeatRate} is this
 *       request), which is what A11 left open: the core protocol has no request for the delay or the
 *       rate at all.
 *   <li>{@code GetKbdByName}: a layout <em>chosen by name</em>, which is what a display server does
 *       instead of running a compiler - the tables are captured off a reference (see B1) and the
 *       reply carries the map, so {@code setxkbmap} is satisfied without one.
 *   <li>{@code GetNames}: which components are loaded, answered with the one name this server can
 *       honestly report - the symbols component the client asked for.
 * </ul>
 *
 * <p><b>What is not here</b>, deliberately and visibly: the map's actions, behaviours, explicit
 * components and virtual-modifier map (a {@code GetMap} reply simply does not claim them, and a
 * {@code SetMap} request carrying them has them dropped); the compatibility map, the indicator names
 * and the XKB events; and every keyboard control other than repeat - slow keys, bounce keys, sticky
 * keys, mouse keys, AccessX, the internal modifier maps - each of which {@code SetControls} accepts
 * and does not apply, and {@code GetControls} reports as not enabled. {@code SetNames} is accepted and
 * dropped like the other three a keymap compiler sends before {@code SetMap}, so a client that *sets*
 * names and reads them back gets the symbols name from its own {@code GetKbdByName} rather than the
 * one it set.
 *
 * <p>The request bodies are decoded with the generated classes, which are correct for every request
 * here (they are fixed size and read positionally); the <em>replies</em> are written as bytes,
 * because XKB's replies carry arrays whose shapes the generated writer does not reproduce.
 *
 * <p><b>Its event range is reserved even though this server sends no XKB event</b> (see
 * {@code XServer.registerExtensions}): libX11 installs its XKB wire-to-event converter over the
 * whole range a server advertises, and a base of zero would put it on top of the core events that
 * every client reads. A reference answers base 85; measured 2026-10-03 while fixing A18, which is
 * the same trap under libXi and XInput.
 */
public final class XkbExtension implements Extension {


    /** Xlib speaks 1.0 to a server that has the extension and 1.0 is all it needs of it. */
    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 0;

    /** {@code XkbUseCoreKbd}; the only keyboard there is. */
    private static final int CORE_KEYBOARD = 0x0100;

    /**
     * {@code XkbRepeatKeysMask}: the one control bit this server honours, and it is the one a
     * keyboard has by default - a reference server's {@code enabledControls} carries it and twelve
     * more.
     */
    private static final int REPEAT_KEYS = 0x1;

    /**
     * The components a {@code GetKbdByName} reply says are in it: {@code XkbGBN_TypesMask},
     * {@code XkbGBN_ClientSymbolsMask} and {@code XkbGBN_ServerSymbolsMask}. Those are exactly the
     * parts {@link XkbMap}'s reply carries, and a client reads one embedded reply per bit it names,
     * so claiming a part this server does not send is how a client ends up reading the wrong bytes.
     */
    private static final int REPORTED_PARTS = 0x01 | 0x04 | 0x08;

    /**
     * The component-name bits this server can answer: {@code XkbSymbolsNameMask} and
     * {@code XkbPhysSymbolsNameMask} - {@code XkbComponentNamesMask} is 0x3f, and only these two of
     * it are names this server has.
     *
     * <p>What the symbols component was asked for is the client's own statement of the keymap it
     * wants loaded, and the one component this server acts on, so it is the one name that is true.
     * The rest are not claimed: answering the request's keycodes or compat strings would report this
     * server's own tables under a name that describes somebody else's, and the client would go
     * looking for a component that is not here.
     */
    private static final int REPORTABLE_NAMES = 0x04 | 0x08;

    /**
     * The symbols component a by-name load was asked for, e.g. {@code pc+de+inet(evdev)}, and the
     * one name this server can honestly report from {@code GetNames}.
     *
     * <p>Per-server state on purpose: the keymap tables are global, but the name describes what a
     * client asked *this* server to load, and the two are set by the same request.
     */
    private volatile String symbolsName;

    private final XkbPlugin plugin = new XkbPlugin();

    @Override
    public String name() {
        return "XKEYBOARD";
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
    public XRequest decodeRequest(int minorOpcode, com.github.moaxcp.x11.protocol.X11Input in)
            throws IOException {
        return switch (minorOpcode) {
            case 0 -> UseExtension.readUseExtension(in);
            // SelectEvents (1) and the four requests a keymap compiler makes on its way to SetMap -
            // SetCompatMap (11), SetIndicatorMap (14), SetNames (18), SetGeometry (20) - are decoded
            // only as far as the opcode: they carry tables this server has no place for, and the body
            // is already buffered by the connection, so nothing has to be read to stay in step.
            case 1, 11, 14, 18, 20 -> new Ignored(minorOpcode);
            case 4 -> GetState.readGetState(in);
            case 5 -> LatchLockState.readLatchLockState(in);
            case 6 -> GetControls.readGetControls(in);
            case 7 -> SetControls.readSetControls(in);
            case 8 -> GetMap.readGetMap(in);
            case 9 -> SetMapRequest.read(in);
            case 17 -> GetNamesRequest.read(in);
            case 21 -> PerClientFlags.readPerClientFlags(in);
            // 23 is GetKbdByName, "load this keymap for me": the one request that asks the SERVER to
            // compile a keymap from a rules file, which is what a server-side xkbcomp does. This
            // server has no compiler, so the reply below says so.
            case 23 -> GetKbdByName.readGetKbdByName(in);
            default -> null;
        };
    }

    @Override
    public void handleRequest(XServer server, ClientConnection client, XRequest request) {
        switch (request) {
            case UseExtension r -> useExtension(client);
            case GetState r -> getState(server, client);
            case GetControls r -> getControls(server, client);
            case SetControls r -> setControls(server, client, r);
            case GetMap r -> getMap(client, r);
            case GetNamesRequest r -> getNames(server, client, r);
            case Ignored r -> ignored(client, r);
            case SetMapRequest r -> setMap(server, client, r);
            case LatchLockState r -> latchLockState(client, r);
            case PerClientFlags r -> perClientFlags(client, r);
            case GetKbdByName r -> getKbdByName(server, client, r);
            default -> throw ProtocolException.unimplemented(
                    "XKEYBOARD request " + request.getClass().getSimpleName());
        }
    }

    /** The version handshake: "supported", and the version this server has. */
    private void useExtension(ClientConnection client) {
        byte[] bytes = new byte[32];
        bytes[0] = 1;
        bytes[1] = 1;                               // supported
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard16(bytes, 8, MAJOR_VERSION, client.isBigEndian());
        putCard16(bytes, 10, MINOR_VERSION, client.isBigEndian());
        send(client, bytes);
    }

    /**
     * The keyboard's state: the modifier mask this server delivers events with, and nothing else.
     *
     * <p>The distinction the reply draws is between modifiers that are *held* and ones that are
     * *locked*. This server has one mask (same as the core events carry) and no separate notion of a
     * latch, so the mask goes in both the current and the base field and the locked field stays
     * empty. The group is always zero because there is one layout.
     */
    private void getState(XServer server, ClientConnection client) {
        int mods = server.inputFor(client).modifierState() & 0xFF;
        byte[] bytes = new byte[32];
        bytes[0] = 1;
        bytes[1] = (byte) XkbMap.CORE_KEYBOARD_DEVICE;
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        bytes[8] = (byte) mods;                     // mods
        bytes[9] = (byte) mods;                     // baseMods
        bytes[10] = 0;                              // latchedMods
        bytes[11] = 0;                              // lockedMods
        bytes[12] = 0;                              // group
        bytes[13] = 0;                              // lockedGroup
        bytes[14] = 0;                              // baseGroup
        bytes[15] = 0;
        bytes[16] = 0;                              // latchedGroup
        bytes[17] = 0;
        putCard16(bytes, 24, server.inputFor(client).pointerState(), client.isBigEndian());
        send(client, bytes);
    }

    /**
     * {@code GetControls}: the keyboard's controls, of which this server has one - repeat, its delay,
     * its rate and the per-key bitmap.
     *
     * <p><b>The layout was read off a reference reply, not derived.</b> The reply is 92 bytes, the
     * long one of the extension: the two settings are at 20 and 22 - which is where changing the rate
     * through {@code xset r rate 200 50} moves the bytes, a two byte field each - the enabled-controls
     * word is at 56, and the per-key bitmap is the last thirty two bytes, one bit per keycode. A field
     * in the wrong place is not an error here, it is a plausible wrong number, so the probe
     * ({@code XkbProbe}, which prints the same fields for both servers) is what checks it.
     *
     * <p><b>The controls this server does not implement are reported as not implemented</b> rather
     * than with the reference's own numbers: {@code enabledControls} carries
     * {@code XkbRepeatKeysMask} and nothing else - no slow keys, no mouse keys, no AccessX - and the
     * delays that belong to them are zero. Publishing a slow-keys delay of 300 ms while nothing on
     * that path is implemented would describe behaviour this server does not have (the same rule
     * DPMS's {@code Capable} and Xinerama's {@code IsActive} follow).
     */
    private void getControls(XServer server, ClientConnection client) {
        InputState input = server.inputFor(client);
        byte[] bytes = new byte[92];
        bytes[0] = 1;
        bytes[1] = (byte) XkbMap.CORE_KEYBOARD_DEVICE;
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(bytes, 4, 15, client.isBigEndian());      // length: (92 - 32) / 4
        bytes[8] = 1;                                       // mouseKeysDfltBtn: the stock default
        bytes[9] = 1;                                       // numGroups: there is one layout
        bytes[10] = 1;                                      // groupsWrap: XkbWrapIntoRange
        // 11..19: the internal and ignore-lock modifier pairs, which are zero on a stock server too.
        putCard16(bytes, 20, input.repeatDelayMillis(), client.isBigEndian());
        putCard16(bytes, 22, input.repeatIntervalMillis(), client.isBigEndian());
        putCard32(bytes, 56, input.repeatKeysEnabled() ? REPEAT_KEYS : 0, client.isBigEndian());
        System.arraycopy(input.autoRepeatBitmap(), 0, bytes, 60, 32);
        send(client, bytes);
    }

    /**
     * {@code SetControls}: how a client changes the repeat - {@code XkbSetAutoRepeatRate} and
     * {@code XkbSetAutoRepeatControls} are both this request, and it is the only way to reach the
     * delay and the rate at all, which is what A11 left open.
     *
     * <p>Three things are applied, and each was measured against Xvfb first:
     *
     * <ul>
     *   <li>{@code changeControls}'s RepeatKeys bit means the request's repeat delay and interval are
     *       to be taken. Measured: a request with that bit and 250/30 changed what GetControls
     *       answered on the reference.
     *   <li>{@code affectEnabledControls}'s RepeatKeys bit means the keyboard's repeat is being
     *       turned on or off, according to the same bit in {@code enabledControls}. Measured: the
     *       reference's {@code enabledControls} went from {@code 0x13a1} to {@code 0x13a0} and back,
     *       with the per-key bitmap untouched either way.
     *   <li><b>The per-key bitmap in the request is ignored, because the reference ignores it.</b>
     *       Measured three ways - all zeros, all ones, and alongside a repeat-rate change - and
     *       Xvfb's bitmap never moved, so per-key repeat stays the core protocol's business
     *       ({@code ChangeKeyboardControl}), where this server has had it since A11. What is logged
     *       rather than silently dropped, because a client that sends one means it.
     * </ul>
     *
     * <p>Everything else in the request - slow keys, bounce keys, sticky keys, mouse keys, AccessX,
     * the internal modifier maps, the group wrap - is read and not applied, since none of it is
     * implemented here. There is no reply: a client reads the result back through GetControls, which
     * is also how a change with no effect (a control this server does not have) is told apart from
     * one that took.
     */
    private void setControls(XServer server, ClientConnection client, SetControls request) {
        InputState input = server.inputFor(client);
        boolean enabledChanged = (request.getAffectEnabledControls() & REPEAT_KEYS) != 0;
        if (enabledChanged) {
            input.setRepeatKeysEnabled((request.getEnabledControls() & REPEAT_KEYS) != 0);
        }
        boolean rateChanged = (request.getChangeControls() & REPEAT_KEYS) != 0;
        if (rateChanged) {
            input.setRepeatRate(request.getRepeatDelay() & 0xFFFF,
                    request.getRepeatInterval() & 0xFFFF);
        }
        JulLog.debug("{} set XKB controls: enabledControls 0x{} (repeat {}), changeControls 0x{},"
                        + " repeat delay {} interval {}; the per-key bitmap and every other control"
                        + " are ignored, as the reference ignores them",
                client, Integer.toHexString(request.getEnabledControls()),
                input.repeatKeysEnabled(), Integer.toHexString(request.getChangeControls()),
                input.repeatDelayMillis(), input.repeatIntervalMillis());
    }

    /**
     * The keymap. The request's slicing fields are honoured for the three parts this server has -
     * a client that asks for a range gets that range - and the parts it does not have are simply not
     * claimed in the reply's {@code present}.
     */
    private void getMap(ClientConnection client, GetMap request) {
        byte[] bytes = XkbMap.getMapReply(
                request.getFull() & 0xFFFF,
                request.getFirstType() & 0xFF, request.getNTypes() & 0xFF,
                request.getFirstKeySym() & 0xFF, request.getNKeySyms() & 0xFF,
                request.getFirstModMapKey() & 0xFF, request.getNModMapKeys() & 0xFF,
                client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        send(client, bytes);
    }

    /**
     * A client asking to lock or latch a modifier, or to switch group. Accepted, and that is all.
     *
     * <p>This server's modifier state is the mask the host's own keyboard produces: there is one
     * keyboard, it is the machine's, and a client that locks Caps Lock here would be locking it for
     * every other client and for the user as well. So the request is not refused - xdotool sends one
     * as part of typing a key sequence and a refusal is an X error that ends the process - and it is
     * not applied either. Group locking has nothing to lock: there is one layout.
     */
    private void latchLockState(ClientConnection client, LatchLockState request) {
        JulLog.debug("{} asked to latch modifiers (locks 0x{} latches 0x{} group {}): there is one"
                        + " keyboard and it is the host's", client,
                Integer.toHexString(request.getModLocks() & 0xFF),
                Integer.toHexString(request.getAffectModLatches() & 0xFF),
                request.isLockGroup());
    }

    /**
     * {@code PerClientFlags}: what a client is allowed to change for itself, and the answer is
     * "nothing".
     *
     * <p>The flags are {@code DetectableAutoRepeat}, {@code GrabsUseXKBState},
     * {@code AutoResetControls}, {@code LookupStateWhenGrabbed} and {@code SendEventUsesXKBState} -
     * the ways a client asks a server to treat it differently from the others. A reference server
     * supports all five (`supported = 0x1f`, measured with {@code XkbProbe}); this one supports none,
     * and says so rather than being silent. That matters because a client asks: JavaFX sends this
     * request as EasyShell starts, and until it is answered the client gets a protocol **error**
     * instead of a reply - which is the difference between "this server cannot do that" and "this
     * request was malformed".
     *
     * <p>Detectable auto-repeat is the one that would have to come first if any of them are ever
     * implemented: it changes the *shape* of the events the server sends (a release-then-press pair
     * per repeat instead of a run of presses) and A11's repeat timer would have to consult the flag
     * per client.
     */
    private void perClientFlags(ClientConnection client, PerClientFlags request) {
        JulLog.debug("{} asked for per-client XKB flags 0x{}: none are supported", client,
                Integer.toHexString(request.getChange()));
        byte[] bytes = new byte[32];
        bytes[0] = 1;
        bytes[1] = (byte) XkbMap.CORE_KEYBOARD_DEVICE;
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        // supported, value, autoCtrls and autoCtrlsValues are all zero: this server can change
        // nothing on a client's behalf, and has no automatic controls to report.
        send(client, bytes);
    }

    /**
     * {@code SetMap}: a client replacing the keymap, which is what {@code xkbcomp} sends after it has
     * compiled one.
     *
     * <p><b>What is applied is the keysyms</b>, keycode by keycode, through the same table
     * {@code GetKeyboardMapping} and this extension's {@code GetMap} answer from - so a client that
     * sets a layout gets it back from both rails, and clients are told with the core
     * {@code MappingNotify} the way {@code ChangeKeyboardMapping} tells them. Nothing else in the
     * request is kept: the types, the actions, the behaviours, the explicit components, the
     * virtual-modifier map and the real-modifier map are all read and dropped, because this server
     * has no place for them - it has four types of its own and one modifier table, and both are what
     * {@link XkbMap} publishes. A client that sets a map with types this server cannot represent
     * gets the characters right and everything else as it was.
     *
     * <p><b>Measured, and this is the tool that matters</b>: {@code xkbcomp <file> <display>} sends
     * SetMap (and the four requests above), which is the path to a chosen layout on a server with no
     * compiler in it. {@code setxkbmap} does not - it asks {@code GetKbdByName}, which wants the
     * server to run the compiler - so this item's "setxkbmap is still a no-op" was the wrong tool to
     * measure by; see the backlog entry.
     */
    private void setMap(XServer server, ClientConnection client, SetMapRequest request) {
        int first = request.firstKeySym();
        int keycode = first;
        int changed = 0;
        for (int[] keysyms : request.syms()) {
            if (keysyms.length > 0 && keycode >= (Keymap.MIN_KEYCODE & 0xFF)
                    && keycode <= Keymap.MAX_KEYCODE) {
                Keymap.assign(keycode, keysyms);
                changed++;
            }
            keycode++;
        }
        if (changed > 0) {
            // The keymap is now the client's own compilation, so the component name a by-name load
            // recorded no longer describes what a client would be typing on - and a name that is
            // wrong is worse than no name at all, so it goes.
            symbolsName = null;
            server.broadcastMappingNotify((byte) 0, (byte) first, (byte) changed);
        }
        JulLog.info("{} set the keymap through XKB: {} keycode(s) of keysyms applied, the types,"
                + " actions, behaviours and modifier map dropped", client, changed);
    }

    /** A request accepted with nothing done about it, which for these is the honest answer. */
    private void ignored(ClientConnection client, Ignored request) {
        JulLog.debug("{} sent XKEYBOARD request {}: accepted, nothing here to set", client,
                request.minor());
    }

    /**
     * {@code GetKbdByName}: "find this layout in a rules file and load it here", which is what
     * {@code setxkbmap} asks and what a server with no keymap compiler cannot do.
     *
     * <p><b>Measured, and this is why the request is handled at all.</b> {@code setxkbmap -layout de}
     * against this server used to die on {@code BadRequest} for this request - {@code X Error of
     * failed request: BadRequest, Major opcode 134 (XKEYBOARD), Minor opcode 23
     * (XkbGetKbdByName)} - because the generated decoder has no class for minor 23 and a decode
     * failure is refused. Xlib's default handler prints an error and exits, so the client never got
     * the chance to do anything else, and the item read as "no layout can be selected".
     *
     * <p><b>The reply is a header plus embedded component replies, and Xlib reads them by position -
     * which is what makes "loaded = 1" alone useless.</b> {@code XkbGetKeyboardByName} checks
     * {@code reported} first and gives up when it is zero, then reads, for every bit set in it, one
     * whole component reply out of the same buffer: a map reply for {@code Types|Symbols}, a compat
     * reply for {@code CompatMap}, a names reply for {@code KeyNames|OtherNames}, a geometry reply
     * for {@code Geometry}, in that order, and finally insists the buffer was consumed exactly (the
     * end of {@code XKBGetByName.c}). So a reply that says "loaded" but carries nothing is not a
     * half-answer - it is the same failure as not being loaded at all, which is why this server
     * installed the layout and {@code setxkbmap} still printed its error.
     *
     * <p>What is embedded is the map this server has: the very bytes {@link XkbMap} answers a
     * {@code GetMap} with, under a header whose length counts it. {@code reported} names exactly the
     * three parts that map carries - {@code Types}, {@code ClientSymbols}, {@code ServerSymbols}
     * (0x0d) - so Xlib reads the map and nothing else, and the length is the map's own: forty bytes
     * of {@code xkbGetMapReply} the client reads into the struct, plus the arrays it consumes.
     */
    private void getKbdByName(XServer server, ClientConnection client, GetKbdByName request) {
        String name = layoutOf(request.symbols());
        boolean loaded = name != null && Keymap.useLayout(name);
        if (loaded) {
            // The names a client would read back with GetNames are the ones the request itself
            // carries: it is the only statement about which components are loaded that this server
            // has, and the symbols component is the one it acts on.
            symbolsName = request.symbols();
            // Every client resolves keycodes through its own copy of the mapping, so a layout that
            // changed under them is a layout they have to be told to re-read - the same notification
            // `SetMap` sends, and the reason a client that missed it would go on typing the old one.
            server.broadcastMappingNotify((byte) 0, Keymap.MIN_KEYCODE,
                    (byte) (Keymap.MAX_KEYCODE - Keymap.MIN_KEYCODE + 1));
        }
        JulLog.debug("{} asked this server to load a keymap by name ({}): {}", client,
                request.symbols(), loaded ? "loaded " + name : "not a layout this server has");
        byte[] map = loaded
                ? XkbMap.getMapReply(XkbMap.ALL_CLIENT_INFO_MASK, 0, 0, 0, 0, 0, 0,
                        client.sequenceNumber() & 0xFFFF, client.isBigEndian())
                : new byte[0];
        byte[] bytes = new byte[32 + map.length];
        bytes[0] = 1;
        bytes[1] = (byte) XkbMap.CORE_KEYBOARD_DEVICE;
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        // The length counts everything after the header, which is the embedded map reply whole - its
        // own header included, because Xlib reads the first forty of those bytes into
        // `xkbGetMapReply` and then takes that reply's length as the size of its arrays.
        putCard32(bytes, 4, map.length / 4, client.isBigEndian());
        bytes[8] = (byte) Keymap.MIN_KEYCODE;
        bytes[9] = (byte) Keymap.MAX_KEYCODE;
        bytes[10] = (byte) (loaded ? 1 : 0);    // loaded
        bytes[11] = 0;                          // newKeyboard: the client may rebuild its copy
        putCard16(bytes, 12, loaded ? REPORTED_PARTS : 0, client.isBigEndian());   // found
        putCard16(bytes, 14, loaded ? REPORTED_PARTS : 0, client.isBigEndian());   // reported
        System.arraycopy(map, 0, bytes, 32, map.length);
        send(client, bytes);
    }

    /**
     * {@code GetNames}: the names a client reads to know which keymap is loaded.
     *
     * <p><b>This used to be refused, and a refusal is the worst answer this extension can give</b> -
     * Xlib's default error handler ends the client's process, which is what killed {@code setxkbmap}
     * here before {@code GetKbdByName} was answered. The reply is a header and, for every bit in its
     * {@code which}, one four byte atom read out of the <em>tail</em> of the reply in a fixed order -
     * keycodes, geometry, symbols, physical symbols, types, compat. The atoms are in the tail and not
     * in the fixed part, which the protocol's field names suggest: {@code _XkbReadGetNamesReply}
     * takes them from the extra data, and a reference's reply has them there (measured with
     * {@code XkbProbe getnames}, and the field offsets below are that reply's).
     *
     * <p>What this server can honestly name is the symbols component, because it is the one it acted
     * on: the client's own request said which keymap to load and this server loaded it. The other
     * components it serves are its own (its four types, its modifier table), and none of them has a
     * name from a rules file that this server could truthfully report - so they are not claimed in
     * {@code which}, and a client that asks for them is told there is nothing, not given a name that
     * would send it looking for a component this server does not have. Before any layout is asked
     * for by name there is nothing to report at all, which is also what {@code which = 0} says.
     */
    private void getNames(XServer server, ClientConnection client, GetNamesRequest request) {
        int which = request.which() & REPORTABLE_NAMES;
        if (symbolsName == null || symbolsName.isEmpty()) {
            which = 0;
        }
        List<Integer> atoms = new java.util.ArrayList<>();
        for (int bit = 1; bit <= REPORTABLE_NAMES; bit <<= 1) {
            if ((which & bit) != 0) {
                atoms.add(server.atoms().intern(symbolsName));
            }
        }
        byte[] bytes = new byte[32 + atoms.size() * 4];
        bytes[0] = 1;
        bytes[1] = (byte) XkbMap.CORE_KEYBOARD_DEVICE;
        putCard16(bytes, 2, client.sequenceNumber() & 0xFFFF, client.isBigEndian());
        putCard32(bytes, 4, atoms.size(), client.isBigEndian());     // length, in four byte units
        putCard32(bytes, 8, which, client.isBigEndian());
        bytes[12] = (byte) Keymap.MIN_KEYCODE;      // minKeyCode
        bytes[13] = (byte) Keymap.MAX_KEYCODE;      // maxKeyCode
        bytes[14] = (byte) XkbMap.typeCount();      // nTypes: the four types this server publishes
        bytes[15] = 0;                              // groupNames: one group, and it is unnamed
        putCard16(bytes, 16, 0, client.isBigEndian());  // virtualMods: none are used
        bytes[18] = (byte) Keymap.MIN_KEYCODE;      // firstKey
        bytes[19] = (byte) (Keymap.MAX_KEYCODE - Keymap.MIN_KEYCODE + 1);   // nKeys
        bytes[20] = 0;                              // nKeyAliases: this server has no aliases
        bytes[21] = 0;                              // nRadioGroups: nor radio groups
        for (int i = 0; i < atoms.size(); i++) {
            putCard32(bytes, 32 + i * 4, atoms.get(i), client.isBigEndian());
        }
        send(client, bytes);
    }

    /**
     * {@code GetNames}: the request carries only the device and the mask of names wanted, so it is
     * read for the mask alone - the reply, not the request, is where the work is.
     */
    private record GetNamesRequest(int which) implements XRequest {

        static GetNamesRequest read(com.github.moaxcp.x11.protocol.X11Input in) throws IOException {
            in.readPad(1);                          // the minor opcode, already known
            in.readCard16();                        // length
            in.readCard16();                        // deviceSpec
            in.readCard16();                        // pad
            return new GetNamesRequest(in.readCard32());
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xkb";
        }
    }

    /**
     * The layout inside the symbols component string a client sends, or null.
     *
     * <p>What {@code setxkbmap -layout de} sends is not the word "de" but the components it resolved
     * the rules file to - {@code pc+de+inet(evdev)} - so the name is one of the `+`-separated parts,
     * and which one is a matter of trying them: the first part this server has a table for is the
     * layout. `pc` and `inet(evdev)` are components of every request and no layout here, and a
     * variant's parentheses are dropped because a variant is a keymap this server has no tables for -
     * the request is answered, and answered as the base layout rather than as something invented.
     */
    private static String layoutOf(String components) {
        if (components == null || components.isEmpty()) {
            return null;
        }
        for (String part : components.split("\\+")) {
            String base = part;
            int parenthesis = base.indexOf('(');
            if (parenthesis >= 0) {
                base = base.substring(0, parenthesis);
            }
            if (Keymap.hasLayout(base)) {
                return base;
            }
        }
        return null;
    }

    private void send(ClientConnection client, byte[] bytes) {
        try {
            client.output().writeRaw(bytes);
        } catch (IOException e) {
            client.close();
        }
    }

    private static void putCard32(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 24);
            bytes[offset + 1] = (byte) (value >> 16);
            bytes[offset + 2] = (byte) (value >> 8);
            bytes[offset + 3] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
            bytes[offset + 2] = (byte) (value >> 16);
            bytes[offset + 3] = (byte) (value >> 24);
        }
    }

    private static void putCard16(byte[] bytes, int offset, int value, boolean bigEndian) {
        if (bigEndian) {
            bytes[offset] = (byte) (value >> 8);
            bytes[offset + 1] = (byte) value;
        } else {
            bytes[offset] = (byte) value;
            bytes[offset + 1] = (byte) (value >> 8);
        }
    }

    /**
     * {@code SetMap}, decoded here because the generated class cannot read it.
     *
     * <p><b>Every list in the generated builder is non-null</b>, and a part the request does not
     * carry is left null - so a {@code SetMap} that sets only the key symbols, which the protocol
     * allows and a client changing one key's character will send, fails to decode with
     * {@code NullPointerException: types is marked non-null but is null}. Measured: that is what
     * happened to a test's one-keycode request while {@code xkbcomp}'s all-parts request went
     * through. What is read here is the header, the type descriptors (skipped, because this server
     * has four types of its own), and the key symbols; the rest of the body is left unread, which is
     * safe because the connection buffers the whole request before it is decoded.
     */
    private record SetMapRequest(int firstKeySym, List<int[]> syms) implements XRequest {

        private static final int KEY_TYPES = 1;
        private static final int KEY_SYMS = 2;

        static SetMapRequest read(com.github.moaxcp.x11.protocol.X11Input in) throws IOException {
            in.readCard8();                             // the major opcode, again
            in.readCard16();                            // length
            in.readCard16();                            // deviceSpec
            int present = in.readCard16();
            in.readCard16();                            // flags
            in.readCard8();                             // minKeyCode
            in.readCard8();                             // maxKeyCode
            in.readCard8();                             // firstType
            // Unsigned: a keymap has 248 keycodes and a byte that reads as -8 is not a loop bound.
            int nTypes = Byte.toUnsignedInt(in.readCard8());
            int firstKeySym = Byte.toUnsignedInt(in.readCard8());
            int nKeySyms = Byte.toUnsignedInt(in.readCard8());
            in.readCard16();                            // totalSyms
            in.readCard8(); in.readCard8(); in.readCard16();    // firstKeyAction, nKeyActions,
            in.readCard8(); in.readCard8(); in.readCard8();     //   totalActions, behaviours
            in.readCard8(); in.readCard8(); in.readCard8();     // explicit
            in.readCard8(); in.readCard8(); in.readCard8();     // modifier map
            in.readCard8(); in.readCard8(); in.readCard8();     // virtual modifier map
            in.readCard16();                            // virtualMods
            if ((present & KEY_TYPES) != 0) {
                skipTypes(in, nTypes);
            }
            List<int[]> syms = new java.util.ArrayList<>();
            if ((present & KEY_SYMS) != 0) {
                for (int i = 0; i < nKeySyms; i++) {
                    in.readCard8(4);                    // the four group type indices
                    in.readCard8();                     // groupInfo
                    in.readCard8();                     // width
                    int nSyms = in.readCard16();
                    int[] keysyms = new int[nSyms];
                    for (int j = 0; j < nSyms; j++) {
                        keysyms[j] = in.readCard32();
                    }
                    syms.add(keysyms);
                }
            }
            return new SetMapRequest(firstKeySym, syms);
        }

        /** The type descriptors, so that the key symbols behind them can be reached. */
        private static void skipTypes(com.github.moaxcp.x11.protocol.X11Input in, int count)
                throws IOException {
            for (int i = 0; i < count; i++) {
                in.readCard8();                         // mask
                in.readCard8();                         // realMods
                in.readCard16();                        // virtualMods
                in.readCard8();                         // numLevels
                int entries = in.readCard8();
                boolean preserve = in.readCard8() != 0;
                in.readCard8();                         // pad
                in.readCard8(4 * entries);              // the entries, four bytes each in a request
                if (preserve) {
                    in.readCard8(4 * entries);
                }
            }
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xkb";
        }
    }

    /**
     * {@code GetKbdByName}: which components to look up and load, followed by the component names
     * themselves. The generated plugin has no class for this request, so it is read here.
     *
     * <p><b>The fixed part is twelve bytes and there are six counted strings, and both were measured
     * rather than read out of a header.</b> Captured off the wire ({@code strace} of
     * {@code setxkbmap -layout de} against a reference, reproduced by {@code XkbProbe getkbdname}):
     * {@code deviceSpec}, {@code want}, {@code need}, {@code load}, a pad byte, then
     * <em>keymap</em>, <em>keycodes</em>, <em>types</em>, <em>compat</em>, <em>symbols</em>,
     * <em>geometry</em> as counted strings. A request built with eight of them - the count a first
     * reading of the field names suggests - is refused by the reference with {@code BadLength}, which
     * is how the six were confirmed.
     *
     * <p>The first reader here assumed a ten byte fixed part, which shifted every string by one and
     * made the layout appear in the sixth field instead of the fifth. It answered correctly anyway -
     * the phantom string it counted first happens to sit in front - but it was reading the wrong
     * field of a request it did not model, and the offsets above are the protocol's.
     */
    private record GetKbdByName(String keycodes, String types, String compat, String symbols,
                                String geometry) implements XRequest {

        /** {@code xkbGetKbdByNameReq}: the fixed part, before the counted strings. */
        private static final int FIXED = 12;

        static GetKbdByName readGetKbdByName(com.github.moaxcp.x11.protocol.X11Input in)
                throws IOException {
            in.readPad(1);                          // the minor opcode, already known
            int length = in.readCard16();           // the request length, in four byte units
            in.readCard16();                        // deviceSpec
            in.readCard16();                        // want
            in.readCard16();                        // need
            in.readCard8();                         // load
            in.readCard8();                         // pad
            int remaining = length * 4 - FIXED;     // what is left for the strings
            String[] fields = new String[6];
            for (int field = 0; field < fields.length && remaining > 0; field++) {
                int count = in.readCard8();
                StringBuilder text = new StringBuilder(count);
                for (int i = 0; i < count; i++) {
                    text.append((char) in.readCard8());
                }
                remaining -= 1 + count;
                fields[field] = text.toString();
            }
            return new GetKbdByName(fields[1], fields[2], fields[3], fields[4], fields[5]);
        }

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xkb";
        }
    }

    /**
     * A request that is decoded only far enough to know it is one of the ones with no answer.
     *
     * <p>{@code SelectEvents} carries a variable number of mask words after its fixed part, sized by
     * its own {@code affectWhich}; the generated reader models that, but the reply to it is nothing
     * at all, so reading the fixed part and leaving the rest is both enough and safer.
     */
    private record Ignored(int minor) implements XRequest {

        @Override
        public byte getOpCode() {
            return 0;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public void write(byte majorOpcode, com.github.moaxcp.x11.protocol.X11Output out) {
        }

        @Override
        public String getPluginName() {
            return "xkb";
        }
    }
}
