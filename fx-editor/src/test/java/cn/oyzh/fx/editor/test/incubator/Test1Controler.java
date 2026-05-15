package cn.oyzh.fx.editor.test.incubator;

import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import jfx.incubator.scene.control.richtext.RichTextArea;

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
    private RichTextArea content;

    public void save(MouseEvent mouseEvent) {
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
