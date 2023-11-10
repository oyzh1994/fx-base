package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.controls.text.FXLabel;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import lombok.NonNull;

/**
 * svg label
 *
 * @author oyzh
 * @since 2022/12/16
 */
public class SVGLabel extends FXLabel {

    {
        this.setCursor(Cursor.HAND);
    }

    public SVGLabel() {
        super("");
    }

    public SVGLabel(String text) {
        super(text);
    }

    public SVGLabel(String text, SVGGlyph graphic) {
        super(text, graphic);
    }

    /**
     * 获取图标
     *
     * @return 图标
     */
    public SVGGlyph graphic() {
        return (SVGGlyph) this.getGraphic();
    }

    public void setUrl(@NonNull String url) {
        if (this.graphic() == null) {
            this.setGraphic(new SVGGlyph(url));
        }
    }

    public String getUrl() {
        if (this.graphic() != null) {
            return this.graphic().getUrl();
        }
        return null;
    }

    public void setSize(@NonNull Number size) {
        if (this.graphic() != null) {
            this.graphic().setSize(size);
        }
    }

    public Number getSize() {
        if (this.graphic() != null) {
            return this.graphic().getSize();
        }
        return null;
    }

    public Color getColor() {
        if (this.graphic() != null) {
            return this.graphic().getColor();
        }
        return null;
    }

    public void setColor(Color color) {
        if (this.graphic() != null) {
            this.graphic().setColor(color);
        }
    }
}
