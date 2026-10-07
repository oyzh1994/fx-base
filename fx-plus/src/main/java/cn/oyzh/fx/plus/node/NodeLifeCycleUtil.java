package cn.oyzh.fx.plus.node;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 节点生命周期工具类，用于在节点树销毁时递归通知节点生命周期回调
 *
 * @author oyzh
 * @since 2024-11-18
 */
public class NodeLifeCycleUtil {

    /**
     * 处理舞台销毁
     *
     * @param stage 舞台
     */
    public static void onStageDestroy(Stage stage) {
        Scene scene = stage.getScene();
        if (scene != null) {
            onParentDestroy(scene.getRoot());
        }
    }

    /**
     * 递归处理父节点销毁
     *
     * @param parent 父节点
     */
    public static void onParentDestroy(Parent parent) {
        if (parent != null) {
            for (Node node : parent.getChildrenUnmodifiable()) {
                if (node instanceof NodeLifeCycle lifeCycle) {
                    lifeCycle.onNodeDestroy();
                }
                if (node instanceof Parent parent1) {
                    onParentDestroy(parent1);
                }
            }
        }
    }
}
