package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.CheckBox;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXCheckBox extends CheckBox implements NodeGroup, NodeAdapter, ThemeAdapter, TipAdapter, StateAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    public FXCheckBox() {
        super();
    }

    public FXCheckBox(String text) {
        super(text);
    }

    /**
     * 选中变更事件
     *
     * @param listener 监听器
     */
    public void selectedChanged(@NonNull ChangeListener<Boolean> listener) {
        this.selectedProperty().addListener(listener);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
        this.setFocusTraversable(false);
    }
}
