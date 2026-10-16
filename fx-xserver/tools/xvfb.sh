#!/bin/sh
# Start the reference display on the VM: Xvfb :98 plus x11vnc, so a human can watch it and a script
# can drive and capture it.
#
# Usage: tools/xvfb.sh [start|stop|status] [WxHxD]
#
#   start    Xvfb :98 (default 1300x800x24) and `x11vnc -display :98 -rfbport 5900` on the VM
#   stop     both of them
#   status   what is running and which ports are listening
#
# This is the other half of `tools/bed.sh xvfb`, which assumes the display exists. It is a script of
# its own because two details of starting it are easy to get wrong and cost time when they are:
#
#   * **`-listen tcp` is required.** A stock Xvfb build does not open TCP at all, so `DISPLAY=:98`
#     works for a client *on the VM* while nothing from this machine can reach it - and a comparison
#     driven from here (tools/compare.sh) needs port 6098.
#   * **`-nolisten unix` breaks the client on the VM.** The client's `DISPLAY=:98` goes through the
#     unix socket, so the display has to keep both transports.
#   * **`-ac` is required for the probes.** Xvfb refuses a connection that offers no credentials, and
#     the kit's probes (`FocusProbe`, `MenuProbe`, `PointProbe`) are hand written clients that send
#     none - they would report "the connection setup was refused" against an otherwise healthy
#     reference display.
#
# The screenshots go to `/tmp/xs-shot.xwd` on the VM, which is where tools/compare.sh reads them.
set -e
HERE="$(cd "$(dirname "$0")/.." && pwd)"
ACTION="${1:-start}"
GEOMETRY="${2:-1300x800x24}"
VM_HOST="${XSERVER_VM_HOST:-192.168.22.152}"
# The cookie the access-controlled reference on :97 is started with; fixed so the probes and the
# documentation can name it (see `secure` below).
SECURE_COOKIE="${SECURE_COOKIE:-deadbeefcafebabe0123456789abcdef}"

case "$ACTION" in
    start)
        # The pkill pattern is bracketed, and the start is a separate ssh command: a pattern that
        # also appears in the command line doing the killing matches the remote shell itself, which
        # kills it before it starts anything (tools/README.md).
        "$HERE/tools/vm.sh" 'pkill -f "Xvfb :9[8]" ; pkill -f "[x]11vnc -display :98" ; true' \
            >/dev/null 2>&1 || true
        sleep 1
        "$HERE/tools/vm.sh" "setsid nohup Xvfb :98 -screen 0 $GEOMETRY -listen tcp -ac \
            > /root/xvfb98.log 2>&1 < /dev/null & sleep 3; \
            setsid nohup x11vnc -display :98 -forever -rfbport 5900 -listen 0.0.0.0 -shared \
            > /root/x11vnc98.log 2>&1 < /dev/null & sleep 2; true" >/dev/null
        "$HERE/tools/xvfb.sh" status
        ;;
    secure)
        # A *second* reference with access control **on**, which is what the SECURITY extension needs:
        # the display above runs with `-ac`, and a server that asks for no credentials has no reason
        # to trust anybody differently - a token handed out by SECURITY is accepted and the
        # connection is trusted, so nothing untrusted can be measured there at all (F7). The cookie
        # is fixed so a probe can be pointed at this display without reading it back:
        #
        #   tools/xvfb.sh secure
        #   tools/kit.sh SecurityProbe 192.168.22.152 6097 MIT-MAGIC-COOKIE-1 deadbeefcafebabe0123456789abcdef
        # The display lock has to go with the server: Xvfb refuses to start on a display whose lock
        # file is still there ("already active"), which is exactly what a killed one leaves behind.
        "$HERE/tools/vm.sh" 'pkill -f "Xvfb :9[7]" ; rm -f /tmp/.X97-lock /tmp/x97.auth; true' \
            >/dev/null 2>&1 || true
        sleep 1
        "$HERE/tools/vm.sh" "xauth -f /tmp/x97.auth add :97 MIT-MAGIC-COOKIE-1 $SECURE_COOKIE" >/dev/null
        "$HERE/tools/vm.sh" "setsid nohup Xvfb :97 -screen 0 $GEOMETRY -listen tcp -auth /tmp/x97.auth \
            > /root/xvfb97.log 2>&1 < /dev/null & sleep 3; true" >/dev/null
        "$HERE/tools/vm.sh" 'pgrep -a "[X]vfb :97" || echo "Xvfb :97 is not running"'
        echo "reference :97 with access control; cookie: $SECURE_COOKIE"
        ;;
    stop)
        "$HERE/tools/vm.sh" 'pkill -f "Xvfb :9[8]"; pkill -f "[x]11vnc -display :98"; pkill -f "Xvfb :9[7]"; true' \
            >/dev/null 2>&1 || true
        echo "stopped"
        ;;
    status)
        "$HERE/tools/vm.sh" 'pgrep -a "[X]vfb" ; pgrep -a "[x]11vnc" ; \
            (ss -ltn 2>/dev/null || netstat -ltn) | grep -E ":6098|:5900" || echo "nothing listening"' \
            | grep -v "^Warning:"
        echo "vnc: vnc://$VM_HOST"
        ;;
    *)
        echo "usage: tools/xvfb.sh [start|stop|status] [WxHxD]" >&2
        exit 2
        ;;
esac
