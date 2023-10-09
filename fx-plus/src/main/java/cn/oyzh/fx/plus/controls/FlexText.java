package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;

/**
 * @author oyzh
 * @since 2022/06/04
 */
public class FlexText extends FXText implements ThemeAdapter, FlexAdapter {

    public FlexText() {
        super();
    }

    public FlexText(String text) {
        super(text);
    }

    @Override
    public void resize(double width, double height) {
        double computeWidth = this.computeWidth(width);
        double computeHeight = this.computeHeight(height);
        super.resize(computeWidth, computeHeight);
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super._getFlexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super._setFlexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super._getFlexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super._setFlexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super._getFlexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super._setFlexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super._getFlexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super._setFlexY(flexY);
    }
}
