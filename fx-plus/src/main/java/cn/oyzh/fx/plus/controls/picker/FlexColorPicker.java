package cn.oyzh.fx.plus.controls.picker;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.util.FXColorUtil;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/04/04
 */
public class FlexColorPicker extends ColorPicker implements FlexAdapter, TipAdapter, FontAdapter, ThemeAdapter {

    {
        NodeManager.init(this);
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    public void setColor(String color) {
        if (StringUtil.isNotEmpty(color)) {
            try {
                this.setValue(Color.valueOf(color));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getColor() {
        try {
            return FXColorUtil.getColorHex(this.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void initNode() {
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
//        this.setFocusTraversable(false);
    }
}
