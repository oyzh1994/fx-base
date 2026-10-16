---
name: cleanup-after-testing
description: "The user asks that every process a test run started be cleaned up when testing stops, because leftovers read as 'still testing'"
metadata: 
  node_type: memory
  type: feedback
  originSessionId: 5ec57e3e-19bc-4a23-9aea-770a8229f44e
  modified: 2026-09-26T08:46:46.888Z
---

The user (2026-09-26): a session must clean up the processes it started when it stops testing - the
test server on the Mac, the ssh tunnels to the reference display, the clients on the VM
(`mount_EasySh`/`mount_termor`), `Xvfb` and `ibus`. Their words: "不然我会以为你还在测试".

**Why:** a leftover server or client is indistinguishable from a run in progress, and one of them -
the `ssh -L 6098` tunnel to the reference - *breaks* the next test run in a way that looks like a bug
in the server (a window id one off in `FocusEventTest`; it cost four runs on 2026-09-26).

**How to apply:** at the end of testing, stop them and say what was stopped. Kill by a pattern that
does not appear in the command doing the killing - `pkill -9 -f "ibus-[d]aemon"`, not
`pkill -9 -f "ibus-daemon -drx"`, whose own text matches. Leave the session's `dbus-daemon` alone:
killing it takes any client on that bus with it (that is how EasyShell died mid-measurement). The
guard for the tunnel case is in `ci.sh`, which refuses to run while a display the suite binds is
occupied; the list of those displays is in `tools/README.md`.
