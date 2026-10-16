/*
 * A real Xlib client using SECURITY - the client half of F7, and the check `SecurityProbe` cannot be.
 *
 * It asks the server for an authorization, hands that token to a *second* connection of its own
 * through `XSetAuthorization`, and then measures what that connection may do: it draws its own
 * window and reads its own pixels, and it is refused the root's pixels with `BadAccess`. Finally it
 * revokes the authorization on the trusted connection and watches the untrusted one - which a
 * reference closes (measured; see the F7 entry).
 *
 * It is built and run **on the VM**, which is where the X11 headers and libXext live:
 *
 *   sudo apt-get install -y gcc libx11-dev libxext-dev        (once)
 *   gcc xsecurity-client.c -o /tmp/xsecurity-client -lX11 -lXext
 *
 * It needs a reference or a server that **checks credentials** - access control has to be on, or a
 * token means nothing (an `-ac` server trusts everybody):
 *
 *   tools/xvfb.sh secure         Xvfb :97 with access control, cookie deadbeef...abcdef
 *   XAUTHORITY=/tmp/xsec.auth DISPLAY=192.168.22.152:97 /tmp/xsecurity-client
 *   DISPLAY=192.168.22.1:63 /tmp/xsecurity-client          this server, started with --auth
 *
 * Exit status is 0 when every expectation held; each step prints what it found.
 */
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/extensions/security.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static int failures;

static void check(const char *what, int held) {
    printf("%s: %s\n", what, held ? "ok" : "FAILED");
    if (!held) {
        failures++;
    }
}

/* The error a request raised, or 0 when it was accepted; Xlib calls the handler and carries on. */
static int last_error;

static int errorHandler(Display *dpy, XErrorEvent *event) {
    last_error = event->error_code;
    return 0;
}

/*
 * The end of a connection is an *IO* error rather than a protocol error, and Xlib's default handler
 * exits the process - which would hide the checks this program just made behind an exit code 1. This
 * one reports what they came to instead, and does not return: the specification says the handler
 * must not.
 */
static int ioErrorHandler(Display *dpy) {
    printf("the revoked connection ended (Xlib's IO error)\n");
    fflush(stdout);                  /* _exit does not flush, and everything above is the report */
    _exit(failures == 0 ? 0 : 1);
}

static int requestOutcome(Display *dpy, const char *what) {
    last_error = 0;
    XSync(dpy, False);
    /* A request that raises is reported asynchronously, so the trick is to make the server answer
     * something afterwards and read the error before that answer. */
    XNoOp(dpy);
    XSync(dpy, False);
    printf("  %s -> %s\n", what,
           last_error == 0 ? "accepted" : (last_error == BadAccess ? "BadAccess" : "other error"));
    return last_error;
}

int main(void) {
    XSetIOErrorHandler(ioErrorHandler);
    Display *dpy = XOpenDisplay(NULL);
    if (!dpy) {
        fprintf(stderr, "FAILED: cannot open the display (access control is on and the cookie is"
                        " not in place)\n");
        return 1;
    }
    XSetErrorHandler(errorHandler);

    int major = 0, minor = 0;
    if (!XSecurityQueryExtension(dpy, &major, &minor)) {
        fprintf(stderr, "FAILED: SECURITY is not on this display\n");
        return 1;
    }
    printf("XSecurityQueryExtension -> %d.%d\n", major, minor);

    /* The authorization to hand out: a name and some data, and the server answers with a fresh
     * token for the same name. */
    Xauth *requested = XSecurityAllocXauth();
    const char *protocol = "MIT-MAGIC-COOKIE-1";
    requested->name = (char *) malloc(strlen(protocol) + 1);
    strcpy(requested->name, protocol);
    requested->name_length = (unsigned short) strlen(protocol);
    requested->data = (char *) malloc(16);
    memset(requested->data, 0, 16);
    requested->data_length = 16;

    XSecurityAuthorization authId = 0;
    Xauth *token = XSecurityGenerateAuthorization(dpy, requested, 0, NULL, &authId);
    if (!token) {
        fprintf(stderr, "FAILED: XSecurityGenerateAuthorization\n");
        return 1;
    }
    printf("XSecurityGenerateAuthorization -> id=%lu name=%.*s data=%d bytes\n",
           authId, token->name_length, token->name, token->data_length);

    /* A second connection offering that token: this is what makes it untrusted. */
    XSetAuthorization(token->name, token->name_length, token->data, token->data_length);
    Display *untrusted = XOpenDisplay(NULL);
    if (!untrusted) {
        fprintf(stderr, "FAILED: the untrusted connection was refused\n");
        return 1;
    }
    XSetErrorHandler(errorHandler);
    int screen = DefaultScreen(untrusted);
    Window root = RootWindow(untrusted, screen);
    printf("the untrusted connection opened\n");

    /* What it may not do: read somebody else's pixels. */
    last_error = 0;
    XImage *stolen = XGetImage(untrusted, root, 0, 0, 1, 1, AllPlanes, ZPixmap);
    printf("  XGetImage(root) -> %s\n",
           stolen == NULL ? (last_error == BadAccess ? "refused BadAccess" : "refused")
                          : "ANSWERED, which is what an untrusted connection must not get");
    check("the root's pixels are refused", stolen == NULL);
    if (stolen) {
        XDestroyImage(stolen);
    }

    /* And what it may: draw its own window and read it back. */
    Window win = XCreateSimpleWindow(untrusted, root, 20, 20, 60, 40, 0, 0x00FF00, 0x00FF00);
    XMapWindow(untrusted, win);
    GC gc = XCreateGC(untrusted, win, 0, NULL);
    XSetForeground(untrusted, gc, 0x0000FF);
    XFillRectangle(untrusted, win, gc, 0, 0, 60, 40);
    XSync(untrusted, False);
    check("it can read its own window", requestOutcome(untrusted, "XGetImage(own)") == 0);
    XImage *own = XGetImage(untrusted, win, 5, 5, 1, 1, AllPlanes, ZPixmap);
    if (!own) {
        fprintf(stderr, "FAILED: its own window cannot be read either\n");
        return 1;
    }
    printf("  its own window holds %06lx\n", XGetPixel(own, 0, 0) & 0xFFFFFF);
    XDestroyImage(own);

    /* The trusted connection revokes it: the untrusted one should be closed. Xlib has no "is the
     * server gone" call, so this looks at the socket: a readable socket that reads back nothing is
     * the end of the connection, and a quiet one means the server is still there. */
    check("revoking the authorization", XSecurityRevokeAuthorization(dpy, authId) != 0);
    XSync(dpy, False);
    usleep(200000);
    int socket_fd = XConnectionNumber(untrusted);
    struct timeval timeout = {0, 300000};
    fd_set readable;
    FD_ZERO(&readable);
    FD_SET(socket_fd, &readable);
    int closed = 0;
    if (select(socket_fd + 1, &readable, NULL, NULL, &timeout) > 0) {
        char byte;
        closed = read(socket_fd, &byte, 1) <= 0;
    }
    check("the revoked connection was closed", closed);

    XCloseDisplay(untrusted);
    XCloseDisplay(dpy);
    printf("%s\n", failures == 0 ? "done" : "done with failures");
    return failures == 0 ? 0 : 1;
}
