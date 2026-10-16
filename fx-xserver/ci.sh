#!/bin/sh
# What a continuous integration run does, in one command.
#
#   ci.sh                 the suite, quietly, and where the report went
#   ci.sh -Dtest=HighDpiTest     anything else is passed to Maven
#
# Backlog E2. The point of this file is that the suite has to survive a machine that cannot show a
# window - a CI runner, a container, a build box reached without a display - and that was not true
# before: `HighDpiTest` started its server without `--backend headless`, so the server took the
# default backend and opened a real window *inside the test JVM*, and where there is no display the
# toolkit's startup never returns and the build hangs for ever instead of failing. The JavaFX test
# had the same assumption from the other side: it asked whether JavaFX was on the classpath, which it
# always is, and not whether a window could be opened at all.
#
# So the two things this script stands for are what CI needs to know about this project:
#
#   * **the suite runs headless**, and skips exactly the two tests that need a window rather than
#     failing or hanging (set `-Djava.awt.headless=true` in the test JVM to reproduce a runner here:
#     `MVN_OPTS=-Djava.awt.headless=true ci.sh`)
#   * **no test opens a host window**, which is why every other server in the suite is started with
#     `--backend headless` and only `JavaFxBackendTest` touches a toolkit at all
#
# What it deliberately does not do is configure a particular CI service: this repository's remote is
# Gitee, whose pipelines take their own YAML, and a workflow file written blind for a host nobody has
# tried would be worse than none. Wire this script into whichever service the repository lives on -
# it needs a JDK that satisfies the pom (25 or newer) and Maven, and it publishes the report itself.
set -e

HERE="$(cd "$(dirname "$0")" && pwd)"
cd "$HERE"

# Maven is not on PATH on the author's machine; anything else can set MVN or put it on PATH.
if [ -z "$MVN" ]; then
    if [ -x "$HOME/Documents/Tools/apache-maven-3.9.11/bin/mvn" ]; then
        MVN="$HOME/Documents/Tools/apache-maven-3.9.11/bin/mvn"
    else
        MVN=mvn
    fi
fi

# A leftover server, bed or ssh tunnel on a display the suite binds makes a test fail as if it were
# broken - `BindException: Address already in use` from one, and from another (a tunnel to the
# reference display, which answers for the server under test) a window id one off, which reads as a
# focus bug. Four runs were lost to exactly that on 2026-09-26, once *after* the trap was written
# down, so the script refuses to start instead. The displays the suite binds are in
# tools/README.md: 10 and 62-99.
leftovers=""
for port in 6010 $(seq 6062 6099); do
    if command -v lsof >/dev/null 2>&1 && lsof -nP -iTCP:"$port" -sTCP:LISTEN >/dev/null 2>&1; then
        leftovers="$leftovers $port"
    fi
done
if [ -n "$leftovers" ]; then
    echo "ci.sh: something is listening on a display the suite binds:$leftovers" >&2
    echo "        a leftover server, bed or tunnel makes a test fail as if it were broken;" >&2
    echo "        stop it and run again (tools/README.md, \"Display numbers\")" >&2
    exit 2
fi

# -B for the plain log a CI log viewer can read, and no -o: a runner's local repository starts empty,
# so the dependencies have to be fetchable. (On this machine `-o` works because the author's is warm;
# a runner that cannot reach the network needs its repository seeded instead.)
if [ -n "$MVN_OPTS" ]; then
    "$MVN" -B test -DargLine="$MVN_OPTS" "$@"
else
    "$MVN" -B test "$@"
fi

echo
echo "surefire report: $HERE/target/surefire-reports/"
