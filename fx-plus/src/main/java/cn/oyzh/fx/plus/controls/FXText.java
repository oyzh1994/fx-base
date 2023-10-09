package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import javafx.scene.CacheHint;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/04/25
 */
public class FXText extends Text implements TextAdapter, FontAdapter, LayoutAdapter {

    {
        this.setCache(true);
        this.setCacheHint(CacheHint.QUALITY);
    }

    public FXText() {
        super();
    }

    public FXText(String text) {
        super(text);
    }

    @Override
    public boolean isResizable() {
        return true;
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
