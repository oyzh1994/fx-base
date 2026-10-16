package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.res.Client;
import com.github.moaxcp.x11.protocol.res.ClientIdSpec;
import com.github.moaxcp.x11.protocol.res.ClientIdValue;
import com.github.moaxcp.x11.protocol.res.QueryClientIds;
import com.github.moaxcp.x11.protocol.res.QueryClientIdsReply;
import com.github.moaxcp.x11.protocol.res.QueryClientPixmapBytes;
import com.github.moaxcp.x11.protocol.res.QueryClientPixmapBytesReply;
import com.github.moaxcp.x11.protocol.res.QueryClientResources;
import com.github.moaxcp.x11.protocol.res.QueryClientResourcesReply;
import com.github.moaxcp.x11.protocol.res.QueryClients;
import com.github.moaxcp.x11.protocol.res.QueryClientsReply;
import com.github.moaxcp.x11.protocol.res.QueryResourceBytes;
import com.github.moaxcp.x11.protocol.res.QueryResourceBytesReply;
import com.github.moaxcp.x11.protocol.res.QueryVersion;
import com.github.moaxcp.x11.protocol.res.QueryVersionReply;
import com.github.moaxcp.x11.protocol.res.ResPlugin;
import com.github.moaxcp.x11.protocol.res.ResourceIdSpec;
import com.github.moaxcp.x11.protocol.res.ResourceSizeSpec;
import com.github.moaxcp.x11.protocol.res.ResourceSizeValue;
import com.github.moaxcp.x11.protocol.res.Type;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.resource.XPixmap;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.eclipse.collections.impl.factory.Lists;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * X-Resource: per-client resource accounting.
 *
 * <p>It answers "who is using the memory" - the question E4's per-client framebuffer work left
 * open, since that item made allocation lazy without leaving a way to see it. Three requests carry
 * it: the client list, the resource counts by type, and the bytes of a client's pixmaps; 1.2 adds
 * the two id and size queries.
 *
 * <p><b>Measured against a reference</b> ({@code tools/ResProbe}, Xvfb):
 *
 * <ul>
 *   <li>{@code QueryVersion} answers **the server's own** version, 1.2, to a client asking for 1.2,
 *       for 1.0 or for 2.0 - the same rule as Composite's and SYNC's, and the opposite of
 *       Present's.
 *   <li>{@code QueryClients} lists **every client including the server's own**, which a reference
 *       reports first with resource base zero. This server's own resources are the ones with no
 *       owning connection - the root window, the predefined fonts and colormaps - so they are that
 *       same entry.
 *   <li>The resource-count reply names types with **atoms** - WINDOW, PIXMAP, GC, FONT, CURSOR -
 *       and the count is per type. The five core names were read off a reference's reply; the
 *       extension types this server has (PICTURE, DAMAGE and the rest) are named by upper-casing the
 *       resource's own type name, which is this server's naming and not a reference's.
 *   <li>An id no client owns is refused **BadValue** carrying the id, by all three requests that
 *       name a client. A {@code QueryClientIds} spec naming an unknown client is *not* an error: it
 *       produces no entries, which is what a reference answers.
 *   <li>{@code QueryClientIds} answers the XID bit with **an entry whose value list is empty**,
 *       which is a reference's own answer and not a missing feature - see the note in the class.
 * </ul>
 *
 * <p><b>Two deliberate differences from the reference, both recorded in {@code tools/README.md}.</b>
 * (1) A window's size is reported as its pixel store, where a reference reports zero - this server
 * keeps every window's pixels (B5), so a window really does hold memory and reporting none would be
 * the wrong answer to the question the extension asks. (2) {@code QueryResourceBytes} matches the
 * {@code type} a spec names against the resource's own type atom, which is what the specification
 * says; a reference answers nothing for any non-zero type, even the resource's own, and a probe that
 * asks with the WINDOW atom for a window gets one row from this server and none from Xvfb.
 */
public final class ResExtension implements Extension {

    /** Measured: a reference answers 1.2 to every client, whatever version it asked with. */
    private static final int MAJOR_VERSION = 1;
    private static final int MINOR_VERSION = 2;

    /** The two bits a client can ask about in {@code QueryClientIds}. */
    private static final int CLIENT_X_I_D = 0b1;
    private static final int LOCAL_CLIENT_P_I_D = 0b10;

    /** Bytes a pixel of a store takes: every store here is an {@code int[]}, at every depth. */
    private static final int BYTES_PER_PIXEL = 4;

    private final ResPlugin plugin = new ResPlugin();

    /**
     * The atom each resource type is reported under, interned on first use.
     *
     * <p>Interned lazily and not at startup, because interning a name is visible to every client
     * ({@code InternAtom} of a name that already exists returns the same number), and a client that
     * never asks about resources should not see this server's numbering move because of it.
     */
    private final Map<String, Integer> typeAtoms = new HashMap<>();

    @Override
    public String name() {
        return "X-Resource";
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
            client.sendReply(QueryVersionReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .serverMajor((short) MAJOR_VERSION)
                    .serverMinor((short) MINOR_VERSION)
                    .build());
        } else if (request instanceof QueryClients) {
            queryClients(server, client);
        } else if (request instanceof QueryClientResources r) {
            queryClientResources(server, client, r.getXid());
        } else if (request instanceof QueryClientPixmapBytes r) {
            queryClientPixmapBytes(server, client, r.getXid());
        } else if (request instanceof QueryClientIds r) {
            queryClientIds(server, client, r.getSpecs());
        } else if (request instanceof QueryResourceBytes r) {
            queryResourceBytes(server, client, r.getClient(), r.getSpecs());
        } else {
            throw new IllegalArgumentException("unhandled X-Resource request " + request.getClass());
        }
    }

    // ------------------------------------------------------------------

    /** The server's own resources, then every connected client - which is the order a reference lists. */
    private void queryClients(XServer server, ClientConnection client) {
        List<Client> clients = new ArrayList<>();
        clients.add(Client.builder().resourceBase(0).resourceMask(ClientConnection.RESOURCE_MASK).build());
        for (ClientConnection connected : server.clients()) {
            clients.add(Client.builder()
                    .resourceBase(connected.resourceIdBase())
                    .resourceMask(connected.resourceIdMask())
                    .build());
        }
        client.sendReply(QueryClientsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .clients(Lists.immutable.withAll(clients))
                .build());
    }

    private void queryClientResources(XServer server, ClientConnection client, int xid) {
        ClientConnection owner = ownerOf(server, xid);
        Map<Integer, Integer> counts = new LinkedHashMap<>();
        for (XResource resource : resourcesOf(server, owner)) {
            counts.merge(typeAtom(server, resource), 1, Integer::sum);
        }
        List<Type> types = new ArrayList<>(counts.size());
        counts.forEach((atom, count) -> types.add(Type.builder()
                .resourceType(atom)
                .count(count)
                .build()));
        client.sendReply(QueryClientResourcesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .types(Lists.immutable.withAll(types))
                .build());
    }

    /**
     * The bytes a client's pixmaps hold.
     *
     * <p>Pixmaps only, which is what the request is named for and what a reference counts too - a
     * client that has made a window and no pixmap is answered zero by both. The total is split
     * across the reply's two fields, low word in {@code bytes} and high word in
     * {@code bytesOverflow}, so a client that reads only the first field still sees a total that is
     * right until it passes 4 GB.
     */
    private void queryClientPixmapBytes(XServer server, ClientConnection client, int xid) {
        ClientConnection owner = ownerOf(server, xid);
        long bytes = 0;
        for (XResource resource : resourcesOf(server, owner)) {
            if (resource instanceof XPixmap pixmap) {
                bytes += (long) pixmap.width() * pixmap.height() * BYTES_PER_PIXEL;
            }
        }
        client.sendReply(QueryClientPixmapBytesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .bytes((int) bytes)
                .bytesOverflow((int) (bytes >>> 32))
                .build());
    }

    /**
     * The client ids a spec asks about, one value per (client, requested bit).
     *
     * <p><b>The XID bit is answered with an empty value list, and that is a reference's own
     * answer</b> - a measured one, and surprising enough that it is worth stating: a reference emits
     * a row for the bit whose value list is empty, so the row names the client and says nothing
     * about it. This server reproduces that rather than inventing the client's resource base, which
     * a client can get from {@code QueryClients} anyway. (It also keeps the generated writer honest:
     * its length field is the number of *values*, where the protocol means bytes, so a non-empty
     * list would be malformed. With an empty list the two agree.)
     *
     * <p><b>The local-pid bit is not answered, and a reference omits the same rows for the same
     * reason</b> - it lists only clients whose local process id it knows, which is a fact about the
     * unix socket they arrived on, and a client that reaches a reference over TCP is missing from
     * its reply exactly as every client here is missing from this one's. This server's transport is
     * TCP, so no client's pid is knowable and no row is emitted; that is a difference in what the
     * two servers *can* say, not in what they do with what they know.
     */
    private void queryClientIds(XServer server, ClientConnection client,
                                org.eclipse.collections.api.list.ImmutableList<ClientIdSpec> specs) {
        List<ClientIdValue> values = new ArrayList<>();
        for (ClientIdSpec spec : specs) {
            for (ClientConnection candidate : clientsMatching(server, spec.getClient())) {
                // The spec echoed back names **one client and one bit**, not the request's own
                // spec: a request with mask 3 produces a mask 1 row and a mask 2 row, each naming
                // the client it is about. Measured, and it is the only way the reply can say which
                // value belongs to which question.
                int clientBase = candidate == null ? 0 : candidate.resourceIdBase();
                if ((spec.getMask() & CLIENT_X_I_D) != 0) {
                    values.add(ClientIdValue.builder()
                            .spec(ClientIdSpec.builder()
                                    .client(clientBase)
                                    .mask(CLIENT_X_I_D)
                                    .build())
                            .value(IntLists.immutable.empty())
                            .build());
                }
                // LOCAL_CLIENT_P_I_D contributes nothing: see the note above. Any other bit is
                // ignored rather than refused, which is how a server treats a bit it does not
                // define - the mask is a set of questions, and the ones a server cannot answer are
                // simply not answered.
            }
        }
        client.sendReply(QueryClientIdsReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .ids(Lists.immutable.withAll(values))
                .build());
    }

    /**
     * The size of each named resource.
     *
     * <p>A window's size is its own pixel store, which this server keeps for every window (B5) - a
     * deliberate difference from a reference, where windows have no backing store and are reported
     * as zero bytes. The reference counts a request like this as "what would be freed if the client
     * went away", and here that is the store.
     *
     * <p>{@code refCount} and {@code useCount} are reported as one each: this server does not count
     * references to a resource, and one is the truth for every resource a client makes. A named
     * window pixmap is the one resource shared between two names, and it too reports one.
     */
    private void queryResourceBytes(XServer server, ClientConnection client, int clientId,
                                    org.eclipse.collections.api.list.ImmutableList<ResourceIdSpec> specs) {
        // A `client` of zero asks about every client, the server's own included - measured, and not
        // guessable: zero is also the server's own resource base, and a reference answered a query
        // naming zero with a resource that belonged to the *asking* connection.
        List<ClientConnection> owners = new ArrayList<>();
        if (clientId == 0) {
            owners.add(null);
            owners.addAll(server.clients());
        } else {
            owners.add(ownerOf(server, clientId));
        }
        List<ResourceSizeValue> sizes = new ArrayList<>();
        for (ResourceIdSpec spec : specs) {
            XResource resource = server.resources().get(spec.getResource());
            if (resource == null || !owners.contains(resource.owner())) {
                continue;                           // not a client that was asked about: no row
            }
            int atom = typeAtom(server, resource);
            if (spec.getType() != 0 && spec.getType() != atom) {
                continue;
            }
            sizes.add(ResourceSizeValue.builder()
                    .size(ResourceSizeSpec.builder()
                            .spec(ResourceIdSpec.builder()
                                    .resource(resource.id())
                                    .type(atom)
                                    .build())
                            .bytes(bytesOf(resource))
                            .refCount(1)
                            .useCount(1)
                            .build())
                    .crossReferences(Lists.immutable.empty())
                    .build());
        }
        client.sendReply(QueryResourceBytesReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .sizes(Lists.immutable.withAll(sizes))
                .build());
    }

    // ------------------------------------------------------------------

    /**
     * Resolves the client an id belongs to, or {@code null} for the server's own resources.
     *
     * <p>The server's range is the one with base zero - the ids {@code nextServerResourceId} hands
     * out - which is how a reference's own client is numbered too.
     */
    private static ClientConnection ownerOf(XServer server, int xid) {
        if ((xid & ~ClientConnection.RESOURCE_MASK) == 0) {
            return null;
        }
        for (ClientConnection candidate : server.clients()) {
            if (candidate.ownsIdRange(xid)) {
                return candidate;
            }
        }
        throw ProtocolException.badValue(xid);
    }

    /** The clients a {@code QueryClientIds} spec asks about; a base of zero means every client. */
    private static List<ClientConnection> clientsMatching(XServer server, int base) {
        List<ClientConnection> matching = new ArrayList<>();
        if (base == 0) {
            matching.add(null);                     // the server's own resources are a client here
            matching.addAll(server.clients());
            return matching;
        }
        for (ClientConnection candidate : server.clients()) {
            if (candidate.resourceIdBase() == base) {
                matching.add(candidate);
            }
        }
        return matching;                            // an unknown base is no clients, not an error
    }

    private static List<XResource> resourcesOf(XServer server, ClientConnection owner) {
        return owner == null ? server.resources().serverResources()
                : server.resources().resourcesOf(owner);
    }

    /** The bytes a resource holds: a pixel store for the two drawable kinds this server keeps. */
    private static int bytesOf(XResource resource) {
        if (resource instanceof XPixmap pixmap) {
            return (int) ((long) pixmap.width() * pixmap.height() * BYTES_PER_PIXEL);
        }
        if (resource instanceof XWindow window) {
            return (int) ((long) window.width() * window.height() * BYTES_PER_PIXEL);
        }
        return 0;
    }

    /**
     * The atom a resource type is reported under.
     *
     * <p>The six core names are the ones a reference uses, read off its reply; the rest are this
     * server's own type name upper-cased, because the extension resource types it has are not a
     * reference's set and there is nothing to agree with.
     */
    private int typeAtom(XServer server, XResource resource) {
        String name = switch (resource.resourceClass()) {
            case XResource.CLASS_WINDOW -> "WINDOW";
            case XResource.CLASS_PIXMAP -> "PIXMAP";
            case XResource.CLASS_GC -> "GC";
            case XResource.CLASS_FONT -> "FONT";
            case XResource.CLASS_CURSOR -> "CURSOR";
            case XResource.CLASS_COLORMAP -> "COLORMAP";
            default -> resource.typeName().toUpperCase(Locale.ROOT);
        };
        synchronized (typeAtoms) {
            Integer atom = typeAtoms.get(name);
            if (atom == null) {
                atom = server.atoms().intern(name);
                typeAtoms.put(name, atom);
            }
            return atom;
        }
    }
}
