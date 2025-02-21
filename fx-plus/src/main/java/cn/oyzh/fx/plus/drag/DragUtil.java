package cn.oyzh.fx.plus.drag;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import lombok.experimental.UtilityClass;

/**
 * 拖动工具类
 *
 * @author oyzh
 * @since 2023/9/12
 */
@UtilityClass
public class DragUtil {

    /**
     * 获取拖动节点
     *
     * @param o 对象
     * @return 拖动节点
     */
    public static DragNodeItem getDragItem(Object o) {
        if (o instanceof TreeCell<?> cell) {
            if (cell.getTreeItem() instanceof DragNodeItem dragItem) {
                return dragItem;
            }
        }
        if (o instanceof DragNodeItem dragItem) {
            return dragItem;
        }
        return null;
    }

    /**
     * 初始化拖动节点功能
     *
     * @param handler 拖动处理器
     * @param node    监听节点
     * @param content 拖动板内容
     */
    @Deprecated
    public static void initDragNode(DragNodeHandler handler, Node node, String content) {
        // 触发拖动
        node.setOnDragDetected(event -> {
            // 获取拖动节点
            DragNodeItem source = DragUtil.getDragItem(event.getSource());
            // 判断是否允许触发拖动事件
            if (source != null && source.allowDrag()) {
                Dragboard db = node.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent clipboardContent = new ClipboardContent();
                clipboardContent.putString(content);
                db.setContent(clipboardContent);
                // 设置特效
                handler.initDragEffect(source);
            }
            event.consume();
        });

        // 拖动完成
        node.setOnDragDone(event -> {
            // 清除特效
            handler.clear();
            // 清除数据
            event.getDragboard().clear();
            event.consume();
        });

        // 拖动经过
        node.setOnDragOver(event -> {
            // 触发拖动事件
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
        });

        // 拖动离开
        node.setOnDragExited(event -> {
            // 清除数据
            node.setOpacity(1);
            event.consume();
        });

        // 拖动进入
        node.setOnDragEntered(event -> {
            DragNodeItem source = DragUtil.getDragItem(event.getGestureSource());
            DragNodeItem target = DragUtil.getDragItem(event.getTarget());
            // 判断是否允许投放
            if (target != null && source != null && target.allowDropNode(source)) {
                node.setOpacity(0.3);
                handler.source(source);
                handler.initDropEffect(target);
            }
            event.consume();
        });

        // 拖动释放
        node.setOnDragDropped(event -> {
            handler.dropNode();
            event.setDropCompleted(true);
            event.consume();
        });
    }

    /**
     * 初始化拖动文件功能
     *
     * @param handler 拖动处理器
     * @param scene   场景
     */
    @Deprecated
    public static void initDragFile(DragFileHandler handler, Scene scene) {
        scene.setOnDragOver(handler::onDragOver);
        scene.setOnDragExited(handler::onDragExited);
        scene.setOnDragDropped(handler::onDragDropped);
        scene.getProperties().put("_dragFileHandler", handler);
    }

    /**
     * 清除文件拖拽资源
     *
     * @param scene 场景
     */
    public static void clearDragFile(Scene scene) {
        if (scene != null && scene.getProperties().containsKey("_dragFileHandler")) {
            DragFileHandler handler = (DragFileHandler) scene.getProperties().get("_dragFileHandler");
            handler.clearEvent(scene);
            // scene.getProperties().remove("_drapFileHandler");
            // scene.setOnDragOver(null);
            // scene.setOnDragExited(null);
            // scene.setOnDragDropped(null);
        }
    }
}
