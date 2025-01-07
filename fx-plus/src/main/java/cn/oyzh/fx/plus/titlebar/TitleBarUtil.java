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

    public static Rectangle2D getScreenBounds(Stage stage) {
        // 是否更新
        double xPos = stage.getX() + stage.getWidth();
        double yPos = stage.getY();
        // 获取所有屏幕的列表
        ObservableList<Screen> screens = Screen.getScreens();
        // 遍历屏幕列表，检查舞台是否在当前屏幕的边界内
        for (Screen screen : screens) {
            Rectangle2D screenBounds = screen.getVisualBounds();
            if (screenBounds.contains(xPos, yPos)) {
                return screenBounds;
            }
        }
        // 兜底设置
        return Screen.getPrimary().getVisualBounds();
    }

    /**
     * 全屏显示
     *
     * @param stage 舞台
     */
    public static void doFullScreen(Stage stage) {
        // 是否更新
        Rectangle2D bounds = getScreenBounds(stage);
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());
        stage.setX(0);
        stage.setY(0);
    }

    public static void exitFullScreen(Stage stage) {
        Double width = (Double) stage.getProperties().remove("stage:width");
        Double height = (Double) stage.getProperties().remove("stage:height");
        if (width == null) {
            width = stage.getWidth() - 300;
            height = stage.getHeight() - 50;
        } else {
            Rectangle2D bounds = getScreenBounds(stage);
            width = Math.min(width, bounds.getWidth() - 300);
            height = Math.min(height, bounds.getHeight() - 150);
        }
        stage.setX(180);
        stage.setY(50);
        stage.setWidth(width);
        stage.setHeight(height);
    }
}
