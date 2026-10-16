#!/usr/bin/env python3
"""Ask Xlib itself whether a server can build a font set for a base font name - backlog C10.

    python3 tools/fontset-probe.py <display> [base-font-name ...]

Prints, for each name: what ``XListFonts`` matches, and what ``XCreateFontSet`` makes of it -
``OK`` plus the list of charsets it could not find a font for, or ``NULL`` plus the list, which is
what a client sees as "no font set" and what makes an X input method unusable.

**Why Xlib and not the wire.** The question is not "what did the server answer" but "what does a
client make of it", and the two came apart here: the replies looked right field by field against a
reference server - ``xlsfonts -ll`` prints the same properties, the same bounds, the same metrics -
and the font set still failed. Only the library that builds font sets can say why, and it is one
``ctypes`` call away.

**Run it under the locale that matters.** ``XCreateFontSet`` resolves the base name against the
locale's font set definition (``/usr/share/X11/locale/<locale>/XLC_LOCALE``), so the charsets it
asks for are the locale's: a UTF-8 Chinese locale asks for ISO8859-1, GB2312.1980-0, GBK-0 and
ISO10646-1. Measured against Xvfb and against this server on 2026-09-26 (both on the VM, with
``LANG=zh_CN.UTF-8``):

    Xvfb          XListFonts("fixed") -> 2 names, both "fixed"
                  XCreateFontSet("fixed") -> OK, missing GB2312.1980-0, GBK-0, ISO10646-1
    this server   XListFonts("fixed") -> 1 name, "fixed"
                  XCreateFontSet("fixed") -> NULL, missing ISO8859-1 twice and those three

so the reference satisfies the ISO8859-1 entries - which is the difference that decides between
"the font set is not as complete as it could be" and "there is no font set". Reported against this
server, the second line is what ``xterm`` prints as

    xterm: Preparation of font set "fixed" for XIM failed.
    xterm: Failed to create input context

**What it found, and it took three fixes rather than one.** The probe's first measurement said
``XCreateFontSet`` answered NULL here and OK against Xvfb, and each step after that was one more
place where a font this server *names* could not be *loaded*: ``ListFontsWithInfo`` reported the
alias instead of the font's own XLFD name; a font loaded by that XLFD name missed the table and fell
back to the Java approximation, which has **no charset properties at all** (checked with
``XGetFontProperty``: the reference answers ``ISO8859``/``1``, the approximation answers neither);
and a *pattern* - what ``XCreateFontSet`` actually builds from a base name and a charset - was
treated as an unknown name. With all three fixed the answer here matches the reference's, and
``ibus`` composing through XIM into a forwarded ``xterm`` commits Chinese (backlog C10).

The next measurement, if a font set ever fails again, is ``tools/font-reply-proxy.py``: capture both
servers' ``ListFonts``/``ListFontsWithInfo`` replies and diff the fields nothing displays.
"""

import ctypes
import sys


def load_xlib():
    x11 = ctypes.CDLL("libX11.so.6")
    x11.XOpenDisplay.restype = ctypes.c_void_p
    x11.XOpenDisplay.argtypes = [ctypes.c_char_p]
    x11.XListFonts.restype = ctypes.POINTER(ctypes.c_char_p)
    x11.XListFonts.argtypes = [ctypes.c_void_p, ctypes.c_char_p, ctypes.c_int,
                               ctypes.POINTER(ctypes.c_int)]
    x11.XCreateFontSet.restype = ctypes.c_void_p
    x11.XCreateFontSet.argtypes = [
        ctypes.c_void_p, ctypes.c_char_p,
        ctypes.POINTER(ctypes.POINTER(ctypes.c_char_p)),   # missing_charset_list
        ctypes.POINTER(ctypes.c_int),                      # missing_charset_count
        ctypes.POINTER(ctypes.c_char_p)]                   # def_string
    return x11


def main(argv):
    if len(argv) < 2:
        print(__doc__.strip())
        return 2
    display = argv[1]
    patterns = argv[2:] or ["fixed"]
    x11 = load_xlib()
    dpy = x11.XOpenDisplay(display.encode())
    if not dpy:
        print("cannot open display %s" % display)
        return 1
    for pattern in patterns:
        count = ctypes.c_int(0)
        names = x11.XListFonts(ctypes.c_void_p(dpy), pattern.encode(), 64, ctypes.byref(count))
        print("XListFonts(%r) -> %d name(s)" % (pattern, count.value))
        for i in range(count.value):
            print("    %r" % names[i].decode())

        missing = ctypes.POINTER(ctypes.c_char_p)()
        missing_count = ctypes.c_int(0)
        default = ctypes.c_char_p()
        font_set = x11.XCreateFontSet(ctypes.c_void_p(dpy), pattern.encode(),
                                      ctypes.byref(missing), ctypes.byref(missing_count),
                                      ctypes.byref(default))
        print("XCreateFontSet(%r) -> %s, %d missing charset(s)"
              % (pattern, "OK" if font_set else "NULL", missing_count.value))
        for i in range(missing_count.value):
            print("    %s" % missing[i].decode())
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
