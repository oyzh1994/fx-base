package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class StopSVGGlyph extends SVGGlyph {

    public StopSVGGlyph() {
        this.setUrl("/fx-svg/stop-circle-line.svg");
    }

    public StopSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.stop());
//        super.initNode();
//    }
}
