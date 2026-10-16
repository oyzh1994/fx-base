#!/usr/bin/env python3
"""A GTK3 client that owns CLIPBOARD and lets GTK answer a SelectionRequest.

The request the server's clipboard bridge sends names the bridge's own window as the *requestor*,
and GDK wraps that window before it answers (gdk_x11_window_foreign_new_for_display). This probe is
the smallest client that drives that path: it takes the selection, and everything after that is
GTK's own clipboard machinery - no signals to connect, nothing to get wrong on this side.

    DISPLAY=<host>:<display> python3 clipboard-owner.py
"""
import gi

gi.require_version("Gtk", "3.0")
from gi.repository import Gtk, Gdk

window = Gtk.Window(title="clipboard-owner")
window.set_default_size(200, 80)
window.connect("destroy", Gtk.main_quit)
window.show_all()

display = Gdk.Display.get_default()
print("screen %s root 0x%x" % (display.get_default_screen().get_number(),
                               Gdk.get_default_root_window().get_xid()), flush=True)
clipboard = Gtk.Clipboard.get_for_display(display, Gdk.SELECTION_CLIPBOARD)
clipboard.set_text("probe-text", -1)
print("took CLIPBOARD", flush=True)
Gtk.main()
