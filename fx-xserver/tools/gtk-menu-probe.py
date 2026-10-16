#!/usr/bin/env python3
"""An entry whose context menu has exactly one item, with its screen position printed.

The item is the only thing in the menu, so it is always the one under the pointer, and its position
is read out of the toolkit's own allocations rather than off a screenshot: a menu row is a few
pixels tall and a screenshot of a scaled window is not precise enough to aim at one.

A fresh item is made for every menu, because GTK does not like one that has been unparented and
parented again - which is a crash inside libgtk and has nothing to do with the server under test.

    DISPLAY=<host>:<display> python3 gtk-menu-probe.py
"""
import gi

gi.require_version("Gtk", "3.0")
from gi.repository import Gtk, Gdk, GLib

window = Gtk.Window(title="menu-probe")
window.set_default_size(400, 120)
entry = Gtk.Entry()
entry.set_text("hello")
window.add(entry)
window.connect("destroy", Gtk.main_quit)


def on_populate_popup(entry, menu):
    for child in menu.get_children():
        menu.remove(child)
    item = Gtk.MenuItem(label="PROBE-COPY")
    item.connect("activate", lambda _i: print("PROBE-COPY activated", flush=True))
    menu.append(item)
    item.show()
    GLib.timeout_add(300, lambda: report(menu, item) or False)


def report(menu, item):
    win = menu.get_window()
    if win is None:
        print("menu has no window yet", flush=True)
        return
    origin = win.get_origin()
    alloc = item.get_allocation()
    print("menu at %s %s, item %sx%s at %s,%s -> item centre %s,%s" % (
        origin.x, origin.y, alloc.width, alloc.height, alloc.x, alloc.y,
        origin.x + alloc.x + alloc.width // 2, origin.y + alloc.y + alloc.height // 2),
        flush=True)
    return False


entry.connect("populate-popup", on_populate_popup)
window.show_all()
print("ready: right-click the entry", flush=True)
Gtk.main()
