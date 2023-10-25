package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.NodeAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.Node;
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

    public FXHBox() {
        super();
    }

    public FXHBox(Node... children) {
        super(children);
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
