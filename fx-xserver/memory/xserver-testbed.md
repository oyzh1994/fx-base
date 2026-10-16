---
name: xserver-testbed
description: "How the EasyShell-vs-xserver test bed is wired (Mac runs the server, two Linux clients run the apps) and the traps that look like server bugs"
metadata: 
  node_type: memory
  type: project
  originSessionId: 20e98c73-c9b5-4cc8-9d2e-466cca178651
  modified: 2026-09-27T07:47:06.699Z
---

**Two client machines as of 2026-09-26** (`tools/vm.sh` takes the host from `$XSERVER_VM_HOST` and the
password from `$XSSH_PASSWORD`, or from `~/.xserver-vm-pass` for the default host). **The addresses
changed on 2026-10-03, and every document in the repository was updated with them**: the bed VM was
`192.168.22.147` and is now **`192.168.22.152`**, and the user's own PC was `192.168.22.150` and is now
**`192.168.22.151`**. The password did *not* change (`user@147` - the string is historical, not the
address). The VM is the user's to start: **do not start it yourself** (asked 2026-10-03 after `vmrun
start` was tried on a machine the user was using).

| host | user / password | what it has |
|------|-----------------|-------------|
| `192.168.22.152` | `root` / `user@147` (in `~/.xserver-vm-pass`) | the original bed: Xvfb `:98`, x11vnc, xterm, ibus + `dbus-x11`, xwd/xwininfo, apt with network (2026-09-26), both AppImages |
| `192.168.22.151` | `root` / `123456` | the user's own PC (`oyzh-PC`, aarch64, `zh_CN.UTF-8`): **both AppImages** (`/root/EasyShell_v1.2.2_linux_arm64_build.AppImage`, `/root/termora-2.0.0-beta.16-linux-aarch64.AppImage`) and `dbus-launch`; **no** Xvfb, xterm, ibus, xwd or x11vnc |

`.151` is the machine to run a client on when `.152` is unavailable or being restarted (it was
restarted under me on 2026-09-26 and came back on a new address-less boot: the whole bed - Xvfb, the
apps - is gone until it is started again). Anything needing a *reference display* or an X input method
still has to be `.152`, which has Xvfb and (since 2026-09-26) xterm/ibus. Use `.151` for
`DISPLAY=192.168.22.1:<port>` client runs with our server, which needs nothing installed on the client
side.

**termora is the second application in this bed (2026-09-23), and it is a Swing/AWT app**, not Compose
Desktop: `/Applications/Termora.app` (macOS, Aqua - it cannot speak X11) and the Linux build
`/root/termora-2.0.0-beta.16-linux-aarch64.AppImage` on the VM, which is the one that is pointed at
this server. The user runs it against the VM's own `Xvfb :98` and watches it over x11vnc - that is the
"Xvfb is normal" control - and against the Mac's server over TCP for the bug. To drive it:

```
DISPLAY=192.168.22.1:<display> /root/termora-2.0.0-beta.16-linux-aarch64.AppImage
```

- Its keymap settings (toolbar gear at 1170,16 on a 1300x800 screen; help "?" button at 839,181 in the
  settings dialog) is what opens the 消息 dialog: an `ActionListener` in `KeymapPanel`, reachable by
  decompiling the app jar (`unzip` it, then `javap -c app/termora/keymap/KeymapPanel.class`) - the
  fastest way to find what triggers any of its dialogs.
- **Killing a client on the VM: bracket the pattern** (`pkill -9 -f '[E]asyShell'`, not
  `pkill -f EasyShell`). Unbracketed, it matches the *remote shell running the pkill* - whose command
  line contains the word - and kills it: ssh returns 255, nothing runs, and there is no output to say
  why. Cost ~20 minutes on 2026-09-24; it is now written up in `tools/README.md`.
- **`xdotool` works against this server now** (2026-09-27) - it segfaulted when the item was written,
  but that was A4, whose cause turned out to be B1 and is fixed. In `--single-window` mode it is the
  easiest way to drive a client from the VM: `DISPLAY=192.168.22.1:<display> xdotool mousemove X Y
  click 3`. The bare-protocol `Drive.java` (XTEST) from the Mac still works and is what the suite uses.
  **Its requests are separable from the client's in the server log**: each `xdotool` call opens a
  connection and leaves a `QueryExtension`/`GetInputFocus`/`GetModifierMapping`/`QueryPointer`/
  `WarpPointer` group, which is how "the client sent no drawing request" was measured for A17.
- **XTEST is scoped to the screen of the client that sent it** (`XTestExtension` -> `server.inputFor(client)`),
  so in the default one-window-per-client mode a probe client cannot drive another client's window. Run
  the server with `--single-window` to reproduce: every client then shares one screen, the probe's
  synthetic input lands on termora, and `xwininfo -root -tree`/`xwd` can see the windows (in per-client
  mode a fresh client gets an empty root of its own and sees nothing).
- Screenshots: the frame dumps (`-Dxserver.dumpFrame`) work in either mode; `xwd` on the VM only works
  against Xvfb or `--single-window`.
- **The verification kit now lives in the repository** (2026-09-24), because `/tmp` is volatile - the
  author cleared it by hand once and macOS recycles it: the tools are in
  `src/test/java/io/github/oyzh1994/xserver/tools/` (`Drive`, `WinTree`, `Focus`, `Pixel`, `Xwd`,
  `Blocks`, `Colors`, `Shift`, `Rect`, `Crop`, a reconstructed `XProxy`), the harness scripts are
  `tools/vm.sh` + `tools/bed.sh` (the VM password is read from `~/.xserver-vm-pass`, never committed),
  and `tools/README.md` is the entry point. `docs/backlog.md` is the item-by-item list of what is
  still missing, written to be worked through one item per branch in a fresh session.
- **Comparing "does it look right" against Xvfb**: run the same AppImage against Xvfb :98 and against
  the Mac's server with `--single-window`, then `xwd -root` both **with the pointer parked in the same
  place and the app in the same state** (hover and selection state show up as huge fake differences, and
  our root is #E8E8E8 where Xvfb's is black). `/tmp/Xwd.java` converts and diffs the captures, with
  `/tmp/Blocks.java` (a grid of difference counts), `/tmp/Colors.java` (every distinct colour in a
  region), `/tmp/Shift.java` (is it a shift or a difference?) and `/tmp/Rect.java` (an ASCII map).
  A depth-8 mask that arrives quantised to 0x00/0x55/0xaa/0xff is the server reading coverage as a
  3-3-2 colour, not the client rasterising badly.

The VM login (the user has had to repeat this three times, so it goes here): `ssh root@192.168.22.152`,
password `user@147`. There is no key auth. **`expect` hangs on the password prompt in this environment
(2026-09-23) - use `SSH_ASKPASS`, which works**: 

```
printf '#!/bin/sh\necho "user@147"\n' > /tmp/askpass.sh && chmod +x /tmp/askpass.sh
SSH_ASKPASS=/tmp/askpass.sh SSH_ASKPASS_REQUIRE=force ssh -o StrictHostKeyChecking=no \
  -o UserKnownHostsFile=/dev/null -o PreferredAuthentications=password -o PubkeyAuthentication=no \
  root@192.168.22.152 'COMMAND'
```

Long jobs must be detached (`setsid nohup ... < /dev/null &`) or ssh kills them when the session ends.
**But a detached job started inside a one-shot `tools/vm.sh` command does not survive either** (tried
2026-09-27 with `setsid nohup Xvfb ... & sleep 40`): the whole command produced no output and left
nothing running. Use `tools/xvfb.sh start`, which detaches correctly.

**`tools/vm.sh` uses `ssh -n`, so a piped stdin is discarded.** `cat script | tools/vm.sh 'cat > /tmp/x'`
writes an **empty** file and exits 0 - which reads as "the script ran and printed nothing". To push a
file, put it on the command line: `tools/vm.sh "printf '%s' '<base64 -w0>' | base64 -d > /tmp/x"`. To
pull one, `tools/vm.sh 'gzip -c f | base64 -w0' | tail -1 | base64 -d | gunzip > f` (the `tail -1`
drops the ssh warning line).

The older form, for reference:

```
expect -c 'set timeout 90; spawn ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
  root@192.168.22.152 "REMOTE COMMAND"; expect { -re "assword:" { send "user@147\r"; exp_continue } eof }'
```

The VM is a **client-only** host - `xclock`, `xeyes`, `xcalc`, `xev`, `xwininfo`, `xdpyinfo`, `xmodmap`,
`xwd`, python3, and no X server of its own - so a client is pointed at the Mac with
`DISPLAY=192.168.22.1:<display>` over TCP (no tunnel needed).

Test bed used to reproduce EasyShell (JavaFX client) against this xserver:

- X server runs **locally on the Mac** (JavaFX backend). The Mac has `bridge101 = 192.168.22.1`, same subnet as the VM, so a client can also connect straight to `192.168.22.1:<display>` over TCP, no tunnel.
- Client runs on the Linux VM `192.168.22.152` (root) — `./EasyShell/bin/EasyShell` or the AppImage in `/root` (same app). The VM also has `xeyes/xclock/xdpyinfo/xev/xwininfo` and an Xvfb on `:99`.
- ssh X forwarding works from the Mac only with `DISPLAY=<xserver display> ssh -Y root@…`; macOS GUI terminals inherit `launchctl getenv DISPLAY` = `/private/tmp/com.apple.launchd.*/org.xquartz:0`, so a plain `ssh -Y` sends the traffic to a dead XQuartz socket, **not** to the xserver. `ssh -X` (untrusted) needs a working local `xauth` (installed via `brew install xauth`).

Two failures that look like server bugs but are not:

1. `org.h2.jdbc.JdbcSQLNonTransientConnectionException … SingleFileStore.lockFileChannel` from `cn.oyzh.store.jdbc.JdbcManager.takeoff`: a **second** EasyShell instance dies during `init`, before it creates any window, because `/root/.easyshell/db.mv.db` is locked by the first one. Always `pkill -f EasyShell; sleep 2` before launching, or isolate with `JDK_JAVA_OPTIONS="-Dstore.dir=/root/.easyshell-dev/"`.
2. A black screen for EasyShell on a display where `xwininfo -root -tree` shows **0 children** means no client ever connected — check `echo $DISPLAY` on the remote and the server's `--log-requests` before blaming rendering.

Diagnostics that actually settle "server drew nothing" vs "window shows nothing": run with `-Dxserver.dumpFrame=<prefix> -Dxserver.dumpFrames=N` and compare `<prefix>-buffer-N.png` (framebuffer), `-image-N.png` (WritableImage handed to JavaFX) and `-scene-N.png` (scene snapshot = what the window shows). See [[xserver-open-bugs]].

Building and driving it without the VM (verified 2026-09-22):

- **`mvn` is not on PATH and the repo has no wrapper** — use `/Users/oyzh/Documents/Tools/apache-maven-3.9.11/bin/mvn`. `~/.m2` already holds every dependency, so `-o` (offline) works and is fast; the full suite is 79 tests, most of them seconds. Java on PATH is 27, which satisfies the pom's `release 25`.
- The bare-protocol Java clients in `/tmp` need no Xlib and no VM: `Keys.java` types a string via XTEST, `Click.java` clicks, `KeymapProbe.java` reads back `GetKeyboardMapping` — the equivalent of `xmodmap -pke`, and the way to see what a client actually resolves a keycode to. Compile with `javac`, then run against `127.0.0.1 6090` with the headless server on `:90` (`java -cp target/classes:<$(mvn -o dependency:build-classpath)> io.github.oyzh1994.xserver.Main --display 90 --backend headless`).
- The Mac has **no XQuartz and no `xmodmap`/`xev`**, and the VM refuses `ssh -o BatchMode` (publickey/password only), so a client-side check has to go through those `/tmp` harnesses unless the user is there to type a password.
- **Reading the rendering needs neither the screen nor any permission**: start the server with `-Dxserver.dumpFrame=<prefix> -Dxserver.dumpFrames=<n>` and it writes `<prefix>-buffer-N.png` (framebuffer), `-image-N.png` and `-scene-N.png` (what the window shows) once a second. That is how `xclock`/`xeyes`/`xcalc` were confirmed fixed on 2026-09-22.
- **`screencapture` screenshots the real screen, cursor included** (`screencapture -x -C`), once Screen Recording is granted. Until the user granted it (2026-09-22) it returned wallpaper only - no windows, no cursor - and it is **not** the sandbox (identical with the sandbox disabled). The permission belongs to the app at the top of the session's chain, which here is `IntelliJ IDEA.app` → `node` → `claude` → `zsh`: System Settings → Privacy & Security → Screen & System Audio Recording → `+` → `/Users/oyzh/Applications/IntelliJ IDEA.app`, then a full ⌘Q and relaunch. `screencapture` itself never appears in that list; granting it to Terminal instead only helps captures run from Terminal.
- **Never match a screenshot colour by exact RGB**: macOS colour-manages the capture, so the probe's `#FF00FF` came back as `#CA26D5`. Match a range, or diff two captures (`/tmp/Crop.java`, `/tmp/Diff.java`, `/tmp/Magenta.java`).
- Tools that made this work, all in `/tmp`: `winlist.c` (on-screen windows with owner, layer and bounds via `CGWindowListCopyWindowInfo` - needs no permission; note floating windows have layer != 0, so filter carefully), `warpcursor.c` (`CGDisplayMoveCursorToPoint`, moves the hardware cursor to a point so a screenshot can be aimed at it), `movemouse.c` (a `CGEventPost` of `kCGEventMouseMoved`, the only one of the two that JavaFX actually sees), `click.c` (move + click, which is how a real window close button gets pressed - that is what verified the ssh-returns-on-window-close behaviour), plus the crop/diff/colour helpers above.
- Driving a client **over ssh** while its display is the xserver on the Mac: `/tmp/vm.exp "DISPLAY=192.168.22.1:<N> xclock"` blocks exactly like a user's `ssh xclock` does, so its exit is the test for "the remote side was told the session is over".
- The sprite is drawn by the display backend over the frame, never into the framebuffer, so an X client (or `xwd` on the VM) never sees it; the `-scene-N.png` dump is the only place it appears.
