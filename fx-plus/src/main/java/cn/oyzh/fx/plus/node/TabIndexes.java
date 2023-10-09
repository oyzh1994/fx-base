package cn.oyzh.fx.plus.node;

import cn.hutool.core.collection.CollUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * tab索引工具类
 *
 * @author oyzh
 * @since 2023/1/16
 */
@Deprecated
@UtilityClass
public class TabIndexes {

    /**
     * 寻找支持tab索引的节点
     *
     * @param root     根节点
     * @param nodeList 节点列表
     */
    private static void find(Parent root, @NonNull List<TabIndex> nodeList) {
        for (Node n : root.getChildrenUnmodifiable()) {
            if (n.isManaged()) {
                if (n instanceof TabIndex index) {
                    nodeList.add(index);
                }
                if (n instanceof Parent p1) {
                    find(p1, nodeList);
                }
            }
        }
    }

    /**
     * 寻找支持tab索引的节点
     *
     * @param root 根节点
     * @return 节点列表
     */
    public static List<TabIndex> find(Parent root) {
        List<TabIndex> nodeList = new ArrayList<>();
        find(root, nodeList);
        nodeList.sort((o1, o2) -> {
            if (o1.getTabIndex() == o2.getTabIndex()) {
                return 0;
            }
            return o1.getTabIndex() > o2.getTabIndex() ? 1 : -1;
        });
        return nodeList;
    }

    /**
     * 寻找支持tab索引的节点
     *
     * @param stage 页面
     * @return 节点列表
     */
    public static List<TabIndex> find(Stage stage) {
        return find(stage.getScene().getRoot());
    }


    /**
     * 获取下一个支持tab索引的节点
     *
     * @param nodeList 节点列表
     * @param current  当前节点
     * @return 支持tab索引的节点
     */
    public static TabIndex getNext(@NonNull List<TabIndex> nodeList, @NonNull TabIndex current) {
        List<TabIndex> list = nodeList.parallelStream().filter(f -> f.getTabIndex() > current.getTabIndex()).toList();
        if (!list.isEmpty()) {
            return list.get(0);
        }
        int index = nodeList.indexOf(current);
        if (index + 1 < nodeList.size()) {
            return nodeList.get(index + 1);
        }
        return null;
    }

    /**
     * 获取首个支持tab索引的节点
     *
     * @param nodeList 节点列表
     * @return 支持tab索引的节点
     */
    public static TabIndex getFirst(@NonNull List<TabIndex> nodeList) {
        return nodeList.isEmpty() ? null : nodeList.get(0);
    }

    /**
     * 跳转到下一个节点
     *
     * @param stage 页面
     * @param event 事件
     */
    public static void toNext(@NonNull Stage stage, KeyEvent event) {
        toNext(stage.getScene().getRoot(), event);
    }

    /**
     * 跳转到下一个节点
     *
     * @param root  根节点
     * @param event 事件
     */
    public static void toNext(@NonNull Parent root, KeyEvent event) {
        List<TabIndex> nodeList = find(root);
        if (CollUtil.isNotEmpty(nodeList)) {
            TabIndex index = null;
            if (event.getTarget() instanceof TabIndex current) {
                // 获取下一个支持tab索引的节点
                index = getNext(nodeList, current);
            }
            // 节点为空，获取首个节点
            if (index == null) {
                index = getFirst(nodeList);
            }
            // 获取焦点
            if (index instanceof Node node) {
                node.requestFocus();
            }
        }
    }
}
