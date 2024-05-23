package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MoveSVGGlyph extends SVGGlyph {

    public MoveSVGGlyph() {
        this.setUrl("/fx-plus/font/move.svg");
    }

    public MoveSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.move());
        super.initNode();
    }
}
