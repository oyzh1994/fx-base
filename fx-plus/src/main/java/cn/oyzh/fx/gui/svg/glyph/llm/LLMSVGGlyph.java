package cn.oyzh.fx.gui.svg.glyph.llm;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 关于 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-05-14
 */
public class LLMSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public LLMSVGGlyph() {
        super("/fx-svg/llm/llm.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public LLMSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
