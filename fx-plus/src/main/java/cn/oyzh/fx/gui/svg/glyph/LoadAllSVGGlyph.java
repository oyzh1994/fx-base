package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 全部加载 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class LoadAllSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public LoadAllSVGGlyph() {
        super("/fx-svg/reload-time.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public LoadAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
