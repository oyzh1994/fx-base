package cn.oyzh.fx.plus.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

/**
 * @author oyzh
 * @since 2024/7/12
 */
@UtilityClass
public class ListViewUtil {

    /**
     * 高亮组件列表行
     *
     * @param cell 行
     */
    public static void highlightCell(ListCell<?> cell) {
        if (cell != null) {
            cell.setOnMouseExited(event -> cell.setBackground(null));
            cell.setOnMouseEntered(event -> cell.setBackground(ControlUtil.hilightBackground()));
        }
    }

    public static ListView<?> findListView(Node node) {
        if (node != null) {
            if (node instanceof ListView<?>) {
                return (ListView<?>) node;
            }
            while (true) {
                Parent parent = node.getParent();
                if (parent == null) {
                    return null;
                }
                if (parent instanceof ListView<?> listView) {
                    return listView;
                }
                node = parent;
            }
        }
        return null;
    }

    /**
     * 鼠标点击时，选中列表行
     *
     * @param node 组件
     */
    public static void selectRowOnMouseClicked(Node node) {
        if (node != null) {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                ListView listView = findListView(node);
                if (listView != null) {
                    listView.getSelectionModel().select(node);
                }
            });
        }
    }
}
