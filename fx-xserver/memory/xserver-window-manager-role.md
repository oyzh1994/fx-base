---
name: xserver-window-manager-role
description: "How the host window acts as the client window's frame - title, icon, resize - and the two JavaFX traps that cost the most time"
metadata: 
  node_type: memory
  type: project
  originSessionId: 08e7b4ed-e369-4572-96d6-f959078dae3d
  modified: 2026-09-22T23:43:27.663Z
---

The host window is the frame around the client's window, so it does the three things a window
manager's frame does. Added 2026-09-23 (uncommitted); the durable write-up is in
`docs/design-notes.md`, section "The host window is the client's window frame".

- **Title and icon** come from the client's largest mapped, non-override-redirect top-level window
  (`WindowIdentity`, `_NET_WM_NAME` or `WM_NAME`, `_NET_WM_ICON`). `AtomHandlers.notifyProperty`
  tells `XServer.windowPropertyChanged` on every write; that filters to those three atoms, because a
  toolkit writes properties constantly and rebuilding the identity copies the icon each time. The
  session holds the identity and the tick applies it - the usual pattern - and the identity is
  compared **by reference** so "new" and "the same" need no pixel comparison.
- **Dragging the window's edge resizes the client's window**, through the ordinary configure path so
  the client gets a real `ConfigureNotify`. Capped at the screen: the framebuffer is not resized
  with it, which is the deferred screen-auto-size work.

Two rules added the same evening, both after the user reported the frame jumping:

- **A drag is measured in what the frame can show, not in the client window's size.** EasyShell's
  window is `1300x700+-50+20` on a 1280x800 root, so only 1250x700 of what it asked for was on the
  screen. Setting the window's size to the number the drag reported shrank its *width* when only the
  height was dragged. `XServer.windowExtent`/`visibleExtent` convert between the two, and a
  dimension whose visible size did not change is left alone. **Since 2026-10-13 (A54) that window is
  no longer the state an application starts in**: `XServer.fitContentOnScreen` moves a window that
  arrives off the left or the top onto the screen and grows the screen to its far edge, so the
  conversion is left guarding the far edge and `--max-screen`.
- **A menu must not size the frame it opens in.** A context menu is an override-redirect top-level
  window, and the frame is the union of the client's top-level windows - so counting menus as content
  grew the frame when one opened and shrank it back when it closed (measured 700 to 740 and back).
  `contentBounds` now sizes the frame to the client's *normal* windows, with popups counted only if
  that is all the client has mapped. **The cost, accepted deliberately:** a popup extending past the
  client's window is now clipped at the frame's edge; stretching the frame to fill the screen is the
  remedy, and it is what makes those popups reach the edge anyway.

- **The window is told how large it may be dragged** (`DisplayBackend.setMaxClientArea`, = screen less
  where the content starts). Without it the user can drag past what the client's windows can fill: the
  frame is left bigger than its content (black, the scene's fill showing where the canvas is not) and
  then pulled back to the content - "javafx 窗口又弹回去了，中间部分黑黑的", reported 2026-09-23. The
  decoration is measured (stage size minus scene size) **where the window is known to fit its content**
  (`sizeToScene`, and once on show), not when the limit is applied, or a window already dragged too
  large reads as decoration that does not exist. On macOS it measures `0.0 x 28.0`.

**Why:** the two traps below each took a measurement to find, and both look like they should work.

**How to apply:** two JavaFX facts that are not obvious from the code -
(a) a `StackPane` root **centres** its child, which is invisible while the canvas is exactly the
scene size and wrong the moment the window is dragged: every coordinate the backend reports shifts
by half the difference. The root is a `Pane` and the canvas is `setManaged(false)`.
(b) a *managed* canvas imposes its size as the window's **minimum**, and a canvas is not resizable,
so a 1200px canvas stops the window ever being made narrower. Unmanaged is what makes shrinking work.
And the one-way guard: the backend sizes the window to the client itself, and each of those resizes
also changes the content area - without `adjustingWindow` plus an "already told" test they come back
as user resizes for ever. See [[xserver-working-state]].
