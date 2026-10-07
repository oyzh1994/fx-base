package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 解锁 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class UnLockSVGGlyph extends SVGGlyph {

    /**
     * 构造解锁 SVG 图标控件
     */
    public UnLockSVGGlyph() {
        super("/fx-svg/unlock.svg");
    }

    /**
     * 构造指定尺寸的解锁 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UnLockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
