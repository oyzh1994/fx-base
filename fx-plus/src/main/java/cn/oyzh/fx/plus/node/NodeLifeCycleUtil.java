package cn.oyzh.fx.plus.node;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author oyzh
 * @since 2024-11-18
 */
public class NodeLifeCycleUtil {

    public static void onStageDestroy(Stage stage) {
        Scene scene = stage.getScene();
        if (scene != null) {
            onParentDestroy(scene.getRoot());
        }
    }

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
