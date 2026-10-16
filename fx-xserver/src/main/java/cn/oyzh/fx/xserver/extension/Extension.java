package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.XWindow;

/**
 * An X extension served by this server.
 *
 * <p>Request decoding is delegated to the generated {@link XProtocolPlugin} for the extension,
 * which knows every opcode and field layout. Extensions only implement behaviour, so adding a new
 * one means wiring the plugin and handling the decoded requests.
 */
public interface Extension {

    /** Protocol name as reported by {@code QueryExtension}, for example {@code SHAPE}. */
    String name();

    /** Major opcode assigned when the extension was registered. */
    byte majorOpcode();

    /** First event code reserved for this extension. */
    byte firstEvent();

    /** First error code reserved for this extension. */
    byte firstError();

    /** The generated protocol plugin used to decode requests. */
    XProtocolPlugin plugin();

    /** Called once after registration, so the extension can allocate its resources. */
    default void init(XServer server) {
    }

    /**
     * Decodes a request the generated decoder cannot, or returns null to let the plugin decode it.
     *
     * <p>Some generated readers size their last list from the request length with the wrong
     * arithmetic and throw on perfectly valid requests. An extension that has such a request says so
     * here; see {@code VariableLengthDecoders} for the full explanation.
     */
    default XRequest decodeRequest(int minorOpcode, com.github.moaxcp.x11.protocol.X11Input in)
            throws java.io.IOException {
        return null;
    }

    /** Handles one decoded request. */
    void handleRequest(XServer server, ClientConnection client, XRequest request);

    /** Lets an extension drop state belonging to a client that has gone away. */
    default void clientDisconnected(XServer server, ClientConnection client) {
    }

    /**
     * Told when a window changes size, which an extension holding buffers sized to that window cannot
     * wait to be asked about.
     *
     * <p>DOUBLE-BUFFER is the extension this exists for: a back buffer is allocated at its window's
     * size, and a reference server reallocates it - and fills it with the window's background - when
     * the window is resized (measured). Nothing else in the protocol tells an extension about a
     * {@code ConfigureWindow}, and catching up on the next request of its own would leave a client
     * that resizes and draws before it swaps with a buffer of the wrong size.
     */
    default void windowResized(XServer server, XWindow window, int oldWidth, int oldHeight) {
    }

    /**
     * Whether the extension should appear before the client has used it. Extensions that must be
     * present in {@code ListExtensions} regardless of usage return true.
     */
    default boolean alwaysPresent() {
        return true;
    }

    /**
     * Whether an <b>untrusted</b> client may see this extension at all - in {@code ListExtensions},
     * in {@code QueryExtension}, or by sending its requests.
     *
     * <p>Almost nothing may: a reference with access control on answers an untrusted client's
     * {@code ListExtensions} with two extensions where a trusted client sees twenty three
     * ({@code BIG-REQUESTS} and {@code XC-MISC}, both of which only ask about the server itself),
     * says "not present" for every other, and answers a request to a hidden extension's opcode with
     * {@code BadRequest}. That is the SECURITY extension's own rule applied to itself, and it is
     * measured rather than derived - see {@link SecurityExtension}.
     */
    default boolean visibleToUntrustedClients() {
        return false;
    }
}
