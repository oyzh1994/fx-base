package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.MouseAdapter;
import cn.oyzh.fx.plus.adapter.StateAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.event.EventHandler;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2020/10/29
 */
public class FXLabel extends Label implements ThemeAdapter, MouseAdapter, TextAdapter, TipAdapter, StateAdapter, FontAdapter, LayoutAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
    }

    public FXLabel() {
        super("");
    }

    public FXLabel(String text) {
        super(text);
    }

    public FXLabel(String text, Node graphic) {
        super(text, graphic);
    }

    @Override
    public void setOnMousePrimaryClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMousePrimaryClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMousePrimaryClicked() {
        return MouseAdapter.super.getOnMousePrimaryClicked();
    }

    @Override
    public void setOnMouseSecondClicked(EventHandler<? super MouseEvent> handler) {
        MouseAdapter.super.setOnMouseSecondClicked(handler);
    }

    @Override
    public EventHandler<? super MouseEvent> getOnMouseSecondClicked() {
        return MouseAdapter.super.getOnMouseSecondClicked();
    }

    @Override
    public void setTipText(String tipTitle) {
        TipAdapter.super._setTipText(tipTitle);
    }

    @Override
    public String getTipText() {
        return TipAdapter.super._getTipText();
    }

    @Override
    public void setFontSize(double fontSize) {
        FontAdapter.super._setFontSize(fontSize);
    }

    @Override
    public double getFontSize() {
        return FontAdapter.super._getFontSize();
    }

    @Override
    public void setFontFamily(@NonNull String fontFamily) {
        FontAdapter.super._setFontFamily(fontFamily);
    }

    @Override
    public String getFontFamily() {
        return FontAdapter.super._getFontFamily();
    }

    @Override
    public double getRealWidth() {
        return LayoutAdapter.super._getRealWidth();
    }

    @Override
    public void setRealWidth(double width) {
        LayoutAdapter.super._setRealWidth(width);
    }

    @Override
    public double getRealHeight() {
        return LayoutAdapter.super._getRealHeight();
    }

    @Override
    public void setRealHeight(double height) {
        LayoutAdapter.super._setRealHeight(height);
    }
}
