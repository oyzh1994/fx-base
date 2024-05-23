package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ResetSVGGlyph extends SVGGlyph {

    public ResetSVGGlyph() {
        this.setUrl("/fx-plus/font/reset.svg");
    }

    public ResetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.reset());
        super.initNode();
    }
}
