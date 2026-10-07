package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 播放 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class PlaySVGGlyph extends SVGGlyph {

    /**
     * 构造播放 SVG 图标控件
     */
    public PlaySVGGlyph() {
        super("/fx-svg/play-circle.svg");
    }

    /**
     * 构造指定尺寸的播放 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public PlaySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
