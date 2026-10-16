---
name: xserver-frame-path-byte-at-a-time
description: "The 'much slower than Xvfb+x11vnc, especially EasyShell, every operation very slow' report had two halves, both fixed 2026-09-30. The FRAME PATH: per-pixel copies, fills and image uploads -> row-bulk, measured on the real bed: the server's CPU for EasyShell on the aliyun SSH tab went ~90-100% -> ~20%. The WIRE: the VNC session re-sent the whole damage rectangle even when the pixels equalled what the client had (5.39 MB/s and 61 updates/s on a screen that was not changing, against x11vnc's nothing) -> the update is the box the differing pixels are in, and an unchanged one is not sent at all (0.00 MB/s, the request held)."
metadata:
  node_type: memory
  type: project
---

**The report (2026-09-30):** "this xserver is much slower than `Xvfb` + `x11vnc` on `.147` watched from
the Mac - especially EasyShell; open a connection (aliyun) and every operation is very slow."

**This is the open `300 fps / full core` item.** It was measured on the real bed this session, not
guessed: run the server **on `.147`** with `--backend vnc` (the same shape as the `Xvfb`+`x11vnc`
baseline), point EasyShell on `.147` at it, open the aliyun SSH tab, and read the server's CPU.

**Measured A/B, same machine, same app, same state (aliyun connection open), settled:**

| build | server CPU |
|---|---|
| `git archive HEAD` (original) | **~90-100%** (about a full core) |
| this session's fix | **~20%** |

**The cause is the frame path's pixel loops, and the profile said which ones.** `top -H` named the
EasyShell connection thread (`xserver-client-N`) as the hot one, and repeated `jstack` of it showed:

1. `XServer.compositeOwnPixels` → `Framebuffer.copyFrom` - the copy of a window's own pixels onto the
   screen that ends **every** drawing request - was a per-pixel loop (a `setPixel`/`pixel` interface
   call and a per-pixel `markDirty` for each of ~900k pixels).
2. `DrawHandlers.copyArea` → `Rasterizer.copyRect` - the terminal's scroll, a window copying its own
   pixels or a **pixmap's** - was per-pixel too, with a clip test each.
3. `RenderExtension.fillRectangles` → `Compositor.draw` → `blend` - what a toolkit paints its panels,
   text backgrounds and alpha clears with - was per-pixel, and it was the single hottest frame.
4. `ImageCodec.putZ` (a `PutImage`, what a JavaFX/GTK frame upload is) was per-pixel through
   `Rasterizer.writePixel`.

The request-body read and the image decode were byte-at-a-time as well (a 4.16 MB image: **~16-17 ms
per byte loop, ~0.5 ms in bulk**), which is real but was not the whole core - the pixel loops were.

**The fix (branch `bugfix`, this session; all measured with the bed above):**

- `Framebuffer.copyFrom`: a **row-wise `System.arraycopy`** when the value needs no conversion (this is a
  window store, or the source is a non-alpha store); the pixel loop stays the general answer.
- `Rasterizer.copyRect`: a **row-wise copy** when the GC is a plain copy, the clip is the box, and the
  whole source is inside the source - and it accepts an **XPixmap** source (EasyShell's CopyArea blits
  from one), which is what it was falling back on before. It marks the store's damage too
  (`DamageTrackingTest` caught the miss).
- `Rasterizer.writeRun` + `ImageCodec.putZ`: a `PutImage`'s row is decoded into an `int[]` and written in
  bulk, not through `writePixel` per pixel.
- `Picture.fillArgb` + `Compositor.fillRectangles`: an **opaque colour under `Src`/`Over`** and **`Clear`**
  fill the writable rectangles (the same rectangles `isWritable` tests point by point) a row at a time.
- Plus, from earlier in the session: the request **body is read in bulk** (`ClientConnection.readFully`),
  `readPutImage` copies the image out of the request's own bytes, and three per-request `log.info` lines
  (`PutImage …`, `RENDER AddGlyphs …`, `RENDER CompositeGlyphs …`) are `log.debug` now.

**Verification.** New `draw/FramebufferCopyTest` holds the `copyFrom` fast path to the pixel-by-pixel
answer (two surfaces, same-surface overlap in both axes, off-edge). `DamageTrackingTest`,
`RasterizerTest`, `ImageCodecTest`, `BlendFastPathTest`, `AlphaMaskTest`, `ArgbAlphaTest`,
`VariableLengthDecodersTest` and `ZrleEncoderTest` are green, and a screenshot of `:95` with the aliyun
tab open renders correctly (terminal, SFTP list, toolbar). The bed was **started and stopped this
session** (server + EasyShell on `.147`); the leftover `Xvfb :11/:12` and `tap.py` on `.147` are not
mine.

**How to reproduce the measurement** (what this cost to get right): the server belongs **on `.147`**
(`--backend vnc`), not on the Mac - a Mac-side server puts every X request on the LAN and is not the
baseline. The classes go over with
`tar czf - target/classes | ssh … 'tar xzf - -C /root/xs'`; start with
`setsid nohup java -Djava.awt.headless=true -cp "target/classes:xslib/*" … --display 95 --geometry
1300x800 --backend vnc --vnc-port 5995`. **On the Mac the server needs `-Djava.awt.headless=true`** or
`GraphicsEnvironment` aborts the JVM (SIGABRT, no `hs_err`). Drive EasyShell with `xdotool` on `.147`
(`DISPLAY=:95`), double-click the host row at ~(62,169), and read CPU with `top -b -n1 -p <pid>` after
it settles; the hot thread is found with `top -H` + `jstack`.

## The wire half, later the same day (A25)

**The report after the fix above:** still "less smooth than Xvfb+x11vnc". This half is not CPU, it is
what a viewer is **sent**, and it was found by counting it.

**The instrument: `tools/vnc-meter.py`** (committed). A hand-written RFB client that does the handshake,
asks for ZRLE, keeps **one incremental request outstanding** - a real client's own pace - and prints
MB/s, updates/s and the seconds a request was **held** (the server sitting on it because nothing
changed, which is what `x11vnc` does). `--trace` prints every update's rectangle; `--dump` decodes ZRLE
into the client's own picture as an XWD, so `tools/kit.sh Xwd diff` can compare it with `xwd -root` of
the server's framebuffer. Two traps paid for here: the meter's first handshake wrote three padding
bytes where the message needs **four** (type + 3), and an XWD `header_size` is in **bytes** (100), not
ints - 25 shifts every pixel by most of a row and still looks like a picture.

**Measured (EasyShell, aliyun tab open, untouched, server on `.147`, `:95`/5995 vs `Xvfb :98`/5900):**

| | our server | `x11vnc` |
|---|---|---|
| idle | **5.39 MB/s, 61 updates/s** | **0.00 MB/s** (request held) |
| pointer moved across the window | 5.42 MB/s | 0.00 MB/s |

61/s is the tick rate: the application repaints its whole panel ~300 times a second **without changing
a pixel** (A21's measurement), so the tick had damage every frame and `updateRequest`/`sendUpdate` built
a full-window ZRLE for it every time. `x11vnc` compares 64x64 tiles against what the client has and
sends nothing when they are equal - which is why it feels smooth: the viewer decodes nothing at all for
a screen that is not changing.

**The fix (committed, `RfbSession`):** a per-session `sentPixels` (what this client has been sent, null
until the first frame, dropped on a resize); `sendUpdate` compares it against the framebuffer and sends
the **bounding box of the pixels that really differ**; nothing differs -> nothing is sent and
`updateRequest` loops back into its wait, so the request stays outstanding until something really
changes. Rows compare with `Arrays.equals` and only a differing row has its extent found pixel by pixel.
A **full** request is still always answered. The region buffer and the ZRLE tile buffer are reused
(a full-screen update used to allocate 4 MB plus one `int[4096]` per tile). A new test,
`VncBackendTest.aRedrawThatChangesNothingIsNotSentButAChangeAfterItIs`, holds it: an identical redraw is
not sent, the change after it is, as the box that differs.

**After: idle 0.00 MB/s and 0 updates with the request held for the whole run; pointer motion 0.00 MB/s**
(the sprite's box is damage, its pixels do not change, and a viewer draws its own pointer); typing,
scrolling and a `yes | head -3000` flood cost 0.01-0.05 MB/s, and `x11vnc` costs the same order on that
flood. Server CPU is unchanged (~23%) because what is left is the application's own drawing - A24's
half.

**End-to-end check that a skip is not a lost update:** after the flood, the meter's decoded picture
differs from `xwd -root` of the framebuffer in **136 of 1,040,000 pixels, all inside 583,678..590,694** -
the text cursor's cell, where the blink raced the capture. The whole suite is green (463 tests).
**The trap to remember:** the comparison is **ARGB**, stricter than the client's packed pixel, because
skipping a real change is a wrong screen and sending one update too many is only a cost.
