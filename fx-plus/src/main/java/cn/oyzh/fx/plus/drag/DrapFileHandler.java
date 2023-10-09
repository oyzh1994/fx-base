package cn.oyzh.fx.plus.drag;

import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.experimental.Accessors;

import java.io.File;
import java.util.List;


/**
 * 拖动增强
 *
 * @author oyzh
 * @since 2023/5/14
 */
@Accessors(chain = true, fluent = true)
public class DrapFileHandler {

    public boolean checkDragboard(Dragboard dragboard) {
        return true;
    }

    public void onDragOver(DragEvent event) {
        event.acceptTransferModes(TransferMode.ANY);
        event.consume();
    }

    public void onDragExited(DragEvent event) {
        event.consume();
    }

    public void onDragDropped(DragEvent event, List<File> files) {
        event.setDropCompleted(true);
        event.consume();
    }
}
