package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ReverseSVGGlyph extends SVGGlyph {

    public ReverseSVGGlyph() {
        this.setUrl("/fx-plus/font/reverse.svg");
    }

    public ReverseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.reverse());
        super.initNode();
    }
}
