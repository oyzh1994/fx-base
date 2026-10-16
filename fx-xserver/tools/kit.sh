#!/bin/sh
# Run one of the verification tools with the right classpath.
#
#   tools/kit.sh Drive 127.0.0.1 6095 "M 100 100" "P 1" "R 1"
#   tools/kit.sh WinTree 127.0.0.1 6095
#
# The classpath is twenty-odd jars long, and pasting it into a command line is what produced
# "command too long" from the shell on the first attempt at driving a client by hand. One script
# that reads it from `target/tools-cp.txt` (which `mvn dependency:build-classpath` writes) removes
# the paste and the quoting from every invocation.
set -e
HERE="$(cd "$(dirname "$0")/.." && pwd)"
MVN="${MVN:-/Users/oyzh/Documents/Tools/apache-maven-3.9.11/bin/mvn}"

if [ $# -lt 1 ]; then
    echo "usage: tools/kit.sh <Tool> [args ...]" >&2
    echo "tools: Drive Keys Shot Wire FocusProbe KeyTargetProbe MenuProbe PointProbe WinTree Focus Pixel Xwd" >&2
    echo "       Blocks Colors Shift Rect Crop AuthProbe SelectionProbe CursorProbe" >&2
    echo "       ClipboardProbe WindowStateProbe XProxy XkbProbe RandrProbe XineramaProbe" >&2
    echo "       ScreenSaverProbe GravityProbe GrabProbe AllowEventsProbe PolyProbe" >&2
    echo "       DamageProbe XiProbe FontProbe ChangeCursorProbe GlyphCursorProbe" >&2
    exit 2
fi

# Regenerated when it is missing *or older than the pom*: a dependency whose version changed - a
# JavaFX upgrade, say - leaves the file listing the jars of the version before it, and the failure
# that produces is a NoSuchMethodError from a class that is there, in another version. That cost an
# afternoon on 2026-10-04, when `HeaderBar.setLeft` "did not exist" while javap said it did.
if [ ! -f "$HERE/target/tools-cp.txt" ] || [ "$HERE/pom.xml" -nt "$HERE/target/tools-cp.txt" ]; then
    (cd "$HERE" && "$MVN" -o -q dependency:build-classpath -Dmdep.outputFile=target/tools-cp.txt)
fi
if [ ! -d "$HERE/target/test-classes" ]; then
    (cd "$HERE" && "$MVN" -o -q test-compile)
fi

TOOL="$1"
shift
# **No proxies.** Every probe talks to a server on the LAN (the Mac's own :61, or the VM's :98), and
# this machine has a system HTTP/SOCKS proxy configured: a JVM that picks that up sends the connection
# to the proxy instead, which shows up as "Malformed reply from SOCKS server" from a probe that has
# nothing to do with SOCKS - and only while the proxy happens to be running. The non-proxy list cannot
# save it either: Java matches host names and globs, not the `192.168.0.0/16` CIDR entry that would
# have covered the testbed.
# The proxies are bypassed in the code rather than here - `RawXClient` opens its socket with
# `Proxy.NO_PROXY`, which is the only switch that works against macOS's launcher passing the system's
# SOCKS proxy to the JVM (see tools/README.md) - but asking the JVM not to look at the system's
# settings costs nothing and covers anything else that opens a socket.
exec java -Djava.net.useSystemProxies=false \
    -cp "$HERE/target/test-classes:$HERE/target/classes:$(cat "$HERE/target/tools-cp.txt")" \
    "io.github.oyzh1994.xserver.tools.$TOOL" "$@"
