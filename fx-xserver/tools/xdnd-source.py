#!/usr/bin/env python3
"""A real GTK3 drag source, for XDND work (backlog C5).

    python3 tools/xdnd-source.py [x] [y]      # a window with DRAG ME in it

Run it together with `tools/xdnd-target.py` **in a separate process** and drive a drag from
one to the other with XTEST (`xdotool mousedown 1`, a few `mousemove`s, `mouseup 1`). Two
processes is the whole point: a drag between two windows of *one* GTK application never
touches XDND at all - GDK shortcuts it internally, so a same-process pair measures nothing
(measured 2026-09-27, the hard way: the target reported the drag and the wire had no
`XdndEnter` in it).

This is the "real toolkit on the other end" the C5 entry says was missing: GDK's XDND
implementation is what a forwarded application uses, so a conversation it accepts is a
conversation that is right, and one it ignores is the measurement to chase.
"""
import sys, os
import gi
gi.require_version("Gtk", "3.0")
from gi.repository import Gtk, Gdk

FILE = "/tmp/xdnd-probe-file.txt"
open(FILE, "w").write("hello from the source\n")
URI = "file://" + FILE

class Src(Gtk.Window):
    def __init__(self):
        super().__init__(title="SRC")
        self.set_default_size(200, 140)
        self.move(int(sys.argv[1]) if len(sys.argv) > 1 else 60,
                  int(sys.argv[2]) if len(sys.argv) > 2 else 60)
        self.add(Gtk.Label(label="DRAG ME"))
        self.drag_source_set(Gdk.ModifierType.BUTTON1_MASK,
                             [Gtk.TargetEntry.new("text/uri-list", 0, 0)], Gdk.DragAction.COPY)
        self.connect("drag-data-get", self.on_get)
        self.connect("drag-begin", lambda *a: print("SRC: drag-begin", flush=True))
        self.connect("drag-end", lambda w, c: print("SRC: drag-end, success=%s" % c.get_successful(), flush=True))
        self.connect("destroy", Gtk.main_quit)
    def on_get(self, w, ctx, data, info, t):
        data.set_uris([URI])
        print("SRC: data -> %s" % URI, flush=True)

s = Src(); s.show_all(); print("SRC mapped at %s" % (s.get_position(),), flush=True); Gtk.main()
