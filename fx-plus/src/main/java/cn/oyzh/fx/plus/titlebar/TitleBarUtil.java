package cn.oyzh.fx.plus.titlebar;

import javafx.collections.ObservableList;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2024-12-25
 */
@UtilityClass
public class TitleBarUtil {

    /**
     * 限制边界
     *
     * @param stage 舞台
     */
    public static void limitations(Stage stage) {
        // 是否更新
        boolean update = false;
        double xPos = stage.getX() + stage.getWidth();
        double yPos = stage.getY();
        // 获取所有屏幕的列表
        ObservableList<Screen> screens = Screen.getScreens();
        // 遍历屏幕列表，检查舞台是否在当前屏幕的边界内
        for (Screen screen : screens) {
            Rectangle2D screenBounds = screen.getVisualBounds();
            if (screenBounds.contains(xPos, yPos)) {
                stage.setHeight(screenBounds.getHeight());
                update = true;
                break;
            }
        }
        // 兜底设置
        if (!update) {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            stage.setHeight(screenBounds.getHeight());
        }
    }
}
