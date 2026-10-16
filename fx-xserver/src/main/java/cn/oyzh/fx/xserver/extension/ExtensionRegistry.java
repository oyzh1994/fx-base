package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.client.ProtocolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Assigns opcode, event and error ranges to extensions and routes requests to them.
 *
 * <p>Opcode space is handed out from 128 upward in registration order. The assignment has to be
 * stable for the lifetime of the server because clients cache it from {@code QueryExtension}.
 */
public final class ExtensionRegistry {

    /** Core protocol requests own opcodes 1..127; extensions start here. */
    public static final byte FIRST_EXTENSION_OPCODE = (byte) 128;
    /** Core events end at 35; extensions start at 64 to leave room for future core events. */
    public static final byte FIRST_EXTENSION_EVENT = (byte) 64;
    /** Core errors end at 17. */
    public static final byte FIRST_EXTENSION_ERROR = (byte) 18;

    private byte nextOpcode = FIRST_EXTENSION_OPCODE;
    private byte nextEvent = FIRST_EXTENSION_EVENT;
    private byte nextError = FIRST_EXTENSION_ERROR;

    private final List<Extension> extensions = new ArrayList<>();
    private final Map<Byte, Extension> byOpcode = new HashMap<>();
    private final Map<String, Extension> byName = new HashMap<>();

    /**
     * Registers an extension and gives it its opcode, event and error ranges.
     *
     * @param eventCount number of event codes the extension defines
     * @param errorCount number of error codes the extension defines
     */
    public synchronized <T extends Extension> T register(T extension, int eventCount, int errorCount) {
        byte opcode = nextOpcode++;
        // An extension that defines no events of its own reports **zero**, not the next free code:
        // measured with `QueryExtension` against a reference (tools/XgeProbe and tools/ResProbe),
        // which answers first_event=0 and first_error=0 for BIG-REQUESTS, XC-MISC, Composite,
        // Present and XGE - every one of them a requests-and-replies extension - while SHAPE (one
        // event, no errors) answers 64/0. Reporting the registry's counter instead is invisible to a
        // client that only ever looks at the ranges it needs, which is why it went unnoticed until
        // an extension with neither range was added.
        //
        // **"Defines no events of its own" is a statement about this server, not about the
        // protocol, and the two are not interchangeable when a client library hooks the range.**
        // XInput and XKEYBOARD have real event numbers in a reference (66 and 85) and a client
        // library that installs a wire-to-event converter for every advertised code - libXi and
        // libX11's own XKB support - so a zero here lands those converters on the *core* event
        // numbers. That is A18, and it is why both are registered with their own event counts even
        // though this server never sends an event in either range: see the registration site in
        // XServer.registerExtensions, which says what the corruption looked like.
        byte firstEvent = eventCount == 0 ? 0 : nextEvent;
        byte firstError = errorCount == 0 ? 0 : nextError;
        nextEvent += eventCount;
        nextError += errorCount;

        // The generated plugin owns these three values; Extension implementations read them back
        // through majorOpcode()/firstEvent()/firstError(), so there is one source of truth.
        extension.plugin().setMajorOpcode(opcode);
        extension.plugin().setFirstEvent(firstEvent);
        extension.plugin().setFirstError(firstError);

        extensions.add(extension);
        byOpcode.put(opcode, extension);
        byName.put(extension.name(), extension);
        return extension;
    }

    /** Looks up an extension by the major opcode a client sent. */
    public Extension byOpcode(byte opcode) {
        return byOpcode.get(opcode);
    }

    /** Looks up an extension by protocol name, used by {@code QueryExtension}. */
    public Extension byName(String name) {
        return byName.get(name);
    }

    public boolean isExtensionOpcode(byte opcode) {
        return byOpcode.containsKey(opcode);
    }

    public List<Extension> all() {
        return List.copyOf(extensions);
    }

    /** Names in registration order, which is what {@code ListExtensions} reports. */
    public List<String> names() {
        List<String> names = new ArrayList<>();
        for (Extension extension : extensions) {
            names.add(extension.name());
        }
        return names;
    }

    /** Resolves an extension or throws the protocol error a client expects. */
    public Extension require(byte opcode) {
        Extension extension = byOpcode.get(opcode);
        if (extension == null) {
            throw ProtocolException.unimplemented("opcode " + (opcode & 0xFF));
        }
        return extension;
    }
}
