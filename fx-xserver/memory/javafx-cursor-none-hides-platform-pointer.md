---
name: javafx-cursor-none-hides-platform-pointer
description: "Hiding a JavaFX window's pointer on macOS: ImageCursor is ignored outright, Cursor.NONE works but is a process-wide hide; and screencapture -C cannot verify either"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 5fd800a7-a3fe-47c3-9a5a-ed03afe05ced
  modified: 2026-09-22T12:17:28.738Z
---

The three things that make "keep the host pointer off the X window" hard in this repo, all measured
on the machine (macOS + JavaFX 25.0.1 loaded from the classpath) on 2026-09-22:

- **`Cursor.NONE` works and is a process-wide hide.** `MacCursor.set()` sets a static `isCursorNONE`
  and `setVisible_impl()` calls the native `_setVisible(false)` → `[NSCursor hide]` in
  `GlassCursor.m`; JavaFX only lifts it when a window's cursor changes *away* from NONE. So it is
  applied as a loan: `JavaFxBackend.hostCursor(pointerOverWindow)` returns NONE while the pointer is
  over the window and DEFAULT everywhere else, re-applied on pointer enter/exit, on window close and
  in `stop()`. Left in place it takes the pointer off the desktop and off every other application -
  the bug the user first reported.

  `pointerOverWindow` is set by *any* mouse event inside, not by `MOUSE_ENTERED` alone, because a
  window that opens under a stationary pointer never gets an enter event at all.

  **A background application cannot hide the pointer at all** (measured 2026-10-01, A28). A native
  probe - no JavaFX in it - called `[NSCursor hide]` and then watched `CGCursorIsVisible`: 0, then **1**
  the moment another application was activated, and never back. JavaFX does not even retry:
  `MacCursor.setVisible_impl` returns **without the native call** when its cached `isNSCursorVisible`
  already equals what is wanted (visible = wanted && !isCursorNONE), and the cache is stale from the
  moment the platform re-shows the cursor. Forcing it through `DEFAULT` then `NONE` in one FX task does
  not help while another application is in front. So the policy is: hide only while **this process has
  a focused window** (`JavaFxBackend.applicationActive`), and **do not draw the sprite** while it does
  not - one pointer either way, which is what the two reported "two pointers" cases were. `A28` in
  `docs/backlog.md` has the numbers and the screenshot harness.
- **`ImageCursor` is ignored by this platform - do not reach for it.** It looks like the clean
  answer (scoped to the window, no global hide) and does nothing: an **opaque red** 32x32 image
  cursor was measured still showing the standard macOS arrow over the window. JavaFX does its part -
  `ImageCursor.getCurrentFrame()` returns an `ImageCursorFrame`, not the `Cursor.DEFAULT` fallback -
  so this is the native side dropping it, and no choice of pixels helps. (A fully transparent image
  was earlier blamed for a white square; that square is really the *sprite*, see below.)
- **Handing the pointer back is a *queued* call, and a window closed in the same turn loses it**
  (A55, 2026-10-13, JavaFX 27 sources read on the machine). `Scene.setCursor(DEFAULT)` only marks
  `CURSOR_DIRTY`; the cursor reaches the platform from the scene's own pass inside a pulse
  (`Scene.syncAll` -> `Scene.mouseHandler.updateCursorFrame()` -> `Scene.peer.setCursor`), and
  `ViewScene.setCursor` posts the native part through `Application.invokeLater` with an
  `if (platformView != null)` guard - the same queue `Platform.runLater` uses
  (`QuantumToolkit.defer`). A close that happens in the same turn as the release therefore disposes
  the scene before the task runs, the task no-ops, and because `MacCursor.isNSCursorVisible` is a
  **stale cache** that still says "hidden", no later `NONE -> DEFAULT` lifts it either: the desktop
  keeps no pointer until another application is activated (measured: the toolkit's own record read
  `false` a minute after the window had gone). `JavaFxBackend.handBackPointerThenClose` closes the
  window **two pulses after** the release for exactly this reason. A55 in `docs/backlog.md` has the
  numbers and the harness.
- **`screencapture -C` cannot verify any of this.** It composites the cursor into the image itself
  and ignores whether the app has hidden it: a capture with the cursor deliberately visible and one
  with it hidden by `Cursor.NONE` came out **byte-identical** (same crop, same pointer position). So
  screenshots can only show *which* cursor image is current (that is how the red-image test above
  worked), never whether the pointer is hidden. The reliable signal is JavaFX's own record,
  `com.sun.glass.ui.mac.MacCursor.isNSCursorVisible`, read by reflection from inside the server JVM:
  it read `false` while NONE was applied and `true` for an image cursor.

Two harness facts from the same session, both in [[xserver-testbed]]: `CGDisplayMoveCursorToPoint`
moves the hardware pointer **without generating a mouse event** (JavaFX never sees it, so the server's
pointer - and with it the sprite - stays wherever it was), while a `CGEventPost` of
`kCGEventMouseMoved` does deliver a real move (`/tmp/movemouse.c`).

Related and still open: the *sprite* the server draws for a client that uses `XCreateFontCursor` is a
solid filled cell (`XCursor.fromGlyph` fills the whole glyph cell), i.e. a white square where the
client asked for an arrow - the likely source of the white square the user reported, and separate
from everything above.
