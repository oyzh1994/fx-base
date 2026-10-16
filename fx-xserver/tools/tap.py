#!/usr/bin/env python3
"""A byte-transparent TCP relay in front of a server, so a real client's stream can be read.

    tap.py <listen-port> <target-port> <log-file>

Then point the client at the display that is the listen port - X's display N is TCP port 6000+N,
so `tap.py 6099 6098 /tmp/tap.log` taps the server on :98 and the client runs with
`DISPLAY=host:99`. Nothing is parsed and nothing is rewritten: both directions are logged as
`<time> c<connection> C2S|S2C <bytes> <hex>` lines, which is what makes it usable where `XProxy`
is not (that one parses, and a client that objects to the parsing hangs - EasyShell does).

Logging is off until a *toggle file* exists, and stops when it goes away:

    touch /tmp/tap-on ; ...the gesture... ; rm /tmp/tap-on

so a tap can sit in front of a server for a whole session and be switched on for the ten seconds
that matter. Logging everything instead writes megabytes per second: a client repainting a window
sends its pixels through here.

Why this exists: it is the only way to read the *reference* answer. `docs/backlog.md` A16 is the
example - this server's `XIQueryPointer` reply named the deepest window instead of the queried
window's own child, which made EasyShell walk a tree with no bottom and ask 3700 times a second;
Xvfb cannot log its requests, and this server's own log cannot show what the client *sends*. The
tap gave the reference's three-line sequence (`GrabServer`, query the root, query the child, name
None, `UngrabServer`) that the reply had to match, and the fix took ten lines.
"""

import os
import socket
import sys
import threading
import time

LISTEN_PORT = int(sys.argv[1])
TARGET_PORT = int(sys.argv[2])
LOG_PATH = sys.argv[3]
TOGGLE = os.environ.get("TAP_TOGGLE", "/tmp/tap-on")

log_lock = threading.Lock()
log_file = open(LOG_PATH, "wb", buffering=0)
connections = [0]


def pump(source, sink, direction, connection):
    """Copies one direction of one connection, logging while the toggle file exists."""
    try:
        while True:
            data = source.recv(1 << 16)
            if not data:
                break
            if os.path.exists(TOGGLE):
                with log_lock:
                    log_file.write(
                        f"{time.time():.3f} c{connection} {direction} {len(data)} ".encode()
                        + data.hex().encode() + b"\n"
                    )
            sink.sendall(data)
    except OSError:
        pass
    finally:
        try:
            sink.shutdown(socket.SHUT_WR)
        except OSError:
            pass


def main():
    server = socket.socket()
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind(("0.0.0.0", LISTEN_PORT))
    server.listen(8)
    while True:
        client, _ = server.accept()
        with log_lock:
            connections[0] += 1
            connection = connections[0]
        upstream = socket.create_connection(("127.0.0.1", TARGET_PORT))
        # A thread per direction, so a client that is waiting for a reply does not stop the reply
        # from being logged - which is exactly the state this is used to look at.
        threading.Thread(target=pump, args=(client, upstream, "C2S", connection),
                         daemon=True).start()
        threading.Thread(target=pump, args=(upstream, client, "S2C", connection),
                         daemon=True).start()


if __name__ == "__main__":
    main()
