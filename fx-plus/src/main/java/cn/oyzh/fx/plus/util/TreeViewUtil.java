package cn.oyzh.fx.plus.util;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * fx控件工具类
 *
 * @author oyzh
 * @since 2023/05/09
 */
//@Slf4j
@UtilityClass
public class TreeViewUtil {

    /**
     * 展开全部
     *
     * @param item 节点
     */
    public static void expandAll(TreeItem<?> item) {
        while (item != null) {
            if (!item.isExpanded()) {
                item.setExpanded(true);
            }
            item = item.getParent();
        }
    }

    /**
     * 获取树组件的全部节点
     *
     * @param treeView 树组件
     * @param filter   过滤器
     * @return 全部节点列表
     */
    public static List<TreeItem<?>> getAllItem(@NonNull TreeView<?> treeView, Function<TreeItem<?>, Boolean> filter) {
        TreeItem<?> root = treeView.getRoot();
        List<TreeItem<?>> items = new ArrayList<>();
        getAllItem(root, items, filter);
        return items;
    }

    /**
     * 获取树节点全部节点
     *
     * @param item   树节点
     * @param filter 过滤器
     * @param items  全部节点列表
     */
    private static void getAllItem(TreeItem<?> item, List<TreeItem<?>> items, Function<TreeItem<?>, Boolean> filter) {
        if (item != null) {
            if (filter == null || filter.apply(item)) {
                items.add(item);
            }
            ObservableList<? extends TreeItem<?>> children = item.getChildren();
            if (children != null && !children.isEmpty()) {
                for (TreeItem<?> child : children) {
                    getAllItem(child, items, filter);
                }
            }
        }
    }

    /**
     * 获取树组件的全部节点
     *
     * @param treeView 树组件
     * @param filter   过滤器
     */
    public static void filterItem(@NonNull TreeView<?> treeView, Consumer<TreeItem<?>> filter) {
        TreeItem<?> root = treeView.getRoot();
        filterItem(root, filter);
    }

    /**
     * 获取树节点全部节点
     *
     * @param item   树节点
     * @param filter 过滤器
     */
    private static void filterItem(TreeItem<?> item, Consumer<TreeItem<?>> filter) {
        if (item != null) {
            if (filter != null) {
                filter.accept(item);
            }
            ObservableList<? extends TreeItem<?>> children = item.getChildren();
            if (children != null && !children.isEmpty()) {
                for (TreeItem<?> child : children) {
                    filterItem(child, filter);
                }
            }
        }
    }

    /**
     * 获取可见树节点
     *
     * @param treeView 树组件
     * @return 可见树节点
     */
    public static List<TreeItem<?>> getVisibleItems(@NonNull TreeView<?> treeView) {
        List<TreeItem<?>> result = new ArrayList<>(treeView.getExpandedItemCount());
        int startIndex = -1;
        for (int i = 0; i < treeView.getExpandedItemCount(); i++) {
            TreeItem<?> item = treeView.getTreeItem(i);
            if (item == treeView.getSelectionModel().getSelectedItem()) {
                startIndex = i;
            }
            if (startIndex > -1 && !item.isExpanded()) {
                startIndex = -1;
            }
            if (startIndex > -1) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * 节点是否可见
     *
     * @param treeView 树组件
     * @param item     节点
     */
    public static boolean isVisible(TreeView<?> treeView, TreeItem<?> item) {
        if (treeView == null || item == null) {
            return false;
        }
        if (treeView.getSelectionModel().getSelectedItem() == item) {
            return true;
        }
        for (int i = 0; i < treeView.getExpandedItemCount(); i++) {
            if (treeView.getTreeItem(i) == item) {
                return true;
            }
        }
        return false;
        // return treeView.getRow((TreeItem) item) != -1;
    }
}
