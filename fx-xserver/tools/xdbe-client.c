/*
 * A real Xlib client using DOUBLE-BUFFER - the client half of F5, and the check `DbeProbe` cannot be.
 *
 * The probe speaks the wire format this server was written from; this speaks **Xlib**, so it is
 * Xlib's own readers that parse the replies (GetVisualInfo's per-drawable lists are the ones worth
 * this: a shape that is wrong there is read as garbage rather than refused) and Xlib's own request
 * builders that drive the swap. It is what a toolkit does, minus the toolkit.
 *
 * It is built and run **on the VM**, which is where the X11 headers and libXext live:
 *
 *   sudo apt-get install -y gcc libx11-dev libxext-dev        (once)
 *   gcc xdbe-client.c -o /tmp/xdbe-client -lX11 -lXext
 *   DISPLAY=:98 /tmp/xdbe-client                  the reference (the VM's own Xvfb)
 *   DISPLAY=192.168.22.1:61 /tmp/xdbe-client      this server, on the Mac
 *
 * Both runs print the same thing except the count of visuals: 144 on the reference's screen, 7 on
 * this server's, which is the screen's own answer rather than a difference in the extension.
 *
 * Exit status is 0 when every call succeeded, and every failure says which call it was. It found one
 * real difference the probe had not: with `num_screens` left at zero - a request that names no
 * drawable at all - Xlib failed against this server while the reference answered one screen, so a
 * request carrying none means "the display's screens" and not "nothing" (measured, and now answered
 * the same way here; `DbeProbe` prints that case, and `DbeTest`'s visual-info case pins the section
 * per drawable).
 */
#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/extensions/Xdbe.h>
#include <stdio.h>
#include <stdlib.h>

static void die(const char *what) {
    fprintf(stderr, "FAILED: %s\n", what);
    exit(1);
}

static unsigned long pixel(Display *dpy, Drawable drawable, int x, int y) {
    XImage *image = XGetImage(dpy, drawable, x, y, 1, 1, AllPlanes, ZPixmap);
    if (!image) die("XGetImage");
    unsigned long value = XGetPixel(image, 0, 0) & 0xFFFFFF;
    XDestroyImage(image);
    return value;
}

int main(void) {
    Display *dpy = XOpenDisplay(NULL);
    if (!dpy) die("cannot open the display");

    int major = 0, minor = 0;
    if (!XdbeQueryExtension(dpy, &major, &minor)) die("XdbeQueryExtension says no");
    printf("XdbeQueryExtension -> %d.%d\n", major, minor);

    int screen = DefaultScreen(dpy);
    Window root = RootWindow(dpy, screen);

    /* The count of drawables is passed *in* (Xlib sends it as the request's n) and the server
     * answers one section per drawable. */
    int screens = 1;
    XdbeScreenVisualInfo *visorinfo = XdbeGetVisualInfo(dpy, &root, &screens);
    if (!visorinfo) die("XdbeGetVisualInfo failed");
    printf("XdbeGetVisualInfo -> %d screen(s)\n", screens);
    if (screens > 0 && visorinfo[0].count > 0) {
        printf("  screen 0: %d visual(s), first visual=0x%lx depth=%d perfLevel=%d\n",
               visorinfo[0].count, (unsigned long)visorinfo[0].visinfo[0].visual,
               visorinfo[0].visinfo[0].depth, visorinfo[0].visinfo[0].perflevel);
    }
    XdbeFreeVisualInfo(visorinfo);

    Window win = XCreateSimpleWindow(dpy, root, 20, 20, 80, 60, 0, 0x00FF00, 0x00FF00);
    XMapWindow(dpy, win);
    XSync(dpy, False);

    GC gc = XCreateGC(dpy, win, 0, NULL);
    XSetForeground(dpy, gc, 0x0000FF);

    XdbeBackBuffer buffer = XdbeAllocateBackBufferName(dpy, win, XdbeCopied);
    printf("XdbeAllocateBackBufferName -> 0x%lx\n", (unsigned long)buffer);

    XdbeBackBufferAttributes *attrs = XdbeGetBackBufferAttributes(dpy, buffer);
    if (!attrs) die("XdbeGetBackBufferAttributes failed");
    printf("XdbeGetBackBufferAttributes -> window 0x%lx (isTheWindow=%d)\n",
           (unsigned long)attrs->window, attrs->window == win);
    XFree(attrs);

    /* The back buffer is a drawable: Xlib's ordinary calls address it by its own id. */
    XFillRectangle(dpy, buffer, gc, 0, 0, 80, 60);
    XSync(dpy, False);
    printf("after drawing into the buffer: window=%06lx buffer=%06lx\n",
           pixel(dpy, win, 5, 5), pixel(dpy, buffer, 5, 5));

    XdbeSwapInfo info;
    info.swap_window = win;
    info.swap_action = XdbeCopied;
    if (!XdbeSwapBuffers(dpy, &info, 1)) die("XdbeSwapBuffers failed");
    XSync(dpy, False);
    printf("after XdbeSwapBuffers: window=%06lx buffer=%06lx\n",
           pixel(dpy, win, 5, 5), pixel(dpy, buffer, 5, 5));

    XdbeBeginIdiom(dpy);
    XdbeEndIdiom(dpy);
    XSync(dpy, False);
    printf("the idioms were accepted\n");

    XdbeDeallocateBackBufferName(dpy, buffer);
    XSync(dpy, False);
    printf("XdbeDeallocateBackBufferName -> ok\n");

    XFreeGC(dpy, gc);
    XDestroyWindow(dpy, win);
    XCloseDisplay(dpy);
    printf("done\n");
    return 0;
}
