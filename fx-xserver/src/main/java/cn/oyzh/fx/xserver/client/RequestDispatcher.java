package cn.oyzh.fx.xserver.client;

import com.github.moaxcp.x11.protocol.X11Input;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xproto.XprotoPlugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.client.ProtocolException;
import cn.oyzh.fx.xserver.extension.Extension;
import cn.oyzh.fx.xserver.extension.XFixesExtension;
import cn.oyzh.fx.xserver.handler.AtomHandlers;
import cn.oyzh.fx.xserver.handler.DrawHandlers;
import cn.oyzh.fx.xserver.handler.InputHandlers;
import cn.oyzh.fx.xserver.handler.MiscHandlers;
import cn.oyzh.fx.xserver.handler.ResourceHandlers;
import cn.oyzh.fx.xserver.handler.WindowHandlers;
import cn.oyzh.fx.xserver.protocol.VariableLengthDecoders;
import cn.oyzh.fx.xserver.resource.ResourceTable;
import cn.oyzh.fx.xserver.resource.XResource;
import cn.oyzh.fx.xserver.resource.XWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Routes a decoded request to the handler that implements it.
 *
 * <p>Decoding is done by the generated protocol plugin, which already understands every value mask,
 * bitcase and switch in the protocol. That turns dispatch into a pattern match over the request
 * type rather than a hand written opcode switch, and it means the wire format cannot drift from
 * the server's idea of it.
 */
public final class RequestDispatcher {

    private static final Logger log = LoggerFactory.getLogger(RequestDispatcher.class);

    private final XServer server;
    private final XprotoPlugin xproto = new XprotoPlugin();

    private final WindowHandlers windows;
    private final DrawHandlers draw;
    private final ResourceHandlers resources;
    private final AtomHandlers atoms;
    private final InputHandlers input;
    private final MiscHandlers misc;

    /**
     * The handler instances are created by the server rather than here, so that requests handled in
     * one area can call into another (drawing asks windows to repaint, for instance).
     */
    public RequestDispatcher(XServer server, WindowHandlers windows, DrawHandlers draw,
                             ResourceHandlers resources, AtomHandlers atoms, InputHandlers input,
                             MiscHandlers misc) {
        this.server = server;
        this.windows = windows;
        this.draw = draw;
        this.resources = resources;
        this.atoms = atoms;
        this.input = input;
        this.misc = misc;
    }

    public void dispatch(ClientConnection client, byte opcode, byte minorOpcode, X11Input in,
                         int requestBytes, byte[] request) {
        // Named before anything can throw, so a request that fails to *decode* is still reported
        // as itself rather than as whatever was handled before it.
        currentRequest = "opcode " + (opcode & 0xFF);
        if ((opcode & 0xFF) >= 128) {
            dispatchExtension(client, opcode, minorOpcode, in);
            return;
        }
        XRequest decoded = decodeCore(client, opcode, minorOpcode, in, requestBytes, request);
        if (server.options().logRequests()) {
            log.debug("{} -> {}", client, decoded.getClass().getSimpleName());
        }
        route(client, decoded);
    }

    /**
     * Name of the last request routed, used only to make the error log readable.
     *
     * <p>Every path that can throw sets it: a core request in {@link #route}, an extension request
     * in {@link #dispatchExtension}, and {@link #dispatch} itself for one that does not survive
     * decoding. Before that last one a request that failed to decode was reported under the name of
     * the previous request - measured live as "{@code InternAtom rejected the request ...
     * RANDR GetOutputProperty}", which names a request the client never sent.
     */
    public String currentRequest() {
        return currentRequest;
    }

    private XRequest decodeCore(ClientConnection client, byte opcode, byte minorOpcode,
                                X11Input in, int requestBytes, byte[] request) {
        try {
            // A handful of requests have to be decoded here; see VariableLengthDecoders for why.
            if (VariableLengthDecoders.handles(opcode & 0xFF)) {
                return VariableLengthDecoders.decode(opcode & 0xFF, in, requestBytes, request);
            }
            // **A per-request length check is what XTS's `Xproto` set still wants here** (A37): every
            // one of its 122 purposes has a case that sends a request whose length field is one unit
            // more (and another, one unit less) than the request type defines and expects
            // `BadLength`. An attempt to do this generically by comparing the *decoded* request's
            // `getSize()` with the declared length was made and **reverted the same hour**: the
            // generated classes' `getSize()` is not the wire size for many requests (GetProperty and
            // the whole extension surface failed the suite), so the check has to come from a table
            // of per-request sizes - exact for the fixed-length requests, "at least" for the rest,
            // which is the distinction Xorg encodes in `REQUEST_SIZE_MATCH` against
            // `REQUEST_AT_LEAST_SIZE`. Not done yet; see the backlog entry for the measurement.
            return xproto.readRequest(opcode, minorOpcode, in);
        } catch (IOException e) {
            // The request was already buffered, so a decode failure cannot desynchronise the
            // stream: report it and carry on.
            throw new ProtocolException(ProtocolException.REQUEST, opcode & 0xFF,
                    "malformed request for opcode " + (opcode & 0xFF));
        } catch (RuntimeException e) {
            throw new ProtocolException(ProtocolException.REQUEST, opcode & 0xFF,
                    "unsupported opcode " + (opcode & 0xFF));
        }
    }

    private void dispatchExtension(ClientConnection client, byte opcode, byte minorOpcode,
                                   X11Input in) {
        Extension extension = server.extensions().byOpcode(opcode);
        if (extension == null) {
            throw new ProtocolException(ProtocolException.REQUEST, opcode & 0xFF,
                    "unknown extension opcode " + (opcode & 0xFF));
        }
        if (!cn.oyzh.fx.xserver.extension.SecurityExtension.visibleToUntrusted(extension)
                && !client.isTrusted()) {
            // An extension an untrusted client may not see is not merely refused: it is not there,
            // and the answer is the one a client gets for an opcode that means nothing to this
            // server. That is what a reference answers for SECURITY's own requests from an untrusted
            // connection, and for every extension but BIG-REQUESTS and XC-MISC (measured for F7).
            throw new ProtocolException(ProtocolException.REQUEST, opcode & 0xFF,
                    "unknown extension opcode " + (opcode & 0xFF));
        }
        XRequest request;
        try {
            request = extension.decodeRequest(minorOpcode & 0xFF, in);
            if (request == null) {
                request = extension.plugin().readRequest(opcode, minorOpcode, in);
            }
        } catch (IOException | RuntimeException e) {
            // The cause matters: a decoder that runs off the end of the request and one that
            // rejects a value look identical in the error the client sees.
            throw new ProtocolException(ProtocolException.REQUEST, minorOpcode & 0xFF,
                    "could not decode " + extension.name() + " request " + (minorOpcode & 0xFF)
                            + ": " + e);
        }
        if (server.options().logRequests()) {
            log.debug("{} -> {} ({} minor {})", client, request.getClass().getSimpleName(),
                    extension.name(), minorOpcode & 0xFF);
        }
        // Set before the handler runs, so a handler that raises a protocol error is reported under
        // its own request name: an extension request used to leave the name of the last *core*
        // request in place, and the error log then named a request the client never sent.
        currentRequest = request.getClass().getSimpleName();
        extension.handleRequest(server, client, request);
    }

    /**
     * The name of the request being handled, for the error log. Set before routing so a handler
     * that throws can say which request it was; an opcode number alone makes a client's error
     * report hard to act on.
     */
    private String currentRequest = "?";

    private void route(ClientConnection client, XRequest request) {
        currentRequest = request.getClass().getSimpleName();
        client.recordRequest(request.getClass());
        switch (request) {
            // ---- windows -------------------------------------------------
            case com.github.moaxcp.x11.protocol.xproto.CreateWindow r -> windows.create(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeWindowAttributes r ->
                    windows.changeAttributes(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetWindowAttributes r ->
                    windows.getAttributes(client, r);
            case com.github.moaxcp.x11.protocol.xproto.DestroyWindow r ->
                    windows.destroy(client, r);
            case com.github.moaxcp.x11.protocol.xproto.DestroySubwindows r ->
                    windows.destroySubwindows(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeSaveSet r ->
                    windows.changeSaveSet(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ReparentWindow r ->
                    windows.reparent(client, r);
            case com.github.moaxcp.x11.protocol.xproto.MapWindow r -> windows.map(client, r);
            case com.github.moaxcp.x11.protocol.xproto.MapSubwindows r ->
                    windows.mapSubwindows(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UnmapWindow r -> windows.unmap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UnmapSubwindows r ->
                    windows.unmapSubwindows(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ConfigureWindow r ->
                    windows.configure(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CirculateWindow r ->
                    windows.circulate(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetGeometry r -> windows.getGeometry(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryTree r -> windows.queryTree(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryBestSize r ->
                    windows.queryBestSize(client, r);
            case com.github.moaxcp.x11.protocol.xproto.TranslateCoordinates r ->
                    windows.translateCoordinates(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ClearArea r -> draw.clearArea(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CopyArea r -> draw.copyArea(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CopyPlane r -> draw.copyPlane(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetImage r -> draw.getImage(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PutImage r -> draw.putImage(client, r);

            // ---- drawing --------------------------------------------------
            case com.github.moaxcp.x11.protocol.xproto.PolyPoint r -> draw.polyPoint(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyLine r -> draw.polyLine(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolySegment r -> draw.polySegment(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyRectangle r ->
                    draw.polyRectangle(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyArc r -> draw.polyArc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FillPoly r -> draw.fillPoly(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyFillRectangle r ->
                    draw.polyFillRectangle(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyFillArc r -> draw.polyFillArc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyText8 r -> draw.polyText8(client, r);
            case com.github.moaxcp.x11.protocol.xproto.PolyText16 r -> draw.polyText16(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ImageText8 r -> draw.imageText8(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ImageText16 r -> draw.imageText16(client, r);

            // ---- graphics contexts, pixmaps, fonts, cursors ---------------
            case com.github.moaxcp.x11.protocol.xproto.CreateGC r -> resources.createGc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeGC r -> resources.changeGc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CopyGC r -> resources.copyGc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetDashes r -> resources.setDashes(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetClipRectangles r ->
                    resources.setClipRectangles(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FreeGC r -> resources.freeGc(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CreatePixmap r ->
                    resources.createPixmap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FreePixmap r -> resources.freePixmap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.OpenFont r -> resources.openFont(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CloseFont r -> resources.closeFont(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryFont r -> resources.queryFont(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryTextExtents r ->
                    resources.queryTextExtents(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListFonts r -> resources.listFonts(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListFontsWithInfo r ->
                    resources.listFontsWithInfo(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetFontPath r ->
                    resources.setFontPath(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetFontPath r ->
                    resources.getFontPath(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CreateCursor r ->
                    resources.createCursor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CreateGlyphCursor r ->
                    resources.createGlyphCursor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FreeCursor r ->
                    resources.freeCursor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.RecolorCursor r ->
                    resources.recolorCursor(client, r);

            // ---- colormaps -------------------------------------------------
            case com.github.moaxcp.x11.protocol.xproto.CreateColormap r ->
                    resources.createColormap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FreeColormap r ->
                    resources.freeColormap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.CopyColormapAndFree r ->
                    resources.copyColormapAndFree(client, r);
            case com.github.moaxcp.x11.protocol.xproto.InstallColormap r ->
                    resources.installColormap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UninstallColormap r ->
                    resources.uninstallColormap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListInstalledColormaps r ->
                    resources.listInstalledColormaps(client, r);
            case com.github.moaxcp.x11.protocol.xproto.AllocColor r ->
                    resources.allocColor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.AllocNamedColor r ->
                    resources.allocNamedColor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.AllocColorCells r ->
                    resources.allocColorCells(client, r);
            case com.github.moaxcp.x11.protocol.xproto.AllocColorPlanes r ->
                    resources.allocColorPlanes(client, r);
            case com.github.moaxcp.x11.protocol.xproto.FreeColors r ->
                    resources.freeColors(client, r);
            case com.github.moaxcp.x11.protocol.xproto.StoreColors r ->
                    resources.storeColors(client, r);
            case com.github.moaxcp.x11.protocol.xproto.StoreNamedColor r ->
                    resources.storeNamedColor(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryColors r ->
                    resources.queryColors(client, r);
            case com.github.moaxcp.x11.protocol.xproto.LookupColor r ->
                    resources.lookupColor(client, r);

            // ---- atoms, properties, selections -----------------------------
            case com.github.moaxcp.x11.protocol.xproto.InternAtom r -> atoms.internAtom(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetAtomName r -> atoms.getAtomName(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeProperty r ->
                    atoms.changeProperty(client, r);
            case com.github.moaxcp.x11.protocol.xproto.DeleteProperty r ->
                    atoms.deleteProperty(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetProperty r -> atoms.getProperty(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListProperties r ->
                    atoms.listProperties(client, r);
            case com.github.moaxcp.x11.protocol.xproto.RotateProperties r ->
                    atoms.rotateProperties(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetSelectionOwner r ->
                    atoms.setSelectionOwner(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetSelectionOwner r ->
                    atoms.getSelectionOwner(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ConvertSelection r ->
                    atoms.convertSelection(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SendEvent r -> atoms.sendEvent(client, r);

            // ---- input, focus and grabs ------------------------------------
            case com.github.moaxcp.x11.protocol.xproto.QueryPointer r ->
                    input.queryPointer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetMotionEvents r ->
                    input.getMotionEvents(client, r);
            case com.github.moaxcp.x11.protocol.xproto.WarpPointer r -> input.warpPointer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetInputFocus r ->
                    input.setInputFocus(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetInputFocus r ->
                    input.getInputFocus(client, r);
            case com.github.moaxcp.x11.protocol.xproto.QueryKeymap r -> input.queryKeymap(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GrabPointer r -> input.grabPointer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UngrabPointer r ->
                    input.ungrabPointer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GrabButton r -> input.grabButton(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UngrabButton r -> input.ungrabButton(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeActivePointerGrab r ->
                    input.changeActivePointerGrab(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GrabKeyboard r ->
                    input.grabKeyboard(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UngrabKeyboard r ->
                    input.ungrabKeyboard(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GrabKey r -> input.grabKey(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UngrabKey r -> input.ungrabKey(client, r);
            case com.github.moaxcp.x11.protocol.xproto.AllowEvents r -> input.allowEvents(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetPointerMapping r ->
                    input.setPointerMapping(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetPointerMapping r ->
                    input.getPointerMapping(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetModifierMapping r ->
                    input.setModifierMapping(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetModifierMapping r ->
                    input.getModifierMapping(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeKeyboardMapping r ->
                    input.changeKeyboardMapping(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetKeyboardMapping r ->
                    input.getKeyboardMapping(client, r);

            // ---- server and connection control ------------------------------
            case com.github.moaxcp.x11.protocol.xproto.GrabServer r -> misc.grabServer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.UngrabServer r -> misc.ungrabServer(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetCloseDownMode r ->
                    misc.setCloseDownMode(client, r);
            case com.github.moaxcp.x11.protocol.xproto.KillClient r -> misc.killClient(client, r);
            case com.github.moaxcp.x11.protocol.xproto.NoOperation r -> {
                // Nothing to do: the request carried no payload we need.
            }
            case com.github.moaxcp.x11.protocol.xproto.QueryExtension r ->
                    misc.queryExtension(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListExtensions r ->
                    misc.listExtensions(client, r);
            case com.github.moaxcp.x11.protocol.xproto.Bell r -> misc.bell(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetPointerControl r ->
                    misc.getPointerControl(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangePointerControl r ->
                    misc.changePointerControl(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetScreenSaver r ->
                    misc.getScreenSaver(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetScreenSaver r ->
                    misc.setScreenSaver(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ForceScreenSaver r ->
                    misc.forceScreenSaver(client, r);
            case com.github.moaxcp.x11.protocol.xproto.GetKeyboardControl r ->
                    misc.getKeyboardControl(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeKeyboardControl r ->
                    misc.changeKeyboardControl(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ChangeHosts r -> misc.changeHosts(client, r);
            case com.github.moaxcp.x11.protocol.xproto.ListHosts r -> misc.listHosts(client, r);
            case com.github.moaxcp.x11.protocol.xproto.SetAccessControl r ->
                    misc.setAccessControl(client, r);

            default -> throw ProtocolException.unimplemented(
                    "request " + request.getClass().getSimpleName());
        }
    }

    /**
     * Tears down everything a disconnecting client owned.
     *
     * <p>Windows are destroyed through the normal path so that clients selecting
     * {@code SubstructureNotify} still see them go away, and in bottom-up order so a child is
     * always destroyed before its parent.
     */
    public void destroyClientResources(ClientConnection client) {
        for (Extension extension : server.extensions().all()) {
            try {
                extension.clientDisconnected(server, client);
            } catch (RuntimeException e) {
                log.warn("extension {} failed during disconnect cleanup", extension.name(), e);
            }
        }
        ResourceTable table = server.resources();
        // The selections this client owns are released before its windows are destroyed, so the
        // subscribers are told the *client* went away rather than that each window did: a reference
        // server distinguishes the two, and a clipboard manager reacts differently - a destroyed
        // owner window is a window it can wait for, a closed client is one it has to replace.
        for (XResource resource : table.resourcesOf(client)) {
            if (resource instanceof XWindow window) {
                server.releaseSelectionsOwnedBy(window.id(), XFixesExtension.SELECTION_CLIENT_CLOSE);
            }
        }
        List<XResource> owned = table.resourcesOf(client);
        for (XResource resource : owned) {
            if (resource instanceof XWindow window && !window.isDestroyed()) {
                windows.destroyWindowTree(client, window);
            }
        }
        // Everything left (pixmaps, GCs, fonts, ...) is simply freed; freeing a window here is a
        // no-op because destroyWindowTree already removed it.
        for (XResource resource : table.resourcesOf(client)) {
            table.free(resource.id());
        }
    }
}
