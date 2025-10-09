package cn.oyzh.fx.plus.util;

import javafx.stage.Screen;

/**
 * @author oyzh
 * @since 2025-08-20
 */
public class ScreenUtil {

    /**
     * 获取所有屏幕的宽
     *
     * @return 所有屏幕的宽
     */
    public static double getAllWidth() {
        double width = 0;
        for (Screen screen : Screen.getScreens()) {
            width += screen.getVisualBounds().getWidth();
        }
        return width;
    }
}
