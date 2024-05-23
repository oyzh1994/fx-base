package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class EditSVGGlyph extends SVGGlyph {

    public EditSVGGlyph() {
        this.setUrl("/fx-plus/font/edit.svg");
    }

    public EditSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.edit());
        super.initNode();
    }
}
