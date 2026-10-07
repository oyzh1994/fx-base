package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.adapter.TipAdapter;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.mouse.MouseAdapter;
import javafx.geometry.Insets;
import javafx.scene.Node;

/**
 * svg面板
 *
 * @author oyzh
 * @since 2025-01-07
 */
public class SVGPane extends FXPane implements MouseAdapter, TipAdapter {

    /**
     * 尺寸，格式为“宽”或“宽,高”
     */
    protected String size;

    /**
     * 获取尺寸字符串
     *
     * @return 尺寸字符串
     */
    public String getSize() {
        return size;
    }

    /**
     * 设置尺寸并同步到子节点
     *
     * @param size 尺寸字符串，格式为“宽”或“宽,高”
     */
    public void setSize(String size) {
        this.size = size;

        Node node = this.getChild(0);
        if (node instanceof SVGGlyph glyph) {
            glyph.setSizeStr(size);
        } else if (node instanceof SVGLabel label) {
            label.setSizeStr(size);
        }
    }

    /**
     * 获取尺寸的宽
     *
     * @return 宽，未设置尺寸时返回 NaN
     */
    public double getSizeWidth() {
        if (this.size == null) {
            return Double.NaN;
        }
        if (this.size.contains(",")) {
            return Double.parseDouble(this.size.split(",")[0].trim());
        }
        return Double.parseDouble(this.size);
    }

    /**
     * 获取尺寸的高
     *
     * @return 高，未设置尺寸时返回 NaN
     */
    public double getSizeHeight() {
        if (this.size == null) {
            return Double.NaN;
        }
        if (this.size.contains(",")) {
            return Double.parseDouble(this.size.split(",")[1].trim());
        }
        return Double.parseDouble(this.size);
    }

    @Override
    public void initNode() {
        this.setPadding(Insets.EMPTY);
        super.initNode();
    }
}
