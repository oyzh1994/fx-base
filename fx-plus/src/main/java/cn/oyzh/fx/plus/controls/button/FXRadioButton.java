package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Cursor;
import javafx.scene.control.RadioButton;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class FXRadioButton extends RadioButton implements NodeGroup, NodeAdapter, ThemeAdapter, StateAdapter, LayoutAdapter, TipAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setMnemonicParsing(false);
    }
}
