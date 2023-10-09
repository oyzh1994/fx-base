package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.NodeAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.layout.HBox;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2022/1/19
 */
public class FXHBox extends HBox implements ThemeAdapter, LayoutAdapter, FontAdapter, NodeAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
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
