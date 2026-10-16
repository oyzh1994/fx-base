---
name: imessage-notification
description: "How a long unattended task tells the person driving this machine that it is done: osascript to Messages, the handle that works, and what failed before"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 01353543-0895-4938-81fe-8b2a65312ab8
---

When a long unattended task finishes, the person driving this machine wants to hear about it. The
route that works is **`osascript` driving Messages.app**, sent to an **email address**:

```sh
osascript -e 'with timeout of 20 seconds
tell application "Messages"
  set targetService to 1st service whose service type = iMessage
  set targetBuddy to buddy "oyzh.1994@qq.com" of targetService
  send "..." to targetBuddy
end tell
end timeout'
```

**The handle is the email address, and it is an email rather than a phone number on purpose**
(2026-10-09): this repository is published, an address is already public, and a phone number is
somebody's private information. Both reach the same phone, so nothing about the notification
changed.

- **Wrap it in `with timeout`**: a send that hits a blocked automation permission *hangs* rather than
  failing, and an unbounded `osascript` stops the session.
- A call that only reads (`tell application "Messages" to get name`) is a cheap check that the
  automation permission is in place; it answered on 2026-09-25 (Messages.app was already running).
- What did **not** work, tried in an earlier session and worth not repeating: Mail.app to the same
  address, `terminal-notifier` (a local notification, not the phone), `shortcuts run`, SMS through
  Messages, and push services (no token). Do not spend a session on those again.

## The text must not come in through the environment - it is not UTF-8

Two messages arrived as mojibake on 2026-10-04 (reported by the user, "你最近发的2条imessage都中文乱码了").
Both had been sent with the body passed in an environment variable and read with
`system attribute "XS_MSG"`:

```sh
XS_MSG="$(cat /tmp/xsn/imsg7.txt)" osascript -e '... send (system attribute "XS_MSG") to targetBuddy ...'
```

**That is the bug.** `system attribute` hands the value to AppleScript in the *system's* encoding
rather than as UTF-8, so every multi-byte character is mangled - measured:

```
in : 中文测试            e4 b8 ad e6 96 87 e6 b5 8b e8 af 95
out:                     e6 b6 93 ef bf bd c2 96 c2 87 e5 a8 b4 c2 8b e7 92 87 c2 95
```

Earlier messages were fine because their text was written *inside* the `-e` argument, and `argv` is
UTF-8. So: **never hand the text over through the environment or a shell variable.** Put it in a file
and read it inside the script with an explicit encoding, which is byte-exact (verified with `xxd` on
the same sentence):

```applescript
set msg to read (POSIX file "/tmp/xsn/imsg8.txt") as «class utf8»
```

`/tmp/xsn/imessage.sh` is the sender that does this - `imessage.sh <file> [handle]`, default handle
the address above. Two things about it that cost a round trip each:

- **`with timeout` must be closed.** Splitting the script across several `-e` arguments is fine
  (`osascript` joins them with newlines), but an opened `with timeout of N seconds` without its
  `end timeout` fails with `syntax error: ... found end of script` at the very end of the script,
  which reads as a problem with the text rather than with the block.
- the `«class utf8»` guillemets survive `-e` unchanged (they are UTF-8 in `argv`), so they are safe.
