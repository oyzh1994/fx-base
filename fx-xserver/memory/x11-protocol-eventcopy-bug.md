---
name: x11-protocol-eventcopy-bug
description: "The moaxcp x11-protocol generator ignores the number on an eventcopy, so five event classes carry the wrong wire type byte"
metadata: 
  node_type: memory
  type: reference
  originSessionId: 20e98c73-c9b5-4cc8-9d2e-466cca178651
  modified: 2026-09-22T05:26:25.552Z
---

`xproto.xml` in the user's x11-protocol fork (`other/x11`, used by both `xserver` and other
projects) declares five events with `<eventcopy ... ref="...">`, and moaxcp's generator **ignores
the `number` attribute on a copy**: the generated class carries the number of the event it copies.
The XML (verified 2026-09-22) and what the generated classes report:

| event | XML says | generated `NUMBER` |
| --- | --- | --- |
| `KeyRelease` | 3 (copies `KeyPress` 2) | **2** |
| `ButtonRelease` | 5 (copies `ButtonPress` 4) | **4** |
| `LeaveNotify` | 8 (copies `EnterNotify` 7) | **7** |
| `FocusOut` | 10 (copies `FocusIn` 9) | **9** |
| `CirculateRequest` | 27 (copies `CirculateNotify` 26) | **26** |

Two `eventcopy`s are `KeyRelease`/`ButtonRelease` and `xproto.xml` has only 29 full `<event>`
definitions, so any *new* eventcopy has to be checked the same way.

On the wire that makes a release look like a second press and a leave like an enter: clients never
see a click completed (buttons never fire), every keystroke arrives twice, and hover/focus tracking
runs backwards. Diagnosed with `xev` (it printed `ButtonPress` twice with the second carrying
`state 0x100`) and pinned by printing the generated `NUMBER` constants. `xdotool`-style synthetic
input hid the problem: its XTEST FakeInput requests in the 2.2 layout are 36 bytes with the
coordinates at offset 24 and the device id in the last byte.

Workaround in place: `ClientOutput.EVENT_NUMBERS` rewrites the type byte for those five classes
where every generated event's bytes already pass through. **The real fix is in the generator** -
honour the number on an `eventcopy`, or replace the copies with full `<event>` entries - and then
rebuild/reinstall the jar; the workaround can go when that lands. See [[xserver-testbed]] for how to
drive a client here (XTEST click/key tools in `/tmp/Click.java`, `/tmp/Keys.java`).
