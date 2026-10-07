package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 暂停 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/08/12
 */
public class PauseSVGGlyph extends SVGGlyph {

    /**
     * 构造暂停 SVG 图标控件
     */
    public PauseSVGGlyph() {
        super("/fx-svg/pause.svg");
    }

    /**
     * 构造指定尺寸的暂停 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PauseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

}
