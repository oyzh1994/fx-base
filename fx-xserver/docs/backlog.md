# Backlog: what this server does not do yet

This is the working list of everything this X server is still missing, written so that each item can
be picked up on its own: what the code does today, why it matters, how to build it, what has already
bitten somebody, and how to prove the result. It complements rather than repeats:

- `README.md` - what is implemented, and the limitations a user should know about;
- `docs/design-notes.md` - why the server is built the way it is, including the deviations that are
  deliberate and the platform traps.

**How to use it.** Every item has a stable id (`A1`, `C4`, ...) so a branch, a commit or a test can
name it. Tick the box in the item's heading when it is *done and verified* - verification means the
check under **Verify** was actually run, not that the code looks right. Items in
[§7 Deliberate deviations](#7-deliberate-deviations-do-not-fix-without-re-measuring) are **not** bugs;
do not "fix" them without re-measuring, because each one was measured to break a real client.

Sizes: **S** ≤ half a day, **M** 1-2 days, **L** a week or more, **XL** a project of its own.

## How this is meant to be worked

**Checking the whole server rather than one item?** `docs/verification-checklist.md` is that tour:
every area, what to drive it with, and what "pass" means - plus the list of things that are
deliberately different and the traps that make a run lie. This file is the history behind it.

This file is a **hand-off to a new session, on a new branch**. Nothing here is in progress; each item
is waiting for somebody with a clean context to pick it up. The loop, per item:

1. Read the item's **Current** and **Traps** before opening the code - more than one of them records a
   change that was made, measured against a real client, and reverted.
2. Make a branch (or a commit) named after the id, e.g. `a7-auth-requires-credentials`.
3. Change the code, then **run the item's own Verify step** - `mvn test` alone is not a verification.
4. Add a regression test that fails without the fix. The design notes' testing section says why this
   matters here: several tests in this repo were first written so loosely that they passed against the
   bug they were meant to catch.
5. Tick the box in the heading and record what the verification measured - two lines now beat a
   re-derivation in three months.
6. Update `README.md`'s "Known limitations" and `docs/design-notes.md`'s "Deliberately not done" when
   they list the item: a fixed item still listed there is worse than no documentation.

A prompt that starts a fresh session on this, verbatim:

> Read `docs/backlog.md` and implement the next unchecked item, in the order of the §0 table unless I
> say otherwise. Work on a branch named after its id. Before changing anything read the item's
> *Current* and *Traps*; after changing anything run its *Verify* step and add a regression test that
> fails without the fix. Then tick the item and tell me what the verification measured.

---

## 0. All items at a glance

| id | item | area | size |
|----|------|------|------|
| ~~E0~~ | ~~finish the verification kit~~ **done 2026-09-25** | engineering | S |
| ~~A6~~ | ~~a keyboard-grab focus change is reported `NotifyNormal`~~ **done 2026-09-25** | correctness | S |
| ~~A14~~ | ~~crossing flags byte, focus bit, chain order and detail~~ **done 2026-09-25** | correctness | S-M |
| ~~A7~~ | ~~`--auth` can be bypassed by offering no credentials~~ **done 2026-09-24** | security | S |
| ~~A3~~ | ~~font cursors are drawn as a filled cell~~ **done 2026-09-24** | correctness | S |
| ~~A4~~ | ~~`xdotool` segfaults against this server~~ **done 2026-09-25** (the cause was B1) | correctness | S-M |
| ~~A2~~ | ~~a bounding shape does not clip drawing~~ **done 2026-09-24** | correctness | S-M |
| ~~A11~~ | ~~no keyboard autorepeat~~ **done 2026-09-24** | correctness | S-M |
| ~~A10~~ | ~~`GetMotionEvents` returns nothing~~ **done 2026-09-24** | correctness | S |
| ~~A12~~ | ~~window gravity, `winGravity` and `bitGravity`~~ **done 2026-09-26** (both halves, measured against the reference) | correctness | S-M |
| ~~A8~~ | ~~XFixes selection/cursor notifications are never delivered~~ **done 2026-09-24** | correctness | S-M |
| ~~A5~~ | ~~`PointerRoot` focus reporting: the premise was wrong, five measured differences remain~~ **done 2026-09-25** | correctness | M |
| ~~A1~~ | ~~passive grabs are recorded but never matched~~ **done 2026-09-25** | correctness | M |
| ~~A15~~ | ~~a filled polygon's scanline crossings are taken half a pixel low~~ **done 2026-09-25**: the `xlogo` difference is now 0 pixels | correctness | S |
| ~~A9~~ | ~~`IncludeInferiors`, backing store, save-unders~~ **done 2026-09-25** (save-unders measured: the reference does not implement it either) | correctness | M-L |
| ~~A13~~ | ~~`AllowEvents` freeze/thaw: a `Sync` grab stops the device, `AllowEvents` starts it again~~ **done 2026-09-26** (X's grab states, the held queue, the replay, and the pointer's two positions) | correctness | M |
| ~~A16~~ | ~~a click outside a context menu leaves EasyShell spinning on `XIQueryPointer`~~ **done 2026-09-27**: the reply's `child` was the deepest window instead of the queried window's own child | correctness | M |
| ~~C1~~ | ~~screen auto-size (grow the screen with the window)~~ **done 2026-09-24** | host window | M |
| ~~C2~~ | ~~raise/activate the host window when a client window appears~~ **done 2026-09-25** (the live A/B is inconclusive: the bed activates it either way) | host window | S-M |
| ~~C4~~ | ~~bridge the host clipboard to the X selections~~ **done 2026-09-24** | host window | M |
| ~~C6~~ | ~~HiDPI automation (`--scale` without hand-editing the client's flags)~~ **done 2026-09-24** | host window | S |
| ~~C7~~ | ~~remember the host window's position per client~~ **done 2026-09-24** | host window | S |
| ~~C3~~ | ~~fullscreen / maximize / urgency from the client~~ **done 2026-09-24** | host window | S-M |
| [~] C5 | host ↔ X drag and drop (XDND): **the drag in works and is owner-verified** with `--drop-mount` over a filesystem the client can see; **the drag out is parked** with the reverted trigger | host window | M |
| ~~C9~~ | ~~tray icons (`_NET_SYSTEM_TRAY`) in the macOS menu bar~~ **done 2026-09-26** (the server is the tray host; AWT's `SystemTray` needs no JNI) | host window | M |
| ~~C10~~ | ~~input methods: the host's composing, and the client-side XIM (three font-path bugs stood in the way)~~ **done 2026-09-26** | host window | M |
| ~~C11~~ | ~~a server that cannot open a window~~ **done 2026-09-25**: the hang, the silence and the end of the fallback chain | host window | S |
| ~~B2~~ | ~~RandR~~ **done 2026-09-25** | extension | M |
| ~~B3~~ | ~~Xinerama~~ **done 2026-09-25** (the three a client asks; the 1.1 extras measured and left) | extension | S-M |
| ~~B4~~ | ~~XInput2~~ **done 2026-09-26**: the protocol's spine, the events, the device grabs, and XInput 1's device list (which AWT segfaults without) - see the entry for what that cost | extension | L |
| ~~B1~~ | ~~XKEYBOARD: read, set, and a layout **by name**~~ **done 2026-09-27**: `setxkbmap -layout <name>` works (its reply has to carry the keymap), `GetNames` is answered, and all seven layouts are byte-identical to the reference's | extension | M-L |
| ~~B7~~ | ~~DPMS and the XScreenSaver extension~~ **done 2026-09-25** | extension | S |
| [~] E2 | continuous integration: the suite runs headless, `ci.sh` is the one command, and it was measured from an empty repository; **the pipeline will be GitHub Actions** once the GitHub mirror exists (Gitee is a code host only) - parked until then | engineering | S |
| ~~E4~~ | ~~per-client framebuffer memory~~ **done 2026-09-26**: allocation is lazy (verified live); freeing while a client is connected is declined, with its reasons in the entry | engineering | M |
| ~~E5~~ | ~~backpressure to a client that has stopped reading~~ **done 2026-09-25** | engineering | M |
| ~~E3~~ | ~~performance: the opaque-source copy path (1.85x), the A8-mask path (2.80x) and the damage region~~ **done 2026-09-25** | engineering | M-L |
| ~~E7~~ | ~~a server that shows nothing must not say it opened a window~~ **done 2026-09-26** (E2's orphan) | engineering | S |
| ~~D3~~ | ~~core-font fidelity~~ **done 2026-09-26** (the standard X fonts are the real ones, captured off the reference; a font file on a font path is still not read) | large | M-L |
| ~~B5~~ | ~~DAMAGE, **every window keeps its own pixels**, and **Composite, Present and SYNC**~~ **done 2026-09-27** - the general store, RENDER included, and the three a compositor needs | extension | L |
| ~~C8~~ | ~~one host window per client toplevel~~ **declined 2026-09-27**: built twice (the second time on B5's store, and it worked), then rejected - the requirement is **one host window per application** | host window | L |
| ~~E1~~ | ~~the `eventcopy` generator fix~~ **closed 2026-09-27**: the library is not forked, built or republished (the owner's decision, and no Lombok), so the rewrite in `ClientOutput` **is** the fix - the seventh of the library's defects worked around here - pinned by `EventTypeByteTest` | engineering | S |
| ~~D1~~ | ~~session persistence and re-attach~~ **done 2026-09-25** (`--detach-on-close` and a control port; a server *restart* is still out, as the item says it is) | large | L-XL |
| [x] D2 | built-in remote transport: **VNC** - raw, ZRLE and (since A32) Tight's JPEG method for photographic rectangles, the Cursor pseudo-encoding, password or not, clipboard both ways, and a browser path with no bridge | large | L-XL |
| ~~A17~~ | ~~a frame JavaFX draws on a timer never reaches the screen - the caret does not blink~~ **closed 2026-09-27**: the caret blinks on both builds, and the hover issues no drawing request at all | correctness | M |
| ~~A18~~ | ~~a click on termora's 密钥管理器 button throws `Nonexistent button 16` in AWT here and opens the dialog on the reference~~ **fixed 2026-10-03**: the wire and the XI2 framing were clean all along - **the `first_event` this server reported for the XInput extension was 0**, and a base is where a client library hooks its wire-to-event converter, so libXi's *device* converter landed on core `ButtonPress` and AWT read every click's root **y** as the button number. A reference answers 66 (and 85 for XKEYBOARD, the same trap under libX11, also fixed); both ranges are reserved now, and `ExtensionEventBaseTest` pins it | correctness | M |
| ~~A19~~ | ~~EasyShell's process dies when it copies~~ **fixed 2026-09-30**: the clipboard bridge asked a client for its data through a window of the **master screen**, which in the default mode is a root no client's setup reply names - GDK wraps the requestor of a `SelectionRequest` and dies in `XScreenNumberOfScreen`. Every earlier attempt had run `--single-window`, where that window *is* on the client's screen | correctness | M |
| ~~A23~~ | ~~a forwarded application's context menu opens and then ignores the mouse~~ **fixed 2026-09-30**: `--focus-on-click` gave the input focus to the **menu itself** - an override-redirect window the window manager never focuses - so the window the menu belonged to was told `FocusOut` and the toolkit unmapped its own popup; 5 of 5 presses destroyed the menu before the fix and 5 of 5 survive after, in both modes | correctness | M |
| ~~A22~~ | ~~a forwarded application's input fields ignore their context menu and every Control shortcut~~ **fixed 2026-09-30**: two causes - the toolkit's connection was killed by a **BadMatch on a cursor whose hotspot is outside its 1x1 placeholder pixmap**, and the **XI2 key event left the word at 72 nought**, which is where GDK reads the modifier state from | correctness | M |
| ~~A21~~ | ~~with the SSH tab open the **pointer sprite flickers**~~ **fixed 2026-09-29**: part-screen damage that misses the sprite leaves last frame's copy in the canvas, and `drawCursor` composites it twice - the sprite's box is unioned into the damage now, and `main` damaged the whole screen so it could not happen there | correctness | M |
| ~~A24~~ | ~~"much slower than Xvfb+x11vnc, especially EasyShell - every operation is very slow"~~ **fixed 2026-09-30**: the frame path's copies, fills and image uploads were per-pixel - now row-bulk; measured on the bed, the server's CPU for EasyShell's aliyun tab went **~90-100% -> ~20%** | performance | M |
| ~~A25~~ | ~~A24's remaining half: the same screen still felt less smooth than Xvfb+x11vnc~~ **fixed 2026-09-30**: the session re-sent the whole damage rectangle even when the pixels equalled what the client had - **5.39 MB/s and 61 updates/s on a screen that was not changing**, against `x11vnc`'s nothing; now the update is the box the differencing pixels are in, and an unchanged one is not sent at all: **0.00 MB/s with the request held**, and pointer motion costs nothing | performance | M |
| ~~A26~~ | ~~`java.lang.IllegalArgumentException: Destination region ... must fit within texture content bounds`, sometimes, and the window never appears~~ **fixed 2026-09-30**: the frame's repaint rectangle covered the pointer sprite's own box, which reaches past the screen when the pointer sits on the last pixel - and JavaFX does not clip a `drawImage` destination region. Reproduced on the real backend (21 throws with the pointer at the right edge), 0 after clipping the union and the sprite; the canvas is in logical pixels | correctness | M |
| ~~A27~~ | ~~the dock's **Quit** did not quit: the window closed and the clients were released, but the process stayed for ever (and an app with no window heard the Quit not at all)~~ **fixed 2026-10-01**: on macOS a JavaFX app's Quit is `GlassStage.requestClosingAllWindows()` - a close on every window, never `System.exit` - and the toolkit's `JavaFX-KeepAlive` thread is not a daemon and outlives `Platform.exit()`. `XServer.startEventLoop` now returns the display thread and `Main` waits for it, then exits the process: EasyShell released at **+1s**, the server gone at **+3s**, both modes | correctness | S |
| ~~A28~~ | ~~**two pointers** over the window: the system arrow *and* the X sprite, until the window is clicked - and again while another application is being used in front of it~~ **fixed 2026-10-01**: the pointer belongs to the **active** application - a native probe watched `[NSCursor hide]` be undone the moment another application was activated, and JavaFX's own cache never retries - so the sprite is what gives way: it is drawn only while this process has a focused window. Measured: active = system pointer hidden + sprite drawn, in the background = system pointer visible + **no** sprite, both by screenshot | correctness | M |
| ~~A29~~ | ~~a client could make the server allocate 17 GB: a window's own pixels were the one allocation with no budget~~ **fixed 2026-10-01**: a 12000x12000 window died with `OutOfMemoryError` in `Framebuffer.allocate` while it was being mapped (every other drawable already had a budget - a pixmap 16M pixels, an image 64M, a request 16 MB). `keepsOwnPixels` now requires the window to fit the largest screen (8192x8192) and a bigger one draws straight onto the screen, which is the root's own path: nothing is refused, and no window that fits on the display changes | robustness | M |
| ~~A30~~ | ~~the ZRLE encoder looked every pixel up in a palette the tile could no longer use~~ **fixed 2026-10-01**: past sixteen colours a tile can only be plain RLE or raw, and neither reads the palette, so the search from the seventeenth colour on was pure work. Photo-like full frames encode in **57 ms instead of 79-100**; two-colour and text-like frames are unchanged (the numbers are in the review) | performance | S |
| ~~A31~~ | ~~the VNC path sends **one rectangle per update**, so a change in two corners is encoded as one box spanning both~~ **fixed 2026-10-01**: the region is cut into 64x64 tiles, a changed tile merges with the changed tiles beside it, and each run is shrunk to the box of the pixels that really differ - A25's answer for one change, no box spanning the screen for two. The old behaviour sent `[5,5,285,185]` where the new one sends two 10x10 rectangles | performance | M |
| ~~A32~~ | ~~no **Tight/JPEG**: photographic content goes as ZRLE-raw, 3.1 MB and 57 ms for one full frame~~ **fixed 2026-10-01**: a rectangle whose sampled colours say "photograph" goes as **Tight's JPEG method** (encoding 7, control byte 0x90, the compact length, a JFIF stream) when the client offered both Tight and a JPEG quality - measured on the wire: **3.12 MB -> 0.70 MB** for the same full-screen change, and 56 ms -> 31 ms of encoding. Flat panels and text stay on ZRLE, which is lossless, so nothing is blurred that did not need it | performance | L |
| ~~A33~~ | ~~`XWindow.ownPixels()` can still be called for a window that keeps no pixels - A29's budget depends on callers asking `keepsOwnPixels()` first~~ **fixed 2026-10-01**: all eight call sites were audited and do ask first, and `ownPixels()` now writes one warning per window naming it and the budget, so a future caller that gets it wrong leaves a breadcrumb instead of an unexplained `OutOfMemoryError` | robustness | S |
| ~~A34~~ | ~~a rectangle that "looks photographic" went out as Tight's JPEG method even when the client never listed Tight or a JPEG quality~~ **fixed 2026-10-03**: the eligibility test was the whole condition, so a viewer that asked for ZRLE alone (a scripted client, some VNC clients) could be handed an encoding it cannot decode - which reads as a rectangle that never updates. JPEG now needs `tightWanted && jpegQuality >= 0` **and** eligibility, and the session logs the viewer's whole encoding list on connect (the first thing to read when a picture looks wrong) | correctness | S |
| ~~A35~~ | ~~"compared with Xvfb+x11vnc seen in the Finder, what this server renders looks less clear"~~ **answered 2026-10-03, and it is the display, not the renderer**: at the default `--scale 1` the client draws 1x and the host window on a Retina panel draws each framebuffer pixel as a 2x2 **block** - measured on a capture of the window: **95.6% of 2x2 blocks are one colour**, i.e. magnification with no added detail - where the Finder's view of a 1x remote screen is 1:1. Our own VNC path is lossless for text (measured: a full frame and scrolling text, with Tight and a JPEG quality in the client's list, produced **0 JPEG rectangles**). The levers are `--scale 2`/`--scale auto` (real 2x detail for clients that follow `Xft.dpi`, which GTK/Qt do and the JVM in termora did **not** - its block ratio stayed ~95% under both `--scale 2` and `-Dsun.java2d.uiScale=2`), or watching the server over VNC so the same viewer presents the same picture | clarity | M |
| ~~A36~~ | ~~"what is still missing against Xvfb"~~ **re-measured 2026-10-03**: the extension set is unchanged (23 each, DPMS here against MIT-SHM there) and every version a client is told matches **except XInputExtension - a reference answers 2.4 where this server answers 2.0**, which is the one capability a modern client can still reach for (AWT logs it, GDK asks for 2.4 and falls back). §9 now carries the re-measurement, and E0's sweep re-run says 17 of 28 probes identical with the two new differences explained | capability | M |
| ~~A37~~ | ~~**XTS (`xts` 5.1.5) against the `Xproto` set**: 121 of 122 purposes failed~~ **two root causes fixed 2026-10-03**: (1) a **BIG-REQUESTS request longer than the maximum got `BadLength` and then the connection was closed** - a reference replies and *discards* the request, so the client's next request is answered; XTS's `TOO_LONG` case (which sends `bigreq_size + 1` and then all sixteen megabytes) went from `UNRESOLVED`+`SIGPIPE` to **PASS**, and (2) **`AllocColorCells`/`AllocColorPlanes` handed out writable cells on a read-only visual** (TrueColor and the statics), where the protocol says `BadAlloc`. Down to **1 case per purpose (120 of 122)**, all of them the same missing piece: **per-request length validation** (a request whose length is one unit more or less than its type defines must be `BadLength`), which needs a table of exact-vs-at-least sizes - see the entry | correctness | L |
| ~~A38~~ | ~~double-clicking the title bar does not maximize, and a second double click does not restore - "it stretches partway and stops", and the behaviour differs from click to click~~ **fixed 2026-10-03**: macOS zooms a window to a "standard frame" worked out from its **content**, and this window's content is a client's X window at the size the client last asserted - so the first double click added height only (1280x828 -> 1280x886), the second added width (-> 1512x886), and the platform's own resize arrived *behind* ours and shrank the client's window back. The gesture is still the platform's (`Stage.maximizedProperty` fires on it); the geometry is the server's: fill = the display's usable area (the screen grows to fit), a resize while filled = restore the remembered size, and the platform's own state is cleared with it so the next double click is the same gesture again. Verified five double clicks in a row: 1512x886 <-> 1280x828 | correctness | M |
| [ ] A39 | `--single-window`: maximize stretches only partway and a second double click does not give the window back; a right-edge drag nudges the window sideways. **One attempt was made and reverted the same night** - sizing the screen on *every* gesture put a feedback loop under the drags (the viewport is derived from the screen and the window's size and position from the viewport), which showed as a window that drifted downwards a pixel per step in per-client mode and jumped up and down during a left/right drag in single-window mode, and it broke both modes' maximize. **Design (b) is in**: single-window mode takes its viewport from the clients' `contentBounds`, exactly like a per-client window, so its window is an ordinary frame around the client windows and the working maximize/restore path applies to it. **The edge drift is fixed 2026-10-04** (the entry has the measurements): the frame was being resized to the client's window on *every flushed frame*, dragging included, and the platform's live resize then took its next step from that smaller frame - a 40 pt pull of the left edge shrank the window 156 pt. The frame now leaves the drag alone while it is in flight and then puts back the edge the user was *not* pulling; all four edges measured exact, in both modes, with a snapping client and with a hint-constrained one. What is left is design (b)'s trade-off (the window shows the client windows, not the whole screen) for the user to judge | correctness | M |
| [ ] A40 | **un-maximizing puts the window back at the right size but not where it was** - a window moved to 506,286 and then maximized comes back at 680x270 at **40,38**, a fixed corner rather than the remembered spot. Measured on the *pre-change* build too, so it is older than the drag work, and the user has not reported it as such ("双击标题栏就是最大化，再次双击恢复之前的大小" was about the size, which is exact) | correctness | M |
| ~~A41~~ | ~~single-window **full screen** grows nothing, often will not restore, and takes the Dock and menu bar with it (they come back only after a lock and unlock) - "本来应该跟最大化和退出最大化一样，能变大和恢复"~~ **fixed 2026-10-04**: macOS reports a full screen window as **maximized** as well, so A38's fill ran a moment after the Space was entered and **resized a full screen window** - and a window whose frame is changed leaves its Space without giving it back, which is the Dock the user had to lock and unlock to get back. The fill also shrank the client's window from the full screen area (1512x945 -> 1512x922) and the zoom then flipped back, so the restore ran against the wrong remembered size. The frame is now the platform's for as long as the window is full screen (`platformOwnsFrame`), the zoom report is ignored there, and the client's window grows and comes back through the ordinary path. Verified by script in both modes, with a client that resizes itself while full screen, and with the user's own ⌃⌘F exit. See the entry | correctness | L |
| ~~A42~~ | ~~**the platform pointer is not handed back after leaving full screen**: "全屏后退出后，这时候鼠标会消失，拖回到x服务窗口恢复" - the cursor was gone over the desktop, came back over the window, and stayed right on the next trip out~~ **fixed 2026-10-04**: whether the pointer is over the window - which is what decides whether the platform's arrow is hidden - came only from JavaFX's enter and leave events, and a window that **moves or changes size under a pointer that does not move** is exactly what those events miss: leaving full screen shrank the window away from a stationary pointer, no `MOUSE_EXITED` arrived, and the arrow stayed hidden globally. The answer is now taken from where the platform says the pointer is, and re-decided whenever the window's geometry changes. Verified by log in the direction that matters - the decision is made **600 ms before** the event that used to be the only trigger (and is made at all when that event is suppressed). The measurement traps are in the entry: `screencapture -C` draws a hidden cursor and `CGCursorIsVisible()` always answers "visible" | correctness | M |
| ~~A51~~ | ~~**the VNC password as a value** (the user's report: "密码明明配置为123456了，但是finder连接却失败了")~~ **fixed 2026-10-04**: the value form itself was fine - what Finder could not do was finish the *handshake*, and then the picture and the input each had a bug of their own. `--vnc-password VALUE` is in (A52's entry has the rest); the three real bugs were the **RFB 3.3 SecurityResult** Apple waits for, the **ZRPE CPIXEL** for a client that asks for depth 32, and **input queued behind an outstanding update request** - each measured against Finder's own Screen Sharing and pinned by a test | correctness | M |
| ~~A52~~ | ~~**the whole option surface, verified on a real machine** ("各种配置项都验证下…要完整验证所有配置项看下是否都生效")~~ **run 2026-10-04**: every option in `ServerOptions` was started for real and probed with a real client (the VM over TCP) or the host's own window list / a real RFB handshake. **Two bugs fixed**: `--title` was read by nobody (every backend got `DEFAULT_TITLE`) and **`--max-clients` had no reader at all** (a `--max-clients 2` server served four connections when the probe kept them open). One difference recorded: the depth in `-screen 0 WxHxD` is accepted for compatibility and ignored, and the server now says so at startup | correctness | M |
| ~~A53~~ | ~~**EasyShell and termora, every backend × both window modes** ("各种backend都测试下，各种窗口模式也都是测试下…窗口弹窗，输入，右键功能，右键菜单")~~ **run 2026-10-06, its findings fixed 2026-10-08 and 2026-10-09**: everything renders and responds in javafx/swing/headless single-window, javafx/swing per-client and vnc, and the **single-window framebuffer is byte-identical across all four backends**. The finding was the user's **"中文输入实际上都是英文字符"** - three bugs in a row: the scene was never an input method client (the focus owner has to carry the requests *and* the handler), a focus that never changes is never re-decided, and the composed character was claimed on **keycode 255**, which a real toolkit cannot resolve. Then two more, from the follow-up: the **pre-edit is now typed into the field and taken back when it changes** (`ni hao` while 你好 is being typed; `a a a a a` then Return commits `aaaaa`), and **each composed character gets a keycode of its own**, because one keycode re-assigned per character is how 你好 reached a field as 好好. Verified with the Mac's Pinyin source into a forwarded EasyShell text field. Three application-side behaviours (a synthetic click on termora's menu items, on EasyShell's title-bar items, termora's empty Quick Connect) were reproduced **identically on Xvfb** and are not this server's | correctness | M |
| ~~A54~~ | ~~**EasyShell is clipped on the left and the right without a suitable `--geometry`** - "如果不指定合适的geometry，152主机上的easyshell明显左右都被裁剪掉一小部分显示，xclock、xeyes却正常"~~ **fixed 2026-10-13**: the client is not the screen's business to crop. EasyShell's own window is **1300x700** and it centres itself on the screen it was told about, so on the default **1280x800** screen it lands at **`-10+33`** - ten pixels off each side - and the frame showed the 1280 columns that exist. xclock and xeyes fit, which is the whole of why they were never seen clipped. `XServer.fitContentOnScreen` now runs the rule C1 set for a drag - **the screen follows the window** - on the moment a window is *mapped*: a window that starts off the left or the top is moved onto the screen (pixels at a negative position do not exist, so no framebuffer size can show them; the client is told with an ordinary ConfigureNotify), and the screen grows to the far edge of what the client has mapped, up to `--max-screen`. EasyShell then sits at `+0+33` on a 1300x800 screen, whole, with no geometry asked for | correctness | M |
| ~~A55~~ | ~~**closing a forwarded window takes the desktop pointer with it** - "在x窗口直接点击关闭窗口，这时候x服务的窗口也退出了，但是鼠标也跟随消失了，应当在这时候恢复系统鼠标"~~ **fixed 2026-10-13**: the platform pointer is hidden as a *loan* (`Cursor.NONE` is a process-wide `[NSCursor hide]` on macOS), and handing it back is **not a native call in the same turn** - the scene's cursor is applied from its own pass inside a **pulse**, and `ViewScene.setCursor` posts the native part through `Application.invokeLater` behind an `if (platformView != null)` guard. A window closed in the same turn as the release therefore won the race: the posted task no-ops against a disposed scene, and the stale `MacCursor.isNSCursorVisible` cache means nothing lifts the hide afterwards - the toolkit's own record read **false** a minute after the window had gone. One path now does it (`JavaFxBackend.handBackPointerThenClose`): hand the pointer back, then close the window **two pulses later**, consuming the platform's close request. Measured on the real machine, pointer parked inside the client's page: the arrow came back both when the **client exited by itself** (the reported case) and when the **frame's close button** was clicked | correctness | M |
| **A56** | **on Windows the platform clips a frame that comes out larger than the display, so the sides of the client's own window go missing** - "在windows还是有问题…本机看下窗口左右两侧为啥还是被裁切了" **fixed 2026-10-14**: A54's rule works on the *X* screen - the client's window is put on it and the screen grows to hold it - which is all macOS needs, because macOS draws a window larger than the display and lets the user move it into view. **Windows does not**: a top-level window wider or taller than the display's *usable* area is clipped by the platform, so the part outside is never drawn and cannot be reached. The frame is one point per framebuffer pixel over `--scale`, so a client that fills the screen makes a frame very nearly the size of the display, and the fix draws the content at the largest factor (=1 in every ordinary case) that keeps the whole frame - content, this backend's header bar and the platform's frame - inside the usable area, as a VNC viewer does when the remote screen is bigger than its window | correctness | M |
| **A57** | **a frame waited for the end of the display period to be drawn, so the picture lagged the client by up to a whole period** - "另外优化任务，尽可能优化渲染效率和速度，目前感觉有点延迟，没有xvfb转发，vnc连接的哪种丝滑感" - **done 2026-10-14**: it was waiting, not working (a frame's blit is 0.013 ms and the server is at 1.7% of one core), in two places that were both the clock: the period was counted from the **end** of a frame's work (so the work was added to every frame - 18.7 ms measured where 16.7 was asked for - and a late wake-up was carried into every frame after it), and a frame that the client had already drawn still waited for the end of the period *and* then for the toolkit's own pulse. `Framebuffer.setDamageListener` -> `XServer.requestFrame` now draws a frame when it is drawn, and the loop keeps its period against an absolute clock. Measured: 54 -> 100 frames a second and 6.0 -> 1.9 ms from the client's draw to the frame, same CPU | performance | M |
| ~~A50~~ | ~~**the SHAPE set of the X Test Suite**, and the XI/XIproto sets~~ **run 2026-10-04**: SHAPE is now **11 of 11 passing** (it was 5 of 11 failing, then 10, then all) - the second fix was the window **border width** in the shape extents; - a reference answers `ShapeGetRectangles` with ordering **YXBanded (3)** for every shape a client sets (measured: Unsorted, YSorted and YXBanded inputs alike), where this server answered `Unsorted (0)` 'with no promise', which is what five XTS cases read as `Expected order of 3, Received 0`; the list is now banded for real and masks are decomposed into their own rectangles. The XI and XIproto sets cannot run here: **107 of XIproto's 107 tests are UNTESTED and 279 of XI's 316**, because both sets are XInput **1.x** protocol this server implements only as far as the device list (A49's neighbour, §9's XI1 hole). A reference passes 88 of 107 there and fails 55 of its own - the target is its numbers, not a clean sheet | correctness | M |
| [ ] A49 | **pointer barriers' blocking half** - the last piece of XI 2.3: XFIXES 5.0 plus `CreatePointerBarrier`/`DestroyPointerBarrier`, the `XI_BarrierHit`/`XI_BarrierLeave` events and the clamping itself (a barrier stops the pointer until its owner releases it). **Recorded, deliberately not built**: nothing in this bed can use a barrier, and the one server that has them cannot demonstrate the blocking (see the entry for the measurements, the cost and how it would be verified) | capability | M |
| ~~A48~~ | ~~**the device model**: this server listed **two** devices where a reference lists six, and named the master as the source of every event~~ **done 2026-10-04**: the device set is a **mutable table** and it is now a reference's own six - the two masters, the two **XTEST** slaves, and the two the host's input arrives on (where a reference keeps its backend's "Xvfb mouse"/"Xvfb keyboard"; named `xserver mouse`/`xserver keyboard` here, the one choice that is not a measurement). XTEST input carries its slave as `sourceid` and host input carries the host's device; **`XIChangeHierarchy`** is implemented (`XIAddMaster` creates the four devices a reference creates, `XIRemoveMaster`/`XIAttachSlave`/`XIDetachSlave` move them, `XI_HierarchyChanged` goes to the clients that selected it with a reference's shape and flags); **XI's passive grabs** are armed, answered and taken back on the table the core grabs use; and the device-state events are selectable on `XIAllDevices` alone. Verified with `xinput` itself: `list` draws the same tree a reference draws, `create-master`/`remove-master` work, removing the core pointer is refused the same way | correctness | M |
| ~~A47~~ | ~~**the XInput implementation is four releases behind a reference** - the one version gap A36 left: Xvfb answers `XInputExtension` **2.4** where this server answered **2.0**~~ **fixed 2026-10-04**: the version is a reference's 2.4 and the surfaces behind it are measured and answered - **raw events (XI 2.1) are implemented and delivered** (a root selection, the reference's own shapes: a motion is 72 bytes, a button and a key 40, and a warp sends nothing), `XIBarrierReleasePointer` (2.3) answers the extension's own barrier error exactly as a reference answers it for a barrier that does not exist, and 2.2's touch has no device on either server to have content. `XIQueryVersion` now follows the reference's rule (the lower of the two, and **BadValue** for a major below 2) and both handshakes answer 2.4. What is left is measured rather than half-answered: barriers themselves need XFIXES 5.0, `XIChangeHierarchy` would need a device model that can change, and XI's passive grabs are still refused | capability | M |
| ~~A46~~ | ~~**the dock's Quit does nothing in single-window mode** - "单窗口下，macos在dock栏右键程序，点击退出没有反应，但是在per-client模式下正常"~~ **fixed 2026-10-04**: the platform's Quit is answered by whichever toolkit *claimed the application*, and the two answer differently - AWT ends the process (so the JVM's shutdown hook stops the server, asking the clients to close first), while a JavaFX (Glass) application only turns it into a close request on its **visible** windows, so an application with no window on the screen answers the platform with nothing at all. Per-client mode's clipboard, tray and scale code all touch AWT while the server starts, which is why that mode's Quit has always worked; single-window mode opened its window first, so JavaFX claimed the application and the Quit went nowhere whenever nothing was connected. `XServer.claimHostApplication` now claims the application for AWT before any window exists. Measured with the Dock's own request (`NSRunningApplication -terminate`): **nothing, for ever** before, **gone in 500 ms** after (3.5 s with a client attached, which is the ask-and-grace path A27 added) | correctness | S |
| ~~A45~~ | ~~**the project is cross-platform and one thing in it was not** - the full screen exit key was macOS's Control-Command-F only~~ **fixed 2026-10-04, and the owner's rule is stricter than the table**: the *tools* may be platform-specific (they are written for whichever system the debugging happens on) but the **product** may not be - and nothing under `src/main` uses the tools or needs a platform. The audit records every place `src/main` touches the platform at all, which is one `os.name` read and one `Files.separatorChar` comparison, and fixes the two real violations: the audit is in the design notes' *Platforms* section (the inventory of everything platform-specific, in one table, and the four rules a change has to keep), and the one real violation is fixed - `F11` everywhere that is not macOS - along with the unix socket, which used to create `/tmp/.X11-unix` on a platform that cannot use it before failing. Nothing else in `src/main` requires a platform: the extended window is behind a capability check, the unix socket behind a fallback, JavaFX behind Swing behind headless, and the macOS quirks are handled in code that is inert elsewhere | correctness | M |
| ~~A44~~ | ~~single-window mode's window with nothing to show - the question was whether it should be hidden with no clients connected and shown, raised and focused when one arrives~~ **changed 2026-10-04**: it was already not opened *before the first client*, and it used to be kept afterwards holding its last frame (a deliberate design, asserted by eight tests); it is now closed after a 1.5 s grace period with nothing to show - long enough that a toolkit which unmaps one window and maps another while starting up does not make it flicker - and the next client that maps a window opens it again, raised **and focused** (`raiseAndFocus`, new; the urgency hint still only raises). See the entry | correctness | S |
| ~~A43~~ | ~~**JavaFX 27, with the window's title bar drawn by the toolkit** (the user upgraded the pom and asked for `HeaderBar`)~~ **done 2026-10-04**: the window is a `StageStyle.EXTENDED` one whose 30 point `HeaderBar` carries the client's title in bold, and the content starts below it. The geometry was adapted around that (`content = canvas below the header`, the pointer mapping, the cursor policy, full screen hiding the bar) and the maximize was **re-designed** rather than ported: JavaFX 27's macOS zoom now goes to the display instead of to the window's content, so A38's fill of the usable area is both unnecessary and harmful - resizing a zoomed window un-zooms it, which made the state oscillate and the second double click restore nothing. The zoom is left alone and the client takes the size the window has, reported once per gesture. Verified: all four edges exact, the header drag leaks no clicks into the client, maximize/restore exact, full screen in and out, the pointer mapping exact, 477 tests green. Two known artifacts are in the entry | correctness | L |
| [ ] A20 | "the context menu vanishes after the 片段 page is opened" - **not reproduced**: the menu opens and stays in both states here *and* on Xvfb, `MenuProbe` is identical, and `main` behaves the same; the page holds the X focus, which no server-side change touches | correctness | M |
| ~~B6~~ | ~~GLX / OpenGL~~ **done 2026-09-27**: a software GL client renders itself and blits, so no renderer was needed - `glxinfo` and `glxgears` pass on `.147` | extension | XL |
| [ ] D4 | audio and other devices - **out of scope by default**, no work planned | large | L-XL |
| [ ] E6 | keep the three documents in step - **ongoing**: the discipline rather than a task, and what the sweep after every change is | engineering | S |
| -- | MIT-SHM | **do not do** | see [B8](#b8-mit-shm--deliberately-not-worth-doing) |

**§9 at the end of this file is a different kind of list**: capabilities this server does not have at
all (an extension a client might ask for), measured against a reference on 2026-09-27 and sized as
candidates rather than as items. **All seven were built on 2026-09-28** (F1, F2, F3, F4, F5, F6, F7),
and each is a full entry with a measurement behind it:

| id | candidate | size | why it is first (or last) |
|----|-----------|------|---------------------------|
| ~~F1~~ | ~~`PresentNotifyMSC` + the SYNC alarms~~ **done 2026-09-28**: the alarms are in; `PresentNotifyMSC` was already answered, and the §9 claim that it was refused was stale | extension | S-M |
| ~~F2~~ | ~~`RECORD`~~ **done 2026-09-28**: the eight requests and the stream, with one deliberate difference (no device-event stage) | extension | M |
| ~~F3~~ | ~~`X-Resource`~~ **done 2026-09-28**: all six requests of 1.2, with two deliberate differences | extension | S |
| ~~F4~~ | ~~`Generic Event Extension`~~ **done 2026-09-28**: advertised and answered, and it exposed a real `QueryExtension` defect that is now fixed | extension | S |
| ~~F5~~ | ~~`DOUBLE-BUFFER` (Xdbe)~~ **done 2026-09-28**: the eight requests, the buffer that is a drawable, and the four swap actions measured colour by colour - the wire format is **not** the specification's | extension | M |
| ~~F6~~ | ~~`XVideo`~~ **done 2026-09-28**: the reference answers zero adaptors, so this does too, with `BadPort` for every port request and no scaler behind `PutImage` (deliberately) | extension | M-L |
| ~~F7~~ | ~~`SECURITY`~~ **done 2026-09-28**: tokens, the untrusted rules measured request by request, the extension hiding and the revoke that closes a connection | extension | M |

Each became a full entry (*Current*, *Traps*, *Verify*) when it was picked up, and each entry now ends
with what its verification measured. §9 has the inventory behind them, which is now **both servers at
23 extensions with 22 of them the same**, and the list of things that are deliberately *not*
candidates.

---

## 1. Verification kit (do E0 first)

Every item below ends with a **Verify** step, and most of them need the same small tools and the same
two-machine test bed. **The kit is now in this repository and complete**: the tools live in
`src/test/java/io/github/oyzh1994/xserver/tools/` (so `mvn -o test-compile` compiles them), the
two-machine harness is `tools/vm.sh` and `tools/bed.sh`, the reference display is `tools/xvfb.sh`, and
`tools/compare.sh` runs a whole comparison as one command. `tools/README.md` says what each tool is
for and how to run it, which tools were **lost** when `/tmp` was cleaned (and which of those came back
in E0), and the measurement rules that go with them. Read that file before running any comparison:
some of the rules exist because ignoring them produced an afternoon of chasing an imaginary rendering
bug - and one of them, the pointer outliving every client, is new in E0 and has the same shape.

**The same-client-two-servers comparison is finally available (2026-09-27)**, which is what `XProxy`
was written for and never managed: `XProxy` truncated every generic event (XI2's), so every real
toolkit desynchronised at its first device event. Fixed, and the first use is in
`tools/README.md` - EasyShell's profile against Xvfb and against this server, for the same interaction
driven by the same driver, agrees request for request. That is the instrument for the next "why does
this client behave differently here" question; use it before re-deriving one by hand.

**And the wire has an instrument of its own (2026-09-30): `tools/vnc-meter.py`**, a real RFB client that
keeps one incremental request outstanding and counts what comes back - MB/s, updates/s, and the seconds
a request was *held* because nothing had changed. It is what turned "the same screen still feels less
smooth than `x11vnc`" into a number (A25: 5.39 MB/s and 61 updates a second of an unchanging screen,
against `x11vnc`'s nothing), and `--dump` decodes ZRLE into the client's own picture so a skip can be
checked as not being a lost update.

### [x] E0. Finish the verification kit. **S** - done 2026-09-25

Restored on 2026-09-24: `Drive`, `WinTree`, `Focus`, `Pixel`, `Xwd`, `Blocks`, `Colors`, `Shift`,
`Rect`, `Crop`, a reconstruction of `XProxy`, a new `AuthProbe` (written for A7), and `SelectionProbe`
with `CursorProbe` (written for A8). `RawXClient`
grew a key driver, an auto-repeat setter and - worth more than the rest - a **reply sequence check**:
a reply that is not the one being waited for is now an error instead of a wrong value, which is how a
test that asked for an empty range once came back with the previous query's three events.

**Written 2026-09-25** (the rest of the item), all in
`src/test/java/io/github/oyzh1994/xserver/tools/`:

- **`Keys`** - types a string through XTEST, resolving each character against `GetKeyboardMapping`
  rather than carrying a layout; a character no key carries is reported and skipped. Measured against
  Xvfb with `xev`: `"Hello, World 123!"` produced exactly the expected presses, shift included
  (`Shift_L` + `keycode 25 (keysym 0x57, W)`), and 17 characters typed with none skipped. Against this
  server the same string types 18 and reports the two Chinese characters as untypable, which is the
  intended answer until C10's keycode claiming is in play.
- **`Shot`** - `GetImage` of the root or a window to a PNG, with no `xwd` and nothing installed on the
  machine that runs it. Measured against both: Xvfb's `xev` window came back white at 5,5 and this
  server's root back as `#E8E8E8`, which is the design's root colour.
- **`Wire`** - the bare-protocol base the probes share, which hands back *every* message as it
  arrives. `RawXClient` cannot be used for a probe: it finds a reply by reading until one turns up,
  and therefore eats the events a probe is counting.
- **`FocusProbe`** - the `Probe` the design notes' focus work was measured with. A three-window tree,
  a scripted sequence of `SetInputFocus` / warp / grab steps, and every focus and crossing event
  printed with detail and mode. **This is the harness for A5 and A6**, and it immediately found the
  differences recorded under A6 below.
- **`MenuProbe`** - the menu gesture as a tool: a popup mapped under a held button, grabbing the
  pointer, left and re-entered, with every crossing printed and an `EnterNotify` arriving in a step
  with no motion flagged `! enter without a move` - the event shape the design notes measured to tear
  EasyShell's menus down. **Measured 2026-09-25: its output against this server and against Xvfb is
  identical, line for line**, so the §7 deviations are intact for this gesture and any future change
  that breaks them shows up as a diff (or as the marker).
- **`PointProbe`** - walks `QueryPointer` from the root down, so "the child of the queried window or
  the deepest window?" is read off directly (A4's question). Both servers answer with the immediate
  child.
- **`tools/xvfb.sh`** - `start`/`stop`/`status` for the VM's reference display, because three of its
  flags are easy to get wrong and each failure looks like something else: `-listen tcp` (a stock
  build opens no TCP, so nothing here can reach `:98`), `-ac` (it refuses the kit's hand-written
  clients, which offer no credentials - the failure reads as "the setup was refused", i.e. as a dead
  display), and not `-nolisten unix` (the VM's own clients use the socket).
- **`tools/compare.sh`** - the six steps a rendering comparison takes as one command: both beds, the
  same gesture driven against each, `xwd -root` on the VM on both sides (rule 3), then the `Xwd` diff
  and the `Blocks` grid.

**Two traps the item itself cost, both now in `tools/README.md`:** the pointer outlives every client,
so a probe that does not park it before mapping gets a crossing at the map on one run and not on the
next - a diff then shows the *previous* run's difference; and the core grab opcodes are contiguous and
easy to shift by one (`GrabKeyboard` 31, `UngrabKeyboard` 32, `GrabKey` 33, `UngrabKey` 34), where
sending the wrong one is either silent or answered with a `BadRequest` that reads as a malformed
request rather than a misnumbered one. A probe that sends `GrabPointer`/`GrabKeyboard` must also read
the reply, or every event afterwards is attributed to the step before it.

`mvn test` 198/198 with all of it in the tree. Full list and usage in `tools/README.md`.

The kit, and what each piece is for:

| tool | what it does |
|------|--------------|
| `RawXClient.java` (already in the repo, under `test/`) | the base for all of them: handshake, create windows, GC-filled rectangles, `GetImage`, XTEST. Everything below is a variation on it. |
| `tools/sweep.sh` | **the reference sweep as one command**: every probe against Xvfb and against this server, the two runs diffed (`probe-diff.py`), and a line per probe saying identical or different. Added 2026-09-27 because each probe had been compared by hand once and nothing re-ran them all - so a change could drift an area nobody probed that day. Full run then: **13 of 18 identical**, and the five that differ are the deliberate ones listed in `tools/README.md`. |
| `Drive` | XTEST driver: `M x y` motion, `P n`/`R n` press/release, `K kc` key, `W ms` wait. This is how a GUI is driven without xdotool. |
| `Keys` | types a string through XTEST (needs the keymap from `GetKeyboardMapping`). |
| `WinTree` | prints the window tree with geometry, class, map state, override-redirect, `WM_NAME`, `WM_CLASS`, `WM_TRANSIENT_FOR`. The per-client mode has no other way to see a client's windows from outside. |
| `Xwd` | reads an XWD file and either writes a PNG or diffs two of them (counts, worst channel delta, delta histogram). Written because **Python's PIL refuses the XWD that `xwd` writes on the VM**; the header is big-endian while the *pixels* are in the order named by header field 7 (LSB here), and the pixel data starts at `header_size + ncolors * 12`. |
| `Blocks` | a grid of per-block difference counts between two captures - where to look. |
| `Colors` | every distinct colour in a rectangle, most common first - how "the icon has three colours where Xvfb's has twenty" was read off. |
| `Shift` | difference cost per offset in ±4 pixels - tells a shift from a rendering difference before blaming rendering. |
| `Rect` | an ASCII map of a rectangle (darkness buckets) - how a ghost dialog was spotted. |
| `Focus` | `SetInputFocus` to an arbitrary window from a bare-protocol client, i.e. plays the window manager for a test. |
| `AuthProbe` | sends a connection setup with chosen credentials and reports the answer: the setup's vendor and screen count, or the refusal's reason. No arguments means "offer nothing". |
| `SelectionProbe` | subscribes to a selection through XFixes and prints every notification, while other connections take the selection, destroy its window and disconnect - the three subtypes A8 is about. Takes a selection name, so a client watching something other than CLIPBOARD can be checked. |
| `CursorProbe` | the same for `SelectCursorInput`: two windows with different glyph cursors, the pointer warped between them, every `CursorNotify` printed. |
| `ClipboardProbe` | plays one end of the clipboard bridge against a live server - `read` prints what a client would paste, `write <text>` takes the selection and serves it, `owner` prints who holds it - so the host's own pasteboard can be the other end of the check. |
| `XProxy` | a byte-level X11 proxy: logs a client's request/event stream against a *real* server, which is the only way to compare what a client sends to Xvfb with what it sends here. |
| `XiProbe` | the XInput2 questions (`XIQueryVersion`, `XIQueryDevice`, `XIGetClientPointer`, `XISelectEvents`), printed as fields **and** bytes, with an XTEST button press so the events a selection actually produces can be read off - the harness B4's measurements were taken with. |
| `Crop`, `Pixel`, `Diff`, `Compare` | small image helpers (Crop magnifies a region around a point). |

The test bed (as of 2026-09-24):

- **The addresses changed on 2026-10-03**: the bed VM is now `192.168.22.152` (was `.147`) and the
  user's own PC is now `192.168.22.151` (was `.150`). The password is still `user@147` - the string
  is historical, not the address. Everything below, and every tool default, says the new numbers.
  **The VMs are the user's to start** - do not boot them from here (asked 2026-10-03).
- The **server runs on the Mac**; clients run on the **Linux VM** at `192.168.22.152` and connect over
  TCP to `192.168.22.1:<display>` - or, for the reference, to the VM's own `Xvfb :98`.
- `ssh root@192.168.22.152`, password `user@147`. **`expect` hangs on the password prompt in this
  environment; use `SSH_ASKPASS`**:
  `SSH_ASKPASS=<script printing the password> SSH_ASKPASS_REQUIRE=force ssh -o PreferredAuthentications=password -o PubkeyAuthentication=no …`
- Long-running commands must be detached (`setsid nohup … < /dev/null &`) or `ssh` kills them when the
  session ends.
- The VM has `xdotool` (**which segfaults against this server** - see A4), `xwd`, `xwininfo`,
  `x11vnc`, python3+PIL, and no X server of its own except the `Xvfb :98` that stays running.
- `Xvfb :98` (1300x800x24) plus `x11vnc -display :98 -forever -rfbport 5900 -listen 0.0.0.0 -shared`
  is the **reference**: connect from macOS with `vnc://192.168.22.152` and a human can compare, or
  drive it with `xdotool` and capture it with `xwd -root` (both work there).
- The forwarded applications used so far: **termora** (`/root/termora-2.0.0-beta.16-linux-aarch64.AppImage`,
  a Swing/AWT terminal, allows one instance at a time) and **EasyShell** (JavaFX, from a previous
  project). Launch with `setsid nohup env DISPLAY=… ./AppImage > log 2>&1 < /dev/null &`.

Server-side diagnostics that cost nothing and answer most questions:

```
--log-requests                              every request, at DEBUG, into the logback file
-Dxserver.dumpFrame=/tmp/d -Dxserver.dumpFrames=3000
                                            one frame a second: d-buffer-N.png (framebuffer),
                                            d-image-N.png (what JavaFX was handed), d-scene-N.png
                                            (what the window shows - the only place the sprite is)
```
`JavaFxBackend.hostEvent` logs every press/release the *host window* receives before anything is done
with it, which is how "the platform swallowed the click" is told apart from "the client ignored it";
`InputState` logs `button N down/up at (x,y) -> 0x…`, which says which window the server delivered it
to; `WindowHandlers` logs which events a client selected.

**Measurement traps that have already cost hours** (they belong in the kit's README):

1. **Park the pointer in the same place, in the same state, before comparing two captures.** One
   capture with the pointer over the toolbar and one without lit up hover states and read as a large,
   entirely imaginary rendering difference. Two captures of the same application on two servers are
   only comparable when the app is in the same state.
2. **Check for a shift before believing a rendering difference** (`Shift`). "Same picture moved" and
   "different picture" are completely different bugs.
3. **`xwd` on the VM only sees the screen of the display it connects to** - in per-client mode a new
   connection gets a root of its own and *nothing* is visible; use `--single-window` for scripted
   captures, or the frame dumps in either mode.
4. **A comparison against Xvfb is a comparison against a *different* root colour** (Xvfb's is black,
   this server's is `#E8E8E8`) and against the window border this server draws: the window's own edge
   always differs, and that is by design.

---

## 2. A - Correctness: the code accepts a request and does the wrong thing

### [x] A7. `--auth` could be bypassed by offering no credentials. **S** - done 2026-09-24

**What it was.** `ConnectionSetup.authenticate()` read
`if (!server.options().authRequired() || authName.isEmpty()) return true;` - so a client that left the
credentials out of its handshake was accepted even though `--auth` was requested. Since the server
listens on every interface by default, that was the whole of the access control: the flag looked like
one and was not.

**What changed.** "Access control is on and nothing was offered" is a refusal (with a log line naming
the peer); an `-ac` server - the default - still takes anything, which is what every other test and
the author's own test bed rely on.

**Measured 2026-09-24** with `tools/AuthProbe` (new in the kit) against live servers:

```
no credentials, --auth on:   REFUSED: Authentication rejected
right cookie:                SETUP: vendor=xserver screens=1
wrong cookie (one byte off): REFUSED: Authentication rejected
no credentials, --no-auth:   SETUP: vendor=xserver screens=1
```

`client/AuthenticationTest` (6 tests) pins all four plus "an empty cookie is not the same as offering
none" and "an unknown authorization protocol is refused"; the bypass test fails without the fix
(`expected: <> but was: <setup>`).

**Traps worth keeping.** A client with no `~/.Xauthority` entry for that display offers nothing, and
the failure looks exactly like "the server is down", so the refusal is logged with the peer - check the
log before blaming the network. And `AuthProbe`'s own handshake bit twice while it was written: the
setup request's two length fields are read in the byte order the client *declares* (little-endian for
`'l'`), and a failure reply is an eight byte header with the reason after it, not two bytes - getting
either wrong reads as a *hung* handshake rather than a bad request.

---

### [x] A3. Font cursors were drawn as a filled cell. **S** - done 2026-09-24

**What it was.** `XCursor.fromGlyph` had no glyph outlines to draw, so it approximated the character
with "this cell is the shape": a client that called `XCreateFontCursor` - `xterm`, `xclock`, `xcalc`,
most Xlib applications - got a solid rectangle under the pointer instead of the standard arrow.

**What changed.** The X cursor font's bitmaps are embedded (`resource/CursorFont.java`, 74 shapes:
source bitmap, mask bitmap, hotspot, generated from `cursor.pcf` by `tools/gen-cursor-font.py`, which
is in the repository so it can be regenerated). `fromGlyph` composes them the way
`XCreateGlyphCursor` defines it: the source bitmap in the foreground over the mask character's bitmap
in the background, at the glyph's own hotspot - and the font argument is not consulted at all on that
path, because a client that asks for `XC_left_ptr` means the arrow every X server draws.

**Measured 2026-09-24.** `resource/CursorFontTest` (3 tests): the arrow leaves most of its cell
transparent and draws a shape in the foreground (a filled cell has no transparent pixels at all), the
hotspot is inside the bitmap, and a shape the table does not have still gets the old approximation.
The two arrow tests fail without the fix.

**Traps.** The font's copyright line is what makes embedding legal - "These glyphs are unencumbered" -
and the hotspot is the font's own convention (left side bearing, ascent), not the bitmap's corner.
Clients with *themed* cursors (GTK, Qt, JavaFX) never touch this path: they upload ARGB images through
`CreateCursor` and a pixmap, which already worked. `CreateGlyphCursor` with an arbitrary glyph set is
a different path again and still has no outlines.

**Verify (partly done).** In process, by `CursorFontTest`. Live, `xterm` over a forwarded display is
what would show it - an I-beam over the text area - but **the VM has no `xterm`** (only `xclock`,
whose cursor is the default arrow and does not take this path), so the sprite has not been looked at
over a real display yet. The sprite appears only in the `-scene-` frame dump: diff it against the
`-image-` dump of the same frame and the differing region is the sprite.

---

### [x] A4. `xdotool` segfaults against this server. **S-M** - done 2026-09-25

**Done, and the cause was not in this item's plan at all: there was no XKEYBOARD.** The crash is a
null pointer dereference of `XkbGetMap`'s answer, and Xlib's `XkbGetMap` returns null when the
extension is absent - so "no XKEYBOARD" and "a malformed keymap" produce the *same* fault, at
`ldr x3, [x0, #32]` with x0 zero, inside the inlined `_xdo_populate_charcode_map`. Measured three
ways: a gdb backtrace on the VM; a trace of the XKB entry points, which shows exactly two calls
before the fault (`XkbUseExtension`, then `XkbGetMap(which=0x7, device=0x100)`); and a `ctypes` call
to `XkbGetMap` itself, which answers `null` here and a pointer against Xvfb. The fix is B1, and with
it in, every subcommand is clean:

```
getdisplaygeometry        1300 800
getmouselocation          x:100 y:100 screen:0 window:45      (after `mousemove 100 100`)
xdotool key a             (nothing)
search --name xterm       (nothing; no crash)
getactivewindow           abort, with a reported error and exit status 0 - this server has no
                          _NET_ACTIVE_WINDOW, which is the documented deviation, not a fault
```

**Why the gdb backtrace alone was not enough, and what actually found it.** `xdo.c` calls
`XkbGetMap` and does not check it; issue #58 in xdotool's own tracker is the same crash against
another server. The plan this item left - "extend `XProxy` to log reply bodies and diff against
Xvfb" - was never needed: the map exists, so what was left was to compare *the reply* with Xvfb's,
which is what `tools/XkbProbe` does. Bisecting it a section at a time found the real cause, and it
is a trap for anyone else implementing XKB: **a `GetMap` reply has to carry XKB's four mandatory key
types.** With three - one-level, two-level and alphabetic, which is every type this server's keymap
uses - Xlib rejects the reply *whole* and returns null. The types section was byte-for-byte identical
to Xvfb's for those three, which is why nothing about it looked wrong.

**The rest of what was ruled out over the two sessions stays ruled out**: Xinerama (implemented in
B3, and the crash survived it), `QueryPointer`'s answer (a real deviation, fixed in A4old, and not
the cause).

**The two sessions of investigation, kept because each dead end cost one of them:**

**Where it crashes, confirmed**: `xdo_new_with_opened_display` in `libxdo.so.3`, from a gdb backtrace
on the VM - xdotool's own library, in its startup, before any of the subcommand's own requests. Every
subcommand dies there, `getdisplaygeometry` included.

**Ruled out: Xinerama.** The obvious suspect was an extension xdotool probes at startup that this
server lacked, and XINERAMA was the one extension Xvfb has and this server did not. It was implemented
(see B3) and **the binary still segfaults in the same function** - so it is not the missing extension,
and the next person does not have to try it.

**What the next step is**: something the library reads out of a reply is malformed enough to crash it,
and the tool for that is a proxy that prints reply *bodies* - `tools/XProxy.java` is a byte-level proxy
in the kit that logs a client's stream and deliberately does not decode replies (E0 records extending
it as this item's first step). A strace of the socket writes gets the request bytes and nothing of the
answers, which is the wrong half.

**Parked 2026-09-24** as too large for the time available: this needs a diagnostic tool before it needs
a fix.

---

### [x] A4old. `xdotool` segfaults against this server. **S-M** - superseded: the cause was B1, and A4 has the record

**Current.** `xdotool getmouselocation` aborts; a hand-written raw-protocol client issues the same
`QueryPointer`/`WarpPointer`/`GetInputFocus` and is fine. So some reply this server sends is well
enough formed for a lenient client and malformed enough for Xlib to misread - a wrong length, a wrong
`nitems`, or a field Xlib dereferences.

**Why it matters.** `xdotool` is *the* tool for scripting an X desktop; without it every test here
needs a hand-written driver (see E0), and any user who scripts their desktop hits a crash they will
blame on their own script.

**Leads so far (2026-09-24).** The crash is **not** in a reply xdotool parses: *every* subcommand
dies, including `getdisplaygeometry`, which needs nothing but the screen - so it happens while xdotool
starts up, in `xdo_new` or in what it requires of the display before dispatching. Checked on the VM
against a live server: `getdisplaygeometry`, `getactivewindow`, `search --name xterm`,
`getmouselocation`, `getwindowfocus` all come back with SIGSEGV (139).

**One thing was fixed on the way, and it is a real deviation either way**:
`InputHandlers.queryPointer` answered with the *deepest* window under the pointer, where the protocol
says the *child of the queried window* - a client uses that answer to name the window it points at,
and handing it a grandchild makes it query a window it never asked about. It now uses
`window.childAt`, which is the immediate child. Xdotool still crashes, so this was not the cause.

**Next step.** A backtrace (`gdb`/`lldb` on the VM, or a core dump - the VM does write one), or
`strace -f -e trace=network`, to see which call is last before the fault. Then `XProxy` in front of
**Xvfb**, extended to log reply *bodies*, to compare what a working session gets back with what this
server sends - `XProxy` prints requests and events only, and extending it is the first thing to do.

**Traps.** A `BadWindow` error in the middle of Xlib's own request sequence also looks like a
segfault, because the error arrives asynchronously and Xlib's default handler exits; check the server
log for `rejected the request` lines at the moment of the crash before assuming a malformed reply.

**Verify.** `xdotool getmouselocation`, `xdotool mousemove 100 100`, `xdotool getactivewindow`,
`xdotool search --name xterm` against a running server, all clean, and the suite still green.

---

### [x] A2. A bounding shape did not clip drawing. **S-M** - done 2026-09-24

**What it was.** Only the input shape was honoured, for hit testing. The bounding shape affected the
extents a client could query and nothing else: a shaped window's corners were painted with its
background, its children painted outside it, and (the half that is easy to miss) the window kept
*obscuring* its whole rectangle, so the corners it no longer covered were never given back to the
screen underneath.

**What changed.** `XWindow` carries its own bounding region (`boundingShape`, in window coordinates),
handed over by `ShapeExtension` on every shape change - the extension owns the shapes, but the region
code runs once per drawing request and has no server to ask. `visibleRegion()` intersects it for the
window and for each ancestor (so a child outside its parent's shape is clipped by the parent's), and
`subtractWindow` subtracts the *shape* rather than the rectangle where there is one, which is what
lets the corners go back to what is under them. `WindowHandlers.shapeChanged` does the two halves a
shape change owes, the same two a move owes: give back what it stopped covering (`repaintUnder`) and
ask the client to draw what it gained (`Expose`).

**Measured 2026-09-24.** `resource/ShapeClippingTest` (3 tests, in-process server, framebuffer
assertions): a shaped window takes the fill only inside its shape; a shape set *after* the window is
mapped hands the corner back; a child outside its parent's shape does not paint. All three fail
without the fix.

**Traps.** A shape is a subtraction from the window's pixels, not a clip on top of them - and the
border lives outside the content rectangle, so a shaped window's border is clipped by the shape too
(`outerVisibleRegion` gets it from the same walk). `SetWindowClipRegion` (XFixes) is a different
thing, a clip on drawing, and stays where it is.

**Left open here, answered and fixed 2026-09-26: the hand-written `ShapeRectangles` in `RawXClient`
was right and the server was refusing a valid request.** The tool's layout was never the problem -
the request reached `ShapeExtension` and died in the *decoder*:

```
opcode 130 rejected the request (error 1): could not decode SHAPE request 1:
    java.lang.IllegalArgumentException: initial capacity cannot be less than 0
```

which is the arithmetic `VariableLengthDecoders` documents for the core requests: the generated
readers size a trailing list with `withInitialCapacity(length - javaStart)`, where `length` is the
request length in **four byte units** and `javaStart` a byte offset. `ShapeRectangles`' fixed part is
sixteen bytes, so any request shorter than sixteen units - **five rectangles or fewer** - computes a
negative capacity and is refused with `BadRequest` before the handler runs. Measured against a live
server: one rectangle refused, ten accepted, which is what kept this looking like a problem with the
tool's own bytes.

`ShapeExtension.decodeRequest` now reads the request by hand (as `RenderDecoders` does for RENDER),
and the two XFIXES requests with the same shape of list needed it too, with a smaller fixed part:
`XFixesCreateRegion` and `XFixesSetRegion` list their rectangles eight bytes in, so **zero, one or
two** are refused - measured, two refused with `initial capacity cannot be less than 0` and three
decoded. That is the path GTK and Qt take when they build or recalculate a region for a window
shape, so it is the ordinary case and not a corner. `ShapeClippingTest` grew three tests that go
over the wire instead of in process: one rectangle through `ShapeRectangles`, one through
`XFixesCreateRegion` + `SetWindowShapeRegion`, and one region *changed* to a single rectangle with
`XFixesSetRegion`, each asserting the pixels. **All three error with
`the server refused request …: error 1` with the decoders taken out.** `mvn test` 327/327.

The general fix is still the fork's (E1): every generated reader with a trailing list carries this
expression, and each one is worked around where it is reached.

---

### [x] A11. There was no keyboard autorepeat. **S-M** - done 2026-09-24

**What it was.** The server delivered the press and the release and nothing in between, while
`GetKeyboardControl` claimed every key repeats - so a held Backspace in a forwarded terminal deleted
one character, and a client that turned a key's repeat off was ignored.

**What changed.** The display tick (`XServer.pollAutoRepeat`, under the server lock, once per frame)
delivers the repeats: a key's first repeat comes `repeatDelayMillis` after it went down and the rest
every `repeatIntervalMillis` - X's own defaults, 660 ms and 40 ms (`xset q`'s "delay 660, rate 25"), as
constants in `InputState`. A release stops it, a second press starts a fresh delay, a **modifier does
not repeat** (XKB decides that on a stock server, and a repeating Shift is a run of presses no toolkit
expects), and a client's `ChangeKeyboardControl` with `AutoRepeatMode` is honoured - for one keycode or,
with no keycode in the value, for every key, and `GetKeyboardControl` now reports what was actually set
rather than a blanket "everything repeats".

**Measured 2026-09-24**, both ways:

- `input/AutoRepeatTest` (5 tests, bare-protocol client, XTEST, the display loop actually running):
  the delay is honoured and the interval is kept, a release stops it, a modifier never repeats, a key
  whose repeat was turned off (and back on) behaves, and - the end-to-end one - a key held for a second
  and a half delivers about twenty presses *on its own, from the tick*. With the tick's call commented
  out, that test fails with `saw 0` while the other four still pass, which is the split the item wants:
  the arithmetic is pinned separately from the delivery.
- Live, against a real client on the VM (termora, `--single-window`), one 1.6 s hold of Backspace
  through XTEST: **27 `KeyPressEvent` deliveries and 1 release**, against 1 and 1 before.

**What is still missing** is the rest of X's repeat vocabulary, and it is XKB's (B1): the *delay* and
the *rate* are only settable through `XkbSetControls`/`XkbSetAutoRepeatRate` - **done 2026-09-25**,
with `GetControls` to read them back, so a client that wants a 200 ms delay asks for one and gets it
(see B1's "controls half") - and `XkbSetDetectableAutoRepeat` is still not there: that is where the
client generates the repeats itself and only needs to be told when a repeat would have started, and it
is the one of the five `PerClientFlags` that changes the *shape* of the events this server sends, so
it would have to come first if any of them is ever implemented. On the core rail, `GetKeyboardControl`
now reports the bitmap and the keyboard's repeat flag separately, which is what the measurement above
showed the reference does - `xset r off` clears the flag and leaves the table alone.

---

### [x] A10. `GetMotionEvents` returned nothing. **S** - done 2026-09-24

**What it was.** The handler replied with an empty list, on the argument that a buffer per window is
waste for a request almost nobody makes. What that cost: a client that wants to tell a drag from a
click, or to draw a stroke with the shape it had, was told the pointer never moved.

**What changed.** `InputState` keeps a bounded ring of the last 256 motions per screen (Xorg's own
`MOTION_HISTORY_SIZE`), each filed under the window the pointer was in at the time and stamped with the
event clock, and `InputHandlers.getMotionEvents` answers from it, clipped to the window and to the
half-open time range the client asked for - compared as 32 bit values that wrap, the way X compares
`Time`.

**Measured 2026-09-24.** `input/MotionHistoryTest` (2 tests, bare-protocol client over TCP, XTEST
motion): three motions inside a window come back in order with their coordinates and usable timestamps;
a motion outside the window is another window's history; two overlapping windows each get their own;
an empty range answers none. Without the fix both tests fail (the first with `expected: <3> but was:
<0>`).

**Traps.** The history holds what the pointer *did*, not what was delivered: a motion swallowed by a
grab, or made over another client's window, is still in it. Times are the ones events carry
(`server.events().time()`), so a client can compare them with what it has already seen - but the server
stamps a motion when it *reads* the request, so a range that begins a millisecond after the client's
own clock reading can still contain a real motion, which is a test that fails one run in four (it did).
Pointer acceleration is still accepted and ignored: that stays a deliberate deviation, since the
platform's own acceleration is what the user feels.

---

### [x] A12. Window gravity is stored but not applied. **S-M** - `winGravity` done 2026-09-25

**What it was.** `bitGravity`/`winGravity` were set, reported in `GetWindowAttributes`/`QueryTree`, and
never used: a parent resize did not move a child by its win gravity. (A window's own `bitGravity` is
the other half, and is still the model's business - see below.)

**Why it matters.** Small in practice - the default is `NorthWest` and almost every toolkit leaves it
there - but the ones *used* in the wild are exactly the ones a caller notices: `EastGravity`/
`SouthGravity` for something pinned to an edge, `StaticGravity` for a window whose content must not
move.

**The reference's whole table, measured 2026-09-25 with `tools/GravityProbe`** (new in the kit: a
parent 300x300, a child 100x100 at 150,150, the parent grown by 200x100, the child's `GetGeometry`
read back). Every value, one gravity at a time, and the two halves are what make it worth pinning -
`North` moves a child by half the *width* change and `South` by all of it:

```
gravity        1 NW  2 N      3 NE     4 W     5 Center   6 E      7 SW    8 S      9 SE   10 Static  0 Forget
moved by       +0,0  +100,0   +200,0   +0,50   +100,50   +200,50  +0,100  +100,100 +200,100 +0,0   +0,0
```

The grid is a three by three: the row says which vertical edge's distance is kept (north keeps the
top, centre splits it, south keeps the bottom) and the column the same horizontally - so the shift is
none, half or all of the change. `Forget` and `Static` move nothing. Rounding is truncation toward
zero, also measured: a change of +201 moves a centred child by +100, and one of -101 by -50.

**What changed.** `WindowHandlers.applyWinGravity`, called from `applyConfigure` when a window's own
size changed: each child is moved by its gravity's share, and the area it leaves is handed back
through the ordinary move path (`visibilityChanged(..., moved=true)`, so the parent repaints where the
child was and the child is asked to draw where it now is).

**Two things measured that the implementation deliberately keeps:**

- **The reference moves the child silently** - no `ConfigureNotify`, no expose, nothing at all to the
  child (measured with the child selecting `StructureNotifyMask` and `ExposureMask`, with and without a
  background). That is not as odd as it sounds: the client declared the gravity, so it can predict
  where the child went. This server sends no `ConfigureNotify` either.
- **It does expose the child, and that is a deliberate divergence.** There is no backing store here
  (A9), so the alternative is a child showing whatever used to be under it. The pixels are ours to
  keep coherent; the events are where the reference's behaviour was copied.

**`bitGravity` is done too (2026-09-26), and the paragraph that used to stand here was wrong twice
over.** It said "with no per-window backing store there is no content to shift" - but the store is not
the point: a window's pixels are in the framebuffer while they are visible, and a reference server
moves them. It also said "anything else would need A9's backing store first", and A9 has been in since
2026-09-25 without this being possible to write from it. Measured one gravity at a time with the tool
the work needed anyway, `tools/BitGravityProbe` - a marker drawn near each corner, the window grown,
the window read back with `GetImage`:

```
gravity  1 NW  2 N      3 NE   4 W     5 Center   6 E      7 SW   8 S      9 SE   10 Static  0 Forget
moves by 0,0   dW/2,0   dW,0   0,dH/2  dW/2,dH/2  dW,dH/2  0,dH    dW/2,dH  dW,dH   0,0       gone
```

The same three-by-three grid as `winGravity`, and the same two odd ones: `Static` does not move the
pixels, and `Forget` **throws them away** - the measured marker is simply gone. What moves is what was
*visible*, since nothing keeps an obscured window's content; the area the content leaves is painted
with the window's background and exposed, the same as a reveal. `applyBitGravity` is called from the
configure path after the gained area is painted, so what moves lands on top of it.

**And the default had to be measured before any of that could be safe**: `bitGravity` was stored as
**0**, which is `Forget` - so a window resized under the default would have thrown its contents away,
every window in the server losing its pixels on its first resize. `GetWindowAttributes` on a freshly
created window answers 1 (`NorthWest`) on a reference server and answered 0 here; both gravities now
start at 1. The test that pins the grid (`WindowGravityTest`,
`aResizeMovesAWindowsOwnContentsByItsBitGravity`) fails without the implementation at the `Forget`
case, where an ink pixel survives that should be gone.

**One primitive had to be fixed on the way**, and it is worth knowing about: `Framebuffer.copyFrom`'s
self-copy direction was decided by the *rows* alone, so a purely horizontal self-copy smeared - every
pixel written over the one the next read wanted. Bit gravity is the first caller to copy pixels
sideways on the same surface (a backing-store save copies between surfaces or downwards).

**Measured 2026-09-25.** `handler/WindowGravityTest` (3 tests, the whole nine-gravity grid plus the two
that do not move and the "a move is not a resize" case). **It fails without the fix** (`+0,+0` where
`+100,+0` is expected). `GravityProbe` now reports the same numbers against this server and against
Xvfb for **all eleven** gravities - checked one at a time, which is how the table above was built.
`mvn test` 210/210. Live: EasyShell connected and its context menu still opened and survived the
out-and-back gesture, and **neither EasyShell nor termora declares a gravity at all** (the server's own
log of gravity moves stayed empty through both sessions), so this change is inert for both of them -
the verification is the reference diff.

**Traps.** The gravity is applied when the *parent's* size changes, not when it moves: a move's delta
is zero and nothing shifts, which the third test pins. And a child's own sub-tree follows it for free -
positions are relative to the parent, so moving a child moves its descendants with it.

---

### [x] A8. XFixes selection and cursor notifications were never delivered. **S-M** - done 2026-09-24

**What it was.** `XFixesExtension` implemented `SelectSelectionInput`/`SelectCursorInput` - it recorded
them - and never sent `SetSelectionOwnerNotify`, `SelectionWindowDestroyNotify`,
`SelectionClientCloseNotify` or `CursorNotify`. A clipboard manager, an `xclip`/`wl-paste` bridge or a
cursor-theme tool, all of which subscribe instead of polling the owner, saw nothing ever change.

**What changed.** The three selection subtypes, raised from the code that changes a selection's owner
(`XFixesExtension.notifySelection`, called from `AtomHandlers.setSelectionOwner`, from
`WindowHandlers.destroyWindowTree` and from the client-disconnect cleanup), and `CursorNotify`, raised
from the display tick - which is where the server knows what the pointer is over. Ownership is now
`XServer.Selection{owner, timestamp}` rather than an owner id alone, because the notification carries
the selection's own timestamp and it is not always now. A subscription is a
`(window, selection, mask)` triple: the window is what the event names, and the selection matters
because a client that watches CLIPBOARD must not hear about PRIMARY - the first version keyed
subscriptions by window alone and the regression test caught it.

**Measured 2026-09-24**, two ways:

- `tools/SelectionProbe` and `tools/CursorProbe` (new in the kit) run the same requests against Xvfb
  and against this server and print every event. The two outputs are **identical** once the things a
  server assigns for itself are normalised away (opcodes, atom ids, cursor serials, absolute clocks),
  which is how the rules in `docs/design-notes.md` ("What a subscriber is told") were established.
  Among them: a destroyed owner window and a disconnecting owner report `owner=0` with the
  *selection's original* timestamp, while every `SetSelectionOwner` - including one that changes
  nothing - sets both times to now; and a cursor's `cursorSerial` names the cursor, so it goes back
  down when the pointer returns to one it has shown before.
- Live, with the real client: EasyShell on the VM subscribes to `_NET_WM_CM_S0` as it starts
  (measured: `mask 0x7 on 0x400001`), and taking that selection from a probe delivered the
  notification to it. Taking `CLIPBOARD` did **not** notify it, which is the point of the triple -
  and what the old "a subscription means this client wants selection events" reading got wrong.

`extension/XFixesNotifyTest` (7 tests, real connections, the display loop running) pins all of it.
Five fail without the notifications, the subscription-lifetime test fails without the window cleanup,
and the seventh is the "did not subscribe, hears nothing" guard that caught the window-only key.

**Traps worth keeping.** The two timestamps are not interchangeable (see above), and sending the
notification *before* the owner is updated makes the client query the old owner. A reference server
**refuses every XFixes request from a client that never called `XFixesQueryVersion`** (`BadRequest`) -
measured while probing, and deliberately not copied: a real client always asks first, so all it would
change is a hand-written probe. Registration order is not ours to pick, so the event base comes from
`firstEvent()`, and the two event classes are numbered correctly by the fork (`NUMBER` 0 and 1),
unlike the five in E1.

**And `CreateGlyphCursor` with a font that is not the cursor font** - A3's other half - **is done
(2026-09-26)**, measured first and implemented from the measurement. `tools/GlyphCursorProbe` defines a glyph cursor on a window, warps the pointer
in and reads the image back through XFIXES' `GetCursorImage`, so the composed result can be compared
rather than the arguments. What it measured against Xvfb (2026-09-26), for `fixed`'s 'A' with 'B' as
the mask character:

```
                     size     hotspot   drawn shape
Xvfb                 6x13     0,11      the MASK character's bitmap, in the background colour
this server          6x13     0,0       a filled cell (the approximation A3 removed for the
                                        cursor font and left here)
```

The rule the four combinations pin (source/mask 'A'/'A', 'A'/'B', 'A'/'0', 'A'/' ') is that **the
mask character's ink is the silhouette**, and the hotspot is the glyph's **origin** - the pen's place
in the cell, `(0, the font's ascent)` - and not the ink's left bearing, which is what this item first
wrote down: measured across seven glyphs, four of them with a bearing of their own ('!' 2, '"' 1,
',' 1, '(' 1), and a reference answers 0,11 for every one of them. `XCursor.fromGlyph` composes
exactly that for a font whose glyphs it has - the cell is the font's own
`maxWidth x ascent+descent`, the mask glyph's ink is the shape, the source glyph's ink is drawn in the
foreground inside it - and the fallback (a font with no table at all) stays the old filled cell.
**Measured: `GlyphCursorProbe`'s output against Xvfb and against this server is identical for
fourteen characters**, and for the mask combinations above.

**And the embedded cursor-font table belongs to the cursor font alone - which this item got wrong in
the other direction.** It said "the font argument is not consulted at all on that path, because a
client that asks for `XC_left_ptr` means the arrow every X server draws"; true for the cursor font and
false for every other one, and the two collide because the cursor font's table is indexed by *code*.
Measured: character 66 is one of the cursor font's codes, so `CreateGlyphCursor(fixed, 66)` answered
with a cursor-font shape here and with **that font's own 'B'** on a reference server. The table is now
consulted only for a font whose name says it is the cursor font (or for no font at all, which is how
the callers that mean the cursor font pass it). Found while A8's `ChangeCursor` was being measured -
the cursor probe was the first thing to ask a *non*-cursor font for a code in that range.

**The one thing the probe cannot check is the two colours**, and that is written down rather than
guessed: both servers answer the image as opaque black whatever colours the request named, so
`GetCursorImage` cannot tell them apart, and this server draws the source's ink in the foreground and
the rest of the silhouette in the background - the spec's composition and the one the cursor font's
own path already uses. The measurement that would settle the other reading (the mask's ink in the
background colour with the source invisible, which is what the probe's *silhouettes* are consistent
with) is the sprite's own pixels in a `-scene-` frame dump, where a JavaFX backend draws them.

**`ChangeCursor` is the fifth leftover and is done (2026-09-26)**, measured first with
`tools/ChangeCursorProbe`: the two arguments are `source` then `destination` and it is the
**destination** that changes; after the call a window already showing the destination shows the
source's shape *and reports the source's serial*, so the two ids name one cursor; the source is
untouched; and **no cursor notification is raised** - a rewrite is not a cursor change. This server
implements it by rewriting the destination **in place** (`XCursor.adopt`), which is what those answers
show: a window that was already holding it sees the new shape, and the tick's notification stays
silent for free, because the cursor the pointer shows is still the same cursor. The sprite cache in
`InputState` gained an image version for the same reason - it is keyed by cursor identity, and a
rewritten cursor keeps its identity. `XFixesCursorImageTest` pins all of it.

**And a cursor's *name* is honoured (2026-09-26)**: `SetCursorName` used to be accepted and ignored
with a log line saying cursors carry no names here, which is true of the shapes this server makes and
not of the name a *client* gives one. Measured against a reference (`GlyphCursorProbe ... names`): a
name given is an atom and a name read back, and a cursor nobody named answers `None`. One difference
is recorded in the code: an explicitly *empty* name answers `None` here where the reference interns
the empty string and reports that atom.

**Left open here, and `ChangeCursorByName` now has a measured answer rather than a guess**: it stays
refused. The probe calls it with `left_ptr`, with `hand2` and with a name nothing has, and a reference
server does **nothing at all** for every one of them - no error, and neither the window showing the
cursor nor a window that takes the same cursor id afterwards sees any change (measured 2026-09-26).
The name is looked up in the *cursor font*, and the cursor font's glyphs carry no names on this
machine: `cursor.pcf.gz`'s character-name table is far too small for its seventy-odd glyphs, so the
lookup fails and fails silently. A server that refuses with `BadImplementation` is therefore the
honest one of the two - the extension's own rule, "rejected rather than silently ignored" - and there
is nothing to copy. `CreateRegionFromPicture` and `SetPictureClipRegion` also stay refused: a picture
here has no client clip of its own (its clip is recomputed from the window's visible region on every
request), so the first would answer with something that is not a picture's clip and the second would
have to intersect a client clip in the drawing path. The first two would rewrite a cursor image in place, and a cursor
is immutable here (a window holds a reference to one), so honouring them needs a level of
indirection this resource model does not have; the third needs a picture's clip read back as a
region.

**`GetCursorImage`/`GetCursorImageAndName` were a fourth, and they are done too (2026-09-26).** Both
answered `rootCursor()` with a comment saying the root cursor never changes, so a client that reads a
cursor back - a screencast drawing the pointer itself, a magnifier, a remote-desktop client, and the
kit's own `GlyphCursorProbe` - got the arrow wherever the pointer was. Measured against Xvfb: it
answers with the cursor the *window* was given (a glyph cursor set on a window came back as that
glyph), and this server answered the arrow for the same sequence. Both now read
`InputState.displayedCursor()` - the same reading the cursor *notifications* have always used - and
the serial travels with it, which is the rule the notifications were measured to follow. The A8
probes never caught it because `CursorProbe` counts *notifications*, and this is a reply: the gap was
in the space between two tools, which is the sort of thing only a new question finds.
`XFixesCursorImageTest` pins it and fails without the change (16x16, the root cursor, where the
window's is 6x13).

**`HideCursor`/`ShowCursor` were the third leftover and are done (2026-09-26).** They used to only
count - `InputState` drew the sprite unconditionally, so the counter was a number nobody read. The
count is now per window (`XWindow.cursorHideCount`) and `InputState.cursorSprite()` walks from the
window the pointer is in up its parents, so the cursor is hidden while the pointer is in the window
that asked *or anything inside it*, and shown again when the hides are given back - hidden **there**
rather than everywhere, which is what the extension defines and what a video player hiding the
pointer means. `input/CursorSpriteTest` grew two tests (three assertions on the ancestor and
counting rules, and the guard that a show with nothing to show is not a credit against a later
hide); **both fail with the hide left out of the sprite decision**. The `isCursorHidden()` accessor
and its counter were deleted rather than kept: they answered a question about the whole server, which
is not the question the sprite depends on.

---

### [x] A6. A focus change made by a keyboard grab is reported as `NotifyNormal`. **S** - done 2026-09-25

**Current.** `EventDispatcher.deliverFocusChange` always reports mode `NotifyNormal`, including the
focus changes a keyboard grab causes; a reference server reports `NotifyGrab`/`NotifyUngrab` for those.

**Why it matters.** A toolkit that tracks the mode tells "my window lost the focus because somebody
grabbed the keyboard" from "because the user clicked elsewhere"; the difference is visible in menus
and in dialog activation policies. Narrow, but it is a documented deviation
(`docs/design-notes.md`, "The focus is the client's to be told about").

**Plan.** Thread the mode through `deliverFocusChange` from the two call sites that can be inside a
grab (`InputState.keyboardGrab`/`UngrabKeyboard`), and leave `NotifyNormal` everywhere else.

**Measured 2026-09-25 with `tools/FocusProbe`** (E0 built it), which is what the plan should actually
be built against - the reference does more than change a mode word:

```
step: grabkey A          (GrabKeyboard on A; the focus is already A)     Xvfb: (nothing)
step: focus C            (a focus change asked for inside the grab)
    Xvfb: FocusOut B detail=Pointer mode=NotifyWhileGrabbed
          FocusOut A detail=Nonlinear mode=NotifyWhileGrabbed
          FocusIn  C detail=Nonlinear mode=NotifyWhileGrabbed
    here: ... the same three, mode=NotifyNormal
step: ungrabkey
    Xvfb: FocusOut B detail=Pointer mode=NotifyUngrab
          FocusOut A detail=Nonlinear mode=NotifyUngrab
          FocusIn  C detail=Nonlinear mode=NotifyUngrab
    here: (nothing)
```

So the reference's model is: **a keyboard grab moves the focus to the window it is taken on and says
so, with `NotifyGrab`; ending it reports the way back with `NotifyUngrab`; and a change asked for
while it is held is reported `NotifyWhileGrabbed`.** This server did none of it: `NotifyNormal`
everywhere and nothing at all at either end of a grab.

**A correction, because the first version of this measurement was wrong.** The paragraph above first
read "a grab on a window that already has the focus reports nothing at its start", and it was written
from a `FocusProbe` run that printed exactly that - because the probe's reply reader looked for the
`GrabKeyboard` reply and *dropped the events it passed over*, and the reply is written before them.
Every measurement in this area must come from a probe that prints what it passes over; the one in the
kit does now, and the difference between the two readings is two events and a wrong model.

**Traps.** The windows named in a grab's event group include the *pointer's* window with detail
`Pointer` when the focus side is PointerRoot, so a fix for the mode word alone still leaves the diff
short. And the whole area is the one the §7 deviations come from: a crossing or focus event that a
client did not cause is what tears EasyShell's menus down (`MenuProbe`'s `! enter without a move`
marker). **Re-run `MenuProbe` before and after** - its output against Xvfb and here is identical
today, and the gesture must still match when this is done.

**Verify.** `FocusProbe` against Xvfb and here, the grab steps byte for byte; **and `MenuProbe` on
both, whose output is identical today**. Then the EasyShell menu gesture on a live server (6 runs, no
dismissal).

**Done 2026-09-25**, in `InputState.setKeyboardGrab` (which now reports the two transitions),
`InputState.setInputFocus` (which reports `NotifyWhileGrabbed` when a grab is held) and
`EventDispatcher.deliverFocusChange`, which grew a mode and a `force` flag - the flag is what lets a
change between two windows that are the same one be reported, which is the grab-on-the-focused-window
case. The reference's rule, measured four ways with `FocusProbe` first:

```
focus on X, grabkey Y     FocusOut X (NotifyGrab), FocusIn Y (NotifyGrab)
focus on X, grabkey X     FocusOut X (NotifyGrab), FocusIn X (NotifyGrab)   - a self-pair
focus none, grabkey Y     (nothing)
ungrabkey                 FocusOut <grab window>, FocusIn <focus now>        - both NotifyUngrab
focus change inside grab  reported NotifyWhileGrabbed
```

**Measured 2026-09-25.** `input/KeyboardGrabFocusTest` (5 tests) pins all four cases plus the guard
that a change outside a grab is still `NotifyNormal`; **four of the five fail with the reporting
disabled** (the fifth is the guard, and passes either way, which is the point of it). `FocusProbe`
against Xvfb and here now agrees on every grab line - the two remaining differences in its default
sequence are the crossing and `PointerRoot` differences recorded under A5, none of which is about
this item.
`MenuProbe`'s output on both servers is still **identical**, so the §7 menu behaviour is intact.
`mvn test` 203/203.

**Live, both clients**: with `--single-window` and a request log, **neither EasyShell nor termora
ever calls `GrabKeyboard` or `GrabKey`** - JavaFX grabs the *pointer* for its context menu
(`GrabPointer`/`UngrabPointer`, which is why §7's crossing deviation exists) and termora draws its
menu as a lightweight Swing popup with a pointer grab. So this fix is currently inert for both, and
the honest verification is the reference diff rather than a client. The menu gesture was run anyway,
6 out-and-back passes each: EasyShell's menu (an override-redirect window) stayed up 6/6 and
termora's stayed up 6/6 with the hovered item tracking the pointer, which is the regression check the
item's own Verify asked for. (The request log also turned up a bug in the kit: `WinTree` wrote its
property length into the **offset** field, so every title read was refused `BadValue` and the tool
printed no names - fixed, and it now prints `WM_CLASS`/`WM_NAME` for both clients:
`cn.oyzh.easyshell.EasyShellApp` and `Termora`.)

**Traps worth keeping.** The grab requests answer with a reply **before** the events they cause, so a
reader that looks for the reply first throws the events away - the first version of `FocusProbe` did
exactly that and produced a measurement that was wrong in a way nothing else would have shown. And
`NotifyWhileGrabbed` (3) is a mode core focus events have but EnterNotify/LeaveNotify do not: the two
event families share the detail lattice and not the mode numbering.

---

### [x] A5. The focus under `PointerRoot`: the premise was wrong, the details are not. **M** - done 2026-09-25

**Done.** `FocusProbe`'s default sequence now prints the same lines against Xvfb and here, and so do
twelve smaller sequences written to pin the corners. The three groups this item listed came down to
**two states that this server kept as one**, plus a chain-order rule:

- **The root window is not `PointerRoot`.** `SetInputFocus` with the root's own id is a state of its
  own: `GetInputFocus` answers that id (where this server answered `1`), the keys stay on the root
  window rather than following the pointer, and the events differ - measured, `"focus A" "warp 50 50"
  "focus pointer"` and the same with `"focus root"` produce completely different sets. A reference
  server *starts* with the root window focused (`xdpyinfo`'s "focus: window 0x1ff"), so this was
  visible from the first request a client made. `InputState` carries the state as a flag beside the
  window; `FocusProbe` gained the root's name (`R`) so a diff can see which of the two a server means.
- **The chain arrives outermost first**, on the way in as well as out. This server sent the incoming
  chain innermost first, which a case with the pointer inside a nested window shows and no other case
  does.
- **The `Pointer` bookkeeping**: the windows the pointer is inside are told when the focus moves into
  or out of the focus the pointer implies - a `FocusOut` with detail `Pointer` on the way out of a
  window, before the ordinary pair, and a `FocusIn` with the same detail on the way in, after it. The
  rule is in `EventDispatcher.pointerLeft`/`pointerStillIn`/`pointerArrived`, and **two of its clauses
  are measured rather than derived**, which is why they are commented there and not here: a change
  *to* `PointerRoot` reports the window the pointer is in even when that window is the one it is
  leaving, and a change to the *root window* does not. The item's own warning stands - "guessing at a
  bookkeeping rule is how the wrong model gets written down" - so the clauses are written as the
  measurements they are, with the sequences named.

**Measured 2026-09-25**, with `tools/FocusProbe` against Xvfb and here: the default sequence, and
twelve more (`"focus A" "warp 50 50" "focus root"`, `"focus A" "warp 50 50" "focus pointer"`,
`"warp 50 50" "focus C" "focus pointer"`, the pointer outside every window, `focus root` from a
window and from a nested one, `focus none` from a window and from PointerRoot, a pointer inside and
outside the window being left) - **all identical line for line**. `MenuProbe` is still identical too,
which is §7's gate for this area. `input/FocusReportingTest` (6 tests) pins the rules - **three of
them fail** with the state folded back and the chain order reverted. `mvn test` 248/248.

**Live**: EasyShell and termora, both connected to a `--single-window` server: EasyShell's context
menu survived the six out-and-back passes 6/6, and termora's menu survived 6/6 with the two captures
byte-identical. Neither declares a gravity and neither had an error in the server log beyond the three
already recorded (RandR's `GetOutputProperty` and JavaFX's two asks about window `0x0`).

**Two existing tests changed**, both of which had encoded the old folded model: `FocusEventTest`'s
menu case relied on the server's initial focus being PointerRoot (it is the root window), and its
`givingTheFocusBackToThePointerMovesItToThePointer` asserted the incoming chain innermost first.

**The corner that was left open, measured and fixed 2026-09-25.** `FocusProbe` had no step that
destroys a window, so the revert rules had never been exercised against the reference. It has one now
(`kill A|B|C`), and the first sequence tried found a real difference. With `B` focused inside `A`,
`kill A`:

```
this server                        Xvfb
FocusOut win=B detail=Ancestor     FocusOut win=B detail=Ancestor
FocusIn  win=A detail=Inferior     FocusOut win=A detail=Virtual
FocusOut win=A detail=Ancestor
focus = R                          focus = R
```

The focus reverts in **two steps** here - `B` to its parent `A`, and only then to the root once `A`
goes away too - so a window being destroyed by the very request is told it has gained the focus and
then that it has lost it. Xvfb reverts past the whole subtree in one step. The cause is the order in
`WindowHandlers.destroyWindowTree`: the children were destroyed (and the focus reverted) **before**
the parent was unmapped, so the parent was still viewable when the revert looked for an ancestor to
land on. Unmapping the window before destroying its subtree makes the whole of it stop being viewable
at once, and the revert then walks past all of it - `FocusOut A detail=Virtual` and nothing else.
Measured afterwards: five `kill` sequences (`focus B` then `kill A`, `focus A` then `kill A`, `focus
A` then `focus none` then `kill A`, `focus A` then `kill B`, `focus C` then `kill A`) and the default
sequence are **all identical to Xvfb**, where the first of them was two events out before. `mvn test`
260/260 - the destroy order is used by every test that tears a window down.

**The same corners through the other way out, measured 2026-09-25.** `FocusProbe` gained an
`unmap A|B|C` step beside `kill`, and five sequences (`focus B` then `unmap A`, `focus A` then
`unmap A`, `focus B` then `unmap B`, `focus A` then `unmap B`, `focus C` then `unmap A`) are **all
identical to Xvfb already** - the unmap path reverts *after* the window has stopped being viewable,
so it never had the ordering fault the destroy path had. That is what makes the two-event difference
above a destroy-path bug rather than a rule that was wrong everywhere.

**The original wording, kept for what it measured:**

### [x] A5old. The focus under `PointerRoot`: the premise was wrong, the details are not. **M** - superseded: A5 has the record, and this is kept for what it measured

**What the item claimed.** That with the focus on `PointerRoot` a server "is supposed to move the
focus from window to window as the pointer moves and report each move", that this server does not, and
that it should be implemented in the crossing path.

**Measured 2026-09-25 with `tools/FocusProbe`, and the premise is false.** Against Xvfb, with the
focus on `PointerRoot` and three of one client's windows each selecting `FocusChangeMask`, a sequence
of pointer warps from the root into A, into B (A's child), into C and back out produced **crossings
only - not one `FocusIn` or `FocusOut`**:

```
step: focus pointer                     focus = PointerRoot
step: warp 5 5                          EnterNotify A detail=Ancestor mode=NotifyNormal focus=yes
step: warp 50 50                        LeaveNotify A detail=Inferior, EnterNotify B detail=Ancestor   focus=yes
step: warp 500 150                      LeaveNotify B, LeaveNotify A, EnterNotify C                    focus=yes
step: warp 1000 600 (onto the root)     LeaveNotify C detail=Ancestor                                 focus=yes
```

The specification agrees: "If focus is PointerRoot, the focus window is dynamically taken to be the
root window of whatever screen the pointer is on at each keyboard event" - the root, per screen, not
the window under the pointer, and nothing in that generates a focus event per window. What a client
learns from a `PointerRoot` focus is carried by the **crossing's focus bit**, which the reference sets
in every one of them. The key events themselves *do* go to the window under the pointer: measured with
`xev` on Xvfb - focus `PointerRoot`, pointer parked over xev's window, a key sent through XTEST - xev
printed the `KeyPress` and the `KeyRelease`. This server does the same, so that half already matches.

**So do not implement "focus follows the pointer".** It is not what a reference server does, and an
event the pointer did not cause is exactly the shape §7 records as tearing EasyShell's menus down.
"Sloppy focus" is a *window manager's* job - it watches `EnterNotify` and calls `SetInputFocus` itself
- which is the division this server is built on.

**What the same runs did turn up.** The default sequence still differs from the reference, none of it
about the pointer moving. Two of the differences were the crossing events' own reporting and are
**done** - see A14 below: the flags byte, the focus bit, the chain order and the chain detail. What is
left is the `PointerRoot` and `None` corners, and one of them is a state this server does not have at
all:

1. **The detail lattice against `PointerRoot`.** Going *to* `PointerRoot`, the reference relates the
   leaving window to the **root window** (`FocusOut C detail=Ancestor`); this server relates it to the
   window the pointer is in (`Nonlinear`). Coming *from* `PointerRoot` the two agree - so it is the
   leaving side that is wrong. (`FocusProbe`'s default sequence has two lines of this, the last
   step's.)
2. **A `FocusIn` with detail `Pointer` that the reference sends to the window the pointer is in**,
   around a change from `None` to a window holding it - six events in the default sequence, all of
   them around the grab steps, none of them sent here. Not explained yet: it looks like Xorg's
   per-window "focus in" bookkeeping being kept consistent (`dix/events.c` and its `focus->focusin`),
   and guessing at a bookkeeping rule is how the wrong model gets written down.
3. **`SetInputFocus` on the root window is not the same as `PointerRoot`.** `FocusProbe`'s `focus
   root` sends the root *window id*; the reference then answers `GetInputFocus` with that id
   (`focus = 0x1ff`) and reports the transition against it, where this server folds "the root window"
   into `PointerRoot` and answers `1`. The two states are two, and a client can tell them apart by
   both answers. **Corrected 2026-09-29**: the *keys* are not one of the differences - "keys go to the
   root window in one and follow the pointer in the other" was an inference from the states being
   different, and it is wrong. Measured with `tools/KeyTargetProbe` (written for the question): against
   Xvfb a `KeyPress` is reported against **the window the pointer is in** for `PointerRoot`, for
   `SetInputFocus(root)` and for a revert that lands on the root alike, and only `None` leaves it
   unreported. This server sent it to the root window in the two states that are not `PointerRoot`,
   which is a text field that looks focused and accepts nothing - see the 2026-09-29 entry in
   `verification-checklist.md` and the omitted-things list in `design-notes.md`.

**Traps.** Both the pointer *and* the focus outlive the connection, so a probe that does not park the
pointer and set the focus before its first step is measuring its own previous run: "the reference
sends a pair here" and "the reference sends nothing at all" were both true of the same request, an
hour apart, for exactly that reason. `FocusProbe` now parks the pointer, sets the focus to `None`, and
prints the server's own `GetInputFocus` answer after every step - which is what made point 5 visible.

**Verify.** `FocusProbe` (default sequence) against Xvfb and here, diffed line by line - **22 differing
lines today**, and the five groups above are all of them. `MenuProbe` on both must stay identical.
Then the EasyShell and termora menu gestures, 6 runs each.

### [x] A14. The crossing events' flags byte, focus bit, chain order and chain detail. **S-M** - done 2026-09-25

**What it was.** Four things a crossing event says besides the window it names, all of them measured
wrong here and none of them visible from the server's side of the socket.

- **The last byte packs the focus flag in bit 0 and `same_screen` in bit 1** - the opposite of what
  the field's name suggests. This server sent `0x01` where a reference server sends `0x02` in the same
  state, which **Xlib decodes as "same screen: no, focus: yes"**: every crossing this server sent
  claimed the pointer was on another screen, and reported the focus bit inverted whenever the
  crossing's window was not focused. Found by reading the same event with an independent decoder -
  `xev` on both servers - after the probe's own reading of the bit had been taken at face value for an
  hour.
- **The focus bit is set when the crossing's window is inside the focus window** (the focus window or
  a descendant of it). This server asked the relation the other way round
  (`window.isAncestorOf(focus)`), so it was set for the focus window's *parents* and clear for its
  children - the wrong answer for all three cases the measurement pins: a child of the focus (set),
  the focus's own parent (clear), an unrelated window (clear).
- **The `EnterNotify` chain arrives outermost-first, with the window itself last**; this server sent it
  innermost-first. Leaving is innermost-first in both, so the two directions are genuinely
  asymmetric - a toolkit walks *down* into the window the pointer entered, which it can only do if the
  windows holding it are named first.
- **A window in the middle of a chain reports `Virtual` when the window on the other end is above it**
  and `NonlinearVirtual` when the other end is unrelated. This server reported `Virtual` for every
  window in the chain, which tells a toolkit the pointer moved inside its own subtree when it left it.

**Measured 2026-09-25.** `input/CrossingReportTest` (4 tests, a three-deep tree so that a window can
be a *virtual* ancestor rather than a parent): the flags byte for a focused and an unfocused crossing,
the focus bit for a child, the parent and an unrelated window, the order of a three-window enter
chain, and both chain details. **All four fail when their own fix is reverted** - verified one family
at a time, since three of them are one assertion away from each other. The flags-byte fix was also
read back through Xlib: `xev` on this server now prints `same_screen YES, focus NO` for exactly the
crossing that used to decode as `same_screen NO, focus YES`, matching the reference word for word.

**And the regression check that matters**, because these four change what every popup is told:
`tools/MenuProbe`'s output against Xvfb and here is **still identical** (it was identical before, and
the whole point is that it stays so - §7's deviations live in this path); and live, with
`--single-window` on a real server, **EasyShell's context menu survived the out-and-back gesture 6
runs out of 6** with its hover highlighting working, and **termora's menu survived 6 out of 6** with
the hovered item tracking the pointer. `mvn test` 207/207.

**Traps worth keeping.** A probe that reads a bit out of a byte is asserting its own reading of the
protocol, not the server's behaviour: the fix here was to print the **raw byte** and compare it with
what Xlib decodes the same event into, and the two disagreed. Every one of the four was also invisible
to the suite as it stood - the server side agreed with itself in each case - so a test written from
the server's own model would have passed against every one of them.

---

### [x] A15. A filled polygon's scanline crossings were taken half a pixel low. **S** - done 2026-09-25

**What it was**, and it is one character of code: `Rasterizer.fillPolygon` computed each edge's
intersection with the scanline at `y + 0.5` (the pixel's centre) where a reference server takes it at
`y`. The horizontal rule was right on both - a span runs from `ceil(xl)` to `ceil(xr) - 1` - so what
moved was *where the edge crosses*, by half a pixel of the edge's slope: a quarter of a pixel in x for
a slope of one, and a whole pixel for a slope of two. On a nearly straight edge it shows on the rows
where the crossing lands near an integer; on a concave shape it changes the area.

**How it was found, and it was not by reading the code.** E3 recorded a live pixel gate - `xlogo`
drawn on both servers, captured the same way, diffed over the logo's own area - and it left **377 of
46656 pixels differing (0.8%)** with full-scale deltas, checked to be the same with E3's fast path in
and out, so pre-existing and unowned. Chasing it meant answering "which primitive", because the first
guess (a wide diagonal *stroke*) was wrong: with `--log-requests` on, `xlogo` turns out to send **five
`FillPoly` and no lines or arcs at all**.

So the harness is `tools/PolyProbe` (new): it fills a fixed table of polygons into a window and reads
the pixels back with `GetImage`, printing them as text, so the two servers' scan conversion can be
diffed line by line. The polygons are chosen so that the crossings land on every kind of fraction -
slopes of 1 (a control, where every rule agrees), 2, 44/21, 44/17 and 44/45, a quadrilateral with no
axis-aligned edge, a sixteen-gon, and the five-pointed star from the suite - because a fractional part
below a half is the only place the two candidate rules differ at all.

**The first version of the probe proved nothing, and that is the trap worth keeping.** It drew no ink
- twice over, for two independent reasons:
- **`FillPoly` is the one drawing request whose bytes are not where you would guess.** Its shape and
  coordinate-mode bytes come *after* the drawable and the GC, not in the data byte every other request
  has, and it carries **no point count at all** - the server derives it from the request length. The
  first version put the shape at byte 1 and a count at byte 16; the server accepted it and drew
  nothing. The layout is in `VariableLengthDecoders.readFillPoly`, and reading it *first* is what
  would have saved the hour.
- **A window paints its background when it is mapped**, so anything drawn before the map is painted
  over.

Neither failure announces itself: the probe printed a clean grid of dots on both servers and the
diff was **zero differing lines**. A probe that draws nothing reports agreement, so the tool now
prints ink counts and the run is only read when they are non-zero - the same shape as E0's "reply
sequence check" trap, and the reason the first xlogo comparison here was re-run rather than believed.

**Measured 2026-09-25**, three ways:

- **`tools/PolyProbe` against Xvfb and here, line for line: 226 differing lines before the fix and
  zero after**, over the ten polygons. The ink counts per cell agree too - the star fills **34**
  pixels on both, where this server used to fill 28.
- **`xlogo` against Xvfb, over the logo's own 220x220: 377 of 46656 pixels before (0.8%), 0 of 48400
  after.** The whole window is now pixel-identical, not "close".
- `draw/RasterizerTest` grew `polygonCrossingsAreSampledAtTheScanlineAndNotAtItsCentre`, which pins
  the two cases from the probe's own triangle (slope 2, so its crossings land on an integer at every
  scanline): row 2's span ends at 45 and row 12's at 25. **Both assertions fail with the sampling put
  back at `y + 0.5`.** The suite's existing star assertion had to change with it, and the way it had
  been written is the reason this is worth recording: it asserted **28 and 41** - *this server's own
  answers* - while its comment already said "Xorg answers 34 and 47 for the same star". The numbers
  were known and the test was pinned to the wrong ones, so nothing failed when the code was wrong.
  `mvn test` 264/264.

**Live, and this is a rendering change so both clients were looked at**: EasyShell connected, drew its
window and its interface, and its context menu opened and survived the six out-and-back passes **6/6**;
termora connected, drew its window with the menu bar and its Chinese
labels, and its menu survived **6/6**, with **no ERROR lines** in the server log either time. An
A/B of EasyShell's own rendering against Xvfb was attempted and is *not* part of this record: the
second EasyShell cannot start while the first holds `~/.easyshell/db.mv.db` (the H2 lock E0 records),
so the two runs have to be strictly sequential - which `tools/compare.sh` does and a hand-run does not.

**Traps worth keeping.** The half pixel is invisible to every test written from the server's own
model, which is why the star assertion passed for a week while being wrong: a scan-conversion rule can
only be checked against another implementation. And the xlogo difference was attributed once before -
E3's note guessed "geometry rather than blend rounding" and was right, but the geometry it meant was
the *stroke*; a request log is what says which primitive a client actually uses.

---

### [x] A1. Passive grabs are recorded but never matched. **M** - done 2026-09-25

**What it was.** `GrabButton`/`GrabKey` were added to `XServer.passiveGrabs()` and removed by the
matching `Ungrab*`; **nothing read the list**. A client that armed one - a global shortcut, a toolkit's
popup binding, a window manager's frame buttons - never heard about the button or the key it asked for.
The last hole in the input model.

**What changed.** `InputState.matchingPassiveGrab` walks the window the event landed on and its
ancestors, so a grab on a frame beats one on the root, and `buttonChanged`/`keyChanged` match before
the event is delivered - a grab that fires means the input belongs to the client that armed it,
wherever it landed. The rules, and all of them were measured against a reference server with
`tools/GrabProbe`:

- **The modifiers**: `AnyModifier` (0x8000) matches any state; a specific one must match the state
  **exactly**. Measured: a grab armed with `ShiftMask` fires with Shift held and does not fire without
  it - and the state compared is the *modifiers*, not the whole pointer state, which carries the button
  that was just pressed (the first version compared that, and a Shift grab never fired).
- **The detail**: `AnyButton`/`AnyKey` (0) matches everything; a specific one only itself.
- **Two arms that match**: the specific modifier beats `AnyModifier` (the closest match), and among
  equals the **last armed wins** - which is what the protocol's "an existing grab with the same
  parameters is replaced" amounts to, and why `grabButton`/`grabKey` now remove an equivalent arm
  before adding theirs.
- **The lifetime**: the *grab* lasts from the press to the release (a button grab holds the pointer
  for exactly that long, a key grab the keyboard), and the *arm* lasts until the client disarms it -
  so the next click fires it again, which is what a shortcut is. Ending the grab is reported to the
  application as the crossing pair's second half.
- **The crossing pair a reference server sends is sent**: `LeaveNotify(NotifyGrab)` on the window the
  pointer is in and `EnterNotify(NotifyGrab)` on the grab window, and the reverse with `NotifyUngrab`
  at the release. **This is the pair §7 withholds for a grab a client takes on its own request**, and
  the measurement is what separates the two cases: a reference server sends it for a *passive* grab
  (measured), and a shortcut's client is not a menu that can be dismissed by it.
- **The press reported to the grabbing client names the child** of the grab window the pointer is in,
  which is how a client that owns the whole screen tells which of its windows was clicked.

**Measured 2026-09-25**, three ways:

- **`tools/GrabProbe` against Xvfb and here, line for line**: with the grab on the root and the pointer
  over another client's window, the press and release go to the grabbing client (reported against the
  grab window, `child` naming the window actually under the pointer), the other client gets the
  `LeaveNotify(NotifyGrab)`/`EnterNotify(NotifyUngrab)` pair, and the two outputs differ in **nothing
  but the window ids**.
- **`input/PassiveGrabTest`** (5 tests): the click belongs to the client that armed the grab and the
  client under the pointer hears nothing; the grabbing client is told the button came up; the pair
  that says the grab is over; a specific modifier fires only with it held (and the event reports the
  state it happened in); a key grab takes the key from the focused window; and the guard that a client
  which armed nothing is unaffected. **Four of the five fail with the matching disabled** (the fifth is
  the guard, and passes either way, which is the point of it).
- `mvn test` 233/233, `MenuProbe`'s output against Xvfb and here still **identical** (§7's gate), and
  live: EasyShell's context menu opened and survived the 6-run gesture, termora's menu opened, and the
  server log had no ERROR lines.

**Two traps, both of which cost a test rewrite.**

- **An event *mask* is not an event *type*.** The first version read its assertions with the mask
  values: `ButtonReleaseMask` is 8 and the event's type is 5, `KeyPressMask` is 1 and the type is 2 -
  and `ButtonPress` is 4 in *both*, which is what hides the mistake. A test reading with the mask waits
  for an event that never comes, and that looks exactly like a server that delivers nothing (the
  client was being sent everything, and the debug print showed it).
- **An armed grab is not an active one.** "The grab ends with the button" is about the *active* grab;
  the arm stays until `UngrabButton`, so the *next* click fires it again. The first version of the test
  expected the second click to be the application's and read as "the grab outlived the button" - the
  implementation was right and the test was wrong.

**Also found while measuring**: `GrabProbe`'s own `UngrabButton`/`UngrabKey` put the button in the
field the window goes in (the button is the byte right after the opcode), so the probe had been
disarming nothing; both it and `RawXClient`'s new helpers are written from the generated reader.
---

### [x] A9. `IncludeInferiors` and the backing store. **M-L** - done 2026-09-25

**What it was.** `Picture.subwindowMode` was stored and never consulted, so every picture over a window
behaved as `ClipByChildren`: it read the window's own pixels and stopped where a child began. For a
frame whose content child covers it - every toolkit's toplevel - that meant **no pixels at all**, so a
client copying "what this window shows" through its parent got an empty picture. `backing_store`,
`save_unders` and `XWindow.backingPixel` are still stored and unused.

**What changed (2026-09-25).** One line, and the plan above was right about where: `Picture.refreshClip`
takes the window's region with its children when the mode says `IncludeInferiors`
(`window.coveredRegion()`, which already existed for the "what does this window leave behind" question)
and without them otherwise (`visibleRegion()`). **What is stacked above the window is excluded either
way** - those pixels are not the window's to own - and the window rectangle still bounds it, so
`IncludeInferiors` widens a window to its children rather than to the screen, and a *write* with the
mode is still clipped to the window's own rectangle (the item's trap).

**Measured 2026-09-25.** `resource/IncludeInferiorsTest` (4 tests, in process, on a parent with a
painted child): the default does not see the child; `IncludeInferiors` does, and still sees the parent's
own pixels; a window stacked above the parent is excluded either way; and the corners outside the
window answer nothing. **The one that fails without the change is the second** (it reads 0 where the
child's colour should be, which is exactly the symptom this item is about); the other three are the
guards that say what did not change. `mvn test` 237/237.

**Done 2026-09-25: the backing store, and the save-unders half measured to be nothing to do.**

**Measured first, with a new harness, `tools/backing-store-probe.py`** (Python rather than one of the
Java tools: it needs `XChangeWindowAttributes` with a value list and `XGetImage` on the root, and a
second opinion from *Xlib* is worth more than another hand-built request here). One red window, a blue
one mapped over its middle, then the blue one unmapped, reading the pixel and counting the `Expose`
events:

```
                     Xvfb        here (before)   here (after)
window asks nothing  1 expose    1 expose       1 expose
backing_store=WhenMapped  0 exposes  1 expose   0 exposes
save_under           1 expose    1 expose       1 expose
```

Two things fall out of that table. **The reference implements backing store**, so a window asking for
it is a real difference and not a hypothetical one - and its setup reply says so
(`xdpyinfo`: `backing-store WHEN MAPPED, save-unders NO`). And **the reference does not implement
save-unders**: it advertises `NO`, and its behaviour with a window asking for it is identical to the
behaviour with a window asking for nothing, so this server matching it *is* the implementation.

**What was built.** `XWindow` keeps a buffer for a window whose `backing_store` is `WhenMapped` or
`Always` - allocated on demand, so the memory is the client's decision and not something spent on
every window - and saves the window's visible pixels at the moment something covers it
(`WindowHandlers.captureUnder`, called before the covering paint) and puts them back on the reveal
(`repaintUnder`), which is what makes the reveal need no `Expose`. The region that needed writing
first is `XWindow.contentRegion`: the window's own rectangle minus its children, **ignoring what is
above it**, because at the moment of capture the covering window is already above and the pixels
worth keeping are no longer in the visible region. Three things throw the buffer away, and each is a
way the saved pixels stop being what the window would show: the client drew (**4**), the window moved
or was resized, and the attribute was turned off. `Screen` now advertises `backingStores = 1`.

**Superseded on 2026-09-27 by B5's general store**: every window keeps its own pixels now, so the
reveal needs no `Expose` for all of them and the capture/restore buffer (and `contentRegion` with it)
is gone. What the item measured - a reference delivers no `Expose` and the same pixels - is what this
server now does unconditionally, which is the behaviour this entry was written to reach.

**The memory question the item raised is answered rather than deferred**: nothing is allocated for a
window that does not ask. The item's warning - "256 clients each keeping a full window's pixels" -
applies to a server that keeps them for everybody, and that is not what this is. (B5 does keep them
for everybody, which is why `ownPixels` is allocated lazily on the first draw - E4's rule.)

**Measured 2026-09-25**, three ways:

- `tools/backing-store-probe.py` in its three modes, against Xvfb and here: the table above, matching
  in every cell.
- `resource/BackingStoreTest` (3 tests): a window that asked for a store is not exposed when it is
  uncovered and its pixels come back; a window that did not ask is exposed, as before; and a window
  that *drew* while it was covered is exposed too. **The first fails with the restore disabled**
  (`expected: <null> but was: <Expose...>`), and the other two are the guards that say what did not
  change.
- `mvn test` 251/251, `MenuProbe` and `FocusProbe` still identical to Xvfb, and live: EasyShell's
  context menu survived the six out-and-back passes 6/6 and termora's 6/6 with two byte-identical
  captures. Neither client asks for a backing store (measured with `xwininfo -all`: both are
  `NotUseful`), so the change is inert for them - which is the point of doing it through the
  attribute rather than for every window.

**Traps.** The capture has to happen *before* the covering window is painted, and the obvious way to
write it - `XWindow.visibleRegion()` - is wrong at exactly that moment: the covering window is
already mapped, so the region no longer contains the pixels about to be painted over, and the first
version silently saved nothing (the probe's `restored=0`). `saveUnder` and `backingStore` are not
interchangeable: the first saves what is *under* a window for the window below's benefit, the second
saves the window's own pixels. And the advertised capability is part of the contract: a server that
answers `backing-store NO` and then keeps the pixels anyway is lying to a client that reads it.

**Traps.** `GetImage` uses `plane_mask` and its own rules, not `subwindowMode` - so the item's Verify
(`xwd -id <frame>`) is not the check for this half, and the in-process test is. A picture's `sample`
takes a *point*, so a pixel is sampled at its centre (`x + 0.5`); sampling its corner reads the
neighbour.
---

### [x] A13. `AllowEvents` freeze/thaw semantics. **M** - done 2026-09-26: X's grab states, the held queue and the replay
**Corrected 2026-09-25: this item was closed on a measurement of the wrong experiment.** `tools/AllowEventsProbe`
had the grab modes **backwards**. X's `GrabModeSync` is **0** and `GrabModeAsync` is **1** (`X.h`:
`#define GrabModeSync 0` / `#define GrabModeAsync 1` - the opposite way round from `AllowEvents`, where
`SyncPointer` is 1), and the probe passed `1` while printing "pointer_mode = Sync". So every run it
ever made was of an **asynchronous** grab, which freezes nothing by definition: "the reference holds
nothing back" was true of the wrong experiment. The probe now defaults to `GRAB_SYNC`, takes
`asyncgrab` to ask for the other, and names the mode it is using in its output.

**Measured again with the fixed probe, and the reference does freeze.** Against Xvfb, in three
arrangements:

```
active GrabPointer(pointer_mode = Sync), then press, two motions, release
  press / motions / release   -> nothing delivered
  AllowEvents(SyncPointer)    -> ButtonPress delivered
  AllowEvents(AsyncPointer)   -> the two motions and the ButtonRelease delivered
passive GrabButton on the root, pointer_mode = Sync
  the activating press        -> LeaveNotify NotifyGrab and ButtonPress delivered *with* the grab
  the motions and the release -> held
  AllowEvents(SyncPointer)    -> both motions, the release, and EnterNotify NotifyUngrab
  a second press, then AllowEvents(ReplayPointer)
                              -> EnterNotify NotifyUngrab, then the press AND its release delivered
                                 with no NotifyGrab - the event reprocessed as if the grab had not
                                 fired, which is what a replay means
passive GrabKey on the root, keyboard_mode = Sync, two connections
  the key                     -> the grabber is told the KeyPress only; the KeyRelease is held
  AllowEvents(ReplayKeyboard) -> the focused CLIENT is told the KeyPress and the KeyRelease
```

So a `Sync` grab freezes the device, the events that follow are not delivered until `AllowEvents`,
`Sync*` releases the held event to the grabbing client, `Async*` flushes the rest, and `Replay*`
reprocesses them as if the grab had not fired - which is only observable with a second client, the
arrangement the keyboard case uses.

**Done 2026-09-26: modelled from the source, then measured.**

**The puzzle the item was opened on has an answer, and it is X's own**: the states are not a boolean.
`FREEZE_NEXT_EVENT` means "start again, and stop on the *next key or button*" - and a **motion is not
one of those**. So an active grab taken with `Sync` (which starts at `FROZEN_NO_EVENT`) releases
everything up to and including the first button and nothing after it, while a passive grab (which
freezes *on* its triggering press) has already used that event up and releases the rest. The rules came
off `include/inputstr.h` (`GRAB_STATE_*`, with `FROZEN` a *sentinel* that `FREEZE_NEXT_EVENT` sits
deliberately below) and `dix/events.c` (`AllowSome`, `CheckGrabForSyncs`,
`FreezeThisEventIfNeededForSyncGrab`, `ComputeFreezes`, `PlayReleasedEvents`, `EnqueueEvent`,
`ActivateGrabNoDelivery`), and every one of them was then confirmed against the reference server.

What `InputState` grew, following X: a per-device sync state (X's `GrabInfoRec.sync`), a queue of the
events a frozen device holds back, a replay slot, and the two transitions that fill them -
`grabActivated` (a grab's two modes decide which device freezes, and whether it does so on the *other*
device's behalf) and `freezeThisEvent` (the event that arrives while a device was told to freeze on the
next one is what it freezes on, and what a `Replay*` later reprocesses).

**Four things the source settled that guessing would have got wrong, all of them measured:**

- **The pointer has two positions while a device is frozen**, and a client can read both: `QueryPointer`
  answers where the pointer was when it stopped, and the events released afterwards carry where the
  device went (`EnqueueEvent` advances the sprite's *physical* position and not the one clients see).
  Measured with a purpose-built probe: frozen at (200,200) with motions to 220,220 and 400,400 waiting,
  `QueryPointer` says (200,200) and **every** released event carries 400,400 - the second motion's
  position, because the delivered events are stamped with where the device is now. That is the whole
  point of the mode for both of its real clients (a game re-centring the cursor, a remote-desktop
  client redelivering a click), so it is modelled rather than approximated.
- **A replay is not a second grab**: the grab is ended and the event reprocessed as if it had not
  fired, and the search for a passive grab starts **below** the window the replayed grab was held on,
  so the same arm cannot take it again. Measured: the replayed press reaches the window underneath with
  no second `NotifyGrab`, and the replayed key reaches the window that has the focus.
- **A key's repeat follows the device, not the delivery**: X cancels the repeat as a release is
  *enqueued*. Measured the hard way - with the bookkeeping left where it was (at delivery), a device
  frozen with a released key repeated into the queue at 25 events a second and reached **372 events**.
- **A passive grab freezes on the event that fired it**, which is why the press that activates one is
  delivered to the grabbing client and can still be replayed afterwards.

**The queue is bounded** (1024 events, the oldest dropped with a line in the log). The reference has no
bound here; this server refuses to grow without limit, for the same reason its output queue to a client
does (E5).

**Found on the way, and it was contaminating the measurements**: a disconnecting client's *passive*
grabs were never dropped - `releaseGrabs` cleared only active ones - so an arm outlived its client and
kept firing on the root for whoever pressed next. A reference server drops them with the client, and
until this was fixed two runs of the same probe disagreed with each other. Fixed in the same commit.

**Measured 2026-09-26**, three ways:

- `tools/AllowEventsProbe` against Xvfb and here: **all five arrangements - `active`, `passive`,
  `key replay`, and the two `asyncgrab` controls - are identical line for line.** (Window ids are
  mapped to the order they first appear, which `tools/probe-diff.py` now does.) Before the change every
  one of the five differed.
- `input/AllowEventsTest` (5 tests, two clients, XTEST, the display loop running): the freeze holds the
  press, the motions and the release, and `SyncPointer` releases exactly the one event it then stops on
  again; a frozen pointer does not move for `QueryPointer` while what it releases carries the new
  position; a replayed button press reaches the window underneath and the grab does not fire again on
  it; a replayed key reaches the window that has the focus; and the guard that an *async* grab holds
  nothing back. **Four of the five fail with `AllowEvents` put back as a no-op and the freeze disabled**
  - the fifth is the guard, and passes either way, which is the point of it.
- `mvn test` 303/303; `MenuProbe` against Xvfb and here still **identical** (§7's gate); and live, with
  both real clients on a `--single-window` server: **EasyShell's context menu survived the six
  out-and-back passes 6/6** and closed on a click outside it, **termora's 6/6** with the hovered item
  tracking the pointer. The request log has the same three rejected requests as before (RandR's
  `GetOutputProperty` and JavaFX's two asks about window `0x0`) and **not one held event** - neither
  client grabs with `Sync`, which is what makes this change inert for both of them. The only way to
  tell the difference for those two is the reference diff.

**Traps worth keeping, and the first one cost an hour.**

- **The button state outlives the client, exactly as the pointer does** (already a rule in
  `tools/README.md`). A probe run that leaves a button *down* makes the next run's press a duplicate
  the server drops, and the measurement then reads as "the press was never queued" - a whole wrong
  model of the queue was built on it before it was spotted, and the throwaway probe that chased the
  coordinates had to be taught to release the buttons first.
- **An event's coordinates are a decision, not a detail.** "The event's own position" and "where the
  device is now" differ for every held motion, and there is nothing on this server's side of the
  socket that can tell them apart - only the reference server can.
- `GrabModeSync` is **0** and `GrabModeAsync` is **1**, the opposite way round from `AllowEvents`,
  where `SyncPointer` is 1. The probe had them backwards once already (see the correction above), and
  the same mistake reads as a plausible measurement rather than as a bug.

**The protocol's time check, added 2026-09-26.** `AllowEvents` names a time, and X ignores the request
when that time is later than now or earlier than the grab itself - a *silent* refusal, not an error, so
the device stays frozen and the client has to ask again with a time it can use. This server kept no
per-grab timestamp, so it had nothing to compare against and did not check at all. `Grab` now carries
the time it was taken, and the comparison is X's own (`CompareTimeStamps`: the sign of the difference,
because the clock wraps). Verified two ways: `AllowEventsProbe` gained a `staletime` arrangement - one
`AllowEvents(SyncPointer)` with a time of 1 - and against **Xvfb it releases nothing while the
`CurrentTime` call that follows releases the press, identical line for line to here**;
`input/AllowEventsTest` grew `aTimeOutsideTheGrabReleasesNothing`, which fails with the check removed.

**Still not modelled**: Xorg's motion compression in the held queue, which merges consecutive motions.
Measured with the reference: two held motions are delivered as **two** events, both carrying the last
position, so nothing here needs the merge - it would change the count of events a client sees, and the
measurement says not to.

**Unchanged**: `AllowEvents` still validates its mode and does nothing else. Neither EasyShell nor
termora depends on a freeze - A1's `GrabProbe` measured both grabbing with `Async` - so this is a
fidelity gap rather than a failure a user meets.

---

### [x] A16. `XIQueryPointer`'s `child` was the deepest window, so a client walking the tree never reached the bottom. **M** - done 2026-09-27

**What a user met.** With EasyShell running against this server, this gesture

```
right-click a host-list entry   (opens the context menu)
leave the menu and re-enter it six times   (the menu survives 6/6 - §7's gate)
left-click somewhere in the content area, outside the menu
```

closes the menu and then leaves the client **spinning**: `XIQueryPointer` ~3700 times a second and
nothing else, until it is killed, with the server too busy to answer anything else. `GrabServer` is in
the log just before it, and no `UngrabServer` ever follows.

**It is not B5's doing** - a worktree at `17f4be9` does exactly the same thing - and **it is not the
client's either**: against Xvfb, with the same client, the same geometry (1300x800) and the same
scripted gesture, EasyShell's CPU time does not move (2s -> 3s over forty seconds). So it is a
difference between two servers, and the reference has the answer.

**The measurement that found it: a byte-transparent tap in front of Xvfb.** `XProxy` parses, and
EasyShell hangs through it, so the reference stream came from a plain TCP relay on the VM
(`python3 /tmp/tap.py 6099 6098 /tmp/tap.log`, logging both directions while a toggle file exists) with
the client pointed at `DISPLAY=192.168.22.152:99`. Decoding that stream by request gives the reference's
answer, and its shape is the whole item:

```
GrabServer
XIQueryPointer(root)        -> child = the client's toplevel
XIQueryPointer(toplevel)    -> child = None
UngrabServer
GetInputFocus
XIChangeCursor / XISetFocus
```

Two queries, and the walk is over. This server answered `input.pointerWindow()` for both - **the
deepest window the pointer is in** - and for the second query that window *is the one being asked
about*, so the answer named itself. A client that descends with this (ask about W, ask about the child
the reply names, until the answer is None) can never reach the bottom: it asks about W again, and
again, at 3700 requests a second.

**The fix** is `XInputExtension.childOf`: the queried window's **own child** that contains the pointer,
walking up from the pointer's window; null when the pointer is in the queried window itself or not in
it at all. `XInputTest.theChildIsTheQueriedWindowsOwnChildAndNotTheDeepestWindow` pins it and
**fails without the fix** (the reply names the window itself, `0x200002`, where None belongs).

**Verified live**: the request mix after the same gesture is ordinary application traffic with **7**
`XIQueryPointer` in the last 3000 log lines (it was ~2000), the button release arrives 170 ms after
the press, `UngrabServer` follows, and the client stays responsive.

**What the grab does here, measured on the way, and worth knowing**: this server's `GrabServer` holds
the request lock, so during a grab *no* other client's request runs - including the XTEST client whose
button release would end the client's own wait, and including the display tick, which takes the same
lock. A reference server defers other clients' requests but keeps rendering and input. That is a
fidelity gap, not this bug's cause (the loop continued after the release was let through when the grab
was disabled for the experiment), and it is recorded here rather than as an item of its own because a
grab that lasts is already a misbehaving client.

**For the scripts**: clicking into the content area to dismiss a menu is fine now, but the gesture
above is the harness if it ever comes back - `tools/kit.sh Drive` for the input, `WinTree` to see the
menu, and a request-mix count off the server log, which is what turns "the app feels stuck" into a
number.

---

## 3. B - Extensions a reference server has

### [x] B1. XKEYBOARD. **M-L** - done 2026-09-27: `setxkbmap -layout <name>` now works against this server (exit 0), the reply's shape is what was missing, `GetNames` is answered, and every layout this server has is byte-identical to the reference's

**Done 2026-09-25.** `extension/XkbExtension` advertises `XKEYBOARD` and answers what a client asks
before it will trust the keyboard: the version handshake (`UseExtension`), the keymap (`GetMap`, built
by `input/XkbMap`), the modifier state (`GetState`), and the requests that have no answer here
(`SelectEvents`, `LatchLockState` - accepted, not refused, because a refusal is an X protocol error and
xdotool sends a latch request as part of typing a sequence). `PerClientFlags` answers "none supported".
It is the fix for **A4**: without it, `xdotool` dereferences Xlib's null `XkbGetMap` and dies in its own
startup.

**Measured 2026-09-25**, three ways:

- **`tools/XkbProbe`** (new) against Xvfb and here, field by field: the version, the size range of the
  keycodes, the four types, each keycode's sym map and keysyms, and the modifier map as
  `(keycode, mods)` pairs. The differences from Xvfb that remain are deliberate: three types against
  twenty four, a two-level keymap against a four-level one, and twelve modifier keys against sixteen -
  this server's own keymap, which is the point of building the reply from `Keymap` rather than
  replaying the reference's.
- **`xev` on both servers, the same keycodes driven by XTEST**: 26 lines of keysym output - Escape, a
  letter, Shift, Caps Lock, Shift+Caps Lock, Return and the keypad - **identical line for line**. That
  is the check that matters for a toolkit, because the keysym a toolkit shows comes from XKB's
  translation, not from the core map.
- **`extension/XkbTest`** (5 tests): the version handshake, **at least the four mandatory types**,
  'a' and Escape's keysyms read out of the reply against `Keymap`'s own, the modifier map naming
  Shift_L, the one-way requests answered with nothing rather than refused, and `PerClientFlags`
  replying "none supported". **The four-types test fails with the keypad type removed** (`got 3`),
  and it is the one that matters: a reply with three types is not a smaller map, it is a `null` map.

**Live**: EasyShell and termora both connected and ran - windows mapped, screens rendered, EasyShell's
context menu surviving the six out-and-back passes of the §7 gesture - with **no XKB error in the log**.
Measured what each of them actually asks: EasyShell sends `UseExtension` four times, `SelectEvents`
twice and `PerClientFlags` once, and **never calls `GetMap`** (JavaFX gets its keymap from the core
request, so this item does not change how it types); `xdotool` and `xev` are the clients that read the
map. `mvn test` 242/242.

**Traps, and the first one is the item.** A `GetMap` reply must carry **XKB's four mandatory types**
(`ONE_LEVEL`, `TWO_LEVEL`, `ALPHABETIC`, `KEYPAD`); with fewer, Xlib rejects the reply whole and
`XkbGetMap` returns null, which the calling client cannot tell from the extension being missing. Three
types - which is all this keymap uses - is exactly what the first version had, and its types section
was byte-for-byte identical to Xvfb's first three. The wire layout misleads twice more: the reply's
fixed part is **forty** bytes, not thirty-two, and each keycode's keysyms follow its own descriptor
**inline** rather than living in one pool at the end. Read the layout off a captured reference reply
(`XkbProbe 192.168.22.152 6098 7 dump <file>`) rather than from a header. And the keypad's digit is
what **Num Lock** selects, not Shift: giving the keypad type a Shift entry makes Shift+KC79 type `KP_7`
where the reference leaves it `KP_Home`.

**The writing half, 2026-09-25: a layout CAN be set, and by which tool was measured.** The item said
"`SetMap` is refused, so no layout can be selected - `setxkbmap` is still a no-op". The first half was
true and the second was the wrong tool: measured with `setxkbmap -layout de` against this server, the
request it dies on is **`XkbGetKbdByName` (minor 23)** - `X Error of failed request: BadRequest ...
Minor opcode 23 (XkbGetKbdByName)` - which asks the *server* to compile a keymap from a rules file.
That is a server-side `xkbcomp`, and it is not in this project's scope. The tool that sends `SetMap`
is `xkbcomp` itself, and **it now works**: `xkbcomp -w 0 /tmp/de.xkb <display>` exits 0, and keycode
52 comes back `y Y` from `xmodmap -pke` against a server whose default is `z Z` - German, for a
keymap compiled on the VM and loaded into this server. The XKB rail follows, because both rails read
one table: `XkbProbe` shows `symmap kc52 ... = 0x79 0x59`.

What was added for it:

- **`SetMap` applied, keysyms only.** Each keycode's keysyms go into `Keymap` through the same
  `assign` the core `ChangeKeyboardMapping` uses, and clients are told with the core
  `MappingNotify`. The types, actions, behaviours, explicit components, virtual-modifier map and
  real-modifier map are read and dropped: this server has four types of its own and one modifier
  table, and both are what this extension publishes. A client that sets a map with types this server
  cannot represent gets the characters right and everything else as it was.
- **`SetMap` decoded by hand**, because the generated class cannot read a partial one: every list in
  its builder is non-null and a part the request omits is left null, so a request that sets only the
  key symbols - which the protocol allows - fails with `NullPointerException: types is marked
  non-null but is null`. Measured: a one-keycode test request hit that while xkbcomp's all-parts
  request went through. The hand-written reader takes the header, skips the type descriptors and
  reads the symbols; the rest of the body is left unread, which is safe because the connection
  buffers the whole request. **A byte that reads as -8 is not a loop bound**: the first version read
  `nKeySyms` signed, so a 248-keycode map decoded as no symbols at all and the live `xkbcomp` run
  silently changed nothing.
- **`GetKbdByName` answered "not loaded"** instead of being refused: Xlib prints an X error and exits
  on a refusal, so `setxkbmap` died with a protocol error before it could say anything; the protocol
  has a reply for a server that cannot compile, and the client reports "Error loading new keyboard
  description" and stops. Measured: the same command, before and after.
- **`SetCompatMap`, `SetIndicatorMap`, `SetNames` and `SetGeometry` accepted** rather than refused,
  because a keymap compiler sends them on its way to `SetMap` and there is nothing here for them to
  set. `xkbcomp` needs all four to get as far as the map.

**Measured 2026-09-25**: `extension/XkbTest` grew `aClientCanSetTheKeymapThroughXkbAndBothRailsAgree`
(6 tests in the file now): a client's one-keycode `SetMap` changes what `GetMap` answers and what the
server's table holds, and the test **fails when the request is left unhandled** (the server refuses it
and the next request reads an error). `mvn test` 260/260. Live, as above.

**The controls half, 2026-09-25: `GetControls` and `SetControls`, and the repeat delay and rate are
reachable at last.** This is A11's leftover: the core protocol has a request for the per-key repeat
bitmap (`ChangeKeyboardControl`) and *none* for the delay or the rate - those are XKB's, through
`XkbSetAutoRepeatRate`, which is a `SetControls` with one bit set. So before this, a client that
wanted a 200 ms delay could not ask for one, and every client got the server's defaults.

**Measured first, with `XkbProbe` extended** (it decodes a `GetControls` reply field by field and can
send a `SetControls` of its own), against Xvfb and here:

- **The reply's layout was read off a reference reply rather than derived.** It is 92 bytes - the
  long one of the extension - and its length field says 15, which is the core rule (`32 + 15*4`). The
  repeat delay and interval are two byte fields at offsets **20** and **22**: measured by changing the
  rate through `xset r rate 200 50` and watching the bytes move, which is also what fixed the probe's
  own first version - it had `SetControls`' fields four bytes out, so the request looked well formed
  and changed nothing, which is exactly what a request for a control the server *does not* implement
  looks like. The enabled-controls word is at 56 and the per-key bitmap is the last thirty two bytes.
- **`changeControls`' RepeatKeys bit is what makes the request's delay and rate take**, and
  `affectEnabledControls`' same bit is what turns the keyboard's repeat on or off: measured, the
  reference's `enabledControls` goes `0x13a1` → `0x13a0` → `0x13a1` (RepeatKeys is bit 0) with the
  per-key bitmap untouched each time. `xset r off` does the same thing on the core rail, which is
  where the two halves meet: the bitmap and the control are separate, and turning repeat off must not
  wipe the table.
- **The `perKeyRepeat` array in the request is ignored, because the reference ignores it** - measured
  three ways (all zeros, all ones, and beside a rate change) and its bitmap never moved. So per-key
  repeat stays the core protocol's business, which is where this server has had it since A11.

What was built: `InputState` grew the control bit (`repeatKeysEnabled`), the delay and the rate as
settable fields, and one place that packs and unpacks the bitmap (`autoRepeatBitmap` /
`setAutoRepeatBitmap`), which `GetKeyboardControl` and XKB's reply now both read. **The default bitmap
changed with it, and this is the half worth knowing**: it used to be "every key", with "a modifier does
not repeat" applied at press time; it is now "every key the modifier map does not name", which is
what a stock server's own `auto_repeats` list holds, and the press-time rule is gone - so a client that
puts a modifier back into the bitmap gets it to repeat, as it would on a reference server. The
remaining difference from Xvfb's list is this server's own keymap showing through: it names Scroll
Lock and Mode switch as modifiers where the reference's does not, and the reference excludes KP_Enter
and one media key where this one excludes nothing beyond the modifier map.

**Measured 2026-09-25**, three ways:

- `tools/XkbProbe` against Xvfb and here, field by field: every field of the reply decodes the same
  except the three this server answers deliberately differently - the delay and rate are its own
  defaults, `enabledControls` is `0x1` where Xvfb's is `0x13a1` (a control this server does not
  implement is reported as *not enabled*, and its delay as zero, rather than publishing the
  reference's own numbers for behaviour that is not here), and the bitmap differs only where the two
  keymaps do.
- `extension/XkbControlsTest` (3 tests): the delay and rate read back as 660/40 and change to 250/30
  through `SetControls`, with a zero meaning "leave it alone"; the per-key bitmap is byte for byte the
  same on the core and the XKB rail and has the modifier keycodes clear while 'a' is set; and - the
  end-to-end one - a client turns repeat off through XKB, holds a key, and gets **one** press, then
  turns it back on and gets a run again. **All three fail with the two requests left unhandled** (the
  server refuses them and the next request reads the error).
- Live, with both forwarded applications on a `--single-window` server. **EasyShell**: its window
  mapped at 1300x700 and the interface rendered, and its context menu survived the six out-and-back
  passes **6/6**. **termora**: window and content mapped, the interface rendered, menu 6/6. Before
  those, `xset r rate 200 50` from the VM (the table above) and
  `xkbcomp -w 0 /tmp/de.xkb <display>` still exit 0 with keycode 52 coming back `y Y` on both rails.
  `MenuProbe`'s output against Xvfb and here is still **identical**, which is §7's gate for anything
  near the input path - the change is where the repeat decision is made, and that gate is what says it
  did not move anything else. The request log has no new `rejected` lines: the three there are the
  ones already recorded (RandR's `GetOutputProperty` and JavaFX's two asks about window `0x0`).
  `mvn test` 263/263.
- Live, and **the client that needs it is `xset`, not `xkbcomp`** - which is worth recording because
  the note this item was picked up from said the opposite. `xkbcomp`'s whole request list against
  this server, from a `--log-requests` run, is `UseExtension`, `SetMap`, `SetCompatMap`,
  `SetIndicatorMap`, `SetNames`, `SetGeometry`, plus the core `GetKeyboardMapping` and
  `GetModifierMapping`: **it never asks for `GetMap` or `GetControls`**. `xset` does, and in exactly
  the order that makes `GetControls` the prerequisite rather than a nicety - Xlib's own
  `XkbSetAutoRepeatRate` is a Get-modify-Set:

  ```
  xset r rate 200 50   ->  GetControls (minor 6)
                           SetControls (minor 7)
  before: repeatDelay=660 repeatInterval=40     after: repeatDelay=200 repeatInterval=20
  ```

  So the item's original "no client here asks for it" was right about `xkbcomp` and wrong about
  clients in general - and the delay and the rate could not be changed at all before this.

**A layout by name is in (2026-09-27), off tables captured from a reference, and the reason it is not
"a compiler in the server" is that it never needed to be one.** A display server compiles nothing: the
tables are read off a server that can ({@code tools/GenLayouts}, {@code tools/gen-layouts.sh}) - the
same trick D3 uses for the standard fonts - and served under the name a client asks for. **(The
bullet below is kept as it was written, but its "sixth of a list of counted strings" is the reader's
off-by-two, corrected higher up this entry: the strings are six and the symbols one is the fifth.)**

- **What a client asks with is not the word "de".** {@code setxkbmap -layout de} sends the components
  the rules file resolved to, `pc+de+inet(pc105)`, as the sixth of a list of counted strings. Which
  field that is was **measured, not read**: with `-variant nodeadkeys` the sixth becomes
  `pc+de(nodeadkeys)+inet(pc105)` and nothing else in the request moves. The layout is then the first
  `+`-separated part this server has a table for, with a variant's parentheses dropped - a variant is
  a keymap there are no tables for, and the base layout is the honest answer to it.
- **The tables are the core mapping at the reference's own width** (seven keysyms per keycode), and
  that is what makes it work: the server sends keycodes and the client does the level arithmetic, so a
  client indexes `GetKeyboardMapping`'s reply by the modifier state its own XKB layer computes. This
  server's own keymap stays two levels until a layout is installed, and nothing else changes when none
  is.
- **Measured, and it is byte for byte**: after `setxkbmap -layout ru` is answered, the whole
  `GetKeyboardMapping` reply - 248 keycodes by seven levels - is **identical to the same request
  against Xvfb in the same layout**, and `de` gives the QWERTZ swap (keycode 29 `z`, keycode 52 `y`)
  and the umlauts at the reference's own levels. `XkbTest`'s new case fails with the install taken out.
- **Every client is told to re-read it** (`MappingNotify`), which is the same notification `SetMap`
  sends and the reason a client that missed it would go on typing the old layout.

**Closed 2026-09-27: `setxkbmap -layout <name>` works, and the missing half was the *reply*, not an
upload.** The paragraph below is kept because it is what the item believed for two sessions, and both
of its claims were measured and are wrong; what settled it was reading the client's code and the
reference's bytes.

- **`strace` on a real `setxkbmap -layout de` against a reference shows no `SetMap` at all.** It
  sends the XKB handshake, **one `GetKbdByName`**, and then a root property update - there is no second
  way, and no client-side compiler in the path. So `setxkbmap` sets a layout by asking the *server*
  to, and the reference's answer to that request is what has to be reproduced.
- **The answer is a header plus embedded component replies, read by position.**
  `XkbGetKeyboardByName` (Xlib, `XKBGetByName.c`) gives up when `reported` is zero and then reads, for
  every bit set in it, one whole component reply out of the same buffer - a map reply for
  `Types|Symbols`, a compat reply, a names reply, a geometry reply - and finally insists the buffer was
  consumed exactly. This server installed the layout and answered `reported = 0`, which is
  indistinguishable from "not loaded": that is why the error was still printed. The reply now carries
  the map this server answers `GetMap` with, embedded whole, under a header whose length counts it,
  and `reported` claims exactly the three parts that map carries (0x0d).
- **The request's own shape was wrong here too, and harmlessly so.** It is a **twelve** byte fixed part
  (`deviceSpec`, `want`, `need`, `load`, pad) and **six** counted strings - keymap, keycodes, types,
  compat, symbols, geometry - with the symbols string **fifth**, not the sixth of eight as the reader
  assumed: the phantom string its two-byte offset error counted first happened to sit in front, so the
  right string came out of the wrong field. Measured with `strace`, and the string count is why a
  request built with eight is refused by a reference with `BadLength`.
- **`GetNames` is answered now, and it used to be refused** - the one answer this extension must never
  give, because Xlib's default error handler ends the process. Its atoms live in the reply's *tail*,
  in the order keycodes, geometry, symbols, physical symbols, types, compat (read off a reference's own
  reply); what this server reports is the symbols component the client asked for, which is the one
  component it acts on, and `which = 0` when no layout has been chosen by name.

**Measured 2026-09-27**: `tools/XkbProbe` gained `getkbdname` and `getnames` (both print the reply as
bytes, with each tail word resolved through `GetAtomName`), and `tools/XProxy` gained a raw dump
(`XProxy listenPort host port logFile rawFile`) which is how `setxkbmap`'s request was read off the
wire. Then: **`setxkbmap -layout <name>` exits 0 with no error for `us`, `gb`, `de`, `fr`, `es`, `ru`
and `cn`, and `xmodmap -pke` after each is byte-identical to Xvfb's in the same layout**;
`xkbcomp -w 0 <map> <display>` still exits 0 and `xmodmap -pke` shows the uploaded map; `setxkbmap
-print`/`-query`, `xset q`, `xset r rate 200 50` and `xdotool` all still work, with **no `rejected`
line in the server log** for any of them. `extension/XkbTest` grew two cases (the by-name reply's
structure, read the way Xlib reads it, and the names reply) and both fail with the fix taken out -
a `reported` of zero fails the first with `expected: <13> but was: <0>`, and a refused `GetNames`
fails the second with the error `setxkbmap` used to die on. `mvn test` 378/378.
**Live on `.147`**, both forwarded applications on a `--single-window` server: **EasyShell** renders,
its context menu opens at the same geometry a reference gives it (157x378+75+132) and survives the
out-and-back gesture **3/3 with a controlled harness** (fresh server, fresh client, pointer parked
first - see the trap below), and typing after `setxkbmap -layout de` puts `zy` in its search box,
which is the German keycodes (29 = z, 52 = y) on both rails at once. **termora** renders, its menu
opens and survives **3/3**, and both ask `XIGetProperty` and are answered.

**The trap this measurement cost the most time to, and it is the kit's own rule 5/6 generalised.** The
out-and-back gesture looked broken (the menu closed) on the new build in two runs and fine on the old
build in two - and it was *the runs*, not the code: with the server **and** the client restarted and
the pointer **parked** before it maps anything, the same build survives 3/3, and the code was never
implicated. A menu measurement needs a fresh server, a fresh client and a parked pointer; anything
less measures the previous run.

**The earlier reasoning, which the paragraph above replaces - `setxkbmap` still prints `Error loading
new keyboard description`**, which is the honest remainder and it is **not** what it looks like:

- **A reference server refuses this request too** - Xvfb answers `BadAccess` to the very bytes a real
  `setxkbmap -layout ru` sends it, captured off the wire - and `setxkbmap` reports **success** there
  anyway, because it goes on to compile the keymap itself and upload it with `SetMap`. **So the
  by-name load is not how a layout gets set on a reference at all**; it is the client's own compiler.
- **That fallback is what this server is missing**, and it is where the next session should look: the
  upload is `SetMap` plus `SetCompatMap`/`SetNames`/`SetGeometry`, the four this server accepts and
  drops (see below). With the by-name answer in place the layout *is* installed and every client reads
  it correctly - the error is `setxkbmap`'s own verdict, on its way to a fallback that then fails.
- **This also corrects the entry above**, which said a client "can go on to `SetMap` instead": it does
  not, and never did - measured, **no request at all follows** the reply, and the same error is printed
  for the honest `loaded = 0` answer this server used to give.

**And a live run found one more thing, which is small and has no oracle (2026-09-27): `GetNames` is
refused.** The log names it - `GetNames rejected the request (error 17): not implemented: XKEYBOARD
request GetNames` - and a refusal is exactly what this server's own documents say is worse than an
empty answer, because Xlib's default handler ends the process (that is what killed `setxkbmap` here
before `GetKbdByName` was answered). `GetNames` is not in the list of requests this extension accepts
and drops, so it falls through to the error. **Fixed the same day** - the paragraph above has the
reply's shape and the measurement; the "a reference refuses it too" in this paragraph was the same
malformed-probe mistake: a hand-built `GetNames` gets a reply from Xvfb once its length and pad word
are right.

**What is still not here.** The compatibility map and the indicator names are absent (the four
`Set...` requests above are accepted and dropped, so a client that sets names and reads them back gets
nothing - `GetNames` answers, but with the symbols component a by-name load recorded rather than with
whatever a client set); every keyboard control other than repeat - slow keys, bounce keys, sticky
keys, mouse keys, AccessX, the two internal modifier maps - is accepted by `SetControls` and not
applied, and reported by `GetControls` as not enabled; `LatchLockState` is accepted and does nothing;
and no XKB event is ever sent, because nothing here changes a layout in a way a client would need to
hear about. `XkbMap` publishes four types of its own whatever the client sets, so a map that relies on
types this server has no equivalent for is applied as characters only.

---

### [x] B2. RandR. **M** - done 2026-09-25

**What it was.** Not advertised at all: a client could not learn the monitor's geometry or its device
pixel ratio, and the screen growth C1 landed had no way to tell anybody - the core protocol's only
answer is a `ConfigureNotify` for the root, which no modern client listens for.

**The model: one CRTC, one output and one mode covering the whole screen**, with the three ids drawn
from the server's own resource space so a client can pass them back. `QueryVersion` (1.6, capped at
what the client asks for), `GetScreenSizeRange`, `GetScreenResources`/`Current`, `GetCrtcInfo`,
`GetOutputInfo`, `GetCrtcGammaSize`/`GetCrtcGamma`, `GetCrtcTransform`, `GetPanning`, `GetScreenInfo`,
`GetMonitors`, `GetOutputPrimary`, `ListOutputProperties` and `SelectInput` with `ScreenChangeNotify`.
Anything that would *change* the screen - `SetScreenConfig`, `SetCrtcConfig`, `CreateMode`, the panning
setters, providers and leases - is refused with `BadImplementation`, and so is `GetOutputProperty`
(see below).

**Measured 2026-09-25**, with the real client and with the kit, and **it is the client that found every
bug in this one** - each of these was a visible failure rather than a wrong number:

- **`xrandr` against this server** prints the same three lines as against Xvfb, byte for byte apart
  from the numbers that are deliberately ours:
  `Screen 0: minimum 1300 x 800, current 1300 x 800, maximum 1300 x 800` /
  `screen connected 1300x800+0+0 344mm x 212mm panning 1300x800+0+0` / `1300x800 0.00*+`.
  **Getting there was one request at a time, because a refusal does not degrade - it crashes the
  client**: `xrandr` died with SIGSEGV on a `BadImplementation` for `RRGetPanning`, and again for
  `GetOutputPrimary`, so `GetPanning`, `GetCrtcTransform`, `GetOutputPrimary` and
  `ListOutputProperties` are there because a real client asked for them and could not carry on without
  an answer. (`GetCrtcGammaSize` answering **0** - the honest "there is no ramp" - made `xrandr` print
  "Failed to get size of gamma" and give up on the output's detail, so the ramp is a real 256-entry
  identity.)
- **`xrandr --listmonitors`** printed nothing and died (`free(): invalid pointer`) until the reply's
  own `nOutputs` was set: it was left at zero, and a client reads that count to know how far the
  monitor array reaches. Found by dumping the reply's bytes next to Xvfb's
  (`tools/RandrProbe`, new). **`xrandr --verbose`** prints the whole block - gamma `1.0:1.0:1.0`, the
  identity transform, the mode, the mm size.
- **`tools/RandrProbe`** against Xvfb and here, field by field: the version, the size range, the one
  CRTC/output/mode, the mode's name length, the output's status/crtc/preferred/counts, the CRTC's
  geometry/mode/rotation/output, and the monitor's atom name/geometry/mm size - all the same. **Two
  differences, both deliberate**: this server reports a real physical size where Xvfb reports `0mm`
  (the core setup reply already tells clients a real one, and the two must agree or a client computes a
  nonsense DPI - the item's own trap), and its size range does not reach down to `1x1` because this
  screen never shrinks.
- **`extension/RandRTest`** (7 tests): the version answered in the client's own terms, the output and
  CRTC both answering about the ids the resource list gave out (including the mm size against the
  screen's own accessor), the size range, the identity gamma ramp, the monitor with a *resolvable* atom
  name, **the `ScreenChangeNotify` a resize delivers to a client that selected it** - the half of C1
  that was missing - and the guard that a client which did not select hears nothing. Removing the
  extension fails five of the seven.

**Live, and this is the part that matters for a forwarded application**: **JavaFX asks RandR seven
times** as EasyShell starts - `QueryVersion`, `GetScreenResourcesCurrent`, `GetCrtcInfo`,
`GetOutputInfo`, `GetMonitors`, `GetOutputProperty`, `SelectInput` - so this is not a protocol for
hypothetical clients. With the extension in, EasyShell's window and its context menu are exactly as
they were, and the log has **no ERROR lines**: the one request it makes that is not implemented
(`GetOutputProperty`) is answered with a clean `BadImplementation` that the application carries on
past. `mvn test` 226/226.

**And one more alignment, 2026-09-26**: `GetOutputInfo` reported the output as carrying the mode
(`1 mode(s)`), where a reference reports none - the mode is on the CRTC, which is where a client that
wants one looks (`GetCrtcInfo` answers it), and an output's list is the *optional* list of modes it
could be set to. The reference diff is now down to the deliberate differences listed above.

**Traps.** RandR's replies are read positionally and some of them are long, so a field in the wrong
place is not an error but a plausible wrong number - or a client that dies in its own library, which is
how the missing `nOutputs` and the refused `GetPanning` showed up. A reply can be longer than thirty
two bytes (`GetMonitors`' array follows twelve bytes of padding), and a probe that reads only the fixed
part leaves the rest of the stream out of step. `xrandr` asks for `GetCrtcTransform`, `GetPanning` and
`GetOutputPrimary` *before* printing anything, and `--verbose` adds `ListOutputProperties` - a
half-implemented extension is not "mostly working", it is a crash. And **`GetOutputProperty` was left
unanswered on purpose**: this server's outputs have no properties (an empty `ListOutputProperties` is
the honest answer), the reference could not be used as a model for the refusal because the request this
probe builds is itself refused by it (`BadLength`), and no client here needs it - JavaFX asks once and
carries on.

**Superseded 2026-09-26: the questions are answered now, and the reference could be used as a model
after all.** The "no client here needs it" half of that paragraph was true and the rest was not.
Measured against Xvfb with `xrandr --prop`: its output carries one property (`non-desktop`) and no
EDID. **What it answers for a property that is not there had to be measured, not read off the
source**, and the first version of this got it wrong: Xorg's source reads like `BadName`, and the
reference answers an **empty reply** - format 0, type `None`, no items, nothing after it (measured
2026-09-26 with a hand-built request, `01 00 03 00 00 00 00 00 …` from both servers only after the
fix). So "the property is not there" is not an error at all on a reference server, and a client that
asks (JavaFX does, as it starts) gets an answer it can act on either way. A
client that is told the second is told the feature is missing rather than the monitor having no EDID,
and JavaFX asks exactly that as it starts (the live log above).

What changed: `GetOutputProperty` answers an **empty reply** (format 0, type `None`, no items);
`GetProviders` answers **zero** providers
(`xrandr --listproviders` prints `number : 0`, which is what a reference server with no GPU driver
prints); creating a lease still refuses, for the same reason a screen change does. The provider and
lease *questions* that name an id stay refused - with no provider there is no id to name, so a client
cannot reach them.

**And the probe that measured it had a bug of its own, which is why the first version of this was
wrong twice over.** `RandrProbe`'s `GetOutputProperty` was built as **six** units where the request
is seven - the last word carries `delete`, `pending` and padding - so *both* servers refused it
(`Xvfb` with `BadLength`, this one with `BadRequest`) and the probe printed a line that looked like
a field report. A hand-built request through `tools/font-reply-proxy.py`'s sibling settled it: with
the word present, the reference's empty reply and this server's answer are identical. **When a probe
and the reference disagree, check the probe's request against the protocol first** - the kit has now
paid for that lesson twice (A2's `ShapeRectangles`, and this).

**Left alone, and it is a deviation worth naming**: a request that names an output or CRTC this
server does not have raises a core error (`BadName`, `BadValue`) where a reference raises the
extension's **own** error (`BadOutput`, `BadCrtc` - measured: 147 from Xvfb's RandR base for an
output of `None`). Doing it properly needs a way for an extension to raise its own error code, which
this server has no mechanism for: every refusal is a core `ProtocolException`. Nothing measured
depends on it - a client passes back the ids it was given - so it is recorded rather than built.

**And every RandR reply carried its *sequence number* as its timestamp** (found 2026-09-26 while the
replies were being compared byte for byte). A client uses `configTimestamp` and `timestamp` for what
they are for - comparing two readings to see whether the configuration changed - and a number that
changes with every request makes every reading look like a change. The reference prints
`Timestamp: 931772` (milliseconds since it started) and this server printed a small sequence number;
the replies now use the server's own event clock, which is what its events carry and is therefore
comparable with them.

**And the log line that found all this had a bug of its own.** The refusal above was reported as

```
client[2 …]: InternAtom rejected the request (error 17): not implemented: RANDR GetOutputProperty
```

- the request named is the last *core* request the client sent, because `RequestDispatcher` only set
the name it prints in `route()`, which extension requests never reach. Every refused extension request
was therefore reported under somebody else's name, and the only way to tell which was to read the
message. `dispatchExtension` (and `dispatch`, for a request that does not survive decoding) now set it
too, and `client/RequestLogTest` pins both halves - the refused request named, and the request before
it *not* named - by reading the log through a logback appender. It fails without the change, on the
line above, verbatim.

**And the same generator arithmetic is in the requests this extension refuses on purpose**, which is
left alone deliberately: `ConfigureOutputProperty`, `ChangeOutputProperty`, `CreateMode`,
`SetCrtcConfig`, `SetCrtcGamma`, `SetCrtcTransform`, the two provider-property requests and
`CreateLease` all carry a trailing list and are decoded by the generated reader, so one whose list is
short enough computes a negative capacity and is answered `BadRequest` where this server means
`BadImplementation`. Found by sweeping every extension request that has a trailing list - building a
minimal valid instance from each protocol XML and reading the server's own log for `could not
decode`; the ones this server *answers* were `ShapeRectangles`, `XFixesCreateRegion` and
`XFixesSetRegion`, all decoded by hand now. Nothing in the paragraph above is answered, so the only
difference is which error a client gets for a request that was going to fail either way - and a
client with no error handler exits on either. The XFIXES requests in the same position are
`CreateAnimCursor` (whose list is "the rest of the request", so it also needs the true byte count)
and `CreatePointerBarrier`.
---

### [x] B3. Xinerama. **S-M** - done 2026-09-25 (the part a client needs, measured)

**Done.** `extension/XineramaExtension` answers the three requests a client asks before it has decided
anything - `QueryVersion` (1.1), `IsActive` (**no**: this server is one screen per client and cannot
span more than one) and `QueryScreens` (that one screen, at the origin, sized to the root).

**Measured 2026-09-25 with `tools/XineramaProbe`** (new) against Xvfb and here, field by field:

```
                     Xvfb                        here
QueryVersion         1.1                         1.1          same
IsActive             yes - several heads         no - one screen   deliberately different
QueryScreens         1 head [0,0 1300x800]       1 head [0,0 1300x800]   same shape and size
```

**And live, with a real client**: `xdpyinfo -ext XINERAMA` against this server prints
`XINERAMA version 1.1 opcode: 133, base event: 67, base error: 21` and `Xinerama is inactive`, with a
zero exit status; against Xvfb it prints the head instead because Xvfb says it *is* active. The opcode
and the two bases are the part a client caches, and they are there.

**The three Xinerama 1.1 requests are deliberately left unanswered**, and this is the measurement that
settles it rather than a preference: `GetState`, `GetScreenCount` and `GetScreenSize` are for a client
that already knows it is on a Xinerama display, and **the reference's own answers are not a model** -
255 for the state, 255 for the count (neither is a value that means anything), and `BadLength` for the
size request even with the window argument the other two take. Both of those first two look like
replies the reference fills in without implementing the request, and building on them would be
guessing for a client that does not exist here. `extension/XineramaTest` (4 tests) pins the three
answers that *are* implemented, the head's size, and the three that are refused - the last one so that
a future session that implements them has to come back to this paragraph.

**Why it exists at all**: `xdotool` calls `XineramaIsActive` as it starts, and a client that asks a
question has to get an answer. Measured: with the extension present the xdotool segfault below is
**still** there, so Xinerama was not its cause - which is what A4's investigation needed ruled out.

### [x] B3old. Xinerama (the original wording). **M**/**S-M** - superseded: B3 has the record (the three a client needs, measured)

**Current.** Not advertised: every client sees one monitor, which is also what this server has.

**Why it matters.** AWT/JavaFX (and browsers) enumerate monitors through Xinerama or RandR when they
want to talk about more than one screen; with neither, a client has one screen and that is fine today.
It becomes relevant the moment the server hosts more than one host window (per-client mode already
does) or a multi-monitor host: the honest answer then is one Xinerama head per host window.

**Plan.** Advertise the extension, answer `QueryVersion` and `QueryScreens` with one head per screen
today. (It once said "per host window once C8 lands": C8 is declined, and there is one host window
per application, so one head per screen is what a head can be.)

**Traps.** Head coordinates are screen coordinates, so a client may create a window at the origin of a
head that is not the root's - which in per-client mode means a window outside the viewport. Decide
whether that is "clipped" (today's behaviour) before advertising.

**Verify.** `xdpyinfo -ext XINERAMA` and a JavaFX/AWT application printing its screen list.

---

### [x] B4. XInput2. **L** - done 2026-09-26, and it had to be all of it

**Current.** Not advertised; `xeyes` prints a warning about the missing `XInputExtension` and falls
back to core input, and that fallback works for every client tested.

**Why it matters.** Modern toolkits prefer XI2 for precise scrolling, touchpad gestures, tablet and
multi-touch input, and "raw" mouse motion (pointer-acceleration free). A client that gets none of it
loses gestures but still works, which is why this is L rather than the next thing to do.

**Plan.** XI2 is a large protocol (devices, classes, event masks, grabs, hierarchy, properties). A
minimal, honest version advertises one pointer and one keyboard device, implements `XIQueryVersion`,
`XIQueryDevice`, `XISelectEvents`, `XIGetClientPointer`, and delivers the XI2 versions of the core
events a client selects. Anything more (touch, tablets, barriers) is a separate item.

**Traps.** Core and XI2 events must not both be delivered to a client that asked for one of them -
toolkits that select XI2 *and* core input double-handle. The device ids and the `XIDeviceInfo` reply
layout are again positional; capture from Xvfb.

**Verify.** `xinput list` on the VM; `xev -event device` prints XI2 events; the applications still
receive input exactly once.

**Done 2026-09-26, and the reconnaissance below is what made it a whole job rather than a partial
one.** `extension/XInputExtension` advertises `XInputExtension`, answers its requests and delivers
its events; `event/Xi2Event` and the delivery hook in `EventDispatcher` are the other two pieces.
What is implemented:

```
XIQueryVersion      XIQueryDevice (two masters, with classes)   XIGetClientPointer
XISetClientPointer  XISelectEvents XIGetSelectedEvents          XIQueryPointer
XIWarpPointer       XIChangeCursor                              XIGetFocus
XIGrabDevice        XIUngrabDevice  XIAllowEvents               XISetFocus
GetExtensionVersion ListInputDevices OpenDevice CloseDevice     GetDeviceKeyMapping
GetDeviceButtonMapping
```

and on the event rail, the XI2 twin of every core event the server already delivers - keys, buttons,
motion, the crossings and the focus - with the emulated core event **suppressed** for a client that
selected the device one, which is what a reference was measured doing.

**A delivery bug found by `xinput test-xi2`, and it is the case every test here was blind to.** The
owner's mask and the list of "other clients" are the **core** selections, so a client that called
`XISelectEvents` and never asked for a core mask appeared in neither list and was reached by nothing:
the server logged the button press delivered to the window and sent the client no event at all.
Measured live (`xinput test-xi2 --root 2` against this server: a selection recorded, the press
delivered, nothing sent) and fixed in `EventDispatcher.deliverToWindow` - the window's XI2 selections
are walked too, once, skipping anyone the two core lists already reached.
`XInputTest.aClientThatSelectsOnlyTheDeviceEventStillGetsIt` is the regression test; every earlier test
selected **both** rails, which is what GDK does and why none of them caught it.

**And that live check produced a measurement worth keeping**: `xinput test-xi2` sends *two*
`XISelectEvents` for the same device - the ordinary masks and then the raw ones - and the second
**replaces** the first. Measured against Xvfb: the same probe printed only `RawButtonPress`/
`RawButtonRelease`, no ordinary `ButtonPress`. So "last wins" is the reference's rule too (this server
matches it), and what that client gets here is silence because the **raw event family is not
implemented** - the documented gap below, not a fault.

**Two requests turned out to be mandatory that the plan did not name, and both were found by a real
client crashing.**

- **`ListInputDevices`, XInput 1's device list.** AWT's `XToolkit.checkXInput` asks it as the JVM
  starts and `getNumButtons` dereferences what comes back, so a server that advertises
  `XInputExtension` and *refuses* it does not make Swing fall back - it **segfaults** in
  `libawt_xawt.so`, measured with termora (`SIGSEGV ... getNumButtons.part.0`). The same shape as A4.
  Answered now from the same two devices, with `OpenDevice`, `CloseDevice` and the two mappings beside
  it, because AWT reads its input model from those.
- **`XIChangeCursor` has no reply.** Answering it was measured to shift the client's whole stream: GDK
  sends it when a text field takes the cursor, and afterwards EasyShell asked `XIQueryPointer`
  **a million times in two minutes**, reading a pointer position that was really a reply header, until
  the server had no time for anything else. `XIGetFocus` is the mirror image - it *has* a reply, and
  accepting it in silence hangs the client (which is what `xinput list` did, on XInput 1's handshake).
  Every request here is therefore answered, genuinely done, or refused, and never accepted and dropped.

**Measured 2026-09-26, three ways.**

- **`tools/XiProbe` against this server and against Xvfb**: the version handshake, the client pointer
  and the whole device list decode the same, field for field - the pointer as device 2 with a button
  class and two axes, the keyboard as device 3 with 248 keycodes - and the reply is **1240 bytes**,
  which is exactly what Xvfb sends for its two masters. `XiProbe ... select 0x30` drives a real click
  and gets the XI2 `ButtonPress` and `ButtonRelease` back with the fields the reference's carry.
- **`extension/XInputTest` (6 tests)**, and the deliveries above are pinned rather than described: the
  version reply's field offsets (four bytes out and `xinput` segfaults), the two masters, the
  device-event-replaces-the-core-event rule, **the event's own size**, and the no-reply request.
  `theDeviceEventIsTheSizeAClientSizesItFrom` fails with the layout the protocol XML implies
  (`expected: <120> but was: <108>`).
- **Live, both clients, `--single-window`, XTEST-driven**: **EasyShell** (JavaFX/GTK, which uses XI2
  for everything - `XISelectEvents`, `XIGrabDevice`, `XIChangeCursor`, `XIQueryPointer`):
  its window renders, a right-click on a host-list entry opens its context menu, **the menu survives
  the out-and-back gesture 6 runs out of 6** (the §7 gate), typing into its search box produces the
  character, and the whole session makes **nine** `XIQueryPointer` calls. **termora** (Swing/AWT,
  which uses XInput 1 for its device list and XI2 for its events): renders, and its menu survives
  3 of 3. `mvn test` 345/345.

**The trap that cost the most, and it is the one to remember: the event's size is not what the
protocol XML adds up to.** The XML's field list for a device event comes to sixty eight bytes; a
reference sends **eighty of fixed part** and the two masks after that - 120 bytes for a key, 136 for a
motion with two axes, and the arithmetic only closes at eighty. An event written to sixty eight is
fifty two bytes short, and GDK does not reject it: it read the key code and the pointer position out
of the middle of it and EasyShell **stopped producing characters from the keyboard**, with nothing in
any log to say so. It was found by A/B - the same gesture on the previous build types the character -
and only after the mask positions were measured (`1<<4` is `ButtonPress`; the mask words sit at 112
for a two-axis motion, which is what fixes the fixed part at eighty).

**Also worth carrying forward**: an XI2 event carries **no 32 bit `full_sequence`** (the header is
twelve bytes and the body starts at the timestamp - assume one and the timestamp reads as 1); its
length field is the size in four byte units **minus eight**; `FP1616` is four bytes where a valuator's
`FP3232` is eight, and writing a position as the wider one overruns four fields; and
`XIGetClientPointer` is answered `BadWindow` for the *root* on a reference, so a probe that asks about
the root measures nothing.

**And the live runs name one of those refusals, which is worth having in writing (2026-09-27).** A
forwarded client asked **XInput2 minor 59, `XIGetProperty`** - a *device property* read, the family this
entry refuses - and the server answered `BadRequest`, because an unknown minor reaches the hand-written
decoder and throws rather than being decoded and refused. Two things follow:

- **It is the *documented* refusal, arriving the hard way** (a decode failure and a protocol error,
  rather than a request decoded and answered "no"), and the entry's own warning applies: a refused XI2
  request **crashed a client here once already** - `libawt_xawt.so` on `ListInputDevices`. A client that
  reads a property and gets an error is the same shape.
- **The clean answer is not free**: `XIGetProperty` expects a *reply*, so "accept and do nothing" would
  leave the client waiting for one - worse than the error it gets now. Answering it means the reply's
  layout, measured off a reference rather than guessed, which is a session's worth of the same work
  `GetNames` below needs: a request whose reply this server has no class for.

**Fixed the same day (2026-09-27), because the answer was measurable in one session after all.** Both
forwarded applications ask it as they start - the property is `libinput Tapping Enabled` on device 2 -
and it is now **answered "absent"** rather than refused: the reply is a reference's, whose only
non-zero fields are the header's (`type = None`, `bytes_after = 0`, `num_items = 0`, `format = 0`),
because this server has no device properties at all. Two measured details, both of which a guess would
have got wrong in a way that reads as a plausible number: the request's fixed part is **six** four byte
units (a reference refuses a seven unit one with `BadLength`, so the `delete` byte a longer reading puts
at the end is not part of it), and the reply's byte 1 carries the request's *minor opcode* - measured by
asking about two different devices and watching it stay 59. `tools/XiProbe` gained a `getprop` mode
(`XiProbe <host> <port> getprop <name> [device] [units]`) which prints the reply as bytes, and
`extension/XInputTest` a case that fails without the fix (the refusal arrives as an `IOException` from
the client side). `README.md`'s XI2 bullet and `docs/design-notes.md`'s "an error is worse than an
empty answer" both carry it now.

**And the same measurement finished the read half of the family the next hour: `XIListProperties`
(minor 56) was refused too, and `xinput list-props 2` is what says so** - it died with `BadRequest`
here and printed the device and its two properties against a reference. It is now answered with an
empty list, whose layout was read off a reference's reply for a device that *has* properties
(`XiProbe listprops 2`): a bare header, the count as a CARD16 at 8, the atoms after the header, and
byte 1 carrying the request's minor opcode - the empty answer is that reply with the count at zero.
`xinput list-props 2` against this server now prints `Device 'Virtual core pointer' does not report
any properties.` and exits 0.

**One thing that measurement corrected in the property read, made an hour earlier**:
{@code XIAllDevices} and {@code XIAllMasterDevices} are selectors, not devices, and a property
belongs to a device - a reference answers an **error** for both requests with device 0, which
`XiProbe listprops 0` shows as a message of zeros. The first version of `getProperty` accepted them
(`checkDevice`, which is right for the queries) and answered "absent"; it now refuses them, like the
list does.

**And the two `bad window 0x0` refusals in every log are the client's, not this server's - measured
2026-09-27.** They were recorded here as "JavaFX's two asks about window `0x0`", which reads like
something to chase; a GTK client through a proxy against a *reference* makes the same two requests
(`GetWindowAttributes(0)` and `GetGeometry(0)`, decoded off the raw stream), so Xvfb answers
`BadWindow`/`BadDrawable` to them as well and GDK ignores both. Every log line of this shape can be
read past.

**A client sweep, for the record (2026-09-27).** Thirty real tools against a fresh server -
`xdpyinfo`, `xwininfo`, `xwd`, `xev`, `xset` (including `r rate`), `xmodmap -pke`/`-pm`, `xrandr`
(and `--prop`/`--listmonitors`), `xinput list`/`list-props`/`set-prop`, `xdotool` (geometry, search,
key, click), `glxinfo`, `xkbcomp`, `setxkbmap -print`/`-layout de`, `xprop -root`, `xlsatoms`,
`xlsclients`, `xclock`, `xeyes`, `xcalc`, `xterm`, `xmessage` - plus a GTK3 target and `nautilus`:
**no X error from any of them**, and the only refusals in the log are the window-`0x0` pair above and
three `BadAtom`s for atoms that do not exist. `xinput set-prop` fails client-side, because the
property it wants is one this server does not have - which is the honest answer, and the reason
`XIListProperties` had to be answered rather than refused.

**What is still not here**, deliberately and visibly refused rather than accepted: touch, tablets,
barriers and the raw event family (a client asks for them by name and this server has no device behind
them), the rest of the device property family (`XIListProperties` and the change/delete requests), and
the hierarchy's changing requests. A client that asks for one gets a protocol error, which is the
honest answer.

**The reconnaissance that came first, kept because it is why the item was done whole.** The tool this
item needed (`tools/XiProbe`, new) was written and run against Xvfb and against this server before
anything was implemented, and what it found is that **the smaller version this item's plan described
would have broken the client it was meant to help**. That is what made the grab family part of the
work rather than a later addition.

**1. Advertising the extension changes what GDK does, not just what it could do.** EasyShell is
JavaFX, and JavaFX's Linux backend is GTK: `libglassgtk3.so` calls `gdk_device_grab`/`gdk_device_ungrab`
(measured with `nm -D` on the AppImage's own copy), and **`libgdk-3.so` resolves
`XIGrabDevice`/`XIUngrabDevice`** where they exist (measured with `strings`, alongside
`XISelectEvents`, `XIQueryVersion`, `XIQueryDevice` and `XIQueryPointer`). GDK3's device grab *is*
`XIGrabDevice` once the extension is there, and every menu, drag and modal dialog a forwarded GTK
application opens goes through one. A server that advertises `XInputExtension` and then answers
`XIGrabDevice` with `BadRequest` therefore breaks EasyShell's context menus - the exact thing §7 spends
four bullets on - and the same applies to every XI2 grab, passive grab, `XIAllowEvents` and
`XIQueryPointer` a toolkit sends. "Advertise and answer the queries first" is not a smaller version of
this item; it is a worse one.

**2. The wire facts are now measured, so the next session does not have to derive them.** All of these
are in `tools/XiProbe`'s output and comments:

- **The mask bits are not the core ones.** `XI_KeyPress` is `1<<2`, `XI_ButtonPress` `1<<4`,
  `XI_ButtonRelease` `1<<5`, `XI_Motion` `1<<6` - one higher than the core event code, with
  `XI_DeviceChanged` at `1<<1`. Measured one bit at a time: `1<<3` selects nothing and lets the core
  events through, `1<<4` delivers an XI2 `ButtonPress` **and suppresses the core one**, `1<<6`
  delivers a `Motion`. (The fork's generated `XIEventMask` has these right - `BUTTON_PRESS(0b10000)` -
  which is the one place its numbering was *not* off by one.)
- **An XI2 event is a `GenericEvent` (type 35) whose first twelve bytes are the header** - type,
  extension opcode, sequence, length, evtype, deviceid - with the XI2 body starting at the
  **timestamp**. There is no 32 bit `full_sequence` on the wire: assuming one (the XGE spec's other
  events have it) puts every field four bytes late, and the timestamp then reads as 1 and the detail
  as the root window's id. The length field is `total/4 - 8`.
- **The fixed part of a device event is 68 bytes**, then `button_mask`, `valuator_mask` and
  `axisvalues`, each sized by the two length fields. A reference answers an XTEST button press with
  120 bytes: 68 + 8 words of button mask + 2 words of valuator mask + one `FP3232` axis.
- **`XIGetClientPointer` answers `BadWindow` for the root window** on a reference server (measured,
  minor 45, bad value `0x1ff`), so it is asked about a window the client made.
- **A reference does not deliver an XI2 `ButtonRelease` to a client that did not select the XI2
  `ButtonPress`** - measured: mask `1<<5` alone is quiet and the core pair flows; `(1<<4)|(1<<5)`
  delivers both XI2 events and suppresses both core ones. So the "emulated core event" rule is per
  pointer-down/up *pair*, not per event.
- **`XIQueryDevice` is a header plus a variable number of `XIDeviceInfo` records**, each carrying its
  own name and a list of `DeviceClass` records whose length is in the class. A reference answers six
  devices for a stock Xvfb (two masters, four slaves); the honest answer here is two masters, and the
  class bodies start at the sixth byte of each class (reading them at the eighth makes a key class
  report its first keycode as its key count).

**3. `tools/XiProbe` is the harness.** `XiProbe <host> <port>` prints the version, the client pointer
and the whole device list, parsed **and** as raw bytes; `XiProbe <host> <port> device <id>` narrows it
to one device; `XiProbe <host> <port> select <mask> [deviceid] [root]` selects XI2 events, warps the
pointer in and drives a button through XTEST, printing every message with both rails visible (it
selects the core masks too, so "no XI2 event" and "the gesture never happened" can be told apart - a
quiet run means different things and only having both in one output says which). Against this server
it prints `XInputExtension is not offered by this server`, which is today's honest state.

**What the next session should do**, in order: land the extension with the queries *and* the event
delivery *and* the device-grab family in one go, verify each request against Xvfb with `XiProbe`, and
only then advertise. The pieces are: a minimal `XProtocolPlugin` (the fork's `x11-protocol-xinput` is
not published to the local Maven repository, so nothing generated is available - the request decoding
has to be hand-written, as `XkbExtension` does for `SetMap`, and the replies written as bytes, as
`XkbExtension.getKbdByName` does); a two-master device model; the selection store; the XI2 event built
from the core event at the delivery funnel; and `XIGrabDevice`/`XIUngrabDevice`/`XIAllowEvents` and the
passive pair, which with one master pointer are the core grabs with an XI2 face.

**Size, honestly.** This is a week of work with the live checks, not the half-day the §0 table's `L`
implies, and it is the one item where a half-finished state is *worse* than not starting: the failure
mode is not "a client loses gestures", it is "a forwarded GTK application's menus stop working".

**And the fixed part is not one shape: enter, leave and focus are different events** (found
2026-09-29, chasing a report that EasyShell's search box "sometimes does not get the focus although
typing works"). The eighty byte fixed part measured above is the **device** event's - a key, a button,
a motion. A reference sends:

```
  key/button/motion   120 bytes   fixed 80, buttons_len 8 and valuators_len 2
  enter/leave          76 bytes   fixed 72, buttons_len 1
  focus in/out        104 bytes   fixed 72, buttons_len 8
```

with fields from offset 48 on in different places: `same_screen` at 48 and `focus` at 49 where a
device event has its two length words, `buttons_len` at 50 where a device event has `valuators_len`,
the modifier state as four CARD32 from 52 where a device event has its source id, four group bytes at
68, and the button mask at 72 where a device event's is at 80 (a crossing also carries `sourceid` and
its `mode`/`detail` as two bytes at 16-19, where a device event's detail is a word and a focus event's
detail is a word again).

This server sent all three in the device shape, which is the nastiest kind of wrong: the length field
is honest so the stream stays in step, the event names the right window and device, and every field a
reader takes from the second half lands two or eight bytes from where it was written - so **GDK read
the server's `buttons_len` as `same_screen` and its mask eight bytes late**, and the window never
looked focused: no focus ring, no caret, while the keys went on arriving (which is what hid it: the
one thing that visibly worked was typing). Measured A/B by forcing the shape back: **1 of 3 starts
showed the focus ring without the fix, 4 of 4 with it**, and the tap capture of our own stream has the
same three sizes as Xvfb's. `Xi2Event.Shape` holds the layouts, `XInputTest.aCrossingAndAFocusEvent-
HaveTheShapesAReferenceSends` pins them field by field, and one small difference is left and recorded:
a **focus event's `detail`** is the core event's (0 for a change out of the root window, `Ancestor`)
where a reference's XI2 event said 3 (`Nonlinear`) - measured in one case only, so it is written down
rather than guessed at.

---

### [x] B5. DAMAGE, **every window keeps its own pixels**, and **Composite, Present and SYNC** - done 2026-09-27. **L**

**The last three are in (2026-09-27), and the store is what made them cheap.** DAMAGE and the
per-window store were the first two halves; Composite, Present and SYNC finish the item, each
measured against a reference before it was written (`tools/CompositeProbe`, `SyncProbe`,
`PresentProbe`) and each with a test that fails without it.

- **Composite.** `RedirectWindow`/`RedirectSubwindows` mark a window (or a subtree, walked through the
  ancestors rather than copied into a flag, so a reparent or an unredirect cannot leave a stale
  answer); `compositeOwnPixels` - the single place every screen composite goes through - skips a
  redirected window, so its drawing stays in its store and stops reaching the screen. That one rule is
  the whole of the redirection semantics, and it is a rule this server gets *wrong by default*: with
  every window composited from a store, an un-guarded version is correct in every test that never
  redirects anything, which was all of them. `NameWindowPixmap` hands over a **live view** of the
  store (`XPixmap.view`, resolving it per access because a resize replaces the array), and the
  reference says drawing *into* that pixmap lands, so the view is read-write.
  **The overlay window is real and translucent**, which is what this server's model needs: every
  window here paints a background and every mapped window above subtracts from the visible region
  below it, so a screen-sized opaque overlay would hide every window and leave them all with no
  `Expose` for ever. It takes the depth 32 visual and a transparent background, and that needed one
  rule to go with it - **a translucent window does not take anything away from
  `XWindow.visibleRegion`** - which is right on its own, because a window that shows what is behind it
  is not in the way.
- **SYNC.** Counters (create/set/change/query over the full signed 64 bits) and fences
  (create/trigger/reset/destroy/query, walked through their four states). `AwaitFence` and `Await`
  return without waiting, and that is not a stub: this server finishes every request before it reads
  the next, so a fence is already in its final state when the wait is read.
- **Present.** `PresentPixmap` copies the pixmap into the window's own pixels and composites from
  there - so a redirected window's frame lands in the pixmap a compositor reads, with no second
  drawing path - and `CompleteNotify` reports it to whoever subscribed.

**Four rules came out of measuring, and three of them are not what the obvious implementation does:**

1. **The version rule is per-extension and they disagree.** Composite and SYNC answer **the server's**
   own version (0.4 to a 0.4 client *and* to a 0.2 one; SYNC 3.1 to a 3.1 and a 3.0 client), while
   Present **echoes the client's** (1.0 to 1.0). Carrying either rule across would have been wrong.
2. **`CreateFence` carries the drawable before the fence**, which is the opposite of the order the
   generated class's getters list them - and the reference answers `BadDrawable` on the fence's own id
   when a probe believes the getters.
3. **`PresentPixmap`'s `valid` and `update` are region ids, not flags** (None is the whole of each);
   a first probe passed `update=1` and was answered `BadPresent` with the value 0x1.
4. **A generic event's length field was never written by this server.** The completion arrived as
   thirty two bytes where the reference sends forty, *with the same kind, mode and serial inside* - so
   every field read correctly and the client then hung on the next request, because the eight bytes of
   `ust`/`msc` it had not been told about stayed in the stream. Fixed in `ClientOutput`, beside
   `correctEventType` and for the same reason.

**And one measurement was wrong first, which is worth keeping.** Read naively - the window's pixels
*before* the completion notification - the reference looks as though it never copies the pixmap into
the window, and the entry above was written saying so with the difference recorded as deliberate. Its
present is asynchronous, so that order samples a race; read the other way the two agree exactly. The
probe now reads the event first, and `PresentProbe` and `CompositeProbe` are both **identical to the
reference line for line** in the sweep.

**Verify, run 2026-09-27: `picom` against this server, and it found three more bugs.** The item's
own Verify names a compositor, and running one was worth a session - every one of the three is
invisible to the probes that were written for the extension, and two of them are invisible to the
whole test suite:

1. **The overlay was depth 32 where a reference's is depth 24**, and that made `picom` **segfault**.
   It asks RENDER for a picture with the root's format and creates it on the overlay; a format that
   does not match the drawable's depth is `BadMatch`, `picom` keeps the null picture and dereferences
   it. The crash is a long way from the cause, and the `GetImage`-and-pixel-compare level of testing
   never sees it. Found by printing the overlay's depth in `tools/CompositeProbe` and comparing with
   the reference. **Fixed**, and `CompositeTest.theOverlayIsAsDeepAsTheRoot` fails without it.
2. **The overlay was itself redirected**, which a compositor does - to the root's whole subtree,
   including the window it draws its result on. On a reference that costs nothing (the display driver
   shows the overlay by a path of its own); here being composited is the overlay's only path to the
   screen, so the result went nowhere and the screen was **blank with `picom` running happily**.
   **Fixed** with an explicit "the server's own surface is not redirected", and
   `CompositeTest.theOverlayKeepsReachingTheScreenWhileItsSubtreeIsRedirected` fails without it.
3. **The overlay painted its background over the screen.** This server approximates a window's
   `background = None` by painting the desktop colour - harmless for an ordinary window, which draws
   over it, and wrong for a screen-sized one: mapping the overlay turned the whole screen grey, and
   `CompositeProbe` read it back as a difference from the reference while a window was redirected.
   **Fixed** by giving the server's own surface no pixels until something draws into it
   (`XWindow.transparentUntilDrawn`, and the composite skips a store that does not exist), and
   `CompositeTest.creatingTheOverlayLeavesTheScreenAlone` fails when the fix is taken out.

**And the two it left open were one bug, found the same day.** They were recorded here as "frames
are intermittently black" and "`--opacity-rule` has no effect"; they are the same fault on the path
both go through, and it is now fixed:

- **A RENDER write into a depth 8 drawable stored the whole ARGB value where the drawable's store
  holds a byte.** `Picture.writePixel` wrote `0x80000000` into a 1x1 A8 mask; the read side
  (`Picture.stored`, and `ImageCodec` on the way in) decodes such a store *through the format*, whose
  mask looks at the low byte - so the mask read back as **zero coverage everywhere**, every masked
  composite wrote **nothing**, and the screen was black with `picom` running happily. The same fault
  made `--opacity-rule` a no-op. **This was found by adding the pixel counts to the composite's debug
  line** (`-> wrote 0 pixels, 23711 blocked`, where the blocked ones were a clip and the rest were
  the empty mask) - the request was *accepted* either way, so nothing else would have said so.
  `AlphaMaskTest.writingAColourIntoAnA8PictureStoresItsCoverage` fails without the fix, and with it
  the masked composite writes its pixels and `picom`'s opacity works visibly.
- **Two log lines came out of it and are kept**, because both were what found this and neither costs
  anything: `SetPicture repeat=…` (a 1x1 source is tiled or empty depending on it, and `picom` sets
  the repeat on its alpha pictures exactly one call before using them) and the per-composite pixel
  count.

**Still different, and it is the documented root colour rather than a bug.** With `picom` running,
the desktop it paints is the root window's content, so ours is `#E8E8E8` where a reference's is
black - one of the two by-design rendering differences `tools/README.md` lists. Comparing the two
alpha captures pixel by pixel is therefore not meaningful without accounting for it; the comparison
that *is* meaningful is that both show the window's content at half opacity over the window behind it.

**What is deliberately different, and it is one line.** `SyncProbe` is the sweep's seventh deliberate
difference: `ListSystemCounters` answers **no** system counters here where the reference keeps eight
elapsed-time counters for its own scheduler. This server has no scheduler to report on, and the
reference's entries are variable length with a name stride a first probe mis-read (one counter's name
came out of the next one's fields) - answering a count with entries a client cannot parse would be
worse than answering none.

`mvn test` **369/369**; the sweep is **15 of 21 identical** with the seven documented differences.


**The general store is in (2026-09-27).** Every window but the root now draws into a store of its
own (`XWindow.ownPixels`), in its own coordinates and clipped to what it owns, and is composited onto
the screen from it. (This entry used to add "the half C8 was waiting for"; C8 is declined, and the
store stands on its own - translucency, a reveal with no `Expose`, a window that answers with its own
pixels.) What that changed, beyond the
sentence: a covered window goes on drawing and keeps what it drew; a revealed window is composited
back with **no `Expose`**, so `backing_store` no longer decides anything; `GetImage` on a covered
window answers with its own pixels rather than with the window on top of it; a move carries the pixels
along (`visibilityChanged` distinguishes a move from a map, restack or resize, which still paint a
background and expose); and `bitGravity` moves the pixels inside the store rather than on the screen
(`applyBitGravityToStore`), with `ownPixels` keeping what was drawn across a resize so there is
something to move - A12's eleven measured gravities are unchanged.

**RENDER had to come with it, and that closes the gap this entry recorded on 2026-09-26.** A picture
over a window used to point at the screen's framebuffer, so a toolkit drawing its ARGB window with
RENDER (cairo's path) drew opaquely onto the screen and left the store empty. `CreatePicture` now
binds a window picture to the window's own store in its own coordinates, `Picture` remembers the
region each request writes (`takeWindowDrawn`), and `Compositor.finish` composites it when the request
ends. Two consequences had to be decided rather than inherited:

- **a picture's write clip and its read clip are different regions.** It may write anywhere the window
  owns - a covered window can be drawn into - while a read is still stopped by whatever covers it, so
  a source picture cannot sample a sibling's pixels;
- **`IncludeInferiors` is answered from the children** (`XWindow.pixelFromInferiors`, topmost first),
  because a child's pixels are in a store of their own and not in the parent's. The three
  `IncludeInferiorsTest` assertions and both `PictureOriginTest` cases were rewritten for this: the
  second is now *stronger* - a picture over a window can no longer address the screen at all, so the
  stale-origin bug it was written for cannot come back for a window.

**The store made the old backing-store buffer dead, and it is gone.** `XWindow`'s `captureBacking`/
`restoreBacking`/`forgetBacking`/`buffer` copied a window's visible pixels out of the screen before
something covered it; with every window's pixels its own there is nothing to copy back, and the only
candidate left was the root - which *is* the screen - so the buffer, the third reveal path and the
`forgetBacking` call every drawing request made are removed. `backing_store` is still recorded and
reported (`GetWindowAttributes`), it just decides nothing.

**Measured, `mvn test` 361/361** (`WindowStoreTest` is new: a covered window answers with its own
pixels, and a moved window keeps them; `PictureWindowDrawnTest` pins the region the composite depends
on). `ownPixels`'s per-pixel `Rectangle` union was replaced with bounds arithmetic, because that loop
is every pixel a RENDER request writes.

**One path the suite never sent until 2026-09-27, and now does.** A17's own step (3) names the requests
a caret blink makes, and the last of them is a whole-window `CopyArea` out of a pixmap - a request
`RawXClient` could not build, so no test had ever sent one and the copy-into-a-store-window path was
unpinned. `RawXClient.copyArea` and `WindowStoreTest.aWindowTakesACopyFromAPixmapIntoItsOwnPixels`
close that: the copy has to land in the window's own pixels *and* reach the screen from there. Both
halves matter and neither is obvious - a store window's own origin is 0 while a screen-drawing
window's is its absolute position, so the coordinate rule is what is really being pinned. Confirmed
by mutation rather than by the test passing: giving the store target the absolute origin fails it
(and `aMovedWindowKeepsItsPixels` with it), which is what makes it a test rather than a restatement.

**Verified live on `.147`, both clients** (`--single-window`, XTEST-driven, and EasyShell again in the
default per-client mode):
- **EasyShell** renders (frame dump read: toolbar, sidebar, welcome page, icons and Chinese text all
  correct), a right-click on a host entry opens its context menu, the menu survives the six
  out-and-back passes **6/6** and renders every item, a click outside closes it, and typing `zz` into
  its search box puts both characters in the box and filters the host list. In per-client mode the
  macOS host window shows the same picture.
- **termora** renders (tab bar, sidebar with 我的主机, the ASCII logo) and its context menu survives
  **6/6**.

**A behaviour that looked like a regression, and was not.** A click outside EasyShell's context menu
made the client `GrabDevice`, `ChangeCursor`, `GrabServer` and then ask `XIQueryPointer` ~3700 times a
second, sending nothing else, until it was killed. **The build before the store does exactly the same
thing** with the same gesture (a worktree at `17f4be9`: 21879 queries in five seconds, the same
`button 1 down` -> `GrabServer` -> flood), so it was not this change - and it turned out not to be the
client's either: against Xvfb its CPU does not move. It is a bug of this server's own, in
`XIQueryPointer`'s `child` field, and it is **A16, fixed the same day** - see that entry for the tap
that found it and the two-query walk a reference answers.

**What was left of B5 was the other three**: Composite, Present and SYNC - **all three done
2026-09-27**, see the top of this entry. The store made them reachable in a way they were not: a
window's pixels are somewhere to hand a compositor, and redirection turned out to be the *stopping* of
a composite this server already did rather than a second drawing path.


**The per-window store, and how much of it is in (2026-09-26).** The plan called for "a per-window
backing store, a damage region per window, Composite and Present and SYNC over it, and a final
composite to the framebuffer in the display tick" - and one slice of that is now real: **a window whose
visual has an alpha channel keeps its own pixels** (`XWindow.ownPixels`, a `Framebuffer` with alpha),
draws into them, and is composited onto the screen from them. So the visual the server advertises
means something: a translucent fill is translucent rather than opaque, a covered alpha window keeps its
pixels and gets them back with no `Expose`, and the tray's icon - a disc on a transparent background -
keeps its shape instead of arriving as a light square (C9's leftover). `TranslucentWindowTest` (4
tests) and `TrayBridgeTest`'s `anIconWithAnAlphaVisualKeepsItsTransparentCorners` pin it, and
`mvn test` is 350/350.

Two protocol rules had to change with it, and each had made an ARGB window impossible to *create* or to
draw translucently into:

- **a child need not share its parent's depth** - `CreateWindow` refused it as a `BadMatch`, and the
  ARGB window every compositing toolkit creates is a child of the root at depth 32 under a depth 24
  root (what has to exist is a visual of that depth, which is checked separately); and
- **a graphics context converts its colours in the visual of the drawable it was created on**, not the
  screen's root visual - otherwise the half-transparent foreground a core request expresses alpha with
  arrives opaque.

**One gap in that slice was measured by looking rather than by a test**, and it is now closed: a
**RENDER picture over such a window** used to point at the screen's framebuffer
(`RenderExtension.createPicture`), with the window's absolute origin - so a client that drew its ARGB
window's content with RENDER (a toolkit's own path) drew opaquely onto the screen and left the store
empty. The 2026-09-27 change bound window pictures to the window's own store and composites what they
write when the request ends; see the top of this entry.

**What was left of the store was the general case** - every window keeping its pixels, not only the
alpha ones - and that is now in too (2026-09-27). It is
what changes "what drawing means for every window", as predicted here: it took the read/write clip
split, `IncludeInferiors` from the children, gravity inside the store, and the RENDER composite.

**Current of the rest (2026-09-27: no longer current - all three are in, see the top).** DAMAGE and
the store were there; **Composite, Present and SYNC** were not. The
model is no longer "every request draws straight into one framebuffer and a revealed window is asked
to redraw with an `Expose`" - a window's pixels are kept and composited - but nothing *asks* the server
to composite a window tree for you: a translucent window is composited against the screen, not against
other windows' stores, and there is no `Composite` request family, no `Present` and no `SYNC` fence to
synchronise on.

**DAMAGE, and the plan above was wrong about it being inseparable from Composite.** It is not the
compositor's prerequisite: it is the answer to "tell me when a window's pixels changed" for clients
that *read* them - a screencast, a magnifier, a remote-desktop encoder - and it needs one hook into
the drawing path and nothing else. That hook turned out to be already there: every drawing request
ends in `DrawHandlers.afterDrawing`, which has computed the region the request wrote since E3 did the
damage region (it is what goes to the backend). `DamageExtension.drew` is called from it, and from
`WindowHandlers.paintBackground` and the backing-store restore - and **the server's own painting has
to count**, which is the half a first version missed: a reference's accumulated damage covers the
whole window once it has been mapped, because mapping paints the background.

**Measured with `tools/DamageProbe`** (new), which creates a damage object, draws, reads the
notifications and subtracts, at each level, against Xvfb:

```
level 0 RawRectangles   one notify per drawing request, carrying that request's own area, cleared
                        as it is reported     -> identical to the reference, line for line
level 2 BoundingBox     nothing until a Subtract; the change after one is reported once, as the
                        bounding box of what accumulated -> identical, apart from the ids
level 3 NonEmpty        the same shape of answer, with one difference recorded rather than copied:
                        on the reference the accumulated damage is not cleared by a subtract, so its
                        report carries everything since the object was created; here it carries what
                        changed since the subtract. Both report once per subtract, and every pixel in
                        this server's area really did change.
```

`DeltaRectangles` (1) is answered like the two bounding levels - measured first: a reference server
behaves identically at that level too ({@code DamageProbe 1}), so the "difference between two damage
regions" the specification describes comes to the same box. The events are thirty two bytes like any other -
a rectangle in them is four shorts, which is what the probe's own first version got wrong.

**Found on the way, and it is a real bug of its own**: this server sent a `NoExpose` for **every**
drawing request when the GC asked for graphics exposures (which is the default), where a reference
sends one only for `CopyArea`/`CopyPlane`. `DamageProbe`'s event reader printed "other event type 14"
after every fill here and nothing at all there. A stray `NoExpose` is a trap for a client that is
waiting for one from an earlier copy - and it is the shape of bug the kit exists to find, in a path
nothing else looked at because everything else about it was right.

**Why it matters.** A compositing window manager has nothing to build on; a translucent popup or a
window with a shadow cannot exist; a client that wants damage notifications (instead of `Expose`)
polls. It is also the prerequisite for a *cheap* C1 (resize the screen without asking everyone to
repaint) and for honest backing store (A9).

**Plan (the rest of it).** This is the architecture item for the remaining three: a per-window
backing store, a damage region per window, `Composite` and `Present` and `SYNC` over it, and a final
composite to the framebuffer in the display tick. Do it *after* C1/C2 if the goal is a usable forwarder - the payoff today is small for the two
applications tested, and the risk to the obscuring/expose machinery (which took three sessions to get
right) is large.

**Traps.** The `Expose` path stays: clients without `Damage` must keep being told to repaint. The
framebuffer is the only surface today, and `Picture`'s clip is computed from `visibleRegion()` per
request - a backing store changes what "visible" means for *reads* (a covered window has pixels again),
which is exactly the `IncludeInferiors` question in A9.

**Verify.** `xcompmgr`/`picom` running against this server; `xeyes` with a translucent background;
`xdpyinfo -ext DAMAGE -ext Composite`.

---

### [x] A17. Reports of a menu with no hover colour and a caret that does not blink - not reproduced, and `--glx` is the one measured difference. **M** - closed 2026-09-27 by measurement: the client asks for no repaint, so the server has nothing to fail at

**What the user saw**, on 2026-09-27, running the build with GLX advertised: a right-click menu whose
hovered item has **no background colour** (clicking works), and a search box that takes focus and
accepts typing but whose **caret never blinks**.

**What measuring it found, and the honest shape of the answer is "state-dependent".** Two `xwd`
captures of the whole screen 0.3 s apart, with the box clicked and the focus left to settle:

```
this server, default (no GLX):   frames 3, 4 and 5 differ from frame 1 by 40 pixels   - the caret
this server, --glx:              every later frame differs from frame 1 by 42 pixels   - see below
```

so **the caret does blink here**, and the same check earlier in the session measured *zero* difference
- because the box was not in the state that blinks. Both symptoms are the kind a toolkit **gates on
the window being active**: a JavaFX caret blinks, and a control's hover is animated, only while the
window is focused. Nothing in this session's changes is implicated by any of it, and the two builds
behave alike in every state that could be controlled from a script.

**What is measured, and it was why `--glx` was opt-in - until the default was put back the same day
(2026-09-27), A17 being closed by measurement: the caret blinks either way and the hover asks for no
repaint at all, so what is left is one abandoned context in a toolkit that takes the GL path and
leaves it. GLX is on by default now and `--no-glx` is the switch; the paragraph below is the
measurement that made it opt-in for a day, kept because it is what "a toolkit changes its own
drawing" means.** Advertising GLX changes what a toolkit does
with its own drawing: with it on, EasyShell (JavaFX, GTK/Glass) probes GLX and the frames it then
sends are different - which is the mechanism behind the user's report, whether or not the exact
gesture can be reproduced. A client that wants OpenGL needs GLX; one that does not may be hurt by it,
so the server advertised it only when `--glx` asked, from 2026-09-27 until later the same day.

**The mechanism is now measured, and it is in B6's entry**: with GLX advertised, JavaFX's Prism gets
*further* into its ES2 attempt - `GLX extension is not supported` and it stops, against initialising
the GL factory, creating a context and querying the GL before rejecting it - and then falls back to its
software pipeline either way. A toolkit halfway down its GL path is a toolkit whose animation state is
not what it would have been, which is the shape of both symptoms.

**What to do if it comes back, in order.** (1) Confirm the server was started **without** `--glx` and
re-try the same gesture. (2) Compare the focus: `GetInputFocus`/the focus chain against Xvfb's while
EasyShell runs - a JavaFX window that does not believe it is active explains *both* symptoms at once,
and a scripted click is exactly the kind of thing that lands in a different focus state than a hand.
(3) If only the caret is affected, the blink's own requests are `CreatePixmap`, `CreatePicture`,
`FillRectangles` (RENDER minor 26), a burst of `PutImage` and a whole-window
`CopyArea(pixmap -> window)` - so a pixmap that does not take the fill, or a frame that lands in the
window and not on the screen, are the two places to look, and `Shot --window <id>` reads a window's
own pixels back for the comparison.

**Re-measured and closed 2026-09-27, and the measurement that closes it is the client's own request
log.** The entry above already said neither symptom reproduces from a script; this is the same answer
with numbers, plus the one measurement it was missing - what the client *asks for* during the gesture.

- **The caret blinks, with GLX and without.** Ten whole-screen `xwd` captures 0.2 s apart, the search
  box clicked and the pointer parked away from it, hashed over the box's own rectangle: the region
  alternates between exactly two states - frames 1,3,6,7,8,10 against 2,4,5,9 on the default build,
  and 1,3,6,7,10 against 2,4,5,9 with `--glx`. The period the sampling can see is 0.8-1.0 s, which is
  JavaFX's 500 ms on/off. **"The caret never blinks" is not this server's doing on either build.**
- **The menu hover repaints nothing because the client draws nothing.** With the context menu open
  and the pointer walked across its items in 1 px steps, the server's request log shows the menu
  drawn exactly once (`CreatePixmap`/`CreatePicture`/`FillRectangles`/`PutImage`/`CopyArea`) and then
  **no drawing request at all** while the pointer moves - only the `QueryExtension`/`QueryPointer`/
  `WarpPointer` groups that are `xdotool`'s own, which is what makes the two separable. The screen
  agrees: twelve pointer positions and a whole-screen `xwd` diff leave the menu's rectangle
  **byte-identical**. A toolkit that does not repaint on hover has not failed to reach the screen; it
  has not drawn.
- **The reference agrees, as far as it could be made to.** On Xvfb, the same gesture and the same
  captures also differ by **0 pixels**. (The Xvfb side is the weaker half on purpose: EasyShell's
  saved state cannot be reset from a script, and two attempts to open the menu on the reference
  landed in an already-open session's file list instead. The request log is the measurement; Xvfb is
  the corroboration.)
- **What a hover needs *is* delivered.** `FocusProbe` and `MenuProbe` are both identical to Xvfb in
  this session's sweep - the gate §7 sets for this area - so the `EnterNotify`/`MotionNotify` and the
  popup's grab are there. The live 6/6 below is the other half of that statement.
- **Live, and this is the regression the item is answered with** (`.147`, XTEST-driven): EasyShell in
  `--single-window` renders; its context menu survives six out-and-back passes **6/6**, with the same
  2400 colours and 1373 dark pixels in its own rectangle after every one; typing `zz` puts both
  characters in the search box; the caret alternates as above. termora renders and its menu survives
  **6/6**. In the default per-client mode EasyShell's host window is on screen carrying the client's
  own title (`EasyShell主页`, 1300x828).

**Nothing in this server is changed by this item** - the report was a toolkit's own animation state,
not a frame this server dropped - and **the mitigation it was answered with has since been
withdrawn**: GLX was advertised only when `--glx` asked for it, from the day this closed until later
the same day, when the default went back to on (`--no-glx` is the switch) on the grounds this item
established: nothing measured says a client that does not want GLX is hurt by it, and a client that
does cannot render here without it. The recipe above is kept for a report that arrives with a
*gesture*, which is the one thing none of these measurements has - a script cannot make the hand's
kind of motion, and that remains the honest hole in the answer.

**What it does leave behind is a pin on the path its step (3) names.** The caret blink's last request
is a `CopyArea` out of a pixmap into the window, and nothing in the kit had ever sent one - so the
copy-into-a-store-window path was untested. `RawXClient.copyArea` and
`WindowStoreTest.aWindowTakesACopyFromAPixmapIntoItsOwnPixels` close that (see B5's entry for what the
test really pins, and the mutation that shows it bites). It is a small thing, but it is the part of
this item that survives: a caret that stops blinking because a copy did not land would now fail in
`mvn test` instead of needing another afternoon of `xwd` captures.

### [x] A23. An application's context menu opens and then ignores the mouse. **M** - fixed 2026-09-30

**The report.** "The input box's context menu does nothing - 复制, 粘贴, 全选 are all dead", in the default
mode. On `main` they work.

**The cause: `--focus-on-click` moved the input focus onto the menu itself, so the application tore its
own popup down.** A menu is an **override-redirect** window, this server's default asks a click to give
the toplevel it lands in the focus - and the toplevel a click *in a popup* lands in is the popup. The
window the menu belonged to was therefore told `FocusOut` on the press, and a toolkit hides a popup
whose owner it believes unfocused: the app unmapped the menu three milliseconds later, so the release
that followed was reported against the window underneath and the item never activated. A window manager
never gives the input focus to an override-redirect window - that half of the rule was missing - and it
is also why the bug is not on `main`, which has no `--focus-on-click` at all.

**The A/B, in both modes, is the measurement** - one build, the flag the only difference, the same
gesture (right-click the search box, press the second item of the menu that opens; in `--single-window`
headless the XTEST driver is `tools/kit.sh Drive`, and in the default mode the host window is driven
with the platform's own input, `tools/hostinput`):

| mode | `--focus-on-click` on (the default) | off | with the fix |
|---|---|---|---|
| `--single-window`, headless, XTEST | the popup is **destroyed** after the press, 5 of 5 | survives, 5 of 5 | survives, **5 of 5** |
| per-client (the default), host window | release reported against the owning window | - | press **and** release both reported against the popup |

The signal to read is the window the button is *reported against*: `InputState: button 1 down at
(234,160) -> 0x40001e` is the popup, and the pre-fix `button 1 up at (234,160) -> 0x400002` is the
window underneath - the menu had already gone. With the focus left alone both land in the popup and the
item acts (`CreateWindow`/`MapWindow` for its dialog 40 ms later, and the dialog is in the host-window
capture).

**`main` is normal, and it is the same rule seen from the other side**: `main` has no
`--focus-on-click` at all, so nothing moves a focus on a click and the menu works there. The same
gesture against the `main` worktree (`--single-window --backend headless`) gives
`button 1 down at (234,160) -> 0x40001c` **and** `button 1 up at (234,160) -> 0x40001c` - both in the
popup - and the item's dialog is created 40 ms later. The fix keeps the window-manager half this server
added on purpose and takes away the half that was wrong.

**The fix** is three lines in `InputState.focusOnClick`: a toplevel that is override-redirect is not
focused. `InputFocusTest.aClickInsideAnOverrideRedirectWindowLeavesTheFocusOnItsOwner` pins it, and it
was checked by mutation - put `false` in place of the test and it fails
(`expected window(0x400001) but was window(0x400003)`).

**The previous round recorded `--focus-on-click` as ruled out, and that measurement is now
contradicted.** Whatever it ran, the flag is the whole difference here when **both the server and the
client are fresh for the run** - every row of the table above restarted both, which is the control the
earlier attempt did not have (the app's own state drifts across scripted gestures, and a stale server is
this project's oldest false A/B). The lesson is the same one A22 recorded, applied to a gesture rather
than to a request: **a focus question is answered by a fresh server and a fresh client per run**.

**What stays ruled out**: the grab status (`XIGrabDevice` is answered `status 0` every time), and the
XI2 button mask - making it the buttons held *during* the event rather than the core state's "before"
did not fix the click and was reverted, because `buttons(state)` is what the verified title-bar drag
(b6424b4) stands on. And the previous round's reading of the gesture was right about this much: the
press **is** delivered to the menu's own window, so the hunt for a wrong window was a dead end.

**One correction for whoever reads the old notes**: the search box's own context menu in EasyShell is
the host list's (添加连接/添加目录/导出数据/…), not a text menu. The 复制/粘贴/全选 of the report belong to
a text field elsewhere - a dialog's, or an SSH tab's - but the defect and the gesture are the same.

**Traps this cost.** Aiming a click at a menu row off a screenshot needs the capture's own geometry:
`screencapture -l <window>` pads the window by **56 points on every side** (a 1280x728 window comes out
1392x840), so `screen = window origin - 56 + 0.696 * displayed`. Three "the item is dead" measurements
before that was noticed were misses, and the hover highlight is the cheap way to prove the pointer is on
the row before believing a click. And `pkill -9 -f "[E]asyShell"` matches the launch line when both are
in the same remote command, so it kills the shell that was about to start the app: send the two in
separate `tools/vm.sh` calls.

### [x] A22. A forwarded application's input fields ignore their context menu and every Control shortcut. **M** - fixed 2026-09-30

**The report.** "In EasyShell's home page the input box's native right-click menu does nothing - the
items are clickable but 剪切/复制/粘贴 have no effect - and the same in 新增目录's dialog and in the box inside
an open SSH tab. Ctrl+A, Ctrl+C and Ctrl+V do nothing either. Through Xvfb+VNC, and on a Mac Finder
connection, it is fine."

**Two independent causes, found one after the other, and the first had to be fixed before the second
could be seen at all.**

1. **The toolkit's connection was being killed by us.** A GTK3 probe on the same display (the toolkit
   the application's own Linux backend is built on) reported, on its first click into a text field:
   `The program received an X Window System error. The error was 'BadMatch' ... request_code 93 (core
   protocol)`. 93 is the **core** `CreateCursor`, and GTK sends one with a one pixel *placeholder*
   pixmap and its hotspot at (1,1) - outside it - when it installs the real, RENDER-built cursor on the
   window. This server answered `BadMatch`; GTK's default error handler ends the process. Everything
   after that (the selection ownership a copy needs, the XFixes cursor change) went nowhere, which is
   what "the menu items do nothing" was. **Measured against a reference**: a 1x1 source with hotspot
   (1,1) is accepted - Xvfb creates the cursor, and this server does now (a C probe run against both).
   The hotspot is an offset into the image, so one outside it is a cursor whose image hangs off its own
   origin, which is drawable. Both handlers dropped the check (`ResourceHandlers.createCursor` and the
   RENDER `createCursor`).

2. **The XI2 key event did not carry the modifier state where a toolkit reads it.** With the connection
   alive, Ctrl+A still did nothing - and the application typed a plain `a`. A core event's `state` was
   right (`xev`: `state 0x4` on the `a` of a Ctrl+A), so the probe went to what *GDK* believes:

   ```
   against Xvfb      KEY press keyval=a state=0x4 ctrl=True
   against ours      KEY press keyval=a state=0x0 ctrl=False
   ```

   GDK takes it from the **device** event, and a byte-for-byte diff of the two XI2 `a` presses found
   the field: an XI2 device event carries the modifier bits **twice** - the four bytes at 60-63 the
   protocol documents, and the word at 72 - and a reference fills both (`04` at 63 and `04` at 72)
   while this server wrote the first and left the second nought. **The A/B is the measurement**: setting
   that word made GDK report `state=0x4 ctrl=True` for the gesture that reported `state=0x0` without it.

**Verified end to end** on EasyShell's search box: Ctrl+A selects (typing then replaces the whole
field), Ctrl+C copies, Ctrl+V pastes it back, and the field's 复制 menu item puts the text on the Mac's
clipboard. The only refusals left in the server's log are the application's own two `window 0x0` probes,
which a reference refuses too.

**Traps this cost, both worth keeping.** *A stale server process is a false A/B*: two rounds of "the
rule is in the source and the behaviour is unchanged" were a server started before the change still
holding the port; check the process's start time against the build. And *a test's own request can be
wrong in a way that makes it pass*: `CursorRequestTest` first wrote the cursor's hotspot at offsets 24/26
where they are 28/30 (after the two six-word colours), so it never sent the case it was about - and its
error reader looked for the opcode at offset 11, which is the minor half, where an error carries the
major opcode at 10. Both were found by making the test prove it can fail: a second request that *must* be
refused is sent with the same reader, and the rule was put back to watch the first one fail.

### [x] A21. With the SSH tab open the pointer sprite flickered on this build and not on `main`. **M** - fixed 2026-09-29

**The report.** "Open a connection (aliyun) and the SSH tab: the loading animation and, once the tab is
open, the cursor keeps flickering, and the page is very laggy - `main` is normal. Other tabs and pages
are fine."

**Reproduced, and it is the mouse pointer, not the terminal's caret.** With the tab open and the app
repainting its window, two captures of the **host window** 0.25-0.35s apart differ in the sprite's own
box - the arrow, ~31x44 at the capture's scale, **753 pixels** - where `main` shows nothing:

| build | consecutive capture pairs whose sprite box changed |
|---|---|
| this build, GLX on (the default) | **6 of 12** |
| this build, `--no-glx` | 3 of 12 |
| `main` | **0 of 12** |

so it is a regression between `main` and now, and GLX **modulates** it rather than causing it. Seen by
eye in the two crops: the same arrow drawn once crisp and once washed out (its outline's antialiasing
differs), which is what "the cursor flickers" describes.

**The instrument matters, and it is two things.** The sprite is drawn **over** the frame and never into
the framebuffer - this project's own rule - so every X-level measurement says the screen is still: a
`GetImage`, the frame dumps and the fast `tools/BlinkProbe` (new: times the changes in a screen
rectangle at thousands of samples a second) all report **zero changes** while the sprite is flickering.
Only a capture of the **host window** sees it: `screencapture -l <window number>`, with the number from
`tools/host-windows.c` (extended to print it - that is the only way to capture a window this machine's
own shells keep covering).

**What is *not* the cause, measured**: the app's own frames are pixel-identical on both builds (it
repaints ~300 frames/s of the same content, which costs this server about a full core - a waste worth
an item of its own, and the same on `main`); the host window is not being resized (`fitWindowToViewport`
leaves it at 1300x828 across ten samples, so a canvas clear from a resize is out); the app sends no
`SetInputFocus` at all in the steady state, so `--focus-on-click` is not implicated.

**The cause, found by logging one frame's damage against the sprite's box.** This server damages
*part* of the screen - a B5-era optimisation - and the app's damage **alternates** between rectangles
that reach the sprite and rectangles that do not (`Rectangle[0,33 1300x700]` against
`Rectangle[0,0 1300x300]`). The sprite is drawn over the frame and never into the framebuffer, so the
canvas keeps last frame's copy wherever this frame's damage does not reach it - and `drawCursor` then
composites that one **again**, darkening the arrow's antialiased edges. Measured on the sprite's own
pixels: 17 dark pixels on a frame whose damage covered it against 102 on one whose damage did not, at
the same centroid, i.e. the same sprite drawn twice. `main` damages the **whole screen** every frame
(`markScreenDirty` there is `markAllDirty`), so the defect could not exist on it - which is exactly why
the report says "main is normal", and it is why a prompt that repaints its whole window never shows it.

**The fix** is in `JavaFxBackend.flushDamage`: the sprite's own box is unioned into the damage before
it is drawn, so the sprite in the canvas is exactly one frame old, always, whatever the client
underneath is repainting. Measured across eight captures with the SSH tab open:
**`17 17 17 17 17 17 17 17`** after, against **`17 96 96 17 17 17 17 17`** before. The Swing backend
does not need it: its panel is an opaque `JPanel`, so the dirty rectangle is erased before it is
painted.

### [x] A24. "Much slower than Xvfb+x11vnc, especially EasyShell - every operation is very slow." **M** - fixed 2026-09-30

**The report.** "This xserver is much slower than starting `Xvfb` + `x11vnc` on the remote `.147` and
connecting from the Mac - especially EasyShell: open a connection (e.g. aliyun) and every operation is
very slow." It is the *frame path*, and it is the cost A21 recorded as "the app repaints ~300 frames/s
of nearly identical content and this server spends about a full core on it".

**Reproduced and measured on the real bed, not guessed.** The server runs **on `.147`** with
`--backend vnc` (the same shape as the `Xvfb`+`x11vnc` baseline, not a Mac-side server), EasyShell runs
on `.147` against it, and the aliyun SSH tab is opened and allowed to settle. `top -H` names the
EasyShell connection thread, and repeated `jstack` of it showed the whole cost in four per-pixel loops:

- `XServer.compositeOwnPixels` -> `Framebuffer.copyFrom` - the copy of a window's own pixels onto the
  screen that ends **every** drawing request - per pixel with a per-pixel `markDirty`;
- `DrawHandlers.copyArea` -> `Rasterizer.copyRect` - the terminal's scroll - per pixel with a clip test,
  and its source is an **XPixmap** (a toolkit's off-screen buffer), not another window store;
- `RenderExtension.fillRectangles` -> `Compositor.draw`/`blend` - a toolkit's panel and text-background
  paint, and its alpha `Clear`s - per pixel; this was the single hottest frame;
- `ImageCodec.putZ` - a `PutImage`, which is how a JavaFX/GTK frame is uploaded - per pixel through
  `Rasterizer.writePixel`.

The request-body read and the image decode were byte-at-a-time too (a 4.16 MB image: **~16-17 ms per
byte loop against ~0.5 ms in bulk**), which is real but was not the whole core.

**The A/B, same machine and same application state: `git archive HEAD` = ~90-100% of one core; the fix
= ~20%.**

**The fix.** `Framebuffer.copyFrom` copies a row at a time (`System.arraycopy`) when the values need no
conversion; `Rasterizer.copyRect` does the same for a plain copy inside the clip and now accepts an
`XPixmap` source (and marks the store's damage, which `DamageTrackingTest` caught missing);
`Rasterizer.writeRun` + `ImageCodec.putZ` decode an image row into an `int[]` and write it in bulk;
`Picture.fillArgb` + `Compositor.fillRectangles` fill the writable rectangles (`Clear`, and an opaque
colour under `Src`/`Over`) a row at a time. The request body is read in bulk, `readPutImage` copies the
image out of the request's own bytes, and three per-request `log.info` lines are `log.debug` now.

**Verification.** New `draw/FramebufferCopyTest` holds the `copyFrom` fast path to the pixel-by-pixel
answer over two surfaces, a same-surface overlap in both axes, and an off-edge request;
`DamageTrackingTest`, `RasterizerTest`, `ImageCodecTest`, `BlendFastPathTest`, `AlphaMaskTest`,
`ArgbAlphaTest`, `VariableLengthDecodersTest` and `ZrleEncoderTest` pass; a capture of the aliyun tab on
the server renders correctly (terminal, SFTP list, toolbar). **Traps:** the server belongs on `.147`
(a Mac-side server puts every X request on the LAN and is not this comparison), and on the Mac it needs
`-Djava.awt.headless=true` or `GraphicsEnvironment` aborts the JVM.

**The wire half of "slower than x11vnc" was still open here** - that the same screen was re-sent 61
times a second for as long as a viewer was watching it. It is **A25**, right below.

### [x] A25. The wire: an unchanged screen was still re-sent 61 times a second. **M** - fixed 2026-09-30

**The report.** After A24, "less smooth than Xvfb+x11vnc" was still true on the same bed. The user's own
comparison is the sharp one: run `EasyShell` against the VM's `Xvfb :98` with `x11vnc` in front of it and
watch it from the Mac's own VNC client, and that is visibly smoother than the same application on this
server's VNC backend.

**Measured, on the bed, with a client that counts what it is sent** (`tools/vnc-meter.py`: the handshake,
ZRLE, then **one incremental request outstanding** - a real client's own pace - counting the bytes that
come back). Aliyun tab open, nothing touched, `.147`:

| | our server | `x11vnc` on `Xvfb :98` |
|---|---|---|
| idle, nothing changing | **5.39 MB/s, 61 updates/s** | **nothing at all** (the request is held) |
| pointer moved across the window | 5.42 MB/s | nothing |

**The cause.** `RfbSession.updateRequest` took the damage rectangle and `sendUpdate` built and sent a
ZRLE frame for it - every time, whatever the pixels were. EasyShell's aliyun tab repaints its whole panel
~300 times a second without changing a pixel (A21 measured the same thing), so the tick had damage every
frame and the session answered a viewer with a full-window ZRLE 61 times a second, forever. `x11vnc`
compares 64x64 tiles against what the client has and sends nothing when they are equal, which is why it
is smooth: its viewer does no decoding work at all for a screen that is not changing, and every update it
does send is a change.

**The fix.** The session remembers **what this client has been sent** (`sentPixels`, one entry per screen
pixel, dropped on a resize, null until the first frame) and `sendUpdate` compares it against the
framebuffer before building anything:

- the update is the **bounding box of the pixels that really differ**, not the damage rectangle. A redraw
  that changes nothing is not sent, and it is not a lie by omission: `updateRequest` loops back into its
  wait, so the request stays outstanding until something really changes - the same answer `x11vnc` gives.
  A blink that costs one cell of pixels now costs that cell (~28 bytes measured), not a 1300x733 ZRLE.
- rows are compared with `Arrays.equals`, and only a row that differs has its extent found pixel by
  pixel, so the common case is a vectorised compare of the damage and nothing else.
- a **full** request is always answered with the pixels it asked about, whatever the client holds, so a
  viewer that reconnects or resizes still gets a screen.
- the region buffer and the ZRLE tile buffer are reused per session: a full-screen update allocated 4 MB
  plus one `int[4096]` per tile (273 of them) before this. They are now paid once.

**Measured after, same bed and state:** idle **0.00 MB/s, 0 updates, the request held for the whole
run**; pointer motion **0.00 MB/s** (the sprite's box is damage but its pixels do not change, so a viewer
that draws its own pointer is not sent a frame for moving it); typing, scrolling and a `yes | head -3000`
flood **0.01-0.05 MB/s**, at the updates the changes are in - and `x11vnc` costs the same order on the
same flood, which is the point.

**Verification.** `tools/vnc-meter.py` decodes ZRLE and dumps the client's own picture; diffed with the
kit's own `Xwd` against `xwd -root` of the server's framebuffer after the flood, **136 of 1,040,000 pixels
differ and all of them are inside 583,678..590,694** - the text cursor's cell, where the blink raced the
capture. Everything else is the same picture to the pixel. New
`VncBackendTest.aRedrawThatChangesNothingIsNotSentButAChangeAfterItIs` holds the behaviour: an identical
redraw is not sent, the change after it is, and it arrives as the box that differs. The whole suite is
green.

**Traps.** A skipped update must not become a **lost** one: the request is not answered and the wait
restarts, the damage is cleared only after the comparison has read the pixels, and any draw that lands
during that read marks damage again - which is why the flood test ends in a pixel-identical screen rather
than a stale one. The comparison is **ARGB**, deliberately stricter than the client's packed pixel:
skipping a real change is a wrong screen, sending an update too many is only a cost. The server still
belongs on `.147` (a Mac-side server puts every X request on the LAN and is not this comparison).

### [x] A26. `IllegalArgumentException: Destination region ... must fit within texture content bounds` on the JavaFX backend. **M** - fixed 2026-09-30

**The report.** "Sometimes this is thrown, and the window never appears":

```
java.lang.IllegalArgumentException: Destination region (x=1268, y=489, w=48, h=25) must fit within
texture content bounds (contentWidth=1300, contentHeight=800)
        at com.sun.prism.impl.BaseTexture.checkUpdateParams(BaseTexture.java:375)
        at com.sun.prism.impl.BaseResourceFactory.getCachedTexture(BaseResourceFactory.java:248)
        at com.sun.javafx.sg.prism.NGCanvas.handleRenderOp(NGCanvas.java:1355)
        ...
        at io.github.oyzh1994.display.cn.oyzh.fx.xserver.JavaFxBackend.openWindow(JavaFxBackend.java:689)
```

Three things in it say what it is before any experiment does. The destination region **runs past the
canvas** (1268 + 48 = 1316 > 1300) while the canvas is the size of the screen; the region is **48 wide
and 25 high**, which is not one cursor sprite but *two* side by side - the union of the box the pointer
is in now (24 wide) with the one it came from; and the throw is inside the paint the window is
**opened** with, which is why "the window never appears" rather than a frame being lost.

**Reproduced on the real backend, not reasoned about.** On the Mac, `--backend javafx --single-window
--geometry 700x500`, a client that draws in a loop (a throwaway `RawXClient` main: map a window, fill a
rectangle every 200 ms - a session that is **not idle**, which matters, because the sprite is only
composited for a session with a client on it), and the host pointer walked to the last column of the
window with `tools/host-input.c` (`hostinput move x y`). The window's position on the display comes from
the X pointer itself - move to two known host points and `PointProbe` reports the X coordinates, which
gives the mapping without trusting `CGWindowListCopyWindowInfo`'s title-bar-inclusive bounds. **A/B:
the build at HEAD threw 21 times in one run, of exactly the reported shape** (`Destination region
(x=494, y=141, w=212, h=16) ... contentWidth=700, contentHeight=500`); the fixed build, same script,
same coordinates, **0**.

**The cause.** `JavaFxBackend.flushDamage` clips the damage to the screen and the viewport and then
unions the **pointer sprite's own box** into it - that union is A21's fix, a sprite whose box the damage
misses leaves last frame's arrow in the canvas. The union was never clipped again, and neither was the
sprite: `sprite.x() + sprite.width()` reaches past the screen whenever the pointer sits on the last
column or row, which an X pointer is allowed to do. JavaFX clips *drawing* but not a `drawImage`
destination region: `NGCanvas` asks Prism to update a texture region outside the canvas and
`checkUpdateParams` throws. `drawCursor` had the same hole for the cursor image itself (a 24 wide box
at the edge, rather than the union's 48).

**The fix.** `damageWithSprite(damage, cursor, bounds)` takes the clip the canvas imposes and returns
`union ∩ bounds`, and `visibleSprite(sprite, viewport)` gives the part of the sprite the canvas covers
(the cursor image is drawn from the matching source offset). Both are static and package-private, so
both are pinned by tests that need no window.

**Verification.** New `JavaFxBackendTest.aSpriteHangingOverTheEdgeIsClippedToWhatTheCanvasCovers` (a
sprite at `WIDTH-4, HEIGHT-4` must leave the repaint inside the canvas and still cover the damage and the
visible part of the sprite), the updated `theFrameDamageCoversTheSprite` - which had been written with a
sprite at (100,200) on a **64x48** bed, i.e. entirely off the screen, and passed only because nothing
clipped - and the live A/B above. Suite: **464 tests, green**.

**Traps.** The clip has to be applied **before** `logical()`, because the canvas is in logical pixels and
the framebuffer is not; this is a *paint-time* failure, so it surfaces far from the code that recorded
the op (a stack that names `openWindow` is telling you where the canvas was first painted, not where the
rectangle was built); and a sprite at the edge is not an error to be avoided, it is the normal state of
a pointer on the last pixel.

### [x] A27. The dock's Quit did not quit: the process stayed, and an app with no window never heard it. **S** - fixed 2026-10-01

**The report.** "Right-click `xserver` in the dock, press Quit - and the EasyShell process on the remote
`.147` is not released."

**What a Quit actually is on macOS, which is the whole of it.** The dock's Quit is an
`NSApplicationTerminate` request. For a **JavaFX** application Glass takes it and calls
`GlassStage.requestClosingAllWindows()` - decompiled from OpenJFX 25, `QuantumToolkit$2.handleQuitAction`
is exactly that one call - i.e. **a close request on every window, and never `System.exit`**. Measured
with a minimal JavaFX application on this machine: the toolkit stays alive, the process stays alive, and
an AppleScript `quit` cancels (-128). With **AWT alone** (no JavaFX), the default strategy *is*
`System.exit(0)`, which is why `--backend vnc` and `--backend headless` already quit properly: that was
measured too, before touching anything.

**So the server's behaviour was**: the window closed (that is the tick's own path - `onWindowClosed`
asks the client `WM_DELETE_WINDOW` and cuts it off once the grace is up), and the client **was**
released - EasyShell exits within a second of the window closing, five runs out of five, verified with
the real application on `.147` in both `--single-window` and per-client mode. What stayed was the
**process**: `jstack` of it twelve seconds after the Quit showed no `xserver-*` thread at all and only
`JavaFX-KeepAlive` (non-daemon, and it outlives `Platform.exit()`) plus `DestroyJavaVM` holding the JVM.
`Main` returned from `main` and nothing ever ended the process, so the dock icon stayed and a second
Quit did nothing.

**The fix.** `XServer.startEventLoop()` returns the display thread and `Main` joins it, then calls
`System.exit(0)`: the display loop is the server's life, and the process follows it. Nothing about *what
ends the server* changed.

**Verification, live, with the real application.** `--backend javafx` on the Mac, EasyShell on `.147`,
then the dock's own Quit (`NSRunningApplication -terminate`, a two-line helper, which is the same request
the dock makes):

| | before | after |
|---|---|---|
| EasyShell on `.147` | released in ~1 s (it quits politely) | released in ~1 s |
| the xserver process | **still alive after 12 s** (`JavaFX-KeepAlive`) | **gone at +3 s** (single-window) / **+1 s** (per-client) |

New `XServerShutdownTest.theDisplayLoopEndsWhenTheServerDoes` pins the property `Main` now relies on.
Suite: **465 tests, green**.

**Traps.** The dock's Quit is *not* `System.exit` on this platform when JavaFX is up, and it cannot be
told apart from closing the window - both are a `WINDOW_CLOSE_REQUEST` - so "quit the app" and "close the
window" mean the same thing here: end the session that window belonged to. `--detach-on-close` is the
opposite intent and keeps the client. And a windowless JavaFX app hears nothing at all, which is why the
measurement above used the no-JavaFX backends to establish what the *platform* does.

**The other half: a *stop* owes the clients the ask too** (2026-10-01, same day, with the user's own
patch, corrected here). The window-close path asks `WM_DELETE_WINDOW` and cuts off after the grace; the
stop path - `Ctrl-C`, a signal, the control port, any backend with no window - only **cut** the
connections. An application whose toolkit is never told to quit can sit on the far machine with a dead
display, which is the difference the user was looking at. `XServer.stop()` now asks every client first
(`askClientsToCloseForStop`) and gives the ones that were asked the grace period to go before anything is
closed. Three traps, each measured or paid for:

- **Through `clients` and `screenOf(client)`, never through `sessions`.** In single-window mode - which
  `--backend vnc` implies - every client's windows are on the master screen and the master session has no
  client of its own, so a loop over `session.client()` asks **nobody**: with `--backend vnc` and EasyShell
  connected the log had **0** asks. After the fix: **4** (its four toplevel windows), same scenario.
- **The wait belongs after the asks, not inside the send loop.** Sleeping 100 ms per window inside
  `closeClientWindows` also runs on the **display tick** (the window-close path calls it), where a client
  with N windows stalls every frame for 100 ms × N; and wrapping `InterruptedException` in a
  `RuntimeException` in a shutdown hook abandons the rest of `stop()` - clients not closed, toolkit not
  released - which turns a quit into a hang. The ask loop now sends everything, then waits (bounded by
  `--close-grace-ms`, and only if somebody was actually asked) for the clients to go.
- **An ask has to be readable to count.** Closing a connection with unread input is a **reset**, and a
  reset discards what the peer had not read: a stop that blocks the reader and then closes has thrown the
  `WM_DELETE_WINDOW` away. That is why the new test reads *while* the stop is running, and why a real
  toolkit - which reacts by quitting, closing the connection first - is the case the wait is for.

New `XServerShutdownTest.stoppingTheServerAsksTheClientsToCloseBeforeItCutsThemOff`. Suite: **466 tests,
green**.

### [x] A28. Two pointers over the window: the system arrow and the X sprite together. **M** - fixed 2026-10-01

**The report, two faces of one thing.** "Start the server, run EasyShell on `.147` against it, then move
the mouse over the server's window: **two pointers**, the system one and the server's own - until you
click on the page, when the system one goes. And if you drag another application's window over it, the
pointer that was the server's alone becomes two again." (The Xvfb+x11vnc comparison does not do this,
which is the tell: there the *client* draws the only pointer and the server composites none.)

**Measured, and the platform is the one that decides.** The pointer belongs to the **active**
application:

- a native probe (no JavaFX in it at all) called `[NSCursor hide]` and then watched
  `CGCursorIsVisible`: **0, then 1** the moment another application was activated, and it never went
  back. A background application cannot keep the pointer hidden, whatever it calls.
- JavaFX does not even try again: `MacCursor.setVisible_impl` compares the wanted visibility with its
  own cached `isNSCursorVisible` and **returns without calling the native method** when they agree - and
  the cache is stale the moment the platform re-shows the cursor. Forcing it through (setting `DEFAULT`
  and then `NONE` again) did not help either: measured, still visible, because the hide itself is gone
  while another application is in front.
- so the state was: the sprite drawn *and* the system cursor visible, which is the "two pointers".
  Clicking the window made this process active, the hide took effect and the system pointer went - the
  user's "after a click it disappears". Dragging another application's window over this one is the same
  state: that application is the active one for the length of the drag.

**The fix: the sprite is what gives way.** `JavaFxBackend.hostCursor(pointerOverWindow,
applicationActive)` hides the platform pointer only while this process has a focused window
(`applicationActive`, a count of focused windows, because the pointer is hidden per *process*), and
`drawCursor` draws nothing while it does not. One pointer either way: the X one whenever the server is
the application in front, the platform's when it is not - which is also the pointer that belongs to
whatever the user is actually using then. A focus change also repaints the sprite's own box
(`repaintCursorArea`), because nothing a *client* draws changes when the host window comes or goes, so
the canvas would otherwise keep the sprite after the application went to the background and keep not
having it after it came back.

**Verification, live on the Mac** (server on `:95` with a client drawing, the pointer parked on a root
area of the window, `screencapture -R` of a 40x30 region **without** `-C` so it shows the sprite and not
the system pointer, plus `CGCursorIsVisible` for the other one):

| application in front | system pointer | sprite on the canvas |
|---|---|---|
| this server | **hidden** | **394 pixels** (drawn) |
| Finder (this server in the background) | **visible** | **0** (not drawn) |
| this server again | **hidden** | **394** (no stale state) |

On the build before the fix the same three states read `hidden/394`, **`visible/402`**, `hidden/394` -
the middle row is the reported two pointers. `JavaFxBackendTest`'s cursor test now pins all three
decisions, and the suite is **466 tests, green**.

### [x] A29. A window's own pixels had no budget. **M** - fixed 2026-10-01

Every window keeps its pixels (B5), and its store is the window's own size - the one allocation in this
server with no bound. Reproduced on a server started with `-Xmx256m`: a client that creates a
12000x12000 window and draws one rectangle into it (which is what allocates the store) hit
`OutOfMemoryError` in `Framebuffer.allocate` inside `MapWindow`, which dropped that connection and left
the server one allocation from the same end - 65535x65535 is 17 GB of store. Every other drawable had a
budget already (a pixmap 16M pixels, an image decode 64M, a BIG-REQUESTS request 16 MB, a WebSocket
message 16 MB, a clipboard 16 MB, the screen `--max-screen`); the window store was the gap.

`XWindow.keepsOwnPixels()` now also requires the window to fit `MAX_STORE_PIXELS` (8192x8192, the
largest screen this server can be asked for). Nothing about the window is refused: it draws straight
onto the screen, the path the root already takes, and every caller already had a branch for it. **No
window that fits on the display is affected.** After the fix the same client maps and draws
(`CreateWindow, MapWindow, CreateGC, PolyFillRectangle`, no OOM, clean disconnect);
`WindowTreeTest.aWindowTooLargeToKeepPixelsKeepsNone` pins the rule without allocating anything. The
whole-project review that found it is `docs/review-2026-10-01.md`.

### [x] A30. The ZRLE palette search ran past the point of being useful. **S** - fixed 2026-10-01

A24 and A25 made an unchanging screen free, so what is left of the VNC path's cost is encoding what
changes. Measured in the encoder's own package (min of 36 full 1300x800 frames per content type):
**two colours 9.8-10.1 ms, text-like 13.5 ms, photo-like 79-100 ms** per frame - at 60 frames a second,
60% to 500% of a core before the socket, which is the number behind A31 and A32.

The encoder looked every pixel up in a palette of up to 128 colours, but past **sixteen** colours a
tile can only be plain RLE or raw, and neither reads the palette - so from the seventeenth colour on
the search was pure work, and a photo-like tile spends most of its pixels there. The search now stops
at the limit (and the per-tile palette array is 17 ints instead of 128): **photo-like frames encode in
57 ms instead of 79-100**, with two-colour and text-like frames unchanged, which is the point - the
common case must not pay for the rare one. `ZrleEncoderTest` and `VncBackendTest` cover the four
subencodings the encoder emits.

### [x] A31. One rectangle per update became the tiles that changed. **M** - fixed 2026-10-01

A25 made an update the **box** the differing pixels are in - right for one change, wrong for two: a
status line and a spinner in opposite corners were one rectangle spanning the screen, and a
full-screen ZRLE frame is 10 ms of one core in flat colours and 57 ms photographic (A30's table).
`RfbSession` now builds an update the way `x11vnc` does: the region in 64x64 tiles, a changed tile
merged with the changed tiles beside it into a run, and each run **shrunk to the box of the pixels that
really differ** - so a small change is still the few pixels it is (A25's rule) and scattered changes are
several small rectangles instead of one big one. A region that changed everywhere is still sent as one
rectangle, which compresses better than a band's worth of them.

`VncBackendTest.aChangeInTwoCornersIsSentAsSmallRectanglesAndNotOneBoxSpanningBoth` drives **one**
`PolyFillRectangle` carrying two rectangles, so the damage cannot be split by the tick, and the test
was checked against the old behaviour: it sends `[5, 5, 285, 185]` where the fix sends two 10x10
rectangles. `RawXClient.fillRectangles` is the harness piece that was needed for it.

### [x] A33. `XWindow.ownPixels()` had no breadcrumb for a window that keeps no pixels. **S** - fixed 2026-10-01

A29's budget depends on every caller asking `keepsOwnPixels()` first. All eight call sites do (audited),
and `ownPixels()` now writes **one warning per window** naming the window and the budget when it is
called for one that keeps none - so a future caller that gets this wrong leaves a log line instead of
an `OutOfMemoryError` with no explanation. The DBE path (`DbeExtension.swap`, the one direct caller
that is not guarded) is unreachable for such a window because DBE buffers are pixmaps and are capped at
16M pixels; it is recorded in the review rather than changed.

### [x] A32. Tight's JPEG method for photographic rectangles. **L** - fixed 2026-10-01

**What A34 changed about it (2026-10-03).** The method is unchanged and still only fires for a
rectangle whose sampled colours say "photograph" - but it now *also* requires the client to have
listed Tight **and** a JPEG quality. Those two facts used to gate only the rectangle splitting, not
the encoding choice, so an eligible rectangle went out as Tight to any viewer, including one that
had asked for ZRLE alone and could not decode it. And because "is this a photograph" is a colour
count over a sample, this is exactly the kind of question a user asks as "is my picture being
blurred?" - so the session now logs `offers [16,0,-223,7,-23]: zrle=… tight=… jpegQuality=…` on
connect, and one line per JPEG rectangle with its size, position and quality. A VNC client that
lists neither Tight nor a JPEG quality can no longer see one, and the log says which it was.

The last of the frame-path costs was content ZRLE is the wrong tool for: a rectangle where nearly every
pixel differs from its neighbour falls through to ZRLE's raw subencoding, which is **3.1 MB and 57 ms**
of one core for a full 1300x800 frame (A30's table, before A30's palette fix it was 83-100 ms). The
protocol's answer has existed since TightVNC and D2 has listed it since the beginning.

**What was built.** `TightEncoder` carries the JPEG half of Tight: a sampled-colour rule for what is a
photograph (`looksPhotographic`), a real JFIF stream at the quality the client's pseudo-encodings allow,
and the protocol's compact 1-3 byte length. `RfbSession` records Tight (7) and the JPEG quality
pseudo-encodings (`-23` is the **highest** quality and `-32` the lowest, which the specification says
explicitly and the numbering does not suggest), splits rectangles wider than the Tight limit of 2048,
and sends a rectangle the rule calls photographic as **encoding 7 with control byte 0x90** - JPEG is a
Tight *compression method*, not an encoding of its own.

**Measured, encoder and wire.** One full 1300x800 frame, min of ten, our own build:

| content | ZRLE | Tight-JPEG |
|---|---|---|
| flat UI | **2.2 ms, 1.5 KB** | (not chosen; 138 KB if it were) |
| text | **13.3 ms, 255 KB** | (not chosen; 304 KB) |
| photograph | 56.3 ms, 3121 KB | **30.6 ms, 697 KB** |
| photograph inside a UI | 34.7 ms, 1682 KB | **23.4 ms, 424 KB** |

and on the wire, a client painting a full-screen photograph while `tools/vnc-meter.py` watched: **3.12 MB
with ZRLE advertised, 0.70 MB with Tight and the JPEG qualities advertised**, same change, same two
updates. The rule's other half is the table's first two rows: text and flat panels are **not** sent as
JPEG, so they keep their losslessness (and would have been ninety times larger as JPEG). New tests:
`TightEncoderTest` (the rule, the compact length with the specification's own `10000 = 90 4E`, and a
stream this JVM's decoder reads back) and
`VncBackendTest.aPhotographicRectangleGoesAsTightJpegAndAFlatOneDoesNot`, which decodes the JPEG the way
a client does and checks both halves.

**What is left of the idea.** ImageIO's JPEG writer is not fast - 30 ms for a large frame - so a
hand-written or native encoder would shrink the encoding time further; and noVNC does not decode
Tight-JPEG, so a browser viewer still gets ZRLE (which is why the ZRLE path had to stay good, and it
did).

### [ ] A20. "The context menu vanishes after the 片段 page has been opened" - not reproduced; measured identical to the reference. **M** - open, 2026-09-29

**The report.** "Click 片段, then right-click the host list: the context menu disappears at once. Without
the 片段 click it is fine." Reported as new, after the XI2 shape and button-mask fixes of the same day.

**What the state is, on both servers.** The 片段 page is a toplevel of its own and the *client* moves
the X input focus to it (`SetInputFocus(page, revert_to = Parent)`, in this server's log; against Xvfb
`xdpyinfo` says `focus: window 0xe0001f, revert to Parent` - the page - after the same click). The main
window is therefore **unfocused**, and it stays that way: neither server focuses a window on a click -
that is the window-manager half this server did not have at the time (it has it now, on by default -
see the switch below), and `main` and Xvfb behave the same - and the client never asks for it either: a tap of the client's own
stream against Xvfb during the gesture has **one** `SetInputFocus` in it (the page's) and none after the
click on the main window, the same as this server's log.

**Measured side by side, same window, same pointer, fresh client, the client's own rendering compared by
pixels** (`Shot` of the menu's rectangle), and **the correction matters**: an earlier run of this table
counted *override-redirect windows in the tree* instead of the pixels, and a tree lists unmapped ones -
so "the menu is there" was read off a window that is always there. By pixels, with the page **closed**
(the reporter's actual sequence - "open 片段, close it, then right-click"):

| state | this server | Xvfb |
|---|---|---|
| right-click a host **row**, no page | menu opens, 77999 non-background pixels, 6003 ink | identical |
| right-click a host row, **page open** | menu opens, 77999 / 2334 | 77998 / 2332 |
| right-click a host row, **page closed** | **no menu**: 76968 / 4169 (the panel's own text) | **76968 / 4169 - identical** |

so **the reference does not open the menu after the page closes either** - the numbers are equal to the
pixel - and the behaviour is the application's, not a difference between servers. `MenuProbe` - the
protocol-level menu gesture, the crossings around a popup that maps under a held button and the grab it
takes - is identical to the reference too. (The click must be aimed at a host **row**: on the panel's
empty background the client's own menus are flaky under automation, on the reference as much as here.)

**And it is not this branch's changes**: the same gesture on `main` (which has no XInput2 at all, so the
client is on the core rail) behaves the same, and so does Xvfb (which does have XInput2). The three
builds agree, so the difference the report sees is not in the input path these changes touched.

**What would settle it** is a capture from the machine where it happens: the server started with
`--log-requests`, the gesture done by hand, and the client's own log - the two together say which event
the client acted on, and the menu's own window (an override-redirect popup, whose map and destroy are
both in the server's log) says whether it was ever mapped. **The mechanism is the one this project has met before**, and the
*switch* that answers it is now in: JavaFX keeps a popup focused only while its owner window is
focused, so a context menu opened from an unfocused main window is hidden again - the shape the report
describes ("it disappears at once") rather than "it never appears". Nothing focuses a window on a click
on either server, and the application never asks after the page closes, so the main window stays
unfocused and its menus cannot stay open. **`--focus-on-click`** gives the missing window-manager half:
a click moves the input focus to the toplevel it lands in, with `revert_to = PointerRoot` so a window
that goes away afterwards hands the focus back to the window the pointer is in. **On by default** (the
owner's call, 2026-09-29): this server is the window manager for what it forwards, and the reported
sequence opens the menu again with it on (measured: 77999 / 6003 after the page is closed, against
76968 / 4169 without it, which is what the reference gives). `--no-focus-on-click` is the comparison
switch - a reference does not focus a window on a click, so a probe gesture that presses a button is no
longer byte-identical to one with it on (measured with `MenuProbe`: the press arrives with a
`FocusIn`).

### [x] A19. EasyShell's process dies when it copies - the bridge asked through a window on the wrong screen. **M** - fixed 2026-09-30

**What it was.** The clipboard bridge is made once, at startup, so its window is a child of the
**master** screen's root. In the default mode every connection gets a screen of its own
(`XServer.openClientScreen`), and its setup reply names *that* screen's root and no other - so when a
client copies, the bridge's `SelectionRequest` names a `requestor` window whose root the client has
never heard of. GDK wraps the window it is asked through (`gdk_x11_window_foreign_new_for_display`,
which is how a toolkit finds out what it was asked about), and a window whose root is not among its
screens is one it cannot place: GDK 3.24 dies in `XScreenNumberOfScreen`, called with the attributes
struct a failed lookup left uninitialised. The bridge now keeps **one window per screen**
(`ClipboardBridge.windowsByRoot`) and asks through the one on the owner's own screen.

**The reproduction, and why every earlier attempt missed it.** Two `hs_err_pid*.log` files the user's
copy left on the VM at 01:55 on 2026-09-30 (SIGSEGV in `XScreenNumberOfScreen`, called from
`gdk_x11_window_foreign_new_for_display`, in the GTK main loop) are the crash; the 20-line GTK3 client
`clipboard-owner.py` reproduces it exactly - it takes `CLIPBOARD`, and the bridge then asks it for the
data. Against `--single-window` it survives (`EXIT=124`, the timeout killing it); against the default
mode it segfaults (`EXIT=139`), and gdb gives the same three frames as the application's own log. The
same app, driven through the host window: with the fix, Ctrl+A and Ctrl+C put the box's text on the
Mac's clipboard and the process lives. **Every scripted attempt in the 2026-09-29 entry below was run
with `--single-window`**, where the master screen *is* every client's screen and the bridge's window
was already placeable - which is why the item read "not reproduced" for a day.

`ClipboardBridgeTest` pins it in the server's own terms: the test server now runs in the default mode,
and `theBridgeAsksFromAWindowOnTheOwnersScreen` asks the owner's window what root the requestor is on
(`RawXClient.rootOf`) and fails without the fix ("the bridge asked through 0x8, which is not on the
owner's own screen").

**The history, kept because it is what the next report like this will have to work from.**

**The report.** "Pick any connection and click 复制连接, or type something into the input box and
select it and right-click to copy - EasyShell's process on the VM crashes." Reproduced by the user, not
by any scripted attempt since (see below), and the crash is the whole of the report: the window goes.

**What the bed's own history says the failure looks like, which is the useful half.** The VM keeps
`/root/hs_err_pid*.log`, and the crashes it has from *before* this session are all one kind, in both
applications (EasyShell's Java 27 and termora's Java 25):

```
siginfo: si_signo: 7 (SIGBUS), si_code: 2 (BUS_ADRERR)
C  [libc.so.6]
C  [libX11.so.6]  _XDefaultIOError
C  [libX11.so.6]  _XIOError                  <- the display connection died
C  [libX11.so.6]  _XEventsQueued
C  [libX11.so.6]  XEventsQueued
J  sun.awt.X11.XlibWrapper.XEventsQueued     <- the toolkit's event loop reading
```

That is **not** a protocol misreading of the kind the XI2 shapes were (those are SIGSEGVs inside
`gdk_display_get_event` at a nought pointer plus `0x90`, and that one is fixed); it is Xlib's fatal I/O
path, i.e. **the server closed the connection**. So the copy is either a request the server answers by
dropping the client, or a server-side exception on that connection's thread - and this server *names*
both cases in its log, so the log from a crash is worth more than any theory:

- `client <peer> disconnected after N requests: failed handling <Request>` (a `RuntimeException`) or
  `... hit <Error> during <Request>` (an `Error`) - `ClientConnection.run`'s two catch blocks;
- `the client is not reading: N messages are already queued for it` - `ClientOutput`'s backpressure
  bound (1024 messages), which closes a client that has stopped draining its socket;
- `XFIXES`/`SECURITY` revokes and `KillClient`, which close a connection by design.

**What was tried, all of it on both the old and the new builds, with the app on the VM and the server
here** (`--single-window`, `--log-requests`): the connection row's 右键 → 复制信息 menu item; the search
box's own 右键 → 复制, with the text selected by Ctrl+A and by a *drag* across it; Ctrl+C and Ctrl+V;
the host clipboard set with `pbcopy` and empty; the reference in place of this server (through
`tools/tap.py`, so both sides' streams are readable). The copy's conversation is healthy every time -
`SetSelectionOwner` from the app, the bridge's `SelectionRequest`, the app's answer, and the host
clipboard holding the copied text a moment later - and nothing on either side dies. **The failure needs
something this reproduction does not have**, and the two candidates worth the next session are (a) the
clipboard round trip as a *loop*: the bridge writes the app's text to the host clipboard, the host
normalises it (line endings, an encoding), the poll reads it back as a *foreign* change, takes the X
selection, GTK re-takes it because it still believes it owns the clipboard, and the two trade it back
and forth - which would fill the app's socket and end at the backpressure bound above; and (b) a
clipboard payload that does not fit one property (the bridge's INCR branch), which no test here has
ever driven.

**The capture was made, and neither theory was it.** The user reproduced it the next morning and the
VM kept the two `hs_err_pid*.log` files: SIGSEGV, `XScreenNumberOfScreen` from
`gdk_x11_window_foreign_new_for_display`, in the GTK main loop - which is the toolkit wrapping a window
it cannot place, not the server closing the connection the bed's older crashes showed. The server's own
log was clean at the moment of every crash (no disconnect line, no refusal), so "the copy is a request
the server answers by dropping the client" was wrong on both counts; the two candidates above (a
clipboard ping-pong, an `INCR` payload) were never involved.

### [x] A18. A click on termora's 密钥管理器 button throws in AWT on this server and opens the dialog on the reference. **M** - **fixed 2026-10-03**; found 2026-09-29

**What it is.** termora (Swing/AWT, 2.0.0-beta.16, the AppImage's own **Java 25.0.2** runtime) started
against a `--single-window` server on the Mac: a click on the toolbar's 密钥管理器 (key manager)
button at (1093,18) makes the application's toolkit thread throw

```
java.lang.IllegalArgumentException: Nonexistent button 16
        at java.awt.event.MouseEvent.<init>
        at sun.awt.X11.XWindow.handleButtonPressRelease
        at sun.awt.X11.XContentWindow.handleButtonPressRelease
```

and the dialog never opens. **The same click on Xvfb opens it** (captured), with no exception.

**What AWT itself says it read.** The JDK's event logger is the instrument, and it is worth recording
because it is the only way to see the client's *reading* of an event rather than the bytes:

```sh
# on the VM, a properties file with sun.awt.X11.event.level=FINE (see the tool's entry in
# tools/README.md), then:
JAVA_TOOL_OPTIONS="-Djava.util.logging.config.file=/root/jul.properties" \
  DISPLAY=<our server> /root/termora-...AppImage
```

It prints, for the failing click:

```
XButtonEvent = type = ButtonPress, serial = 1303, send_event = false,
  window = sun.awt.X11.XContentWindow[0,0,1280x800], root = 0, subwindow = 2, time = 0,
  x = -383051049, y = 0, x_root = 1083, y_root = 18, state = 1093, button = 18, same_screen = false
```

`serial = 1303` is **our ButtonPress's own sequence number** (0x0517, read off the tap), and the type
byte is right - but almost every other field is wrong, and the *wrong* values are the coordinates of
the **previous** event on the same spot (1083/18 are that event's `event_x`/`event_y`, 1093/18 its
`root_x`/`root_y`). That is the shape of a struct read out of a **stale buffer**: the first bytes of
this server's message landing in AWT's native `XEvent` and the rest still holding the event before it.
`button = 18` is what turns into "button 16": AWT maps extra buttons by subtracting two.

**What the wire says, which is the puzzle.** A byte-transparent tap in front of this server during the
same click (`tools/tap.py 6096 6095`, termora on `:96`) captured exactly three well-framed 32-byte
messages - `MotionNotify`, `ButtonPress` with `detail = 1`, `ButtonRelease` with `detail = 1` - and
`xev` against this server prints `button 1, same_screen YES` for the same click. So the button AWT
complains about is not in the stream as Xlib reads it, and the two servers' bytes have not been
diffed field by field yet.

**And now they have been** (2026-10-01, whole-project review). A raw client that selects
`ButtonPress|ButtonRelease|MotionNotify` on its own window and prints every field was run against this
server and against `Xvfb :98` on `.147`, with the same click driven by `tools/kit.sh Drive`:

| field | ours | Xvfb |
|---|---|---|
| type / detail | 4 / 1, then 5 / 1 | 4 / 1, then 5 / 1 |
| root / event / child | 0x2 / the window / 0 | 0x1ff / the window / 0 |
| rootX, rootY, eventX, eventY | 100, 100, 100, 100 | 100, 100, 100, 100 |
| state | 0x0 press, **0x100** release (Button1Mask) | the same |
| same_screen | 1 | 1 |
| time | wall-clock ms (`System.currentTimeMillis`) | Xvfb's uptime ms |

**Every semantic field is identical**, and the only differences are the ids a server picks for itself
and the clock base - which is a *documented* deviation (this server's clock is wall-clock
milliseconds, a reference's is its uptime) and one Xlib is built to absorb. So the `button 16` AWT
reports is not in our bytes at all; its own dump (`button = 18`, `state = 1093`, `x` a 32 bit
garbage value, the rest the *previous* event's fields) is a struct read out of a **stale buffer**, and
that is a client-side desynchronisation.

**What is left to test.** The next experiment is the one that fits that shape: a **raw byte** probe
(not `RawXClient`, which parses the same length fields the client does, and so cannot see a desync) in
front of this server *while termora is the client*, checking the `length` field of every **XI2
GenericEvent** against the bytes actually sent - termora selects XI2, and a generic event whose length
field and payload disagree is exactly how a stream loses its footing while a tap (which logs chunks,
not messages) still looks well-framed and `xev` still reads the core events after it correctly.

**Not a regression from the focus fix of the same day.** The pre-fix build throws the same exception
with the same garbage struct (measured: the fix's source was reverted, rebuilt and the click repeated
- identical log line, identical exception), and the changed method (`InputState.focusWindow`) is
reached only from the key paths.

**Next step.** Run the tap and AWT's logger *at the same time* so the 32 bytes AWT was handed and the
struct it printed can be lined up field by field - that identifies whether AWT read a *different*
message, or ours with an offset. Two candidates to check first: termora selects XI2 (`XIAllDevices`)
on the **root** and on its icon window but not on the content window that was clicked, and AWT asked
for XI2 2.2 where this server answers 2.0 (`Desired version is 2.2, server version is 2.0`, in the
same log) - a toolkit that half-enables XI2 is the kind of client whose event reading is worth
distrusting, and the reference answers 2.2.

**It was not the wire, and it was not the XI2 length field either: it was the event base
`QueryExtension` reports for the XInput extension** (2026-10-03). The XI2-length experiment was run
first and came back clean - every generic event this server sent to a real client was framed
correctly - so the client's reading was instrumented instead, and it turned out to be a *client-side
misreading by design*, which is why the bytes had to be right.

**The instrument that ended it, and it is worth keeping.** AWT lets a class register itself as an
`XEventDispatcher` for a window (`sun.awt.X11.XToolkit.addEventDispatcher`, package-private) and
hands it the **same `XEvent`** the toolkit is about to dispatch, so the ninety six bytes that
produced the exception can be read back:

```java
// --add-exports java.desktop/sun.awt.X11=ALL-UNNAMED --add-opens java.desktop/sun.awt.X11=ALL-UNNAMED
XToolkit.addEventDispatcher(contentWindow, e -> {
    MemorySegment seg = MemorySegment.ofAddress(e.getPData()).reinterpret(96);
    ... hex dump, and e.get_xbutton().get_*() ...
});
```

With that, one host-mouse click on a forwarded window printed both the struct AWT had and, off a tap
at the same moment, the bytes the server sent. **The struct was the device layout**: `serial` right
at 8, `window` right at 32, and then `root` at **48** instead of 40, `child` at 40 instead of 48,
`time` at **64** instead of 56, `x` at **72** instead of 64, `state` at **80** instead of 72 - every
field from `window` on one eight byte slot further along. `button` was therefore the click's root
**y**, which is exactly A18's first report: a click at y = 18 read as button 18, AWT's extra-button
mapping subtracted two, and `MouseEvent` threw `Nonexistent button 16`. Two clicks at different
places produced the same shift, which is what ruled out a stale buffer and pointed at a conversion.

**Why a client would do that, and why the wire could not show it.** The event base in a
`QueryExtension` reply is not a description of what a server sends; it is where a client library
installs its wire-to-event converter. `XESetWireToEvent(dpy, first_event + i, handler)` writes
straight into Xlib's conversion table - the same table the core events use - and libXi calls it for
its whole range the first time a client touches XInput. AWT, GDK and every other toolkit do that at
startup through `XListInputDevices`. **This server answered `first_event = 0` for XInput** (the
registry reports zero for an extension that defines no events *here*, and XI2 sends generic events
at core event 35, so nothing of ours lives in the range), which put libXi's `XDeviceButtonEvent`
converter on top of core `ButtonPress`. A reference answers **66**; Xvfb advertises base 66 and
libX11's XKB support base 85, and neither hijacks anything. `xev`, and a raw Xlib connection that
never called into libXi, read the same button press correctly all along - which is the trap the
entry above kept falling into: **two clients of the same server can disagree about the same bytes
when one of them has hooked the event numbers.**

**The fix.** `XInputExtension` is registered with **16 event codes** (XInput 1.x's own count) and
`XkbExtension` with **12** (XKB 1.0's), so both advertise a base outside the core events and the
range is reserved against every other extension. Nothing this server sends moves: XI2's events are
generic events at 35, XKB sends none, and the *other* extensions' bases shift upward, which their
clients read from the handshake. `ExtensionRegistry`'s "no events, so report zero" rule now carries
the exception in the comment, and a new test (`ExtensionEventBaseTest`) pins both: the two
handshakes report a base ≥ 64, and so does every extension that puts a real event on the wire.

**Verified on the bed** (2026-10-03, per-client mode, `--glx`, the user's own launcher):
`xev`-style raw reads, the toolkit dump above and termora itself all agree - the same click that
threw reads `x = 294, y = 191, x_root = 304, y_root = 191, state = 0, button = 1,
same_screen = true`, `grep -c Nonexistent` over the running application's AWT log is **0** with a
dozen clicks in the toolbar, the terminal and the message list, and the full suite is 475 green.
**AWT is the one measured doing it** (its toolkit connection calls `XListInputDevices` - minor
opcode 2 - at startup, where the GDK connection beside it went straight to XI2's `XIQueryDevice`),
and libX11's XKB support is the same trap with a different library: **any client whose library hooks
the range it was given reads its buttons with that library's layout**. That is the likeliest
explanation for "the click landed somewhere else" reports that no scripted test could reproduce,
because a scripted driver connects, selects and reads without the toolkit's startup path.

### [x] B6. GLX / OpenGL. **XL** - done 2026-09-27, and it needed no renderer: a software GL client renders itself and blits

**What is in.** `extension/GlxExtension`: the handshake a software OpenGL client needs, and nothing
that would need a GPU. The version, both config lists, both server strings, the client's own
announcement, context create/destroy/make-current, whether a context is direct, the context query and
a drawable's attributes. **A direct context is granted and an indirect one is refused** - that is the
decision the whole thing rests on, see below.

**Verified with the item's own tools, on `.147`** (the two that the item's Verify names, installed
with `mesa-utils`):

```
$ DISPLAY=192.168.22.1:61 glxinfo | head -4        $ DISPLAY=192.168.22.1:61 glxgears
display: 192.168.22.1:61  screen: 0                948 frames in 5.0 seconds = 189.564 FPS
direct rendering: Yes
server glx vendor string: xserver
server glx version string: 1.4
```

and a `GetImage` of the screen while it ran shows the three gears - blue, red and green - drawn by
OpenGL on this server. The reference runs the same binary at 2832 FPS, so ours is slower (the client
renders in software and pushes every frame over the wire as `PutImage`, which it does against Xvfb
too: 315 FPS through a byte-transparent tap there), but it renders.

**Why no renderer was needed, and this is the measurement that reframed the item.** The entry said
indirect GLX "needs a GL implementation on the server side". Tapping the wire (`tools/tap.py`) while
`glxgears` ran against Xvfb says otherwise: **the client sends no GL commands at all** - 769 MB of
`PutImage` in 5.6 seconds, its own rendered frames, one `GetGeometry` per frame as the
synchronisation. It asks for `DRI3`, `Present` and `MIT-SHM` and the reference has them no more than
this server does, so it renders in-process through Mesa's software `drisw` path and blits. A client
that is granted a **direct** context never sends a GL request; one told "no" would send its commands
over the wire, which is the renderer this server does not have - which is why the indirect case is
refused at the context rather than at every draw call.

**What is deliberately not advertised, and it is the honest part.** The extension string is **empty**:
a client is told GLX 1.4 core and no extensions, so a toolkit that wants `CreateContextAttribsARB` -
the ES2/shaders path - finds it missing and takes its own fallback instead of being handed a context
that cannot draw. The entry's own trap was "do not advertise GLX before it works"; what makes it work
is that the client draws, and what keeps the trap shut is that nothing is promised beyond it.

**The layouts are measured, and two cost an hour each** (they are the kind that read as a client
bug):

- the opcodes are Mesa's, from `/usr/include/GL/glxproto.h` on the VM: from 18 up they are
  `QueryExtensionsString`, `QueryServerString`, `ClientInfo`, `GetFBConfigs` - one apart from the
  obvious reading, and `QueryServerString` is 19, not 18;
- **the two string requests carry no string**: `QueryServerString` is screen and name only, and its
  reply carries the length at offset 12 with the text after the thirty two byte header;
- **the length is the text plus the NUL.** A reference answers 4 for `"1.4"`, and a client reads
  exactly that many bytes into a string it has already allocated - one short and `glxinfo` printed
  `1.4` followed by two characters of whatever came next in memory;
- `GetVisualConfigs` answers **40 ints per visual**: eighteen fields and then eleven `(token, value)`
  pairs, where the GLX 1.2 specification's struct has eighteen and the header's
  `__GLXvisualConfig` has thirty. Pinned by running `tools/GlxProbe` against Xvfb and correlating its
  first config with `glxinfo`'s own table for the same visual id - never by guessing, because a client
  reads `numProps` ints per visual into a fixed struct and a layout a few ints out is a client reading
  its own field boundaries wrong;
- `GetFBConfigs` answers `(token, value)` pairs and is **self-describing**, so it may carry any subset;
- **the decoder has to skip the request header** (`minor`, then `length`) before the body. Reading the
  body straight away shifts every field by three bytes - invisible in a request whose body is ignored
  (a screen number) and wrong in every one where it is not. `GlxProbe` caught it on the first run:
  `QueryServerString` answered `BadValue` for the name.

**Why advertising it changes anything, and why it is not a speedup - both measured 2026-09-27, with
JavaFX's own words** (`JAVA_TOOL_OPTIONS=-Dprism.verbose=true` on the VM client, which is the only way
to see *why* a toolkit takes the path it takes):

```
without GLX ("ES2 Prism: Error - GLX extension is not supported")
  Prism pipeline init order: es2 sw -> GLFactory could not be initialized -> Fallback to Prism SW

with GLX ("System GPU doesn't meet the es2 pipe requirement")
  Prism pipeline init order: es2 sw -> GLX CreateNewContext, IsDirect, GetDrawableAttributes ->
  Fallback to Prism SW
```

**The end state is the same** - JavaFX renders with its **software** pipeline either way, because its
ES2 pipeline refuses a software GL - so **GLX buys JavaFX no speed. **What it changes is how far the
attempt gets**: with the extension present the toolkit initialises the GL factory, **creates a context**
and queries the GL before rejecting it, and a client halfway into its GL path is a client whose
animation/pulse state is not the state it would have been in. That is the mechanism behind the owner's
report (a menu that stops highlighting, a caret that stops blinking, while input-driven drawing keeps
working), and it is why the default is off.

**And it is compatibility, not efficiency, for the clients that do use it.** A GL client on a machine
with no GPU renders with **software Mesa (llvmpipe) in its own process** and ships **every frame as
`PutImage`** - 769 MB in 5.6 seconds measured against the reference - where the reference hands the same
frames over through a shared DRI buffer. Measured end to end: `glxgears` runs at **~190 FPS against
this server and 2832 FPS against Xvfb**, because one of them carries the pixels as requests and the
other does not. A real speedup would need a **GPU on the client side** with DRI3/Present (shared
buffers instead of uploads) or **server-side GL** with indirect GLX (a real GL in the server) - neither
of which exists here, by design.

**`tools/GlxProbe`** is the harness (checked into the kit, and in `tools/sweep.sh`, where it is now a
comparison rather than a missing feature).

### [x] B7. DPMS and the XScreenSaver extension. **S** - done 2026-09-25

**What it was.** The core screen-saver requests were implemented (`Get/Set/ForceScreenSaver`) and the
two extensions were not advertised at all, so `xset q` printed no DPMS section and anything that wants
to say "do not blank while this plays" - a video player, a presentation tool, a remote-desktop client -
was told the extension was missing. The core handlers also answered *constants*: a client that set a
timeout and read it back saw 600 whatever it had set.

**What changed.**

- **`extension/ScreenSaverExtension`** (MIT-SCREEN-SAVER): `QueryVersion` (1.1, the reference's answer),
  `QueryInfo` (state, saver window, the two countdowns, the caller's event mask and kind), `SelectInput`
  (kept per client, for the notification), `Suspend` (kept), and `SetAttributes`/`UnsetAttributes`
  accepted and ignored - they configure a saver *window*, and there is none to configure here. The
  `ScreenSaverNotify` is sent to subscribers from the core `ForceScreenSaver`, which is the one state
  change this server has.
- **`extension/DpmsExtension`**: `GetVersion` (1.2), `Capable` (**no** - see below), `GetTimeouts`/
  `SetTimeouts`, `Enable`/`Disable`, `ForceLevel` and `Info`, all backed by real state.
- **`display/ScreenSaverState`**, one per server rather than per screen: what would blank is the host
  window and there is one of them. It keeps the core settings, the saver's state, and - hooked from
  the input path, one assignment per motion, button and key - the time of the last input, so
  `QueryInfo`'s countdowns are real numbers rather than constants.
- **The core handlers now keep and report what was set** (`GetScreenSaver` answers the timeout and
  preferences a client set, `SetScreenSaver` stores them, `ForceScreenSaver` changes the state and
  raises the notification) - the same rule the auto-repeat work set for `GetKeyboardControl`.

**Measured 2026-09-25**, three ways:

- **Live, with the real client, which is this item's own Verify.** On the VM against this server:
  `xset q` prints the `DPMS (Display Power Management Signaling)` section - it printed none before -
  and `Display is not capable of DPMS`, which is the honest answer; `xset s 60 0` returns 0 and
  `xset q` then reads back `timeout: 60  cycle: 0`, **byte for byte what Xvfb prints for the same
  command**; `xset s activate` and `xset s reset` are accepted and the state a probe reads through
  `QueryInfo` goes On and back to Off, with `until_server` counting down against the timeout that was
  set; `xset dpms force standby` is accepted. No client request was refused (`rejected` count in the
  server log: 0).
- **`tools/ScreenSaverProbe`** (new): against Xvfb and here, MIT-SCREEN-SAVER answers **1.1**,
  `state=Off` before anything forces it, `kind=0`, `eventMask=0`, and two countdowns that are real
  numbers on both. The one field that deliberately differs is the saver window: Xvfb names the black
  window it maps to blank the display, this server answers `None` because it maps none.
- **`extension/ScreenSaverTest`** (8 tests): both extensions advertised; both versions; DPMS's
  `Capable` = no and the timeouts/level read back; the core settings read back as set; `QueryInfo`'s
  countdown inside a one-minute timeout; the notification reaching a subscriber with `state` and
  `forced` and the drawable it subscribed with; **a client that did not subscribe hearing nothing**
  (the guard that makes the previous test mean something); and `Suspend` kept and released.
  Removing the kept settings fails two of them; removing the extensions fails the rest.

**Decisions worth keeping.**

- **`Capable` is no.** The display is the host window on a machine whose power management is the
  system's business; a client that checks capability then skips its DPMS logic, which is right here.
  Same shape as XINERAMA's `IsActive` answering no.
- **Nothing blanks.** The host window owns the display and on this platform the system's screen saver
  is what blanks it, so the settings and the state are kept and reported and no black window is mapped.
  `QueryInfo`'s saver window being `None` is that decision showing.
- **There is no DPMS reference to diff against**: Xvfb does not have the extension at all (measured -
  `QueryExtension` answers 0), so its replies follow the protocol and the two version numbers the
  extension has ever had.

**Traps.** `ForceScreenSaver` is a **four byte** request - a mode byte and a length - and this server
accepts an eight byte one, which a reference server refuses with `BadLength`; the same class of
leniency the design notes already record for `SetInputFocus`. A probe that also *forces* the saver on
every run is measuring its own doing: the first version of `ScreenSaverProbe` sent a
`ForceScreenSaver(activate)` before reading the state, and every reading said `On`. And the core
opcodes are 107/108/115: a test written from memory used 116/117/124, of which 124 is
`unsupported opcode` here - the live `xset` is what settled them.
---

<a name="b8-mit-shm--deliberately-not-worth-doing"></a>
### B8. MIT-SHM - deliberately not worth doing.

The extension exists to let a client and the server share memory instead of copying pixels through
the socket. **A client on another machine cannot use it at all** (the protocol requires the same
host), and every client here is remote - the VM, or a container. Implementing it would speed up
*same-host* clients only, and those already work (they fall back to `PutImage`/`GetImage`, which is
what the log shows). Do not put it in the plan; if a local client ever becomes the main use case,
revisit.

---

## 4. C - The host window (where this project differs from a plain server)

The host window plays the part of a window manager's frame. Everything in this section is about making
that frame behave the way a user expects, and each item is as much a product decision as a technical
one.

### [x] C1. Screen auto-size: grow the screen with the window. **M** - done 2026-09-24

**What it was.** The screen was the size `--geometry` gave it for the life of the server, so a client's
window was capped at the screen and a forwarded application that wanted to be 2000 pixels wide could
not be. Dragging the host window's edge stretched the frame and stopped.

**What changed.** The screen grows to fit what a drag asks for:

- `Framebuffer.resize` moves the pixels into a larger array and keeps this object's identity - the
  backends wrap `pixels()` and every picture over the screen holds the store - so what changes is the
  size, not the object. `Screen.resize` changes the framebuffer, the root window's geometry and the
  reported size together, because the damage rectangles, the pointer clamp and every window's clip are
  computed from them and a screen whose parts disagreed would draw at the wrong stride.
- The resize happens from the display tick, under the server lock, in `applyRequestedSize` - the same
  place a drag is already applied - and the backends are told afterwards, outside the lock, through a
  new `DisplayBackend.screenResized` (the JavaFX `PixelBuffer`/`WritableImage` and the Swing panel's
  `BufferedImage` are rebuilt there).
- Clients that asked to hear it are sent a `ConfigureNotify` for the root: there is no RandR here, so
  it is the only way a client learns the display changed under it.
- `ConnectionSetup.sendSetup` reads the screen's size under the server lock, so a client cannot be
  told a size the framebuffer does not have.
- `--max-screen WxH` (default 8192x8192) is the ceiling: the setup reply's fields are sixteen bit, and
  8192x8192 is already 256 MB of pixels.
- The window's maximum size (`setMaxClientArea`) is now `--max-screen` rather than the screen. A limit
  at the screen's present edge would stop the very drag that was meant to grow it.

**A screen never shrinks.** Growing is what a user asks for by dragging a window bigger, and there is
no matching gesture that says "and now the desktop is smaller".

**Measured 2026-09-24**, two ways:

- `handler/ScreenResizeTest` (4 tests, a real client over TCP): a drag past the screen grows it in both
  dimensions; what was drawn is still there afterwards *and* the area it grew into really exists (a
  screen claiming 500x400 over a 400x300 array reads nothing at that corner); the growth stops at
  `--max-screen`; and the client is sent a `ConfigureNotify` naming the root and its new size.
  Disabling the growth fails all four, disabling the notification fails exactly one, and disabling the
  pixel move fails exactly one. `HostWindowTest`'s drag tests now assert the new behaviour - the drag
  is honoured rather than capped - and its maximum-size test asserts `--max-screen`.
- Live, on the Mac with EasyShell from the VM on a 900x600 screen: a synthesized drag of the frame's
  bottom-right corner grew the screen to **1200x789** (confirmed from a framebuffer dump) and the
  client's window was resized with it. EasyShell refuses a window larger than 1000x756, so the frame
  came back to **1000x784** = its content plus the title bar - which is the "the frame is pulled back
  to the content" behaviour the design notes describe, now working in the direction it never did.

**Traps.** Two things this ran into, both now in the design notes: an unmanaged canvas contributes
nothing to its parent's preferred size, so `sizeToScene()` leaves the window at the size the scene was
built with - a window whose content changed used to keep whatever size the user had dragged it to - and
a window size applied while the user is still dragging is discarded by the platform, so the fit has to
be re-checked every frame rather than only when the viewport moves.

**Left open**: a screen does not shrink (above); and a client that has not asked for structure events
learns the new size only by asking again (`xdpyinfo` reports it, since each connection is answered from
the screen as it is now).

---

### [x] C2. Raise and activate the host window when a client window appears. **S-M** - done 2026-09-25
**What it was.** The host window was made once and never brought forward, so a forwarded application
started while something else was frontmost stayed behind it: grey title bar, no keyboard, and - since
macOS ignores a background application's cursor changes - the platform pointer left visible next to
the sprite.

**What changed**, as the plan recorded here asked: the tick raises the frame when the client's **first
top level window** is mapped, and only after a frame has been presented. `ScreenSession` carries the
two facts (`raisePending`, `framePresented`), `XServer.tick` sets the first where the identity source
goes from nothing to a window and consumes it at the end of a tick, and the backend call is the
`raise()` that C3's urgency hint already used (`stage.toFront()` / `frame.toFront()`, deliberately not
a focus request).

Three things keep it from being a window that steals the front: it fires **once per session** (a
client's later windows, dialogs included, are its own business), it ignores **override-redirect**
windows (the identity source is already the largest non-override-redirect toplevel, so a menu mapped
per keystroke cannot trigger it), and it **waits for a frame**, because raising onto an empty frame is
telling the user their application arrived and showing them nothing. Everything a raise waits for is
the tick's own work, so the raise can happen on a tick that presents nothing new - a window that has
drawn once and then sits still never damages the framebuffer again, and a raise gated on damage would
never happen at all (which is how the first version of this failed its own test).

**Measured 2026-09-25**, `HostWindowRaiseTest` (3 tests, a recording backend): the first window brings
the frame forward, and a `present` call is in the record before the raise; a second, larger window
does not raise again; and a popup does not raise at all. **The first two fail with the raise disabled**
(the popup test passes either way - it asserts that nothing happens, which is also what a server with
no raise does, so it is a guard rather than a test of the fix). `mvn test` 254/254. C3's
`WindowStateTest` was updated for the interaction: its urgency test measured raises from zero and now
measures the *change* the hint makes, because a client window appearing is itself a raise now.

**The live check is inconclusive here, and this is the honest result.** With IDEA frontmost and
EasyShell launched from the VM, the host window came to the front - but it did so **with the raise
disabled as well**. Both oracles agree: `tools/host-windows.c`'s list puts the host window first in
both runs, and the frontmost *application* (`lsappinfo front`) becomes the server's own process in both
runs. A JavaFX stage shown by a stage that is already opening the window activates its application by
itself, so the difference this item is about cannot be produced in this test bed even by removing the
fix. What the tests cover is the ordering and the once-only rules; what is *not* covered by any
measurement here is the case the item was reported from.

**Traps.** `toFront()` on macOS activates the application, which is a visible side effect (the user's
own front window changes) - kept deliberately, because the report is about an application that was not
active. The second trap is the one that cost a rewrite: `raisePending` has to be consumed on a tick
that needs no damage, or a still window never gets its raise.

---

### [x] C4. Bridge the host clipboard to the X selections. **M** - done 2026-09-24

**What it was.** Selections worked *inside* the X server, so copy/paste between two forwarded windows
worked, and nothing connected them to the macOS pasteboard: text copied in a forwarded application
could not be pasted into a Mac application, and the other way round.

**What changed.** `clipboard/ClipboardBridge`, a window of the server's own that owns `CLIPBOARD`
whenever a client does not, plus `clipboard/HostClipboard` over the platform's. The design decisions
that were not obvious, and the measurements behind them:

- **The host-to-X half is a poll, not a request.** Every X request runs while the server lock is held,
  and reading a platform pasteboard is a round trip that can block, so the poll thread (200 ms) keeps
  the text and the request path only ever reads the copy. The bridge's state is guarded by the *server
  lock* and nothing else, so the two threads cannot take two locks in opposite orders.
- **The X-to-host half has to be eager**, which contradicts the plan above: macOS does not tell anybody
  that a pasteboard was *read*, so there is no "someone is about to paste" moment to fetch on. When a
  client takes the selection the bridge asks it for the data straight away and puts it on the host -
  which is also why copying inside a forwarded application replaces the Mac's clipboard, the way every
  bridge on a platform without a read notification behaves.
- **The bridge owns the selection only while the host has text to serve**, and gives it up when it does
  not, so a paste with a picture on the Mac finds no owner and leaves what the user had alone.
- **A host copy wins over a client's**, or the client that copied before would answer the next paste
  with its old text: a change to the pasteboard that this bridge did not make takes the selection over.
- `PRIMARY` is not bridged (a pasteboard has no three-button pointer) and neither are images or files
  (no X11 equivalent to offer). `INCR` is honoured when *receiving* from a client, whose choice it is;
  the bridge serves a property in one write, because it is not a client and is not bound by a request
  size.

**Measured 2026-09-24**, three ways:

- `clipboard/ClipboardBridgeTest` (9 tests) against a clipboard the test owns: the bridge takes the
  selection when no client does and gives it up when the host holds no text; a client pastes what the
  host holds and is told `TARGETS`/`UTF8_STRING`/`STRING`/`TEXT`; a target it cannot serve is refused
  with `property None`; a client's copy reaches the host and the client keeps the selection; both
  directions in Chinese; a chunked (`INCR`) transfer arrives whole; and a Mac-side copy takes the
  selection over. All nine fail when the bridge is neutered.
- Live, against the **real macOS pasteboard**: both directions with `tools/ClipboardProbe`, and both
  directions with a **real toolkit** (`java.awt`'s clipboard on the VM, which is the X selection - the
  same path termora's Swing clipboard takes), Chinese included, each end checked with the platform's
  own pasteboard. EasyShell was connected throughout and the round trip left it alone.
- `mvn test` 158/158.

**Found on the way, and fixed**: `GetProperty` read `long_length` as a signed int, so the
`0xFFFFFFFF` a client sends to mean "the whole property" was `-1` and came back as
`BadImplementation` instead of data - which is what *every* client sends when it reads a clipboard
property. `handler/GetPropertyTest` (4 tests) pins the read sizes.

**Traps worth keeping.** A probe that holds the selection and does not answer `TARGETS` looks exactly
like a broken bridge: a toolkit asks for `TARGETS` first and gives up if it is refused, and that hunt
cost an hour (`tools/ClipboardProbe` did not answer it either until a real client pointed it out).
`UTF8_STRING` is what carries anything but ASCII, and `STRING` is Latin-1 by the letter of the spec
and UTF-8 in practice, so it is read as UTF-8 when it is valid UTF-8 - the only reading that gets both
kinds of client right. The shell's `pbcopy`/`pbpaste` are blocked in this environment's sandbox while
AWT is not, so the host end of a live check goes through Java.

**Not covered here**: pasting into a real application's widget by hand (the last mile is a mouse and a
keyboard, and a scripted gesture inside EasyShell has side effects); images and files; `PRIMARY`.

---

### [x] C6. HiDPI automation. **S** - done 2026-09-24

**What it was.** `--scale N` made the picture sharp on a Retina panel, but the user had to know the
panel's scale factor and pass it by hand, and a client that sizes itself from the display had to be
started with a flag of its own for every application.

**What changed**, and it is two halves with only one of them a server's to do:

- **`--scale auto`** resolves to the host's own scale factor (`ServerOptions.hostScale`, read from the
  platform's default transform - the one piece of HiDPI information the JDK gives up without a
  toolkit). The scale stays 1 by default, and the reason is measured below.
- **The screen publishes `Xft.dpi`** in the root window's `RESOURCE_MANAGER` whenever it is scaled -
  the same property `xrdb -merge` sets, and the question a toolkit that renders through Xft (GTK, Qt,
  an `xterm` with Xft fonts) asks the display. A client that reads it draws its interface at the
  resolution the screen really has, which is what makes a forwarded GTK application sharp without
  being started with anything special. A client that does not read it is unaffected, which is why
  publishing it is safe.
- **The other half is not automatable here**: a JavaFX or AWT client sizes itself from its own toolkit
  settings, so it needs `-Dsun.java2d.uiScale=2` (or the equivalent) to draw at 2x. The server cannot
  set a client's JVM flags, so the honest version is a loud line in the log, which an unscaled server
  on a scaled display now prints with the option's name in it.

**Measured 2026-09-24**:

- `ServerOptionsTest` (5 tests) and `handler/HighDpiTest` (2): `--scale auto` is the host's scale and
  the screen is that many times the logical size; a scale no display can line up with is refused;
  `--max-screen` is parsed and capped at what sixteen bit fields can describe; a scaled screen
  publishes exactly `Xft.dpi:\t192\n` and an unscaled one publishes nothing. Not publishing fails one
  and making `auto` a constant fails the other - on this machine, where the host really is 2x.
- Live, on the Retina panel: `--scale auto` resolved to 2 (in the log), `xdpyinfo` from the VM
  reported **1280x800 pixels (169x106 mm) / 192x192 dpi** for a 640x400 `--geometry`, and the
  resource manager carried `Xft.dpi: 192`.
- **And the measurement that keeps the default at 1**: EasyShell, run against that server, is a
  JavaFX application that does not read `Xft.dpi`. Its toplevel came out 1280x800 - filling the screen
  it was told about - so the host window was **640x428 logical points**, with the application's
  interface at half the size it should be. Sharp, and half-sized: exactly the trade-off
  `docs/design-notes.md` records for `--scale`, now measured with the client the author actually
  forwards.

**Traps.** The screen is four times the memory at scale 2 (see E4) and the software blend has four
times the pixels (E3). A client that scales by the reported DPI *and* is given its own `uiScale`
double-scales - the DPI is there to be followed, so do not also pass the flag to something that
already reads it.

**Left open**: a per-client scale setting, which is what would let one server serve a follower and a
non-follower at once; it needs the client to say what it wants, and the protocol has no way to.

---

### [x] C7. Remember the host window's position per client. **S** - done 2026-09-24

**What it was.** Every session opened the host window at the platform's default position.

**What changed.** The position is remembered per client in `~/.xserver/host-windows.properties` and
handed back to the backend when the window opens, clamped to the part of the display windows may
occupy. `DisplayBackend` grew `setWindowPosition` and an `InputSink.windowMoved` report; the JavaFX
and Swing backends implement both, and the record of a position is written from the display tick
rather than from the toolkit's thread.

**Only the position, not the size.** The size half of the item is already how the server works: the
host window is sized to the client's windows, and dragging its edge resizes the client's window - so a
client that cares how big it is remembers that itself and asks for it next time. Opening a window
larger than its content is the failure mode the design notes call out (the part it grew into shows
nothing, and the next content change pulls it back), so remembering a *size* would fight the frame
the server already keeps sized to what it shows.

**Measured 2026-09-24**, two ways:

- `HostWindowPositionTest` (4 tests, a recording backend) and `HostWindowStoreTest` (6): a window opens
  where it was left; a second client's remembered position is not applied to another's; the position a
  window *opens* at is not treated as a move; a client with no `WM_CLASS` is not remembered at all;
  and remembered positions are clamped - off the right and bottom, off the left and top, and a window
  larger than the display keeping its top-left. Neutering the write and the restore makes them fail.
- Live, on the Mac with EasyShell from the VM: its real `WM_CLASS` is
  `cn.oyzh.easyshell.EasyShellApp`; a synthesized title-bar drag (CGEvent) moved the window from
  `106,81` to `-194,195` and the store followed it exactly. On restart the server logged
  `opened a window for display :91 ... at -194,195` and the window came up at **`0,195`** - the
  backend clamped the part that was off the left edge - while the store kept the user's own value.

**Traps.** A window's *opening* position arrives through the same report a drag does, and writing it
out overwrites what was remembered with the very place the restore was about to change: measured
happening on the first live run, which is why the first position of a session is adopted as a baseline
rather than recorded. A drag reports a position per frame, so the write waits for the position to hold
still (400 ms) - the same settle the viewport uses. And the clamp belongs to the backend, not to the
store: only the backend knows what the display has room for now.

---

### [x] C3. Fullscreen and urgency from the client. **S-M** - done 2026-09-24

**What it was.** The host window took the client's title and icon and resized the client's window
when dragged, but nothing else of the EWMH vocabulary: a client that asked to be fullscreen, or set
its urgency hint, was ignored.

**What changed.** The two things a client asks its window manager for, applied to the frame:

- **Full screen** (`_NET_WM_STATE_FULLSCREEN`) shows the host window full screen and gives it back
  when the client asks for it back. `WindowIdentity` reads the state off the client's toplevel, and
  the tick applies it, the same path the title already takes. `JavaFxBackend.fitWindowToViewport`
  stands aside while the window is full screen - that is the one time the window is deliberately not
  the size of its content, and the platform is the one sizing it.
- **Urgency** (`WM_HINTS`' `UrgencyHint`) brings the window to the front, once per hint, and
  deliberately does **not** take the keyboard. This is *not* the raise-on-show that C2 describes and
  that the user parked: nothing happens unless a client says it wants attention, which is the one
  case where pulling a window out from behind another is what was asked for.

**The protocol half, which is where the work was.** A toolkit does not set `_NET_WM_STATE` - it sends
a `ClientMessage` to the root and waits for the window manager to write the state into the property,
and a client that is ignored asks again for ever. So `AtomHandlers` handles the message
(`applyWindowStateMessage`), and there is a **found bug** behind why it could not work before:
`SendEvent` filled in the event's window field with the window it was *delivered* to, which is right
for every event that is reported *against* a window and wrong for a `ClientMessage`, whose window
field is the subject the sender put there. Every EWMH request - and the `WM_DELETE_WINDOW` reply -
was arriving with its subject replaced by the root. Fixed in `AtomHandlers.sendEvent`.

**Measured 2026-09-24**:

- `handler/WindowStateTest` (6 tests, a real client, a recording backend): asking to fill the display
  shows the display *and* writes the state back; asking again gives it back; `toggle` adds and then
  removes; a request naming two states (maximised horizontally and vertically) applies both; a state
  written straight to the property is honoured too; and the urgency hint raises the frame exactly
  once - a second raise per frame is what a window the user cannot get away from would look like.
  All six fail when the message handling or the tick's application of it is disabled.
- Live, on the Mac with `tools/WindowStateProbe` (new in the kit): `fullscreen on` put the host
  window into the platform's full screen (`window is now full screen` in the log), the client read
  `[_NET_WM_STATE_FULLSCREEN]` back out of its own property, and `on,off` in one connection gave it
  back again.

**Decisions worth keeping.** Full screen is the **platform's** full screen - on macOS another Space,
with the animation - rather than a window stretched to cover the menu bar: it is what the system does
for an application that asks to fill the display, and the user leaves it the usual way. It is a
product decision rather than a technical one, and it is one line to change if it is wrong.
**Maximised** is deliberately *not* mapped to a host window state: the host window follows its
content, and a client that maximises itself resizes its own window, which the screen now grows to fit
(C1) - so the request is written back to the property and the client's own resize does the rest.

**Left open**: `WM_TRANSIENT_FOR` still only exists as a property, and it stays that way: with one
host window per application there is nothing for a transient to be relative to (C8, which would have
made one, is declined).

---

### [~] C5. Host ↔ X drag and drop (XDND). **M** - **the drag in works, owner-verified 2026-09-27**, once the client can open the path it is handed (`--drop-mount`); the drag out is still the reverted trigger

**What it was.** Nothing: dragging a file from Finder onto a forwarded window did nothing.

**What was built (the half that can be).** `drop/DropBridge`, a window of the server's own that speaks
XDND as the drag's *source*: the platform reports the drop and the files, the bridge takes the
`XdndSelection`, sends `XdndEnter`/`XdndPosition` to the window under the pointer that publishes
`XdndAware`, waits for its `XdndStatus` (or 700 ms, whichever comes first) and sends `XdndDrop`, then
answers the `ConvertSelection` that follows with a `text/uri-list`. The host side is a few lines of
`DragEvent` in each backend - `InputSink.hostDrop` - because the platform's half of a drag is the only
part no X client can know. The bridge only exists for a server that presents a window, on the same
reasoning as the clipboard bridge.

**Found on the way, and it is a real protocol bug: `SendEvent` with no event mask did not deliver.**
X sends such an event to the client that *created* the window it names, whatever that client selected
- it has to be that way round, because a target cannot select for a message it does not know is
coming - and the server treated "no mask" as "nobody is interested", so every one of these messages
was dropped. XDND is built entirely out of them, which is where it was found; `WM_DELETE_WINDOW`
replies and everything else in EWMH use the same form. Fixed in `AtomHandlers.sendEvent`, pinned by
`handler/ClientMessageTest` (2 tests).

**Measured 2026-09-24**: `drop/DropBridgeTest` (3 tests, a real client playing the target): a dropped
file is offered to the window under the pointer, with the source window, the XDND version and the type
it carries; a target that answers `XdndStatus` is dropped on and can ask for the files, and gets
`file:///…` one per line out of the property; a window that does not publish `XdndAware` is left
alone. Disabling the `XdndEnter` send fails two of them.

**Both directions were built, and both fail against a real application. Measured 2026-09-27, on the
two clients this bed has, after the owner tried them by hand:**

- **A drag that starts inside an application froze termora and did nothing in EasyShell.** That is the
  drag *out* half, and the cause is a design fault of the first version rather than a bug in it: the
  host window started a platform drag whenever **any** client owned `XdndSelection`, and a client
  dragging something *inside itself* takes the same selection. So the platform took the drag over and
  the application's own drop never arrived - termora waited for a session that had been hijacked and
  hung, EasyShell's internal move never happened. **The trigger was reverted the same day**; the X side
  (`DropBridge.selectionOwnerChanged`/`askDraggingClient`/`dragOutPaths`) is in and tested, because it
  is the enabling piece, but nothing calls it yet.
- **The corrected design, and it is the one thing the first version got wrong**: a platform drag must
  be started only when the gesture **leaves the host window** with the button still down - the only
  moment that tells "drag out to the desktop" from "drag inside the application". macOS keeps
  delivering dragged events to the view that got the press, so the session can be started from the
  event that crosses the edge. That is what the next attempt should build, and it is a small change to
  the handler that was reverted.
- **A drag from the host's desktop into an application reaches the window and stops there.** Driven
  from outside (`hostDrop` straight into the server, so no Finder gesture is needed), the app's window
  publishes `XdndAware` version 5, the bridge finds it and sends `XdndEnter`/`XdndPosition`/`XdndDrop`
  - and then **the application never answers**: no `XdndStatus`, and no `ConvertSelection` for the
  data, so nothing is uploaded. Its own UI reacts (EasyShell renames its window to
  "===松开鼠标以释放文件"), so the messages arrive and are understood as a drag. What a real toolkit
  wants that this does not provide is **not yet known** - the next step is a *real* XDND source to
  compare against, because `DropBridgeTest`'s target is this kit's own client and agrees with the
  bridge by construction.

**The lesson for the second half is the same as B5's Verify's: a synthetic client is not a client.**
`DropBridgeTest`'s three tests passed throughout, and the probe that "verified" the drag out was one
the bridge itself had asked to answer. Both directions need a real toolkit on the other end before
either is called done.

**Measured 2026-09-27 with a controlled drag source, and the answer is not the protocol: EasyShell
*does* answer, and the file is on the wrong machine.** A JavaFX drag source of this project's own
(`/tmp/HostDragSource.java`: a fixed, always-on-top window holding a real file) started a **real
platform drag** onto a forwarded EasyShell on this server, and the log shows the whole conversation:
the bridge sends `XdndEnter`/`XdndPosition`/`XdndDrop`, EasyShell fires a burst of `InternAtom` (GDK
resolving the type atoms it was just told about), then **`SendEvent` twice - its `XdndStatus` - and
`ConvertSelection` six times, asking for the data**, which the bridge serves with a `SelectionNotify`
each time. So the entry's "the application never answers: no `XdndStatus`, and no `ConvertSelection`"
is **stale for this build**: the app answers exactly as it should.

What it cannot do is *read the file*. What the bridge hands over is a **host path** -
`file:///private/tmp/xdnddir/dropped.txt` - and the client is on another machine, where that path does
not exist (on Linux not even `/private/tmp` exists). That is the whole of "nothing happens", and it
also explains the owner's own observation exactly: the **macOS build of the same application** shows
its release marker when a Finder file is dropped on it, because *there the URI resolves*.

**So the open question is not XDND but file transfer**, and XDND is URI-based by design - the target
opens a path, it does not download a file. The ways out, in the order they are worth trying:

- **A filesystem the client can see - and a mapping, which is now in (`--drop-mount H=C`,
  2026-09-27).** With the host's directory mounted on the client (a VMware/Parallels shared folder, or
  an sshfs/NFS mount) the URI resolves, and `--drop-mount /Users/me/Shared=/mnt/hgfs/Shared` rewrites
  the path so the URI names the file at *its client path*. `DropBridge` matches the longest prefix and
  leaves everything else alone; a test drops two files and checks one is rewritten and one is not, and
  it fails with the mapping taken out. Without the option the path is reported as it is, which is
  right for a client on this machine.
- **Normalise the path the bridge reports - done 2026-09-27.** `JavaFxBackend` hands over
  `/private/tmp/...` (macOS's resolved `/tmp`), and a client that shares `/tmp` cannot find that, so
  `DropBridge.toUris` now reports the path without the prefix when what is left is the same file.
  **The same function was escaping only spaces**, which breaks on the names files actually have - a
  `#` starts a fragment, a `%` starts an escape, and a non-ASCII name (the common case for this
  server's own user) is not a byte a URI may carry raw. Both directions now escape and decode UTF-8
  byte by byte, with tests on each (`DropBridgeTest`, two new cases).
- **Serve the bytes.** An application that fetches the *contents* over the selection (a target like
  `text/plain` or `application/octet-stream`) could be satisfied by this bridge - it already has the
  file on disk. That is a per-application convention rather than a protocol, so it only helps the
  applications that implement it.

**The owner's own hands then closed it (2026-09-27): the drag in works, in a configuration where the
client can open the file.** With a directory of the host's shared into the VM and the server started
with `--drop-mount /Users/oyzh/MacDrop=/mnt/hgfs/MacDrop` - any mount will do, the point is that the
*path in the URI exists where the application runs* - a file dragged from that directory onto a
forwarded EasyShell arrives and the application uses it. **First configuration in which this direction
has been seen to work with a real application**, and it fits everything measured below: the protocol
path was already right, and the missing piece was a path the client could open.

**The owner has parked the item here (2026-09-27)**: drag-in works and is verified, and the drag-out
half - the corrected trigger, which needs a hand to accept - is deliberately left where it is rather
than carried on with. Nothing about the item blocks anything else.

**What it costs is the configuration** - the owner's words: it works, but setting it up is a nuisance.
The alternative that removes that step was offered and **declined**: a server-side "push"
(`--drop-push user@host:/dir`) that copies the dropped file to the client machine's directory and hands
over *that* path, so any file could be dragged from anywhere. Declined because it **writes files,
silently, onto another machine** - so if it is ever added it has to be opt-in, and it is worth saying
what it is not: it copies to the machine *running the client*, not to whatever host the application's
own session points at (that step, if it happens at all, is the application's own).

**And the earlier correction is kept because it is what the two halves look like apart**: *without* a
filesystem the client can see, dragging a file from Finder onto a forwarded EasyShell does nothing at
all, while the *macOS* build of the same application shows its `松开鼠标以释放文件` marker. The
measurement below is why: the application is handed a URI naming a *host* path, and it opens paths, it
does not download files.

**Measured 2026-09-27 with a real toolkit on the other end: a real GDK *target* receives the files.**
`tools/xdnd-target.py` (new) is a **GTK3 drop target** - the same GDK a forwarded application uses -
and a real Finder drag onto it, on this server, ends with the target printing
`DATA ['file:///private/tmp/xdnddir/dropped.txt']`. The whole path is in this server's own log:
`a host drag is over the window with 1 file(s); accepted=true` → `the host dropped 1 file(s) at
824,487` → `DropBridge: dropping 1 file(s) on 0x200002` → the target's `XdndStatus`, its
`ConvertSelection`, and the `text/uri-list` the bridge served. **So the bridge's conversation is one
a real toolkit accepts.** (The paragraph in this entry that says "the application never answers" was
written before the drag could be driven at EasyShell at all; **the measurement further down supersedes
it** - the application answers, and what it cannot do is open the file it is told about.)

**What that session cost, in harness facts worth not re-deriving:**

- **A drag between two windows of *one* GTK process never uses XDND.** GDK handles it internally, so
  a source and a target in the same script produce a drop, a `drag-drop` signal, and **not one
  `XdndEnter` on the wire** - measured by decoding the capture and finding no XDND atom in it. The
  pair has to be **two processes** (`tools/xdnd-source.py` and `tools/xdnd-target.py`), and that is
  the mistake to avoid: it looks exactly like a working drag.
- **`tools/host-drag.c` is too abrupt to deliver a *drop*.** Its 24 moves then an immediate mouse-up
  move a window fine (C1/C7) and produce drag-*over* events here, but no `drag-drop`; a variant with a
  pause before the mouse-up (`/tmp/host-drag2`, 300 ms before the moves and 700 ms before the button
  goes up) delivers it every time. The kit's tool is the one to fix if this is done again.
- **In this environment any new shell command raises the IDE window over Finder**, so a *Finder-sourced*
  drag only works inside a single command that re-raises Finder first (`open -a Finder …; sleep 2;
  drag`) - and even then the source went flaky after the first two drags. **The scriptable, repeatable
  oracle is the two GTK processes**; the Finder drag is for a human's hand.
- **`tools/tap.py` logs byte *chunks*, not messages.** A reader has to reassemble the stream before it
  can decode anything, and the C2S setup is 12 bytes **plus the auth name and data** (each padded to
  four) - a framer that assumes a bare 12 desynchronises immediately and looks like a client that
  sends nothing. `XProxy` does the framing for you, which is why its own two framing bugs mattered so
  much (next paragraph).

**And the tool that made those captures possible was broken in three ways, all of them hidden.**
`XProxy` assumed every request has an eight byte head - `BIG-REQUESTS`' `Enable` is **four**, and xcb
sends it on every connection; an extended length is triggered by a **zero length field**, not by
opcode 139; and **a generic event is not a plain event** - it carries its own length, and forwarding
its first thirty two bytes leaves the rest of it in the stream. The last one is what the kit's notes
had given up on as "EasyShell hangs through `XProxy`, the reason was never found": GDK's input path is
XI2, so the client desynchronised at its **first device event** and stalled before drawing a pixel.
All three are fixed, and the check is now end to end: EasyShell renders through the proxy on Xvfb -
the whole interface, in a capture - with every stream consistent against its own length fields, and
this is the first time the "same client, two servers, compare the request sets" measurement this
class exists for is actually available. (Each line now carries its **connection id** as well: a client
opens several connections, they interleave in one file, and a reader that assumes one stream sees a
misframe that is not there - an afternoon lost to exactly that before the ids were added.) It still
cannot proxy the VM-to-reference direction from the Mac, so it is compiled **on the VM** - `java` is
there (`/tmp/kit`) - and `tools/tap.py` is the fallback when it is not.

**What is still open in C5, precisely.** The drag-in *code* has now been exercised by a real toolkit
and works; **the drag-out trigger has not been touched since it was reverted**, and it is the half
that needs the corrected condition (start the platform drag only when the gesture **leaves the host
window with the button still down**) plus a hand to confirm that the applications' own internal drags
and menus still work. The two applications this bed has were tried again with the Finder drag this
session and did not reproduce the earlier "no answer" - the scripted gesture is not reliable enough in
this environment to call that settled either way, and `tools/xdnd-*.py` is the instrument for the next
attempt: put the GTK target on this server and compare, step by step, what it is told.

**The earlier reasoning, kept because it is what the code is built on:**

**X does say when a drag has started, and the entry's two reasons for not doing this were wrong.** A
client that decides to drag takes the `XdndSelection` - the one signal a server can see - and a
JavaFX window *can* start a platform drag (from a drag-detected handler), so neither "no signal" nor
"no drag source" was true. What was wrong was the trigger's *condition*.

---

### [~] C8. One host window per client toplevel. **L** - **declined 2026-09-27**, after being built twice; the requirement is one host window per *application*

**The requirement that decides this, and it is the owner's:** *one host window renders one
application*. In their words, 2026-09-27 - "我不需要每个窗口都对应宿主机的一个窗口，而是一个窗口渲染一个
应用即可". An application is one window on the host's desktop, whatever it opens inside itself; a
dialog is part of that application's window, not a window of its own. **This is a product decision,
not a technical blocker**, and it is recorded here so the item is not attempted a third time - what
the two attempts kept discovering was that it needed work, when what it actually needed was not to be
built.

**What the server does instead** (and did before either attempt): a connection's screen is shown by
**one** host window, following the union of the top levels that connection has mapped
(`contentBounds`). An application that opens a dialog beside its parent makes its own frame grow to
cover both, which is the behaviour the owner wants.

**The second attempt, and why "it works now" was not enough.** After B5's per-window store landed, the
model was rebuilt on it and the pixel problem that had it reverted on 2026-09-26 was gone: a frame
carried the window's own subtree composited from the stores (`draw/WindowSubtree`), `mvn test` 361/361
with `WindowSubtreeTest` and `PerToplevelWindowTest` new, and live on `.147` EasyShell's 设置 opened
`应用设置` in a host window of its own (750x678) while the main window stayed 1300x728 and free of the
dialog's pixels. It was reverted in the same session on the requirement above.

**What was seen while it was up, in case a future session is tempted:** the frames get the platform's
default position, so a dialog's host window opened exactly on top of its parent's (both at
`95,174`) - a dialog the user has to move before they can see what it came from; and the host-window
behaviour a user already knows (where it was left, its title, its raise, the idle state) becomes
per-window state, so every window an application opens starts its own arrangement from scratch.

**The design and every trap the two attempts cost are kept below**, because they are the reason this
entry can be answered rather than re-derived: a frame cannot show a rectangle of the screen (the
dialog is in it), nor the top level's own store (its content window covers it), and the per-window
bookkeeping has four sharp edges.



**What it is.** A connection gets one host window showing the **union** of every top level it has
mapped (`contentBounds`), and that rectangle is what the frame follows. An application that opens a
dialog beside its parent makes the parent's frame grow and jump; a small window with a dialog far away
arrives in a large frame with empty space in it.

**The whole item was built, and then measured against the thing that made it not shippable.** The
refactor is written and was green (`mvn test` 350/350 with five new tests); it is reverted, and what
follows is the design and the measurement so the next session starts where this one finished rather
than at the beginning.

**The design that was prototyped.** Everything that belongs to a window rather than to a screen moves
into a `HostWindow`: the backend, the viewport, the title and icon, the largest size it may be dragged
to, where the user left it, whether it has been raised or told it is idle, and - the part that makes
the rest work - **its own `InputSink`**. The backend hands pointer and key events to that sink, which
passes them to the screen's `InputState` (their coordinates are screen coordinates already, because the
backend adds the viewport's origin) and keeps the two window-shaped callbacks, `windowResized` and
`windowMoved`, for itself: a drag of *this* frame resizes *this* X window. `ScreenSession` keeps what
belongs to the screen - the framebuffer, the cursor notifications, the connection - and the list of
windows showing it. The rules the tick followed: one frame per mapped non-override-redirect top level;
a menu gets none (and a client that has mapped only menus gets one frame showing them); a frame follows
its own window and no other; closing a frame asks *that* window to close through `WM_DELETE_WINDOW` and
leaves the session alone; and the whole-screen frame of single-window mode keeps the old
"largest mapped top level" rule for its title, full-screen state and raise. Positions are remembered
per window, a client's first keeping the plain `WM_CLASS` and later ones carrying a cascade number.

**Why it was reverted, and this is the measurement that matters.** A frame shows a rectangle of the
**screen**, and there is one framebuffer per screen (A9) - so two frames of one client are two views of
one surface, and where the client's windows overlap, the upper window's pixels are in the lower one's
rectangle too. Measured live on EasyShell in per-client mode: with its 工具 dialog open, the menu-bar
shows two windows as intended (`Main | EasyShell主页 | 95,174 1300x728` and
`Main | 工具 | 138,113 1050x628`), and a screen capture of the **main window's own area** is filled
with the *dialog's* content - the connection-scan table and its buttons - with the main window's sidebar
and welcome page only at the edges. The dialog is shown twice, and the window under it looks taken
over by it. That is exactly what `docs/design-notes.md` predicted when it rejected this model
("per-window native windows would either lose dialogs or cost a second rendering model"), and the
measurement now says the prediction is right.

**This was written when C8 was believed to be the next item; C8 is now declined (see its entry).**
The alpha store (2026-09-26) was the
first half; the **general** store - every window keeping its pixels, `XWindow.ownPixels`, composited
from it and read back from it. It was built for C8's frame per top level - the one thing the union
frame could not show was a window's own content where a sibling covers it - and C8 is now declined, so
that is not what it is for. What it is for is everything else it changed: a translucent window, a
reveal that needs no `Expose`, and a window that answers with its own pixels.

**Traps the prototype cost, each one a way to get it wrong quietly** - worth having before the code is
written again:

- **A resize request belongs to a window, not to a screen.** Reading the requested size off the
  session's input state - which is where it had always been - applies a drag of one frame to every
  window the client has. `ScreenResizeTest`'s "the screen did not grow" was the first sign.
- **The whole-screen frame has no X window of its own**, so the "largest mapped top level" rule has to
  survive for it: without it single-window mode loses its title bar, its full-screen state and its
  raise (`WindowStateTest`: five failures, every one of them `expected: <true> but was: <false>`).
- **A reported position must not be cleared as it is read.** The move-settle writes a position only
  after seeing the same one on two ticks, so consuming it leaves nothing to compare against and no
  position is ever written (`HostWindowPositionTest`'s "the move was never remembered").
- **The master window has to be opened by `start()`**, not by the first tick: it is what the tray and
  the clipboard present through, and a test that never runs the display loop (`TrayBridgeTest`) saw no
  backend at all ("the backend never started").
- **A frame whose client window is destroyed goes with it.** The per-connection window used to stay
  with its last frame; a frame around a window that is gone is a stale picture
  (`PerClientWindowTest`'s idle assertion, rewritten while the prototype was in).

### [x] C9. Tray icons in the macOS menu bar. **M** - done 2026-09-26: the server is the tray host, and the icons are shown in the menu bar

**Current.** Nothing. A client that wants a notification-area icon (Java's `SystemTray`, Qt's
`QSystemTrayIcon`, GTK's `StatusIcon`) looks for a `_NET_SYSTEM_TRAY_Sn` selection owner and gives up
when there is none.

**Why it matters.** Any application with a tray icon (chat clients, sync tools) silently loses that
part of its UI - and it is a visible "this is a real desktop" feature.

**Plan.** The tray protocol is ICCCM-shaped and small: the server (or the host window's Java code)
becomes the owner of `_NET_SYSTEM_TRAY_S0`, publishes `_NET_SYSTEM_TRAY_OPCODE`/`_VISUAL`/
`_ORIENTATION`, hears `SYSTEM_TRAY_REQUEST_DOCK` with a window id, and from then on *is* the tray
host for that window. The docked window is an ordinary X window: reparent it (or show it in a
`Stage`), forward its clicks, and expose the image in the macOS menu bar (JavaFX/AWT has no menu-bar
API; this needs JNI or a native helper, which is what makes it M rather than S).

**Traps.** Reparenting a client's window into a host window changes its coordinates and its
`ConfigureNotify`s; the tray window expects the tray host to resize it to the icon size it asked for.
A client that docks is usually also the one drawing the icon and its own context menu - the menu is a
popup on the X screen, so it must appear where the *host* icon is, not on the X screen.

**Verify.** A Java application using `SystemTray` (`xeyes`-style, or a raw client that sends
`SYSTEM_TRAY_REQUEST_DOCK`) shows up; its context menu opens at the menu-bar icon.

**Done 2026-09-26, and the item's own premise was wrong in a way that made it cheaper.**

**No JNI is needed.** The plan said "JavaFX/AWT has no menu-bar API; this needs JNI or a native
helper". JavaFX has none, but **AWT does**: `java.awt.SystemTray` has been implemented on macOS for
years and puts its `TrayIcon` in the menu bar. That was measured before anything was written - a
plain Java process added an icon and a screenshot showed it - and it is what the whole item rests on.

`tray/TrayBridge` is the tray host: it owns `_NET_SYSTEM_TRAY_S0` with a window of the server's own,
publishes the three properties a client reads before docking (`_NET_SYSTEM_TRAY_OPCODE`,
`_ORIENTATION`, `_VISUAL`), hears `SYSTEM_TRAY_REQUEST_DOCK`, reparents the icon window into a tray
window of its own, sends the XEmbed message that says so, and copies the icon's pixels into the menu
bar every 200 ms - there is no X event for "the icon was redrawn", so the pixels are read out of the
framebuffer the client draws into. A click on the menu-bar icon is delivered to the icon window as a
synthetic press and release, which is how every tray host does it. `--no-tray` turns it off, and a
server with no window (headless, VNC) never starts it: a viewer has its own desktop, and a menu bar
that is not on this machine is not something an icon can be shown in.

**The selection is taken at startup, not when a window appears**, and that is not an implementation
detail: an application asks whether a notification area exists while it is starting up and gives up
for good if the answer is no, so a tray owned only once something has been drawn is owned too late.

**The real client found a real bug in this server, and it is the interesting half of the item.**
Java's tray peer creates its icon window, sends the dock request and then **blocks for five seconds**
waiting to be told it has been embedded; when it gives up it throws `TrayIcon couldn't be displayed`.
What it waits for is a `ReparentNotify` on its *own* window - and `WindowHandlers.reparent` delivered
that event to the *new parent* only. Measured by removing the wait's cause: with the reparented window
told as well, the same client prints `added` where it had thrown. That is a protocol gap the tray
found and not a tray-specific hack: X11 says the window that was moved hears about it, and this
server had only ever told the parent.

**Two differences from a panel, both forced by what the screen is here, and both in the design
notes**: the icon is shown in the menu bar *and* stays where the client drew it (this server has one
surface, so a window off the screen has no pixels to read), and a client's own context menu opens on
the X screen where its window is rather than under the menu-bar icon (the menu is a window the client
places itself, and no tray host on this architecture can move it).

**Measured 2026-09-26**, three ways:

- `tray/TrayBridgeTest` (5 tests, a recording backend so nothing touches the real menu bar): the
  selection is owned and the three properties say what they should (the opcode property's value is
  the atom a dock request must carry, the orientation is horizontal, the visual is not zero); a docked
  icon reaches the "menu bar" as the *client's own pixels*, read out of the framebuffer; a click
  arrives at the icon window as a press and a release at the place it was clicked; an icon whose
  window is destroyed leaves the tray and the menu bar is told; and the guard that a backend with no
  menu bar has no tray at all. **Three of the five fail when the dock request is ignored** (the other
  two are guards).
- **Live, with a real Java client** (`SystemTray`, an AppImage-less `TrayClient` on the VM): it prints
  `systemTraySupported=true` and then `added` - it docks, and **a screenshot of the Mac's menu bar
  shows its icon**, a blue disc drawn by the client, next to the status items. Before the
  `ReparentNotify` fix the same client threw `TrayIcon couldn't be displayed`.
- `mvn test` 312/312.

**Every Java application's icon was a blank square, and that is the second real bug the tray had
(2026-10-02).** Reported as "termora's tray icon is not right". Measured with the window tree of a
running `java.awt.SystemTray` client on the VM against a server on this machine:

```
0x800021 "Content window" (TrayApp)                 24x24+0+0     <- the window the client docked
  1 child:
  0x800029 "sun-awt-X11-XCanvasPeer" (TrayApp)      24x24+0+0     <- the image is drawn here
```

A real toolkit does not draw into the window it docks - it paints the image into a **child** of it.
`TrayBridge.capture` read the icon window's *own* pixels only, which is what B5 says a window's store
holds (its own pixels, not its children's), so what reached the menu bar was that window's empty
background: a white square where the icon should be, for every Java application. The capture now uses
`XWindow.pixelFromInferiors` - the same read `IncludeInferiors` gives a client, the deepest window
covering the point in its own alpha, which is what the icon looks like - and keeps the two earlier
fallbacks (the icon window's store, and the screen for a client that ignored the published visual).

**Verified live, the same way the icon was found**: a minimal `SystemTray` client on the VM, a server
on this machine, and a screenshot of the menu bar - before, a plain white square; after, the icon's
white background with its red disc. `TrayBridgeTest.anIconDrawnByAChildWindowReachesTheMenuBarToo`
is the same thing without a display: an icon with a green background and a child that draws red -
without the fix it reads the green background (measured: `expected: <16711680> but was: <65280>`).

**And this is why "it is the same with Xvfb + x11vnc" is not the same thing**: **Xvfb owns no tray at
all**, and `x11vnc` does not provide one either, so a client's dock request finds nobody and its icon
never appears there - expected, and a different failure from this one. This server *is* the tray host
(C9), which is why the bug was ours and worth fixing.

**The click, measured again 2026-09-26, and it found a real bug in the bridge.** Two things were
established and one is still open:

- **The platform does deliver a menu-bar click to AWT.** A standalone probe - a `TrayIcon` with a
  mouse listener and nothing else - printed `CLICK button=1 at=1060,12` when its icon was clicked.
  The earlier "the client kept exiting" was the *client*, not the platform.
- **The coordinates are screen coordinates, and the bridge assumed they were its own.** AWT reports a
  status item's click in screen space, and a status item's own geometry is not something it exposes;
  the first version of `TrayBridge.clicked` looked the position up in the strip of icons it had
  composed, found nothing for x=1060 on a strip 24 wide, and sent the client nothing. A click now
  falls back to the icon itself (its centre) when the position is outside the strip, which is what a
  tray icon means anyway. `TrayBridgeTest` still passes, including the in-strip case.
- **Still not shown end to end**, and the reason is now the *client*: the Java tray client on the VM
  aborts with libxcb's `xcb_xlib_threads_sequence_lost` assertion when the synthetic event arrives -
  AWT and Xlib on two threads, which is the client's own problem and not this server's. Worth doing by
  hand with a client that does not do that (termora's terminal, or a GTK application).

**Fixed 2026-09-26 with the per-window store (B5).** `_NET_SYSTEM_TRAY_VISUAL` now publishes the 32 bit
ARGB visual, the client's icon window is made with it, and such a window keeps its own pixels and is
composited - so a transparent corner stays transparent. Measured in process:
`TrayBridgeTest.anIconWithAnAlphaVisualKeepsItsTransparentCorners` asserts the composed menu-bar image
has a transparent corner and the disc's colour at its centre. **Live, the icon reaches the menu bar but
a screenshot could not confirm its shape**: the icon is docked and handed over (the server logs "an
icon is in the menu bar now"), and it goes away with its client, but it was not found in the captures -
and an A/B with the 24 bit visual could not find it either, so that is the tool, not the change. The
record below is what the item said before the store existed.

**The detail the live check showed, when the icon was still opaque**: it was a *square with a light
background* rather than a cut-out disc, because `_NET_SYSTEM_TRAY_VISUAL` published the root's 24 bit
visual - so a
client's icon window has no alpha and its transparent corners arrive opaque. ~~Publishing a 32 bit ARGB
visual would fix the shape, and is a small item of its own.~~

**That last sentence is wrong, and it was measured rather than argued (2026-09-26).** The fix it
proposes was designed and then abandoned: **publishing a 32 bit visual cannot give the icon its alpha
back, because the alpha is thrown away before the tray ever reads the pixels.** Three facts settle it,
and each is in the code rather than in a guess:

- A window's drawing goes straight into the **screen's** framebuffer, and `Framebuffer.opaque()`
  forces every pixel stored there to `0xFF……` - deliberately, because a screen has no alpha
  (`Framebuffer.opaque`'s comment says so: `GXclear` and `GXinvert` produce values with a zero top
  byte, and a backend that honours alpha then draws holes). An ARGB32 window's drawing therefore
  *reaches* the framebuffer with its alpha already gone.
- `TrayBridge.capture` reads that framebuffer, and forces the byte itself a second time
  (`framebuffer.pixel(x, y) | 0xFF000000`), which is belt and braces over the same thing.
- The only other store a docked window has is A9's backing store, and `XWindow.buffer()` allocates a
  `Framebuffer` too - the same opacity rule, for the same reason.

So the shape a client draws with alpha only exists where a **per-window ARGB surface** exists, and
there is none: that is B5's architecture (a store per window, and the composite to the screen done in
the tick), not a one-line property change. Publishing the 32 bit visual without it would be worse than
doing nothing, because the client would then draw its icon with a transparent background and the tray
would show the *screen's* colour through the holes rather than the icon's shape.

The item stays where it is until B5 lands; what is recorded here is that its remaining part is B5's and
not a small item of its own.

**Traps.**

- **The dock message's words are not where they look.** `data[0]` is the timestamp, the **opcode is
  `data[1]`** and the window is `data[2]`. Reading the opcode out of `data[0]` makes every request
  look like "dock window 0x0" - a warning in the log and an application that reports it cannot show
  its icon, which reads as the tray ignoring it rather than as a message understood differently. Java's
  peer sends it the second way and is what the shape was measured against.
- **32 bit property data is stored in X11's order, which is big endian** - the client swaps what it
  has to. A server-side writer that stores little endian is read correctly by a hand-written probe
  and wrongly by Xlib.
- **A status item is not where you think it is.** Two screenshots with no icon in them "proved" the
  feature broken; the icon was there, at the left of the status area, outside both crops. What settled
  it was comparing a capture with an icon against one without - and the cheapest check of all is a
  second, unmistakable icon (a fixed red disc) beside it.

---

### [x] C10. Input methods. **M** - done 2026-09-26: both halves verified live, and the client-side one needed three font-path fixes rather than an environment

**Current.** Unknown - and that is the item. An X input method (fcitx/ibus with the XIM protocol, or
client-side modules) runs as an X *client* and talks to applications through the `XIM_SERVERS`
selection and X properties, which this server implements (selections work - see C4). Typing Chinese
through the *host's* IME into the host window is a different path: the JavaFX key events carry the
composed characters of a dead-key sequence only if the platform delivers them, and nothing forwards
the host IME's candidate window into the X session.

**Why it matters.** The author writes Chinese; a forwarded terminal that cannot be typed into in
Chinese is not usable for real work, and this is the kind of gap that is discovered at the worst
moment.

**Plan.** First verify: run `fcitx5` (or `ibus`) on the VM against this server and type into a
forwarded `xterm`; the XIM handshake (`XOpenIM`) and the conversion must work, and if they do, this is
a documentation item ("run an X IME on the client side"). If they do not, the failure is in the
selection/property path and is a bug to fix. The host-IME bridge - macOS IME composing into the
forwarded window - is a much larger feature (the IME's candidate window is native UI); decide
explicitly whether it is in scope.

**Verify.** Chinese (and Japanese/Korean) input into a forwarded `xterm` and into termora's terminal
with an X IME running on the VM; the same against Xvfb as the reference for "the IME itself is fine".

**Worked on 2026-09-24: the server half is implemented and tested; not ticked.** What a forwarded
application can be sent is a keycode, and a keycode cannot express a character no key carries - so the
platform's composed text has to become a keycode the server claims for it:

- **`InputState.keyTyped(char)`, reached from every backend.** The host's input method composes the
  text, the platform hands it to the backend, and the server gives a keycode no keyboard carries the
  character as its keysym (`0x01000000 | codepoint`, X's own spelling for a character with no keysym
  of its own), sends `MappingNotify` so the clients re-read the mapping, and presses that keycode. A
  character the keyboard *does* carry is left alone - the key press already delivered it - and the
  keycode is claimed once and kept, because a mapping change per keystroke would be a mapping change
  per keystroke. Platform-independent by construction: every toolkit reports composed text the same
  way, and everything after that is the same X protocol.
- **`Keymap` owns the whole mapping again.** It had grown a second table in `InputHandlers` for a
  client's `ChangeKeyboardMapping`, so two places could disagree about what a keycode means.
- **A real bug fell out of the tests**: the server's own synthetic keycode counted as "a key that is
  on the keyboard", so the *second* time a user typed the same character it was swallowed. Typing the
  same character twice is two keystrokes.
- **JavaFX needs two hooks, and the second is the whole feature on a Mac - measured.** `KEY_TYPED`
  does not carry what an input method committed: the first live attempt produced nothing at all. It
  arrives as `Scene.setOnInputMethodTextChanged`'s committed text, now wired up beside it. AWT needs
  only the one, as `keyTyped`.

`input/TypedCharacterTest` (4 tests): the character reaches the client as a key press whose keycode
answers `GetKeyboardMapping` with it; the mapping change is sent; a character the keyboard carries is
not sent twice; the same character can be typed twice; one keycode is claimed and kept.

**Why it is not ticked: the live check could not be made to happen here, and not because of the
server.** A key event cannot be injected into the host window from a script in this session -
`osascript`'s `keystroke` fails on the process lookup, and a `CGEventKeyboardSetUnicodeString` event
from `tools/host-type.c` (which reports "typed 2 characters") reaches nothing: the observer on the X
side sees no key press at all. The X half is covered by the tests, so what is unverified is the two
lines between the platform's input method and `input.keyTyped`. Finishing it needs a hand on a
keyboard: start the server (`--single-window`, for the reason below), run an application that shows
text, and type Chinese with the host's own input method.

**And the client-side input method needs a mode, which is now measured.** An input method draws its
pre-edit text and candidate list in a window of its own placed over the application's caret - and in
the default one-window-per-client mode every client gets a screen of its own, so that window is drawn
in the *input method's* host window instead. The protocol half survives the split (selections,
properties and sent events are server-wide), the pixels do not.
`PerClientWindowTest.aClientThatIsNotAnApplicationGetsAWindowOfItsOwnToo` drives exactly that: two
connections, the second mapping an override-redirect window over the first client's window, and it gets
a host window and a screen of its own. **So an X input method belongs in `--single-window` mode.** The
same reasoning covers a window manager and a clipboard manager.

**The live check is blocked on the VM, not on the server**: that machine has no `dbus-launch` and no
D-Bus session bus, and `apt-get` has no network, so `ibus-daemon -drx` starts and exits without a word.
The remaining work is: install `dbus` (or run the input method from a desktop session), start `ibus`
against a `--single-window` server, and type into a forwarded application - which the VM has no
`xterm` for either, so the client would be termora's terminal. The host-IME question - macOS composing
into the forwarded window, which is what the work above is about - is answered; the client-side one
needs that environment.

**Re-measured 2026-09-26, and the host half now *is* verified live - the item's "could not be made to
happen here" was a measurement failure, not a feature missing.** The recipe, which is worth keeping
because two of its three steps are easy to get wrong:

1. **`tools/host-type.c` works.** The item recorded that a character posted with it "reaches nothing:
   the observer on the X side sees no key press at all". It does reach: with the host window in front
   and a client holding the X focus, `/tmp/type "你好"` produces, **per character**, a `KeyPressEvent`,
   a **`MappingNotifyEvent`** and the synthetic keycode's press and release - in the server's own log.
2. **Grepping the log for the wrong strings is how it looks like nothing happened.** The first run of
   this check "found nothing" because it was greped for `hostEvent`/`keyTyped`, neither of which the
   event dispatcher prints; what it prints is `event KeyPressEvent -> client[…]`. The measurement is
   the log line, not the absence of one.
3. **What the client sees is a keycode, and the character comes from `GetKeyboardMapping`.** A small
   client that reads the mapping after the typing answers it exactly: **keycode 255 carries U+597D**
   (好) - X's own spelling for a character no key has, `0x01000000 | codepoint`. That is the whole
   path: the host composed it, the server claimed a keycode and said so, and a client turns the
   keycode back into the character.

   **Corrected 2026-10-08**: that keycode was **255**, and a real toolkit cannot resolve it - the
   character never appeared in a forwarded application at all (A53 has the measurement and the three
   fixes, of which this is one). The claim is now the **lowest** keycode nothing carries, and `你好`
   reaches an EasyShell text field typed through the Mac's Pinyin source. The probe was satisfied
   either way, because what it measured was the *mapping*; a toolkit resolves the keycode through the
   map the server advertises, and the far end of the range is a keycode its own tables can leave
   without an entry. That is why this stood as "verified live" for a month.

A second route was measured on the way and works too, which matters when the window is awkward to
focus: **`CGEventPostToPid`** with the unicode string attached delivers to a given process what
`CGEventPost(kCGHIDEventTap, …)` delivers to the frontmost window. `tools/host-type.c` posts to the
tap; posting to the server's pid is what a script should do when the window cannot be brought
forward.

**Still open, and it is the client-side half**: no X input method can run on the VM as it is (2), so
"an application typed into through an IME running *on the client*", which is the other reading of the
item's title, has not been exercised. The host's side - the one the author actually needs, since the
Mac's own input method is what types Chinese - is done and verified.

**The VM's environment changed, and the client-side half was attempted - 2026-09-26.** The paragraph
above says "no input method can run on the VM"; that was true when it was written and is not any
more: `apt-get update` now works on it (670 kB/s from `ports.ubuntu.com`), and `dbus-daemon` and
`ibus` were already installed. `apt-get install dbus-x11 xterm ibus-libpinyin` finished the job, a
D-Bus session and `ibus-daemon -drx` start, and **`XIM_SERVERS` is set on this server's root** - the
input method registers, and a client that reads the property finds it.

**And that is where a real bug turned up, in this server, after all.** `xterm` on Xvfb creates its
XIM input context and runs; against this server it printed

```
xterm: Preparation of font set "fixed" for XIM failed.
xterm: Failed to create input context
```

so no XIM client could work here regardless of the input method. Measured with a `ctypes` call to
Xlib itself, the same way A4's cause was found - the request stream is byte-identical against both
servers, and the answers are not:

```
                         Xvfb                                  this server (before)
XListFonts("fixed")      2 names, both "fixed"                 1 name, "fixed"
  XCreateFontSet         OK, missing GB2312/GBK/ISO10646-1      NULL, missing ISO8859-1 (twice) and those three
ListFontsWithInfo         the fonts' own XLFD names              the name the pattern matched: "fixed"
```

**One real divergence was found and fixed, and it is not the whole cause.** `ListFontsWithInfo`
answered with the name the pattern *matched*; a reference server answers with the font's **own**
name. Measured: Xvfb answers `fixed` to `ListFonts("fixed")` and
`-misc-fixed-medium-r-semicondensed--13-120-75-75-c-60-iso8859-1` to
`ListFontsWithInfo("fixed")` - the name in `fonts.dir`/`fonts.alias` is what `ListFonts` matches and
reports, and the XLFD the font carries is what the info replies carry. Those are the two names a font
path has, and this server used one for both. `XFont.realName()` now answers the font's own name and
`ResourceHandlers.listFontsWithInfo` uses it for the reply's name and for the `repliesHint`
arithmetic; `BitmapFontTest.anAliasedFontIsReportedUnderItsOwnName` pins both halves (either alone
passes against a server that answers the same name to both) and **fails without the change**
(`expected: <[-Misc-Fixed-…-ISO8859-1]> but was: <[fixed]>`). It matters on its own account - the
XLFD's last two fields are where a client reads a font's charset from - so it is committed as a
fidelity fix.

**It was not sufficient, and the two remaining halves were found the same way - done 2026-09-26.**

With the name fixed, `XCreateFontSet` still answered NULL, so the difference was elsewhere in the same
gesture. A proxy that logs *every* request (`tools/font-reply-proxy.py`'s sibling, written for this)
showed what Xlib actually does, and it is three requests deep: it asks `ListFonts("fixed")`, then
`ListFontsWithInfo("fixed")` to learn the font's real name, and then **`ListFontsWithInfo` for that
name** - and *that* request came back empty here, because the XLFD name was not in the font list at
all. The server was telling a client the name of a font it could not then find.

Measuring further with `XGetFontProperty` (the same `ctypes`-at-Xlib approach) turned up the second
half: loading a font *by its own XLFD name* here landed on the Java approximation, whose font has
**no `CHARSET_REGISTRY`/`CHARSET_ENCODING` properties at all** - the reference answers
`ISO8859`/`1` to both and this server answered neither - and Xlib reads a font's charset from exactly
those two properties. A *pattern* (`-misc-fixed-*-iso8859-1`, which is what `XCreateFontSet` builds
out of a base name and the charset it is looking for) had the same fate.

Three fixes, all in the font tables:

- `BitmapFont` indexes every font by its own XLFD name as well as by its alias, so a font this server
  *names* is one it can *load*;
- an X font pattern is resolved to the first table font it matches (a fixed order, so it resolves the
  same way twice);
- `ResourceHandlers`' font list carries those XLFD names, so the name a `ListFontsWithInfo` reply
  hands out is one the server will find when the client asks about it.

**Measured end to end, and this is what the item was for.** On the VM with `ibus` and `xterm` (both
installed for this): before, `xterm` printed `Preparation of font set "fixed" for XIM failed` and
`Failed to create input context`; after, it starts with no message at all, and `nihao` typed through
ibus - the trigger and the letters sent by XTEST, the candidate taken with a space - **commits 你好
into the xterm**, captured on screen. The probe agrees with the reference now:

```
XCreateFontSet("fixed")   Xvfb: OK, missing GB2312.1980-0, GBK-0, ISO10646-1
                          here: OK, missing the same three       (before: NULL, missing five)
```

`BitmapFontTest.aFontIsLoadableByItsOwnNameAndByAPattern` pins the three lookups and **fails without
them** (`expected: <[-Misc-Fixed-…-ISO8859-1]> but was: <[]>`). `mvn test` 328/328, and both live
clients were looked at again after the change: EasyShell's context menu survived the six out-and-back
passes 6/6 and termora's 6/6 with the two captures byte-identical.

**So the item's framing was wrong in a way worth keeping**: "an X IME running on the client is a
documentation item" was written from a probe that could not distinguish "the IME is fine" from "the
font set is not". It was not a documentation item - three real bugs sat between a client-side input
method and a working one, and all three were invisible to every test written from this server's own
side, because the server agreed with itself in each case.

**The two ways to type Chinese, and which one a given setup can use (2026-10-02).** Reported as
"EasyShell and termora do not accept Chinese when the Mac's input method is switched on, and the same
happens with Xvfb + x11vnc + Finder". The *same symptom* there is expected and has a different cause on
each side:

- **The host's input method cannot reach a forwarded X client through VNC at all.** macOS composes
  the text for the application that has the *Mac's* focus - the Screen Sharing window - and RFB's key
  events carry keysyms, not composed text, so nothing arrives at the remote application. Xvfb+x11vnc
  behaving "the same" is therefore the reference confirming the mechanism, not a second bug.
- **This server does bridge the host's composing** (`InputState.keyTyped`): the composed character
  becomes a keycode the server claims for it, with a `MappingNotify` so clients re-read the mapping -
  verified live by typing 你好 with `tools/host-type.c` and reading the mapping back (keycode 255 =
  U+597D). A *toolkit* has to honour that mapping change for the character to appear; whether JavaFX
  and AWT do is a property of those toolkits and is not something this server can force.
- **The path that works in every client is an input method on the client machine**, and it needs four
  things, none of which is a server bug:

  ```sh
  # on the client machine, against this server's display
  dbus-launch --exit-with-session &      # ibus needs a session bus (the VM has one now)
  XMODIFIERS=@im=ibus ibus-daemon -drx & # -x is the XIM bridge, -r replaces, -d daemonises
  ibus engine <engine>                   # a Chinese engine: ibus-chewing is installed, ibus-libpinyin
                                         # is what pinyin wants (apt-get install ibus-libpinyin)
  DISPLAY=<server> XMODIFIERS=@im=ibus <application>   # GTK/JavaFX: GTK_IM_MODULE=ibus as well
  ```

  **and the server must run `--single-window`.** An input method draws its pre-edit text and candidate
  list in a window of its own placed over the application's caret; in the default mode every client
  gets a screen of its own, so that window is drawn in the *input method's* host window instead of
  over the application - measured, and `PerClientWindowTest.aClientThatIsNotAnApplicationGetsAWindowOfItsOwnToo`
  is what says so. With those four in place the project's own verification is ibus composing `nihao`
  and committing **你好 into a forwarded `xterm`** (above), and that was this server, not a reference.

The next session that has a hand on the keyboard can close the last gap in one line: run
`--single-window`, start EasyShell or termora with `XMODIFIERS=@im=ibus` and a pinyin engine, and type -
if the candidate list appears over the application and commits, the recipe above is confirmed for the
two applications the author actually forwards.

---

### [x] C11. A server whose window cannot be opened. **S** - done 2026-09-25: the hang, the silence and the end of the fallback chain

**Found while doing E2**, which is about running the suite on a machine with no display, and it is
worse than the CI problem that led to it. With the JavaFX backend and no display - measured with
`-Djava.awt.headless=true` on macOS - `QuantumToolkit.startup` parks on a latch whose count never
reaches zero, because the render thread failed first (`ArrayIndexOutOfBoundsException: Index 0 out of
bounds for length 0`, an empty screen list). Nothing in the toolkit's API reports that: no exception,
no return value, no timeout. The tick thread calling it never came back, so:

- no frame was ever shown again, and the last line in the log was
  `nothing to show on display :95: the window holds its last frame`;
- the request threads went on answering clients, so the server looked healthy from the outside;
- the only explanation was a stack trace from a render thread, printed once.

**What changed.** `JavaFxBackend.startupToolkit` starts the toolkit on a daemon thread and waits
**ten seconds** for the FX thread to run the startup's own runnable, which is the one honest signal
that JavaFX works; a wait that ends without it throws with a message that says what happened and what
to do instead (`--backend headless`). And `XServer.tick` catches a backend that refuses to start,
logs one line and stops the server, because a display server that cannot display has one honest thing
to say - `HostWindowFailureTest` (2 tests) pins both halves of that: a backend that cannot open a
window stops the server and lets its client go, and a server nobody has mapped anything on is
untouched by the refusal it never meets. **The first fails with the guard removed.**

**Measured 2026-09-25**, live: with `-Djava.awt.headless=true` and a client that maps a window, the
server used to hang for ever; it now logs

```
WARN  i.g.o.x.BackendFactory - JavaFX is not usable (java.lang.IllegalStateException: the JavaFX
      toolkit did not start within 10s: this machine cannot show a window, so nothing would be
      forwarded. Run with --backend headless to serve clients without one.), falling back to the
      swing backend
```

**The second half of the finding, closed 2026-09-25.** The chain used to end at the Swing backend
whatever the machine was, and a Swing window that cannot be opened fails on the event dispatch thread
- out of the caller's sight - so on a machine where AWT is headless too the server ended up serving
clients with a backend that would never show anything and no line saying so. `BackendFactory` now
asks `GraphicsEnvironment.isHeadless()` before either toolkit: JavaFX failing there, or Swing being
asked for on such a machine, gives the **headless backend** and one line that says what happened.
Measured with `-Djava.awt.headless=true`: the server logs

```
WARN BackendFactory - JavaFX is not usable (... did not start within 10s ...): nothing can be shown
     on this machine, so the server is running headless - clients are served and no window is opened
```

and then serves clients normally (a FocusProbe run against it completes), where before it went on with
a Swing backend that could fail invisibly. `mvn test` 260/260.

## 5. D - Large / product-level

### [x] D1. Session persistence and re-attach. **L-XL** - both halves done 2026-09-25; only the
across-a-server-restart step is not attempted, which the item itself rules out

**Current.** The session ends with the host window: closing it sends `WM_DELETE_WINDOW` to the clients
that asked for it, waits `--close-grace-ms`, then disconnects them. A client that dies takes its
windows with it. There is no way to leave the session running without a window and come back to it.

**Why it matters.** It is the difference between "a display" and "a place where work survives"
(`xpra`/`x2go`/`tmux`-for-GUIs). For a user who forwards a long-running application, losing it when
the window closes is the most painful failure mode.

**Plan.** Two halves, and both are needed: (a) keep the X server and its clients alive with no host
window (the screen keeps existing; clients keep drawing; nothing is presented), and (b) let a new host
window attach to a running screen, at the framebuffer level (`setViewport` already exists) and
eventually per client. Persisting across a *server* restart is a third, much larger step (the
framebuffer would have to be journalled and the clients' connections re-established, which plain X11
cannot do - the realistic answer is "the server stays up, in the background").

**Done, both halves (2026-09-25).**

- **`--detach-on-close`**: the window closes, the session does not. The clients keep being served -
  their requests are answered, their keys repeat, their pixels land in the framebuffer - and nothing is
  presented, because there is nothing to present to. Off by default, so the behaviour this server has
  always had (close the window, the application was in it) is what a user gets unless they ask for the
  other one.
- **`--control-port N`** is how a window comes back: one command per connection over loopback, `attach`,
  `detach` or `status`, answered in one line. `detach` works on its own too - it is how the window gets
  out of the way without the session ending.
- **What the plan called "eventually per client" is not done, and is not needed yet**: `attach` opens a
  window for every detached session, and since there is one host window per session that is the same
  thing a user means. A command naming one session is the next step if a server ever shows several.

**Traps, all three measured rather than argued.**

- **"Detached" has to be a state of its own, not "the backend is null".** The tick opens a window
  whenever a session has none and its client has drawn something - that is how a client that only asks
  questions avoids a window - so a detached session that was only "a session without a backend" had a
  window opened for it again on the next frame. The tick's guard is the flag, and it is also where a
  window that is still up gets closed (after the lock is released, because closing one waits on the
  toolkit's thread).
- **C2's raise must not fire with no window**, and does not: detaching clears `raisePending`, so a
  client that maps a new top level while nobody is watching does not queue a raise that would fire at
  whatever window comes next.
- **The tick does not need an idle state, and that is a measurement.** Measured on a real server with
  an idle client: **1.4-2.1% of a core detached against 1.4-2.3% attached**. The per-frame work is the
  bookkeeping - which window is the identity, what the viewport is, taking the damage - and it runs
  either way; presentation was never where the cost was. So the item's "can idle to save CPU" is
  answered rather than implemented.

**Measured 2026-09-25**, three ways:

- `test/SessionDetachTest` (4 tests, a recording backend, real connections): closing the window with
  the flag on keeps the client served (a rectangle it draws comes back out of the framebuffer) and
  **no window is opened for it again**; attaching over the control port opens one and its first frame
  is **the whole viewport** rather than the damage; the control port's own `detach` and `status` do
  what they say; and the guard that a session which was never detached is not attached twice. **Two of
  the four fail with the `--detach-on-close` branch disabled** and a third pins the control port
  independently.
- **Live, on the Mac with a real window and EasyShell**: the window's close button was clicked
  (`tools/host-drag.c` with both ends on the same point is a click), the log said
  `window closed and --detach-on-close is on: 2 client(s) keep running with no window`, the host window
  disappeared from `tools/host-windows.c` and `status` read `windows=0 detached=1 clients=2` - while
  EasyShell's own windows were still mapped and answering. `printf 'attach\n' | nc -w 2 127.0.0.1 6200`
  opened a new window, and a screenshot of the Mac showed **EasyShell's interface exactly as it was**
  (the welcome page, the host list) - which is the whole-viewport frame doing its job, since the damage
  that drew it had been consumed while there was no window.
- `mvn test` 272/272.

**Found on the way, and fixed**: `HostWindowPositionTest.aWindowOpensWhereItWasLeftLastTime` called
`.awaitWindow()` and then `window.input()` without `.awaitInput()` - the other tests in the file do both,
this one did not, and the gap is the few microseconds between a backend being handed to a session and
being started. It passed for a week and failed once under a full suite's load, which is what a race
that narrow looks like. One line, and the file's own helper was already there.

---

### [x] D2. Built-in remote transport. **L-XL** - done 2026-09-26: RFB over TCP and over a WebSocket, raw and ZRLE frames, the Cursor pseudo-encoding, a password or open, and the clipboard both ways. Tight's JPEG method was added on 2026-10-01 (A32); Tight's zlib path and `CoRRE` are optimisations left undone

**Current.** The framebuffer is presented by three backends: JavaFX and Swing windows on the machine
the server runs on, and a headless one that presents nothing. To watch a screen from elsewhere the
user runs `x11vnc` on the *client* machine (which is what the test bed does today on the VM).

**Why it matters.** "Forward one application, or a whole desktop, to another machine or a browser" is
the natural product shape of this codebase, and it needs no client-side software beyond a browser -
which is exactly what the author's own test bed is missing today (an `Xvfb` + `x11vnc` stack on the
VM exists only to *compare*).

**Plan.** A fourth backend: an RFB (VNC) or WebSocket+canvas presentation of the same framebuffer, on
the same tick. The damage rectangles and the viewport the two backends already use are exactly the
presentation contract; a `DisplayBackend` that writes into an RFB framebuffer-update encoder is the
smallest honest version (raw encoding first, then `ZRLE`/`Tight` if bandwidth matters). Input comes
back as RFB pointer/key events into the same `InputState`.

**Traps.** Bandwidth: raw 1300x800x4 per frame is 4 MB - the damage rectangles and a dirty-region
update are what make it usable, and `--scale`/colour-depth reduction are the knobs. Input mapping
needs a reverse of `screenCoordinate`. Authentication: RFB's own (`VNC`/`VncAuth`) or the server's
`--auth` (A7) - and A7 must be fixed before either is offered on a network.

**Done 2026-09-25: `--backend vnc`.** `display/VncBackend` is a `DisplayBackend` with no window on
this machine, and `display/RfbSession` is the protocol: RFB 3.8 (3.3 and 3.7 are answered in their own
terms), security `None` or the VNC scheme, **raw encoding only**, true colour only, the client's own
pixel format honoured (8, 16 or 32 bits, either byte order, any channel maxima - a 565 client gets
565), `DesktopSize` for C1's screen growth, and pointer and key events into the same `InputState` the
other backends feed. `--vnc-port` (5900 by default), `--vnc-password` (a value, for an embedder)
and `--vnc-password-file` (a line, for a shell). It implies
`--single-window`: one screen is what a viewer watches.

- **It is request-driven, and that is the whole design.** Nothing is sent until a client asks, and the
  socket is written from that client's own thread, so a viewer that stops reading costs nothing and
  no other client waits for it - the protocol's backpressure and this server's rule (E5) are the same
  thing here. `present` does nothing but add the damage to each session's pending rectangle.
- **A detached server and a VNC server are the same thing**, which is why D1 composed with this for
  free: `isHeadless()` is true and `isClosed()` is never true, so the display tick opens no window and
  the server runs until it is stopped.

**The trap the item predicted, and it was real.** "The damage rectangles are what make it usable" -
except the server marked the *whole screen* as damaged after every drawing request. A viewer's first
version re-sent four megabytes for a keystroke, and 1300x800 of "damage" for a mouse move. The fix
belongs under E3 ("blend only the damage region") and is done there: `Rasterizer` records the region
it actually wrote, and `afterDrawing` marks that instead of the screen. Two of the three things that
went wrong on the way are worth naming: **`afterDrawing` had a second, older whole-screen mark left
under the new one** (so the precision looked like it did nothing for one round of testing), and
**`ImageCodec` built its own rasterizer**, so a `PutImage` was drawn with no clip at all - over
whatever covered the window it went into - and recorded its drawn region nowhere.

**Measured 2026-09-25**, three ways:

- `display/VncBackendTest` (7 tests, a real socket and a hand-written RFB *client*, because somebody
  else's software is the thing this backend is for): a pixel an X client drew arrives in an update;
  the whole screen comes back for a non-incremental request and **the damage, not the screen**, for an
  incremental one - and an incremental request with nothing to send is answered with nothing at all
  rather than by spinning; a 565 client's format is honoured; a pointer press and a key reach the X
  client as the events they are (the keycode via `Keymap.keycodeForKeysym`); the right VNC password
  gets the screen and a wrong one is refused before it; and the DES itself is pinned against a vector
  computed with **openssl**, because the scheme's bit-reversed key is the part every implementation
  gets wrong once and no library does for you.
- `draw/DamageTrackingTest` (3 tests) is the other half and belongs to E3: for eleven primitive
  families - block fill, stippled fill, thin and wide lines, polyline with round joins, polygon, arc
  fill, arc outline, points, rectangle outline, text, `PutImage`, `copyArea` - **every changed pixel is
  inside the damage the request marked**, and the damage is smaller than the surface. A pixel changed
  outside it would be a pixel left stale on somebody's screen, with no error and no log line; that is
  what the test is for.
- **Live, and with two real clients** (2026-09-25/26): EasyShell connected to a `--backend vnc`
  server and drew its interface, and **noVNC** - the standard web VNC client, run in Chrome against
  the same server over a websockify bridge - showed it: the page's own title bar read
  `Connected to xserver :96` and the canvas carried EasyShell's interface, welcome page, host list
  and all. **Input went the other way too**: a click in that canvas moved the X pointer (measured
  with `xdotool getmouselocation` on the VM), and a hand-written RFB client sending a pointer event
  to (1000,700) and then (50,40) moved it to exactly those coordinates.
- **macOS Screen Sharing found a real gap, and it is the reason to use a real client.** It announces
  **RFB 003.003**, not 3.8 - captured on the wire (`RFB 003.003\n`) - and the 3.3 branch of the
  handshake had **no test at all**: 3.3 has no security-type list and no `SecurityResult`, so a
  server that answers a 3.3 client the 3.8 way leaves it waiting for a byte that never comes.
  `VncBackendTest` now pins it (`aClientThatSpeaksThirtyThreeIsAnsweredInItsOwnTerms`), and with the
  branch disabled that test **times out exactly the way the real client sat silent**. Screen Sharing
  itself completed the handshake and the DES authentication against this server three times (the log
  lines name the connection and the screen it was given); what it did *not* do in this environment was
  display the picture, and a control experiment says why that is not this server: **a minimal,
  by-the-book 3.3 server written for the occasion gets the same silence from it** (handshake, then
  nothing at all, and it does not even read a frame pushed at it), and the reference `x11vnc` behaves
  like this server under the same probe - nothing unprompted after `ServerInit`, frames as soon as one
  is asked for.
- **The browser path, and no bridge in front of it** (2026-09-26): `--vnc-ws-port N` serves the same
  sessions carried inside WebSocket frames (`net/WebSocketListener`, and `RfbSession`'s transport is
  now an interface with a socket and a WebSocket under it - the session above cannot tell them apart).
  noVNC in Chrome, pointed at `ws://127.0.0.1:6080` with **no websockify**, showed EasyShell's
  interface with the page titled `Connected to xserver :96`, and a pointer event sent over that
  WebSocket moved the X pointer to exactly the coordinates it named. `net/WebSocketTest` (5 tests) pins
  the parts a browser checks: the RFC's own `Sec-WebSocket-Accept` vector, all three payload-length
  encodings, fragmentation arriving as one run of bytes, a ping answered with a pong, and an unmasked
  frame refused.
- **The clipboard, both ways, out of process** (2026-09-26): with `--backend vnc` running, a viewer
  that sent `ClientCutText` "from the viewer" had it read back out of the X `CLIPBOARD` selection by
  the kit's own `ClipboardProbe read`, and a `ClipboardProbe write` of "from the X session" arrived
  at that viewer as `ServerCutText`. A viewer's copy goes down the path an X client's copy takes -
  into the host's pasteboard and out again - so "somebody copied on the Mac" and "a viewer copied"
  cannot fight over the selection, and the Mac's clipboard gets the viewer's text the way every
  remote-desktop client makes it.
- `mvn test` 283/283.

**ZRLE, done 2026-09-26.** `display/ZrleEncoder` writes the encoding every VNC client understands
after raw: 64x64 tiles, each solid, a packed palette, plain RLE or raw, run through one zlib stream
per session. A client that lists ZRLE (16) gets it; one that does not gets raw frames, which the
protocol requires every client to decode.

- **The measurement**: a 320x240 frame of flat colours and a stripe - exactly what a forwarded
  terminal looks like - comes back under **an eighth** of raw in the test, and a full 1300x800 desktop
  of solid colours is **1092 bytes of tiles compressed to 26**, against 4.16 MB raw. The live check is
  noVNC over the WebSocket rendering EasyShell's interface with ZRLE in play.
- **Four traps, and the last one was found by the live check rather than by a test.** A CPIXEL is
  **three** bytes for the common 32bpp format, not four. The zlib stream belongs to the *connection*
  and is flushed, never finished. A run never crosses a tile. And **the deflate loop must not treat a
  zero-byte result as completion**: the first version stopped there and truncated the stream at
  exactly four output buffers - invisible in every test whose frame fitted in one buffer, and a
  client that closes the connection on a real desktop. There is a test now with a frame whose
  compressed output spans many buffers, and it fails against the old loop.
- **Not emitted**: palette RLE (129..255), Tight's zlib path and `CoRRE`. A client only has to decode
  what it is sent, so those are optimisations rather than gaps - and the palette/RLE/solid/raw set is
  what a screen of windows actually needs. Tight *is* emitted as its JPEG method for photographic
  rectangles (A32), which is the one place where the palette set is the wrong tool.

**The Cursor pseudo-encoding, done 2026-09-26.** A viewer that lists it (-239) is sent the pointer's
image, so it draws the X session's pointer - an I-beam over a text field, a resize handle on a border
- instead of an arrow of its own. Measured live: an X client's own 24x24 cursor arrived at a viewer as
`hotspot=(7,4), 24x24, 231 masked pixels` - a shape with a real hole in it, not a filled cell.

- **The one thing about the message that is unlike every other**: the rectangle's **x and y are the
  hotspot**, not the corner of a region. Read from the client's own source (noVNC's `_handleCursor`)
  rather than guessed, because a viewer that reads them as a corner draws the pointer in the wrong
  place and looks like a server that is one pixel out.
- **A viewer that connects to an idle screen is told nothing, and that is the design**: the frame
  holds its last contents and there is no sprite to describe (the same state that logs "nothing to
  show"). With an application on the screen the pointer arrives with the next tick - which is why the
  `DisplayBackend` grew `presentCursor`, told every frame rather than only when something drew: a
  viewer that has just connected has to be answered on a screen that is otherwise quiet.
- The mask is one bit per pixel, so a *semi*-transparent cursor edge travels as opaque; RFB's
  `AlphaCursor` would carry it, and no client here needs it.

**What is not done, and what each would take.**
- **The page itself is not served.** The WebSocket carries RFB for a browser, but noVNC's own files
  come from somewhere else (the live check used `python3 -m http.server` for them). Serving a page
  from this server would mean HTTP file serving in a display server, which is a different product
  decision than a transport.
- **`Tight`, `CoRRE` and palette RLE** are not implemented; ZRLE is what a client gets, and it is the
  encoding every VNC client understands after raw.
- **One port for the whole screen**, and therefore one screen: per-client mode would need a port per
  client, which is why `--backend vnc` implies `--single-window`.
- **`--scale`** is the HiDPI knob and works (the screen really is that many pixels), but the *colour
  depth* reduction the item mentions as a bandwidth knob is not implemented.

**Verify.** A real VNC client on another machine, and the browser path in a page with a canvas; the
same scripted gesture on both, compared with the frame dumps. **Half done**: the real client is
`display/VncBackendTest`'s RFB client plus three accepted connections from macOS Screen Sharing; the
browser path waits for the item above.

---

### [x] D3. Core-font fidelity. **M-L** - done 2026-09-26: the standard X fonts are served from tables captured off a reference server

**Current.** Fonts are Java fonts looked up by X font name (`XFont`), not rasterised X font files: the
shapes and the metrics are the JVM's, and `SetFontPath`/`GetFontPath` are bookkeeping. Xft text (which
goes through RENDER) is unaffected and looks identical to a real server.

**Why it matters.** `xterm` with a core font (`-fn fixed`), `xfontsel`, and any client that measures
text through `QueryTextExtents` gets the JVM's metrics; a *font path* pointing at a real `misc-fixed`
or a bitmap font is not honoured. This is visible in the fonts' look.

**Plan.** Parse X font files (`.pcf`/`.bdf`, not `.pfb` - the vector ones are a different project) for
the fonts a user would put on a font path, and serve those bitmaps and metrics through the existing
`XFont`/`QueryFont`/`ImageText8` path; or, cheaper and honest, keep the Java-font approximation and
document it as a limitation (the README already does).

**Traps.** `QueryFont`'s reply is a large structure with per-character metrics; a client that gets them
subtly wrong mis-lays-out every string. Font names are aliases (`fixed` -> a specific file) resolved by
the font path.

**Verify.** `xfd`/`xfontsel` and an `xterm -fn`-based comparison against Xvfb: the same glyphs and the
same `QueryTextExtents` numbers.

**Done 2026-09-26, and the shape of the answer is not the one the plan above describes.**

`fixed`, `6x13`, `7x13`, `9x15`, `10x20` and `12x24` - and the XLFD names they answer to - are now
served from tables **captured off a reference server** (`tools/kit.sh GenBitmapFont`), so the metrics
a client reads through `QueryFont` and `QueryTextExtents` and the glyphs it draws are the reference's
own. Every other name falls back to the Java approximation, which is the old behaviour kept on
purpose: a client that opens a font nobody here has should get something it can draw with.

**Why the reference and not a PCF reader**, which is what the plan said: parsing the font *file* means
reimplementing a format and hoping it agrees with the server a client is comparing against, while
taking `QueryFont`'s answer plus each glyph's ink (drawn into a window and read back with `GetImage`)
cannot disagree with it. The cost is that the generator needs a display with the fonts, which is why
the generated table is checked in - like `CursorFont`, and for the same reason.

**Four things about the capture were measured rather than assumed, and each one looks like a working
capture of a different font.** All four are in the design notes and in the tool's own comments:

- **Text drawn into a one-bit pixmap comes back as the font's default character** - a solid block for
  every code, space included - on a reference server whose `xlsfonts -ll` says the font has all 256
  codes. The capture draws into a window and reads 32 bit pixels instead. Testing "is this pixel
  white" matters more than it sounds: the black the strip is painted with reads back as `#000001`.
- **A character's right bearing is measured from the origin**, not from the end of its advance, so a
  five pixel glyph has `rightBearing` 5 in a six pixel cell. Read the other way, every glyph is one
  pixel wide and the font draws as vertical bars.
- **`CHARINFO`s are padded to sixteen bytes in the reply** (the fixed part is 60 bytes, not 52), and
  **a font's codes do not always begin at zero**: 12x24 starts at one and answers with
  `min-char-or-byte2` 1 and 255 metrics. Either mistake draws a font that is quietly wrong - the
  second one drew every glyph one character early, which looks like a slightly odd typeface.
- **`GetImage` of a rectangle that leaves the window is a `BadMatch`**, not a clipped answer, so the
  capture works in batches; a 1852 pixel strip on a 1300 pixel screen is refused.

**Measured 2026-09-26**, three ways:

- `tools/FontProbe` (new in the kit: `QueryFont` field by field, `QueryTextExtents`, and the pixels of
  a string drawn with `ImageText8` and read back with `GetImage`) against Xvfb and here, for all six
  fonts: **identical line for line** - every character's bearings, width, ascent, descent and
  attributes, the properties (in the reference's own order, with the string valued ones resolved so
  that two servers' atom *numbers* are not compared), the extents, and the rendered pixels.
- `resource/BitmapFontTest` (4 tests, a real connection): the reference's bounds, ascent, descent,
  min/max character codes, property count and per-character metrics; the string metrics and edges
  (`overall-right` is the last glyph's right bearing plus its pen, and `overall-ascent` is the
  *string's* nine pixels and not the font's eleven); the drawn shape of 'H' and 'i'; and the guard
  that a name with no table still opens. **Three of the four fail when the tables are disabled** (the
  fourth is the guard, which passes either way, which is the point of it).
- `mvn test` 307/307.

**What is still not there**, and the README's limitation says so: a font **file** on a font path is
still not read - `SetFontPath`/`GetFontPath` remain bookkeeping - and a name with no table is a Java
font. The tables are for the fonts a client is most likely to ask for by name; the item's plan
("parse X font files for the fonts a user would put on a font path") would generalise this rather than
replace it.

**Done 2026-09-26: the font path is read.** `resource/PcfFont` reads a PCF file (plain or gzipped)
into the same `BitmapFont.Font` the generated tables carry, so nothing downstream changed, and
`resource/FontPath` does the standard resolution - alias to full name, full name to file - with
`~/.xserver/fonts` on the default path. Measured with `tools/FontProbe` against Xvfb for `8x13`
(a font with no built-in table, read from `8x13-ISO8859-1.pcf.gz`): **identical line for line** -
the header, the bounds, all 256 character metrics, the properties, `QueryTextExtents` and the pixels
of a drawn string - and the six built-in fonts are unchanged and still identical. `mvn test`
318/318. The traps: the section numbers of the encodings and the accelerators are easy to swap
(32 and 256), the header is in the accelerators section rather than the metrics one, the per-character
metrics are the *ink* ones, and `fonts.dir`'s count line is the only line to skip - a font file's
name usually begins with a digit.

**What is not there**: BDF files are recognised by name but not parsed (PCF is what the fonts on a
font path are), and a compressed `.pcf.gz` is read into memory whole. The default path is this
server's own directory followed by the machine's (`/usr/share/fonts/X11/misc`, `/usr/share/fonts/misc`,
`/opt/X11/share/fonts/misc`, each only if it exists) and then the built-ins.

**The notes below are the reading that preceded it, kept for the detail.** The reading is done - `libXfont`'s `src/bitmap/pcfread.c`, which is the format's
own implementation - and the shape it has to fit is already here: a reader only has to produce a
`BitmapFont.Font`, which is exactly what `XFont` consumes, so nothing downstream changes.

- **The table of contents is little endian whatever the rest of the file is**: the version `\x01fcp`,
  a count, then (type, format, size, offset) per entry, all LSB. The *format* word of a section is
  what says how to read that section's payload: bit 2 is the byte order, bit 3 the bit order, bits 0-1
  the glyph padding (1, 2, 4 or 8 bytes a row), bits 4-5 the scan unit.
- **The metrics section begins with the font's own header** - seven flag bytes, a padding byte,
  `fontAscent`, `fontDescent`, `maxOverlap`, then min-bounds and max-bounds as six `INT16`s each (and
  ink bounds too when the format says `ACCEL_W_INKBOUNDS`) - followed by a count and that many
  metrics. A "compressed" metrics section stores each of the five numbers in **one byte minus 0x80**,
  which is the one part of the format that cannot be guessed from the field sizes.
- **Properties are a name and a value plus an `isString` byte each, then a block of NUL-terminated
  strings**; both the name and (for a string property) the value are *offsets into that block*. That
  is exactly the `propertyNames`/`propertyValues`/`propertyStrings` shape the tables already carry.
- **The bitmaps are indexed by glyph, not by character**: `nbitmaps` offsets, then four sizes (one per
  glyph padding), then the data; a glyph's rows are `rb - lb` wide and `asc + desc` tall, padded up to
  the glyph pad.
- **The encodings table is what turns a code into a glyph**: first/last column, first/last row, the
  default character, then one `INT16` per cell that is a glyph index or `0xFFFF` for "no such
  character". A font whose glyphs are in code order - which the `misc` ones are - makes this look
  redundant, and assuming it is would break on the first font that is not.
- The name resolution on top of it is the part with a standard: `<dir>/fonts.alias` maps a short name
  to an XLFD, `<dir>/fonts.dir` maps an XLFD to a file name, and `SetFontPath` names the directories.
  The VM's `/usr/share/fonts/X11/misc` is a working example of all three, and **the Mac has no PCF
  fonts at all** - so a live check has to copy a few (and their `fonts.dir`) somewhere the server can
  read, `~/.xserver/fonts` being the directory this project already keeps its own files in.
- **The verification is already built**: `tools/FontProbe` compares `QueryFont`, `QueryTextExtents` and
  the *pixels* of a string against a reference server, and a font with no built-in table - `8x13` is
  one - would exercise the new path end to end the moment it resolves.

---

### [ ] D4. Audio and other devices - out of scope by default.

Not an X server's job, but users will ask. The usual answer is a side channel (a PulseAudio/PipeWire
client or an RDP-style redirection), not the display protocol. Note it here so the question has an
answer, and keep it out of the plan unless the product is a full remote desktop.

---

## 6. E - Engineering and quality

### [~] E2. Continuous integration. **S** - the suite runs on a machine with no display 2026-09-25; the pipeline itself is the repository host's to configure
**What was wrong, measured.** The item's own note - "the JavaFX backend test opens a real window when
JavaFX is available - on a headless runner it must skip, which the test already does if written that
way; check, and if it does not, make it" - had two halves, and neither was true:

- `JavaFxBackendTest.skipWithoutJavaFx` asked whether JavaFX was **on the classpath** and not whether a
  window could be opened. JavaFX is a dependency, so the class is always there: on a runner with no
  display the test walked straight into `Platform.startup`, and the toolkit's own startup then either
  failed or never returned.
- **`HighDpiTest` was the worse one, and the one nobody was looking at.** It starts a server without
  `--backend headless`, so the server took the default backend and opened a real window *inside the
  test JVM* - every run, on every machine, and on a machine with no display the toolkit's
  `QuantumToolkit.startup` parks for ever. **The whole suite hung there**, not for a second but
  indefinitely: reproduced with `-Djava.awt.headless=true`, with a thread dump showing `main` parked
  in `QuantumToolkit.startup` and the render thread failing in Prism with
  `ArrayIndexOutOfBoundsException: Index 0 out of bounds for length 0` (an empty screen list). CI would
  have hit a job that never ends rather than a red build.

**What changed.** `JavaFxBackendTest` now also asks `GraphicsEnvironment.isHeadless()`, which is the
platform's own answer - true on a Linux runner with no `DISPLAY`, false on a desktop with one - and
aborts with a message saying a window cannot be opened there. `HighDpiTest` starts its server with
`--backend headless`, like every other test in the suite that starts one: what it is about is the
screen's size and the property it publishes, and neither needs a window. A check over the whole test
tree says it was the only one.

**Measured 2026-09-25**: `mvn -o test -DargLine=-Djava.awt.headless=true` **hangs before** (with the
thread dump above) and **completes after**: `Tests run: 257, Failures: 0, Errors: 0, Skipped: 2` - the
two skips being exactly `JavaFxBackendTest`'s toolkit tests - and `BUILD SUCCESS`. Without the flag,
on a desktop, the same suite runs 257 with **0** skipped, so the guard skips only where skipping is
right.

**`ci.sh`** is what a pipeline runs: one command, `MVN_OPTS=-Djava.awt.headless=true ci.sh` to
reproduce a runner here, and it prints where the surefire report went. It finds Maven the way the
other scripts do (not on `PATH` on the author's machine), and it deliberately does **not** pass `-o`:
a runner's local repository starts empty. It also deliberately does not configure a service - this
repository's remote is Gitee, and a workflow YAML written blind for a host nobody has tried is worse
than no file. The two facts a pipeline needs are in the script's own comments.

**Measured again 2026-09-27, and this is the half that a runner does that this machine had not: a
build with an *empty* local repository.** `ci.sh` deliberately does not pass `-o`, because a runner
starts with nothing, and that claim had never been tested - every run here used the author's `~/.m2`.
With `MVN_OPTS="-Dmaven.repo.local=/tmp/fresh-m2 -Djava.awt.headless=true"` (a directory that did not
exist), the script resolves everything from Maven Central and finishes:

```
[WARNING] Tests run: 387, Failures: 0, Errors: 0, Skipped: 2
[INFO] BUILD SUCCESS
surefire report: .../target/surefire-reports/
```

The two skips are exactly `JavaFxBackendTest`'s toolkit tests, which is what the guard is for. A
second thing falls out of it, and it is worth knowing: that run resolved the **published** protocol
artifacts - not this machine's locally-patched one - and passed, which is E1's compatibility claim
measured from the other side.

**And the owner has settled what that pipeline would be (2026-09-27), which retires the guesswork the
paragraph below warns about**: this repository lives on **Gitee as a code host only**, is going to be
**mirrored to GitHub**, and continuous integration will run on **GitHub Actions**. So the missing file
is a `.github/workflows/*.yml` whose one job runs `ci.sh` - a script that is already the whole
interface, needing only a JDK and Maven - and it is to be written when the mirror exists rather than in
advance. It is also settled that **the no-display case is not this project's own use**: the server is
meant to be rendered by JavaFX on a desktop. The headless support stays regardless, because it is a
*means* rather than a goal - it is what lets a container or a runner run the suite at all - and it costs
a desktop run nothing (387 tests, 0 skipped there; the two guards skip only where skipping is right).

**Left open.** The green run on a pull request (and the deliberately broken commit failing it) cannot
be produced here - there is no CI service in this checkout - so the item stays open until the script is
wired to one and seen to run. With the decision above that is GitHub Actions, and the script's own
comments say what a pipeline needs of it: a JDK that satisfies the pom, and Maven.

**Found on the way, and now its own item and done: a server with no display serves into the void.**
With `-Djava.awt.headless=true` and the default backend, a client that maps a window made the tick
thread call `JavaFxBackend.start`, whose toolkit startup never returned: the request threads went on
answering clients, the tick thread was parked, and the log's last words were a raw JavaFX stack trace
and `nothing to show on display :95: the window holds its last frame`. That is a backend-selection
question rather than a CI one, and **E7** is the item - the toolkit startup is bounded, the fallback
to the headless backend happens, and what the server *says* about it is honest now.

---

### [x] E4. Per-client framebuffer memory. **M** - done 2026-09-26: allocation is lazy, and the other half is declined rather than pending
**What it was.** In the default mode every connection gets a `Screen`, and every `Screen` allocated its
`Framebuffer` at the handshake (`width * height * 4` bytes; 4.2 MB at 1300x800, 16.7 MB at
`--scale 2`) - whether or not the connection ever drew anything. With 256 clients that is a gigabyte
of framebuffers, and the connections that paid it include the ones that only ask questions: the kit's
probes, `xdpyinfo`, and `xdotool`, which opens a connection per invocation.

**What changed.** `Framebuffer` allocates its pixels on the first write and not in its constructor, so
a screen that is never drawn into is a size and a colour. Reads do not allocate - an unallocated
screen answers its background, which is what it would hold - and `resize` copes with an array that
does not exist yet. The object keeps its identity, which is what the backends and every picture over
it hold on to; that is the same rule C1's resize follows, and the reason they can be swapped under a
caller at all.

**Measured 2026-09-25**, two ways:

- `IdleScreenMemoryTest` (3 tests, real connections): four clients that connect and ask nothing hold
  no pixels; one client that maps a window allocates **its own** screen and the two others stay
  empty; and twenty idle connections grow the used heap by ~6 MB where twenty framebuffers alone
  would be 81 MB. **All three fail with the allocation put back in the constructor** - including the
  heap one, which then sees the 84 MB.
  **The heap is read after a collection, as of 2026-09-28** (`settledHeap()`), and the number is now
  **2.5-3 MB** (2541, 2607 and 2965 KB on three runs; the same test read 20572 KB and failed while the
  suite ran in one JVM). Before that it read `totalMemory - freeMemory` directly, which is the live set
  *plus* whatever the collector had not got round to - and the whole suite runs in one JVM, so the
  same twenty connections "grew" it by 15.9 MB on one run and 20.1 MB on the next and failed the 20 MB
  bound on a loaded machine while nothing was wrong. A framebuffer is strongly reachable from a live
  connection and survives a collection, so collecting first measures what the item is about; what it
  removes was never the connection's memory.
- Live, against the server on the Mac, twenty `xdpyinfo` connections from the VM: resident memory
  went from 202 MB to **241 MB** with the lazy allocation and to **304 MB** without it - 101 MB
  before, 39 MB after, so about 62 MB of the difference is the framebuffers that are no longer
  touched. What is left is the connection machinery itself (a thread and its buffers each), which is
  E5's bounded queue and not this item.

`mvn test` 257/257. The real clients are unaffected in the way that matters: EasyShell draws, so its
screen is allocated as it always was, and the measurement above shows it (the run that launched it
added a screen's worth).

**Ticked 2026-09-26**, because both halves are settled: the lazy allocation is in and measured (below),
and the second half is **declined with its reasons rather than left pending** - which is a decision, not
an unfinished piece of work. The entry below is the record of both.

**What is *not* done, and why.** The other half of the item's plan - *free* a client's framebuffer
when its last window goes away - is deliberately not implemented. A framebuffer holds more than the
windows' content: it holds the root the client may have drawn on (a wallpaper client, `xsetroot`,
and any client that paints its desktop), and it is the store every picture over that screen points
at. Releasing it while the client is still connected would either hide the root's content the next
time a window appeared, or hand the rasteriser an array that is no longer the screen's - the
use-after-free the item warns about, inside the JVM's own heap. The memory it would save is bounded
by the clients that have drawn and then closed every window, which is not the case the item
describes as fatal; the case it does describe, many connections that hold a screen each, is the one
the lazy allocation takes away. **Sharing one framebuffer between screens of the same size** is the
plan's other option and is not available either: in the default mode every client's screen shows the
whole display at the same origin, so two clients' windows are at the same coordinates and a shared
store would show each client the other's windows.

---

### [x] E5. Backpressure to a client that has stopped reading. **M** - done 2026-09-25

**What it was.** Every reply and event was written to the client's socket on whichever thread held the
server lock - the display tick for an event, another client's request thread for a reply. A client that
stopped reading filled its socket buffer, that write blocked, and the lock was never released: **one
hung client froze every other client's screen**, which the README listed as a known limitation and
which is the worst failure mode a display server can have.

**What changed.** `ClientOutput` now has a **bounded queue (1024 messages) and a writer thread of its
own** per connection. Nothing writes to the socket from the caller's thread: the only thread that ever
waits for a client is that client's own, and a queue that fills up means the client has stopped reading
altogether - so it is dropped, with the reason in the log line (`the client is not reading: 1024
messages are already queued for it`) rather than a bare "broken pipe". Order is preserved by
construction: one queue, one thread taking from it, flushed after each message. `flush()` became a
no-op (the writer flushes every message, so nothing has to be pushed) and the request loop's
flush-after-each-request - which existed to break a deadlock this removes - no longer has anything to
do. A close **drains briefly first** (100 ms), because a refusal of the connection setup, a final error
and the farewell a graceful shutdown sends are the last thing a client will hear.

**Measured 2026-09-25.** `net/BackpressureTest` (2 tests, and the item's own Verify): a client that
handshakes, shrinks its receive buffer and then sends requests whose replies it never reads, while
another client draws and reads its own pixels back. With the fix: 2/2 in 0.7 s. **Without it: both
fail** - the second client's round trip dies with `SocketTimeoutException: Read timed out` (the server
frozen, exactly as the item says) and the stalled client is never dropped. `mvn test` 228/228.

**Two traps, and both cost a rewrite of the test.**

- **A test that hangs is not a regression guard.** The first version flooded from the test's own thread
  and, with the fix reverted, blocked there: a socket write has no timeout, so the *test* stopped
  instead of failing - for ten minutes. The flood now runs on a daemon thread of its own and the test's
  own waits are all bounded by read timeouts.
- **The first measurement was a lie, and it was the socket's rather than the server's.** One
  screen-sized `GetImage` reply as "more than any buffer can hold" is not more than any buffer can hold:
  the server absorbed it without blocking, the round trip succeeded, and the test read as "the unfixed
  server is fine". What is past any buffer is *several megabytes* of replies - measured by adding them
  until the freeze appeared.

**Left open, and named in the design notes**: a client that reads *slowly* rather than not at all is
served in order and keeps its connection; there is no per-client bandwidth policy and no coalescing of
pending motion events, which is what a real server does and the next thing to do here if a slow client
ever becomes a real case.
---

### [x] E3. Performance: local blending, fewer copies. **M-L** - done 2026-09-25: the opaque-source path (1.85x), the A8-mask path (2.80x) and the damage region (which was the whole screen)
**Measured with a benchmark of the compositor's own pixel loop**, on one scene and with everything else
equal: two hundred full-screen (1300x800) `FillRectangles` through `Compositor.draw`, which is the one
loop every request reduces to. Op 1 is `Src` and op 3 is `Over`, the two an opaque fill uses.

```
                       before       after
Src   200 full fills   1795 ms      984 ms     (8.98 -> 4.92 ms/frame)
Over  200 full fills   1770 ms      953 ms     (8.85 -> 4.77 ms/frame)
```

**What changed.** One branch in `Compositor.blend`: an opaque source with a full mask under `Src` or
`Over` is the source, so the pixel loop stops doing the double-precision arithmetic for it. That is
not an approximation of what the general path answers - it is what it answers, with `fa = 1` and
`fb = 0`, and the arithmetic on a value that came out of eight bit channels is exact - so the two
agree pixel for pixel by construction. It is the case most of a desktop's drawing takes: an opaque
background paint, a `PutImage` of an opaque image, a `CopyArea`.

**The masked path, done 2026-09-25.** The other half of the same idea, and the one the item warned
about: `Src`/`Over` with an **A8 mask** - which is what every toolkit's text and icons take - was the
general double-precision blend, and "a fast path that changes rounding must not be added quietly".

It is not quiet, and that is what makes it safe to add. `Compositor` now splits into `blend` - the
one the pixel loop calls, fast paths included - and `blendGeneral`, the double-precision form, and
**`draw/BlendFastPathTest` compares the two over the space the fast paths claim**: all 256 mask alphas
against both ends of every channel, the values either side of a rounding boundary (1, 2, 127, 128,
253, 254), both operators, and twenty thousand random pixels including the component-alpha case. The
property is that they never disagree, and the reason they cannot is worth writing down: both fast
paths are the same rational number in integers, `(src*m + dst*(255-m)) / 255`, and a rounding tie
there would need `2A = 255 * odd`, which has no solution. The test is what says so rather than the
argument. Two cases are deliberately *not* taken - a translucent source, and `Over` onto a
translucent destination, which is not a pixel this server composites text onto - and there are tests
that they still come out of the general path.

```
masked Over, 1040000 pixels    fast 2 ms    general 5 ms    (2.80x)
```

**Measured on a real client, because this is the path its text takes.** EasyShell rendered twice
through the same capture - once with the masked fast path in and once with it short-circuited - and
the two are **identical: 0 of 910000 pixels differ** over its window. That is the check the identity
test cannot make: the test compares two functions, this compares a JVM's worth of glyph compositing.

**And the damage itself, which D2 needed and which is this item's last bullet (2026-09-25).** "Blend
only the damage region" turned out to be about *which* region: every drawing request marked the
**whole screen**, so the presentation of a keystroke was a full frame - invisible for a local window,
four megabytes for a VNC viewer. `Rasterizer` now records the region it actually wrote
(`drawnBounds`) and `DrawHandlers.afterDrawing` marks that, and `draw/DamageTrackingTest` holds it to
the only standard that matters: for eleven primitive families, **every pixel that changed is inside
the damage**, checked by comparing the surface against a pristine copy. The two misses found on the
way are recorded in D2 (the older whole-screen mark left under the new one, and `ImageCodec` building
its own rasterizer, which lost both the clip and the record). (b) The JavaFX `WritableImage` round trip turns out
not to exist: `JavaFxBackend.present` copies nothing - the `PixelBuffer` wraps the framebuffer's own
`int[]` and only the union of the damage is handed to the FX thread - so the third bullet of the
plan was answered by looking rather than by changing anything.

**The live pixel gate, measured 2026-09-25.** `xlogo` (static, so the two captures are comparable -
`xclock` is not: its hands move with the time, which cost one run) drawn on both servers at the same
geometry, captured with the same `xwd -root` on the VM, and diffed over the logo's own area:

- **fast path against slow path, same scene: 0 of 46656 pixels differ** - the change is
  pixel-identical, which is what the design says it must be.
- against Xvfb the same run differs in **377 pixels (0.8%)**, and **the slow path differs in exactly
  the same 377** - so it is a pre-existing difference and not this change's. (Worth an item of its
  own if it is ever chased: the deltas are full-scale, so it is geometry rather than blend rounding,
  and the suite's `RasterizerTest` compares against Xorg pixel for pixel.) **Chased and fixed
  2026-09-25 - see A15**: the geometry was the polygon filler's scanline rule, not the blend, and the
  same `xlogo` comparison is now **0 of 48400 pixels** over the logo's own window.

The suite is 268/268 with both fast paths in, and `mvn test` is the only place the blend's identity
property is checked - there is no client that can be made to exercise every mask alpha.

---

### [x] E1. The `eventcopy` generator fix lives in the protocol fork. **S** - **closed 2026-09-27 by the owner's decision: the fix stays in this repository, and the library is neither forked, built nor republished**

**The decision, 2026-09-27, and it is what closes the item.** The owner will not fork the library,
build it, or republish its artifacts - that work is out of proportion to five wrong bytes - and
**Lombok is not a dependency this project will take on**. So the rewrite in `ClientOutput` is not a
stopgap any more: **it is the fix**, the seventh entry in the README's "Bugs worked around in the
protocol library" beside six of the same kind (a wrong list decoder, a wrong reply length, a wrong
modifier-map count...), and it is pinned by a test that reads the bytes off the wire.

**The fallback, if it ever needs revisiting, is a same-named-class override (option B), and it was
checked before being set aside.** Five classes at
`src/main/java/com/github/moaxcp/x11/protocol/xproto/` - `ButtonReleaseEvent`, `KeyReleaseEvent`,
`LeaveNotifyEvent`, `FocusOutEvent`, `CirculateRequestEvent` - would shadow the library's, because
this project's own `target/classes` precedes the dependency on the classpath and javac prefers a
source in the compilation. Two facts make it *safe* (measured): **nothing else in the library
constructs or references those five classes** (the only references are inside their own files), and
this server never *decodes* them, so only `write`/`getNumber` matter. What it costs is the code: each
class is ~150 lines of Lombok-generated boilerplate (`@Value @Builder`), so the override means either
taking Lombok - rejected above - or writing those classes out without it (~450 lines, since `read`
can be dropped), generated from the library's own sources by a script so it can be refreshed. That is
the price of "the class itself is right" over "the bytes are right", and the owner chose the bytes.

**Why the rewrite is enough.** Every event this server sends passes through
`ClientOutput.writeEvent`, which is where the five bytes are corrected; the interface surface is
`XEvent` (`write`, `getNumber`, `getResponseSize`), so no other path can produce them; and the
symptom the item is about - a release arriving as a press, a leave as an enter, a focus-out as a
focus-in - is what `EventTypeByteTest` now fails on if the correction is removed (measured against the
published artifact: 4 of 5 cases fail).

**Current.** `ClientOutput.EVENT_NUMBERS` rewrites five event type bytes by hand, because the
generator in the `moaxcp/x11-protocol` fork ignores the `number` on an `eventcopy` and renumbers
sequentially. The workaround is documented in the README and in the design notes.

**Why it matters.** Any new event that the fork generates with an `eventcopy` - which is how the
protocol defines most of the aliases - will be wrong again, and the failure mode is silent and
confusing: a release arriving as a press is exactly the bug this workaround fixes.

**And the upstream fix is written and verified anyway (2026-09-27), then set aside** - it is the
record of what is wrong *there*, kept because it costs nothing to keep and because it names the traps
of building that library. `docs/fork-eventcopy-fix.md` is the patch, the reasoning and the recipe.
What it says, in short:

- The generator *reads* a copy's number and then takes the copied event's **whole unit list** as its
  own - including the first unit, the `NUMBER` field, which carries the *ref's* constant. So the copy
  is emitted with the ref's type byte. The patch copies the list and replaces that one field.
- Published to the local repository and read back: `ButtonReleaseEvent.NUMBER` 4 -> **5**,
  `KeyRelease` 2 -> **3**, `LeaveNotify` 7 -> **8**, `FocusOut` 9 -> **10**, `CirculateRequest`
  26 -> **27**, with the events they copy unchanged.
- **`client/EventTypeByteTest` is new** and is the test this item's plan asked for - but written
  against the *wire* rather than against the classes, so it passes with the rewrite, passes with a
  fixed generator (the rewrite is then a no-op), and fails only when neither is there: with the
  published artifact back and `EVENT_NUMBERS` disabled, **4 of 5 fail** - a `KeyRelease` arrives as
  type 2, a `ButtonRelease` as 4, a `LeaveNotify` as 7, a `FocusOut` as 9.
- It is *not* pursued, by the decision above. If it ever is: publish a corrected artifact first,
  then the rewrite can be deleted - measured, the two are compatible, so the order does not matter
  and nothing in this repository has to change until then.
- **A trap of this machine**: the fork's Gradle wrapper cannot read the class files of the default
  JDK 27 (`Unsupported class file major version 71`), so the build needs `JAVA_HOME` pointed at a
  JDK 21, and a local publish needs the signing tasks off (an init script disabling
  `org.gradle.plugins.signing.Sign`). Both are in the recipe file.

**Verify.** `mvn -o test -Dtest=EventTypeByteTest` (5 cases: key, button, leave, focus, and the
circulate request this server never sends, pinned where it lives).

---

### [ ] E6. Keep the three documents in step. **S, ongoing**

**Swept 2026-09-27**, after A17 was re-measured and closed. The drift it found this time was in *this*
file rather than in the other two, which is the shape worth recording:

- **Two entries were still marked `[ ]` while the items they describe were superseded and finished** -
  `A5old` ("the original wording, kept for what it measured") and `B3old` (the same for Xinerama). An
  open-looking box on a closed item is the drift this item exists for, one step worse than the usual
  form: it does not just omit what is done, it advertises it as waiting for somebody. Both now carry
  the `- superseded:` marker `A4old` established.
- **§0's table had a second, unticked `A6` row** while A6 was already ticked two lines above it - the
  same fault in the table that is the first thing a new session reads. Removed.
- **A17's own record needed the two other documents touched**, and got it: the README's `--glx` bullet
  and the design notes' "GLX, and why a server without a GPU can run OpenGL" both said the caret
  "blinks in the states that can be controlled"; both now carry the actual measurement (ten captures,
  two alternating states, on *both* builds) and the sharper half - that the hover paints nothing
  because the client issues no drawing request at all. The delivery-shipped text was not *wrong*; it
  was weaker than what had been measured, which is the failure mode in the other direction and worth
  the same correction.
- **The reference sweep was re-run and is clean**: 13 probes identical, and the six that differ are
  the six `tools/README.md` lists as deliberate (`ChangeCursorProbe`, `XiProbe`, `GlxProbe`,
  `RandrProbe`, `XineramaProbe`, `ScreenSaverProbe`). No sixth-but-one difference, which is what a
  finding would have looked like. `FocusProbe` and `MenuProbe` are among the identical, which is the
  gate §7 sets for the input area and is what let A17 be answered without a code change.

**Swept again later the same day**, when B5's three extensions landed and the documents had to
follow them: the README's limitation bullet said Composite, Present and SYNC were not there and that
XInput2 was "measured and not started" - which had been false since B4 and was two items stale in one
paragraph, the exact drift this item exists for. The design notes gained the section for the store's
other side, the README gained the third generated-class fault in `ClientOutput`, and `tools/README.md`
went from "13 of 18" to "15 of 21" with three new probes described and one new deliberate difference
(`SyncProbe`'s system counters). The rule the item states has now caught something in each direction:
a document that omits what is done, and a document that lists it as missing.

**Swept 2026-09-26**, after three sessions of work (XInput2, the C8 build-and-revert, the alpha store),
and it found the drift it exists for - one entry that had been true and was not any more:

- **Both documents said `CreateGlyphCursor` against a font other than the cursor font had no outlines
  and fell back to a filled cell.** A8 recorded the opposite: the composition was implemented (the mask
  glyph's ink is the silhouette, the source's ink is drawn inside it, the hotspot is the glyph's origin
  at the font's ascent) and measured identical to a reference for fourteen characters. The fallback
  only applies to a font the server has no table for at all. Both documents corrected; the README's
  version was garbled besides, and now says what a cursor *does* have - and that a cursor **name** is
  what is still refused, which is what A8 measured a reference doing too.
- **The alpha store (B5's first slice) needed three places updated**, and got them as it landed: the
  README's tray bullet (the icon is a shape now) and its "the screen is opaque" section (which now says
  what an alpha window does instead), and the design notes gained the section for it.
- **The C8 revert left two claims to fix**, both made when the item was believed shippable: the README's
  "one window per screen" paragraph now says a host window per X top level was built, measured and is
  not what this is, and the C8 entry says what it waits on.
- **A gap this sweep found while checking the picture bullet**: RENDER's pictures still point at the
  screen, so they do not see a window's own pixels for an alpha window. Recorded in B5's entry and in
  the README rather than left as a silent half-truth.

The rule the item states still stands and is what caught the first two: a document that lists something
as missing when it is done is worse than one that does not mention it, because the next reader
re-derives it - or, worse, "fixes" what is already there.

**Swept 2026-09-25.** The three documents were read against the items that are done, which is what
this item is for, and the sweep is worth recording because it found exactly the drift it warns about:

- **`§7`'s "Deliberate deviations" still listed `IncludeInferiors` as behaving like
  `ClipByChildren`**, with "(A9 is the item)" against it - a week after A9 stopped being true. That
  list is the one a change has to re-measure before it is proposed, so a fixed entry there is worse
  than a missing one: it tells the next reader that something is deliberate which is simply done. The
  bullet is removed, and A9 keeps the record.
- **The design notes' "Deliberately not done" had two more of the same**: "SHAPE's bounding shape
  does not clip drawing" (A2) and "Screen auto-size is not implemented" (C1). Both were corrected in
  this session rather than left for the next one.
- The README's "Known limitations" was checked bullet by bullet and is current: every entry there is
  still true, and the ones this session changed (backing store, XKEYBOARD, the fallback chain) were
  updated with their items.

`README.md` (what a user sees), `docs/design-notes.md` (why it is built this way) and this file (what
is missing) answer three different questions and each duplicates part of the others. When an item here
is done, the README's "Known limitations" and the design notes' "Deliberately not done" are usually
where the change has to be *recorded*, or the next reader re-derives it. Treat the tick as part of the
item, not as an afterthought.

---

### [x] E7. A server that shows nothing must not say it opened a window. **S** - done 2026-09-26

**What it was.** E2's orphan, which it says "wants its own item": with no display, the server falls
back to the headless backend and warns that nothing can be shown - and then, one line later, when a
client mapped its first window, logged

```
WARN BackendFactory - ...nothing can be shown on this machine, so the server is running headless - clients are served and no window is opened
INFO XServer         - opened a window for display :64 showing java.awt.Rectangle[x=0,y=0,width=166,height=166]
```

Two lines, one of them false, about the one thing a user is trying to find out. Nothing was broken
(the client was served and drew - 71 requests from `xclock`), so this is honesty rather than
behaviour: the sentence a user follows to find where their application went named a window that does
not exist. The idle line had the same fault from the other side - `nothing to show on display :64: the
window holds its last frame`, said for the headless backend and for a session with no backend at all,
which is *every* idle moment of per-client mode's master session.

**What changed.** `DisplayBackend.isHeadless()` - which already existed, with both the headless and
VNC backends overriding it, and had **no caller** - now decides what the log says:
`XServer.openedMessage` chooses between "opened a window for display :N showing ..." and "display :N is
served by the <name> backend, which opens no window: showing ...", and the idle line says "the window
holds its last frame" only where there is a window. The method's contract is written down where the
caller can read it: it is "opens no window **on this machine**", not "draws nothing" - a VNC backend
draws for every viewer - which is why VNC counts.

**Measured 2026-09-26, live.** A server started with `-Djava.awt.headless=true`, and `xclock` from the
VM against it: the toolkit startup gives up after its ten seconds with the reason, the fallback
warning follows, and the next line is now

```
INFO XServer - display :64 is served by the headless backend, which opens no window: showing java.awt.Rectangle[x=0,y=0,width=166,height=166]
```

and the idle moments say `no window is showing it`. `HeadlessWindowTest` (3 tests) pins the decision
and the contract it rests on; **`aBackendWithNoWindowIsNotDescribedAsHavingOpenedOne` fails without the
fix** (`a window that does not exist: opened a window for display :95 showing ...`). The decision is a
pure function deliberately, because the case that went wrong is the one a machine *with* a display
cannot otherwise reach.

**Still open, and it is a client's, not this server's**: the JavaFX toolkit dumps a raw
`RenderJob.run: internal exception` / `ArrayIndexOutOfBoundsException` stack trace to stderr on the
way to giving up. The server now says what happened in its own words; the trace underneath it is the
platform's and there is no switch that turns it off.

---

## 7. Deliberate deviations (do not "fix" without re-measuring)

These are recorded in `docs/design-notes.md` with the measurement that settled them. Each one was
changed once, measured against a real client, and reverted - so a change here needs the same
measurement *before* it is proposed, not after.

- **No window manager semantics.** No decoration, no `_NET_*` handling beyond what is listed in C3, no
  activation protocol, no `WM_S0` owner. The host window is the frame and the "window manager" is the
  user's own desktop.
- **The crossing pair a grab reports at its start and end (`NotifyGrab`/`NotifyUngrab`) is not
  delivered.** Measured 3 runs out of 3: EasyShell tears its menus down within a millisecond of
  receiving it. A real server sends it.
- **A window that appears under a stationary pointer is reported as entered at the map**, so the
  client knows the pointer is inside it before the first move. The *pair* that a grab implies is still
  withheld (previous item).
- **A menu that is left and re-entered**: the crossings match the reference event for event; the
  dismissal that made this look like a server bug was the missing focus events, now implemented. Do
  not "improve" the crossing rules without re-running that gesture - the notes describe the harness
  (`/tmp/menu-outin.sh` and friends) and the 6/6 result.
- **The blend rounds; Xorg's fixed-point composition truncates.** A one-unit difference in a channel is
  expected and is not a bug to chase.
- **Every window's pixels are kept, so the `backing_store` attribute decides nothing.** Since B5's
  general store (2026-09-27) a revealed window is composited from its own pixels and no client is
  asked to draw again, whatever the attribute says. The attribute is still advertised as `WhenMapped`
  in the setup reply, which is what a reference server advertises. `save_under` stays unadvertised and
  unimplemented deliberately - the reference does not implement it either, measured.
- **The pointer cannot leave the host window's viewport** and is clamped to the screen: the sprite is
  hidden outside it, and a warp beyond it is clamped rather than moving the platform pointer.

---

## 8. Suggested order

Sized so that each phase ends with something a user can see, and ordered so that the cheapest
high-value items come first:

1. **Groundwork**: E0 (check the tools in), E2 (CI). Everything after this is cheaper to verify.
2. **Trustworthiness**: A7 (auth), A4 (xdotool), A3 (font cursors), A2 (bounding shape), A11
   (autorepeat). Five small items, each with a one-line user-visible outcome.
3. **Host window**: C1 (auto-size) and C2 (raise) - both already have their decisions recorded - then
   C6, C7, C3.
4. **Interaction**: C4 (clipboard) and C5 (drag and drop). These are what make it feel like a desktop.
5. **Protocol depth**: A5/A6/A8/A1 (the input and notification gaps), then B2/B3 (RandR, Xinerama) and
   B1 (XKB).
6. **Scale and shape**: E4/E5/E3 (memory, backpressure, performance), C9/C10 (tray, input methods).
   C8 (one host window per top level) was in this phase and is declined - see its entry.
7. **Product decisions**: D1 (session persistence) and/or D2 (remote transport); D3 (fonts) if core
   fonts matter; B4/B5/B6 (XInput2, compositing, GLX) only with a reason that pays for them.

Items that are *observations* rather than features - A6, A10, A12, A13 - can be folded into whatever
phase touches their code; none of them is worth a branch of its own.

---

## 9. What this server does not have, as an X server, measured 2026-09-27

### Re-measured 2026-10-03 (the same walk, after A18/A34 and the bed's move to `.152`)

Nothing here changed the answer, and two things are worth writing down because they are the only
places this server is still *behind* a reference rather than deliberately different:

- **Extension set: unchanged.** Both advertise 23; the only two that differ are the ones above -
  this server has **DPMS** (B7) where this Xvfb build has none, and the reference has **MIT-SHM**
  where this server declines it by decision (B8).
- **Versions: no gap left, since A47.** Every version a client is told now matches, including the
  one that did not: **XInputExtension 2.4** (a reference's answer - `xinput --version`, `xdpyinfo
  -ext XInputExtension`), because XI 2.1's raw events are implemented and delivered, 2.2's and
  2.4's touch has no device on either server to have content, and 2.3's barrier release answers what
  a reference answers for a barrier that does not exist. Measured equal on both: RandR **1.6**,
  XVideo **2.2** with no adaptors, XFIXES **4.0** (the probe prints it, and **5.0 on the reference**
  - that difference is a barrier item, see A47), XKEYBOARD **1.0**, XTEST **2.2**, RENDER **0.11**,
  SHAPE **1.1**, XINERAMA **1.1**, SYNC **3.1**, Composite **0.4**, DOUBLE-BUFFER **1.0**, RECORD
  **1.13**, and the OpenGL version string a client prints, because the client renders in process
  (B6). Still missing inside XInput, and measured rather than assumed: pointer barriers themselves
  (they need XFIXES 5.0), a changeable device hierarchy (`XIChangeHierarchy`), XI's passive grabs,
  and the slave devices a reference lists.
- **The holes inside extensions are unchanged**: `ShapeGetRectangles`, `XFixesGetCursorImageAndName`
  and `XFixesChangeSaveSet`, XI1 past `ListInputDevices`, pointer acceleration (`ChangePointerControl`
  accepts and ignores) and host access control (`ChangeHosts`/`SetAccessControl` as no-ops).
- **E0's sweep, re-run: 17 of 28 probes identical, 11 different** - the nine documented ones plus
  two that were identical on 2026-09-28 and are understood rather than new bugs: `SelectionProbe`
  names one window with a different role because the probe opens a **fresh connection per section**
  and the resource-id base a new connection gets depends on when the server reaps the previous one
  (a race, and the ids are the server's to choose); `MenuProbe` reports a `FocusIn` for the click
  and `focus=yes` on the crossings after it, which is `--focus-on-click` doing its job where Xvfb
  has no window manager to set a focus at all. Both are now in `tools/README.md`'s
  expected-difference list, with the measurement behind them.

Not items that were worked and left: **capabilities a client could ask for and this server does not
have at all.** The inventory was taken the only way that means anything - against a reference server -
by comparing `xdpyinfo`'s extension list on both sides and then walking the *core* protocol's requests
one by one:

- **The core protocol is complete at the dispatch level: all 120 requests xproto defines have a case
  in `RequestDispatcher`**, and all but two do real work. The two that do nothing are deliberate and
  commented: `ChangeHosts` (host-based access control is not enforced - the display is open or it wants
  a cookie) and `SetAccessControl` (the same switch, read-only). `KillClient`, `SetCloseDownMode`,
  `ForceScreenSaver`, `ChangeKeyboardControl`, `RecolorCursor`, `StoreColors`, `QueryColors`,
  `AllocColorCells`, `CopyPlane`, `ClearArea` and `QueryBestSize` are all implemented, not stubs.
- **The extensions are no longer where the gaps are.** Measured with `xdpyinfo` on both sides the
  same way (re-measured 2026-09-28, after F6 landed): **both advertise 23**, and **22 of them are the
  same extension**. The two that differ are one the reference has and this server does not -
  **MIT-SHM**, declined by decision (B8) - and one this server has and that reference build does not:
  **DPMS**, which is B7's work. Every row of the candidate table below is done.

| extension | what it is | who asks | here |
|-----------|-----------|----------|------|
| `DOUBLE-BUFFER` (Xdbe) | a client draws into an off-screen back buffer and swaps | toolkits that refuse to flicker (older GTK/Qt paths, games); most fall back to normal drawing | ~~absent~~ **done 2026-09-28 (F5)** |
| `Generic Event Extension` (XGE) | the handshake for extension-defined events | clients that query it before using generic events - Xlib's cookie API is client-side, so this is a *handshake*, not a mechanism this server lacks (XI2's generic events already work) | ~~absent~~ **done 2026-09-28 (F4)** |
| `MIT-SHM` | shared-memory images, so pixels do not travel over the socket | every toolkit probes it; all of them fall back | **absent, declined** - B8 has the reasons |
| `RECORD` | a client subscribes to the event stream (all clients', or one's) | `x11vnc -record`, screen recorders, input loggers | ~~absent~~ **done 2026-09-28 (F2)** |
| `SECURITY` (XSecurity) | per-connection authorization, generated auth, trusted/untrusted | Xinerama-less sandboxes, `xauth generate` users | ~~absent~~ **done 2026-09-28 (F7)** |
| `X-Resource` | per-client resource accounting (memory, windows, pixmaps) | `xrestop` | ~~absent~~ **done 2026-09-28 (F3)** |
| `XVideo` (Xv) | video overlay/port: a player hands frames to the server to scale in hardware | `mplayer -vo xv`, `vlc --vout xcb`; both fall back to plain X11 drawing | ~~absent~~ **done 2026-09-28 (F6)**, with no adaptors - which is what a reference without the hardware answers |

And the holes *inside* extensions this server does have, from the same walk of the request surface:

| hole | the requests | who would notice |
|------|--------------|------------------|
| SYNC **alarms** | `CreateAlarm`, `ChangeAlarm`, `DestroyAlarm`, `QueryAlarm` | a client that wants a server-side timer (frame pacing, idle timeouts) - ~~refused today~~ **done 2026-09-28 (F1)**: the four requests, the `AlarmNotify` event, and the two refusals |
| Present's **MSC** | `PresentNotifyMSC` | a client that paces itself against the display's refresh (a compositor, a video player) - ~~refused today~~ **this claim was stale**: B5's Present work had already answered it, and `PresentProbe` now pins it (kind 1, the client's serial, identical to the reference) |
| Shape's **rectangles** | `ShapeGetRectangles` | a client reading a window's shape back rather than setting one |
| XFixes' tail | `XFixesGetCursorImageAndName`, `XFixesChangeSaveSet` | rare; `GetCursorImage` (without the name) is here |
| **XI1** beyond the device list | `SelectExtensionEvent`, `GetSelectedExtensionEvents`, `GetDeviceMotionEvents`, `ChangeDeviceControl`, `ListDeviceProperties`, XI1's `GrabDevice` | an XI1-era client; AWT uses XI1 only for the device list, which is done |
| **pointer acceleration** | `ChangePointerControl` accepts and ignores; `GetPointerControl` replies with a constant 2/1/4 | a client that sets acceleration and reads it back gets the constant - the *host's* pointer is what accelerates here |
| **host access control** | `ChangeHosts`, `SetAccessControl` | `xhost +`, `xauth`-style host rules - no-ops today, which is a security decision rather than a gap |

**What the list is worth, honestly.** *No client in this bed asks for any of them*, and that is
measured rather than assumed: a client that queries an absent extension gets "not present" rather than
a refusal, so the evidence is the **request profiles** - EasyShell's profile against the reference and
against this server agrees request for request (the A/B method), and a sweep of thirty client tools
against this server produced no refusals beyond the known two. None of them reached for Xv, RECORD
or the rest. **One of them did reach for DOUBLE-BUFFER once it was there**: on the F5 round's live run,
termora's AWT connection queried the extension as it started and asked `GetVisualInfo` - the
probe-and-fall-back this list is about, caught in the log (F5's entry has it). So these are
capabilities a *different* client would use, and one of them was already asking, and the candidates
below are
ordered by **what breaks without it** - the first is the one to do if only one is done. **All seven
were taken on 2026-09-28**, each with a measurement behind it and each in this file as a full entry,
so the list is a record of what was built rather than a queue

### [x] F1. `PresentNotifyMSC` and the SYNC alarms. **S-M** - done 2026-09-28, and one half was already in

**What it was.** The two holes inside extensions this server already has, and the ones a *modern*
client could plausibly reach for: a compositor pacing itself against the display's refresh
(`PresentNotifyMSC`), and a toolkit's own frame timer or idle timeout
(`CreateAlarm`/`ChangeAlarm`/`DestroyAlarm`/`QueryAlarm`).

**What was actually missing.** Only the alarms. `PresentNotifyMSC` had been answered since B5's
Present work - the §9 table said "refused today" and that was **stale**, which `tools/PresentProbe`
now shows: the request produces a completion notification with kind 1 and the client's own serial,
identical on both servers. The alarms were refused, and that is the half that was built.

**Current.** `SyncExtension` now keeps alarms as resources: `CreateAlarm`, `ChangeAlarm`,
`DestroyAlarm` and `QueryAlarm`, and the **`AlarmNotify` event** - which is a real extension event at
`firstEvent + 1`, not a generic one, which is why SYNC is now registered with two events instead of
none. An alarm is a trigger on a counter, evaluated when that counter's value moves (a counter here
only moves when a client sets or changes it, so that is the complete set of moments an alarm can
fire). **The trigger rules are measured, not recalled** (`tools/SyncProbe`, whose alarm section walks
each one and is identical to the reference line for line):

- a **positive transition** fires when the value goes from at or below the threshold to above it, a
  **negative transition** is the mirror, and both stay **active** after firing;
- after firing, the threshold moves by the delta - **added, whatever the test is** (10 → 110 → 210 for
  a positive transition with delta 100; 10 → -90 for a negative one with delta -100);
- a **comparison** fires once and then goes **inactive**, and says so in its own state byte - moving
  further the same way, or back across, produces nothing;
- a transition whose delta would move its threshold the way its test cannot use - negative with a
  positive delta, positive with a negative delta - is refused **`BadMatch` on the alarm's own id**.
- A refused one-way request leaves an error on the stream that the next read would consume as a reply;
  `SyncProbe.readRefusal` reads it, and the first version that did not reported a counter value of
  -4294967420.

**What is this server's reading rather than a measurement** (the specification leaves these loose and
the probe does not cover them, so they are marked in the class): a `Relative` value type is the
threshold relative to the counter's value when the alarm was armed, `ChangeAlarm` re-arms without
firing, and an alarm created while its condition already holds waits for the next change.

**Also fixed here, because the probes made it visible**: an id that is not the resource a SYNC request
expects is now answered with **this extension's** error (`BadCounter`/`BadAlarm`/`BadFence` at the
extension's error base) rather than the core `BadDrawable` the resource table's own lookup produced -
which is what a reference answers.

**Live on `.147` (2026-09-28)**, the whole round's regression, server on the Mac in the mode the clients use (`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders, typing lands** (its search box comes back with the two characters the driver typed), **its context menu opens and survives the out-and-back gesture**, and **termora renders** - and neither log has a refusal beyond the two known `window 0x0` probes every GTK/Swing client makes. `mvn test` **407/407**, and the headless run **407 with its 2 toolkit skips, BUILD SUCCESS**.

**Verify****: `tools/SyncProbe` (the alarm section) against `Xvfb` and against this server - **identical
line for line**, including both refusals and the inactive state of a comparison - plus `SyncTest`'s
four new cases (fire, comparison-once, refusal, destroyed-alarm).

### [x] F2. `RECORD`. **M** - done 2026-09-28

**What it is.** The extension a client subscribes to in order to see *other* clients' traffic:
`CreateContext` over a set of clients and a set of ranges, `EnableContext`, and then a stream of
"replies", one per recorded element, on the enabling connection - which is what `x11vnc -record` does,
and what `XProxy` does today by sitting in front of the server. `EnableContext` is the whole design:
the request is answered by a stream that ends when the recorder says so, not by a reply.

**Current.** All eight requests (`QueryVersion`, `CreateContext`, `RegisterClients`,
`UnregisterClients`, `GetContext`, `EnableContext`, `DisableContext`, `FreeContext`), the one error
`BadContext`, and the stream. There is **no generated protocol module for RECORD** in the x11 library,
so the decoders and the `GetContext` reply are hand written the way XGE's and X-Resource's are, and
`RecordTest` (nine cases) pins the offsets.

**What is not guessable, measured with `tools/RecordProbe` against Xvfb**, and now written into
`RecordExtension`'s class comment:

- **The version is the server's own 1.13**, whatever the client asked for - SYNC's and XGE's rule, not
  Present's echo.
- **The client specs come first on the wire and the ranges follow**, while the `GetContext` *reply* has
  them the other way round (each spec immediately followed by its own ranges). Reading a request the
  reply's way turns the client's spec into a range, which is what this server did until the probe's
  first run said so. An extension slot in a range is **six** bytes - a pair of major codes *and* a pair
  of minor ones - and a five byte reading shifts every field after it.
- **A spec is resolved when the request arrives**: `CurrentClients` becomes the clients connected now,
  each named by its resource base, `FutureClients` becomes the literal **2**, and `AllClients` both,
  deduplicated by resource. A spec that is a client's own base is that client. One combination is
  deliberately *not* compared, because no rule reconciles it: `[current,future]` comes back with seven
  specs on a reference where `[all]` - the same set - comes back with eight.
- **A datum's header is 32 bytes** and its length counts the data **after** the header (length 913 for a
  3652 byte client-started payload). Type 1, category, the enabling request's sequence number, the
  element header, `clientSwapped`, the recorded client's base, the server time, the recorded sequence
  number, eight bytes of padding.
- **A client's arrival is a `ClientStarted` whose data is its whole setup reply** (which is how a
  recorder learns its resource base), and **the setup reply is not also a `FromServer` message** - the
  first version recorded both, which put a redundant datum in front of every client. **A departure is a
  `ClientDied` whose data is the client's last sequence number**, four bytes in the recorded client's
  order: measured with a range that covered *one* request of the six a client sent, and the sixth
  arrived - so it is the sequence and not a count of what was recorded.
- **A `FromServer` element is a datum of its own and consecutive `FromClient` elements share one** -
  three requests arrive as one datum if nothing is delivered between them, and each delivered event or
  reply arrives alone.
- **The element header decides the stamps per element**: four bytes of server time on a server element
  when `FromServerTime` is set; on a client element four bytes of time and four of the client's
  sequence, each only when its bit is set. The width matters: the probe's own parse consumes the batch
  exactly, so a wrong width moves every element after it.
- **A datum is attributed to the request that was being executed when it was written** - a reply names
  the client that asked, an event with no request behind it names nobody (base 0, sequence 0).
- **No stream ever ends with `EndOfData`** - not on a disable, not on a free, not on a second enable
  (measured three ways), so this server sends none either. A second enable while a context is enabled
  does nothing at all.

**One deliberate difference, and it is this server's execution model rather than a gap.** A reference
processes input on a loop of its own and therefore records **device elements**: a pointer gesture came
back as three (`MotionNotify`, the XTEST request, `ButtonPress`...) with the *input-driven events
attributed to nobody*, because no request was in flight when they were delivered. This server derives
its XI2 events inside the delivered core event, so there is no device stage to record and the events
that do appear are attributed to the request that produced them (the `XTEST` request, which is itself
recorded from the wire). The sweep's gesture is therefore **protocol-only**; the input half is behind
`--input` and is measured on its own. A second, cosmetic difference: the reference gives every request
of one batch the same stamp, while this server takes its clock once per request.

**Verify**: `tools/RecordProbe` run against Xvfb and against this server - **identical line for line,
and it is in `tools/sweep.sh` now** (three numbers that differ by construction are normalised in
`tools/probe-diff.py`: the clock, the setup reply's own size, and the release number). **Full sweep run
of 2026-09-28: 17 of the 25 probes identical, `RecordProbe` among them**, and the same eight documented
differences as before - no ninth. Plus `RecordTest`'s nine cases and `mvn -o test`.

**Live on `.147` (2026-09-28)**, the whole round's regression, server on the Mac in the mode the clients
use (`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders** (its menu bar,
its host list with `aliyun SSH` and its welcome page, captured with `tools/kit.sh Shot`), **typing
lands** - `xdotool` on the VM typed `al` into its search box and the box showed it while the host list
filtered itself - and **its context menu opens and survives the out-and-back gesture** (right-click the
host row, move the pointer away to `700,600` and back: the menu with 打开连接/打开SFTP/编辑连接/... is
still there two seconds later, and the app takes more typing afterwards).
**RECORD with a real client is what this round added**: `tools/RecordProbe` pointed at that same live
server with a context over `AllClients` recorded EasyShell itself - its `ClientStarted` carrying the
336 byte setup reply, a **910 KB `FromClient` batch** of its own `CreateGC`/`PutImage`/`CopyArea`/...
requests at sequence 2250 and beyond, the events those produced, and its `ClientDied` when it was killed
- which is the extension working on traffic nobody staged. **termora renders** too (title, toolbar,
`我的主机 (1)`, its splash), and the only rejections in the whole log are the two known `window 0x0`
probes each GTK client makes at startup. `mvn test` **416/416**, and the headless run **416 with its 2
toolkit skips, BUILD SUCCESS**.

### [x] F3. `X-Resource`. **S** - done 2026-09-28

**What it is.** Per-client resource accounting: "who is using the memory" - the question E4's
per-client framebuffer work left open, since that item made allocation lazy without a way to see it.

**Current.** All six requests of 1.2 are answered (`QueryVersion`, `QueryClients`,
`QueryClientResources`, `QueryClientPixmapBytes`, `QueryClientIds`, `QueryResourceBytes`), from the
server's own resource table - the counts are real, and they move when a client allocates. Measured
against a reference with `tools/ResProbe`, four things are not guessable and three of them are now
deliberate differences:

- **`QueryVersion` answers the server's own 1.2** to a client asking for 1.2, 1.0 or 2.0 - the same
  rule as Composite's and SYNC's, the opposite of Present's.
- **`QueryClients` lists the server's own resources as a client with base zero**, before the
  connected ones. This server's own resources are the ones with no owning connection - the root
  window, the predefined fonts and colormaps, the bridges' windows - which is the same thing a
  reference's client 0 holds.
- **The resource types are atoms**, and the six core names (WINDOW, PIXMAP, GC, FONT, CURSOR,
  COLORMAP) were read off a reference's reply. The extension types this server has (PICTURE, DAMAGE,
  GLYPHSET, COUNTER, FENCE, REGION, GLX CONTEXT) are named by upper-casing its own type name - this
  server's naming, and there is nothing to agree with, because Xorg's set is closed and different.
  They are interned **lazily**, so a client that never asks about resources does not see the atom
  table move.
- **An id no client owns is refused `BadValue` carrying the id**, by every request that names one -
  except `QueryClientIds`, where a spec naming an unknown client is **no rows, not an error**, and
  the XID bit is answered with an **empty value list**. Both are a reference's own answers, measured;
  the empty list is surprising enough to be worth stating in the code as well.

**Deliberate differences from the reference** (both in `tools/README.md`'s sweep list): **a window
holds bytes here** - 4 bytes a pixel for its store, where a reference reports zero because a window
there has no backing store, and this server keeps every window's pixels (B5), so reporting none would
be the wrong answer to the question the extension asks; and **a named `type` is matched against the
resource's own type atom**, which is what the specification says, where a reference answers nothing
for any non-zero type - even the resource's own - so `QueryResourceBytes` for a window with the WINDOW
atom returns one row here and none from Xvfb. The local-pid bit is **not answered** and that is not a
difference: a reference omits those rows for a client it reached over TCP, and this server's transport
is TCP, so no client's process id is knowable.

**Live on `.147` (2026-09-28)**, the whole round's regression, server on the Mac in the mode the clients use (`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders, typing lands** (its search box comes back with the two characters the driver typed), **its context menu opens and survives the out-and-back gesture**, and **termora renders** - and neither log has a refusal beyond the two known `window 0x0` probes every GTK/Swing client makes. `mvn test` **407/407**, and the headless run **407 with its 2 toolkit skips, BUILD SUCCESS**.

**Verify****: `tools/ResProbe` against both (the four differences above, and nothing else), and
`ResTest`'s seven cases.

### [x] F4. `Generic Event Extension`. **S** - done 2026-09-28

**What it is.** A handshake and a version number, so a client that checks *properly* for generic-event
support is told yes rather than falling back. XI2's events already work here; this is what makes them
*findable* the way the protocol intends.

**Current.** `XgeExtension` answers `QueryVersion` **1.0** - the server's own version, measured: a
reference answers 1.0 to a client asking for 1.0 and to one asking for 0.0. There is **no generated
protocol module for XGE** in the x11 library, so the request decoder and the reply are written out by
hand (eight bytes and thirty two; the offsets are stated in the class and pinned by `XgeTest`).

**A real defect it exposed, now fixed.** `QueryExtension` reported the registry's next free code as an
extension's `first_event`/`first_error` even when the extension defines **none**. A reference answers
**0** for both on every requests-and-replies extension - measured for BIG-REQUESTS, XC-MISC,
Composite, Present and XGE - and 64/0 for SHAPE, which defines one event and no errors. XGE is the
first extension here with neither, so it is what made the old behaviour visible; `ExtensionRegistry`
now reports 0 for a range it did not allocate. Nothing in the suite depended on the old numbers, and
`XgeTest` pins the new ones.

**Live on `.147` (2026-09-28)**, the whole round's regression, server on the Mac in the mode the clients use (`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders, typing lands** (its search box comes back with the two characters the driver typed), **its context menu opens and survives the out-and-back gesture**, and **termora renders** - and neither log has a refusal beyond the two known `window 0x0` probes every GTK/Swing client makes. `mvn test` **407/407**, and the headless run **407 with its 2 toolkit skips, BUILD SUCCESS**.

**Verify****: `tools/XgeProbe` against both - **identical** - plus `xdpyinfo` listing
`Generic Event Extension`, and `XgeTest`'s three cases.

### [x] F5. `DOUBLE-BUFFER` (Xdbe). **M** - done 2026-09-28

**What it is.** The extension a client uses to draw into a buffer nobody is looking at and then show
it in one step: every toolkit's no-flicker path asks for it, and until now every one of them fell back
to plain drawing. B5's per-window store is what makes it cheap here - a back buffer is a pixmap of the
window's size, and the window's own pixels are the front buffer it swaps with.

**Current.** All eight requests (`QueryVersion`, `AllocateBackBufferName`, `DeallocateBackBufferName`,
`SwapBuffers`, `BeginIdiom`, `EndIdiom`, `GetVisualInfo`, `GetBackBufferAttributes`) and the one error
`BadBuffer`. There is **no generated protocol module for DBE** in the x11 library, so the decoders and
the two replies are hand written, and `DbeTest` (nine cases) pins the offsets. A back buffer is an
ordinary pixmap registered under the client's id, which is what makes it a **drawable**: every
core request addresses it without knowing DBE exists.

**What is not guessable, measured with `tools/DbeProbe` against Xvfb**, and written into
`DbeExtension`'s class comment. The first version of this was written from the Xdbe *specification*'s
own request tables and **every request was refused**; the layouts a client actually compiles against
are in `X11/extensions/dbeproto.h` (on the VM), and they differ in three places that matter:

- **`AllocateBackBufferName` is window, buffer, swapAction** - the buffer before the action - **and has
  no reply at all**. Xlib's own `XdbeAllocateBackBufferName` returns `Success` without reading
  anything, which is why the probe's "accepted" is the whole of that request's evidence.
- **`SwapBuffers` names a window, not a buffer**: the request is a count followed by that many
  (window, swapAction) pairs, so a window's buffers are swapped together and there is no field that
  could name one. `buffer = 0` does not mean "all buffers" here - that idea belonged to the wrong
  reading of the request.
- **`GetBackBufferAttributes` is minor 7**, where 6 is `GetVisualInfo` - and a client that sends 6 with
  a buffer in it is answered `BadLength` with the buffer as the error's value, which reads as "the
  buffer is bad" rather than "the request is".
- **`GetVisualInfo`'s reply is not the flat list its header describes**: it is a count of the drawables
  answered and then, *per drawable*, a count followed by that many `{visualID, depth, perfLevel}`
  records. The reference's reply for one drawable is 289 units long with 144 records in it; the flat
  reading expects two records in eight bytes, and the two arithmetics differ - which is what settled it.

**What each swap action does**, measured colour by colour with **two** buffers on one window (with one
buffer, "keeps its own contents" and "is given the front's contents" are the same picture): the front
takes the **first allocated** buffer's contents, and then **every** buffer of that window is left
holding what the action names - `Copied` and `Undefined` the new front (the reference treats the two
identically), `Untouched` the front's *previous* contents, `Background` the window's background.

**The refusals, also measured**: a swap for a window with no buffers is `BadMatch`; an undefined action
is `BadValue` on both the allocate and the swap; an id that is already a resource is `BadIDChoice`; a
buffer allocated on an id that is not a window is `BadWindow`; freeing an id that is not the client's
own buffer is `BadBuffer`, this extension's own error; and **asking about one answers zero rather than
raising** - for an id that was never a buffer, for a freed one, and for another client's, which is what
a reference does. A `SwapBuffers` is validated whole before any of it happens: two pairs with a bad
second one left the first window's pixels exactly as they were. And a **second client may swap another
client's window** (a window is not private in X) while being unable to see or free its buffers.

**A buffer follows its window.** A resize reallocates it to the new size and fills it with the window's
background (measured: a read outside the old size answers the background, not an error) - which is what
`Extension#windowResized` exists for, called from the core `ConfigureWindow` path, because catching up
on the extension's next request would leave a client that resizes and draws with a buffer the window
has outgrown.

**One deliberate difference** in the probe's output and only one: `GetVisualInfo` reports **this
server's screen** - 7 visuals against the reference's 144 - which is the same kind of difference
`ResProbe`'s client list has, and the reply's *shape* is what the comparison is for.

**Verify**: `tools/DbeProbe` against Xvfb and against this server - identical line for line except the
visual count above - plus a **real Xlib client** (`Xdbe` itself, compiled on the VM: `XdbeQueryExtension`
1.0, `XdbeGetVisualInfo` parsed by Xlib's own reader, allocate, attributes, drawing into the buffer,
`XdbeSwapBuffers` showing it, the idioms, deallocate) run against both servers with the same result,
plus `DbeTest`'s nine cases and `mvn -o test`.

**Live on `.147` (2026-09-28)**, the whole round's regression, server on the Mac in the mode the
clients use (`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders** (its
menu bar, host list and welcome page), **typing lands** (its search box showed `al` and filtered the
host list to `aliyun SSH`), and **its context menu survives the out-and-back gesture** - the same
gesture the F2 entry records. Then the two checks this item adds: the **Xlib `Xdbe` client** run
against that live server while EasyShell was connected did the whole transaction (1.0 handshake, the
visual list, allocate, attributes, drawing into the buffer, the swap showing it, both idioms,
deallocate), and **termora's own toolkit asked for the extension** - the JDK's AWT connection sent
`QueryExtension` three times and then `GetVisualInfo (DOUBLE-BUFFER)` as it started, which is exactly
the probe-and-fall-back F5 removes: before this round that query answered "not present" and the
toolkit took its non-double-buffered path. It went no further than asking, which is its own decision
rather than the server's. **termora renders** as it did before, and the only rejections in the whole
log are the known `window 0x0` startup probes the toolkits make. `mvn test` **425/425** (416 plus this
item's nine), and the headless run **425 with its 2 toolkit skips, BUILD SUCCESS**; the sweep is
**17 of 26 probes identical** with the ninth difference being this one's visual count.

### [x] F6. `XVideo`. **M-L** - done 2026-09-28, and it is the *smallest* implementation of the seven

**What it is.** The extension a video player asks for: which adaptors the server has (hardware that
scales and colour-converts a frame), which ports and encodings each offers, and then it hands frames
over to be scaled. There is no such hardware behind this server's host window and there never can be -
the picture arrives as pixels like everything else.

**What the reference does, measured first with `tools/XvProbe`, and it settles the size of this item**:
Xvfb advertises XVideo and then answers **zero adaptors**. That is the whole of its surface on a
machine with no video hardware, and it is what a player reads before taking its non-Xv path - the path
every player has. So the honest implementation here is the same answer, and it is three lines of
behaviour rather than a scaler:

- **`QueryAdaptors` answers zero adaptors**, with the reply otherwise empty. The drawable it names is
  still resolved, so an id that is not a drawable is the core `BadDrawable` rather than silence.
- **`QueryExtension`** - Xv's own, not the core one - answers the extension's **own version, 2.2**.
  The extension is registered with its own two events (`VideoNotify`, `PortNotify`) and three errors
  (`BadPort`, `BadEncoding`, `BadControl`), so a client's cache of it is right.
- **Every request that names a port is refused with `BadPort`** - this extension's own error, at its
  error base - in the **reference's order of checks**: `PutVideo`, `PutStill`, `GetVideo`,
  `GetStill`, `PutImage` and `ShmPutImage` resolve their drawable and GC *first*, so a GC of `None` is
  `BadGContext` and only well-formed requests reach `BadPort`. (`StopVideo` carries no GC at all; the
  probe caught the first implementation checking one there and answering `BadGContext` where the
  reference answers `BadPort`.)
- **`SelectVideoNotify` names a drawable rather than a port and a reference accepts it** even with no
  adaptors: the mask is recorded and nothing ever fires. A client that arms it and waits sees silence
  on both servers.

**Why this is the whole of it, and not a software scaler behind `PutImage`.** The scaler is what the
candidate entry proposed, and it would be a *better* answer for a client that reaches a port - but no
client can reach one here, because this server would then be alone in advertising an adaptor it does
not have. A player would take a video path that ends in a scaler nobody asked for, at the cost of the
fallback it takes today; and the fallback draws the same frames through the same window. The extension
exists here so that "is XVideo there?" is answered truthfully, which is what a player actually asks.

**Verify**: `tools/XvProbe` against Xvfb and against this server - **identical line for line**, the
whole surface including the check order - plus `XvTest`'s four cases and `mvn -o test`.

**Live on `.147` (2026-09-28)**, the round's regression, server on the Mac in the mode the clients use
(`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders** (menu bar, host
list, welcome page), **typing lands** (its search box showed `al` and filtered the list to
`aliyun SSH`) and **its context menu survives the out-and-back gesture**; **termora renders** as
before, and the only rejections in the log are the known `window 0x0` startup probes. The check this
item adds is the **Xlib `Xvlib` client** (`tools/xv-client.c`) against that live server: version 2.2,
**zero adaptors**, `XvQueryAdaptors` parsed by Xlib's own reader, and its exit status 0 - the same
answers the reference gives. **Neither application asks for XVideo**, and that is the finding rather
than an accident: no client in this bed wants a video path, which is why this item is the smallest of
the seven and why its implementation is the reference's own answer instead of a scaler. `mvn test`
**436/436** (432 plus this item's four), and the headless run **436 with its 2 toolkit skips, BUILD
SUCCESS**; the sweep is **19 of 28 probes identical** - `XvProbe` among them - with the same nine
documented differences.

### [x] F7. `SECURITY` (XSecurity). **M** - done 2026-09-28

**What it is.** The extension whose subject is not a resource but a **connection**: a trusted client
asks for an authorization - a token - and hands it to a client it does not trust; the connection that
client makes with the token is *untrusted*, and a restricted set of requests is refused on it. It is
the real answer to "how do I run a client I do not trust", which without it is "you do not".

**Current.** All three requests (`SecurityQueryVersion` 1.0 to every client that asks,
`SecurityGenerateAuthorization`, `SecurityRevokeAuthorization`), the one event
(`AuthorizationRevoked`), and the enforcement: an untrusted connection's refused requests, the
extensions it cannot see, and the way a revoke ends its connections. There is **no generated protocol
module for SECURITY** in the x11 library, so the requests, the replies and the event are hand written,
and `SecurityTest` (seven cases) pins them.

**What is not guessable, measured with `tools/SecurityProbe` against a reference with access control
on.** The probe generates a token, connects with it, and sends the **same battery of requests from
that connection and from a trusted one** - because an error that appears on *both* is a request the
probe got wrong, and only a difference between the two is the trust level. The first version could not
tell them apart, which is how a malformed request reads as a security rule.

- **A token is an ordinary credential.** Two names that look right are not: the specification's prose
  suggests `SECURITY` and the extension's own header defines `XSecurityAuthorizationName` as
  "XC-QUERY-SECURITY-1", and a reference refuses **both** at the setup ("Authorization protocol not
  supported by server"). `SecurityGenerateAuthorization` answers with the name the authorization was
  created for (usually `MIT-MAGIC-COOKIE-1`) and a fresh sixteen byte cookie, and it is the trust level
  stored with it that makes the connection untrusted.
- **A generated authorization is *untrusted* unless it says otherwise**: the trust level is an
  attribute (mask bit `XSecurityTrustLevel` with value `XSecurityClientTrusted`), and the first token
  this project generated - with no attributes - produced a connection that could read the root's
  pixels no more than the list below allows.
- **Refused `BadAccess`, and only when the request names a window the client does not own**:
  `GetImage`, `GetProperty`, `ChangeProperty`, `ConfigureWindow`, `SetInputFocus` - and the root is not
  the client's. `SetSelectionOwner` is refused the same way whatever it names, because a selection is
  shared state rather than a drawable.
- **The extension itself is hidden**: an untrusted client's `ListExtensions` answers with **two**
  extensions where a trusted client sees twenty three - `BIG-REQUESTS` and `XC-MISC` - `QueryExtension`
  says "not present" for every other, and a request to a hidden extension's opcode is `BadRequest`
  rather than `BadAccess`. That is what SECURITY's own requests get from an untrusted connection too.
- **What it may still do is as measured**: draw its own windows and pixmaps, read its own pixels,
  properties and geometry, and ask about *geometry* anywhere - `GetWindowAttributes`, `QueryTree` and
  `GetGeometry` on any window are answered, which is what a toolkit needs to place itself.
  `GrabServer`, `SendEvent`, `GetKeyboardMapping`, `GetMotionEvents` and `ChangeHosts` are accepted by
  that reference, and they are accepted here for the same reason: a rule the reference does not have
  is a difference a client can see. (What an *earlier* build of the probe measured for `ChangeHosts`
  and `SetAccessControl` is in the entry's history: accepted by an `-ac` reference and refused
  `BadAccess` by one with an authority file - and `SetAccessControl` is no longer sent at all, because
  it changes the server for every client *and* turns access control back on in an `-ac` Xvfb, after
  which that reference refuses the next run's connections.)
- **A revoke closes the connections that use it**: the token cannot authenticate again (the setup
  refuses it, as a wrong cookie would be), and a client still connected with it is disconnected - told
  with `AuthorizationRevoked` only if its authorization asked for that event mask, which is what the
  reference did with the mask unset.
- **A token is honoured with or without `--auth`.** Measured on an `-ac` reference: it still treats a
  connection that offers a generated token as untrusted, because `-ac` means "never refuse a client
  for a missing cookie", not "ignore the credentials a client does offer". `--auth` decides only
  whether a client that offers *nothing* is let in.

**Verify**: `tools/SecurityProbe` against a reference with access control on (`tools/xvfb.sh secure`
starts one on `:97` with a fixed cookie) and against this server with `--auth` - **identical line for
line** - and the same probe against the ordinary `-ac` pair, also identical; plus the **real Xlib
client** (`tools/xsecurity-client.c`: `XSecurityGenerateAuthorization`, the token handed to a second
connection through `XSetAuthorization`, the root's pixels refused, its own window drawn and read, the
revoke closing the connection) run against both servers with the same answers; plus `SecurityTest`'s
seven cases and `mvn -o test`.

**Live on `.147` (2026-09-28)**, the round's regression, server on the Mac in the mode the clients use
(`:95`, `--single-window`, JavaFX window) with `--log-requests`: **EasyShell renders** (menu bar, host
list, welcome page), **typing lands** (its search box showed `al` and filtered the list to
`aliyun SSH`) and **its context menu survives the out-and-back gesture**. Then the check this item
adds: the **Xlib `XSecurity` client** run against that live server *while EasyShell was connected*
asked for a token, connected with it, was **refused the root's pixels** (in the log as `GetImage
refused: the connection is untrusted and 0x2 is not its own`, error 10 = `BadAccess`), drew and read
its own window, and had its connection closed when the authorization was revoked - and EasyShell, a
different connection with its own authorization, never noticed. **termora renders** as before, and the
only other rejections in the log are the known `window 0x0` startup probes. `mvn test` **432/432**
(425 plus this item's seven), and the headless run **432 with its 2 toolkit skips, BUILD SUCCESS**;
the sweep is **18 of 27 probes identical**, `SecurityProbe` among them.

**Not intended, and the reason, so that a later session does not size them as gaps**: `DRI3`/`DRI2`
and `XFree86-VidMode.DGA` (there is no GPU behind the host window, and B6 measured that a GL client
renders itself and blits); `XDMCP` (this server presents *the host's own window*, so there is no login
to serve - D2's VNC is the remote path, and it is done); `MIT-SHM` (B8); `Xevie`, `LBX`,
`MIT-SUNDRY-NONSTANDARD`, `XPrint`, `XTrap` (dead protocols, with no client anywhere).

**Verify (any of them).** `xdpyinfo` against this server must list the extension; the probe for it
belongs in `tools/` beside the others and in `tools/sweep.sh`, so its answers are compared against the
reference line for line - that is the standard every other extension here met, and the reason the
sweep exists.

### [x] A37. The X Test Suite against the `Xproto` set - two root causes fixed, one class left. **L** - 2026-10-03

**What was run, and how to run it again.** The X Test Suite (the revamped `xts` from
`gitlab.freedesktop.org/xorg/test/xts`, 5.1.5) is not packaged for Ubuntu, so it is built on the bed:

```sh
# once, on the bed VM. Needs build-essential autoconf automake libtool pkg-config xutils-dev
# libx11-dev libxext-dev libxi-dev libxt-dev libxtst-dev libxmu-dev libxaw7-dev x11-utils
# x11-xserver-utils xfonts-utils - installed with Acquire::ForceIPv4=true, because DNS answers with
# IPv6 first here and the VM has no IPv6 route (curl is not installed either; python3 is).
python3 -c "import urllib.request; urllib.request.urlretrieve('https://gitlab.freedesktop.org/xorg/test/xts/-/archive/master/xts-master.tar.gz','/root/xts-master.tar.gz')"
cd /root && tar xzf xts-master.tar.gz && mv xts-master xts && cd xts
NOCONFIGURE=1 ./autogen.sh && ./configure --prefix=/opt/xts && make -j6 && make install

# the scenario: xts5/tet_scen defines scenarios by name, so a subset is a section that includes one
# of xts5/scenarios/<set>_scen. The Xproto set is 122 purposes and takes about eight minutes a server.
printf 'min\n\t"VSW5TESTSUITE SECTION Xproto 122 389 0 0"\n\t:include:/scenarios/Xproto_scen\n' >> xts5/tet_scen

cd /root/xts                       # the *build* tree: the installed one has no test binaries
DISPLAY=:98 TET_ROOT=/root/xts /opt/xts/bin/xts-run -o /root/xts-xvfb min
DISPLAY=192.168.22.1:61 TET_ROOT=/root/xts /opt/xts/bin/xts-run -o /root/xts-ours min
/opt/xts/bin/xts-report -f <results-dir>/journal -d 3
```

**The reference first, because XTS's own README says a failure is not automatically a bug.** Xvfb
(1300x800x24, the bed's reference) answers **`Xproto` 92 cases / 294 test cases: 281 PASS, 13
UNTESTED, 0 FAIL** - "All 122 tests passed, 21 tests were not run". The 13 untested ones are the
BAD_LENGTH sub-cases, which a BIG-REQUESTS server cannot test at all (a zero length *is* the
big-request escape) and XTS says so in the journal itself.

**This server, before the fixes: 121 of 122 purposes failed.** Two root causes, both real protocol
behaviour rather than test pedantry:

1. **An over-long BIG-REQUESTS request closed the connection.** `ClientConnection.requestLoop` sent
   `BadLength` and then returned, which ends the connection. Tapped while XTS ran: the client sends a
   big request whose extended length is `bigreq_size + 1` (4 194 304 units, sixteen megabytes) and
   writes all of it; a reference answers **one** `BadLength`, *discards the whole request*, and the
   next request on that connection is answered normally. The fix is to discard the declared bytes -
   in 64 KB chunks - and carry on. XTS's own `TOO_LONG` case went from `UNRESOLVED` (`child process
   was terminated by signal 13 (SIGPIPE)`) to **PASS**, and with it the third case of every purpose
   in the set.
2. **`AllocColorCells` and `AllocColorPlanes` gave out writable cells on a read-only visual.**
   TrueColor, StaticColor and StaticGray colormaps have no writable entries; the protocol says both
   requests answer `BadAlloc` there, and XTS says it in words ("If the default visual class ... is
   DirectColor, PseudoColor, or GrayScale ... otherwise ... BadAlloc"). This server answered with a
   reply and pixels `0..n-1`. `AllocColor` is a different request and still works - looking up an RGB
   value on TrueColor is the normal case - and `XColormap.requireWritableCells` now refuses only the
   two cell requests.

**What is left, and it is one thing.** After both fixes **120 of 122 purposes still fail, and every
one of them is the same case** - `PURPOSE 2`, whose assertions are "a request whose length field is
one unit **less** than the request type's minimum gets `BadLength`" and "one unit **more** gets
`BadLength`". This server executes those requests as though they were well formed (the journal says
`Expect: wanted ERROR - BadLength, got REPLY` / `got NOTHING` / `got BadRequest`). It needs a
**per-request notion of the length a request type must have**: exact for the fixed-length requests,
"at least" for the variable ones - exactly the distinction Xorg encodes as `REQUEST_SIZE_MATCH`
against `REQUEST_AT_LEAST_SIZE` in every `Proc<Name>`. **A generic attempt was made and reverted
within the hour**: comparing the decoded request's `getSize()` with the declared length looks like it
should work, but the generated classes' `getSize()` is not the wire size for many requests
(`GetProperty` among them) and the check failed three hundred of this repository's own tests. That is
written into `RequestDispatcher.decodeCore`'s comment so the next attempt starts from the table
rather than from `getSize()`. The table is the work: 120-odd entries, each exact or at-least, and XTS
is the oracle that says which - the reference passes all of them.

**What this bought.** Xproto's *first* case of every purpose - the well-formed behaviour of each core
request - passes here, which is the broadest conformance statement this project has: 281 of the
reference's 294 assertions are green on both servers. And the two fixes are behaviour a *client* can
see: the connection no longer dies on a request the client mis-sized (a failure that reads as "the
application hung" rather than as an error), and a toolkit that asks for writable colour cells on a
TrueColor visual is now told no the way it expects, instead of being handed cells it cannot use.

### [x] A38. A double click on the title bar maximizes, and the next one restores. **M** - fixed 2026-10-03

**What it was.** "Double clicking the title bar should maximize, and a second double click should give
back the old size, but on xserver it stretches partway and stops, and the clicks do not behave the
same way twice." Both halves of that are the platform's *maximize* meeting a window whose content is
not its own.

**Measured** (per-client mode, `--glx --geometry 1300x800`, termora, host window 1280x828 on a
1512x982 display): the first double click took it to **1280x886** - the height only, the width left
where the client's window was - the second to **1512x886**, and a third restored nothing. The
server's own log says why: macOS's zoom picks a "standard frame" from the window's *content*, and this
window's content is a client's X window, whose size the client re-asserts as soon as it is resized.
Two more things showed in the same log: the platform's resize arrives a moment *after* the size this
server asks for, and reporting it as a user drag shrank the client's window straight back
("window sized to 1280.0x886.0 for content 1280x858" right after a fill to 1512), and the platform's
`maximized` state is set on the zoom *in* but not cleared on the zoom *out*, so the next double click
was a platform-only resize with no event this server could see.

**The fix, in `JavaFxBackend`.** The *gesture* stays the platform's - `Stage.maximizedProperty` fires
on the double click, which is the only signal there is - and the *geometry* is this server's:

- on `maximized = true`: remember the content size, then fill to the **display's usable area**
  (`Screen.getPrimary().getVisualBounds()`, decoration taken off) through the same path a drag takes,
  so the screen grows to fit (`growScreenToFit`, up to `--max-screen`) and the client's window is
  resized with it;
- a content resize **while the window is filled by the server** is the second double click: the
  remembered size is put back, and the platform's own maximized state is cleared with it - without
  that, the third double click is a zoom the property does not change, and the window lands on the
  platform's geometry (measured: 1289x886 after two good clicks);
- the platform's own version of either resize is absorbed by a short settle window (700 ms), during
  which the window is put back to the size the server has - that is what stops the platform's
  "standard frame" from winning the race.

New sink method `DisplayBackend.InputSink.windowMaximized(boolean, int, int)` carries this separately
from `windowResized`, and `InputState` applies it in preference to the ordinary resize requests that
arrive behind it, once each.

**Verified on the bed**: five double clicks in a row, 1512x886 <-> 1280x828, alternating exactly, in
per-client mode with termora forwarded; and the full suite is green (475).

### [ ] A39. `--single-window` maximize, restore and drags. **M** - one attempt reverted 2026-10-03; the diagnosis is what is left

**The report.** "`--single-window` has a lot of problems: maximizing leaves the application's page
unstretched, cancelling the maximize does not give the window back, and dragging the right edge makes
the window run downwards. Various double clicks behave differently." Then, after the first attempt:
"you have broken the maximize in **both** modes, and there are stretching problems: per-client, pulling
downwards drifts the window down a little with every step; single-window is completely unusable, a
left/right drag makes it jump up and down, and it snaps back on release."

**What the attempt did, and why it was wrong.** `growScreenToFit` (the screen follows a drag, but may
only *grow*) was replaced by a `sizeScreenTo` that made the screen the size each gesture asked for, in
both directions, on **every** resize request - the theory being that single-window mode's window *is*
the screen, so a restore had to be able to shrink it. The theory was right about the goal and wrong
about the mechanism: the screen is upstream of everything this server does with a window -
`contentBounds(screen)` is the viewport, the backend sizes and places the host window from the
viewport - so changing it *during* a drag is a feedback loop, and the loop is the user's report
exactly: per-client, each drag step moved the content bounds, which moved the window down a little and
the next step moved it again; single-window, the same loop ran through the window's own size, so a
left/right drag moved the window up and down and the release settled it back. Both modes' maximize
broke with it, because the maximize path goes through the same function.

**Reverted**, and with it the two smaller pieces that had been added for single-window: treating the
platform's `maximized` dropping to false as the restore, and ignoring a maximize that arrives right
behind a restore.

**What was measured while it was in, because it is what a next attempt has to respect.** In
single-window mode the platform fights this server for the same rectangle:

```
00:12:53.056 filling the window to 1512x858 (display 1512x886)
00:12:53.064 growing the screen of display :95 to 1512x858
00:12:53.090 host window maximized true -> false (1300x800)      <- the platform's own reaction
00:12:53.090 restoring the window to 1300x800                    <- and the server read it as the user
00:12:53.314 host window maximized false -> true (1510x858)      <- then maximized again
```

A fill is undone by macOS within a tenth of a second - and *with a size change*, so a discriminator
that looks at the size (rather than at the moment) cannot tell that reaction from a user's second
double click. Two attempts at a time-based discriminator (a 700 ms settle, then a 300 ms one) failed
in opposite directions: the first let the reaction through and corrupted the remembered restore size
(after which every restore restored *to the maximum*), the second swallowed the user's first click.

**The state now.** `git revert` of the attempt, which is the A38 state: per-client mode verified
again - four double clicks alternating 1512x886 <-> 1280x828, and a downward drag that grows the window
without moving it (the drift is gone); single-window mode is back to what it was before all of this -
drags grow the screen and leave the window anchored, the maximize is the platform's own zoom with the
screen grown to fit (which stretches *partway*: the platform's standard frame is the client's width),
and the second double click still does not give the window back. 475 tests green.

**What a next attempt should do differently.** Do not read the platform's `maximized` state and do not
resize the screen on the way *through* a gesture. Either (a) take the gesture itself - a double click
on the title bar is a native event this backend could observe before Glass acts on it - and then set
the geometry once, with the screen resized exactly once per gesture and never during a drag; or
(b) give single-window mode a *content-bounds* viewport like per-client mode's, so the window stops
being a 1:1 view of the whole screen and a restore is an ordinary window resize. (b) is the cleaner
model - it is the same design per-client mode already uses - but it changes what the mode means
(a window would no longer show the whole screen), so it is a decision for the user, not a bug fix.

**Design (b) is now in, for the user to look at** (2026-10-04). `XServer`'s tick takes the viewport
from `contentBounds(screen)` for the master session too, where it used to be the whole screen:

```java
wanted = contentBounds(session.screen());     // was: client == null ? whole screen : contentBounds
```

With that, a single-window host window is an ordinary frame around the clients' windows: measured on
the bed, a client that maps a 1280x800 window gets a **1280x828** window at the client's position
instead of a screen-sized 1300x828 one, and the maximize/restore path that works in per-client mode
applies to it unchanged. Per-client mode is unaffected (it already used `contentBounds`) and was
re-verified: four double clicks alternating 1512x886 <-> 1280x828; 475 tests green.

**What to watch for, because it is the trade-off**: the window now follows the clients' windows -
it moves and resizes when a client moves or grows its window, and a client whose window is small gets
a small window with the rest of the screen not shown at all; with several clients it tracks the union
of all of them. If that reads as "the window jumps around" rather than "the window fits the
application", the alternative is (a) - keep the whole-screen view and take the title-bar double click
itself - which is more work but changes nothing about what the window shows.

**Its own bug, found the same night and fixed: the window grew for ever with xclock/xeyes.** Reported
as "in single-window mode xclock and xeyes keep slowly stretching the window, and it flickers". Two
one-pixel accounting errors, each a feedback loop:

1. **The viewport counted the clients' window *borders*.** `contentBounds` used
   `absoluteBoundsWithBorder`, so the viewport was one pixel larger than the window on each side. The
   host window was sized to that, its resize was read as the user's, the client's window was resized
   to the viewport, and the content bounds grew by the border again - **2 px per cycle, for ever**
   (measured: 302 -> 304 -> 306 -> 308 on every maximize/restore cycle in per-client mode, and in
   single-window mode 1242 -> 1244 -> ... -> 1250 and on, until the window filled the screen and
   flickered). `contentBounds` now uses `absoluteBounds` - the window itself - and the host window's
   own frame is what stands in for the border.
2. **The platform applies a window size in two steps, and the step in between was read as a drag.**
   The resize callback arrived with the *new width and the old height* (202x800 for a viewport of
   202x202), which is neither size, so `isUserResize` said yes and the loop above ran even with the
   borders fixed. `JavaFxBackend` now marks a viewport it has just set (`viewportPending`) and ignores
   the platform's reports until they actually equal it, putting its own size back meanwhile; the flag
   clears the moment the two agree, so a drag a frame later is not swallowed.

**Verified after those two**: single-window with xclock *and* xeyes - the window is the union of the
two (500x428) and **stays** there (four samples over twelve seconds, identical); per-client with
xclock - the maximize/restore toggle is **exact** (300x328 <-> 1511x885, three cycles, no drift);
475 tests green.

**The edge-drift, which the user narrowed down for us** (2026-10-04, same night): "dragging the right
edge leftwards and the bottom edge upwards are fine; dragging the top edge downwards and the left edge
rightwards drift". Reproduced with xclock in per-client mode: a left-edge drag of **40 px** moved the
window 40 px and shrank it by **5** (300 -> 295), and the server's own log of the sizes it was asked
for bounced instead of falling:

```
[dbg] request 265x300 viewport=(51,51,300,300)
[dbg] request 298x300 viewport=(51,51,300,300)      <- up again
[dbg] request 297x300
[dbg] request 295x300
```

**Why the two edges differ, and it is one asymmetry.** A right or bottom edge drag is *size only*:
the platform computes the new size from the pointer against the edge that stays put, our server
resizes the client's window to it, and the two agree step for step. A left or top edge drag changes
the window's **origin as well as its size**, and the platform's arithmetic for the next step is
anchored on the window it just made. This server, meanwhile, was re-sizing the host window to the
viewport on every tick - and the viewport is the client's window, which is always one step behind the
pointer - so each of our corrections moved the platform's anchor and the drag lost ground (the bounce
above; 40 px of drag produced 5 px of shrink).

**The fix**: while a user resize is in flight, the backend moves the **pixels** and leaves the
**window's size** alone (`JavaFxBackend.userDragPending`, set when a resize is reported and cleared
when the client's window has taken the size, with a one-second safety net). The drag keeps its
anchor, the client's window catches up, and the viewport then matches - so the two never correct each
other mid-gesture. The guard that ignores the platform's half-applied size
({@code viewportPending}) was narrowed at the same time to *only* the mixtures of the old and new
sizes, because the first version of it ignored anything that did not match and would have swallowed a
real drag for ever.

**Verified**: 475 tests green; the growth and flicker with xclock/xeyes stay fixed (that is what the
narrowed guard is for). **Not verified by script**: the four edge drags themselves - the synthetic
drags could not reach the host window in this session (an IDE window covering it takes the events, and
macOS delivers a drag to whatever is under the pointer), so this one is for the user to try by hand,
with the numbers above as what "fixed" should look like: a 40 px left-edge drag must move the window
40 px and shrink it 40 px.

**The guard above was half the fix, and the user was right that it still drifted** (2026-10-04). With
`userDragPending` in, a scripted left-edge pull of 40 pt on an xterm moved the window's origin **40 pt**
and shrank it by **156** (784 -> 628), in steps of one character cell (13). The second half was found
by logging every change of the frame's size with the frames of this package that asked for it - the
scene's own listener cannot tell the platform's live resize from the backend's own `setWidth`, and
telling them apart was the whole diagnosis:

```
frame width 784 -> 777 pt | ... lambda$openWindow$11      <- the platform, from the pointer
frame width 777 -> 776 pt | ... lambda$openWindow$11
frame width 776 -> 774 pt | ... lambda$openWindow$11
frame width 744 -> 732 pt | ... sizeFrameToContent | fitWindowToViewport | flushDamage
```

**The root cause: `fitWindowToViewport` runs at the top of `flushDamage` - every flushed frame - and
it knew nothing about a drag.** So while the user held an edge, the frame was pulled down to the
*client's* window size, which is one round trip and one snap behind what the pointer is asking for,
and the platform then worked its next live-resize step out of that smaller frame. That is why the two
edges differed: a right- or bottom-edge drag recomputed from the same numbers every step, while a
left- or top-edge drag carried the origin along at the platform's rate and the size at the runaway's
rate - the window slid sideways. `fitWindowToViewport` now refuses to size the frame while a drag is in
flight, exactly as `setViewport` does.

**And then the snapped remainder, which is what makes all four edges behave alike.** A client with
resize increments will not take the size the pointer asks for - xterm snapped 744 to 732 - so sizing
the frame back to the content moves the *far* edge by the snapped 12 pt, which is the drift the user
saw in miniature. `JavaFxBackend` now remembers the frame's geometry as of the last time no drag was
in flight (`settledFrame*`), works out which edge moved when the drag ends (the left or top edge is
the one that moves the window's origin), and puts that edge back where it was
(`sizeFrameToContent`). The settle is driven by a `PauseTransition` restarted on every resize report
rather than by the next flushed frame, because a client that has stopped drawing - which is what a
window someone just finished resizing usually is - left the band of nothing in place for as long as it
felt like it (measured: still there 2.5 s later).

**Verified** (scripted drags, `/tmp/xsn/hostdrag`, on the bed at `.152`; the far edge named in
brackets must not move):

| drag | per-client, xterm (snapping) | per-client, xclock (hints) | single-window, xclock |
| --- | --- | --- | --- |
| left edge +40 | x 626->678, w 784->732, [right 1410] | x 80->120, w 300->260, [right 380] | x 106->146, w 300->260, [right 406] |
| top edge +40 | y 386->409, h 372->304, [bottom 741] | y 38->78, h 328->288, [bottom 366] | y 81->121, h 328->288, [bottom 409] |
| right edge -40 | w 771->719, **x 639 unchanged** | - | x 106 unchanged |
| bottom edge -40 | h 355->304, **y 386 unchanged** | - | y 81 unchanged |

(The left/top rows are the ones that drifted; both the 40 pt of pointer travel *and* the client's snap
are visible in them, and the bracketed edge is the same number before and after every one.) A drag that
**pauses for two seconds in the middle** - the case the one-second deadline exists for - also lands
with the far edge exact (824 before, 824 after). Single-window mode with xclock+xeyes together stays
where it is (four samples over twelve seconds) and 475 tests are green.

### [ ] A40. Un-maximizing restores the size but not the position. **M**

**The observation** (2026-10-04, while measuring A39's drags). An xterm's host window was moved by its
title bar to **506,286**, double-clicked to maximize (**0,38 1511x882**), and double-clicked again: the
size came back **exactly** (680x270) and the position did not - the window landed at **40,38**, and a
second round from 440,286 landed at 40,38 again. It is not a regression from the drag work: the same
sequence on the pre-change build (HEAD, with the drag fix reverted) gives 40,38 as well.

**Why it is worth a look anyway.** "Double click to maximize and double click to restore" is what the
report asked for, and a normal window comes back *where it was*; a fixed corner is the kind of thing
that reads as "the behaviour differs from click to click". Nothing in this server sets that position -
`keepOnScreen` can only clamp, `setWindowPosition` is called when a session opens - so the first thing
to read is what the platform does with `stage.setMaximized(false)`: the fill is this server's own
geometry (`filledByServer`, `enforceWindowSize`), so macOS's saved pre-zoom frame and our own resizes
may simply not be the same frame by the time the un-zoom is asked for. `JavaFxBackend` keeps
`restoreContentWidth/Height` for the size already; a `restoreX/Y` taken when the fill happens, applied
with `sizeFrameToContent` when the restore comes back, is the shape of the fix - but it needs the
same care as A38's settle, because the platform's own un-zoom arrives behind ours.

### [x] A41. Full screen leaves nothing grown, often will not come back, and takes the Dock with it. **L** - fixed 2026-10-04

**The report.** "单窗口模式全屏和退出全屏不符合预期，本来应该跟最大化和退出最大化一样，能变大和恢复，但是现在不会，经常恢复不了，有时候全屏焦点还会被桌面抢走，造成奇怪的问题，甚至导致dock栏消失，要锁定和解锁才能恢复dock栏，但是没有渲染x窗口的时候，全屏和退出全屏是正常的。"

**What was measured** (single-window mode, a client that maps a window, asks for
`_NET_WM_STATE_FULLSCREEN` and stays connected - `FsProbe`, which is the only way to hold the state
while watching it):

```
02:19:28.953  host window fullScreen false -> true        <- the state arrived, the Space is entered
02:19:29.672  window now shows (0,0,1512,945)             <- the client's window grew to the display
02:19:30.086  host window maximized false -> true         <- and macOS calls a full screen window a zoom
02:19:30.086  filling the window to 1512x922 (display 1512x950)   <- A38's fill, on a full screen window
02:19:30.124  window sized to 1512.0x950.0 for content 1512x922
02:19:30.848  host window fullScreen true -> false        <- out of the Space it goes
02:19:30.853  host window maximized true -> false (1512x917)
```

**The cause, and it is one line of consequence.** macOS reports a **full screen window as maximized**
as well, so `JavaFxBackend`'s maximize handling - A38, which fills the window to the display's usable
area - fired about a second after the Space was entered and *resized a window that was in its own
Space*. A full screen window whose frame is changed leaves the Space **without giving it back**: the
picture jumps back to where it was, the front application becomes whoever was behind (the user's
"焦点被桌面抢走"), and the menu bar and the Dock do not come back until the session is locked and
unlocked - which is exactly what was reported, and it is why the mode worked with no client window:
nothing of ours sizes the frame when there is nothing to size it for.

Three more things fell out of the same reading, each of which the report names:

- the fill **shrank** the client's window from the full screen area (1512x945 -> 1512x922), and the
  zoom then flipped back to false, so the un-maximize path restored a size that was read from a scene
  that was no longer the frame the platform had - "经常恢复不了";
- the **Dock** is macOS's own reaction, above;
- **and nothing ever grew the client.** A full screen window is only useful if the application gets
  the bigger window, and by X's rules that is the window manager's job - a client that asks to be
  full screen expects to be resized. It happened by accident here (the platform's own resize of the
  frame arrives as a scene resize, which the server reads as "the client's window should be this
  size"), and the same accident is what restored it on the way out.

**The fix: the frame is the platform's for as long as the window is full screen, and nothing else
about full screen is special.**

- `platformOwnsFrame()` answers yes for a drag of the frame's edge *or* a full screen window, and every
  path that sizes or moves the frame asks it: `setViewport` (the canvas still follows the viewport, as
  it does for a drag), `fitWindowToViewport`, `sizeFrameToContent`, `keepOnScreen`,
  `enforceWindowSize` and the drag-settle timer. This is the whole of the Dock fix.
- The `maximizedProperty` listener returns while the window is full screen: **macOS's zoom report is
  not the user's maximize**, and acting on it is what resized the Space's window in the first place.
- `setMaxClientArea` remembers the limit and defers it while full screen (`applyMaxClientArea`, applied
  when the window leaves), because a constraint put on a full screen window is another way to make the
  platform resize it.
- `windowMovedByUser` ignores a move while full screen - it is the Space putting the window at the top
  left of the display, and remembering that would reopen the client's window in the corner.
- A `fullScreenProperty` change clears `dragJustEnded`: which edge a drag pulled before the window went
  full screen has nothing to do with where it comes back to.
- **The key that leaves full screen is ⌃⌘F, not Escape.** JavaFX's default is Escape and JavaFX
  consumes it before the scene sees it, so a forwarded application in a full screen window would never
  be given its own Escape - which a terminal, an editor and an IDE all need - and pressing it would
  throw the window out of full screen instead. `Ctrl+Meta+F` is macOS's own combination, so leaving
  full screen still works the way every other application on the system does.

**Verified by script** (`FsProbe`/`FsProbe2` from the Mac against the bed, per-client and
single-window):

| what | result |
| --- | --- |
| the client's window on entering full screen | 400x300 -> **1512x945**, held for 12 s sampled every 500 ms |
| the client's window on leaving (`_NET_WM_STATE` removed) | back to **400x300** within 1 s |
| the host window | back at **106,81 400x328** - exactly where it was before - and no `filling`/`maximized` line in the log at all |
| a client that **reconfigures its own window while full screen** (500x400) | the restore still lands on **400x300**, the platform's own frame, not the size it had while full screen |
| the user's own exit (**⌃⌘F**) | the client comes back to 400x300 and the window to 106,81 400x328 |
| **Escape** while full screen | no longer leaves full screen (so the forwarded application gets its Escape) |
| per-client mode | identical numbers |

475 tests green. The maximize/restore path itself is untouched for a window that is not full screen
(the A/B against the pre-change backend shows the same behaviour), and the per-client A38 measurements
still hold.

**Left undone, deliberately: the client is not told.** When the *user* leaves full screen through the
platform (⌃⌘F, the green button), the client's own `_NET_WM_STATE` still carries
`_NET_WM_STATE_FULLSCREEN` - a real window manager removes the atom and lets the client see it in a
`PropertyNotify`, so an application that draws a full screen UI would keep drawing it until it
toggles itself. Our server keeps the list exactly as the client set it (`AtomHandlers` writes it from
the client's message), so this needs a sink call from the backend plus a property write on the server
side. It is not done here because of the way that can fail: `AtomHandlers`' own comment says several
clients ask again for ever when they are ignored, and an application that re-asserts the state it
already has would bounce the window in and out of the Space - worth doing with a testbed client that
re-asserts, not in the same change as the fix above.

### [x] A42. The platform pointer is not handed back after leaving full screen. **M** - fixed 2026-10-04

**The report.** "全屏后退出后，这时候鼠标会消失，拖回到x服务窗口恢复，这时候再拖出x窗口到桌面正常，全屏时鼠标正常，退出全屏，不在x服务窗口鼠标就消失" - after leaving full screen the pointer is gone over the desktop, it comes back once it is over the xserver window, and the trip after that is right.

**What it is.** Hiding the platform pointer is a loan (see `hostCursor`): over the window the server draws the client's own cursor and asks macOS for a **process-wide** `[NSCursor hide]`, and everywhere else it hands the arrow back with `Cursor.DEFAULT`. Whether the pointer is "over the window" came only from JavaFX's `MOUSE_ENTERED`/`MOUSE_EXITED`, and the case those miss is **a window that moves or changes size under a pointer that does not move** - which is what full screen is made of. The window shrinks away from a stationary pointer on the way out, no leave event arrives, the answer stays "over the window", and the arrow stays hidden: the sprite that would have been the visible pointer is clipped by the canvas, so the pointer is simply gone over the desktop. Coming back inside and leaving again delivers a real leave event, which is why the next trip is fine - the shape of the report exactly.

**The fix.** The answer is taken from **where the pointer really is** (`java.awt.MouseInfo`, compared against the window's frame in `JavaFxBackend.pointerInside`) rather than from the events alone, and `applyHostCursor` is run again whenever the window's position or size changes - so the entry and the exit of full screen, and every drag of an edge, re-decide it. The events still give the answer the moment they happen; this only removes their being the *only* source. The decision is logged when it changes ("host pointer hidden/handed back: over the window=..., application active=..., idle=..."), which is the breadcrumb that says which of the three inputs moved.

**Verified**, and in the direction that matters: leaving full screen with the pointer parked outside the window, the log hands the arrow back **28 ms after `fullScreen true -> false`**, and the decision is taken from the geometry alone - with the leave-event handler temporarily disabled the same line still appears, and the event itself does not arrive until **600 ms later**. So the case that was reported (no event at all) is covered by construction. 476 tests green, including a new one pinning `pointerInside` (the corner that belongs to the window, the right and bottom edge that do not, and the reported pair: the same point inside a full screen window and outside the window it came back to).

**Two traps that cost real time here, and both invalidate a measurement rather than a fix.** The cursor's visibility **cannot be measured from a shell on this machine**:

- `screencapture -C` draws the cursor **even when it is hidden**: with an AppKit tool holding `[NSCursor hide]` (the cursor is visibly gone), a capture with `-C` still differs from one without it by the whole cursor shape at the pointer (measured 439 px). Any "is the cursor there" test built on it is meaningless.
- `CGCursorIsVisible()` is deprecated *and* always answers `true` - it read "visible" with the cursor hidden by the same tool.

So the platform's side of this is the **user's to confirm**; what is verified here is the decision, which is what was wrong. A second, opposite desync is recorded below rather than fixed.

**The other direction, measured but not fixed: the hide can also fail to take effect.** After a full screen transition, macOS re-shows the pointer on its own (the Space switch is an activation), while JavaFX's `MacCursor.isNSCursorVisible` flag still says "hidden" - and `set()` skips the native call when the flag already matches, so the arrow can stay drawn over a window whose sprite is meant to be the only pointer (two pointers). It cannot be forced through the public API without calling `[NSCursor unhide]` an extra time, which AppKit counts - an extra unhide makes the *next* hide fail instead, so a "pump" trades one symptom for the other. Worth another session with a way to read the platform's own state (a private API, or `NSSystemCursorIsVisible`-style), not worth guessing at.

### [x] A43. JavaFX 27, and the window's title bar drawn by the toolkit. **L** - done 2026-10-04

**The request.** "把javafx的版本升级到27了，请使用headerbar作为标题栏，并完成javafx端的渲染适配，要完整测试，headerbar能更好地兼容整个页面，效果更佳，headerbar高度固定为30".

**What JavaFX 27 gives.** `StageStyle.EXTENDED` (a conditional feature, `EXTENDED_WINDOW`) is a
window whose platform title bar is gone and whose header buttons are drawn as an overlay on the top of
the *scene*, plus `javafx.scene.layout.HeaderBar` - a layout container for `left`/`center`/`right`
content that carries the drag-to-move and double-click-to-zoom behaviours the platform would have
provided. It also needs **`javafx.enablePreview=true`**: the feature throws from
`PreviewFeature.checkEnabled` (JavaFX's own message: "can only use preview features when the following
system property is set") unless the application opts in, and the property has to be set before the
toolkit reads it. That is done in `JavaFxBackend`'s static initializer, which is loaded before the
toolkit starts and therefore the earliest the backend can speak for itself; an application that has
already answered keeps its answer, and one that says no gets the platform's decorated window with no
header bar of ours - the only way not to end up with two title bars.

**The header bar.** Fixed at **30 points** (`min`/`pref`/`max`, which is what overrides the bar's
default of the platform's own title bar height), with the client's title in `center`. The title label is
marked `HeaderDragType.DRAGGABLE_SUBTREE`: content is not draggable by default, and the window's name is
exactly where a hand goes to move it. The left area needs no inset of its own - the bar pads it for the
platform's header buttons - and nothing is put there any more: the client's icon used to be, and the user
asked for it out (2026-10-04). The title is **bold**, by `-fx-font-weight` rather than by a font value,
because the platform's own title is the system UI font in bold and naming a family here would freeze the
size and the family along with the weight that was actually wanted.

**The rendering adaptation, which is what "content" means now.** The window's scene is the *whole*
window - header bar and all - while what the client is shown is the canvas below it, which changes
every piece of geometry that used to assume "scene == content":

- `contentWidth()/contentHeight()` read the window's own size minus the header bar and the platform's
  frame, and `frameWidth()/frameHeight()` add them back. `windowHeight(content, header, border)` is
  package-private and tested: two different things sit above the content and both are easy to forget -
  the header bar is *inside* the scene and the platform's title bar is *outside* it.
- `sizeToScene()` was replaced by `sizeFrameToContent()`. What `sizeToScene` measures is the root's
  *preferred* size, and with the content in a `BorderPane` centre that is the header bar's own height:
  the window opened as a 30 point strip, and the platform's own resize of it arrived as "the user
  dragged the frame" and shrank the client to 21 points tall (measured before this line existed).
- The pointer mapping subtracts the canvas's place in the scene (`contentOrigin()`), asked of the
  canvas rather than assumed to be 30, so full screen (where the bar is gone) needs no special case.
  Verified: a click 10 px inside the content's top-left lands at screen (11,61) for a window showing
  the rectangle at (1,51) - exactly right.
- Mouse events on the chrome are **not** forwarded to the client: the header bar is inside this window,
  so the toolkit delivers them to the scene, and a press or drag there is the window being moved or
  zoomed. Without the guard, every attempt to move the window by its title delivered a click at the
  top row of the screen. A button that is already held is the exception (a drag that wanders out of
  the content keeps going), and an unmatched release is dropped rather than sent.
- The cursor policy asks about the **content**, not the window: the header bar is the platform's
  chrome, the client's sprite is not drawn there, and its buttons are pressed with the platform's own
  arrow. (A42's decision stays, one rectangle narrower.)
- Full screen hides the header bar - `managed` false as well as invisible, or the layout keeps the gap
  - so the client gets the whole display (1512x945 measured) and the bar is back on the way out.
- A window on its way open passes through sizes nobody asked for (the scene shrinks to its root's
  preferred size before the platform applies ours: 0x374, 0x66, 784x66 ...). They are the frame
  settling, so while `openingUntil` runs the frame is put back and the client is told nothing.

**The maximize was re-designed rather than ported, and it is a JavaFX 27 change.** A38 replaced the
platform's zoom with a fill of our own because macOS worked its zoomed frame out from the window's
*content* - for a forwarded window, a client's X window at whatever size the client last asserted
(measured 2026-10-03: 1280x828 -> 1280x886 -> 1512x886, restoring nothing). **That is no longer what it
does**: measured 2026-10-04 under 27, a 784x344 window zooms to 1504x853, which is the display and not
the client. And the fill has a cost of its own now: setting a window's size while macOS has it zoomed
**un-zooms it**, so the fill was undone within the second, the state flipped back, and the window
oscillated - the second double click restored nothing and left the window at neither size (the same
was measured on the pre-change revision, so it is the upgrade, not this work). So the zoom is left
alone: its resize arrives on the scene like any other, the client's window takes the size the window
now has, and the restore is the platform putting its own frame back - exactly as full screen works
(A41). The one thing added is a **settle on the gesture** (`platformSettle`, 160 ms of the frame
holding still) because the zoom is animated

    measured: 785, 787, 798, 858, 875, ... on the way to 1511

and telling the client every intermediate made it chase the animation, while its own re-assertion
after each one left the window flapping between 1511 and 1499 twice a second. What the client is told
is the size the frame *stops* at, once.

**Verified** (scripted, on the bed; the window is the *frame*, and every "far edge" number below must
not move):

| what | result |
| --- | --- |
| a window with an xterm | opens **784x374** = content 784x344 + the 30 point header |
| dragging the header bar | 106,66 -> 146,106, size **unchanged**, and **0 button events** reached the client |
| double click on the header | **0,38 1511x884** (the platform's zoom) |
| double click again | back to **146,106 784x374** exactly |
| left edge +40 / -40 | far edge 1512 -> **1512**, twice |
| top edge +40 / -40 | far edge 423 -> **423**, twice |
| right edge -40 | origin 741 -> **741**; bottom edge -40 | origin 66 -> **66** |
| a click 10 px inside the content | lands at screen **(11,61)** = the viewport's (1,51) + (10,10) |
| full screen | the header bar is gone, the client gets **1512x945**, and 400x300 comes back on the way out |
| `mvn -o test` | **477 green** (the new one pins `windowHeight`) |

**Two known artifacts, both from the platform's zoom rather than from this work** - recorded rather
than papered over:

1. **A maximized window is up to one client increment wider than what the client takes.** xterm will
   not take 1511 and takes 1499 (a whole number of character cells plus its border), so a 12 point
   strip of the window is not covered by the client's picture (measured: white up to x=1499, the
   window's own light grey from 1500 to 1510). A window manager would clamp the zoom to the hints
   before setting the frame, and we cannot: the frame a macOS-zoomed window is not ours to set - doing
   so is what un-zooms it and made the previous design oscillate. The user's own clients (EasyShell,
   termora) do not snap, so they fill the window; a terminal that does will show the strip.
2. **The first left or top edge drag after a maximize/restore cycle can slide the window.** macOS's
   live-resize reference frame can still be the *zoomed* one: measured after a restore, a left-edge
   drag of 40 points moved the window's origin from 0 to **779** and kept the far edge at the zoomed
   1511, i.e. it resized against the wrong edge. A drag from a window that has never been zoomed is
   exact (the table above), and the drags after the first are exact too, so this is the platform's
   stale reference rather than the anchor arithmetic (A39) - which is why it is written down instead of
   guessed at.

**Its own follow-up, reported the same day: the pointer was hidden over the header bar.** "从x窗口往下到
标题栏下方边界至桌面这部分区域，也就是整个标题栏区域，鼠标是隐藏的，不符合预期，导致我想按一下最小化
或者全屏按钮都很困难". Two things were wrong, and both are fixed:

- **The decision was not re-taken when the pointer crossed from the page to the chrome.** It is taken
  from where the pointer really is (A42), but only *asked* on the events that used to matter: entering
  and leaving the window, a focus change, an idle change and the window's own geometry. Crossing from
  the content to the header bar is none of those - the scene is entered and left, not the window - and
  `enterWindow` sees the flag already set and does nothing. So the arrow stayed hidden for as long as
  the pointer was over the header, which is where the window's own buttons are. A move now re-decides
  (`pointerMoved` calls `applyHostCursor`), which is a `MouseInfo` query and a no-op when nothing
  changed. Verified by the log: content -> "host pointer hidden", header bar -> "**host pointer handed
  back**", content again -> hidden.
- **The client's sprite was still drawn there.** The sprite is the *client's* pointer and has no place
  over the chrome, but the pointer's X position stays where it last was over the page, so a sprite was
  left sitting on the strip the header bar occupies - a second pointer beside the platform's arrow, on
  the one strip where the window's buttons are. `drawCursor` now skips it while the pointer is over the
  chrome, and a change of the decision repaints the sprite's own box so the frame without it is what
  erases it. Verified by comparing two captures of the same window: with the pointer over the page the
  only difference between them is a 24x30 pixel box at the sprite's place; with the pointer over the
  header bar that box is the framebuffer, i.e. clean.

477 tests green.

**Two more of its follow-ups, both reported together (2026-10-04)** - "全屏反应速度有点慢，正常窗口200ms
就完成了，这个xserver要超过1秒" and "如果拉伸过窗口，这时候按住标题栏拖动窗口，改变窗口位置，窗口经常
弹回去到旧位置":

- **Full screen took over a second because the display tick was blocked by the transition.** The client's
  window was resized 19 ms after the full screen state changed, the server's tick applied it 34 ms in -
  and then the canvas did not follow for another **658 ms**. The cause is `stage.setFullScreen`, which
  does not return until the whole Space animation is over, called through `runOnFxThreadAndWait` from
  the tick: the tick waited, so everything the tick does after it - the viewport above all - waited with
  it. It is now submitted with `Platform.runLater`, which nothing needs to wait for (the server has
  already decided, and the toolkit runs these in the order they were submitted). Measured after the
  change: the size the window went to is reported 19 ms in, the client's window takes it **34 ms** in
  (`growing the screen ... to 1512x945` 14 ms after the report). What is left is the picture: the
  platform's transition holds the toolkit thread, so nothing queued from the tick - the canvas resize
  among them - runs until it is over, and the old picture in the new window for the length of a Space
  animation is what the animation looks like from the inside. Fixing that needs the client's window to
  be resized *before* the transition starts (the server would size it to the display the platform is
  about to use, from the request rather than from the scene arriving), which is worth doing on its own
  rather than in the same change as a state fix.
- **Dragging the window by its title bar after a resize pulled it back to where the resize began.** The
  anchor A39 puts back the far edge of a *resize* is worked out from where the frame stood when the
  resize started (`settledFrame*`), and it was armed by the resize and then left armed: a *move* is the
  same kind of change to the frame's x or y that a left or top edge drag makes, so the next move
  out of the title bar was read as "the same resize, one step on" and the window snapped back
  ("拉伸过窗口，这时候按住标题栏拖动窗口...窗口经常弹回去到旧位置"). The two are told apart by the
  question the platform already answers: **a live resize of an edge delivers no mouse events to the
  window at all** (measured 2026-10-04, and A39's whole design rests on it), while a title-bar drag
  delivers the press and the moves - so a button that is down when the frame's position changes means a
  move, and a move now clears everything a resize left pending before recording where the window is.
  Verified: resize the left edge +40 (146,66 744x374), then drag the title bar by +90,+70 immediately -
  the window ends at **248,136**, where the hand put it, rather than back at the old position.

477 tests green.

### [x] A44. Single-window mode's window with no clients. **S** - asked about and changed 2026-10-04

**The question.** "单窗口模式你帮我确认下，默认显示窗口是正常的么，还是说可以做成没有任何x客户端连接的
时候，可以默认隐藏主窗口，有连接了再显示并置顶聚焦一次".

**What it did.** Two halves, and only one of them was what the question assumed: with no client *at the
start* there is no window at all - the tick does not open one until the first window a client maps
(`the window is opened by the first window a client maps, so a connection that only asks questions never
opens one`) - but once a window had been opened it kept it: a session that goes idle **holds its last
frame**, which is a deliberate design ("the window holds its last frame", and eight tests assert it,
`HostWindowRaiseTest` and `XServerShutdownTest` among them). So the change is the second half: the
window goes when there is nothing left to frame.

**What it does now.** A session with nothing to show keeps its window for `IDLE_WINDOW_GRACE_MILLIS`
(1500 ms) and then the window is closed - below the lock, where closing a window may wait on the
toolkit's thread - and opened again by the first window a client maps, which was already where the raise
was. The grace period is the part that is not obvious: several toolkits unmap their first window and map
another a moment later while they start up, and a window that disappears and comes back in between reads
as a flicker rather than as an application - which is exactly what the eight tests are about, and they
are all still green with it (they do not wait a second and a half). Only a *window* is closed: a headless
or VNC session has none, and a VNC viewer keeps watching a screen no client is drawing on.

**Its own follow-up, the same day: the window was shown once at startup and then hidden.** "我以单窗口
模式启动时，会先显示一次窗口，然后过一会这个窗口消失，我不想要这样，我要跟per-client一样保持静默，直到x客户端
连接才显示真正的窗口，当所有x客户端断开，应当马上关闭窗口，保持和保证设计上的一致性". Two halves, and the
first one is the interesting one:

- **The window is no longer shown when it is built.** The backend is still created when the server starts
  (that is what the VNC, tray and in-process paths need: a VNC server serves viewers before any client
  draws, and a test's fake backend is the window it asserts about), but showing is the *server's* answer
  rather than the backend's own decision: `setIdle(false)` shows it, and a session with nothing to show is
  idle from the first tick. Everything else about the window - its size, its placement, its decoration -
  is measured when it is first shown, because until then the platform has no frame to measure.
- **And it is hidden the moment the session goes idle** - the last client vanishing, or a client whose
  windows are all gone - which is the same rule a per-client window follows when its client disconnects.
  Hidden rather than closed: the backend belongs to the session, and the server still decides when a
  window stops existing (it closes the backend of a session that has had nothing to show for the grace
  period above).

**And the first attempt at that still flashed, which the user caught by reading this file**: the backend
was left to decide for itself with `if (!idle) showWindow()`, and a backend's own `idle` is false until it
is *told* - the server told it on a later tick than the one that built it, so the window was shown and
hidden again inside the first two ticks ("我看了下还是会极短地闪一下窗口，并且这行代码明显被调用了").
The state now comes first: the session is marked idle before its backend is built (`masterSession.setIdle`
in `start`), and `openSessionBackend` tells the new backend what the session is before it returns - so the
first thing the window hears is "there is nothing to show", and `stage.show()` is never reached. Verified
at startup: the log has **no `window shown` line at all** and the window list is empty.

This is what "silent until a client connects" means, and it is where the two modes agree: verified in
single-window mode, a server started with no client has **no window at all**, a client connecting puts the
window on the screen *first in the window list* (raised and focused, above), and killing the client takes
the window away **within 400 ms** (`window hidden: nothing to show in it`). 477 tests green with no change
to the server or to any test - the whole change is the JavaFX backend's own show/hide.

**And it is raised *and focused* now**, not only raised: `DisplayBackend.raiseAndFocus` is new, used for
"a window has just appeared for a client that has started drawing" - the one moment the user is waiting
for it - while the urgency hint keeps calling `raise()` alone, because a client saying "look at me" has
not asked for the keyboard away from whatever the user is typing into.

**Verified** on the bed in single-window mode: a client connecting puts the host window **first in the
window list** (front to back); killing the client leaves the log saying `nothing to show ... the window
holds its last frame`, then `has had nothing to show for 1500 ms: its window is closed`; connecting again
gives a window with a **new window id** - a new stage, so the old one really was closed - and it is the
front window again. 477 tests green.

The `--single-window` trade-off from A39 is unchanged and still the thing to judge: the window shows the
clients' windows, not the whole screen.

### [x] A45. The project is cross-platform, and one thing in it was not. **M** - fixed 2026-10-04

**Asked for.** "这个项目是跨平台的，你检查项目所有代码和配置，不单单只考虑macos这一个系统，记录进文档，严格
执行，我记得你有个全屏退出快捷键就是仅适配macos".

**The audit** (every `src/main` file, and the configuration) is in `docs/design-notes.md`'s **Platforms**
section: the table of everything platform-specific and what happens elsewhere, and the four rules a
change has to keep. Two things were wrong:

1. **The full screen exit key really was macOS only** (`KeyCombination.valueOf("Ctrl+Meta+F")`, and the
   reader of this project remembered it). It is now `JavaFxBackend.keyFor(osName)`: Control-Command-F on
   macOS, **F11** everywhere else - and it is the only place in the codebase that reads `os.name`. Which
   key it is happens to be a platform convention; *that Escape is not it* is not (JavaFX consumes its
   exit key before the scene sees it, so a forwarded terminal, editor or IDE would be denied its own
   Escape). Pinned by a test that gives the answer for four platform names and for a platform that will
   not say.
2. **The unix domain socket created its directory before asking whether the platform has the family.**
   `/tmp/.X11-unix` is a POSIX path; on Windows it resolves against the current drive, so a machine that
   cannot use a unix socket at all was left with a `\tmp\.X11-unix` and *then* told the socket was
   unavailable. The channel is now opened first, so only what the platform supports is created; the TCP
   listener carries on either way, as it already did.

**Everything else that is platform-specific, and why it is not a violation**: the window's own title bar
is `StageStyle.EXTENDED` behind `Platform.isSupported(EXTENDED_WINDOW)`, with the platform's decorated
window as the fallback; `Cursor.NONE`'s process-wide hide and the "a full screen window is maximized too"
quirk are macOS behaviours that the code *handles* with guards that are inert elsewhere; the tray goes
through AWT's `SystemTray`; the display scale comes from the display; and the JavaFX preview feature the
header bar needs is opted into by the backend itself rather than by a command line (A43), so no platform
needs a special invocation. The tools under `tools/` are macOS-only (AppKit/CoreGraphics probes and ssh to
the test bed) and deliberately so: nothing under `src/main` depends on them.

478 tests green.

**And the owner's rule for the tools, which is stricter than the table**: "测试工具例外，如果要在其他系统下调试，
我会要求创建对应平台的测试工具，但是xserver本身不应该依赖系统平台". So the tools being macOS-only is by
design and is not a finding against them - what would be a finding is the *product* needing them, and the
audit's answer is that nothing under `src/main` does. The design notes now carry that reading as well as
the complete list of every place `src/main` touches the platform at all (one `os.name` read, one
`File.separatorChar` comparison, one font *search list* that ends in the built-in tables, `user.home`, no
`ProcessBuilder` anywhere), so the next reader can check the list instead of the code.

### [x] A46. The dock's Quit does nothing in single-window mode. **S** - fixed 2026-10-04

**The report.** "发现一个小问题，单窗口下，macos在dock栏右键程序，点击退出没有反应，但是在per-client模式下
正常，寻找下原因，修复下".

**Reproduced, both modes, with the Dock's own request** (`NSRunningApplication -terminate`, a two-line
AppKit helper - the same `NSApplicationTerminate` event the Dock's Quit item sends, and the same trick
A27 used):

| mode | state | before: the Dock's Quit | after |
| --- | --- | --- | --- |
| per-client | idle | **gone in ~500 ms** | unchanged |
| per-client | a client's window up | **gone in ~500 ms** | unchanged |
| single-window | idle (**no window at all** - A44's state) | **nothing happened, ever** | **gone in ~500 ms** |
| single-window | a client's window up | gone in ~1 s (the window's close request) | gone in ~3.5 s (the ask-and-grace path below) |

The state that mattered is the idle one, and it is A44's own doing: before A44 a single-window server
had a window up from the start, and the Quit had something to close; since A44 the window is closed
while nothing is connected, and that is exactly when the Quit stopped working.

**The cause, and it is a JavaFX behaviour rather than this server's.** The platform asks *the
application* to quit, and **the application belongs to whichever toolkit claimed it first**:

- **AWT** answers the request by ending the process (the JVM's shutdown hooks run, and this server's
  hook is `Main`'s: `server.stop()`, which asks every client to close, waits the grace, and exits).
- **JavaFX (Glass)** turns the same request into a close request on each of its **visible** windows -
  `QuantumToolkit$2.handleQuitAction` is `GlassStage.requestClosingAllWindows()`, and that loop skips
  a window whose `isVisible()` is false - so an application with no window on the screen answers the
  platform with **nothing at all**, and the request hangs unfinished. Measured with a minimal JavaFX
  program: a hidden stage and the Dock's Quit did nothing; the same program with one AWT call before
  `Platform.startup` was gone in half a second.

**Why the two modes differed.** Per-client mode's clipboard bridge (`HostClipboard.open`), tray bridge
and scale probe (`ServerOptions.hostScale`) all reach into AWT *while the server is starting* - before
any client has a window - so AWT has always claimed the application there. Single-window mode opens its
screen's window inside `openSessionBackend` **before** all of them, so JavaFX claimed it instead. That
is also why the report says per-client mode is normal: it is, by accident of the order of the calls,
and A27 had already recorded the underlying asymmetry ("a windowless JavaFX app hears nothing at all").

**The fix - `XServer.claimHostApplication`, called before any window can be built.** It asks for AWT's
toolkit, which is the claim; it is skipped where there is no host window at all (`--backend headless`,
`--backend vnc`: no dock icon, nothing to quit from), and a machine where AWT will not start is left as
it was, with a debug line. Explicit rather than left to the bridges that happen to do it today, because
every one of them can be switched off (`--no-clipboard`, `--no-tray`, `--scale 1`) and a mode whose
Quit works by accident is a mode whose Quit stops working the next time one of them changes.

**Nothing in this is a platform requirement** (`docs/design-notes.md`'s rules): AWT is the toolkit the
clipboard and the tray are built on everywhere, a process with both toolkits in it is the ordinary
JavaFX-in-Swing shape, and on a platform where the question does not arise the call is inert. It adds
no internal API use - the audit line "uses an internal API | nowhere" still holds, which is why this
was done by *claiming the application* rather than by hooking Glass's quit handler (that would have
been the alternative: `com.sun.glass.ui.Application.setEventHandler`, precise but internal).

**Verified live, in the mode that was broken.** Single-window, nothing connected: before, the Dock's
Quit left the process running for as long as it was watched (12 s, then killed); after, gone in 500 ms,
with `xserver: display :96 stopped` in the log. With `xclock` on the VM attached: the window is shown
when the client connects (header bar, 400x300 content in a 400x330 frame, empty window list while idle),
the Quit ends the process in 3.5 s and the client is asked to close rather than cut off. The two
behaviours most at risk from changing who owns the application were measured against per-client mode
(which has always been AWT-owned) and are identical: the platform pointer is **hidden** over the
content (`CGCursorIsVisible` 0) and **shown** over the header bar, repeatedly; the header bar still
drags the window (`hostdrag`, +60/+87 exactly); and clicks that do not reach a title bar button here do
not reach it in the pre-fix build either - that is the synthetic-click limitation the tools' notes
already carry, not a regression.

**One thing to know**: the process is registered with the platform as AWT's, which is what the Dock
shows for a bundle-less JVM. In single-window mode the label the Dock reports changes with it (from the
JavaFX default to the AWT/JDK one) - the same label per-client mode has shown all along; naming the
application itself (`apple.awt.application.name`) was not asked for and was not done.

478 tests green, unchanged (this is platform behaviour with no seam a unit test can reach - A27's
no-JavaFX backends were the only way to establish what the platform does, and the same helper is the
verification again).

### [x] A47. The XInput implementation is four releases behind a reference. **M** - fixed 2026-10-04

**Asked for.** "你上次提到xinput实现落后xfvb几个版本，回忆下，确有此事的话，也完善下" - and the
recollection is right: A36's re-measurement named it as **the one version gap on the whole page**
("a reference answers 2.4, this server answers 2.0 ... the one capability a *modern* client could
still reach for"). The version was 2.0 on purpose, with the reason written next to it: "this is the
version this server implements, and a client that is told 2.4 will ask for requests that would be
refused". Closing the gap therefore meant making that sentence false, not editing it.

**What a reference does, measured with `tools/XiProbe versions` (its new mode) on 2026-10-04**, one
connection per ask, on Xvfb `:98` and against this server:

| asked | Xvfb | this server, before | this server, after |
| --- | --- | --- | --- |
| 2.0 / 2.1 / 2.2 / 2.3 | the same back | always **2.0** | the same back |
| 2.4, 2.5, 2.9 | 2.4 | 2.0 | 2.4 |
| 3.0 (major ahead) | **2.4** | 2.0 | 2.4 |
| 1.0 (major behind) | **BadValue**, value 1, minor 47 | answered 1.0 | **BadValue**, value 1, minor 47 |

`GetExtensionVersion` (XInput **1.x**'s handshake, which is what `xinput` asks first) answers 2.4 on
both. Two details were invisible until this: a **second** `XIQueryVersion` on one connection is
answered with the version that connection already latched (2.0, 2.0, 2.0 for asks of 2.0, 2.3, 2.9) -
which is why the probe now opens a connection per ask, and why a test that asked twice measured
nothing; and a client whose **major** is ahead is not refused, it is told what the server speaks.

**What the four releases add, and what this server now does with each.**

| release | what it adds | here |
| --- | --- | --- |
| 2.1 | raw events (`XI_RawMotion` and the four around it), `XIGetSelectedEvents` | **implemented**: selected on a **root** window, delivered for every device event the server receives, including input that is delivered to nobody |
| 2.2 | touch (`XITouchClass`, `XI_TouchBegin/Update/End/Ownership`, the touch modes of `XIAllowEvents`) | **nothing to implement and nothing to lie about**: no device reports a touch class on this server *or on Xvfb* (measured with `XiProbe device <id>` - Button and two Valuators, no Touch), and `XIAllowEvents` with `XIAcceptTouch` is accepted in silence by both |
| 2.3 | pointer barriers: `XI_BarrierHit`/`XI_BarrierLeave`, `XIBarrierReleasePointer` | **the request is answered as a reference answers it**: a barrier that does not exist is refused with the extension's **own barrier error, code 12**, with the barrier as the bad value (Xvfb: error 141 of a base of 129; here: 40 of a base of 28). Barriers are *created* through **XFIXES 5.0** (`CreatePointerBarrier`), which this server does not have - see the item below |
| 2.4 | touch ownership semantics | nothing to implement, same reason as 2.2 |

**Raw events, which are the substance of 2.1 and the part that had to be built.** The wire layout was
measured off Xvfb with the probe's new `raw` mode rather than derived, and it is a **third** shape -
neither a device event nor a crossing:

```
0  type 35            16 detail (keycode/button; 0 for a motion)
1  extension opcode   20 source id         22 valuators_len = 2 words, measured, even for a key
4  length             24 flags             28 pad
8  evtype             32 the valuator mask, two words
10 device id          [40 the raw values, one FP3232 per axis in the mask]
12 time               [56 the processed values, the same shape]
```

| gesture (XTEST) | Xvfb | this server |
| --- | --- | --- |
| motion | 72 bytes, device 2, **source 4** (its XTEST slave), mask 3, values 100/200 twice | 72 bytes, device 2, **source 2**, mask 3, values 100/200 twice |
| button press/release | 40 bytes, empty two-word mask, detail = the button | the same |
| key press/release | 40 bytes, empty mask, detail = the keycode, device 3 | the same |
| `WarpPointer` | **nothing** | **nothing** |

The one difference is the source id: a reference names its XTEST slave (4/5) because it has one, and
this server has **no slave devices at all** (its device list is two masters where Xvfb's is six -
measured, and its own item below). Naming the device itself is the same choice its device events
already make, and it is the honest answer for a server with nothing else to name.

Three things about the delivery are worth keeping, each of them a measurement:

- **The root of the *screen the input belongs to*, not the server's screen.** A client is told about
  the screen it was given, and this server builds one screen per client in the mode a user runs, so
  the first version delivered to `server.screen().root()` - and the probe's selection sat on 0x9
  while the delivery looked at 0x2 (and, with a second connection driving XTEST, at *its* screen's
  0xc). XTEST is scoped to the screen of the connection that sends it, which is why the probe and the
  unit tests drive it from the same connection that selected.
- **A warp is not a device event.** Measured on Xvfb: six XTEST motions arrive as raw events and a
  `WarpPointer` sends nothing, because a warp is the server moving the pointer rather than the device
  reporting it. The test pins it, and the live check reproduced it exactly (`xdotool mousemove`
  produces nothing, `xdotool click` a `RawButtonPress`).
- **The error has to be inside the range the extension declared.** Registering XInput with **one**
  error code made code 12 land outside what `QueryExtension` had told the client, so the answer
  arrived as an unnameable error; the registration now says **thirteen** (XServer's registration
  site has the measurement: ours answered 40 where a reference answers 141).
- Also measured while implementing: the raw values are **`FP3232`** (two words: integer and
  fraction), not the `FP1616` a device event's positions are - writing `100 << 16` makes the integral
  part 6553600, and the reference's own bytes (`64000000 00000000`) are what settled it.

**Verified live with a real client.** `xinput test-xi2 --root 2` (libXi's own parsing, not a probe's)
against this server: **`EVENT type 17 (RawMotion)`** for XTEST motion and **`EVENT type 15
(RawButtonPress)`** for a click, with `device: 2`, `detail: 0`, and *nothing* after a
`xdotool mousemove` (a warp). Termora (GDK) renders through the same build with no new refusals, and
**GDK does not select the raw family at all** - measured from this server's own log: its masks are
bits 4-8, 18-20 and 27-32 (buttons, motion, crossings, touch, gestures), so the new events go only to
clients that ask for them. The suite is **482 tests, green** - four new ones pin the version rule,
the raw motion shape and values, the raw key shape, and the barrier error.

**What is left on this page, measured so the next item starts where this one stopped.**

- **Pointer barriers themselves** - the other half of 2.3. Xvfb's XFIXES is **5.0** and
  `CreatePointerBarrier` works there (measured: the request is accepted once the barrier id comes from
  the client's own range and the length counts the device list; a bad id is `BadIDChoice`, and
  `XIBarrierReleasePointer` for a real barrier is accepted in silence, where a made-up one is refused
  with error 12). This server's XFIXES answers **4.0**, so nothing can create a barrier and the only
  reachable answer for a release is the one above. Doing it means XFIXES 5.0, the two requests, the
  **`XI_BarrierHit`/`XI_BarrierLeave` events** (a 68 byte generic event - `evtype`, `deviceid`,
  `eventid`, `root`/`event`/`barrier`, `dtime`, `flags`, `sourceid`, `root_x/y`, `dx`/`dy` - delivered
  to clients that selected bits 25/26 on the barrier's window) **and the motion blocking itself**.
  **And the bed cannot measure the blocking**: a *registered* barrier on Xvfb does not stop an
  XTEST-driven pointer (measured 2026-10-04, absolute and relative motion, with and without a device
  list: the pointer walks straight through and no event arrives), so the semantics have to come from
  the reference's own implementation - `Xi/xibarriers.c`, fetched the same day: the hit is per master
  pointer, a hit clamps the pointer to the barrier until the owner releases it
  (`XIBarrierReleasePointer` sets the release id to the hit's event id), leaving the barrier's hit box
  ends the hit and starts a new event id, and the blocking is done in the screen's
  `ConstrainCursorHarder` hook. That is why this is a feature of its own rather than a follow-up.
- **A device model that can change** - `XIChangeHierarchy` (an XI **2.0** request). **Done, see A48** - the two XTEST slaves a reference has are now
  here, listed with its names, types, attachments and class source ids, and XTEST input carries the
  slave as its source. What is left is the *changing*: a device table that can grow (AddMaster /
  RemoveMaster / AttachSlave / DetachSlave) with the `XI_HierarchyChanged` event a client selects
  (bit 11 - GDK does, measured), and `XIPassiveGrabDevice`/`XIPassiveUngrabDevice` (54/55, also 2.0,
  still refused: a reference answers the first with a modifier list, empty when nothing conflicts, and
  accepts the second in silence).

### [~] A48. The device model: the slaves first. **M** - the first half done 2026-10-04

**Asked for** as the follow-up to A47: "继续完成你记录剩下的事项", whose second half was "a device model
that can change" - this server listed **two** devices where a reference lists six, and named the
**master** as the source of every event it delivered.

**What a reference's device list is, measured with `XiProbe device 0` on 2026-10-04:**

| id | name | type | attached to | classes (with the source id they carry) |
| --- | --- | --- | --- | --- |
| 2 | Virtual core pointer | 1 master pointer | 3 | button (13, 10 buttons) + two valuators, **sourceid 4** |
| 3 | Virtual core keyboard | 2 master keyboard | 2 | key (250, 248 keys 8..255), **sourceid 5** |
| 4 | Virtual core XTEST pointer | 3 slave pointer | 2 | button + two valuators, sourceid 4 |
| 5 | Virtual core XTEST keyboard | 4 slave keyboard | 3 | key, sourceid 5 |
| 6 | Xvfb mouse | 3 slave pointer | 2 | button (3 buttons) + two valuators |
| 7 | Xvfb keyboard | 4 slave keyboard | 3 | key |

Its XI 1 `ListInputDevices` lists the same six, with `use` 0 and 1 for the masters and **4/3** for the
slaves; and an XTEST-driven event carries **`device 2, sourceid 4`** - the master as the device and the
slave that produced the input as its source.

**What is here now.** The four devices a reference leads with: the two masters and the two **XTEST
slaves**, with those ids, names, types, attachments and class records, the masters' class records
naming the *slave* their class comes from (device 2's button class says sourceid 4, as a reference's
does), the XI 1 list carrying the same four with the reference's `use` values, and
`XIAllMasterDevices` still answering masters alone. **XTEST input now says where it came from**: the
XTEST extension marks its events with the slave that produced them (`InputState.setSourceDevice`
around each faked event), so both the raw events of A47 and the ordinary device events carry
`sourceid 4`/`5` - which is what a client that wants to know *which device* did something reads.
Verified live: `XiProbe device 0` against this server prints the reference's first four records, and
`XiProbe raw` prints `device 2, sourceid 4` for its XTEST motion.

**What is deliberately not here, and why the item is not closed.** A reference's last two devices are
its **backend's** ("Xvfb mouse"/"Xvfb keyboard"): the devices its own input arrives on. This server's
host input *is* the master's own, so it has no equivalent - and introducing one means naming a device
after the host, which is a design decision rather than a measurement. The changing half of the item needs the device
table to become mutable, and it is now **measured end to end** so the next slice starts from numbers:

- **`XIChangeHierarchy` with `XIAddMaster`** (measured 2026-10-04, name "Probe master", send_core 1,
  enable 1) creates **four** devices, not two: `<name> pointer` (master pointer, attached to the new
  keyboard), `<name> keyboard` (master keyboard), `<name> XTEST pointer` (slave pointer, attached to
  the new pointer) and `<name> XTEST keyboard` (slave keyboard) - ids 8, 9, 10 and 11 for the first
  master added to a fresh reference, and the device list goes from six to ten.
- **`XI_HierarchyChanged`** (evtype 11) is a 32 byte fixed part - `deviceid` 0, `time`, **`flags`**
  (0x55 measured = `XIMasterAdded | XISlaveAdded | XISlaveAttached | XIDeviceEnabled`) and `num_info`
  - followed by **one 12 byte record per device the server has** (`deviceid`, `attachment`, `use`,
  `enabled`, `flags`): the whole hierarchy *after* the change, so the event was 344 bytes with 26
  devices on the reference after repeated runs.
- **Each new device also produces `XI_PropertyEvent`s** (evtype 12, one per new device, naming it),
  which is what a client that watches properties sees when a device appears.
- **The device-state events are selectable on `XIAllDevices` alone**: bits 1, 11 and 12 on
  `XIAllMasterDevices` are **BadValue naming the bit** (error 2, value 11), and `XIAllDevices` takes
  all three - which is the selection GDK makes (measured). **That rule is implemented now** (and pinned by a test), because
  accepting it everywhere tells a client it will hear about a device appearing when it will not; the
  request itself is still refused until the table can change.
- Earlier measurements for the rest of the request: `AttachSlave` naming a device that is not a slave
  is the extension's `BadDevice` (base + 0) with the device as its bad value; **`XIPassiveGrabDevice`
  is answered with a reply whose `num_modifiers` counts the conflicts (0 when nothing conflicts) and
  `XIPassiveUngrabDevice` is accepted in silence - both are implemented now**, on the same table the
  core `GrabKey`/`GrabButton` use, with the XI2 mask translated to the core mask the delivery path
  works in (grab types 3 and up - enter/leave and touch - are refused with `BadValue`, since no input
  path here arms a crossing grab and neither server has a touch device).

**Verified with the real tool** (2026-10-04, the reference display and this server side by side):
`xinput create-master TestMaster` against this server creates the four devices and `xinput list`
draws the same tree a reference draws - `TestMaster pointer` (6) with its `TestMaster XTEST pointer`
(8) under it, and `TestMaster keyboard` (7) with its `TestMaster XTEST keyboard` (9) - and
`xinput remove-master "TestMaster pointer"` takes them away again, where removing
`"Virtual core pointer"` is refused by both servers (this one with `BadValue`, value 0, on minor 43,
the same answer the reference gives). That is `xinput` itself driving `XIChangeHierarchy` and reading
the result back, which is the end-to-end check the probe's own round only approximated.

**Two new findings this round cost the barrier item its measurability**: a *registered* barrier on
Xvfb does not stop an XTEST-driven pointer - absolute or relative motion, with or without a device
list, the pointer walks straight through and no `XI_BarrierHit` arrives (measured 2026-10-04). So
Xvfb's own barrier behaviour cannot be used as an oracle in this bed; the semantics have to come from
the reference's implementation, which is written up in A47's "what is left" with the event's field
list. 486 tests green (five new tests: the device records a reference leads with, the XTEST slave and the source id its input carries, the device-state events being selectable on XIAllDevices alone, `XIChangeHierarchy` creating the four devices with the event that reports them, and a passive grab being armed, answered and taken back).

### [ ] A49. Pointer barriers' blocking half - recorded, not built. **M** - 2026-10-04

**What is missing, precisely.** XI 2.3's barrier *surface* is here (A47): `XIBarrierReleasePointer` is
answered exactly as a reference answers it, and the selection rule for the device-state events is in.
What is not here is the thing barriers are *for*: **XFIXES 5.0** (this server answers 4.0, so
`CreatePointerBarrier` is refused with `BadImplementation` and no barrier can exist at all), the
`XI_BarrierHit`/`XI_BarrierLeave` events, and the pointer clamping - a barrier stops the pointer at its
line until its owner releases it (`XIBarrierReleasePointer`), which is what makes a hot corner or a
"keep the pointer in the guest" edge work.

**Why it is not being built now, and the honest reasons rather than a shrug.**

1. **No consumer in this bed, measured.** `libXfixes.so.3` on the bed does export the call (`nm -D`:
   40 `XFixes` symbols, `XFixesCreatePointerBarrier` and `XFixesDestroyPointerBarrier` among them - the
   calibration that says the check is real), and **none of the clients or toolkits here references it**:
   `x11vnc`, `xinput`, `xdotool`, `Xvfb` and **`libgdk-3.so.0`** (what the forwarded applications draw
   with) all have zero references. Barriers are used by compositors (Mutter's hot corners, KWin's edge
   actions) and by dedicated pointer-confinement tools - the *host desktop's* side of the world, not a
   forwarded application's.
2. **A client that wants them is already told the truth**: this server answers XFIXES **4.0**, so a
   client checks, finds the feature absent, and falls back - exactly as it would against an older
   server. Nothing is reported as done that is not.
3. **The bed cannot verify the blocking**, which was the deciding measurement (2026-10-04): a barrier
   that a reference *accepts* (and whose removal by `XIBarrierReleasePointer` it accepts too, so it
   really is registered) does **not** stop a pointer driven through XTEST - absolute or relative
   motion, with and without a device list - and no `XI_BarrierHit` arrives. Xvfb's barrier behaviour is
   therefore not an oracle, and the semantics would have to come from the reference's own
   implementation (`Xi/xibarriers.c`): the hit state is per **master pointer**, a hit clamps the pointer
   to the barrier until the owner's release (which sets the release id to the hit's event id), leaving
   the hit box ends the hit, sends `XI_BarrierLeave` and starts a new event id, and the clamping is
   done in the screen's `ConstrainCursorHarder` hook.
4. **It touches the pointer-motion hot path.** Every pointer move would consult the barrier list, and a
   mistake there is a pointer that sticks or stutters - the one failure this project has spent the most
   time on. Worth paying only for a capability something uses.

**What the two reachable halves look like now.** `CreatePointerBarrier` is refused (the XFIXES
version says 4.0, and the refusal is visible rather than silent); `XIBarrierReleasePointer` answers the
extension's own barrier error, code 12, with the barrier as its bad value - which is what a reference
answers for a barrier that does not exist, and no barrier can exist here.

**If it is ever built, this is how it would be done and verified** (so the next session starts from a
plan rather than a blank page):

- **The request/reply surface can be compared against a reference**: creation accepted for a
  client-owned barrier id with a horizontal *or* vertical line (a diagonal or zero-length line is
  `BadValue`; a bad id is `BadIDChoice`; the device list takes master ids only), `DestroyPointerBarrier`
  silent, `XIBarrierReleasePointer` silent for a real barrier and error 12 for a made-up one - all
  measured 2026-10-04 and listed in A47.
- **The event layout is in the protocol header** (`xXIBarrierEvent`, 68 bytes: `evtype`, `deviceid`,
  `eventid`, `root`/`event`/`barrier`, `dtime`, `flags`, `sourceid`, `root_x`/`root_y`, `dx`/`dy`),
  delivered to the clients that selected bits 25/26 on the barrier's window.
- **The behaviour can only be verified on this server, with the *host* pointer** - and that is the
  point worth keeping: the clamping would live in this server's own input path (host input ->
  `InputState.movePointer`), which is exactly the path XTEST-only Xvfb cannot exercise. So the check
  is: a kit tool (C + `libXfixes`, or a Java probe writing the request by hand) creates a barrier and
  prints the XI barrier events it receives, then a *real* hand - or the bed's CGEvent tools
  (`/tmp/xsn/hostinput`, `tools/hostdrag`) - drives the host pointer into it. Expected: the pointer
  stops at the barrier's line, the owner gets `XI_BarrierHit`; after `XIBarrierReleasePointer` the same
  motion crosses; moving away from the hit box produces `XI_BarrierLeave`. `xdotool getmouselocation`
  (or a probe's `QueryPointer`) is the assertion for the clamped position, and `tools/sweep.sh` keeps
  the request surface from drifting.

### [x] A50. The X Test Suite, again: SHAPE fixed, and what the XI sets say. **M** - 2026-10-04

**What was run.** The X Test Suite on the bed (A37's build, `/root/xts`), with a section that A37 did
not run - `ShapeExt`, `XI` and `XIproto` - against Xvfb `:98` and against this server on `:61`
(`--single-window --no-clipboard --no-tray --backend headless`). Adding a section is A37's own recipe:

```
xsinput
	"VSW5TESTSUITE SECTION ShapeExt 11 11 0 0"
	:include:/scenarios/shape_scen
	"VSW5TESTSUITE SECTION XI 36 316 0 0"
	:include:/scenarios/XI_scen
	"VSW5TESTSUITE SECTION XIproto 35 107 0 0"
	:include:/scenarios/XIproto_scen
```

**The harness lesson first, because it invalidated the first run.** The server has to be started in a
session that *stays alive*: a server backgrounded from a one-shot shell command was killed when that
command finished, and the run then reported `81 of 82 tests failed` with every XI and XIproto purpose
`UNINITIATED`/`ABORT` - all of them, in the journal, `ERROR: avs_xext_init: Unable to open display`.
That is a dead server, not a protocol failure, and it looks exactly like one from the summary line.

**SHAPE: 5 of 11 failing, and it was a real protocol bug.** A reference answers
`ShapeGetRectangles` with `ordering = YXBanded (3)` **whatever the client set** - measured with a
probe that sets one rectangle three ways (Unsorted, YSorted, YXBanded) and asks for it back: a
reference answers `3` and the same rectangles every time, this server answered `0` with a comment
promising nothing. Five XTS cases read it as `Expected order of 3, Received 0`, and a client that
merges the list would too. The list is now actually put in that order (`ShapeExtension.band`: sorted
by y then x, rectangles of a band merged when they touch) rather than the field being dressed up, and
a **mask is decomposed into its own rectangles** where it used to be answered as one big rectangle
("the closest honest answer"). XTS: **10 of 11 SHAPE cases pass** now, verified live too (the probe's
four cases answer exactly what the reference answers, line for line).

**The last SHAPE case, chased down and fixed: the window's border width.** Purpose 8 of
`XShapeQueryExtents` expects a bounding box of `-1,-1 1154x654` against our `0,0 1152x652`, and the
test's own source says why: its window is created with a **one pixel border**, and the shape coordinate
space puts the border *outside* the window's origin, so an unshaped window's bounding shape is
`(-bw, -bw, w + 2bw, h + 2bw)`. Measured against Xvfb with a probe (a 60x40 window, border 1): bounding
extents **-1,-1 62x42** and clip extents **0,0 60x40**; and `ShapeGetRectangles` for the same window
answers **-1,-1 61x41** for the bounding shape - one less in each dimension than `ShapeQueryExtents`,
because a region's extents are half-open. This server ignored the border entirely, which is *invisible*
when the border is nought (the case every earlier probe used, and why nothing noticed): it answered
`0,0 60x40` for both. Both replies now carry the convention - the bounding shape includes the border,
the clip shape does not (it is the window's interior, which no client may enlarge), and the rectangle
list is one smaller - and the probe matches the reference line for line. **XTS: `All 11 tests passed`.**
The same convention now applies to the extents a `ShapeNotify` carries.

**XI and XIproto: the sets cannot run, and that is the XI **1.x** surface.** 107 of XIproto's 107
tests and 279 of XI's 316 are `UNTESTED` here against 12 and 49 on the reference, because both sets
are XInput **1.x** protocol - `SelectExtensionEvent`, `GetDeviceMotionEvents`, `GetDeviceControl`,
`ChangeFeedbackControl`, `GrabDevice*`, `DeviceBell`, `ChangeKeyboardDevice` - which this server
implements only as far as `ListInputDevices`/`OpenDevice`/the two mappings (the AWT-critical part).
§9 already lists that hole; this is its measurement, and the reference's own numbers are the target:
**88 of 107 XIproto tests pass there and 7 fail; of XI's 316, 200 pass, 48 fail, 13 unresolved**.
Implementing XI 1.x's event selection and device control is a project of its own, and the modern
clients here do not use it.

**Also re-confirmed**: A37's Xproto result stands (the 120-odd purposes that fail are its PURPOSE 2,
the malformed-length cases, which need the per-request length table A37 describes), and the suite runs
without a single crash on the server.

### [x] A51. Three bugs in one report: "Finder cannot connect, and then the picture is 花屏, and then clicks do nothing". **M** - fixed 2026-10-04

**The report, in three acts**, each verified against **macOS's own Screen Sharing** (Finder -> `vnc://`,
driven here with the bed's host-input tools, which is what makes a real client's behaviour measurable):

1. "密码明明配置为123456了，但是finder连接却失败了" - the log said `gave the wrong password`, but the
   password was right.
2. After that was fixed: "画面异常，花屏等".
3. And then: "无法正常点击vnc页面，无任何反应".

**1. The password was fine; the handshake was not: RFB 3.3 waits for a SecurityResult.** Measured with
a temporary log of the challenge and the response in `RfbSession.vncAuthentication`: two connections
arrive, one with an **all-zero response** (Screen Sharing's first probe, before its dialog is answered
- that is the `gave the wrong password` line the report quoted) and one with **exactly the expected
response** - and then the handshake stopped dead: the client never sent ClientInit, its dialog sat on
"connecting". The reason is that Screen Sharing speaks **RFB 003.003** (measured: `minor 3`), and after
the challenge response it waits for the four byte **SecurityResult** that 3.7 introduced and that 3.3
does not have. A spec-compliant 3.3 client sends ClientInit straight away instead, so the two are told
apart by **the byte that does not come**: the session waits 500 ms for ClientInit, and sends the result
only if it is still not there (`clientInitOrAppleResult`). Sending it to a compliant client would be
worse - those four bytes would be read as the start of its ServerInit. **The same mistake was on the
other side of the same version**: SecurityResult was sent for 3.8 and later, where 3.7 is where it
arrived, so a 3.7 client would have hung the same way.

**2. 花屏: the ZRLE CPIXEL, which is three bytes for *any* 32bpp client whose masks fit in 24 bits.**
Measured: the client asks for `SetPixelFormat 32bpp depth **32**` and offers encodings
`[1011,1002,6,16,-239,...]` - ZRLE (16) among them - so the session sent ZRLE frames; the picture came
out black with a few coloured slivers, and the same content through `x11vnc` on the same X display was
correct (a blue root with a clock, captured from both). The cause is one condition:
`compressedPixelBytes` returned **4** when `depth > 24`, per the specification's prose ("32bpp *and* a
depth of 24 or less" uses a 3 byte CPIXEL) - but real clients, Apple's included, use **3 bytes for any
32bpp client whose colour masks fit in the low 24 bits, whatever depth they announce**. The test
client here had been written with the real clients' rule, which is why the whole suite passed while a
real viewer showed 花屏; with Apple's exact format it failed in one line (`the root outside it:
expected 15263976 but was 0`), and the fix is that condition's removal. `ZrleEncoder` was already
CPIXEL-size-agnostic.

**3. Clicks doing nothing: the request was waited on with input unread.** The viewer sends its pointer
and key events *after* the update request it has outstanding. `updateRequest` was a loop that **waited
on damage inside the request** - so on a screen that is not repainting itself (a clock, an idle
desktop) the session never went back to read, and the viewer's clicks sat in the socket. Measured with
a thread dump: the session thread `TIMED_WAITING ... at RfbSession.updateRequest` while the X pointer
had not moved. The fix keeps the wait (damage still wakes it, so a frame is never late) but **checks
`in.available()` first**: if anything is waiting, the request is remembered in `outstanding` and the
message loop serves the input, answering the request from there - after every message and on every
idle poll, and announcing a resize with it.

**Verified.** Finder against this server, end to end: it connects with the password, draws the screen
correctly (captured and compared with `x11vnc` on the same display), and the input path is pinned by a
new test that reproduces act 3 exactly - an **incremental request outstanding on a static screen**,
then a press and release, which must reach an X client that selected `ButtonPressMask` (it did not
before the fix). `VncBackendTest` is 18 tests; the suite is **491 tests green** (excluding the
project's own `XServerTest`, which launches a real server and cannot run inside a test JVM - it is what
makes a plain `mvn test` report "the forked VM terminated": worth knowing when reading a red run).

### [x] A52. The whole option surface, on a real machine: every option started for real and probed with a real client. **M** - run 2026-10-04

**Why.** The user's last request before the wrap-up: "走完整的真机测试，各种配置项都验证下（例如各种backend、vnc密码和密码文件配置），要完整验证所有配置项看下是否都生效，有问题就改，如果暂时改不了的或者决定不了的，就记录在文档". An earlier matrix pass had already gone over some of the options, but two of its one-liners were themselves wrong - an Xauthority file written as *text* (the server reads the binary format) and four `--max-clients` connections opened and closed **one at a time**, which reuses the freed slot - so this pass redid the ambiguous cases with real tools and covered the options the matrix had not reached.

**Harness.** The bed is the real machine: the VM (`192.168.22.152`) is the client, reached over TCP from the Mac (`192.168.22.1`) with `xdpyinfo`/`xrandr`/`xwininfo`/`xclock`/`xeyes`, and the host's own window list is used where a window is the thing under test. Three tools were written for this pass, each because a probe without it would be a guess:

- a **real RFB 3.8 client** (Java; the DES is `javax.crypto`) that performs the whole handshake with the password given and reads the `ServerInit`. It was validated **first against `x11vnc -rfbport 5901 -passwd secret`** on the VM - the right password answers `OK`, a wrong one `WRONG-PASSWORD` - so a failure against this server is this server's and not the tool's;
- a **WebSocket client** that does the HTTP upgrade and reads the RFB banner out of the frames: the noVNC path with nothing installed;
- a client that **asks to be told before its window closes and then ignores the message**, timing the server's own cut-off - the only way to measure `--close-grace-ms` from outside.

**Two bugs, fixed.**

1. **`--title` was read by nobody.** It was parsed (`ServerOptions.title`), documented ("window title") and passed to *no* backend: `BackendFactory` handed its own `DEFAULT_TITLE` (`"xserver"`) to the JavaFX, Swing and VNC backends alike. After the fix, a JavaFX and a Swing window open with the given title when a client that publishes no `WM_NAME` maps a window (`xsn-title-javafx`, `xsn-title-swing` in the host window list), and the **VNC desktop name is the title**: measured `xsn-audit-vnc :71`, which is what a viewer shows in its own title bar. Pinned by `VncBackendTest.theTitleOptionNamesTheDesktopAViewerShows`.

2. **`--max-clients` had no reader at all.** `ServerOptions.maxClients()` was parsed, documented ("maximum simultaneous clients (default 256)") and **read nowhere** - the only ceiling was the 256 slot index space itself. The earlier matrix pass "confirmed" it with four connections it closed one at a time, so each reused the slot the last one freed: the option did nothing and the probe could not see it. It is enforced now where the index space is reserved (`XServer.allocateClientIndex`), counting *allocated* slots so a connection still in its setup handshake counts too. Measured with the connections held open: a **`--max-clients 2` server accepts exactly 2 of 4**, the other two are reset, and the log says `refusing connection: 2 clients are already connected (--max-clients)`. Pinned by `net/MaxClientsTest`, which keeps its sockets open - the probe's own earlier mistake, written into the test so it cannot come back.

**One difference recorded, not a bug: the depth in `--geometry WxHxD` is accepted and ignored.** `--geometry 640x480x16` and `640x480x8` both answer `depth of root window: 24 planes`: the root is always 24 bit TrueColor (`XServer.ROOT_DEPTH`) and only the *window* visuals carry 16/8/1 bit. The depth is part of the ordinary X-server invocation (`-screen 0 1280x800x16`, which is what the bed's own Xvfb runs) so it is accepted rather than refused, and the server now **says so once at startup** rather than ignoring it silently. Honouring it means the whole framebuffer path at another root depth, which is a project of its own.

**Everything else verified green** (each started for real, probed with a real client):

| option | what was measured |
|---|---|
| `--backend headless` | served a real `xclock`; `800x600` at `24 planes`; the server composed the client's window |
| `--backend javafx` | a window opens for a connecting client, carrying `--title` |
| `--backend swing` | the same |
| `--backend vnc` | a real RFB 3.8 handshake, frames and input |
| `--display 72` | TCP 6072 listening and `/tmp/.X11-unix/X72` present |
| `--geometry WxH` | `800x600`; `--scale 2` gives `1600x1200` |
| `--no-tcp` / `--no-unix` / `--local` | exactly as asked; `--local` binds `127.0.0.1` and the VM cannot reach it (`rc=1`) |
| `--auth FILE` | no cookie -> `Authentication rejected`; a real binary Xauthority entry -> connects; a wrong cookie -> rejected; `--no-auth` after it -> connects |
| `--glx` / `--no-glx` | the extension is listed / not (`xdpyinfo`) |
| `--no-clipboard` / `--no-tray` | no bridge lines at all |
| `--max-screen 900x700` | the growth cap (4.3); RandR's range deliberately stays the current size - "this screen grows and never shrinks" (A12) |
| `--tick-rate 30` / `120` | RandR 1.1's rate; `xrandr` prints `0.00`, exactly as the reference does (its mode has no dot clock either) |
| `--control-port 6231 --detach-on-close` | with a client connected: `status` = 1 client / 1 window, `detach` closes the window while the client keeps running, `attach` opens it again |
| `--max-clients 2` | 2 of 4, above |
| `--log-requests` | **509 DEBUG lines** from one `xclock` (with a DEBUG appender; the earlier "0" was a probe with no client *and* no DEBUG appender) |
| `--vnc-port 0` | picks a free port and prints `connect with vnc://<host>:<N>` |
| `--vnc-password VALUE` | a real handshake with the right password is `OK`, a wrong one is refused |
| `--vnc-password-file FILE` | the same, through the file |
| both forms at once | refused: `--vnc-password and --vnc-password-file are the same password two ways` |
| a missing password file | refused: `cannot read the VNC password file ...` |
| no password | security type `1` (None) offered, and a client that takes it is in |
| `--vnc-ws-port 6080` | the HTTP upgrade is accepted and `RFB 003.008\n` is read out of the WebSocket frames |
| `--single-window` | `xclock` and `xeyes` in **2 windows on one shared root** |
| `--close-grace-ms 1500` | a client that ignores the close request is cut off **1513 ms** after the ask |
| `--focus-on-click` / `--drop-mount` | accepted and reach their readers; the gesture behaviours are the checklist's (2.2, 4.8) and their tests |

**The suite** is **493 tests, 0 failures** (`VncBackendTest` is 19 now, `net/MaxClientsTest` is the other new one), still excluding the project's own `XServerTest` - it launches a real server inside a test JVM, which is what makes a plain `mvn test` report "the forked VM terminated".

### [x] A53. EasyShell and termora on every backend and both window modes. **M** - run 2026-10-06, its findings fixed 2026-10-08 and 2026-10-09

**The request.** "把easyshell和termora所有功能都测试下，连接本机xserver，各种backend都测试下，各种窗口模式也都是测试下，看下功能是否正常，不正常记录文档，我会择机处理，主要测试方向，窗口弹窗，输入，右键功能，右键菜单".

**How the gestures were driven.** The VM's copies of the two apps were pointed at the Mac's server. Three instruments, each because the obvious one does not reach:

- **Single-window mode is the one an XTEST driver can reach.** In per-client mode every connection gets a screen of its own (that is the design), so a probe client's `xdotool` lands on an empty screen of its own and never in the app; `--single-window` puts every client on one screen and there it works. So the scripted sweeps drive `xdotool` on the VM and capture the server's own root with `xwd` (1:1, so image pixels *are* screen coordinates - the trap that costs a click when forgotten).
- **Per-client mode is driven the way a user does it**: `tools/host-input.c` posts real CGEvents at the host window, and the capture is `screencapture -l <window id>` of that window, which is **2x the frame in points**. A client screen point `(x, y)` is the frame point `(x, y - 3)` (the client's window is at `+0+33` on its screen, the frame has a 30 point header), so the Mac point is the frame's origin plus that.
- **Where a list row is** comes from the capture, not from a guessed layout: `RowScan` (a scratch tool) prints the text bands of a column, and the click goes to a band's centre.

**What was on screen, per configuration** (every one a fresh app launch):

| backend | mode | EasyShell | termora |
|---|---|---|---|
| javafx | single-window | renders; right-click menu; 添加连接 wizard; a dialog with a text field; a local terminal that runs commands and echoes them; ASCII and Chinese typed in | renders; right-click menu; Quick Connect dialog and a modal message dialog |
| javafx | per-client | renders; right-click menu through the host's own pointer; a context-menu item opens the 传输文件 dialog as a new window the frame shows | renders |
| swing | single-window | renders | renders |
| swing | per-client | renders; right-click menu | renders |
| headless | single-window | renders (nothing to watch, the app cannot tell) | renders |
| headless | per-client | **no window at all, by design** - the client connects and runs, and nothing on this machine shows it | the same |
| vnc | single-window (`--backend vnc` implies it) | renders | renders |
| vnc | per-client | not a thing: `--backend vnc` is one screen for viewers, and it sets `--single-window` itself | the same |

**The strongest positive result: the backends do not change a pixel.** With `--single-window`, `xwd` captures of the *same* app in the *same* state are **byte-identical** between javafx, swing, headless and vnc - `md5` equal, for EasyShell both before and after the right-click and for termora before it. The one exception is app *state*, not rendering: termora's post-right-click capture through vnc differs by **6552 of 1040000 pixels (0.63%)**, all inside `15,40..287,63` - the "My hosts" row's hover/selection, which `tools/kit.sh Xwd diff` localises exactly. (That is also the §8 trap: two captures of a live application are only comparable in the same state.)

**The bug this pass found, and its three causes - fixed 2026-10-08** (the user's follow-up: "中文输入实际上都是英文字符，好像也没看到 input method committed 这种消息"). The measurement that settled it was the Mac's own **Pinyin** input source, driven with real keycodes: `n i h a o` + space, into a text field of a forwarded EasyShell, and a capture of what arrived. Three separate things were wrong, and each had to be fixed for `你好` to appear:

1. **The scene was never an input method client at all.** JavaFX enables input method events only when the *scene's focus owner* has both `getInputMethodRequests()` and `getOnInputMethodTextChanged()` set - `Scene.InputMethodStateManager.updateInputMethodEventEnableState` asks exactly that and calls `SceneHelper.enableInputMethodEvents` only then. This window had no focus owner and the handler was on the **Scene**, so the platform was never asked to compose anything: no `input method committed` line could ever be printed, and the keystrokes arrived as the letters printed on them. The content now owns the focus and the handler (the handler may not also be on the scene: the event is fired *at the focus owner* and bubbles, so two handlers type every character twice - measured: `NODE-IME committed='你好'` **and** `SCENE-IME committed='你好'` in a probe that had both).
2. **A focus that never changes is an input method that is never enabled.** The manager re-decides only when the focus owner *changes*, and the content was being focused the moment the scene was shown - before the window was up - after which nothing could re-trigger it. Whether the composition worked came down to timing: the same build composed on one run and not the next. The content is now **not traversable until the window has the focus**, and on every window focus it is taken while the focus is parked on a hidden node first, which is the change the platform waits for. (JavaFX 27's `StageStyle.EXTENDED` was suspected first - a probe with a plain decorated window composed while the extended one did not - but that was the same timing, and the extended window composes once the focus is taken properly.)
3. **The composed character was put on a keycode a real toolkit cannot resolve.** `InputState.keyTyped` claims a keycode no key carries and gives it the character as its keysym; it claimed **255**, the top of the range, and a text field in a forwarded EasyShell received **nothing** for `中` - while the same character typed by `xdotool` arrived, and `xdotool` claims **keycode 8** (the server's own log: `client mapping change: first=8 count=1 perKeycode=1 keysyms=[16797229]`). A keycode at the far end of the range is one the map's own tables can leave without an entry, so the client resolved it to nothing. The claim is now the **lowest** keycode nothing carries, and `中` and `文` reach the field. Pinned by `TypedCharacterTest.theCharacterGoesToTheLowestKeycodeNothingCarries`.

**And a key event that carries text but no keycode no longer types the keycode.** `tools/host-input type "中文输入abc"` posts every character with the keycode of the `a` key and the real text beside it; before this, each character also typed an `a`. Now a press whose text is a character **no key on the keyboard can produce** is dropped, and the character comes through `KEY_TYPED` (which was already wired to `InputState.keyTyped`) - the two halves of the same problem. The guard is deliberately narrow: a press whose text the keycode *can* produce is left alone, because Shift+1 is text `!` on the DIGIT1 key and skipping it would type nothing at all. The tool's remaining artifact is its own: it posts ASCII letters with the `a` key's keycode too, so `host-input type "中文abc"` now arrives as `中文aaa`. Type ASCII with real keycodes (`hostkey`) or `xdotool`.

**Verified end to end**: with the Mac's Pinyin source selected, `n i h a o` + space into a forwarded EasyShell text field produced **你好**; real keycodes `a b c 1 2 3` with the ABC source produced **abc123**; and the server's log carries `input method committed 2 characters`.

**And the two things that were still wrong with it, fixed 2026-10-09** (the user: "输入框应当显示中间字符…按下空格后提交内容…这时候直接回车…输入框应当是aaaaa"). Measured first, with the platform's own reports logged: a composition of `nihao` arrives as `composed='n'`, `'ni'`, `'ni h'`, `'ni ha'`, `'ni hao'` and then `committed='你好'`; a composition of `aaaaa` arrives as `'a'`, `'a a'`, … `'a a a a a'` and then, on **Return rather than space**, as `committed='aaaaa'` - the raw text, exactly as the user described. Neither reached the field, and neither could have: the pre-edit is the *platform's* conversation with the person, and the second needed the first.

1. **The pre-edit is now typed into the field, and taken back when it changes.** The application never sees a composition - the candidate window is the platform's own - so the only way its field can show what is being typed is for the server to type it. Each `InputMethodEvent` now deletes what the field is showing (`preEditLength` BackSpaces) and puts the new state down: the composed text while it is still composing, the committed text once it commits. That is what a local input method does to a field, one BackSpace at a time. Measured: the field shows `ni hao` while 你好 is being typed, `你好` after space, `a a a a a` while 啊啊啊啊啊 is being typed, and `aaaaa` after **Return** - which is the raw-text commit the user asked about.
2. **Each composed character gets a keycode of its own.** `InputState.keyTyped` used to put every character on *one* keycode, re-assigned each time; a client resolves a keycode through the mapping it was **last** told, so 你好 reached the field as **好好** - with the keystrokes 25 ms apart and 150 ms apart alike, so it is not a race a delay can fix. `Keymap.claimKeycodeFor` now gives each character its own keycode (8, then 93 - the two lowest nothing carries) and the same character keeps the one it has. Pinned by `TypedCharacterTest.eachComposedCharacterKeepsItsOwnKeycode`, which replaces the test that asserted the old "one keycode, kept" rule.

Characters the keyboard already carries (the pre-edit's letters and spaces, and a raw-text commit) go on their own key rather than a claimed one - a key that was always there needs no mapping change, and a client can resolve it however stale its copy of the mapping is - with Shift around it when the character is the shifted half of the pair.

**Three differences that are the applications', not the server's** - each checked against the VM's own `Xvfb :98` (started for the comparison, no window manager), because "does not respond here" is worth nothing until the same gesture has been tried on a reference:

1. **termora's context-menu items ignore a synthetic click** - the menu opens and stays open, and neither `mousemove x y click 1` nor a separate move-then-click does anything, **identically on Xvfb** (the two captures are byte-identical before and after). So the deeper menu actions could not be scripted at all; the menu's own rendering is what was verified.
2. **EasyShell's own title-bar items** (片段 消息 工具 关于 设置 退出) do the same: a left click, a press-and-hold, and a release produce no reaction and no window on this server **and none on Xvfb**.
3. **termora's Quick Connect with an empty form** answers with its own modal `Message: getScheme(...) must not be null` - a validation gap in the application, printed inside a dialog this server drew correctly.

**Not a finding, recorded so it is not reported as one**: a **headless server in per-client mode has nothing to look at**. The client connects, draws and runs; there is no window on the host and no way to see the client's own screen, which is inherent to the mode rather than a defect.

**Scratch tools used and not kept in the repository** (they live in `/tmp` and are worth rewriting rather than trusting to survive): `RowScan` (text bands of a column), `ColorBlocks` (saturated blocks - the only reliable way to find a button by colour when a node cannot be introspected), `sweep.sh`/`probe.sh`/`probe2.sh` (the three drivers above).

---

### [x] A54. A window that arrives larger than the screen is put on it, not cropped by it. **M** - fixed 2026-10-13

**The report.** "如果不指定合适的geometry，152主机上的easyshell明显左右都被裁剪掉一小部分显示，xclock、xeyes却正常，你看下咋回事，修复下" - EasyShell is visibly clipped on both the left and the right with the default screen, and xclock and xeyes are not.

**Measured first, on the real machine (per-client `javafx`, default `--geometry`, EasyShell forwarded from the VM).** The client's own window, read out of the server's tree while both it and the frame were up:

```
0x400002 "EasyShell主页"  1300x700+-10+33        <- the client's window, on its own 1280x800 screen
opened a window for display :61 showing java.awt.Rectangle[x=0,y=33,width=1280,height=700]
```

**The cause is the client, and the screen is why it cannot be shown.** EasyShell's window is a fixed **1300x700** and it centres itself on the screen it was told about - `(1280 - 1300) / 2 = -10` - so ten pixels of it are off the left of the screen and ten off the right, and a client that centres a window *wider than the display* gets the same answer from a reference server: the same window started against the VM's own **`Xvfb :98` at 1280x800** lands at exactly `1300x700+-10+33`. The frame, which shows the part of the screen the client's windows occupy, therefore showed 1280 of the window's 1300 columns: 10 lost on each side, with no scrollbar and no way to ask for the rest. **xclock and xeyes are small enough to fit**, which is the whole of the difference the report noticed. `--geometry 1300x800` was the workaround for the same reason - on a 1300 wide screen the centred window has nothing to hang off.

**The fix, in `XServer.fitContentOnScreen` (called from the tick, before the frame's rectangle is computed).** Two halves, and both are rules this server already had:

- **a window that starts off the left or the top is moved onto the screen.** An X screen has no pixels at a negative position, so a window there can never be shown in full however large the framebuffer is; moving it is what a window manager's placement does, and the client is told through the ordinary configure path (`WindowHandlers.moveWindow` -> `applyConfigure`), so it gets the same `ConfigureNotify` a window manager sends and the area it stops covering is repainted.
- **the screen grows to the far edge of what the client has mapped**, which is C1's rule - *the screen follows the window* - applied to a window that **arrived** too large rather than to one the user dragged. It is capped at `--max-screen` exactly like the drag, and the growth goes through the one `growScreen` the drag path uses, so the clients that asked to hear about it are told and every session showing that screen is marked to rebuild.

Measured on the real machine afterwards, both window modes, no geometry asked for:

```
moving a 1300x700 window of display :61 from -10,33 to 0,33: a window that starts off the screen can never be shown in full
growing the screen of display :61 to 1300x800: a client's window does not fit on it
opened a window for display :61 showing java.awt.Rectangle[x=0,y=33,width=1300,height=700]
```

The frame is 1300x730 on the 1512x982 display and shows EasyShell whole - the icon column on the left and the application's own window controls on the right are both fully there in `screencapture -l <frame>` (2x). **xclock is unaffected**: a 164x164 window at `+0+0` produced no move and no growth, and the frame is the window.

**What this changes for the tests that pinned the old picture.** `HostWindowTest`'s four window cases used EasyShell's own `1300x700` at `-50+20` on a 1280x800 screen and asserted the cropped `Rectangle[0,20,1250,700]`; they now assert the whole window and the grown screen (`aWindowLargerThanTheScreenIsPutOnItAndADragStillFollowsIt`), and `ScreenResizeTest` gained four tests for the rule itself - a window off the left, one taller than the screen, one past `--max-screen`, and that the client is **told** its window was moved. The `windowExtent`/`visibleExtent` conversion a drag uses stays: what it now guards is the far edge and `--max-screen`, not a window hanging off the start of the screen.

**Traps.** The nudge has to be idempotent, and it is - it only fires while a window is at a negative position, and after the move it is not - because the tick runs sixty times a second: a client that insists on its own negative position would be moved once per frame, which is the one shape of application this rule argues with. The growth is the same code the drag uses, so a screen grown by an arriving window never shrinks back, exactly like one grown by a drag.

---

### [x] A55. Closing the window hands the platform pointer back before the window goes. **M** - fixed 2026-10-13

**The report.** "在x窗口直接点击关闭窗口，这时候x服务的窗口也退出了，但是鼠标也跟随消失了，应当在这时候恢复系统鼠标" - the forwarded window goes, and the pointer goes with it: the desktop is left with no cursor, and only activating another application brings it back.

**Why the pointer is hidden at all.** The server draws the client's own cursor as a sprite over the frame and hides the platform's while the pointer is over the client's *content* (A28). On macOS `Cursor.NONE` is not an empty cursor but a **process-wide `[NSCursor hide]`**, so it is a **loan**: `JavaFxBackend.hostCursor`/`applyHostCursor` hand it back when the pointer leaves the page, when the window loses the focus, and when the window is closed.

**The cause: the hand-back is a *task*, and the window can be gone before it runs.** `Scene.setCursor(DEFAULT)` only marks `CURSOR_DIRTY`; the cursor reaches the platform from the scene's own pass inside a pulse (`Scene.syncAll` -> `Scene.mouseHandler.updateCursorFrame()` -> `Scene.peer.setCursor(frame)`), and `ViewScene.setCursor` posts *that* through `Application.invokeLater` (JavaFX 27 sources, read on the machine):

```java
public void setCursor(final Object cursor) {
    super.setCursor(cursor);
    Application.invokeLater(() -> {
        final Cursor platformCursor = CursorUtils.getPlatformCursor(cursorFrame);
        if (platformView != null) {
            Window window = platformView.getWindow();
            if (window != null) window.setCursor(platformCursor);
        }
    });
}
```

`Platform.runLater` is the same queue (`QuantumToolkit.defer` is `Application.invokeLater`), so the release is drained as an ordinary FX task. A window closed **in the same turn** as the release therefore wins the race: the task runs against a disposed scene, `platformView` is null, the native unhide never happens - and because `MacCursor.isNSCursorVisible` is a **stale cache** that still says "hidden", no later `NONE -> DEFAULT` lifts it either. Measured before the fix, with the toolkit's own record of the hide read by reflection from inside the server's own JVM (a `screencapture` cannot show this - it composites the pointer in regardless):

```
22:30:32 DEBUG JavaFxBackend - host pointer hidden: over the client's page=true, application active=true, idle=false
PROBE isNSCursorVisible=false
... the client is killed and the window closes ...
PROBE isNSCursorVisible=false   (still, a minute later)
```

**The fix, in `JavaFxBackend.handBackPointerThenClose`** - now the one path both `stop()` and `stage.setOnCloseRequest` go through: hand the pointer back, then close the window **on the second pulse after it** (an `AnimationTimer` counts two pulses and then calls `Stage.close()`). The pulse is the unit that matters - the scene's pass runs inside a pulse and the task it queues is drained before the next one, so by the second pulse the scene has taken the arrow back - and the close-request handler `consume()`s the platform's event so that the close really is this server's to finish, a pulse later, which is not something a user can see. Nothing downstream changed: `XServer.onWindowClosed` is driven by `isClosed()`, which the handler still sets in the same turn, so per-client, single-window and `--detach-on-close` all react exactly as before. `LIVE_WINDOWS` (the counter that offsets the next window) is decremented **at the decision** rather than in the timer, so a toolkit that is exiting (`releaseToolkit`, on the way out of the process) cannot leave it leaked.

**Verified on the real machine afterwards, current build** (`--display 61`, per-client, xclock forwarded from the VM at 192.168.22.152, the host pointer parked inside the client's page so the arrow was hidden - `PROBE isNSCursorVisible=false`):

```
--- the client exits by itself: the reported case ---
22:30:48 INFO XServer - client /192.168.22.152:42330 disconnected after 104 requests: the client closed the connection
22:30:48 INFO XServer - screen of display :61 freed
PROBE isNSCursorVisible=true                    CGCursorIsVisible=1

--- the frame's own close button, with the pointer over the page and one move+click at 131,81 ---
22:31:16 DEBUG JavaFxBackend - host pointer handed back: over the client's page=false, application active=true, idle=false
the frame is gone from host-windows         CGCursorIsVisible=1
```

And the counter: the next window of the same run opened at the same `116,66` as the first, which is `LIVE_WINDOWS` back at zero.

**What the test can pin.** Not the native hide - that needs a real display and the platform's own record - so `JavaFxBackendTest.presentsFramebufferToImage` pins the half that is visible from inside: **right after `stop()`, the window is still there** (`windowStillOpen()`, which is `stage != null || closePending`), the scene's cursor is `DEFAULT`, and the window *is* taken down once the release has landed. Against the old code, where `stop()` closed the window in the same turn, the first of those fails.

---

### [x] A56. A frame the display cannot hold is drawn smaller rather than clipped by the platform. **M** - fixed 2026-10-14

**The report.** "查看git补丁de6c661…修复了x窗口大小被裁剪的问题，在macos上正常了，但是在windows还是有问题…本机看下窗口左右两侧为啥还是被裁切了" - the same client against the same server: whole on macOS, and the sides of the client's own window missing on Windows.

**A54's half is right, and it is not this.** `XServer.fitContentOnScreen` works on the *X* screen: a window that arrives off the left or the top is moved onto it, and the screen grows to the far edge of what the client has mapped. Measured on the real machine with EasyShell forwarded from the VM to a `--scale auto` server, it does exactly that and leaves nothing to crop inside the picture:

```
moving a 2600x1400 window of display :5 from -20,67 to 0,67: a window that starts off the screen can never be shown in full
growing the screen of display :5 to 2600x1600: a client's window does not fit on it
```

**What is different on Windows is what the platform does with the frame.** `JavaFxBackend` sizes the host window to what it shows, one point per framebuffer pixel over `scale`, so a client that fills its screen makes a frame very nearly the size of the display: with EasyShell's `2600x1400` framebuffer at `scale 2` the frame comes out `1314x737` logical points. A **top-level window wider or taller than the display's usable area is clipped by Windows** - the part outside is not drawn and cannot be reached, however the window is dragged - while macOS draws it past the edge and lets the user move it into view. That is the whole of the platform difference, and it is why the same server looked correct there. It is a function of the display's *usable* width and height (the taskbar comes off the height; the work area is the bound), which is why it is invisible on a display with room to spare and why the report came from a box without one.

**Measured on the real machine, A/B, the same server and the same client** (host display 2560x1440 physical; EasyShell's `1300x700` forwarded from the VM, at a logical size small enough to be the reported case - `-Dglass.win.uiScale=2`, a 1280x720 logical work area):

```
without:  frame 0,0 2622x1471 physical   <- 62 px wider and 31 px taller than the 2560x1440 display; the client's own window controls (- o x) and the right of its content are off the screen
with:     frame 0,0 2430x1368 physical   <- fits the display whole
          JavaFxBackend: this display cannot show the whole screen: it is drawn at 93% of its size, ...
```

and with a client large enough to overflow a display with room to spare (`xclock -geometry 3600x1200`, screen grown to `3600x1600` fb = `1800x800` logical at `scale 2`, usable area `1707x912`):

```
without:  frame 0,56 2722x956 physical (= 1814x637 logical)   <- 107 logical points wider than the display; the right of the clock face is off the screen
with:     frame 0,56 2561x903 physical (= 1707x602 logical, drawn at 94%)  <- fits, and the scene image holds the complete clock face
```

**The fix, `JavaFxBackend.applyViewScale`.** The window is not made to fit by cropping it - that is the bug - and not by asking the client to draw less: the picture is drawn into the canvas at a uniform factor instead, so all of it stays visible and only its size on screen changes. The factor is `fitScale(logical(viewport.width), logical(viewport.height))`, the largest value (=1 whenever everything already fits) for which the whole window - the content, this backend's own header bar and the platform's frame - stays inside `Screen.getPrimary().getVisualBounds()`; the header bar and the frame have heights of their own in points and are not scaled with the content, so they come off the area first. It is applied as one `javafx.scene.transform.Scale` on the canvas, and every place that can move the window's geometry (the frame being sized, its maximum being set, the window being built) recomputes it; it is idempotent - the transform is only replaced when the factor actually changed - so the frames where nothing did pay nothing. `viewScale` is 1 everywhere else in the code, and the two places that map between the window and the screen divide by it: `contentWidth`/`contentHeight` (what the client is told its window may be) and `screenX`/`screenY` (where the pointer is), so a scaled drawing still has its pointer in the right place.

**What the tests pin.** `JavaFxBackendTest.contentTooLargeForTheDisplayIsDrawnSmallerRatherThanClipped` pins the arithmetic without a display: content that fits is not scaled, the binding dimension is the one that decides, and a window with no content or a display with no room answers 1. Measured against the old build, the frame is larger than the display in both the EasyShell and the xclock cases above and fits after the change.

---

### [x] A57. A frame is drawn when it is drawn, not at the end of the display period. **M** - done 2026-10-14

**The report.** "另外优化任务，尽可能优化渲染效率和速度，目前感觉有点延迟，没有xvfb转发，vnc连接的哪种丝滑感" - the same forwarded clients that feel immediate through `Xvfb` plus a VNC viewer felt a beat behind here.

**What the delay is, measured.** Not work. One window under continuous damage blits in **0.013 ms**, the whole `flushDamage` is **0.03 ms**, and the server sits at **1.7% of one core** while a forwarded EasyShell repaints a hundred times a second. It is *waiting*, in two places, and each of them is a clock:

1. **The period was counted from the end of a frame's work.** `Thread.sleep(frameMillis)` ran after the tick, so the work was added to every frame - measured 2 ms of work on a 16.7 ms frame giving **18.7 ms a tick, 53 fps where 60 was asked for** - and, because that sleep starts after the work, a wake-up that landed 1.2 ms late was added to the *next* frame and to every frame after it. The loop now waits until an absolute deadline counted from the **start** of the frame, so the work is part of the period and a late wake-up is corrected by the next sleep instead of accumulated.
2. **A frame waited for the end of the period although the client had already drawn it**, and then waited again for the toolkit to look at it (the toolkit's own pulse - `10 ms` apart here). Two free-running clocks, and every frame crossed both. `Framebuffer.setDamageListener` now tells the display loop (`XServer.requestFrame`) the moment damage is marked - one call per batch of damage, on the thread that drew - so the frame is handed over as soon as it exists. It is at most `FRAMES_PER_PERIOD` (**4**) frames to a period and never sooner than 1 ms after the last one, so a client damaging its window in a tight loop cannot turn the loop into a spin, and the toolkit coalesces what is left over anyway.

**Measured A/B.** Windows 11, 20 cores, RTX 4070 SUPER at 180 Hz, `--scale auto`, EasyShell 1300x700 forwarded from the VM at `192.168.3.21`. "Draw to frame" is the time from the client's first draw of a batch to the frame handed to the toolkit, over every frame of a 2 s window, from a temporary probe in `flushDamage` (removed again). Before is `7088da4`:

| | before | after |
|---|---|---|
| EasyShell alone, frames drawn a second | 54 | **100** |
| ... client's draw to frame | 6.0-7.2 ms mean, 17.0-17.7 ms worst | **1.8-2.2 ms mean, 5.6-6.5 ms worst** |
| with `x11perf -dot` (continuous, two windows) | 120 a second, 11.5 ms mean, 17.6 ms worst | **380 a second, 3.6 ms mean, 6.0 ms worst** |
| CPU, one core, the same runs | 1.7-3.4% | 1.7-2.2% |

**The frame rate is no longer the tick rate.** The toolkit here paints at about 100 Hz, and the old loop was handing it 54 frames a second - the tick's own 60, less the presents that fell inside a pulse that had already painted - so a client's frame was drawn *and* painted one period late. `--tick-rate` is now the rate a *quiet* screen is drawn at; anything a client draws goes out at once.

**What was measured and found not to be the cause** (on the real machine, and in `memory/xserver-open-bugs.md` with the numbers): the Windows timer resolution (`Thread.sleep(16)` measured 16.0-16.6 ms here, so a coarse timer was not the 18.7 ms); `Platform.requestNextPulse()` after the `runLater` (6.9 ms mean against 5.0 without - the toolkit already pulses for a frame that has been handed to it, so it was taken back out); and the behaviour of a **programmatically** maximized window, which is identical on both builds in the same order (three `maximize`/`restore` cycles: filled, then `zoom=true` with the window left where it was, then filled) - the drag-following repro is A38/A39's and needs a real drag to re-measure, so this is recorded, not fixed.

**Tests.** `DamageTrackingTest.aBatchOfDamageWakesTheDisplayOnce` pins the half that needs no display: three marks of one batch are one wake, and damage after the batch has been taken is a wake of its own - so a client that marks damage a hundred times for one frame does not wake the loop a hundred times. Whole suite **509 tests, 4 failures**, all four `DropBridgeTest`'s, which assert POSIX paths (`file:///tmp/...`, `/private/...`) and fail on Windows before this change as well.
