package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class NextSVGGlyph extends SVGGlyph {

    public NextSVGGlyph() {
        this.setUrl("/fx-svg/direction-down.svg");
    }

    public NextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.next());
//        super.initNode();
//    }
}
