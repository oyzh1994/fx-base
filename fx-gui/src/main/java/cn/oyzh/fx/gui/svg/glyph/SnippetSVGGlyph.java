package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class SnippetSVGGlyph extends SVGGlyph {

    public SnippetSVGGlyph() {
        this.setUrl("/fx-svg/snippet.svg");
    }

    public SnippetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
