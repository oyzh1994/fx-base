package cn.oyzh.fx.plus.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class RightSideSVGGlyph extends SVGGlyph {

    public RightSideSVGGlyph() {
        this.setUrl("/fx-plus/font/rightside.svg");
    }

    public RightSideSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.add());
        super.initNode();
    }
}
