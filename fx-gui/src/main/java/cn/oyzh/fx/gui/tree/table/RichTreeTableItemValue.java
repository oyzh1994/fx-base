package cn.oyzh.fx.gui.tree.table;

import cn.oyzh.common.util.Destroyable;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.theme.ThemeManager;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import lombok.NonNull;


/**
 * 富功能树节点值
 *
 * @author oyzh
 * @since 2023/11/10
 */
public abstract class RichTreeTableItemValue implements Destroyable {

    private RichTreeTableItem<?> item;

    public RichTreeTableItemValue(@NonNull RichTreeTableItem<?> item) {
        this.item = item;
        this.flush();
    }

    public RichTreeTableItem<?> item() {
        return this.item;
    }

    public void flush() {
        this.flushColumn();
        this.flushGraphic();
        this.flushGraphicColor();
    }

    /**
     * 获取当前图标
     *
     * @return 当前图标
     */
    public Node graphic() {
        return this.item.getGraphic();
    }

    /**
     * 设置当前图标
     *
     * @param graphic 当前图标
     */
    public void graphic(Node graphic) {
        if (graphic != null) {
            this.item().setGraphic(graphic);
        }
    }

    /**
     * 刷新文本
     */
    public void flushColumn() {
    }

    /**
     * 刷新图标
     */
    public void flushGraphic() {
        this.graphic(this.graphic());
    }

    /**
     * 刷新图标颜色
     */
    public void flushGraphicColor() {
        if (this.graphic() instanceof SVGGlyph glyph) {
            // 移除等待动画设置的颜色，避免被重复覆盖
            glyph.removeProp("_color");
            // 设置颜色
            if (ThemeManager.isDarkMode()) {
                glyph.setColor(Color.WHITE);
            } else {
                glyph.setColor(Color.BLACK);
            }
        }
    }

    @Override
    public void destroy() {
        this.item = null;
    }


}
