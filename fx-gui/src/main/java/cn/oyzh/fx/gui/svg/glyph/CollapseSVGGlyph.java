package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CollapseSVGGlyph extends SVGGlyph {

    public CollapseSVGGlyph() {
        this.setUrl("/fx-plus/font/left-arrow-to-left.svg");
    }

    public CollapseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.collapse());
        super.initNode();
    }
}
