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

    public boolean isRichMode() {
        return richMode;
    }

    public void setRichMode(boolean richMode) {
        this.richMode = richMode;
    }

    public RichTreeItemValue() {
        super();
    }

    public RichTreeItemValue(RichTreeItem<?> item) {
        super(item);
    }
}
