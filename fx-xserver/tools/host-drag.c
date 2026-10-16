// A left-button drag from one point to another, as a user's hand makes it.
#include <ApplicationServices/ApplicationServices.h>
#include <stdlib.h>
#include <unistd.h>
int main(int argc, char **argv) {
    if (argc < 5) { fprintf(stderr, "drag x1 y1 x2 y2\n"); return 2; }
    double x1 = atof(argv[1]), y1 = atof(argv[2]), x2 = atof(argv[3]), y2 = atof(argv[4]);
    CGEventRef e;
    e = CGEventCreateMouseEvent(NULL, kCGEventMouseMoved, CGPointMake(x1, y1), kCGMouseButtonLeft);
    CGEventPost(kCGHIDEventTap, e); CFRelease(e); usleep(400000);
    e = CGEventCreateMouseEvent(NULL, kCGEventLeftMouseDown, CGPointMake(x1, y1), kCGMouseButtonLeft);
    CGEventPost(kCGHIDEventTap, e); CFRelease(e); usleep(400000);
    for (int i = 1; i <= 24; i++) {
        CGPoint p = CGPointMake(x1 + (x2 - x1) * i / 24.0, y1 + (y2 - y1) * i / 24.0);
        e = CGEventCreateMouseEvent(NULL, kCGEventLeftMouseDragged, p, kCGMouseButtonLeft);
        CGEventPost(kCGHIDEventTap, e); CFRelease(e); usleep(25000);
    }
    e = CGEventCreateMouseEvent(NULL, kCGEventLeftMouseUp, CGPointMake(x2, y2), kCGMouseButtonLeft);
    CGEventPost(kCGHIDEventTap, e); CFRelease(e);
    return 0;
}
