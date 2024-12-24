package cn.oyzh.fx.plus.drag;

import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.experimental.Accessors;


/**
 * 文件拖动处理
 *
 * @author oyzh
 * @since 2023/5/14
 */
@Accessors(chain = true, fluent = true)
public class DragFileHandler {

    /**
     * 检查拖动板
     *
     * @param dragboard 拖动板
     * @return 结果
     */
    protected boolean checkDragboard(Dragboard dragboard) {
        return false;
    }

    /**
     * 拖动经过事件
     *
     * @param event 事件
     */
    public final void onDragOver(DragEvent event) {
        if (this.checkDragboard(event.getDragboard())) {
            event.acceptTransferModes(TransferMode.ANY);
            // event.consume();
            this.dragOver(event);
        }
    }

    /**
     * 拖动经过事件，内部
     *
     * @param event 事件
     */
    protected void dragOver(DragEvent event) {

    }

    /**
     * 拖动离开事件
     *
     * @param event 事件
     */
    public final void onDragExited(DragEvent event) {
        if (this.checkDragboard(event.getDragboard())) {
            // event.consume();
            this.dragExited(event);
        }
    }

    /**
     * 拖动离开事件，内部
     *
     * @param event 事件
     */
    protected void dragExited(DragEvent event) {

    }

    /**
     * 拖动完成事件
     *
     * @param event 事件
     */
    public final void onDragDropped(DragEvent event) {
        if (this.checkDragboard(event.getDragboard())) {
            this.dragDropped(event);
        }
    }

    /**
     * 拖动完成事件，离开
     *
     * @param event 事件
     */
    protected void dragDropped(DragEvent event) {
        if (this.checkDragboard(event.getDragboard())) {
            event.setDropCompleted(true);
            // event.consume();
        }
    }

    /**
     * 初始化事件
     *
     * @param scene 场景
     */
    public void initEvent(Scene scene) {
        scene.addEventFilter(DragEvent.DRAG_OVER, this::dragOver);
        scene.addEventFilter(DragEvent.DRAG_EXITED, this::dragExited);
        scene.addEventFilter(DragEvent.DRAG_DROPPED, this::dragDropped);
        scene.getProperties().put("_drapFileHandler", this);
    }

    /**
     * 清除事件
     *
     * @param scene 场景
     */
    public void clearEvent(Scene scene) {
        scene.removeEventFilter(DragEvent.DRAG_OVER, this::dragOver);
        scene.removeEventFilter(DragEvent.DRAG_EXITED, this::dragExited);
        scene.removeEventFilter(DragEvent.DRAG_DROPPED, this::dragDropped);
        scene.getProperties().remove("_drapFileHandler", this);
    }
}
