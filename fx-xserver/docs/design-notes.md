# Design notes

Why this server is built the way it is. The README describes the shape of the code; this file
records the decisions behind it, the platform behaviour that forced some of them, and what is
deliberately left undone - so that a change does not have to rediscover it.

Everything here was measured on macOS with JavaFX 25 loaded from the classpath, connecting real
clients (`xclock`, `xeyes`, `xcalc`, and a JavaFX application) from a Linux VM over TCP. Where a
statement is a platform fact rather than a preference, that is said.

## One screen per client connection, one window per screen

A connection is given a `Screen` of its own when it connects (`XServer.openClientScreen`, called from
the handshake before the setup reply), so a client's windows live on a screen no other client can
draw into, and the setup reply names that screen's root as the only one the client sees.

This is what makes each forwarded application appear as its own window, and it is deliberately not
the alternative - one native window per X top-level window, as XQuartz does. That model needs every
window's pixels kept separately, because two windows of one client (a dialog over its parent) overlap
and the covered one's content has to survive; this server has one framebuffer per screen, draws
straight into it and keeps no obscured content, so per-window native windows would either lose
dialogs or cost a second rendering model. One screen per client gets the same user-visible result
for the case that matters (one forwarded application = one window) and keeps a client's own dialogs,
menus and popups overlapping correctly inside its window.

**The alternative was built twice, and what settles it is the requirement rather than the code.** The
per-top-level model - one native window per X top level, as XQuartz does - was built on 2026-09-26 and
reverted: a frame showed a rectangle of the **screen**, so with EasyShell's 工具 dialog open a screen
capture of the main window's own area came back filled with the *dialog's* content. What it needed was
pixels of the window's own, which is B5's per-window store; that landed the next day, and the model was
rebuilt on it and worked - the subtree composite (`draw/WindowSubtree`), `mvn test` 361/361, verified
live with the dialog in a host window of its own while the parent's window stayed its own size and
free of the dialog's pixels.

**And it was rejected anyway, which is the part worth keeping** (*2026-09-27*). The requirement is
simpler than the model: **one host window renders one application**. An application is one window on
the host's desktop, whatever it opens inside itself, and a user who has arranged that window does not
want a second one appearing beside it - nor the per-window bookkeeping (position, title, focus, raise)
that a frame per top level multiplies. So the model this server uses is the one described above, and
`docs/backlog.md`'s C8 is recorded as **declined** rather than pending: what it was waiting on was
never only pixels.

The host window shows a **sub-rectangle** of the screen (`DisplayBackend.setViewport`): the union of
the client's mapped top-level windows, so a small application arrives in a small window rather than
in a screen-sized one with black around it. The screen grows when a window is dragged past it - see
"The screen grows with the window" below - and never shrinks; a window larger than the screen is
clipped until the drag that needs the room grows it.

**The cost of it is anything that is infrastructure rather than an application.** An X input method,
a window manager, a clipboard manager and a drag-and-drop source are all X clients that draw windows
of their own, positioned over somebody else's: an input method's pre-edit text and candidate list are
a window placed over the application's caret. Every one of those clients gets a screen of its own
here, so its window is drawn in its own host window - an input method's candidate list appears
*beside* the application rather than over it, and a connection that maps anything at all gets a host
window whether or not anybody would call it an application. The protocol parts survive the split,
because selections, properties and sent events are server-wide rather than per screen; the pixels do
not. `--single-window` is the mode where they can coexist, and it is the mode to run an input method
in - `PerClientWindowTest.aClientThatIsNotAnApplicationGetsAWindowOfItsOwnToo` is what says so.

`--single-window` keeps the older shape - one shared screen, every client on it - which is also the
mode the in-process tests build on.

**A screen's pixels are allocated when something draws into it, not when it is made** (E4). The
screen itself is still created at the handshake - it has to be, the setup reply names its root - but
its framebuffer is a size and a colour until the first write, so a connection that only asks
questions costs no pixels. That matters because this model gives *every* connection a screen: the
kit's probes, `xdpyinfo`, and `xdotool`, which opens a connection per invocation, were each paying
4 MB (16 MB at `--scale 2`) for a screen they never drew into. Measured with twenty `xdpyinfo`
connections against the running server: 202 MB to 241 MB resident with the lazy allocation, and to
304 MB without it.

The other half - releasing a client's framebuffer when its last window goes away - is deliberately
absent. A framebuffer is not only the windows' content: it is the root a client may have painted,
and it is the store every picture over that screen points at, so releasing it while the client is
still connected would either lose the root's pixels or hand the rasteriser an array that is no longer
the screen's. Sharing one framebuffer between screens is absent for the opposite reason: in the
default mode every client's screen shows the whole display at the same origin, so their windows are
at the same coordinates and one store would show each client the other's windows.

## The host window is the client's window frame

Three things make a forwarded application look like a window rather than a picture of one, and all
three are work a window manager would do: the frame's title, its icon, and letting the user resize
the window by dragging the frame.

**The title and icon are the client's own.** `WindowIdentity` reads `_NET_WM_NAME` (UTF-8) or
`WM_NAME` off the client's top-level window, and `_NET_WM_ICON` for the icon; `AtomHandlers` reports
every property write to `XServer.windowPropertyChanged`, which rebuilds the identity only for those
three properties - a toolkit writes properties constantly, and rebuilding the identity for each of
those would copy a 128x128 icon every time. Everything else is the display tick's usual pattern: the
session holds the new identity, the tick notices it is not the one it already applied and calls
`DisplayBackend.setWindowTitle`/`setWindowIcon`. The identity is compared **by reference**, so "a new
value" and "the same value" are told apart without comparing pixel arrays.

Which window is the one the frame is named after: the **largest mapped top-level window**, not the
first. A toolkit's first window is often a 1x1 one it keeps for itself, and override-redirect windows
are left out entirely - a menu has no title of its own, and an application that retitles itself does
not retitle its menus. Measured against EasyShell: `_NET_WM_NAME` arrives as UTF-8 and the Chinese
title survives, and GTK publishes several icon sizes of which the largest under 256 pixels is used.

**Dragging the frame resizes the client's window.** The JavaFX window is resizable, and a change of
its content area that the client has not already been told about is a resize the user made
(`JavaFxBackend.isUserResize`). The backend only records it - it runs on the toolkit's thread, where
taking the server lock would deadlock against the tick waiting on the toolkit - and the tick applies
it: the client's top-level window is resized through the ordinary configure path
(`WindowHandlers.resizeWindow` into `applyConfigure`), so the client gets the same `ConfigureNotify`
a window manager sends, and the area the window stops covering is handed back to what is under it.

**A drag is measured in what the frame can show, not in how big the client's window is.** The two are
not the same number, and both differences were reported as bugs before they were fixed:

- A window that hangs off the screen is showing less than its own size. EasyShell's own window, measured,
  is `1300x700+-50+20` on a 1280x800 root: 1250x700 of what it asked for is on the screen. Dragging the
  frame *taller* used to set the window's width to the number the drag reported, which shrank it from 1300
  to 1250 and changed the width of a window whose height was the only thing dragged - so the frame came
  out 1200 wide. The window size that shows `wanted` pixels is `wanted` plus the part hanging off the
  start of the screen, capped at the screen's far edge (`XServer.windowExtent`), and a dimension whose
  visible size did not change is left alone entirely. **As of A54 the surviving half of that case is the
  far edge and `--max-screen`**: a window that *arrives* off the left or the top - which is what
  EasyShell's centred 1300 wide window on a 1280 wide screen is - is moved onto the screen and the screen
  grows to its far edge, so the frame is no longer smaller than the window it frames.
- A drag stops at the most there is, and does not overshoot and get pulled back: since the screen
  grows to fit the drag, the most there is is now `--max-screen`, and a drag that reaches it lands
  there rather than being pulled back to a size the frame had already left.

**The window is told how large it may be dragged** (`DisplayBackend.setMaxClientArea`, the server pushing
`--max-screen` less where the content starts on the screen - it was the screen itself until the screen
became something a drag could grow). Without it the platform happily lets the user drag the
window past what the client's windows can fill, and the two failure modes are both visible: the frame is
left larger than its content, so the part it grew into shows the scene's black, and the next time the
server says how big the content really is the window is pulled back to it - away from the pointer that is
still dragging. This is what `WM_NORMAL_HINTS`'s maximum size does for a window manager, and it is applied
at the backend because the toolkit is what owns the window. Two details are worth keeping:

- The limit applies to the **window**, and the window carries the platform's decoration, so the
  decoration is measured and added back. Measured where the window is known to fit its content - just
  after `sizeToScene`, and once on show - rather than when the limit is applied: a window that has
  already been dragged too large reads as decoration that does not exist. On macOS it comes out as
  `0.0 x 28.0`, which is the title bar and no side borders; a wrong value here is not cosmetic, since a
  limit that is too small cannot be resized out of.
- It is pushed only when it changes, which is when the viewport's **origin** moves - the size the window
  may reach does not depend on how big the client's windows are, only on where they start.

**A menu does not size the frame it is opened in.** A context menu is an override-redirect top-level
window, and the frame is the union of the client's top-level windows, so counting menus as content
made the frame grow the moment one opened and shrink back the moment it closed: measured, the frame
went 700 to 740 and back for a menu that reached the bottom of the screen. A window manager sizes the
frame to the client's *window* and lets a popup be clipped by the screen; this server is the same rule
with the frame standing in for the screen (`XServer.contentBounds`). The cost is real and worth
knowing: a popup that extends past the client's window **is** clipped at the frame's edge now, where
before the frame grew to show it. Stretching the frame to fill the screen removes it - a popup inside
the screen then fits inside the frame - which is also the gesture that made the popups reach the edge
in the first place. A client that has mapped *nothing but* popups is still sized to them, or it would
have no size at all.

Two platform details were forced by measurement rather than design:

- **The scene root is a `Pane` and the canvas is unmanaged.** A `StackPane` centres what it holds,
  which is invisible while the canvas is exactly the size of the scene - and wrong the moment the
  user drags the window, because centring shifts the canvas by half the difference and every
  coordinate this backend reports is measured from the canvas's top-left. A managed canvas also
  imposes its size as the window's minimum: a 1200 pixel canvas stops the window ever being made
  narrower, so shrinking would simply not work.
- **A resize the backend makes itself is not reported back.** The backend sizes the window to the
  client's window whenever the viewport changes, and that also changes the content area; without
  `adjustingWindow` and the "already told" test, each of those would come back as a request to resize
  the client to the size it already has, for ever.

The client is free to refuse - a window with a fixed size keeps its size - and the frame is then
simply larger than what it frames.

**The frame comes forward when an application arrives** (C2). Without it a forwarded application
started while something else was frontmost stayed behind that window with a grey title bar and no
keyboard - and, because macOS ignores a background application's cursor changes, with the platform
pointer still visible beside the sprite. The raise happens on the client's *first* top level window,
once per session, and only after a frame has been shown: the two conditions are what keep it from
being a window that takes the front at every popup or arrives as an empty frame. Both live in the
tick (`ScreenSession.raisePending` and `framePresented`), because that is the only thread that
touches a backend, and the raise itself is the `raise()` the urgency hint already used.

**One measurement worth writing down**: this is *not* observable in the test bed. With IDEA
frontmost and a client launched, the host window comes forward and the frontmost *application*
becomes the server's own process - **with the raise disabled as well**. A JavaFX stage shown by a
process that is opening its first window activates its application by itself, so the two runs agree
and the difference this item is about (`lsappinfo front`, and `tools/host-windows.c`'s order) cannot
be produced here by removing the fix. What the tests pin is the part that can be: the order (a
`present` before the raise) and the once-only rules.

## The display tick owns the windows

`XServer.eventLoop` runs on its own non-daemon thread and is the only thread that opens or presents
to a window. A request thread never calls into a backend:

- `DisplayBackend.start`, `setViewport`, `stop` and `releaseToolkit` reach the toolkit's thread and
  wait for it, and holding `serverLock` across that wait deadlocks - the JavaFX input callbacks take
  the same lock.
- So the tick reads everything it needs under the lock, releases it, and only then calls the backend.

Two consequences worth keeping:

- **A window opens when the client first maps something**, not when it connects, so a client that
  only asks questions (`xdpyinfo`) never opens one.
- **`Platform.exit()` is called once, at the very end** (`releaseToolkit`), never by the first window
  that closes: it is one-way, and would take every other client's window with it and stop the toolkit
  starting again.
- **The display loop is the process's life.** `Main` waits for that thread and then ends the process
  (`System.exit(0)`), because returning from `main` is not enough: the JavaFX toolkit's
  `JavaFX-KeepAlive` thread is **not** a daemon and outlives `Platform.exit()`, so a process whose
  server had finished - the window closed, every client released - sat in the dock for ever with no
  server thread left in it at all (measured 2026-10-01, A27). What ends the server is unchanged: the
  session ending in single-window mode, `stop()` from the control port or a shutdown hook, or a
  signal - this is only about the process following it.
- **A stop asks before it cuts.** The window-close path sends `WM_DELETE_WINDOW` and gives the client
  the grace period; a stop (Ctrl-C, a signal, the control port) used to send nothing and close the
  socket, which leaves an application whose toolkit was never told to quit running with a dead display.
  `XServer.stop()` asks first - through `clients` and `screenOf(client)`, because in single-window mode
  the master session has no client of its own - and waits, bounded by `--close-grace-ms`, only for the
  clients it actually asked.
- **A window that cannot be opened is a failure the server acts on, not one it sits in** (C11).
  `JavaFxBackend.startupToolkit` starts the toolkit on a daemon thread and waits ten seconds for the
  FX thread to run the startup's runnable: where there is no display `QuantumToolkit.startup` parks on
  a latch that never opens, with no exception and no return value to notice, and the tick thread that
  called it used not to come back - the server went on answering clients with nothing ever shown
  again. When it does give up, `BackendFactory` falls back to Swing (it always did), and a backend
  whose `start` refuses makes the tick log one line and stop the server rather than serve into the
  void. The chain ends at the **headless** backend where the platform itself says there is no
  display (`GraphicsEnvironment.isHeadless()`), with a line saying that clients are served and no
  window will be opened: a Swing window is opened on the event dispatch thread, so a machine that
  cannot show one fails there rather than in `start`, out of the caller's sight.

## The pointer sprite

The sprite comes from the client's cursor and is drawn by the backend **over** the presented frame,
never into the framebuffer - a client's pixels are then never covered by it, and `GetImage` never
reads it back. Two rules make it behave:

- **Its bookkeeping happens before the damage for the frame is taken**, and outside the "is there any
  damage" test. Sitting behind that test, a pointer moving over a window whose client was not
  repainting produced no damage and so no frame, and the cursor only caught up whenever the client
  happened to draw. That reads as a stuttering cursor while dragging, and it was the hardest bug in
  this codebase to see, because every load that keeps the client busy hides it.
- **Nothing is drawn until the server knows where the pointer is** (`InputState.pointerKnown`, set by
  the first `movePointer`). The position starts at the origin, and a sprite drawn there is a second
  pointer in the window's top left corner, next to the platform's own arrow, until the first mouse
  event arrives.
- **A client can ask for it not to be drawn, in one window at a time** (`XFixesHideCursor`, counted
  per window and walked from the pointer's window up its parents, so a hide on a toplevel covers its
  children). It is the client's own request about its own window rather than a display state - which
  is why the count lives on the window and disappears with it, and why hiding it over one window
  leaves it drawn everywhere else. The platform's side is separate and does not change: the host
  window still hides the *system* pointer over itself, so with the sprite hidden too the window shows
  no pointer until the client shows it again.
- **It is clipped to what the canvas covers, like anything else drawn into it.** The box a sprite is
  drawn from is the sprite's own rectangle, and a pointer on the last column or row has one that
  reaches past the screen - which is a normal position, not an error. The sprite's box is unioned into
  the frame's repaint rectangle (the bullet above), so the union has to be clipped as well, and JavaFX
  does not clip a `drawImage` destination region: it throws `Destination region ... must fit within
  texture content bounds` from the *paint*, which for a window being drawn for the first time means
  the window never appears at all. Both `JavaFxBackend.damageWithSprite` and
  `JavaFxBackend.visibleSprite` exist for that clip, and both are pinned without a display.

## The host pointer on macOS

The window hides the platform's pointer so the sprite is the only one over it. Three platform facts
shape that (`JavaFxBackend.hostCursor`, `MacCursor`):

- **`Cursor.NONE` is not an empty cursor.** JavaFX implements it as a process-wide
  `[NSCursor hide]`, undone only when a window's cursor changes away from NONE, so it has to be a
  loan: NONE while the pointer is over the window, `Cursor.DEFAULT` everywhere else, re-applied on
  enter/exit, on window close and in `stop()`.
- **An image cursor is ignored by the platform.** A fully transparent one is drawn as an opaque white
  square, and an *opaque* one was measured still showing the standard arrow. No choice of pixels
  makes an `ImageCursor` work here, so `Cursor.NONE` plus handing it back is the only mechanism.
- **Whether the window is active is not part of the test.** A freshly opened window is usually not
  active, and requiring focus left the platform arrow on top of the client's cursor - two pointers a
  few pixels apart - until the user clicked. `pointerOverWindow` is set by *any* mouse event inside,
  not by `MOUSE_ENTERED` alone, because a window that opens under a stationary pointer never gets an
  enter event.
- **But the platform only lets the *active* application hide the pointer at all** (A28, measured
  2026-10-01). A native probe called `[NSCursor hide]` and watched `CGCursorIsVisible` go 0 -> 1 the
  moment another application was activated, and it never came back; JavaFX's own
  `MacCursor.setVisible_impl` additionally skips the native call whenever its cached
  `isNSCursorVisible` agrees with what is wanted, and that cache is stale from that moment on. So the
  hide is applied only while this process has a focused window (`JavaFxBackend.applicationActive`), and
  **the sprite is what gives way** when it does not: `drawCursor` draws nothing while the application
  is in the background, which is one pointer either way - the X one whenever the server is the
  application in front, the platform's when it is not. A focus change repaints the sprite's own box,
  because nothing a client draws changes when the host window comes or goes.

Two things cannot be verified from a test and need an eye on the screen: **`screencapture -C`
composites the cursor itself and ignores whether the application has hidden it** (a capture with the
pointer deliberately visible and one with it hidden came out byte-identical), and the pointer is only
painted where the application is active.

A host window that opens behind the IDE never becomes active (its title bar stays grey), so the
platform's arrow is the pointer there until the user clicks it - and since A28 the sprite is not drawn
in that state, so what the user sees is one pointer, not two. **Raising the window when its client first
draws** makes the sprite appear earlier, and since A44 it is raised *and focused* once, at that one
moment (a window that has just appeared is one the user is waiting for); the urgency hint still only
raises, because a client asking to be looked at has not asked for the keyboard.

## Input is mapped through the viewport

The host window shows a sub-rectangle of the screen, so a point in the window is not a point on the
screen: the window's top left is the rectangle's top left. `JavaFxBackend` converted a scene point to
a screen point one for one and ignored that corner, while `SwingBackend` has always added it - so on
the JavaFX path every pointer position, click and scroll was reported as far off as the rectangle's
origin is from the screen's. Measured for a JavaFX client in per-client mode on a 1200x760 screen:
its 1300x700 toplevel lands at (-50,20), the viewport comes out as `x=0,y=20`, and every click was
reported twenty pixels above where it happened. The visible symptom is a menu that closes as soon as
the pointer is moved: the position the client is told about leaves the menu's rectangle twenty pixels
before the pointer does, and the crossing events that say so - see above - then dismiss it.

Both backends now add the viewport origin and clamp to the screen
(`JavaFxBackend.screenCoordinate`); the server logs the viewport it opened for a session
(`opened a window for display :N showing java.awt.Rectangle[...]`) at INFO, which is how to tell
whether a given setup is affected. A backend with **no** window says that instead - `display :N is
served by the headless backend, which opens no window: showing ...` - because the sentence above is
what a user follows to find where their application went, and a server that fell back to headless
used to claim a window beside its own warning that nothing could be shown (E7).

## A screen bigger than the window looks

`--scale N` (default 1) tells clients about a screen N times the pixels across, for the same physical
size, so that a client which scales itself by the reported resolution draws at the resolution of a
HiDPI display instead of being enlarged to it. `--geometry` stays the size the window looks; the
screen, the framebuffer and the root are `N` times that in pixels, and `Screen` reports the physical
size from the *logical* size so the resolution comes out at `96N` - measured with `xdpyinfo`:
`--geometry 760x480 --scale 2` gives `1520x960 pixels (201x127 millimeters)` and `192x192 dots per
inch`.

The host window then does the one thing that makes it worth anything: it lays its canvas out at the
*logical* size and blits the framebuffer into it scaled by `1/N`, so one framebuffer pixel lands on
exactly one device pixel. Everything that talks in framebuffer coordinates stays in them - the
viewport, the damage rectangles, the sprite - and only the two edges convert: `logical()` on the way
out to the window, and `screenCoordinate`'s multiplication by `N` on the way in from it. A drag is
reported in framebuffer pixels, because that is what the client's window is resized in, and the
window's own maximum size is converted back the other way.

**The clip belongs on the framebuffer side of that edge**: the canvas is in logical pixels, so
anything that has to fit inside it is clipped before `logical()` divides it - which is also the only
place where "the part of the sprite the canvas shows" has a source offset to go with it.

Why it is off by default: a client that ignores the resolution gets a screen twice the size and lays
itself out exactly as before, which on a HiDPI display means a window in the corner at half the
intended size. Measured with termora (a Swing application, so AWT draws it): it kept its 1x interface
on a 192 dpi screen until it was started with `-Dsun.java2d.uiScale=2`, and then its icons came out as
sharp as they do running natively - the same application whose icons were blank blocks before the A8
mask below was fixed, so the two together are what makes it look right.

Measured again on 2026-09-24 with the client the author actually forwards, and it is the whole trade
in one number: EasyShell on a `--scale auto` server (which is 2 on this machine) filled the screen it
was told about - 1280x800 - so the host window came out **640x428 logical points**, sharp and at half
size, because a JavaFX client does not read the display's resolution from anywhere.

`--scale auto` is the host's own scale factor (`ServerOptions.hostScale`, from the platform's default
transform) for a user who wants the sharpness and knows their client follows. **A scaled screen
publishes `Xft.dpi` in the root window's resource manager** - the property `xrdb -merge` sets, and the
question a toolkit that renders through Xft asks the display - so GTK, Qt and an Xft `xterm` size
themselves correctly without being started with anything special. That is the half a server can
automate; the other half is a client's own toolkit flag, and a server that is unscaled on a scaled
display says so in its log rather than guessing on the user's behalf.

## A picture follows its window

A RENDER picture over a window carries the translation from its own coordinates to the framebuffer,
and for a window that translation *is* the window's position. Reading the position once, when
`CreatePicture` builds the picture, is wrong for every window that is placed after it is created -
which is how AWT opens a dialog: it creates the window at the origin of the screen and only then
configures it where it belongs. The stale position is not only used to address the pixels; it is also
the point the window's clip is tested against, and once the two disagree the writes are not misplaced
so much as refused, all of them: the client composites a dialog onto its window and the framebuffer
keeps whatever was there.

Measured with termora, whose keymap settings put this on screen as "some dialogs are grey": the 消息
message box is 305x130, created at (0,0) and configured to (497,335), and the composite of its
contents reported

```
composite onto window 0x200071 at (0,0) 305x130: 0 pixels written, 39650 blocked;
picture origin (0,0), window at (497,335), visible [Rectangle(497,335,305,130)]
```

- every pixel refused, which is why the dialog showed its background - painted by `ClearArea`, which
  works in absolute coordinates - and none of its text, icon or button. `Picture.refreshClip()` now
  re-reads the position along with the clip at the start of each request, the same place and for the
  same reason the clip is renewed, and `PictureOriginTest` pins the two halves of it (a write and a
  read after the window moved).

The same log line stays in `Compositor` as the diagnostic for this class of problem: it is the only
place that says a client's drawing was *seen* and yet went nowhere, and printing the picture's origin
next to the window's position is what named the cause here. A composite over a window that is refused
entirely is otherwise indistinguishable from a client that drew nothing.

The core protocol never had the bug, because `DrawHandlers.Target` asks the window for its position
per request; only the picture path cached it, which is why everything except a moved window's
*contents* - its background, its border, its event handling - behaved.

## What a window leaves behind when it goes

A window's own visible region is its rectangle minus its children - a parent does not draw over its
children - and for the shape every toolkit gives a toplevel or a dialog that comes out **empty**: the
frame's content window covers the frame completely. That is the right answer for drawing and for
`Expose`, and the wrong one for "what does this window leave behind", which is what an unmap, a
destroy, a move or a restack has to hand back to the windows underneath.

Measured with termora: closing the settings dialog sent `UnmapWindow` and then `DestroyWindow`, the
window really was gone from the tree - and its picture stayed on the screen, because the region handed
back for repainting was the frame's own, which is nothing at all. The symptom the user reported was
that the close button "needs two clicks": the first click closed the dialog, and the picture it left
behind was what they went on aiming at, so the second click - the one whose hover made the client
repaint that area - looked like the one that took effect. The same shape explains the two others they
saw: a dialog's confirm button closing a dialog, and a popup dismissed by clicking outside it.

`XWindow.coveredRegion()` is the other region: the rectangle including its border and its children,
minus what is stacked above it - the area the window is *seen* in. `WindowHandlers.coveredRegionOf` is
what the code asks wherever the question is what a window leaves behind: `unmapWindow`,
`applyConfigure` (a moved or resized window hands back where it was, so a dragged dialog no longer
leaves a trail) and `circulateSubwindows`. `visibleRegion()` stays what drawing and exposure are
clipped to, which is why the two are not one method.
`WindowObscuringTest.unmappingAFrameGivesBackTheAreaItsContentWindowCovered` pins it and fails without
the fix.

**And no window is restored that way any more, because none of them is ever lost.** A reveal used to
be paint-and-`Expose` for every window, unless the client had asked for a `backing_store` of
`WhenMapped` or `Always` - a reference server then delivers **no `Expose`** and the pixels that come
back are the ones that were there (measured with `tools/backing-store-probe.py`), where the default
`NotUseful` delivers one. That is now what **every** window gets: a window's pixels are in a store of
its own and the reveal composites them, so the attribute decides nothing but is still recorded and
reported. `XWindow`'s old capture/restore buffer - which copied a window's visible pixels out of the
screen before it was covered, and threw them away when the client drew, moved, resized or turned the
attribute off - had nothing left to do and is gone.

The region the store changed is the one beside `visibleRegion` and `coveredRegion`: what a window
**owns** (`XWindow.ownRegion`) - its rectangle minus the bounding shapes, and minus neither its
children nor what is above it. That is what a drawing request is clipped to, which is what lets a
covered window go on drawing; the part that *shows* is still `visibleRegion`, and that is what the
composite back to the screen is clipped to.

**`save_under` is the other half and is deliberately not implemented, because the reference does not
implement it either.** Xvfb advertises `save-unders NO` in its setup reply, and the probe's third
mode - a window asking for `save_under` - behaves exactly as the mode where nothing is asked for:
one `Expose`, on both servers. A client that asks (EasyShell's context menu is one, measured with
`xwininfo -all`) gets the ordinary restore, which is what it would get from the reference.

## A depth 8 drawable holds bytes, not colours

A depth 8 pixel is stored as the byte the client sent, unconverted, whether or not the drawable has a
visual. Mostly it is not a colour: a toolkit rasterises its antialiased shapes and glyphs into a depth
8 pixmap whose bytes *are* coverage, and composites a colour through it. Reading that byte as a colour
through the visual destroys six of its eight bits. This server offers a depth 8 visual of the usual
3-3-2 layout, so each byte came back as the blue field of its own expansion - coverage modulo four,
which reaches the picture as 0x00, 0x55, 0xaa and 0xff and nothing in between.

Measured against Xvfb on the same application (termora again): its toolbar icons drew with exactly
three colours between the icon colour and the background, where Xvfb's - the same client, the same
requests - drew twenty. On screen that is the difference between a flat, bold icon and the one the
application is supposed to show, and the same loss hits every antialiased glyph beside it. It is
invisible to `xdpyinfo`-level inspection: the mask *picture* is alpha-only on both servers, so the
format is right and only what the pixmap holds is wrong.

Why Xvfb is right by luck rather than by effort: its screen is 24 bit, so it has no depth 8 visual at
all. A client that asks for a depth 8 pixmap there gets one with no visual attached, and the byte is
simply kept. Matching that exactly would mean dropping the depth 8 visual from the screen, which also
takes away depth 8 *windows*; keeping the byte and decoding it through the format on the way out
(`Picture.stored`, whose alpha-only and depth 8 pictures go through their format's masks) leaves both
working. `ImageCodecTest.depthEightPixelsKeepAllEightBitsOfTheirValue` pins the storage and the
round trip.

The core protocol's `PutImage` was the only path that converted, and `packed` on the read side was the
only one that converted back; everything else already treated a depth 8 store as raw. The log line
that found it is `DrawHandlers' `PutImage landed: ... values=`, which prints the distinct bytes a
small image put down: three values where a mask should have had a gradient is what named the cause.

**The write side of RENDER did not, and that is the second half of the same convention** - found a
day later with a real compositor rather than a toolkit. `Picture.writePixel` stored the whole ARGB
value, so a `FillRectangles` into an A8 picture put the coverage in the top byte where the format's
mask never looks, and the mask read back as **zero coverage everywhere**: every composite through it
wrote nothing and `picom`'s frames were blank. The convention has to hold in both directions or the
read side's care buys nothing - a picture is only a picture if what is written into it is what is read
out of it. `Picture.storedForm` is the write half, and
`AlphaMaskTest.writingAColourIntoAnA8PictureStoresItsCoverage` pins the round trip. Both halves were
invisible until something wrote *into* a depth 8 picture rather than only reading one, which is why
the toolkit that rasterised its own masks never noticed.

## Every window keeps its own pixels

The server used to have one surface - the screen's framebuffer - and every window drew straight into
it through a clip, so **obscured content was simply not kept**: a window that drew while something
covered it lost what it drew, and a client that read a covered window back got whatever the window on
top had painted at that rectangle. A window whose visual had an alpha channel was the one exception,
because the screen has no alpha to keep (see "The screen is opaque"), and that exception is what the
32 bit ARGB visual and the tray icon's shape rested on.

**Now every window but the root keeps its own pixels** (`XWindow.ownPixels`, a `Framebuffer` sized to
the window) and is composited onto the screen from them. The root is the exception because it *is* the
screen: its pixels are the framebuffer's, which is what the display backends read.

**And so is a window larger than the largest screen this server can be asked for** (A29, 2026-10-01).
A store is the window's own size, which made it the one allocation here with no budget: measured, a
12000x12000 window reached `OutOfMemoryError` inside `Framebuffer.allocate` while it was being mapped.
Such a window is not refused - it draws straight onto the screen, exactly as the root does, so it
simply does not keep what a window on top of it covers. Every window that fits on the display keeps
its pixels exactly as before.

**Where it draws.** `DrawHandlers.target` is the one place a drawing request is pointed at a surface,
so that is where the choice is made: a window draws into its own store, in the window's own
coordinates, and clipped to what it **owns** (`XWindow.ownRegion` - its rectangle with its own and its
ancestors' bounding shapes taken out, and with neither its children nor whatever is above it
subtracted) rather than to the part that shows. That is the whole point: a covered window goes on
drawing, so what it drew is there when it is uncovered. `afterDrawing` then composites what the request
wrote onto the screen, clipped to `visibleRegion()` so it cannot paint over what is above it. An
ordinary window's store is opaque exactly as the screen is, so its composite is a straight copy; only
an alpha visual's store keeps the alpha and goes through `Compositor.compositeOver`, whose identity
argument is in "The hot path in the blend".

**What follows from it, and each of these had to be decided rather than inherited:**

- **A window's pixels come back without an `Expose`.** The reveal path (`repaintUnder`) composites the
  store, so `backing_store` no longer decides whether a client is asked to redraw - every window is
  kept, which is the reference's behaviour with backing store and is what a frame per top level needs
  (C8). The area a window *gains* (a map, a resize, a restack) is still painted with its background and
  still exposed: it is an area the client has to draw. A **move** is the one case that must not do
  that - the pixels travel with the window, so painting a background there would wipe them - and
  `visibilityChanged` distinguishes the two for that reason.
- **`bitGravity` moves the pixels inside the store** (`applyBitGravityToStore`), not on the screen,
  and `ownPixels` keeps what was drawn across a resize so there is something to move. A12's eleven
  measured gravities are unchanged.
- **A picture's write clip and its read clip are different regions.** RENDER addresses the window's
  own store in the window's own coordinates (`Picture.refreshClip`), and a picture may write anywhere
  the window owns - a covered window can be drawn into - while a read is still stopped by whatever
  covers it, so a source picture cannot sample a sibling's pixels.
- **`IncludeInferiors` is answered from the children.** A child's pixels are in a store of their own
  and not in the parent's, so a read that falls in a child is taken from that child
  (`XWindow.pixelFromInferiors`, topmost first) - which is what the window *shows* there.
- **RENDER draws into the store and is composited when the request ends.** `Picture` remembers the
  region it wrote (`takeWindowDrawn`) and `Compositor.finish` composites it. Before this a window's
  RENDER picture pointed at the screen, so a toolkit drawing its ARGB window with cairo drew opaquely
  onto the screen and left the store empty - the gap this item's first half recorded.
- **The root's picture is the only one that still carries a screen translation**, and the root never
  moves - so the class of bug `PictureOriginTest` was written for (a picture reading the position it
  was created at) cannot come back for a window.

**The two protocol rules that had to change with the alpha half**, both of which made an ARGB window
impossible before it and neither of which is obvious from the symptom:

- **A child need not share its parent's depth.** `WindowHandlers.create` refused a window whose depth
  differed from its parent's as a `BadMatch` - and the ARGB window every compositing toolkit creates is
  a child of the root, depth 32 under a depth 24 root. What has to exist is a *visual* of that depth,
  which is checked separately. (Xvfb cannot settle this one: it advertises no depth 32 visual at all.)
- **A graphics context converts its colours in the visual of the drawable it was created on.** This was
  hard-coded to the screen's root visual, so a client that created a context on its ARGB window and set
  a half transparent foreground got an opaque one - the alpha a core drawing request can *only* express
  that way. A real client does create its context on the window it draws on, which is why the bug hid
  behind "the client asked for the wrong thing".

**What it costs.** Every window now holds a window-sized `int[]` (allocated lazily, on the first draw,
as E4's lazy screen allocation is), and a drawing request writes the store and copies the visible part
to the screen rather than writing the screen once. The copy is a straight row copy for an ordinary
window. Measured live with the two clients: EasyShell's window renders identically and its context
menu survives the out-and-back gesture 6/6, termora's 6/6 - and the one behaviour that looked like a
regression was measured on the *previous* build too (see the B5 entry in `docs/backlog.md`).

## A compositor is the store, seen from the other side

Composite, Present and SYNC are the extensions a compositing window manager is built on, and the
store above is why they were cheap: a window's pixels are already somewhere to hand a compositor, and
redirection turned out not to be a second drawing path but **the stopping of one**. Every request ends
in `DrawHandlers.afterDrawing`, which asks `XServer.compositeOwnPixels` to put what was drawn on the
screen; a redirected window is simply one that method declines to composite, and the drawing itself
does not change at all. That is the whole of `RedirectWindow`. `NameWindowPixmap` then hands over
`XPixmap.view` - a pixmap that *is* the window's store rather than a copy of it, resolving the store
on every access because a resize replaces the array - so a compositor reads live pixels and never
reads the screen.

**The trap is that this server is right by default in every test that does not redirect anything.**
With every window composited from a store, forgetting the guard changes nothing that any test could
see; the reference is what says otherwise, and it says it plainly: repaint a redirected window there
and its pixmap holds the new colour while the screen holds the old one.

**Three things about the overlay had to be right, and a real compositor found all three.** Depth:
it is the root's, because a compositor creates a RENDER picture on it with the *root's* format and a
format that does not match the drawable's depth is `BadMatch` - which `picom` does not check, so the
symptom was a segfault a long way from the cause. Redirection: a compositor redirects the root's whole
subtree, the overlay included, and here being composited is the overlay's only path to the screen, so
it is exempt. And background: a reference creates the overlay with `background = None`, so it
contributes nothing until the compositor draws; this server approximates `None` by painting the
desktop colour, which for a screen-sized window means the whole screen - so the server's own surface
has no pixels at all until something draws into it.

**The overlay window is the one part that could not be inherited.** A reference answers
`GetOverlayWindow` with a screen-sized window of its own and the compositor draws its result there.
Here a screen-sized window *above every other* is a problem twice over: every window in this server
paints a background, so an opaque overlay would hide everything behind it, and the obscuring
machinery subtracts every mapped window above a window from that window's visible region - so the
moment the overlay was mapped, every window below it would have had an empty visible region and would
never be sent an `Expose` again. The overlay takes the depth 32 visual with a fully transparent
background, and one rule went with it: **a translucent window does not take anything away from
`XWindow.visibleRegion`**. The rule is right on its own - a window that shows what is behind it is not
in the way - and it is what makes an overlay possible here at all.

**Present is the same path from the client's end**: it copies the pixmap into the window's own pixels
and composites from there, so a frame presented to a redirected window lands in the pixmap the
compositor reads, and there is no second drawing path to keep in step. **SYNC waits on nothing, and
says so**: this server finishes every request before it reads the next, so a fence is already in its
final state when `AwaitFence` is read, and a `Wait` that returns immediately is the truth rather than
a stub.

## GLX, and why a server without a GPU can run OpenGL

OpenGL looked like the one thing this server could never do: direct rendering needs a driver talking
to the same GPU the host window is on, and indirect GLX - the old path, where the client sends GL
commands over the wire - needs a GL implementation *inside* the server. Neither is here. **And neither
is needed, for the clients that can run on a machine without a GPU at all.**

That is a measurement, not an argument (2026-09-27): `tools/tap.py` in front of Xvfb, `glxgears`
running through it, and the client's own stream read back request by request. The client sends **no GL
commands**. It asks for `DRI3`, `Present` and `MIT-SHM`, gets none of them (the reference does not have
them either), and falls to Mesa's software path - `drisw` - where it renders in its own process and
pushes each finished frame as an ordinary **`PutImage`**: 769 MB of them in 5.6 seconds, one
`GetGeometry` per frame as the synchronisation, and not one GLX `Render`.

**And it is off unless `--glx` asks for it**, which is the item's own trap arriving in a form worth
recording. Advertising GLX changes what a toolkit does with its own drawing, and on the build that
advertised it unconditionally EasyShell (JavaFX) was reported to stop updating what it animates - a
menu item with no hover colour, a caret that does not blink - while clicks and typing still worked.
That report is not reproducible from a script, and re-measuring it on 2026-09-27 came at it from the
side the first attempt could not: **the client's own requests**. The caret blinks on both builds (ten
captures 0.2 s apart, the search box's rectangle alternating between exactly two states); and while
the pointer crosses an open context menu the client issues **no drawing request at all** - the menu is
drawn once, and after that the screen is byte-identical across twelve pointer positions. A toolkit
that does not repaint on hover has not failed to reach the screen; it has not drawn. The mechanism the
report points at is still the one thing that *is* measured: with GLX present the client takes a
different path, and the frames it sends differ - but the path it takes is one it abandons (below), so
the report is a toolkit's own state and not a frame this server failed to deliver. **That is why the
default was put back to on (2026-09-27, the same day it was made opt-in)**: a client that wants
OpenGL needs the extension and nothing measured says a client that does not is hurt by it, so the
switch the user gets is `--no-glx`, which is what comparing two builds wants.

**Why, in the toolkit's own words** (`-Dprism.verbose=true`, 2026-09-27). Without GLX: *"ES2 Prism:
Error - GLX extension is not supported"* and it falls straight to the software pipeline. With it:
JavaFX initialises its X11 GL factory, **creates a context**, queries the GL, and only then reports
*"System GPU doesn't meet the es2 pipe requirement"* and falls back - **to the same software pipeline**.
So the extension buys JavaFX nothing and costs it a half-taken GL path - one abandoned context at
startup, and the same software pipeline either way. (That cost is real and small, which is why
`--no-glx` exists; it is not what the original report was about, and A17 closed that report.) **It is not a speedup anywhere here**: a GL
client without a GPU renders in software and ships every frame as `PutImage` (769 MB in 5.6 s
measured), where the reference shares the buffer instead - `glxgears` runs at ~190 FPS here against
2832 against Xvfb. Speed would need a GPU on the client with DRI3/Present, or a real GL in the server
with indirect GLX.

So the extension is a handshake, and the handshake is what `extension/GlxExtension` implements: the
version, both config lists, the two server strings, contexts, and a drawable's attributes. **The one
decision it rests on is the direct context.** `GLXCreateContext` carries a flag, and a client told
"direct" renders itself and blits its frames here; a client told "indirect" sends its GL commands
instead, and there is nothing here to execute them. So direct is granted and **indirect is refused** -
a client that wants the wire path is better off failing at the context than at every draw call.

**And nothing is promised beyond it.** The extension string is empty: a client is told GLX 1.4 core
and no extensions, so a toolkit that wants `CreateContextAttribsARB` - the ES2/shaders path - finds it
missing and takes its own fallback rather than being handed a context that cannot draw. EasyShell is
that client here: it asks GLX its questions, finds nothing it can use, and renders through JavaFX's
software pipeline exactly as it did before (measured live, with GLX advertised). The item's own trap -
"do not advertise GLX before it works" - is kept by making "works" mean *a client draws*, and by
keeping the advertisement to the part that does.

**The layouts are measured, and three of them read as a client bug if you guess them:**

- the opcodes are Mesa's, from `glxproto.h`: from 18 up they are `QueryExtensionsString`,
  `QueryServerString`, `ClientInfo`, `GetFBConfigs`. One apart from the obvious reading, and
  `QueryServerString` is 19, not 18.
- **the two string requests carry no string** - `QueryServerString` is screen and name, and the reply
  carries the length at offset 12 with the text after the fixed part - **and that length is the text
  plus its NUL**. A reference answers 4 for `"1.4"`, and a client reads exactly that many bytes into a
  buffer it has already allocated: one short and `glxinfo` prints the string followed by whatever
  happens to be next in memory.
- **`GetVisualConfigs` answers forty ints per visual**, where the specification's struct has eighteen
  and the header's `__GLXvisualConfig` has thirty. It was pinned by running `tools/GlxProbe` against
  Xvfb and laying its first config beside `glxinfo`'s own table for the same visual id - eighteen
  fields, then eleven `(token, value)` pairs. A client reads `numProps` ints per visual into a fixed
  struct, so a layout a few ints out is that client reading its own field boundaries wrong.
- **the request body starts three bytes in**: a decoder is handed the input at the request's *minor*
  byte, so `minor` and `length` have to be read before the body. Reading the body straight away shifts
  every field by three bytes, which is invisible in a request whose body is ignored (a screen number)
  and wrong in every one where it is not - `GlxProbe` caught it on the first run, `QueryServerString`
  answering `BadValue` for the name.

What this does **not** give: hardware rendering (there is no GPU in the path), shaders and ES2 (the
extension list is empty and `CreateContextAttribsARB` is refused), and GLX pixmaps, pbuffers and
windows. A client that needs any of those is told so and falls back, which is the same answer it got
before this existed - the difference is that `glxinfo` and `glxgears` now work.

## The hot path in the blend

The compositor's pixel loop is where every request ends up, and it was written for clarity: per pixel
it reads a mask, the source and the destination, converts all four channels of both to doubles, picks
an operator from a switch and blends. That is the right shape for the operators that need it, and the
wrong cost for the one case most of a desktop's drawing takes - **an opaque source with a full mask**,
under `Src` (a window's background paint, a `CopyArea`) or under `Over` (an opaque `PutImage`, a
RENDER composite of an opaque picture). The answer there is the source pixel.

`Compositor.blend` now returns it directly for those two operators. That is not an approximation that
has to be checked: the general path answers `fa = 1, fb = 0` for both, and its arithmetic on values
that came out of eight bit channels is exact, so the two agree pixel for pixel by construction -
measured, not assumed, on a live scene: 0 of 46656 pixels differ with the fast path in and out.

Measured on the loop itself, two hundred full-screen 1300x800 fills:

```
                       before       after
Src   200 full fills   1795 ms      984 ms     (8.98 -> 4.92 ms/frame)
Over  200 full fills   1770 ms      953 ms     (8.85 -> 4.77 ms/frame)
```

**The mask paths came later and the same way** (2026-09-25): an A8 mask over an opaque source, under
`Src` or under `Over` onto an opaque destination, which is what a glyph or an icon is. The condition
is that a fast path of this kind changes no rounding - and the way to make that true rather than
hopeful is to *show* it: the double-precision body was split out as `blendGeneral`, and
`BlendFastPathTest` compares the two over everything the fast path claims - every one of the 256 mask
alphas against both ends of every channel and the values either side of a rounding boundary, both
operators, twenty thousand random pixels, and the component-alpha case - so a future edit that makes
them disagree by one unit fails a test instead of changing one pixel of somebody's text. It is not a
proof, it is a bound: the two fast paths are the same rational number in integers, and a rounding tie
would need `2A = 255 * odd`, which has no solution.

```
masked Over, opaque source, A8 mask, 1040000 pixels    fast 2 ms    general 5 ms    (2.80x)
```

and on a real client, because this is the path its text takes: EasyShell rendered with the fast path
in and with it short-circuited is **the same picture, 0 of 910000 pixels different**.

## How close the picture is to a real server

Measured against Xvfb on the same application, screenshotted from both and compared pixel for pixel
(this is the only test of "does it look right" that does not depend on somebody's eye):

- **99.5% of the window is identical** - 907497 of 912000 pixels. Of the rest, 2745 differ by one
  unit in a channel, 343 by two to sixteen, and 1415 by more.
- The **one-unit class is left alone on purpose**. It is the blend arithmetic: this server rounds,
  and Xorg's fixed-point composition truncates, so a value that should be 0x74 can come out 0x74 here
  and 0x73 there. Nothing is more correct about being off by one in Xorg's direction, and it is
  invisible - but it is the reason a byte-for-byte comparison against a reference server will never
  be clean, and why the interesting question is where the *larger* differences are.
- The **larger differences that used to be confined to the top fifty rows** of that application's
  window - the title bar and its icons - are **gone**: they were the depth 8 pixels being read as
  3-3-2 colours, which is written up above. The toolbar strip now comes out with a worst channel delta
  of **2**, 936 of its 9280 pixels differing by one unit and none by more, where before a fifth of them
  differed by up to 232. What still differs inside the window's own rectangle is the one-unit class
  plus, along its edge, the root colour this server paints and the border it draws - both deliberate.
  The measurement only means anything with both applications in the same state: the first version of
  this comparison had the pointer parked over the toolbar in one capture and not the other, which
  lights up hover states and reads as a large, entirely imaginary difference.
- **A filled polygon's scanline crossings are taken at `y`, not at the pixel's centre.** This is the
  one rasterisation rule in this server that was *wrong* rather than approximated, and it was found by
  a whole-window comparison rather than by reading the code: `xlogo` differed in 377 of 46656 pixels
  (0.8%) with full-scale deltas, and the cause was half a pixel - `FillPoly` computed each edge's
  intersection with the scanline at `y + 0.5` where a reference server takes it at `y`. Half a pixel of
  slope is a quarter of a pixel in x for a 45-degree edge and a whole one for a slope of two, and on a
  concave shape it changes the *area*. Measured over four triangles and a star, every row: with the
  crossing at `y` the two servers' fills agree pixel for pixel, and `xlogo`'s window is now **0 of
  48400 pixels** different. The horizontal rule was right all along (a span is `ceil(xl)` to
  `ceil(xr) - 1`), so this half pixel is the whole of it - see backlog A15 for how the probe that found
  it nearly reported the opposite.
- **Scan for a shift before believing a rendering difference.** A whole-window scan over offsets of
  +/-4 pixels is cheap and says whether the two renders disagree in place or are the same picture moved,
  which are very different bugs.

## Default colours

`Screen.DEFAULT_BACKGROUND_ARGB` (#E8E8E8) is what a screen shows where nobody has drawn;
`DEFAULT_BORDER_ARGB` (#A0A0A0) is what a window's border starts with, a shade darker so a window
has an edge. `XWindow`'s fields carry both, so every constructor path agrees.

The light default is not cosmetic. A real X server paints its root window with a light grey weave,
so a client that never sets a background - `xeyes` is one: its request stream contains no
`CWBackPixel` at all - comes up looking light. With a black default the same client arrives as a
black rectangle, which is what "xeyes looks wrong here" turned out to mean.

Borders are painted after `repaintUnder`, clipped to `outerVisibleRegion()`. Both parts matter: the
border lives outside the window's content, so clipping it to the content region leaves all four
bands zero wide and draws nothing (which is what this server did - the one entry point that got it
right had no callers), and painting it before the windows underneath are repainted lets the root
cover it again.

## Ending a session

Both directions used to leave the other side hanging, and both are now explicit:

- **Closing a window** asks that client to close, the way a window manager does: a
  `ClientMessage` with `WM_PROTOCOLS` as its type and `WM_DELETE_WINDOW` as its first item, sent only
  to root children whose `WM_PROTOCOLS` property lists it (read as big-endian 32 bit items, which is
  how properties are stored). Once `--close-grace-ms` is up the connection is cut, which is what
  actually ends it - toolkits often ignore the message.
- **A client going away** closes its window and frees its screen, and leaves the other clients alone.
  In `--single-window` mode the last client going quiet stops frames rather than leaving a frozen
  empty window live.
- **Closing the window can stop being the end of the session** (`--detach-on-close`, backlog D1). The
  clients keep running with nobody showing them, and `--control-port` is how a window comes back:
  `attach`, `detach` and `status`, one command per connection, over loopback.

## A window that is not the session

The frame around a forwarded application is not the application, and the failure that says so loudest
is a user who closes the window of a long-running session and loses the session. `--detach-on-close`
makes the window only a window (backlog D1), and the three things it takes to be honest about that
were each worth measuring:

- **"Detached" is a state, not "the backend is null".** The display tick opens a window whenever a
  session has none and its client has mapped something - that is how a connection that only asks
  questions never gets one - so a detached session has to say so, or the next tick opens a window
  straight back up. The tick's own guard is that state, and it also closes any window the session
  still had, at the point in the frame where the lock is released and waiting on a toolkit is allowed.
- **The frame that comes back is the whole viewport.** A detached screen goes on drawing: the client's
  requests are answered, its keys repeat and its framebuffer fills, but the damage each frame carried
  was consumed by a tick that had nowhere to present it. A window that waited for *new* damage would
  show the client's background until the client happened to draw again - so attaching sets a flag that
  makes the next frame the whole viewport, from the same framebuffer the pixels never left.
- **Detaching does not save CPU, and the measurement says why.** The trap the item names is "the tick
  can idle to save CPU when nothing is presented"; measured on a real server with an idle client,
  detached costs 1.4-2.1% of a core against 1.4-2.3% attached. Presentation was never the cost - the
  per-frame work is the bookkeeping (which window is the identity, what the viewport is, the damage
  take) and that runs either way. So no idling state was added: it would have been a mechanism for a
  cost that is not there.
- **A control port rather than a signal.** `SIGHUP` is the unix way to say "come back" and the JVM
  cannot take it without internal API; the port can be driven from a script, from `nc`, and from a
  test, and it answers. It is bound to loopback and has no authentication of its own - the display it
  belongs to is on the same machine, and a control port on every interface would be a remote kill
  switch.
- **`attach` is per session, not per window in the protocol sense.** Every detached session gets a
  window back; there is one window per session, so that is the same thing a user means. A control
  command that named one session would be the next step if a server ever showed several.

## Grabs and crossing events

EnterNotify and LeaveNotify are the only way a client learns which of its own windows the pointer is
in, and a menu is built entirely on them: its submenu is a separate window that appears beside the
menu the pointer is hovering, and the EnterNotify that says the pointer arrived inside it is what
makes the submenu highlight its items and accept clicks. Clicks and motion alone are not enough -
a toolkit routes them by the window it last saw the pointer enter.

This server used to deliver no crossing events at all while a pointer grab was held, and a menu
holds a grab whenever it is open. The submenu was drawn and could not be used: nothing in it
highlighted (measured), and a press inside it was read as a click outside the parent menu, which
dismissed the whole menu instead of activating the item.

What a real server does, measured against Xorg for the same gestures request for request, and what
is implemented here:

- **A grab that reports events to itself confines crossings to the grab window's tree.**
  `GrabPointer` with `owner_events` false takes the pointer over: the window the pointer leaves
  still hears its LeaveNotify, and a window outside the grab window's subtree hears nothing. The
  implicit grab a press starts confines the same way, which is why a window that merely appears
  under a dragged pointer is not "entered" by it.
- **A grab with `owner_events` confines nothing.** The grabbing client goes on receiving the
  crossings of its own windows. A menu that grabs the pointer on its main window depends on this:
  confining those would leave it never learning that the pointer reached one of its own popups.
- **A grab confines crossings while a button is down.** The implicit grab a press starts pins the
  pointer to the window it landed on, and crossings are confined to that window's subtree until the
  button comes up - which is why a popup a menu maps under the pointer mid-press is not "entered"
  until the button is released. The reference server was measured doing exactly that.
- **An event for a window that is not the grabbing client's goes to the grab window.** A motion or a
  click that lands on the root, or on another client's window, while a grab with `owner_events` is
  held is reported against the window that holds the grab. Without it the motion that takes the
  pointer off a menu goes nowhere at all, and a click outside it - how a menu learns to close - is
  never delivered.
- **A crossing between the grab window and a window outside it is reported as a move inside the
  grabbing client's tree** (`NotifyAncestor` rather than `NotifyNonlinear`), and the focus bit of a
  crossing is set when the focus window is the event window or inside it, with `PointerRoot` counting
  as the window the pointer is in. Both are what the reference server sends.
- **An explicit grab supersedes the implicit one.** A menu that pops up under a held button is the
  case: the press pins the pointer to the window underneath, the client then grabs the pointer for
  the popup it just created, and the release has to be reported against the popup. Reporting it
  against the window the press landed on is a release the menu reads as a click outside itself.

**Four things about a crossing that the wire format does not tell you**, all measured against the
reference and all wrong here until 2026-09-25 (backlog A14). They are worth keeping because each one
is invisible from the server's own side - the server agreed with itself in every case - and only an
independent decoder or a comparison shows them:

- **The last byte of a crossing is the focus bit in bit 0 and `same_screen` in bit 1**, which is the
  opposite order from what the field's name suggests. Sending `0x01` where the reference sends `0x02`
  reads, through Xlib, as "same screen: no, focus: yes" - so every crossing this server sent claimed
  the pointer was on another screen. Read it back with `xev` rather than trusting a bit test in one's
  own probe; the probe's reading and Xlib's disagreed, and Xlib is what clients use.
- **The focus bit is set when the crossing's window is inside the focus window** - the focus window or
  a descendant of it - and cleared for the focus window's own parent.
- **The enter chain arrives outermost first**, the window itself last; the leave chain is the other way
  round, and that asymmetry is the reference's.
- **A window in the middle of a chain reports `Virtual` when the window on the other end is above it**
  and `NonlinearVirtual` when the other end is unrelated. `Virtual` for every chain window says the
  pointer moved inside the subtree it just left.

**Was a first-level menu that is left and re-entered dismissed** by EasyShell, where it stayed up
against Xvfb. A scripted `right-click -> move out of the menu -> move back in` closed the menu every
time on this server and never on Xvfb, and the crossings involved matched the reference byte for byte
(window, detail, mode, state, coordinates and focus bit, in the same order), so the client was
reacting to something that was not in the crossing itself. Narrowed down by trial: suppressing the
enter-into-the-menu on re-entry kept the menu up, but the same crossing is what makes a submenu
active in the first place, so suppressing it left the submenu drawn but dead.

**It was the focus, and it is the one thing the crossings could not show.** The client was never told
it had the keyboard (see the next section), and every measurement in the paragraph above was taken on
a server that changed the focus and said nothing. With `FocusIn`/`FocusOut` delivered, the same
scripted gesture leaves the menu up **6 runs out of 6**, with the hovered item highlighting, where it
was closed in 3 out of 3 before. Two things had to be true for that, and the first alone was not
enough:

- The client has to be told the focus at all, which is what makes its window "focused" and lets its
  popup machinery run.
- **The window the pointer is already inside must still be told**, which is the ordinary case: the
  menu is opened under the pointer, and the window that takes the focus is the one the pointer is in.
  An early version treated that as "nothing changed" and sent nothing, which put the menu straight
  back to being dismissed; a reference server was measured sending the whole pair there.

The gesture is a scripted one (`/tmp/menu-outin2.sh`); a hand on a real mouse moves differently, so
the interactive case is worth re-checking rather than assuming.

Two deliberate deviations, both measured rather than reasoned:

- **Starting and ending a grab does not report the crossing pair Xorg sends** (LeaveNotify on the
  window the pointer is in and EnterNotify on the grab window with `mode` NotifyGrab, and the
  reverse with NotifyUngrab). It should: it is how a submenu becomes the window the pointer
  "belongs to" the moment it opens. **This is about grabs a client takes on its own request**; a
  *passive* grab - `GrabButton`/`GrabKey` - sends the pair, because a reference server does
  (measured) and because a shortcut's client is not a menu that can be dismissed by it. With the pairs in place EasyShell's context menu was torn down
  within microseconds of opening in three runs out of three, against one in three for the same
  scripted gesture on this server without them; with them left out the menu behaves as it does
  against Xorg and the submenu works. Since the pointer's next move reports the same information,
  the pairs are the part that can be given up.
  **Measured again on 2026-09-23**, by accident: an uncommitted change had re-added the pairs, and
  the menu was then torn down 3 runs out of 3 where the committed server kept it up in 3 out of 3
  (the signal is the client's own `UnmapWindow` and `DestroyWindow` after the menu's `MapWindow`).
  The message that produced it is still in `InputHandlers.ungrabPointer`'s history: a pair was sent
  on **every** ungrab, and a menu grabs and ungrabs repeatedly while it is open, so the client was
  told "you left your main window and entered the popup" several times while the pointer sat still.
  A grab that ends without changing which window the pointer is in has nothing to report.
- **A popup is closed by an `EnterNotify` on it if the pointer has not moved.** This is the same
  measurement from the client's side, and it is what the leave-and-re-enter dismissal is made of:
  the server log shows the client sending `UngrabPointer` and unmapping the popup **one millisecond**
  after the `EnterNotify` that reports the pointer back inside the menu, with no request in between -
  it is reacting to the crossing itself. So the two deviations above are not timidity: every crossing
  that reports a window "entered" without the pointer having moved was measured to cost the menu,
  whether it is sent at the map, at the release of the button, or at the end of a grab.
- **A window that appears under a stationary pointer is reported as entered by the move that
  follows, not by the map that put it there.** Xorg sends the crossing at the map; doing that here
  tore the menu down as it was mapped in three runs out of three. A window that *goes away* under
  the pointer is reported at once, because the alternative there is a pointer pinned to a window
  that no longer exists and a next click that is reported against nothing.

## A synchronous grab stops the device

A grab taken with `GrabModeSync` is a client asking the server to **stop the device** after the event
that triggered it, so it can decide what happens to what comes next - a game that re-centres the
cursor after every motion, a remote-desktop client that redelivers a click from somewhere else. The
events a stopped device produces wait in a queue, and `AllowEvents` starts it again or reprocesses the
event it stopped on. This server delivered everything immediately and validated `AllowEvents`' mode and
did nothing else, which made every `Sync` and `Replay` mode a no-op (backlog A13).

**The rules are X's, read off `include/inputstr.h` and `dix/events.c` rather than guessed**, because
the protocol's own prose does not describe them and the measurements do not fit a simple reading:

- **The states are not a boolean.** `FREEZE_NEXT_EVENT` sits *below* X's `FROZEN` sentinel on purpose:
  a device told to freeze on the next event is still delivering until one arrives. So `Sync` means
  "start again, and stop on the next **key or button**" - and a motion is not one of those, which is
  why a `SyncPointer` release lets through everything up to and including the first button and nothing
  after it.
- **A grab names a mode for each device**, and the mode named for the *other* one freezes that device
  on the grab's behalf (`CheckGrabForSyncs`). Both live clients here grab with `Async`/`Async`, so the
  freeze is inert for them - the only way to tell the difference is the reference diff, and
  `tools/AllowEventsProbe`'s five arrangements are identical to Xvfb.
- **A replay is not a second grab.** The grab is ended and the event reprocessed *as if it had not
  fired*, and the search for a passive grab starts below the window the replayed grab was held on - so
  the same arm cannot take it again. Measured: a replayed press reaches the window underneath with no
  second `NotifyGrab`, and a replayed key reaches the window that has the focus.

**The pointer has two positions while a device is frozen, and a client can read both.** `QueryPointer`
answers where the pointer was when it stopped; the events released afterwards carry where the device
went. X does the same (`EnqueueEvent` advances the sprite's *physical* position, and the events are
stamped from it). Measured with a purpose-built probe: frozen at (200,200) with motions to 220,220 and
400,400 waiting, `QueryPointer` says (200,200) and every released event carries 400,400 - the *last*
position, so two held motions arrive as two events both reporting it. That is not an accident of the
queue: it is what a client that froze the pointer to re-centre it is reading, and it is why the two
positions are separate fields here rather than one.

**Two things the queue owes that are easy to leave out.** A key's repeat follows the *device*, not the
delivery: X cancels a repeat as a release is enqueued, and with the bookkeeping left at delivery time a
device frozen with a released key repeated into the queue at 25 events a second (measured: 372 events).
And the queue is bounded (1024, oldest dropped with a log line) - the reference has no bound, and this
server refuses to grow without limit for the same reason its output queue to a client does.

**One bug in this area was found by the measurement being irreproducible**: a disconnecting client's
*passive* grabs were never dropped, so an arm outlived its client and kept firing on the root for
whoever pressed next. A reference server drops them with the client. Two runs of the same probe
disagreed with each other until that was fixed.

## The order a window tree is torn down in

`WindowHandlers.destroyWindowTree` unmaps a window **before** it destroys its children, and that order
is load-bearing rather than incidental: unviewability is what the focus revert walks past, so unmapping
the parent first makes the whole subtree stop being viewable at once and a focus inside it reverts to
the root in one step. Destroying the children first left the parent viewable while the focus moved off
a child, and the parent - which the same request was about to destroy - was told it had gained the
focus and then that it had lost it. Measured with `FocusProbe`'s `kill` step against Xvfb (A5).

## The focus is the client's to be told about

`FocusIn` and `FocusOut` are the only way a client learns it has the keyboard, and a toolkit is built
on being told: it moves the focus to a dialog and enables the dialog's fields when the dialog says it
arrived. This server changed the focus on `SetInputFocus` and sent nothing at all, which was recorded
here as a known gap for two sessions without a symptom attached to it. The symptom, when it finally
came, was from JavaFX: a `ContextMenu` opened from a `Canvas` "could not get the focus and could not
be clicked", where the same menu in the connection list worked.

Measured on that client, and it is worth re-measuring rather than re-reasoning:

- EasyShell **selects `FocusChangeMask`** (`0x6ba77f` on its main window - bit 21) and calls
  `SetInputFocus` **once**, for its main window, as it is mapped at startup.
- The server sent **zero** `FocusIn`/`FocusOut` for the whole session. The client asked to be told and
  was never told.
- The client worked around it app-side, and the workaround is the framework's own path: showing the
  menu from `ContextMenuEvent.CONTEXT_MENU_REQUESTED` is exactly what `Control` does internally
  (`Control.contextMenuHandler`: `c.getContextMenu().show(c, event.getScreenX(), event.getScreenY())`
  then `event.consume()`), so a `Canvas` - which is not a `Control` - has to do it by hand to get the
  same treatment. (Their remaining first-time-only failure was unrelated to any of this: the menu
  object had not been initialised yet when the first event arrived.)

What is implemented now, in `EventDispatcher.deliverFocusChange`, called from the only two places the
focus moves - `InputState.setInputFocus` and the revert when the focused window is destroyed:

- **The window losing the focus gets `FocusOut` and the window gaining it gets `FocusIn`**, each to
  whoever selected `FocusChangeMask` on it. One mask bit covers both events, not one each.
- **The detail is the crossing lattice**, the same helper (`crossingDetail`) rather than a second
  copy: how the window on the other side of the change relates to this one, which is how a toolkit
  tells a focus that moved inside its own tree from one that left it entirely. Two details exist only
  for focus: `NotifyPointerRoot` (6) when the other side is the root, and `NotifyDetailNone` (7) when
  it is nowhere at all.
- **A change that is not a change is not reported**, and neither is a window reported as losing a
  focus it did not have - which is what keeps a client from being told the same thing repeatedly.
- **A keyboard grab moves the focus and says so.** Taking one reports the focus moving to the grab
  window with mode `NotifyGrab`, ending one reports it moving back with `NotifyUngrab`, and a change
  asked for *while* a grab is held is reported `NotifyWhileGrabbed`. Four cases were measured against
  a reference server, and two of them are not what prose suggests: a grab taken on the window that
  already has the focus **still reports the pair** (a client grabs to be told it holds the keyboard,
  and a server that treats "already there" as a no-op leaves it waiting), and a grab taken while the
  focus is on nothing at all **reports nothing**, not even to the window it grabbed - the ungrab
  still tells that window the focus left. `input/KeyboardGrabFocusTest` pins all four.
- **`FOCUS_CHANGE` is not in `EventMasks.PROPAGATE`**, so focus events do not propagate. That is
  deliberate and measured.

The rules were taken from a reference server, not from the specification's prose:
`src/test/java/io/github/oyzh1994/xserver/tools/FocusProbe.java` drives the same window tree and the
same requests against Xvfb and against this server, prints every focus event with its detail and mode
and the server's own answer to `GetInputFocus`, and the two outputs are diffed. That is what settled
the things prose does not: which windows are told, in what order, and with which detail. **Its default
sequence now prints the same lines against both servers**, and so do the twelve smaller sequences that
were written to pin the corners - a change to PointerRoot, a change to the root window, the pointer
inside or outside the window being left, a grab on the window that already has the focus. Two of the
rules that came out of them are not in the specification at all, and are noted where they are
implemented: a change *to* PointerRoot reports the window the pointer is in even when that window is
the one it is leaving, and a change to the *root window* does not.

**Two things about running it**, both learned the hard way: the pointer and the focus outlive every
client, so a probe that does not park the pointer and set the focus before it maps anything gets a
different starting state on every run (the focus, like the pointer, is whatever the last client left
behind); and `GrabKeyboard`/`GrabPointer` answer with a reply **before** the events they cause, so a
probe reads the status first and the events after it.

- **Every window on the way in and on the way out is told**, not only the two at the ends. The window
  losing the focus hears first, then the windows holding it, innermost outwards; going the other way
  the holding windows hear first, outermost inwards, and the window itself last - so a toolkit can
  walk down into the window that gained the focus. The windows in between carry `NotifyVirtual` when
  the two ends are related and `NotifyNonlinearVirtual` when they are not.
- **`PointerRoot` and the root window are two states, and this server used to keep one.** Both are
  reported against the window the pointer is in, with `NotifyPointer` - that window and every window
  holding it, on both the gaining and the losing side - and it is the same window on both sides when
  the pointer is already in the window asking for the focus, which is not a no-op: a reference server
  sends the whole pair, and treating it as one leaves the client that asked never told. That was the
  difference between a menu that is dismissed and a menu that is not. But `PointerRoot` is *not* the
  root window: `GetInputFocus` answers `PointerRoot` for one and the root's own id for the other, and
  the events differ - a reference server sends a chain of `FocusOut`/`FocusIn` on the way out of one
  and something else entirely out of the other, measured with `FocusProbe` for both. A fresh server's
  focus is the root **window** (`xdpyinfo`'s "focus: window 0x1ff"), which is what a client gets
  before it asks for anything, and folding the two is how a client that focuses the root to hand the
  keyboard back to the desktop was told a focus it did not ask for. **Where a *key* goes is the one
  thing they share**, and that half was wrong here until 2026-09-29: both deliver a `KeyPress` to the
  window the pointer is in, and only `None` leaves it with nowhere to go. See the omitted-things list
  at the end of this file for the measurement, and `InputState.focusWindow` for the rule.
- **The chain arrives outermost first, on the way in as well as on the way out.** A change to
  `PointerRoot` tells the window holding the one the pointer is in before the window itself, and the
  same for any other chain of ancestors - a toolkit walks *down* into the window that gained the
  focus. This server sent the incoming chain innermost first until `FocusProbe` was diffed against a
  reference server for a case where the pointer was in a nested window, which is the only kind of
  case where the order shows at all.
- **`None` is a state of its own**, kept as null rather than folded into PointerRoot: nothing is told
  it gained the keyboard, `GetInputFocus` answers None, and `focusWindow` returns null so the keys
  have nowhere to go. Folding it into PointerRoot reports a focus the client did not ask for.
- **A window that stops being viewable gives the focus up, and reverts it the way the last
  `SetInputFocus` asked for** - to the nearest ancestor that is still viewable, to PointerRoot, or to
  nothing - whether it is the focused window itself or one holding it. A toolkit that unmaps a dialog
  instead of putting the focus back itself is relying on exactly this, and keyboard events aimed at a
  window nobody can see are keyboard events nobody handles. Destroying a window does the same thing.
- **A click moves the focus to the toplevel it lands in, unless that toplevel is override-redirect**
  (`--focus-on-click`, on by default). This is the one piece of window-manager behaviour in this
  server that a reference does not have, and it is here for an application that never asks for the
  focus itself and is left with it on the root window - a toolkit hides the popup of an owner it
  believes unfocused, so the application's own context menus stop opening. **The exception is the
  other half of the same rule and is not a detail**: a menu *is* an override-redirect window, so
  moving the focus onto it takes the focus off the window the menu belongs to, and the application
  unmaps its own popup. Measured on EasyShell: the input box's context menu opened, took the focus on
  the press, and was gone three milliseconds later - every item dead to the mouse while the keyboard
  still activated them (A23, 2026-09-30; 5 of 5 presses destroyed the menu with the rule missing and
  5 of 5 survive with it, in `--single-window` and in the default per-client mode alike).
  `--no-focus-on-click` is the comparison switch for a run that has to look like a reference's.
- **`FocusOutEvent` is one of the five event classes the protocol generator numbers wrongly**, and
  `ClientOutput` already rewrites it to 10. Focus events therefore have to go out through the ordinary
  send path, not around it, or the client reads a `FocusOut` as something else entirely.

Two traps the probe fell into, both worth an hour of somebody else's time:

- **A request this server accepts may be one a real server rejects.** The first version of
  `RawXClient.setInputFocus` sent sixteen bytes; `xSetInputFocusReq` is twelve, and Xvfb answered
  every one of them with `BadLength` while this server read the length, handed the payload on and
  accepted it. A probe that only ever runs against this server would have gone on passing. `BadLength`
  is also what a probe should print rather than skip: the first version dropped errors silently and
  looked exactly like a server that was sending no focus events at all.
- **Known deviations from the reference, measured rather than assumed**: in the two transitions where
  a revert lands on PointerRoot, this server related the chain to the root where a reference server
  relates it to the window the pointer is in (details `0/1` where it sends `3/4`, and the reverse in
  one other place). Sixteen of the eighteen probe steps matched exactly; those two were in the corner
  where a `RevertToParent` fails and the focus falls through to PointerRoot. **That corner is now
  covered by the new rule** - a revert to PointerRoot sets the state, and the chain is then built the
  way the reference builds it - but it is *unverified*: the probe has no step that destroys a focused
  window, so the two steps those details came from are still the only evidence, and they have not been
  re-measured. Written down so the next person does not have to re-derive them, and knows what to
  check first.

## A client that is not reading is dropped

Every connection has a **bounded queue of outgoing messages and a writer thread of its own**. The
thread is not an optimisation: replies and events were written to the socket on whichever thread held
the server lock - the display tick for an event, another client's request thread for a reply - so a
client that stopped reading filled its socket buffer, the write blocked, and the lock was never
released. One hung client froze every other client's screen, which is the worst failure mode a display
server can have.

- **The queue is the policy**: 1024 messages, which is far more than a healthy client ever has
  outstanding. A full queue is the one failure the output reports rather than absorbs, and the caller -
  the tick, or another client's request - is released instead of blocked. The client is dropped, and a
  real server drops it for the same reason.
- **Order is preserved** because there is one queue and one thread taking from it: replies, events and
  errors leave in the order they were queued, and the writer flushes after each message so a client
  waiting for a reply is not left waiting for the next one.
- **A close drains first, briefly.** A refusal of the connection setup, a final error and the farewell
  a graceful shutdown sends are the last thing a client will hear; closing over them turns "the server
  said no" into "the server hung up", which is exactly what the authentication tests caught when this
  closed immediately. The drain is bounded because the caller may be holding the server lock and the
  client may be the one that stopped reading.

**What it costs**: one more thread per connection. A connection already reads on a thread of its own,
so this doubles that, and the alternative - a single writer with a global queue - is a lock in the one
place that must not have one.

**What is not covered**: a client that reads *slowly* rather than not at all is served in order and
keeps its connection; there is no per-client bandwidth policy, and no coalescing of pending events
(a client that is behind gets every motion, not the last one). A real server drops motion events it
has not sent yet, which is the next thing to do here if a slow client is ever a real case.

## The server generates the keyboard's repeats

A held key speaks once, and the run of characters that follows is the server's own work: a client is
told the key went down and is not expected to press it again. Worth writing down because it is easy to
assume the client does it - and the symptom of getting it wrong is not an error but a terminal that
deletes one character however long Backspace is held.

- The repeats come from the **display tick** (`XServer.pollAutoRepeat`), under the lock the tick already
  holds, once per frame: a key's first repeat `repeatDelayMillis` after the press, the rest every
  `repeatIntervalMillis` - X's own 660 ms and 40 ms (`xset q`'s "delay 660, rate 25").
- **One repeat per key per tick, never a catch-up burst.** A tick that was late - a stalled screen, a
  machine that slept - would otherwise deliver a run of presses at once, which a client prints as
  characters nobody typed.
- **A modifier does not repeat, and that lives in the per-key bitmap** rather than in a rule applied
  when the key goes down: the bitmap's default is "every key the modifier map does not name", which is
  what a stock server's own `auto_repeats` list holds, and a client that puts one back gets it to
  repeat. A repeating Shift is a run of presses no toolkit expects, which is why the default matters.
- **A release stops it and a second press starts a fresh delay**, or a tapped key repeats immediately
  on the next tap.
- **There are two separate things on this keyboard and X keeps them apart**, measured against Xvfb
  before either was implemented here: a *flag* - XKB's `RepeatKeys` control, which is what
  `ChangeKeyboardControl` with no keycode and XKB's `SetControls` both turn on and off - and the
  *per-key bitmap*. `xset r off` clears the flag and leaves the table exactly as it was, so turning
  repeat off and on again gives a client back the keys it had configured; both rails report the two
  separately.
- **The delay and the rate are XKB's** (`SetControls`, behind `XkbSetAutoRepeatRate`) - the core
  protocol has no request for either - and they are read back through `GetControls`. Their reply is the
  extension's long one (92 bytes), and the two settings sit at offsets 20 and 22, which is where
  changing the rate through `xset r rate` moves the bytes.
- **The per-key bitmap in an XKB `SetControls` is ignored, because the reference ignores it** -
  measured three ways, all zeros, all ones, and beside a rate change. Per-key repeat is the core
  protocol's (`ChangeKeyboardControl`) on both servers.

## The keymap a client reads through XKB

There are two ways to ask an X server what a keycode types, and this server answers both from one
table. `GetKeyboardMapping` is the core protocol's; `XkbGetMap` is XKEYBOARD's, and a fair number of
clients - `xdotool`, `xev`, anything built on XKB's own translation - only ever ask the second.
`XkbMap` builds that reply out of `Keymap`, which is the point: a client that reads XKB and a client
that reads the core mapping have to agree about what a keycode means, or the same keystroke types two
different characters depending on the toolkit. Measured with `xev` driven by the same keycodes on both
servers: 26 lines of keysym output, identical line for line, over Escape, a letter, Shift, Caps Lock,
Shift+Caps Lock, Return and the keypad.

**A client can also *set* the keymap** (B1), and **the way `setxkbmap` does it is the by-name load,
not a compiler in the server and not a compiler in the client.** That was measured twice, and both
times the obvious reading was wrong. `strace` on `setxkbmap -layout de` against a reference shows the
whole of what it sends: the XKB handshake, one `GetKbdByName`, and then a root property update - **no
`SetMap`, and nothing else at all**; and the by-name request gets a plain reply from a reference, not
the `BadAccess` an earlier session measured (that was a hand-built probe request of the wrong length,
and a probe that is refused measures nothing). So `setxkbmap` sets a layout by *asking the server
to*, and a server that answers has to answer the way a reference does.

**And the way a reference answers is what this server had to get right: `reported` is not a flag, it
is a list of the component replies that follow it in the buffer.** Xlib's `XkbGetKeyboardByName`
checks `reported` and gives up when it is zero, then reads, for every bit set in it, one whole
component reply - the map for `Types|Symbols`, a compat reply, a names reply, a geometry reply, in
that order - and finally insists the buffer was consumed exactly. So a reply that says `loaded = 1`
and carries nothing is not a partial answer, it is the same failure as not being loaded: this server
installed the requested layout and `setxkbmap` still printed `Error loading new keyboard description`.
What the reply carries now is the map this server answers a `GetMap` with, embedded whole, under a
header whose length counts it, with `reported` claiming exactly the parts that map carries (`Types`,
`ClientSymbols`, `ServerSymbols`).

**`GetKbdByName` is answered with a layout where this server has one**, off tables captured from a
reference (`tools/gen-layouts.sh`) - the same trick D3 uses for the standard fonts, and the reason no
compiler is needed: a display server compiles nothing, it serves what a server that can has already
produced. What a client asks with is not the name but the components the rules file resolved to
(`pc+de+inet(evdev)`), and the request's own shape was read off the wire rather than out of a header:
a twelve byte fixed part and **six** counted strings (keymap, keycodes, types, compat, symbols,
geometry), of which the symbols string is the fifth - a request built with eight strings, which is
what a first reading of the field names suggests, is refused by a reference with `BadLength`. The
mapping a client reads afterwards is the layout's own, **byte for byte the same as a reference's** in
the same layout: `xmodmap -pke` after `setxkbmap -layout <name>` is identical to the reference's for
all seven layouts this server has, and the same command exits 0 with no error. The four requests a
compiler sends on its way to its own upload - `SetCompatMap`, `SetIndicatorMap`, `SetNames`,
`SetGeometry` - are accepted and dropped, and the parts of `SetMap` this server has no place for
(types, actions, behaviours, explicit components, modifier maps) are dropped too, so a client that
uploads its own compilation (`xkbcomp <file> <display>`) gets its characters right and everything else
as it was - a German keymap loaded that way makes keycode 52 answer `y Y` from `xmodmap -pke` and
`0x79 0x59` from `XkbProbe`.

**A names request is answered with the one name that is true.** `GetNames` is what a client reads to
learn which components are loaded, and refusing it is the worst answer this extension can give (the
default Xlib error handler ends the process). The reply's shape was read off a reference's own reply -
and its atoms live in the *tail*, not in the fixed part the protocol's field list suggests, in the
order keycodes, geometry, symbols, physical symbols, types, compat. What this server reports is the
symbols component it was asked to load, and nothing else: that is the client's own statement of the
keymap it wanted and the one component this server acts on, so it is the only name here that is not
somebody else's description of a keymap that is not loaded.

**The four mandatory types are not optional, and that is the trap.** A `GetMap` reply has to carry at
least `ONE_LEVEL`, `TWO_LEVEL`, `ALPHABETIC` and `KEYPAD`, and a reply with fewer is not trimmed by
Xlib - it is **rejected whole**, `XkbGetMap` returns `null`, and the client goes on to dereference it.
The first version here had three types (this server's keymap uses no more) and was byte-for-byte
identical to a reference server's for those three; it still failed, and the failure is indistinguishable
from the extension being absent - which is exactly what `xdotool` crashes on. It was found by bisecting
the reply a section at a time against Xvfb's own, so the note worth keeping is this one: an XKB map is
all-or-nothing, and "the parts this server uses" is not a smaller map, it is none.

The other things measured on the way:

- **The wire layout misleads twice.** The reply's fixed part is **forty** bytes rather than the usual
  thirty-two (the tail holds the modifier map's first/count pair), and each keycode's keysyms follow
  its own eight-byte descriptor **inline** - they are not one pool at the end. Both were read off a
  reference reply with `tools/XkbProbe` rather than from a header, and a first reading that assumed
  thirty-two bytes and a shared pool produced plausible nonsense.
- **The modifier map is a list of `(keycode, mods)` pairs**, not the byte-per-key its field name
  suggests, and its length is the reply's `totalModMapKeys` - sixteen on a reference server, twelve
  here, while `nModMapKeys` counts the key range instead.
- **A key's width is the number of levels it really has**: a key whose two levels are the same is one
  level wide, which is what the reference does with Escape. Padding ours to two would put the next
  key's keysyms in the wrong place.
- **The keypad's digit belongs to Num Lock, not to Shift.** Giving the keypad type a Shift entry made
  Shift+KC79 type `KP_7` where the reference leaves it `KP_Home`; the type selects on Num Lock (this
  server's Mod2) and on nothing else.
- **Caps Lock works through the type, not through the server.** `ALPHABETIC` carries `Lock`, so a
  client translates Caps Lock + `a` to `A` and Caps Lock + Shift + `a` back to `a`, which is what the
  reference does - and none of it is code here, it is the type's own table.
- **The requests with no answer are answered with nothing, not refused.** `SelectEvents` and
  `LatchLockState` have no reply, and a refusal is an X protocol error, which ends a client's process
  under Xlib's default handler; `xdotool` sends a latch request as part of typing a key sequence.
  `PerClientFlags` does have a reply and answers "none supported" (`supported = 0` where a reference
  server says `0x1f`) - JavaFX sends it as an application starts, and an error there is a client that
  is told its request was malformed rather than unsupported.

## An extension is advertised only when the client can use all of it

The rule an extension's absence follows here is not "is there something behind it" but "would a
client that took us at our word be worse off than one that fell back". Two extensions were added for
the opposite reason - XINERAMA and XKEYBOARD - because their *absence* was not a fallback at all:
`xdotool` dereferences the null `XkbGetMap` a server without XKEYBOARD hands back, and dies in its own
startup (A4). Those cost three small replies and one keymap.

**XInput2 is the case the rule was written for, and it is now whole (B4, and 2.4 since A47).** The
version it answers is a reference's own - the last thing that was behind, and the reason was the rule
below read forwards: 2.0 was claimed because 2.4's surfaces would have been refused. They are not any
more: XI 2.1's raw events are delivered, 2.2's touch has no device on either server to have content,
and 2.3's barrier release is answered exactly as a reference answers it. The reconnaissance came
first, and it is why: JavaFX's Linux backend is GTK, `libglassgtk3.so` calls
`gdk_device_grab`/`gdk_device_ungrab`, and **`libgdk-3.so` resolves
`XIGrabDevice`/`XIUngrabDevice`** wherever they exist - measured with `strings` on the AppImage's own
copy of the library. GDK3's device grab *is* `XIGrabDevice` once the extension is advertised, so a
server that answered `XIQueryVersion` and then refused it would not have given a forwarded GTK
application better scrolling; it would have taken its menus away. The extension therefore went in
with its **device grabs, its events and XInput 1's device list** in the same piece of work - and the
last of those showed the rule is not only about XI2: AWT asks `ListInputDevices` as the JVM starts and
`getNumButtons` dereferences the answer, so refusing it **segfaults Swing** rather than making it fall
back. A client that takes the server at its word is not being unreasonable; the server is.

The other half of the same rule is what a client is *told* when something is missing, and it is worth
stating because it is not symmetric: an error is worse than an empty answer. A refusal is an X
protocol error, and Xlib's default handler ends the client's process - which is why `GetKbdByName`
answers a layout where it has one and "not loaded" where it does not, why RandR answers an empty `GetOutputProperty` reply, and
why `PerClientFlags` says "none supported". A client that gets an answer it can read falls back; one
that gets an error does not get the chance.

**And for a request that expects a reply, silence is worse than both.** XI2's `XIGetProperty` - a
*device* property read, which GTK sends as an application starts (a live EasyShell asks for
`libinput Tapping Enabled` on the pointer device) - was refused, because an unknown minor reaches the
hand-written decoder and throws; and "accept it and do nothing" was never an option, because the
client would wait for a reply that is not coming. So it is **answered**, and the answer is a
reference's, measured: this server has no device properties, and a reference answers a read of a
property that does not exist with a plain reply whose only non-zero fields are the header's - `type =
None`, `bytes_after = 0`, `num_items = 0`, `format = 0`. Two details of it were measured rather than
reasoned, and a wrong guess at either would have looked like a plausible number rather than an error:
the request's fixed part is **six** four byte units (a reference refuses a seven unit one with
`BadLength`, so the `delete` byte a longer reading of the request puts at the end is not part of it),
and the reply's byte 1 carries the request's minor opcode, which is what a reference puts there.

## The pointer's history

`GetMotionEvents` answers from a bounded ring per screen - 256 entries, Xorg's own
`MOTION_HISTORY_SIZE` - and it holds what the pointer *did* rather than what was delivered: a motion
swallowed by a grab, or made over another client's window, is part of it, filed under the window the
pointer was in at the time. The timestamps are the ones the events carried, so a client can compare
them with what it has already seen; the caveat is that the server stamps a motion when it *reads* the
request, so a range beginning a millisecond after the client's own clock reading can still contain one
(which is a test that fails one run in four).

## What a subscriber is told

XFixes lets a client ask to be told that a selection changed hands, or that the pointer moved onto a
different cursor, instead of polling. What the notification *says* is not in the specification in the
form the clients rely on, so all of it was measured - `tools/SelectionProbe` and `tools/CursorProbe`
drive the same requests against Xvfb and against this server, and their output is diffed. The two
runs agree event for event once the things a server assigns for itself are normalised away (opcodes,
atom ids, cursor serials), which is what makes the rules below worth writing down rather than
re-deriving:

- **A selection notification names the window the subscription was made on**, not the owner window
  and not the window under the pointer. That is the only thing that window is for: it says which
  screen, and it carries the event.
- **Three ways for a selection to change hands, three subtypes**, and the timestamps tell them
  apart. `SetSelectionOwner` reports the new owner and sets *both* times to now. An owner window
  being destroyed (`SelectionWindowDestroy`) or its client disconnecting (`SelectionClientClose`)
  reports **no owner** and keeps the time the selection was *taken* as the selection's own time -
  nothing changed hands, the selection lost its holder. A client that compares the two times to
  decide whether a notification is news is relying on exactly that.
- **Every `SetSelectionOwner` is reported**, including one that names the owner already there and one
  that gives the selection up to `None`. Both look like a no-op and neither is.
- **The subscription is a resource of the window it was made on**, and dies with it: destroying that
  window stops the notifications. A client that wants to go on hearing about the clipboard keeps the
  window alive, and a client that destroys it hears nothing more.
- **A cursor's `cursorSerial` names the cursor, not the change.** Moving the pointer off a window and
  back onto it reports the serial the cursor had the first time, because it is the same cursor - a
  reference server's serials go back down on the way back. What is counted is cursor *creation*, so
  the counter is assigned where a cursor is built, and the root's own cursor has one too.
- **The cursor event goes with the display, not with a client's drawing**: it is raised from the
  display tick, which is where the server knows what the pointer is over, and the first cursor the
  server can name after it learns where the pointer is is the baseline rather than a change.

A real server refuses every XFixes request from a client that never called `XFixesQueryVersion`
(`BadRequest`), which this server does not - measured, and deliberately left alone: it gains a client
nothing to be told off for it, and every real client asks first anyway. It shows up in a hand-written
probe, which is how this note exists.

## The host clipboard, on the X selection

Copying inside a forwarded application and pasting into a Mac application is the interaction a user
notices most, and it is not a copy between two places that hold data: the X side is a *lazy* selection
- the owner keeps the bytes and hands them over when a requestor asks - and the host side is a
pasteboard that holds whatever was last put there. `ClipboardBridge` is the thing in the middle, and
three of its four decisions are forced rather than chosen.

**Reading the platform pasteboard never happens on a request path.** Every X request runs while the
server lock is held, and a pasteboard read is a round trip to the platform's clipboard service that
can block; a bridge that read it while serving a `ConvertSelection` would stall every other client
whenever that service was slow. A poll thread (200 ms) keeps the text, and the request path reads the
copy. The bridge's own state is guarded by the *server lock* and nothing else, which is what keeps the
two threads from ever holding two locks in the opposite order - and the pasteboard calls happen
outside it, deliberately.

**The X-to-host direction has to be eager.** It is tempting to fetch from the client only when the
pasteboard is read, as the backlog's plan first said; macOS does not tell anybody that a pasteboard
was *read*, so there is no such moment to fetch on. When a client takes the selection the bridge asks
it for the data straight away and puts it on the host. The consequence is visible and should be
expected: copying inside a forwarded application replaces the Mac's clipboard, which is what every
bridge on a platform without a read notification does.

**The bridge owns the selection only while the host has something to serve.** With a picture on the
Mac's clipboard there is nothing an X client could be given that it asked for, so the bridge gives the
selection up rather than answer the paste with an empty string, and a client finds no owner - which
leaves what the user had alone instead of wiping it. And a change to the pasteboard that the bridge
did not make *does* take the selection over from a client that holds it, because otherwise the client
would answer the next paste with what it copied before the user copied something else on the Mac.

**What is not bridged, and why**: `PRIMARY`, because a pasteboard is not a three-button pointer and a
second invisible clipboard nobody can reach is worse than none; images and files, because there is no
X11 target for them that a forwarded application would have asked for; and `INCR` when *serving*,
because the bridge is not a client and is not bound by a request size, so one write serves a property
of any size. `INCR` is honoured when *receiving*, since whether to chunk is the source client's
decision and not one this server gets to make for it.

Two things this cost an hour each, both worth knowing. **A toolkit asks for `TARGETS` first and gives
up if it is refused** - so an owner that serves `UTF8_STRING` and not `TARGETS` looks exactly like a
bridge that does not work, and finding that means looking at which target the client asked for, which
is why that is a debug line in `serve`. And **the shell's `pbcopy`/`pbpaste` were blocked in this
environment's sandbox while AWT was not**, so the host end of a live check goes through Java; the
verification that mattered was a real `java.awt` clipboard on the VM, which *is* the X selection and
the same path a Swing application's clipboard takes.

## The screen grows with the window

A client can only be told a window is as large as the framebuffer behind it, so "the frame can be
dragged bigger" means the *screen* has to move first: a drag past the screen grows it, up to
`--max-screen`, and the client's window is resized into the room that made. The screen never shrinks -
growing is what a user asks for by dragging, and there is no matching gesture that says "and now the
desktop is smaller" - so the extra pixels stay allocated for the session.

Everything about the screen's size is computed from two numbers, so the three parts that own them -
the framebuffer's array, the root window's geometry, and what the setup reply says - change together,
from the display tick and under the server lock. The pixels move into a *larger array* rather than the
framebuffer being replaced: the backends wrap `pixels()` and every picture over the screen holds the
store, so it is the size that changes and not the object. The backends are told afterwards, outside the
lock, through `screenResized`, because a backend that wrapped the old array is holding a picture of the
size the screen used to be.

Two things this cost, and both would look like the feature simply not working:

- **An unmanaged canvas contributes nothing to its parent's preferred size.** The canvas is unmanaged
  on purpose - a managed one puts a floor under the window that the user cannot drag below - so
  `sizeToScene()`, which is what "the window follows its content" had always used, silently did
  nothing: a window whose content changed kept whatever size the user had last dragged it to. The
  window is now sized explicitly from the viewport plus the measured decoration.
- **A window size applied while the user is still dragging is discarded by the platform.** The size is
  therefore re-checked every frame (`fitWindowToViewport`, on the FX thread) rather than only when the
  viewport moves. This is also what brings the frame back when a client *refuses* a resize: the content
  stops growing, the viewport stops changing, and without the per-frame check nothing would ever pull
  the frame back to what it frames.

The decoration is measured when the window opens and not again: the platform applies a new window size
asynchronously, so a measurement taken right after setting one reads the old size and comes out a title
bar wrong - once per viewport change, which walks the window down a title bar at a time. That was
measured, and it is why the measurement lives in `openWindow` alone.

## Typing a character no key has

A keycode says which key was pressed, and a client turns it into a character with its own copy of the
keyboard mapping. That covers everything printed on a keyboard and nothing else - no keyboard has a
key per Chinese character - so text an input method composed arrives at the server as a *character*,
and the server has to make an X client see it. It does that the only way X allows: claims a keycode no
key carries, gives it the character as its keysym (`0x01000000 | codepoint`, X's own spelling for a
character with no keysym of its own), sends `MappingNotify` so every client re-reads the mapping, and
presses that keycode. A character the keyboard *does* carry is left alone - the key press has already
delivered it, and sending it again would type it twice - and the keycode is claimed once and kept,
because a mapping change per keystroke would be a mapping change per keystroke.

This is the platform-independent half of an input method, and it is deliberately the whole of what the
server does: which window an input method draws its candidates in, and how it composes, is the input
method's own business. Every toolkit reports composed text the same way, and everything after the
report is the same X protocol. What the platform reports it *through* is not the same, and that cost
an attempt:

- **JavaFX needs two hooks, and the second is the whole feature on a Mac.** `KEY_TYPED` does not carry
  what an input method committed - measured, by typing Chinese through the host's input method and
  watching nothing arrive at all. It comes in as `Scene.setOnInputMethodTextChanged`'s committed text,
  which is wired up beside it. A platform fact rather than a preference, and one that would have made
  a finished server look broken.
- AWT reports it as `keyTyped`, which is the same event as an ordinary character and needs no second
  hook.

## A drag out of a forwarded application, and the trigger that was wrong

The entry said this needed a native drag source and a lazily-filled promise. It needs neither: **X
says when a drag has started** - a client that decides to drag takes the `XdndSelection` - and a
JavaFX window can start a platform drag from a drag-detected handler. `DropBridge` watches that
selection, asks the owner for `text/uri-list`, and answers a backend's question about what is on
offer. That much is in and tested.

**What was wrong was when to start the platform drag, and the mistake is worth more than the
feature.** The first version started one whenever a client owned the selection - and a client
dragging something **inside itself** takes the same selection, because X's drag and drop is client to
client and an internal drag is just a drag whose target is another window of the same application.
So the host took over a drag the application meant to keep: termora waited for a drop that the
platform had hijacked and froze, EasyShell's internal move never happened. It was reverted the same
day.

The condition that tells the two apart is the **pointer leaving the host window with the button still
down** - the only observable difference between "the user is dragging something out to the desktop"
and "the user is dragging something across the application". macOS keeps delivering dragged events to
the view that received the press, wherever the pointer goes, so a session started from the event that
crosses the edge is the shape the next attempt wants. Until then the X half is inert by design, which
is why it can stay in the tree.

The same session measured the *other* direction and found it stopping short in a way no test had
caught: the application's window publishes `XdndAware`, the bridge's messages arrive and its own UI
reacts, and then it never asks for the data. `DropBridgeTest`'s target is this kit's own client, so
the bridge agrees with it by construction - **a synthetic client is not a client**, and both
directions need a real toolkit before either is called done.

## A drag from the host's desktop

X's drag and drop is client to client, and the *source* drives it: it takes the `XdndSelection`,
tells the window under the pointer what it is carrying, drops, and hands the data over when that
window asks. The target half is a forwarded application's own code, which has been dragging and
dropping on real desktops all along - so the only thing missing is a source on the X side that speaks
for the host's drag, which is `DropBridge`. The platform reports the drop and the files
(`InputSink.hostDrop`); everything after that is a short conversation that has to look exactly like a
drag a client started itself.

The bridge is a **source and not a target**, so it publishes no `XdndAware` of its own and never maps
its window: the drag the user sees is the platform's. Dragging *out* of an application is a different
problem and is not attempted - a JavaFX window cannot start a platform drag, because the platform's
drag is the user's gesture and the toolkit only reports one.

**The bug this turned up is worth knowing about on its own.** `SendEvent` with no event mask sends the
event to the client that *created* the window it names, whatever that client selected - it has to be
that way round, because a target cannot select for a message it does not know is coming. The server
treated "no mask" as "nobody is interested" and dropped the message, which silently broke every
message of this form: all of XDND, the `WM_DELETE_WINDOW` reply an application sends back, and
anything else in EWMH. It is one branch in `AtomHandlers.sendEvent` and two tests in
`ClientMessageTest`, and it had been there since SendEvent was written.


**What a drop cannot do across machines, measured 2026-09-27.** The bridge is a correct XDND source -
a real application answers it (`XdndStatus`, then `ConvertSelection` for the data, which this bridge
serves) - and the application still does nothing, because what it is handed is a **URI on the host's
filesystem**. XDND is URI-based by design: a target *opens* the path, it does not download the file.
For a client on the same machine, or one whose filesystem has the host's directory (a shared folder,
an sshfs mount), that is exactly right; for a client on another machine the path is nowhere, which is
the whole of "nothing happens". The other half of the same finding is that the path has to be one a
target can *use*: macOS reports a dragged file as its resolved path (`/private/tmp/...` for `/tmp`),
and a URI has to escape what a URI cannot carry raw (`#`, `%`, and every non-ASCII byte of a name) -
both fixed in `DropBridge`, both with tests.

## What the frame does when the client asks for something

Two things an application asks its window manager for reach the host window, and both are read off
the client's toplevel the same way the title is: `_NET_WM_STATE_FULLSCREEN` shows the frame full
screen, and `WM_HINTS`' urgency bit brings it to the front once.

**Full screen is the platform's full screen**, which on macOS means another Space and the animation,
not a window stretched over the menu bar. That is what the system does for an application that asks
to fill the display, and it is what a user of that system expects; it is also the one time the frame
is deliberately *not* the size of its content, which is why `fitWindowToViewport` stands aside while
it lasts. **Maximised is deliberately not a host window state**: the frame follows its content, and a
client that maximises resizes its own window - which the screen now grows to fit - so mapping the
request onto the frame as well would be two things sizing the same window.

**The urgency hint raises the frame and nothing else** - not the keyboard, and not every frame: a
window that puts itself in front sixty times a second is a window the user cannot get away from, so
the hint is acted on when it appears and not while it stays. This is not the raise-on-show that C2
describes and that was parked; nothing here happens unless a client says it wants attention.

**The protocol is the awkward half, and it hid a real bug.** A toolkit does not set `_NET_WM_STATE`:
it sends a `ClientMessage` to the root and waits for the window manager to write the state into the
property, and a client that is ignored asks again for ever. Handling that message turned up a
long-standing fault in `AtomHandlers.sendEvent` - the server filled in the event's window field with
the window the event was **delivered to**, which is right for every event that is *reported against*
a window and wrong for a `ClientMessage`, where that field is the subject the sender put there. Every
EWMH request, and the `WM_DELETE_WINDOW` reply an application sends back, arrived with its subject
replaced by the root. The field is now left alone for a `ClientMessage` and filled in for everything
else, which is what the protocol says and what a reference server does.

## Where a window was left

A user who arranges their forwarded windows once expects them to come back there, and nothing in the X
protocol carries that: the position belongs to the host window, which is the server's own. So it is
remembered between runs, in `~/.xserver/host-windows.properties` - the first state this server keeps
after it exits, and a plain properties file so that a user can read it and deleting it costs nothing
but the remembered positions.

**Keyed by `WM_CLASS`**, read off the same window the host window's title comes from. Not the title,
which is what the user is looking at and changes with the document: the class is the application's own
name for itself, and a client that publishes neither has nothing to be remembered by, so it is not
remembered at all.

**Written from the display tick, once the position has held still** (400 ms - the same settle the
viewport uses). The move is reported by the backend, which runs on the toolkit's thread where writing
a file is work done between the user's drag and the next frame, and a drag reports a position per
frame.

Two things this got wrong first, both worth knowing because both look like the feature not working:

- **The position a window *opens* at arrives through the same report a drag does.** Writing it out
  overwrites what was remembered with the very place the restore was about to change, so a client
  would never come back to where it was - measured happening on the first live run, which is why the
  first position of a session is adopted as a baseline rather than recorded.
- **The clamp belongs to the backend, not to the store.** A position remembered on a display that is
  no longer attached is a window the user cannot reach, so `setWindowPosition` keeps the window inside
  the usable area - measured: a remembered `-194,195` came up at `0,195`, with the store still holding
  what the user had left it at, so the intent survives the clamp.

The **size** is deliberately not remembered, and the reason is that the server already works that way:
the host window is sized to the client's windows and dragging its edge resizes the client's window, so
a client that cares how big it is remembers that itself and asks for it next time. A window opened
larger than its content is the failure mode the frame's maximum size exists to prevent.

## Serving the screen over the network

`--backend vnc` (backlog D2) is the fourth backend, and the only one whose screen leaves the machine.
It fits the contract the other three already meet - the display tick calls `present` with the
rectangle that changed, which is what an RFB update *is* - and everything about it follows from two
decisions.

- **The client sets the pace.** RFB is request-driven: a viewer asks for an update and the server
  answers when the area it asked about has changed. So `present` only records the damage (one
  rectangle per session, unioned), the socket is written from that session's own thread, and a viewer
  that stops reading is a viewer that stops being served. That is E5's rule - no other client waits
  for a slow one - and the protocol's own backpressure, which is why a viewer costs nothing when
  nothing is happening.
- **What a viewer gets is the damage, not the screen.** The item warned that "the damage rectangles
  are what make it usable", and until this work they were the whole screen after every draw: a
  keystroke cost four megabytes, because `afterDrawing` marked everything the screen could have
  changed rather than what the request did change. `Rasterizer.drawnBounds` is the fix - the region a
  request really wrote - and `draw/DamageTrackingTest` is its proof. Two misses on the way are worth
  keeping: the older whole-screen mark was left *under* the new precise one for one round of testing
  (so the precision appeared to do nothing), and `ImageCodec` built a rasterizer of its own, which
  meant a `PutImage` was drawn **with no clip** - over whatever covered the window it went into - and
  recorded its region on a rasterizer nobody read.

**The handshake, and the two parts of it that are easy to get wrong.** Version 3.8 (3.3 and 3.7 are
answered in their own terms - and **macOS Screen Sharing announces 3.3**, which is worth knowing
before writing the version branch off as legacy: 3.3 has no security-type list and no
`SecurityResult`, so answering it the 3.8 way leaves the client waiting for a byte that never comes.
Nothing in this server's own tests spoke 3.3 until a real client did.), security `None`, or the VNC scheme when `--vnc-password` or `--vnc-password-file` is given
(the same password, one as a value for an embedder and one as a line for a shell - a value in an
argument is visible in `ps`, which is why both exist).
That scheme is a sixteen byte challenge encrypted with the password as a **DES key with each byte's
bits reversed** - which is not a thing any DES library does for you, and which fails *silently*: the
server simply refuses every password. It is pinned by a test against a vector computed with
`openssl`, because a test that used this server's own encryption to check this server's encryption
would agree with it on any mistake they shared. Apple's Screen Sharing is why it exists at all: it
prompts for a password whatever the server offers, so a `None` server cannot be reached from the Mac
it runs on.

**The clipboard is the same bridge as the Mac's**, which is why it is wired where it is rather than
written again (D2). A viewer's `ClientCutText` is handed to `ClipboardBridge.remoteCopy`, which is the
path an X client's copy takes: it goes to the host's pasteboard, and the bridge serves it to the X
session from there. That keeps one source of truth for "what the clipboard holds", so a copy on the
Mac and a copy in a viewer cannot fight over the selection, and it makes a viewer's copy appear on the
Mac the way every remote-desktop client does. The other direction is the bridge's listener: whenever
the text it serves changes, every viewer is sent `ServerCutText`.

Two things about that are worth keeping. **The listener runs under the server lock**, so a session
only *queues* the text - its own thread writes it, which is this backend's whole shape. And that
thread is usually blocked reading its socket, so the message loop waits for the client's next byte
with a **half-second timeout** and uses the wake-up to send what is queued; the timeout is on the
first byte of a message only and cleared for its body, so a client that dribbles a message out cannot
have it split. Measured live: `ClipboardProbe read` showed a viewer's text in the X selection, and a
`ClipboardProbe write` arrived at the viewer as `ServerCutText`.

**A browser reaches it through the same sessions** (D2's other half). `--vnc-ws-port` starts a
WebSocket listener, and the RFB session above it is *unchanged*, because the session was written
against two streams and a transport interface now sits between it and the socket. The adapter
(`net/WebSocketListener`) does the only translation that makes sense - message payloads are appended
to the stream a reader sees, and a flush becomes one binary frame - so one RFB message is one frame
without anything depending on it.

Two traps in that adapter were real, and both looked like something else. **A read timeout is only
meaningful before a frame starts**: the session sets one to do its idle work (the clipboard), and a
timeout that arrived *inside* a frame left the reader mid-frame - which the session cannot tell from
"the client is quiet", so the next read came from the wrong offset. The first byte of a frame is read
with the caller's timeout and everything after it is read with the timeout retried. And **the
listener closes the connection when its handler returns**, so a handler that starts a thread and
returns has its socket closed under the session - measured, and it looks exactly like a client that
connects and is dropped.

**ZRLE is what a client with a narrow link gets** (D2, 2026-09-26). 64x64 tiles, each written solid,
as a packed palette, as runs, or raw, and the whole update run through one zlib stream per session.
A full 1300x800 desktop of solid colours is 1092 bytes of tiles and 26 bytes on the wire, against
4.16 MB raw.

Four things about the format bite, and one of them bit only on a real desktop. A **CPIXEL** is three
bytes for the common 32bpp format, not four. The **zlib stream is the connection's** - flushed, never
finished, so successive updates share its dictionary. A **run never crosses a tile**. And the
**deflate loop must run until the input is consumed *and* the output buffer came back not-full**: a
call can return zero bytes with input pending, and treating that as done truncated every stream at
exactly four output buffers. Every test with a tile-sized frame passed while a real 1300x800 desktop
made the client close the connection - which is why the ZRLE test set now includes one frame whose
compressed output spans many buffers.

**The pointer travels as an image, not as a position** (D2, 2026-09-26). A viewer that lists the
Cursor pseudo-encoding is sent the X cursor itself, so an I-beam over a text field looks like one. Two
things about it are worth knowing: the rectangle's **x and y are the hotspot**, unlike every other
update, and the mask is one bit per pixel - so a semi-transparent edge travels opaque. Measured live:
an application's own 24x24 cursor arrived as `hotspot=(7,4), 231 masked pixels`, a shape with a hole
in it rather than a filled cell.

It also made `DisplayBackend` grow one method, and the reason is a state this server has had all
along: a session with no client windows is **idle** - the frame holds its last contents and nothing is
presented - so a viewer that connects to an idle screen has no frame to ride along with and would
otherwise draw its own pointer for ever. `presentCursor` is told every frame, whether or not anything
drew, and the backend decides whether it is news.

**What is not there** (the item keeps the list): Tight's zlib path and `CoRRE`, palette RLE, the noVNC
page itself (the WebSocket carries RFB; the page comes from wherever the user keeps it), and any
colour-depth reduction. **Tight's JPEG method is there** (A32): a rectangle whose content looks
photographic goes as encoding 7 with the JPEG control byte when the client has offered both Tight and a
JPEG quality, which is 3.12 MB -> 0.70 MB for a full-screen change of such content; text and flat panels
stay on ZRLE, which is lossless.

## The fonts a client asks for

Core protocol text goes through a font a client opens by name, and for a long time every name was
answered by a Java font chosen to look like it: the metrics a client measured with
`QueryTextExtents` were the JVM's, and so were the shapes it drew. That is visible - a client that
lays a line out from `QueryFont`'s per-character bearings gets it wrong the moment the bearings are
not the font's, and `xterm -fn fixed` drew a proportional font - and it is the font's *own* numbers
that a client acts on, not something it can be told about.

**The tables are captured off a reference server rather than parsed out of a font file**, and that
is the decision worth recording. A PCF reader would mean reimplementing a file format and hoping it
agreed with the server a client is comparing against; taking `QueryFont`'s answer and each glyph's
ink from the reference cannot disagree with it. `tools/kit.sh GenBitmapFont` does it: it opens the
font on a reference display, reads every per-character metric and the properties, draws the whole
table as one strip into a window, reads the strip back with `GetImage` and writes
`resource/BitmapFont.java` - one table per font, rows packed eight pixels to a byte, expanded when a
glyph is asked for. The generated file is checked in, like `CursorFont`, because generating it needs
a display with those fonts.

Four things about the capture were measured rather than assumed, and each of them looks like a
working capture of a *different* font:

- **Text drawn into a one-bit pixmap comes back as the font's default character** - a solid block for
  every code, space included - on a reference server whose `xlsfonts -ll` says the font has all 256
  codes. The capture draws into a window and reads a 32 bit image back instead, and tests for the
  colour the text was drawn in rather than for "not black": the black the strip is painted with comes
  back as `#000001`, so every background pixel is non-zero in its first byte.
- **A character's right bearing is measured from the origin**, not from the end of its advance: a
  glyph five pixels wide has `rightBearing` 5 and a character width of 6. Reading it the other way
  makes every glyph one pixel wide, which draws a font of vertical bars.
- **`QueryFont`'s `CHARINFO`s are padded to sixteen bytes** in the reply (the fixed part is 60 bytes,
  not 52), and **a font's codes do not always start at zero** - 12x24's begin at one, and it answers
  with `min-char-or-byte2` 1 and 255 metrics. A table that assumes 0..255 either reads every field
  four bytes early or draws the wrong glyph for every character.
- **`GetImage` of a rectangle that leaves the window is a `BadMatch`**, not a clipped answer, so the
  capture works in batches: a 1852 pixel strip on a 1300 pixel screen is refused.

**Every other name still gets the Java approximation**, which is the old behaviour kept deliberately:
a client that opens a font nobody here has should get something it can draw with, and a server with
no font at all is a refusal an application cannot work around. What is not pretended is the
metrics - the approximation reports its own, and the README's limitations say which names are real.

## The notification area is the menu bar

A client that wants a tray icon looks for the owner of `_NET_SYSTEM_TRAY_S0` and gives up when there
is none, which is what happened here: an application silently lost a piece of its own interface. This
server owns that selection and is the tray host (C9), and the icons go in the host's menu bar.

**No native code, against the item's own expectation.** The plan said JavaFX/AWT had no menu-bar API
and that this would need JNI. JavaFX has none, but AWT does: `java.awt.SystemTray` puts a `TrayIcon`
in the macOS menu bar, measured before any of this was written. That is one small class
(`display/AwtTrayIcon`) shared by the JavaFX and Swing backends, and the whole of the host's half.

**The X half is `tray/TrayBridge`, and its shape is forced by two things about this server.**

- **The pixels are read from the window's own store, and the icon keeps its shape.** An icon window is
  created with the ARGB visual this bridge publishes, so it keeps its own pixels and they carry the
  alpha the client drew: the bridge reads `XWindow.ownPixels` every 200 ms and hands that to the menu
  bar, and its transparent corners stay transparent. A client that ignored the published visual falls
  back to the old reading - the window's rectangle out of the screen's framebuffer, opaque, and
  reading as what covers it. (This is the correction of an earlier note here, which said the shape
  needed a per-window store "which is B5": it needed the *alpha* half of the store, and that landed
  before the general one.)
- **The selection is taken at startup.** An application asks whether a notification area exists while
  it is starting and never asks again, so a tray that appears when the first window does is a tray
  that application has already given up on.

**A real client found a real bug in the server, and it was not in the tray.** Java's tray peer creates
its icon window, sends the dock request, and then blocks for five seconds waiting to be told it is
embedded - it throws `TrayIcon couldn't be displayed` when nothing arrives. What it waits for is a
`ReparentNotify` on its *own* window, and `WindowHandlers.reparent` delivered that event only to the
*new parent*. A reparent that does not tell the window it was moved is a gap in the server, not in the
tray: X11 says the window hears about it, and the tray was simply the first thing here to reparent a
window it did not own.

**Two deliberate differences from a panel**, both of which a client can see and neither of which this
architecture can avoid:

- The icon is in the menu bar **and** stays where the client drew it, because the window has to be on
  the screen for its pixels to exist. A panel reparents the icon into itself and the icon is then only
  there; here the tray window is where the icon already was, so a reparent does not move it.
- A client's own context menu opens on the X screen next to its window, not under the menu-bar icon.
  The menu is a window the client places itself and no tray host can move it.

## A font file on the font path

The built-in tables answer the names a client is most likely to ask for, and a **font path** answers
every other name an X server would: `SetFontPath` names the directories, `<dir>/fonts.alias` maps a
short name to a full one, `<dir>/fonts.dir` maps that to a file, and the file is read
(`resource/PcfFont`, `resource/FontPath`). `~/.xserver/fonts` is on the default path, so a font
dropped there is served without a flag.

**The file format was read off `libXfont`'s `src/bitmap/pcfread.c` rather than guessed**, and four
things in it are not where the shape of the file suggests. Each one was found by a wrong answer that
looked plausible:

- **The table of contents is little endian whatever the rest of the file is**, and each section's own
  format word is what says how *its* payload is read: byte order, bit order, glyph padding, scan unit.
- **The font's header is in the `ACCELERATORS` section**, not the metrics one - eight flag bytes, then
  the ascent, the descent, the overlap and the bounds. And the section numbers are the pair that is
  easy to swap: **the encodings are 32 and the accelerators 256**. Read the other way round, the font
  header lands where the character map should be and every field decodes as nonsense.
- **What `QueryFont` reports per character is the file's *ink* metrics** (section 16) where it has
  them, not the plain metrics: `8x13-ISO8859-1`'s metrics table says every glyph is the whole cell and
  its ink table says what each one covers, and a client lays text out from the second.
- **The bounds are the extremes over the file's glyphs**, including the ones the encoding table does
  not map, while **a character code the table does not map answers with zeros** rather than with the
  default character's metrics - which were both guesses here until the reference said otherwise.

The path resolution has a trap of its own, and it is a one-line one: **`fonts.dir`'s first line is a
count and the only line to skip**. A font file's name usually begins with a digit
(`8x13-ISO8859-1.pcf.gz` is one), so a reader that skips "lines that start with a number" throws the
table away and falls back to the file that shares the short name - which is what happened here, and
the property list said so: `CHARSET_REGISTRY=ISO10646` where the alias means ISO8859-1.

## Deliberately not done

This is the list of things left out *on purpose*, with the measurement that settled them. The
wider list of what is not implemented yet - including everything a stock server has and this one
does not - is `backlog.md`, which is written to be worked through item by item.

- **XKEYBOARD has no compiler in the server, and nothing it changes is a client's alone.** The map
  (`GetMap`), the state (`GetState`), the controls (`GetControls`/`SetControls`, which is the only way
  the repeat delay and rate can be changed at all) and the version handshake are real, and a client
  that compiles its own keymap can load it (`SetMap` - `xkbcomp <file> <display>` puts a layout on this
  keyboard, and both rails read the one table afterwards). Choosing a layout **by name** is served
  too, off tables captured from a reference, and `setxkbmap` uses it: what is *not* here is the map's
  types, actions and behaviours, the compatibility map, the indicator names, the XKB events, every
  control other than repeat, `SetNames` (accepted and dropped, so a name a client *sets* is not one it
  reads back), and `LatchLockState` - accepted and doing nothing, because a client locking a modifier
  would be locking the machine's own keyboard for every other client. The per-client flags are all
  unsupported and `PerClientFlags` says so, detectable auto-repeat included.
- **`GetKeyboardMapping` follows what the display backends translate**, and letters are looked up by
  character (`Keymap.keycodeForLetter`), because US keycodes are not in alphabet order. What that
  means for a *client* changed with B1: the reply is read from `Keymap`, so a layout chosen by name
  (or uploaded with `SetMap`) is what a client resolves its keycodes through - the backends translate
  the host's keys *into* keycodes, and the layout says what those keycodes mean.
- **A cursor is drawn from its glyphs, and the approximation is left for a font the server has no
  glyphs for.** The cursor font's own 74 bitmaps are embedded (`resource/CursorFont.java`, generated
  from `cursor.pcf` by `tools/gen-cursor-font.py`), which is what `XCreateFontCursor` asks for, and
  `CreateGlyphCursor` against any font this server serves is composed the way X composes one - the
  mask character's bitmap is the silhouette, the source glyph's ink is drawn inside it, the hotspot is
  the glyph's origin at the font's ascent (measured against a reference with `tools/GlyphCursorProbe`,
  identical for fourteen characters - `docs/backlog.md` A8). What still falls back to a filled cell is
  a font the server has no table for at all.
- **A screen grows but never shrinks** (C1). A drag past the edge of the screen makes it bigger, up to
  `--max-screen`, and the host window resizes the client's window with it; there is deliberately no
  gesture that says "and now the desktop is smaller", which is why `SetScreenConfig` and the rest of
  RandR's changing requests are refused.
- **The root is always 24 bit TrueColor, whatever depth a geometry names** (A52). `--geometry WxHxD`
  accepts the `D` because `-screen 0 WxHxD` is how X servers are usually started (the bed's own Xvfb
  runs `x16`), and the server says so once at startup rather than ignoring it silently - measured: a
  `640x480x16` geometry still answers `depth of root window: 24 planes`. Only the *window* visuals
  carry 16/8/1 bit. Honouring a 16 bit root would mean a second whole framebuffer path (packing,
  compositing, `GetImage`) at another depth, which nothing in this bed has needed.
- **A session survives its window, not the server.** `--detach-on-close` keeps the clients running
  with no window and `--control-port` puts one back (D1), but a server that is *stopped* still takes
  its clients with it: persisting across a restart would mean journalling the framebuffer and
  re-establishing the clients' connections, which plain X11 has no way to do. The honest answer is the
  one D1 records - the server stays up instead.
- **The focus does not follow the pointer, and a reference server's does not either.** No
  `FocusIn`/`FocusOut` is generated as the pointer moves, whichever state the focus is in - which was
  verified against Xvfb rather than assumed, because the obvious reading of the specification says
  otherwise and an earlier version of these notes claimed it. What tells a client it has the focus
  there is the crossing's focus bit. `SetInputFocus`, the ends of a keyboard grab and a focused window
  going away are what produce focus events, and that covers what a toolkit actually does. The
  reporting differences that *were* real are listed in `backlog.md` A5, which is now closed: the
  probe's default sequence prints the same lines against both servers, and the rules behind it are in
  "The focus is the client's to be told about" above.
- **Where a key goes under `PointerRoot` and under the root window: the window the pointer is in, for
  both.** The two states are told apart by what they *report* - `GetInputFocus`, and the chain of
  `FocusIn`/`FocusOut` a change between them produces - and not by where the keys go, which is the
  same window either way, with only `None` leaving them nowhere. This was measured on 2026-09-29 with
  a probe written for the question (`tools/KeyTargetProbe`: park the pointer in one of the probe's own
  windows, focus another with `revert_to = Parent`, unmap it, press a key through XTEST) - against Xvfb
  a `KeyPress` is reported against the window the pointer is in for the revert case, for
  `SetInputFocus(root)` and for `PointerRoot` alike, and nowhere at all for `None`. This server sent it
  to the **root window** in the two states that are not `PointerRoot`, which no client is listening
  for, and the cost was **EasyShell's home page losing the ability to type into its search box**: its
  片段 page is a toplevel of its own, the client focuses it and closes it by unmapping it, and the
  revert out of a child of the root lands on the root window. Everything pointer-driven kept working,
  which is exactly the shape of an input bug that is not in the input path. `InputState.focusWindow`
  now sends the keys to the pointer's window from either state, and `InputFocusTest` pins all three
  cases (both states, and the revert that reaches one of them).
- The X11 protocol classes come from a fork of `moaxcp/x11-protocol`, and three of its faults are
  patched in `ClientOutput` rather than worked around at each caller, because every reply and event
  already passes through there: the generator ignores the `number` on an `eventcopy`, which is why
  `ClientOutput.EVENT_NUMBERS` rewrites five event type bytes - and where the correction deliberately
  stays, the library being neither forked nor rebuilt for it (E1); a reply's length field is computed from
  the wrong base and short replies are emitted unpadded; and **a generic event's length field is never
  written at all**. The last one is the one that hides: Present's completion notification arrived as
  thirty two bytes where a reference sends forty, with the same kind, mode and serial inside, so every
  field a reader looked at was right - and the eight bytes of `ust`/`msc` it had not been told about
  stayed in the stream, so the *next* request was read from the wrong offset and the client hung. The
  real fix for all three belongs in the generator.

## Testing notes

`mvn test` runs the suite. It takes no X client and starts no display server of its own, and **no test
opens a host window**: every server a test starts is given `--backend headless`, and the one test that
touches a toolkit (`JavaFxBackendTest`) skips itself where a window cannot be opened. That was not
true until E2 - `HighDpiTest` took the default backend, which opens a real window inside the test JVM,
and a machine with no display parked the toolkit's startup for ever, so the suite hung rather than
failed. On the author's machine Maven is at `~/Documents/Tools/apache-maven-3.9.11/bin` and is not on
PATH; `ci.sh` is the suite as a continuous integration run does it, with that path built in.

End-to-end checks that do need one:

- **The server's own frame dumps are the cheapest way to see what it drew**: run with
  `-Dxserver.dumpFrame=<prefix> -Dxserver.dumpFrames=N` and read `<prefix>-buffer/-image/-scene-N.png`.
  They need no screen-recording permission, and the `-scene` one is the only place the pointer sprite
  appears.
- A **bare-protocol client** drives the server without Xlib being installed: see
  `src/test/java/io/github/oyzh1994/xserver/test/RawXClient.java`, which handshakes, creates windows,
  sends GC-filled rectangles, reads `GetImage`, and moves the pointer through XTEST.
- **A regression test is only worth writing once it has been seen to fail without the fix.** Several
  of the tests here were rewritten after the first version passed against the bug (`pointerKnown`, the
  border clip, and the sprite-damage ordering all needed a sharper test before they meant anything).
- **When the difference is between two servers and Xvfb cannot log, tap the wire instead of reading
  the source.** `tools/tap.py` is a byte-transparent relay - it parses nothing, which is what makes it
  usable in front of a real client where the parsing `XProxy` hangs it - started on the VM in front of
  Xvfb, with the client pointed at `DISPLAY=host:99` and logging switched on for the seconds that
  matter. Decoding the client's stream as requests (`opcode, data, length`, skip the body) is then the
  reference's own answer, in order. That is what A16 turned on: this server's `XIQueryPointer` reply
  named the deepest window instead of the queried window's own child, and the reference's stream shows
  the two-query walk (`child` = the toplevel, then `None`) that the wrong answer turns into a thousand
  a second. **Prefer a measurement of the two servers to an argument about which is right** - three
  sessions of reasoning about that flood produced nothing, and the tap produced the answer in an
  afternoon.

## Platforms

**The product is cross-platform; only a handful of conventions are the platform's own.** The X side -
the protocol, the handlers, the input state machine, the framebuffer - has no platform in it at all.
The display backends are chosen with `--backend`: `javafx` (all platforms JavaFX runs on), `swing` (AWT,
for a machine where JavaFX will not start), `headless` (a server with no window, for tests and for a
machine reached over the network) and `vnc` (the screen served to a viewer instead of to a window).

**Everything platform-specific, in one list** - this is the inventory a change has to keep true:

| what | where | on a platform that is not macOS |
| --- | --- | --- |
| the key that leaves full screen | `JavaFxBackend.keyFor` - the **only** `os.name` read in the codebase | `F11` instead of Control-Command-F. Never Escape: JavaFX consumes that before the scene sees it, so a forwarded terminal, editor or IDE would lose its own Escape |
| the window's own title bar | `StageStyle.EXTENDED` + `HeaderBar`, behind `Platform.isSupported(EXTENDED_WINDOW)` | the platform's decorated window and no header bar of ours - the same check that keeps two title bars from appearing |
| `Cursor.NONE` hiding the pointer process-wide | `JavaFxBackend.hostCursor`, the `CURSOR_OWNER` bookkeeping | NONE is an ordinary "no cursor" for one window; the loan-and-give-it-back discipline is harmless there |
| a full screen window being reported as *maximized* too | `JavaFxBackend`'s maximized listener | the same guard is inert |
| the unix domain socket at `/tmp/.X11-unix/X<n>` | `ClientListener.startUnixSocket` | the family is asked for *before* the directory is made, so nothing is left behind where it is unsupported, and the TCP listener carries on (`unix domain socket unavailable: ...`) |
| the notification area | `AwtTrayIcon`, through AWT's `SystemTray` | the menu bar on macOS, the notification area on Windows, whatever the desktop provides on Linux |
| the display's own scale (`--scale auto`) | `Screen.getOutputScaleX/Y` | 1x where the display is not scaled |
| **the toolkit that owns the application**, and so is the one the platform asks to quit | `XServer.claimHostApplication` (A46) - AWT is asked for before a JavaFX window can be built | inert: AWT is the toolkit the clipboard and the tray are already built on, a process with both toolkits in it is the ordinary JavaFX-in-Swing shape, and no other platform turns an application's Quit into "close every visible window" the way macOS's Dock does |
| the testbed's tools | `tools/*.c`, `tools/vm.sh`, `tools/host-*` | **an exception, by the owner's own rule: the tools may be platform-specific, the product may not.** They are AppKit and CoreGraphics probes and ssh to this project's test bed - written for the machine the tests are driven from - and *if debugging on another system needs tools of its own, tools for that system are written then*. Nothing under `src/main` uses them, and that is the part that matters |

**What the audit found, beyond that table** - the complete list of places where `src/main` touches the
platform at all, so that the next reader can check the list instead of the code:

| what the code does | where | why it is not a dependency |
| --- | --- | --- |
| reads `os.name`, once | `JavaFxBackend.keyFor` (A45) | a *convention*: the answer for every platform is in one testable function, and the fallback is what most platforms share |
| compares `File.separatorChar == '\\'` once | `DropBridge`'s file URIs | it is *platform-aware* rather than platform-bound: the other branch is POSIX, and both are the same rule |
| looks in a few X font directories | `FontPath.defaultPath` | a **search list**: only the ones that exist are used and the built-in tables are always last, so a machine with no X fonts at all still answers every font request |
| reads `user.home` | `HostWindowStore`, the font directory a client may set | the JVM's own answer on every platform |
| shells out | nowhere | the only `Runtime` use is a shutdown hook, and there is no `ProcessBuilder` in `src/main` at all |
| uses an internal API | nowhere: `sun.misc.Signal` is *mentioned* in a comment in `ControlListener` as the thing the control port exists to avoid (`ControlListener`'s own words) |

**The rules a change has to follow**, which is what "cross-platform" means here:

1. **Nothing in `src/main` may require one platform** - not "should prefer not to": a change that only
   works on one platform is a bug in this project, and the tools' exception is the tools' alone. A
   capability that is not universal goes behind a check with a fallback - A capability that is not universal goes behind a
   check with a fallback - `Platform.isSupported(ConditionalFeature.EXTENDED_WINDOW)` above is the
   shape of it, and the JavaFX preview feature it needs is opted into by the backend itself rather than
   by a command line (see A43).
2. **A convention - a key, a path, a gesture - is read from the platform, and defaulted to the common
   choice.** `fullScreenExitKey` is the model: the answer for every platform is in one place, is
   testable without a window, and the fallback is the convention most platforms share.
3. **A platform's quirk may be handled, but the code has to say which platform it is and what happens
   elsewhere.** Every macOS measurement in these notes names macOS and says what the alternative is -
   that is documentation, not a platform requirement.
4. **A platform whose answer is "no" degrades, it does not fail**: no extended windows means a decorated
   window, no unix socket means TCP, no JavaFX means Swing, no display at all means headless.
