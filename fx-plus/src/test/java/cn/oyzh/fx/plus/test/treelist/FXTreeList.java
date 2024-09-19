package cn.oyzh.fx.plus.test.treelist;

import javafx.scene.layout.VBox;

/**
 * @author oyzh
 * @since 2024-09-19
 */
public class FXTreeList extends VBox {

    public void setRoot(FXTreeNode root) {
        this.getChildren().add(root);
    }

}
