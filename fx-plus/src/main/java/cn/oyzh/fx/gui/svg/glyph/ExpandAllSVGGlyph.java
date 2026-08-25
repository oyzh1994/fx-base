package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandAllSVGGlyph extends SVGGlyph {

    public ExpandAllSVGGlyph() {
        super("/fx-svg/colum-height.svg");
    }

    public ExpandAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.expandAll());
//        super.initNode();
//    }
}
