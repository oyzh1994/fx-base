package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class RefreshSVGGlyph extends SVGGlyph {

    public RefreshSVGGlyph() {
        this.setUrl("/fx-svg/reload.svg");
    }

    public RefreshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.refresh());
//        super.initNode();
//    }
}
