package cn.oyzh.fx.plus.node;

import javafx.scene.Node;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 节点分组能力
 *
 * @author oyzh
 * @since 2023/08/09
 */
public abstract class NodeGroupAble {

    /**
     * 节点列表
     */
    @Getter
    protected List<Node> nodes;

    /**
     * 节点添加事件
     *
     * @param node 节点
     */
    protected abstract void onAdd(Node node);

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
            this.onAdd(node);
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
}
