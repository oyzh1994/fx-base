package cn.oyzh.fx.plus.opacity;

import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
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
     * 应用透明度
     *
     * @param opacity 透明度配置
     */
    public static void apply(OpacityConfig opacity) {
        if (opacity != null) {
            try {
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
                // 设置当前透明度
                currentOpacity = opacity;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
