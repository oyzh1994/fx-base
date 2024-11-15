package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/12
 */
public class CollapseAllSVGGlyph extends SVGGlyph {

    public CollapseAllSVGGlyph() {
        this.setUrl("/fx-plus/font/vertical-align-middl.svg");
    }

    public CollapseAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.collapseAll());
        super.initNode();
    }
}
