package cn.oyzh.fx.tty;

import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.core.Color;

/**
 * 终端工具类。
 *
 * @author oyzh
 * @since 2026-07-06
 */
public class TtyTerminalUtil {

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
}
