---
name: xserver-deferred-features
description: "xserver features that were designed, reverted, and their state: window-close/idle session ends are done, screen auto-size and raise-on-show are still deferred"
metadata: 
  node_type: memory
  type: project
  originSessionId: 20e98c73-c9b5-4cc8-9d2e-466cca178651
  modified: 2026-09-22T12:30:26.848Z
---

On 2026-09-22 the user asked for three features, approved a plan, then had the work reverted
mid-implementation to prioritise a cursor bug. The full plan is in
`~/.claude/plans/temporal-marinating-gizmo.md` (overwritten since, so this is the surviving record).
**State as of the evening of 2026-09-22: (1) is delivered a different way, (2) is not done, (3) is
implemented.**

A later request - "each X client forwarding should open its own window" - landed as **one screen per
connection with one window per screen** (`ScreenSession`, per-connection `Screen` created in the
handshake, `DisplayBackend.setViewport`/`releaseToolkit`, and a per-session display tick). That also
answers the user-visible half of (1): a window is sized to the union of its client's top level
windows, so a small application no longer arrives in a screen-sized window with black around it. The
**framebuffer itself is deliberately not grown**: the viewport shows a sub-rectangle of it, which
avoids making `Screen`/`Framebuffer` sizes mutable and avoids telling a client a root size it can
never see. `--single-window` restores the old shared screen.

1. **屏幕跟着客户端窗口自适应** – still to do. Grow the X screen (root + framebuffer + host window) to
   fit a client's top-level window, floor at the configured default, shrink back when it goes away.
   Key decisions: `Framebuffer.resize` must reallocate **in place** (`Picture.store` is final and
   every backend captured the array); apply the resize from the **display tick**, not from the
   request thread, because the JavaFX/AWT input callbacks take `serverLock` (holding it while
   waiting on `runOnFxThreadAndWait` deadlocks); the backends rebuild their `PixelBuffer`/
   `WritableImage`/`Canvas`/`Scene` (JavaFX) or panel/`BufferedImage` (Swing); cap the size
   (`--max-screen`, default 8192x8192) since the setup reply casts to `short`; take the server lock
   in `ConnectionSetup.sendSetup` so a client cannot be told a size the framebuffer does not have.
2. **渲染后置顶一次** – still to do, and now with a second reason to do it. `DisplayBackend.raise()`
   (JavaFX `stage.toFront()`, Swing `frame.toFront()`), set a `raisePending` flag when a client's
   top-level window is mapped and consume it in the tick after a frame was actually presented.
   Diagnosed on 2026-09-22: a client window that appears while the IDE is frontmost has a **grey
   title bar** - it never becomes the active window - and macOS ignores cursor changes made by a
   background application, so `Cursor.NONE` never takes effect and the desktop arrow sits next to
   the client's cursor until the user clicks the window. Raising/focusing the window once when its
   client first draws is what would fix that; the user parked it (2026-09-22) rather than pay the
   "a window that jumps to the front on every client" cost.
3. **两个方向的会话收尾** – **done** (`XServer`, `--close-grace-ms`, 2026-09-22). Both ends used to
   leave the other hanging:
   - Host window closed → `askClientsToClose()` sends ICCCM `WM_DELETE_WINDOW` (`RawEvent` +
   `EventDispatcher.send`, only to root children that list the protocol in `WM_PROTOCOLS` — read as
   big-endian 32 bit items, which is how `AtomHandlers.normalize` stores properties), then the
   display tick waits out the grace period, then the clients are cut off and `stop()` runs, so the
   JVM exits and `ssh -X` returns. Verified end to end: with `ssh root@vm xclock` running against the
   server, clicking the window's close button made the client quit within a millisecond and the ssh
   process return.
   - Last client gone → `updateIdle(true)`: the server stops presenting frames (the window holds the
     last one, after one final flush with no sprite so the pointer is not left painted on it) and
     calls `DisplayBackend.setIdle(true)` so the host pointer is handed back to the platform; a new
     connection resumes rendering. The user chose freeze-and-keep-serving over ending the session,
     precisely so the next client does not need a server restart.

Obscuring support (`XWindow.visibleRegion()`, a region-aware `Rasterizer`, `Picture` regions,
`visibilityChanged`/`repaintUnder` in `WindowHandlers`) was implemented afterwards and is a
prerequisite for the auto-size work landing cleanly, since both touch the clip path. The Expose bug
that broke `xclock`/`xeyes`/`xcalc` came out of that work — see [[xserver-open-bugs]].
