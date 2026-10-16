package cn.oyzh.fx.xserver.input;

import cn.oyzh.fx.xserver.input.Keymap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The keyboard mapping is the only thing a client has to resolve a key event with.
 *
 * <p>{@code GetKeyboardMapping} tells a client which keysym each keycode carries, and that is what
 * it reports when the keycode arrives - so a letter placed on the wrong keycode is typed as
 * whatever letter that keycode really carries. US keycodes are numbered by the physical key's
 * position rather than alphabetically, which makes the letters from {@code s} to {@code k} the
 * ones a table built by counting off from {@code a} silently gets wrong: they all land six
 * keycodes early, so {@code s} arrives as {@code b}.
 */
public class KeymapTest {

    @Test
    void lettersSitOnTheKeycodesAClientExpects() {
        int[][] expected = {
                {'a', 38}, {'b', 56}, {'c', 54}, {'d', 40}, {'e', 26}, {'f', 41}, {'g', 42},
                {'h', 43}, {'i', 31}, {'j', 44}, {'k', 45}, {'l', 46}, {'m', 58}, {'n', 57},
                {'o', 32}, {'p', 33}, {'q', 24}, {'r', 27}, {'s', 39}, {'t', 28}, {'u', 30},
                {'v', 55}, {'w', 25}, {'x', 53}, {'y', 29}, {'z', 52},
        };
        for (int[] entry : expected) {
            char letter = (char) entry[0];
            assertEquals(entry[1], Keymap.keycodeForLetter(letter),
                    "keycode of '" + letter + "'");
            assertEquals(entry[1], Keymap.keycodeForLetter(Character.toUpperCase(letter)),
                    "a shifted letter names the same key");
        }
    }

    @Test
    void everyLetterReadsBackAsItself() {
        // The property a client sees: the keycode a letter is typed as carries that letter, and
        // its shifted level carries the capital. Broken by a mapping that is merely a permutation
        // of the right keycodes as much as by one that drops a letter.
        for (char letter = 'a'; letter <= 'z'; letter++) {
            int keycode = Keymap.keycodeForLetter(letter);
            assertEquals(letter, Keymap.keysym(keycode, 0), "unshifted '" + letter + "'");
            assertEquals(letter - 32, Keymap.keysym(keycode, 1), "shifted '" + letter + "'");
        }
    }

    @Test
    void lettersAreNotClaimedByPunctuationOrByEachOther() {
        for (char punctuation : new char[] {';', '\'', '`', '\\', ',', '.', '/', '-', '=', '[',
                ']'}) {
            assertEquals(0, Keymap.keycodeForLetter(punctuation),
                    "'" + punctuation + "' is punctuation, not a letter");
        }
        // Every letter needs its own keycode, or one of them is unreachable.
        boolean[] used = new boolean[Keymap.MAX_KEYCODE + 1];
        for (char letter = 'a'; letter <= 'z'; letter++) {
            int keycode = Keymap.keycodeForLetter(letter);
            assertFalse(used[keycode], "keycode " + keycode + " carries two letters");
            used[keycode] = true;
        }
    }

    /**
     * The modifier rows follow the keymap in force, because a modifier map names keycodes and a
     * keycode only means something next to the keymap it was read from.
     *
     * <p>Measured against a real client (2026-09-28, {@code xmodmap -pm} on both servers): this
     * server's rows were a hand-written American arrangement whose Mod3 was empty and whose Mod5 held
     * Scroll_Lock - so <b>AltGr was in no row at all</b>, and a client that binds {@code Mod5} for a
     * key never matched. A captured layout is a reference's own table, so it brings that reference's
     * rows: an evdev arrangement, where the key above the right Shift is AltGr and the four extra
     * keys above 200 are modifiers too.
     */
    @Test
    void theModifierRowsFollowTheKeymapInForce() {
        try {
            Keymap.useBuiltinLayout();
            assertArrayEquals(new int[] {78}, Keymap.modifierRow(7),
                    "the built-in map's Mod5 is Scroll_Lock");
            assertArrayEquals(new int[] {}, Keymap.modifierRow(5), "and its Mod3 is empty");
            assertEquals(0, Keymap.modifierMaskForKeycode(92),
                    "keycode 92 is KP_Enter in that keymap, not a modifier");
            assertNotEquals(0, Keymap.modifierMaskForKeycode(64), "Alt is Mod1 either way");

            assertTrue(Keymap.useLayout("de"), "the captured German layout");
            assertArrayEquals(new int[] {92}, Keymap.modifierRow(7), "an evdev Mod5 is AltGr");
            assertArrayEquals(new int[] {203}, Keymap.modifierRow(5),
                    "and Mod3 is the ISO level-5 shift");
            assertNotEquals(0, Keymap.modifierMaskForKeycode(92),
                    "so a client's Mod5 binding matches the key above the right Shift");
            assertNotEquals(0, Keymap.modifierMaskForKeycode(204),
                    "and the extra evdev Alt is a modifier");
        } finally {
            Keymap.useBuiltinLayout();
        }
    }
}
