---
name: xserver-working-state
description: "Where the xserver work stands: the backlog is the record; as of 2026-10-01 (branch `bugfix`) the reported bugs are fixed - **A19** (a copy inside a forwarded app killed it), **A23** (an application's context menu opened and then ignored the mouse: `--focus-on-click` gave the input focus to the menu, which is override-redirect, so the app unmapped its own popup), **A26** (JavaFX threw \"Destination region ... must fit within texture content bounds\" and the window never appeared, because the repaint covered the pointer sprite's box past the edge of the canvas), **A27** (the dock's Quit did not end the process: on macOS a JavaFX Quit is \"close every window\", never System.exit, and the toolkit's keep-alive thread is not a daemon), and **both halves of \"much slower than Xvfb+x11vnc\"**: **A24** the frame path's per-pixel copies/fills/uploads (now row-bulk, ~90-100% -> ~20% of a core) and **A25** the wire (an unchanged screen was re-sent 61 times a second, 5.39 MB/s, where `x11vnc` sends nothing - now the update is the box that differs, or nothing: see [xserver-frame-path-byte-at-a-time](xserver-frame-path-byte-at-a-time.md)). 475 tests. **A18 is FIXED (2026-10-03)**: the XInput extension advertised `first_event = 0`, and an event base is where a client library installs its wire-to-event converter, so libXi's device converter sat on core `ButtonPress` and AWT read every click's root y as the button number, which is exactly the "Nonexistent button 16" - see [the event-base trap](x11-extension-event-base-hijack.md). **A34** fixed the JPEG gate (a Tight/JPEG rectangle could go to a viewer that never listed Tight), **A35** answered the "less clear than x11vnc in the Finder" report a 1x framebuffer magnified 2x by the Retina window (measured 95.6% one-colour 2x2 blocks), not the renderer or the encoder - use `--scale 2` or watch it over VNC), **A36** re-measured the gap against Xvfb (one: XInput 2.0 against 2.4) and **A37** ran the X Test Suite: two protocol bugs fixed (an over-long BIG-REQUESTS request closed the connection; colour cells were handed out on a read-only visual) and one class left (per-request length validation). **A38** made the title-bar double click a real maximize/restore toggle in per-client mode. **A39** (single-window maximize/restore/drags) was attempted and **reverted the same night**: sizing the screen on every gesture is a feedback loop (screen -> viewport -> window size and position), which drifted the window downwards a step at a time in per-client mode and made single-window drags jump up and down, and it broke both modes' maximize. **Open: the reference motion shape, A37's per-request length table, and A39** - single-window's maximize/restore, where the platform undoes a fill within 0.1 s *with a size change*, so both the state-based and the timing-based discriminators fail (the entry has the log and two candidate designs). **All seven §9 capability candidates are DONE (F1-F7)** on `feat`, so both servers advertise 23 extensions, 22 the same; GLX is on by default (--no-glx). E1 CLOSED by decision (no fork/republish of the x11 library, no Lombok); E2 parked (GitHub Actions once the mirror exists); E6 ongoing; docs/verification-checklist.md is the whole-server tour."
metadata: 
  node_type: memory
  type: project
  originSessionId: 01353543-0895-4938-81fe-8b2a65312ab8
  modified: 2026-09-30T11:16:14.775Z
---

**The record that travels with the code is `docs/backlog.md`, `docs/design-notes.md` and `README.md`.**
This memory is only for picking the work up; when they disagree, the repository is right.

**2026-10-03 (later in the same session): "the picture is less clear than Xvfb+x11vnc seen in the
Finder" is the display, not the renderer (A35), and the JPEG gate was wrong (A34).**

**2026-10-03 (the end of the same session, bed at `.152`): the capability re-measurement (A36) and
the X Test Suite (A37).**

**2026-10-03 (A38, the same evening): a double click on the title bar now maximizes and the next one
restores.** macOS's zoom uses a "standard frame" derived from the window's **content**, and this
window's content is a client's X window whose size the client re-asserts - so the zoom stopped at the
client's width (1280x828 -> 1280x886 -> 1512x886, restoring nothing), and the platform's resize
arrived *behind* the server's own and shrank the client's window back. The fix keeps the platform's
**gesture** (`Stage.maximizedProperty` fires on the double click) and takes the **geometry**: fill =
the display's usable area (the screen grows to fit), a resize while filled = restore the size
remembered at the fill, and the platform's maximized state is cleared on that restore so the next
double click is a fresh `false -> true`. A 700 ms settle window puts the window back to the server's
size while the platform's own frame arrives; a new sink method `windowMaximized(boolean, w, h)` keeps
that request from being overwritten by the resize noise (measured: five double clicks alternating
1512x886 <-> 1280x828). Details and numbers in A38.

- **A36.** Xvfb and this server advertise the same 23 extensions (we have DPMS, they have MIT-SHM -
  unchanged) and **every version matches except XInputExtension: reference 2.4, here 2.0** - the one
  capability a modern client can still reach for (AWT logs it, GDK asks 2.4 and falls back). E0's
  sweep re-run is 17 of 28 identical; the two new differences are `SelectionProbe` (a fresh
  connection per section gets whatever resource-id base the server's reaping timing allows) and
  `MenuProbe` (the `FocusIn` a click produces, because `--focus-on-click` is on by default where
  Xvfb has no WM) - both now in `tools/README.md`.
- **A37, XTS.** Built `xts` 5.1.5 on the VM (`/root/xts`, installed to `/opt/xts`; the full recipe is
  in the backlog entry) and ran its `Xproto` set against both servers. Reference: **281 PASS /
  13 UNTESTED / 0 FAIL** ("All 122 tests passed, 21 not run"). Ours went from **121 of 122 purposes
  failing** to **120**, with the third case of every purpose now passing, after two real fixes:
  **an over-long BIG-REQUESTS request now gets `BadLength` and is discarded** (it used to close the
  connection, which the client sees as `SIGPIPE` and a hung application) and **`AllocColorCells` /
  `AllocColorPlanes` now answer `BadAlloc` on a read-only visual class** (they used to hand out cells
  that do not exist on TrueColor). The one class left is **per-request length validation**: every
  purpose's second case sends a request with a length one unit over or under its type's requirement
  and expects `BadLength`; that needs a table of exact-vs-at-least sizes, because the generated
  classes' `getSize()` is not the wire size (a generic attempt broke 300 of our own tests and was
  reverted the same hour - the comment in `RequestDispatcher.decodeCore` says so).
- **Bed notes**: the VM's IP is `.152` now (`tools/vm.sh` and every doc updated); it has **no
  IPv6 route**, so `apt` wants `Acquire::ForceIPv4=true` and there is no `curl` (use `python3`); the
  X server on `:61` (single-window, `--no-clipboard --no-tray`) is what XTS and the sweep run
  against, and it must be started as a *foreground session* - a `( … & )` inside a one-shot command
  is killed when that command returns. **Do not start the VMs yourself** (asked 2026-10-03).

- **Measured, not argued.** With the VNC backend and a viewer that lists Tight **and** a JPEG quality
  (the meter's `--enc 16,0,-223,7,-23`), a full frame and a scrolling terminal produced **0 JPEG
  rectangles** - the wire is ZRLE, i.e. the client's picture is the framebuffer bit for bit, so
  nothing on our side softens text. What differs is presentation: at the default `--scale 1` the
  client draws 1x, and the host window on a Retina panel magnifies it - a capture of the window at
  device resolution is **95.6% one-colour 2x2 blocks** (nearest, not bilinear: mean |host - nearest2x|
  3.27 against |host - bilinear2x| 3.49), i.e. one framebuffer pixel drawn as a 2x2 block. A Finder
  view of a 1x remote screen is 1:1, so the same picture looks crisper there.
- **The levers**: `--scale 2`/`--scale auto` (framebuffer 2600x1600, `Xft.dpi` 192, window maps 1:1 -
  real 2x detail for clients that follow the DPI), or watch the server over VNC so the same viewer
  presents the same picture. **The JVM does not follow `Xft.dpi`**: termora's block ratio stayed
  ~95% (i.e. still 1x) under `--scale 2` *and* under `-Dsun.java2d.uiScale=2`, so a Java client needs
  its own HiDPI handling before this window can be genuinely sharp.
- **A34, found while looking**: `RfbSession.sendUpdate` chose Tight/JPEG on the *eligibility test
  alone*, so a rectangle that looked photographic went to a client that never listed Tight or a JPEG
  quality - undecodable, and it reads as a rectangle that never updates. Now gated on
  `tightWanted && jpegQuality >= 0 && jpegEligible(...)`, and the session logs the viewer's whole
  encoding list on connect plus one line per JPEG rectangle. 475 tests green.

**2026-10-03, fourteenth session (branch `bugfix`): A18 is fixed, and it was the event base the
XInput extension advertised.** Reported as "you broke something - termora now errors on every click:
`java.lang.IllegalArgumentException: Nonexistent button 18` (and 16)". It was not a regression: it is
A18, open since 2026-09-29, and the deeper reason it survived a month of looking is that **the wire
really was correct and `xev` really did read it correctly** - the client that read it wrongly was the
one that had called into libXi first.

- **What it was.** `QueryExtension` answers a `first_event`, and that number is where a client
  library installs its wire-to-event converter: `XESetWireToEvent(dpy, first_event + i, handler)`
  writes into Xlib's own conversion table. libXi does that for its whole range the first time a
  client touches XInput 1.x (`XListInputDevices`) - AWT's toolkit connection does it at startup, measured; the GDK connection beside it went straight to XI2's `XIQueryDevice`. This
  server answered **`first_event = 0`** for XInput (the registry reports zero for an extension that
  defines no events *here*, and XI2's events are generic events at 35), so libXi's
  `XDeviceButtonEvent` converter landed on core `ButtonPress`: every click was unpacked with the
  device layout, which shifts everything from `root` on by one eight byte slot, and AWT read the
  click's root **y** as the button - y = 18 read as button 18, AWT's extra-button mapping subtracted
  two, and `MouseEvent` threw `Nonexistent button 16`. Xvfb answers base 66 for XInput and 85 for
  XKEYBOARD; ours were 0 and 0. **XKEYBOARD is the same trap under libX11**, so both ranges are
  reserved now (16 and 12 codes) and `ExtensionEventBaseTest` pins it. 475 tests green.
- **The instrument.** `sun.awt.X11.XToolkit.addEventDispatcher(window, dispatcher)` hands a
  registered `XEventDispatcher` the *same* `XEvent` the toolkit is about to dispatch, so the ninety
  six bytes that produced the exception can be hex-dumped with
  `MemorySegment.ofAddress(e.getPData()).reinterpret(96)` - next to the same click's bytes off
  `tools/tap.py`. That is what turned "stale buffer" into arithmetic: right at 8 and 32, wrong from
  40 on, the same shift on every click. The class lives on the VM as `/root/DispProbe.java`
  (`javac --add-exports java.desktop/sun.awt.X11=ALL-UNNAMED`) and needs
  `--add-opens java.desktop/java.awt=ALL-UNNAMED` plus `sun.awt.AWTAccessor` to get the content
  window id. See [the event-base trap](x11-extension-event-base-hijack.md).
- **Verified live** with the user's own launcher (per-client, `--glx`, `--geometry 1300x800`):
  termora's own AWT log prints correct fields (`x = 294, y = 191, x_root = 304, y_root = 191,
  state = 0, button = 1, same_screen = true`) for a dozen clicks in the toolbar, the terminal and
  the message list, and `grep -c Nonexistent` is 0.
- **Trap for next time**: the live server binds `:95` and `GrabCrossingTest` binds 95 too - stop the
  test server before `mvn test` (the same note is in the 2026-09-28 block below, and it bit again).

**2026-10-02, eleventh session (branch `bugfix`): the tray icon was a blank square, and the Chinese
input question is answered.** Reported as "EasyShell's and termora's Chinese input (switching the Mac's
input method) and termora's tray still look wrong, and Xvfb+x11vnc is the same".

- **The tray was a real bug of ours, and it is fixed.** A real toolkit does not draw into the window it
  docks: `java.awt.SystemTray` docks a window named "Content window" and draws the image in its **child**
  (`sun-awt-X11-XCanvasPeer`) - read off this server's own window tree while the client ran. The tray
  captured the docked window's *own* pixels, which per B5 hold its pixels and not its children's, so
  what reached the menu bar was an empty background: a white square, for every Java application.
  `TrayBridge.capture` now reads with `XWindow.pixelFromInferiors` (the `IncludeInferiors` read, alpha
  preserved). Verified live (menu bar: white square -> the icon's white background with its red disc)
  and pinned by `TrayBridgeTest.anIconDrawnByAChildWindowReachesTheMenuBarToo`, which reads the icon
  window's background (green) before the fix and the child's drawing (red) after. **473 tests green.**
- **Xvfb+x11vnc "the same" is expected and is a different cause**: Xvfb owns no tray at all and x11vnc
  provides none, so a dock request finds nobody and the icon never appears - no bug there.
- **Chinese through the Mac's input method**: over VNC it can never arrive (RFB's key events are
  keysyms, not composed text) - which is why the reference looks the same. This server *does* bridge the
  host's composing (C10: a claimed keycode + `MappingNotify`, verified at the wire level), but whether a
  toolkit honours that mapping change is the toolkit's business. **The path that works in every client
  is an input method on the client machine**: `dbus-launch --exit-with-session`,
  `XMODIFIERS=@im=ibus ibus-daemon -drx`, a Chinese engine (`ibus-chewing` is on the VM;
  `ibus-libpinyin` is what pinyin wants), the application started with `XMODIFIERS=@im=ibus` (and
  `GTK_IM_MODULE=ibus` for GTK/JavaFX) - **against a `--single-window` server**, because an input
  method's candidate window is a window of its own and in the default mode it lands on that client's own
  screen instead of over the application (measured; `PerClientWindowTest` says so). The project's own
  verification is ibus composing `nihao` and committing 你好 into a forwarded `xterm`.
- **What a scripted session cannot do** (and why this session's host-IME re-check was inconclusive):
  injecting a *composition* into the Mac's input method needs a hand on the keyboard - `hostinput type`
  posts `CGEventKeyboardSetUnicodeString`, which bypasses the IME entirely, and plain key posts into the
  server's window did not produce a composition in this session. The recipe above does not depend on it.

**2026-10-01, tenth session (branch `bugfix`): A32 - Tight's JPEG method for photographic content.**
The last item the review left open, and the one the review's own numbers pointed at: ZRLE is a
palette-and-run encoding, so a rectangle where nearly every pixel differs from its neighbour falls
through to raw - 3.1 MB and 56 ms of one core for a full 1300x800 frame.

- **The protocol, from the community RFB specification (fetched, not remembered)**: JPEG is a *Tight
  compression method* - encoding **7** with control byte **0x90** - carrying a compact 1-3 byte length
  and a JFIF stream; the **JPEG quality pseudo-encodings are the client's capability**, and `-23` is
  the **highest** quality with `-32` the lowest (the opposite of what the numbering suggests); a Tight
  rectangle may not be wider than **2048**, so `RfbSession.splitForTight` keeps its rectangles inside
  that. `tools/vnc-meter.py` learned to skip a Tight rectangle's JPEG so a measurement can be taken
  from a real socket.
- **The rule**: `TightEncoder.looksPhotographic` samples the rectangle and asks for more than 64
  colours. A photograph inside flat panels counts as one (JPEG renders the flat parts faithfully and
  ZRLE is the wrong tool for the whole rectangle), while text and panels - a handful of antialiasing
  greys - stay on ZRLE, which is lossless. That second half is what the tests pin hardest.
- **Measured**: encoder, one 1300x800 frame - flat UI 2.2 ms/1.5 KB and text 13.3 ms/255 KB (both stay
  ZRLE) against photograph 56.3 ms/3121 KB -> **30.6 ms/697 KB**; on the wire, a client painting a
  full-screen photograph with the meter watching - **3.12 MB with ZRLE advertised, 0.70 MB with Tight
  and the JPEG qualities advertised**, same change. Tests: `TightEncoderTest` and the JPEG half of
  `VncBackendTest`, which decodes the stream the way a client does.
- **Left**: ImageIO's JPEG writer costs 30 ms for a large frame (a hand-written encoder would shrink
  that), and noVNC does not decode Tight-JPEG - a browser viewer still gets ZRLE, which is why the ZRLE
  path had to stay good. **468 tests green.**

**2026-10-01, ninth session (branch `bugfix`): the whole-project review** - `docs/review-2026-10-01.md`
is the record, and it is the document to read before picking anything up from it. Two defects were
reproduced and fixed (**A29**, **A30**), the rest are written down as A31-A33 and in the review's own
list.

- **A29: a window's own pixels had no budget.** Every window keeps its pixels (B5) and the store is the
  window's own size - the one allocation here with no bound. Reproduced with `-Xmx256m`: a client that
  maps a 12000x12000 window hit `OutOfMemoryError` inside `Framebuffer.allocate` and was dropped;
  65535x65535 would be 17 GB. `keepsOwnPixels()` now requires the window to fit the largest screen
  (8192x8192) and a bigger one draws straight onto the screen (the root's path) - nothing is refused,
  and no window that fits on the display changes. Live A/B: before, `OOM during MapWindow`; after, the
  same client maps and draws with no OOM.
- **A30: the ZRLE palette search ran past the point of being useful.** Past sixteen colours a tile can
  only be RLE or raw and neither reads the palette. A/B by content (min of 36 full 1300x800 frames,
  harness in the encoder's package): photo-like **79-100 ms -> 57 ms**, two-colour 10 ms and text-like
  13.5 ms **unchanged** - the common case must not pay for the rare one.
- **The numbers that matter for what is left**: a full-screen ZRLE update costs 10 ms flat-colour to
  57 ms photographic of one core, so the VNC path's remaining wins are tile-level updates (A31) and
  Tight/JPEG for photos (A32). The wire already skips unchanged screens entirely (A25).
- **Checked and sound** (so the next review does not redo it): every client-controlled allocation has a
  budget now; every thread is a daemon or is closed by `stop()`; `clients`/`sessions` are COW and the
  rest is under the server lock by convention (the clipboard bridge takes it itself); an unimplemented
  request throws `BadImplementation` rather than doing nothing.

**2026-10-01, eighth session (branch `bugfix`): A28 - two pointers over the window, fixed by making
the *sprite* give way.** Reported: moving the mouse over the server's window showed the system arrow
*and* the X sprite until the window was clicked, and dragging another application's window over it
brought the system arrow back. One cause, and the platform decides it:

- **The pointer belongs to the active application.** A native probe (no JavaFX) called
  `[NSCursor hide]` and watched `CGCursorIsVisible`: 0, then **1** the moment another application was
  activated, and never back. JavaFX's `MacCursor.setVisible_impl` also **skips the native call** when
  its cached `isNSCursorVisible` agrees with what is wanted, and the cache is stale from that moment -
  forcing `DEFAULT` then `NONE` does not help while another application is in front.
- **The fix:** hide the platform pointer only while this process has a focused window
  (`JavaFxBackend.applicationActive`, a count of focused windows - the hide is per process), and draw
  **no sprite** while it does not; a focus change repaints the sprite's own box
  (`repaintCursorArea`), since no client draws anything when the host window comes or goes.
- **Measured live** (server on `:95` + a client that draws, pointer on a root area of the window,
  `screencapture -R` of a 40x30 region **without** `-C`, plus `CGCursorIsVisible`): ours-in-front
  **hidden/394 sprite pixels**; Finder-in-front **visible/0**; ours again **hidden/394**. Before the
  fix the middle row was **visible/402** - the two pointers. **466 tests green.**
- **The harness that made it measurable** (reusable): `/tmp/xsn/cursorvisible.c`
  (`CGCursorIsVisible`), `screencapture -x -R"x,y,w,h"` + `sips -s format bmp` + a pixel count against
  the background colour (`/tmp/xsn/region_check.py`) to see whether the *sprite* is on the canvas, and
  `osascript -e 'tell application "System Events" to set frontmost of first process whose unix id is
  <pid> to true'` to make *this* application the front one by pid (a synthetic click does **not**
  activate it reliably).

**2026-10-01, seventh session (branch `bugfix`): A27 - the dock's Quit did not end the process.** The
report was "right-click xserver in the dock, press Quit, and the EasyShell process on `.147` is not
released". What the Quit *is* turned out to be the whole answer:

- **On macOS a JavaFX app's Quit is `GlassStage.requestClosingAllWindows()`** - a close request on every
  window, and never `System.exit` (decompiled from OpenJFX 25: `QuantumToolkit$2.handleQuitAction` is
  that one call). A minimal JavaFX app ignores the request entirely and stays alive; with **AWT alone**
  the default strategy is `System.exit(0)`, which is why `--backend vnc`/`headless` already quit fine.
- **The clients *were* released**: the window close asks each client `WM_DELETE_WINDOW` and cuts it off
  after the grace, and EasyShell on `.147` exits within a second of that - 5 runs of 5, in both modes.
  What stayed was the **process**: `jstack` twelve seconds after the Quit showed no `xserver-*` thread
  at all, only `JavaFX-KeepAlive` (non-daemon, outlives `Platform.exit()`) and `DestroyJavaVM`.
- **The fix:** `XServer.startEventLoop()` returns the display thread and `Main` **joins it and then
  `System.exit(0)`** - the loop is the server's life, and the process follows it. Live A/B with the real
  EasyShell: before, alive 12 s later; after, EasyShell gone at +1 s and the server at +3 s
  (`--single-window`) / +1 s (per-client). New test `XServerShutdownTest.theDisplayLoopEndsWhenTheServerDoes`;
  **465 tests green**.
- **The recipe for the next time** (a real dock Quit, aimed at one pid - `osascript quit app` cannot
  target the right `java`): a two-line Objective-C tool calling
  `[[NSRunningApplication runningApplicationWithProcessIdentifier:pid] terminate]`
  (`clang -fobjc-arc -o appquit appquit.m -framework AppKit`). It is the same request the dock makes, and
  it is refused or ignored exactly as the dock's is. Watching a *client* on `.147` means running it from a
  background local `ssh` that stays up (`setsid nohup` inside a one-shot `tools/vm.sh` command does not
  survive), and counting with `pgrep -c -f "EasyShell/bin"`.
- **The other half, with the user's own patch (same day)**: the window-close path *asks*
  (`WM_DELETE_WINDOW`) and then cuts, but the **stop** path - Ctrl-C, a signal, the control port, any
  backend with no window - only cut, which is what leaves a program whose toolkit was never told to quit
  running with a dead display. `XServer.stop()` now asks first (`askClientsToCloseForStop`) and waits for
  the ones it asked (bounded by `--close-grace-ms`) before closing anything. Three things to keep:
  **iterate `clients` with `screenOf(client)`, not `sessions`** (in single-window mode - which
  `--backend vnc` implies - the master session has no client, so a `session.client()` loop asks **0**;
  measured 0 before, 4 after with EasyShell); **the wait goes after the asks**, not as a sleep per window
  inside the send loop (that loop also runs on the display tick, and an interrupt thrown out of it
  abandons the shutdown); and **an ask must be readable** - closing with unread input is a reset, which is
  the case the new test reproduces by reading while the stop is running. **466 tests green.**

**2026-09-30, sixth session (branch `bugfix`): A26 - the JavaFX backend threw
`IllegalArgumentException: Destination region ... must fit within texture content bounds` and the window
never appeared.** The rectangle in the message named it: 48 wide, 25 high, 1268..1316 against a 1300
wide canvas is **two** 24 wide cursor sprites side by side - the union of the sprite's box now with the
one it came from. `flushDamage` clipped the damage to the screen and the viewport and then unioned the
**sprite's own box** into it (that union is A21's fix), without clipping the union; the sprite's box
reaches past the screen whenever the pointer is on the last pixel, and JavaFX does not clip a
`drawImage` destination region. `drawCursor` had the same hole for the cursor image.

- **The fix:** `damageWithSprite(damage, cursor, bounds)` returns `union ∩ bounds`, and
  `visibleSprite(sprite, viewport)` clips the sprite for the cursor image (drawn from the matching
  source offset). The clip is applied **before** `logical()` - the canvas is in logical pixels.
- **Reproduced live, and this is the recipe to reuse:** Mac, `--backend javafx --single-window
  --geometry 700x500`, plus **a client that keeps drawing** (a throwaway `RawXClient` main: map a
  window, fill a rectangle every 200 ms - without a client the session is idle and no sprite is
  composited at all, which is why the first attempts saw nothing), then walk the host pointer to the
  last column with `tools/host-input.c` (`clang -o hostinput tools/host-input.c -framework
  ApplicationServices`; `hostinput move x y`). Where the window is comes from the X pointer itself:
  move to two known host points and `PointProbe` reports the X coordinates. **A/B: HEAD threw 21
  times in one run, identically shaped; the fixed build 0.**
- **Tests:** `JavaFxBackendTest.aSpriteHangingOverTheEdgeIsClippedToWhatTheCanvasCovers` (new) and
  `theFrameDamageCoversTheSprite`, which had a sprite at (100,200) on a **64x48** bed - off screen -
  and passed only because nothing clipped. **464 tests green.**

**2026-09-30, fifth session (branch `bugfix`): A25, the WIRE half of "less smooth than Xvfb+x11vnc",
is fixed - and it was not CPU at all.** Counted, not guessed, with a new instrument committed as
`tools/vnc-meter.py` (a real RFB client that keeps one incremental request outstanding). EasyShell's
aliyun tab open, untouched, server on `.147`: our server was sending **5.39 MB/s and 61 full-window
updates a second** - the tick rate, for a screen that was not changing, because the application
repaints its whole panel ~300 times a second without changing a pixel - where `x11vnc` sends **nothing
at all** and holds the request.

- **The fix:** `RfbSession` remembers what each client has been sent (`sentPixels`) and sends only the
  **bounding box of the pixels that really differ**; an unchanged region is not sent, and
  `updateRequest` loops back into its wait so the request stays outstanding. **After: 0.00 MB/s idle,
  0.00 MB/s for pointer motion** (the sprite's box is damage but its pixels do not change), and
  0.01-0.05 MB/s for typing/scroll/flood - the same order `x11vnc` costs on that flood.
- **Verification that a skip is not a lost update:** the meter's decoded picture vs `xwd -root` of the
  server's framebuffer after a flood = **136 differing pixels out of 1,040,000, all in the text
  cursor's 8x17 cell** (the blink raced the capture). New
  `VncBackendTest.aRedrawThatChangesNothingIsNotSentButAChangeAfterItIs`; whole suite **463/463**.
- **Committed this time** (the fourth session's sandbox could not): `5be50f9` (frame path), `e431f51`
  (docs + memory) and this session's perf commits, and the iMessage route works. **Not pushed.**
- **Traps:** a leftover `--backend headless --display 68` server on the Mac makes `TrayBridgeTest`,
  `DropBridgeTest` and `XInputTest` fail with "Address already in use" (killed this session); the
  meter's client-view XWD needs `header_size` in **bytes** (100) or the picture is shifted by most of
  a row; the comparison in the server is ARGB, deliberately stricter than the client's packed pixel.

**2026-09-30, fourth session (branch `bugfix`): the "everything is very slow, especially EasyShell"
report is FIXED, and it was the frame path's per-pixel loops, not (only) the byte-at-a-time reads.**
Measured on the real bed, not guessed: server **on `.147`** with `--backend vnc` (the same shape as the
`Xvfb`+`x11vnc` baseline), EasyShell on `.147`, the aliyun SSH tab open, settled.

- **The A/B: `git archive HEAD` = ~90-100% CPU of one core; the fixed build = ~20%.** `top -H` named the
  EasyShell connection thread, and repeated `jstack` of it showed the whole cost was four per-pixel
  loops: `XServer.compositeOwnPixels` -> `Framebuffer.copyFrom` (the copy that ends every draw),
  `DrawHandlers.copyArea` -> `Rasterizer.copyRect` (a scroll, and its source was an **XPixmap**, which
  the row fast path first rejected), `RenderExtension.fillRectangles` -> `Compositor.draw`/`blend`, and
  `ImageCodec.putZ` (a `PutImage`).
- **The fix**: row-bulk `Framebuffer.copyFrom` (`System.arraycopy`); row-bulk `Rasterizer.copyRect`
  accepting a pixmap source and marking the store's damage; `Rasterizer.writeRun` + `ImageCodec.putZ`
  bulk row write; `Picture.fillArgb` + `Compositor.fillRectangles` bulk fill for opaque
  `Src`/`Over` and `Clear`; plus the request body read in bulk, `readPutImage` copying from the
  request's own bytes, and three per-request `log.info` demoted to `log.debug`.
- **New test** `draw/FramebufferCopyTest` pins the `copyFrom` fast path against the pixel loop (both
  surfaces, same-surface overlap both axes, off-edge); `DamageTrackingTest` caught the one real miss
  (the fast copy did not mark the framebuffer's damage). Green: `RasterizerTest`, `ImageCodecTest`,
  `BlendFastPathTest`, `AlphaMaskTest`, `ArgbAlphaTest`, `VariableLengthDecodersTest`, `ZrleEncoderTest`;
  a screenshot of the aliyun tab renders correctly.
- **Traps this cost**: the server must run **on `.147`** for a fair comparison (a Mac-side server puts
  every X request on the LAN); on the Mac it needs `-Djava.awt.headless=true` or `GraphicsEnvironment`
  aborts the JVM; easy to launch/stop with `setsid nohup` + `xdotool` on `.147`. Bed stopped after.
- **The wire half of "slower than x11vnc" was left open here, and is A25** (see the fifth session
  above): `RfbSession` sent the whole damage rectangle every time even when the pixels equalled the
  last frame, where x11vnc compares tiles and sends nothing when they have not changed.
- **This session could not commit or notify** (the sandbox kept `.git` read-only and blocked Apple
  Events to Messages). **Both were done by hand in the fifth session**: the frame-path work is
  `5be50f9`, the docs and memory are `e431f51`, and iMessage works.

**2026-09-30, third session (branch `bugfix`): A23 is FIXED - the right-click menu that ignored the
mouse - and the fix is three lines in `InputState.focusOnClick`.** `mvn test` **458/458**, commits
`e931b4a` (fix + test), `9b10cea` (docs), **not pushed**.

- **The cause, and it is `--focus-on-click` (the default) doing the wrong half of its job**: it gave the
  input focus to the toplevel a click landed in, and the toplevel a click *inside a menu* lands in is
  **the menu** - an override-redirect window. So the window the menu belonged to was told `FocusOut` on
  the press, and a toolkit hides a popup whose owner it believes unfocused: EasyShell unmapped its own
  menu **3 ms** later, the release was reported against the window underneath, and every item was dead
  to the mouse while the keyboard still activated them. A window manager never focuses an
  override-redirect window. The fix: `if (target.isOverrideRedirect()) return;` before the
  already-focused check, so the window-manager half this server added on purpose stays.
- **The A/B is one build with the flag the only difference**: the popup is destroyed after a press on an
  item **5 of 5** times with the rule missing and **survives 5 of 5** with it, in `--single-window`
  headless (XTEST) *and* in the default per-client mode (the host window driven by `tools/hostinput`,
  built from `tools/host-input.c`). The line to read is the window a button is *reported against*:
  `button 1 up at (234,160) -> 0x40001e` is the popup, `-> 0x400002` is the window underneath.
- **`main` is normal because it has no `--focus-on-click` at all** - the user's own premise, measured.
  Its worktree is `/private/tmp/xmain` (already built); the same gesture there gives press **and**
  release in the popup and the item's dialog 40 ms later. Also: **`--focus-on-click` still focuses an
  ordinary toplevel** (the log shows `focus set to 0x400002, was the pointer` when the search box is
  right-clicked), which is the A20 case it exists for.
- **The previous round's "`--focus-on-click` is ruled out" was wrong**, and what it lacked is the
  control: **a fresh server AND a fresh client per run**. Restart the client between rows - the app's
  own state drifts across scripted gestures, and one successful activation leaves a modal dialog over
  the next run.
- **Bed notes this round**: `tools/bed.sh` passes `$XSERVER_OPTIONS` to the **JVM**, so `--backend` and
  friends cannot go there (`Unrecognized option: --backend`) - start the server by hand instead
  (`nohup java -Dlogback.configurationFile=... -cp "target/classes:$(cat target/tools-cp.txt)"
  io.github.oyzh1994.xserver.Main --display 95 ...`). The EasyShell AppImage is
  `/root/EasyShell_v1.2.2_linux_arm64_build.AppImage` and the left-panel **search box is at screen
  (186,115)**; the menu it opens is the *host list's* (添加连接/添加目录/…), not a text menu - the
  复制/粘贴/全选 of the report belong to a text field elsewhere. In per-client mode the host window was
  at global (106,81) 1300x728 with its content 1:1 over screen (0,33) - **offset +106,+76**, calibrated
  off the log rather than off a capture.

**2026-09-30, second session (branch `bugfix`): A19 is FIXED - a copy kills the application because the
clipboard bridge asked through a window on the wrong screen - and A23 is new and open: a context menu
that opens and then ignores the mouse.** Commits `4f16ed6` (the fix + its test), `24a3498` (docs +
instruments), **not pushed**.

- **The cause, and it is the *mode* that hid it for a day**: the bridge is made once at startup, so its
  window is a child of the **master** screen's root - and in the **default** (one window per client)
  mode every connection gets a screen of its own, whose setup reply names that root and no other. The
  `requestor` of the bridge's `SelectionRequest` was therefore a window the client could not place; GDK
  wraps whatever window it is asked through (`gdk_x11_window_foreign_new_for_display`) and dies in
  `XScreenNumberOfScreen` on the attributes struct a failed lookup left uninitialised. The bridge keeps
  **one window per screen** now and asks through the owner's own screen's.
- **Every "not reproduced" attempt in the previous entry ran `--single-window`**, where the master
  screen *is* every client's screen and the window was already placeable. **A bed that never runs the
  default mode cannot see this class of bug** - that is now checklist item 10.
- **The reproduction is 20 lines**: `tools/clipboard-owner.py` (a GTK3 window that takes `CLIPBOARD`,
  so the bridge then asks it for the data) - `EXIT=124` (alive, killed by the timeout) against
  `--single-window`, `EXIT=139` (SIGSEGV) in the default mode, with gdb giving the application's own
  three frames. The app itself, driven through the host window with `tools/host-input.c`: Ctrl+A then
  Ctrl+C puts the box's text on the Mac's clipboard and the process lives. `ClipboardBridgeTest` now
  runs in the default mode and asks the requestor's root (`RawXClient.rootOf`); it fails without the
  fix.
- **A23 (open)**: the menu opens, the item under the pointer highlights, and the click does nothing
  while **the same item activated from the keyboard copies** (Down, Return: the Mac's clipboard went
  from a hash string to the box's `aa`). The press and release *are* delivered to the menu's own window
  as XI2 device events; a **GTK3** client's menu item does activate on the same server, so it is what
  the toolkit reads out of the event rather than where it goes. Ruled out by measurement:
  `--focus-on-click`, the grab status (`status 0` every time), and the XI2 button mask (flipping it to
  the buttons held *during* the event did not fix it and was **reverted** - `buttons(state)` is what the
  verified title-bar drag stands on). Next step is `tools/tap.py` on both servers with the same gesture,
  diffing the XI2 button event; `tools/XiProbe`'s dump now prints the mask at 80 for that.
- **Two aiming traps, both in A23**: `screencapture -l <window>` pads a window by **56 points on every
  side** (1280x728 comes out 1392x840), so `screen = origin - 56 + 0.696 * displayed` - three "the item
  is dead" readings were misses before that; and the **hover highlight** is the cheap way to prove the
  pointer is on a row. Also: `pkill -9 -f "[E]asyShell"` matches the *launch* line when both are in one
  remote command, so it kills the shell about to start the app - two `tools/vm.sh` calls.
- **A WIP hunk is left in the working tree, uncommitted, and it is not mine**: `ClipboardBridge`'s
  `FETCH_TARGETS` with `COMPOUND_TEXT` added (the previous session's paste hypothesis, 03:17). It looks
  wrong on the merits - the bridge serves UTF-8 bytes for *every* text target, so offering
  `COMPOUND_TEXT` invites a client to decode UTF-8 as compound-text - and nothing measured it. The
  paste path was measured working today without needing it (`wait_for_text` returns the host's text).
  My fix and test were committed with that hunk taken out, so the tree's only difference from HEAD is
  that hunk.
- **`mvn test` 457 tests, one flake**: `ExposeOnMapTest` failed once with `Connection reset` on connect
  (display 96) and passes on re-run in isolation - a port/timing flake, not the change. The three new
  instruments are in `tools/` now: `clipboard-owner.py`, `gtk-menu-probe.py` (prints its own menu
  geometry - the aiming fix), `host-input.c` (click/type/`ctrl` with the Control key itself held).

**2026-09-30 (branch `bugfix`): an application's input fields lost their context menu AND their Control
shortcuts - two independent causes.** `mvn test` **456/456**, commits `6d8be4f` (the cursor), `b5fa930`
(the XI2 modifier word), `839ccfa`/`a3c24f1` (docs), **not pushed**.

- **Cause 1, and it had to be fixed first: this server was killing the toolkit's connection.** GTK
  installs a RENDER-built cursor by sending a **core `CreateCursor` with a 1x1 placeholder pixmap and
  hotspot (1,1)** - outside it - and we answered **`BadMatch`**; GTK's default handler *ends the
  process*. Everything after that (the selection ownership a copy needs) went nowhere, which is what
  "the menu items are clickable but do nothing" was. A reference accepts it (measured with a C probe on
  both displays). Both handlers dropped the check.
- **Cause 2: the XI2 device event left the word at 72 nought.** A device event carries the modifier
  bits **twice** - the four bytes at 60-63 (documented) and the word at 72 - and **GDK reads the
  second**: against Xvfb it reported `state=0x4 ctrl=True` for a Ctrl+A where ours reported `state=0x0`.
  Measured by a byte-for-byte diff of the same XI2 event on both servers.
- **The instrument that found both: a GTK3 probe on the same display** (the toolkit the application's
  own Linux backend uses) printing its own `state` per key - `strings libgdk-3.so` shows it calls
  XkbGetState/XkbGetMap/XkbSelectEvents, so it is GDK's reading that decides, never the core event's.
- **Two traps, both recorded in A22**: **a stale server process is a false A/B** (two rounds of "the
  change is in the source and nothing changed" were a server started *before* the build still holding
  the port - check `ps -o lstart` against the build), and **a test's own request can be malformed in a
  way that makes it pass** (the new test wrote the cursor hotspot at 24/26 where they are 28/30, and its
  error reader looked at offset 11 instead of 10) - found by making the test prove it can fail.

**2026-09-29 (branch `bugfix`): the flickering pointer is a sprite drawn twice - part-screen damage
left last frame's copy in the canvas.** `mvn test` **454/454**, commits `63da328` (docs + instruments),
`d4acd75` (the fix), `08709aa` (the test).

- **The cause**: `main` damages the **whole screen** every frame; this branch damages *part* of it (a
  B5-era optimisation), and the app's damage alternates between rectangles that reach the sprite and
  rectangles that do not. The sprite is drawn over the frame and never into the framebuffer, so the
  canvas kept last frame's copy wherever the damage missed it - and `drawCursor` composited **that**
  again: the arrow's antialiased edges darkened (17 dark pixels on a covered frame against 102 on an
  uncovered one, same centroid).
- **The fix**: `JavaFxBackend.flushDamage` unions the sprite's box into the damage
  (`damageWithSprite`, unit-tested - the test fails without it). Measured across 8 host-window captures
  with the SSH tab open: `17 96 96 17 17 17 17 17` before, `17 17 17 17 17 17 17 17` after. The Swing
  backend needs nothing: an opaque `JPanel` erases the dirty rect first.
- **The instruments that were needed**, because the sprite is not in the framebuffer: capture the
  **host window** (`screencapture -l <number>`, and `tools/host-windows.c` prints the number now), and
  time the changes rather than sampling once a second (`tools/BlinkProbe`). A frame dump writes once a
  second, so a flicker at the client's repaint rate lands on the same phase every sample.
- **`tools/RepaintProbe`** drives the frame path without an application, with a note that it does *not*
  reproduce this one: its damage always covers the sprite.
- **Still open, and it is the "laggy" half of the report**: the app repaints ~300 frames/s of nearly
  identical content and this server spends about a full core on it - the same on `main`, so it is a
  frame-path cost rather than a regression.

**2026-09-29, later (branch `bugfix`): "the context menu flashes and vanishes after the 片段 page is
closed" - measured as the *application's* behaviour, and a switch answers it.** `mvn test` **453/453**,
commits `e44b6b1` (a focus-reporting corner), `275e5ad` (`--focus-on-click`), **not pushed**.

- **The measurement that settled it**: with the page *closed*, a right-click on a host row renders no
  menu on **this server and on Xvfb identically** (76968 non-background pixels / 4169 ink = the panel's
  own text; the menu is 77999 / 6003). My first run of that table counted *override-redirect windows in
  the tree* - which lists unmapped ones - and read "the menu is there" off a window that is always
  there; **the correction is the lesson**: compare the client's *pixels*, not the tree's rows.
- **The state** (both servers): the page takes the X focus (`SetInputFocus(page, revert_to = Parent)`),
  the revert out of a child of the root leaves it on the **root window**, nothing focuses a window on a
  click on either server, and the application never asks afterwards - so the main window is unfocused
  and JavaFX hides a popup whose owner looks unfocused. That is the flash.
- **`--focus-on-click`** (off by default, because a reference does not do it): a click moves the focus to
  the toplevel it lands in, with `revert_to = PointerRoot` so a window that goes away hands it back to
  the pointer's window. With it on, the reported sequence opens the menu again (77999 / 6003).
- **A real divergence found on the way** (`e44b6b1`): a window *on its way out* was handed
  `FocusIn <departing> detail=Pointer`, which a reference never sends and GDK takes for where the focus
  lives. Two probes pin it now - the new **`tools/FocusRevertProbe`** (the pointer *in* the window that
  goes away: two overlapping root children, `FocusOut`, `FocusIn <underneath> detail=Inferior`, and
  nothing else) and `FocusProbe`'s `"warp 50 50" "focus B" "unmap B"` - and both are identical to the
  reference. The rule: a leaving window is not told the pointer's half, *except* under PointerRoot,
  where a reference does tell it.
- **`FocusEventTest` parks the pointer in its setup now**: one of its cases depends on what the pointer
  is in, and it had been reading whatever the previous test left behind - the §8 trap, in a test.

**2026-09-29 (twenty-fifth round, branch `bugfix`): two more reported bugs - a window that could not be
dragged, and a flaky focus that turned out to be the XI2 event shapes.** `mvn test` **450/450**, commits
`e2f1bdd` (the shapes), `b6424b4` (the button mask), `8bd95fb` (docs), **not pushed**; iMessage sent and
the beds stopped.

- **The "sometimes the input box does not get the focus although typing works"** was **the XI2 enter,
  leave and focus events sent in the device event's shape**: a reference sends 120 bytes for a key,
  **76 for a crossing and 104 for a focus event**, with `same_screen`/`focus` at 48/49, `buttons_len`
  at 50 and the mask at 72; all three came out at 120 with a device event's offsets, so GDK read this
  server's lengths as its flags and its mask eight bytes late. A/B: **1 of 3 starts showed the focus
  ring before, 4 of 4 after**. `Xi2Event.Shape` holds the layouts and `XInputTest` pins them.
- **"No window can be dragged by its title bar, main was fine"** was **the XI2 button mask left zero on
  every device event**: GDK reads the drag state from it, so a motion with nothing held is a move. A
  reference sends bit n for button n (button 1 = `0x02`) and the state *before* the event (a press is
  clear, its release still set - the core `state` convention). Same scripted drag: 60 px on `main`
  (core rail, no XI2 there), 20 px before the fix, 60 px after.
- **The measured tool for both was `tools/tap.py` in front of *both* servers with the same client
  gesture**, decoding each log as messages (a reply 32 + 4/unit, an event 32, a GenericEvent 32 + its
  own length, and **the setup reply is the one with an eight byte header**). `tools/README.md` has the
  recipe; the sizes are the line to compare.
- **Two traps on the way, both mine and both pinned by the test now**: an XI2 event's arrays follow the
  *whole* mask (values four bytes early are read as garbage), and a motion must be written in the
  *device* branch of the two length fields at 48/50 - get either wrong and the client reads past the
  event: **GDK died of it (SIGSEGV at a nought pointer plus `0x90` inside `gdk_display_get_event`)
  every time a title bar was dragged**, which is what a "process crash" looks like from the outside.
- **A reference motion's shape (136 bytes, two FP3232 axes) is written and measured but no caller uses
  it**: GDK dies on its first arrival (same SIGSEGV) while the shorter, valid form works. Recorded in
  `Xi2Event.Shape.MOTION` and the checklist.
- **The copy crash is NOT reproduced** and is open as **A19**: A19 has the report, the bed's own crash
  signature (Xlib's `_XIOError`/SIGBUS - the display connection died, seen in both apps before today),
  the four places this server closes a connection with the log line each leaves, everything tried (the
  connection menu's 复制信息, the search box's 右键 → 复制 with Ctrl+A and a drag selection, Ctrl+C/V,
  the host clipboard set and empty, on both builds) and the two candidates left (the clipboard round
  trip looping, and the bridge's INCR branch). **The next step is a capture, not a theory**: the
  server's disconnect line plus the app's `hs_err_pid*.log` name which side died first.
- **Bed notes**: `es-run.sh` on the VM now waits for a real start and reports `FAILED display|db-lock`
  (a stale instance makes a fresh start die of the H2 lock and reads as a crash); a leftover server on
  a display the suite binds (6094 was mine) makes `VncBackendTest` fail with `BindException`; and the
  app's own crash logs live in `/root/hs_err_pid*.log` - they are the first thing to fetch.

**2026-09-29 (twenty-fourth round, branch `bugfix`): the first user-reported bug - a keystroke went to
the *root window* when the focus was left on it, so EasyShell could not type after closing a page.**
`mvn test` **449/449** (was 447), headless **449 with its 2 skips, BUILD SUCCESS**, commit `1aefb94`,
**not pushed**; iMessage sent and the beds stopped.

- **The report** (the user's own words, from the phone): click 片段, close the page, then click the home
  page's search box - no character arrives; open and close 设置 and it types again.
- **The cause, from the server log with the client on the VM**: the 片段 page is a toplevel of its own,
  the client calls `SetInputFocus(page, revert_to = Parent)` when it opens it and closes it with
  **`UnmapWindow` alone** (it relies on the server's revert, which X11 allows). The revert lands on the
  root window - a reference does the same (`xdpyinfo` on Xvfb after the same gesture: `focus: window
  0x1ff, revert to None`) - **but this server then reported every keystroke against the root window**,
  where no client listens, so the keys were dropped (`InputState.focusWindow` returned the focus window
  and only `PointerRoot` followed the pointer). Nothing told the app's window either. The 设置/关于
  dialogs "fix" it because they are *transient*: the client puts the focus back itself, one log line
  before the unmap. **A text field that looks focused, reacts to clicks, and accepts nothing.**
- **Measured, not reasoned**: new tool **`tools/KeyTargetProbe`** (parks the pointer in its own window,
  leaves the focus on the root - `SetInputFocus(root)` or the unmap revert - and prints the window each
  XTEST `KeyPress` is reported against). Six modes; **before: nothing reported for `root`/`unmap`/`kill`;
  after: identical to Xvfb line for line in all six** (`tools/probe-diff.py`). `InputFocusTest` +2 cases,
  **both seen to fail without the fix**.
- **The fix** is one condition: `if (inputFocus.isRoot())` in `InputState.focusWindow` - the two states
  stay two (GetInputFocus answers the id vs `1`, different focus-event chains) but the keys follow the
  pointer from either; only `None` leaves them nowhere. Comments that said the opposite were corrected
  in `InputState`, `InputHandlers` and `FocusProbe`, plus the design notes' omitted-things list and
  backlog A5's leftover bullet.
- **Verified end to end**: EasyShell on the VM, the gesture, `hello` typed into the search box with no
  dialog in between, arriving as `Xi2Event[evtype=2 device=3]` (XI2 KeyPress). **termora**, the bed's
  Swing/AWT app, receives core `KeyPress`/`KeyRelease` in the same state (focus left on the root).
- **Found and left OPEN as A18**: clicking termora's 密钥管理器 toolbar button throws
  `IllegalArgumentException: Nonexistent button 16` in its toolkit thread here, where the same click
  opens the dialog on Xvfb. **Not from this change** (the pre-fix build throws it too, same log line),
  and AWT's own logger shows its *reading* of the event to be the strange part (the serial and type byte
  are ours, every other field holds the previous event's values) while the tapped wire is three
  well-framed messages with `detail = 1`. The recipe - `tools/tap.py` plus
  `JAVA_TOOL_OPTIONS=-Djava.util.logging.config.file=…` with `sun.awt.X11.event.level=FINE` - is in
  `tools/README.md`.
  **RESOLVED 2026-10-03**: "every other field holds the *next* field's value" is libXi's device
  layout, because XInput advertised `first_event = 0` - see the session note at the top of this
  file and [the event-base trap](x11-extension-event-base-hijack.md).
- **Bed notes this round**: a `--single-window` server on `:95` plus `tools/kit.sh Drive/Shot/Keys`
  drives EasyShell exactly (the window lands at `+0+33`, so click at window coordinates **+33 in y** -
  clicking the visual position in the *screen* screenshot hits the root); `pkill -f '[m]ount_EasySh'`
  and `'[m]ount_termor'` for the clients; Xvfb via `tools/xvfb.sh start/stop`; **stop the `:95` server
  before `mvn test` or `GrabCrossingTest` fails with `BindException`** (it binds 95); and a background
  `nohup` script started from a one-shot shell does not always survive - run long A/B scripts with the
  harness's background mode.

**2026-09-28 (twenty-third round): F5 `DOUBLE-BUFFER`, F6 `XVideo`, F7 `SECURITY` - the rest of §9.**
`mvn test` **436/436** (was 416), headless **436 with its 2 skips, BUILD SUCCESS**, sweep **19 of 28
identical** with the same 9 documented differences, **not pushed**; iMessages sent and both beds stopped.

- **F5 Xdbe** (`DbeExtension`): the wire format is *not* the specification's - `AllocateBackBufferName`
  is window/buffer/swapAction with **no reply**, `SwapBuffers` names a *window* with (window, action)
  pairs, `GetBackBufferAttributes` is minor **7**, and `GetVisualInfo`'s reply is a per-drawable count
  then records. The swap rule with two buffers: the front takes the first-allocated buffer's contents
  and every buffer is then left holding what the action names. `Extension#windowResized` is new (the
  core ConfigureWindow path calls it) because a buffer follows its window.
- **F6 XVideo** (`XVideoExtension`): the reference answers **zero adaptors**, so this does too, with
  version 2.2, two events, three errors, `BadPort` for every port request **in the reference's order**
  (drawable and GC first) and `SelectVideoNotify` accepted. No scaler, deliberately.
- **F7 SECURITY** (`SecurityExtension`): a token is an ordinary `MIT-MAGIC-COOKIE-1` credential
  (**not** "SECURITY" or "XC-QUERY-SECURITY-1" - both refused by a reference), untrusted by default;
  the six `BadAccess` denials, the hidden-extension list (an untrusted client sees only BIG-REQUESTS
  and XC-MISC), `BadRequest` for a hidden opcode, and a revoke that closes the connections using it.
  `Extension#visibleToUntrustedClients` is the rule; `ConnectionSetup` checks a generated token even
  when `--auth` is off, which is what an `-ac` reference does.
- **Kit lessons this round**: macOS's `java` launcher hands the JVM the system SOCKS proxy, so
  `RawXClient` opens its socket with `Proxy.NO_PROXY`; a probe must not send `SetAccessControl` (it
  turns access control back on in an `-ac` Xvfb and breaks the next run); `tools/xvfb.sh secure`
  starts a second reference on `:97` with an authority file; and a probe prints what a base *means*
  ("idBase=actor") rather than the number, because a reused base destabilises the diff's id mapping.
- Live on `.147` for each item: EasyShell renders/types/keeps its context menu, termora renders, and
  the round's own clients - the Xlib `Xdbe` client, the Xlib `XSecurity` client (against a live server
  with an application connected) and the Xlib `Xvlib` client - all answered identically to the
  reference.

**2026-09-28 (twenty-second round): F2, `RECORD`.** `mvn test` **416/416** (was 407), headless
**416 with its 2 skips, BUILD SUCCESS**, sweep **17 of 25 identical** with the same 8 documented
differences, **not pushed**; iMessage sent and the servers/clients stopped.

- **F2 `RECORD` (new `RecordExtension` + the `ProtocolRecorder` hook)**: the eight requests, the one
  error and the stream. The scripture that is not guessable is in the class comment and the probe:
  specs come **first** on the wire and ranges after (the `GetContext` reply is the other way round), a
  range is 24 bytes with **six**-byte extension slots, `CurrentClients` resolves to each base and
  `FutureClients` to the literal 2, a datum's length counts the data after its 32 byte header,
  `ClientStarted` carries the **whole setup reply** (and it is not also a FromServer datum),
  `ClientDied` carries the client's **last sequence number**, consecutive `FromClient` items share one
  datum, and **no stream ends with EndOfData**.
- The hook is **`ClientOutput.setListener`** - one call covers replies, events, errors and every
  hand-encoded image - and it is attached *after* the setup reply is sent, which is what keeps
  ClientStarted from being preceded by a redundant datum.
- Two tests taught the traps: `RecordTest`'s MapNotify check must read **past the FromServerTime
  stamp** (a datum's first byte is a stamp, not the event type), and `IdleScreenMemoryTest` now
  measures the heap **after a collection** (`settledHeap()`), because the raw number carried the JVM's
  uncollected garbage and failed its 20 MB bound (20572 KB against a settled 2.5-3 MB).
- Live on `.147`: EasyShell renders, typing lands, its context menu survives the out-and-back, and a
  `RecordProbe` context over `AllClients` recorded **EasyShell's own traffic** (its setup reply and a
  910 KB request batch). termora renders. No refusals but the two known `window 0x0` probes.

**2026-09-28 (twenty-first round): the three small capability candidates - F1, F3, F4 - and the two
extension modules that were already in the dependency but unused.** `mvn test` **407/407** (was 393),
headless **407 with its 2 skips, BUILD SUCCESS**, sweep **16 of 23 identical** with the 8 documented
differences, **not pushed**.

- **F4 `Generic Event Extension` (new `XgeExtension`)**: advertised, `QueryVersion` answers 1.0 to a
  1.0 and a 0.0 client (measured). **No generated protocol module exists for XGE in the x11 library**,
  so the request decoder and the reply are hand-written. It exposed a **real defect**: `QueryExtension`
  reported the registry's next free code for `first_event`/`first_error` even for an extension that
  defines none - a reference answers **0** for both (measured on BIG-REQUESTS, XC-MISC, Composite,
  Present, XGE) and 64/0 for SHAPE, which defines one event. `ExtensionRegistry` now reports 0 for a
  range it did not allocate; nothing depended on the old numbers.
- **F3 `X-Resource` (new `ResExtension`)**: all six requests of 1.2, from the server's own resource
  table (the generated module `x11-protocol-res` was already a dependency). Measured with the new
  `tools/ResProbe`: version is the server's 1.2; `QueryClients` lists the **server's own resources as a
  client with base zero** first; the type atoms are interned **lazily** (the six core names read off a
  reference, extension types upper-cased from `typeName()`); an id no client owns is **BadValue**;
  `QueryClientIds` answers the XID bit with an **empty value list** (a reference's own answer) and the
  local-pid bit with nothing (all transport here is TCP, so no pid is knowable). **Two deliberate
  differences, both in `tools/README.md`**: a *window* holds bytes here (4/pixel, B5's store) where a
  reference says 0, and `QueryResourceBytes` **matches a named `type` against the resource's own atom**
  (the spec's rule) where a reference answers nothing for any non-zero type.
- **F1 SYNC alarms** - and **half the item was already done**: `PresentNotifyMSC` has been answered
  since B5, so §9's "refused today" was **stale** (`PresentProbe` now pins it: kind 1, the client's
  serial, identical on both). The alarms are new in `SyncExtension`: `CreateAlarm`/`ChangeAlarm`/
  `DestroyAlarm`/`QueryAlarm` plus the **`AlarmNotify` event** (a real extension event at
  `firstEvent + 1`, so **SYNC is now registered with 2 events**, not 0). **The trigger rules are
  measured, not recalled** (`SyncProbe`'s alarm section is identical to the reference line for line):
  a transition fires on the crossing and stays active; **after firing the threshold moves by the delta,
  added whatever the test is** (10→110→210 for +100; 10→-90 for -100); a comparison fires once and goes
  **inactive**; and **a transition whose delta would move its threshold the wrong way is refused
  `BadMatch` on its own id** (negative+positive delta, positive+negative delta). Unmeasured and marked
  as this server's reading in the class: `Relative` value type, `ChangeAlarm` re-arms without firing,
  no fire at creation. **Also fixed**: SYNC ids that are not the right resource now answer the
  extension's own `BadCounter`/`BadAlarm`/`BadFence` instead of a core `BadDrawable`.
- **Sweep additions**: `XgeProbe` (**identical** on both) and `ResProbe` (a documented difference) are
  in `tools/sweep.sh`; `probe-diff.py` now normalises `counter=`/`alarm=` (server-assigned ids printed
  in decimal) and `SyncProbe` names an extension error by its **offset from the error base**, so
  `BadAlarm` reads the same on two servers with different bases.
- **Live regression on `.147`** (server on the Mac, `:95 --single-window`, JavaFX window, `--log-requests`):
  **EasyShell renders, typing lands** (its search box showed the two characters typed), **its context
  menu opens and survives the out-and-back**, **termora renders**; each client's log has only the two
  known `window 0x0` refusals. **termora's menu gesture is not scriptable in its current profile
  state** - its host tree had no rows to right-click, so a hand is what settles it (the checklist's
  §2.12 now says so).
- **Still open**: F2 (RECORD), F5 (Xdbe), F6 (XVideo), F7 (SECURITY) - the remaining §9 candidates -
  plus C5's drag-out (parked), E2 (waiting on the GitHub mirror) and E6 (ongoing). **xdpyinfo now shows
  19 extensions against the reference's 23, 18 shared**.

**2026-09-28 (twentieth round): the capability inventory - what this server is missing *as an X
server*.** Commits `2976cca`, `f996397`, **not pushed**; `mvn test` **393/393**.

- **Measured against a reference, not recalled**: `xdpyinfo` on both sides shows the reference
  advertising **23** extensions and this server **17** (16 shared); the difference is seven the
  reference has and **DPMS is the one only here** (B7). And the *core* protocol is complete at the
  dispatch level - **all 120 xproto requests have a case**, all but two doing real work (`ChangeHosts`
  and `SetAccessControl` are deliberate no-ops: host-based access control is not enforced).
- **The seven absent extensions**: DOUBLE-BUFFER (Xdbe), Generic Event Extension (XGE), MIT-SHM
  (declined, B8), RECORD, SECURITY, X-Resource, XVideo. **The holes inside the ones we have**: SYNC's
  alarms, Present's `PresentNotifyMSC`, `ShapeGetRectangles`, XFixes' tail
  (`GetCursorImageAndName`, `ChangeSaveSet`), XI1 beyond the device list, and pointer acceleration
  (`ChangePointerControl` is accepted-and-ignored, `GetPointerControl` replies a constant). All of it is
  `docs/backlog.md` **§9**, with each candidate sized by what breaks without it (F1 MSC+alarms, F2
  RECORD, F3 X-Resource, F4 XGE, F5 Xdbe, F6 XVideo, F7 SECURITY) and a "not intended" list (DRI3/DRI2,
  VidMode, XDMCP, MIT-SHM, the dead protocols).
- **The honest half**: no client in this bed asks for any of them - measured from the request profiles
  (the A/B method and the thirty-tool client sweep), not assumed. These are capabilities for clients
  this project has not met yet.
- The verification checklist's §7 now points at §9, so a pass that hits an absent extension finds it as
  "not built yet" rather than as "decided".

**2026-09-27 (nineteenth round): GLX is on by default, the whole-server checklist exists, and the
proxy's framing is pinned.** `mvn test` **393/393**, commits `2a50319`, `07bda16`, `5c7d488`, `92aa398`,
`fc60986`, **not pushed**.

- **GLX is now advertised by default and `--no-glx` is the switch** - the owner's call, on the
  measurement that made it opt-in being closed (A17: the caret blinks either way, the hover asks for no
  repaint). What is left of the difference: a toolkit finds GLX, creates a context, finds no GPU that
  suits it and falls back to the same software pipeline - one abandoned context, and the only way a GL
  client renders here. Live: on a *default* server `glxinfo` says `direct rendering: Yes` and
  `glxgears` runs on llvmpipe. GlxTest now runs without the flag (so the class exercises the shipped
  default) and its default-pinning case is inverted.
- **`docs/verification-checklist.md` is new**: the whole-server tour - baseline, core and drawing,
  input, extensions, host window, transports, engineering - as "do this → with this tool → it passes
  when", plus the two sections that stop false alarms: what is deliberately different, and how a run
  lies (the ten measured traps). Linked from the README's Tests section and the backlog's opening.
- **`XProxyFramingTest`** pins the proxy's three framing rules, which were each a silent bug: a
  one-unit request is four bytes (not eight - `Enable`, sent on every connection), a zero length field
  means read the next word (and *not* "the opcode is 139"), and a generic event carries its length like
  a reply (76 bytes for an 11-unit event, not 32 - the EasyShell stall). Both mutations of the helpers
  are caught: 4 against 8, and 76 against 32. The arithmetic is two small functions now.
- **The §0 table was lying** (C5 still "both directions fail", E2 unwired, and D4/E6 had no row) - the
  table is what a session is planned from, so it was repaired with the entries.
- **Nothing else is open**: D4 is out of scope, MIT-SHM is deliberate, C8 declined, C5 parked (drag-out
  left as-is), E1 closed, E2 waiting on the GitHub mirror, E6 ongoing (and swept again this round).

**2026-09-27 (eighteenth round): C5's drag-in is OWNER-VERIFIED - it works, with a filesystem the
client can see.** Commit `32ec154`, **not pushed**.

- **The working configuration**: a host directory shared into the VM, and the server started with
  `--drop-mount /Users/oyzh/MacDrop=/mnt/hgfs/MacDrop` (any mount will do - the point is that the path
  in the URI exists where the application runs). A file dragged from *inside that directory* onto a
  forwarded EasyShell arrives and the application uses it. The owner's own words: **it works, but
  setting it up is a nuisance**.
- **What was offered and declined**: a server-side push (`--drop-push user@host:/dir`) that copies the
  dropped file to the client machine and hands over that path - so any file could be dragged from
  anywhere, with no mount. Declined because it **writes files silently onto another machine**; if ever
  added it must be opt-in. It copies to the machine *running the client*, not to the host the
  application's own session points at.
- **The other half is unchanged**: the drag *out* is still the reverted trigger.
- **The drag source lives in the tools package now** (`src/test/java/.../tools/HostDragSource.java`,
  run by `tools/kit.sh HostDragSource <file> [x] [y]`) - the owner chose that home over the single-file
  script under `tools/`, which is deleted. Commit `34eaef0`.
- **C5 is parked by the owner** (2026-09-27): drag-in works and is verified, and the drag-out trigger
  is deliberately left alone rather than carried on with. The entry says so, so the next session does
  not treat it as pending.
- **E2 is parked too, and its one unknown is gone**: Gitee is a **code host only** - the repository will
  be **mirrored to GitHub**, and CI will run on **GitHub Actions**. The missing file is a
  `.github/workflows/*.yml` whose single job runs `ci.sh`, to be written when the mirror exists. And
  **the no-display case is not the owner's own use** (this server is meant to be rendered by JavaFX on
  a desktop) - headless support stays as a *means* to CI, not a goal, and nothing about it changes.

**2026-09-27 (seventeenth round): E1's generator fix is written and verified, E2's last unmeasured claim
is measured, E6 swept.** `mvn test` **387/387**, commits `c5ebe64`, `d1648d8`, `f39c89d`, `120c935`,
**not pushed**.

- **E1 is CLOSED (2026-09-27) by the owner's decision, and the constraint is a standing one: the x11
  library is never forked, built or republished, and Lombok is never a dependency.** The rewrite in
  `ClientOutput` is therefore *the fix* (the seventh entry in the README's "Bugs worked around in the
  protocol library"), pinned by `EventTypeByteTest`. The fallback is a same-named-class override, whose
  two safety facts were checked (nothing else in the library constructs/references the five classes;
  this server never decodes them, so only `write`/`getNumber` matter) and whose price is ~450 lines
  written out without Lombok - recorded in `docs/fork-eventcopy-fix.md`, not built.
- **What that round had measured for the upstream fix** (kept in that file, useful if anyone ever
  builds the library): The root cause: the
  generator *reads* a copy's own number and then takes the copied event's **whole unit list** as its
  own - the first unit being the `NUMBER` field carrying the *ref's* constant - so five events go out
  with the wrong type byte (a release as a second press, a leave as an enter, a focus-out as a
  focus-in). Patch, reasoning and recipe are in **`docs/fork-eventcopy-fix.md`** (in the repository,
  because the fork is a plain directory and a fix there is not restorable). Verified: published
  locally, numbers read back 5/3/8/10/27 (was 4/2/7/9/26); with `EVENT_NUMBERS` **disabled** the new
  test is 5/5; with the *published* artifact back and the rewrite still off, **4 of 5 fail** - each
  with its symptom. (The rewrite and the upstream fix were measured compatible, so the order never
  mattered - and neither is pursued now, per the decision above.)
- **The new test** is `client/EventTypeByteTest`: written against the **wire** (drive the event, read
  the type byte) rather than against the classes, so it passes in both worlds and fails only when
  neither the fix nor the rewrite is there. Its two traps: keys need `SetInputFocus` first (buttons go
  to the pointer's window), and a `KeyRelease`'s byte 1 is the keycode, not zero.
- **The fork's build traps**: its Gradle wrapper cannot read this machine's default JDK 27 class files
  (`Unsupported class file major version 71`) - use `JAVA_HOME` = the JBR 21; and a local publish needs
  signing off (`--init-script` disabling `org.gradle.plugins.signing.Sign`).
- **E2**: `ci.sh` with `-Dmaven.repo.local` pointed at a directory that did not exist (a runner's
  empty repository - the one claim never tested here) resolves everything from Central and finishes
  **387 tests, 2 skips, BUILD SUCCESS**; it also resolved the *published* artifacts, which is E1's
  compatibility claim seen from the other side.
- **E2's pipeline, settled by the owner (2026-09-27)**: **Gitee is a code host only** - the repository
  is going to be **mirrored to GitHub**, and CI will run on **GitHub Actions**. The missing file is
  therefore a `.github/workflows/*.yml` whose one job runs `ci.sh` (the script is the whole interface;
  it needs a JDK and Maven), to be written when the mirror exists. **And the no-display case is not the
  owner's use** - this server is meant to be rendered by JavaFX on a desktop - so headless support is
  kept as a *means* to CI, not as a goal; nothing about it changes.
- **E6 sweep**: three stale claims fixed (the README's workaround list was missing the eventcopy one;
  the kit's note said a forwarded application "ignores" the drag, which this session disproved; the
  README's drag bullet). And **`tools/host-drag-source.java` is checked in** - the always-on-top
  JavaFX drag source that can drive a real platform drag for C5's host half, with the activation-click
  trap in its header.
- **Next**: the drag-out trigger (needs a hand), the GitHub Actions workflow once the mirror
exists, E6 ongoing.

**2026-09-27 (sixteenth round): C5's drag-in mystery is solved - the protocol works and the *file* is on
the wrong machine.** `mvn test` **382/382**, commits `aa6ac00`, `80d2afa`, **not pushed**.

- **The measurement, and it needed a controlled drag source**: a Finder-sourced drag cannot be driven
  from a script here (several stacked windows, and every new shell command raises the IDE over them),
  so `/tmp/HostDragSource.java` is a JavaFX window holding a real file with `setAlwaysOnTop(true)` -
  it cannot be covered, and a `CGEventPost` drag from it starts a **real platform drag**. One lesson
  inside that: **a synthetic click on a window whose app is not active is swallowed as the activation
  click** - three clicks before the drag made it work, and the JavaFX source then logs its own
  `drag started`.
- **What the log showed**: the drop reaches the server (`the host dropped 1 file(s) at ...`), the bridge
  sends `XdndEnter`/`XdndPosition`/`XdndDrop`, and **EasyShell answers** - an `InternAtom` burst (GDK
  resolving the type atoms), `SendEvent` twice (its `XdndStatus`), `ConvertSelection` six times asking
  for the data, which the bridge serves. **So the C5 entry's "the application never answers: no
  XdndStatus, and no ConvertSelection" is stale** and was corrected in the entry.
- **The real blocker**: what is handed over is a **host path** - `file:///private/tmp/...` - and XDND
  is URI-based (a target *opens* the path, it does not download a file). Across machines that path is
  nowhere, which is the whole of "nothing happens" **and why the macOS build of the same application
  shows its release marker** (there the URI resolves).
- **Two real defects fixed on this side**: `DropBridge.toUris` escaped only spaces (a `#` starts a
  fragment, a `%` an escape, and a non-ASCII name - the common case for this user - is not a byte a URI
  may carry raw), and the path carried macOS's resolved `/private` prefix, so even a client sharing
  `/tmp` could not open it. Both directions now escape/decode UTF-8 byte by byte (3 new tests).
- **`--drop-mount H=C`** (new, opt-in): with the client mounting the host's directory, the URI is
  rewritten to the client path, which is the one configuration where drag-in actually works. Longest
  prefix wins; unit-tested, fails without it.
- **Still open**: the drag-out trigger (still the reverted one), and whether the *applications* do
  something useful with a path they *can* open (that needs a shared folder and the owner's hands).

**2026-09-27 (fifteenth round): the same-client-two-servers comparison is a working method now, and
its first use is clean.** `mvn test` **379/379**, commits `47358ae`, `d097ae6`, **not pushed**.

- **The method**: same client through `XProxy` against a reference and directly against this server,
  the **same interaction driven by the same driver** (`xdotool` on the VM - using XTEST here for one
  side and xdotool for the other compares the *drivers*, a third of the traffic), profiles taken
  **after a mark** in each log (comparing one run's startup against another's interaction is the other
  way to get nonsense), compared by core request name and by **(extension, minor)**.
- **The result**: EasyShell agrees request for request - `QueryExtension` 70/70, `GetInputFocus`
  21/21, `GetProperty` 19/19, `GetKeyboardMapping` 18/18, `GetModifierMapping` 14/14, and the RENDER /
  XKEYBOARD / XFixes / XTEST / XInput sets within a couple. The only real difference is ~25% more
  *drawing* on this side (`PutImage` 408 vs 314) - this server's compositing against a server that
  draws to the screen, i.e. a performance note, not a protocol difference. (Not chased; E3 is closed.)
- **Two more `XProxy` faults, both of which made its own logs lie**: the core opcode name table was
  out by one through the input range (33 printed `GrabKeyboard` where it is `GrabKey`, 37 as
  `GrabServer` where it is `UngrabServer`) and stopped at 43; and extension requests carried no minor,
  which is the only label two servers' extension traffic can be compared by (they number the
  extensions differently). Both fixed - the protocol's own list is the table now, through 119
  (`GetModifierMapping`).
- **Watch for**: `Enable` in our log is `BIG-REQUESTS Enable` in the proxy's - the same request, two
  names, and it reads as a difference in a diff.

**2026-09-27 (fourteenth round): the owner's hands disproved my drag-in claim, and the hunt for
something else found a months-old tool bug.** `mvn test` **379/379**, commits `61aaf97`, `4f9f76d`,
`d097ae6` on top of the thirteenth round's, **not pushed**.

- **Correction to the thirteenth round**: a Finder drag onto a forwarded **EasyShell** still does
  nothing (the macOS build of the same app shows its release marker), so C5 is *not* done in either
  direction - my "drag-in verified" was measured against a GTK script, and a target that accepts is
  not the application that has to. The backlog entry says so at the top now, with the next step
  (GTK target and EasyShell side by side on this server, compare what each is sent).
- **`XProxy` truncated every GenericEvent** (type 35: it carries its own length) to 32 bytes - and
  GDK's input path is XI2, so a forwarded application desynchronised at its **first device event** and
  stalled before drawing. **That is the "EasyShell hangs through XProxy, the reason was never found"
  the kit had given up on.** With type 35 framed like a reply, EasyShell renders through the proxy on
  Xvfb, complete; all eight of its connection streams check out against their own length fields.
  Two smaller framing bugs went with it (an eight byte head assumed for every request - `Enable` is
  four; an extended length triggered by opcode 139 rather than a zero length field).
- **The proxy's lines now carry a connection id**, and that is not cosmetic: a client opens several X
  connections into one file, and a reader that assumes one stream sees a misframe that is not there
  (an hour of this session went to exactly that).
- **The A/B it exists for now works, and the first run is clean**: EasyShell's request profile against
  the reference and against this server line up request for request; the three differences are the
  *reference's* environment (GetAtomName for `X11VNC_TICKER`/root properties x11vnc leaves, two
  CreateColormaps for Xvfb's extra visuals, a GetMotionEvents probe not made here).
- **Two sweeps, both recorded**: thirty real client tools plus a GTK app and nautilus -> **no X error
  from any of them**; the probe sweep is 15 identical with the same seven documented differences. And
  the two `bad window 0x0` refusals in every log are the **client's** - a GTK client makes the same
  two requests against Xvfb, so a reference refuses them too and GDK ignores both.
- **Still open**: C5 both directions (needs the corrected drag-out trigger *and* the owner's hands),
  E1 (the fork), E2 (the CI host), E6 (ongoing). The next session's cheapest real win is probably the
  side-by-side drag comparison above, with the proxy finally able to show what a client is told.

**2026-09-27 (thirteenth round): the XI2 property *family* (both reads) is answered, and C5's drag-in
is measured with a real toolkit.** `mvn test` **379/379**, commits `543d6e2` and `544672c`, **not
pushed**.

- **`XIListProperties` (minor 56) was refused like `XIGetProperty` was**, and `xinput list-props 2`
  said so (BadRequest against us, a device and two properties against Xvfb). Answered now with an
  empty list; the reply layout came off a reference's reply for a device that *has* properties
  (`XiProbe listprops 2`, new): count is a CARD16 at 8, atoms follow the header, byte 1 is the minor
  opcode. **A reference refuses the pseudo-devices (0/1) for property requests** (`XiProbe listprops
  0` answers an error), so both reads do now too - `checkDevice` is right for the queries and wrong
  here.
- **C5's drag-IN works with a real GTK3 drop target** (`tools/xdnd-source.py` / `tools/xdnd-target.py`,
  new and checked in): a real Finder drag onto this server ends with the target printing
  `DATA ['file:///…/dropped.txt']`. The bridge's conversation is one a real toolkit accepts.
- **Four harness facts, each of which looks like something else** (all in the C5 entry): a drag
  between two windows of **one** GTK process never uses XDND (GDK shortcuts it - the capture has no
  `XdndEnter` in it); `host-drag.c` is too abrupt to deliver a *drop* (needs ~700 ms before the
  button-up); `tap.py` logs byte **chunks**, so a reader has to frame the stream (a C2S setup is 12
  bytes **plus auth**, padded); and **any new shell command raises the IDE over Finder**, so a
  Finder-sourced drag only works inside a single command that re-raises it - and even then it is not
  reliable, so the two-process GTK pair is the scriptable oracle.
- **`XProxy` was broken for every xcb client** and that is the mystery the kit's notes had given up
  on: it assumed an eight byte head, `BIG-REQUESTS`' `Enable` is four, so it desynced one message in
  and the client hung with nothing logged. Fixed (four byte head; extended length triggered by a
  **zero length field**, not by opcode 139; a server's messages always have the full header). It
  still cannot proxy VM→reference, so compile it **on the VM** (`java` is there).
- **Still open**: C5's drag-out trigger (the corrected condition is "start the platform drag only
  when the gesture leaves the host window with the button down") needs implementing *and* a hand to
  confirm the applications' own internal drags and menus survive - and the two apps' answer to a
  drag-in was not reproduced either way this session, so it is not settled. The rest is E1 (the fork),
  E2 (the CI host) and E6 (ongoing).

**2026-09-27 (twelfth round): B1 is DONE, and an XI2 device property read is answered instead of
refused.** `mvn test` **378/378**, commits `7fa7b13`, `4493df3`, `ad1cb89`, **not pushed**. Both
forwarded applications were re-run on `.147` and both logs are clean.

- **`setxkbmap -layout <name>` exits 0 now, and the missing half was the *reply*, not an upload.** The
  entry had two beliefs and `strace -e writev` on the VM killed both: a real `setxkbmap` sends **no
  `SetMap`** (the XKB handshake, one `GetKbdByName`, then a root property update), and a reference
  **answers** that request - the `BadAccess` an earlier session recorded was a hand-built probe of the
  wrong length. Simpler than a proxy for reading a client's own stream: `strace -e writev -x -s 400`.
- **What Xlib does with the answer is the whole fix**: `XkbGetKeyboardByName` gives up when `reported`
  is zero and otherwise reads **one whole component reply per bit set in it** (map for `Types|Symbols`,
  then compat, names, geometry) and insists the buffer was consumed exactly. `reported = 0` with
  `loaded = 1` is therefore indistinguishable from "not loaded". The reply now embeds the `GetMap`
  reply whole, under a header whose length counts it, and claims 0x0d.
- **The request's shape was wrong here too and harmlessly so**: twelve fixed bytes and **six** counted
  strings with the symbols string **fifth** - the reader assumed ten bytes and eight strings, and the
  two byte shift happened to make it read the right string out of the wrong field. A request with eight
  strings is refused with `BadLength`, which is how the count was measured.
- **`GetNames` is answered** (refusing it is the worst XKB answer: Xlib's default handler kills the
  client). Its atoms are in the reply's **tail**, not the fixed part the field list suggests; what this
  server reports is the symbols component a by-name load recorded, and `which = 0` before any has.
- **XI2 `XIGetProperty` (minor 59)**: both apps ask it as they start (`libinput Tapping Enabled` on
  device 2) and it used to be a `BadRequest` from the decoder's throw. Now answered "absent" - a
  reference's reply for a property that does not exist (`type = None`, everything else zero). Measured,
  not reasoned: the request is **six** four byte units (seven gets `BadLength`), and the reply's byte 1
  carries the request's minor opcode (`XiProbe getprop` is how both were read).
- **THE TRAP OF THIS ROUND, and it cost an afternoon of believing the change broke EasyShell's menus.**
  The out-and-back gesture closed the menu on the new build 2/2 and survived on the old one, which read
  as a regression in a change that touches no part of the input path. It was the *runs*: with a fresh
  server **and** a fresh client **and** the pointer parked before anything maps, the same build is
  **3/3 survived**. A real application driven through XTEST needs all three controlled, not just the
  pointer - `tools/README.md` rule 5 now says so. (Sanity check that settled it: the protocol-level gate
  `MenuProbe` against Xvfb was identical throughout.)
- **Measured, for the record**: all seven layouts `xmodmap -pke`-identical to Xvfb through real
  `setxkbmap`; `xkbcomp`/`xset q`/`xset r rate`/`xdotool` still fine with no `rejected` line;
  EasyShell renders, its context menu opens at Xvfb's own geometry (157x378+75+132) and survives 3/3,
  typing after `setxkbmap -layout de` puts `zy` in its search box; termora renders and its menu
  survives 3/3. Both logs show only the two known window-0x0 asks.
- **Kit**: `XkbProbe getkbdname|getnames`, `XiProbe getprop`, `XProxy`'s raw dump argument. Note
  `tools/tap.py` is the byte-transparent proxy to use when a client hangs through `XProxy` (EasyShell
  does, after its first `QueryExtension`) - and the `XProxy` raw dump only ever worked for
  Mac-originated clients; the VM-to-reference direction is what `tap.py` exists for.

**2026-09-27 (eighth round): B5 is DONE - Composite, Present and SYNC are in, and the backlog has no
extension item left.** `mvn test` **369/369**, commits `98cb61c`, `7dbfc58`, `f36f61d`, `910d324`,
**not pushed**. Sweep **15 of 21 identical** with the seven documented differences.

- **The store is what made them cheap.** Every window already draws into pixels of its own and is
  composited from them, so redirection is the *stopping* of that compositing - `XServer.compositeOwnPixels`
  is the single place every screen composite goes through, and the guard there is three lines.
  `NameWindowPixmap` hands over `XPixmap.view` (a live view of the store, resolved per access because a
  resize replaces the array). The reference says drawing *into* a named window pixmap lands, so the view
  is read-write.
- **The trap: this server is right by default in every test that never redirects anything.** With every
  window composited from a store, forgetting the guard changes nothing any test could see. The reference
  is what says otherwise - repaint a redirected window there and its pixmap holds the new colour while
  the screen holds the old one.
- **The overlay window is the one part that could not be inherited** - and **the design it was first
  given here was wrong in all three respects**, which the ninth round below records: it was made the
  depth 32 visual with a transparent background and a rule that a translucent window does not obscure.
  The right answer is the root's own depth and visual, no background at all, and an exemption from
  redirection. That first version also introduced and then removed a change to
  `XWindow.visibleRegion`'s obscuring rule that nothing needed - do not re-add it.
- **Four rules came out of measuring, three of them not what the obvious implementation does.**
  (1) The version rule is **per-extension and they disagree**: Composite and SYNC answer *the server's*
  (0.4 to a 0.4 and to a 0.2 client; 3.1 to 3.1 and 3.0), Present **echoes the client's** (1.0 to 1.0).
  (2) **`CreateFence` carries the drawable before the fence** - the opposite of the generated getters'
  order, and the reference answers `BadDrawable` on the fence's own id if you believe the getters.
  (3) **`PresentPixmap`'s `valid`/`update` are region ids, not flags** (None = the whole of each).
  (4) **A generic event's length field was never written** - Present's completion arrived as 32 bytes
  where the reference sends 40, *with the same kind, mode and serial inside*, so every field read
  correctly and the client then hung on the next request (the 8 bytes of `ust`/`msc` stayed in the
  stream). Fixed in `ClientOutput` beside `correctEventType`.
- **And one measurement was wrong first, worth not repeating**: reading the window's pixels *before*
  the completion notification makes the reference look as though it never copies the pixmap. Its
  present is **asynchronous**, so that samples a race. Read the event first and the two agree exactly.
- **`PresentPixmap` is decoded by hand** (`PresentExtension.decodeRequest`): the generated reader sizes
  its trailing notifies list from `length - javaStart` (four-byte units against a byte offset) and
  refuses every present with no extra notifies. Same fault as `VariableLengthDecoders`' sixteen.
- **`RegionResource` moved out of XFixes into `resource/`** - both XFixes and Composite traffic in
  regions and a region made by one has to be usable by the other.
- **`SyncProbe`'s system counters are the seventh deliberate sweep difference**: this server answers
  none where the reference keeps eight, whose entries are variable length with a name stride the first
  probe mis-read. Everything else in SYNC agrees line for line.
- **Test-bed note**: `tools/kit.sh` runs the probes; the new ones are `CompositeProbe`, `SyncProbe`,
  `PresentProbe`, all three in `tools/sweep.sh`. **`PresentProbe` is the one probe that serialises with
  the generated protocol classes rather than hand-built bytes** - a dozen fields, several 64 bit - and
  that is safe because the comparison is against a reference that refuses what it cannot read.

**2026-09-27 (eleventh round): B1's "layout by name" is in - off tables captured from a reference,
and the item's framing was wrong.** `mvn test` **375/375**, commit `31c626d`, **not pushed**. Sweep
unchanged at 15 of 21 with the seven documented differences.

- **"Choosing a layout by name needs a compiler in the server" was the wrong framing**: a display
  server compiles nothing. `tools/GenLayouts` + `tools/gen-layouts.sh` read the layouts off a reference
  (the D3 trick), and `GetKbdByName` serves them by name. Seven: us, gb, de, fr, es, ru, cn.
- **What a client asks with is not the word "de"**: `setxkbmap` sends the components the rules file
  resolved to (`pc+de+inet(pc105)`), as one counted string in a list. **Which one was measured** by
  varying it (`-variant nodeadkeys` changes only the sixth) - never read out of a spec the fork has no
  class for. The layout is the first `+`-part this server has a table for; a variant's parentheses are
  dropped.
- **The tables are the *core* mapping at the reference's own width (7 keysyms per keycode)** - the
  client does the level arithmetic, so the server never has to understand a level beyond the second.
  This server's own keymap stays two levels until a layout is installed.
- **Verified byte for byte**: after a layout is installed, `GetKeyboardMapping`'s whole reply (248
  keycodes x 7 levels) is identical to Xvfb's in the same layout, for `de` and for `ru`.
  `XkbTest`'s new case fails with the install taken out. **`Keymap` is process-global** - the test
  restores the built-in layout, and a real server keeps a client's choice for its lifetime (a keyboard
  is one keyboard).
- **`setxkbmap` still reports `Error loading new keyboard description`, and that corrected the entry:**
  **a reference refuses this request too** (Xvfb answers `BadAccess` to the very bytes a real
  `setxkbmap -layout ru` sends), so what sets a layout on a reference is the client compiling the
  keymap and uploading it with `SetMap`. **The entry's claim that a client "can go on to SetMap" is
  measurably false - no request at all follows the reply.** The missing half is the *upload*
  (`SetMap` + `SetCompatMap`/`SetNames`/`SetGeometry`), which is where the next session should look.
- **Traps**: a JVM method is capped at 64KB of bytecode (all SEVEN tables in one initialiser is past
  it - one method per layout), and **a trailing comma is legal in an array initialiser and not in a
  method's argument list**. Also `mvn test-compile` cannot run while a *main* source is broken, so
  changing a generator that produces a main source needs a valid stub in place first.

**2026-09-27 (tenth round): C5's drag-out was built, verified with a synthetic client, and then
REVERTED the same day because the owner tried it by hand and it broke both apps.** The X half is in
and inert; the trigger is gone. **Neither direction works with a real application yet.**

- **What broke, and why it is a design fault not a bug**: the host window started a platform drag
  whenever **any** client owned `XdndSelection` - and a client dragging **inside itself** takes the
  same selection (X's DnD is client to client; an internal drag is a drag between two windows of one
  application). So the platform took over drags the application meant to keep: **termora froze** (it
  waited for a drop the platform had hijacked) and **EasyShell did nothing**.
- **The corrected condition**: start the platform drag only when the pointer **leaves the host window
  with the button still down**. macOS keeps delivering dragged events to the view that got the press,
  so the session can be started from the event that crosses the edge. That is the next attempt.
- **The other direction stops short too, measured for the first time**: EasyShell's window publishes
  `XdndAware` v5, the bridge finds it and sends `XdndEnter`/`XdndPosition`/`XdndDrop`, the app's own
  UI reacts (it renames its window to `===松开鼠标以释放文件`) — and then **it never answers**: no
  `XdndStatus`, no `ConvertSelection`, so nothing is uploaded. What a real toolkit wants that we do
  not provide is **not yet known**.
- **THE LESSON, and it is the same as B5's Verify's: a synthetic client is not a client.**
  `DropBridgeTest`'s target is this kit's own client, so the bridge agreed with it by construction,
  and the drag-out "verification" was a probe the bridge itself had asked to answer. Both directions
  need a real toolkit before either is called done. **Ask the owner to try gestures by hand before
  believing a new interaction works.**
- **Still true and worth keeping**: X *does* announce a drag (the `XdndSelection`), a JavaFX window
  *can* start a platform drag (`setOnDragDetected` + `startDragAndDrop`), and the whole drag-out path
  *is* scriptable (`DragOutProbe` + `/tmp/drag` + a Finder window on a scratch directory - Finder
  copied both files). The mistake was the trigger's condition, nothing else.

**The original tenth-round note, kept for what it built:**

**2026-09-27 (tenth round): C5's other half - the reason it was left open was wrong twice.** `mvn test` **374/374**, commits `09e9d07` and `f88e311`, **not pushed**. Sweep unchanged at
**15 of 21 identical** with the seven documented differences.

- **X *does* say when a drag has started**: a client that decides to drag **takes the
  `XdndSelection`**. That is the whole of the design - `DropBridge` watches it (via
  `XServer.setSelectionOwner`, the same hook the clipboard bridge uses), asks the owner for
  `text/uri-list` **the moment it sees it**, and a backend starts a platform drag from its
  drag-detected handler when `InputSink.draggingOut()` says so.
- **Asking on sight is the timing argument**: the host's drag must start from the gesture already in
  progress and JavaFX's dragboard holds **real files, not a promise**, so the answer has to be in
  hand by then - and a client answers in the time it takes the user to move a few pixels.
- **The entry's two reasons were wrong**: "a JavaFX window cannot start a platform drag" (it can,
  `setOnDragDetected` + `startDragAndDrop`), and "the payload cannot be scripted" - true of dragging
  *from* the desktop *into* an app, never of dragging out, where the payload comes from the X client.
  **The two directions were conflated in the backlog for three days.**
- **Verified end to end with a script**, which the entry said could not be done: `tools/DragOutProbe`
  stands in for the application, `/tmp/drag` posts a hand's gesture in the host window, and a Finder
  window opened on a scratch directory **received both files with their contents intact**. The
  server's log names each step (`a forwarded application is dragging from 0x…`, `the dragging client
  offers 2 file(s)`, `a drag of 2 file(s) left a forwarded application`).
- **The probe trap that cost its first version**: it did `geometry()` after `setSelectionOwner` to
  sync, and a reply read **skips events** - so it ate the SelectionRequest it existed to answer. The
  server asks while *processing* SetSelectionOwner. **A probe never round-trips after the request it
  is waiting for.**
- **A scene-level drag-detected handler had to be checked against the two clients** (their menus are
  press-and-move gestures): EasyShell's menu still survives 6/6 and typing still lands, termora's 6/6.
- **Still unverified**: a *real* application offering a real drag - the probe decides to drag, whether
  EasyShell or termora do is up to them.

**2026-09-27 (ninth round): B5's own Verify was run - `picom` against this server - and it found
three bugs the probes and the whole suite had missed.** `mvn test` **371/371**, commits `c077584` and
`dbfa0b7`, **not pushed**. Sweep back to **15 of 21 identical** with the seven documented differences.

- **The lesson: run the Verify step the item names.** All three are invisible to `CompositeProbe` as
  it was and to every test, and the first is a **segfault**.
- 1. **The overlay was depth 32 where the reference's is 24** -> `picom` creates a RENDER picture on
  it with the root's format -> `BadMatch` -> it keeps the null picture and dereferences it. Found by
  making `CompositeProbe` print the overlay's depth.
- 2. **The overlay was itself redirected** - a compositor redirects the root's whole subtree including
  the window it draws on; on a reference the driver shows the overlay by its own path, here being
  composited is its only path to the screen. Blank screen, picom running happily.
- 3. **The overlay painted its background over the screen** (this server approximates
  `background = None` by painting the desktop colour - harmless for an ordinary window, wrong for a
  screen-sized one). Fixed by giving the server's own surface no pixels until something draws into it.
- Each fix has a test that fails without it, **checked by mutation rather than by passing** - and the
  third test's first version passed with the whole fix removed and was a restatement, which is the
  trap to watch for. A mutation that does *not* fail the test is the signal.
- **The two findings it left open were one bug, fixed the same day** (`3dc1f3e`, `8954afe`): **a
  RENDER write into a depth 8 picture stored the whole ARGB value where the drawable's store holds a
  byte**. `Picture.writePixel` put `0x80000000` into a 1x1 A8 mask; the read side decodes such a store
  *through the format*, whose mask looks at the low byte, so the mask read back as **zero coverage** -
  every masked composite wrote nothing (black frames) and `--opacity-rule` did nothing. Fixed by
  `Picture.storedForm` (the write half of a convention `AlphaMaskTest` already pinned for reads).
  **What found it was adding the pixel counts to the composite's debug line**: `-> wrote 0 pixels,
  23711 blocked`, where the blocked ones were a clip and the rest were the empty mask. The request is
  *accepted* either way - nothing else in any log would have said so.
- **The 1x1-mask/repeat lead above was a red herring**: `picom` *does* set `repeat=1` (RepeatNormal)
  on its 1x1 alpha pictures, one call before using them (the `SetPicture repeat=` log line added for
  this is kept). The mask was empty for a different reason.
- **A flaky measurement nearly sent me the wrong way twice**: I concluded the overlay-background fix
  broke picom from one black sample, reverted it, then found picom black without it too. Sampling
  three times settled it. **Sample before concluding.**
- **`git checkout --` is dangerous mid-item**: it reverted the *uncommitted* redirect-exemption fix
  along with the change I meant to drop. Back up with `cp` to /tmp before reverting.
- **The trap that bit three times**: `pkill -f <pattern>` kills the ssh command running it when the
  pattern is anywhere on that command line - **including as an argument** (`sh /tmp/x.mount.sh` is
  fine, `sh xeyes-start.sh 192.168.22.1:61 xeyes` is not). Bake the command into a neutrally-named
  script and pass no application name on the line.
- **picom 10.2 and gdb are now installed on .147**; the launchers used live in `/tmp` on the VM
  (`xs-client.sh`, `xs-comp.sh`, `xs-at.sh`) and go through `tools/vm.sh`.

**2026-09-27 (seventh round): A17 is CLOSED by measurement - it is not a server defect.** `mvn test`
**362/362**, commits `75d04e6` (test) and `639b414` (docs), **not pushed**.

- **The symptom was the owner's**: with GLX advertised, a context menu item with no hover colour and a
  caret that never blinks, while clicks and typing worked. The entry said "not reproduced"; this round
  answered it from the side the first attempt could not - **what the client asks the server for**.
- **The caret blinks on both builds.** Ten whole-screen `xwd` captures 0.2 s apart, the search box
  clicked and the pointer parked away, hashed over the box's own rectangle: exactly two alternating
  states (frames 1,3,6,7,8,10 vs 2,4,5,9 default; 1,3,6,7,10 vs 2,4,5,9 with `--glx`), period
  0.8-1.0 s = JavaFX's 500 ms on/off.
- **The hover paints nothing because the client draws nothing.** With the menu open and the pointer
  walked across its items in 1 px steps, the server's request log shows the menu drawn **once**
  (`CreatePixmap`/`CreatePicture`/`FillRectangles`/`PutImage`/`CopyArea`) and then **no drawing request
  at all**; the log's `WarpPointer`/`GetInputFocus`/`QueryPointer` groups are **`xdotool`'s own**, which
  is what makes the two separable. The screen agrees: 12 pointer positions, menu rectangle
  byte-identical. On Xvfb the same gesture is also 0 pixels - but that half is weak evidence, because
  EasyShell's saved state cannot be reset from a script and two attempts to open the menu on the
  reference landed in an already-open session's file list.
- **No code change.** The mitigation stands (GLX only under `--glx`). What the item left behind is
  `RawXClient.copyArea` + `WindowStoreTest.aWindowTakesACopyFromAPixmapIntoItsOwnPixels`, pinning the
  caret's last request - a `CopyArea` out of a pixmap into a store window, which **nothing in the kit
  had ever sent**. Verified by mutation, not by passing: giving the store target the absolute origin
  fails it (and `aMovedWindowKeepsItsPixels`).
- **E6 swept**: `A5old`/`B3old` were still `[ ]` while superseded, and §0's table had a duplicate
  unticked `A6` row - both fixed. Remaining open items are unchanged: C5's drag-out, B1's layout by
  name, E2's pipeline (the remote is **Gitee**, and `ci.sh` deliberately writes no blind YAML), B5's
  Composite/Present/SYNC, E1 (the fork is not a git checkout), E6 (ongoing).
- **The reference sweep is clean**: 13 identical, the 6 deliberate differences, no finding.
- **Test-bed traps that cost time this round**: `tools/vm.sh` uses `ssh -n`, so **a piped stdin is
  discarded** - a script pushed that way arrives as an empty file; put it on the command line as
  `printf '%s' '<base64>' | base64 -d`. And **`pkill -f <pattern>` still kills the remote shell when
  the pattern (or a nearby `echo`) is anywhere on its own command line** - `pkill -9 -f mount_EasySh`
  did it again; use `tools/xvfb.sh stop` and bracketed patterns.
- **The caret measurement needs no PIL and no new tooling**: the VM's `xwd` output is refused by PIL,
  but its header is fixed (25 CARD32, then `header_size + ncolors*12` to the pixels), so a 15-line
  pure-Python reader hashes a region per frame. `tools/kit.sh Xwd diff` is the other route.

**2026-09-27 (third round, same day): C8 was built a second time and then DECLINED by the owner.**

- **The requirement, in the owner's words (2026-09-27)**: "我不需要每个窗口都对应宿主机的一个窗口，而是
  一个窗口渲染一个应用即可" - **one host window renders one application**. An application is one window on
  the host's desktop, whatever it opens inside itself; a dialog belongs inside that window. They used the
  per-window build and found it wrong. **This is a product decision, not a technical blocker: do not
  build C8 a third time.** It is recorded in `docs/backlog.md` C8 as declined.
- **What was built and reverted the same day** (`6cccef6`, `6cb04e6`, then reverted by `d1b1b2a`): a host
  window per mapped top level, each showing its window's own **subtree composite** (`draw/WindowSubtree`)
  built from B5's stores - the pixel problem that had it reverted on 2026-09-26 was gone by then, and it
  worked: `mvn test` 361/361 and live on .147 the settings dialog opened in its own host window while the
  parent's stayed its own size. All of that is **out of the tree**; the tree is back to one host window
  per screen with the union viewport (`contentBounds`), which is the behaviour the owner wants.
- **What was seen while it was up, in case a future session is tempted**: the frames get the platform's
  default position, so a dialog's host window opened exactly on top of its parent's (both at `95,174`);
  and every window an application maps starts its own arrangement (position, title, raise, idle) from
  scratch.
- **Live-driving a per-client-mode server needs the platform's own input** (XTEST is scoped to the
  sender's screen): `tools/host-windows.c` (clang + ApplicationServices) lists on-screen macOS windows
  with title and bounds, and a 6-line `CGEventCreateMouseEvent` click tool drives the host window. The
  mapping that worked for EasyShell was global = client + (95, 169) with the window at (95,174).
- **Test doubles implementing `DisplayBackend`**: the interface's `start`/`present` take a `Screen`, so a
  double whose signature drifts compiles as an unused overload and the test reads as "the backend's
  input is null" - ten of them were caught by that when C8's `Surface` was in and out again.

**2026-09-27 (fourth round, same day): the reference sweep is now one command, and the server came out
clean.** `tools/sweep.sh` runs every probe against Xvfb and against this server and diffs the two;
**13 of the 18 are identical**, and the five that differ are deliberate and listed in
`tools/README.md` under "What the sweep is expected to differ on" (`ChangeCursorProbe`, `XiProbe`,
`RandrProbe`, `XineramaProbe`, `ScreenSaverProbe`). A sixth difference is a finding. `mvn test` 357/357.

- **Two things had to be fixed before the sweep could be read**: `probe-diff.py` normalised only `win=`
  ids (so `drawable=`/`damage=`/prose ids counted as differences), and `ChangeCursorProbe` *died* on
  this server's `BadImplementation` for `ChangeCursorByName` - a refused request is answered with an
  error before the reply to the round trip that follows, so the probe now prints the refusal and
  consumes the queued reply.
- **Two ways a run lies**: the reference must be clean (`tools/xvfb.sh stop`/`start` - a client killed
  with windows mapped leaves them on Xvfb, and `MenuProbe` then differs in its first crossing's detail
  for that reason alone), and this server has to be started `--no-clipboard --no-tray` (the clipboard
  bridge taking `CLIPBOARD` reads as an extra `SelectionNotify`).
- **The pixel comparison was checked too**, termora against Xvfb through `xwd` on the VM: the windows
  are visually identical, and every counted difference is one of the three documented ones - the root
  colour (#E8E8E8 against black, which is most of them by count), 1-unit blend rounding along the
  window's top and bottom edges ("ours rounds, Xorg truncates"), and a few hundred antialiased glyph
  pixels in the logo (deltas to 53, invisible).
- **Remaining backlog after this**: B6 (GLX, XL), B5's Composite/Present/SYNC (each needs its own
  session, and their payoff is not in either client), E1 (blocked on the protocol fork's ownership),
  E2 (the CI pipeline is the repository host's). The docs' own assessment is that none of them is a
  session's work, which is why this round went to the kit.

**2026-09-27 (fifth round): B6 (GLX) reconnaissance - the item is much cheaper than it looked, and
nothing was advertised.** `mvn test` 357/357, one commit (`e2efbf0`).

- **The finding**: tapping Xvfb while `glxgears` ran showed the client sends **no GL commands** - it
  renders in-process with Mesa's software `drisw` path and blits frames as ordinary **`PutImage`**
  (769 MB in 5.6 s, one `GetGeometry` per frame). The reference has no DRI3/Present either. So a server
  needs **no GL implementation** for software clients, only GLX's query and context surface (nine
  requests) - and then the frames arrive as requests this server already answers.
- **`tools/GlxProbe`** is checked in and in `tools/sweep.sh` (where it is the one difference that is a
  whole missing feature). GLX opcodes are Mesa's, from `/usr/include/GL/glxproto.h` on the VM: from 18
  up they are QueryExtensionsString, QueryServerString, ClientInfo, GetFBConfigs (one apart from the
  obvious reading); the two string requests carry **no string**; `MakeContextCurrent` has a leading
  `oldContextTag`. `mesa-utils` and `libgl-dev` are now installed on the VM.
- **The one trick**: a server must grant a **direct** context, or the client sends GL commands over the
  wire instead of rendering locally.
- **The first thing to measure next**: `GetVisualConfigs`' **40-int-per-visual** layout (the GLX 1.2
  spec has 18, `__GLXvisualConfig` in `glxint.h` has 30, and neither is it) - pin it by correlating
  Xvfb's reply with `glxinfo`'s visual list, never by guessing.
- **Nothing is advertised**: the item's own trap (a client that sees GLX tries it instead of falling
  back) and the client at risk is EasyShell, which renders correctly today through JavaFX's software
  pipeline. The entry has the order the rest should be done in.

**2026-09-27 (sixth round): B6 (GLX) is DONE.** `mvn test` 360/360, commits `20c040c` and `e17e11a`.

- **`extension/GlxExtension`**: the handshake a software GL client needs. **A direct context is granted
  and an indirect one is refused** - that is the whole design, because a client told "direct" renders
  in its own process and pushes frames as `PutImage`, and one told "indirect" sends GL commands this
  server cannot execute.
- **Verified on .147** with the item's own tools (`mesa-utils` is installed there): `glxinfo` says
  `direct rendering: Yes`, vendor `xserver`, 1.4; **`glxgears` runs at ~190 FPS and the gears are
  drawn by OpenGL** (a `GetImage` shows them). **(Superseded below by the measurement that made it
  opt-in: EasyShell is *not* unaffected - it creates a GL context before falling back.)** The item's
  trap ("do not advertise before it works") is kept by advertising only the core.
- **Wire traps, each recorded in the code and docs**: the decoder must skip `minor`+`length` before the
  body (three bytes early is invisible when the body is ignored); the two string requests carry **no
  string**; their reply's length **counts the NUL** (one short and `glxinfo` prints memory after the
  string); `GetVisualConfigs` is **40 ints per visual** (18 fields + 11 token/value pairs), pinned by
  correlating Xvfb's reply with `glxinfo`'s table - never guessed. `GlxTest` pins all of it and fails
  without the fixes.
- `tools/GlxProbe` is in the kit and the sweep; the sweep is 13/18 identical with the six differences
  all deliberate.
- **GLX is off unless `--glx` (2026-09-27, the same day).** The user ran the GLX build and reported two
  things EasyShell animates no longer updating - a menu item with no hover colour and a caret that does
  not blink, while clicks and typing worked. Measured: **the caret does blink here** (a whole-screen
  capture 0.3 s apart differs by 40 pixels once the box is focused and settled) and neither symptom
  reproduces deterministically from a script; both are the kind a toolkit **gates on the window being
  active**. The one measured difference is the advertisement itself - with GLX the client takes a
  different drawing path - which is why the default is now off (the pre-B6 behaviour) and `--glx` is
  the user's switch. Recorded as **A17** with the numbers, the requests a caret blink makes
  (`FillRectangles` RENDER 26 into a pixmap, then a whole-window `CopyArea`), and "focus first" as the
  order to look in.
- **The mechanism, in JavaFX's own words** (`JAVA_TOOL_OPTIONS=-Dprism.verbose=true` on the VM client -
  the only way to see *why* a toolkit chooses a path): without GLX it stops at `ES2 Prism: Error - GLX
  extension is not supported` and goes straight to its software pipeline; with GLX it initialises
  `X11GLFactory`, **creates a context** (`CreateNewContext`/`IsDirect`/`GetDrawableAttributes` seen on
  the wire) and only then reports `System GPU doesn't meet the es2 pipe requirement` - falling back to
  **the same software pipeline**. So the extension buys JavaFX nothing and costs it a half-taken GL
  path, and a toolkit's *animations* (a caret, a hover) are what notice.
- **GLX here is compatibility, not speed**: a GL client with no GPU renders in software and ships every
  frame as `PutImage` (769 MB in 5.6 s measured) where the reference shares a DRI buffer - `glxgears`
  ~190 FPS against this server, 2832 FPS against Xvfb. Speed would need a GPU on the client with
  DRI3/Present, or a real GL in the server with indirect GLX.

**Trap that cost time this session**: `RawXClient.createWindow(..., backgroundPixel)` reads a
**negative** int as "no background", and `0xFF0000FF` as a Java int literal *is* negative - a window
background is a **visual** pixel (`0x0000FF`), not ARGB. Getting it wrong paints a grey window and
reads exactly like a server bug.

**Bed note**: `ci.sh` refuses to start when anything listens on 6010 or 6062-6099. The user's own IDE
debug server (`XServerTest`, display :10 → port 6010, `--log-requests`) blocks the suite; on
2026-09-27 it was theirs and it ended by itself, so check port 6010 before killing anything.


**The user commits batches themselves, but on 2026-09-25 they said "你自己提交"** - so from that point
commits are mine to make on branch `feat`, one per item, with the item id in the message. On 2026-09-26
the user said **commit but do not push** - the branch `feat` is local and ahead of origin, and that is
how they want it.

**The afternoon of 2026-09-26 (a long session): `mvn test` 328/328, tree clean at `d86305b`.** What it
left behind, each with the measurement in `docs/backlog.md`:

- **C10 is done, and it was not a documentation item.** A client-side X input method needed three
  font-path fixes: `ListFontsWithInfo` reported the alias instead of the font's own XLFD name; a font
  loaded by that name (or by a pattern) missed the table and fell back to the Java approximation, which
  has *no* `CHARSET_REGISTRY`/`CHARSET_ENCODING` properties - the two Xlib reads a charset from; and
  the XLFD names were not in the font list, so the server named fonts it could not find. Verified end
  to end: `ibus` composing through XIM into a forwarded `xterm` commits 中文.
- **Three request decoders** (`ShapeRectangles`, `XFixesCreateRegion`, `XFixesSetRegion`) hand-written
  because the generated readers size a trailing list from the request length in four byte units where
  they mean a byte offset - a small list computes a negative capacity and the request is *refused*.
  This is A2's long-open kit question; a sweep of every extension request with a trailing list is how
  the other two were found, and `VariableLengthDecoders` documents the class.
- RandR's questions are all answered now (`GetOutputProperty` → `BadName`, `GetProviders` → zero), and
  the refusal log names the request it is about (it used to name the previous one).
- XFixes' `HideCursor`/`ShowCursor` are honoured (counted per window, walked up the parent chain).
- `tools/fontset-probe.py` and `tools/font-reply-proxy.py` are in the kit, and `tools/README.md`'s
  display-number advice changed: the suite binds 63-99 and `XkbControlsTest` takes **:91**, so a
  hand-started server goes on **:61**.
- **Testing went on both machines**: EasyShell and termora each ran their menu gesture 6/6 (termora's
  two captures byte-identical) from `.147`, and EasyShell again from `.150`.

**Later the same day, 46 commits in, `mvn test` 335/335.** What the second half added, all
measured against Xvfb first:

- **A8 finished**: `ChangeCursor` (the destination is rewritten in place - shape and serial become the
  source's, no notification), `GetCursorImage` answers the *displayed* cursor rather than the root's,
  `HideCursor`/`ShowCursor` honoured. `ChangeCursorByName` stays refused **because the reference does
  nothing at all for a name** (its cursor font has no glyph names) - a measured answer, not a guess.
- **A3's other half**: a glyph cursor from any font is composed as X composes one (the *mask* glyph's
  ink is the silhouette, the hotspot is the glyph's origin at the font's ascent), and the embedded
  cursor-font table is used only for the cursor font - a collision at code 66 was giving the wrong
  shape.
- **A12's other half**: `bitGravity`, and **the default had to be fixed first** (`bitGravity` was
  stored as 0 = Forget, which throws a window's contents away - every window would have lost its
  pixels on its first resize). All eleven gravities identical to the reference.
- **B5's first part**: DAMAGE, hooked into `DrawHandlers.afterDrawing` plus the server's own painting.
- Smaller: RandR's timestamps were sequence numbers (now the event clock); a spurious `NoExpose` was
  sent for every drawing request; the font path reports a font's own name as well as its alias and
  resolves patterns, which is what let a font set be built at all (C10).
- **`ci.sh` now refuses to start when a display the suite binds is in use** - a leftover bed or an
  `ssh -L` tunnel to the reference failed four runs that day, each looking like a bug in the server.

**D3 is done too** (`7f96b2f`): the standard X fonts (`fixed`, `6x13`, `7x13`, `9x15`, `10x20`,
`12x24`) are now served from tables captured off the reference server by `tools/kit.sh GenBitmapFont`
into `resource/BitmapFont.java` (172 KB of packed rows, checked in). `tools/FontProbe` - QueryFont
field by field, QueryTextExtents, and the pixels a string draws - is identical to Xvfb for all six,
and everything else still falls back to the Java approximation. The traps that cost the most are in
the design notes' "The fonts a client asks for": text into a **one-bit pixmap** comes back as the
font's *default character* for every code, a right bearing is measured **from the origin**, and a
font's codes do not always start at zero (12x24 begins at one). And `mvn clean` deletes
`target/tools-cp.txt`, which a hand-written `java -cp` needs.

**A13 is done** (`c5bbfda`): X's grab states and the held queue, read off `include/inputstr.h` and
`dix/events.c` and then measured. `AllowEventsProbe`'s five arrangements and `MenuProbe` are identical
to Xvfb line for line; `input/AllowEventsTest` fails without the fix. Two things worth carrying
forward: **a disconnecting client's passive grabs are now dropped with it** (they never were, and the
stale arms made two runs of the same probe disagree), and **the button state outlives the client
exactly as the pointer does** - a probe that ends with a button down makes the next run's press a
duplicate the server drops, which cost an hour and produced a wrong model of the queue.

**Small items since** (`1c40d0e`, `809df68`): the default font path now includes the machine's own
font directories; the tray click's coordinates were **screen** coordinates being read as strip
coordinates (found with a standalone probe, which also proved the platform delivers the click); and
A13's last gap - `AllowEvents` ignores a request whose time is outside the grab, verified against
Xvfb with the probe's new `staletime` arrangement. **A trap that bit again**: a server started by hand
on `:91` makes `XkbControlsTest` fail with `Address already in use` - the suite binds 66-99, so
`tools/README.md`'s ":91 is what the kit uses" is wrong about the suite's range; kill any hand-started
server before `mvn test`.

**C9 is done** (`165d9f2`): the server owns `_NET_SYSTEM_TRAY_S0` and is the tray host
(`tray/TrayBridge`), the icons are shown in the host's menu bar through AWT's `SystemTray`
(`display/AwtTrayIcon` - **no JNI**, which the item expected to need), and `--no-tray` turns it off.
The XEmbed half is what a real Java client needed and found missing: `WindowHandlers.reparent` told
only the *new parent* about a reparent, and Java's tray peer blocks five seconds for the
`ReparentNotify` on its *own* window (it threw "TrayIcon couldn't be displayed" until that was
fixed). Two things about the live check: the menu-bar icon really does appear (a screenshot of the
Mac's menu bar shows the client's blue disc - my first two screenshots "proved" otherwise because the
status area starts further left than I cropped), and the *click* was never verified live because the
client kept exiting between finding the icon's coordinates and pressing it (the click path rests on
`TrayBridgeTest`).

**D3 is now complete** (`ec823be`): the second half - a font **file on a font path** - is in.
`resource/PcfFont` reads PCF (plain or gzipped) into the same `BitmapFont.Font` the generated tables
carry, `resource/FontPath` does `fonts.alias` → `fonts.dir` → file with `~/.xserver/fonts` on the
default path, and `tools/FontProbe` against Xvfb for `8x13` (no built-in table, read from a file) is
**identical line for line** - metrics, properties, extents and pixels. The format's traps are in the
design notes; the sharpest is that **the encodings are section 32 and the accelerators 256** (swap
them and everything decodes as nonsense), the header is in the *accelerators* section, the
per-character metrics are the **ink** ones, and `fonts.dir`'s count line is the only line to skip (a
font file's name usually starts with a digit).

**C10's host half is verified live** (`ba638fb`) and the correction matters: `tools/host-type.c`
*does* reach the window. The item said it "reaches nothing", which was a measurement failure - the
first run was greped for strings the dispatcher never prints. With the host window in front and a
client holding the X focus, typing 你好 produces a KeyPress, a **MappingNotify** and the synthetic
keycode's press and release per character, and a client reading the keymap afterwards answers
**keycode 255 carries U+597D**. `CGEventPostToPid` works too when the window cannot be brought
forward. The client-side XIM half stays open (no dbus on the VM).

**E6 was swept** (`ea1c5f9`): A4old and C11 are ticked, and **E1 is blocked on measurement** - the
protocol fork at `~/IdeaProjects/other/x11` is not a git checkout, so fixing the generator there
needs the owner's decision (version-control it, or replace the five `eventcopy` entries with full
`event` entries and re-publish).

**Later on 2026-09-26, `mvn test` 345/345, tree clean at `380856a`: B4 is DONE - XInput2 is
advertised.** `1626616` is the implementation, `ff27ee5` the harness, `380856a` the documents.

- **What is in**: version handshake, device list (two masters with classes), client pointer, event
  selection, pointer query/warp, cursor, **device grabs**, the XI2 twin of every core input event
  (with the emulated core event withheld from a client that selected the device one), plus XInput 1's
  `ListInputDevices`/`OpenDevice`/`CloseDevice`/two mappings. Refused, not ignored: touch, tablets,
  barriers, raw events, device properties.
- **Why it had to be whole**: `libgdk-3.so` resolves `XIGrabDevice` where it exists, and GTK grabs
  every menu through it - and **AWT segfaults** (`libawt_xawt.so getNumButtons`) on a refused
  `ListInputDevices`. Both measured on the clients' own libraries.
- **The trap that cost the most**: an XI2 device event's fixed part is **80 bytes**, not the 68 the
  protocol XML's field list adds up to (120 total for a key, 136 for a 2-axis motion). Twelve bytes
  short and GDK does not reject it - it reads the key code out of the middle and the application
  stops producing characters, silently. Also: no 32 bit `full_sequence`; length = size/4 - 8;
  `FP1616` is 4 bytes where `FP3232` is 8; `XIChangeCursor` has **no** reply (answering it made
  EasyShell ask `XIQueryPointer` a million times in two minutes) while `XIGetFocus` **has** one.
- **Verified live, both clients** (`--single-window`, XTEST-driven): EasyShell renders, its context
  menu opens and **survives the out-and-back gesture 6/6**, typing in its search box produces the
  character, and the session makes ~9 `XIQueryPointer` calls; termora renders with its menu 3/3.

**Still on 2026-09-26 (third session), `mvn test` 350/350, tree clean at `b1e99da`: B5's per-window
store is in FOR ALPHA WINDOWS, which is what C9's tray icon and C8 both waited on.**

- **What is in** (`cf29f86`): a window whose visual has an alpha channel keeps a store of its own
  (`XWindow.ownPixels`), draws into it in its own coordinates - the whole window, not only the part
  that shows - and is composited onto the screen (`XServer.compositeOwnPixels`, `Compositor.
  compositeOver`, called from `DrawHandlers.target`/`afterDrawing`). `GetImage` reads the store, the
  background paints into it, and the reveal path composites it instead of exposing. **Real
  translucency works now**, and `_NET_SYSTEM_TRAY_VISUAL` publishes the ARGB visual so an icon keeps
  its shape.
- **Two protocol rules had to change**, each of which had made an ARGB window impossible: **a child
  need not share its parent's depth** (the ARGB window is depth 32 under a depth 24 root - Xvfb cannot
  settle this, it advertises no depth 32 visual) and **a GC converts colours in the visual of the
  drawable it was created on**, not the root's - otherwise a half-transparent foreground arrives
  opaque.
- **Tests**: `TranslucentWindowTest` (4) and `TrayBridgeTest.anIconWithAnAlphaVisualKeepsItsTransparent
  Corners`; two of the four fail with the store taken out. EasyShell is unaffected (its windows have no
  alpha) and was re-run live: menu 3/3, typing works.
- **What is left of B5**: the **general** store - every window keeping its pixels - which is what C8
  needs for a frame per top level, plus Composite/Present/SYNC.
- **Live-tool trap, again**: the tray icon docks and is handed to the menu bar (the server logs "an
  icon is in the menu bar now") and goes away with its client, but **a menu-bar screenshot could not
  find it** - and neither could one taken with the old 24 bit visual, so it is the screenshotting, not
  the change. C9's record already warns about exactly this ("the icon was there, outside both crops").

**That session was preceded by: `mvn test` 345/345, tree clean at `76b3da2` - C8 was BUILT and
REVERTED, and the reason is worth not re-deriving.**

- **What it was**: one host window per X top level (a `HostWindow` with its own backend, viewport,
  title, position and input sink; one frame per mapped non-override-redirect toplevel; a menu gets
  none; the whole-screen frame keeps the largest-toplevel rule). It was green - 350/350 with five new
  tests - **and it is reverted**.
- **Why**: with one framebuffer per screen, a dialog's pixels are in its parent's *rectangle* too. A
  screen capture of EasyShell's main-window area with its 工具 dialog open showed the **dialog's content
  filling it**, sidebar and all - the dialog is shown twice and the window under it looks taken over.
  That is the cost the design notes predicted; it is now measured. **C8 needs per-window pixels, which
  is B5's per-window store, before it can be honest.**
- **The five traps the prototype cost** (all in the backlog entry, all of them "quiet" failures): a
  resize request belongs to a window and not a screen (`ScreenResizeTest` "the screen did not grow");
  the whole-screen frame still needs the largest-toplevel rule (`WindowStateTest`); a reported position
  must **not** be cleared as it is read or the settle never fires (`HostWindowPositionTest`); the
  master window must be opened by `start()` and not by the first tick (`TrayBridgeTest` "the backend
  never started"); a frame whose client window is destroyed goes with it (`PerClientWindowTest`).
- **Debugging note for the next session**: surefire **swallows stdout/stderr** of a test JVM here -
  `System.out`, `System.err` and a `-Dlogback.configurationFile` file all produced nothing - so a
  print-based hunt inside a test run is a dead end. Reason about the behaviour and let the assertion
  be the signal.

**The reconnaissance that preceded that (same day), `mvn test` 339/339, tree clean at `5187cf3`.**
`docs/backlog.md` is the record; what matters for picking the work up:

- **E7 is done and ticked** (`4a94f8a`): E2's orphan - a server with no display logged "opened a window
  for display :95 showing ..." one line under its own warning that nothing could be shown.
  `DisplayBackend.isHeadless()` (which existed with **no caller**) now decides the wording.
- **`tools/XiProbe` is new** (`91f6bf4`) and is B4's harness. It prints the XI2 version handshake, the
  client pointer and the whole device list as fields *and* bytes, and its `select` mode subscribes,
  warps the pointer in and drives a button through XTEST with the core masks selected too.
- **B4 cannot be landed in halves, and that is measured, not guessed.** JavaFX's Linux backend is GTK:
  `libglassgtk3.so` calls `gdk_device_grab`, and **`libgdk-3.so` resolves
  `XIGrabDevice`/`XIUngrabDevice`** (checked with `strings` on the AppImage's own copies). GDK3's grab
  *is* `XIGrabDevice` once the extension exists, so advertising XInput2 without the device-grab family
  costs a forwarded GTK application its menus. The item's plan ("queries first, events later") is a
  worse version of the item, not a smaller one. The wire facts (mask numbering, the 12 byte
  GenericEvent header with **no full_sequence**, the 68 byte fixed part, `XIGetClientPointer`'s
  `BadWindow` for the root, the press/release pairing rule) are all in the backlog's B4.
- **C9's "publish a 32 bit ARGB visual" is disproved**: the screen framebuffer stores no alpha by
  design, so the icon's transparency is gone before the tray reads it; it needs a per-window store,
  i.e. B5.

**What is left, and every one of them is days, not hours:** **B5** (Composite/Present/SYNC, needs a store per window - C9's leftover is
part of it), **B6** (GLX, a project of its own), **C8** (one host window per client toplevel, L, no
written plan), **C5** (the drag *out* needs a native drag source), **E1** (blocked: the fork at
`~/IdeaProjects/other/x11` is not a git checkout, so the owner has to decide), **E2** (only the CI
pipeline is left, and that is the repository host's), **E4** (its second half is declined with reasons
in the entry). The next session should pick one of B4/B5/C8 knowingly and give it the days it needs -
a session is not enough for any of them.

**The test bed, as it is used**: the server runs on the Mac, EasyShell and termora on the VM at
`192.168.22.152` against `192.168.22.1:<display>`; `Xvfb :98` on the VM is the reference. EasyShell is
the primary client and termora the secondary (the user's own instruction). The launch recipes are
still in `/tmp` (`es-start.sh`, `termora-start.sh`, `client-run.sh`), and `tools/bed.sh single` starts
the server plus one client - but **a bed left on :95 makes `GrabCrossingTest` fail with "Address
already in use"**, and `mvn clean` deletes `target/tools-cp.txt`, so `tools/kit.sh` has to regenerate
it (it does by itself; a hand-written `java -cp` has to wait).

**Traps this session cost time** (each is now in the backlog, the design notes or `tools/README.md`):
the button state outliving the client (above); a stale passive grab arm doing the same; `pkill -f` on
the VM killing the remote shell when the pattern is anywhere on its command line, a path as an
argument included - `[m]ount_termor` is the spelling that works; `tools/vm.sh`'s `ssh -n`; and that
`mvn test`'s incremental compile does **not** recompile a test when only a *main* class's signature
changed (it shows up as `NoSuchMethodError` in a test that looks untouched), so a signature change
wants `mvn clean test` once.

**And the XInput2 traps, because each one reads as a server fault**: the XI2 *event mask bits* are not
the core ones - `1<<4` is `ButtonPress` where `1<<3` selects nothing - and a wrong mask is **accepted**
and changes nothing, which looks exactly like a server that cannot deliver a device event; an XI2
event is a `GenericEvent` whose header is **twelve** bytes with no 32 bit `full_sequence` (assume one
and the timestamp reads as 1 and the detail as the root window's id); it must be read **whole** or the
next message decodes as garbage; and `XIGetClientPointer` is answered `BadWindow` for the *root* on a
reference server, so a probe that asks about the root measures nothing.

**2026-10-04: "实测还是会漂移" - the edge drag is fixed, and the leftover guard was only half of it.**
The commit `69dc2a4` (`userDragPending`: the frame is not sized back under the pointer during a drag)
did not stop it. Re-measured with scripted drags against `xterm` on the bed: a **40 pt pull of the left
edge** moved the window's origin 40 pt and shrank it by **156** (784 -> 628), in steps of one character
cell. **Two causes, both found by logging every change of the frame's size together with the frames of
this package that asked for it** - the scene's listener cannot tell the platform's live resize from the
backend's own `setWidth`, and telling them apart was the whole diagnosis:

1. **`fitWindowToViewport` runs at the top of `flushDamage` - every flushed frame - and knew nothing
   about a drag.** It pulled the frame down to the *client's* window (one round trip and one snap
   behind the pointer) on every frame, and the platform then worked its next live-resize step out of
   that smaller frame. Hence the asymmetry the user reported: a right/bottom drag recomputed from the
   same numbers every step, while a left/top drag carried the origin at the platform's rate and the
   size at the runaway's rate. It now refuses to size the frame while a drag is in flight, exactly as
   `setViewport` does.
2. **The snapped remainder.** A client with resize increments will not take the size the pointer asks
   for (`xterm` snapped 744 to 732), so fitting the frame back to the content moved the *far* edge by
   the snapped 12 pt - the same drift in miniature. `settledFrame*` (the frame's geometry as of the
   last time no drag was in flight) now says which edge moved, and `sizeFrameToContent` puts that edge
   back. The settle is a `PauseTransition` restarted on every resize report rather than the next
   flushed frame: a client that has stopped drawing left the band of nothing in place for as long as it
   felt like it (2.5 s measured).

**Verified by script on all four edges, in both modes**: left/top drags keep the right/bottom edge
*exactly* (xterm 1410 -> 1410, xclock 380 -> 380, single-window 406 -> 406), right/bottom drags keep
the origin exactly, a drag that **pauses two seconds in the middle** still lands with the far edge
exact, and xclock+xeyes in single-window mode stay at a fixed size. 475 tests green. `mvn -o test`.

**Two things this session cost time, both worth remembering.** (1) `CGEvent` drags go to whatever is
under the pointer, so a host window behind the IDE silently takes them - a drag that "does nothing" is
usually this, and `activate <pid>` (AppKit, `/tmp/xsn/activate`) plus a check of the store's remembered
position is what makes it reachable; the window's own remembered position moves between runs, so the
grab point has to be read from `winlist` each time, not pasted. (2) `winlist` (CGWindowList) is in
**points**, the same units as `CGEvent` - a JavaFX `Scene` of 384 pt is a *392 pt* window (392 + 0
decoration wide, 28 pt of title bar), and reading the two as if one were pixels produced an hour of
nonsense. `--geometry 1300x800` with `--scale` unset means `options.scale() == 1`, so scene points and
framebuffer pixels are 1:1 in these tests.

**Found on the way, not fixed (A40)**: un-maximizing restores the size exactly but puts the window at
**40,38** instead of where it was; measured on the pre-change build too, so it is older than this work.

**Also changed**: the pulse probe (`startPulseProbe`) is now started only at debug level - it is an
`AnimationTimer`, and a running one asks the toolkit for a pulse every frame for the window's whole
life (measured 120/s on a scene with nothing to redraw).

**2026-10-04 (later): "单窗口模式全屏和退出全屏不符合预期" - one line of consequence, and it is A38's.**
macOS reports a **full screen window as maximized as well**, so the maximize handling (A38: fill the
window to the usable area) fired about a second after the Space was entered and **resized a window that
was in its own Space** - and a full screen window whose frame is changed leaves the Space without
giving it back, which is why the menu bar and the **Dock** stayed hidden until a lock and unlock, and
why the front application became whoever was behind ("全屏焦点被桌面抢走"). It is also why the mode
worked with no client window: nothing of ours sizes the frame when there is nothing to size it for.
The same fill shrank the client's window from the full screen area (1512x945 -> 1512x922) and the zoom
flipped back to false, so the restore ran against the wrong remembered size - "经常恢复不了".

**The fix is a rule rather than a patch**: `platformOwnsFrame()` answers yes for a drag of the frame's
edge **or a full screen window**, and every path that sizes or moves the frame asks it (`setViewport`,
`fitWindowToViewport`, `sizeFrameToContent`, `keepOnScreen`, `enforceWindowSize`, the drag settle
timer). The maximized listener returns while full screen, `setMaxClientArea` defers its `setMaxWidth`
until the window is out, `windowMovedByUser` ignores the Space's move to the top left, and a
`fullScreenProperty` change clears `dragJustEnded`. The grow and the restore themselves needed no new
code: the platform's full screen frame arrives as a scene resize, which the server already reads as
"the client's window should be this size", and the platform's own frame on the way out is what the
client's window is put back to. **⌃⌘F replaces Escape as the exit key** - JavaFX consumes Escape before
the scene sees it, so a forwarded application in full screen would never be given its own Escape.

**Verified by script in both modes** with `/tmp/fsprobe` (`FsProbe`/`FsProbe2`, a raw client that holds
the state and prints its own geometry): 400x300 -> 1512x945 on entry, held 12 s; -> 400x300 within 1 s
on exit; the host window back at exactly 106,81 400x328; a client that reconfigures *itself* while full
screen still restores to the platform's own frame (400x300, not the 500x400 it had); the user's ⌃⌘F
exit works; Escape no longer leaves full screen. 475 tests green. The maximize path itself is unchanged
for a non-full-screen window (A/B against the pre-change backend agrees), and A40 is still open
(un-maximize restores the size but puts the window at 40,38).

**Traps, again**: a synthetic double click on a title bar did **not** zoom the host window in this
session (on the pre-change build either) even with `AppleActionOnDoubleClick=Maximize`, while the same
tool zoomed an xterm window earlier in the day - so the maximize itself is for the user to re-check by
hand; and a host window that is behind the IDE takes none of the synthetic events, which is why every
measurement here was taken with the window freshly opened (`raisePending`) or after
`/tmp/xsn/activate <pid>`. `FsProbe` must be started in a session of its own: started with `&` its
process group is killed when the command returns, which reads as the client disconnecting mid-test.

**2026-10-04 (the pointer after full screen, A42): a loan that was never given back.** Reported as
"全屏后退出后鼠标会消失，拖回 xserver 窗口就恢复，再拖出就正常". The cause is the *decision*, not the
hiding: whether the pointer is over the window - which is what decides whether macOS's arrow is hidden
with a process-wide `[NSCursor hide]` - came only from JavaFX's enter/leave events, and the case those
miss is **a window that moves or changes size under a pointer that does not move**, which is what full
screen is made of. The window shrinks away from a stationary pointer on the way out, no leave event
arrives, the answer stays "over the window", and the arrow stays hidden - and the sprite that would have
been visible is clipped by the canvas, so the pointer is simply gone over the desktop.

**Fix**: `updatePointerOverWindow` takes the answer from `java.awt.MouseInfo` compared against the
window's frame (`pointerInside`, package-private and tested), and `applyHostCursor` runs again whenever
the window's position or size changes. The events still give it the moment they happen. Logged when the
decision changes: `host pointer hidden/handed back: over the window=..., application active=...,
idle=...` - three inputs, one line.

**Verified in the direction that matters**: leaving full screen with the pointer parked outside, the
arrow is handed back 28 ms after `fullScreen true -> false`, from the geometry alone - with the
leave-event handler disabled the line still appears, and the event itself only arrives 600 ms later.
476 tests green (the new one pins the geometry decision, including the reported pair).

**And the instrument traps, because both invalidate a measurement rather than a fix**: on this machine
the cursor's real visibility **cannot be measured from a shell** - `screencapture -C` draws the cursor
*even while it is hidden* (calibrated with an AppKit tool holding `[NSCursor hide]`: a full-screen
capture pair still differs by the whole cursor, 439 px), and `CGCursorIsVisible()` is deprecated and
always answers `true`. Every "visible/hidden" reading taken before that calibration was noise; do not
build a test on either. The opposite desync (macOS re-shows the pointer on a Space switch while
JavaFX's flag still says hidden, so the next hide is skipped and two pointers show) is in the backlog
with why a hide/unhide "pump" only trades one symptom for the other.

**2026-10-04 (the JavaFX 27 upgrade and the window's own title bar, A43): the user put the pom at 27
and asked for `HeaderBar` as the title bar, 30 points fixed.** Three things about JavaFX 27 that are
worth not re-learning: **`StageStyle.EXTENDED` and `HeaderBar` are preview features** and throw unless
`javafx.enablePreview=true` is set *before the toolkit reads it* (done in `JavaFxBackend`'s static
initializer, which loads before the toolkit starts); the window is extended-only where
`Platform.isSupported(ConditionalFeature.EXTENDED_WINDOW)` says so, and a platform that answers no
keeps its own title bar and gets no header bar of ours; and **`target/tools-cp.txt` does not follow the
pom** - the tools' manual classpath kept listing JavaFX 25 after the upgrade, which shows up as a
`NoSuchMethodError` for a method that exists in the jar on the classpath, and `tools/kit.sh` now
regenerates it when the pom is newer than it.

**The rendering adaptation is "content" changing meaning**: the scene is the whole window including the
header bar, and the client is shown the canvas below it. So the content size is read from the window
minus the header (and the platform's frame), the pointer mapping subtracts the canvas's place in the
scene, mouse events on the chrome are not forwarded to the client (a press there is the window being
moved - without the guard every title-bar drag delivered a click at the top row), the cursor policy is
about the content rather than the window, and full screen hides the bar (`managed` false as well as
invisible). `sizeToScene()` had to go: its "preferred size" is the BorderPane's header bar alone, so
the window opened as a 30 point strip and the client was shrunk to 21 points by the platform's own
resize of it.

**The maximize was re-designed, and that is an upgrade effect rather than a port.** JavaFX 27's macOS
zoom now goes to the display (measured: a 784x344 window zooms to 1504x853) where the old measurement
had it coming from the window's content (1280x828 -> 1280x886), so A38's fill is unnecessary - and
harmful: **setting a window's size while macOS has it zoomed un-zooms it**, which undid the fill within
the second, flipped the state back and restored nothing (measured on the pre-change revision too). The
zoom is now left alone and the client takes the size the window has; the one addition is a 160 ms
settle that reports only the size the frame *stops* at, because the zoom is animated (785, 787, 798,
... 1511) and the client chasing every intermediate flapped the window between two sizes twice a second.

**Verified**: the four edges exact with the header bar (all far edges preserved), the header drag moves
the window without leaking a single button event into the client, the maximize/restore is exact
(784x374 <-> 1511x884), full screen hides the bar and gives the client 1512x945, a click 10 px inside
the content lands at screen (11,61) for a viewport at (1,51), and **477 tests green**. Two artifacts
are recorded in A43: a maximized window can be up to one client increment wider than a client that
snaps to its cells (xterm: 1499 of 1511 - we may not clamp a zoomed window's frame, which is what
would un-zoom it), and the first left/top edge drag right after a maximize/restore can slide because
macOS's live-resize reference can still be the zoomed frame.

**2026-10-04 (the two header-bar details, the same day):** the client's icon is **out** of the header bar
(the user asked for it), and the title is **bold** - by `-fx-font-weight` rather than by setting a font,
because the platform's own title is the system UI font in bold and naming a family would freeze the size
and the family along with the weight that was wanted. With the left area empty the centred title sits in
the middle of the window, which is where a title bar's title belongs. 477 tests green.

**2026-10-04 (same day, the pointer over the header bar):** "整个标题栏区域，鼠标是隐藏的…想按最小化
或者全屏按钮都很困难". The decision (A42) was taken from the platform's pointer position, but only
*asked* on the events that used to matter - entering or leaving the window, focus, idle, geometry - and
crossing from the content to the header bar is none of them (the scene is not entered or left, and
`enterWindow` finds the flag already set). So `pointerMoved` now re-decides on every move, and
`drawCursor` skips the client's sprite while the pointer is over the chrome, with a decision change
repainting the sprite's own box so that frame is what erases it. Verified: the log says "handed back"
over the bar, and two captures of the same window differ only in the 24x30 pixel box where the sprite
was. 477 tests green.

**2026-10-04 (two more of A43's follow-ups):** **full screen was slow because the tick was blocked by
the transition** - `stage.setFullScreen` blocks the toolkit thread for the whole Space animation (658 ms
measured) and was called with `runOnFxThreadAndWait` from the tick, so the client's resize, already
reported 19 ms in, was not applied for 700 ms. It now goes through `Platform.runLater`, and the client's
window is at the new size **34 ms** after the state changes. What remains is the *picture*: the
transition holds the toolkit thread, so nothing the tick queues runs until it is over (the old picture
in the new window for the length of the animation). And **a title-bar move after a resize was pulled
back**: A39's far-edge anchor is armed by a resize and a *move* changes the same x/y a left/top edge drag
does, so the move was read as the resize continuing. A move is now told apart by the platform's own
answer - an edge's live resize delivers no mouse events at all, a title-bar drag delivers them - so a
button that is down when the position changes clears the pending anchor (verified: resize then drag ends
at 248,136, not at the old position). 477 tests green.

**2026-10-04 (A44, single-window mode with no clients):** the question was whether the window should be
hidden when no client is connected and shown/raised/focused when one arrives. What it did: with no client
*at the start* there is no window at all (it is opened by the first window a client maps), but a window
that had been opened was **kept, holding its last frame** - a deliberate design that eight tests assert.
Changed so that a session with nothing to show closes its window after **1500 ms** of grace (below the
lock) and the next client to map a window opens it again - which was already where the raise happened, and
now `raiseAndFocus` (new in the backend API) gives it the keyboard too, while the urgency hint keeps only
raising. The grace period is what keeps the eight tests and their design intent: a toolkit that unmaps one
window and maps another while starting up must not make the window flicker. Verified on the bed: connect
-> the window is first in the window list; kill the client -> "holds its last frame" then "has had nothing
to show for 1500 ms: its window is closed"; connect again -> a **new window id** and front again. 477
tests green.

**2026-10-04 (A44's follow-up): the window was shown once and then hidden.** "我要跟per-client一样保持静默，
直到x客户端连接才显示真正的窗口，当所有x客户端断开，应当马上关闭窗口" - and the first half matters: the
**backend** is still created at startup (VNC needs to serve viewers before any client draws, the tray and
the in-process tests need the backend object) but **showing** is now the server's answer rather than the
backend's own decision - `setIdle(false)` shows the window, and a session with nothing to show is idle from
the first tick. Everything measured from a frame (the size, the placement, the platform's decoration) is
measured at that first show. Hiding happens the moment the session goes idle, which is the same rule a
per-client window follows on disconnect. Verified: a server started with no client has no window at all;
connecting puts it on the screen first in the window list; killing the client takes it away within 400 ms.
477 tests green, with no change to the server or to any test - the whole change is the JavaFX backend.

**2026-10-04 (the same fix, corrected after the user read the file):** the first attempt left the decision
to the backend (`if (!idle) showWindow()`) and a backend's own `idle` is false until it is *told* - the
server told it a tick later, so `stage.show()` ran anyway and the window flashed inside the first two
ticks ("我看了下还是会极短地闪一下窗口，并且这行代码明显被调用了"). The state now comes first:
`masterSession.setIdle(true)` before its backend is built, and `openSessionBackend` tells the new backend
what the session is before it returns. Verified: no `window shown` line in the log at startup at all.
Lesson worth keeping: a backend flag that mirrors a server decision is not a decision the backend can
make - ask the server, or be told before you act.

**2026-10-04 (A45, cross-platform):** asked to audit the whole project for macOS-only assumptions and
record them. The audit is `docs/design-notes.md`'s **Platforms** section - a table of everything
platform-specific with what happens elsewhere, and four rules a change has to keep - and two things were
actually wrong: the **full screen exit key was macOS's Control-Command-F only** (now `F11` on everything
that is not macOS, via `JavaFxBackend.keyFor`, the only `os.name` read in the codebase, pinned by a test),
and the **unix socket created `/tmp/.X11-unix` before asking whether the platform has that family** (the
channel is opened first now, so a Windows machine is not left with a `\tmp` directory before being told
the socket is unavailable). Everything else is either a capability behind a check with a fallback
(extended windows -> decorated, JavaFX -> Swing -> headless, unix socket -> TCP) or a macOS quirk handled
in code that is inert elsewhere. `tools/` is macOS-only on purpose and nothing under `src/main` uses it.
477 tests green.

**2026-10-04 (A45, the owner's rule for the tools):** "测试工具例外，如果要在其他系统下调试，我会要求创建对应
平台的测试工具，但是xserver本身不应该依赖系统平台". The tools' platform-specificity is by design, and the
audit's *conclusion* is what matters: nothing under `src/main` uses them, or needs a platform at all. The
complete list of every platform touch in `src/main` is now in the design notes' Platforms section - one
`os.name` read (the full screen exit key, A45), one `File.separatorChar` comparison (Windows-aware file
URIs in DropBridge), one font *search list* that ends in the built-in tables (so a machine with no X fonts
answers every request anyway), `user.home`, and no `ProcessBuilder` anywhere - so the next reader checks the
list instead of the code. 478 tests green.

**2026-10-04 (A46, the Dock's Quit in single-window mode):** the report was "单窗口下，macos在dock栏右键程序，
点击退出没有反应，但是在per-client模式下正常". Reproduced with the Dock's own request
(`NSRunningApplication -terminate`, the A27 helper): per-client mode was gone in ~500 ms in both states,
single-window mode did nothing at all **when nothing was connected** - which is A44's state, a session
with no window. The cause is who the *application* belongs to: the platform asks one toolkit to quit,
and **JavaFX (Glass) only turns that into close requests on its *visible* windows** (measured with a
minimal JavaFX program: a hidden stage hears nothing), while **AWT ends the process** - and per-client
mode's clipboard/tray/scale code happens to touch AWT while the server starts, which is why that mode
always worked. Single-window mode opened its window first, so JavaFX claimed it.
`XServer.claimHostApplication` now asks for AWT's toolkit before any window can be built, skipped where
there is no host window (headless, VNC). Verified: idle single-window Quit is gone in 500 ms with
`display :96 stopped`; with a client attached it is 3.5 s and the client is asked to close rather than
cut off; the pointer is still hidden over the content and shown over the header bar, the header bar
still drags the window, and the two A/B checks against the pre-fix build agree (the title-bar buttons
do not take synthetic clicks in *either* build - that is the tools' known limitation, not a
regression). No internal API was used (the alternative was hooking `com.sun.glass.ui`'s quit handler),
so the design notes' audit line "uses an internal API | nowhere" still holds; the new row in the
Platforms table says what happens elsewhere. 478 tests green.

**2026-10-04 (A47, the XInput version gap):** asked whether the XInput implementation really was
several releases behind a reference ("你上次提到xinput实现落后xfvb几个版本...也完善下"). It was: A36
had measured **2.0 here against Xvfb's 2.4**, the one version gap on §9's whole page, and the code
said why (a client told 2.4 would ask for requests that were refused). So the work was to make that
sentence false. `XIQueryVersion` now follows the reference's rule - the lower of the two versions,
**BadValue** for a major below 2 (measured: error 2, value 1, minor 47), and 3.0 answered as 2.4 -
and both handshakes say 2.4. **Raw events (XI 2.1) are implemented**: selected on a root window,
delivered for every device event, in the reference's own third shape (a motion is 72 bytes with the
mask, the raw values and the processed ones; a button and a key are 40 with an empty two-word mask)
- with `xinput test-xi2 --root 2` and libXi's own parser as the live check, a warp sending nothing
(as a reference does), and the root taken from **the screen the input belongs to** (the first
version delivered to the server's screen and reached nobody, because a client is given a screen of
its own in per-client mode and XTEST is scoped to the sender's screen). `XIBarrierReleasePointer`
(2.3) now answers the extension's own barrier error, code 12, inside a range XInput declares (13
errors, not 1 - the registration site says why), which is exactly what a reference answers for a
barrier that does not exist; barriers themselves need XFIXES 5.0 and are written up as the next
item, together with `XIChangeHierarchy` and XI passive grabs (both measured: a reference accepts an
empty change list and *creates* a master, and answers a passive grab with a modifier list). Live:
termora renders through the new build with no new refusals, and GDK does not select the raw family
at all (measured from the log), so the new events only reach clients that ask. 482 tests green.

**2026-10-04 (A48, the device model's first half):** the follow-up to A47's "what is left" was "继续
完成你记录剩下的事项", and the device model went first because it is the half a reference can be
measured against. Xvfb's own device list is six entries (measured with `XiProbe device 0`): two
masters, the two **XTEST** slaves (4/5) and its backend's "Xvfb mouse"/"Xvfb keyboard" (6/7), with the
masters' *class* records naming the slave the class comes from and an XTEST event carrying `device 2,
sourceid 4`. This server now has the same four devices a reference leads with (ids, names, types,
attachments, classes, and the XI 1 list with the reference's `use` values), and **XTEST input carries
its slave as the source** in raw and device events alike (`InputState.setSourceDevice`, set by
XTESTExtension around each faked event) - verified live: `XiProbe device 0` prints the reference's
first four records and `XiProbe raw` prints `sourceid 4`. What is not here is written down: a backend
device of this server's own (a naming decision, where a reference has "Xvfb mouse"), and the changing
half - `XIChangeHierarchy` (still refused; a reference accepts an empty list and really creates a
master), the `XI_HierarchyChanged` event (bit 11; GDK selects it) and XI passive grabs (54/55). Two
findings for the barrier item: a registered barrier on Xvfb does **not** stop an XTEST-driven pointer
(absolute or relative, with or without a device list) and sends no hit event, so that item's semantics
have to come from the reference's `Xi/xibarriers.c` rather than from this bed. 483 tests green.

**2026-10-04 (A48, cont.):** "那你继续往下做剩下的" - the next slice is the *changing* device table, and
this round measured it end to end so the implementation starts from numbers: `XIAddMaster` (name,
send_core, enable) creates **four** devices, not two - `<name> pointer`/`<name> keyboard` plus
`<name> XTEST pointer`/`<name> XTEST keyboard` attached to them (ids 8..11 for the first master added
to a fresh reference, list 6 -> 10) - and each new device also emits `XI_PropertyEvent`s.
`XI_HierarchyChanged` (evtype 11) is a 32 byte fixed part (`deviceid` 0, time, `flags` 0x55 =
MasterAdded|SlaveAdded|SlaveAttached|DeviceEnabled, `num_info`) followed by **one 12 byte record per
device in the server** (the whole hierarchy after the change: 344 bytes with 26 devices on the
reference). The one piece of it that could be landed without the mutable table is the selection rule,
and it is in: bits 1/11/12 are only selectable on **XIAllDevices** - on a master a reference answers
**BadValue naming the bit** (error 2, value 11, measured), which is what a client reads to know it
will hear about a device appearing. Implemented and pinned by a test; the request itself stays refused
until the table can change, and `XIPassiveGrabDevice`/`Ungrab` (54/55) go with it. 484 tests green.

**2026-10-04 (A48, the changing device table):** "那就继续吧" - the device model became a **mutable
table** (`input/DeviceTable`, built from the four devices A48's first half measured) and
`XIChangeHierarchy` is implemented on it: `XIAddMaster` creates the **four** devices a reference
creates (`<name> pointer`/`<name> keyboard` and their `<name> XTEST pointer`/`keyboard`, ids from 6
here where a reference starts at 8 because it has two more devices of its own), `XIRemoveMaster`
takes a created master and its slaves away (the core pair is refused with the same `BadValue` a
reference answers), `XIAttachSlave`/`XIDetachSlave` re-attach a slave (anything that is not a slave is
the extension's `BadDevice`), and the **`XI_HierarchyChanged`** event goes to the clients that
selected bit 11 with `XIAllDevices` - a registry kept because that event names no window - in a
reference's shape: 32 bytes of `deviceid` 0, time, flags and count, then one 12 byte record per device
the server has (flags **0x55** for an add = MasterAdded|SlaveAdded|SlaveAttached|DeviceEnabled,
measured on Xvfb; the reference's own extra `XI_PropertyEvent`s per new device are not sent because
this server has no device properties at all). Also measured and implemented: `ListInputDevices` (XI 1)
lists the core pair and every *slave* but **not** an added master pair - a reference listed eight
devices after an AddMaster and not the master it had just made. What is left of the item:
`XIPassiveGrabDevice`/`XIPassiveUngrabDevice` (54/55) and a naming decision for a backend device of
this server's own. 485 tests green. Verified with `xinput` itself against this server: `create-master TestMaster` makes
the four devices and `xinput list` draws the same tree the reference draws, `remove-master` takes
them away, and removing `"Virtual core pointer"` is refused by both servers with the same BadValue.

Verified with `xinput` itself against this server (and beside the reference): `create-master TestMaster`
makes the four devices and `xinput list` draws the same tree the reference draws, `remove-master`
takes them away again, and removing `"Virtual core pointer"` is refused by both servers with the same
BadValue (minor 43, value 0).

**2026-10-04 (A48, the passive grabs - the item closes):** "那你继续往下做剩下的" - the last piece of
the device-model item was `XIPassiveGrabDevice`/`XIPassiveUngrabDevice` (54/55), and it is in: the grab
is armed on the same `server.passiveGrabs()` table the core `GrabKey`/`GrabButton` use (so a press
inside the window really activates it), answered with a 32 byte reply whose `num_modifiers` counts the
modifiers another client already holds for the same window and detail - which is what a reference is
measured answering when nothing conflicts (0) - and the ungrab removes it (accepted in silence, as a
reference accepts it). The XI2 mask is translated to the core mask the delivery path works in
(`XInputMasks.coreMaskFor`), and grab types 3 and up are refused with `BadValue` because no input path
here arms a crossing grab and neither server has a touch device. Both request layouts are pinned by a
test: armed (reply, 0 conflicts), a second client told the conflict (naming `XIAnyModifier`), and the
ungrab taking it back. 486 tests green. What remains of the item is the naming decision for a backend
device of this server's own (a reference has "Xvfb mouse"/"Xvfb keyboard"), and the barrier item
stands as A47 recorded it.

**2026-10-04 (A48, the host's pair - the item closes):** the last piece of the device model was the
one thing that is *not* a measurement: a reference has two more slaves, its backend's ("Xvfb mouse" 6
and "Xvfb keyboard" 7 attached to the core pair), and this server's host input was the master's own.
It now has the same pair - `xserver mouse` (6) and `xserver keyboard` (7), names mirroring `Xvfb`'s -
so the device list is a reference's six, host input names its own device as its source where XTEST
names its slaves, and an added master starts at 8 exactly as a reference's does. Verified with
`xinput list` side by side: the same tree with the same ids and attachments, differing only in the
backend pair's name. 486 tests green. The device-model item (A48) is closed; what is left on the
XInput page is the barrier item (A47's blocking half, unmeasurable in this bed) and nothing else.

**2026-10-04 (A49 recorded, A50 the X Test Suite):** asked to record the barrier blocking rather than
build it, and to run the X11 completeness tests. **A49** now carries the whole picture: what is
missing (XFIXES 5.0, the barrier events, the clamping), why it is parked (no client in this bed links
`XFixesCreatePointerBarrier` - checked with `nm -D`: libXfixes exports it, x11vnc/xinput/xdotool/Xvfb/
libgdk-3 all have zero references - a client is told 4.0 and falls back, the bed cannot verify the
blocking, and it would touch the pointer-motion hot path), and exactly how it would be verified
(request surface against a reference, event layout from the header, behaviour on this server with the
*host* pointer via a kit tool plus the CGEvent tools). **A50** is the XTS run: SHAPE went from 5 of 11
failing to 10 of 11 - a reference answers `ShapeGetRectangles` with ordering **YXBanded (3)** for
every shape it is given, where this server answered `Unsorted (0)`, and the list is now really banded
(and masks decomposed) rather than the field dressed up; one case is left with its journal numbers
(the bounding-box -1/-1 convention). The XI/XIproto sets cannot run at all - 107 of 107 and 279 of 316
UNTESTED - because they are XInput **1.x**, which is implemented only as far as the device list; the
reference's own numbers there are 88/107 pass and 55 failures, so that gap is a project of its own.
Harness lesson: an XTS run against a server backgrounded from a one-shot shell command reports "81 of
82 failed" with every purpose ABORT - the server was killed when the command ended; run it in a live
session. 486 tests green.

**2026-10-04 (A50 cont., the last SHAPE case):** "关于SHAPE的问题，追查下，想办法解决" - the remaining
case was `XShapeQueryExtents` purpose 8, expecting `-1,-1 1154x654` against our `0,0 1152x652`. The
test's own source says why: the window has a **one pixel border**, and the shape coordinate space puts
the border outside the window's origin, so an unshaped window's *bounding* shape is
`(-bw, -bw, w + 2bw, h + 2bw)` while its *clip* shape is `(0,0,w,h)` - and `ShapeGetRectangles`
answers one less in each dimension than `ShapeQueryExtents` for the same shape (regions are
half-open). Measured with a probe on Xvfb and here: bounding extents -1,-1 62x42, clip 0,0 60x40,
rectangles -1,-1 61x41. This server ignored the border, which is invisible when the border is nought -
the case every earlier probe used. Fixed in `ShapeExtension.extents` (and the ShapeNotify extents use
the same convention now). **The SHAPE set is `All 11 tests passed`.** 486 tests green.

**2026-10-04 (A51, three VNC bugs from one report):** the user reported that Finder could not connect
with the right password, then that the picture was 花屏, then that clicks did nothing. All three were
real and were measured against Finder's own Screen Sharing driven by the bed's input tools: (1) Apple
speaks **RFB 003.003** and waits for the **SecurityResult** that only 3.7 has, so the handshake stopped
after the challenge response - the fix waits 500 ms for the ClientInit a compliant 3.3 client sends
instead and sends the result only if it does not come (and the result is now sent for **3.7+**, not
3.8+, which was the same mistake on the other side); the `gave the wrong password` line in the log was
Screen Sharing's first, empty probe. (2) The **ZRLE CPIXEL** was four bytes because the client
announces depth 32 - real clients use three for any 32bpp client whose masks fit in 24 bits, and the
condition that made it four is gone (the test client already had the real rule, which is why the suite
was green while the picture was not). (3) Clicks did nothing because the update request **waited for
damage inside itself with the client's input unread** - a thread dump showed the session `TIMED_WAITING
... at updateRequest` while the pointer had not moved; now the wait checks `in.available()` first and
remembers the request in `outstanding`, which the message loop answers. 491 tests green (the project's
own `XServerTest` excluded: it launches a real server and is what makes a plain `mvn test` say "the
forked VM terminated").

**2026-10-04 (A52, the whole option surface on a real machine):** the user asked for a complete
real-machine pass over every configuration option ("各种配置项都验证下…要完整验证所有配置项看下是否都生效").
Every option in `ServerOptions` was started for real and probed with a real client - the VM over TCP
(`xdpyinfo`/`xrandr`/`xwininfo`/`xclock`/`xeyes`), the host's window list for the two window backends,
a **real RFB 3.8 handshake** (Java + `javax.crypto` DES, validated first against `x11vnc` so a failure
is the server's), a **WebSocket RFB banner** for the noVNC path, and a client that ignores the close
request to time `--close-grace-ms`. **Two bugs fixed**: `--title` was parsed, documented and read by
**nobody** (every backend got `DEFAULT_TITLE`; the VNC desktop name is now `--title`, pinned by a new
`VncBackendTest` case), and **`--max-clients` had no reader at all** (the earlier "accepted 4 of 4"
matrix result was the probe closing each connection before the next, so it never exceeded one; a
`--max-clients 2` server now accepts 2 of 4 held open, logs the refusal, and is pinned by
`net/MaxClientsTest`). One difference **recorded**: the depth in `-screen 0 WxHxD` is accepted for
compatibility and ignored (the root is always 24 bit; the server now warns once at startup) - the doc
is the checklist's §7 and the entry. Everything else measured green, including both passwords forms
(handshake OK / wrong refused) and the both-forms and missing-file refusals. **493 tests green.**

**2026-10-06 (A53, EasyShell and termora on every backend and both window modes):** the user asked for
the two applications to be walked over the whole configuration matrix - "各种backend都测试下，各种窗口
模式也都是测试下…窗口弹窗，输入，右键功能，右键菜单". Both apps were launched against javafx, swing,
headless and vnc in single-window, and against javafx and swing per-client (vnc implies single-window;
headless per-client has no window to look at by design). Everything renders and answers: context menus,
a wizard, a dialog with a text field, termora's Quick Connect and a modal message dialog, a local
terminal that runs commands, ASCII and Chinese typing, clicks and hover. **The single-window
framebuffer is byte-identical between javafx, swing, headless and vnc** (`md5` of the `xwd` captures;
the one difference was termora's own hover/selection state, 0.63% of pixels in one row). Two lessons
about the instruments: single-window mode is the only mode an XTEST driver can reach (per-client gives
every client its own screen, so per-client must be driven with `tools/host-input.c` through the host
window), and `xwd` captures are 1:1 while `screencapture` of a window is 2x. **One candidate bug
recorded**: `host-input type "中文输入abc"` (a keycode-0 event carrying text, the shape an IME's commit
has) arrives as `aaaaaaa` - the server translates the keycode and drops the event's text; whether a
real Mac IME commit is affected needs a hand test (the `InputMethodEvent` path exists and was added for
this report). Three app-side behaviours were reproduced **identically on Xvfb** and are therefore not
this server's: termora's context-menu items ignore synthetic clicks, EasyShell's own title-bar items
do nothing, and termora's empty Quick Connect shows its own `getScheme(...) must not be null`.

**2026-10-08 (A53's finding, fixed: "中文输入实际上都是英文字符"):** the user's follow-up settled what the
A53 pass had only suspected - typing with the Mac's own **Pinyin** source into a forwarded text field
produced **English letters and no `input method committed` line** - and it was three bugs in a row.
(1) The scene was never an input method client: JavaFX enables input method events only when the
scene's *focus owner* carries both `getInputMethodRequests()` and `getOnInputMethodTextChanged()`
(`Scene.InputMethodStateManager` asks exactly that), and this window had no focus owner while the
handler sat on the Scene. The content now owns both - and the scene may **not** also have a handler,
because the event is fired at the focus owner and bubbles, so two handlers type every character
twice. (2) The manager re-decides only on a focus *change*, and the content was focused while the
window was still coming up, so it composed on some runs and not others: the content is now
non-traversable until the window has the focus, and every window focus takes it while the focus is
parked on a hidden node first. (3) The composed character went on **keycode 255**, which a real
toolkit cannot resolve - the mapping answered the character and the application still showed nothing;
the claim is now the **lowest** keycode nothing carries, which is what `xdotool` does too (keycode 8
in the server's own log). A key event that carries text but no usable keycode no longer types the
keycode either. **Verified end to end**: `n i h a o` + space with Pinyin into a forwarded EasyShell
text field produced **你好**; real keycodes `a b c 1 2 3` with the ABC source produced `abc123`.
**494 tests green** (`TypedCharacterTest.theCharacterGoesToTheLowestKeycodeNothingCarries` fails on
the old claim).

**2026-10-09 (the same path, two more things the user found):** "输入框应当显示中间字符…按下空格后提交内容…这时候
直接回车…输入框应当是aaaaa". The platform's own reports were logged first: `nihao` arrives as
`composed='n' … 'ni hao'` then `committed='你好'`, and `aaaaa` as `'a' … 'a a a a a'` then, on
**Return**, `committed='aaaaa'` - exactly the two shapes asked for, and neither reached the field.
Fixed by (1) **typing the pre-edit into the field and taking it back when it changes**: each
`InputMethodEvent` deletes what is on screen (`preEditLength` BackSpaces) and puts the new state
down - the composed text while composing, the committed text once it commits; that is what a local
input method does to a field. (2) **a keycode per composed character**: one keycode re-assigned per
character is how 你好 arrived as **好好** (a client resolves a keycode through the mapping it was last
told - 25 ms and 150 ms apart alike, so no delay fixes it); `Keymap.claimKeycodeFor` gives each
character its own (8, then 93) and keeps it. Characters the keyboard carries go on their own key, with
Shift when needed. Verified: `ni hao` → 你好, `a a a a a` → Return → `aaaaa`, and `abc123` still
works. 494 tests green.

**2026-10-13 (A54, "不指定合适的geometry时 easyshell 左右都被裁剪"):** nothing was wrong with the
renderer or with the frame - the **client** asks for a window the screen cannot hold and then centres
it there. EasyShell's window is a fixed **1300x700** and it centres itself on the screen it was told
about, so on the default 1280x800 screen it lands at **`-10+33`**: ten pixels off each side, which the
frame showed as a crop of exactly 1280 columns. The VM's own **`Xvfb :98` at 1280x800** puts the same
window in the same place, so this is what a reference does too - and **xclock and xeyes simply fit**,
which is why only EasyShell was reported. `--geometry 1300x800` worked around it by making the screen
as wide as the window. Fixed by `XServer.fitContentOnScreen` (in the tick, before the frame's
rectangle): a mapped window that starts off the left or the top is **moved onto the screen**
(`WindowHandlers.moveWindow`, an ordinary ConfigureNotify - nothing can show a negative-position
pixel) and the screen **grows to the window's far edge** through the same `growScreen` the drag path
uses, capped at `--max-screen`. Measured on the real machine in both window modes with no geometry:
`moving a 1300x700 window of display :61 from -10,33 to 0,33`, `growing the screen of display :61 to
1300x800`, then `showing Rectangle[x=0,y=33,width=1300,height=700]` on a 1300x730 frame - EasyShell
whole, and a 164x164 xclock untouched at `+0+0` with no growth. `HostWindowTest`'s four window cases
asserted the old crop (`Rectangle[0,20,1250,700]`) and now assert the whole window; `ScreenResizeTest`
gained four tests (off the left, taller than the screen, past `--max-screen`, and the client being
told). **504 tests green.**

**2026-10-13 (A55, "关闭窗口后鼠标也跟随消失了"):** the frame went and the desktop was left with no
pointer. The platform pointer is hidden as a **loan** (`Cursor.NONE` is a process-wide `[NSCursor
hide]` on macOS), and handing it back is **not a native call in the same turn**: the scene's cursor is
applied from its own pass inside a pulse and `ViewScene.setCursor` posts the native part through
`Application.invokeLater` behind an `if (platformView != null)` guard. A window closed in the same turn
as the release therefore wins the race, the posted task no-ops against a disposed scene, and the hide
stays - `MacCursor.isNSCursorVisible` measured **false** a minute after the window had gone, and only
activating another application brought the arrow back. Fixed with `JavaFxBackend.handBackPointerThenClose`
(one path, used by both `stop()` and `stage.setOnCloseRequest`): hand the pointer back, then close the
window **two pulses later** (an `AnimationTimer` counts them), and `consume()` the platform's close
request so the close is this server's to finish - `isClosed()` is still set in the same turn, so
`XServer.onWindowClosed` and everything behind it (per-client, single-window, `--detach-on-close`) is
unchanged. `LIVE_WINDOWS` is decremented at the decision rather than in the timer, so a toolkit that is
exiting cannot leak it. Verified on the real machine with the toolkit's own record of the hide
(`--display 61`, xclock forwarded from 192.168.22.152, pointer parked inside the client's page so the
arrow was hidden): the arrow came back both when the **client exited by itself** (the reported case)
and when the **frame's close button** was clicked, `CGCursorIsVisible=1` each time, and the next window
opened at the same `116,66`, the counter back at zero. `JavaFxBackendTest.presentsFramebufferToImage`
now pins the half a test can see - right after `stop()` the window is still there (`windowStillOpen()`),
and it is taken down once the release has landed - which fails against the old same-turn close. **504
tests green.** See also [[javafx-cursor-none-hides-platform-pointer]].
