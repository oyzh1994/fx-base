package cn.oyzh.fx.plus.test.headerbar;

import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.tree.view.FXTreeView;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HeaderBar;
import javafx.stage.Modality;

/**
 * @author oyzh
 * @since 2023/11/21
 */
@StageAttribute(
        title = "HeaderBar测试",
        modality = Modality.WINDOW_MODAL,
        stageStyle = FXStageStyle.EXTENDED,
        value = "/headerBar/test.fxml"
)
public class HeaderBarTestController extends StageController {

    @FXML
    private HeaderBar headerBar;

}
