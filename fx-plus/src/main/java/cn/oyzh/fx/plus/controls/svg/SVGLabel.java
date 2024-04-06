package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.controls.text.FXLabel;
import javafx.scene.Cursor;
import javafx.scene.paint.Paint;
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

    /**
     * 设置URL地址
     * @param url 图标URL地址
     */
    public void setUrl(@NonNull String url) {
        if (this.graphic() == null) {
            this.setGraphic(new SVGGlyph(url));
        }
    }

    /**
     * 获取URL地址
     *
     * @return URL地址，如果图形对象为空则返回null
     */
    public String getUrl() {
        if (this.graphic() != null) {
            return this.graphic().getUrl();
        }
        return null;
    }

    /**
     * 设置尺寸
     * @param size 尺寸
     */
    public void setSize( double size) {
        if (this.graphic() != null) {
            this.graphic().setSize(size);
        }
    }

    /**
     * 获取尺寸的大小。
     *
     * @return 尺寸的大小，如果图形对象为空则返回null。
     */
    public double getSize() {
        if (this.graphic() != null) {
            return this.graphic().getSize();
        }
        return 0.d;
    }

    /**
     * 获取颜色
     * @return 颜色对象，如果图形为空，则返回null
     */
    public Paint getColor() {
        if (this.graphic() != null) {
            return this.graphic().getColor();
        }
        return null;
    }

    /**
     * 设置颜色
     * @param color 颜色对象
     */
    public void setColor(Paint color) {
        if (this.graphic() != null) {
            this.graphic().setColor(color);
        }
    }
}
