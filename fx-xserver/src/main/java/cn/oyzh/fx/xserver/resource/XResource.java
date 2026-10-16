package cn.oyzh.fx.xserver.resource;

import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.ResourceTable;

/**
 * Base class for everything a client can name with a resource id: windows, pixmaps, graphics
 * contexts, fonts, cursors, colormaps and the extension defined resource types.
 */
public abstract class XResource {

    /** Resource classes, used for the {@code BadMatch}/{@code Bad* error} checks. */
    public static final int CLASS_WINDOW = 1;
    public static final int CLASS_PIXMAP = 2;
    public static final int CLASS_GC = 3;
    public static final int CLASS_FONT = 4;
    public static final int CLASS_CURSOR = 5;
    public static final int CLASS_COLORMAP = 6;
    public static final int CLASS_OTHER = 7;

    /** Extension resource classes start here so they never collide with the core classes. */
    private static final int CLASS_EXTENSION_BASE = 100;
    private static int nextExtensionClass = CLASS_EXTENSION_BASE;

    private final int id;
    private final int resourceClass;
    private final ClientConnection owner;

    /** Set when the resource has been destroyed but not yet reaped, so double frees are no-ops. */
    private boolean destroyed;

    protected XResource(int id, int resourceClass, ClientConnection owner) {
        this.id = id;
        this.resourceClass = resourceClass;
        this.owner = owner;
    }

    /** Allocates a fresh resource class for an extension. */
    public static synchronized int newExtensionClass() {
        return nextExtensionClass++;
    }

    public final int id() {
        return id;
    }

    public final int resourceClass() {
        return resourceClass;
    }

    public final ClientConnection owner() {
        return owner;
    }

    public final boolean isDestroyed() {
        return destroyed;
    }

    /**
     * Releases everything this resource holds. Called exactly once by {@link ResourceTable#free}.
     */
    protected void onDestroy() {
    }

    final void markDestroyed() {
        destroyed = true;
    }

    /** Name used in error messages and diagnostics. */
    public String typeName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return typeName() + "(0x" + Integer.toHexString(id) + ")";
    }
}
