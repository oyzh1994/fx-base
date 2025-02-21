package cn.oyzh.fx.plus.drag;

import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;


/**
 * 拖动增强
 *
 * @author oyzh
 * @since 2023/5/14
 */
@Getter
@Accessors(chain = true, fluent = true)
public class DragNodeHandler {

    /**
     * 来源
     */
    @Setter
    private DragNodeItem source;

    /**
     * 目标
     */
    @Setter
    private DragNodeItem target;

    /**
     * 清理
     */
    public void clear() {
        this.clearDrapEffect();
        this.source = null;
        this.target = null;
    }

    /**
     * 清理拖动特效
     */
    public void clearDrapEffect() {
        if (this.source != null) {
            this.source.clearEffect();
        }
    }

    /**
     * 清理投放特效
     */
    public void clearDropEffect() {
        if (this.target != null) {
            this.target.clearEffect();
        }
    }

    /**
     * 初始化来源拖动特效
     *
     * @param source 节点
     */
    public void initDragEffect(DragNodeItem source) {
        this.clearDrapEffect();
        source.initDragEffect();
        this.source = source;
    }

    /**
     * 初始化来源拖动特效
     *
     * @param target 节点
     */
    public void initDropEffect(DragNodeItem target) {
        this.clearDropEffect();
        target.initDropEffect();
        this.target = target;
    }

    /**
     * 执行投放
     */
    public void dropNode() {
        try {
            if (this.source != null && this.target != null) {
                this.target.onDropNode(this.source);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * 初始化事件
     *
     * @param node    节点
     * @param content 内容
     */
    public void initEvent(Node node, String content) {
        // 触发拖动
        node.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            // 获取拖动节点
            DragNodeItem source = DragUtil.getDragItem(event.getSource());
            // 判断是否允许触发拖动事件
            if (source != null && source.allowDrag()) {
                Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(content);
                db.setContent(clipboardContent);
                // 设置特效
                this.initDragEffect(source);
            }
            event.consume();
        });
        // 拖动完成
        node.addEventFilter(DragEvent.DRAG_DONE, event -> {
            // 清除特效
            this.clear();
            // 清除数据
            event.getDragboard().clear();
            event.consume();
        });
        // 拖动完成
        node.addEventFilter(DragEvent.DRAG_OVER, event -> {
            // 触发拖动事件
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });
        // 拖动离开
        node.addEventFilter(DragEvent.DRAG_EXITED, event -> {
            // 设置透明度
            node.setOpacity(1);
            event.consume();
        });
        // 拖动进入
        node.addEventFilter(DragEvent.DRAG_ENTERED, event -> {
            DragNodeItem source = DragUtil.getDragItem(event.getGestureSource());
            DragNodeItem target = DragUtil.getDragItem(event.getTarget());
            // 判断是否允许投放
            if (target != null && source != null && target.allowDropNode(source)) {
                node.setOpacity(0.3);
                this.source(source);
                this.initDropEffect(target);
            }
            event.consume();
        });
        // 拖动释放
        node.addEventFilter(DragEvent.DRAG_DROPPED, event -> {
            this.dropNode();
            event.setDropCompleted(true);
            event.consume();
        });
    }
}
