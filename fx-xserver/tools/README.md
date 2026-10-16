# Verification tools

The kit the backlog (`docs/backlog.md` §1) refers to: small programs that drive a server, capture what
it drew, and compare that against a reference server. They are **not** part of the server, and nothing
in `mvn test` depends on them - but every item in the backlog ends in a **Verify** step that one of
these is meant to carry out, so keep them working.

## What is here

| what | where | why |
|------|-------|-----|
| the tools | `src/test/java/io/github/oyzh1994/xserver/tools/` | Java, in the test source root so `mvn -o test-compile` compiles them with everything else: a tool that stops compiling is caught instead of rotting |
| the launcher | `tools/kit.sh` | `tools/kit.sh <Tool> <args>` - the tools with the classpath filled in, so no tool has to be pasted in with twenty jars |
| the two-machine harness | `tools/vm.sh`, `tools/bed.sh` | start a server here and a client on the VM against it (or against the VM's own Xvfb, for a comparison) |
| the reference display | `tools/xvfb.sh` | `start`/`stop`/`status` for the VM's `Xvfb :98` plus `x11vnc`: the other side of every comparison, and the three flags it needs are easy to get wrong (below). **`secure`** starts a *second* reference on `:97` **with access control on** (a fixed cookie), which is what SECURITY's trust levels can only be measured against - an `-ac` server trusts everybody (F7) |
| one-command comparison | `tools/compare.sh` | the six steps a rendering comparison takes - both beds, the same gesture on each, the same capture tool, the `Xwd` diff and the `Blocks` grid - as one command |
| the wire meter | `tools/vnc-meter.py` | a real RFB client that keeps **one incremental request outstanding** and counts what the server sends back, per second: MB/s, updates/s, JPEG rectangles, and the seconds a request was **held** because nothing changed. `--enc` chooses what to advertise (`--enc 7,16,0,-23,-32` asks for Tight's JPEG as a viewer would), `--trace` prints every update's rectangle, and `--dump` decodes ZRLE into the client's own picture as an XWD for `tools/kit.sh Xwd diff` against `xwd -root` of the server's framebuffer. The instrument behind A25 (**5.39 MB/s and 61 updates/s of an unchanging screen** against `x11vnc`'s nothing), A31 and A32 (photographic content: **3.12 MB -> 0.70 MB** with Tight's JPEG advertised), and the check that skipping an unchanged update is not losing one - it skips a Tight-JPEG rectangle's compact length, so it can measure either encoding |
| the cursor font generator | `tools/gen-cursor-font.py` | regenerates `resource/CursorFont.java` from `cursor.pcf` with PIL - run it on the VM, where the X11 fonts live, and copy the result back |
| the XDND pair | `tools/xdnd-source.py`, `tools/xdnd-target.py` | **a real GTK3 drag source and a real GTK3 drop target**, for the C5 work: run them as **two processes** (a drag inside one GTK process never uses XDND at all - GDK shortcuts it) and drive the gesture with XTEST. This is the "real toolkit on the other end" the C5 entry asked for: a Finder file dropped into a real GTK target on this server arrives, and an application answers the same conversation (`XdndStatus` and a `ConvertSelection` for the data, measured) - what it *cannot* do is open the file, because XDND hands over a host path (C5 has the measurement and `--drop-mount`). It prints every step, so a conversation that stops halfway is visible as the step that never arrived. Needs PyGObject + GTK3, which the VM has |
| the host drag source | `tools/kit.sh HostDragSource <file> [x] [y]` (source: `src/test/java/io/github/oyzh1994/xserver/tools/HostDragSource.java`) | a macOS window that is `alwaysOnTop` and holds a real file: press on it and drag, and the platform starts a **real drag** - the gesture C5's host half needs, and the one a Finder-sourced scripted drag cannot produce here. It lives in the tools package like every other tool, so `mvn -o test-compile` keeps it compiling; **click it two or three times before dragging** or the first synthetic click is swallowed as the activation click (measured) |
| the Xlib client for DOUBLE-BUFFER | `tools/xdbe-client.c` | a real Xlib client using `Xdbe`, built and run **on the VM** (`gcc xdbe-client.c -o /tmp/xdbe-client -lX11 -lXext`, needs `libxext-dev`): it is the one check `DbeProbe` cannot be, because it is **Xlib's own** readers that parse this server's `GetVisualInfo` reply and its own request builders that drive the swap - and it found a real difference the probe had not (a `GetVisualInfo` that names no drawable means "the display's screens", not "nothing"). Both servers print the same thing except the count of visuals, which is the screen's own |
| the Xlib client for SECURITY | `tools/xsecurity-client.c` | a real Xlib client using XSecurity, built and run on the VM like the DBE one (`gcc xsecurity-client.c -o /tmp/xsecurity-client -lX11 -lXext`). It asks for an authorization, hands the token to a second connection through `XSetAuthorization`, checks that the root's pixels are refused while its own window is drawn and read, then revokes it and watches the connection end. It needs a display that checks credentials: `tools/xvfb.sh secure` (then `XAUTHORITY=<a file with that cookie> DISPLAY=192.168.22.152:97`) or this server with `--auth` |
| the host-window tools | `tools/host-windows.c`, `tools/host-drag.c`, `tools/host-type.c` | C, built with `clang -framework ApplicationServices`: the first lists the on-screen windows with their bounds (needs no permission), the second makes a left-button drag between two points - which is how a real window is moved or resized by a script, and the only way C7's "remember where the window was" and C1's "the screen grows with the window" could be verified against a real window. **Both ends on the same point make it a click**, which is how D1's live check closes a window the way a user does (the close button is at the top left of the title bar, about 14,13 in from the bounds the first tool prints) |

Run a tool with the test classpath - `tools/kit.sh` is that line with the classpath filled in, which
is worth using because pasting the twenty jars by hand produces `command too long` from the shell:

```sh
mvn -o test-compile
tools/kit.sh WinTree 127.0.0.1 6095
tools/kit.sh Drive 127.0.0.1 6095 "M 150 200" "W 300" "P 3" "R 3"
```

Start a bed (server + client), in one of the two modes that matter:

```sh
tools/bed.sh single      # --single-window: a probe/XTest driver can reach the client's windows
tools/bed.sh perclient   # the mode a user runs: one host window per client
tools/bed.sh xvfb        # no server here: the client runs on the VM's Xvfb :98 (the reference)
XSERVER_OPTIONS="-Dxserver.dumpFrame=/tmp/d -Dxserver.dumpFrames=3000" tools/bed.sh single
```

`tools/vm.sh` runs a command on the VM over ssh. The password is read from `$XSSH_PASSWORD` or
`~/.xserver-vm-pass` and is **deliberately not in this repository** - write it once into that file.
`expect` hangs on the password prompt in this environment, which is why the script goes through
`SSH_ASKPASS` instead; long-running remote commands need `setsid nohup … < /dev/null &` or ssh kills
them when the session ends.

**Talking to a detached server** (D1): a server started with `--control-port N` answers one command per
connection on `127.0.0.1`, so no tool is needed beyond `nc`:

```sh
printf 'status\n' | nc -w 2 127.0.0.1 6200    # display :95 sessions=1 windows=0 detached=1 clients=2
printf 'attach\n' | nc -w 2 127.0.0.1 6200    # opening a window for 1 session(s); 2 client(s) connected
```

Two tools that are not Java, and need building (macOS only):

```sh
clang -o /tmp/winlist tools/host-windows.c -framework ApplicationServices
clang -o /tmp/drag    tools/host-drag.c    -framework ApplicationServices
/tmp/winlist                       # owner | title | x,y WxH, one window per line
/tmp/drag 650 209 950 329          # a left-button drag, as a hand makes it
# (both ends on one point is a click; and a *drop* needs a pause before the button goes up -
#  this one releases it too soon after the last move for the platform to deliver the drop,
#  which is fine for moving a window and not for dropping a file on one)
clang -o /tmp/type tools/host-type.c -framework ApplicationServices
/tmp/type "你好"                    # types text, each character as a Unicode key event
/tmp/keypid <pid> "你好"            # the same, posted straight to a process instead of the front window
```

**A client that misreads an event can be read from its own side, and for AWT that is a logging
switch.** `tap.py` shows what a server *sent*; when a client behaves as though it were sent something
else, the JDK's event logger shows the other half - what the toolkit *read*. Put

```
handlers=java.util.logging.FileHandler
.level=INFO
java.util.logging.FileHandler.pattern=/root/jul.log
java.util.logging.FileHandler.append=false
sun.awt.X11.event.level=FINE
```

in a properties file and start the Swing application with
`JAVA_TOOL_OPTIONS="-Djava.util.logging.config.file=<that file>"`: every button event is printed as
the toolkit's own struct (`XWindow.handleButtonPressRelease` logs `XButtonEvent = type = ...
button = ...`), which is how backlog A18's "Nonexistent button 16" was pinned to a *reading* of ours
rather than to our bytes.

**The same relay is how two servers' *events* are compared byte for byte, which is what found the XI2
shapes** (2026-09-29). Point the *same* client at `tap.py <listen> <this-server>` on one run and at a
second relay in front of the reference on another (`tap.py 6097 6098` **on the VM**, since the
reference is there), drive the same gesture, and decode both logs as messages: a reply is 32 bytes
plus four per length unit, an event 32, and a `GenericEvent` 32 plus four per its own length field -
while **the first message on a connection, the setup reply, is the one with an eight byte header**
(its length is a CARD16 at 6). The line to compare is the size: an XI2 key is 120 bytes, a crossing 76
and a focus event 104, and a server that sends all of them at 120 is one whose fields sit where a
toolkit does not read them.

`winlist` needs no permission; `drag` posts events into the session and so needs the *Accessibility*
permission for whatever is at the top of the shell's parent chain (the same grant that screen
recording needs, see the testbed notes). Dragging a window by its title bar moves it; dragging its
bottom-right corner resizes it, which is what C1's verification does.

**The sweep's id mapping is a run's own, so a probe that prints *another client's* id makes the
verdict flaky.** `probe-diff.py` maps every hex literal to the order it first appears in, which is
stable for ids the probe itself chooses - but a base the *server* assigns to another client can appear
twice in one run and once in the other when a server reuses a freed one (this one does; a reference did
not, on 2026-09-28), and the numbering of everything after it shifts. `RecordProbe` was reported
identical on one sweep and different on the next with the datums byte for byte alike. The rule that
came out of it: **a probe prints what an id means, not the id** - "idBase=actor", "spec=keeper" - and
the numbers stay where they are compared deliberately, in the tests.

**A probe connects to the LAN, and this machine has a system proxy.** macOS's `java` launcher passes
the system's HTTP and SOCKS proxy settings to the JVM, so on a machine where a proxy is running, a
probe's socket goes to the proxy instead of to the display - which reads as `Malformed reply from
SOCKS server` from a probe that has nothing to do with SOCKS, and only while that proxy happens to be
up (measured 2026-09-28, twice, with two different misleading errors). Java's non-proxy list cannot
save it: it matches host names and globs, not the `192.168.0.0/16` CIDR entry that would have covered
this testbed. `RawXClient` therefore opens its socket with `Proxy.NO_PROXY`, and `tools/kit.sh` asks
the JVM not to read the system settings either.

**Do not `pkill` by a name the *test bed itself* is running under.** `pkill -9 -f dbus-daemon`, while
setting up C10's input method on 2026-09-26, took the session bus down with it and EasyShell - still
connected and drawing - went with it, thirty seconds before the measurement it was there for. Kill
the process you started, by its own name and its own arguments (`pkill -9 -f "ibus-daemon -drx"`), and
treat the shared daemons - the session bus, `xdg-*`, the window server - as the user's, not the run's.

**Killing a client on the VM: put the pattern in brackets.** `pkill -9 -f EasyShell` matches the
command line of the *remote shell running it* - which contains the word - and kills it, so ssh
returns 255 and nothing you asked for happens, with no output to say why. Write
`pkill -9 -f '[E]asyShell'`: it still matches the running application and no longer matches the
command that is doing the killing. (The same trap applies to any `pkill -f` whose pattern appears in
its own command line, on either machine.)

**The brackets do not save you when the path itself is on the same command line.** Launching with
`pkill -9 -f '[E]asyShell'; ... /root/EasyShell_v1.2.2_linux_arm64_build.AppImage` still kills the
shell it is running in, because the *path* contains `EasyShell` and the pattern matches that. The
launcher is a script on the VM with the path inside it (`/tmp/es-start.sh`, `/tmp/termora-start.sh`),
so the command line that runs it names no application at all. And the AppImage is not the process
either: it mounts itself and runs `/tmp/.mount_<something>/bin/<App>`, so a stop has to match
`mount_` as well - `pkill -9 -f mount_EasySh` - or the client is still there, still holding its
windows, looking like a server that does not clean up after a dead client.

The tools, one line each (the backlog's §1 table says what each is *for*):

- `GlxProbe` - the GLX questions a client asks, field by field and as bytes - B6's harness. It exists
  because `glxinfo` says "no GLX" and stops: there is no other way to see what a client needs from a
  server, or what a reference answers. It also carries the two layouts that are easy to get wrong
  (the opcode numbering from 18 up, and `QueryServerString` being screen and name with no string), and
  the measurement that reframed B6 - a software GL client sends **no GL commands at all**, it renders
  itself and blits with `PutImage`.
- `BlinkProbe` - **times the changes in a small rectangle of the screen**: it reads the box with
  `GetImage` as fast as the server answers (thousands a second) and prints a line with the interval
  whenever the pixels change, which separates a blink from a still picture where the frame dumps cannot
  (they are written once a second, so a flicker at thirty frames a second lands on the same phase every
  sample). The box is on the *root*, so it watches what the screen shows - an application's own drawing.
  **It cannot see the pointer sprite**, which is drawn over the frame and never into the framebuffer
  (backlog A21 is what that cost: every X-level measurement reported a still screen while the sprite was
  flickering; only `screencapture -l <window number>` saw it, with the number from
  `tools/host-windows.c`).
- `Drive` - XTEST driver: `M x y`, `P n`, `R n`, `K kc`, `KD/KU kc`, `W ms`.
- `Keys` - types a string through XTEST (`Keys <host> <port> "hello"`, `--delay ms` for a slower
  typist): resolves each character against the keymap read with `GetKeyboardMapping`, presses it
  shifted or unshifted as that mapping says, and reports any character no key carries rather than
  guessing (a Chinese character is one, unless the server has claimed a keycode for it - C10).
- `Shot` - `GetImage` of the root or a window straight to a PNG, with no `xwd` and no X tooling on
  the machine that runs it, so it works in the per-client mode where `xwd` on the VM sees nothing.
- `Wire` - not a tool: the bare-protocol base the probes share, which hands back **every** message in
  the order the server sent it. `RawXClient` cannot be used for a probe, because it finds a reply by
  reading until one turns up and so eats the events a probe is counting.
- `FocusProbe` - its steps include `kill A|B|C` and `unmap A|B|C`, which are how the focus *revert*
  rules are exercised: the window holding the focus goes away, and where the focus lands depends on
  what the last `SetInputFocus` asked for. That pair was added for A5's leftover corner and found a
  real ordering bug in the destroy path on the first sequence tried. It drives a three-window tree
  through a sequence of `SetInputFocus`, pointer warps and
  grabs, printing every focus and crossing event with its detail and mode. This is the `Probe` the
  design notes' focus measurements were made with, and the harness for backlog items A5 and A6.
- `KeyTargetProbe` - **where a keystroke lands**, which is the half of the focus no diff of the focus
  *events* can see: two servers can send the same `FocusIn`/`FocusOut` around a revert and route the
  keys that follow to different windows. It maps a window of its own with the pointer parked inside
  it, leaves the focus on the root - `SetInputFocus(root)`, or the revert a client gets when the page
  it focused is unmapped with `revert_to = Parent` - and presses a key through XTEST, printing the
  window each `KeyPress` is reported against. **Measured against Xvfb and here on 2026-09-29**: a
  reference reports it against the window the pointer is in for `PointerRoot`, for the root window and
  for that revert alike, and nowhere for `None`; this server sent it to the root window in the two
  states that are not `PointerRoot`, which is how EasyShell's search box stopped accepting input after
  its 片段 page was closed. The modes are `unmap` (default), `kill`, `root`, `pointer`, `none` and
  `parent`, the last being the control where the focus is on the window the pointer is in.
- `FocusRevertProbe` - **what a client is told when a window that holds the focus *and the pointer*
  goes away**: two windows of one client, both children of the root and overlapping (so the point the
  pointer is parked on is inside both), the focus moved to the one in front with
  `revert_to = Parent`, and then that window unmapped or destroyed. Every message is printed with the
  crossings' `same_screen`/`focus` flags, which is the pair a toolkit with no window manager goes by.
  It exists because a report ("the context menu flashes and vanishes after a page is closed") led to a
  corner no probe covered: the pointer being *in* the window that disappears. A reference reports
  `FocusOut <gone> detail=Ancestor`, `FocusIn <underneath> detail=Inferior` and the crossing pair, and
  nothing else; this server added a `FocusIn <gone> detail=Pointer` to the window that was leaving,
  which GDK took for where the focus lives - so the window underneath was left looking unfocused.
  `FocusProbe`'s `"warp 50 50" "focus B" "unmap B"` is the same corner one level down, and the two
  together are what pinned the rule (`FocusRevertProbe <host> <port> [unmap|kill]`).
- `MenuProbe` - the same idea for the menu gesture: maps a popup under a held button, grabs the
  pointer, leaves and re-enters, and prints the crossings - marking any `EnterNotify` that arrives in
  a step where no motion was sent (`! enter without a move`), which is the event shape measured to
  tear a menu down (see the design notes).
- `PointProbe` - walks `QueryPointer` from the root down and prints the chain, which is how "is the
  answer the child of the queried window or the deepest window?" is read off (A4).
- `GravityProbe` - creates a parent and a child with chosen gravities, resizes the parent, and prints
  where the child ended up: the harness for A12, whose whole eleven-value table it produced.
- `RandrProbe` - the harness for B2: the RandR questions `xrandr` asks, with the replies printed as
  **bytes** as well as fields. Its `GetOutputProperty` request used to be **a word short** (six units
  where the request is seven, the last carrying `delete`/`pending`/padding), so both servers refused
  it and the line it printed measured nothing - the reference's answer for a property that is not
  there (an empty reply, not the `BadName` Xorg's source suggests) had never been seen until it was
  fixed on 2026-09-26. RandR's replies are read positionally, so a field the server put in the
  wrong place produces a plausible wrong number or a client that dies in its own library - the reply
  count that was left at zero here is what `xrandr --listmonitors` crashed on.
- `XineramaProbe` - asks every question Xinerama has and prints the answers field by field, so two
  servers are compared by their numbers rather than by "it replied". Its first version sent the three
  1.1 requests as four bytes each and read the reference's `BadLength` as "not implemented" - they
  take a window argument, and `GetScreenSize` refuses even that.
- `backing-store-probe.py` - the harness for A9's backing store, and **Python** rather than one of the
  Java tools for the reason `gen-cursor-font.py` is: it needs `XChangeWindowAttributes` with a value
  list and `XGetImage` on the root, and a second opinion from *Xlib* is worth more than another
  hand-built request. `python3 tools/backing-store-probe.py <display> [none|backing|save]` maps a blue
  window over a red one, takes it away, and prints the pixel and the number of `Expose` events the red
  one was sent. The three modes are the three answers a server can give, and the difference between
  them is the whole feature: `none` and `save` send one expose on both servers (the reference
  advertises `save-unders NO` and does not implement it), and `backing` sends one here and **zero** on
  a reference server, which does keep the pixels. Run it against both and diff the table.
- `fontset-probe.py` - the harness for C10's font-set question, and **Python** for the same reason
  `backing-store-probe.py` is: the question is what *Xlib* makes of a server's font replies, so the
  measurement has to be the library call itself - `XCreateFontSet` - and a `ctypes` call to it is
  worth more than another hand-built request. It prints what `XListFonts` matches and whether a font
  set was built, with the charsets no font was found for. Run it under the locale that matters
  (`LANG=zh_CN.UTF-8`), because the charsets asked for are the locale's own. See its header for the
  two servers' measured output, for the three font-path bugs it led to, and for what to measure
  next if a font set ever fails again.
- `font-reply-proxy.py` - the tool C10's font-set question needs next: a byte-level proxy that
  **captures a server's `ListFonts` and `ListFontsWithInfo` replies**, one framed message per line
  (`<opcode> <pattern> <hex>`), so two servers' answers to the same requests can be diffed byte for
  byte - including the fields nothing displays (`replies_hint`, the reply lengths, how many replies
  came back). It is what the item asks for after the names were the only difference anything
  *showed*: `python3 tools/font-reply-proxy.py 7061 <host> <port> /tmp/ours.txt`, point
  `fontset-probe.py` or `xterm` at `127.0.0.1:7061`, and do the same through a second instance in
  front of Xvfb. The replay half (feeding one server's answer to the other's client, which is how the
  remaining difference gets bisected) is not written yet; capturing is what says whether it is needed.
  Two framing traps are handled in it because they cost an hour each: the setup request and reply are
  the only messages with an eight byte header, and a reply's length field counts the bytes *after* its
  header rather than the whole message.
- `probe-diff.py` - diff two probe runs, ignoring what each server assigns for itself. `python3
  tools/probe-diff.py xvfb-run.txt ours-run.txt` prints `IDENTICAL` or a unified diff, and the point
  is that a difference here is a behaviour difference and not a formatting one. It normalises the
  extension opcodes and event bases, atom ids, cursor serials and event times - and, since A13,
  **window ids**, which it maps to the order they first appear (`win=W0`, `win=W1`): a server hands a
  client its id space, so the same probe run names the same windows `0xc00001` on Xvfb and `0x200001`
  here, and which window an event is *reported against* is the decision while its number is not.
- `XkbProbe` - the harness for B1, and the tool that found A4's cause: the two `XkbUseExtension` calls
  and the `XkbGetMap(which=0x7)` that `xdotool` makes, plus `GetState`, `GetControls`,
  `PerClientFlags` and `GetDeviceInfo`. The GetMap reply is printed field by field **and can be
  dumped** (`XkbProbe <host> <port> 7 dump <file>`), which is how its wire layout was read: the fixed
  part is forty bytes rather than thirty-two, and each keycode's keysyms follow its own descriptor
  inline. Run it against Xvfb for the reference - the two replies differ in the maps and nowhere
  else. **The controls half** (B1's `GetControls`/`SetControls`) is decoded field by field too - the
  92 byte reply with the repeat delay and interval at 20 and 22, the enabled-controls word at 56 and
  the per-key bitmap at 60 - and the probe can *send* a `SetControls` of its own:
  `XkbProbe <host> <port> setrate <delay> <interval>` (the `XkbSetAutoRepeatRate` case),
  `setkeys zero|all` (the per-key bitmap, which the reference ignores) and `setrepeat off|on` (the
  `RepeatKeys` control). It prints the reply before and after and a core `GetKeyboardControl`
  beside them, which is how each of those three was measured against Xvfb rather than assumed.
  **The two requests whose replies are the work** (B1's last half) have modes of their own:
  `getkbdname <symbols>` sends the by-name load a real `setxkbmap` sends (six counted strings, the
  symbols one fifth - a request with eight is refused by a reference with `BadLength`, which is how
  the count was measured) and `getnames [which]` sends a `GetNames`. Both print the reply as bytes
  **with every tail word resolved through `GetAtomName`**, which is what makes a reply whose fields
  are atom ids readable off the reference instead of guessed: `GetNames`' names live in the tail, not
  in the fixed part the protocol's field list suggests.
- `XiProbe` - the harness for B4 (XInput2), and the tool that says **why that item cannot be landed in
  pieces**. `XiProbe <host> <port>` prints the version handshake, `XIGetClientPointer` and the whole
  device list **parsed and as bytes**; `device <id>` narrows the list to one device; `select <mask>
  [deviceid] [root]` selects XI2 events on a window, warps the pointer in, drives a button through
  XTEST and prints every message - with the core masks selected as well, so a run that produces no
  XI2 event and a run whose gesture never happened can be told apart (a quiet run means different
  things and only having both rails in one output says which). **A device property read has a mode
  too**: `getprop <name> [device] [units]` sends an `XIGetProperty` for a property and prints the
  reply - which is how the answer to "this device has no such property" was read off Xvfb (a plain
  reply, `type = None`, nothing after the header) and how the request's length was settled (six four
  byte units; a reference refuses seven with `BadLength`). **`listprops [device]`** is its twin for
  `XIListProperties` - the count is a CARD16 at 8 and the atoms follow the header, which is how the
  empty answer this server gives was built. The pseudo-devices (`0`, `1`) are **refused** by a
  reference for both property requests, which is also measured here and is why this server refuses
  them.
  **Three modes came with A47** (the version gap): `versions` asks `XIQueryVersion` for every
  version a client might ask for, **one connection per ask** - a reference latches the first
  negotiation and answers it for every later call on that connection, which is a measurement of the
  latch rather than of the negotiation. `raw` selects the raw family (XI 2.1, bits 13 to 17) on the
  **root** and drives XTEST: a motion comes back as 72 bytes with the master as the device and the
  XTEST slave as the source, a button and a key as 40 with an empty mask, and a warp as *nothing* -
  the server moving the pointer is not the device reporting it. `barrier [id]` sends
  `XIBarrierReleasePointer` (XI 2.3) for a barrier that cannot exist and prints the extension's own
  barrier error, which is the answer a reference gives too.
  **Run it against Xvfb before writing anything**: a mask bit that looks wrong is wrong
  (`1<<3` selects nothing where `1<<4` is `ButtonPress`, and a wrong mask is *accepted* and changes
  nothing, which reads as a server that cannot deliver a device event at all), an XI2 event is a
  `GenericEvent` whose header is twelve bytes with no 32 bit full sequence - read it as though it had
  one and the timestamp reads as 1 - and it must be read **whole** (a length field says how much
  follows; the first version read thirty two bytes of a 120 byte event and every message after it was
  garbage, which showed up as the next reply decoding as `bad value 0x640000`, i.e. as the event's own
  `root_x`). Its own XTEST driver cost two rounds the same way: `FakeInput` is **thirty six bytes**
  with a length of 9 - not the sixteen its fixed fields suggest - and its `type` field is a **core**
  event type number (4 press, 5 release, 6 motion), not a numbering of XTEST's own.
- `CompositeProbe` - the harness for the last of B5's three: it asks the version, asks for the overlay
  window, makes a region from a window's border clip, redirects the window, names the pixmap that
  holds its contents, reads a pixel out of it, repaints the window **while it is redirected** and
  reads both the pixmap and the screen, covers it, draws into the named pixmap, and puts everything
  back. That repaint is the measurement the extension turns on and the one this server gets wrong by
  default: here the screen follows the window, on a reference it keeps what was there, so the guard
  in `XServer.compositeOwnPixels` is what the probe pins. Two smaller readings came out of it: the
  version answered is the **server's** (a client asking for 0.4 and one asking for 0.2 are both told
  0.4), and a client **may draw into** a named window pixmap - the fill lands - where the
  specification leaves it undefined, which is why the view is read-write. It also prints the
  **overlay's depth**, which is the field that decided whether `picom` runs at all: a depth-32 overlay
  makes a compositor's RENDER picture on it `BadMatch`, and the crash that follows is a long way from
  the cause.
- `PresentProbe` - the harness for Present, the last of B5's three: the version (asked twice), the
  capabilities, then a window painted one colour and a pixmap another, a `SelectInput` for the
  completion, a `PresentPixmap`, and both the event and the pixels read back. **It is the one probe
  that serialises with the generated protocol classes rather than hand-built bytes** - a dozen fields,
  several of them 64 bit - and SYNC's probe is why that is defensible: the generated *getters* listed
  a fence's fields in the opposite order to the wire, but the generated *writer* is right, and a wrong
  layout is still caught because the comparison is against a reference that refuses what it cannot
  read. Two of its own readings were wrong first: `PresentPixmap`'s `valid` and `update` are regions
  (None is the whole of each) and not flags, and **the order the event and the pixels are read in
  changes the answer** - the reference's present is asynchronous, so asking for the pixels before
  reading the completion samples a race and reports that nothing was copied.
- `GenLayouts` - reads the keyboard layouts off a reference server and prints them as Java source for
  `input/Layouts.java`; `tools/gen-layouts.sh` switches the reference with `setxkbmap` once per name
  and assembles the file. The same trick as `GenBitmapFont` (D3): a display server cannot compile a
  layout, so it serves tables captured off one that can. What it reads is the **core** mapping, and
  the width is the reference's own - a client indexes the reply by the modifier state its own XKB
  layer computes, so a narrower table would put the next key's symbols in the wrong place. Two traps
  are in the script: a method is capped at 64KB of bytecode, so each layout is a method of its own,
  and a trailing comma is legal in an array initialiser and not in a method's argument list.
- `DragOutProbe` - a forwarded application that is dragging files **out** of itself, which is C5's
  other half: it takes the `XdndSelection` (the only signal X gives that a drag has started) and
  answers the server's request for `text/uri-list`. It exists because whether a real application
  offers a drag is up to the application, so the host's side cannot be driven from the two on this
  bed - and `DragOutProbe <host> <port> path...` plus `/tmp/drag x1 y1 x2 y2` (post a hand's gesture
  inside the host window) exercises the whole path: the server sees the drag, asks, and starts a
  platform drag carrying the files - **except that the trigger for that was reverted the same day**,
  because a client dragging inside itself takes the same selection and the platform took over drags
  the application meant to keep (docs/backlog.md C5). What remains is the X half, and the probe is
  what would confirm a corrected trigger. **Its own first version never saw the request**, because it
  synchronised with `geometry()` after taking the selection and a reply read skips events - the
  server asks while *processing* the `SetSelectionOwner`, so the request is already on the wire. A
  probe never round-trips after the request it is waiting for.
- `SyncProbe` - the harness for SYNC, the second of B5's three: the version handshake (asked twice,
  because whether the answer is the server's version or the client's is a rule the reply settles),
  the system counters, a counter created, set, changed and read back, `GetPriority`/`SetPriority`, a
  fence walked through created-untriggered, triggered, reset and destroyed, and - since F1 - the
  alarm half: a trigger on a counter armed at an absolute threshold, the counter moved across it and
  the `AlarmNotify` read, then moved again inside the threshold to check that nothing arrives. Three
  more alarms answer the questions one crossing cannot: a negative transition, a transition whose
  delta re-arms its own threshold, and a comparison, which fires once and then reports itself
  inactive. It is also where the **refusals** are measured, because they are behaviour: a
  transition whose delta would move its threshold the way its test cannot use is answered
  `BadMatch` on its own id, and the raw error is read off the stream with `readRefusal` - an unread
  error is read as the *next* reply and shifts every value after it, which is how a first version of
  this probe reported a counter value of -4294967420. **Its first run was wrong in another way only
  a reference catches**: `CreateFence` carries the *drawable* first and the fence second, which is
  the opposite of the order the generated class's getters list them in, and the reference answers
  `BadDrawable` on the fence's own id when a probe believes the getters.
- `XgeProbe` - the Generic Event Extension, which is a version handshake and nothing else (F4): the
  extension asked for by name, then `QueryVersion` sent twice - a client asking for 1.0 and one
  asking for 0.0 - because whether the answer is the server's version or the client's is per
  extension and measured each time. There is no generated protocol module for XGE, so the request is
  four fields written by hand; the run is **identical** on both servers.
- `ResProbe` - X-Resource as a client uses it (F3): the version asked three times, the client list,
  the resource counts for a client that has just made one of each of a window, a pixmap, a GC, a
  font and a cursor (the type atoms are **resolved to their names**, because the numbers are each
  server's own), the pixmap bytes, the client-id masks, a named resource's size - and an id no client
  owns, which both servers refuse `BadValue`. It is a deliberately **different** run on the two
  servers; the differences and their reasons are in "What the sweep is expected to differ on".
- `RecordProbe` - RECORD (F2), and the tool the whole extension was written from: a client creates a
  context over a range of traffic, enables it, and every element of the recording arrives as a
  reply-shaped datum on that connection, which the probe decodes field by field - the element header's
  three stamps, the category, the client a message is attributed to, and (for a `ClientStarted`) the
  size, status and version of the setup reply it carries, or (for a `ClientDied`) the four bytes of
  its last sequence number. Flags choose what to vary: `--element-headers` for a single stamp,
  `--free-enabled` for a stream that is freed while enabled, `--narrow` for a range covering one
  request (which is how the `ClientDied` payload was told apart from a count of recorded items), and
  `--input` to put XTEST behind the default gesture. The default gesture is protocol-only - create,
  configure, map, a `GetProperty`, an unmapped client that arrives and leaves - because the probe must
  produce the *same* traffic on both servers for the diff to mean anything. The run is **identical**
  on both servers once the normaliser has, as usual, mapped the ids and the clock - plus, for this
  probe, the size of the setup reply, which is a different reply on each server for the obvious
  reason and is not what the extension is being measured for.
- `DbeProbe` - DOUBLE-BUFFER (F5), which is the extension every toolkit's no-flicker path asks for.
  It lives a buffer's whole life: the version handshake, what `GetVisualInfo` answers (and what it
  answers for a request that names no drawable), allocating a buffer on a mapped window, drawing into
  it - it is a drawable, so the ordinary core requests address it - the swap under **each** of the four
  actions, the attributes query, the refusals an id that is not your buffer deserves, two buffers on
  one window swapped in one request, a window resized under its buffers, the two idiom requests, and a
  **second client** that may swap this one's window but cannot see or free its buffer. Front and back
  are told apart by colour: both are filled with a colour of their own before every swap, so one line
  says which surface the window shows afterwards and what the buffer was left holding - which is what
  separates `Copied` (the buffer keeps what it had) from `Untouched` (the buffer gets the front's
  previous contents). It is **identical to the reference except the count of visuals**, which is the
  screen's own (7 here, 144 there) - see "What the sweep is expected to differ on".
- `SecurityProbe` - SECURITY (F7), whose subject is a *connection* rather than a resource. It generates
  an authorization, connects with it, and then sends the same battery of requests **from that
  connection and from a trusted one**, printing both answers per request: an error that appears on both
  is a request the probe got wrong, and only a difference is the trust level. That is what pins the six
  `BadAccess` refusals, the two extensions an untrusted client may still see, and the fact that a
  revoke closes the connection using it - none of which is in the specification to be read off. It
  needs the reference to **check credentials** for the trust half to mean anything: `tools/xvfb.sh
  secure` starts the second Xvfb for exactly that, and the probe's own (trusted) connection is
  authenticated by passing the cookie: `SecurityProbe <host> <port> MIT-MAGIC-COOKIE-1 <hex>`. Both
  pairs - the ordinary `-ac` one and the secured one - come out **identical** against this server, the
  second with `--auth` on. **It never sends `SetAccessControl`**: that request changes the server for
  every client, and on an `-ac` Xvfb it turns access control *on*, after which that reference refuses
  the next run's connections (measured the hard way).
- `XvProbe` - XVideo (F6), which is the smallest of the seven because the reference is small too: it
  walks the whole surface a player walks - the extension's own version, `QueryAdaptors`, and then every
  request that names a port (encodings, attributes, formats, image attributes, grab, put, stop,
  ungrab, `ShmPutImage`) - and prints what came back for each, because "there is no adaptor here" and
  "this request was refused" are different findings to a player. Xvfb advertises XVideo and answers
  **zero adaptors**, so that is what this server answers, and the requests that name a port are refused
  with the extension's own `BadPort`. The run is **identical** on both servers, including the *order*
  the errors are raised in: the drawable and the GC are resolved before the port, which is how a GC of
  `None` is `BadGContext` rather than `BadPort` - the one thing this probe caught in the first
  implementation of the extension.
- `DamageProbe` - the harness for DAMAGE: it creates a damage object on a window at a chosen level,
  draws, prints every `DamageNotify` (level, area, and the drawable's geometry), then subtracts and
  draws again - which is the part the extension turns on. `DamageProbe <host> <port> [level]`, with
  X's own numbering, which is **not** the order the names suggest: 0 `RawRectangles`, 2 `BoundingBox`,
  3 `NonEmpty`. Its first version read a `DamageNotify` as forty eight bytes (it is thirty two, and a
  rectangle in it is four shorts) and could not tell a stray filter request from a `NoExpose` - which
  is how the spurious-expose bug in B5 was found.
- `ChangeCursorProbe` - the harness for A8's `ChangeCursor`: two glyph cursors on two windows, the
  image of each read back through `GetCursorImage` before and after the change - which says **which**
  of the two arguments changes, that the window already showing it sees it, and (from the cursor
  notifications, with the pointer left over the destination) that a rewrite raises none.
- `BitGravityProbe` - the harness for A12's other half: a marker near each corner of a window, the
  window grown, and the window read back with `GetImage` as per-row ink spans - so the shift a
  `bitGravity` asks for is a number rather than a guess. Its own header says what the eleven values
  measure; the first version summarised the picture as ASCII art and the column arithmetic lost the
  shift, which is why it prints coordinates now.
- `GlyphCursorProbe` - the harness for A3's other half: a cursor built from an arbitrary font's glyphs,
  read back the way a client can (`XFixesGetCursorImage` answers with the cursor the pointer is
  showing, so the probe defines the cursor on a window, warps the pointer in, and prints the size, the
  hotspot and the image). `GlyphCursorProbe <host> <port> [font] [chars] [mask-chars]`; `GLYPH_FG` and
  `GLYPH_BG` set the two colours and `GLYPH_RAW=1` prints the pixels' values as well as their shapes.
  What it measured, and what it could not, is in the backlog (A8/A3).
- `PolyProbe` - the harness for A15, and the tool that turned the 0.8% `xlogo` difference into a rule:
  it fills a fixed table of polygons - a control at 45 degrees, slopes of 2, 44/21, 44/17 and 44/45,
  a quadrilateral with no axis-aligned edge, a sixteen-gon, and the star the suite uses - into a
  window, reads the pixels back with `GetImage` and prints them as text, so the two servers' scan
  conversion can be diffed line by line. `PolyProbe <host> <port> xlogo` instead draws the five
  polygons `xlogo` itself sends (read off this server's request log) in a window at the same place
  with the same border. **A probe that draws nothing reports agreement**, so both failures it had on
  the way - the `FillPoly` wire layout, which is unlike every other drawing request, and drawing
  before the map - are recorded in the backlog; read the ink counts before believing a zero diff.
- `ScreenSaverProbe` - the harness for B7: the core screen saver's settings, MIT-SCREEN-SAVER's
  version and `QueryInfo`, and DPMS's version/capability/timeouts/power level. Run it with `core` to
  round-trip the core settings too, and read them with the real `xset` - its own hand-built core Set
  is refused by the reference (`BadLength`) while `xset s` works on both.
- `AllowEventsProbe` - the harness for A13: grabs with `pointer_mode = Sync` (or a passive grab that a
  press activates, or a `GrabKey` with `keyboard_mode = Sync`), then watches what arrives and what each
  `AllowEvents` mode releases. `active`/`passive`/`key` for the arrangement, `sync`/`async`/`replay`/
  `all` for what it sends, and `injector`/`warp` to inject the input from a second connection or by
  `WarpPointer` instead of XTEST. Its five arrangements - the three plus the two `asyncgrab` controls -
  are **identical against Xvfb and against this server now**, and none of them was before A13 was
  done.
- `FontProbe` - the harness for D3: `QueryFont` field by field (the bounds, the ascent and descent,
  the properties **with the string valued ones resolved** - comparing two servers' atom *numbers*
  compares nothing - and every character's bearings, width, ascent and descent), `QueryTextExtents`
  of a fixed string, and the pixels that string draws, read back with `GetImage` and printed as text.
  Run it against both servers and diff: all six fonts the server has tables for are identical line
  for line, and none of them was before.
- `GenBitmapFont` - generates `resource/BitmapFont.java`, the font tables, **from a reference server**
  (`tools/kit.sh GenBitmapFont <host> <port> <out.java> [font-name ...]`). It is a generator rather
  than a probe and needs a display with the fonts, which is why its output is checked in. See the
  design notes for why the reference rather than the font file, and for the four traps the capture
  cost - the worst of which is that a font drawn into a one-bit pixmap comes back as its *default
  character* for every code.
- `GrabProbe` - arms a passive `GrabButton`/`GrabKey` (on the root, or a given window) and then uses
  the pointer or the keyboard, printing every event **both** connections receive. Two connections are
  the point: with the window under the pointer belonging to the *other* client, "the grab took the
  press" can be told from "the press went where it landed" - see A1, and the two arrangements that
  look right and measure nothing.
- `WinTree` - the window tree: geometry, class, map state, override-redirect, titles, transient-for.
- `Focus` - `SetInputFocus` to a window, i.e. the window manager's half of a focus test.
- `Pixel` - one pixel of a PNG as hex.
- `Xwd` - XWD to PNG, or a diff of two XWDs (counts, worst delta, histogram).
- `Blocks` - a grid of per-block difference counts, to find where to look.
- `Colors` - every distinct colour in a rectangle, most common first.
- `Shift` - difference cost per ±4 pixel offset: a shift or a real difference?
- `Rect` - an ASCII map of a rectangle, so a region can be judged without a viewer.
- `Crop` - a magnified crop around a point.
- `AuthProbe` - sends a connection setup with chosen credentials and says what came back: a setup (with
  the vendor and screen count) or the refusal's reason. No arguments means "offer nothing", which is
  what backlog item A7 is about.
- `SelectionProbe` - subscribes to a selection through XFixes and prints every notification it gets,
  while other connections take the selection, destroy its owner window and disconnect: the three
  subtypes, one line each. `SelectionProbe <host> <port> [name]`, the name defaulting to `CLIPBOARD`
  (a client watching something else is checked by naming it - EasyShell watches `_NET_WM_CM_S0`).
- `CursorProbe` - the same for `SelectCursorInput`: two windows carrying different glyph cursors, the
  pointer warped between them, every `CursorNotify` printed. What it is for is the thing about the
  event that is easy to get wrong: the serial names the *cursor*, so it goes back down when the
  pointer returns to one it has shown before.
- `ClipboardProbe` - one end of the clipboard bridge, for checking a live server against the
  host's own pasteboard: `read` prints what a client would paste, `write <text>` takes the selection
  and serves it, `owner` prints who holds it. `write` stays connected - an X selection is lazy, so the
  data has to still be there when the host asks for it.
- `WindowStateProbe` - asks a server for a window state the way a toolkit does: a `ClientMessage` to
  the root, and then a read of the window's own `_NET_WM_STATE` to see what came back. The actions are
  a comma separated list because they share one window - `fullscreen on,off` is the request and its
  withdrawal on one connection.
- `XProxy` - a byte-level X11 proxy that logs a client's stream against a *real* server, one line
  per message, with a **connection id** on every line (`c1 C>S req 98 QueryExtension`) - a client may
  open several connections and the two logs are written from all of them at once, so without the id a
  reader that assumes one stream reports a desync that is not there (that cost an afternoon on
  2026-09-27, chasing a "misframe" that was the other connection's setup reply).
  **Three framing bugs made it useless for exactly the clients it was written for, and they are all
  fixed now (2026-09-27):**
  * it assumed every request has an eight byte head - `BIG-REQUESTS`' `Enable` is **four**, and xcb
    sends it on every connection, so the proxy desynchronised one message in and the client hung
    with nothing logged on either side;
  * an extended length is triggered by a **zero length field**, not by opcode 139 (whose ordinary
    `Enable` form is three units);
  * and **a generic event (type 35) is not a plain event**: it carries its own length, and forwarding
    its first thirty two bytes leaves the rest in the stream. That is what "EasyShell hangs through
    `XProxy`, the reason was never found" was - GDK's whole input path is XI2, so the client
    desynchronised at its first device event and stalled before drawing a pixel. With the fix,
    EasyShell renders through the proxy, complete, and every stream checks out against its own length
    fields.
  **Its first real use (2026-09-27) is clean, and it is now a method**: run the *same* client through
  the proxy against a reference and directly against this server, drive **the same interaction** on
  both with the same driver (`xdotool` on the VM, not XTEST from here - the driver's own startup
  queries are a third of the traffic and using two drivers compares the drivers), and compare
  interaction-only profiles by request name and by **(extension, minor)**. EasyShell's profile comes
  out request for request: `QueryExtension` 70/70, `GetInputFocus` 21/21, `GetProperty` 19/19,
  `GetKeyboardMapping` 18/18, `GetModifierMapping` 14/14, WarpPointer/GrabServer/ChangeProperty and
  the whole RENDER/XKEYBOARD/XFixes/XTEST/XInput set within a couple of each other. The only
  differences are a *name* (`Enable` here against `BIG-REQUESTS Enable` there) and about 25% more
  *drawing* on this side (`PutImage` 408 against 314, the picture churn to match) - this server's
  compositing path against a server that draws straight to the screen, which is a performance note and
  not a protocol difference. An earlier run of the same comparison was misled twice and both are
  recorded here because they look like findings: comparing profiles from **different drivers**, and
  comparing a run's **startup** against another's **interaction** (mark the log, or the window is
  wrong). The three differences the startup comparison *did* show are the reference's own environment:
  `GetAtomName` for `X11VNC_TICKER` and the root properties x11vnc leaves, `CreateColormap` for
  visuals Xvfb has and this server deliberately does not, and a `GetMotionEvents` probe the client
  does not make here.
  A fifth argument adds a **raw dump**
  (`XProxy <listenPort> <host> <port> <logFile> <rawFile>`), one line per message as
  `<conn> <direction> <hex>`, for when the *bytes* are what is being hunted - that is how
  `setxkbmap`'s own `GetKbdByName` request was read. It still cannot proxy the VM-to-reference
  direction from this machine (compile it **on the VM** - `java` is there), and `tools/tap.py` is the
  byte-transparent fallback, at the price of logging chunks a reader has to frame themselves.
- `clipboard-owner.py` - the smallest client that drives the clipboard bridge's **X to host** half: a
  GTK3 window that takes `CLIPBOARD` (`Gtk.Clipboard.set_text`), at which point the bridge asks *it* for
  the data. Twenty lines, and it is the reproduction A19 needed: the window the bridge asks through is
  the one the toolkit wraps, so a bridge that names a window of another screen kills this client
  outright (`EXIT=139` in the default mode, `EXIT=124` - killed by the timeout, alive - against
  `--single-window`). `DISPLAY=<host>:<display> python3 tools/clipboard-owner.py`.
- `gtk-menu-probe.py` - an entry whose context menu holds exactly one item, which prints when it is
  activated, and which **prints its own screen geometry** (the menu's origin and the item's centre, from
  the toolkit's allocations) when the menu opens. That readout is the point: a menu row is a few pixels
  tall and a screenshot of a scaled host window is not precise enough to aim at one - A23 lost three
  measurements to that before the probe printed the coordinates. `DISPLAY=... python3
  tools/gtk-menu-probe.py`, then right-click the entry.
- `host-input.c` - input as a **hand** makes it, for a client whose screen is a host window on this Mac
  (the default mode, where XTEST from another client cannot reach it): `move`, `click <x> <y> [3]`,
  `type <text>`, `ctrl <keycode>` (the Control key itself held across the letter - a flag-only event is
  not what a server that tracks the modifier keys sees) and `key <keycode> [up]`. Coordinates are
  CGEvent points, which is what `host-windows.c` prints. `clang -o /tmp/host-input tools/host-input.c
  -framework ApplicationServices`. The same file also notes what aiming at a capture costs: see A23.
- `tap.py` - the same idea, but **byte-transparent and it does not parse anything**, which is what
  makes it usable where `XProxy` is not (a client that objects to the parsing hangs - EasyShell does,
  after its first `QueryExtension`). `python3 tools/tap.py 6099 6098 /tmp/tap.log` listens on the
  port display `:99` would use and forwards to the server on `:98`, logging both directions as
  `<time> c<connection> C2S|S2C <bytes> <hex>`. **It logs only while `/tmp/tap-on` exists** -
  `touch /tmp/tap-on`, do the gesture, `rm /tmp/tap-on` - because logging a repainting client writes
  megabytes a second. This is how A16's answer was read off Xvfb, whose own server logs nothing:
  decode the `C2S` stream as requests (`opcode, data(minor), length` and skip the body) and the
  reference's sequence is right there. It runs on the *VM*, in front of Xvfb.

The two XFixes probes are how A8's rules were taken from a reference server. Their output against Xvfb and
against this server is identical once the things a server assigns for itself are normalised away
(opcodes, atom ids, cursor serials, and the absolute clock, which is a different epoch on each
server) - compare the two runs by hand, or with a small normaliser, before believing any difference.

**`RawXClient`'s hand-written `ShapeRectangles` was right and the server was wrong** - the question
this file used to leave open, answered 2026-09-26. The request arrived and was refused by the
generated *decoder* (`initial capacity cannot be less than 0`), because it sizes a trailing list from
the request length in four byte units where it means a byte offset: one rectangle was refused and ten
accepted, which is why the bytes were blamed for so long. `ShapeExtension.decodeRequest` and
`XkbExtension`-style hand decoders fix it at each request, and `ShapeClippingTest` now sets a shape
over the wire; A2 in the backlog has the measurement. The kit's rule from it: **when a hand-written
request is refused, read the server's log line before changing a single byte** - it named the exact
exception, and had been there since the request was first tried.

## What the sweep is expected to differ on

`tools/sweep.sh` runs every protocol probe against Xvfb and against this server and diffs the two
(`tools/probe-diff.py`, which ignores the opcodes, resource ids, atom ids, cursor serials and clocks
each server assigns for itself). **Full run 2026-09-28, after F6: 19 of the 28 probes identical.** The
nine that differ are all deliberate, and each is measured rather than assumed - a run that reports one
of these is a run that agrees with the reference, and a run that reports a *tenth* one is a finding.

**Full run 2026-10-03 (the bed at `.152`, after A18/A34): 17 of 28 identical**, and the eleven are the
nine below plus `SelectionProbe` and `MenuProbe`, both of which are measured and explained at the end
of this list - a resource-id base the probe's own timing decides, and the focus `--focus-on-click`
puts where the reference has none. So the list to read a future run against is **eleven**, and a
*twelfth* difference is the finding.
`RecordProbe`, `SecurityProbe` and `XvProbe` are the three of this round that came out identical, and
`DbeProbe` is the one that differs by a line: RECORD as this server streams it is byte-for-byte what
the reference streams for the same gesture (F2), SECURITY agrees request for request on both the
ordinary and the access-controlled pair (F7), and XVideo answers a player's whole query surface the way
a reference with no video hardware does (F6).

- **`SyncProbe`** - one line. `ListSystemCounters` answers **no** system counters here where the
  reference keeps eight (elapsed-time counters for its own scheduling): this server has no scheduler
  to report on, and the reference's entries are variable length with a name stride a probe cannot
  guess - the first version read one counter's name out of the next one's fields. Answering a count
  with entries a client cannot parse would be worse than answering none. Everything else in the
  extension agrees line for line: the version (`Initialize` answers **the server's** 3.1 to a client
  asking for 3.1 and to one asking for 3.0), a counter's create/set/change/query over the full 64
  bits, `GetPriority`/`SetPriority`, a fence walked through untriggered, triggered, reset and
  destroyed, **and since F1 the whole alarm section** - the four trigger arrangements, the delta that
  re-arms a threshold, the comparison that goes inactive after firing, and both `BadMatch` refusals,
  each identical. (The counters and alarms are printed in decimal and the normaliser maps ids, as it
  does an atom's; the *values* are what the tests are about and are not normalised.)
- **`ResProbe`** - structurally different, and every part of it is one of four measured facts rather
  than a formatting accident. (1) **The client list has a different length**: a reference lists seven
  clients because `x11vnc`, driven by `tools/xvfb.sh`, holds six connections to it, and this server
  lists its own resources plus the probe. (2) **`QueryClientIds` has no local-pid rows here.**
  `LOCAL_CLIENT_P_I_D` is answered from the peer credentials of a *unix socket*, and this server's
  transport is TCP, so no client's process id is knowable - a reference omits the same rows for a
  client that reached it over TCP, which is the row this server is in for every client. The XID bit
  is answered with an empty value list by both, which is the reference's own answer. (3) **A window
  holds bytes here.** A reference reports zero for a window (no backing store) and this server keeps
  every window's pixels (B5), so `QueryResourceBytes` says 1024 bytes for a 40x30 window - reporting
  none would be the wrong answer to the question the extension exists to ask. (4) **A named type
  matches.** `QueryResourceBytes` compares the `type` a client names against the resource's own type
  atom, which is what the specification says; **a reference answers nothing for any non-zero type,
  even the resource's own**, so a probe asking with the WINDOW atom gets one row here and none from
  Xvfb. The last two are this repository's decisions and are recorded as such in the F3 entry.

- **`DbeProbe`** - three lines, and they are one fact: `GetVisualInfo` reports **this server's
  screen**, which has 7 visuals against the reference's 144, so the count and the reply's length
  differ. Everything else in the run is identical, including all four swap actions colour for colour,
  every refusal, a window resized under its buffers, and what a second client may do with the first
  one's window and buffer. The count is the screen's own answer rather than the extension's - the same
  kind of difference `ResProbe`'s client list is - and the reply's *shape* (a section per drawable, a
  count per section, eight byte records) is what the comparison is for.
- **`ChangeCursorProbe`** - this server refuses `XFixesChangeCursorByName` with `BadImplementation`,
  where a reference **accepts every name and does nothing with it** (its cursor font has no glyph
  names, so the lookup finds nothing and the cursor is left alone): both are "the name changes
  nothing", one says so. Measured and recorded as A8's answer; the probe prints the refusal per name
  rather than dying on it, which is what it used to do.
- **`XiProbe`** - this server answers **XInput 2.4** since A47, the version a reference answers, and
  lists **four devices** since A48 - its two masters and the two XTEST slaves a reference has, with a
  reference's names, types, attachments and class source ids, and with XTEST input carrying the slave
  as its `sourceid`. Where a reference still lists two more (its backend's "Xvfb mouse"/"Xvfb
  keyboard", 6/7) this server has none: its host input is the master's own. What is behind the version: raw events (XI 2.1) are delivered on the root with
  the reference's own shapes, `XIBarrierReleasePointer` (XI 2.3) is answered with the extension's
  barrier error exactly as a reference answers it for a barrier that does not exist, and no touch or
  gesture device exists on either server for 2.2's and 2.4's surfaces to have any content. What is
  still not here: pointer barriers themselves (they are created through **XFIXES 5.0**, which this
  server does not have), a changeable device hierarchy (`XIChangeHierarchy`), and XI's passive
  grabs - each measured and written down rather than half-answered.
- **`RandrProbe`** - three fields. `GetScreenSizeRange` answers min = the screen's current size
  (it never shrinks; `--max-screen` bounds the growth), where Xvfb's minimum is 1x1; the output
  carries no per-output properties (a `GetOutputProperty` for one answers an empty reply); and the
  output's physical size in millimetres is computed from the pixel size where Xvfb reports 0x0.
- **`XineramaProbe`** - Xinerama 1.1's `GetState`/`GetScreenCount`/`GetScreenSize` are **refused**
  (B3: the three questions a client actually asks are answered - `QueryVersion`, `IsActive`,
  `QueryScreens` - and the 1.1 extras were measured and left). Xvfb also reports several heads; this
  server has one screen per client connection.
- **`GlxProbe`** - GLX is **present on both and deliberately a subset here** (B6, done 2026-09-27).
  What differs is the advertisement and the range: this server answers vendor `xserver` where the
  reference answers `SGI`, an **empty** extension list where the reference lists 24 (which is the
  honest part - no ES2, no shaders, and a client that wants them falls back; counted off the sweep of
  2026-09-27, the day GLX became the shipped default), and four visual configs
  to the reference's 144 and 304 fbconfigs, because it has four true-colour visuals. The request
  surface, the layouts and the *contexts* agree: `CreateContext(direct)` created and `IsDirect` 1 on
  both, `CreateContext(indirect)` refused here and created there - the one place the two disagree on
  purpose, because indirect means GL commands over the wire and there is no renderer here.
- **`ScreenSaverProbe`** - this server **implements DPMS** (B7) and the Xvfb built for this testbed
  does not, so our run has a `DPMS version 1.2 / capable = no / timeouts` section that its run has no
  line for. The one field that differs inside `QueryInfo` is `window`, which is `None` here and a
  leftover id there.

- **`SelectionProbe`** - one role name, sometimes. The probe opens a **fresh connection for each
  section** ("the same window takes the selection twice" is a section of its own), and which
  resource-id base that connection gets is the *server's* choice: a reference reuses the slot of the
  connection that just closed, this server may hand out the next one because it has not reaped the
  old one yet. `probe-diff.py` maps ids by the order they first appear, so a base that is new where
  the reference reused one lands in a new role (`owner=R3` against `owner=R1`) with the *sequence*
  of events otherwise identical. Measured 2026-10-03; it is a race in the probe's own timing, not a
  difference in what the selection does - and the ids are the server's to assign.
- **`MenuProbe`** - the focus. This server reports a `FocusIn` for the press and `focus=yes` on the
  crossings that follow, because **`--focus-on-click` is on by default** (A19/A23's fix) and this
  server therefore *has* a focus window where Xvfb, with no window manager and nothing setting a
  focus, has none. That is the same feature the menu tests in A23 exist to protect; the fix for a
  sweep that wants the old shape is `--no-focus-on-click`, not a change to the crossing code.

**Two things make a run lie, both measured on 2026-09-27.** The reference must be **clean** (`tools/xvfb.sh
stop` and start again): a client killed while its windows were mapped can leave them behind on Xvfb,
and `MenuProbe` then differs in the detail of its *first* crossing - `Nonlinear` against `Ancestor` -
purely because the parked pointer is inside a leftover window on one server and on the root on the
other. And this server must be started **`--no-clipboard --no-tray`**: those bridges are host-side
features a reference has nothing like, and the clipboard bridge taking the `CLIPBOARD` selection when
a client gives it up reads as an extra `SelectionNotify` from an owner the probe never made.

## What is missing

The originals lived in `/tmp` and were deleted (by hand, to clear screenshots) on 2026-09-24. What was
restored above is everything whose source survived in a session log, and the rest - `Keys`, `Shot`,
and the three probes - was re-created from the descriptions on 2026-09-25 (backlog item E0). Still
missing, and only worth re-creating if something asks for it:

- `SetupDump`, `SetupHex`, `QT` - handshake dumps, for when the setup reply itself is in question.
- assorted image helpers (`Diff`, `DiffStats`, `Compare`, `Find`, `Magenta`, `Count`, `Ascii`, …) -
  `Xwd diff`, `Blocks`, `Colors` and `Rect` cover what they did.

`XProxy` is a **reconstruction** (its header says so): the framing and the message descriptions are
the original's, the opcode/event name table is shorter, and it does not log reply *bodies*. That used
to be described as "step one of backlog item A4", which is no longer where it would help: A4 (the
`xdotool` segfault) turned out to be B1 and is fixed, and what actually reads a stream now is
`tools/tap.py`, which is byte-transparent and so cannot get in a client's way.

## Rules that go with the tools

These cost hours each, the first time:

1. **Park the pointer in the same place and the application in the same state before capturing two
   sides.** One capture with the pointer over a toolbar and one without lights up hover states and
   reads as a large, entirely imaginary rendering difference.
2. **Run `Shift` before believing a rendering difference.** A whole-window scan over ±4 pixels says
   whether the two renders disagree in place or are the same picture moved, and those are very
   different bugs.
3. **Capture XWD with the same tool on both sides** (`xwd` on the VM works against Xvfb and against a
   `--single-window` server here), and read it with `Xwd` - Python's PIL refuses the XWD that the VM's
   `xwd` writes.
4. **Two differences are by design** and will always show up: this server's root colour is `#E8E8E8`
   where Xvfb's is black, and it draws a window border where a WM-less real server would not.
5. **The pointer outlives every client, so a probe has to park it before it maps anything.** The
   pointer is not part of a connection: a run that starts wherever the last one left it gets a
   crossing at the map on one run and not on the next, and a diff between two servers then shows the
   *previous* run's difference. `FocusProbe` and `MenuProbe` warp to a point no window of theirs
   reaches before they map, for exactly this reason - and it was found the hard way. **The same rule
   applies to a real application driven through XTEST, and there the *server* is part of the state
   too**: measuring a menu opening and surviving a leave-and-re-enter needs a fresh server, a fresh
   client and the pointer parked *first* - anything less measures the previous run, and it produced
   a wrong "this change broke the menus" for an afternoon (B1, 2026-09-27: the same build is 3/3 when
   the three are controlled and 0/2 when they are not).
6. **The *button* state outlives every client too, and it is worse than the pointer.** A probe run
   that ends with a button down makes the next run's press a duplicate the server drops - and the
   measurement then reads as "the press was never delivered", not as "the state was dirty". It cost
   an hour while A13 was being measured, and produced a whole wrong model of the held queue before it
   was spotted. A probe that presses anything has to release it before it exits, and one that is
   *chasing* a press/motion question has to release the buttons before its first step.
7. **Xvfb needs three flags to be usable as the reference**: `-listen tcp` (a stock build opens no
   TCP at all, so nothing here can reach `:98`), `-ac` (it refuses the kit's hand-written clients,
   which offer no credentials - the failure reads as "the connection setup was refused", i.e. as if
   the display were not running), and *not* `-nolisten unix` (the VM's own clients reach it through
   the unix socket). `tools/xvfb.sh start` does all three.

**`WinTree`'s output carries NUL bytes, so grep reads it as binary.** `WM_CLASS` is two strings
separated by a NUL (`class=EasyShell\0EasyShell`), and a pipe or a file with a NUL in it makes most
greps say nothing at all - "the window is not there" was three times "grep ate the line" on
2026-09-26, with the menu window plainly in the tree. Write `grep -a`, or read the file.

**Every hand-built request has to be padded to its protocol size, or a reference server refuses it.**
`RandrProbe`'s `GetOutputProperty` was a word short (six units where the request is seven), and the
new `GlyphCursorProbe` was wrong twice in the same way before it measured anything - `XFixesGetCursorImage`
takes *no* arguments (one unit, not two) and a `CreateGlyphCursor` is eight units, not nine. The trap
is that **this server accepts them anyway**: it reads the length the request declares, so a request
that is short of a field still decodes, while Xvfb answers `BadLength` or `BadRequest`. So "the probe
works against this server" says nothing about whether the probe is right, and a probe that disagrees
with the reference is checked against the protocol XML first.

**A trap in the numbering, worth ten minutes:** the core grab opcodes are contiguous and easy to
shift by one - `GrabPointer` 26, `UngrabPointer` 27, `GrabButton` 28, `UngrabButton` 29,
`ChangeActivePointerGrab` 30, `GrabKeyboard` 31, `UngrabKeyboard` 32, `GrabKey` 33, `UngrabKey` 34.
Sending `GrabKey` where `GrabKeyboard` was meant is silent (a passive grab is recorded, nothing
happens), and sending `UngrabKey`'s opcode with `UngrabKeyboard`'s shorter body is answered with
`BadRequest` - which the probe prints as `ERROR 1 opcode=34`. And a probe that sends `GrabPointer` or
`GrabKeyboard` **must read the reply**, or every event afterwards is attributed to the step before.

**Display numbers, and the trap that costs a confusing test failure.** The suite binds 10 and
63-99: `MotionHistoryTest` takes 90, `GrabCrossingTest` takes **95**, `FocusEventTest` 98,
`KeyboardGrabFocusTest` 83, `PassiveGrabTest` 70, `AllowEventsTest` 71, `XkbControlsTest` **91**,
`RandRTest` 75, `WindowStateTest` 74, and others in between. Two consequences:

* a server started by hand for probes should take a display **no test binds**: `:61` (below the
  suite's range) is what the kit's own runs should use. `:91` was the advice here until 2026-09-26,
  when it turned out to be the display `XkbControlsTest` binds - so a hand-started server on it made
  `mvn test` fail with `BindException: Address already in use`, two minutes of blaming the suite for a
  leftover server. That is the same failure mode as the bullet below, one test further along;
* **`tools/bed.sh`'s default `single`/`perclient` display is 95, which is one the suite wants**, so a
  bed left running while `mvn test` runs makes `GrabCrossingTest` fail with `BindException: Address
  already in use` - a failure that looks like a broken test and is a leftover server. Stop the bed
  (`pkill -f "xserver.Main --display 95"`) before running the suite;
* **an `ssh -L` tunnel to the reference counts as a server on that display, and it fails *worse* than
  the bind error.** `ssh -N -L 6098:127.0.0.1:6098 root@…` (which is how a probe here reaches the
  VM's Xvfb) puts Xvfb where the suite's own test server expects to be, and the client under test
  then talks to *Xvfb*: `FocusEventTest` failed three runs out of three with a window id one off
  (2026-09-26) and read exactly like a focus bug in this server. `lsof -nP -iTCP:<port> -sTCP:LISTEN`
  says who owns it; kill the tunnel before `mvn test`, and start it again for the next comparison.
