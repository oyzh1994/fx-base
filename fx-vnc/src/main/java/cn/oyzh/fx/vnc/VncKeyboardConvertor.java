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

import javafx.scene.input.KeyCode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 面向 JavaFX 的键盘字符转换器，替代原基于 AWT 的实现。
 * 通过可配置的键盘布局参数进行字符转换，而非解析平台相关的扫描码。
 *
 * @author oyzh
 * @since 2026-07-18
 */
public class VncKeyboardConvertor {

    /** 键码到基础/Shift 字符对的映射，键码值采用 AWT 键码 */
    private static final Map<Integer, CodePair> KEY_MAP = new HashMap<>();

    static {
        // Same key map as the original KeyboardConvertor
        KEY_MAP.put(192 /* Back Quote */, new CodePair('`', '~'));
        KEY_MAP.put(49 /* 1 */, new CodePair('1', '!'));
        KEY_MAP.put(50 /* 2 */, new CodePair('2', '@'));
        KEY_MAP.put(51 /* 3 */, new CodePair('3', '#'));
        KEY_MAP.put(52 /* 4 */, new CodePair('4', '$'));
        KEY_MAP.put(53 /* 5 */, new CodePair('5', '%'));
        KEY_MAP.put(54 /* 6 */, new CodePair('6', '^'));
        KEY_MAP.put(55 /* 7 */, new CodePair('7', '&'));
        KEY_MAP.put(56 /* 8 */, new CodePair('8', '*'));
        KEY_MAP.put(57 /* 9 */, new CodePair('9', '('));
        KEY_MAP.put(48 /* 0 */, new CodePair('0', ')'));
        KEY_MAP.put(45 /* Minus */, new CodePair('-', '_'));
        KEY_MAP.put(61 /* Equals */, new CodePair('=', '+'));
        KEY_MAP.put(92 /* Back Slash */, new CodePair('\\', '|'));

        KEY_MAP.put(81 /* Q */, new CodePair('q', 'Q'));
        KEY_MAP.put(87 /* W */, new CodePair('w', 'W'));
        KEY_MAP.put(69 /* E */, new CodePair('e', 'E'));
        KEY_MAP.put(82 /* R */, new CodePair('r', 'R'));
        KEY_MAP.put(84 /* T */, new CodePair('t', 'T'));
        KEY_MAP.put(89 /* Y */, new CodePair('y', 'Y'));
        KEY_MAP.put(85 /* U */, new CodePair('u', 'U'));
        KEY_MAP.put(73 /* I */, new CodePair('i', 'I'));
        KEY_MAP.put(79 /* O */, new CodePair('o', 'O'));
        KEY_MAP.put(80 /* P */, new CodePair('p', 'P'));
        KEY_MAP.put(91 /* Open Bracket */, new CodePair('[', '{'));
        KEY_MAP.put(93 /* Close Bracket */, new CodePair(']', '}'));

        KEY_MAP.put(65 /* A */, new CodePair('a', 'A'));
        KEY_MAP.put(83 /* S */, new CodePair('s', 'S'));
        KEY_MAP.put(68 /* D */, new CodePair('d', 'D'));
        KEY_MAP.put(70 /* F */, new CodePair('f', 'F'));
        KEY_MAP.put(71 /* G */, new CodePair('g', 'G'));
        KEY_MAP.put(72 /* H */, new CodePair('h', 'H'));
        KEY_MAP.put(74 /* J */, new CodePair('j', 'J'));
        KEY_MAP.put(75 /* K */, new CodePair('k', 'K'));
        KEY_MAP.put(76 /* L */, new CodePair('l', 'L'));
        KEY_MAP.put(59 /* Semicolon */, new CodePair(';', ':'));
        KEY_MAP.put(222 /* Quote */, new CodePair('\'', '"'));

        KEY_MAP.put(90 /* Z */, new CodePair('z', 'Z'));
        KEY_MAP.put(88 /* X */, new CodePair('x', 'X'));
        KEY_MAP.put(67 /* C */, new CodePair('c', 'C'));
        KEY_MAP.put(86 /* V */, new CodePair('v', 'V'));
        KEY_MAP.put(66 /* B */, new CodePair('b', 'B'));
        KEY_MAP.put(78 /* N */, new CodePair('n', 'N'));
        KEY_MAP.put(77 /* M */, new CodePair('m', 'M'));
        KEY_MAP.put(44 /* Comma */, new CodePair(',', '<'));
        KEY_MAP.put(46 /* Period */, new CodePair('.', '>'));
        KEY_MAP.put(47 /* Slash */, new CodePair('/', '?'));

        // 105th key on some keyboards
        KEY_MAP.put(60 /* Less */, new CodePair('<', '>'));
    }

    /** 键盘布局语言标识 */
    private final String keyboardLayout;

    /** 大写锁定是否开启 */
    private boolean capsLockOn;

    /**
     * 使用系统默认区域设置构造转换器。
     */
    public VncKeyboardConvertor() {
        this(Locale.getDefault());
    }

    /**
     * 使用指定区域设置构造转换器。
     *
     * @param locale 区域设置，为空时按英文布局处理
     */
    public VncKeyboardConvertor(Locale locale) {
        this.keyboardLayout = locale != null ? locale.getLanguage().toLowerCase() : "en";
        this.capsLockOn = false;
    }

    /**
     * 设置大写锁定状态。
     *
     * @param capsLockOn 大写锁定是否开启
     */
    public void setCapsLockOn(boolean capsLockOn) {
        this.capsLockOn = capsLockOn;
    }

    /**
     * 获取大写锁定状态。
     *
     * @return 大写锁定是否开启
     */
    public boolean isCapsLockOn() {
        return capsLockOn;
    }

    /**
     * 依据键盘布局转换按键字符，当区域设置为德语时处理 QWERTZ 布局的 Y/Z 互换。
     *
     * @param keyChar   原始字符
     * @param keyCode   按键的 AWT 键码
     * @param shiftDown 是否按下 Shift 键
     * @return 转换后的字符
     */
    public int convert(int keyChar, int keyCode, boolean shiftDown) {
        // Handle German QWERTZ Y/Z swap
        CodePair codePair = KEY_MAP.get(keyCode);
        if (codePair == null) {
            return keyChar;
        }

        if ("de".equals(keyboardLayout)) {
            if (keyCode == 90 /* Z */) {
                codePair = KEY_MAP.get(89); /* Y */
            } else if (keyCode == 89 /* Y */) {
                codePair = KEY_MAP.get(90); /* Z */
            }
        }

        boolean isCapsLock = capsLockOn && Character.isLetter(codePair.code);
        boolean useShifted = (shiftDown && !isCapsLock) || (!shiftDown && isCapsLock);

        return useShifted ? codePair.codeShifted : codePair.code;
    }

    /**
     * 将 JavaFX KeyCode 映射为近似的 AWT 键码值。
     * 由于键映射表使用 AWT 键码值，故需要此转换。
     *
     * @param code 按键编码
     * @return 对应的 AWT 键码值，无法映射时返回 0
     */
    public static int keyCodeToAwtCode(KeyCode code) {
        if (code == null) return 0;

        // For letter keys, JavaFX KeyCode matches ASCII uppercase
        if (code.isLetterKey()) {
            String name = code.getName();
            if (name.length() == 1) {
                return name.charAt(0);
            }
        }

        // For digit keys
        if (code.isDigitKey()) {
            String name = code.getName();
            if (name.length() == 1) {
                return name.charAt(0);
            }
        }

        // Common mappings
        switch (code) {
            case BACK_QUOTE: return 192;
            case MINUS: return 45;
            case EQUALS: return 61;
            case BACK_SLASH: return 92;
            case OPEN_BRACKET: return 91;
            case CLOSE_BRACKET: return 93;
            case SEMICOLON: return 59;
            case QUOTE: return 222;
            case COMMA: return 44;
            case PERIOD: return 46;
            case SLASH: return 47;
            case LESS: return 60;
            default: return 0;
        }
    }

    /**
     * 键码对应的基础字符与 Shift 字符对。
     */
    private static class CodePair {

        /** 未按 Shift 时的字符 */
        final int code;

        /** 按下 Shift 时的字符 */
        final int codeShifted;

        /**
         * 构造字符对。
         *
         * @param code        未按 Shift 时的字符
         * @param codeShifted 按下 Shift 时的字符
         */
        CodePair(int code, int codeShifted) {
            this.code = code;
            this.codeShifted = codeShifted;
        }
    }
}
