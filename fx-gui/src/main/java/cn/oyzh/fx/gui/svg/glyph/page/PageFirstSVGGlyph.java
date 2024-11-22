package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class PageFirstSVGGlyph extends SVGGlyph {

    public PageFirstSVGGlyph() {
        this.setUrl("/fx-svg/page/page-first.svg");
    }

    public PageFirstSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.firstPage());
        super.initNode();
    }
}
