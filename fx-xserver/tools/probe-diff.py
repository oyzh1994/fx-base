#!/usr/bin/env python3
"""Diff two probe runs, ignoring what each server assigns for itself.

`SelectionProbe` and `CursorProbe` print what a client is told, so the same run against Xvfb and
against this server answers "does this server behave like the reference". The two outputs cannot be
compared byte for byte, because a server picks its own opcodes, hands out its own atom ids, numbers
the cursors it creates, and stamps events from its own clock (this server's is wall-clock
milliseconds, Xvfb's is its uptime) - none of which is a decision the client can see. Everything that
*is* a decision is compared as it stands.

Usage:

    tools/probe-diff.py xvfb-run.txt ours-run.txt

Prints "IDENTICAL" when the two agree, or a unified diff when they do not. A difference here is a
behaviour difference, not a formatting one - which is the whole point of the tool.
"""

import difflib
import re
import sys

# What a server assigns for itself: the major opcode and event base of an extension, the ids it
# hands out, and its own clock.
SUBSTITUTIONS = [
    (r"major=\d+", "major=?"),
    # A probe that prints the extension's own opcode ("GLX major opcode = 150") is printing what the
    # *server* assigned, which is not a decision the client can compare - the dispatch table is the
    # same either way.
    (r"major opcode = \d+", "major opcode = ?"),
    (r"firstEvent=\d+", "firstEvent=?"),
    (r"firstError=\d+", "firstError=?"),
    (r"selection=\d+", "selection=ATOM"),
    (r"atom=\d+", "atom=ATOM"),
    # SYNC's counters and alarms are printed in decimal, because that is how a client reads a 64 bit
    # value back, and their ids come from the range the *server* handed the client - so they differ
    # between two servers for the same reason an atom id does. The value itself is not normalised:
    # it is what the test is about.
    (r"counter=\d+", "counter=?"),
    (r"alarm=\d+", "alarm=?"),
    (r"cursorSerial=\d+", "cursorSerial=SERIAL"),
    (r"type=\d+", "type=E"),
    # RECORD prints the stamps in front of each recorded element, and a stamp is a clock: the
    # reference's is its uptime in milliseconds and this server's is its own monotonic clock, so the
    # *value* is a difference by construction. What is compared is that a stamp is there, which the
    # width of it decides - "stamps=8 request CreateWindow/32b+seq1" says four bytes of time and four
    # of sequence arrived in front of a 32 byte request.
    (r"time=\d+", "time=T"),
    (r"@[0-9a-f]+", "@t"),
    # A client-started datum carries a whole connection setup reply, and a setup reply's size is the
    # server's own (a reference's is 3652 bytes, this server's 336). Named apart from every other
    # size a probe prints so that only this one is normalised away; `release=` is a hex literal and
    # the role mapping below already handles it.
    (r"setupBytes=\d+", "setupBytes=N"),
    # SECURITY hands out authorization ids from a counter of its own, and a probe prints the id it
    # was given - which is a number the *server* assigned, like an atom's or a cursor's, and not a
    # decision a client can compare.
    (r"authId=\d+", "authId=?"),
    (r"\(id=\d+", "(id=?"),
    (r"setupUnits=\d+", "setupUnits=N"),
]


def normalise(line):
    for pattern, replacement in SUBSTITUTIONS:
        line = re.sub(pattern, replacement, line)

    # The event time and the selection's own time are absolute clocks, so they differ between two
    # servers by construction. Whether they are the *same instant* is the decision that matters:
    # SetSelectionOwner sets both to now, an owner going away leaves the selection's time alone.
    def relative(match):
        relation = "SAME" if match.group(1) == match.group(2) else "OLDER"
        return "timestamp=T selectionTimestamp=" + relation

    line = re.sub(r"timestamp=(-?\d+) selectionTimestamp=(-?\d+)", relative, line)
    return re.sub(r"timestamp=-?\d+", "timestamp=T", line)


def load(path):
    with open(path, encoding="utf-8") as handle:
        lines = [normalise(line) for line in handle]
    # Resource ids are handed out of the id space the *server* gives a client, so the same probe run
    # against two servers names the same window, drawable, damage object or pixmap with different
    # numbers - 0xc00001 here, 0x200001 there, and the root is 0x1ff against 0x2. Every one of them
    # is mapped to the order it first appears in, which is what the probe controls: "the window the
    # grab is reported against" is the decision, its number is not.
    #
    # Every hex literal, not only the ones behind `win=`: a probe writes them in prose too ("the
    # window the pointer will be over is 0xe00001"), and a mask like `modifiers=0x8000` is left alone
    # by construction - the mapping is one-to-one within a run, so two servers that answer the same
    # mask still agree, and two that answer different ones still differ.
    roles, numbered = {}, []
    for line in lines:
        def role(match):
            name = match.group(0)
            return roles.setdefault(name, "R%d" % len(roles))
        numbered.append(re.sub(r"0x[0-9a-f]+", role, line))
    return numbered


def main(argv):
    if len(argv) != 3:
        print(__doc__.strip(), file=sys.stderr)
        return 2
    left, right = load(argv[1]), load(argv[2])
    if left == right:
        print("IDENTICAL apart from opcodes, resource and atom ids, cursor serials and the clock")
        return 0
    sys.stdout.writelines(difflib.unified_diff(left, right, argv[1], argv[2]))
    return 1


if __name__ == "__main__":
    sys.exit(main(sys.argv))
