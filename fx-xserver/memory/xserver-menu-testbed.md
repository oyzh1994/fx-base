---
name: xserver-menu-testbed
description: "How to reproduce and measure an interactive GUI bug in this xserver without a visible screen, and how to get Xorg's answer for the same gesture"
metadata: 
  node_type: memory
  type: project
  originSessionId: 2d880a1b-4f17-4c22-8722-28d87eaedf57
  modified: 2026-09-30T11:16:25.798Z
---

Chasing "the context menu's submenu is drawn but cannot be clicked" (2026-09-23) needed a loop that
did not depend on a screen, a permission, or the user's hand on the mouse. What worked:

- **Run the server headless and drive it with XTEST.** `--backend headless` + `--single-window`
  means the physical pointer can never inject anything and every client shares one screen, so a
  bare-protocol XTEST client can drive the GUI exactly. `/tmp/Drive.java` sends motion/press/release
  steps (`"M x y" "W ms" "P 3" "R 3"`), and `/tmp/Shot.java` reads the root window with `GetImage`
  and writes a PNG - that is the screenshot, with no macOS screen-recording permission and no frame
  dump. `/tmp/Pixel.java` then turns a screenshot into a number (`#FFEAEEF2` on a hovered menu item
  vs `#FFFFFFFF` on a plain one), which is what makes an acceptance check automatic.
- **Use the Mac's real display only when the pointer mapping itself is under test**: the earlier
  JavaFX-backend runs were polluted by the user's physical mouse (motions over the window silently
  moved the X pointer between an XTEST motion and its press - the press lands somewhere else and the
  log's `button ... -> 0x...` line is the only hint).
- **Xvfb on the VM is the reference oracle**: `Xvfb :98 -screen 0 1200x760x24 -listen tcp -ac`
  (`-ac` matters - without it a client from the Mac gets a failed setup, and a bracketed `pkill`
  pattern matters because the start command's own path matches a plain `pkill -f`). The same gesture
  driven with XTEST against Xvfb gives the event stream a real server produces;
  `/tmp/Probe.java` and `/tmp/MenuProbe.java` (raw protocol, no Xlib) print it: which window each
  crossing is reported against, its detail and mode, and what happens across grab start/ungrab.
  `java -cp /tmp/driveclasses Probe 192.168.22.152 6098 B 0` is the "grab on the submenu, move into
  it" case that the whole fix turns on.
- **`/tmp/XProxy.java` is a byte-level X11 proxy** (listen, forward to Xvfb, log both directions) for
  reading a real client's own stream off a server that cannot log it. It works for Xlib clients
  (`xwininfo` through it is fine) but EasyShell hangs after its first `QueryExtension` through it -
  the reason was never found, so the reference measurements came from the probes instead.

**2026-09-29, driving EasyShell from the kit (checked, and the one trap that wastes a click):** the
whole loop is `tools/kit.sh Drive 127.0.0.1 6095 "M x y" "W 200" "P 1" "R 1" "W 800"`, `tools/kit.sh
Shot`, `tools/kit.sh Keys`, `tools/kit.sh WinTree`, against a `--single-window` server started exactly
as `tools/bed.sh single 95` does (the server log needs `--log-requests` and a logback config with the
package at DEBUG). **A toplevel lands at `+0+33` in root coordinates while its pixels look like they
start at y=0 in the screen capture** (`WinTree` is the truth): a click aimed at the position the
*screenshot* shows hits the root for the top 33 rows, so add 33 to every y read off a capture - the
2026-09-29 round spent its first click on exactly that, and the server log's `button 1 down at (85,13)
-> 0x2` is what says so. `tools/kit.sh Crop` magnifies a region so a text field can be read without
eyes, and `Keys` reports how many characters it resolved (a character no key carries is reported rather
than guessed).

**EasyShell's own menus are flaky in scripted runs - including on the unmodified server.** A scripted
right-click opens the menu and in roughly one run in three the app tears it down again within a
second (measured on the baseline: 1 of 3 runs, and with the fix unchanged at 1 of 3). So a single
"the menu closed" observation means nothing: measure several runs, and prefer the protocol-level
comparison against Xvfb, which is deterministic.

**2026-09-23, a tighter loop.** `/tmp/menu-cycle.sh` is the restart helper now (headless server on
:92, `--single-window --log-requests`, EasyShell from the VM); `/tmp/jfx-cycle.sh` is the same with
`--backend javafx` on :93, which is the only way to exercise the real window. `/tmp/menu-check.sh`
right-clicks the first connection row, screenshots, and reports what the **client** did - the signal
that matters is a `-> MapWindow` with no `-> UnmapWindow`/`-> DestroyWindow` after it, since the
app's own drawing makes pixel checks ambiguous. `/tmp/menu-outin.sh` adds the leave-and-re-enter
gesture and restarts the client first, because the app's state drifts between runs. Both were
deterministic (3/3) where the old notes say 1-in-3, so the flakiness was the app's state, not the
server.

`/tmp/menu-audit.sh` compares two openings of the same menu request by request - the tool for "the
first time behaves differently" questions, and it showed they do not differ at all. When a client
behaves as if it were never told something, check what it **subscribed to**: the handler now logs
`0x{peer} selected 0x{mask} on 0x{window}` on every `ChangeWindowAttributes`, which is how the
`FocusChangeMask` question was settled in one run instead of five.

Two more traps found the hard way: a manual server on :92 or :93 makes `PerClientWindowTest` /
`XServerShutdownTest` fail with `BindException: Address already in use`, which reads exactly like a
code regression - kill the manual server before running `mvn test`. And the request log is already
complete enough to see the client's decisions: `/tmp/xs92-logback.xml` sets DEBUG for the package,
so `--log-requests` gives every request, every event delivered and the `InputState` lines that say
which window a click was reported against.

**2026-09-30, driving the DEFAULT (per-client) mode from a script** - the one mode a probe client
cannot reach, because a fresh connection gets a root of its own and XTEST is scoped to its screen.
The host window is what has to be driven, with the platform's own input: `tools/host-windows.c` prints
the window's global bounds and number, `tools/host-input.c` posts a click at a global point. **Calibrate
the mapping off the server's own log, never off a capture**: the server logs
`opened a window for display :95 showing java.awt.Rectangle[x=0,y=33,width=1300,height=700]` (the
viewport), and a click at global (306,376) came back as `button 1 down at (200,300)` - so
`global = screen + (106,76)` with the window's bounds at (106,81) + a 28 point title bar. One harmless
click on an empty area is all the calibration costs, and it beats `screencapture -l`'s 56 point pad.
Then the whole gesture is scriptable in the default mode, and the signal to read is the window each
button is *reported against* in the log.

Rebuild-and-restart helper: `/tmp/cycle92.sh` (server on display :92 + EasyShell on the VM); the
client is killed with `pkill -9 -f "[E]asyShell"` in one ssh call and started with `setsid nohup` in
another, because a `pkill` pattern that also matches the start command kills the shell running it.
