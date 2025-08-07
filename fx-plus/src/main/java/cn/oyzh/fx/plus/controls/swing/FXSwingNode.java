package cn.oyzh.fx.plus.controls.swing;

import cn.oyzh.fx.plus.adapter.PropAdapter;
import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.flex.FlexAdapter;
import cn.oyzh.fx.plus.font.FontAdapter;
import cn.oyzh.fx.plus.node.NodeAdapter;
import cn.oyzh.fx.plus.node.NodeGroup;
import cn.oyzh.fx.plus.node.NodeManager;
import cn.oyzh.fx.plus.swing.SwingUtil;
import cn.oyzh.fx.plus.theme.ThemeAdapter;
import javafx.embed.swing.SwingNode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.awt.Component;
import java.awt.Dimension;

/**
 * @author oyzh
 * @since 2025-08-04
 */
public class FXSwingNode extends SwingNode implements NodeGroup, ThemeAdapter, FlexAdapter, PropAdapter, TipAdapter, FontAdapter {

    {
        NodeManager.init(this);
    }

    public void setSize(double width, double height) {
        SwingUtil.runWait(() -> {
            this.getContent().setSize((int) width, (int) height);
            this.getContent().setMinimumSize(new Dimension((int) width, (int) height));
        });
    }

    public void setWidth(double width) {
        this.setSize(width, this.getHeight());
    }

    public double getWidth() {
        return this.getContent().getWidth();
    }

    public void setHeight(double height) {
        this.setSize(this.getWidth(), height);
    }

    public double getHeight() {
        return this.getContent().getHeight();
    }

    @Override
    public void resize(double width, double height) {
        double[] size = this.computeSize(width, height);
        this.setSize(size[0], size[1]);
        super.resize(size[0], size[1]);
    }

    /**
     * 获取真实组件，部分组件被滚动条等包裹了
     *
     * @return 真实组件
     */
    protected Component realComponent() {
        return this.getContent();
    }

    /**
     * 获取真实组件字体
     *
     * @return 真实组件字体
     */
    protected java.awt.Font getRealComponentFont() {
        if (this.realComponent() != null&& this.getContent() != null) {
            return this.realComponent().getFont();
        }
        return null;
    }

    /**
     * 设置真实组件字体
     *
     * @param font 字体
     */
    protected void setRealComponentFont(java.awt.Font font) {
        if (this.realComponent() != null && this.getContent() != null) {
            this.realComponent().setFont(font);
        }
    }

    @Override
    public void setFont(Font font) {
        if (this.realComponent() != null) {
            this.setRealComponentFont(SwingUtil.fromFxFont(font));
        }
    }

    @Override
    public Font getFont() {
        java.awt.Font font = this.getRealComponentFont();
        return SwingUtil.toFxFont(font);
    }

    @Override
    public void setFontSize(double fontSize) {
        java.awt.Font font = this.getRealComponentFont();
        if (font != null) {
            java.awt.Font newFont = new java.awt.Font(font.getFamily(), font.getStyle(), (int) fontSize);
            this.setRealComponentFont(newFont);
        }
    }

    @Override
    public void setFontFamily(String fontFamily) {
        java.awt.Font font = this.getRealComponentFont();
        if (font != null) {
            java.awt.Font newFont = new java.awt.Font(fontFamily, font.getStyle(), font.getSize());
            this.setRealComponentFont(newFont);
        }
    }

    @Override
    public void setFontWeight(FontWeight fontWeight) {
        java.awt.Font font = this.getRealComponentFont();
        if (font != null) {
            String family = SwingUtil.fromFxWeight(font.getFamily(), fontWeight);
            java.awt.Font newFont = new java.awt.Font(family, font.getStyle(), font.getSize());
            this.setRealComponentFont(newFont);
        }
    }
}
