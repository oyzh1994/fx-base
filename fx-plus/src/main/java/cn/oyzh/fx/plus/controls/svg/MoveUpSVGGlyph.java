package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class MoveUpSVGGlyph extends SVGGlyph {

    public MoveUpSVGGlyph() {
        this.setUrl("/fx-plus/font/direction-up.svg");
    }

    public MoveUpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.moveUp());
        super.initNode();
    }
}
