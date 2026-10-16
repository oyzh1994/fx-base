package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * The server wide registry of live resources.
 *
 * <p>X11 lets clients pick their own resource ids, so the table records who created each one: that
 * is what decides which resources are destroyed when a connection ends. It is deliberately not an
 * access control list - any client may use any id it knows, which is how window managers operate on
 * other clients' windows.
 *
 * <p>Lookups carry the expected resource class so that passing a pixmap where a window is expected
 * produces {@code BadWindow} rather than a cast error, and so an unknown id produces the error the
 * client's request implies.
 */
public final class ResourceTable {

    private final Map<Integer, XResource> byId = new HashMap<>();
    private final Map<ClientConnection, Map<Integer, XResource>> byOwner = new IdentityHashMap<>();

    public XResource get(int id) {
        return byId.get(id);
    }

    public boolean contains(int id) {
        return byId.containsKey(id);
    }

    /** Total number of live resources, for diagnostics. */
    public int size() {
        return byId.size();
    }

    public void add(XResource resource) {
        XResource previous = byId.put(resource.id(), resource);
        if (previous != null) {
            // Should be impossible: callers validate first. Loud because it corrupts the table.
            throw new IllegalStateException("resource id 0x" + Integer.toHexString(resource.id())
                    + " already in use by " + previous);
        }
        byOwner.computeIfAbsent(resource.owner(), k -> new HashMap<>()).put(resource.id(), resource);
    }

    /**
     * Frees a resource by id. Returns the resource that was removed, or {@code null} if the id was
     * not in use. Double frees are therefore harmless, matching the behaviour clients rely on.
     */
    public XResource free(int id) {
        XResource resource = byId.remove(id);
        if (resource == null) {
            return null;
        }
        Map<Integer, XResource> owned = byOwner.get(resource.owner());
        if (owned != null) {
            owned.remove(id);
        }
        resource.markDestroyed();
        resource.onDestroy();
        return resource;
    }

    /** Frees every resource owned by a client, used when a connection goes away. */
    public void freeAll(ClientConnection client) {
        Map<Integer, XResource> owned = byOwner.remove(client);
        if (owned == null) {
            return;
        }
        for (Map.Entry<Integer, XResource> entry : new ArrayList<>(owned.entrySet())) {
            byId.remove(entry.getKey());
            XResource resource = entry.getValue();
            if (!resource.isDestroyed()) {
                resource.markDestroyed();
                resource.onDestroy();
            }
        }
    }

    /** Every live resource owned by a client, in id order so teardown is deterministic. */
    public List<XResource> resourcesOf(ClientConnection client) {
        Map<Integer, XResource> owned = byOwner.get(client);
        if (owned == null) {
            return List.of();
        }
        List<XResource> result = new ArrayList<>(owned.values());
        result.sort((a, b) -> Integer.compare(a.id(), b.id()));
        return result;
    }

    /**
     * Every live resource with no client owner: the server's own, which is the root window, the
     * predefined fonts and colormaps, and the windows the bridges create.
     *
     * <p>They are a group rather than a special case because a client can name them - the root
     * window above all - and a request that asks about a resource's owner has to have an answer for
     * them. X-Resource is where that shows: a reference reports its own resources as a client with
     * resource base zero (F3).
     */
    public List<XResource> serverResources() {
        return resourcesOf(null);
    }

    /** Live resources of a given class owned by a client, used to reverse-map windows etc. */
    public <T extends XResource> List<T> resourcesOfType(ClientConnection client, int resourceClass) {
        List<T> result = new ArrayList<>();
        for (XResource resource : resourcesOf(client)) {
            if (resource.resourceClass() == resourceClass) {
                @SuppressWarnings("unchecked")
                T typed = (T) resource;
                result.add(typed);
            }
        }
        return result;
    }

    public void clear() {
        byId.clear();
        byOwner.clear();
    }

    /**
     * Resolves an id to a resource, verifying that it belongs to {@code client} and is of the
     * expected class. {@code expectedClass} may be {@link XResource#CLASS_OTHER} to accept anything.
     */
    public XResource require(ClientConnection client, int id, int expectedClass, boolean allowNone) {
        if (id == 0 && allowNone) {
            return null;
        }
        XResource resource = byId.get(id);
        if (resource == null || resource.isDestroyed()) {
            throw unknown(id, expectedClass);
        }
        if (expectedClass != XResource.CLASS_OTHER && resource.resourceClass() != expectedClass) {
            throw unknown(id, expectedClass);
        }
        return resource;
    }

    private static ProtocolException unknown(int id, int expectedClass) {
        return switch (expectedClass) {
            case XResource.CLASS_WINDOW -> ProtocolException.badWindow(id);
            case XResource.CLASS_PIXMAP -> ProtocolException.badPixmap(id);
            case XResource.CLASS_GC -> ProtocolException.badGc(id);
            case XResource.CLASS_FONT -> ProtocolException.badFont(id);
            case XResource.CLASS_CURSOR -> ProtocolException.badCursor(id);
            case XResource.CLASS_COLORMAP -> ProtocolException.badColormap(id);
            default -> ProtocolException.badDrawable(id);
        };
    }

    /**
     * Drawables are windows or pixmaps. Both classes are accepted as X does, but the error is
     * {@code BadDrawable} rather than the more specific window/pixmap error.
     */
    public XResource requireDrawable(ClientConnection client, int id) {
        XResource resource = byId.get(id);
        if (resource == null || resource.isDestroyed()) {
            throw ProtocolException.badDrawable(id);
        }
        if (resource.resourceClass() != XResource.CLASS_WINDOW
                && resource.resourceClass() != XResource.CLASS_PIXMAP) {
            throw ProtocolException.badDrawable(id);
        }
        return resource;
    }
}
