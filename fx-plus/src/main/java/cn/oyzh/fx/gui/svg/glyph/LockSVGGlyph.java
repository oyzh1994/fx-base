package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 锁定 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class LockSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public LockSVGGlyph() {
        super("/fx-svg/lock.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public LockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
