package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/07/15
 */
public class MinusSVGGlyph extends SVGGlyph {

    public MinusSVGGlyph() {
        this.setUrl("/fx-plus/font/minus.svg");
    }

    public MinusSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.minus());
        super.initNode();
    }
}
