package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 桶 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class BucketSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public BucketSVGGlyph() {
        super("/fx-svg/bucket.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public BucketSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
