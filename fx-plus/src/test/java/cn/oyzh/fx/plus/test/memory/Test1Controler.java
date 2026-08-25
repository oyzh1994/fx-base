package cn.oyzh.fx.plus.test.memory;

import cn.oyzh.common.object.ObjectWatcherManager;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupManager;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;

/**
 *
 * @author oyzh
 * @since 2026-05-15
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "test1.fxml"
)
public class Test1Controler extends StageController {

    @FXML
    private TextArea content;

    public void save(MouseEvent mouseEvent) {
        PopupAdapter popup = PopupManager.parsePopup(Test1PopupController.class);
        popup.show(content);
        ObjectWatcherManager.watch(popup);
    }

    public void run(MouseEvent mouseEvent) {
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
    }

    @Override
    public void destroy() {
        if (content != null) {
            content.getSkin().dispose();
        }
        super.destroy();
    }
}
