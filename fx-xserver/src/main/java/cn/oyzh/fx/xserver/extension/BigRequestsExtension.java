package cn.oyzh.fx.xserver.extension;

import com.github.moaxcp.x11.protocol.XProtocolPlugin;
import com.github.moaxcp.x11.protocol.XRequest;
import com.github.moaxcp.x11.protocol.bigreq.BigreqPlugin;
import com.github.moaxcp.x11.protocol.bigreq.Enable;
import com.github.moaxcp.x11.protocol.bigreq.EnableReply;
import cn.oyzh.fx.xserver.XServer;
import cn.oyzh.fx.xserver.client.ClientConnection;
import cn.oyzh.fx.xserver.extension.Extension;

/**
 * BIG-REQUESTS.
 *
 * <p>Without it a request cannot exceed 256 KB, which real clients hit constantly: a full screen
 * {@code PutImage}, a wallpaper sized {@code ChangeProperty}, a large paste. Enabling it lets a
 * client mark a request with a zero length field and put the real length in the next four bytes.
 *
 * <p>The encoding is handled in the request loop, not here; this extension only has to answer
 * {@code Enable} with the size the server is willing to accept.
 */
public final class BigRequestsExtension implements Extension {

    /** Largest request we accept, in 4 byte units: the protocol maximum of 4 GB / 4. */
    private static final int MAXIMUM_REQUEST_LENGTH = (1 << 22) - 1;

    private final BigreqPlugin plugin = new BigreqPlugin();

    @Override
    public String name() {
        return "BIG-REQUESTS";
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
        if (request instanceof Enable enable) {
            enable(server, client, enable);
            return;
        }
        throw new IllegalArgumentException("unhandled BIG-REQUESTS request " + request.getClass());
    }

    private void enable(XServer server, ClientConnection client, Enable request) {
        client.enableBigRequests();
        client.sendReply(EnableReply.builder()
                .sequenceNumber(client.sequenceNumber())
                .maximumRequestLength(MAXIMUM_REQUEST_LENGTH)
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
