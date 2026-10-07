package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 取消暂停 SVG 图标控件
 *
 * @author oyzh
 * @since 2025-03-13
 */
public class UnPauseSVGGlyph extends SVGGlyph {

    /**
     * 构造取消暂停 SVG 图标控件
     */
    public UnPauseSVGGlyph() {
        super("/fx-svg/un-pause.svg");
    }

    /**
     * 构造指定尺寸的取消暂停 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public UnPauseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
