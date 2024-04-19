package cn.oyzh.fx.plus.opacity;

import cn.oyzh.fx.plus.stage.StageUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
    public static void apply(double opacity) {
        try {
            // 变更透明度
            List<StageWrapper> wrappers = StageUtil.allStages();
            for (StageWrapper wrapper : wrappers) {
                applyCycle(wrapper.root(), opacity);
            }
            // 设置当前透明度
            currentOpacity = opacity;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 应用透明度，循环处理
     *
     * @param root    根节点
     * @param opacity 透明度
     */
    private static void applyCycle(EventTarget root, double opacity) {
        if (root instanceof OpacityAdapter adapter) {
            adapter.changeOpacity(opacity);
        }
        if (root instanceof Parent parent) {
            for (Node node : new CopyOnWriteArrayList<>(parent.getChildrenUnmodifiable())) {
                applyCycle(node, opacity);
            }
        } else if (root instanceof Popup popup) {
            for (Node node : new CopyOnWriteArrayList<>(popup.getContent())) {
                applyCycle(node, opacity);
            }
        } else if (root instanceof Stage stage) {
            applyCycle(stage.getScene(), opacity);
        } else if (root instanceof Window window) {
            applyCycle(window.getScene(), opacity);
        } else if (root instanceof Scene scene) {
            applyCycle(scene.getRoot(), opacity);
        }
    }
}
