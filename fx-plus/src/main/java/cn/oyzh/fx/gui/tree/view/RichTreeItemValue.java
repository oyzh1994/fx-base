package cn.oyzh.fx.gui.tree.view;

import cn.oyzh.fx.plus.controls.tree.view.FXTreeItemValue;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeItemValue extends FXTreeItemValue {

    /**
     * 富文本模式
     */
    private boolean richMode;

    /**
     * 是否富文本模式
     *
     * @return 是否富文本模式
     */
    public boolean isRichMode() {
        return richMode;
    }

    /**
     * 设置富文本模式
     *
     * @param richMode 是否富文本模式
     */
    public void setRichMode(boolean richMode) {
        this.richMode = richMode;
    }

    /**
     * 构造富功能树节点值
     */
    public RichTreeItemValue() {
        super();
    }

    /**
     * 构造富功能树节点值
     *
     * @param item 所属树节点
     */
    public RichTreeItemValue(RichTreeItem<?> item) {
        super(item);
    }
}
