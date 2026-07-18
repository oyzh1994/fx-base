// Copyright (C) 2010 - 2014 GlavSoft LLC.
// All rights reserved.
//
// -----------------------------------------------------------------------
// This file is part of the TightVNC software.  Please visit our Web site:
//
//                       http://www.tightvnc.com/
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
// -----------------------------------------------------------------------
//
package cn.oyzh.fx.vnc;

import com.glavsoft.rfb.client.KeyEventMessage;
import com.glavsoft.rfb.protocol.Protocol;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

import static com.glavsoft.utils.Keymap.*;

/**
 * JavaFX replacement for KeyEventListener.java.
 * Translates JavaFX KeyEvent to RFB KeyEventMessage using X11 keysyms from Keymap.
 *
 * Critical difference from AWT: In JavaFX, KEY_PRESSED events do NOT carry
 * character data (getCharacter() returns ""). Characters must be derived
 * from the KeyCode and modifier state.
 */
public class VncKeyEventHandler {

    private final Protocol protocol;
    private boolean convertToAscii;
    private VncKeyboardConvertor convertor;

    /** Maps JavaFX KeyCode to X11 keysym for action/non-character keys */
    private static final Map<KeyCode, Integer> KEYCODE_TO_KEYSYM = new HashMap<>();

    /** Maps JavaFX KeyCode to X11 keysym for numpad keys */
    private static final Map<KeyCode, Integer> NUMPAD_TO_KEYSYM = new HashMap<>();

    /** Maps JavaFX KeyCode to US keyboard base character (unshifted) */
    private static final Map<KeyCode, Integer> KEYCODE_TO_CHAR = new HashMap<>();

    static {
        // Function keys
        KEYCODE_TO_KEYSYM.put(KeyCode.F1, K_F1);
        KEYCODE_TO_KEYSYM.put(KeyCode.F2, K_F2);
        KEYCODE_TO_KEYSYM.put(KeyCode.F3, K_F3);
        KEYCODE_TO_KEYSYM.put(KeyCode.F4, K_F4);
        KEYCODE_TO_KEYSYM.put(KeyCode.F5, K_F5);
        KEYCODE_TO_KEYSYM.put(KeyCode.F6, K_F6);
        KEYCODE_TO_KEYSYM.put(KeyCode.F7, K_F7);
        KEYCODE_TO_KEYSYM.put(KeyCode.F8, K_F8);
        KEYCODE_TO_KEYSYM.put(KeyCode.F9, K_F9);
        KEYCODE_TO_KEYSYM.put(KeyCode.F10, K_F10);
        KEYCODE_TO_KEYSYM.put(KeyCode.F11, K_F11);
        KEYCODE_TO_KEYSYM.put(KeyCode.F12, K_F12);

        // Navigation keys
        KEYCODE_TO_KEYSYM.put(KeyCode.HOME, K_HOME);
        KEYCODE_TO_KEYSYM.put(KeyCode.END, K_END);
        KEYCODE_TO_KEYSYM.put(KeyCode.PAGE_UP, K_PAGE_UP);
        KEYCODE_TO_KEYSYM.put(KeyCode.PAGE_DOWN, K_PAGE_DOWN);
        KEYCODE_TO_KEYSYM.put(KeyCode.UP, K_UP);
        KEYCODE_TO_KEYSYM.put(KeyCode.DOWN, K_DOWN);
        KEYCODE_TO_KEYSYM.put(KeyCode.LEFT, K_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.RIGHT, K_RIGHT);
        KEYCODE_TO_KEYSYM.put(KeyCode.INSERT, K_INSERT);
        KEYCODE_TO_KEYSYM.put(KeyCode.DELETE, K_DELETE);
        KEYCODE_TO_KEYSYM.put(KeyCode.ESCAPE, K_ESCAPE);
        KEYCODE_TO_KEYSYM.put(KeyCode.ENTER, K_ENTER);
        KEYCODE_TO_KEYSYM.put(KeyCode.TAB, K_TAB);
        KEYCODE_TO_KEYSYM.put(KeyCode.BACK_SPACE, K_BACK_SPACE);

        // Modifier keys (left variants; right = left + 1)
        KEYCODE_TO_KEYSYM.put(KeyCode.CONTROL, K_CTRL_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.SHIFT, K_SHIFT_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.ALT, K_ALT_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.META, K_META_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.WINDOWS, K_SUPER_LEFT);
        KEYCODE_TO_KEYSYM.put(KeyCode.CONTEXT_MENU, K_HYPER_LEFT);

        // Numpad keys
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD0, K_KP_0);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD1, K_KP_1);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD2, K_KP_2);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD3, K_KP_3);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD4, K_KP_4);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD5, K_KP_5);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD6, K_KP_6);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD7, K_KP_7);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD8, K_KP_8);
        NUMPAD_TO_KEYSYM.put(KeyCode.NUMPAD9, K_KP_9);
        NUMPAD_TO_KEYSYM.put(KeyCode.MULTIPLY, K_KP_MULTIPLY);
        NUMPAD_TO_KEYSYM.put(KeyCode.ADD, K_KP_ADD);
        NUMPAD_TO_KEYSYM.put(KeyCode.SEPARATOR, K_KP_SEPARATOR);
        NUMPAD_TO_KEYSYM.put(KeyCode.SUBTRACT, K_KP_SUBTRACT);
        NUMPAD_TO_KEYSYM.put(KeyCode.DECIMAL, K_KP_DECIMAL);
        NUMPAD_TO_KEYSYM.put(KeyCode.DIVIDE, K_KP_DIVIDE);

        // KeyCode → US keyboard base character (unshifted)
        // Letters
        for (char c = 'A'; c <= 'Z'; c++) {
            KEYCODE_TO_CHAR.put(KeyCode.valueOf(String.valueOf(c)), (int) 'a' + (c - 'A'));
        }
        // Digits
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT0, (int) '0');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT1, (int) '1');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT2, (int) '2');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT3, (int) '3');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT4, (int) '4');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT5, (int) '5');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT6, (int) '6');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT7, (int) '7');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT8, (int) '8');
        KEYCODE_TO_CHAR.put(KeyCode.DIGIT9, (int) '9');
        // Punctuation
        KEYCODE_TO_CHAR.put(KeyCode.SPACE, (int) ' ');
        KEYCODE_TO_CHAR.put(KeyCode.MINUS, (int) '-');
        KEYCODE_TO_CHAR.put(KeyCode.EQUALS, (int) '=');
        KEYCODE_TO_CHAR.put(KeyCode.BACK_SLASH, (int) '\\');
        KEYCODE_TO_CHAR.put(KeyCode.OPEN_BRACKET, (int) '[');
        KEYCODE_TO_CHAR.put(KeyCode.CLOSE_BRACKET, (int) ']');
        KEYCODE_TO_CHAR.put(KeyCode.SEMICOLON, (int) ';');
        KEYCODE_TO_CHAR.put(KeyCode.QUOTE, (int) '\'');
        KEYCODE_TO_CHAR.put(KeyCode.COMMA, (int) ',');
        KEYCODE_TO_CHAR.put(KeyCode.PERIOD, (int) '.');
        KEYCODE_TO_CHAR.put(KeyCode.SLASH, (int) '/');
        KEYCODE_TO_CHAR.put(KeyCode.BACK_QUOTE, (int) '`');
        // Numpad (for character mapping when needed)
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD0, (int) '0');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD1, (int) '1');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD2, (int) '2');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD3, (int) '3');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD4, (int) '4');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD5, (int) '5');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD6, (int) '6');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD7, (int) '7');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD8, (int) '8');
        KEYCODE_TO_CHAR.put(KeyCode.NUMPAD9, (int) '9');
        KEYCODE_TO_CHAR.put(KeyCode.DECIMAL, (int) '.');
        KEYCODE_TO_CHAR.put(KeyCode.DIVIDE, (int) '/');
        KEYCODE_TO_CHAR.put(KeyCode.MULTIPLY, (int) '*');
        KEYCODE_TO_CHAR.put(KeyCode.SUBTRACT, (int) '-');
        KEYCODE_TO_CHAR.put(KeyCode.ADD, (int) '+');
    }

    public VncKeyEventHandler(Protocol protocol) {
        this.protocol = protocol;
        this.convertToAscii = false;
    }

    public void setConvertToAscii(boolean convertToAscii) {
        this.convertToAscii = convertToAscii;
        if (convertToAscii && convertor == null) {
            convertor = new VncKeyboardConvertor();
        }
    }

    public void handleKeyPressed(KeyEvent event) {
        processKeyEvent(event, true);
        event.consume();
    }

    public void handleKeyReleased(KeyEvent event) {
        processKeyEvent(event, false);
        event.consume();
    }

    public void handleKeyTyped(KeyEvent event) {
        event.consume();
    }

    private void processKeyEvent(KeyEvent event, boolean pressed) {
        KeyCode keyCode = event.getCode();

        // Track Caps Lock state
        if (keyCode == KeyCode.CAPS && pressed && convertor != null) {
            convertor.setCapsLockOn(!convertor.isCapsLockOn());
            return;
        }

        if (processModifierKeys(event, pressed)) return;
        if (processSpecialKeys(event, pressed)) return;
        if (processActionKey(event, pressed)) return;

        // Regular character key: derive character from KeyCode + modifier state
        int keyChar = keyCodeToChar(keyCode, event.isShiftDown());

        if (keyChar == 0) {
            return;
        }

        // Handle control-character combinations (Ctrl+A = 0x01, etc.)
        if (keyChar < 0x20) {
            if (event.isControlDown() && keyChar != 0) {
                keyChar += 0x60; // Ctrl-H vs Ctrl-Backspace distinction
            } else {
                switch (keyChar) {
                    case '\b': keyChar = K_BACK_SPACE; break;
                    case '\t': keyChar = K_TAB; break;
                    case '\n':
                    case '\r': keyChar = K_ENTER; break;
                    case 0x1b: keyChar = K_ESCAPE; break;
                    default: break;
                }
            }
        } else if (keyChar == 0x7f) {
            keyChar = K_DELETE;
        } else if (convertToAscii) {
            int awtCode = VncKeyboardConvertor.keyCodeToAwtCode(keyCode);
            keyChar = convertor.convert(keyChar, awtCode, event.isShiftDown());
        } else {
            keyChar = unicode2keysym(keyChar);
        }

        if (keyChar != 0) {
            sendKeyEvent(keyChar, pressed);
        }
    }

    /**
     * Derive the US keyboard character from a KeyCode and shift state.
     * In JavaFX, KEY_PRESSED events don't carry character data (unlike AWT),
     * so we must compute it from the physical key and modifier state.
     */
    private static int keyCodeToChar(KeyCode code, boolean shiftDown) {
        if (code == null) {
            return 0;
        }

        // Letters: A-Z → a-z (unshifted) or A-Z (shifted)
        if (code.isLetterKey()) {
            String name = code.getName();
            if (name.length() == 1) {
                char c = name.charAt(0);
                return shiftDown ? (int) c : (int) Character.toLowerCase(c);
            }
        }

        // Digits: DIGIT0-9 → 0-9 (unshifted) or )!@#$%^&*( (shifted)
        if (code.isDigitKey()) {
            char c = code.getName().charAt(code.getName().length() - 1);
            if (shiftDown) {
                switch (c) {
                    case '1': return '!';
                    case '2': return '@';
                    case '3': return '#';
                    case '4': return '$';
                    case '5': return '%';
                    case '6': return '^';
                    case '7': return '&';
                    case '8': return '*';
                    case '9': return '(';
                    case '0': return ')';
                    default: return c;
                }
            }
            return c;
        }

        // Other keys with fixed character mapping
        Integer ch = KEYCODE_TO_CHAR.get(code);
        if (ch != null) {
            if (!shiftDown) return ch;
            // Shifted versions of common punctuation
            switch (code) {
                case MINUS: return '_';
                case EQUALS: return '+';
                case BACK_SLASH: return '|';
                case OPEN_BRACKET: return '{';
                case CLOSE_BRACKET: return '}';
                case SEMICOLON: return ':';
                case QUOTE: return '"';
                case COMMA: return '<';
                case PERIOD: return '>';
                case SLASH: return '?';
                case BACK_QUOTE: return '~';
                case DIGIT0: return ')';
                case DIGIT1: return '!';
                case DIGIT2: return '@';
                case DIGIT3: return '#';
                case DIGIT4: return '$';
                case DIGIT5: return '%';
                case DIGIT6: return '^';
                case DIGIT7: return '&';
                case DIGIT8: return '*';
                case DIGIT9: return '(';
                default: return ch;
            }
        }

        return 0;
    }

    /**
     * Process modifier keys: Ctrl, Shift, Alt, Meta, Windows, Context Menu.
     */
    private boolean processModifierKeys(KeyEvent event, boolean pressed) {
        Integer keysym = KEYCODE_TO_KEYSYM.get(event.getCode());
        if (keysym == null) return false;

        switch (event.getCode()) {
            case CONTROL:
            case SHIFT:
            case ALT:
            case META:
            case WINDOWS:
            case CONTEXT_MENU:
                break;
            default:
                return false;
        }

        sendKeyEvent(keysym, pressed);
        return true;
    }

    /**
     * Process special keys: AltGr, numpad keys.
     */
    private boolean processSpecialKeys(KeyEvent event, boolean pressed) {
        KeyCode keyCode = event.getCode();

        // AltGr: JavaFX reports UNDEFINED key code with Alt+Ctrl modifiers
        if (keyCode == KeyCode.UNDEFINED && event.isAltDown() && event.isControlDown()) {
            return true;
        }

        // Numpad keys
        Integer numpadKeysym = NUMPAD_TO_KEYSYM.get(keyCode);
        if (numpadKeysym != null) {
            sendKeyEvent(numpadKeysym, pressed);
            return true;
        }

        return false;
    }

    /**
     * Process action keys: F1-F12, Home, End, arrows, Insert, Delete, Escape, Enter, Tab, Backspace.
     */
    private boolean processActionKey(KeyEvent event, boolean pressed) {
        KeyCode keyCode = event.getCode();

        Integer keysym = KEYCODE_TO_KEYSYM.get(keyCode);
        if (keysym == null) {
            return false;
        }

        // Skip modifiers (handled by processModifierKeys)
        switch (keyCode) {
            case CONTROL:
            case SHIFT:
            case ALT:
            case META:
            case WINDOWS:
            case CONTEXT_MENU:
                return false;
            default:
                break;
        }

        sendKeyEvent(keysym, pressed);
        return true;
    }

    private void sendKeyEvent(int keyChar, boolean pressed) {
        protocol.sendMessage(new KeyEventMessage(keyChar, pressed));
    }
}
