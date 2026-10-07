package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 音乐 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-04-11
 */
public class MusicSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public MusicSVGGlyph() {
        super("/fx-svg/music.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public MusicSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
