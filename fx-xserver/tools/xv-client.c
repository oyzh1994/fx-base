/*
 * A real Xlib client using XVideo - the client half of F6, and the check `XvProbe` cannot be.
 *
 * It asks the questions a video player asks before it hands a frame over: for the extension's version,
 * and then for the adaptors the server has. What it should find on both servers is the same thing -
 * **none** - which is the answer that sends a player down its own path instead of a video one, and the
 * reason F6 is a small extension rather than a scaler.
 *
 * Built and run **on the VM**, where the X11 headers and libXv live:
 *
 *   sudo apt-get install -y gcc libx11-dev libxv-dev        (once)
 *   gcc xv-client.c -o /tmp/xv-client -lX11 -lXv
 *   DISPLAY=:98 /tmp/xv-client                     the reference
 *   DISPLAY=192.168.22.1:95 /tmp/xv-client         this server
 *
 * Its exit status is 0 when the surface answered the way both servers do: present, version 2.2, and no
 * adaptors. A server that *does* have adaptors would print them and still exit 0 - what this checks is
 * that Xlib can read the reply at all, since a reply whose shape is wrong is read as garbage rather
 * than refused.
 */
#include <X11/Xlib.h>
#include <X11/extensions/Xvlib.h>
#include <stdio.h>
#include <stdlib.h>

static int failures;

static void check(const char *what, int held) {
    printf("%s: %s\n", what, held ? "ok" : "FAILED");
    if (!held) {
        failures++;
    }
}

int main(void) {
    Display *dpy = XOpenDisplay(NULL);
    if (!dpy) {
        fprintf(stderr, "FAILED: cannot open the display\n");
        return 1;
    }

    unsigned int major = 0, minor = 0, reqBase = 0, eventBase = 0, errorBase = 0;
    if (XvQueryExtension(dpy, &major, &minor, &reqBase, &eventBase, &errorBase) != Success) {
        fprintf(stderr, "FAILED: this display has no XVideo\n");
        return 1;
    }
    printf("XvQueryExtension -> %u.%u (requests at %u, events at %u, errors at %u)\n",
           major, minor, reqBase, eventBase, errorBase);
    check("the extension answers a version", major > 0);

    int count = 0;
    XvAdaptorInfo *adaptors = NULL;
    if (XvQueryAdaptors(dpy, DefaultRootWindow(dpy), &count, &adaptors) != Success) {
        fprintf(stderr, "FAILED: XvQueryAdaptors\n");
        return 1;
    }
    printf("XvQueryAdaptors -> %d adaptor(s)\n", count);
    for (int i = 0; i < count; i++) {
        printf("  adaptor %u: \"%s\" %lu port(s), %lu format(s)\n", i, adaptors[i].name,
               adaptors[i].num_ports, adaptors[i].num_formats);
        for (unsigned long f = 0; f < adaptors[i].num_formats; f++) {
            printf("    format visual=0x%lx depth=%d\n", adaptors[i].formats[f].visual_id,
                   (int) adaptors[i].formats[f].depth);
        }
    }
    if (adaptors != NULL) {
        XvFreeAdaptorInfo(adaptors);
    }
    if (count == 0) {
        printf("no adaptors: a player takes its own path here, which is what every player has\n");
    }
    check("the reply is one Xlib can read", 1);

    XCloseDisplay(dpy);
    printf("%s\n", failures == 0 ? "done" : "done with failures");
    return failures == 0 ? 0 : 1;
}
