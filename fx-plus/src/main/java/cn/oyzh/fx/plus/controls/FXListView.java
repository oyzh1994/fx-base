package cn.oyzh.fx.plus.controls;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.SelectAdapter;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.control.ListView;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/4/24
 */
public class FXListView<T> extends ListView<T> implements ThemeAdapter, LayoutAdapter, FontAdapter, SelectAdapter<T> {

    {
        this.setCache(true);
        this.setCacheShape(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.setCursor(Cursor.HAND);
    }

    @Override
    public void setInitIndex(int initIndex) {
        SelectAdapter.super.setInitIndex(initIndex);
    }

    @Override
    public int getInitIndex() {
        return SelectAdapter.super.getInitIndex();
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
}
