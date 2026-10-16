#!/usr/bin/env python3
"""Capture a server's ListFonts / ListFontsWithInfo replies, request by request - backlog C10.

    python3 tools/font-reply-proxy.py <listen-port> <upstream-host> <upstream-port> <out-file>

Point a client at the listening port instead of at the server and the two requests this server's
font-set question turns on are written to the output file, framed so two servers' answers can be
diffed byte for byte:

    <opcode> <pattern> <hex of the reply or replies>

**Why a proxy and not a probe.** The question C10 is stuck on is what *differs* between a server that
can build a font set and one that cannot, and the two servers' answers to these two requests look the
same field by field (`xlsfonts -ll` prints the same properties, bounds and metrics, and
`tools/fontset-probe.py` shows the names were the one difference anything displayed). What is left is
the bytes nobody displays - `replies_hint`, the reply lengths, the number of replies - so the tool
that answers it is one that can show them, and the tool after that is one that can *replace* them (see
the item's notes: replay an Xvfb answer to this server's client and see whether the font set appears;
if it does, bisect the bytes).

**The blocking rule, which is the whole trick.** A reply is 32 bytes plus `4 * length_field`, and the
client's stream can also carry events, so the S->C direction is read as messages: a byte whose type is
1 is a reply - and while a ListFonts-family request is outstanding that reply belongs to it, while the
request is `ListFontsWithInfo` the replies continue until one whose name length is zero (the
terminator). Anything else is an event and is passed through untouched.

The C->S direction is parsed the same way: `opcode`, `data`, and the length in four byte units, so a
request of any size is consumed whole; only 49 (`ListFonts`) and 50 (`ListFontsWithInfo`) are
recorded, everything else is relayed and ignored.
"""

import socket
import struct
import sys
import threading

LIST_FONTS = 49
LIST_FONTS_WITH_INFO = 50


def read_exactly(stream, count):
    data = b""
    while len(data) < count:
        chunk = stream.recv(count - len(data))
        if not chunk:
            raise EOFError
        data += chunk
    return data


class Proxy:
    def __init__(self, listen_port, upstream_host, upstream_port, out_path):
        self.listen_port = listen_port
        self.upstream = (upstream_host, upstream_port)
        self.out = open(out_path, "w")
        self.lock = threading.Lock()
        # The request the client is waiting on, if it is one this tool cares about.
        self.pending = None

    def serve(self):
        server = socket.socket()
        server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        server.bind(("127.0.0.1", self.listen_port))
        server.listen(4)
        print("listening on 127.0.0.1:%d, upstream %s:%d, writing %s"
              % (self.listen_port, self.upstream[0], self.upstream[1], self.out.name))
        while True:
            client, _ = server.accept()
            upstream = socket.create_connection(self.upstream)
            threading.Thread(target=self.to_server, args=(client, upstream), daemon=True).start()
            threading.Thread(target=self.to_client, args=(upstream, client), daemon=True).start()

    # ---- client to server: parse requests, remember the ones that matter ----
    def to_server(self, client, upstream):
        try:
            # The connection setup request is the one message with no four byte header: order, pad,
            # major, minor, then the two lengths of the credentials. Read as a request it claims a
            # length of eleven units and the proxy waits for forty bytes that never come - which
            # reads as a client hanging on the setup reply.
            setup = read_exactly(client, 12)
            name_length = struct.unpack("<H", setup[6:8])[0]
            data_length = struct.unpack("<H", setup[8:10])[0]
            upstream.sendall(setup
                             + read_exactly(client, (name_length + 3) // 4 * 4
                                            + (data_length + 3) // 4 * 4))
            while True:
                header = read_exactly(client, 4)
                opcode = header[0]
                length_units = struct.unpack("<H", header[2:4])[0]
                body = read_exactly(client, max(length_units * 4 - 4, 0))
                with self.lock:
                    if opcode in (LIST_FONTS, LIST_FONTS_WITH_INFO) and len(body) >= 4:
                        pattern_length = struct.unpack("<H", body[2:4])[0]
                        self.pending = (opcode, body[4:4 + pattern_length].decode("latin-1"))
                upstream.sendall(header + body)
        except (EOFError, OSError):
            pass
        finally:
            for sock in (client, upstream):
                try:
                    sock.shutdown(socket.SHUT_RDWR)
                except OSError:
                    pass

    # ---- server to client: frame the replies so the caller sees message boundaries ----
    def to_client(self, upstream, client):
        try:
            # The connection setup reply is the one message with an eight byte header: its length
            # field counts what follows those eight bytes, where every later reply's counts what
            # follows thirty two. Reading it as a reply leaves eight bytes in the stream and puts
            # everything after it out of step - the trap that cost an hour in the probe first.
            header = read_exactly(upstream, 8)
            client.sendall(header + read_exactly(upstream,
                                                 struct.unpack("<H", header[6:8])[0] * 4))
            while True:
                header = read_exactly(upstream, 32)
                extra = struct.unpack("<I", header[4:8])[0] * 4
                message = header + (read_exactly(upstream, extra) if extra else b"")
                with self.lock:
                    pending = self.pending
                if pending is not None and header[0] == 1:
                    self.record(pending, message)
                client.sendall(message)
        except (EOFError, OSError):
            pass
        finally:
            for sock in (client, upstream):
                try:
                    sock.shutdown(socket.SHUT_RDWR)
                except OSError:
                    pass

    def record(self, pending, message):
        opcode, pattern = pending
        with self.lock:
            self.out.write("%d %s %s\n" % (opcode, pattern, message.hex()))
            self.out.flush()
            # One reply for ListFonts; a run ending in the terminator for ListFontsWithInfo, whose
            # terminator is the reply with a zero name length.
            if opcode == LIST_FONTS or message[1] == 0:
                self.pending = None


def main(argv):
    if len(argv) != 5:
        print(__doc__.strip())
        return 2
    Proxy(int(argv[1]), argv[2], int(argv[3]), argv[4]).serve()
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
