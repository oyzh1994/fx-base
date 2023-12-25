package cn.oyzh.fx.plus.test.treeview;

import cn.oyzh.fx.plus.FXStyle;
import cn.oyzh.fx.plus.controller.Controller;
import cn.oyzh.fx.plus.controls.tree.FlexTreeView;
import cn.oyzh.fx.plus.theme.AtlantaFX;
import cn.oyzh.fx.plus.stage.StageAttribute;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.Modality;

/**
 * @author oyzh
 * @since 2023/11/21
 */
@StageAttribute(
        title = "treeView测试",
        modality = Modality.WINDOW_MODAL,
        // cssUrls = AtlantaFX.CUPERTINO_LIGHT + ";" + FXStyle.FX_BASE,/
        value = "/treeview/test.fxml"
)
public class TreeViewTestController extends Controller {

    @FXML
    private FlexTreeView tree;

    public void test1(ActionEvent actionEvent) {
        tree.setRoot(new TreeItem("根节点"));
    }

    public void test2(ActionEvent actionEvent) {
        for (int i = 0; i < 10; i++) {
            tree.getRoot().getChildren().add(new TreeItem<>("子节点" + i));
        }
    }
}
