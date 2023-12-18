package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.adapter.FontAdapter;
import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.theme.Theme;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.CacheHint;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/04/25
 */
public class FXText extends Text implements ThemeAdapter, TextAdapter, FontAdapter, LayoutAdapter {

    {
        this.setCache(true);
        this.setCacheHint(CacheHint.QUALITY);
        this.changeTheme(ThemeManager.currentTheme());
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

    @Override
    public void changeTheme(Theme theme) {
        ThemeAdapter.super.changeTheme(theme);
        if (theme.isDarkMode()) {
            this.setFill(Color.WHITE);
        } else {
            this.setFill(Color.BLACK);
        }
    }
}
