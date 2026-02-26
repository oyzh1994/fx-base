package cn.oyzh.fx.plus.drag;

import cn.oyzh.fx.plus.mouse.MouseUtil;
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;


/**
 * 拖动节点处理器
 *
 * @author oyzh
 * @since 2023/5/14
 */
public class DragNodeHandler {

    // /**
    //  * 拖动中标志位
    //  */
    // public static AtomicBoolean DRAGGING = new AtomicBoolean(false);

    /**
     * 来源
     */
    private DragNodeItem source;

    /**
     * 目标
     */
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

    // /**
    //  * 拖动的节点
    //  */
    // private Node node;
    //
    // /**
    //  * 内容
    //  */
    // private String content;
    //
    // /**
    //  * 拖动经过
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragOver(DragEvent event) {
    //     // 触发拖动事件
    //     event.acceptTransferModes(TransferMode.MOVE);
    //     event.consume();
    // }
    //
    // /**
    //  * 拖动完成
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragDone(DragEvent event) {
    //     // 清除特效
    //     this.clear();
    //     // 清除数据
    //     event.getDragboard().clear();
    //     DRAGGING.set(false);
    //     event.consume();
    // }
    //
    // /**
    //  * 拖动离开
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragExited(DragEvent event) {
    //     // 设置透明度
    //     node.setOpacity(1);
    //     event.consume();
    // }
    //
    // /**
    //  * 拖动进入
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragEntered(DragEvent event) {
    //     DragNodeItem source = DragUtil.getDragItem(event.getGestureSource());
    //     DragNodeItem target = DragUtil.getDragItem(event.getTarget());
    //     // 判断是否允许投放
    //     if (target != null && source != null && target.allowDropNode(source)) {
    //         node.setOpacity(0.3);
    //         this.setSource(source);
    //         this.initDropEffect(target);
    //     }
    //     event.consume();
    // }
    //
    // /**
    //  * 拖动投放
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragDropped(DragEvent event) {
    //     event.setDropCompleted(true);
    //     this.dropNode();
    //     // 清除数据
    //     event.getDragboard().clear();
    //     DRAGGING.set(false);
    //     event.consume();
    // }
    //
    // /**
    //  * 拖动触发
    //  *
    //  * @param event 事件
    //  */
    // protected void onDragDetected(MouseEvent event) {
    //     if (DRAGGING.get()) {
    //         return;
    //     }
    //     // 获取拖动节点
    //     DragNodeItem source = DragUtil.getDragItem(event.getSource());
    //     // 判断是否允许触发拖动事件
    //     if (source != null && source.allowDrag()) {
    //         DRAGGING.set(true);
    //         Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
    //         ClipboardContent clipboardContent = new ClipboardContent();
    //         clipboardContent.putString(content);
    //         db.setContent(clipboardContent);
    //         // 设置特效
    //         this.initDragEffect(source);
    //     }
    //     event.consume();
    // }

    /**
     * 初始化事件
     *
     * @param node    节点
     * @param content 内容
     */
    public void initEvent(Node node, String content) {
        // 触发拖动
        node.addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            if (MouseUtil.isPrimaryButton(event)) {
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
                this.setSource(source);
                this.initDropEffect(target);
            }
            event.consume();
        });
        // 拖动释放
        node.addEventFilter(DragEvent.DRAG_DROPPED, event -> {
            this.dropNode();
            event.setDropCompleted(true);
            // 清除数据
            event.getDragboard().clear();
            event.consume();
        });
        // this.node = node;
        // this.content = content;
        // // 初始化事件
        // this.node.addEventFilter(DragEvent.DRAG_DONE, this::onDragDone);
        // this.node.addEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
        // this.node.addEventFilter(DragEvent.DRAG_EXITED, this::onDragExited);
        // this.node.addEventFilter(DragEvent.DRAG_ENTERED, this::onDragEntered);
        // this.node.addEventFilter(DragEvent.DRAG_DROPPED, this::onDragDropped);
        // this.node.addEventFilter(MouseEvent.DRAG_DETECTED, this::onDragDetected);
    }

    // /**
    //  * 销毁事件
    //  *
    //  */
    // public void destroyEvent() {
    //     if (this.node != null) {
    //         this.node.removeEventFilter(DragEvent.DRAG_DONE, this::onDragDone);
    //         this.node.removeEventFilter(DragEvent.DRAG_OVER, this::onDragOver);
    //         this.node.removeEventFilter(DragEvent.DRAG_EXITED, this::onDragExited);
    //         this.node.removeEventFilter(DragEvent.DRAG_ENTERED, this::onDragEntered);
    //         this.node.removeEventFilter(DragEvent.DRAG_DROPPED, this::onDragDropped);
    //         this.node.removeEventFilter(MouseEvent.DRAG_DETECTED, this::onDragDetected);
    //     }
    // }

    public synchronized DragNodeItem getSource() {
        return source;
    }

    public synchronized void setSource(DragNodeItem source) {
        this.source = source;
    }

    public DragNodeItem getTarget() {
        return target;
    }

    public void setTarget(DragNodeItem target) {
        this.target = target;
    }

    // @Override
    // public void destroy() {
    //     this.clear();
    //     this.destroyEvent();
    //     // this.node = null;
    //     // this.content = null;
    // }
}
