package cn.oyzh.fx.plus.controls.tree.view;

import cn.oyzh.common.object.Destroyable;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.paint.Color;

import java.lang.ref.WeakReference;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public class FXTreeItemValue implements Destroyable {

    private WeakReference<SVGGlyph> graphic;

    public SVGGlyph graphic() {
        return graphic == null ? null : graphic.get();
    }

    public void graphic(SVGGlyph graphic) {
        this.graphic = new WeakReference<>(graphic);
    }

    private WeakReference<FXTreeItem<?>> item;

    public FXTreeItem<?> item() {
        return this.item == null ? null : this.item.get();
    }

    public FXTreeItemValue() {
    }

    public FXTreeItemValue(FXTreeItem<?> item) {
        this.item = new WeakReference<>(item);
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
        return ThemeManager.currentForegroundColor();
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
        return ThemeManager.currentForegroundColor();
    }

    @Override
    public void destroy() {
        if (this.item != null) {
            this.item.clear();
        }
        if (this.graphic != null) {
            this.graphic.clear();
        }
        this.item = null;
        this.graphic = null;
    }
}
