package cn.oyzh.fx.gui.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class EditDocumentSVGGlyph extends SVGGlyph {

    public EditDocumentSVGGlyph() {
        super("/fx-svg/edit_document.svg");
    }

    public EditDocumentSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

//    @Override
//    public void initNode() {
//        this.setTipText(I18nHelper.edit());
//        super.initNode();
//    }
}
