package cn.oyzh.fx.gui.setting;

import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;

/**
 * 设置左侧树节点值
 *
 * @author oyzh
 * @since 2024-12-29
 */
public class SettingLeftTreeItemValue extends RichTreeItemValue {

    /**
     * 节点标识
     */
    private String id;

    /**
     * 节点名称
     */
    private String name;

    /**
     * 父节点标识
     */
    private String parentId;

    /**
     * 构造节点值
     *
     * @param name 节点名称
     */
    public SettingLeftTreeItemValue(String name ) {
        this.name = name;
    }

    /**
     * 构造节点值
     *
     * @param name 节点名称
     * @param id   节点标识
     */
    public SettingLeftTreeItemValue(String name, String id) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String name() {
        return this.name;
    }

    /**
     * 创建节点值
     *
     * @param name 节点名称
     * @param id   节点标识
     * @return 节点值
     */
    public static SettingLeftTreeItemValue of(String name, String id){
        return new SettingLeftTreeItemValue(name, id);
    }

    /**
     * 获取标识。
     *
     * @return 标识
     */
    public String getId() {
        return id;
    }

    /**
     * 设置标识。
     *
     * @param id 标识
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取名称。
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称。
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取父级标识。
     *
     * @return 父级标识
     */
    public String getParentId() {
        return parentId;
    }

    /**
     * 设置父级标识。
     *
     * @param parentId 父级标识
     */
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
}
