package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ClearSVGGlyph extends SVGGlyph {

    public ClearSVGGlyph() {
        this.setUrl("/fx-plus/font/clear2.svg");
    }

    public ClearSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.clear());
        super.initNode();
    }
}
