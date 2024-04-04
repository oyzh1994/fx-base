package cn.oyzh.fx.plus.controls.picker;

import cn.hutool.core.util.StrUtil;
import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.handler.StateManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2024/04/04
 */
public class FlexColorPicker extends ColorPicker implements FlexAdapter, TipAdapter, FontAdapter, ThemeAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setPickOnBounds(true);
        this.setCursor(Cursor.HAND);
        this.setFocusTraversable(false);
        this.changeTheme(ThemeManager.currentTheme());
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

    @Override
    public void setStateManager(StateManager manager) {
        FlexAdapter.super.stateManager(manager);
    }

    @Override
    public StateManager getStateManager() {
        return FlexAdapter.super.stateManager();
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
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super.fontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super.fontFamily();
    }

    @Override
    public double getRealWidth() {
        return FlexAdapter.super.realWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super.realWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super.realHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super.realHeight(height);
    }

    public void setColor(String color) {
        if (StrUtil.isNotEmpty(color)) {
            try {
                this.setValue(Color.valueOf(color));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public String getColor() {
        try {
            return ThemeUtil.getColorHex(this.getValue());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
