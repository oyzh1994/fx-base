package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;

/**
 * 复选框控件，继承自 CheckBox，支持主题、字体、状态、提示等适配
 *
 * @author oyzh
 * @since 2020-10-29
 */
public class FXCheckBox extends CheckBox implements NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    /**
     * 构造检查面板对象。
     */
    public FXCheckBox() {
        super();
    }

    /**
     * 构造检查面板对象。
     *
     * @param selected 是否选中
     */
    public FXCheckBox(boolean selected) {
        super();
        this.setSelected(selected);
    }

    /**
     * 构造检查面板对象。
     *
     * @param text 文本
     */
    public FXCheckBox(String text) {
        super(text);
    }

    /**
     * 构造检查面板对象。
     *
     * @param text 文本
     * @param selected 是否选中
     */
    public FXCheckBox(String text, boolean selected) {
        super(text);
        this.setSelected(selected);
    }

    /**
     * 选中变更事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
        NodeAdapter.super.initNode();
    }

    /**
     * 反转选择
     */
    public void reversalSelected() {
        this.setSelected(!this.isSelected());
    }
}
