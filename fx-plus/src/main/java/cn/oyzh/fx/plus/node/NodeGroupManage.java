package cn.oyzh.fx.plus.node;

import cn.hutool.core.util.BooleanUtil;
import javafx.scene.Node;

/**
 * 节点分组管理
 *
 * @author oyzh
 * @since 2022/12/19
 */
public class NodeGroupManage extends NodeGroupAble {

    /**
     * 可见的节点
     */
    private Node visibleNode;

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

    @Override
    protected void onAdd(Node node) {
        if (node == null || this.visibleNode == null) {
            return;
        }
        boolean res = node == this.visibleNode;
        node.setVisible(res);
        if (!node.managedProperty().isBound() && this.isManageBindVisible()) {
            node.setManaged(res);
        }
    }

    /**
     * 设置可见节点
     *
     * @param node 可见节点
     */
    public void visible(Node node) {
        this.visibleNode = node;
        for (Node node1 : this.nodes) {
            this.onAdd(node1);
        }
    }
}
