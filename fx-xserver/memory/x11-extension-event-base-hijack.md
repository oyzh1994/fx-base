---
name: x11-extension-event-base-hijack
description: "The first_event a server reports in QueryExtension is where a client library hooks its wire-to-event converter; reporting 0 puts it on the core events"
metadata:
  node_type: memory
  type: reference
  modified: 2026-10-03
---

An extension's `QueryExtension` reply carries a `first_event`, and **that number is not a
description of what the server will send - it is where the client library installs its
wire-to-event converter**: `XESetWireToEvent(dpy, first_event + i, handler)` writes straight into
Xlib's conversion table (`dpy->wire_vec[]`), one slot per event code, and that is the same table the
core events are read through. libXi installs its **device** converters for its whole range the first
time a client touches XInput 1.x (`XListInputDevices`) - measured: AWT's toolkit connection does it
at startup (minor opcode 2, its second XInput request), while the GDK connection beside it went
straight to XI2's `XIQueryDevice`.

So a server that answers **`first_event = 0` for XInput** - which is what this one did, because
our XI2 events are generic events at event 35 and nothing of ours lives in an extension range -
puts `XDeviceButtonEvent`'s layout on top of core `ButtonPress`. Xlib then unpacks every click with
the device struct, whose `deviceid` word shifts everything from `root` on by eight bytes, and
**AWT reads the click's root y as the button number**: a click at y = 18 becomes
`IllegalArgumentException: Nonexistent button 16` (AWT's extra-button mapping subtracts two) and the
click is dropped. That is backlog A18, unexplained for a month because the *wire* really was
correct and `xev` really did read it correctly - the one client that read it wrongly was the one
that had called into libXi first.

| what | base event |
| --- | --- |
| Xvfb, XInputExtension | 66 |
| Xvfb, XKEYBOARD | 85 |
| ours before 2026-10-03 | **0** for both |
| ours after | 83 / 68 (the number is the server's to choose; only "≥ 64, and reserved" matters) |

XKEYBOARD is the same trap with a different library: libX11's own XKB support hooks its range, so a
base of zero aims it at `KeyPress`. Any extension whose client library hooks its range needs a real
base whether or not the server ever sends an event there; the extensions that legitimately answer
zero (BIG-REQUESTS, XC-MISC, Composite, Present, XGE, RECORD, DOUBLE-BUFFER - all measured against a
reference) are the ones whose libraries do not hook.

**How it was caught, which is the reusable part.** `sun.awt.X11.XToolkit.addEventDispatcher(window,
disp)` is package-private, and an `XEventDispatcher` is handed the *same* `XEvent` the toolkit is
about to dispatch - so the exact ninety six bytes that produced the exception can be hex-dumped with
`MemorySegment.ofAddress(e.getPData()).reinterpret(96)` while a tap records the same click's wire
bytes. The mismatch is then arithmetic rather than guesswork: right at 8 and 32, wrong from 40 on,
the same shift on every click. See [xserver test bed](xserver-testbed.md) for the two-machine rig.

**When a client's reading of an event makes no sense, suspect what the client did to its own
connection first** - the number of extensions it asked for, the version it was told, and the event
bases it was given. Two clients of the same server can legitimately disagree about the same bytes.
