package cn.oyzh.fx.plus.controls.tree.view;

import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.experimental.Accessors;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class FXTreeItemValue implements Destroyable {

    @Getter
    @Accessors(fluent = true, chain = false)
    protected SVGGlyph graphic;

    protected FXTreeItem<?> item;

    public FXTreeItemValue() {
    }

    public FXTreeItemValue(FXTreeItem<?> item) {
        this.item = item;
    }

    protected FXTreeItem<?> item() {
        return this.item;
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String name() {
        return null;
    }

    /**
     * 获取额外内容
     *
     * @return 额外内容
     */
    public String extra() {
        return null;
    }

    /**
     * 获取额外内容颜色
     *
     * @return 额外内容颜色
     */
    public Color extraColor() {
        return null;
    }

    /**
     * 获取显示文本
     *
     * @return 显示文本
     */
    public String text() {
        String text = this.name();
        String extra = this.extra();
        if (text == null) {
            text = extra;
        } else if (extra != null) {
            text = text + extra;
        }
        return text;
    }

    /**
     * 获取图标颜色
     *
     * @return 图标颜色
     */
    public Color graphicColor() {
//        if (ThemeManager.isDarkMode()) {
//            return Color.WHITE;
//        }
//        return Color.BLACK;
        return ThemeManager.currentForegroundColor();
    }

    @Override
    public void destroy() {
        this.item = null;
        this.graphic = null;
    }
}
