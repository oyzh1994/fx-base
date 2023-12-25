package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.NodeAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.CacheHint;
import javafx.scene.layout.FlowPane;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/12/25
 */
public class FXFlowPane extends FlowPane implements ThemeAdapter, FontAdapter, TipAdapter, StateAdapter, NodeAdapter, LayoutAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.changeTheme(ThemeManager.currentTheme());
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super.fontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super.fontSize();
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public void setTipText(String tipText) {
        TipAdapter.super.tipText(tipText);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super.tipText();
    }

    @Override
    public void setStateManager(StateManager manager) {
        StateAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return StateAdapter.super.stateManager();
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
}
