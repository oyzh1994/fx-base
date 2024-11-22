package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/09/05
 */
public class RightSideSVGGlyph extends SVGGlyph {

    public RightSideSVGGlyph() {
        this.setUrl("/fx-gui/font/rightside.svg");
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
