package cn.oyzh.fx.plus.controls.text;

import cn.oyzh.fx.plus.adapter.LayoutAdapter;
import cn.oyzh.fx.plus.adapter.TextAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import lombok.NonNull;

/**
 * @author oyzh
 * @since 2023/04/25
 */
public class FXText extends Text implements ThemeAdapter, TextAdapter, FontAdapter, LayoutAdapter, NodeAdapter {

    {
        NodeManager.init(this);
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
    public void setFontWeight(FontWeight fontWeight) {
        FontAdapter.super.fontWeight(fontWeight);
    }

    @Override
    public FontWeight getFontWeight() {
        return FontAdapter.super.fontWeight();
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
    public void changeTheme(ThemeStyle style) {
        if (this.isEnableTheme()) {
            ThemeAdapter.super.changeTheme(style);
            if (style.isDarkMode()) {
                this.setFill(Color.WHITE);
            } else {
                this.setFill(Color.BLACK);
            }
        }
    }

    @Override
    public void initNode() {

    }
}
