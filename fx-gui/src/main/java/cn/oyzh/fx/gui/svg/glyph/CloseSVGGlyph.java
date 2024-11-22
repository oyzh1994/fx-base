package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CloseSVGGlyph extends SVGGlyph {

    public CloseSVGGlyph() {
        this.setUrl("/fx-svg/close.svg");
    }

    public CloseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.close());
        super.initNode();
    }
}
