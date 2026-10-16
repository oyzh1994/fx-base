#!/usr/bin/env python3
"""A real GTK3 drop target, for XDND work (backlog C5).

    python3 tools/xdnd-target.py [x] [y]      # a window that says "drop on me"

It prints every step it is told about - `drag-motion`, `drag-drop` and the URIs it finally
receives - so a drag from the host (the server's `DropBridge`) or from another XDND source
can be told apart, and a conversation that stops halfway is visible as the step that never
arrived. Run it against this server and the application is the same toolkit a forwarded
GTK client uses (see `tools/xdnd-source.py` for why the source has to be a second process).
"""
import sys
import gi
gi.require_version("Gtk", "3.0")
from gi.repository import Gtk, Gdk

class Tgt(Gtk.Window):
    def __init__(self):
        super().__init__(title="TGT")
        self.set_default_size(260, 180)
        self.move(int(sys.argv[1]) if len(sys.argv) > 1 else 700,
                  int(sys.argv[2]) if len(sys.argv) > 2 else 400)
        self.label = Gtk.Label(label="drop on me")
        self.add(self.label)
        self.drag_dest_set(Gtk.DestDefaults.ALL,
                           [Gtk.TargetEntry.new("text/uri-list", 0, 0)],
                           Gdk.DragAction.COPY)
        self.connect("drag-motion", self.on_motion)
        self.connect("drag-leave", lambda *a: print("TGT: drag-leave", flush=True))
        self.connect("drag-drop", self.on_drop)
        self.connect("drag-data-received", self.on_data)
        self.connect("destroy", Gtk.main_quit)
    def on_motion(self, w, ctx, x, y, t):
        print("TGT: drag-motion at %d,%d" % (x, y), flush=True)
        Gdk.drag_status(ctx, Gdk.DragAction.COPY, t)
        return True
    def on_drop(self, w, ctx, x, y, t):
        print("TGT: drag-drop at %d,%d targets=%r" % (x, y, [str(t_) for t_ in ctx.list_targets()]), flush=True)
        w.drag_get_data(ctx, ctx.list_targets()[0], t)
        return True
    def on_data(self, w, ctx, x, y, data, info, t):
        print("TGT: DATA %r" % (data.get_uris(),), flush=True)
        self.label.set_text("got %s" % (data.get_uris() or "nothing"))
        Gtk.drag_finish(ctx, bool(data.get_uris()), False, t)

t = Tgt(); t.show_all(); print("TGT mapped at %s" % (t.get_position(),), flush=True); Gtk.main()
