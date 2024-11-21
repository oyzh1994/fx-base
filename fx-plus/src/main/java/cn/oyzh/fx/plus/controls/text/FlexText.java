package cn.oyzh.fx.plus.controls.text;

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
        double[] size = this.computeSize(width, height);
        super.resize(size[0], size[1]);
        this.resizeNode();
    }

    @Override
    public String getFlexWidth() {
        return FlexAdapter.super.flexWidth();
    }

    @Override
    public void setFlexWidth(String flexWidth) {
        FlexAdapter.super.flexWidth(flexWidth);
    }

    public String getFlexHeight() {
        return FlexAdapter.super.flexHeight();
    }

    @Override
    public void setFlexHeight(String flexHeight) {
        FlexAdapter.super.flexHeight(flexHeight);
    }

    @Override
    public String getFlexX() {
        return FlexAdapter.super.flexX();
    }

    @Override
    public void setFlexX(String flexX) {
        FlexAdapter.super.flexX(flexX);
    }

    @Override
    public String getFlexY() {
        return FlexAdapter.super.flexY();
    }

    @Override
    public void setFlexY(String flexY) {
        FlexAdapter.super.flexY(flexY);
    }

    // @Override
    // public void setStateManager(StateManager manager) {
    //     FlexAdapter.super.stateManager(manager);
    // }
    //
    // @Override
    // public StateManager getStateManager() {
    //     return FlexAdapter.super.stateManager();
    // }
}
