package cn.oyzh.fx.plus.controls.svg;

import cn.oyzh.fx.plus.i18n.I18nResourceBundle;

/**
 * @author oyzh
 * @since 2024/4/11
 */
public class TestSVGGlyph extends SVGGlyph {

    public TestSVGGlyph() {
        this.setUrl("/fx-plus/font/link.svg");
    }

    public TestSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nResourceBundle.i18nString("base.test"));
        super.initNode();
    }
}
