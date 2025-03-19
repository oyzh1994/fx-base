package cn.oyzh.fx.plus.window;

import cn.oyzh.fx.plus.util.FXUtil;
import javafx.collections.ObservableList;
import javafx.stage.Window;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * 窗口工具类
 *
 * @author oyzh
 * @since 2024/07/12
 */

public class WindowManager {

    /**
     * 获取所有窗口
     */
    public static List<Window> allWindows() {
        return new ArrayList<>(Window.getWindows());
    }

    /**
     * 关闭所有窗口
     */
    public static void closeWindows() {
        try {
            ObservableList<Window> windows = Window.getWindows();
            for (Window window : windows) {
                FXUtil.runLater(window::hide);
            }
        } catch (NoSuchElementException ignore) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 获取活跃窗口
     *
     * @return Window
     */
    public static Window getActiveWindow() {
        for (Window window : Window.getWindows()) {
            if (window.isFocused() && window.isShowing()) {
                return window;
            }
        }
        return null;
    }
}
