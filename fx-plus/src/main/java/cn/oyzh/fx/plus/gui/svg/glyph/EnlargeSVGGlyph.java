package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/7/9
 */
public class EnlargeSVGGlyph extends SVGGlyph {

    public EnlargeSVGGlyph() {
        this.setUrl("/fx-plus/font/enlarge.svg");
    }

    public EnlargeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.edit());
        super.initNode();
    }
}
