// Types a string into the frontmost window, as a keyboard that had every character on it.
//
// A CGEvent carries a keycode, which cannot express a character no key has - so the string goes in
// as a Unicode string on the event instead, which is what the platform's own input tools do. What
// arrives at the application is the text, without an input method in the middle: the point of this
// tool is to drive the server's composed-character path (backlog item C10) from a script.
//
//   clang -o /tmp/type tools/host-type.c -framework ApplicationServices
//   /tmp/type "你好"
#include <ApplicationServices/ApplicationServices.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

int main(int argc, char **argv) {
    if (argc < 2) {
        fprintf(stderr, "usage: type <text>\n");
        return 2;
    }
    CFStringRef text = CFStringCreateWithCString(NULL, argv[1], kCFStringEncodingUTF8);
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
        CGEventPost(kCGHIDEventTap, down);
        CFRelease(down);
        usleep(30000);
        CGEventRef up = CGEventCreateKeyboardEvent(NULL, 0, false);
        CGEventKeyboardSetUnicodeString(up, 1, &characters[i]);
        CGEventPost(kCGHIDEventTap, up);
        CFRelease(up);
        usleep(30000);
    }
    printf("typed %ld characters\n", (long) length);
    return 0;
}
