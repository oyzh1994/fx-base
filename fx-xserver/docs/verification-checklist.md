# The whole-server pass: what to test, with what, and what "pass" means

Written to be worked through a line at a time by a session that is checking *everything*, rather than
by a session that is fixing one item. `docs/backlog.md` is the history and the measurements; this is
the tour. Each line is **what to do → the tool → the judgement**, and the two sections at the end are
the ones that stop a pass from producing false alarms: [what is deliberately different](#7-what-is-deliberately-different-do-not-report-these)
and [how a run lies](#8-how-a-run-lies-measured-traps).

Tick as you go. A line that fails is worth a `docs/backlog.md` entry of its own; a line that is
*ambiguous* means the judgement column was too loose and should be tightened here.

---

## 0. The baseline, before anything else

| # | do | tool | pass when |
|---|----|------|-----------|
| 0.1 | `mvn -o test` on this machine | Maven | **456 tests, 0 failures, 0 errors, 0 skipped** (a desktop run should skip nothing) |
| 0.2 | `MVN_OPTS=-Djava.awt.headless=true ci.sh` | `ci.sh` | 456 tests, **2 skipped** (exactly `JavaFxBackendTest`'s two toolkit cases) and `BUILD SUCCESS`; it prints the surefire report path |
| 0.3 | `tools/xvfb.sh start` | the reference display | Xvfb `:98` and x11vnc up, `6098` and `5900` listening. `tools/xvfb.sh secure` additionally starts `:97` **with access control**, which §3.14 needs |
| 0.4 | start this server on `:61`, `--single-window --no-clipboard --no-tray --no-focus-on-click` (the last one for a *comparison* run: focusing on a click is on by default and a reference does not do it) | `java -cp target/classes:$(cat target/tools-cp.txt) io.github.oyzh1994.xserver.Main --display 61 --geometry 1300x800 --single-window --no-clipboard --no-tray --no-focus-on-click` | it says `display :61 ready` |
| 0.5 | `tools/sweep.sh` | the probe sweep | **19 identical of 28, and the 9 differences are exactly the documented ones** (the list in `tools/README.md`) - a *tenth* difference is a finding (run it twice before believing a single run: two probes sit on timing, and the diff's id mapping is a run's own) |
| 0.7 | **before comparing two servers, put both in the same state** | the probes, and the reason below | a run compared against a reference that still carries the *previous* run's state reports differences that are not differences - `setxkbmap`'s layout, `xset r rate`, the parked pointer. Restart both, or set both to the same thing, before believing a diff |

| 0.6 | stop everything: `pkill -f 'io.github.oyzh1994.xserver.Main'`, `tools/xvfb.sh stop`, kill the VM's clients | - | nothing left listening on 6010 or 6062-6099, no `Xvfb`/`x11vnc`/AppImage on the VM (a leftover makes the *next* run fail as if the code were broken) |

`tools/kit.sh <Tool> <args>` is the launcher with the classpath filled in; run `mvn -o test-compile`
first if `tools/` has changed. Probes take `host port` - `127.0.0.1 6061` for the server above,
`192.168.22.152 6098` for the reference.

## 1. The protocol's core, and drawing

| # | do | tool | pass when |
|---|----|------|-----------|
| 1.1 | window tree, attributes, geometry, events | `WinTree`, `Xwd`, `RawXClient`-based tests | `WinTree` prints the same tree against both servers for the same client |
| 1.2 | the rasteriser: lines, arcs, polygons, dashes, fills, clipping | `PolyProbe`, `RasterizerTest` | `PolyProbe` identical to the reference; the test suite's pixel comparisons pass |
| 1.3 | `PutImage`/`GetImage` at every depth, including bit and byte orders | `ImageCodecTest`, `Blocks`/`Colors` on a capture | identical pixels; a depth-8 mask that arrives quantised to `0x00/0x55/0xaa/0xff` is coverage read as colour - a bug, not a client |
| 1.4 | RENDER: formats, pictures, composites, glyphs, masks | `DamageProbe`, `AlphaMaskTest`, `ArgbAlphaTest` | the suite's RENDER cases pass; a masked composite writes pixels (the debug line `-> wrote N pixels` is the instrument) |
| 1.5 | **every window keeps its pixels** (B5's store) and is composited from them | `CompositeProbe` | identical to the reference; a window revealed by a move keeps its contents with no client repaint |
| 1.6 | DAMAGE: report levels, `Subtract`'s repair region | `DamageProbe` | identical to the reference |
| 1.7 | Composite: redirect, `NameWindowPixmap`, overlay | `CompositeProbe` | identical; a redirected window's pixmap holds the new pixels while the screen holds the old |
| 1.8 | Present: a client's finished frame, completion event | `PresentProbe` | identical; the completion is **40 bytes**, not 32 |
| 1.9 | SYNC: counters, fences, `Await` | `SyncProbe` | the documented difference is the system counters (none here, eight there); everything else agrees line for line |
| 1.10 | SHAPE: bounding shape clips drawing, obscuring and hit testing | `PolyProbe`, the suite's SHAPE cases | identical |
| 1.11 | fonts: the standard six, a font file on a font path, font sets | `FontProbe` | identical to the reference for `fixed`,`6x13`,`7x13`,`9x15`,`10x20`,`12x24`, `8x13` (from a file) - metrics, properties, extents and pixels |
| 1.12 | cursors: shapes, glyph cursors, hiding | `CursorProbe`, `GlyphCursorProbe`, `ChangeCursorProbe` | glyph cursors identical for fourteen characters; the cursor **sprite is drawn over the frame, never in the framebuffer** |
| 1.13 | a full render comparison of a real application | `tools/compare.sh` | only the documented differences: root colour (`#E8E8E8` against black), the border this server draws, and blend rounding |

## 2. Input

| # | do | tool | pass when |
|---|----|------|-----------|
| 2.1 | pointer motion, buttons, keys through XTEST | `Drive` (`M x y`, `P n`, `R n`, `K kc`, `W ms`), `Keys` | the client reacts identically to the reference |
| 2.2 | focus: `SetInputFocus`, reverts, a focused window going away, `PointerRoot` | `Focus`, `FocusProbe` | `FocusProbe` identical to the reference, including the revert cases (`kill A`, `unmap A`) |
| 2.2b | **where a key lands** in each focus state - the window the pointer is in, or nowhere | `KeyTargetProbe` | identical to the reference in all six modes: `pointer`, `root` and the `unmap`/`kill` reverts report the `KeyPress` against the window the pointer is in, `parent` against the focused window, and `none` reports nothing. A server that answers the focus events correctly and sends the key to the root window still fails here |
| 2.3 | crossings: enter/leave, details, modes, the flags byte | `MenuProbe`, `FocusProbe` | identical to the reference - **and the menu gesture must be run fresh** (§8) |
| 2.4 | grabs: pointer, keyboard, passive grabs, owner-events | `GrabProbe`, `GrabCrossingTest` | identical; passive grabs are matched, and a disconnecting client's arms are dropped with it |
| 2.5 | `AllowEvents`: freeze/thaw, the held queue, replay | `AllowEventsProbe` | all five arrangements identical to the reference |
| 2.6 | gravity: `winGravity` and `bitGravity` | `GravityProbe`, `BitGravityProbe` | all eleven values identical |
| 2.7 | auto-repeat: the delay, the rate, off/on, per-key | `XkbProbe setrate/setrepeat`, `xset r rate 200 50` on the VM | the delay and rate read back; repeat off gives **one** press and no run |
| 2.8 | XInput2: version, device list, client pointer, selection, query/warp, device grabs | `XiProbe`, `xinput list` | identical except the documented device-list difference (two masters here, six devices there) |
| 2.9 | XInput2 events: key/button/motion, the 80-byte fixed part, the masks | `XiProbe select`, `XInputTest` | a device event is **120 bytes for a key, 136 for a two-axis motion** - a 68-byte event is not rejected by GDK, it corrupts the application |
| 2.9b | **the other two XI2 shapes**: a crossing and a focus event are not device events | `XInputTest`, a `tap.py` capture of the same gesture on both servers | the three sizes are **120 / 76 / 104**, `same_screen` is at 48 and `focus` at 49 in the two short ones, `buttons_len` at 50, the mask at 72 - send them in the device shape and a toolkit reads the server's lengths as its flags, which is a window that never looks focused while typing still works |
| 2.10 | the cursor a client sets, and `HideCursor`/`ShowCursor` | `ChangeCursorProbe`, `CursorProbe` | identical; `ChangeCursorByName` stays refused (measured: a reference does nothing for it either) |
| 2.11 | typing into a real application, in the host's input method | the Mac's own input source (A53: `imeswitch` + `hostkey` with real keycodes), `tools/host-type.c`, the VM's `ibus` | **the character arrives in the application** - not merely in the mapping. **The pre-edit is in the field while the person types** (`ni hao` for 你好, `a a a a a` for 啊啊啊啊啊), space commits the candidate, **Return commits the raw text**; each composed character has a keycode of its own, with a `MappingNotify` for each. A probe that only reads `GetKeyboardMapping` passes even when a real toolkit shows nothing, which is how a broken claim on keycode 255 read as verified for a month (A53). `tools/host-input type` posts every character with the `a` key's keycode, so ASCII letters arrive as `a` - use `hostkey` or `xdotool` for those |
| 2.12 | the two apps' own gestures | EasyShell, termora by hand | EasyShell's context menu survives the out-and-back; typing lands; termora the same. **EasyShell is scriptable and was driven that way on 2026-09-28** (right-click a host row, leave and re-enter, capture: the menu is still there). **termora's is not, from a script**: its host tree had no rows to right-click on the run that day, so a scripted pass can only confirm it *renders* - a hand is what settles its menus, and the entry for the gesture says so everywhere it has been measured |

## 3. The extensions

| # | do | tool | pass when |
|---|----|------|-----------|
| 3.1 | RandR: screens, CRTC, outputs, modes, `--prop`, `--listmonitors` | `RandrProbe`, `xrandr` on the VM | the answers a client needs are identical; a *change* request is refused with `BadImplementation` |
| 3.2 | Xinerama: `QueryVersion`, `QueryScreens`, `IsActive` | `XineramaProbe` | the three a client asks agree; the 1.1 extras are deliberately left |
| 3.3 | XKEYBOARD: handshake, map, state, controls, `SetMap`, by-name layout | `XkbProbe`, `setxkbmap`, `xkbcomp`, `xmodmap -pke`, `xev` | `setxkbmap -layout <name>` exits 0 and `xmodmap -pke` is byte-identical to the reference for all seven layouts; `xkbcomp <file> <display>` still loads a map |
| 3.4 | MIT-SCREEN-SAVER and DPMS | `ScreenSaverProbe`, `xset q` | identical except the documented DPMS section (this server implements it, the reference build does not) |
| 3.5 | GLX: handshake, configs, contexts, strings | `GlxProbe`, `glxinfo`, `glxgears` on the VM | `glxinfo` says `direct rendering: Yes`; the extension list is deliberately **empty**; an indirect context is refused |
| 3.6 | XFixes: selections, cursor notifications, regions | `SelectionProbe`, `CursorProbe`, `XFixesNotifyTest` | identical |
| 3.7 | the shape of a refusal | any probe | a request this server does not implement is refused with a *named* error, never accepted-and-dropped and never a decode crash |
| 3.8 | SYNC alarms: a trigger on a counter, its four arrangements, its delta and its two refusals | `SyncProbe` | identical to the reference line for line (the alarm section is the part that changed in F1) |
| 3.9 | Present's `NotifyMSC`, alongside its completion notifications | `PresentProbe` | identical: the completion carries kind 1 and the client's serial |
| 3.10 | the Generic Event Extension: advertised, version handshake | `XgeProbe`, `xdpyinfo` | listed as `Generic Event Extension`, answers 1.0 to a client asking 1.0 or 0.0, twin probe identical |
| 3.11 | X-Resource: the client list, counts by type, pixmap bytes, a named resource's size | `ResProbe`, `ResTest` | the four deliberate differences below, and everything else identical |
| 3.12 | RECORD: a context registered on this server and on Xvfb produces the same stream for the same gesture | `RecordProbe`, `RecordTest` | identical element for element - same categories in the same order, same framing, same stamps and the same attribution - with one deliberate difference: there is no device-event stage here, so a reference's device elements are not reproduced |
| 3.13 | DOUBLE-BUFFER: a client draws into a back buffer and swaps it in front of its window | `DbeProbe`, `DbeTest`, an Xlib `Xdbe` client on the VM | identical to the reference - the four swap actions, every refusal, the resize and the cross-client cases - except the visual count in `GetVisualInfo`, which is the screen's own (7 here, 144 there). The Xlib client is the check the probe cannot be: Xlib's **own** readers parse this server's replies (`XdbeGetVisualInfo` especially) and its own request builders drive the swap |
| 3.14 | SECURITY: an untrusted connection is refused what it must be and can still draw its own window | `SecurityProbe`, `SecurityTest`, an Xlib `XSecurity` client on the VM | identical to the reference **twice over**: against the ordinary `-ac` pair, and against a reference with access control on (`tools/xvfb.sh secure`) with this server under `--auth`. What is compared is a battery sent from an untrusted connection *and* from a trusted one - only a difference between the two is the trust level - plus the hidden-extension list an untrusted client is given, and the way a revoke closes the connection using it |
| 3.15 | XVideo: a player asks for adaptors, ports and encodings, and is told there are none | `XvProbe`, `XvTest` | identical to the reference line for line - zero adaptors, the extension's own version 2.2, its two events and three errors, `BadPort` for every request that names a port **in the reference's order of checks** (drawable and GC first), and `SelectVideoNotify` accepted for a drawable |

## 4. The host window - where this server is not a plain server

| # | do | tool | pass when |
|---|----|------|-----------|
| 4.1 | the window carries the client's title and icon | `host-windows.c`, a real client | the title follows `_NET_WM_NAME`/`WM_NAME`; the icon arrives from `_NET_WM_ICON` |
| 4.2 | resize: the client's window follows the host's drag | `host-drag.c` on an edge | the client's window is resized, and the screen grows first if needed |
| 4.3 | the screen grows, up to `--max-screen`, and never shrinks | `host-drag.c`, `xrandr` | `ScreenChangeNotify` reaches a client that asked; past the limit the drag stops |
| 4.4 | the window's position is remembered per client | `host-windows.c`, restart the server | it reopens where it was left (the *first* position of a session is adopted, not recorded) |
| 4.5 | fullscreen and urgency from the client | `WindowStateProbe` | fullscreen gives the display and gives it back; urgency raises the frame exactly once |
| 4.6 | the clipboard, both ways | `ClipboardProbe` | text pasted on the host pastes in the client and the reverse |
| 4.7 | tray icons | a client that docks one (C9), the menu bar | the icon appears in the macOS menu bar and goes away with its client |
| 4.8 | a file dragged from the Finder into a forwarded application | `tools/HostDragSource.java` or the hand, plus a shared folder and `--drop-mount` | the application receives a path **it can open** and uses the file: the drag must start *inside* the mapped directory (§7) |
| 4.9 | close, detach, re-attach | `host-drag.c` (a click on the close button), `nc` to the control port | a client that closes takes the session; `--detach-on-close` keeps it and `attach` brings it back |
| 4.10 | the fallback chain when no window can be opened | `--backend` combinations, `C11`'s cases | it says what it is doing instead of hanging, and never claims a window it did not open |

## 5. Transports

| # | do | tool | pass when |
|---|----|------|-----------|
| 5.1 | TCP | a client on the VM at `192.168.22.1:61` | it connects and works |
| 5.2 | a unix socket and `--no-tcp`/`--no-unix`/`--local` | a client on this machine | the transports restrict exactly as asked |
| 5.3 | `--auth` and the no-credentials case | `AuthProbe` | a client with no credentials is refused; with the cookie it connects |
| 5.4 | VNC: raw, ZRLE and (A32) Tight's JPEG method for photographic rectangles, the cursor pseudo-encoding, a password, the clipboard | `--backend vnc`, a VNC viewer, `ZrleEncoderTest`, `TightEncoderTest` | the screen appears, the pointer is drawn, the clipboard crosses - the recipe is below |
| 5.5 | VNC over WebSocket, in a browser | `--vnc-ws-port`, noVNC | the screen appears with no bridge process |

**The VNC recipe, as one command.** This is the mode where the server opens no window at all and the
whole screen is served over RFB - so it runs on a machine with no display, and what a VNC client's
mouse and keyboard do is drive the X pointer and keyboard (`RfbSession` -> `input.pointerMoved` /
`keyChanged` / `buttonChanged`).

```sh
# A password file is one line; macOS's Screen Sharing insists on a password.
echo 'my-password' > ~/.xserver-vnc-pass
mvn exec:java -Dexec.args="--backend vnc --vnc-port 0 --geometry 1300x800 \
    --vnc-password-file ~/.xserver-vnc-pass --vnc-ws-port 6080"
# (or --vnc-password VALUE - the same password as a value, for an embedded launch)
# the log then says:  VNC on port <N>: connect with vnc://<this machine>:<N>
#                     VNC over WebSocket on port 6080: point noVNC at ws://<this machine>:6080/
```

Then: **macOS** Finder -> Cmd-K -> `vnc://<host>:<N>` (the built-in Screen Sharing); **a phone** any VNC
app; **a browser** noVNC's `vnc.html` at `ws://<host>:6080` - nothing to install, no bridge process.
`--vnc-port 0` takes any free port and prints it, which is the honest way to run it beside anything
else. Before bothering with a viewer, the objective check is one line - a real VNC server answers with
its protocol version:

```sh
python3 -c "import socket; print(socket.create_connection(('127.0.0.1', <N>)).recv(12))"   # RFB 003.008
```

**Either/or with the host window**: `--backend vnc` opens no window (that is what lets it run without a
display), so this and the JavaFX window are two modes rather than two features at once - **and it
implies `--single-window`**, every client sharing the one screen, which is what a VNC-served X display
has always looked like. X clients connect exactly as they otherwise do (`DISPLAY=<host>:<N>`); VNC is
only how a person watches and drives it. **Encodings**: raw, and ZRLE when the client lists it, and
**Tight's JPEG method** for a rectangle whose content is photographic when the client has offered both
Tight and a JPEG quality (A32) - never Tight's zlib path, and no `CoRRE`. Raw is mandatory for every
client, so any viewer works - it is bandwidth on a slow link that suffers, not compatibility.
| 5.6 | session persistence | `--detach-on-close --control-port N` | clients survive the window, `attach`/`detach`/`status` answer |

## 6. Engineering

| # | do | tool | pass when |
|---|----|------|-----------|
| 6.1 | framebuffer memory is allocated lazily | `IdleScreenMemoryTest` | a connection that only asks questions allocates nothing |
| 6.2 | a client that stops reading is not waited on | `E5`'s cases | the server stays responsive to everyone else |
| 6.3 | the performance paths hold | `E3`'s cases | the opaque-source and A8-mask paths and the damage region are still there (the numbers are in the entry) |
| 6.4 | the suite runs where there is no display | `ci.sh` with `-Djava.awt.headless=true` | 407 tests, 2 skips, success |
| 6.5 | the documents agree with the code | §7 and §8 below | nothing here contradicts the three documents - which is what E6 is |

---

## 7. What is deliberately different (do not report these)

(**Capabilities this server simply does not have** - extensions a client could ask for and the holes
inside the ones it does have - are a separate, measured list: `docs/backlog.md` §9. A pass should not
report those as bugs either, but for the other reason: nobody has built them yet.)

Each of these was measured against a reference and decided; `docs/design-notes.md`'s "Deliberately
not done" and `docs/backlog.md`'s §7 have the measurement.

- **No window-manager semantics**: no decorations, no `_NET_*` beyond C3, no activation protocol -
  **except that a click moves the input focus** (`--focus-on-click`, on by default), which is what a
  forwarded application whose page leaves the focus on the root window needs to get its menus back.
  It moves it to the toplevel the click landed in **unless that toplevel is override-redirect** - a
  window manager never focuses a menu, and focusing one takes the focus off the window the menu
  belongs to, which is what makes the application unmap its own popup (A23, §6). **`--no-focus-on-click`
  is the comparison switch**: a reference server does not focus a window on a click, so a sweep or an
  A/B whose gesture presses a button wants it off.
- **The crossing pair a grab implies** (`NotifyGrab`/`NotifyUngrab`) is not delivered - measured 3/3:
  EasyShell tears its menus down within a millisecond of receiving it.
- **A window appearing under a stationary pointer** is reported as entered at the map (the *pair*
  above is still withheld).
- **The blend rounds; Xorg's fixed-point composition truncates** - a one-unit channel difference.
- **Every window keeps its pixels**, so `backing_store` decides nothing (it is still advertised as
  `WhenMapped`); `save_under` is unadvertised and unimplemented, as the reference is.
- **The pointer cannot leave the host window's viewport**; a warp beyond it is clamped.
- **GLX is advertised by default and is a subset**: no GLX extensions, four visual configs; an
  indirect context is refused. `--no-glx` turns it off.
- **XKB**: a variant is served as its base layout; the compatibility map, indicator names, a
  `SetNames` round-trip, every control other than repeat, `LatchLockState` and XKB events are absent;
  `GetDeviceInfo` is refused (no client in this bed sends it). Two consequences a real client shows,
  measured on 2026-09-28: `xset q` prints **`XKB indicators: None`** where a reference lists thirteen
  (the names are what it would print), and **`xkbcomp -xkb <display>` cannot export the map** - it
  stops at `XkbGetCompatMap` with `BadImplementation` and then at `XkbGetGeometry` - while the
  direction that matters for a client that wants a layout (`xkbcomp <file> <display>`, `setxkbmap
  -layout <name>`) works. `setxkbmap -print` and `_XKB_RULES_NAMES`, which follow from the rules
  property, are **identical** to a reference's for the layouts in the kit.
- **The per-key repeat bitmap differs on four keycodes**: 204-207, the extra evdev Alt/Meta/Super/Hyper
  keys, repeat on a reference and not here. Its repeat map comes from the XKB map's key *behaviours*,
  which this server does not carry, so the rule it uses is the simpler "every key but the modifiers of
  the keymap in force" - and those four are modifiers here. Nothing types them (their unshifted level
  is `NoSymbol`), which is why the difference is recorded rather than chased.
- **XI2**: touch, tablets, barriers, the raw event family and the property *writes* are refused; both
  property *reads* are answered "nothing".
- **MIT-SHM is not done** (deliberate, with reasons).
- **`ChangeCursorByName` is refused** - a reference does nothing at all for a name.
- **VNC carries raw, ZRLE, and Tight only as its JPEG method** (A32): a client that lists Tight but no
  JPEG quality is served ZRLE, and `CoRRE` is not implemented.
- **A server restart is out** (D1 detaches the *window*, not the process).
- **The `eventcopy` rewrite in `ClientOutput` is the design**, not a workaround to remove (E1, closed).
- **A drag out of an application is not finished** (C5: the trigger was reverted; the owner has
  parked it).
- **The depth in a `-screen 0 WxHxD` / `--geometry WxHxD` invocation is accepted and ignored** (A52):
  the root is always 24 bit TrueColor and only the *window* visuals carry 16/8/1 bit, so a server
  asked for `x16` answers `depth of root window: 24 planes` and says so once at startup. It is
  accepted because it is part of the usual X-server invocation (the bed's own Xvfb runs `x16`), and
  honouring it would mean the whole framebuffer path at another root depth.
- **A headless server in per-client mode shows nothing anywhere** (A53). The client connects, draws
  and runs; there is no window on the host and no way to see the client's own screen - every
  connection gets a screen of its own, which is the mode's design, so this is not a defect to report.
- **The applications' own quirks, not this server's** (A53, each reproduced identically on Xvfb): a
  synthetic click does not activate termora's context-menu *items* (the menu opens and stays open),
  a click on EasyShell's own title-bar items (片段 消息 工具 关于 设置 退出) does nothing, and
  termora's Quick Connect with an empty form answers with its own `getScheme(...) must not be null`.

## 8. How a run lies (measured traps)

1. **A menu or crossing measurement needs a fresh server, a fresh client, and the pointer parked
   first.** Anything less measures the previous run's state - it produced a wrong "this change broke
   the menus" for an afternoon, and the same build is 3/3 with the three controlled.
2. **The pointer and the button state outlive every client.** A probe that ends with a button down
   makes the next run's press a duplicate the server drops.
3. **A leftover server, bed or ssh tunnel on a display the suite binds** (`6010`, `6062-6099`) fails a
   test as `BindException`, which reads exactly like a code regression. `ci.sh` refuses to start when
   one is there.
4. **The reference must be clean**: a client killed with windows mapped leaves them on Xvfb, and the
   next comparison differs in its first crossing for that reason alone. `tools/xvfb.sh stop`/`start`.
5. **The two `bad window 0x0` refusals** in every log are the *client's* (GTK asks at startup, a
   reference refuses them too). Not a finding.
6. **`--glx` changes what a toolkit does with its own drawing** (it takes a GL path and abandons it).
   Comparisons of two builds should say which side had it; it is on unless `--no-glx`.
7. **A client-side A/B needs the same driver and marked logs** - `xdotool` on the VM rather than XTEST
   from here, a line count taken before the interaction, and request profiles compared per
   (extension, minor). `XProxy` does the framing; `tools/tap.py` is the byte-transparent fallback
   (and logs chunks a reader must frame themselves).
8. **EasyShell refuses to start twice** (`/root/.easyshell/db.mv.db` is locked) - kill it first.
9. **The five event type bytes are corrected in `ClientOutput`** (E1): with the rewrite removed *and*
   the published artifact, a `KeyRelease` arrives as a press. If an event type looks wrong, that is
   the pair to check, not the client.
10. **`mvn test`'s incremental compile does not recompile a test when only a main class's signature
    changed** - it shows up as `NoSuchMethodError` in a test that looks untouched, so a signature
    change wants `mvn clean test` once.

11. **`x11vnc` disables the reference's screen saver**, and `tools/xvfb.sh start` runs it beside Xvfb: the
    reference then answers `GetScreenSaver` with `timeout 0 / prefer blanking no` where a bare Xvfb -
    and this server - answer the X defaults (`600 / yes`). Measured 2026-09-28 by starting a second
    Xvfb with no VNC on it: its `xset q` matches this server's exactly. Not a difference to chase.
12. **A modifier key's own event carries the state *before* it** - a Control press arrives with `state
    0x00` and its release with `0x04` - while a plain key pressed under it carries the modifier. This
    server had that the other way round until 2026-09-28 (found with `xev` on both servers), and
    `Ctrl`/`Shift` reaching the *following* key was always right, so a client that reads only the
    state field was the one that saw it.
13. **Every client's log has a few `0x0` refusals of its own** - `GetProperty`, `GetWindowAttributes`
    and `GetGeometry` on window/drawable `0x0` - as a toolkit starts and asks about things it has not
    made yet. A reference refuses them too; they are not findings.
14. **A capture is in pixels of *something*, and the two kinds differ by a factor of two** (A53):
    `xwd -root` on the server's own root is 1:1, so a point in that image *is* a screen coordinate -
    but `screencapture -l <window id>` of a host window is **2x the frame's points**, so a click
    computed from it must be halved. Mixing the two puts the pointer at twice the intended place,
    which reads as "the app ignores clicks there".
15. **A synthetic click and a synthetic keystroke are not the same instrument on every toolkit**
    (A53): JavaFX (EasyShell) activates a context-menu item from `xdotool mousemove x y click 1`,
    while Swing (termora) does not activate one from that, a separate move-then-click, or a
    press-hold-release - **on this server and on Xvfb alike**. Before recording "the menu does not
    work", run the same gesture on the reference; what is left is the application's.

## What the 2026-09-28 verification round found

The checklist was walked end to end - the suites, the sweep twice, the real clients on the VM
(`xdpyinfo`, `xrandr`, `xset`, `xmodmap`, `setxkbmap`, `xinput`, `glxinfo`, `xterm`, `xev`, `xdotool`),
the host window with a real JavaFX frame, and both applications - and it turned up six defects, every
one of them invisible to the suite and to the probes as they were. They are listed here because each
one is the kind of thing a *client* trips over, and because the tests that now pin them are what makes
the next round cheaper:

1. **Windows of the server's own were in the tree but not in the resource table.** The clipboard, tray
   and host-drag bridges each made a window in their constructor and registered it in `start()`, so a
   bridge that never started - a headless server, `--no-clipboard`, `--no-tray` - left children of the
   root that the server could not resolve: `xwininfo -root -tree` printed `X Error: 3: Bad Window` for
   each and could not finish its walk. `WindowTreeTest` makes both bridges without starting them and
   walks the tree over the wire.
2. **The modifier map said two keycodes per modifier where X says four**, and its rows were a
   hand-written American arrangement whose Mod3 was empty and whose Mod5 held Scroll_Lock - so
   **AltGr was in no row at all**, and a client binding Mod5 could never match it. A captured layout
   now brings a reference's own rows (an evdev arrangement: Mod3 the level-5 shift, Mod5 AltGr, and
   the four extra keys above 200 as modifiers), and `KeymapTest` pins both tables.
3. **The per-key repeat bitmap was a copy taken when the server started**, so a client that loaded a
   layout by name left it describing the keyboard of whichever layout ran first - `xset q`'s block then
   differed from a reference's by exactly the keys the layouts disagree about. It is derived from the
   keymap in force now, a client's own bitmap still wins, and `AutoRepeatTest` covers both.
4. **`GetOutputInfo`'s generated reply writer puts `numPreferred` before `numClones`**, where the
   protocol has it after, so every count from `numClones` on was shifted two bytes: `xrandr` warned
   *"Output screen is not disconnected but has no modes"* and printed the CRTC's mode without its
   `*current` marker. The reply is written out by hand, and `RandrProbe`'s own field labels - shifted
   by one, and the source of a wrong comment in the extension - are fixed with it.
5. **`GetPanning` answered the whole screen as the panning area**, which every `xrandr --verbose`
   printed as `Panning:`/`Tracking:`/`Border:` lines a reference's output does not have. Zeros, which
   is what a reference answers.
6. **`ImageText8` and `ImageText16` were refused for a string of 128 characters or more**, because the
   library reads their CARD8 length as a *signed* byte. **xterm died on it**: redrawing after a host
   window resize it sent a 130 character run, got `X Error: BadRequest ... X_ImageText8`, and quit -
   taking its host window with it. Both requests are hand-decoded now, and `DrawTextTest` pins the
   boundary.

The round also closed a difference that was *not* invisible: `_XKB_RULES_NAMES` was never published, so
`setxkbmap` described the keyboard in its own fallback terms ("base", a keycodes component of
`xfree86`) where a reference says `evdev` - in the property and in `setxkbmap -print` both. It is
published on every screen's root now, and `setxkbmap -print` is byte-identical for `us`, `de` and `cn`.

Two differences were *not* defects, and are recorded as traps above so the next round does not chase
them: the reference's screen saver settings belong to `x11vnc`, and `Ctrl`/`Shift` do reach the
following key on this server (measured with `xev` on both) whatever a first look at an application
suggests.

## What the 2026-09-29 bug round found

The bug branch works through what a *user* of a real application reports, one item per branch. The
first of them:

1. **A keystroke went to the root window instead of the window the pointer is in, once the focus was
   left on the root window - which is where a `RevertToParent` out of a child of the root lands.**
   Reported as "click 片段, close the page, then click the home page's search box: no character
   arrives; open and close 设置 and it types again", and it is not a client bug and not a rendering
   one: everything pointer-driven kept working, and the search box even took the focus the click gave
   it. Traced in the server log with EasyShell on the VM against a `--single-window` server:

   - the 片段 page is a toplevel of its own (`0x40001e`), the client calls
     `SetInputFocus(page, revert_to = Parent)` when it opens it, and closing it sends `UnmapWindow`
     and nothing else - the client is relying on the server's revert;
   - the revert runs, and lands correctly on the root window: `focus reverted from 0x40001e to 0x2
     (the root window)`, which is what Xvfb does with the same gesture too (`xdpyinfo` on the
     reference after closing the page: `focus: window 0x1ff, revert to None`);
   - **but this server then delivered the keys to the root window** (`InputState.focusWindow`
     returned the focus window itself, and only `PointerRoot` followed the pointer), so every
     keystroke went to a window no client is listening for and was dropped;
   - the 设置/关于 dialogs "fix" it because they are *transient* windows: the client puts the focus
     back itself (`SetInputFocus(main)` one millisecond before the `UnmapWindow`, in the log), which
     is why the same code path behaved differently for the two gestures.

   **Measured, not reasoned.** `tools/KeyTargetProbe` was written for the question - it parks the
   pointer in a window of its own, leaves the focus on the root (`SetInputFocus(root)`, or the revert
   from unmapping a focused child), presses a key through XTEST, and prints the window the
   `KeyPress` is reported against. Against Xvfb and here, all six modes: `pointer`, `root`, `unmap`
   and `kill` report the key against **the window the pointer is in**, `parent` against the focused
   window, `none` reports nothing. Before the fix this server reported nothing at all for `root`,
   `unmap` and `kill` - the key had gone to the root. The probe run is identical line for line to the
   reference's (`tools/probe-diff.py`) with no other client's window under the pointer it parks.

   **The fix** is one rule in `InputState.focusWindow`: the root window and `PointerRoot` are two
   states a client can still tell apart - `GetInputFocus` answers the id for one and `1` for the
   other, and a change between them reports different event chains - but the keys follow the pointer
   from either, and only `None` leaves them nowhere. `InputFocusTest` pins the three cases (both
   states, and the revert that reaches the root window), and both of the new tests were seen to fail
   without the fix.

   **Verified end to end**: EasyShell on the VM against this server, the same gesture, typed `abc`
   into the home page's search box with no dialog opened in between - and the app receives it as
   `Xi2Event[evtype=2 device=3]` (XI2 `KeyPress`), which is how the toolkit reads the keyboard.

**termora, the second application in the bed, was checked the same way** - it is Swing/AWT and reads
the keyboard through core events rather than XI2, so it is the control for "the fix is not a
JavaFX-shaped accident". Started against the same server, with the focus left on the root window by a
fourth client's probe (`tools/KeyTargetProbe root`) and its pointer parked over termora's window, the
three typed keys arrive as `KeyPressEvent -> client[1]` - core `KeyPress`/`KeyRelease`, which is what
AWT dispatches to a focused component. Before the fix this is the state in which nothing arrives.

**And one finding that is *not* the reported bug, left open as `backlog.md` A18**: clicking termora's
密钥管理器 toolbar button on this server makes its toolkit thread throw `Nonexistent button 16` and the
dialog never opens, where the same click on Xvfb opens it. It is **not** from this fix - the pre-fix
build throws the same exception with the same event log - and AWT's own logger shows its *reading* of
the event to be the strange part: the serial and the type byte are ours, and every other field holds
the previous event's values. The wire, tapped through `tools/tap.py` during the same click, is three
well-framed messages with `detail = 1` on the press. A18 has the detail, the harness and the next step.

2. **An XI2 enter, leave or focus event was sent in the device event's shape, so a toolkit read the
   server's own length fields as its flags** (found 2026-09-29, from the report that EasyShell's
   search box "sometimes does not get the focus although typing works"). A reference sends 120 bytes
   for a key, **76 for a crossing and 104 for a focus event**, with `same_screen`/`focus` at 48/49,
   `buttons_len` at 50 and the button mask at 72 - this server sent all three at 120 with a device
   event's offsets, so GDK read `buttons_len` as `same_screen` and its modifier state eight bytes
   late, and the window never looked focused (no ring, no caret) while the keys kept arriving. The
   A/B is the report's own "not always": **1 of 3 starts showed the focus ring before the fix, 4 of 4
   after**. `Xi2Event.Shape` holds the three layouts and `XInputTest` pins them field by field; one
   difference is left and written down rather than guessed - a focus event's `detail` comes from the
   core event (0, `Ancestor`) where the reference's XI2 event said 3 (`Nonlinear`), measured in one
   case only. The capture recipe is the one above: `tools/tap.py` in front of *both* servers with the
   same client gesture, and the sizes read off the two logs.

3. **The XI2 button mask was always empty, so a window could not be dragged by its title bar** (found
   2026-09-29, reported as "the settings page, any other window, even the main page - pressing the
   left button and dragging no longer moves it; `main` was fine"). The two builds differ in one way
   that decides it: `main` has no XInput2, so the application reads **core** motion events there and
   its window follows the drag; this branch advertises XInput2, so GDK reads the **XI2** motion, and
   takes the drag state from that event's *button mask* - which this server left zero for every device
   event. A motion with nothing held is a move, not a drag, so the window never followed: the same
   scripted drag that moves it 60 pixels on `main` moved it 20 (the first step only) here, and on the
   fixed build moves it 60 again. Measured with `tools/tap.py` on both servers, the reference's mask
   for a drag is `02 00 00 00` in the first word - **bit n is button n**, Xorg's own numbering, not
   `1 << (n-1)` - and it is the state *before* the event: a press arrives with the mask clear and its
   release with the button still set, the same convention the core `state` field follows.
   `Xi2Event.Builder.buttons` writes it (from the core event's `state`, so the two rails cannot
   disagree) and `XInputTest.aCrossingAndAFocusEventHaveTheShapesAReferenceSends` pins the bytes.

   **What is *not* fixed, and is the next thing here**: a reference's **motion is 136 bytes** - the
   same fixed part as a key, and then the two axes as `FP3232` values behind the valuator mask - where
   this server sends 120 with an empty valuator mask. That shorter form is valid XI2 (the mask says no
   axis is present) and works, but it is a difference from a reference. Sending the reference's shape
   was written, measured byte for byte against Xvfb's drag, **and crashes GDK** the moment the first
   motion arrives (`SIGSEGV` at a nought pointer plus `0x90` inside `gdk_display_get_event`, three
   times, with the axes at the right offset - `Xi2Event.Shape.MOTION` is the layout, pinned by the
   test, and no caller uses it yet). The two bugs found on the way *were* ours and are fixed: the
   values must follow the whole mask array, not one word, and a motion has to be in the same branch as
   a key when the two length fields at 48/50 are written. What crashes a client that is handed the
   reference's own shape is not known.

4. **"The context menu vanishes after the 片段 page has been opened"** - reported 2026-09-29, **not
   reproduced**, and the measurements say it is not this branch's: with a fresh client, a right-click on
   a host **row** opens the menu on this server *and* on Xvfb, with the client's own pixels identical
   (77999 non-background pixels and 6003 ink with no page; 77999/2334 here against 77998/2332 there with
   the page open), the menu stays for as long as it is watched, `MenuProbe` is identical to the
   reference, and the same gesture on `main` - where there is no XInput2 at all - behaves the same. The
   state the report turns on is real and worth knowing: **the 片段 page holds the X input focus** (the
   client asks for it; `xdpyinfo` on Xvfb agrees), so the main window is unfocused, no server focuses a
   window on a click (neither does Xvfb), and the client never asks for it either - a tap of its stream
   against the reference has one `SetInputFocus` in it, the page's. `backlog.md` A20 has the table, the
   candidate mechanism (JavaFX hides a popup whose owner window is not focused) and what a capture from
   the machine where it happens would add. **Run the menu gesture on a row, not on the panel's empty
   background**: the background is where the client's own behaviour is flaky under automation, on the
   reference as much as here.

5. **A window on its way out was handed a `FocusIn`** (found 2026-09-29, from the same report as the
   menu above: chasing it produced a corner no probe had covered - the pointer being *in* the window
   that holds the focus when it goes away). Two probes now pin it: `tools/FocusRevertProbe` (new: two
   overlapping root children, the focus on the front one with `revert_to = Parent`, then unmapped or
   destroyed) and `FocusProbe`'s `"warp 50 50" "focus B" "unmap B"`. A reference reports the ordinary
   pair and the crossings and **nothing else**; this server added `FocusIn <the departing window>
   detail=Pointer`, a window no client can act on - and GDK took it for where the focus lives, which is
   what leaves the window underneath looking unfocused (and what a toolkit hides a popup over). The
   rule in `EventDispatcher` is now that a window is not told the pointer's half of a change when it is
   the one leaving, unless the focus is going to `PointerRoot`, where a reference *does* tell it
   (measured). Both probes are identical to the reference, and `FocusEventTest`'s class parks the
   pointer in its setup: it had been relying on wherever the previous test left it, which is the same
   trap §8 records for the probes.

6. **`--focus-on-click`, and the application behaviour it is for** (2026-09-29). With the 片段 page
   *closed*, this application's context menus do not open - on this server **and on Xvfb**, to the
   pixel (77968 non-background pixels / 4169 ink, which is the panel's own text; the menu is 77999 /
   6003). The state is real and is the application's: the page took the X focus (its own
   `SetInputFocus`, `revert_to = Parent`), the revert left the focus on the **root window**, no server
   focuses a window on a click, and the application never asks for it - so the main window is unfocused
   and a toolkit hides the popup of an unfocused owner. **`--focus-on-click`** is the missing
   window-manager half, and **on by default** (the owner's call): a click moves the focus to the
   toplevel it lands in with `revert_to = PointerRoot`. With it on the menu opens after the page closes
   (77999 / 6003) and the rest of the session is unchanged; `--no-focus-on-click` is the comparison
   switch, since with it off nothing moves a focus on a click, which is what a reference does.

   **And the same rule has a second half (2026-09-30, A23)**: the click that lands **in the menu** must
   not move the focus either. A menu is an override-redirect window, so without that exception the
   press gave the focus to the menu, the window that owned it was told `FocusOut`, and the toolkit
   unmapped the popup three milliseconds later - every item dead to the mouse while the keyboard still
   activated them. To check it: right-click an input box, press a menu item, and read the window the
   press **and** the release are reported against in the server log - both must be the popup's. Do it in
   both modes, with a fresh server and a **fresh client per run**: the app's state drifts across
   scripted gestures, and a stale server is a false A/B.

7. **The pointer sprite flickers with a client that repaints continuously** (2026-09-29, reported as "the
   cursor keeps flickering and the page is very laggy with the SSH tab open; `main` is normal").
   Reproduced on the **host window** and nowhere else: the sprite's box changes in **6 of 12** capture
   pairs on this build against **0 of 12** on `main` (`--no-glx` halves it, so GLX modulates rather than
   causes). The sprite is drawn over the frame and never into the framebuffer, so `GetImage`, the frame
   dumps and `BlinkProbe` all report a still screen while it flickers - **a flicker of the pointer needs
   a capture of the host window** (`screencapture -l <number>`, the number from `tools/host-windows.c`).
   The app's own frames are identical on both builds, it just repaints ~300 a second, which costs about
   a full core here as it does on `main`.

8. **A sprite drawn twice: part-screen damage that misses the pointer** (A21, fixed 2026-09-29). The
   sprite is drawn over the frame and never into the framebuffer, so the canvas keeps last frame's copy
   wherever this frame's damage does not reach it - and the next frame's `drawCursor` composites it
   again, which darkens the arrow. Seen only with a client whose damage **alternates** between
   rectangles that reach the sprite and rectangles that do not (EasyShell's SSH tab; the fix is in
   `JavaFxBackend.flushDamage`, which now unions the sprite's box into the damage). `main` damaged the
   whole screen every frame, so it could not happen there - and **a prompt that repaints its whole
   window never shows it**, which is why `tools/RepaintProbe` (a client that repaints in a loop) does
   not reproduce it either: its damage always covers the sprite. To see it at all the capture has to be
   of the **host window** (`screencapture -l <number>`), and the measurement is the sprite's own dark
   pixels across eight captures: `17 17 17 17 17 17 17 17` after the fix against
   `17 96 96 17 17 17 17 17` before.

9. **A forwarded application's input fields ignore their context menu and every Control shortcut**
   (A22, fixed 2026-09-30) - **two causes, in this order**. (1) The toolkit's connection was being
   killed: GTK sends a **core `CreateCursor` with a 1x1 placeholder pixmap and hotspot (1,1), outside
   it**, when it installs a RENDER-built cursor, and this server answered `BadMatch`, which ends a GTK
   process. A reference accepts it. (2) With the connection alive, Ctrl+A still did nothing: an XI2
   device event carries the modifier bits **twice** - the four bytes at 60-63 *and* the word at 72 -
   and GDK reads the second, which this server left nought. The instruments: **a GTK3 probe on the same
   display** (the toolkit the application itself uses) printing its own `state` per key, and a
   byte-for-byte diff of the same gesture's XI2 event on both servers. Two traps are recorded in A22:
   **a stale server process is a false A/B**, and **a test's own request can be malformed in a way that
   makes it pass** - both of that test's bugs were found by making it prove it can fail.

10. **A copy inside a forwarded application kills the application** (A19, fixed 2026-09-30). The
   clipboard bridge is made once, at startup, so its window belongs to the **master** screen - and in
   the default mode each connection has a screen of its own, whose setup reply names no other root. The
   `requestor` of the bridge's `SelectionRequest` was therefore a window the client could not place,
   and GDK wraps whatever window it is asked through: SIGSEGV in `XScreenNumberOfScreen`. The bridge
   keeps one window per screen now and asks through the owner's. **The A/B is the server mode**: a
   20-line GTK3 client that takes `CLIPBOARD` survives `--single-window` and segfaults in the default
   mode, and the frames are the application's own `hs_err_pid*.log` frames. `ClipboardBridgeTest` pins
   it without a toolkit, by asking the requestor's root (`RawClient.rootOf`) - and the whole class now
   runs in the default mode, which is where the bug lived. The trap: **a bed that only ever runs
   `--single-window` cannot see this class of bug at all** - a day of "not reproduced" was a day of
   testing a mode where the window was already placeable.
