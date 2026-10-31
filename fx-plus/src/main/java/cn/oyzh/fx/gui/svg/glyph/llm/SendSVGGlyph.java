package cn.oyzh.fx.gui.svg.glyph.llm;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 关于 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-05-14
 */
public class SendSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public SendSVGGlyph() {
        super("/fx-svg/llm/send.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public SendSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
