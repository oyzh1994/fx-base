package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/01
 */
public class DesignSVGGlyph extends SVGGlyph {

    public DesignSVGGlyph() {
        this.setUrl("/fx-plus/font/design.svg");
    }

    public DesignSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.design());
        super.initNode();
    }
}
