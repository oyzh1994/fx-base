# E1's fix: the generator must honour a copy's own number

**Not pursued, by the owner's decision of 2026-09-27, and this file is kept as the record rather than
as a to-do**: the library is neither forked, built nor republished for five wrong bytes, and a
class-level override of the generated events would mean taking Lombok, which this project will not
depend on. **The fix that ships is the rewrite in `ClientOutput.EVENT_NUMBERS`** - the seventh entry in
the README's "Bugs worked around in the protocol library" - pinned by `client/EventTypeByteTest`. What
follows is what is wrong *upstream*, the patch, the verification, and the traps of building that
library, for anyone who ever takes it on.

The sibling document to the backlog entry: the patch, why it is that line, and how it was verified.
It lives here because **the fork it patches is not under version control** - `~/IdeaProjects/other/x11`
is a plain directory - so a fix in it is a change nobody can review and nothing can restore.

## The fault

`xproto.xml` declares five events with `<eventcopy ... number="N" ref="...">` - a release is a press
with another number, a leave is an enter, and so on:

| copy | number | copies | the generated class carried |
|------|--------|--------|------------------------------|
| `ButtonRelease` | 5 | `ButtonPress` | 4 |
| `KeyRelease` | 3 | `KeyPress` | 2 |
| `LeaveNotify` | 8 | `EnterNotify` | 7 |
| `FocusOut` | 10 | `FocusIn` | 9 |
| `CirculateRequest` | 27 | `CirculateNotify` | 26 |

Measured by reading `NUMBER` off the generated classes:

```
ButtonReleaseEvent.NUMBER = 4   (should be 5)
KeyReleaseEvent.NUMBER    = 2   (should be 3)
LeaveNotifyEvent.NUMBER   = 7   (should be 8)
FocusOutEvent.NUMBER      = 9   (should be 10)
CirculateRequestEvent     = 26  (should be 27)
```

The generator *reads* the copy's number - `XTypeEvent.xTypeEventCopy` parses it and gives it to the
event - and then takes the copied event's whole unit list as its own, **including the first unit,
which is the `NUMBER` field carrying the ref's constant**. So the copy is emitted with the ref's
type byte. On the wire a release looks like a second press, a leave like an enter, and a focus-out
like a focus-in: a client never completes a click, sees every keystroke twice, and tracks hover and
focus backwards.

## The patch

```diff
--- /tmp/XTypeEvent.groovy.orig	2026-09-27 22:22:46
+++ /Users/oyzh/IdeaProjects/other/x11/x11-protocol-gradle-plugin/src/main/groovy/com/github/moaxcp/x11protocol/xcbparser/XTypeEvent.groovy	2026-09-27 22:22:46
@@ -43,15 +43,26 @@
     }
     
     static XTypeEvent xTypeEventCopy(XResult result, Node node) {
-        int number = Integer.valueOf((String) node.attributes().get('number'))
+        int numberValue = Integer.valueOf((String) node.attributes().get('number'))
         boolean noSequenceNumber = node.attributes().get('no-sequence-number')
         XTypeEvent ref = result.resolveXType((String) node.attributes().get('ref'))
         if(ref.genericEvent) {
-            number = 35
+            numberValue = 35
         }
-        XTypeEvent event = new XTypeEvent(result: result, number: number, noSequenceNumber: noSequenceNumber, name: node.attributes().get('name'), basePackage: result.basePackage, javaPackage: result.javaPackage)
+        XTypeEvent event = new XTypeEvent(result: result, number: numberValue, noSequenceNumber: noSequenceNumber, name: node.attributes().get('name'), basePackage: result.basePackage, javaPackage: result.javaPackage)
         event.superTypes = ref.superTypes
-        event.protocol = ref.protocol
+        // **A copy's own number has to reach the wire.** The units come from the event this one copies
+        // - that is what a copy is - and the first of them is the NUMBER field, which carries the
+        // *ref's* constant. Emitted as-is, a ButtonRelease goes out as a ButtonPress, a KeyRelease as
+        // a KeyPress: five events in xproto (the releases, LeaveNotify, FocusOut and
+        // CirculateRequest), each silently the wrong type byte on the wire. The list is copied and
+        // the field replaced rather than the ref's own list being mutated, or the event copied *from*
+        // would change with it.
+        event.protocol = new ArrayList<>(ref.protocol)
+        if (!ref.genericEvent && !event.protocol.isEmpty()) {
+            XUnitField number = event.protocol.get(0)
+            event.protocol.set(0, new XUnitField(result: result, name: number.name, type: number.type, constantValue: numberValue))
+        }
         if(ref.genericEvent) {
             event.genericEvent = ref.genericEvent
             event.genericEventNumber = ref.genericEventNumber
```

The list is **copied** before the field is replaced: mutating `ref.protocol` in place would change the
event being copied from, since `event.protocol = ref.protocol` shares one list between them.

## How it was verified (2026-09-27)

1. Patch `XTypeEvent.groovy` as above (the original is kept beside it as a backup).
2. Publish: the build needs a JDK 21 (`JAVA_HOME=.../jbr-21.0.10`) because the wrapper's Gradle cannot
   read the class files of this machine's default JDK 27, and it needs the signing tasks off for a
   local publish:

   ```sh
   cd ~/IdeaProjects/other/x11
   JAVA_HOME=/Users/oyzh/Library/Java/JavaVirtualMachines/jbr-21.0.10/Contents/Home \
     ./gradlew --offline -q --init-script /tmp/no-sign.gradle \
     :x11-protocol:x11-protocol-xproto:publishToMavenLocal
   ```

   (`/tmp/no-sign.gradle` is three lines: `allprojects { tasks.withType(org.gradle.plugins.signing.Sign).configureEach { enabled = false } }`.)
   Only `x11-protocol-xproto` publishes the five classes - the other modules' jars do not contain them.
3. Read the numbers back off the new artifact: they are 5, 3, 8, 10 and 27, and the events they copy
   are unchanged (4, 2, 7, 9, 26).
4. `mvn -o test -Dtest=EventTypeByteTest` in this repository, **with `ClientOutput.EVENT_NUMBERS`
   disabled**: 5/5 pass - the fixed generator alone is enough.
5. The same test with the **published** 0.22.0 artifact put back and the rewrite still disabled:
   **4 of 5 fail**, each with the symptom above (a `KeyRelease` arrives as type 2, a `ButtonRelease`
   as 4, a `LeaveNotify` as 7, a `FocusOut` as 9).

## What is left, and it is the fork owner's

The fix is in the fork on this machine and in this file; what is missing is the *release*. Until the
fork is under version control and a corrected artifact is published, this repository keeps its
rewrite (`ClientOutput.EVENT_NUMBERS`), which - measured - is **idempotent with a fixed generator**:
both together pass all five tests, so the rewrite can stay until the artifact is out and be deleted
afterwards. Note that this machine's `~/.m2` now holds the fixed artifact while Maven Central holds
the buggy one, so a fresh clone still resolves the buggy one - and the rewrite is what makes that
harmless.

## The fallback, if the rewrite is ever dropped: a same-named-class override (measured 2026-09-27)

Five classes under `src/main/java/com/github/moaxcp/x11/protocol/xproto/` - `ButtonReleaseEvent`,
`KeyReleaseEvent`, `LeaveNotifyEvent`, `FocusOutEvent`, `CirculateRequestEvent` - would shadow the
library's, because this project's `target/classes` precedes the dependency on the classpath and javac
prefers a source in the compilation. Two facts make it safe, and both were checked: **nothing else in
the library constructs or references those five classes** (their only references are inside their own
files), and this server never *decodes* them, so only `write`, `getNumber` and `getResponseCode`
matter. The cost is the code: the generated classes are ~150 lines each and Lombok-built (`@Value
@Builder`), so an override is either "copy them verbatim and take Lombok" (rejected by the owner) or
"write them out without it" (~450 lines, `read` can be dropped, generated from the library's own
sources by a script so it can be refreshed). It was set aside in favour of the rewrite, not rejected
as wrong.
