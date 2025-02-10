package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class TransportSVGGlyph extends SVGGlyph {

    public TransportSVGGlyph() {
        this.setUrl("/fx-svg/arrow-left-right-line.svg");
    }

    public TransportSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.transport());
//        super.initNode();
//    }
}
