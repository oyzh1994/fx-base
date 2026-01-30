package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class AddSVGGlyph extends SVGGlyph {

    public AddSVGGlyph() {
        this.setUrl("/fx-svg/add.svg");
    }

    public AddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.add());
//        super.initNode();
//    }
}
