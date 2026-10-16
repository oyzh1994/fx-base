#!/bin/sh
# Start this server and a client on the VM against it, or against the VM's own Xvfb for a comparison.
#
# Usage: tools/bed.sh [single|perclient|xvfb] [display]
#
#   single      server on :95 in --single-window mode (a probe or XTEST driver can see the client's
#               windows and reach them; this is the mode scripted tests use)
#   perclient   server on :95 in the default mode, one host window per client (what a user runs)
#   xvfb        no server here: the client runs against the VM's own Xvfb :98, the reference
#
# Anything else about the run - dumping frames, logging requests - goes in $XSERVER_OPTIONS, e.g.
#   XSERVER_OPTIONS="-Dxserver.dumpFrame=/tmp/d -Dxserver.dumpFrames=3000" tools/bed.sh single
#
# The server is started from target/classes (run `mvn -o test-compile` first); the client is whatever
# $CLIENT is, and the default is the termora AppImage the author tests with.
set -e
MODE="${1:-single}"
DISPLAY_NUMBER="${2:-95}"
CLIENT="${CLIENT:-/root/termora-2.0.0-beta.16-linux-aarch64.AppImage}"
MVN="${MVN:-/Users/oyzh/Documents/Tools/apache-maven-3.9.11/bin/mvn}"
HERE="$(cd "$(dirname "$0")/.." && pwd)"
HOST_IP="$(ipconfig getifaddr bridge101 2>/dev/null || echo 192.168.22.1)"

if [ "$MODE" != "xvfb" ]; then
    pkill -f "io.github.oyzh1994.xserver.Main --display $DISPLAY_NUMBER" 2>/dev/null || true
    sleep 1
    if [ ! -f target/tools-cp.txt ]; then
        (cd "$HERE" && "$MVN" -o -q dependency:build-classpath -Dmdep.outputFile=target/tools-cp.txt)
    fi
    # One logback config per run: the log file is named after the display, and `append` false so a
    # restarted run does not mix its requests with the previous one's.
    rm -f /tmp/xs-bed-$DISPLAY_NUMBER.log
    cat > /tmp/xs-bed-logback.xml <<XML
<configuration>
    <appender name="file" class="ch.qos.logback.core.FileAppender">
        <file>/tmp/xs-bed-$DISPLAY_NUMBER.log</file>
        <append>false</append>
        <encoder><pattern>%d{HH:mm:ss.SSS} %-5level %logger{20} - %msg%n</pattern></encoder>
    </appender>
    <logger name="io.github.oyzh1994.xserver" level="DEBUG"/>
    <root level="INFO"><appender-ref ref="file"/></root>
</configuration>
XML
    case "$MODE" in
        single) EXTRA="--single-window" ;;
        perclient) EXTRA="" ;;
        *) echo "unknown mode $MODE" >&2; exit 2 ;;
    esac
    (cd "$HERE" && nohup java -Dlogback.configurationFile=/tmp/xs-bed-logback.xml $XSERVER_OPTIONS \
        -cp "target/classes:$(cat target/tools-cp.txt)" io.github.oyzh1994.xserver.Main \
        --display "$DISPLAY_NUMBER" --geometry 1300x800 $EXTRA --log-requests \
        > /tmp/xs-bed-$DISPLAY_NUMBER.out 2>&1 &)
    sleep 5
    CLIENT_DISPLAY="$HOST_IP:$DISPLAY_NUMBER"
else
    CLIENT_DISPLAY=":98"
fi

# One client instance at a time: termora refuses to start twice.
"$HERE/tools/vm.sh" 'pkill -9 -f mount_termor' >/dev/null 2>&1 || true
sleep 3
"$HERE/tools/vm.sh" "cd /root && setsid nohup env DISPLAY=$CLIENT_DISPLAY $CLIENT \
    > /root/xs-client.log 2>&1 < /dev/null & sleep 24; echo started" 2>&1 | tail -1

echo "client display: $CLIENT_DISPLAY"
echo "server log:     /tmp/xs-bed-$DISPLAY_NUMBER.log"
