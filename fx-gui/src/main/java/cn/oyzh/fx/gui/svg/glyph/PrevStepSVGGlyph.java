package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class PrevStepSVGGlyph extends SVGGlyph {

    public PrevStepSVGGlyph() {
        this.setUrl("/fx-plus/font/prev-step.svg");
    }

    public PrevStepSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.prevStep());
        super.initNode();
    }
}
