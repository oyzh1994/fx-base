package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.xc_misc.GetVersion;
import com.github.moaxcp.x11.protocol.xc_misc.GetVersionReply;
import com.github.moaxcp.x11.protocol.xc_misc.GetXIDList;
import com.github.moaxcp.x11.protocol.xc_misc.GetXIDListReply;
import com.github.moaxcp.x11.protocol.xc_misc.GetXIDRange;
import com.github.moaxcp.x11.protocol.xc_misc.GetXIDRangeReply;
import com.github.moaxcp.x11.protocol.xc_misc.XcMiscplugin;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.resource.ResourceTable;
import org.eclipse.collections.impl.factory.primitive.IntLists;

import java.util.ArrayList;
import java.util.List;

/**
 * XC-MISC.
 *
 * <p>Xlib keeps a pool of resource ids it has not used yet and refills it by asking the server for
 * a range. Without this extension a long running client that exhausts its pool cannot get more, so
 * it is worth answering even though the server never allocates ids on the client's behalf.
 */
public final class XCMiscExtension implements Extension {

    private static final short MAJOR_VERSION = 1;
    private static final short MINOR_VERSION = 1;

    /** How many ids to hand out at a time when a client asks for a range. */
    private static final int RANGE_SIZE = 1024;

    private final XcMiscplugin plugin = new XcMiscplugin();

    @Override
    public String name() {
        return "XC-MISC";
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
        if (request instanceof GetVersion version) {
            getVersion(client, version);
        } else if (request instanceof GetXIDRange range) {
            getXidRange(server, client, range);
        } else if (request instanceof GetXIDList list) {
            getXidList(server, client, list);
        } else {
            throw new IllegalArgumentException("unhandled XC-MISC request " + request.getClass());
        }
    }

    private void getVersion(ClientConnection client, GetVersion request) {
        client.sendReply(GetVersionReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .serverMajorVersion(MAJOR_VERSION)
                .serverMinorVersion(MINOR_VERSION)
                .build());
    }

    private void getXidRange(XServer server, ClientConnection client, GetXIDRange request) {
        int start = client.resourceIdBase() | 1;
        client.sendReply(GetXIDRangeReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .startId(start)
                .count(RANGE_SIZE)
                .build());
    }

    private void getXidList(XServer server, ClientConnection client, GetXIDList request) {
        int count = request.getCount();
        if (count <= 0) {
            org.eclipse.collections.api.list.primitive.IntList empty = IntLists.immutable.empty();
            client.sendReply(GetXIDListReply.builder()
                    .sequenceNumber(client.sequenceNumber())
                    .ids(empty)
                    .build());
            return;
        }
        ResourceTable table = server.resources();
        List<Integer> ids = new ArrayList<>(count);
        int candidate = client.resourceIdBase() | 1;
        int limit = client.resourceIdBase() | client.resourceIdMask();
        while (ids.size() < count && candidate <= limit) {
            if (!table.contains(candidate)) {
                ids.add(candidate);
            }
            candidate++;
        }
        if (ids.size() < count) {
            throw new cn.oyzh.fx.xserver.client.ProtocolException(
                    cn.oyzh.fx.xserver.client.ProtocolException.ALLOC, count,
                    "no free resource ids");
        }
        client.sendReply(GetXIDListReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .ids(IntLists.immutable.ofAll(ids))
                .build());
    }
    /**
     * Visible to an untrusted client: this extension only asks the server about itself, which is
     * what a reference leaves visible to one (measured - see {@code SecurityExtension}).
     */
    @Override
    public boolean visibleToUntrustedClients() {
        return true;
    }

}
