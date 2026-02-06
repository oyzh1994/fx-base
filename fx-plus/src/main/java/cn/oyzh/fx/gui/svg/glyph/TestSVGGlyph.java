package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TestSVGGlyph extends SVGGlyph {

    public TestSVGGlyph() {
        this.setUrl("/fx-svg/link.svg");
    }

    public TestSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.test());
//        super.initNode();
//    }
}
