package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/07/15
 */
public class ReduceSVGGlyph extends SVGGlyph {

    public ReduceSVGGlyph() {
        this.setUrl("/fx-plus/font/reduce2.svg");
    }

    public ReduceSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.reduce());
        super.initNode();
    }
}
