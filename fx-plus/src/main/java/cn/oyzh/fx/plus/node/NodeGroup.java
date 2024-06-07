package cn.oyzh.fx.plus.node;

import cn.oyzh.fx.plus.adapter.PropAdapter;

/**
 * 节点分组
 *
 * @author oyzh
 * @since 2024/06/08
 */
public interface NodeGroup extends PropAdapter {

    /**
     * 设置分组id
     *
     * @param groupId 分组id
     */
    void setGroupId(String groupId);

    /**
     * 获取分组id
     * @return 分组id
     */
    String getGroupId();

    /**
     * 设置分组id
     *
     * @param groupId 分组id
     */
    default void groupId(String groupId) {
        this.setProp("_groupId", groupId);
    }

    /**
     * 获取分组id
     * @return 分组id
     */
    default String groupId() {
        return this.getProp("_groupId");
    }
}
