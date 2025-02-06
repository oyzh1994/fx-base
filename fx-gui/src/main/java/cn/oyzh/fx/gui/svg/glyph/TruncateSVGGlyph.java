package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/07260
 */
public class TruncateSVGGlyph extends SVGGlyph {

    public TruncateSVGGlyph() {
        this.setUrl("/fx-svg/truncate.svg");
    }

    public TruncateSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.truncate());
//        super.initNode();
//    }
}
