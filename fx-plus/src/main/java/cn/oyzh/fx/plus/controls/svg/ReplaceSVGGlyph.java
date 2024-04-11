package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.BaseResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ReplaceSVGGlyph extends SVGGlyph {

    public ReplaceSVGGlyph() {
        this.setUrl("/fx-plus/font/financial_replace.svg");
    }

    public ReplaceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
