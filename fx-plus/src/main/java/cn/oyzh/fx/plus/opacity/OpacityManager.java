package cn.oyzh.fx.plus.opacity;

import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.window.StageWrapper;
import cn.oyzh.fx.plus.window.WindowManager;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 透明度管理器
 *
 * @author oyzh
 * @since 2024/04/19
 */
@UtilityClass
public class OpacityManager {

    /**
     * 默认透明度
     */
    public static double defaultOpacity = 1.0;

    /**
     * 当前透明度
     */
    private static Double currentOpacity;

    /**
     * 获取当前透明度
     *
     * @return 当前透明度
     */
    public static double currentOpacity() {
        if (currentOpacity == null) {
            return defaultOpacity;
        }
        return currentOpacity;
    }

    /**
     * 应用透明度
     *
     * @param opacity 透明度
     */
    public static void apply(Double opacity) {
        if (opacity != null && !Double.isNaN(opacity)) {
            try {
                opacity /= 100;
                // 变更透明度
                List<Window> windows = WindowManager.allWindows();
                for (Window window : windows) {
                    if (window instanceof OpacityAdapter adapter) {
                        adapter.changeOpacity(opacity);
                    }
                }
                List<StageWrapper> stages = StageManager.allStages();
                for (StageWrapper stage : stages) {
                    if (stage instanceof OpacityAdapter adapter) {
                        adapter.changeOpacity(opacity);
                    }
                }
                // 设置当前透明度
                currentOpacity = opacity;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
