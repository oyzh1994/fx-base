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
 * @since 2023-11-10
 */
public class FXTreeItemValue implements Destroyable {

    /**
     * 图形引用
     */
    private WeakReference<SVGGlyph> graphic;

    /**
     * 获取图形
     *
     * @return 图形
     */
    public SVGGlyph graphic() {
        return graphic == null ? null : graphic.get();
    }

    /**
     * 设置图形
     *
     * @param graphic 图形
     */
    public void graphic(SVGGlyph graphic) {
        this.graphic = new WeakReference<>(graphic);
    }

    /**
     * 节点引用
     */
    private WeakReference<FXTreeItem<?>> item;

    /**
     * 获取节点
     *
     * @return 节点
     */
    public FXTreeItem<?> item() {
        return this.item == null ? null : this.item.get();
    }

    /**
     * 构造树项值对象。
     */
    public FXTreeItemValue() {
    }

    /**
     * 构造树项值对象。
     *
     * @param item 项
     */
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
