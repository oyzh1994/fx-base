package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Cursor;
import javafx.scene.control.Button;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXButton extends Button implements FlexAdapter, NodeGroup, NodeAdapter, ThemeAdapter, MouseAdapter, TipAdapter, StateAdapter, LayoutAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.setPickOnBounds(true);
        this.setMnemonicParsing(false);
//        this.setFocusTraversable(false);
    }

    public FXButton() {
        super();
    }

    public FXButton(String text) {
        super(text);
    }

//    @Override
//    public boolean isEnableTheme() {
//        return ThemeAdapter.super.isEnableTheme();
//    }
//
//    @Override
//    public void setEnableTheme(boolean enableTheme) {
//        ThemeAdapter.super.setEnableTheme(enableTheme);
//    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
