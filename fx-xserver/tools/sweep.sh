#!/bin/sh
# Run every protocol probe against the reference server and against this one, and diff the two.
#
#   tools/sweep.sh                 every probe that runs with just a host and a port
#   tools/sweep.sh PointProbe XiProbe      only these
#
# Why this exists. The kit's probes are each a measurement of one area, and the way a difference is
# found is to run one against Xvfb and against this server and diff the two (`tools/probe-diff.py`).
# That was done by hand, area by area, and what comes of it is written into `docs/backlog.md` - but
# nothing re-ran them all, so a change could drift an area that nobody happened to probe that day.
# This is that command: it is the regression check a change like B5's per-window store wants, because
# what such a change can break is a reply, and a reply is what a probe reads.
#
# Both servers must be up and the same shape: a 1300x800 screen, and `--single-window` here so one
# screen is what a probe sees (a probe does not care what the backend is; the headless one is fine).
#
#   tools/xvfb.sh start                                       the reference, on the VM's :98
#   java -cp target/classes:$(cat target/tools-cp.txt) io.github.oyzh1994.xserver.Main \
#       --display 61 --geometry 1300x800 --single-window \
#       --no-clipboard --no-tray &                           this server
#   tools/sweep.sh
#
# **`--no-clipboard --no-tray` is part of the recipe**: those bridges are host-side features a
# reference server has nothing like, and they answer for the *client* - the clipboard bridge takes
# the `CLIPBOARD` selection when a client gives it up (which a probe using that name reads as an
# extra `SelectionNotify` from an owner it never made). Measured 2026-09-27: with the bridges on,
# `SelectionProbe` differs by one event; with them off it is identical.
#
# **The reference must be clean before a run.** A client killed while it had windows mapped can leave
# them behind on Xvfb, and then the two servers do not start from the same state: `MenuProbe` differs
# in the detail of its *first* crossing (Nonlinear against Ancestor) purely because the parked pointer
# is in a leftover window on one server and on the root on the other. `tools/xvfb.sh stop` and start
# it again between runs that matter.
#
# **The pointer is parked on both servers before every probe** (rule 5 in tools/README.md): it
# outlives every client, so a probe that starts wherever the last one left it measures this server's
# history rather than the two servers' behaviour - `PointProbe` reports the pointer's position and
# would otherwise be a difference on every run.
#
# A difference is not automatically a bug: some are deliberate and are listed in
# `tools/README.md` ("What the sweep is expected to differ on"). Read that before chasing one.
set -e
HERE="$(cd "$(dirname "$0")/.." && pwd)"
MVN="${MVN:-/Users/oyzh/Documents/Tools/apache-maven-3.9.11/bin/mvn}"
DISPLAY_NUMBER="${DISPLAY_NUMBER:-61}"
VM="${VM:-192.168.22.152}"
VM_REFERENCE_PORT="${VM_REFERENCE_PORT:-6098}"          # the VM's Xvfb :98
OUT="${OUT:-/tmp/xs-sweep}"

# The probes that take a host and a port and nothing else, and the two that need one argument.
PROBES="PointProbe GravityProbe PolyProbe WindowStateProbe DamageProbe CursorProbe ChangeCursorProbe
        GlyphCursorProbe XiProbe GlxProbe RandrProbe XineramaProbe ScreenSaverProbe SelectionProbe
        FontProbe FocusProbe AllowEventsProbe MenuProbe CompositeProbe SyncProbe PresentProbe
        XgeProbe ResProbe RecordProbe DbeProbe SecurityProbe XvProbe"
PROBES="$PROBES GrabProbe:button"
if [ $# -gt 0 ]; then
    PROBES="$*"
fi

CP="target/test-classes:target/classes:$(cat "$HERE/target/tools-cp.txt")"
mkdir -p "$OUT"

run_probe() {                               # run_probe <probe> <host> <port> <outfile>
    probe="$1"; host="$2"; port="$3"; out="$4"
    shift 4
    # Parked first, and from this machine, so both runs start from the same place.
    (cd "$HERE" && java -cp "$CP" io.github.oyzh1994.xserver.tools.Drive "$host" "$port" \
        "M 2 2" "W 200" >/dev/null 2>&1) || true
    (cd "$HERE" && java -cp "$CP" "io.github.oyzh1994.xserver.tools.$probe" "$host" "$port" "$@") \
        > "$out" 2>&1 || echo "  ($probe against $host:$port exited non-zero)"
}

identical=0
different=""
for entry in $PROBES; do
    probe="${entry%%:*}"
    args="${entry#*:}"
    if [ "$args" = "$probe" ]; then
        args=""
    fi
    # shellcheck disable=SC2086
    run_probe "$probe" "$VM" "$VM_REFERENCE_PORT" "$OUT/$probe.xvfb.txt" $args
    # shellcheck disable=SC2086
    run_probe "$probe" 127.0.0.1 "$((6000 + DISPLAY_NUMBER))" "$OUT/$probe.here.txt" $args

    verdict=$(python3 "$HERE/tools/probe-diff.py" "$OUT/$probe.xvfb.txt" "$OUT/$probe.here.txt" \
        2>&1 | head -1)
    case "$verdict" in
        IDENTICAL*)
            printf '%-20s %s\n' "$probe" "identical"
            identical=$((identical + 1))
            ;;
        *)
            printf '%-20s %s\n' "$probe" "DIFFERENT - $OUT/$probe.xvfb.txt vs $OUT/$probe.here.txt"
            different="$different $probe"
            ;;
    esac
done

echo
echo "$identical probe(s) identical to the reference."
if [ -n "$different" ]; then
    echo "different:$different"
    echo "read tools/README.md, \"What the sweep is expected to differ on\", before chasing one"
fi
