package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PrevSVGGlyph extends SVGGlyph {

    public PrevSVGGlyph() {
        this.setUrl("/fx-svg/direction-up.svg");
    }

    public PrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.prev());
//        super.initNode();
//    }
}
