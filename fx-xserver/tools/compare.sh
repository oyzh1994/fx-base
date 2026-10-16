#!/bin/sh
# Compare what two servers draw for the same client, driven through the same gesture.
#
# Usage: tools/compare.sh [-- drive-step ...]
#
#   tools/compare.sh -- "M 300 200" "W 500" "P 1" "R 1"
#
# What it does, in order (each step is one of the six commands this replaces):
#
#   1. starts this server on :95 in --single-window mode and the client against it (tools/bed.sh)
#   2. drives the gesture steps through XTEST, from this machine (tools/Drive)
#   3. captures the screen with `xwd -root` **on the VM** - the same tool on both sides, which is
#      the only reading that is comparable (docs/backlog.md §1, rule 3)
#   4. stops the client and starts it again against the VM's own Xvfb :98, the reference
#   5. drives the same steps and captures the same way
#   6. prints the `Xwd` diff and the `Blocks` grid of the two captures
#
# The reference client has to run *after* the first one, not beside it: termora and EasyShell both
# refuse to start twice (an AppImage lock, and /root/.easyshell/db.mv.db's H2 lock), so the two runs
# are sequential by necessity.
#
# Before believing any difference, remember the two that are by design (rule 4): this server's root
# is #E8E8E8 where Xvfb's is black, and it draws a window border that a WM-less real server does not.
#
#   CLIENT=/root/EasyShell.AppImage     what to run on the VM (default: the termora AppImage)
#   GESTURE_WAIT=1200                   how long to wait after the gesture, before capturing (ms)
#   PARK="2 2"                          where to park the pointer before capturing, on both sides
#   NAME=run1                           labels the files under /tmp/xs-compare/
set -e
HERE="$(cd "$(dirname "$0")/.." && pwd)"
VM="192.168.22.152"
VM_DISPLAY_PORT=6098                        # the VM's Xvfb :98
DISPLAY_NUMBER="${DISPLAY_NUMBER:-95}"
HOST_IP="$(ipconfig getifaddr bridge101 2>/dev/null || echo 192.168.22.1)"
GESTURE_WAIT="${GESTURE_WAIT:-1200}"
PARK="${PARK:-2 2}"
NAME="${NAME:-compare}"
OUT="/tmp/xs-compare"
MVN="${MVN:-/Users/oyzh/Documents/Tools/apache-maven-3.9.11/bin/mvn}"

STEPS=""
if [ "${1:-}" = "--" ]; then
    shift
    STEPS="$*"
fi

mkdir -p "$OUT"
CP="target/test-classes:target/classes:$(cat "$HERE/target/tools-cp.txt")"

drive() {                                   # drive <host> <port> <steps...>
    host="$1"; port="$2"; shift 2
    # shellcheck disable=SC2086
    (cd "$HERE" && java -cp "$CP" io.github.oyzh1994.xserver.tools.Drive "$host" "$port" $STEPS \
        "W $GESTURE_WAIT" "M $PARK")
}

capture() {                                 # capture <display-on-the-vm> <local-name>
    # xwd runs on the VM against whichever display it is pointed at, so both captures come from the
    # same tool and the same machine.
    "$HERE/tools/vm.sh" "xwd -root -display $1 -silent -out /tmp/xs-shot.xwd" >/dev/null
    "$HERE/tools/vm.sh" "cat /tmp/xs-shot.xwd" > "$OUT/$2"
    echo "$OUT/$2: $(wc -c < "$OUT/$2") bytes"
}

echo "== run 1: this server on :$DISPLAY_NUMBER =="
CLIENT="$CLIENT" "$HERE/tools/bed.sh" single "$DISPLAY_NUMBER" | tail -2
drive "$HOST_IP" "$((6000 + DISPLAY_NUMBER))"
capture "$HOST_IP:$DISPLAY_NUMBER" "$NAME-here.xwd"

echo "== run 2: the VM's Xvfb :98 (the reference) =="
CLIENT="$CLIENT" "$HERE/tools/bed.sh" xvfb | tail -1
drive "$VM" "$VM_DISPLAY_PORT"
capture ":98" "$NAME-xvfb.xwd"

echo "== diff =="
(cd "$HERE" && java -cp "$CP" io.github.oyzh1994.xserver.tools.Xwd png "$OUT/$NAME-here.xwd" \
    "$OUT/$NAME-here.png" >/dev/null && java -cp "$CP" io.github.oyzh1994.xserver.tools.Xwd png \
    "$OUT/$NAME-xvfb.xwd" "$OUT/$NAME-xvfb.png" >/dev/null)
echo "PNGs: $OUT/$NAME-here.png  $OUT/$NAME-xvfb.png"
(cd "$HERE" && java -cp "$CP" io.github.oyzh1994.xserver.tools.Xwd diff "$OUT/$NAME-xvfb.xwd" \
    "$OUT/$NAME-here.xwd")
(cd "$HERE" && java -cp "$CP" io.github.oyzh1994.xserver.tools.Blocks "$OUT/$NAME-xvfb.xwd" \
    "$OUT/$NAME-here.xwd" 40)
