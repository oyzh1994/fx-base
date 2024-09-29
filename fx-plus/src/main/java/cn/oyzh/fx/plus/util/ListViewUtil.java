package cn.oyzh.fx.plus.util;

import cn.oyzh.fx.common.util.CollectionUtil;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * @author oyzh
 * @since 2024/7/12
 */
@UtilityClass
public class ListViewUtil {

    /**
     * 上移行
     *
     * @param listView 组件
     */
    public static void moveUp(ListView<?> listView) {
        int index = listView.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            return;
        }
        ObservableList<?> list = listView.getItems();
        Object object = CollectionUtil.get(list, index - 1);
        if (object != null) {
            Collections.swap(list, index, index - 1);
            listView.getSelectionModel().select(index - 1);
        }
    }

    /**
     * 下移行
     *
     * @param listView 组件
     */
    public static void moveDown(ListView<?> listView) {
        if (listView == null) {
            return;
        }
        int index = listView.getSelectionModel().getSelectedIndex();
        if (index == -1) {
            return;
        }
        ObservableList<?> list = listView.getItems();
        Object object = CollectionUtil.get(list, index + 1);
        if (object != null) {
            Collections.swap(list, index, index + 1);
            listView.getSelectionModel().select(index + 1);
        }
    }

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
        selectRowOnMouseClicked(node, null);
    }

    /**
     * 鼠标点击时，选中列表行
     *
     * @param node     组件
     * @param itemNode 节点组件
     */
    public static void selectRowOnMouseClicked(Node node, Node itemNode) {
        if (node != null) {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                ListView listView = findListView(node);
                if (listView != null) {
                    listView.getSelectionModel().select(itemNode == null ? node : itemNode);
                }
            });
        }
    }
}
