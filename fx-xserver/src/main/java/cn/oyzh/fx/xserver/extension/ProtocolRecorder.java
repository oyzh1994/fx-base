package cn.oyzh.fx.xserver.extension;

import cn.oyzh.fx.xserver.client.ClientConnection;

/**
 * Where the core tells an extension what every client sent and received, which is what RECORD is
 * (F2).
 *
 * <p>There are four places in the server where a message is visible as a whole - a request as it
 * arrived, a message on its way out, a connection's setup reply, and a connection ending - and each
 * of them calls one method here. Nothing else in the server knows RECORD exists, and a server with
 * no record context holds {@link #NONE}, so the calls cost a virtual call and nothing else.
 *
 * <p><b>The messages are handed over as bytes</b>, in the byte order of the client they came from or
 * are going to, because that is what a recorder replays: a datum's payload is the recorded message
 * verbatim, and whether it needs byte swapping is the recorder's business (the {@code clientSwapped}
 * field says so).
 *
 * <p>Calls happen while the server lock is held - a request from the client's own request thread, an
 * event from whichever thread the event was produced on - so an implementation must not block. It
 * must also not throw: a recorder that has gone away must not fail the client whose message it was
 * about to record.
 */
public interface ProtocolRecorder {

    /** A recorder for a server that records nothing, so the call sites need no null check. */
    ProtocolRecorder NONE = new ProtocolRecorder() {

        @Override
        public void clientRequest(ClientConnection client, byte[] request, short sequenceNumber) {
        }

        @Override
        public void serverMessage(ClientConnection recipient, byte[] message,
                                  ClientConnection answeringClient, short answeringSequence) {
        }

        @Override
        public void clientStarted(ClientConnection client, byte[] setupReply) {
        }

        @Override
        public void clientDied(ClientConnection client) {
        }
    };

    /**
     * One client's request, as it arrived on the wire and before it was decoded.
     *
     * @param client         the client that sent it
     * @param request        the request's bytes, including its header
     * @param sequenceNumber the client's own sequence number for it
     */
    void clientRequest(ClientConnection client, byte[] request, short sequenceNumber);

    /**
     * One message on its way to a client: a reply, an event or an error.
     *
     * <p>{@code answeringClient} is the client whose request was being executed when the message was
     * produced, and {@code answeringSequence} its sequence number - or null and zero when the message
     * was produced by the server itself rather than by a request (a pointer event, a screen change).
     * That distinction is the recorded {@code clientResource} and {@code recordedSequenceNumber}, and
     * a reference decides it the same way: a reply carries the client that asked, an event with no
     * request behind it carries nothing.
     */
    void serverMessage(ClientConnection recipient, byte[] message, ClientConnection answeringClient,
                       short answeringSequence);

    /**
     * A connection's setup reply, which RECORD delivers as the client-started element: the bytes a
     * recorder needs to rebuild the client that produced the rest of the stream.
     */
    void clientStarted(ClientConnection client, byte[] setupReply);

    /** A connection ended. The client's last sequence number is {@link ClientConnection#sequenceNumber()}. */
    void clientDied(ClientConnection client);
}
