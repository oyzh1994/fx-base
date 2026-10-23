package cn.oyzh.fx.tty;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * 终端工具类。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyTerminalUtil {

    /**
     * 首屏缓冲优化开关，默认关闭。
     */
    private static final String FIRST_SCREEN_BUFFER_ENABLED = "fx-tty.firstScreenBuffer";

    /**
     * 是否启用首屏缓冲优化。
     * <p>
     * 启用后使用更大的终端读取缓冲，并在解析读取块期间暂停重绘，避免首屏内容分阶段显示；
     * 默认关闭以保持原始首屏响应速度。
     *
     * @return 是否启用
     */
    public static boolean isFirstScreenBufferEnabled() {
        return Boolean.getBoolean(FIRST_SCREEN_BUFFER_ENABLED);
    }

    /**
     * 获取退格码
     *
     * @param backspaceType 退格类型
     * @return 退格码
     */
    public static Object getBackspaceCode(Integer backspaceType) {
        if (backspaceType == null || backspaceType == 1) {
            return new byte[]{0x08};
        }
        if (backspaceType == 0) {
            return new byte[]{0x7F};
        }
        if (backspaceType == 2) {
            return "ESC[3~";
        }
        return null;
    }

    /**
     * 从 JavaFX 颜色转换生成终端颜色
     *
     * @param color1 JavaFX 颜色
     * @return 终端颜色
     */
    public static Color fromFXColor(javafx.scene.paint.Color color1) {
        int red = (int) (color1.getRed() * 255);
        int green = (int) (color1.getGreen() * 255);
        int blue = (int) (color1.getBlue() * 255);
        int opacity = (int) (color1.getOpacity() * 255);
        return new Color(red, green, blue, opacity);
    }

    /**
     * 创建zModem协议的tty连接器
     *
     * @param widget    tty组件
     * @param connector tty连接器
     * @return zModem 连接器
     */
    public static TtyZModemTtyConnector createZModemTtyConnector(TtyTermWidget widget, TtyStreamable connector) {
        return new TtyZModemTtyConnector(widget.getTerminal(), connector);
    }

    /**
     * 获取按键字符
     *
     * @param event 事件
     * @return 结果
     */
    public static char getKeyChar(KeyEvent event) {
        if (event == null) {
            return '\uffff';
        }
        KeyCode code = event.getCode();
        // Only Ctrl held: derive the control character from the key code. On Windows/Linux a
        // JavaFX KEY_PRESSED event never carries it, while jediterm recognizes Ctrl combinations
        // solely by testing whether keyChar is an ISO control character, so Ctrl+A / Ctrl+E
        // used to send nothing there.
        if (event.isControlDown() && !event.isShiftDown() && !event.isAltDown() && !event.isMetaDown()) {
            Character ctrlChar = getCtrlChar(code);
            if (ctrlChar != null) {
                return ctrlChar;
            }
        }
        // Esc: Windows/Linux never carry the ESC character in the event (macOS does), add it here.
        if (code == KeyCode.ESCAPE && !event.isControlDown() && !event.isAltDown() && !event.isMetaDown()) {
            return (char) 0x1B;
        }
        // Only Alt held: Windows/Linux put no character in the event either, derive it from the
        // key code so that jediterm's altSendsEscape() check keeps working.
        if (event.isAltDown() && !event.isControlDown() && !event.isShiftDown() && !event.isMetaDown()) {
            Character altChar = getPrintableChar(code);
            if (altChar != null) {
                return altChar;
            }
        }
        // Windows/Linux fill getText() with a NUL placeholder for keys that produce no character
        // (modifiers, lock keys, ...). Ignore it, otherwise NUL (^@) would be written to the terminal.
        if (StringUtil.isNotEmpty(event.getText()) && event.getText().charAt(0) != 0) {
            return event.getText().charAt(0);
        }
        if (StringUtil.isNotEmpty(event.getCharacter()) && event.getCharacter().charAt(0) != 0) {
            return event.getCharacter().charAt(0);
        }
        return '\uffff';
    }

    /**
     * Derives the control character produced by a key when only Ctrl is held.
     *
     * @param code the key code
     * @return the control character, or null when there is none
     */
    private static Character getCtrlChar(KeyCode code) {
        if (code == null) {
            return null;
        }
        int value = code.getCode();
        // Ctrl+A ~ Ctrl+Z -> 0x01 ~ 0x1A
        if (value >= KeyCode.A.getCode() && value <= KeyCode.Z.getCode()) {
            return (char) (value - KeyCode.A.getCode() + 1);
        }
        // Ctrl+0 ~ Ctrl+9 -> 0x00 ~ 0x09 (number row, keypad and soft keys)
        if (value >= KeyCode.DIGIT0.getCode() && value <= KeyCode.DIGIT9.getCode()) {
            return (char) (value - KeyCode.DIGIT0.getCode());
        }
        if (value >= KeyCode.NUMPAD0.getCode() && value <= KeyCode.NUMPAD9.getCode()) {
            return (char) (value - KeyCode.NUMPAD0.getCode());
        }
        if (value >= KeyCode.SOFTKEY_0.getCode() && value <= KeyCode.SOFTKEY_9.getCode()) {
            return (char) (value - KeyCode.SOFTKEY_0.getCode());
        }
        return switch (code) {
            case SPACE -> (char) 0x00;
            case OPEN_BRACKET -> (char) 0x1B;
            case BACK_SLASH -> (char) 0x1C;
            case CLOSE_BRACKET -> (char) 0x1D;
            case SLASH -> (char) 0x1F;
            default -> null;
        };
    }

    /**
     * Derives the printable character of a letter or digit key.
     *
     * @param code the key code
     * @return the character, or null when the key is neither a letter nor a digit
     */
    private static Character getPrintableChar(KeyCode code) {
        if (code == null || !(code.isLetterKey() || code.isDigitKey())) {
            return null;
        }
        String ch = code.getChar();
        if (ch == null || ch.isEmpty()) {
            return null;
        }
        return Character.toLowerCase(ch.charAt(0));
    }
}
