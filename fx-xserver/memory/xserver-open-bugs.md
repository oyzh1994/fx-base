---
name: xserver-open-bugs
description: Rendering bugs still open in the xserver repo after the Expose-coordinate fix
metadata: 
  node_type: memory
  type: project
  originSessionId: 20e98c73-c9b5-4cc8-9d2e-466cca178651
  modified: 2026-09-23T18:59:59.528Z
---

Fixed and verified in this repo (uncommitted as of 2026-09-22): `WindowHandlers.deliverExpose()` sent Expose events with **screen** coordinates instead of window-relative ones (a window at screen (102,602) was told to repaint from `(102,602)` inside a 200x198 window), which makes any client that clips its repaint to the exposed rectangle draw nothing — a black window that never paints. Proof: `xev -geometry 200x200+100+600` before `(102,602)`, after `(0,0)`. Also added a `-scene-N.png` dump next to the existing `-image`/`-buffer` dumps in `JavaFxBackend`.

Fixed later the same day (committed by the user as `5a4d856` / `4a7617c` / the obscuring change):

- **Cursor save/restore was stale**: `InputState` painted the sprite into the framebuffer and wrote the saved pixels back unconditionally, so a client that drew inside the sprite's rectangle had its work reverted by the next pointer move (a trail of patches). The sprite is now handed to the display backend as a `CursorSprite` and drawn over the presented frame, never into the framebuffer; the ARGB32 alpha that a JavaFX cursor needs is preserved in `VisualInfo.toArgb`/`ImageCodec`; and the host window hides the platform pointer so there is only one pointer.

  That last part first used `scene.setCursor(Cursor.NONE)`, which **was itself a bug**: on macOS NONE is a process-wide `[NSCursor hide]`, so the desktop lost its pointer once the pointer left the server window. Replaced on 2026-09-22 with a transparent `ImageCursor` plus an enter/exit swap - see [[javafx-cursor-none-hides-platform-pointer]].
- **Obscuring is now tracked**: `XWindow.visibleRegion()` (own rectangle minus the siblings above it at every level *and* its own children), a region-aware `Rasterizer`, `Picture` regions, and `visibilityChanged`/`repaintUnder` in `WindowHandlers` which repaint and `Expose` the area a window stops covering. `XWindow.clipRect()` still only intersects ancestors - the region is the new API.

Fixed on 2026-09-22, found with real clients off the VM (`xclock` black, `xeyes`/`xcalc` blank until the
pointer was dragged across them, and `xcalc`'s button labels missing even then):

- **A window mapped before its parent was never sent an Expose.** `WindowHandlers.mapWindow` snapshots
  the window's visible region, maps it, then re-maps its mapped children - but it read each *child's*
  region at that point, by which time the parent was already mapped and the child's region therefore
  looked unchanged. Nothing gained means no Expose, and a toolkit maps its children first and draws
  itself from the Expose the parent's map owes it, so the window stayed black. `mapWindow` now carries a
  `newlyVisible` flag down the recursion (the entry always passes true) so every descendant of a window
  that just became viewable is told its whole area is new. Verified against a real Xlib client: the
  server's request log had **zero** Expose events before and `xcalc`/`xclock`/`xeyes` render fully after;
  `ExposeOnMapTest` drives a real connection (child mapped, then parent) and fails without the fix.

Fixed on 2026-09-23 (**uncommitted**), the reason a right-click menu's submenu was drawn but dead:
**no crossing events were delivered while a pointer grab was held** (`InputState.movePointer` skipped
`deliverCrossing` whenever `pointerGrab != null`). EnterNotify is the only way a toolkit learns
which of its own windows the pointer is in, so the submenu never highlighted and a press in it was
read as a click outside the parent menu, which dismissed the menu. What is now implemented, each
rule measured against Xvfb with a raw-protocol probe (`/tmp/Probe.java`, `/tmp/MenuProbe.java`):
crossings are delivered while grabbed but confined to the grab window's subtree when the grab has
`owner_events` false (`EventDispatcher.deliverCrossing(..., inside)`), an `owner_events` grab
confines nothing, and an explicit grab supersedes the implicit one for event routing
(`InputState.grabbedWindow`), which is what makes the release of a menu that appeared under a held
button land on the popup. Two behaviours of a real server are deliberately **not** implemented
because they were measured to tear EasyShell's menus down within microseconds of opening (3 runs
out of 3 against 1 in 3 for the same scripted gesture without them): the crossing **pair** a grab
reports at its start and end (mode NotifyGrab/NotifyUngrab), and reporting a window that appears
under a stationary pointer at the moment it is mapped. Both are written up in
`docs/design-notes.md`; `GrabCrossingTest` pins the two rules that are implemented.

Still open, deliberately not changed:

**A popup that appears under the pointer was never entered (fixed 2026-09-23, end of the third
session).** The deviation the design notes record as deliberate - "a window that appears under a
stationary pointer is reported as entered by the move that follows, not by the map" - left termora's
override-redirect popups (measured: `Override Redirect State: yes`) **never entered at all**, because
the press that opens one holds the implicit grab, which withholds the enter, and nothing delivered it
afterwards. The app therefore never knew the pointer was inside its menu: nothing highlighted and a
click in it was read as a click outside. A reference server withholds the enter only *until the button
comes up*, which is now what happens here: `WindowHandlers.mapWindow` reports the crossing at the map,
`InputState` remembers what the client was *told* (`announcedWindow`, separate from `pointerWindow`,
which button events update silently) and `settlePointerWindow` delivers the owed enter when the
implicit grab ends. **The reason it was left out originally no longer holds**: with the focus work in
place, EasyShell survives the out-and-in gesture 3/3 and opens as before, where the same change tore
its menus down 3/3 before. Not verified against Xvfb for the popup gesture - worth doing before
trusting it further.

**SOLVED 2026-09-24 (uncommitted): "the close button needs two clicks" was a ghost, not a click.**
The click worked: termora's settings dialog sent `UnmapWindow` on the *first* click and its window
really went away. What stayed was its **picture** - the area it covered was never repainted, so the
user went on aiming at the dialog they could still see, and the second click (whose hover made the
client repaint) is what made it disappear. Root cause: the region handed back when a window goes is
`window.visibleRegion()`, which subtracts the window's **own children** - and an AWT toplevel is a
frame whose content window covers it completely, so that region is *empty* and nothing was restored.
`XWindow.coveredRegion()` (own rect + border + children, minus what is above) is now what
`WindowHandlers` uses for `unmapWindow`, `applyConfigure` and `circulateSubwindows`;
`WindowObscuringTest.unmappingAFrameGivesBackTheAreaItsContentWindowCovered` pins it. Also explains the
same two-click report for a dialog's 确定 and for a popup dismissed by clicking outside it.

**How it was found - the log told us, and the lesson is about *what* to log**: with the user's own
clicks (their server, their hand) the trace showed `button 1 down at (957,141) -> 0x200032` for the
first click and `-> 0x200021` for the second - i.e. the dialog was already gone by the second click,
which meant the click was never the problem. What settled it was dumping the **framebuffer** after a
scripted single click (`-Dxserver.dumpFrame`, or `xwd -root` on a `--single-window` server) and seeing
that the closed dialog's pixels were still there, byte for byte. Two useful diagnostics came out of
this hunt and are kept: `JavaFxBackend.hostEvent` logs every press/release the host window itself
receives (before anything is done with it), which separates "the platform swallowed the click" from
"the client ignored it"; and `DrawHandlers`' `PutImage landed: ... values=` prints the distinct bytes a
small image put down.

**SOLVED 2026-09-23 (uncommitted): termora's "一团灰色" dialogs are a picture drawing at a stale
position.** `Picture` cached the window's position when the picture was created; AWT opens a dialog at
the origin of the screen and *then* configures it where it belongs, so the 消息 box stood at (497,335)
with a picture that still thought it was at (0,0). The same stale origin is what the window's clip is
tested against, so it is not a misplaced draw but a refused one - all 39650 pixels of it, which
`Compositor`'s diagnostic now prints (`0 pixels written, 39650 blocked; picture origin (0,0), window at
(497,335)`). The background came from `ClearArea`, which works in absolute coordinates, so the box
appeared with a hole exactly where its contents belonged. `Picture.refreshClip()` re-reads the position
along with the clip; `PictureOriginTest` pins write and read. The earlier hypothesis in this file
(override-redirect dialog clipped by `contentBounds`) was wrong. **Note the same diagnostic is how it
was found: a composite onto a window that is refused entirely is otherwise invisible in the log.** The
client-side facts that made the reproduction possible - termora is Swing, it runs on the VM, and XTEST
is scoped to the caller's own screen - are in [[xserver-testbed]].

**How close the output is to a real X server (measured 2026-09-23).** Shooting the same application
on both servers and diffing: **99.5% of the window identical** (907497 of 912000 pixels); 2745 differ
by one unit in a channel, 343 by 2..16, 1415 by more. The one-unit class is **blend rounding - ours
rounds, Xorg's fixed-point composition truncates** - and is deliberately kept, since neither is more
correct and it is invisible. The 1415 larger ones sit **only in the top ~50 rows** (title bar and its
icons), are not a shift (a +/-2 pixel offset scan finds nothing better), and are *not* explained: the
composites that draw them are 1:1 masked `Over` with the nearest filter, and the mask's coverage was
confirmed in the low byte by the server's own `PutImage landed` diagnostic. Next step if it matters:
diff the mask pixmap itself against a reference render.

**The user's "非常糊" was the Retina display, not the rendering.** The Mac is a 3024x1964 Retina panel
and JavaFX reports `scale 2.0x2.0`: a 1x framebuffer drawn through a canvas is enlarged 2x by macOS,
so the window is soft however correct the pixels are. Fixed by the `--scale` option below.

**Fixed 2026-09-23 (third session): an A8 mask was read as an opaque colour.** termora's toolbar
icons and filled paths arrived as solid grey blocks; the same AppImage on Xvfb drew them correctly,
so it was ours. RENDER masks are **depth 8 alpha-only** pictures (the log now prints it:
`mask=pixmap(...)(depth 8 alpha-only)`, which is how it was identified) and a drawable's pixels are
held as `0xAARRGGBB`, where such a mask's *value* is its coverage - reading it as a colour channel
with an opaque alpha made every mask pixel fully covered, so everything composited through a mask was
painted as a solid rectangle of the source colour. `Picture.stored` now decodes through the format
when it `isAlphaOnly()` (the helper `PictFormat.toArgb` already existed and the glyph path already
used it, which is why text was never affected). `AlphaMaskTest` pins it, verified failing without the
fix. **SOLVED 2026-09-23 (fourth session, uncommitted): the "bolder/jagged icons" were depth 8 pixels read
as 3-3-2 colours.** `ImageCodec` converted a depth 8 wire pixel through the drawable's *visual* on the
way into the store, and this server offers a depth 8 visual of the usual 3-3-2 layout, so a coverage
byte came back as the blue field of its own expansion - coverage modulo four, i.e. 0x00/0x55/0xaa/0xff
and nothing between. The client had been sending smooth coverage all along (the masks are PutImage'd);
we threw six of the eight bits away. Measured: termora's toolbar icons had exactly three colours
between the icon colour and the background where Xvfb's - the same client, the same requests - had
twenty. Fixed by storing the byte (`ImageCodec.argb`/`packed`, and `Picture.stored` decodes a depth 8
drawable through its format's masks, so a colour format still shows colours);
`ImageCodecTest.depthEightPixelsKeepAllEightBitsOfTheirValue` pins it and fails without the fix. **The
toolbar strip now differs from Xvfb's by a worst channel delta of 2** (936 of 9280 pixels, all one
unit). The old hypothesis here - "the app chooses a different rasterisation path because our RENDER
advertisement differs" - was wrong: the client's requests were identical.

**Measurement trap, cost an hour**: two captures of the same application in *different UI states* look
like a large rendering difference. The pointer being parked over the window in one run and not the
other lights up hover states (the sidebar row drew blue in one capture and not the other), and a
*different root colour* - ours is #E8E8E8, Xvfb's is black - shows up as hundreds of large-delta
pixels along the window edge. Park the pointer in the same place and check for a shift before
believing any of it.

**The focus gap has client-side evidence now (2026-09-23, second session).** EasyShell **selects
`FocusChangeMask`** (mask `0x6ba77f` on its main window, bit 21 set) and calls **`SetInputFocus` once**,
at startup when its main window is mapped - and the server sent **zero** `FocusIn`/`FocusOut` in the
whole session. So a client that asks for the focus and explicitly subscribes to being told is never
told; JavaFX's rule is that a popup is focused only while it is visible *and* its owner window is
focused, and a window's focused state comes from glass receiving `FocusIn`/`FocusOut`. What the user
saw from this: a `ContextMenu` opened from a `Canvas` "couldn't get focus and couldn't be clicked".
They resolved it **app-side** by showing the menu from `setOnContextMenuRequested` - which is exactly
what `Control` does internally (`Control.contextMenuHandler` does
`c.getContextMenu().show(c, event.getScreenX(), event.getScreenY()); event.consume();`), so they gave
the non-Control `Canvas` the framework's own path. Their remaining "only the first time" failure was
**their own initialisation order** - the menu object was not built yet when the first event arrived;
initialising it up front fixed it. Nothing to do with the server.

**IMPLEMENTED 2026-09-23** (third session, uncommitted): `EventDispatcher.deliverFocusChange`, called
from `InputState.setInputFocus` and from the revert when the focused window is destroyed. FocusOut to
the window losing the focus and FocusIn to the window gaining it, to whoever selected
`FocusChangeMask` (one bit covers both events), with the **crossing detail lattice reused**
(`crossingDetail`) plus `NotifyPointerRoot` (6) and `NotifyDetailNone` (7). Verified against the real
client: its startup `SetInputFocus` now produces a `FocusInEvent` where the log previously showed
none, and the app still renders and behaves the same. `FocusEventTest` (5 tests) pins the details,
the no-change case, and the deliberate fold of `None` into `PointerRoot`.

**Still missing** (recorded in the design notes): the focus does not follow the pointer while it is on
`PointerRoot` (keys follow it, no events are generated), and a focus change made by a keyboard grab is
reported as `NotifyNormal` rather than Grab/Ungrab.

Also measured and worth not re-deriving: the **first and later openings of the same context menu are
protocol-identical** (same CreateWindow x2 / MapWindow / ConfigureWindow / MapWindow / GrabPointer
sequence, all grabs `granted`, the popup's X windows recreated every time rather than reused, and the
button release delivered to the popup both times). So any "the first time behaves differently"
symptom is not in this server's event stream.

0. **Passive grabs are recorded and never matched**: `GrabButton`/`GrabKey` land in
   `XServer.passiveGrabs` and nothing reads that list for matching, so a window manager or a
   popup-shortcut arming one never hears about the button or key. **`SetInputFocus` generates no
   `FocusIn`/`FocusOut`** either, so a client that waits to be told it has the focus (a dialog, a
   keyboard-opened menu) is never told, while keys are delivered to the focused window. Both were
   noticed while chasing the menu bug and left alone.

1. **SHAPE's bounding shape does not clip drawing** (only the input shape is honoured), and the visible region does not consult it, so a shaped window's corners are still painted.
2. **`XCursor.fromGlyph` approximates font cursors as a filled cell**, so a client that uses `XCreateFontCursor` (xterm, xclock) draws a solid rectangle under the pointer instead of the standard arrow. Fixing it means embedding the cursor font's glyph bitmaps.
3. **`xdotool` segfaults against this server** (`getmouselocation` aborts); a hand-written raw-protocol client does the same QueryPointer/WarpPointer calls fine, so something in a reply is malformed enough to make Xlib misread it.

Not a bug, seen while testing and worth remembering: a one-frame 100%-black screen can be a client's own `ClearArea` with an unset `background_pixel` (xclock does it on startup), and `xserver --display N` on a display whose unix socket/TCP port is taken exits loudly with `BindException: Address already in use` ("failed to start") rather than half-listening.

Fixed 2026-09-23 (**uncommitted**), the "context menu closes as soon as the pointer is moved"
report: `JavaFxBackend` mapped *scene* coordinates to X screen coordinates one for one, ignoring that
the window shows the viewport sub-rectangle of the screen - `SwingBackend` had always added the
origin, so the JavaFX path (the one that is actually used) was the odd one out. Measured for
EasyShell in per-client mode on a 1200x760 screen: its toplevel lands at (-50,20), the viewport comes
out as `x=0,y=20`, so every click and motion was reported 20 pixels above where it happened; with the
crossing events now delivered (see above), the client is told the pointer left the menu 20px before
it does, and the menu closes. `JavaFxBackend.screenCoordinate` is now the single conversion, with a
unit test in `JavaFxBackendTest`.

Round two on the same area (2026-09-23 evening, still uncommitted). The viewport input mapping is now
**verified end-to-end**, not just reasoned: with a real JavaFX window in per-client mode the server
logged `showing java.awt.Rectangle[x=0,y=20,...]`, a physical click at window point (100,100) was
reported as `(100,120)` after the fix and as `(100,100)` before - and the user confirmed the cursor no
longer sits 20px above the pointer. Three more rules were added, each measured the same way (raw
protocol probes against Xvfb, byte-for-byte): an event for a window that is not the grabbing client's
goes to the grab window (a motion off a menu was previously dropped); the focus bit of a crossing is
set when the focus window is the event window or inside it, with PointerRoot meaning "the window the
pointer is in"; and a crossing between the grab window and a window outside it is reported as
`NotifyAncestor`. The implicit grab a press starts confines crossings while a button is down, which
is why a popup mapped under the pointer mid-press is not entered until the button comes up.

**2026-09-23, second session - the dismissal is the client reacting to the crossing itself.** A
reproducible harness (`/tmp/menu-cycle.sh` starts the headless server on :92 plus EasyShell on the
VM, `/tmp/menu-check.sh` right-clicks and reports whether the client unmaps the popup,
`/tmp/menu-outin.sh` adds the leave-and-re-enter gesture) shows the client sending `UngrabPointer`
and unmapping the popup **1 ms** after the `EnterNotify` that reports the pointer back inside the
menu, with no request in between. So *every* crossing that reports a popup "entered" while the
pointer has not moved costs the menu: measured 3/3 torn down. The uncommitted batch in the working
tree at the start of that session (`EventDispatcher.deliverGrabCrossing` + `InputState.announceGrab`
/`announceUngrab`, saved as `/tmp/regression-batch-announceGrabUngrab.patch`) re-added the grab
crossing pair the notes above say is deliberately left out, on **every** ungrab - and a menu grabs
and ungrabs repeatedly while open - so the menu was torn down 3/3 at open where the committed server
kept it up 3/3. That batch was reverted; the tree went back to 92/92 green.

**SOLVED 2026-09-23 evening - and it was the focus.** The dismissal above is gone in the scripted
harness (6/6 survive, hovered item highlighting; it was 3/3 dismissed before), and the missing
`FocusIn`/`FocusOut` was the cause. Two things had to be true, and the second is what took the time:
the client has to be told the focus at all, **and the window the pointer is already inside must still
be told** - an early version treated "the window taking the focus is the one the pointer is in" as no
change and sent nothing, which put the menu straight back to being dismissed. `FocusIn`/`FocusOut` is
now modelled on a reference server (`/tmp/FocusProbe.java` diffed against Xvfb: Xvfb run with
`-listen tcp -ac` on the VM, display :98): the whole chain of windows on each side, innermost-first
out and outermost-first in, `NotifyVirtual`/`NotifyNonlinearVirtual` for the windows in between,
`NotifyPointer` for the chain under PointerRoot, `None` as a real third state, and the focus reverting
out of a window (or a window holding it) that stops being viewable, per `revert_to`. 16 of 18 probe
steps match byte for byte; the two that do not are a corner where a failed `RevertToParent` falls
through to PointerRoot, recorded in the design notes.

**Historical, for the trail**: a first-level menu that is left and re-entered was dismissed by EasyShell
(`right-click -> move out -> move back in` closes it every time here, never against Xvfb). The
crossings involved match the reference byte for byte - window, detail, mode, state, coordinates,
focus bit, order - so the client is reacting to something that is not in the crossing, and the hunt
stopped there. Ruled out by trial: suppressing the crossing that leaves the grab window keeps the
menu up but makes a submenu inert again (that crossing is what makes a menu "the window the pointer
is in"); suppressing only its Leave half keeps the submenu working and does not help; suppressing the
pair at the map is what kills the menu at open time. The next thing worth doing is getting this
client's *own* event stream off Xvfb - `/tmp/XProxy.java` is a byte-level X11 proxy built for exactly
that and it works for Xlib clients (`xwininfo` through it is fine) but EasyShell stops after its
first `QueryExtension` through it, for a reason never found.

Harness warning for whoever picks this up: scripted clicks have real effects on EasyShell (one of
them opened an SSH session in the test instance), so the app's state drifts during a measurement run
and menu coordinates from an earlier run go stale - restart the app (`/tmp/cycle92.sh`) before
trusting a coordinate-based check.

**2026-10-13, real machine (xclock/xeyes from the VM 192.168.3.21 forwarded to display :5).** Two
bugs were reproduced and fixed, and both are the "the window shrank / went black / would not come
back" family the report named.

*Fixed*: **stretching xclock showed a black band, and after a restore the clock was drawn only in
its top-left corner.** The Expose a resize owes is the *whole* of the new window, not the strip the
window gained. Measured against the reference (Xvfb) with a raw client: a 600x500 window grown to
800x600, a width-only growth, a height-only growth and a shrink to 400x400 are each sent **one**
Expose covering the whole new window with count 0 - never the gained strip alone.
`WindowHandlers.visibilityChanged` delivered `gained`; it now takes a `resized` flag
(`applyConfigure` passes it, the gravity path and the map path pass false) and delivers `after`.
`xclock` (`XtExposeCompressMaximal`) frees its backing pixmap on a resize, makes a new one at the new
size and paints into it *exactly the area its Exposes name*, so the strip alone left the rest of a
stretched window undefined - the black band down the side, and the half-drawn clock a restore came
back to. `xeyes` was never affected for the same reason it "looked fine": it is not xclock's
free-the-pixmap shape, so a strip-only Expose is enough for it. Verified on the real machine: a
stretched xclock renders its whole face, and after four maximize/restore cycles it comes back fully
drawn.

*Fixed*: **after a few maximize/restore cycles - especially after stretching - the window suddenly
got bigger and never came back to its size.** The backend sized the frame on every flushed frame
(`fitWindowToViewport`/`sizeFrameToContent`, and `setViewport`) even while the host window was
**maximized**. Setting a size on a window the platform has maximized *un-maximizes* it on Windows,
and the size it is left at - the content plus the frame, not the work area - becomes the one the
platform then restores to; the client is never told anything, because the frame it is drawn in did
not change size. Control experiment (old behaviour, forced frames while maximized): the client's
window shrank every second, 3413x1764 -> 3413x1749 -> 3413x1735 -> 3413x1721. `platformOwnsFrame`
now includes `stage.isMaximized()` (the rule is the static `platformOwnsFrame(fullScreen, maximized,
dragging)`, pinned by a test), and `sizeFrameToContent`/`enforceWindowSize` return early while
maximized. Verified: stretch 1900x1050, then four maximize/restore cycles with forced frames,
restore returns to exactly 1901x1050 every time, no shrink and no growth; the maximized xclock
renders its whole face.

*Still open, recorded and deliberately not changed*: **the first stretch that has to grow the X
screen can revert.** With xclock at 1000x700, a `MoveWindow` to 1900x1050 - which needs the screen
grown from 2560x1600 to 3772x2025 - snapped the window back to ~1001x700, three attempts running in
the same session; the *same* call after the screen had already grown took the size (1901x1050)
first time, and every stretch that fits the screen (1400x900, 1650x1000, 1500x1050) always took.
The log shows `growing the screen of display :5 to 3772x2025` and then `window now shows
...1971x1325` - the *pre-stretch* client window - followed by a 1px oscillation (1971/1972), i.e.
the frame being settled back to the viewport the server had before the stretch. The likely cause is
that the first growth (a framebuffer reallocation) outlives the 1000ms drag deadline, so the
viewport the server pushes afterwards is applied when the backend is no longer "drag in flight" and
`sizeFrameToContent` pulls the frame down to the stale, pre-stretch viewport. Left alone: the exact
trigger is not pinned down, it reproduces only on the first growth, and the drag/settle invariants
around it are delicate. Recipe: a fresh server, one xclock, one resize past the screen edge.

*Also seen once, not reproduced (recorded, not fixed)*: in one run with **xeyes** as a second client
(the X screen already grown to 3772x2025 by the stretches above), three maximize/restore cycles in a
row left the window at the *maximized* size after every restore - `rect` read 1721x919 with
`zoom=False` where the pre-maximize size was 700x500, the "window suddenly got bigger" symptom -
and the log shows the restore's settle reporting the maximized content (`platform gesture settled at
3412x1764`) instead of the restored one. Rendering stayed correct (both eyes drawn), only the
geometry was wrong, and 919 against the platform's usual 927 for that work area is the tell that
some size was put on the frame while maximized. Four cycles on a *fresh* server first thing, and
four more on a fresh server with the screen grown first, both restored to 700x500 every time, so it
is state-dependent and not reproducible on demand. xclock never showed it in any run.


**2026-10-13, real machine (xclock forwarded to display :5), third round.** The report this time was
three things: Windows F11 "seems to do nothing"; a stretch, then maximize, then restore that comes
back at the wrong size *and* in the wrong place, every time; and (once) a window that suddenly grew
after a few maximize/restore cycles.

*Fixed*: **stretch, then maximize, then restore returned the wrong size and the wrong position.**
Two mechanisms write the frame while the platform is between "not maximized" and "maximized", and
both leave behind the rectangle the platform later restores to (setting a size on a maximized window
*un-maximizes* it on Windows):

- **the restore gap** - the header bar's own restore control flips `maximized` to false *before* the
  platform has put the window back, and the frame that runs in between sizes it to the viewport that
  is still the maximized one. Covered by the gesture deadline (`platformGestureUntil`), which
  `platformOwnsFrame` now reads.
- **the zoom-in gap** - Windows moves and resizes the window to the work area *before* it reports
  `maximized = true` (~146 ms measured). For that moment every flag says the window is an ordinary
  one, nothing else marks the zoom, and the size written into the frame then is the size the
  platform ever after restores to. `platformOwnsFrame` therefore also takes
  `zoomedByPlatform()`: not full screen and not maximized, but positioned past the top-left of the
  usable area and no narrower than it - a frame only the platform's own zoom puts there (macOS zooms
  exactly to the usable area, so the origin test is false there and the rule is inert).

  `platformOwnsFrame` is now the static five-argument
  `platformOwnsFrame(fullScreen, maximized, dragging, platformGesture, zoomedGeometry)`. Verified on
  the real machine, DPI-aware measurements: fresh server, one xclock, stretch the bottom-right corner
  +500,+350 (747x631 frame at 357,104), maximize, restore: **five cycles, every restore exactly
  `357,104 747x631`**, every maximized frame `-11,-11 2582x1390`. A pre-fix build was poisoned to
  `-7,-7 499x421` (wrong position *and* size) by the same recipe; that is the state the zoom-in rule
  prevents. The poisoning is timing-dependent (it did not re-trigger on the fixed build), so the rule
  is pinned by the mechanism and by the test rather than by an A/B on one run.

*Fixed*: **F11 did nothing.** Two halves, both needed, and the second is why the first alone was not
enough:

- **The platform's full screen key was the way out and never the way in.** JavaFX's
  `setFullScreenExitKeyCombination` only *leaves* full screen. The scene key filter now reads the
  same key (F11 here, `Ctrl+Meta+F` on macOS) and, when the window is not full screen, enters it -
  taken rather than forwarded, exactly as it already is while the window is full screen.
  JavaFX's own exit handler runs **first and does not consume the event**, so a press on a full
  screen window arrives with the window *already* out of full screen: without a guard the second
  half of that one press put it straight back in (measured: `false -> true -> false -> true` from a
  single press). The state change now stamps `fullScreenChangedAt`, and a press within
  `FULL_SCREEN_PRESS_MILLIS` (500) of it is the same press and is not answered again. Verified: one
  F11 in - `fullScreen false -> true`, frame `0,0 1707x960`; one F11 out - `true -> false`, frame
  back to `206,37 165x187`.
- **The root advertised nothing, so a toolkit never asked.** The server honours
  `_NET_WM_STATE_FULLSCREEN` (a `ClientMessage` to the root, `AtomHandlers.applyWindowStateMessage`)
  but published neither `_NET_SUPPORTED` nor `_NET_SUPPORTING_WM_CHECK`. GDK's
  `gdk_x11_window_set_fullscreen` returns without sending anything unless the root's
  `_NET_SUPPORTED` lists the state, and AWT asks for `_NET_SUPPORTING_WM_CHECK` before it will use
  EWMH at all. A missing state is not refused, it is never *asked for* - measured: `eog` forwarded
  to :5, F11 pressed, no request arrived at all. `XServer.publishWindowManagerHints(Screen)` now
  publishes, on **every** screen's root (a client asks its own root in the default one-screen-per-
  client mode, as `_XKB_RULES_NAMES` already does): `_NET_SUPPORTED` =
  { `_NET_WM_STATE_FULLSCREEN`, `_NET_WM_STATE` }, and a 1x1 unmapped child of that root carrying
  `_NET_SUPPORTING_WM_CHECK` pointing at itself and `_NET_WM_NAME` = "xserver", with the root
  pointing at it. The maximised states are deliberately *not* advertised: a client that found them
  would stop resizing itself for a maximised window and wait for a manager that does not act on
  them.

  Two things found while wiring it: `AtomTable.intern(name, true)` is the **only-if-exists**
  overload and answers none for atoms no client has interned yet, so the method published nothing at
  startup until it interned them properly (measured: `xprop -root` showed no `_NET_SUPPORTED` even
  though the code ran); and the manager window is made per screen (one per client connection), so it
  is now freed in `closeClientScreen` with that screen's root. Verified with a raw client on the VM:
  root `_NET_SUPPORTED` = `_NET_WM_STATE_FULLSCREEN, _NET_WM_STATE`; the check window is a child of
  the root and points at itself; and a `_NET_WM_STATE` add/remove `ClientMessage` - byte for byte
  what GTK sends - takes the host window to `0,0 1707x960` full screen and back to its exact size.
  Pinned by `WindowManagerHintsTest` (new) and by the updated `PerClientWindowTest`, which now
  allows the display's own check window among a root's children.

*Recorded, not changed*: `eog` could not serve as the end-to-end F11 client on this VM - it starts
and logs nothing but never maps a window, so the F11/EWMH path was exercised with the raw client
above instead. That looks like the test VM (dbus/session), not the server.

*Recorded 2026-10-14, on the Windows half of A54* (the fix is A56 in `docs/backlog.md`; this is what
was measured and what was found **not** to be a bug, so a later reader does not re-chase it):

- **The clip is the platform's, and it is a display *size* question, not a client one.** The X side
  is right after A54 - measured, EasyShell's `2600x1400` framebuffer window was moved from `-20,67`
  to `0,67` and the screen grew to `2600x1600`, so nothing inside the picture is lost. What goes
  missing is the host frame itself: `JavaFxBackend` sizes it one point per framebuffer pixel over
  `scale`, and **Windows clips a top-level window larger than the display's usable area** (macOS
  draws it past the edge and lets the user move it, which is why the same server looked whole
  there). Measured on the 2560x1440-at-150% host: `xclock -geometry 3600x1200` gave a frame
  `2722x956` physical against a 2560-wide display - the right of the clock face off the screen -
  and EasyShell at a 1280x720 logical work area gave `2622x1471` against 2560x1440.
- **`--scale 1` does not crop EasyShell, and neither does `--scale auto` on this host.** EasyShell's
  fixed `1300x700` window makes a `1314x737` frame, which fits a 1707x912 usable area with room to
  spare; the crop needs a display whose *usable* area is narrower than the frame (a 1280x720 work
  area does it). The `xclock` case above was used to force the same shape on a bigger display, and
  the two behave identically - so this is the size of the frame against the size of the display, not
  a client or a scale setting.
- **The header bar and the platform frame come off the area before anything is scaled.** They are 30
  and ~37 logical points on this host; a window that fits only because it ignores them still comes
  out larger than the display.
- **Maximize and restore were re-checked and are not involved.** With the window at the display's
  own bounds, `2200x1200 <-> -11,-11 2582x1390` and the restore landed back at exactly
  `294,56 2200x1200`; the crop does not depend on either. (See A41's `platformOwnsFrame` entry above
  for the maximize/restore rules themselves.)

*Recorded 2026-10-14, from the A57 work on the display loop (the entry is in `docs/backlog.md`; these are
the things that were measured and found **not** to be the cause, so a later reader does not re-chase them
- and one behaviour that was seen while verifying and is **not** this change's):*

- **The Windows timer is not coarse on this host, so it is not what the loop's 18.7 ms was.** A
  throwaway probe (`Thread.sleep` / `LockSupport.parkNanos`, 200 samples each): `sleep(16)` **16.0-16.6 ms**
  p50, `sleep(2)` 2.0-2.2 ms, `sleep(1)` **1.06 ms**, `parkNanos(200us)` 1.05 ms. The 1 ms floor is real,
  so a loop that asks for a 16.7 ms sleep gets one; the extra 2 ms per tick was the loop's own
  accounting (the sleep started *after* the work) and not the platform's clock.
- **`Platform.requestNextPulse()` after the `runLater` buys nothing here.** Added to `present` it measured
  **6.9 ms** mean damage-to-flush where the same client without it measured **5.0 ms**, on the same 180 Hz
  display - JavaFX already pulses promptly for a frame handed to it, and the call was removed rather than
  kept on the strength of the javadoc. (What was left in its place is the *server-side* half: the loop is
  woken by the damage instead of by the clock.)
- **A programmatic maximize is not the title bar's gesture, and its anomaly is older than this work.**
  `win2.ps1 -Action max|restore` is `ShowWindow(SW_MAXIMIZE)` / `SW_RESTORE`. Three cycles on a forwarded
  EasyShell (host window `1972x1106` at `342,104` on a 2560x1440 display whose usable area is 1707x960
  logical): filled to `-11,-11 2582x1390`; then `zoom=true` with the window left at `952,104` and its own
  size (no fill, no move by the platform); then filled again. The **same three cycles in the same order,
  and the same for the stretch-then-maximize shape** (resize to 2100x1200 first: filled, then left at
  `342,104`, then `824,104` with `zoom=true`, then filled) run against `7088da4` with none of this work in
  it. So the change does not touch it. Whether this is the gesture the user reported ("最大化和取消最大化
  经常出问题") is **not settled**: that repro goes through a real drag, which is `platformOwnsFrame`'s path
  (A38/A39) and needs a real pointer drag to re-measure, not `MoveWindow`. Recorded, not fixed.
- **The display path still draws a frame for damage that changes no pixel.** A client that repaints its
  whole window without changing it (`EasyShell` paints its panel ~300 times a second) marks damage every
  time, and the host window has no equivalent of the VNC path's `sentPixels` comparison (A25), so each of
  those marks now costs a frame at the `FRAMES_PER_PERIOD` limit rather than one per tick. Measured cost of
  that worst case, a client damaging continuously: **2.2% of one core**, against 1.7% with the tick alone -
  bounded, and left as it is; the comparison is what the wire path needed, and the toolkit coalesces the
  frames it is handed.
