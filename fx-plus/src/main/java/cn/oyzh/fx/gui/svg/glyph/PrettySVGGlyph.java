package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/08/13
 */
public class PrettySVGGlyph extends SVGGlyph {

    public PrettySVGGlyph() {
        this.setUrl("/fx-svg/pretty.svg");
    }

    public PrettySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.pretty());
//        super.initNode();
//    }
}
