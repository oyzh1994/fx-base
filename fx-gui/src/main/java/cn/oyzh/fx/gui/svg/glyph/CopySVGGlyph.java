package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class CopySVGGlyph extends SVGGlyph {

    public CopySVGGlyph() {
        this.setUrl("/fx-plus/font/copy.svg");
    }

    public CopySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.copy());
        super.initNode();
    }
}
