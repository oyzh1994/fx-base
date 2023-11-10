package cn.oyzh.fx.plus.controls.button;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.control.RadioButton;

/**
 * @author oyzh
 * @since 2022/1/20
 */
public class FXRadioButton extends RadioButton implements ThemeAdapter, StateAdapter, LayoutAdapter, TipAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setCursor(Cursor.HAND);
        this.setMnemonicParsing(false);
    }

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super.tipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public double getRealWidth() {
        return LayoutAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        LayoutAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return LayoutAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        LayoutAdapter.super.realHeight(height);
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
    }
}
