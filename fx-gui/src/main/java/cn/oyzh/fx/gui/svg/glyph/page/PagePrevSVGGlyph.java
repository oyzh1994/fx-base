package cn.oyzh.fx.gui.svg.glyph.page;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * @author oyzh
 * @since 2024/08/06
 */
public class PagePrevSVGGlyph extends SVGGlyph {

    public PagePrevSVGGlyph() {
        this.setUrl("/fx-plus/font/page/page-prev.svg");
    }

    public PagePrevSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public void initNode() {
        this.setTipText(I18nHelper.prevPage());
        super.initNode();
    }
}
