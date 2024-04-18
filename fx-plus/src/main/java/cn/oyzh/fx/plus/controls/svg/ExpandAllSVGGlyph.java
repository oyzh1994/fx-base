package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/10
 */
public class ExpandAllSVGGlyph extends SVGGlyph {

    public ExpandAllSVGGlyph() {
        this.setUrl("/fx-plus/font/colum-height.svg");
    }

    public ExpandAllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("expandAll"));
        super.initNode();
    }
}
