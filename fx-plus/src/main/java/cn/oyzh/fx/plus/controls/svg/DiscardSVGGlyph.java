package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class DiscardSVGGlyph extends SVGGlyph {

    public DiscardSVGGlyph() {
        this.setUrl("/fx-plus/font/close.svg");
    }

    public DiscardSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.discard());
        super.initNode();
    }
}
