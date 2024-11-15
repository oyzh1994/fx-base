package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpendSVGGlyph extends SVGGlyph {

    public ExpendSVGGlyph() {
        this.setUrl("/fx-plus/font/arrow-to-right.svg");
    }

    public ExpendSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.expand());
        super.initNode();
    }
}
