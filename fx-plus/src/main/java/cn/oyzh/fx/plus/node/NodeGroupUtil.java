package cn.oyzh.fx.plus.node;

import cn.hutool.core.util.ArrayUtil;
import cn.oyzh.fx.plus.stage.StageWrapper;
import cn.oyzh.fx.plus.util.NodeUtil;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumnBase;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

/**
 * 节点分组工具
 *
 * @author oyzh
 * @since 2024/06/08
 */
@UtilityClass
public class NodeGroupUtil {

    /**
     * 是否有分组id
     *
     * @param group   分组对象
     * @param groupId 分组id
     * @return 结果
     */
    public static boolean hasGroupId(NodeGroup group, String groupId) {
        String id = group.getGroupId();
        if (id == null || !id.contains(groupId)) {
            return false;
        }
        return ArrayUtil.contains(id.split(","), groupId);
    }

    /**
     * 列举分组对象
     *
     * @param node    节点
     * @param groupId 分组id
     * @return 分组列表
     */
    public static List<NodeGroup> list(Object node, String groupId) {
        List<NodeGroup> groups = new ArrayList<>();
        list(node, groupId, groups);
        return groups;
    }

    /**
     * 列举分组对象
     *
     * @param node    节点
     * @param groupId 分组id
     * @param groups  分组列表
     */
    public static void list(Object node, String groupId, List<NodeGroup> groups) {
        if (node instanceof NodeGroup group && hasGroupId(group, groupId)) {
            groups.add(group);
        }
        if (node instanceof StageWrapper wrapper) {
            list(wrapper.root(), groupId, groups);
        } else if (node instanceof Stage stage) {
            list(stage.getScene().getRoot(), groupId, groups);
        } else if (node instanceof Scene scene) {
            list(scene.getRoot(), groupId, groups);
        } else if (node instanceof TabPane tabPane) {
            for (Tab node1 : tabPane.getTabs()) {
                list(node1, groupId, groups);
            }
        } else if (node instanceof Tab tab) {
            list(tab.getContent(), groupId, groups);
        } else if (node instanceof TableColumnBase<?, ?> columnBase) {
            list(columnBase, groupId, groups);
        } else if (node instanceof Parent parent) {
            for (Node node1 : parent.getChildrenUnmodifiable()) {
                list(node1, groupId, groups);
            }
        }
    }

    /**
     * 禁用分组
     *
     * @param node    节点
     * @param groupId 分组id
     */
    public static void disable(Object node, String groupId) {
        List<NodeGroup> groups = list(node, groupId);
        for (NodeGroup group : groups) {
            NodeUtil.disable(group);
        }
    }

    /**
     * 启用分组
     *
     * @param node    节点
     * @param groupId 分组id
     */
    public static void enable(Object node, String groupId) {
        List<NodeGroup> groups = list(node, groupId);
        for (NodeGroup group : groups) {
            NodeUtil.enable(group);
        }
    }
}
