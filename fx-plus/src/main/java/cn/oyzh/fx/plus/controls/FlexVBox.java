package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.layout.VBox;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2022/06/03
 */
public class FlexVBox extends VBox implements ThemeAdapter, FlexAdapter, FontAdapter {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
    }

    @Override
    public void resize(double width, double height) {
        double computeWidth = this.computeWidth(width);
        double computeHeight = this.computeHeight(height);
        super.resize(computeWidth, computeHeight);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
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
        return FlexAdapter.super._getRealWidth();
    }

    @Override
    public void setRealWidth(double width) {
        FlexAdapter.super._setRealWidth(width);
    }

    @Override
    public double getRealHeight() {
        return FlexAdapter.super._getRealHeight();
    }

    @Override
    public void setRealHeight(double height) {
        FlexAdapter.super._setRealHeight(height);
    }
}
