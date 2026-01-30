package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class BucketSVGGlyph extends SVGGlyph {

    public BucketSVGGlyph() {
        this.setUrl("/fx-svg/bucket.svg");
    }

    public BucketSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
