package cn.oyzh.fx.gui.svg.glyph.llm;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 关于 SVG 图标控件
 *
 * @author oyzh
 * @since 2024-05-14
 */
public class ChatSVGGlyph extends SVGGlyph {

    /**
     * 构造 SVG 图标控件
     */
    public ChatSVGGlyph() {
        super("/fx-svg/llm/chat.svg");
    }

    /**
     * 构造指定尺寸的 SVG 图标控件
     *
     * @param size 尺寸
     */
    public ChatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
