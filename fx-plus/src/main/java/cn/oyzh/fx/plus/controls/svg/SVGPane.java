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
 * @since 2025/01/07
 */
public class SVGPane extends FXPane implements MouseAdapter, TipAdapter {

    {
        this.setPadding(Insets.EMPTY);
    }

    protected String size;

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;

        Node node = this.getChild(0);
        if (node instanceof SVGGlyph glyph) {
            glyph.setSizeStr(size);
        } else if (node instanceof SVGLabel label) {
            label.setSizeStr(size);
        }
    }

    public double getSizeWidth() {
        if (this.size == null) {
            return Double.NaN;
        }
        if (this.size.contains(",")) {
            return Double.parseDouble(this.size.split(",")[0].trim());
        }
        return Double.parseDouble(this.size);
    }

    public double getSizeHeight() {
        if (this.size == null) {
            return Double.NaN;
        }
        if (this.size.contains(",")) {
            return Double.parseDouble(this.size.split(",")[1].trim());
        }
        return Double.parseDouble(this.size);
    }
}
