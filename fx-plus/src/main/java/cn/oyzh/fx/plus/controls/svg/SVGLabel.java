package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.controls.label.FXLabel;
import javafx.scene.Cursor;
import javafx.scene.paint.Paint;

/**
 * svg标签控件
 *
 * @author oyzh
 * @since 2022/12/16
 */
public class SVGLabel extends FXLabel {

    {
        this.setCursor(Cursor.HAND);
    }

    /**
     * 构造标签对象。
     */
    public SVGLabel() {
        super("");
    }

    /**
     * 构造标签对象。
     *
     * @param text 文本
     */
    public SVGLabel(String text) {
        super(text);
    }

    /**
     * 构造标签对象。
     *
     * @param text 文本
     * @param graphic 图形
     */
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
     *
     * @param url 图标URL地址
     */
    public void setUrl(String url) {
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
     *
     * @param size 尺寸
     */
    public void setSize(double size) {
        if (this.graphic() != null) {
            this.graphic().setSize(size);
        }
    }

    /**
     * 获取尺寸
     *
     * @return 尺寸大小，图形对象为空时返回 0
     */
    public double getSize() {
        if (this.graphic() != null) {
            return this.graphic().getSize();
        }
        return 0.d;
    }

    /**
     * 设置尺寸
     *
     * @param size 尺寸
     */
    public void setSizeStr(String size) {
        if (this.graphic() != null) {
            this.graphic().setSizeStr(size);
        }
    }

    /**
     * 获取尺寸字符串
     *
     * @return 尺寸字符串，图形对象为空时返回 null
     */
    public String getSizeStr() {
        if (this.graphic() != null) {
            return this.graphic().getSizeStr();
        }
        return null;
    }


    /**
     * 获取颜色
     *
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
     *
     * @param color 颜色对象
     */
    public void setColor(Paint color) {
        if (this.graphic() != null) {
            this.graphic().setColor(color);
        }
    }
}
