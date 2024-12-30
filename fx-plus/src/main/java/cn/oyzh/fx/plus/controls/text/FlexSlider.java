package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.Cursor;
import javafx.scene.control.Slider;

/**
 * @author oyzh
 * @since 2024/4/20
 */
public class FlexSlider extends Slider implements FlexAdapter, ThemeAdapter, TipAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public void initNode() {
        this.setCursor(Cursor.HAND);
        this.valueProperty().addListener((observableValue, number, t1) -> this.setTipText(t1 != null ? t1.intValue() + "" : ""));
    }
}
