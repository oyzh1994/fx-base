package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/07/15
 */
public class MinusSVGGlyph extends SVGGlyph {

    public MinusSVGGlyph() {
        this.setUrl("/fx-svg/minus.svg");
    }

    public MinusSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.minus());
//        super.initNode();
//    }
}
