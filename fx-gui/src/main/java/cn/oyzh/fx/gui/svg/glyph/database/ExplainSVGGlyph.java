package cn.oyzh.fx.gui.svg.glyph.database;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/16
 */
public class ExplainSVGGlyph extends SVGGlyph {

    public ExplainSVGGlyph() {
        this.setUrl("/fx-svg/database/explain.svg");
    }

    public ExplainSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.explain());
//        super.initNode();
//    }
}
