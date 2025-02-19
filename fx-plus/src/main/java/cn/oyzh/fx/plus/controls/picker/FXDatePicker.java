package cn.oyzh.fx.plus.controls.picker;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.DatePicker;

public class FXDatePicker extends DatePicker implements ThemeAdapter, FlexAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }
}
