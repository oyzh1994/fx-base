package cn.oyzh.fx.db.data.dto;

/**
 * 数据传输对象，用于表示一个可勾选的数据传输项。
 *
 * @author oyzh
 * @since 2024-09-06
 */
public class DBDataTransportObject {

    /**
     * 函数名称
     */
    private String name;

    /**
     * 是否选中
     */
    private boolean selected = true;

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
     * 是否选中。
     *
     * @return 选中
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * 设置选中。
     *
     * @param selected 是否选中
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
