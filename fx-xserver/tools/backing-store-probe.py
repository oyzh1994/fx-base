#!/usr/bin/env python3
"""Does a server keep a window's pixels while something covers it?

The harness for backlog A9's backing store half, and the measurement that settled it. It is Python
rather than one of the Java tools because it needs the parts of Xlib the hand-written protocol
client has no reason to grow - `XChangeWindowAttributes` with a value list, and `XGetImage` on the
root - and because a second opinion from *Xlib* is worth more here than another hand-built request.

    python3 tools/backing-store-probe.py <display> [none|backing|save]

Run it against a reference server and against this one. What it does: a red window at 100,100 is
mapped, a blue one is mapped over the middle of it, and then the blue one is unmapped. Two things
are read - the pixel where the blue one was, and how many `Expose` events the red one is sent.

The three modes are the three answers a server can give:

  none     the blue window asks for nothing. Every server measured sends the red one an `Expose`,
           because the red one's pixels were painted over and it is the client's job to draw them
           again. Xvfb: 1 expose. This server: 1 expose.
  backing  the red window asks for a backing store (`backing_store = WhenMapped`), which is a client
           saying "keep my pixels while I am covered". Xvfb: **0 exposes**, and the pixels are back.
           This server before A9: 1 expose. After: 0.
  save     the blue window asks for `save_under`. **Neither server implements it**: Xvfb advertises
           `save-unders NO` in its setup reply and behaves exactly as it does in `none`, and so does
           this one. That is why the item is closed with `saveUnders(false)` still in Screen.

The reference's own capability bits come from `xdpyinfo | grep options`:
`backing-store WHEN MAPPED, save-unders NO` for Xvfb, against this server's `backing-store NO` before
A9 and `WHEN MAPPED` after.
"""
import ctypes
import os
import sys
import time

lib = ctypes.CDLL("libX11.so.6")

lib.XOpenDisplay.restype = ctypes.c_void_p
lib.XOpenDisplay.argtypes = [ctypes.c_char_p]
lib.XDefaultRootWindow.restype = ctypes.c_ulong
lib.XDefaultRootWindow.argtypes = [ctypes.c_void_p]
lib.XCreateSimpleWindow.restype = ctypes.c_ulong
lib.XCreateSimpleWindow.argtypes = [ctypes.c_void_p, ctypes.c_ulong, ctypes.c_int, ctypes.c_int,
                                    ctypes.c_uint, ctypes.c_uint, ctypes.c_uint, ctypes.c_ulong,
                                    ctypes.c_ulong]
lib.XChangeWindowAttributes.argtypes = [ctypes.c_void_p, ctypes.c_ulong, ctypes.c_ulong,
                                        ctypes.c_void_p]
lib.XMapWindow.argtypes = [ctypes.c_void_p, ctypes.c_ulong]
lib.XUnmapWindow.argtypes = [ctypes.c_void_p, ctypes.c_ulong]
lib.XSelectInput.argtypes = [ctypes.c_void_p, ctypes.c_ulong, ctypes.c_long]
lib.XSync.argtypes = [ctypes.c_void_p, ctypes.c_int]
lib.XPending.restype = ctypes.c_int
lib.XPending.argtypes = [ctypes.c_void_p]
lib.XNextEvent.argtypes = [ctypes.c_void_p, ctypes.c_void_p]
lib.XGetImage.restype = ctypes.c_void_p
lib.XGetImage.argtypes = [ctypes.c_void_p, ctypes.c_ulong, ctypes.c_int, ctypes.c_int,
                          ctypes.c_uint, ctypes.c_uint, ctypes.c_ulong, ctypes.c_int]
lib.XGetPixel.restype = ctypes.c_ulong
lib.XGetPixel.argtypes = [ctypes.c_void_p, ctypes.c_int, ctypes.c_int]
lib.XDestroyImage.argtypes = [ctypes.c_void_p]

CW_BACKING_STORE = 1 << 6
CW_SAVE_UNDER = 1 << 10
EXPOSURE_MASK = 1 << 15
EXPOSE = 12
Z_PIXMAP = 2
RED = 0xFF0000
BLUE = 0x0000FF

EXPOSES = 0


class XSetWindowAttributes(ctypes.Structure):
    """`XSetWindowAttributes`, whose offsets are the C struct's: only the two fields used are set."""

    _fields_ = [
        ("background_pixmap", ctypes.c_ulong),
        ("background_pixel", ctypes.c_ulong),
        ("border_pixmap", ctypes.c_ulong),
        ("border_pixel", ctypes.c_ulong),
        ("bit_gravity", ctypes.c_int),
        ("win_gravity", ctypes.c_int),
        ("backing_store", ctypes.c_int),
        ("backing_planes", ctypes.c_ulong),
        ("backing_pixel", ctypes.c_ulong),
        ("save_under", ctypes.c_int),
        ("event_mask", ctypes.c_long),
        ("do_not_propagate_mask", ctypes.c_long),
        ("override_redirect", ctypes.c_int),
        ("colormap", ctypes.c_ulong),
        ("cursor", ctypes.c_ulong),
    ]


def main():
    display = os.environ.get("DISPLAY", ":0")
    mode = sys.argv[2] if len(sys.argv) > 2 else "none"
    d = lib.XOpenDisplay(display.encode())
    if not d:
        print("cannot open", display)
        return 2
    root = lib.XDefaultRootWindow(d)

    lower = lib.XCreateSimpleWindow(d, root, 100, 100, 200, 200, 0, RED, RED)
    lib.XSelectInput(d, lower, EXPOSURE_MASK)
    if mode == "backing":
        attributes = XSetWindowAttributes()
        attributes.backing_store = 1               # WhenMapped
        lib.XChangeWindowAttributes(d, lower, CW_BACKING_STORE, ctypes.byref(attributes))
    lib.XMapWindow(d, lower)
    settle(d)

    upper = lib.XCreateSimpleWindow(d, root, 150, 150, 100, 100, 0, BLUE, BLUE)
    if mode == "save":
        attributes = XSetWindowAttributes()
        attributes.save_under = 1
        lib.XChangeWindowAttributes(d, upper, CW_SAVE_UNDER, ctypes.byref(attributes))
    lib.XMapWindow(d, upper)
    settle(d)
    covered = pixel(d, root, 200, 200)

    global EXPOSES
    EXPOSES = 0
    lib.XUnmapWindow(d, upper)
    settle(d)
    revealed = pixel(d, root, 200, 200)

    print("display=%-20s mode=%-8s covered=0x%06x revealed=0x%06x exposes=%d  -> %s"
          % (display, mode, covered, revealed, EXPOSES,
             "RESTORED" if revealed == RED else "left as the window above drew it"))
    return 0


def settle(d):
    """Runs the display's queue and waits a moment, counting every Expose on the lower window."""
    global EXPOSES
    deadline = time.time() + 1.0
    event = ctypes.create_string_buffer(192)
    while time.time() < deadline:
        lib.XSync(d, 0)
        while lib.XPending(d) > 0:
            lib.XNextEvent(d, event)
            if event.raw[0] == EXPOSE:
                EXPOSES += 1             # lower is the only window this client asked to hear from
        time.sleep(0.02)


def pixel(d, drawable, x, y):
    image = lib.XGetImage(d, drawable, x, y, 1, 1, 0xFFFFFFFF, Z_PIXMAP)
    if not image:
        return -1
    value = lib.XGetPixel(image, 0, 0)
    lib.XDestroyImage(image)
    return value


sys.exit(main())
