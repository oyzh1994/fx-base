# xserver

A modern X11 display server written in Java, rendered into a JavaFX window.

It speaks the X11 protocol to real clients — Xlib, XCB, Xt, Motif, GTK's core-font path — and
paints their windows into a framebuffer that is blitted to the screen. It is not a compatibility
shim around an existing server: the window tree, the resource model, event delivery, grabs and the
rasteriser are all implemented here.

## Running it

Build with Maven (3.9 or newer) and run:

```sh
mvn compile
mvn exec:java -Dexec.args="--display :1 --geometry 1280x800"
```

Or run `io.github.oyzh1994.xserver.Main` straight from the IDE, which is what the tests do. The only
runtime dependency beyond the JDK is the JavaFX and protocol jars Maven resolves.

Then point any X client at it:

```sh
DISPLAY=:1 xdpyinfo
DISPLAY=:1 xterm -fn fixed
DISPLAY=:1 twm & DISPLAY=:1 xclock & DISPLAY=:1 xcalc &
```

Options:

| Option | Meaning |
| --- | --- |
| `--display :N` | display number; listens on TCP 6000+N and `/tmp/.X11-unix/XN` |
| `--geometry WxH[xD]` | root window size (default 1280x800). The depth is accepted for compatibility with the usual `-screen 0 WxHxD` invocation and **ignored**: the root is always 24 bit TrueColor and the server warns once at startup when a geometry names another depth |
| `--backend NAME` | `javafx` (default), `swing`, `headless`, or `vnc` |
| `--auth [FILE]` | require MIT-MAGIC-COOKIE-1 from `~/.Xauthority` |
| `--no-tcp` / `--no-unix` | restrict the transports |
| `--log-requests` | log every request at debug level |
| `--tick-rate N` | display refresh rate (default 60) |
| `--single-window` | render every client into one shared window (default: one window per client) |
| `--no-clipboard` | do not bridge the host's clipboard to the X `CLIPBOARD` selection |
| `--no-tray` | do not serve the notification area (the tray a client's icon docks in) |
| `--focus-on-click` / `--no-focus-on-click` | a click moves the input focus to the window it lands in - **on by default**, because this server is the window manager for what it forwards and an application whose page leaves the focus on the root window cannot get it back otherwise (its menus stop opening). An **override-redirect** window (a menu) is never focused: a window manager does not, and focusing one takes the focus off the window the menu belongs to, so the application unmaps its own popup. `--no-focus-on-click` is the comparison switch: a reference does not focus a window on a click |
| `--glx` / `--no-glx` | GLX is advertised (so a software OpenGL client - `glxgears`, a browser, anything with a GL surface - can render); `--no-glx` turns it off, which is what comparing two builds wants |
| `--max-screen WxH` | the largest the screen may be grown to when a window is dragged past it (default 8192x8192) |
| `--drop-mount H=C` | where the host's path prefix `H` is on the client's filesystem, for a file dragged onto a forwarded application (see below) |
| `--scale N\|auto` | screen pixels per logical pixel: `auto` is the host's own scale, 2 on a Retina panel (default 1) |
| `--close-grace-ms N` | time a client gets to close itself when its window is closed (default 2000) |
| `--detach-on-close` | closing the window leaves the clients running with no window instead of ending the session |
| `--control-port N` | listen on `127.0.0.1:N` for `attach`, `detach` and `status` (0: any free port) |
| `--vnc-port N` | the port `--backend vnc` serves on (default 5900; `vnc` implies `--single-window`) |
| `--vnc-password VALUE` | require `VALUE` as the password from VNC clients - the form for a program that starts this server as a library (a value in an argument is visible in `ps`, which is why the file form exists) |
| `--vnc-password-file F` | require the password in `F` (one line) from VNC clients |
| `--vnc-ws-port N` | also carry the same sessions over WebSocket on port `N`, for a browser |

The JavaFX backend falls back to Swing automatically when JavaFX cannot start, and to `headless` -
with a line saying so - where the platform reports no display at all, since neither toolkit can open a
window there. `headless` itself runs the server with no window whatever, which is what tests use and
what `xwd` needs.

`vnc` presents no window either, and serves the screen to VNC clients instead:

```sh
# serve the whole screen on 5900, to anyone who connects
mvn exec:java -Dexec.args="--backend vnc --vnc-port 5900 --geometry 1300x800"

# or ask clients for a password (one line in the file; macOS's Screen Sharing requires one)
echo 'my-password' > ~/.xserver-vnc-pass
mvn exec:java -Dexec.args="--backend vnc --vnc-password-file ~/.xserver-vnc-pass"

# the same password as a value, which is what embedding this server wants -
# a library has the password in hand and no file to put it in. It is visible
# to anyone who can run ps, so a shell should prefer the file above
mvn exec:java -Dexec.args="--backend vnc --vnc-password my-password"
```

Any VNC client works (`vnc://host:5900`), including the ones on phones; macOS's Screen Sharing
**requires** a password, so use `--vnc-password`/`--vnc-password-file` if that is the client - on the Mac, Finder's
**Cmd-K** takes `vnc://host:5900` directly. `--vnc-port 0` takes any free port and prints the one it
got, which is the honest way to run it beside anything else. **This mode opens no host window** - that
is what lets it run on a machine with no display - and it implies `--single-window`, so every client
shares the one screen; X clients connect exactly as they otherwise do (`DISPLAY=host:N`), and a VNC
client's mouse and keyboard *are* the X pointer and keyboard. With `--vnc-ws-port`, a
browser reaches it too - `noVNC` pointed at `ws://host:6080`, with **nothing to install and no bridge
process**: open its `vnc.html` and put the host and port in the URL (the page itself is not served by
this server, only the RFB inside the WebSocket). What is sent is the part of the damage whose pixels
**actually differ from what that client already has** - **raw, or ZRLE when the client lists it**,
which every VNC client does and which is what makes a desktop of text or flat colour cheap. An
unchanged screen costs nothing at all: the comparison is against what the viewer was last sent, so an
application that repaints identical content (EasyShell's aliyun tab does it ~300 times a second) sends
nothing, as it does with `x11vnc`. **A photographic rectangle goes as Tight's JPEG method** when the
client offers it (a few hundred kilobytes instead of the three megabytes ZRLE has to send for content
where no pixel matches its neighbour, measured 2026-10-01), while text and flat panels stay on ZRLE,
which is lossless. `CoRRE` is the only encoding still on the list in
[docs/backlog.md](docs/backlog.md) (D2).

**Stopping it**: `Ctrl-C`, or the dock's **Quit** on macOS. A Quit closes the host window, which the
server answers the way it answers the window's own close button - the clients are asked to quit
(`WM_DELETE_WINDOW`) and cut off once the grace period is up, which is what releases a forwarded
application on the far machine - and the process ends with the server. `--detach-on-close` is the
opposite: the clients keep running with no window and the control port can put one back.

## Architecture

Why the code looks the way it does - the decisions behind the per-client screens, the display tick,
the pointer sprite and the host pointer, the default colours, and what is deliberately not done - is
in [docs/design-notes.md](docs/design-notes.md).

```
io.github.oyzh1994.xserver
├── net/          sockets: accepting clients, and encoding replies/events/errors
├── client/       one object per connection: handshake, request loop, dispatch, resource ids
├── protocol/     decoders for the requests the generated ones get wrong
├── display/      Screen, VisualInfo, and the JavaFX/Swing/headless/VNC backends
├── draw/         Framebuffer, Rasterizer (X11 raster ops), ImageCodec (PutImage/GetImage)
├── resource/     windows, pixmaps, GCs, fonts, cursors, colormaps, atoms, properties
├── event/        event masks, propagation, crossing events, raw event assembly
├── input/        pointer/keyboard state, keymap, cursor sprite
├── clipboard/    the bridge between the host's clipboard and the X selections
├── tray/         the notification area, shown in the host's menu bar
├── handler/      the request implementations, grouped by area
└── extension/    extension framework plus BIG-REQUESTS, XC-MISC, SHAPE, XTEST, XFIXES, XINERAMA,
                  XInput2, RANDR, XKEYBOARD, MIT-SCREEN-SAVER, DPMS, GLX, RENDER, Composite, SYNC,
                  Present, X-Resource, RECORD, DOUBLE-BUFFER, SECURITY, XVideo and the Generic
                  Event Extension
```

Three decisions shape everything else:

**Requests are decoded by generated code, not by hand.** The `x11-protocol-*` artifacts from
[Maven Central](https://central.sonatype.com/artifact/com.github.moaxcp.x11.x11-protocol/x11-protocol-core)
contain a reader and writer for every request, reply, event and error, produced from the XCB
protocol descriptions. Decoding gives a typed object, so dispatch is a `switch` over request types
rather than a table of byte offsets, and the wire format cannot drift from the server's idea of it.
The exceptions are documented in `VariableLengthDecoders` (see below).

**One screen per client connection, and one window per application.** A connection is given a screen
of its own - its own root window and framebuffer - so each forwarded application appears as **one**
window, sized to what it draws, and no client can draw over another's pixels. The window follows the
client's top level windows: it is the union of their rectangles, so an application that opens a dialog
beside its parent grows its own window to cover both, and a small application does not arrive in a
screen-sized window with black around it. **A host window per X top level - one per dialog, as
XQuartz does - was built twice and is deliberately not what this is**: it was first reverted because a
frame had no pixels of its own to show, and then, once B5's per-window store gave it some and it
worked, rejected on the requirement - an application is one window on the desktop, whatever it opens
inside itself. `docs/backlog.md`'s C8 has the two attempts and why it is closed.
`--single-window` restores the older shape, one shared screen with every client on it. A
screen's pixels are allocated when something first draws into it, so a connection that only asks
questions - `xdpyinfo`, `xdotool`, anything that connects to look - costs nothing until it draws.

**OpenGL works with GLX, which is on by default, and the server has no renderer of its own.** It was
opt-in for a day, on a report that EasyShell (JavaFX) lost the things it animates with GLX advertised
- a menu item with no hover colour, a caret that did not blink. **That report was closed by
measurement (A17)**: the caret **does** blink on both builds (ten captures 0.2 s apart, the box's own
rectangle alternating between exactly two states), and the hover paints nothing because the client
issues **no drawing request at all** while the pointer crosses the menu - a toolkit's animation state,
not a frame this server dropped. What is left is that a toolkit which finds GLX takes a GL path it
then abandons - JavaFX creates a context, finds no GPU that meets its requirement and falls back to the
same software pipeline - so it costs such a client one context and buys it nothing, while it is the
only way a GL client renders here at all. `--no-glx` is the comparison switch.
With it on, it advertises GLX - 1.4, the
version,
the two config lists, contexts - and grants a **direct** context, which is the whole trick: a software
GL client (Mesa on a machine with no GPU) renders in its own process and pushes each finished frame as
an ordinary `PutImage`, so `glxgears` runs here and the gears are drawn by OpenGL. What is *not*
advertised is any GLX *extension*: the list is empty, so a toolkit that wants the ES2/shaders path
finds it missing and takes its own fallback rather than being handed a context that cannot draw. A
client that asks for an *indirect* context is refused, because indirect means sending GL commands to
the server, and there is nothing here to execute them. Measured, and the reason none of this needs a
GL implementation: with `glxgears` against the reference, 769 MB of `PutImage` and not one GL request.

**One framebuffer per screen, and every window keeps pixels of its own.** A window draws into its own
store, in its own coordinates and clipped to what it owns - the whole window, not only the part that
shows - and what reaches the screen is that store composited over it, clipped to the part of the window
that is visible. So a covered window goes on drawing and keeps what it drew, a revealed window gets its
pixels back with no `Expose` (which is what `backing_store` used to buy), a client that reads a window
back gets its contents rather than whatever is on top of it, and a *move* carries the pixels along.
The root is the exception: it **is** the screen. An ordinary window's store is opaque like the screen
and is composited with a straight copy; a window whose visual has an alpha channel keeps the alpha and
is blended, which is what makes a translucent window translucent. Coordinates are relative to the
parent, and every draw request is translated into the window's own coordinates on the way in.

**Obscuring is tracked.** A window's visible region is its rectangle minus everything stacked above
it - the siblings above it at every level of the tree, and its own children, which draw inside it -
and the composite back to the screen is clipped to that region, in the core rasteriser and through
RENDER alike. So a window does not paint over the dialog on top of it, which is what makes a popup
stay on screen instead of being wiped by the next repaint of the window underneath. When a window is
unmapped, destroyed, moved or restacked, the area it stops covering is handed back: the windows
underneath are composited from their own stores, so they are not asked to draw again. The area a
window *gains* is still painted with its background and sent an `Expose`, because that part the client
has never drawn. `VisibilityNotify` reports unobscured, partially obscured and fully obscured as that
changes.

**A client that stops reading is dropped, not waited for.** Every connection has a bounded queue of
outgoing messages and a writer thread of its own, so the only thread that ever waits for a client is
that client's own - and a queue that fills up means the client has stopped reading altogether, which
is what gets it disconnected. Before this, messages were written to the socket on the thread that held
the server lock, so one hung client froze every other client's screen.

**Replies and events are serialised to memory before they reach the socket.** Three faults of the
generated protocol classes are corrected there rather than at every caller, because every message
passes through it: a reply's length field is computed from the wrong base, short replies are emitted
without their trailing padding, and a **generic event's length field is never written at all** - so a
client reading Present's completion notification, which is forty bytes where an ordinary event is
thirty two, was told it was thirty two and read the next message from the wrong offset. `ClientOutput`
rewrites the lengths from the bytes actually produced and pads what needs padding, in one place.

## What is implemented

Core protocol: windows (create, map, configure, restack, reparent, circulate), the full drawing set
(points, lines, segments, rectangles, arcs, polygons, both text requests, `PutImage`/`GetImage`,
`CopyArea`/`CopyPlane`, `ClearArea`), GCs, pixmaps, fonts, cursors, colormaps, atoms, properties,
selections, keyboard and pointer mapping, focus, grabs (active ones, the implicit grab a press starts,
and passive `GrabButton`/`GrabKey` - a global shortcut fires, with the modifier, button and window
matching the protocol's rules), a **synchronous** grab and `AllowEvents` - a grab taken with
`GrabModeSync` stops the device, the events it produces wait in a queue, and `AllowEvents` starts it
again or reprocesses the event it stopped on, which is what a game that re-centres the cursor and a
remote-desktop client that redelivers a click are made of - the pointer's motion history
(`GetMotionEvents`, 256 events per screen), and `SendEvent`.

The keyboard's repeats are the server's own work, as they are on a real one: a held key starts
repeating 660 ms after it goes down and then every 40 ms until it comes up, a modifier never repeats,
and `ChangeKeyboardControl`'s `AutoRepeatMode` is honoured for one key or for the keyboard as a
whole. The delay and the rate themselves are XKB's (`SetControls`, behind `XkbSetAutoRepeatRate` -
the core protocol has no request for either) and are read back through `GetControls`; the per-key
bitmap is on both rails, and it is the same table in both. See `docs/backlog.md` B1 for what of
XKEYBOARD is still missing.

Events: structure notifications, exposes, property and colormap notifications, crossing and focus
events with the full detail lattice and the grab modes, pointer and keyboard events with propagation
and `doNotPropagateMask`, and `ClientMessage` in both directions.

The **notification area** is the host's menu bar (C9): a client that docks a tray icon -
Java's `SystemTray`, Qt's, GTK's - is shown there, and a click on the icon is delivered to the window
the client drew it into. `--no-tray` turns it off, and a server with no window has none.

Extensions: `BIG-REQUESTS`, `XC-MISC`, `SHAPE` (rectangles, masks and the five combine
operations), `XTEST` (synthetic input), `RANDR` (one CRTC, output and mode over the whole screen, with
the screen-change notification - `xrandr` prints it and `xrandr --listmonitors` names it, both byte for
byte as a reference server does), `XINERAMA` (the one screen, which is the answer a toolkit's
startup asks for), `XKEYBOARD` (the keymap, the state, and the version handshake - built from the
same table `GetKeyboardMapping` answers from, so a client that reads XKB and one that reads the core
mapping cannot disagree about what a keycode means; `xdotool` needs it and dies in its own startup
without it, and `xev` translates keycodes through it identically to a reference server),
`MIT-SCREEN-SAVER` and `DPMS` (the idle state: the settings a client sets are kept
and read back, `QueryInfo`'s countdowns count against the timeout that was set, a subscriber is told
when the state changes, and `xset q` shows both - though nothing here blanks the display, which is the
host window's business, and DPMS answers `Capable` = no for the same reason), `XFIXES` (cursor images, regions,
`SetWindowClipRegion`, and the selection and cursor subscriptions — a client that asks to be told
when the clipboard changes, or when the pointer moves onto a different cursor, is told, where
before it heard nothing), and `RENDER` — pictures, formats, `Composite` with the full operator table,
`FillRectangles`, `Trapezoids`/`Triangles`/`TriStrip`/`TriFan`, the solid and gradient fills, and
the glyph path (`CreateGlyphSet`, `AddGlyphs`, `CompositeGlyphs8/16/32`) that Xft and cairo draw
their text with.

`--scale 2` draws the screen at twice the pixels for the same physical size, which is what makes a
forwarded application sharp on a Retina panel: the host window becomes half the logical size and one
framebuffer pixel lands on one device pixel. `--scale auto` is the host's own scale factor, so it is
`--scale 2` on such a display. **It is off by default, and the client is the reason**: a client that
does not draw at the resolution it is told about lays itself out for a screen half the size it has
been given. Measured with a forwarded JavaFX application: 1280x800 of screen arrived as a 640x428
window, sharp and at half size. A client that reads `Xft.dpi` - GTK, Qt, an Xft `xterm` - follows by
itself, because a scaled screen publishes it in the root window's resource manager; a JavaFX or AWT
client has to be started with `-Dsun.java2d.uiScale=2` (or its toolkit's equivalent), which no server
can do for it. The server logs a line saying so when it is running unscaled on a scaled display.

The screen grows with the window it shows: dragging the frame's edge past what the screen can show
makes the screen bigger - the framebuffer, the root and the size clients are told all follow, up to
`--max-screen` - so a forwarded application can be given a window bigger than `--geometry`. The screen
never shrinks. The same rule covers a window that **arrives** too large for it, which is what an
application that centres a fixed-size window on the display does: a window mapped off the left or the
top of the screen is moved onto it (the pixels at a negative position do not exist, so nothing else
could ever show them) and the screen grows to the window's far edge, so `--geometry` does not have to
be chosen around the size an application asks for. A client that watches its root is sent a
`ConfigureNotify` for the new size, and one
that listens through RandR is sent `ScreenChangeNotify` - which is what a modern toolkit, browser or
terminal actually waits for.

The host window answers the two things an application asks its window manager for: a client that
goes full screen (`_NET_WM_STATE_FULLSCREEN`) gets the platform's full screen, and a client that sets
its urgency hint (`WM_HINTS`) brings the frame to the front once, without taking the keyboard.
Maximising is left to the client, which resizes its own window for it.

The host window comes back where it was left: where the user arranged it is remembered per client (by
`WM_CLASS`) in `~/.xserver/host-windows.properties`, and the next session opens there - clamped to the
part of the display windows may occupy, so a position remembered on a monitor that is no longer
attached does not put the window out of reach. The window's *size* is not remembered: it follows the
client's own windows, and a client that cares how big it is asks for that itself.

The host's clipboard is bridged to the X `CLIPBOARD` selection, both ways: a client that pastes
gets the host's text (served by the server itself, so no client has to be running), and a client that
copies puts its text on the host's clipboard. Text only, `CLIPBOARD` only - a pasteboard has no
primary selection, and there is no X target for the images and files a pasteboard can also hold.
`--no-clipboard` turns it off; a server with no window (`--backend headless`) never has it.

The host window follows the client it shows: its title and icon are the ones that client publishes
on its top-level window (`_NET_WM_NAME` or `WM_NAME`, and `_NET_WM_ICON`), and dragging its edge
resizes the client's window, which is told with a `ConfigureNotify`. A resize is capped at the
screen, since the framebuffer is not resized with it.

It also comes to the front by itself when an application arrives: the first top-level window a
client maps raises the frame, once per client and only after a frame has been shown, so starting a
forwarded application while something else is frontmost does not leave its window grey and
keyboardless behind it. A menu or a tooltip - anything `override_redirect` - is not a reason to take
the front away from whatever the user is doing.

Verified against real clients: `xdpyinfo`, `xwininfo`, `xprop`, `xlsclients`, `xwd`, `xeyes`,
`xlogo`, `xclock`, `xcalc`, `xmessage`, `xterm` with core fonts and with Xft (`-fa`, which needs
RENDER), and `twm` as a window manager.

## Bugs worked around in the protocol library

These are real defects in `x11-protocol-*:0.22.0`, each confirmed against Xvfb or the protocol
specification, and each worked around rather than patched upstream:

- **Variable length lists are decoded from the wrong arithmetic.** Readers size lists with
  `withInitialCapacity(length - javaStart)`, mixing four byte units with a byte offset. For most
  real requests the argument is negative and Eclipse Collections throws; for the rest it silently
  reads too few items, truncating `PutImage`, `PolyText8` and `QueryColors` data. Sixteen requests
  are decoded in `VariableLengthDecoders` instead.
- **`QueryTextExtents` miscounts by one** for an odd length string, because the padding byte is
  counted as half a character.
- **`GetModifierMapping` replies with the wrong `keycodes-per-modifier`**, writing the total keycode
  count rather than the per-row count. xterm segfaults reading such a reply.
- **A CARD8 comes back from `readCard8` as a signed byte.** Widened to an `int` it is negative above
  127, so a request whose *length or count field* is a CARD8 breaks at exactly half its range: the
  generated `ImageText8` reader hands the string's length to a list constructor and throws, and this
  server then answered `BadRequest` where a reference answers the request. **Measured 2026-09-28 with
  a real client**: xterm, redrawing after a window resize, sent a 130 character run and died with
  `X Error: BadRequest ... X_ImageText8`, taking its host window with it. `ImageText8` and
  `ImageText16` are decoded in `VariableLengthDecoders` (the mask is the whole fix), and
  `DrawTextTest` pins the boundary.
- **`GetOutputInfo`'s reply writes `numPreferred` in the wrong place.** The protocol's fixed part is
  `numCrtcs, numModes, numClones, numPreferred, nameLen`; the generated writer emits
  `crtcs, modes, numPreferred, clones, name`, so every count from `numClones` on is shifted by two
  bytes. A client reading the counts its own way is told the output has **no modes** - measured with
  `xrandr`, which warned *"Output screen is not disconnected but has no modes"* and printed the
  CRTC's mode without its `*current` marker. The reply is written out by hand in `RandRExtension`,
  byte for byte the way a reference's is.
- **`XReply.getLength()`** computes the reply length from the total size instead of the size after
  the header, so every reply's length field is wrong.
- **Replies shorter than 32 bytes** are written without their padding, which a client reads as part
  of the next reply.
- **`ConfigureRequestEvent`'s builder hides `value_mask`** behind private methods, so window
  managers can never see which fields a client asked to change. That event is assembled as raw
  bytes instead.
- **A generator that ignores an `eventcopy`'s own number** is the deepest one, because it is in the
  *generator* rather than in a class: five of `xproto.xml`'s events are declared as copies of another
  (`ButtonRelease` is `ButtonPress` with another number, `LeaveNotify` is `EnterNotify`, and so on)
  and the generator emitted each with the number of the event it copies. On the wire a release then
  looked like a second press, a leave like an enter and a focus-out like a focus-in. `ClientOutput`
  rewrites the five type bytes, and **that is where the fix lives**: the library is deliberately not
  forked, built or republished for five bytes (nor given a Lombok dependency, which a class-level
  override of the generated events would need), and `client/EventTypeByteTest` reads the bytes off
  the wire so the correction cannot be lost. `docs/fork-eventcopy-fix.md` records what is wrong
  upstream and how to build it, for anyone who ever wants to.

## The screen is opaque, even though the windows behind it are not

One invariant is worth stating because breaking it produces a bug that hides from every
server-side test: **the framebuffer only ever stores opaque pixels**. X has no alpha on the screen -
everything is composited onto an opaque root - but the raster operations can produce any 32 bit
value, and `GXclear` yields zero while `GXinvert` turns white into transparent black. A display
backend that honours alpha, as JavaFX does when it uploads a premultiplied pixel buffer, draws
those pixels as holes.

The trap is that `GetImage` reads back colour only, so a transparent-black pixel is still a correct
black: `xwd`, the tests and every protocol-level check agree the screen is right while the window
shows nothing. `Framebuffer` therefore forces every stored pixel opaque, and `XGC` initialises its
foreground to opaque black rather than zero - a client that draws with a graphics context's default
foreground is extremely common.

**A window whose visual has an alpha channel is the exception, and it is the only one.** What such a
client draws is meant to be composited over what is under it, so it cannot be written to an opaque
screen at all: those windows keep the alpha in their store (every window has one - see "One
framebuffer per screen, and every window keeps pixels of its own" - but only this one's
`Framebuffer` is told to keep it), and the part that shows is composited over the screen with the same
blend every other composite here uses. A translucent fill is translucent, a covered window keeps its
pixels and gets them back, and the window the screen holds is still an ordinary opaque picture - which
is what the rest of this section is about.

If a window is ever blank again, `-Dxserver.dumpFrame=<prefix>` writes the JavaFX image and the
framebuffer behind it to PNG files once a second. Comparing the two says immediately whether the
server failed to draw or the display failed to show it.

## A sharp picture on a HiDPI display

A client cannot draw more detail than the screen it is told about, and the host window can only
show what the framebuffer holds. On a display that draws two device pixels per logical one - a
Retina panel, or a 4K screen at 200% - a screen of the size the window *looks* is therefore shown
enlarged, and everything in it is soft. `--scale N` fixes that by making the screen N times the
pixels across for the same physical size:

```
xserver :1 --geometry 1200x800 --scale 2
```

`--geometry` stays the size the window looks; the screen, the framebuffer and the client's windows
are twice that in pixels, the client is told about a screen of twice the resolution, and the host
window shows that larger picture at the smaller logical size - one framebuffer pixel per device
pixel. A client that sizes its interface by the reported resolution then draws at the display's real
resolution, and the result is as sharp as it would be running natively.

Off by default, because a client that ignores the resolution gets a screen twice the size and would
otherwise just be laid out the way it was before. Clients that do ignore it can sometimes be told:
Compose Desktop / AWT applications take `-Dsun.java2d.uiScale=2`, which is what made termora sharp
in the measurement above. Either way the picture is only as detailed as the client draws it - this
option gives it somewhere to draw, it does not enlarge anything.

## RENDER formats are matched by shape, not by identity

A client does not look up a picture format by its id. It looks it up by the *shape* of the pixel
layout - `XRenderFindFormat(type, depth, redMask, greenMask, blueMask, alphaMask, ...)` - and the
masks it compares against are **field widths, not pixel masks**. `RGB24` is `red 0xff, green 0xff,
blue 0xff` with the shifts carried separately, and `ARGB32` is the same with `alpha 0xff`.

Reporting the full pixel mask (`0xff0000`) instead, or letting it be truncated to the 16 bits the
wire format gives it, says every channel is zero bits wide. Nothing errors: the lookup simply fails,
and Xft's per-display `solidFormat` - which it resolves once, as `ARGB32` - comes back null, so every
`XftDrawString` returns without sending a request at all. The window keeps its background and its
cursor and simply never draws text.

The same distinction has to be honoured when a picture is *read*: a drawable's pixels are held as
`0xAARRGGBB`, which is the right reading for a format with colour channels and the wrong one for a
format that is nothing but coverage. An A8 mask's pixel value is its alpha, so reading it as a colour
channel with an opaque alpha makes every pixel of the mask fully covered - and everything composited
through it is drawn as a solid rectangle in the source's colour. That is what an application's icons
and filled paths look like when it happens: blank blocks where the artwork should be. Text is
unaffected, because the glyph path decodes through the format already; only pictures did not.

Two smaller traps in the same area: the `CompositeGlyphs` command stream is in the connection's byte
order (only the glyphset-switch item is always most-significant-byte first), and a glyph is drawn at
`origin - info.x/y`, not `+`.

## Known limitations

The list below is what a user notices; the working list of everything still missing, item by
item with a plan and a way to verify each one, is [docs/backlog.md](docs/backlog.md).

- **XInput2 is there**, in the shape a server with one pointer and one keyboard can honestly have:
  the version handshake, the device list (two masters, with their classes), the client pointer, the
  event selection, the pointer query and warp, the cursor, the device **grabs** - which is what a
  toolkit's menus go through - and the XI2 twin of every core input event this server delivers, with
  the core one withheld from a client that selected the device one. `xeyes` no longer warns, `xinput`
  lists the devices, and JavaFX's GTK backend and Swing's AWT both work through it rather than around
  it. The two **device property reads** are answered "none" rather than refused - `XIGetProperty`
  (which GTK asks as an application starts) says "absent" for any property, and `XIListProperties`
  says the device has none, which is what `xinput list-props` prints as *does not report any
  properties*. Both are the shapes a reference answers for a property that is not there, and the same
  shape RandR's `GetOutputProperty` is answered with here. Not there, and refused rather than ignored:
  touch, tablets, barriers, the raw event family, and the device property requests that *change*
  something (`XIChangeProperty`, `XIDeleteProperty`) - this server has no device behind them.
- **DAMAGE, and the three a compositor needs: Composite, Present and SYNC.** A client that wants to
  hear what changed in a window - a screencast, a magnifier, a remote-desktop encoder, anything that
  reads pixels rather than drawing them - creates a damage object and is told, at X's own levels:
  every change as it happens, or once per `Subtract` at the bounding levels. What a notify carries is
  a bounding box, and the rectangles are available through `Subtract`'s repair region.
  A compositing window manager is what the other three are for, and **the store is what made them
  cheap**: `Composite` redirects a window - its drawing goes on going into pixels of its own and
  simply stops reaching the screen, which is the one rule that had to be added - and
  `NameWindowPixmap` hands the compositor a live view of those pixels, so it never reads the screen.
  `Present` is the same path from the other end: a client that has rendered a frame itself hands it
  over as one `PresentPixmap` and is told when it arrived. `SYNC` gives the counters and the fences a
  toolkit waits on to know that a frame has been *seen*. Each was measured against a reference before
  it was written (`tools/CompositeProbe`, `SyncProbe`, `PresentProbe`), and two of the three traps
  those runs found were in this server's own reading of the protocol rather than in the code:
  `CreateFence` carries its drawable *before* its fence, and a generic event's length field is the
  only thing that tells a client it is longer than thirty two bytes.
  **RandR is there**, in the shape this server has: one CRTC, one output and one mode covering the
  whole screen, a real physical size (so a client can compute a DPI), and a `ScreenChangeNotify` to
  the clients that asked for one when the screen grows - which is what C1's screen growth had no way
  to announce before. A request to *change* the screen, or to create a lease, is refused with
  `BadImplementation`: the screen belongs to the host window. What a client can **ask** is answered
  even where the answer is "none": the output has no properties (`ListOutputProperties` is empty, and
  `GetOutputProperty` answers an **empty reply** - format 0, type `None`, no items - which is what a
  reference server answers for a property that is not there, measured. JavaFX asks for one as it
  starts, and both a refusal *and* an error would leave it worse off than an answer it can read), there are no providers (`GetProviders` answers
  zero, so `xrandr --listproviders` prints `number : 0` like the reference), and
  `xrandr --prop` prints no property lines.
- **XVideo, answered honestly rather than decorated.** A player asks which adaptors the server has -
  hardware that scales and colour-converts a video frame - and there is none behind this server's host
  window, so `QueryAdaptors` says **zero adaptors**: the answer that sends a player down its own
  non-Xv path, and the answer a reference server without that hardware gives too (measured). The
  extension's own version 2.2, its two events and three errors are there, a request that names a port
  is refused with its own `BadPort`, and `SelectVideoNotify` is accepted for a drawable. **No software
  scaler is built behind it**, deliberately: this would be the only server advertising an adaptor it
  does not have, and a player would take a video path that ends in a scaler nobody asked for instead of
  the fallback it takes today.
- **SECURITY, for running a client you do not trust.** A trusted client asks for an authorization
  and hands the token to the client it does not trust; the connection that one makes is *untrusted* -
  it draws its own windows and asks the questions every client asks, and it is refused the pixels,
  properties, configuration and focus of windows that are not its own, refused the selections, and it
  cannot even see the extensions a trusted client can (a reference answers its `ListExtensions` with
  two where a trusted client sees twenty three). Revoking the authorization ends the connections using
  it. Measured against a reference with access control on (`tools/xvfb.sh secure`), with
  `tools/SecurityProbe`, and checked through Xlib's own `XSecurity` client
  (`tools/xsecurity-client.c`). **A token works with or without `--auth`**: `-ac` means "never refuse a
  client for a missing cookie", not "ignore the credentials it does offer".
- **DOUBLE-BUFFER, so a client can draw off screen and show the result in one step.** A client
  allocates a back buffer *on* a window, draws into it with the ordinary requests - a back buffer is a
  drawable - and `SwapBuffers` makes what it drew the window's contents, which is what every toolkit's
  no-flicker path is looking for and what all of them fall back from today. The buffer follows its
  window: a resize reallocates it at the new size and fills it with the window's background. The four
  swap actions are the measured part and one rule covers them - the window takes the first allocated
  buffer's contents, and every buffer of that window is then left holding what the action names
  (`Copied` and `Undefined` the new front, `Untouched` the front's previous contents, `Background` the
  window's background). Measured against a reference with `tools/DbeProbe`, and checked the harder way
  with **Xlib's own `Xdbe` client** (`tools/xdbe-client.c`), whose readers parse these replies and whose
  request builders drive the swaps: identical on both servers.
- **RECORD, which is how one client watches another's traffic.** A client creates a context over a set
  of clients and a set of ranges - requests and replies, core and extension, delivered events, errors,
  a client arriving and a client leaving - enables it, and from then on receives one reply per recorded
  element on that connection: the request as it arrived, the reply or event it produced, the setup
  reply a connecting client was given, the last sequence number a departing one sent. That is what
  `x11vnc -record`, a replay tool and a test harness stand on, and it is the extension that lets a
  recording be taken *inside* the server rather than in front of it - which is what `tools/XProxy` does
  here, and what this replaces for anything that can ask. Measured against a reference with
  `tools/RecordProbe`, which drives the same protocol-only gesture on a second connection and prints
  every datum (category, element header, stamps, which client it is attributed to, and the payload
  decoded) - identical element for element. One difference is this server's own execution model and is
  documented rather than papered over: input is derived inside the delivered core event, so a recording
  here has no *device* elements, and the events a request produces are attributed to that request where
  a reference records a device stage of its own.
- **XKEYBOARD answers what a client asks before it will trust the keyboard, and no more.** The
  version handshake, the keymap (`GetMap`), the modifier state (`GetState`) and the keyboard's
  controls (`GetControls`/`SetControls`) are there, the requests that have no answer here
  (`SelectEvents`, `LatchLockState`) are accepted rather than refused, and `PerClientFlags` answers
  "none supported" instead of erroring - which is what JavaFX sends as an application starts. A client
  can also **set** the keymap (`SetMap`), applying its keycodes' keysyms to the one table both this
  extension and `GetKeyboardMapping` read, which is how `xkbcomp <file> <display>` loads a chosen
  layout. **The repeat delay and rate are settable through XKB and no other way** - the core protocol
  has no request for either - so `SetControls` with `RepeatKeys` changes them, and a client that wants
  a 200 ms delay gets one. **A layout can also be chosen by name, and `setxkbmap` is what does it**:
  `setxkbmap -layout de` exits 0 against this server, and every keycode then answers what a reference
  server answers in that layout - `xmodmap -pke` is byte-identical to Xvfb's for `us`, `gb`, `de`,
  `fr`, `es`, `ru` and `cn`, and every client is told to re-read the mapping. What makes that work is
  the reply *carrying the keymap*: Xlib reads a by-name reply by position (one embedded component
  reply per bit of `reported`, and it gives up entirely when that is zero), so a server that installs
  the layout but answers nothing is indistinguishable from one that did not. What
  is *not* there: a variant is answered as its base layout; the
  map's types, actions and behaviours (a client gets its characters right and everything else as it
  was), the compatibility map, the indicator names, every keyboard control other than
  repeat (slow keys, mouse keys, AccessX and the rest are accepted and not applied, and reported as
  not enabled), the "detectable auto-repeat" a client can ask for, and locking or latching a modifier
  from a client, which would lock the machine's own keyboard for everyone. `GetNames` is answered, but
  only for the one component this server can honestly name - the symbols component the client itself
  asked for - and `SetNames` is accepted and dropped, so a client that *sets* names and reads them
  back gets the name from its own by-name load rather than the one it set. One caveat worth knowing: a
  `GetMap` reply has to carry XKB's four mandatory key types or Xlib rejects the whole reply, so a
  server that offers *fewer* is not gracefully degraded, it is a `null` keymap the client will fault
  on.
- **SHAPE's bounding shape is honoured for drawing, obscuring and hit testing**: a shaped window is
  painted only inside its shape, its children are clipped by it, and the corners it does not cover are
  given back to the screen underneath. **Cursor shapes are composed the way X composes them**: the
  cursor font's own 74 bitmaps are embedded, and `CreateGlyphCursor` against *any* font the server
  serves is drawn from that font's glyphs - the mask character's bitmap is the silhouette, the source
  glyph's ink is drawn inside it in the foreground colour, and the hotspot is the glyph's origin at the
  font's ascent - all measured against a reference with `tools/GlyphCursorProbe` and identical for
  fourteen characters. What a cursor does *not* have here is a name looked up in a font: a client that
  asks for a cursor by name gets `BadImplementation`, which is what `docs/backlog.md` A8 measured a
  reference doing too (its lookup fails silently). Still missing: `ShapeNotify` details beyond the
  extents.
- **A picture over a window draws into that window's own pixels.** RENDER's pictures address the
  window's store in the window's own coordinates (see "Every window keeps its own pixels" in the
  design notes), and what a request wrote is composited when it ends - so a toolkit that draws an ARGB
  window with cairo gets translucency, not an opaque square on the screen. A picture may write
  anywhere the window owns, showing or not, while a read is still stopped by whatever covers it.
- **A picture over a window sees the window's own pixels, not its children's**, unless the client
  asks otherwise: `SubwindowMode` is honoured, so `IncludeInferiors` gives a client what the window
  *shows* - a point a child covers is answered from that child's own store - which is what copying the
  screen through a parent window needs. What is stacked above the window is excluded either way.
- **Every window keeps its pixels, so a reveal asks nobody to draw.** `backing_store` is a hint the
  server is free to satisfy always, and it does: a window's contents are kept while it is covered and
  come back with no `Expose`, whatever the attribute says. The area a window *gains* (a map, a resize,
  a restack) is painted with its background and exposed, because that part has never been drawn.
  **A window too large to keep pixels for draws straight onto the screen instead** (the path the root
  takes): a store is the window's own size, so it is bounded by the largest screen this server can be
  asked for (`--max-screen`, 8192x8192 by default) - a client that creates something bigger is not
  refused and simply does not keep what a window on top of it covers (A29).
  `save_under` is **not** implemented - measured, and the reference does not implement it either.
- **The VNC backend sends raw frames, and nothing else yet.** `--backend vnc` serves the whole
  screen (and so implies `--single-window`) to any VNC client as uncompressed damage rectangles, with
  RFB's `None` security or a password from a file, and **the clipboard goes both ways** - a viewer's
  copy lands in the X `CLIPBOARD` selection and on the Mac, and a copy in the session reaches the
  viewer, and a browser can be one of the clients (`--vnc-ws-port`, no bridge needed). Frames are raw
  unless the client asks for **ZRLE**, which every VNC client understands and which turns a desktop of
  flat colours into kilobytes instead of megabytes, and a client that asks for the **Cursor**
  pseudo-encoding is sent the X pointer's own shape. **An update is the box the differing pixels are
  in, and an unchanged screen is sent nothing** - the session holds what each client has been given and
  compares against it, so a viewer watching an application that repaints the same pixels costs no
  bandwidth and no decoding at all (measured: 5.39 MB/s and 61 updates a second before that comparison,
  against `x11vnc`'s nothing - A25 in [docs/backlog.md](docs/backlog.md)). **An update carries every
  tile that changed** (A31), and a rectangle whose content is photographic goes as **Tight's JPEG
  method** when the client offers it (A32). Not there: Tight's zlib path and `CoRRE`.
- **A tray icon is shown in the menu bar and stays where the client drew it.** The docked window is
  a window of the X screen like any other - this server has one surface, so a window off it would
  have no pixels to read - and its image is copied into the menu bar on top of that. A client's own
  context menu therefore opens next to its window on the X screen, not under the menu-bar icon: the
  menu is a window the client places itself, and no tray host here can move it. And the icon keeps its
  **shape**: `_NET_SYSTEM_TRAY_VISUAL` publishes the 32 bit ARGB visual, the client's icon window is
  made with it, and a window with an alpha channel keeps its own pixels and is composited - so a
  transparent corner stays transparent instead of arriving as a light square. What a *screenshot* of
  the menu bar shows has not been confirmed: the icon is handed to the menu bar (the server logs it)
  but was not found in the captures, which is the trap C9's own record warns about.
- **Fonts are real X fonts.** `fixed`, `6x13`, `7x13`, `9x15`, `10x20` and `12x24` - and the XLFD
  names they answer to - are served from tables captured off a reference server, and **any other name
  is looked up on the font path**: `fonts.alias` maps a short name to a full one, `fonts.dir` maps
  that to a file, and the file (`.pcf`, `.pcf.gz` or `.bdf`) is read. `~/.xserver/fonts` is on the
  default path, and `SetFontPath` replaces it. Measured against Xvfb with `tools/kit.sh FontProbe`,
  `QueryFont` field by field, `QueryTextExtents` and the pixels of a drawn string are **identical**
  for the built-in fonts and for one read from a file. The last resort - a name nothing has - is a
  Java font with the JVM's shapes and metrics. A font reached through an alias is listed under the
  alias and **reported under its own XLFD name**, which is what a font path does and what a client
  needs: `ListFonts` answers the name a pattern matched and `ListFontsWithInfo` the font's own name,
  which is where a client reads a font's charset from. **And a font is loadable by both names and by
  a pattern**: the table is indexed by each font's XLFD name as well as its alias, a wildcard name is
  resolved to the first table font it matches, and the font list carries the XLFD names - so the name
  a `ListFontsWithInfo` reply gives out is a name the server then finds. That is the whole of what a
  font set needs, and it was three separate misses away: measured against Xvfb, a font loaded by its
  own XLFD name here had **no `CHARSET_REGISTRY`/`CHARSET_ENCODING` properties at all** (the Java
  approximation has none, and Xlib reads a font's charset from exactly those), a pattern was treated
  as an unknown name, and the XLFD names were not listed. With them fixed, `XCreateFontSet` answers
  the same as the reference's, and an X input method on the client side works: `ibus` composing
  through XIM into a forwarded `xterm` commits Chinese (`docs/backlog.md`, C10).
- **Pointer acceleration is accepted and ignored**: the platform's own acceleration is what the user
  feels, and nothing here adds to it. (`GetMotionEvents` does keep a history now - 256 motions per
  screen - so a client that asks what the pointer did is answered.)
- **A focus change reports FocusIn and FocusOut, but nothing moves the focus on its own.** Every
  window on the way in and out is told, with the relationship lattice in the detail, `None` is a state
  of its own, and a window that stops being viewable gives the focus up the way the last
  `SetInputFocus` asked for - all of it measured against a reference server. A keyboard grab is the
  one thing that does move it: taking one reports the focus arriving on the grab window with
  `NotifyGrab`, ending one reports it going back with `NotifyUngrab`, and a change asked for while a
  grab is held is reported `NotifyWhileGrabbed`. With the focus on PointerRoot the keys follow the
  pointer and no focus events are generated as it moves, which is what a reference server does too:
  measured, and recorded in `docs/backlog.md` (A5), where an earlier claim to the contrary is
  corrected. **The root window is a third state, not PointerRoot**: `SetInputFocus` with the root's own
  id keeps the keys on the root window and `GetInputFocus` answers that id, where PointerRoot sends
  them to whatever the pointer is in - and the two report different events, both measured. The focus
  this server starts with is the root *window*, which is what a reference server starts with.
- **A window that appears under a stationary pointer is entered by the move that follows**, not by
  the map. Xorg reports it at the map; doing so here tears EasyShell's context menus down as they are
  mapped. See the design notes for the measurement.
- **A menu that is left and re-entered used to be dismissed** by EasyShell, where it stays up against
  a real X server. The crossings matched the reference event for event, which is what made it a
  puzzle: the cause was not in them but under them - the client was never told it had the keyboard,
  because `SetInputFocus` generated no focus events. With those delivered the scripted gesture leaves
  the menu up 6 runs out of 6 where it closed 3 out of 3, and the design notes record what that took.
- **The screen saver's settings and state are kept, and nothing blanks.** `MIT-SCREEN-SAVER` and
  `DPMS` are advertised and answer honestly, so `xset s`, `xset dpms` and a client that checks before
  blanking all get an answer rather than "extension not present" - but the display belongs to the host
  window and the system's own screen saver is what blanks it, so the server reports and does not act.
- **Both gravities are applied.** A child moves the way its own `winGravity` says when its parent is
  resized (and, also like the reference, the child is not sent a `ConfigureNotify`), and a window's
  own contents move the way its `bitGravity` says when *it* is resized: the same three-by-three grid,
  measured against a reference server for all eleven values including `Static`, which moves nothing,
  and `Forget`, which throws the contents away. The pixels that move are the visible ones - nothing
  keeps an obscured window's content - and the area they leave is painted with the window's background
  and exposed.
- **A resize grows the screen, up to `--max-screen`.** The host window is resizable and resizes the
  client's window; a drag past what the screen currently shows makes the screen bigger first, so a
  forwarded application can be given a window larger than `--geometry`. Past `--max-screen` the drag
  stops, and the screen never shrinks again for the session.
- **A file dragged from the desktop reaches a forwarded application, and only if the application can
  open what it is handed.** The drag is bridged as XDND, the application answers it (`XdndStatus`, then
  a `ConvertSelection` for the data - measured), and what it is handed is a **host path**: XDND is
  URI-based, so a target *opens* the file and nothing downloads it. Across machines that path is
  nowhere, which is the whole of "nothing happens" - and it is why the same application running
  natively shows its release marker. **Give the client a filesystem that can see the host's directory
  and it works**: with a shared folder (or an sshfs mount) and
  `--drop-mount /Users/me/Shared=/mnt/hgfs/Shared`, a file dragged from *inside that directory*
  arrives and the application uses it - verified against a real forwarded application on 2026-09-27.
  Dragging *out* of an application is the half that is not finished: its trigger was reverted, because
  it took over drags the application meant to keep inside itself and froze one client.
  `docs/backlog.md` C5 has both measurements. An application from before drag and drop - `xterm`,
  `xclock` - has no way to receive one and always will not.
- **Text the host's input method composes reaches a forwarded application as a key press**, which is
  the only thing an X client can be sent: the server claims a keycode for the character and tells the
  clients the mapping changed. What is *not* bridged is the input method's own window - the candidate
  list is the platform's, and a client-side input method (fcitx, ibus) needs `--single-window` for the
  reason below. **A VNC viewer cannot carry this at all** - RFB's key events are keysyms, not composed
  text - so typing Chinese through a VNC client needs the input method on the *client* machine instead:
  run `ibus-daemon -drx` with a Chinese engine there and start the application with
  `XMODIFIERS=@im=ibus` (plus `GTK_IM_MODULE=ibus` for a GTK/JavaFX client), against a
  `--single-window` server so the candidate window lands over the application.
- **Each client gets a screen of its own, which anything that is not an application notices.** An X
  input method, a window manager or a clipboard manager draws windows positioned over somebody else's
  window - an input method's candidate list being the clearest case - and in the default mode that
  window lands on the client's own screen, so it is shown in a host window of its own instead of over
  the application. Run such a client with `--single-window`, where every client shares one screen.
- **A screen grows but does not shrink.** Dragging a forwarded window past the edge of the screen
  makes the screen bigger so the client can be told about the larger window; nothing brings it back
  down when the window goes small again, so the extra framebuffer stays allocated for the session.
- **The clipboard bridge is text only, and only `CLIPBOARD`.** Images and files on the host
  pasteboard are not offered to clients (there is no X target for them that a forwarded application
  would have asked for), `PRIMARY` is not bridged, and a client that copies a very large amount of
  text has it delivered through `INCR`. Copying inside a forwarded application replaces the Mac's
  clipboard - the host-to-X direction would otherwise have nothing to notice, since macOS does not
  report that a pasteboard was read.
- **A popup that extends past the client's window is clipped by the host window.** The window is
  sized to the client's own windows - a menu is transient and does not resize the frame it opens in,
  which it used to, growing the window and shrinking it back every time one was opened. Stretching the
  window to fill the screen is what makes room for a menu near its bottom edge.

## Tests

```sh
mvn test
```

A whole-server verification pass - every area, the tool to drive it with, and what "pass" means,
including the differences that are deliberate and the traps that make a run lie - is
[docs/verification-checklist.md](docs/verification-checklist.md).

- `RasterizerTest` — the raster ops, line and arc geometry, dashes, fills and clipping, checked
  pixel for pixel against Xorg.
- `VariableLengthDecodersTest` — round trips every request the custom decoders handle, by encoding
  with the generated writer and decoding with the server's reader.
- `JavaFxBackendTest` — proves pixels drawn into the framebuffer reach the JavaFX image, which a
  window on a hidden display cannot show.

## Not affiliated with X.Org

The protocol implementation is independent; the X11 specification and the XCB protocol
descriptions were the reference.
