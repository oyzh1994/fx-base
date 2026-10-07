package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 代码片段 SVG 图标控件
 *
 * @author oyzh
 * @since 2024/4/11
 */
public class SnippetSVGGlyph extends SVGGlyph {

    /**
     * 构造代码片段 SVG 图标控件
     */
    public SnippetSVGGlyph() {
        super("/fx-svg/snippet.svg");
    }

    /**
     * 构造指定尺寸的代码片段 SVG 图标控件
     *
     * @param size 图标尺寸
     */
    public SnippetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
