#!/usr/bin/env python3
"""Measure what the VNC backend puts on the wire, and what a client ends up seeing.

The other half of "much slower than Xvfb+x11vnc, especially EasyShell". The frame path was
per-pixel (done, see memory/xserver-frame-path-byte-at-a-time.md); this measures the wire, which is
where a viewer that an application redraws identical content at 300 Hz found 5.4 MB/s of updates for
an unchanging screen while `x11vnc` sent nothing. A hand-written RFB client, because the number that
matters is what an actual client receives:

    tools/vnc-meter.py <host> <port> <seconds> [--label NAME] [--enc 16,0] [--trace] [--dump FILE]

It does the handshake, asks for ZRLE (the encoding every desktop client can decode after raw), then
keeps **one incremental request outstanding** - a real client's own pace, so a server that answers
with nothing is measured as answering with nothing - and prints per second:

    MB/s  updates/s  seconds with a request held

"Held" is the interesting column: it is the time the server sat on a request because the screen had
not changed, which is how `x11vnc` behaves and what this server was not doing.

`--trace` prints every update's rectangle and `--dump` writes the client's own decoded picture as an
XWD, so it can be diffed against a capture of the server's framebuffer with the kit's own tool:

    tools/vnc-meter.py 192.168.22.152 5995 10 --dump /tmp/client.xwd &
    tools/vm.sh 'DISPLAY=:95 xwd -root -silent > /tmp/server.xwd'   # and pull it back
    tools/kit.sh Xwd diff /tmp/server.xwd /tmp/client.xwd

Two pixels that differ is a real bug; a handful inside the text cursor's cell is the blink racing the
capture. The ZRLE decoder covers the four subencodings this server emits (raw, solid, packed palette,
plain RLE) and a 3-byte CPIXEL, which is what a 32bpp/24-depth client is sent - not a general ZRLE
implementation, and it says so rather than guessing when it meets something else.
"""

import socket
import struct
import sys
import time
import zlib

TIGHT, ZRLE, RAW, COPYRECT, DESKTOP_SIZE, CURSOR = 7, 16, 0, 1, -223, -239


class Zrle:
    """The client's own picture, ZRLE by ZRLE (RFB 7.7.6)."""

    def __init__(self, width, height):
        self.width = width
        self.height = height
        self.pixels = [0] * (width * height)
        self.inflate = zlib.decompressobj()

    def rect(self, x, y, w, h, data):
        raw = self.inflate.decompress(data)
        at = 0
        for tile_y in range(0, h, 64):
            tile_h = min(64, h - tile_y)
            for tile_x in range(0, w, 64):
                tile_w = min(64, w - tile_x)
                n = tile_w * tile_h
                sub = raw[at]
                at += 1
                tile = [0] * n
                if sub == 0:                             # raw
                    for i in range(n):
                        tile[i] = int.from_bytes(raw[at:at + 3], "little")
                        at += 3
                elif sub == 1:                           # solid
                    colour = int.from_bytes(raw[at:at + 3], "little")
                    at += 3
                    tile = [colour] * n
                elif 2 <= sub <= 16:                     # packed palette
                    palette = []
                    for _ in range(sub):
                        palette.append(int.from_bytes(raw[at:at + 3], "little"))
                        at += 3
                    bits = 1 if sub == 2 else 2 if sub <= 4 else 4
                    per_byte = 8 // bits
                    for i in range(n):
                        if i % per_byte == 0:
                            byte = raw[at]
                            at += 1
                        shift = 8 - bits * (i % per_byte + 1)
                        tile[i] = palette[(byte >> shift) & ((1 << bits) - 1)]
                elif sub == 128:                         # plain RLE
                    i = 0
                    while i < n:
                        colour = int.from_bytes(raw[at:at + 3], "little")
                        at += 3
                        length = 1
                        while True:
                            b = raw[at]
                            at += 1
                            length += b
                            if b != 255:
                                break
                        for k in range(length):
                            if i + k < n:
                                tile[i + k] = colour
                        i += length
                else:
                    raise SystemExit("ZRLE subencoding %d is not one this server sends" % sub)
                for ty in range(tile_h):
                    base = (y + tile_y + ty) * self.width + x + tile_x
                    self.pixels[base:base + tile_w] = tile[ty * tile_w:(ty + 1) * tile_w]

    def xwd(self, path):
        """The view as the XWD the kit's Xwd tool reads, so two of them can be diffed."""
        body = bytearray()
        for pixel in self.pixels:
            # Field 7 says the pixels are LSBFirst and the masks are the server's own, so a pixel is
            # three bytes blue, green, red and the unused one - the order Xwd.java reads them in.
            for shift in (0, 8, 16):
                body.append((pixel >> shift) & 0xFF)
            body.append(0)
        # header_size is in *bytes* (100 = 25 ints, no colour map); a fraction of a row of error here
        # still produces something that looks like a picture, which is how it was found.
        head = [25 * 4, 7, 2, 24, self.width, self.height, 0, 0, 32, 0, 32, 32, self.width * 4, 4,
                0xFF0000, 0x00FF00, 0x0000FF, 8, 256, 0, self.width, self.height, 0, 0, 0]
        out = bytearray()
        for value in head:
            out += struct.pack(">I", value)
        out += body
        with open(path, "wb") as file:
            file.write(out)


class Client:
    """One RFB client, counting what it is sent."""

    def __init__(self, host, port, encodings):
        self.sock = socket.create_connection((host, port), timeout=15)
        self.sock.setsockopt(socket.IPPROTO_TCP, socket.TCP_NODELAY, 1)
        self.read = 0
        self.updates = 0
        self.rects = 0
        self.pixels = 0
        self.encodings = encodings
        self.last_rect = (0, 0, 0, 0)
        self.view = None
        self.jpgs = 0
        self.handshake()

    def recv(self, n):
        out = bytearray()
        while len(out) < n:
            chunk = self.sock.recv(n - len(out))
            if not chunk:
                raise EOFError("server closed after %d bytes" % self.read)
            out += chunk
        self.read += n
        return bytes(out)

    def handshake(self):
        version = self.recv(12)
        if not version.startswith(b"RFB "):
            raise SystemExit("not RFB: %r" % version)
        minor = int(version[8:11])
        self.sock.sendall(b"RFB 003.008\n" if minor >= 8 else b"RFB 003.003\n")
        if minor >= 7:
            count = self.recv(1)[0]
            offered = list(self.recv(count))
            if 1 not in offered:
                raise SystemExit("no 'None' security type; the server offered %r" % offered)
            self.sock.sendall(b"\x01")
        if minor >= 8:
            result = struct.unpack(">I", self.recv(4))[0]
            if result != 0:
                raise SystemExit("security result %d" % result)
        self.sock.sendall(b"\x01")                       # ClientInit: shared
        init = self.recv(24)
        self.width, self.height = struct.unpack(">HH", init[:4])
        name_len = struct.unpack(">I", init[20:24])[0]
        self.name = self.recv(name_len).decode("utf-8", "replace")
        # 32bpp little endian, red at 16: what a desktop client asks for, and what makes a CPIXEL
        # three bytes rather than four.
        fmt = struct.pack(">BBBBHHHBBBBBB", 32, 24, 0, 1, 255, 255, 255, 16, 8, 0, 0, 0, 0)
        self.sock.sendall(b"\x00" * 4 + fmt)             # SetPixelFormat: type + 3 padding
        self.sock.sendall(b"\x02\x00" + struct.pack(">H", len(self.encodings))
                          + b"".join(struct.pack(">i", e) for e in self.encodings))

    def decode_into(self, view):
        self.view = view

    def request(self, incremental=True):
        self.sock.sendall(struct.pack(">BBHHHH", 3, 1 if incremental else 0,
                                      0, 0, self.width, self.height))

    def skip(self, n):
        while n > 0:
            self.recv(min(n, 1 << 16))
            n -= min(n, 1 << 16)

    def one_update(self):
        """Reads until one FramebufferUpdate has been consumed."""
        while True:
            kind = self.recv(1)[0]
            if kind == 0:                                # FramebufferUpdate
                self.recv(1)
                rects = struct.unpack(">H", self.recv(2))[0]
                for _ in range(rects):
                    x, y, w, h, enc = struct.unpack(">HHHHi", self.recv(12))
                    self.last_rect = (x, y, w, h)
                    self.skip_payload(enc, x, y, w, h)
                self.updates += 1
                self.rects += rects
                return rects
            if kind == 3:                                # ServerCutText
                self.recv(3)
                self.skip(struct.unpack(">I", self.recv(4))[0])
                continue
            if kind == 2:                                # Bell
                continue
            raise SystemExit("unexpected server message %d" % kind)

    def skip_payload(self, enc, x, y, w, h):
        if enc == TIGHT:
            # This server sends Tight only as its JPEG method (control byte 0x90): a compact
            # one-to-three byte length and then the JFIF stream.
            control = self.recv(1)[0]
            if control != 0x90:
                raise SystemExit("Tight control 0x%02x is not the JPEG method this server sends"
                                 % control)
            length = self.compact_length()
            self.skip(length)
            self.jpgs += 1
        elif enc == ZRLE:
            length = struct.unpack(">I", self.recv(4))[0]
            data = self.recv(length)
            if self.view is not None:
                self.view.rect(x, y, w, h, data)
        elif enc == RAW:
            self.pixels += w * h
            raw = self.recv(w * h * 4)
            if self.view is not None:
                for i in range(w * h):
                    self.view.pixels[(y + i // w) * self.view.width + x + i % w] = \
                        int.from_bytes(raw[i * 4:i * 4 + 3], "little")
        elif enc == COPYRECT:
            self.skip(4)
        elif enc == DESKTOP_SIZE:
            self.width, self.height = w, h
        elif enc == CURSOR:
            self.skip(w * h * 4 + ((w + 7) // 8) * h)
        else:
            raise SystemExit("server sent encoding %d, which this meter cannot measure" % enc)

    def compact_length(self):
        """Tight's length in one, two or three bytes, seven bits at a time."""
        first = self.recv(1)[0]
        if first & 0x80 == 0:
            return first
        second = self.recv(1)[0]
        if second & 0x80 == 0:
            return (first & 0x7F) | second << 7
        third = self.recv(1)[0]
        return (first & 0x7F) | (second & 0x7F) << 7 | third << 14


def main():
    args = sys.argv[1:]
    host, port, seconds = args[0], int(args[1]), float(args[2])
    label = "run"
    encodings = [ZRLE, RAW, COPYRECT, DESKTOP_SIZE]
    if "--label" in args:
        label = args[args.index("--label") + 1]
    trace = "--trace" in args
    dump = args[args.index("--dump") + 1] if "--dump" in args else None
    if "--enc" in args:
        encodings = [int(e) for e in args[args.index("--enc") + 1].split(",")]
    client = Client(host, port, encodings)
    if dump:
        client.decode_into(Zrle(client.width, client.height))
    print("%s: %s %dx%d, %d encoding(s)" % (label, client.name, client.width, client.height,
                                            len(encodings)))
    client.request(incremental=False)                    # the first frame, not measured
    client.one_update()
    mark_bytes, mark_updates = client.read, client.updates
    client.sock.settimeout(0.5)
    start = time.time()
    deadline = start + seconds
    last = start
    last_bytes = mark_bytes
    outstanding = False
    held = 0.0
    try:
        while time.time() < deadline:
            if not outstanding:
                client.request()
                outstanding = True
            try:
                client.one_update()
                outstanding = False
            except socket.timeout:
                held += 0.5                          # the server is holding the request: no damage
                continue
            now = time.time()
            if trace:
                print("  %7.3fs  update %d: %dx%d at %d,%d  (%d bytes so far)"
                      % (now - start, client.updates, client.last_rect[2], client.last_rect[3],
                         client.last_rect[0], client.last_rect[1], client.read))
            if now - last >= 1.0:
                print("  %5.1fs  %8.2f MB/s  %6.1f updates/s  %5.1fs held"
                      % (now - start, (client.read - last_bytes) / (now - last) / 1e6,
                         (client.updates - mark_updates) / (now - last), held))
                last, last_bytes, held = now, client.read, 0.0
                mark_updates = client.updates
    except EOFError as e:
        print("  the server closed: %s" % e)
    if dump:
        client.view.xwd(dump)
        print("wrote %s (%d bytes decoded)" % (dump, len(client.view.pixels)))
    elapsed = max(1e-6, time.time() - start)
    total = client.read - mark_bytes
    print("%s: %.2f MB in %.1fs = %.2f MB/s, %d updates (%.1f/s), %d JPEG rectangles, %.1fs with a"
          " request held"
          % (label, total / 1e6, elapsed, total / elapsed / 1e6,
             client.updates - mark_updates, (client.updates - mark_updates) / elapsed, client.jpgs,
             held))


main()
