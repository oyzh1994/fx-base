package cn.oyzh.fx.plus.opacity;

import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.plus.window.WindowManager;
import javafx.stage.Window;

import java.util.List;

/**
 * 透明度管理器
 *
 * @author oyzh
 * @since 2024/04/19
 */

public class OpacityManager {

    /**
     * 最低透明度
     */
    public static float minOpacity = .4f;

    /**
     * 最大透明度
     */
    public static float maxOpacity = 1f;

    /**
     * 默认透明度
     */
    public static float defaultOpacity = 1.f;

    /**
     * 当前透明度
     */
    private static OpacityConfig currentOpacity;

    /**
     * 获取当前透明度
     *
     * @return 当前透明度
     */
    public static OpacityConfig currentOpacity() {
        if (currentOpacity == null) {
            OpacityConfig opacityConfig = new OpacityConfig();
            opacityConfig.setTitleOpacity(defaultOpacity * 100);
            opacityConfig.setWindowOpacity(defaultOpacity * 100);
            return opacityConfig;
        }
        return currentOpacity;
    }

    /**
     * 修正透明度
     *
     * @param opacity 透明度值
     * @return 结果
     */
    public static float fixOpacity(Float opacity) {
        if (opacity != null && !Float.isNaN(opacity)) {
            if (opacity < minOpacity) {
                return minOpacity;
            }
            if (opacity > maxOpacity) {
                return maxOpacity;
            }
            return opacity;
        }
        return defaultOpacity;
    }

    /**
     * 应用透明度
     *
     * @param opacity 透明度配置
     */
    public static void apply(OpacityConfig opacity) {
        if (opacity != null) {
            try {
                // 设置当前透明度
                currentOpacity = opacity;
                // 变更透明度
                List<Window> windows = WindowManager.allWindows();
                for (Window window : windows) {
                    if (window instanceof OpacityAdapter adapter) {
                        adapter.changeOpacity(opacity);
                    }
                }
                List<StageAdapter> stages = StageManager.allStages();
                for (StageAdapter stage : stages) {
                    if (stage instanceof OpacityAdapter adapter) {
                        adapter.changeOpacity(opacity);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
