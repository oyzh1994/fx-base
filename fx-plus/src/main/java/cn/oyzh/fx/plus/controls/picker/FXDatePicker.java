package cn.oyzh.fx.plus.controls.picker;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.control.DatePicker;

/**
 * 日期选择器控件
 *
 * @author oyzh
 * @since 2023-10-09
 */
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
