package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/26
 */
public class NextStepSVGGlyph extends SVGGlyph {

    public NextStepSVGGlyph() {
        this.setUrl("/fx-plus/font/next-step.svg");
    }

    public NextStepSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.nextStep());
        super.initNode();
    }
}
