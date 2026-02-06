package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class ErrorInfoSVGGlyph extends SVGGlyph {

    public ErrorInfoSVGGlyph() {
        this.setUrl("/fx-svg/error-info.svg");
    }

    public ErrorInfoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.file());
//        super.initNode();
//    }
}
