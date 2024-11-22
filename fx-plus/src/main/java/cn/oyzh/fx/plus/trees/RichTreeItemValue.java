package cn.oyzh.fx.plus.trees;

import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.paint.Color;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class RichTreeItemValue implements Destroyable {

    private RichTreeItem<?> item;

    public RichTreeItemValue() {
    }

    public RichTreeItemValue(RichTreeItem<?> item) {
        this.item = item;
    }

    protected RichTreeItem<?> item() {
        return this.item;
    }

    public String name() {
        return null;
    }

    public String extra() {
        return null;
    }

    public String text() {
        String text = this.name();
        String extra = this.extra();
        if (text == null) {
            text = extra;
        } else if (extra != null) {
            text += extra;
        }
        return text;
    }

    /**
     * 刷新图标颜色
     */
    public SVGGlyph graphic() {
        return null;
    }

    /**
     * 刷新图标颜色
     */
    public Color graphicColor() {
        if (this.graphic() instanceof SVGGlyph glyph) {
            // 移除等待动画设置的颜色，避免被重复覆盖
            glyph.removeProp("_color");
        }
        // 设置颜色
        if (ThemeManager.isDarkMode()) {
            return Color.WHITE;
        }
        return Color.BLACK;
    }

    @Override
    public void destroy() {
        this.item = null;
    }
}
