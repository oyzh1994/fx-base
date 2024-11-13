package cn.oyzh.fx.plus.node;

import cn.oyzh.common.util.BooleanUtil;
import javafx.scene.Node;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 节点互斥器
 *
 * @author oyzh
 * @since 2022/12/19
 */
public class NodeMutexes {

    /**
     * 节点列表
     */
    @Getter
    private List<Node> nodes;

    /**
     * manage属性是否绑定visible属性
     */
    private Boolean manageBindVisible;

    /**
     * manage属性绑定到visible属性
     */
    public void manageBindVisible() {
        this.setManageBindVisible(true);
    }

    /**
     * 设置manage属性绑定到visible属性
     *
     * @param manageBindVisible 是否绑定
     */
    public void setManageBindVisible(boolean manageBindVisible) {
        this.manageBindVisible = manageBindVisible;
    }

    /**
     * 是否manage属性绑定到visible属性
     *
     * @return 结果
     */
    public boolean isManageBindVisible() {
        return BooleanUtil.isTrue(this.manageBindVisible);
    }

    /**
     * 添加节点
     *
     * @param node 节点
     */
    public void addNode(Node node) {
        if (node != null) {
            if (this.nodes == null) {
                this.nodes = new ArrayList<>();
            }
            this.nodes.add(node);
        }
    }

    /**
     * 添加多个节点
     *
     * @param nodes 多个节点
     */
    public void addNodes(@NonNull Collection<Node> nodes) {
        for (Node node : nodes) {
            this.addNode(node);
        }
    }

    /**
     * 添加多个节点
     *
     * @param nodes 节点集合
     */
    public void addNodes(@NonNull Node... nodes) {
        if (nodes != null) {
            for (Node node : nodes) {
                this.addNode(node);
            }
        }
    }

    /**
     * 设置节点可见
     *
     * @param node 节点
     */
    public void visible(Node node) {
        boolean manageBindVisible = this.isManageBindVisible();
        for (Node node1 : this.nodes) {
            boolean res = node1 == node;
            node1.setVisible(res);
            if (!node1.managedProperty().isBound() && manageBindVisible) {
                node1.setManaged(res);
            }
        }
    }
}
