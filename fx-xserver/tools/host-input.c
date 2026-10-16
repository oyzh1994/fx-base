// Input as a hand makes it: move, click, type text, and press a key with a modifier held.
//
//   clang -o host-input host-input.c -framework ApplicationServices
//   host-input pos
//   host-input move <x> <y>
//   host-input click <x> <y> [button]
//   host-input type <text>
//   host-input ctrl <letter>          // keycode of the letter, control held
//
// Coordinates are in the points CGEvent uses, which is what a window's bounds in
// CGWindowListCopyWindowInfo are in too.
#include <ApplicationServices/ApplicationServices.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

static void post(CGEventRef event) {
    CGEventPost(kCGHIDEventTap, event);
    CFRelease(event);
}

static void move(double x, double y) {
    post(CGEventCreateMouseEvent(NULL, kCGEventMouseMoved, CGPointMake(x, y), kCGMouseButtonLeft));
    usleep(120000);
}

int main(int argc, char **argv) {
    if (argc < 2) {
        fprintf(stderr, "usage: hostinput pos|move|click|type|ctrl\n");
        return 2;
    }
    const char *what = argv[1];

    if (!strcmp(what, "pos")) {
        CGEventRef event = CGEventCreate(NULL);
        CGPoint p = CGEventGetLocation(event);
        CFRelease(event);
        printf("%.0f %.0f\n", p.x, p.y);
        return 0;
    }
    if (!strcmp(what, "move")) {
        move(atof(argv[2]), atof(argv[3]));
        return 0;
    }
    if (!strcmp(what, "click")) {
        double x = atof(argv[2]), y = atof(argv[3]);
        int button = argc > 4 ? atoi(argv[4]) : 1;
        CGMouseButton which = button == 3 ? kCGMouseButtonRight : kCGMouseButtonLeft;
        CGEventType down = button == 3 ? kCGEventRightMouseDown : kCGEventLeftMouseDown;
        CGEventType up = button == 3 ? kCGEventRightMouseUp : kCGEventLeftMouseUp;
        move(x, y);
        post(CGEventCreateMouseEvent(NULL, down, CGPointMake(x, y), which));
        usleep(120000);
        post(CGEventCreateMouseEvent(NULL, up, CGPointMake(x, y), which));
        usleep(200000);
        return 0;
    }
    if (!strcmp(what, "type")) {
        CFStringRef text = CFStringCreateWithCString(NULL, argv[2], kCFStringEncodingUTF8);
        if (text == NULL) {
            fprintf(stderr, "not valid UTF-8\n");
            return 2;
        }
        CFIndex length = CFStringGetLength(text);
        UniChar *characters = malloc(length * sizeof(UniChar));
        CFStringGetCharacters(text, CFRangeMake(0, length), characters);
        for (CFIndex i = 0; i < length; i++) {
            CGEventRef down = CGEventCreateKeyboardEvent(NULL, 0, true);
            CGEventKeyboardSetUnicodeString(down, 1, &characters[i]);
            post(down);
            usleep(40000);
            CGEventRef up = CGEventCreateKeyboardEvent(NULL, 0, false);
            CGEventKeyboardSetUnicodeString(up, 1, &characters[i]);
            post(up);
            usleep(40000);
        }
        return 0;
    }
    if (!strcmp(what, "key")) {
        // A raw keycode, down or up, with nothing else held: how a modifier key itself is pressed.
        int keycode = atoi(argv[2]);
        int down = argc > 3 && !strcmp(argv[3], "up") ? 0 : 1;
        post(CGEventCreateKeyboardEvent(NULL, (CGKeyCode) keycode, down != 0));
        usleep(80000);
        return 0;
    }
    if (!strcmp(what, "ctrl")) {
        // The Control key itself, held across the letter - which is what a hand does, and what a
        // server that tracks the modifier keys rather than reading a flag needs to see.
        int keycode = atoi(argv[2]);
        post(CGEventCreateKeyboardEvent(NULL, (CGKeyCode) 59, true));
        usleep(120000);
        post(CGEventCreateKeyboardEvent(NULL, (CGKeyCode) keycode, true));
        usleep(80000);
        post(CGEventCreateKeyboardEvent(NULL, (CGKeyCode) keycode, false));
        usleep(80000);
        post(CGEventCreateKeyboardEvent(NULL, (CGKeyCode) 59, false));
        usleep(200000);
        return 0;
    }
    fprintf(stderr, "unknown command %s\n", what);
    return 2;
}
