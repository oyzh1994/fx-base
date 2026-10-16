"""Generates CursorFont.java from the X cursor font that ships with the VM's X11 fonts.

The font's own copyright line says it: "These glyphs are unencumbered" - which is why they can be
embedded here at all, and why a client that asks for XC_left_ptr can be given the real arrow rather
than an approximation.
"""
import gzip
import sys

from PIL import PcfFontFile

FONT = "/usr/share/fonts/X11/misc/cursor.pcf.gz"

# The shapes a client actually asks for, with the name the cursorfont header gives them.
SHAPES = [
    (0, "XC_X_cursor"),
    (2, "XC_arrow"),
    (4, "XC_based_arrow_down"),
    (6, "XC_based_arrow_up"),
    (8, "XC_boat"),
    (10, "XC_bogosity"),
    (12, "XC_bottom_left_corner"),
    (14, "XC_bottom_right_corner"),
    (16, "XC_bottom_side"),
    (18, "XC_bottom_tee"),
    (20, "XC_box_spiral"),
    (22, "XC_center_ptr"),
    (24, "XC_circle"),
    (26, "XC_clock"),
    (28, "XC_coffee_mug"),
    (30, "XC_cross"),
    (32, "XC_cross_reverse"),
    (34, "XC_crosshair"),
    (36, "XC_diamond_cross"),
    (38, "XC_dot"),
    (40, "XC_dotbox"),
    (42, "XC_double_arrow"),
    (44, "XC_draft_large"),
    (46, "XC_draft_small"),
    (50, "XC_exchange"),
    (52, "XC_fleur"),
    (54, "XC_gobbler"),
    (56, "XC_gumby"),
    (58, "XC_hand1"),
    (60, "XC_hand2"),
    (62, "XC_heart"),
    (64, "XC_icon"),
    (66, "XC_iron_cross"),
    (68, "XC_left_ptr"),
    (70, "XC_left_side"),
    (72, "XC_leftbutton"),
    (74, "XC_ll_angle"),
    (76, "XC_lr_angle"),
    (78, "XC_man"),
    (80, "XC_middlebutton"),
    (82, "XC_mouse"),
    (84, "XC_pencil"),
    (86, "XC_pirate"),
    (88, "XC_plus"),
    (90, "XC_question_arrow"),
    (92, "XC_right_ptr"),
    (94, "XC_right_side"),
    (96, "XC_rightbutton"),
    (98, "XC_rtl_logo"),
    (100, "XC_sailboat"),
    (102, "XC_sb_down_arrow"),
    (104, "XC_sb_h_double_arrow"),
    (106, "XC_sb_left_arrow"),
    (108, "XC_sb_right_arrow"),
    (110, "XC_sb_up_arrow"),
    (112, "XC_sb_v_double_arrow"),
    (114, "XC_shuttle"),
    (116, "XC_sizing"),
    (118, "XC_spider"),
    (120, "XC_spraycan"),
    (122, "XC_star"),
    (124, "XC_target"),
    (126, "XC_tcross"),
    (128, "XC_top_left_arrow"),
    (130, "XC_top_left_corner"),
    (132, "XC_top_right_corner"),
    (134, "XC_top_side"),
    (136, "XC_top_tee"),
    (138, "XC_trek"),
    (140, "XC_ul_angle"),
    (142, "XC_umbrella"),
    (144, "XC_ur_angle"),
    (146, "XC_watch"),
    (148, "XC_xterm"),
]


def bitmap_of(font, code):
    """The glyph's bitmap image, or None when the font has no such character."""
    if code >= len(font.glyph) or font.glyph[code] is None:
        return None
    image = font.glyph[code][3]
    return None if image.size[0] == 0 or image.size[1] == 0 else image


def rows(image):
    """One packed byte per eight pixels, most significant bit first - the X bitmap order."""
    width, height = image.size
    pixels = image.load()
    out = []
    for y in range(height):
        value = 0
        count = 0
        for x in range(width):
            value = (value << 1) | (1 if pixels[x, y] else 0)
            count += 1
            if count == 8:
                out.append(value)
                value = 0
                count = 0
        if count:
            out.append(value << (8 - count))
    return out


def main():
    font = PcfFontFile.PcfFontFile(gzip.open(FONT, "rb"))
    print("package io.github.oyzh1994.xserver.resource;")
    print()
    print("/**")
    print(" * The X cursor font's bitmaps, so a client that asks for a cursor shape gets it.")
    print(" *")
    print(" * <p>{@code XCreateFontCursor} is {@code XCreateGlyphCursor} on the font called \"cursor\": a")
    print(" * shape is a character, and its mask is the character after it. The server used to answer")
    print(" * that request with a filled cell - a solid rectangle under the pointer where every X")
    print(" * application draws an arrow, an I-beam or a resize handle.")
    print(" *")
    print(" * <p>Generated from the font itself (`cursor.pcf`, whose copyright line reads \"These")
    print(" * glyphs are unencumbered\") by `tools` - do not edit by hand. The hotspot is the X cursor")
    print(" * font's own convention: the glyph's left side bearing and ascent.")
    print(" */")
    print("final class CursorFont {")
    print()
    print("    /** One shape: its source and mask bitmaps, both packed one bit per pixel, MSB first. */")
    print("    record Glyph(int width, int height, int hotspotX, int hotspotY, byte[] source,")
    print("                 byte[] mask) {")
    print("    }")
    print()
    print("    private CursorFont() {")
    print("    }")
    print()
    print("    /** The shape, or null when it is not one this table carries. */")
    print("    static Glyph glyph(int shape) {")
    print("        return switch (shape) {")
    for code, name in SHAPES:
        # PIL's glyph entry is (side bearings, (xoffset, yoffset, width, height), (ascent, descent,
        # attributes), bitmap) - the bitmap is last, and the metrics are the X cursor font's own
        # hotspot convention away from being a hotspot.
        source = bitmap_of(font, code)
        if source is None:
            continue
        metrics = font.glyph[code]
        xoffset = metrics[1][0]
        width = metrics[1][2]
        height = metrics[1][3]
        ascent = metrics[2][0]
        mask = bitmap_of(font, code + 1)
        if mask is None or mask.size != source.size:
            mask = source
        hotspot_x = -xoffset
        hotspot_y = ascent
        print(f"            // {name}")
        print(f"            case {code} -> new Glyph({width}, {height}, {hotspot_x}, {hotspot_y},")
        print(f"                    bytes({', '.join(str(b) for b in rows(source))}),")
        print(f"                    bytes({', '.join(str(b) for b in rows(mask))}));")
    print("            default -> null;")
    print("        };")
    print("    }")
    print()
    print("    private static byte[] bytes(int... values) {")
    print("        byte[] data = new byte[values.length];")
    print("        for (int i = 0; i < values.length; i++) {")
    print("            data[i] = (byte) values[i];")
    print("        }")
    print("        return data;")
    print("    }")
    print("}")


main()
