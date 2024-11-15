package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class PrevSVGGlyph extends SVGGlyph {

    public PrevSVGGlyph() {
        this.setUrl("/fx-plus/font/direction-up.svg");
    }

    public PrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.prev());
        super.initNode();
    }
}
