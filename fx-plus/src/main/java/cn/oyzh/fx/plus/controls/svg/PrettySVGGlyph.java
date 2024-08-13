package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/13
 */
public class PrettySVGGlyph extends SVGGlyph {

    public PrettySVGGlyph() {
        this.setUrl("/fx-plus/font/pretty.svg");
    }

    public PrettySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.pretty());
        super.initNode();
    }
}
