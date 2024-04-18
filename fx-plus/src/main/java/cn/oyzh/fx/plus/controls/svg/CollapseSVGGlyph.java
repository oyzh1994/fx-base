package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class CollapseSVGGlyph extends SVGGlyph {

    public CollapseSVGGlyph() {
        this.setUrl("/fx-plus/font/left-arrow-to-left.svg");
    }

    public CollapseSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.collapse"));
        super.initNode();
    }
}
